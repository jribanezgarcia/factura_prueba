package cabofactu.modelo.negocio;

import cabofactu.modelo.negocio.sqlite.Conexion;
import cabofactu.modelo.negocio.sqlite.Migraciones;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import cabofactu.modelo.dominio.Empresa;
import cabofactu.modelo.dominio.Serie;
import cabofactu.modelo.negocio.sqlite.ConfiguracionDAO;
import cabofactu.modelo.negocio.sqlite.SerieDAO;

class EmpresasTest {

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        Conexion.setCarpetaRaiz(tempDir);
        Conexion.cerrarConexion();
        Sesion.reiniciar();
    }

    @AfterEach
    void tearDown() {
        Conexion.cerrarConexion();
    }

    @Test
    void slugNormalizaNombre() {
        assertEquals("mi_empresa", Empresas.slugDe("Mi Empresa"));
        assertEquals("acme_s_a", Empresas.slugDe("ACME S.A."));
        assertEquals("ole", Empresas.slugDe("Ólé"));
    }

    @Test
    void crearCreaLaBaseSinActivarla() throws Exception {
        Empresas.EmpresaInfo e = Empresas.crearEmpresa("Mi Empresa");
        assertEquals("mi_empresa", e.slug());
        assertEquals("Mi Empresa", e.nombre());
        assertTrue(Conexion.getEmpresasDisponibles().contains("mi_empresa"));
        assertTrue(Files.exists(Conexion.rutaBaseDe("mi_empresa")));
    }

    @Test
    void listarMuestraNombreDelCatalogo() throws Exception {
        Empresas.crearEmpresa("Empresa Prueba");
        Empresas.crearEmpresa("Otra S.L.");
        List<Empresas.EmpresaInfo> lista = Empresas.listarEmpresas();
        assertTrue(lista.stream().anyMatch(x -> x.slug().equals("empresa_prueba") && x.nombre().equals("Empresa Prueba")));
        assertTrue(lista.stream().anyMatch(x -> x.slug().equals("otra_s_l") && x.nombre().equals("Otra S.L.")));
    }

    @Test
    void dosEmpresasNoCompartenDatos() throws Exception {
        Empresas.crearEmpresa("Primera");
        Empresas.conectar("primera", LocalDate.now());
        Conexion.establecerConexion();
        var repo = new SerieDAO();
        var s = new Serie();
        s.setCodigo("A");
        s.setDescripcion("Serie de la primera");
        s.setSiguienteCorrelativo(1);
        s.setEsRectificativa(false);
        s.setReutilizarAnulados(false);
        long idPrimera = repo.insertar(s, LocalDate.now().getYear());

        Empresas.crearEmpresa("Segunda");
        Empresas.conectar("segunda", LocalDate.now());
        Conexion.establecerConexion();
        assertEquals(1, repo.getSiguiente(idPrimera, LocalDate.now().getYear()));
        assertTrue(repo.listar().stream().noneMatch(x -> "Serie de la primera".equals(x.getDescripcion())));
    }

    @Test
    void eliminarEmpresaBorraCarpeta() throws Exception {
        Empresas.crearEmpresa("Para Borrar");
        Empresas.crearEmpresa("Mantener");
        Empresas.conectar("mantener", LocalDate.now());
        Conexion.establecerConexion();
        String slug = "para_borrar";
        assertTrue(Files.exists(Conexion.rutaBaseDe(slug)));
        Empresas.eliminarEmpresa(slug);
        assertFalse(Files.exists(Conexion.carpetaRaiz().resolve(slug)));
        assertFalse(Empresas.listarEmpresas().stream().anyMatch(e -> e.slug().equals(slug)));
    }

    @Test
    void noSePuedeEliminarLaActiva() throws Exception {
        Empresas.crearEmpresa("Activa");
        Empresas.conectar("activa", LocalDate.now());
        assertThrows(IllegalArgumentException.class,
                () -> Empresas.eliminarEmpresa(Sesion.empresaSlug()));
    }

    @Test
    void crearNoCambiaLaEmpresaActiva() throws Exception {
        Empresas.crearEmpresa("Empresa A");
        Empresas.conectar("empresa_a", LocalDate.now());
        String slugAntes = Sesion.empresaSlug();
        Path dirAntes = Conexion.carpetaEmpresa();

        Empresas.crearEmpresa("Empresa B");

        assertEquals(slugAntes, Sesion.empresaSlug());
        assertEquals(dirAntes, Conexion.carpetaEmpresa());
        assertEquals("empresa_a", PreferenciasGlobales.get(PreferenciasGlobales.ULTIMA_EMPRESA));
    }

    @Test
    void crearNoRompeLaConexionEnCurso() throws Exception {
        Empresas.crearEmpresa("Empresa A");
        Empresas.conectar("empresa_a", LocalDate.now());
        Conexion.establecerConexion();
        var repo = new SerieDAO();
        var s = new Serie();
        s.setCodigo("A");
        s.setDescripcion("Serie persistente");
        s.setSiguienteCorrelativo(1);
        s.setEsRectificativa(false);
        s.setReutilizarAnulados(false);
        long id = repo.insertar(s, LocalDate.now().getYear());

        Empresas.crearEmpresa("Empresa B");

        assertEquals(1, repo.getSiguiente(id, LocalDate.now().getYear()));
        assertTrue(repo.listar().stream().anyMatch(x -> "Serie persistente".equals(x.getDescripcion())));
    }

    @Test
    void laBaseNuevaTieneElEsquemaCompleto() throws Exception {
        Empresas.crearEmpresa("Esquema Completa");
        Path base = Conexion.rutaBaseDe("esquema_completa");
        try (var c = DriverManager.getConnection("jdbc:sqlite:" + base);
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("PRAGMA user_version")) {
            assertTrue(rs.next());
            assertEquals(Migraciones.ultimaVersion(), rs.getInt(1));
        }
    }

    @Test
    void conectarRecuerdaElTemaDeCadaEmpresa() throws Exception {
        Empresas.crearEmpresa("Primera");
        Empresas.crearEmpresa("Segunda");
        Empresas.conectar("primera", LocalDate.now());
        new ConfiguracionDAO().setPreferencia(PreferenciasGlobales.TEMA, "omarchy");
        Empresas.conectar("segunda", LocalDate.now());
        assertEquals("", PreferenciasGlobales.get(PreferenciasGlobales.TEMA));
        new ConfiguracionDAO().setPreferencia(PreferenciasGlobales.TEMA, "esmeralda");
        Empresas.conectar("primera", LocalDate.now());
        assertEquals("omarchy", PreferenciasGlobales.get(PreferenciasGlobales.TEMA));
        Empresas.conectar("segunda", LocalDate.now());
        assertEquals("esmeralda", PreferenciasGlobales.get(PreferenciasGlobales.TEMA));
    }
}
