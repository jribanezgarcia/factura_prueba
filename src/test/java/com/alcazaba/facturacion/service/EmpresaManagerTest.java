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
    void crearCreaLaBaseSinActivarla() throws Exception {
        EmpresaManager.EmpresaInfo e = EmpresaManager.crearEmpresa("Mi Empresa");
        assertEquals("mi_empresa", e.slug());
        assertEquals("Mi Empresa", e.nombre());
        assertTrue(Database.getEmpresasDisponibles().contains("mi_empresa"));
        assertTrue(Files.exists(Database.dbPathDe("mi_empresa")));
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
        EmpresaManager.conectar("primera", LocalDate.now());
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
        EmpresaManager.conectar("segunda", LocalDate.now());
        Database.getConnection();
        assertEquals(1, repo.getSiguiente(idPrimera, LocalDate.now().getYear()));
        assertTrue(repo.listar().stream().noneMatch(x -> "Serie de la primera".equals(x.getDescripcion())));
    }

    @Test
    void eliminarEmpresaBorraCarpeta() throws Exception {
        EmpresaManager.crearEmpresa("Para Borrar");
        EmpresaManager.crearEmpresa("Mantener");
        EmpresaManager.conectar("mantener", LocalDate.now());
        Database.getConnection();
        String slug = "para_borrar";
        assertTrue(Files.exists(Database.dbPathDe(slug)));
        EmpresaManager.eliminarEmpresa(slug);
        assertFalse(Files.exists(Database.baseDataDir().resolve(slug)));
        assertFalse(EmpresaManager.listarEmpresas().stream().anyMatch(e -> e.slug().equals(slug)));
    }

    @Test
    void noSePuedeEliminarLaActiva() throws Exception {
        EmpresaManager.crearEmpresa("Activa");
        EmpresaManager.conectar("activa", LocalDate.now());
        assertThrows(IllegalArgumentException.class,
                () -> EmpresaManager.eliminarEmpresa(Sesion.empresaSlug()));
    }

    @Test
    void crearNoCambiaLaEmpresaActiva() throws Exception {
        EmpresaManager.crearEmpresa("Empresa A");
        EmpresaManager.conectar("empresa_a", LocalDate.now());
        String slugAntes = Sesion.empresaSlug();
        Path dirAntes = Database.dataDir();

        EmpresaManager.crearEmpresa("Empresa B");

        assertEquals(slugAntes, Sesion.empresaSlug());
        assertEquals(dirAntes, Database.dataDir());
        assertEquals("empresa_a", PreferenciasGlobales.get(PreferenciasGlobales.ULTIMA_EMPRESA));
    }

    @Test
    void crearNoRompeLaConexionEnCurso() throws Exception {
        EmpresaManager.crearEmpresa("Empresa A");
        EmpresaManager.conectar("empresa_a", LocalDate.now());
        Database.getConnection();
        var repo = new com.alcazaba.facturacion.repository.SerieRepository();
        var s = new com.alcazaba.facturacion.model.Serie();
        s.setCodigo("A");
        s.setDescripcion("Serie persistente");
        s.setSiguienteCorrelativo(1);
        s.setEsRectificativa(false);
        s.setReutilizarAnulados(false);
        long id = repo.insertar(s);

        EmpresaManager.crearEmpresa("Empresa B");

        assertEquals(1, repo.getSiguiente(id, LocalDate.now().getYear()));
        assertTrue(repo.listar().stream().anyMatch(x -> "Serie persistente".equals(x.getDescripcion())));
    }

    @Test
    void laBaseNuevaTieneElEsquemaCompleto() throws Exception {
        EmpresaManager.crearEmpresa("Esquema Completa");
        Path base = Database.dbPathDe("esquema_completa");
        try (var c = DriverManager.getConnection("jdbc:sqlite:" + base);
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("PRAGMA user_version")) {
            assertTrue(rs.next());
            assertEquals(Migrations.ultimaVersion(), rs.getInt(1));
        }
    }
}
