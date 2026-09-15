package cabofactu;

import cabofactu.modelo.negocio.sqlite.CargarDemo;
import cabofactu.modelo.negocio.sqlite.Conexion;
import cabofactu.modelo.negocio.Empresas;
import cabofactu.modelo.negocio.PreferenciasGlobales;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PreparacionDatosTest {

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
    void crearCarpetaEsIdempotente() throws Exception {
        Path sub = tempDir.resolve("sub").resolve("datos");
        Conexion.setCarpetaRaiz(sub);
        PreparacionDatos.crearCarpeta();
        assertTrue(Files.isDirectory(sub));
        PreparacionDatos.crearCarpeta();
        assertTrue(Files.isDirectory(sub));
    }

    @Test
    void sinEmpresasCargaLaDemoYLaDejaComoUltima() throws Exception {
        assertTrue(PreparacionDatos.cargarDemoSiNoHayEmpresas());
        assertTrue(Empresas.listarEmpresas().stream()
                .anyMatch(e -> e.slug().equals(CargarDemo.SLUG)));
        assertEquals(CargarDemo.SLUG,
                PreferenciasGlobales.get(PreferenciasGlobales.ULTIMA_EMPRESA));
    }

    @Test
    void segundaLlamadaNoDuplica() throws Exception {
        assertTrue(PreparacionDatos.cargarDemoSiNoHayEmpresas());
        assertFalse(PreparacionDatos.cargarDemoSiNoHayEmpresas());
        assertEquals(1, Empresas.listarEmpresas().size());
    }

    @Test
    void conOtraEmpresaNoCargaLaDemo() throws Exception {
        Empresas.crearEmpresa("Otra");
        assertFalse(PreparacionDatos.cargarDemoSiNoHayEmpresas());
        assertEquals(1, Empresas.listarEmpresas().size());
        assertFalse(Empresas.listarEmpresas().stream()
                .anyMatch(e -> e.slug().equals(CargarDemo.SLUG)));
    }
}
