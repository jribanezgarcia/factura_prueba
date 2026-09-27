package cabofactu.modelo.negocio;

import cabofactu.modelo.dominio.Empresa;
import cabofactu.modelo.dominio.EmpresaDisponible;
import cabofactu.modelo.dominio.ResumenCopia;
import cabofactu.modelo.negocio.sqlite.Conexion;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Las copias de seguridad de la empresa activa: crearlas, leerlas, comprobar
 * su estructura contra el script de tablas y restaurarlas, sobre la propia
 * empresa o como una empresa nueva.
 */
public class CopiaSeguridad {

    private static final String CARPETA_RESCATE = "copias_previas";
    private static final DateTimeFormatter FORMATO_NOMBRE = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private static CopiaSeguridad copiaSeguridad;

    private CopiaSeguridad() {
    }

    public static CopiaSeguridad getCopiaSeguridad() {
        if (copiaSeguridad == null) {
            copiaSeguridad = new CopiaSeguridad();
        }
        return copiaSeguridad;
    }

    /**
     * Creamos la copia en la carpeta indicada, la recordamos para la próxima
     * vez y devolvemos la ruta del archivo generado.
     */
    public Path crear(Path carpeta) throws Exception {
        if (carpeta == null) {
            throw new Exception("Elija la carpeta de la copia.");
        }
        Path archivo = copiarEn(carpeta);
        PreferenciasGlobales.set(PreferenciasGlobales.CARPETA_COPIAS, carpeta.toString());
        return archivo;
    }

    /** La carpeta recordada para las copias, o null si no hay ninguna o ya no existe. */
    public Path carpetaCopias() {
        String carpeta = PreferenciasGlobales.get(PreferenciasGlobales.CARPETA_COPIAS);
        if (carpeta == null || carpeta.isBlank()) {
            return null;
        }
        Path ruta = Path.of(carpeta);
        if (!Files.isDirectory(ruta)) {
            return null;
        }
        return ruta;
    }

    /** Leemos y comprobamos una copia, devolviendo su resumen. */
    public ResumenCopia leer(Path origen) throws Exception {
        if (origen == null || !Files.isRegularFile(origen) || !Files.isReadable(origen)) {
            throw new Exception("El archivo elegido no se puede leer.");
        }
        if (origen.toAbsolutePath().normalize().equals(Conexion.rutaBase().toAbsolutePath().normalize())) {
            throw new Exception("No se puede restaurar la base de la empresa activa sobre sí misma.");
        }
        try (Connection copia = DriverManager.getConnection("jdbc:sqlite:" + origen)) {
            comprobarIntegridad(copia);
            comprobarEstructura(copia);
            return crearResumen(copia);
        } catch (SQLException e) {
            throw new Exception("El archivo no es una base de datos válida.");
        }
    }

    /** True si la empresa activa tiene el mismo NIF que la copia, así que se puede reemplazar. */
    public boolean puedeReemplazar(ResumenCopia resumen) throws Exception {
        Empresa activa = Configuracion.getConfiguracion().buscarEmpresa();
        if (activa == null) {
            return false;
        }
        String nifActiva = "";
        if (activa.getNif() != null) {
            nifActiva = activa.getNif().trim();
        }
        return nifActiva.equalsIgnoreCase(resumen.getNif().trim());
    }

    /** Sustituimos la empresa activa por los datos de la copia, guardando antes una de rescate. */
    public Path restaurar(Path origen) throws Exception {
        ResumenCopia resumen = leer(origen);
        if (!puedeReemplazar(resumen)) {
            throw new Exception(String.format(
                    "La copia es de otra empresa (NIF %s). Restáurela como empresa nueva.", resumen.getNif()));
        }
        Path carpetaRescate = Conexion.carpetaEmpresa().resolve(CARPETA_RESCATE);
        Path rescate = copiarEn(carpetaRescate);
        try {
            sustituirBase(origen);
        } catch (Exception e) {
            sustituirBase(rescate);
            throw new Exception("No se pudo restaurar la copia; se ha recuperado la base anterior: " + e.getMessage());
        }
        Empresas.getEmpresas().recordarTema();
        return rescate;
    }

