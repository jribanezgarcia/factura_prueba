package cabofactu.modelo.negocio.sqlite;

import cabofactu.modelo.negocio.Empresas;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Comprobamos el trocado de sentencias y, contra una base de datos temporal,
 * que cargar la demostración deja el logo copiado y la empresa en modo logo.
 */
class CargarDemoTest {

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        Conexion.setCarpetaRaiz(tempDir);
        Empresas.getEmpresas().cerrar();
    }

    @AfterEach
    void tearDown() {
        Conexion.cerrarConexion();
    }

    @Test
    void sentenciaSimple() {
        assertEquals(List.of("SELECT 1", ""), CargarDemo.trocear("SELECT 1;"));
    }

    @Test
    void dosSentencias() {
        assertEquals(List.of("SELECT 1", " SELECT 2", ""), CargarDemo.trocear("SELECT 1; SELECT 2;"));
    }

    @Test
    void puntoYComaDentroDeCadenaNoCorta() {
        assertEquals(List.of("INSERT INTO t VALUES ('Montaje; incluye transporte')", ""),
                CargarDemo.trocear("INSERT INTO t VALUES ('Montaje; incluye transporte');"));
    }

    @Test
    void comillaEscapadaNoCortaLaCadena() {
        assertEquals(List.of("INSERT INTO t VALUES ('L''Hospitalet; centro')", ""),
                CargarDemo.trocear("INSERT INTO t VALUES ('L''Hospitalet; centro');"));
    }

    @Test
    void cargarDejaLogoYModoLogo() throws Exception {
        CargarDemo.cargar();
        Conexion.setEmpresaActiva(CargarDemo.CARPETA);
        try (Statement sentencia = Conexion.establecerConexion().createStatement();
             ResultSet fila = sentencia.executeQuery("SELECT cabecera_modo, logo_path FROM empresa WHERE id = 1")) {
            fila.next();
            assertEquals("LOGO", fila.getString("cabecera_modo"));
            Path logo = Path.of(fila.getString("logo_path"));
            assertTrue(Files.exists(logo));
            assertEquals(Conexion.rutaBaseDe(CargarDemo.CARPETA).getParent(), logo.getParent());
        }
    }

    @Test
    void cargarDosVecesDejaUnSoloLogo() throws Exception {
        CargarDemo.cargar();
        Empresas.getEmpresas().baja(CargarDemo.CARPETA);
        CargarDemo.cargar();
        File carpetaDemo = Conexion.rutaBaseDe(CargarDemo.CARPETA).getParent().toFile();
        int copias = 0;
        for (File fichero : carpetaDemo.listFiles()) {
            if (fichero.getName().startsWith("logo")) {
                copias++;
            }
        }
        assertEquals(1, copias);
    }
}
