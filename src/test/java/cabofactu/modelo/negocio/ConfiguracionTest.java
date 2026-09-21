package cabofactu.modelo.negocio;

import cabofactu.modelo.dominio.Empresa;
import cabofactu.modelo.negocio.sqlite.ConfiguracionDAO;
import cabofactu.modelo.negocio.sqlite.Conexion;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfiguracionTest {

    @TempDir
    Path tempDir;

    private final Configuracion service = new Configuracion(new ConfiguracionDAO());

    private Empresa completa() {
        Empresa e = new Empresa();
        e.setNombre("Talleres Ejemplo S.L.");
        e.setNif("12345678Z");
        e.setDireccion("Calle Mayor 1");
        e.setCp("28001");
        e.setLocalidad("Madrid");
        e.setProvincia("Madrid");
        e.setEmail("taller@ejemplo.es");
        e.setTelefono("910000000");
        return e;
    }

    @BeforeEach
    void setUp() {
        Conexion.setCarpetaRaiz(tempDir);
        Empresas.getEmpresas().cerrar();
    }

    @AfterEach
    void tearDown() {
        Conexion.cerrarConexion();
    }

    private Configuracion conEmpresaGuardada(Empresa empresa) throws Exception {
        Empresas.getEmpresas().alta("Prueba");
        Empresas.getEmpresas().abrir("prueba", LocalDate.now());
        ConfiguracionDAO dao = new ConfiguracionDAO();
        dao.saveEmpresa(empresa);
        return new Configuracion(dao);
    }

    @Test
    void empresaVaciaDevuelveLasOchoEtiquetasEnOrden() {
        assertEquals(
                List.of("Nombre / razón social", "NIF", "Dirección", "CP",
                        "Localidad", "Provincia", "Email", "Teléfono"),
                service.datosPendientes(new Empresa()));
    }

    @Test
    void empresaCompletaYValidaDevuelveListaVacia() {
        assertTrue(service.datosPendientes(completa()).isEmpty());
    }

    @Test
    void nifNoValidoDevuelveNif() {
        Empresa e = completa();
        e.setNif("12345678A");
        assertEquals(List.of("NIF"), service.datosPendientes(e));
    }

    @Test
    void cpInexistenteDevuelveCp() {
        Empresa e = completa();
        e.setCp("99999");
        assertEquals(List.of("CP"), service.datosPendientes(e));
    }

    @Test
    void emailMalFormadoDevuelveEmail() {
        Empresa e = completa();
        e.setEmail("taller@ejemplo");
        assertEquals(List.of("Email"), service.datosPendientes(e));
    }

    @Test
    void camposConSoloEspaciosCuentanComoVacios() {
        Empresa e = new Empresa();
        e.setNombre("   ");
        e.setNif("   ");
        e.setDireccion("  ");
        e.setCp("   ");
        e.setLocalidad(" ");
        e.setProvincia("  ");
        e.setEmail("   ");
        e.setTelefono(" ");
        assertEquals(
                List.of("Nombre / razón social", "NIF", "Dirección", "CP",
                        "Localidad", "Provincia", "Email", "Teléfono"),
                service.datosPendientes(e));
    }

    @Test
    void comprobarEmpresaCompletaNombraLoQueFalta() throws Exception {
        Empresa e = completa();
        e.setTelefono("");
        Configuracion servicio = conEmpresaGuardada(e);
        Exception lanzada = assertThrows(Exception.class, () -> servicio.comprobarEmpresaCompleta());
        assertTrue(lanzada.getMessage().contains("Teléfono"));
    }

    @Test
    void comprobarEmpresaCompletaNoLanzaConLaEmpresaCompleta() throws Exception {
        Configuracion servicio = conEmpresaGuardada(completa());
        servicio.comprobarEmpresaCompleta();
    }
}
