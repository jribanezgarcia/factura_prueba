package cabofactu.modelo.negocio.sqlite;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MigracionesTest {

    @TempDir
    Path tempDir;

    @AfterEach
    void cerrar() {
        Conexion.cerrarConexion();
    }

    private Connection baseNueva() throws Exception {
        Conexion.setCarpetaRaiz(tempDir);
        Conexion.cerrarConexion();
        Conexion.setEmpresaActiva("nueva");
        return Conexion.establecerConexion();
    }

    private int contar(Connection c, String sql) throws Exception {
        try (Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            rs.next();
            return rs.getInt(1);
        }
    }

    @Test
    void baseNuevaConVersion1YTodasLasTablas() throws Exception {
        try (Connection c = baseNueva()) {
            assertEquals(1, Migraciones.versionActual(c));
            assertEquals(1, Migraciones.ultimaVersion());

            Set<String> tablas = new HashSet<>();
            try (Statement st = c.createStatement();
                 ResultSet rs = st.executeQuery(
                         "SELECT name FROM sqlite_master WHERE type = 'table' AND name NOT LIKE 'sqlite_%'")) {
                while (rs.next()) {
                    tablas.add(rs.getString(1));
                }
            }
            for (String esperada : new String[]{"cliente", "serie", "serie_siguiente", "tipo_iva",
                    "tipo_retencion", "factura", "factura_version", "factura_linea",
                    "numero_disponible", "empresa", "preferencias"}) {
                assertTrue(tablas.contains(esperada), "Falta la tabla " + esperada);
            }

            assertEquals(4, contar(c, "SELECT COUNT(*) FROM tipo_iva"));
            assertEquals(1, contar(c, "SELECT COUNT(*) FROM tipo_iva WHERE es_suplido = 1"));
            try (Statement st = c.createStatement();
                 ResultSet rs = st.executeQuery("SELECT nombre FROM tipo_iva WHERE es_suplido = 1")) {
                assertTrue(rs.next());
                assertEquals("Suplido", rs.getString(1));
            }
        }
    }

    @Test
    void migrarDosVecesNoDuplicaSiembras() throws Exception {
        try (Connection c = baseNueva()) {
            Migraciones.migrar(c);
            Migraciones.migrar(c);
            assertEquals(1, Migraciones.versionActual(c));
            assertEquals(4, contar(c, "SELECT COUNT(*) FROM tipo_iva"));
            assertEquals(1, contar(c, "SELECT COUNT(*) FROM tipo_iva WHERE es_suplido = 1"));
            assertEquals(1, contar(c, "SELECT COUNT(*) FROM empresa"));
        }
    }
}
