package cabofactu.modelo.negocio;

import cabofactu.modelo.dominio.EmpresaDisponible;
import cabofactu.modelo.dominio.ResumenCopia;
import cabofactu.modelo.negocio.sqlite.Conexion;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * El negocio de las copias de seguridad: crearlas, leerlas, comprobar su
 * estructura y restaurarlas, sobre la empresa activa o como una nueva.
 */
class CopiaSeguridadTest {

    @TempDir
    Path tempDir;

    private CopiaSeguridad servicio;

    @BeforeEach
    void setUp() throws Exception {
        Conexion.setCarpetaRaiz(tempDir);
        PreferenciasGlobales.set(PreferenciasGlobales.CARPETA_COPIAS, "");
        Empresas.getEmpresas().cerrar();
        Empresas.getEmpresas().alta("Pruebas Backup");
        Empresas.getEmpresas().abrir("pruebas_backup", LocalDate.now());
        servicio = CopiaSeguridad.getCopiaSeguridad();
    }

    @AfterEach
    void tearDown() {
        Conexion.cerrarConexion();
    }

    /** Empresa activa completa (para que se pueda reemplazar) con una factura. */
    private void insertarDatosBasicos() throws Exception {
        try (Statement st = Conexion.establecerConexion().createStatement()) {
            st.executeUpdate("INSERT INTO empresa (id, nombre, nif, direccion, cp, localidad, provincia, "
                    + "email, telefono, logo_path) "
                    + "VALUES (1, 'Pruebas Backup', 'B12345674', 'Calle Falsa 123', '29001', 'Málaga', 'Málaga', "
                    + "'pruebas@backup.es', '600000000', '') "
                    + "ON CONFLICT(id) DO UPDATE SET nombre='Pruebas Backup', nif='B12345674', "
                    + "direccion='Calle Falsa 123', cp='29001', localidad='Málaga', provincia='Málaga', "
                    + "email='pruebas@backup.es', telefono='600000000', logo_path=''");
            st.executeUpdate("INSERT INTO serie (id, codigo, descripcion, es_rectificativa, sufijo_fecha) "
                    + "VALUES (1, 'C', 'Serie C', 0, 'MES')");
            st.executeUpdate("INSERT INTO factura (id, serie_id, anio, correlativo, numero, fecha, estado, "
                    + "cli_nombre, cli_nif, descuento, base_total, iva_total, total) VALUES (1, 1, 2026, 1, "
                    + "'C-1/8', '" + LocalDate.now() + "', 'EMITIDA', 'Pruebas Backup', 'B12345674', 0, "
                    + "'100.00', '21.00', '121.00')");
        }
    }

    private Path crearCopia() throws Exception {
        return servicio.crear(tempDir.resolve("copias"));
    }

    private void borrarCarpeta(Path carpeta) {
        File[] archivos = carpeta.toFile().listFiles();
        if (archivos != null) {
            for (File archivo : archivos) {
                archivo.delete();
            }
        }
        carpeta.toFile().delete();
    }

    @Test
    void restaurarDevuelveLosDatosDeLaCopia() throws Exception {
        insertarDatosBasicos();
        Path copia = crearCopia();

        try (Statement st = Conexion.establecerConexion().createStatement()) {
            st.executeUpdate("DELETE FROM factura_linea");
            st.executeUpdate("DELETE FROM factura");
            st.executeUpdate("DELETE FROM serie");
            st.executeUpdate("UPDATE empresa SET nombre='Otra' WHERE id=1");
        }

        servicio.restaurar(copia);

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
            st.executeUpdate("UPDATE factura SET numero='C-2/8' WHERE id=1");
        }

        servicio.restaurar(copiaAntigua);

        Path rescates = tempDir.resolve("pruebas_backup").resolve("copias_previas");
        assertTrue(Files.isDirectory(rescates), "Debe crearse copias_previas");
        File[] archivosRescate = rescates.toFile().listFiles();
        assertTrue(archivosRescate != null && archivosRescate.length >= 1, "Debe existir un archivo de rescate");

        Path rescate = archivosRescate[0].toPath();
        try (Connection c = DriverManager.getConnection("jdbc:sqlite:" + rescate);
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT numero FROM factura WHERE id=1")) {
            assertTrue(rs.next());
            assertEquals("C-2/8", rs.getString("numero"));
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
            st.executeUpdate("INSERT INTO serie (id, codigo, descripcion, es_rectificativa, sufijo_fecha) "
                    + "VALUES (1, 'A', 'Serie A', 0, 'MES')");
            st.executeUpdate("INSERT INTO factura (id, serie_id, anio, correlativo, numero, fecha, estado, "
                    + "cli_nombre, cli_nif, descuento, base_total, iva_total, total) VALUES (1, 1, 2026, 1, "
                    + "'A-1', '" + LocalDate.now() + "', 'EMITIDA', 'Activa', 'A11111119', 0, "
                    + "'50.00', '10.50', '60.50')");
        }
        Path copia = servicio.crear(tempDir.resolve("copiasActiva"));