    /** Creamos una empresa nueva con los datos de la copia, sin tocar la empresa activa. */
    public EmpresaDisponible restaurarComoEmpresa(Path origen, String nombre) throws Exception {
        leer(origen);
        EmpresaDisponible nueva = Empresas.getEmpresas().alta(nombre);
        try {
            Path destino = Conexion.rutaBaseDe(nueva.getCarpeta());
            Files.copy(origen, destino, StandardCopyOption.REPLACE_EXISTING);
            borrarDiario(destino.getParent());
        } catch (Exception e) {
            // Si tampoco se puede dar de baja, enseñamos el error de la copia, que es el que importa.
            try {
                Empresas.getEmpresas().baja(nueva.getCarpeta());
            } catch (Exception ignorada) {
            }
            throw new Exception("No se pudo crear la empresa desde la copia: " + e.getMessage());
        }
        return nueva;
    }

    /**
     * Copiamos la base activa a un archivo nuevo de la carpeta, sin recordar
     * la carpeta: así la copia de rescate no cambia la de las copias.
     */
    private Path copiarEn(Path carpeta) throws Exception {
        try {
            Files.createDirectories(carpeta);
        } catch (IOException e) {
            throw new Exception("No se pudo crear la carpeta: " + e.getMessage());
        }
        String nombre = Sesion.getSesion().getCarpetaEmpresa() + "_" + LocalDateTime.now().format(FORMATO_NOMBRE);
        Path archivo = rutaLibre(carpeta, nombre);
        String ruta = archivo.toString().replace("'", "''");
        String vacuum = "VACUUM INTO '" + ruta + "'";
        try (Statement sentencia = Conexion.establecerConexion().createStatement()) {
            sentencia.execute(vacuum);
        } catch (SQLException e) {
            throw new Exception("Error SQLite: " + e.getMessage());
        }
        return archivo;
    }

    private Path rutaLibre(Path carpeta, String nombre) {
        Path primero = carpeta.resolve(nombre + ".db");
        if (!Files.exists(primero)) {
            return primero;
        }
        int contador = 2;
        Path candidato = carpeta.resolve(nombre + "_" + contador + ".db");
        while (Files.exists(candidato)) {
            contador++;
            candidato = carpeta.resolve(nombre + "_" + contador + ".db");
        }
        return candidato;
    }

    /** Comprobamos que el archivo es una base SQLite consistente. */
    private void comprobarIntegridad(Connection copia) throws Exception {
        String consulta = "PRAGMA quick_check";
        try (Statement sentencia = copia.createStatement();
             ResultSet fila = sentencia.executeQuery(consulta)) {
            if (fila.next() && !"ok".equals(fila.getString(1))) {
                throw new Exception("El archivo no es una base de datos válida.");
            }
        }
    }

    /**
     * Comprobamos que la copia tiene todas las tablas y columnas de la
     * aplicación. Cómo funciona: creamos una base vacía en memoria con el
     * script de tablas y la usamos de referencia; no crea ningún archivo y
     * desaparece al cerrarla.
     */
    private void comprobarEstructura(Connection copia) throws Exception {
        try (Connection referencia = DriverManager.getConnection("jdbc:sqlite::memory:")) {
            Conexion.crearTablasSiFaltan(referencia);
            List<String> tablasCopia = tablas(copia);
            List<String> faltan = new ArrayList<>();
            for (String tabla : tablas(referencia)) {
                if (!tablasCopia.contains(tabla)) {
                    faltan.add("la tabla '" + tabla + "'");
                    continue;
                }
                List<String> columnasCopia = columnas(copia, tabla);
                for (String columna : columnas(referencia, tabla)) {
                    if (!columnasCopia.contains(columna)) {
                        faltan.add("la columna '" + columna + "' de '" + tabla + "'");
                    }
                }
            }
            if (!faltan.isEmpty()) {
                throw new Exception("La copia no contiene " + String.join(", ", faltan) + ".");
            }
        } catch (SQLException e) {
            throw new Exception("Error SQLite: " + e.getMessage());
        }
    }

