package com.alcazaba.facturacion.service;

import com.alcazaba.facturacion.db.Database;
import com.alcazaba.facturacion.db.Migrations;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BackupServiceTest {

    @TempDir
    Path tempDir;

    private BackupService servicio;

    @BeforeEach
    void setUp() throws Exception {
        Database.setDataDir(tempDir);
        Database.resetConnection();
        Sesion.reiniciar();
        EmpresaManager.crearEmpresa("Pruebas Backup");
        EmpresaManager.conectar("pruebas_backup", LocalDate.now());
        servicio = new BackupService();
    }

    @AfterEach
    void tearDown() {
        Database.resetConnection();
    }

    private void insertarDatosBasicos() throws Exception {
        try (Statement st = Database.getConnection().createStatement()) {
            st.executeUpdate("INSERT INTO empresa (id, nombre, nif, logo_path) "
                    + "VALUES (1, 'Pruebas Backup', 'B12345678', '') "
                    + "ON CONFLICT(id) DO UPDATE SET nombre='Pruebas Backup', nif='B12345678', logo_path=''");
            st.executeUpdate("INSERT INTO serie (id, codigo, descripcion, es_rectificativa, siguiente_correlativo, reutilizar_anulados, sufijo_fecha) "
                    + "VALUES (1, 'C', 'Serie C', 0, 1, 0, 'MES')");
            st.executeUpdate("INSERT INTO factura (id, serie_id, correlativo) VALUES (1, 1, 1)");
            st.executeUpdate("INSERT INTO factura_version (id, factura_id, version_num, numero, fecha_factura, fecha_guardado, estado, base_total, iva_total, total) "
                    + "VALUES (1, 1, 1, 'C-1/8', '" + LocalDate.now() + "', '" + LocalDate.now() + "', 'EMITIDA', '100.00', '21.00', '121.00')");
        }
    }

    private Path crearCopia() throws Exception {
        return servicio.crearBackup(tempDir.resolve("copias"));
    }

    @Test
    void restaurarDevuelveLosDatosDeLaCopia() throws Exception {
        insertarDatosBasicos();
        Path copia = crearCopia();

        try (Statement st = Database.getConnection().createStatement()) {
            st.executeUpdate("DELETE FROM factura_version");
            st.executeUpdate("DELETE FROM factura");
            st.executeUpdate("DELETE FROM serie");
            st.executeUpdate("UPDATE empresa SET nombre='Otra', nif='X99999999' WHERE id=1");
        }

        servicio.restaurarEnEmpresaActiva(copia);

        try (Statement st = Database.getConnection().createStatement();
             ResultSet rs = st.executeQuery("SELECT nombre, nif FROM empresa WHERE id=1")) {
            assertTrue(rs.next());
            assertEquals("Pruebas Backup", rs.getString("nombre"));
            assertEquals("B12345678", rs.getString("nif"));
        }
        try (Statement st = Database.getConnection().createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) AS n FROM factura")) {
            assertTrue(rs.next());
            assertEquals(1, rs.getInt("n"));
        }
    }

    @Test
    void restaurarDejaCopiaDeRescateConElEstadoPrevio() throws Exception {
        insertarDatosBasicos();
        Path copiaAntigua = crearCopia();

        try (Statement st = Database.getConnection().createStatement()) {
            st.executeUpdate("UPDATE empresa SET nif='Z00000000' WHERE id=1");
        }
        Path copiaNueva = crearCopia();

        servicio.restaurarEnEmpresaActiva(copiaAntigua);

        Path rescates = tempDir.resolve("pruebas_backup").resolve("copias_previas");
        assertTrue(Files.isDirectory(rescates), "Debe crearse copias_previas");
        assertTrue(countDb(rescates) >= 1, "Debe existir un archivo de rescate");

        try (var stream = Files.list(rescates)) {
            Path rescate = stream.filter(p -> p.toString().endsWith(".db")).findFirst().orElseThrow();
            Files.copy(rescate, tempDir.resolve("rescate.db"), java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            try (var c = DriverManager.getConnection("jdbc:sqlite:" + tempDir.resolve("rescate.db"));
                 Statement st = c.createStatement();
                 ResultSet rs = st.executeQuery("SELECT nif FROM empresa WHERE id=1")) {
                assertTrue(rs.next());
                assertEquals("Z00000000", rs.getString("nif"));
            }
        }
    }

    private long countDb(Path carpeta) throws Exception {
        try (var stream = Files.list(carpeta)) {
            return stream.filter(p -> p.toString().endsWith(".db")).count();
        }
    }

    @Test
    void restaurarComoEmpresaNuevaNoTocaLaActiva() throws Exception {
        EmpresaManager.crearEmpresa("Activa");
        EmpresaManager.conectar("activa", LocalDate.now());
        Database.getConnection();
        try (Statement st = Database.getConnection().createStatement()) {
            st.executeUpdate("INSERT INTO empresa (id, nombre, nif) VALUES (1, 'Activa', 'A11111111') "
                    + "ON CONFLICT(id) DO UPDATE SET nombre='Activa', nif='A11111111'");
            st.executeUpdate("INSERT INTO serie (id, codigo, descripcion, es_rectificativa, siguiente_correlativo, reutilizar_anulados, sufijo_fecha) "
                    + "VALUES (1, 'A', 'Serie A', 0, 1, 0, 'MES')");
            st.executeUpdate("INSERT INTO factura (id, serie_id, correlativo) VALUES (1, 1, 1)");
            st.executeUpdate("INSERT INTO factura_version (id, factura_id, version_num, numero, fecha_factura, fecha_guardado, estado, base_total, iva_total, total) "
                    + "VALUES (1, 1, 1, 'A-1', '" + LocalDate.now() + "', '" + LocalDate.now() + "', 'EMITIDA', '50.00', '10.50', '60.50')");
        }
        Path copia = servicio.crearBackup(tempDir.resolve("copiasActiva"));

        EmpresaManager.EmpresaInfo nuevaInfo = servicio.restaurarComoEmpresaNueva(copia, "Nueva B");

        assertEquals("nueva_b", nuevaInfo.slug());
        assertTrue(Files.exists(Database.dbPathDe("nueva_b")));

        try (var c = DriverManager.getConnection("jdbc:sqlite:" + Database.dbPathDe("nueva_b"));
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT nombre, nif FROM empresa WHERE id=1")) {
            assertTrue(rs.next());
            assertEquals("Activa", rs.getString("nombre"));
            assertEquals("A11111111", rs.getString("nif"));
        }

        assertEquals("activa", Sesion.empresaSlug());

        EmpresaManager.conectar("activa", LocalDate.now());
        try (Statement st = Database.getConnection().createStatement();
             ResultSet rs = st.executeQuery("SELECT nombre, nif FROM empresa WHERE id=1")) {
            assertTrue(rs.next());
            assertEquals("Activa", rs.getString("nombre"));
            assertEquals("A11111111", rs.getString("nif"));
        }
    }

    @Test
    void leerResumenDevuelveDatosCorrectos() throws Exception {
        insertarDatosBasicos();
        Path copia = crearCopia();

        BackupService.ResumenBackup r = servicio.leerResumen(copia);

        assertEquals("Pruebas Backup", r.nombreEmpresa());
        assertEquals("B12345678", r.nif());
        assertEquals(1, r.numFacturas());
        assertEquals(LocalDate.now(), r.ultimaFecha());
        assertEquals(Migrations.ultimaVersion(), r.userVersion());
    }

    @Test
    void leerResumenSinFacturas() throws Exception {
        Path copia = crearCopia();

        BackupService.ResumenBackup r = servicio.leerResumen(copia);

        assertEquals(0, r.numFacturas());
        assertNull(r.ultimaFecha());
    }

    @Test
    void rechazaArchivoQueNoEsBaseDeDatos() throws Exception {
        Path falso = tempDir.resolve("falso.db");
        Files.writeString(falso, "esto no es una base de datos");

        assertThrows(ValidationException.class, () -> servicio.leerResumen(falso));

        try (Statement st = Database.getConnection().createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM empresa")) {
            assertTrue(rs.next());
        }
    }

    @Test
    void rechazaCopiaSinLasTablasDeLaAplicacion() throws Exception {
        insertarDatosBasicos();
        Path copia = crearCopia();

        Path mutilada = tempDir.resolve("mutilada.db");
        Files.copy(copia, mutilada);
        try (var c = DriverManager.getConnection("jdbc:sqlite:" + mutilada);
             Statement st = c.createStatement()) {
            st.executeUpdate("DROP TABLE numero_disponible");
        }

        assertThrows(ValidationException.class, () -> servicio.leerResumen(mutilada));
    }

    @Test
    void rechazaLaPropiaBaseActivaComoOrigen() throws Exception {
        insertarDatosBasicos();
        assertThrows(ValidationException.class,
                () -> servicio.leerResumen(Database.dbPath()));
    }

    @Test
    void aceptaEsquemaPosteriorConLasMismasTablas() throws Exception {
        insertarDatosBasicos();
        Path copia = crearCopia();

        Path posterior = tempDir.resolve("posterior.db");
        Files.copy(copia, posterior);
        try (var c = DriverManager.getConnection("jdbc:sqlite:" + posterior);
             Statement st = c.createStatement()) {
            st.executeUpdate("PRAGMA user_version = 99");
        }

        BackupService.ResumenBackup r = servicio.leerResumen(posterior);
        assertEquals(99, r.userVersion());
        assertTrue(r.tablasCoinciden());
    }

    @Test
    void rechazaEsquemaPosteriorConTablasDistintas() throws Exception {
        insertarDatosBasicos();
        Path copia = crearCopia();

        Path distinta = tempDir.resolve("distinta.db");
        Files.copy(copia, distinta);
        try (var c = DriverManager.getConnection("jdbc:sqlite:" + distinta);
             Statement st = c.createStatement()) {
            st.executeUpdate("DROP TABLE numero_disponible");
            st.executeUpdate("CREATE TABLE numero_disponible_v2 (id INTEGER PRIMARY KEY)");
            st.executeUpdate("PRAGMA user_version = 99");
        }

        assertThrows(ValidationException.class, () -> servicio.leerResumen(distinta));
    }

    @Test
    void limpiaElDiarioHuerfano() throws Exception {
        insertarDatosBasicos();
        Path copia = crearCopia();

        Path wal = Database.dataDir().resolve("facturas.db-wal");
        Files.writeString(wal, "diario huerfano");

        servicio.restaurarEnEmpresaActiva(copia);

        assertFalse(Files.exists(wal), "El diario wal debe desaparecer tras restaurar");
    }
}
