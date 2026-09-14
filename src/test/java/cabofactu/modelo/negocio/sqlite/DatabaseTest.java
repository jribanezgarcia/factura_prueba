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

class DatabaseTest {

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        Database.setDataDir(tempDir);
        Database.resetConnection();
    }

    @AfterEach
    void tearDown() {
        Database.resetConnection();
    }

    @Test
    void setEmpresaActivaApuntaASubcarpeta() throws Exception {
        Database.setEmpresaActiva("mi_empresa");
        assertTrue(Database.dbPath().startsWith(tempDir.resolve("mi_empresa")));
        Database.getConnection();
        assertTrue(Files.exists(Database.dbPath()));
    }

    @Test
    void crearBaseDejaVersionDeEsquema() throws Exception {
        Path destino = tempDir.resolve("nueva").resolve("facturas.db");
        Database.crearBase(destino);
        assertTrue(Files.isRegularFile(destino));
        try (Connection c = DriverManager.getConnection("jdbc:sqlite:" + destino);
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("PRAGMA user_version")) {
            assertTrue(rs.next());
            assertEquals(Migrations.ultimaVersion(), rs.getInt(1));
        }
    }

    @Test
    void migrarBaseNoFalla() throws Exception {
        Path destino = tempDir.resolve("otra").resolve("facturas.db");
        Database.crearBase(destino);
        Database.migrarBase(destino);
        assertTrue(Files.isRegularFile(destino));
    }
}
