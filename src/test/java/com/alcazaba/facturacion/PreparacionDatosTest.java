package com.alcazaba.facturacion;

import com.alcazaba.facturacion.db.CargarDemo;
import com.alcazaba.facturacion.db.Database;
import com.alcazaba.facturacion.service.EmpresaManager;
import com.alcazaba.facturacion.service.PreferenciasGlobales;
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
        Database.setDataDir(tempDir);
        Database.resetConnection();
    }

    @AfterEach
    void tearDown() {
        Database.resetConnection();
    }

    @Test
    void crearCarpetaEsIdempotente() throws Exception {
        Path sub = tempDir.resolve("sub").resolve("datos");
        Database.setDataDir(sub);
        PreparacionDatos.crearCarpeta();
        assertTrue(Files.isDirectory(sub));
        PreparacionDatos.crearCarpeta();
        assertTrue(Files.isDirectory(sub));
    }

    @Test
    void sinEmpresasCargaLaDemoYLaDejaComoUltima() throws Exception {
        assertTrue(PreparacionDatos.cargarDemoSiNoHayEmpresas());
        assertTrue(EmpresaManager.listarEmpresas().stream()
                .anyMatch(e -> e.slug().equals(CargarDemo.SLUG)));
        assertEquals(CargarDemo.SLUG,
                PreferenciasGlobales.get(PreferenciasGlobales.ULTIMA_EMPRESA));
    }

    @Test
    void segundaLlamadaNoDuplica() throws Exception {
        assertTrue(PreparacionDatos.cargarDemoSiNoHayEmpresas());
        assertFalse(PreparacionDatos.cargarDemoSiNoHayEmpresas());
        assertEquals(1, EmpresaManager.listarEmpresas().size());
    }

    @Test
    void conOtraEmpresaNoCargaLaDemo() throws Exception {
        EmpresaManager.crearEmpresa("Otra");
        assertFalse(PreparacionDatos.cargarDemoSiNoHayEmpresas());
        assertEquals(1, EmpresaManager.listarEmpresas().size());
        assertFalse(EmpresaManager.listarEmpresas().stream()
                .anyMatch(e -> e.slug().equals(CargarDemo.SLUG)));
    }
}
