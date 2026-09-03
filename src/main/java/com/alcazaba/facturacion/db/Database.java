package com.alcazaba.facturacion.db;

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
public final class Database {

    public static final String SLUG_EMPRESA_INICIAL = "comercial_alcazaba";
    private static final String DB_FILE = "facturas.db";
    private static Connection connection;
    private static Path baseDataDir = defaultDataDir();
    private static Path dataDir = baseDataDir;

    private Database() {
    }

    private static Path defaultDataDir() {
        String appdata = System.getenv("APPDATA");
        Path base = appdata != null && !appdata.isBlank()
                ? Path.of(appdata)
                : Path.of(System.getProperty("user.home"));
        return base.resolve("Facturacion");
    }

    /** Raiz fija de datos: %APPDATA%/Facturacion (o la carpeta de pruebas). */
    public static Path baseDataDir() {
        return baseDataDir;
    }

    public static Path dataDir() {
        return dataDir;
    }

    /**
     * Redirige la carpeta de datos (uso en pruebas): fija la raiz y la carpeta
     * activa sin empresa. Debe llamarse antes de la primera conexion.
     */
    public static void setDataDir(Path dir) {
        baseDataDir = dir;
        dataDir = dir;
    }

    /**
     * Activa la empresa cuyo slug da nombre a la subcarpeta de datos. La
     * conexion anterior, si existe, queda cerrada.
     */
    public static void setEmpresaActiva(String slug) {
        dataDir = baseDataDir.resolve(slug);
        resetConnection();
    }

    /**
     * Subcarpetas de la raiz de datos que contienen una base de empresas.
     */
    public static List<String> getEmpresasDisponibles() {
        List<String> lista = new ArrayList<>();
        if (!Files.isDirectory(baseDataDir)) {
            return lista;
        }
        try (Stream<Path> carpetas = Files.list(baseDataDir)) {
            carpetas.filter(Files::isDirectory)
                    .filter(d -> Files.exists(d.resolve(DB_FILE)))
                    .sorted()
                    .forEach(d -> lista.add(d.getFileName().toString()));
        } catch (IOException ignored) {
        }
        return lista;
    }

    /**
     * Migracion de instalacion de un solo archivo a carpetas por empresa: si
     * existe BASE_DATA_DIR/facturas.db y todavia no hay ninguna empresa, mueve
     * la base (y su lock) a la carpeta de la empresa inicial. Devuelve el slug
     * creado o null si no ha lugar.
     */
    public static String migrarInstalacionUnArchivo() throws IOException {
        Path legacy = baseDataDir.resolve(DB_FILE);
        if (!Files.exists(legacy) || !getEmpresasDisponibles().isEmpty()) {
            return null;
        }
        Path destino = baseDataDir.resolve(SLUG_EMPRESA_INICIAL);
        Files.createDirectories(destino);
        Files.move(legacy, destino.resolve(DB_FILE));
        Path lock = baseDataDir.resolve("facturas.lock");
        if (Files.exists(lock)) {
            Files.move(lock, destino.resolve("facturas.lock"));
        }
        return SLUG_EMPRESA_INICIAL;
    }

    /** Lock de instancia unica global, independiente de la empresa activa. */
    public static Path lockPathGlobal() {
        return baseDataDir.resolve("facturas.lock");
    }

    public static void resetConnection() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException ignored) {
            }
        }
        connection = null;
    }

    public static Path dbPath() {
        return dataDir().resolve(DB_FILE);
    }

    /**
     * Ruta de la base de datos de una empresa cualquiera, sin activarla.
     */
    public static Path dbPathDe(String slug) {
        return baseDataDir.resolve(slug).resolve(DB_FILE);
    }

    public static Path lockPath() {
        return dataDir().resolve("facturas.lock");
    }

    public static synchronized Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Files.createDirectories(dataDir());
            } catch (IOException e) {
                throw new SQLException("No se pudo crear la carpeta de datos", e);
            }
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath());
            try (Statement st = connection.createStatement()) {
                st.execute("PRAGMA foreign_keys = ON");
            }
            Migrations.migrate(connection);
        }
        return connection;
    }

    public static void commit() {
        try {
            Connection c = getConnection();
            if (!c.getAutoCommit()) {
                c.commit();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al confirmar la transaccion", e);
        }
    }

    public static void rollback() {
        try {
            Connection c = getConnection();
            if (!c.getAutoCommit()) {
                c.rollback();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al revertir la transaccion", e);
        }
    }

    public static void beginTransaction() {
        try {
            Connection c = getConnection();
            if (c.getAutoCommit()) {
                c.setAutoCommit(false);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al iniciar la transaccion", e);
        }
    }

    public static void endTransaction() {
        try {
            Connection c = getConnection();
            if (!c.getAutoCommit()) {
                c.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al finalizar la transaccion", e);
        }
    }
}
