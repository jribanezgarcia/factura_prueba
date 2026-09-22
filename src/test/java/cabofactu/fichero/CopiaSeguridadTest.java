package cabofactu.fichero;

import cabofactu.modelo.negocio.sqlite.Conexion;
import cabofactu.modelo.negocio.sqlite.CopiaSeguridadDAO;
import cabofactu.modelo.negocio.sqlite.FacturaDAO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Clock;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import cabofactu.modelo.dominio.Serie;
import cabofactu.modelo.dominio.EmpresaDisponible;
import cabofactu.modelo.negocio.Empresas;
import cabofactu.modelo.negocio.Configuracion;
import cabofactu.modelo.negocio.PreferenciasGlobales;
import cabofactu.modelo.negocio.Sesion;
import cabofactu.modelo.negocio.ValidacionException;

class CopiaSeguridadTest {

    @TempDir
    Path tempDir;

    private CopiaSeguridad servicio;

    @BeforeEach
    void setUp() throws Exception {
        Conexion.setCarpetaRaiz(tempDir);
        Empresas.getEmpresas().cerrar();
        Empresas.getEmpresas().alta("Pruebas Backup");
        Empresas.getEmpresas().abrir("pruebas_backup", LocalDate.now());
        servicio = new CopiaSeguridad(new CopiaSeguridadDAO(), new FacturaDAO(), Clock.systemDefaultZone());
    }

    @AfterEach
    void tearDown() {
        Conexion.cerrarConexion();
    }

    private void insertarDatosBasicos() throws Exception {
        try (Statement st = Conexion.establecerConexion().createStatement()) {
            st.executeUpdate("INSERT INTO empresa (id, nombre, nif, logo_path) "
                    + "VALUES (1, 'Pruebas Backup', 'B12345674', '') "
                    + "ON CONFLICT(id) DO UPDATE SET nombre='Pruebas Backup', nif='B12345674', logo_path=''");
            st.executeUpdate("INSERT INTO serie (id, codigo, descripcion, es_rectificativa, siguiente_correlativo, reutilizar_anulados, sufijo_fecha) "
                    + "VALUES (1, 'C', 'Serie C', 0, 1, 0, 'MES')");
            st.executeUpdate("INSERT INTO factura (id, serie_id, correlativo) VALUES (1, 1, 1)");
            st.executeUpdate("INSERT INTO factura_version (id, factura_id, version_num, numero, fecha_factura, fecha_guardado, estado, base_total, iva_total, total) "
                    + "VALUES (1, 1, 1, 'C-1/8', '" + LocalDate.now() + "', '" + LocalDate.now() + "', 'EMITIDA', '100.00', '21.00', '121.00')");
        }
    }

    private Path crearCopia() throws Exception {
        return servicio.crearCopia(tempDir.resolve("copias"));
    }

