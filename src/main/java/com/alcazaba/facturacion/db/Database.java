package com.alcazaba.facturacion.db;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Gestiona la conexion SQLite compartida y la carpeta de datos de la aplicacion
 * (%APPDATA%/Facturacion), separada de la instalacion.
 */
public final class Database {

    private static final String DB_FILE = "facturas.db";
    private static Connection connection;
    private static Path dataDir = defaultDataDir();

    private Database() {
    }

    private static Path defaultDataDir() {
        String appdata = System.getenv("APPDATA");
        Path base = appdata != null && !appdata.isBlank()
                ? Path.of(appdata)
                : Path.of(System.getProperty("user.home"));
        return base.resolve("Facturacion");
    }

    public static Path dataDir() {
        return dataDir;
    }

    /**
     * Redirige la carpeta de datos (uso en pruebas). Debe llamarse antes de la
     * primera conexion.
     */
    public static void setDataDir(Path dir) {
        dataDir = dir;
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
