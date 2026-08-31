package com.alcazaba.facturacion.db;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * Runner de migraciones basado en PRAGMA user_version.
 * Cada script debe llamarse NNN_nombre.sql y aplicarse en orden.
 */
public final class Migrations {

    private static final List<String> SCRIPTS = List.of(
            "db/migrations/001_init.sql",
            "db/migrations/002_datos_factura_pdf.sql",
            "db/migrations/003_formato_numeracion_series.sql",
            "db/migrations/004_serie_siguiente.sql",
            "db/migrations/005_retencion_irpf.sql",
            "db/migrations/006_retencion_irpf_snapshot.sql",
            "db/migrations/007_numeros_disponibles.sql"
    );

    private Migrations() {
    }

    public static synchronized void migrate(Connection conn) throws SQLException {
        int current = userVersion(conn);
        for (int i = 0; i < SCRIPTS.size(); i++) {
            int version = i + 1;
            if (version <= current) {
                continue;
            }
            String sql = readScript(SCRIPTS.get(i));
            executeScript(conn, sql);
            try (Statement st = conn.createStatement()) {
                st.execute("PRAGMA user_version = " + version);
            }
        }
    }

    /**
     * Ejecuta cada sentencia del script por separado (una unica llamada a
     * execute no garantiza que el driver procese todas las sentencias).
     */
    private static void executeScript(Connection conn, String sql) throws SQLException {
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

    private static int userVersion(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("PRAGMA user_version")) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private static String readScript(String resource) {
        try (InputStream in = Migrations.class.getClassLoader().getResourceAsStream(resource)) {
            if (in == null) {
                throw new IllegalStateException("Migracion no encontrada: " + resource);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Error al leer la migracion " + resource, e);
        }
    }
}
