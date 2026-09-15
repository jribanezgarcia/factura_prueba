package cabofactu.modelo.negocio.sqlite;

import cabofactu.fichero.CopiaSeguridad;
import cabofactu.modelo.negocio.ValidacionException;

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
            "cliente", "serie", "tipo_iva", "factura", "factura_version",
            "factura_linea", "empresa", "preferencias"
    );

    private static final List<String> TABLAS_APLICACION = List.of(
            "cliente", "serie", "tipo_iva", "factura", "factura_version",
            "factura_linea", "empresa", "preferencias", "serie_siguiente",
            "tipo_retencion", "numero_disponible"
    );

    private static final Map<String, List<String>> COLUMNAS_APLICACION = Map.ofEntries(
            Map.entry("cliente", List.of("id", "nombre", "nif", "direccion", "cp", "localidad", "provincia", "activo", "email")),
            Map.entry("serie", List.of("id", "codigo", "descripcion", "es_rectificativa", "siguiente_correlativo", "reutilizar_anulados", "sufijo_fecha")),
            Map.entry("tipo_iva", List.of("id", "nombre", "porcentaje", "motivo_exencion", "activo", "es_suplido")),
            Map.entry("factura", List.of("id", "serie_id", "correlativo", "cliente_id")),
            Map.entry("factura_version", List.of("id", "factura_id", "version_num", "numero", "fecha_factura", "fecha_guardado", "estado",
                    "descuento_porcentaje", "observaciones", "referencia_rectifica", "cli_nombre", "cli_nif", "cli_direccion",
                    "cli_cp", "cli_localidad", "cli_provincia", "base_total", "iva_total", "total", "cli_email", "forma_pago",
                    "vencimiento", "realizada_por", "tipo_retencion_id", "importe_retencion", "tipo_retencion_nombre", "tipo_retencion_porcentaje",
                    "total_suplidos")),
            Map.entry("factura_linea", List.of("id", "factura_version_id", "orden", "cantidad", "descripcion", "precio_unitario",
                    "total_base", "tipo_iva_id", "iva_nombre", "iva_porcentaje", "iva_motivo_exencion", "iva_importe", "es_suplido")),
            Map.entry("empresa", List.of("id", "nombre", "nif", "direccion", "cp", "localidad", "provincia", "actividad", "email",
                    "telefono", "cabecera_modo", "logo_path", "logo_x", "logo_y", "logo_ancho", "logo_alto", "pie_legal")),
            Map.entry("preferencias", List.of("clave", "valor")),
            Map.entry("serie_siguiente", List.of("serie_id", "anio", "siguiente")),
            Map.entry("tipo_retencion", List.of("id", "nombre", "porcentaje", "activo")),
            Map.entry("numero_disponible", List.of("id", "serie_id", "anio", "correlativo"))
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
    public CopiaSeguridad.ResumenCopia leerResumen(Path origen) throws ValidacionException {
        try (Connection c = DriverManager.getConnection("jdbc:sqlite:" + origen)) {
            try (Statement st = c.createStatement();
                 ResultSet rs = st.executeQuery("PRAGMA quick_check")) {
                if (rs.next() && !"ok".equals(rs.getString(1))) {
                    throw new ValidacionException("El archivo no es una base de datos SQLite válida.");
                }
            }

            comprobarTablasNucleo(c);

            int uv;
            try (Statement st = c.createStatement();
                 ResultSet rs = st.executeQuery("PRAGMA user_version")) {
                uv = rs.next() ? rs.getInt(1) : 0;
            }
            if (uv <= 0) {
                throw new ValidacionException("La copia no tiene una versión de esquema válida.");
            }

            List<String> faltantes = elementosFaltantes(c);
            if (!faltantes.isEmpty()) {
                throw new ValidacionException("La copia no contiene "
                        + String.join(", ", faltantes) + "." + notaVersion(uv));
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
                 ResultSet rs = st.executeQuery("SELECT MAX(fecha_factura) FROM factura_version")) {
                if (rs.next()) {
                    String fecha = rs.getString(1);
                    if (fecha != null && !fecha.isBlank()) {
                        ultimaFecha = LocalDate.parse(fecha);
                    }
                }
            }

            boolean logoExiste = !logoPath.isBlank() && Files.exists(Path.of(logoPath));

            return new CopiaSeguridad.ResumenCopia(nombre, nif, logoPath, logoExiste,
                    numFacturas, ultimaFecha, uv);
        } catch (SQLException e) {
            throw new ValidacionException("No se pudo leer la copia: " + e.getMessage());
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
        Conexion.migrarBase(destinoDb);
    }

    private static void borrarDiario(Path carpeta) throws IOException {
        String base = Conexion.rutaBase().getFileName().toString();
        Files.deleteIfExists(carpeta.resolve(base + "-wal"));
        Files.deleteIfExists(carpeta.resolve(base + "-shm"));
    }

    private static void comprobarTablasNucleo(Connection c) throws SQLException, ValidacionException {
        for (String tabla : TABLAS_NUCLEO) {
            try (Statement st = c.createStatement();
                 ResultSet rs = st.executeQuery(
                         "SELECT name FROM sqlite_master WHERE type='table' AND name='" + tabla + "'")) {
                if (!rs.next()) {
                    throw new ValidacionException("Falta la tabla '" + tabla + "' en la copia.");
                }
            }
        }
    }

    private static String notaVersion(int uv) {
        int app = Migraciones.ultimaVersion();
        if (uv < app) {
            return " La versión de esquema de la copia (" + uv + ") es anterior a la de la aplicación (" + app + ").";
        }
        if (uv > app) {
            return " La versión de esquema de la copia (" + uv + ") es posterior a la de la aplicación (" + app + ").";
        }
        return "";
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
