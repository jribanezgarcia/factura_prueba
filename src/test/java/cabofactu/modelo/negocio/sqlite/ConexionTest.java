package cabofactu.modelo.negocio.sqlite;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConexionTest {

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        Conexion.setCarpetaRaiz(tempDir);
        Conexion.cerrarConexion();
    }

    @AfterEach
    void tearDown() {
        Conexion.cerrarConexion();
    }

    @Test
    void setEmpresaActivaApuntaASubcarpeta() throws Exception {
        Conexion.setEmpresaActiva("mi_empresa");
        assertTrue(Conexion.rutaBase().startsWith(tempDir.resolve("mi_empresa")));
        Conexion.establecerConexion();
        assertTrue(Files.exists(Conexion.rutaBase()));
    }

    @Test
    void crearBaseCreaTodasLasTablas() throws Exception {
        Path destino = tempDir.resolve("nueva").resolve("facturas.db");
        Conexion.crearBase(destino);
        assertTrue(Files.isRegularFile(destino));
        try (Connection c = DriverManager.getConnection("jdbc:sqlite:" + destino)) {
            Set<String> tablas = new HashSet<>();
            try (Statement st = c.createStatement();
                 ResultSet rs = st.executeQuery(
                         "SELECT name FROM sqlite_master WHERE type = 'table' AND name NOT LIKE 'sqlite_%'")) {
                while (rs.next()) {
                    tablas.add(rs.getString(1));
                }
            }
            String[] esperadas = new String[]{"cliente", "serie", "serie_siguiente", "tipo_iva",
                    "tipo_retencion", "factura", "factura_version", "factura_linea",
                    "numero_disponible", "empresa", "preferencias"};
            for (int i = 0; i < esperadas.length; i++) {
                assertTrue(tablas.contains(esperadas[i]), "Falta la tabla " + esperadas[i]);
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
    void crearTablasDosVecesNoDuplica() throws Exception {
        Path destino = tempDir.resolve("otra").resolve("facturas.db");
        Conexion.crearBase(destino);
        try (Connection c = DriverManager.getConnection("jdbc:sqlite:" + destino)) {
            Conexion.crearTablasSiFaltan(c);
            Conexion.crearTablasSiFaltan(c);
            assertEquals(4, contar(c, "SELECT COUNT(*) FROM tipo_iva"));
            assertEquals(1, contar(c, "SELECT COUNT(*) FROM tipo_iva WHERE es_suplido = 1"));
            assertEquals(1, contar(c, "SELECT COUNT(*) FROM empresa"));
        }
        assertTrue(Files.isRegularFile(destino));
    }

    private int contar(Connection c, String sql) throws Exception {
        try (Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            rs.next();
            return rs.getInt(1);
        }
    }
}
