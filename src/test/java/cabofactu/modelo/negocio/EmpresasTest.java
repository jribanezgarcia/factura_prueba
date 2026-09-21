package cabofactu.modelo.negocio;

import cabofactu.modelo.dominio.EmpresaDisponible;
import cabofactu.modelo.negocio.sqlite.Conexion;
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
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import cabofactu.modelo.dominio.Serie;
import cabofactu.modelo.negocio.sqlite.ConfiguracionDAO;
import cabofactu.modelo.negocio.sqlite.SerieDAO;

/**
 * Las empresas con la API nueva: alta, listado, baja, abrir y cerrar.
 * Cada prueba usa una carpeta temporal como raíz de datos.
 */
class EmpresasTest {

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

    private boolean estaEnElListado(String carpeta) throws Exception {
        List<EmpresaDisponible> lista = Empresas.getEmpresas().listado();
        for (EmpresaDisponible empresa : lista) {
            if (empresa.getCarpeta().equals(carpeta)) {
                return true;
            }
        }
        return false;
    }

    @Test
    void carpetaNormalizaNombre() {
        assertEquals("mi_empresa", Empresas.carpetaDe("Mi Empresa"));
        assertEquals("acme_s_a", Empresas.carpetaDe("ACME S.A."));
        assertEquals("ole", Empresas.carpetaDe("Ólé"));
    }

    @Test
    void altaCreaLaBaseSinAbrirla() throws Exception {
        EmpresaDisponible empresa = Empresas.getEmpresas().alta("Mi Empresa");
        assertEquals("mi_empresa", empresa.getCarpeta());
        assertEquals("Mi Empresa", empresa.getNombre());
        assertTrue(estaEnElListado("mi_empresa"));
        assertTrue(Files.exists(Conexion.rutaBaseDe("mi_empresa")));
        assertNull(Sesion.getSesion().getCarpetaEmpresa());
    }

    @Test
    void altaConNombreVacioFalla() {
        assertThrows(Exception.class, () -> Empresas.getEmpresas().alta(""));
        assertThrows(Exception.class, () -> Empresas.getEmpresas().alta(null));
    }

    @Test
    void listadoMuestraNombreDelCatalogoOrdenado() throws Exception {
        Empresas.getEmpresas().alta("Otra S.L.");
        Empresas.getEmpresas().alta("Empresa Prueba");
        List<EmpresaDisponible> lista = Empresas.getEmpresas().listado();
        assertEquals(2, lista.size());
        assertEquals("empresa_prueba", lista.get(0).getCarpeta());
        assertEquals("Empresa Prueba", lista.get(0).getNombre());
        assertEquals("otra_s_l", lista.get(1).getCarpeta());
        assertEquals("Otra S.L.", lista.get(1).getNombre());
    }

    @Test
    void dosEmpresasNoCompartenDatos() throws Exception {
        Empresas.getEmpresas().alta("Primera");
        Empresas.getEmpresas().abrir("primera", LocalDate.now());
        Conexion.establecerConexion();
        SerieDAO repo = new SerieDAO();
        Serie s = new Serie();
        s.setCodigo("A");
        s.setDescripcion("Serie de la primera");
        s.setSiguienteCorrelativo(1);
        s.setEsRectificativa(false);
        s.setReutilizarAnulados(false);
        long idPrimera = repo.insertar(s, LocalDate.now().getYear());

        Empresas.getEmpresas().alta("Segunda");
        Empresas.getEmpresas().abrir("segunda", LocalDate.now());
        Conexion.establecerConexion();
        assertEquals(1, repo.getSiguiente(idPrimera, LocalDate.now().getYear()));
        boolean encontrada = false;
        List<Serie> series = repo.listar();
        for (Serie serie : series) {
            if ("Serie de la primera".equals(serie.getDescripcion())) {
                encontrada = true;
            }
        }
        assertFalse(encontrada);
    }