    private List<String> tablas(Connection c) throws Exception {
        String consulta = "SELECT name FROM sqlite_master WHERE type = 'table' AND name NOT LIKE 'sqlite_%'";
        List<String> lista = new ArrayList<>();
        try (Statement sentencia = c.createStatement();
             ResultSet filas = sentencia.executeQuery(consulta)) {
            while (filas.next()) {
                lista.add(filas.getString(1));
            }
        } catch (SQLException e) {
            throw new Exception("Error SQLite: " + e.getMessage());
        }
        return lista;
    }

    private List<String> columnas(Connection c, String tabla) throws Exception {
        String consulta = "SELECT name FROM pragma_table_info(?)";
        List<String> lista = new ArrayList<>();
        try (PreparedStatement sentencia = c.prepareStatement(consulta)) {
            sentencia.setString(1, tabla);
            try (ResultSet filas = sentencia.executeQuery()) {
                while (filas.next()) {
                    lista.add(filas.getString(1));
                }
            }
        } catch (SQLException e) {
            throw new Exception("Error SQLite: " + e.getMessage());
        }
        return lista;
    }

    /** Pasamos la fila de la empresa y el recuento de facturas de la copia a su resumen. */
    private ResumenCopia crearResumen(Connection copia) throws Exception {
        String consultaEmpresa = "SELECT nombre, nif, logo_path FROM empresa WHERE id = 1";
        String nombre = "";
        String nif = "";
        String logo = "";
        try (Statement sentencia = copia.createStatement();
             ResultSet fila = sentencia.executeQuery(consultaEmpresa)) {
            if (fila.next()) {
                nombre = fila.getString("nombre");
                nif = fila.getString("nif");
                logo = fila.getString("logo_path");
            }
        } catch (SQLException e) {
            throw new Exception("Error SQLite: " + e.getMessage());
        }
        String consultaFacturas = "SELECT COUNT(*), MAX(fecha) FROM factura";
        int numeroFacturas = 0;
        LocalDate ultimaFecha = null;
        try (Statement sentencia = copia.createStatement();
             ResultSet fila = sentencia.executeQuery(consultaFacturas)) {
            if (fila.next()) {
                numeroFacturas = fila.getInt(1);
                String fecha = fila.getString(2);
                if (fecha != null && !fecha.isBlank()) {
                    ultimaFecha = LocalDate.parse(fecha);
                }
            }
        } catch (SQLException e) {
            throw new Exception("Error SQLite: " + e.getMessage());
        }
        return new ResumenCopia(nombre, nif, numeroFacturas, ultimaFecha, logo);
    }

    /** Cerramos la conexión, sustituimos la base activa por el archivo indicado y volvemos a conectar. */
    private void sustituirBase(Path origen) throws Exception {
        Conexion.cerrarConexion();
        try {
            Files.copy(origen, Conexion.rutaBase(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new Exception("No se pudo copiar el archivo: " + e.getMessage());
        }
        borrarDiario(Conexion.carpetaEmpresa());
        try {
            Conexion.establecerConexion();
        } catch (SQLException e) {
            throw new Exception("Error SQLite: " + e.getMessage());
        }
    }

    private void borrarDiario(Path carpeta) throws Exception {
        try {
            Files.deleteIfExists(carpeta.resolve("facturas.db-wal"));
            Files.deleteIfExists(carpeta.resolve("facturas.db-shm"));
        } catch (IOException e) {
            throw new Exception("No se pudo borrar el diario: " + e.getMessage());
        }
    }
}