        EmpresaDisponible nuevaInfo = servicio.restaurarComoEmpresa(copia, "Nueva B");

        assertEquals("nueva_b", nuevaInfo.getCarpeta());
        assertTrue(Files.exists(Conexion.rutaBaseDe("nueva_b")));

        try (Connection c = DriverManager.getConnection("jdbc:sqlite:" + Conexion.rutaBaseDe("nueva_b"));
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

        ResumenCopia r = servicio.leer(copia);

        assertEquals("Pruebas Backup", r.getNombreEmpresa());
        assertEquals("B12345674", r.getNif());
        assertEquals(1, r.getNumeroFacturas());
        assertEquals(LocalDate.now(), r.getUltimaFecha());
    }

    @Test
    void leerResumenSinFacturas() throws Exception {
        Path copia = crearCopia();

        ResumenCopia r = servicio.leer(copia);

        assertEquals(0, r.getNumeroFacturas());
        assertNull(r.getUltimaFecha());
    }

    @Test
    void rechazaArchivoQueNoEsBaseDeDatos() throws Exception {
        Path falso = tempDir.resolve("falso.db");
        Files.writeString(falso, "esto no es una base de datos");

        assertThrows(Exception.class, () -> servicio.leer(falso));

        try (Statement st = Conexion.establecerConexion().createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM empresa")) {
            assertTrue(rs.next());
        }
    }

    @Test
    void rechazaCopiaSinLasTablasDeLaAplicacion() throws Exception {
        insertarDatosBasicos();
        Path copia = crearCopia();

        try (Connection c = DriverManager.getConnection("jdbc:sqlite:" + copia);
             Statement st = c.createStatement()) {
            st.executeUpdate("DROP TABLE factura");
        }

        Exception e = assertThrows(Exception.class, () -> servicio.leer(copia));
        assertTrue(e.getMessage().contains("factura"), "El rechazo debe mencionar la tabla que falta: " + e.getMessage());
    }

    @Test
    void rechazaLaPropiaBaseActivaComoOrigen() throws Exception {
        insertarDatosBasicos();
        assertThrows(Exception.class, () -> servicio.leer(Conexion.rutaBase()));
    }

    @Test
    void aceptaCopiaConTablasOColumnasDeMas() throws Exception {
        insertarDatosBasicos();
        Path copia = crearCopia();

        try (Connection c = DriverManager.getConnection("jdbc:sqlite:" + copia);
             Statement st = c.createStatement()) {
            st.executeUpdate("CREATE TABLE tabla_extra (id INTEGER PRIMARY KEY)");
            st.executeUpdate("ALTER TABLE cliente ADD COLUMN columna_extra TEXT");
        }

        ResumenCopia r = servicio.leer(copia);
        assertEquals("Pruebas Backup", r.getNombreEmpresa());
    }

    @Test
    void rechazaCopiaALaQueLeFaltaUnaColumna() throws Exception {
        insertarDatosBasicos();
        Path copia = crearCopia();

        try (Connection c = DriverManager.getConnection("jdbc:sqlite:" + copia);
             Statement st = c.createStatement()) {
            st.executeUpdate("ALTER TABLE cliente RENAME TO cliente_viejo");
            st.executeUpdate("CREATE TABLE cliente (id INTEGER PRIMARY KEY, nombre TEXT, nif TEXT)");
        }

        Exception e = assertThrows(Exception.class, () -> servicio.leer(copia));
        assertTrue(e.getMessage().contains("cliente"), "El rechazo debe mencionar lo que falta: " + e.getMessage());
    }

    @Test
    void restaurarDejaLaBaseUtilizable() throws Exception {
        insertarDatosBasicos();
        Path copia = crearCopia();

        servicio.restaurar(copia);

        try (Statement st = Conexion.establecerConexion().createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM tipo_iva")) {
            assertTrue(rs.next());
            assertEquals(4, rs.getInt(1));
        }
    }

    @Test
    void restaurarLimpiaElDiarioHuerfano() throws Exception {
        insertarDatosBasicos();
        Path copia = crearCopia();

        Path wal = Conexion.carpetaEmpresa().resolve("facturas.db-wal");
        Files.writeString(wal, "diario huerfano");

        servicio.restaurar(copia);

        assertFalse(Files.exists(wal), "El diario wal debe desaparecer tras restaurar");
    }

