package cabofactu.modelo.negocio.sqlite;

import cabofactu.fichero.CopiaSeguridad;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Todo lo que toca SQLite en las copias: crearlas, leerlas y comprobarlas,
 * y sustituir o instalar bases a partir de ellas.
 */
public class CopiaSeguridadDAO {

    private static final List<String> TABLAS_NUCLEO = List.of(
            "cliente", "serie", "tipo_iva", "factura",
            "factura_linea", "empresa", "preferencias"
    );

    private static final List<String> TABLAS_APLICACION = List.of(
            "cliente", "serie", "tipo_iva", "factura",
            "factura_linea", "empresa", "preferencias",
            "tipo_retencion"
    );

    private static final Map<String, List<String>> COLUMNAS_APLICACION = Map.ofEntries(
            Map.entry("cliente", List.of("id", "nombre", "nif", "direccion", "cp", "localidad", "provincia", "activo", "email")),
            Map.entry("serie", List.of("id", "codigo", "descripcion", "es_rectificativa", "sufijo_fecha")),
            Map.entry("tipo_iva", List.of("id", "nombre", "porcentaje", "motivo_exencion", "activo", "es_suplido")),
            Map.entry("factura", List.of("id", "serie_id", "anio", "correlativo", "numero", "fecha", "estado",
                    "cliente_id", "cli_nombre", "cli_nif", "cli_direccion", "cli_cp", "cli_localidad",
                    "cli_provincia", "cli_email", "descuento", "observaciones", "rectifica_id", "forma_pago",
                    "vencimiento", "realizada_por", "retencion_id", "retencion_nombre", "retencion_porcentaje",
                    "base_total", "iva_total", "importe_retencion", "total_suplidos", "total")),
            Map.entry("factura_linea", List.of("id", "factura_id", "orden", "cantidad", "descripcion", "precio_unitario",
                    "tipo_iva_id", "iva_nombre", "iva_porcentaje", "iva_motivo_exencion", "es_suplido")),
            Map.entry("empresa", List.of("id", "nombre", "nif", "direccion", "cp", "localidad", "provincia", "actividad", "email",
                    "telefono", "cabecera_modo", "logo_path", "pie_legal")),
            Map.entry("preferencias", List.of("clave", "valor")),
            Map.entry("tipo_retencion", List.of("id", "nombre", "porcentaje", "activo"))
    );

    /** Genera la copia de la base activa en el archivo indicado. */
    public void crearCopia(Path archivo) {
        String ruta = archivo.toString().replace("'", "''");
        try (Statement st = Conexion.establecerConexion().createStatement()) {
            st.execute("VACUUM INTO '" + ruta + "'");
        } catch (SQLException e) {
            throw new DatosException(e);
        }
    }

    /** Lee y comprueba una copia, devolviendo su resumen. */
    public CopiaSeguridad.ResumenCopia leerResumen(Path origen) throws Exception {
        try (Connection c = DriverManager.getConnection("jdbc:sqlite:" + origen)) {
            try (Statement st = c.createStatement();
                 ResultSet rs = st.executeQuery("PRAGMA quick_check")) {
                if (rs.next() && !"ok".equals(rs.getString(1))) {
                    throw new Exception("El archivo no es una base de datos SQLite válida.");
                }
            }

            comprobarTablasNucleo(c);

            List<String> faltantes = elementosFaltantes(c);
            if (!faltantes.isEmpty()) {
                throw new Exception("La copia no contiene "
                        + String.join(", ", faltantes) + ".");
            }
            String nombre = "";
            String nif = "";
            String logoPath = "";
            try (Statement st = c.createStatement();
                 ResultSet rs = st.executeQuery(
                         "SELECT nombre, nif, logo_path FROM empresa WHERE id = 1")) {
                if (rs.next()) {
                    nombre = rs.getString("nombre") == null ? "" : rs.getString("nombre");
                    nif = rs.getString("nif") == null ? "" : rs.getString("nif");
                    logoPath = rs.getString("logo_path") == null ? "" : rs.getString("logo_path");
                }
            }

            int numFacturas;
            try (Statement st = c.createStatement();
                 ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM factura")) {
                numFacturas = rs.next() ? rs.getInt(1) : 0;
            }

            LocalDate ultimaFecha = null;
            try (Statement st = c.createStatement();
                 ResultSet rs = st.executeQuery("SELECT MAX(fecha) FROM factura")) {
                if (rs.next()) {
                    String fecha = rs.getString(1);
                    if (fecha != null && !fecha.isBlank()) {
                        ultimaFecha = LocalDate.parse(fecha);
                    }
                }
            }

            boolean logoExiste = !logoPath.isBlank() && Files.exists(Path.of(logoPath));

            return new CopiaSeguridad.ResumenCopia(nombre, nif, logoPath, logoExiste,
                    numFacturas, ultimaFecha);
        } catch (SQLException e) {
            throw new Exception("No se pudo leer la copia: " + e.getMessage());
        }
    }

    /** Sustituye la base activa por la copia indicada. */
    public void reemplazarBaseActiva(Path origen) throws IOException {
        Conexion.cerrarConexion();
        try {
            Files.copy(origen, Conexion.rutaBase(), StandardCopyOption.REPLACE_EXISTING);
            borrarDiario(Conexion.carpetaEmpresa());
            Conexion.establecerConexion();
        } catch (SQLException e) {
            throw new DatosException(e);
        }
    }

    /** Instala la copia como base en la ruta de destino. */
    public void instalarComoBase(Path origen, Path destinoDb) throws IOException {
        Files.copy(origen, destinoDb, StandardCopyOption.REPLACE_EXISTING);
        borrarDiario(destinoDb.getParent());
    }

    private static void borrarDiario(Path carpeta) throws IOException {
        String base = Conexion.rutaBase().getFileName().toString();
        Files.deleteIfExists(carpeta.resolve(base + "-wal"));
        Files.deleteIfExists(carpeta.resolve(base + "-shm"));
    }

    private static void comprobarTablasNucleo(Connection c) throws SQLException, Exception {
        for (String tabla : TABLAS_NUCLEO) {
            try (Statement st = c.createStatement();
                 ResultSet rs = st.executeQuery(
                         "SELECT name FROM sqlite_master WHERE type='table' AND name='" + tabla + "'")) {
                if (!rs.next()) {
                    throw new Exception("Falta la tabla '" + tabla + "' en la copia.");
                }
            }
        }
    }

    private static List<String> elementosFaltantes(Connection c) throws SQLException {
        List<String> faltantes = new ArrayList<>();
        for (String tabla : TABLAS_APLICACION) {
            boolean existe = false;
            try (Statement st = c.createStatement();
                 ResultSet rs = st.executeQuery(
                         "SELECT name FROM sqlite_master WHERE type='table' AND name='" + tabla + "'")) {
                existe = rs.next();
            }
            if (!existe) {
                faltantes.add("la tabla '" + tabla + "'");
            }
        }
        for (Map.Entry<String, List<String>> e : COLUMNAS_APLICACION.entrySet()) {
            String tabla = e.getKey();
            Set<String> columnas = new HashSet<>();
            try (Statement st = c.createStatement();
                 ResultSet rs = st.executeQuery("PRAGMA table_info(" + tabla + ")")) {
                while (rs.next()) {
                    columnas.add(rs.getString("name"));
                }
            }
            for (String col : e.getValue()) {
                if (!columnas.contains(col)) {
                    faltantes.add("la columna '" + col + "' de '" + tabla + "'");
                }
            }
        }
        return faltantes;
    }
}
