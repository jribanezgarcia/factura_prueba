package cabofactu.modelo.negocio;

import cabofactu.modelo.dominio.Empresa;
import cabofactu.modelo.negocio.sqlite.ConfiguracionDAO;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfiguracionTest {

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
}
