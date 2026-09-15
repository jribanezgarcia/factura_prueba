package cabofactu.modelo.negocio.sqlite;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Gestiona la conexion SQLite compartida y la carpeta de datos de la aplicacion
 * (%APPDATA%/Facturacion), separada de la instalacion. Cada empresa tiene su
 * propia base de datos en una subcarpeta de BASE_DATA_DIR.
 */
public final class Conexion {

    private static final String FICHERO_BASE = "facturas.db";
    private static Connection conexion;
    private static Path carpetaRaiz = carpetaRaizPorDefecto();
    private static Path carpetaEmpresa = carpetaRaiz;

    private Conexion() {
    }

    private static Path carpetaRaizPorDefecto() {
        String appdata = System.getenv("APPDATA");
        Path base = appdata != null && !appdata.isBlank()
                ? Path.of(appdata)
                : Path.of(System.getProperty("user.home"));
        return base.resolve("Facturacion");
    }

    /** Raiz fija de datos: %APPDATA%/Facturacion (o la carpeta de pruebas). */
    public static Path carpetaRaiz() {
        return carpetaRaiz;
    }

    public static Path carpetaEmpresa() {
        return carpetaEmpresa;
    }

    /**
     * Redirige la carpeta de datos (uso en pruebas): fija la raiz y la carpeta
     * activa sin empresa. Debe llamarse antes de la primera conexion.
     */
    public static void setCarpetaRaiz(Path dir) {
        carpetaRaiz = dir;
        carpetaEmpresa = dir;
    }

    /**
     * Activa la empresa cuyo slug da nombre a la subcarpeta de datos. La
     * conexion anterior, si existe, queda cerrada.
     */
    public static void setEmpresaActiva(String slug) {
        carpetaEmpresa = carpetaRaiz.resolve(slug);
        cerrarConexion();
    }

    /**
     * Subcarpetas de la raiz de datos que contienen una base de empresas.
     */
    public static List<String> getEmpresasDisponibles() {
        List<String> lista = new ArrayList<>();
        if (!Files.isDirectory(carpetaRaiz)) {
            return lista;
        }
        try (Stream<Path> carpetas = Files.list(carpetaRaiz)) {
            carpetas.filter(Files::isDirectory)
                    .filter(d -> Files.exists(d.resolve(FICHERO_BASE)))
                    .sorted()
                    .forEach(d -> lista.add(d.getFileName().toString()));
        } catch (IOException ignored) {
        }
        return lista;
    }

    /** Lock de instancia unica global, independiente de la empresa activa. */
    public static Path rutaBloqueoGlobal() {
        return carpetaRaiz.resolve("facturas.lock");
    }

    public static void cerrarConexion() {
        if (conexion != null) {
            try {
                conexion.close();
            } catch (SQLException ignored) {
            }
        }
        conexion = null;
    }

    public static Path rutaBase() {
        return carpetaEmpresa().resolve(FICHERO_BASE);
    }

    /**
     * Ruta de la base de datos de una empresa cualquiera, sin activarla.
     */
    public static Path rutaBaseDe(String slug) {
        return carpetaRaiz.resolve(slug).resolve(FICHERO_BASE);
    }

    public static Path rutaBloqueo() {
        return carpetaEmpresa().resolve("facturas.lock");
    }

    public static synchronized Connection establecerConexion() throws SQLException {
        if (conexion == null || conexion.isClosed()) {
            try {
                Files.createDirectories(carpetaEmpresa());
            } catch (IOException e) {
                throw new SQLException("No se pudo crear la carpeta de datos", e);
            }
            conexion = abrir(rutaBase());
            Migraciones.migrar(conexion);
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

    /** Crea una base nueva en la ruta indicada, con la carpeta padre y el esquema. */
    public static void crearBase(Path destinoDb) {
        try {
            Files.createDirectories(destinoDb.getParent());
        } catch (IOException e) {
            throw new DatosException("No se pudo crear la carpeta de datos", e);
        }
        try (Connection c = abrir(destinoDb)) {
            Migraciones.migrar(c);
        } catch (SQLException e) {
            throw new DatosException(e);
        }
    }

    /** Aplica las migraciones pendientes a una base existente. */
    public static void migrarBase(Path destinoDb) {
        try (Connection c = abrir(destinoDb)) {
            Migraciones.migrar(c);
        } catch (SQLException e) {
            throw new DatosException(e);
        }
    }

    public static void confirmar() {
        try {
            Connection c = establecerConexion();
            if (!c.getAutoCommit()) {
                c.commit();
            }
        } catch (SQLException e) {
            throw new DatosException("Error al confirmar la transaccion", e);
        }
    }

    public static void deshacer() {
        try {
            Connection c = establecerConexion();
            if (!c.getAutoCommit()) {
                c.rollback();
            }
        } catch (SQLException e) {
            throw new DatosException("Error al revertir la transaccion", e);
        }
    }

    public static void iniciarTransaccion() {
        try {
            Connection c = establecerConexion();
            if (c.getAutoCommit()) {
                c.setAutoCommit(false);
            }
        } catch (SQLException e) {
            throw new DatosException("Error al iniciar la transaccion", e);
        }
    }

    public static void terminarTransaccion() {
        try {
            Connection c = establecerConexion();
            if (!c.getAutoCommit()) {
                c.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new DatosException("Error al finalizar la transaccion", e);
        }
    }
}
