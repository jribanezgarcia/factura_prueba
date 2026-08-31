package com.alcazaba.facturacion.service;

import com.alcazaba.facturacion.db.Database;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmpresaManagerTest {

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        Database.setDataDir(tempDir);
        Database.resetConnection();
        Sesion.reiniciar();
    }

    @AfterEach
    void tearDown() {
        Database.resetConnection();
    }

    @Test
    void slugNormalizaNombre() {
        assertEquals("mi_empresa", EmpresaManager.slugDe("Mi Empresa"));
        assertEquals("acme_s_a", EmpresaManager.slugDe("ACME S.A."));
        assertEquals("ole", EmpresaManager.slugDe("Ólé"));
    }

    @Test
    void creaEmpresaYLaDejaActiva() throws Exception {
        EmpresaManager.EmpresaInfo e = EmpresaManager.crearEmpresa("Mi Empresa");
        assertEquals("mi_empresa", e.slug());
        assertEquals("Mi Empresa", e.nombre());
        assertTrue(Database.getEmpresasDisponibles().contains("mi_empresa"));
        assertTrue(e.slug().equals(Sesion.empresaSlug()));
        assertTrue(Files.exists(Database.dbPath()));
    }

    @Test
    void listarMuestraNombreDelCatalogo() throws Exception {
        EmpresaManager.crearEmpresa("Comercial Alcazaba");
        EmpresaManager.crearEmpresa("Otra S.L.");
        List<EmpresaManager.EmpresaInfo> lista = EmpresaManager.listarEmpresas();
        assertTrue(lista.stream().anyMatch(x -> x.slug().equals("comercial_alcazaba") && x.nombre().equals("Comercial Alcazaba")));
        assertTrue(lista.stream().anyMatch(x -> x.slug().equals("otra_s_l") && x.nombre().equals("Otra S.L.")));
    }

    @Test
    void dosEmpresasNoCompartenDatos() throws Exception {
        EmpresaManager.crearEmpresa("Primera");
        Database.getConnection();
        var repo = new com.alcazaba.facturacion.repository.SerieRepository();
        var s = new com.alcazaba.facturacion.model.Serie();
        s.setCodigo("A");
        s.setDescripcion("Serie de la primera");
        s.setSiguienteCorrelativo(1);
        s.setEsRectificativa(false);
        s.setReutilizarAnulados(false);
        long idPrimera = repo.insertar(s);

        EmpresaManager.crearEmpresa("Segunda");
        Database.getConnection();
        assertEquals(1, repo.getSiguiente(idPrimera, LocalDate.now().getYear()));
        assertTrue(repo.listar().stream().noneMatch(x -> "Serie de la primera".equals(x.getDescripcion())));
    }

    @Test
    void eliminarEmpresaBorraCarpeta() throws Exception {
        EmpresaManager.crearEmpresa("Para Borrar");
        String slug = Sesion.empresaSlug();
        assertTrue(Files.exists(Database.dbPath()));
        EmpresaManager.crearEmpresa("Mantener");
        EmpresaManager.eliminarEmpresa(slug);
        assertFalse(Files.exists(Database.baseDataDir().resolve(slug)));
        assertFalse(EmpresaManager.listarEmpresas().stream().anyMatch(e -> e.slug().equals(slug)));
    }

    @Test
    void noSePuedeEliminarLaActiva() throws Exception {
        EmpresaManager.crearEmpresa("Activa");
        assertThrows(IllegalArgumentException.class,
                () -> EmpresaManager.eliminarEmpresa(Sesion.empresaSlug()));
    }
}