    @Test
    void bajaBorraLaCarpeta() throws Exception {
        Empresas.getEmpresas().alta("Para Borrar");
        Empresas.getEmpresas().alta("Mantener");
        Empresas.getEmpresas().abrir("mantener", LocalDate.now());
        Conexion.establecerConexion();
        String carpeta = "para_borrar";
        assertTrue(Files.exists(Conexion.rutaBaseDe(carpeta)));
        Empresas.getEmpresas().baja(carpeta);
        assertFalse(Files.exists(Conexion.carpetaRaiz().resolve(carpeta)));
        assertFalse(estaEnElListado(carpeta));
    }

    @Test
    void laEmpresaEnUsoNoSePuedeDarDeBaja() throws Exception {
        Empresas.getEmpresas().alta("Activa");
        Empresas.getEmpresas().abrir("activa", LocalDate.now());
        assertThrows(Exception.class, () -> Empresas.getEmpresas().baja("activa"));
    }

    @Test
    void altaNoCambiaLaEmpresaAbierta() throws Exception {
        Empresas.getEmpresas().alta("Empresa A");
        Empresas.getEmpresas().abrir("empresa_a", LocalDate.now());
        Path dirAntes = Conexion.carpetaEmpresa();

        Empresas.getEmpresas().alta("Empresa B");

        assertEquals("empresa_a", Sesion.getSesion().getCarpetaEmpresa());
        assertEquals(dirAntes, Conexion.carpetaEmpresa());
        assertEquals("empresa_a", PreferenciasGlobales.get(PreferenciasGlobales.ULTIMA_EMPRESA));
    }

    @Test
    void altaNoRompeLaConexionEnCurso() throws Exception {
        Empresas.getEmpresas().alta("Empresa A");
        Empresas.getEmpresas().abrir("empresa_a", LocalDate.now());
        Conexion.establecerConexion();
        SerieDAO repo = new SerieDAO();
        Serie s = new Serie();
        s.setCodigo("A");
        s.setDescripcion("Serie persistente");
        s.setSiguienteCorrelativo(1);
        s.setEsRectificativa(false);
        s.setReutilizarAnulados(false);
        long id = repo.insertar(s, LocalDate.now().getYear());

        Empresas.getEmpresas().alta("Empresa B");

        assertEquals(1, repo.getSiguiente(id, LocalDate.now().getYear()));
        boolean encontrada = false;
        List<Serie> series = repo.listar();
        for (Serie serie : series) {
            if ("Serie persistente".equals(serie.getDescripcion())) {
                encontrada = true;
            }
        }
        assertTrue(encontrada);
    }

    @Test
    void laBaseNuevaTieneElEsquemaCompleto() throws Exception {
        Empresas.getEmpresas().alta("Esquema Completa");
        Path base = Conexion.rutaBaseDe("esquema_completa");
        try (Connection c = DriverManager.getConnection("jdbc:sqlite:" + base);
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='empresa'")) {
            assertTrue(rs.next());
        }
    }

    @Test
    void abrirRecuerdaElTemaDeCadaEmpresa() throws Exception {
        Empresas.getEmpresas().alta("Primera");
        Empresas.getEmpresas().alta("Segunda");
        Empresas.getEmpresas().abrir("primera", LocalDate.now());
        new ConfiguracionDAO().setPreferencia(PreferenciasGlobales.TEMA, "omarchy");
        Empresas.getEmpresas().abrir("segunda", LocalDate.now());
        assertEquals("", PreferenciasGlobales.get(PreferenciasGlobales.TEMA));
        new ConfiguracionDAO().setPreferencia(PreferenciasGlobales.TEMA, "esmeralda");
        Empresas.getEmpresas().abrir("primera", LocalDate.now());
        assertEquals("omarchy", PreferenciasGlobales.get(PreferenciasGlobales.TEMA));
        Empresas.getEmpresas().abrir("segunda", LocalDate.now());
        assertEquals("esmeralda", PreferenciasGlobales.get(PreferenciasGlobales.TEMA));
    }

    @Test
    void cerrarDejaLaSesionSinEmpresa() throws Exception {
        Empresas.getEmpresas().alta("Activa");
        Empresas.getEmpresas().abrir("activa", LocalDate.now());
        Empresas.getEmpresas().cerrar();
        assertNull(Sesion.getSesion().getCarpetaEmpresa());
        assertNull(Sesion.getSesion().getFechaTrabajo());
    }
}