    @Test
    void restaurarDevuelveLosDatosDeLaCopia() throws Exception {
        insertarDatosBasicos();
        Path copia = crearCopia();

        try (Statement st = Conexion.establecerConexion().createStatement()) {
            st.executeUpdate("DELETE FROM factura_version");
            st.executeUpdate("DELETE FROM factura");
            st.executeUpdate("DELETE FROM serie");
            st.executeUpdate("UPDATE empresa SET nombre='Otra', nif='X99999999' WHERE id=1");
        }

        servicio.restaurarEnEmpresaActiva(copia);

        try (Statement st = Conexion.establecerConexion().createStatement();
             ResultSet rs = st.executeQuery("SELECT nombre, nif FROM empresa WHERE id=1")) {
            assertTrue(rs.next());
            assertEquals("Pruebas Backup", rs.getString("nombre"));
            assertEquals("B12345674", rs.getString("nif"));
        }
        try (Statement st = Conexion.establecerConexion().createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) AS n FROM factura")) {
            assertTrue(rs.next());
            assertEquals(1, rs.getInt("n"));
        }
    }

    @Test
    void restaurarDejaCopiaDeRescateConElEstadoPrevio() throws Exception {
        insertarDatosBasicos();
        Path copiaAntigua = crearCopia();

        try (Statement st = Conexion.establecerConexion().createStatement()) {
            st.executeUpdate("UPDATE empresa SET nif='Z0000000M' WHERE id=1");
        }
        Path copiaNueva = crearCopia();

        servicio.restaurarEnEmpresaActiva(copiaAntigua);

        Path rescates = tempDir.resolve("pruebas_backup").resolve("copias_previas");
        assertTrue(Files.isDirectory(rescates), "Debe crearse copias_previas");
        assertTrue(countDb(rescates) >= 1, "Debe existir un archivo de rescate");

        try (var stream = Files.list(rescates)) {
            Path rescate = stream.filter(p -> p.toString().endsWith(".db")).findFirst().orElseThrow();
            Files.copy(rescate, tempDir.resolve("rescate.db"), StandardCopyOption.REPLACE_EXISTING);

            try (var c = DriverManager.getConnection("jdbc:sqlite:" + tempDir.resolve("rescate.db"));
                 Statement st = c.createStatement();
                 ResultSet rs = st.executeQuery("SELECT nif FROM empresa WHERE id=1")) {
                assertTrue(rs.next());
                assertEquals("Z0000000M", rs.getString("nif"));
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
        Empresas.getEmpresas().alta("Activa");
        Empresas.getEmpresas().abrir("activa", LocalDate.now());
        Conexion.establecerConexion();
        try (Statement st = Conexion.establecerConexion().createStatement()) {
            st.executeUpdate("INSERT INTO empresa (id, nombre, nif) VALUES (1, 'Activa', 'A11111119') "
                    + "ON CONFLICT(id) DO UPDATE SET nombre='Activa', nif='A11111119'");
            st.executeUpdate("INSERT INTO serie (id, codigo, descripcion, es_rectificativa, siguiente_correlativo, reutilizar_anulados, sufijo_fecha) "
                    + "VALUES (1, 'A', 'Serie A', 0, 1, 0, 'MES')");
            st.executeUpdate("INSERT INTO factura (id, serie_id, correlativo) VALUES (1, 1, 1)");
            st.executeUpdate("INSERT INTO factura_version (id, factura_id, version_num, numero, fecha_factura, fecha_guardado, estado, base_total, iva_total, total) "
                    + "VALUES (1, 1, 1, 'A-1', '" + LocalDate.now() + "', '" + LocalDate.now() + "', 'EMITIDA', '50.00', '10.50', '60.50')");
        }
        Path copia = servicio.crearCopia(tempDir.resolve("copiasActiva"));

        EmpresaDisponible nuevaInfo = servicio.restaurarComoEmpresaNueva(copia, "Nueva B");

        assertEquals("nueva_b", nuevaInfo.getCarpeta());
        assertTrue(Files.exists(Conexion.rutaBaseDe("nueva_b")));

        try (var c = DriverManager.getConnection("jdbc:sqlite:" + Conexion.rutaBaseDe("nueva_b"));
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT nombre, nif FROM empresa WHERE id=1")) {
            assertTrue(rs.next());
            assertEquals("Activa", rs.getString("nombre"));
            assertEquals("A11111119", rs.getString("nif"));
        }

        assertEquals("activa", Sesion.getSesion().getCarpetaEmpresa());

        Empresas.getEmpresas().abrir("activa", LocalDate.now());
        try (Statement st = Conexion.establecerConexion().createStatement();
             ResultSet rs = st.executeQuery("SELECT nombre, nif FROM empresa WHERE id=1")) {
            assertTrue(rs.next());
            assertEquals("Activa", rs.getString("nombre"));
            assertEquals("A11111119", rs.getString("nif"));
        }
    }

    @Test
    void leerResumenDevuelveDatosCorrectos() throws Exception {
        insertarDatosBasicos();
        Path copia = crearCopia();

        CopiaSeguridad.ResumenCopia r = servicio.leerResumen(copia);

        assertEquals("Pruebas Backup", r.nombreEmpresa());
        assertEquals("B12345674", r.nif());
        assertEquals(1, r.numFacturas());
        assertEquals(LocalDate.now(), r.ultimaFecha());
    }

    @Test
    void leerResumenSinFacturas() throws Exception {
        Path copia = crearCopia();

        CopiaSeguridad.ResumenCopia r = servicio.leerResumen(copia);

        assertEquals(0, r.numFacturas());
        assertNull(r.ultimaFecha());
    }

    @Test
    void rechazaArchivoQueNoEsBaseDeDatos() throws Exception {
        Path falso = tempDir.resolve("falso.db");
        Files.writeString(falso, "esto no es una base de datos");

        assertThrows(ValidacionException.class, () -> servicio.leerResumen(falso));

        try (Statement st = Conexion.establecerConexion().createStatement();
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
            st.executeUpdate("DROP TABLE factura");
        }

        assertThrows(ValidacionException.class, () -> servicio.leerResumen(mutilada));
    }

    @Test
    void rechazaCopiaSinLasTablasNucleo() throws Exception {
        insertarDatosBasicos();
        Path copia = crearCopia();

        Path sinNucleo = tempDir.resolve("sin_nucleo.db");
        Files.copy(copia, sinNucleo);
        try (var c = DriverManager.getConnection("jdbc:sqlite:" + sinNucleo);
              Statement st = c.createStatement()) {
            st.executeUpdate("DROP TABLE factura");
        }

        assertThrows(ValidacionException.class, () -> servicio.leerResumen(sinNucleo));
    }

    @Test
    void restaurarDejaLaBaseUtilizable() throws Exception {
        insertarDatosBasicos();
        Path copia = crearCopia();

        try (Statement st = Conexion.establecerConexion().createStatement()) {
            st.executeUpdate("UPDATE empresa SET nif='Z0000000M' WHERE id=1");
        }

        servicio.restaurarEnEmpresaActiva(copia);

        try (Statement st = Conexion.establecerConexion().createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM tipo_iva")) {
            assertTrue(rs.next());
            assertEquals(4, rs.getInt(1));
        }

        try (Statement st = Conexion.establecerConexion().createStatement();
             ResultSet rs = st.executeQuery("SELECT nif FROM empresa WHERE id=1")) {
            assertTrue(rs.next());
            assertEquals("B12345674", rs.getString("nif"));
        }
    }

    @Test
    void rechazaLaPropiaBaseActivaComoOrigen() throws Exception {
        insertarDatosBasicos();
        assertThrows(ValidacionException.class,
                () -> servicio.leerResumen(Conexion.rutaBase()));
    }

    @Test
    void aceptaCopiaConLasMismasTablas() throws Exception {
        insertarDatosBasicos();
        Path copia = crearCopia();

        Path posterior = tempDir.resolve("posterior.db");
        Files.copy(copia, posterior);

        CopiaSeguridad.ResumenCopia r = servicio.leerResumen(posterior);
        assertEquals("Pruebas Backup", r.nombreEmpresa());
    }

    @Test
    void rechazaCopiaConTablasDistintas() throws Exception {
        insertarDatosBasicos();
        Path copia = crearCopia();

        Path distinta = tempDir.resolve("distinta.db");
        Files.copy(copia, distinta);
        try (var c = DriverManager.getConnection("jdbc:sqlite:" + distinta);
             Statement st = c.createStatement()) {
            st.executeUpdate("DROP TABLE numero_disponible");
            st.executeUpdate("CREATE TABLE numero_disponible_v2 (id INTEGER PRIMARY KEY)");
        }

        ValidacionException e = assertThrows(
                ValidacionException.class, () -> servicio.leerResumen(distinta));
        assertTrue(e.getMessage().contains("numero_disponible"),
                "El rechazo debe mencionar lo que falta: " + e.getMessage());
    }

    @Test
    void limpiaElDiarioHuerfano() throws Exception {
        insertarDatosBasicos();
        Path copia = crearCopia();

        Path wal = Conexion.carpetaEmpresa().resolve("facturas.db-wal");
        Files.writeString(wal, "diario huerfano");

        servicio.restaurarEnEmpresaActiva(copia);

        assertFalse(Files.exists(wal), "El diario wal debe desaparecer tras restaurar");
    }

    @Test
    void restaurarRecuerdaElTemaDeLaCopia() throws Exception {
        Configuracion.getConfiguracion().guardarPreferencia(PreferenciasGlobales.TEMA, "sakura");
        Path copia = crearCopia();

        Configuracion.getConfiguracion().guardarPreferencia(PreferenciasGlobales.TEMA, "neon");
        PreferenciasGlobales.set(PreferenciasGlobales.TEMA, "neon");
        servicio.restaurarEnEmpresaActiva(copia);

        assertEquals("sakura", PreferenciasGlobales.get(PreferenciasGlobales.TEMA));
    }
}
