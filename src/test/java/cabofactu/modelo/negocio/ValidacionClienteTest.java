package cabofactu.modelo.negocio;

import cabofactu.modelo.dominio.Cliente;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Comprobamos cada mensaje de ValidacionCliente con su caso, sin base de datos. */
class ValidacionClienteTest {

    private Cliente completo() {
        Cliente c = new Cliente();
        c.setNombre("Cliente Prueba");
        c.setNif("12345678Z");
        c.setDireccion("Calle Prueba 1");
        c.setCp("28001");
        c.setLocalidad("Madrid");
        c.setProvincia("Madrid");
        c.setEmail("cliente@prueba.es");
        return c;
    }

    @Test
    void nombreVacioDaError() {
        assertEquals("Indique el nombre del cliente.", ValidacionCliente.errorNombre(null));
        assertEquals("Indique el nombre del cliente.", ValidacionCliente.errorNombre("   "));
        assertNull(ValidacionCliente.errorNombre("Cliente Prueba"));
    }

    @Test
    void nifDistingueVacioFormatoYLetra() {
        assertEquals("El NIF/NIE es obligatorio.", ValidacionCliente.errorNif(""));
        assertEquals("Formato NIF/NIE incorrecto. Debe ser como 12345678Z (DNI), X1234567L (NIE) o B12345674 (CIF).",
                ValidacionCliente.errorNif("123"));
        assertEquals("La letra no es correcta.", ValidacionCliente.errorNif("12345678A"));
        assertNull(ValidacionCliente.errorNif("12345678Z"));
    }

    @Test
    void direccionVaciaDaError() {
        assertEquals("La dirección del cliente es obligatoria.", ValidacionCliente.errorDireccion(""));
        assertNull(ValidacionCliente.errorDireccion("Calle Prueba 1"));
    }

    @Test
    void codigoPostalDistingueVacioYNoValido() {
        assertEquals("El código postal es obligatorio.", ValidacionCliente.errorCodigoPostal(null));
        assertEquals("El código postal debe tener cinco dígitos y comenzar entre 01 y 52.",
                ValidacionCliente.errorCodigoPostal("123"));
        assertNull(ValidacionCliente.errorCodigoPostal("28001"));
    }

    @Test
    void localidadVaciaDaError() {
        assertEquals("La localidad del cliente es obligatoria.", ValidacionCliente.errorLocalidad(""));
        assertNull(ValidacionCliente.errorLocalidad("Madrid"));
    }

    @Test
    void provinciaVaciaDaError() {
        assertEquals("La provincia del cliente es obligatoria.", ValidacionCliente.errorProvincia("  "));
        assertNull(ValidacionCliente.errorProvincia("Madrid"));
    }

    @Test
    void emailVacioValeYMalEscritoDaError() {
        assertNull(ValidacionCliente.errorEmail(""));
        assertNull(ValidacionCliente.errorEmail(null));
        assertEquals("Revise el formato del correo electrónico.", ValidacionCliente.errorEmail("pepe@"));
        assertNull(ValidacionCliente.errorEmail("pepe@correo.es"));
    }

    @Test
    void comprobarNuloPideDatos() {
        Cliente nulo = null;
        ValidacionException e = assertThrows(ValidacionException.class, () -> ValidacionCliente.comprobar(nulo));
        assertEquals("Indique los datos del cliente.", e.getMessage());
    }

    @Test
    void comprobarAvisaPrimeroDelNombre() {
        Cliente c = completo();
        c.setNombre("");
        c.setNif("");
        ValidacionException e = assertThrows(ValidacionException.class, () -> ValidacionCliente.comprobar(c));
        assertEquals("Indique el nombre del cliente.", e.getMessage());
    }

    @Test
    void comprobarAceptaClienteCompleto() {
        assertDoesNotThrow(() -> ValidacionCliente.comprobar(completo()));
    }

    @Test
    void comprobarAceptaEmailVacio() {
        Cliente c = completo();
        c.setEmail("");
        assertDoesNotThrow(() -> ValidacionCliente.comprobar(c));
    }
}
