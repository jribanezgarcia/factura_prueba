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
    void crearBaseDejaVersionDeEsquema() throws Exception {
        Path destino = tempDir.resolve("nueva").resolve("facturas.db");
        Conexion.crearBase(destino);
        assertTrue(Files.isRegularFile(destino));
        try (Connection c = DriverManager.getConnection("jdbc:sqlite:" + destino);
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("PRAGMA user_version")) {
            assertTrue(rs.next());
            assertEquals(Migraciones.ultimaVersion(), rs.getInt(1));
        }
    }

    @Test
    void migrarBaseNoFalla() throws Exception {
        Path destino = tempDir.resolve("otra").resolve("facturas.db");
        Conexion.crearBase(destino);
        Conexion.migrarBase(destino);
        assertTrue(Files.isRegularFile(destino));
    }
}
