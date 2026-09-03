package com.alcazaba.facturacion.service;

import com.alcazaba.facturacion.db.Database;
import com.alcazaba.facturacion.db.Migrations;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Copias de seguridad de la base de datos SQLite mediante VACUUM INTO, que
 * produce una copia consistente en caliente. El archivo lleva un timestamp
 * (facturas_AAAAMMDD_HHMMSS.db).
 */
public class BackupService {

    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

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
            Map.entry("tipo_iva", List.of("id", "nombre", "porcentaje", "motivo_exencion", "activo")),
            Map.entry("factura", List.of("id", "serie_id", "correlativo", "cliente_id")),
            Map.entry("factura_version", List.of("id", "factura_id", "version_num", "numero", "fecha_factura", "fecha_guardado", "estado",
                    "descuento_porcentaje", "observaciones", "referencia_rectifica", "cli_nombre", "cli_nif", "cli_direccion",
                    "cli_cp", "cli_localidad", "cli_provincia", "base_total", "iva_total", "total", "cli_email", "forma_pago",
                    "vencimiento", "realizada_por", "tipo_retencion_id", "importe_retencion", "tipo_retencion_nombre", "tipo_retencion_porcentaje")),
            Map.entry("factura_linea", List.of("id", "factura_version_id", "orden", "cantidad", "descripcion", "precio_unitario",
                    "total_base", "tipo_iva_id", "iva_nombre", "iva_porcentaje", "iva_motivo_exencion", "iva_importe")),
            Map.entry("empresa", List.of("id", "nombre", "nif", "direccion", "cp", "localidad", "provincia", "actividad", "email",
                    "telefono", "cabecera_modo", "logo_path", "logo_x", "logo_y", "logo_ancho", "logo_alto", "pie_legal")),
            Map.entry("preferencias", List.of("clave", "valor")),
            Map.entry("serie_siguiente", List.of("serie_id", "anio", "siguiente")),
            Map.entry("tipo_retencion", List.of("id", "nombre", "porcentaje", "activo")),
            Map.entry("numero_disponible", List.of("id", "serie_id", "anio", "correlativo"))
    );

    public record ResumenBackup(
            String nombreEmpresa,
            String nif,
            String logoPath,
            boolean logoExiste,
            int numFacturas,
            LocalDate ultimaFecha,
            int userVersion,
            boolean tablasCoinciden
    ) {
    }

    /**
     * Crea la copia en la carpeta indicada y devuelve la ruta generada.
     */
    public Path crearBackup(Path carpetaDestino) throws SQLException, IOException {
        Files.createDirectories(carpetaDestino);
        String nombre = "facturas_" + LocalDateTime.now().format(STAMP);
        Path archivo = rutaLibre(carpetaDestino, nombre);

        String ruta = archivo.toString().replace("'", "''");
        try (Statement st = Database.getConnection().createStatement()) {
            st.execute("VACUUM INTO '" + ruta + "'");
        }
        return archivo;
    }

    static Path rutaLibre(Path carpeta, String base) {
        Path primero = carpeta.resolve(base + ".db");
        if (!Files.exists(primero)) {
            return primero;
        }
        for (int i = 2; ; i++) {
            Path candidato = carpeta.resolve(base + "_" + i + ".db");
            if (!Files.exists(candidato)) {
                return candidato;
            }
        }
    }

    public ResumenBackup leerResumen(Path origen) throws ValidationException {
        if (origen == null || !Files.isRegularFile(origen) || !Files.isReadable(origen)) {
            throw new ValidationException("El archivo seleccionado no es un archivo legible.");
        }
        if (origen.toAbsolutePath().normalize().equals(Database.dbPath().toAbsolutePath().normalize())) {
            throw new ValidationException("No se puede usar la propia base activa como origen de restauración.");
        }
        try (Connection c = DriverManager.getConnection("jdbc:sqlite:" + origen)) {
            try (Statement st = c.createStatement();
                 ResultSet rs = st.executeQuery("PRAGMA quick_check")) {
                if (rs.next() && !"ok".equals(rs.getString(1))) {
                    throw new ValidationException("El archivo no es una base de datos SQLite válida.");
                }
            }

            comprobarTablasNucleo(c);

            int uv;
            try (Statement st = c.createStatement();
                 ResultSet rs = st.executeQuery("PRAGMA user_version")) {
                uv = rs.next() ? rs.getInt(1) : 0;
            }
            if (uv <= 0) {
                throw new ValidationException("La copia no tiene una versión de esquema válida.");
            }

            boolean tablasCoinciden = true;
            if (uv > Migrations.ultimaVersion()) {
                tablasCoinciden = estructuraCompleta(c);
                if (!tablasCoinciden) {
                    throw new ValidationException("La copia es de una versión de esquema más nueva ("
                            + uv + ") que la aplicación (" + Migrations.ultimaVersion()
                            + ") y su estructura no coincide.");
                }
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

            return new ResumenBackup(nombre, nif, logoPath, logoExiste,
                    numFacturas, ultimaFecha, uv, tablasCoinciden);
        } catch (SQLException e) {
            throw new ValidationException("No se pudo leer la copia: " + e.getMessage());
        }
    }

    public Path restaurarEnEmpresaActiva(Path origen) throws IOException, SQLException, ValidationException {
        leerResumen(origen);

        Path carpetaRescate = Database.dataDir().resolve("copias_previas");
        Path rescate = crearBackup(carpetaRescate);

        Database.resetConnection();

        try {
            Files.copy(origen, Database.dbPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            borrarDiario(Database.dataDir());
            Database.getConnection();
        } catch (Exception e) {
            Database.resetConnection();
            Files.copy(rescate, Database.dbPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            borrarDiario(Database.dataDir());
            Database.getConnection();
            throw new IOException("No se pudo restaurar; se ha recuperado la base anterior: " + e.getMessage(), e);
        }

        return rescate;
    }

    public EmpresaManager.EmpresaInfo restaurarComoEmpresaNueva(Path origen, String nombre)
            throws IOException, ValidationException {
        leerResumen(origen);

        EmpresaManager.EmpresaInfo nueva = null;
        try {
            nueva = EmpresaManager.crearEmpresa(nombre);

            Path destino = Database.dbPathDe(nueva.slug());
            Files.copy(origen, destino, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            borrarDiario(destino.getParent());

            try (Connection c = DriverManager.getConnection("jdbc:sqlite:" + destino)) {
                Migrations.migrate(c);
            }
        } catch (Exception e) {
            if (nueva != null) {
                try {
                    EmpresaManager.eliminarEmpresa(nueva.slug());
                } catch (Exception ignored) {
                }
            }
            throw new IOException("No se pudo crear la empresa desde la copia: " + e.getMessage(), e);
        }

        return nueva;
    }

    private static void borrarDiario(Path carpeta) throws IOException {
        String base = Database.dbPath().getFileName().toString();
        Files.deleteIfExists(carpeta.resolve(base + "-wal"));
        Files.deleteIfExists(carpeta.resolve(base + "-shm"));
    }

    private static void comprobarTablasNucleo(Connection c) throws SQLException, ValidationException {
        for (String tabla : TABLAS_NUCLEO) {
            try (Statement st = c.createStatement();
                 ResultSet rs = st.executeQuery(
                         "SELECT name FROM sqlite_master WHERE type='table' AND name='" + tabla + "'")) {
                if (!rs.next()) {
                    throw new ValidationException("Falta la tabla '" + tabla + "' en la copia.");
                }
            }
        }
    }

    private static boolean estructuraCompleta(Connection c) throws SQLException {
        for (String tabla : TABLAS_APLICACION) {
            boolean existe = false;
            try (Statement st = c.createStatement();
                 ResultSet rs = st.executeQuery(
                         "SELECT name FROM sqlite_master WHERE type='table' AND name='" + tabla + "'")) {
                existe = rs.next();
            }
            if (!existe) {
                return false;
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
                    return false;
                }
            }
        }
        return true;
    }
}