    @Test
    void restaurarRecuerdaElTemaDeLaCopia() throws Exception {
        insertarDatosBasicos();
        Configuracion.getConfiguracion().guardarPreferencia(PreferenciasGlobales.TEMA, "sakura");
        Path copia = crearCopia();

        Configuracion.getConfiguracion().guardarPreferencia(PreferenciasGlobales.TEMA, "neon");
        PreferenciasGlobales.set(PreferenciasGlobales.TEMA, "neon");
        servicio.restaurar(copia);

        assertEquals("sakura", PreferenciasGlobales.get(PreferenciasGlobales.TEMA));
    }

    @Test
    void crearCopiaGeneraFichero() throws Exception {
        insertarDatosBasicos();
        Path destino = servicio.crear(tempDir.resolve("copias"));
        assertTrue(Files.isRegularFile(destino));
    }

    @Test
    void leerResumenDevuelveNumeroDeFacturas() throws Exception {
        insertarDatosBasicos();
        Path copia = crearCopia();

        ResumenCopia r = servicio.leer(copia);

        assertEquals(1, r.getNumeroFacturas());
    }

    @Test
    void leerResumenDeUnArchivoDeTextoLanzaExcepcion() throws Exception {
        Path texto = tempDir.resolve("noes.db");
        Files.writeString(texto, "esto no es una base de datos");

        assertThrows(Exception.class, () -> servicio.leer(texto));
    }

    @Test
    void restaurarBorraElDiarioYElArchivoCompartidoHuerfanos() throws Exception {
        insertarDatosBasicos();
        Path copia = crearCopia();

        Path wal = Conexion.carpetaEmpresa().resolve("facturas.db-wal");
        Path shm = Conexion.carpetaEmpresa().resolve("facturas.db-shm");
        Files.writeString(wal, "diario huerfano");
        Files.writeString(shm, "compartido huerfano");

        servicio.restaurar(copia);

        assertFalse(Files.exists(wal));
        assertFalse(Files.exists(shm));
    }

    @Test
    void elNombreEmpiezaPorLaCarpetaDeLaEmpresaYLaFechaDeHoy() throws Exception {
        Path copia = crearCopia();

        String hoy = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        assertTrue(copia.getFileName().toString().startsWith("pruebas_backup_" + hoy + "_"),
                "El nombre debe empezar por la carpeta de la empresa y la fecha de hoy: " + copia.getFileName());
    }

    @Test
    void dosCopiasSeguidasDanDosArchivosDistintos() throws Exception {
        Path carpeta = tempDir.resolve("copiasDobles");
        Path primera = servicio.crear(carpeta);
        Path segunda = servicio.crear(carpeta);

        assertNotEquals(primera, segunda);
        assertTrue(Files.exists(primera));
        assertTrue(Files.exists(segunda));
    }

    @Test
    void carpetaCopiasDevuelveLaUltimaCarpetaUsadaYNullSiSeBorra() throws Exception {
        Path carpeta = tempDir.resolve("copiasRecordadas");
        servicio.crear(carpeta);

        assertEquals(carpeta, servicio.carpetaCopias());

        borrarCarpeta(carpeta);
        assertNull(servicio.carpetaCopias());
    }

    @Test
    void restaurarNoCambiaLaCarpetaRecordada() throws Exception {
        insertarDatosBasicos();
        Path copia = crearCopia();

        servicio.restaurar(copia);

        assertEquals(tempDir.resolve("copias"), servicio.carpetaCopias());
    }

    @Test
    void puedeReemplazarConElMismoNifIgnorandoMayusculas() throws Exception {
        insertarDatosBasicos();
        ResumenCopia resumen = new ResumenCopia("Otra", "b12345674", 0, null, "");

        assertTrue(servicio.puedeReemplazar(resumen));
    }

    @Test
    void noPuedeReemplazarConOtroNif() throws Exception {
        insertarDatosBasicos();
        ResumenCopia resumen = new ResumenCopia("Otra", "X99999999", 0, null, "");

        assertFalse(servicio.puedeReemplazar(resumen));
    }

    @Test
    void restaurarUnaCopiaConOtroNifLanzaYNoCambiaNada() throws Exception {
        insertarDatosBasicos();
        Path copia = crearCopia();

        try (Connection c = DriverManager.getConnection("jdbc:sqlite:" + copia);
             Statement st = c.createStatement()) {
            st.executeUpdate("UPDATE empresa SET nif='X99999999' WHERE id=1");
        }

        Exception e = assertThrows(Exception.class, () -> servicio.restaurar(copia));
        assertTrue(e.getMessage().contains("X99999999"), "El mensaje debe nombrar el NIF de la copia: " + e.getMessage());

        try (Statement st = Conexion.establecerConexion().createStatement();
             ResultSet rs = st.executeQuery("SELECT nif FROM empresa WHERE id=1")) {
            assertTrue(rs.next());
            assertEquals("B12345674", rs.getString("nif"));
        }
    }
}
