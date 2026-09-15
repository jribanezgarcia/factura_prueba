package cabofactu.modelo.negocio.sqlite;

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
public final class Migraciones {

    private static final List<String> SCRIPTS = List.of(
            "db/migrations/001_baseline.sql"
    );

    private Migraciones() {
    }

    /** Numero total de migraciones (ultima version de esquema conocida). */
    public static int ultimaVersion() {
        return SCRIPTS.size();
    }

    public static synchronized void migrar(Connection conn) throws SQLException {
        int actual = versionActual(conn);
        for (int i = 0; i < SCRIPTS.size(); i++) {
            int version = i + 1;
            if (version <= actual) {
                continue;
            }
            String sql = leerScript(SCRIPTS.get(i));
            ejecutarScript(conn, sql);
            try (Statement st = conn.createStatement()) {
                st.execute("PRAGMA user_version = " + version);
            }
        }
    }

    /**
     * Ejecuta cada sentencia del script por separado (una unica llamada a
     * execute no garantiza que el driver procese todas las sentencias).
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

    public static int versionActual(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("PRAGMA user_version")) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private static String leerScript(String resource) {
        try (InputStream in = Migraciones.class.getClassLoader().getResourceAsStream(resource)) {
            if (in == null) {
                throw new IllegalStateException("Migracion no encontrada: " + resource);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Error al leer la migracion " + resource, e);
        }
    }
}
