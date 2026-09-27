package cabofactu.modelo.negocio.sqlite;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Gestionamos la conexión SQLite compartida y la carpeta de datos de la
 * aplicación (%APPDATA%/Facturacion), separada de la instalación. Cada
 * empresa tiene su propia base de datos en una subcarpeta de la raíz.
 */
public final class Conexion {

    private static final String FICHERO_BASE = "facturas.db";
    private static final String SCRIPT_TABLAS = "db/crear_tablas.sql";
    private static Connection conexion;
    private static Path carpetaRaiz = carpetaRaizPorDefecto();
    private static Path carpetaEmpresa = carpetaRaiz;

    private Conexion() {
    }

    private static Path carpetaRaizPorDefecto() {
        String appdata = System.getenv("APPDATA");
        Path base;
        if (appdata != null && !appdata.isBlank()) {
            base = Path.of(appdata);
        } else {
            base = Path.of(System.getProperty("user.home"));
        }
        return base.resolve("Facturacion");
    }

    /** Raíz fija de datos: %APPDATA%/Facturacion (o la carpeta de pruebas). */
    public static Path carpetaRaiz() {
        return carpetaRaiz;
    }

    /** Carpeta de datos de la empresa activa. */
    public static Path carpetaEmpresa() {
        return carpetaEmpresa;
    }

    /**
     * Redirigimos la carpeta de datos (uso en pruebas): fijamos la raíz y la
     * carpeta activa sin empresa. Hay que llamarlo antes de la primera conexión.
     */
    public static void setCarpetaRaiz(Path dir) {
        carpetaRaiz = dir;
        carpetaEmpresa = dir;
    }

    /**
     * Activamos la empresa cuyo slug da nombre a la subcarpeta de datos. La
     * conexión anterior, si existe, queda cerrada.
     */
    public static void setEmpresaActiva(String slug) {
        carpetaEmpresa = carpetaRaiz.resolve(slug);
        cerrarConexion();
    }

    /** Subcarpetas de la raíz de datos que contienen una base de empresa, ordenadas. */
    public static List<String> getEmpresasDisponibles() {
        List<String> lista = new ArrayList<>();
        File[] carpetas = carpetaRaiz.toFile().listFiles();
        if (carpetas == null) {
            return lista;
        }
        for (File carpeta : carpetas) {
            if (carpeta.isDirectory() && new File(carpeta, FICHERO_BASE).exists()) {
                lista.add(carpeta.getName());
            }
        }
        Collections.sort(lista);
        return lista;
    }

    /** Lock de instancia única global, independiente de la empresa activa. */
    public static Path rutaBloqueoGlobal() {
        return carpetaRaiz.resolve("facturas.lock");
    }

    /** Cerramos la conexión activa, si la hay. */
    public static void cerrarConexion() {
        if (conexion != null) {
            try {
                conexion.close();
            } catch (SQLException ignorada) {
            }
        }
        conexion = null;
    }

    public static Path rutaBase() {
        return carpetaEmpresa().resolve(FICHERO_BASE);
    }

    /** Ruta de la base de datos de una empresa cualquiera, sin activarla. */
    public static Path rutaBaseDe(String slug) {
        return carpetaRaiz.resolve(slug).resolve(FICHERO_BASE);
    }

    public static Path rutaBloqueo() {
        return carpetaEmpresa().resolve("facturas.lock");
    }

    /** Abrimos la conexión de la empresa activa, creando su base si hace falta. */
    public static Connection establecerConexion() throws SQLException {
        if (conexion == null || conexion.isClosed()) {
            try {
                Files.createDirectories(carpetaEmpresa());
            } catch (IOException e) {
                throw new SQLException("No se pudo crear la carpeta de datos", e);
            }
            conexion = abrir(rutaBase());
            crearTablasSiFaltan(conexion);
        }
        return conexion;
    }

    private static Connection abrir(Path db) throws SQLException {
        Connection c = DriverManager.getConnection("jdbc:sqlite:" + db);
        try (Statement st = c.createStatement()) {
            st.execute("PRAGMA foreign_keys = ON");
        }
        return c;
    }

    /** Creamos una base nueva en la ruta indicada, con su carpeta y sus tablas. */
    public static void crearBase(Path destinoDb) throws Exception {
        try {
            Files.createDirectories(destinoDb.getParent());
        } catch (IOException e) {
            throw new Exception("No se pudo crear la carpeta de datos: " + e.getMessage());
        }
        try (Connection c = abrir(destinoDb)) {
            crearTablasSiFaltan(c);
        } catch (SQLException e) {
            throw new Exception("Error SQLite: " + e.getMessage());
        }
    }

    /**
     * Creamos las tablas de la aplicación si la base todavía no las tiene.
     * Consideramos que una base es nueva cuando no existe la tabla empresa;
     * así, abrir una base ya creada no vuelve a insertar los tipos de IVA.
     */
    public static void crearTablasSiFaltan(Connection c) throws SQLException {
        if (!existeTabla(c, "empresa")) {
            ejecutarScript(c, leerScript(SCRIPT_TABLAS));
        }
    }

    private static boolean existeTabla(Connection c, String tabla) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT name FROM sqlite_master WHERE type = 'table' AND name = ?")) {
            ps.setString(1, tabla);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Ejecutamos cada sentencia del script por separado: una única llamada a
     * execute no garantiza que el driver procese todas las sentencias.
     */
    private static void ejecutarScript(Connection conn, String sql) throws SQLException {
        for (String sentencia : sql.split(";")) {
            String t = sentencia.trim();
            if (t.isEmpty()) {
                continue;
            }
            try (Statement st = conn.createStatement()) {
                st.execute(t);
            }
        }
    }

    private static String leerScript(String recurso) throws SQLException {
        try (InputStream entrada = Conexion.class.getClassLoader().getResourceAsStream(recurso)) {
            if (entrada == null) {
                throw new SQLException("No se encontró el script de tablas.");
            }
            return new String(entrada.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new SQLException("No se pudo leer el script de tablas.");
        }
    }
}
