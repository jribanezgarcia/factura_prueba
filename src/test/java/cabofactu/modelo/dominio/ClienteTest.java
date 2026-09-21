package cabofactu.modelo.dominio;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Comprobamos que Cliente se valida sola: el constructor y los setters
 * comprueban los datos y lanzan Exception con el mensaje para el usuario.
 */
class ClienteTest {

    private Cliente clienteValido() throws Exception {
        return new Cliente("Ana García", "12345678Z", "Calle Mayor 1", "28013", "Madrid", "Madrid");
    }

    @Test
    void nombreVacio() throws Exception {
        Exception e = assertThrows(Exception.class, () -> clienteValido().setNombre("   "));
        assertEquals("Indique el nombre del cliente.", e.getMessage());
    }

    @Test
    void nifVacio() throws Exception {
        Exception e = assertThrows(Exception.class, () -> clienteValido().setNif(""));
        assertEquals("El NIF/NIE es obligatorio.", e.getMessage());
    }

    @Test
    void nifConFormatoMalo() throws Exception {
        Exception e = assertThrows(Exception.class, () -> clienteValido().setNif("123"));
        assertEquals("Formato NIF/NIE incorrecto. Debe ser como 12345678Z (DNI), "
                + "X1234567L (NIE) o B12345674 (CIF).", e.getMessage());
    }

    @Test
    void nifConLetraMala() throws Exception {
        Exception e = assertThrows(Exception.class, () -> clienteValido().setNif("12345678A"));
        assertEquals("La letra no es correcta.", e.getMessage());
    }

    @Test
    void nifEnMinusculasSeGuardaEnMayusculas() throws Exception {
        Cliente c = clienteValido();
        c.setNif("12345678z");
        assertEquals("12345678Z", c.getNif());
    }

    @Test
    void cpVacio() throws Exception {
        Exception e = assertThrows(Exception.class, () -> clienteValido().setCp(""));
        assertEquals("El código postal es obligatorio.", e.getMessage());
    }

    @Test
    void cpDeCuatroDigitos() throws Exception {
        Exception e = assertThrows(Exception.class, () -> clienteValido().setCp("2801"));
        assertEquals("El código postal debe tener cinco dígitos y comenzar entre 01 y 52.",
                e.getMessage());
    }

    @Test
    void cpQueEmpiezaPor53() throws Exception {
        Exception e = assertThrows(Exception.class, () -> clienteValido().setCp("53001"));
        assertEquals("El código postal debe tener cinco dígitos y comenzar entre 01 y 52.",
                e.getMessage());
    }

    @Test
    void localidadVacia() throws Exception {
        Exception e = assertThrows(Exception.class, () -> clienteValido().setLocalidad(""));
        assertEquals("La localidad del cliente es obligatoria.", e.getMessage());
    }

    @Test
    void provinciaVacia() throws Exception {
        Exception e = assertThrows(Exception.class, () -> clienteValido().setProvincia(""));
        assertEquals("La provincia del cliente es obligatoria.", e.getMessage());
    }

    @Test
    void emailVacioQueVale() throws Exception {
        Cliente c = clienteValido();
        c.setEmail("   ");
        assertEquals("", c.getEmail());
    }

    @Test
    void emailMalEscrito() throws Exception {
        Exception e = assertThrows(Exception.class, () -> clienteValido().setEmail("ana@"));
        assertEquals("Revise el formato del correo electrónico.", e.getMessage());
    }

    @Test
    void constructorCopia() throws Exception {
        Cliente original = clienteValido();
        original.setEmail("ana@correo.es");
        Cliente copia = new Cliente(original);
        assertNotSame(original, copia);
        assertEquals(original.getNombre(), copia.getNombre());
        assertEquals(original.getNif(), copia.getNif());
        assertEquals(original.getDireccion(), copia.getDireccion());
        assertEquals(original.getCp(), copia.getCp());
        assertEquals(original.getLocalidad(), copia.getLocalidad());
        assertEquals(original.getProvincia(), copia.getProvincia());
        assertEquals(original.getEmail(), copia.getEmail());
        assertEquals(original, copia);
        copia.setNombre("Otra Persona");
        assertNotEquals(original.getNombre(), copia.getNombre());
    }

    @Test
    void equalsPorNif() throws Exception {
        Cliente uno = clienteValido();
        Cliente otro = new Cliente("Empresa Distinta", "12345678Z", "Calle 2", "49001", "Zamora", "Zamora");
        assertEquals(uno, otro);
        assertEquals(uno.hashCode(), otro.hashCode());
        Cliente distinto = new Cliente("Ana García", "B77777779", "Calle Mayor 1", "28013", "Madrid", "Madrid");
        assertNotEquals(uno, distinto);
    }

    @Test
    void errorXdiceLoMismoQueElSetter() {
        assertEquals("Indique el nombre del cliente.", Cliente.errorNombre("   "));
        assertEquals("El NIF/NIE es obligatorio.", Cliente.errorNif(""));
        assertEquals("Formato NIF/NIE incorrecto. Debe ser como 12345678Z (DNI), "
                + "X1234567L (NIE) o B12345674 (CIF).", Cliente.errorNif("123"));
        assertEquals("La letra no es correcta.", Cliente.errorNif("12345678A"));
        assertEquals("La dirección del cliente es obligatoria.", Cliente.errorDireccion("   "));
        assertEquals("El código postal es obligatorio.", Cliente.errorCp(""));
        assertEquals("El código postal debe tener cinco dígitos y comenzar entre 01 y 52.",
                Cliente.errorCp("2801"));
        assertEquals("La localidad del cliente es obligatoria.", Cliente.errorLocalidad(""));
        assertEquals("La provincia del cliente es obligatoria.", Cliente.errorProvincia(""));
        assertEquals("Revise el formato del correo electrónico.", Cliente.errorEmail("ana@"));
    }

    @Test
    void errorXdevuelveNullConUnDatoCorrecto() {
        assertNull(Cliente.errorNombre("Ana García"));
        assertNull(Cliente.errorNif("12345678Z"));
        assertNull(Cliente.errorDireccion("Calle Mayor 1"));
        assertNull(Cliente.errorCp("28013"));
        assertNull(Cliente.errorLocalidad("Madrid"));
        assertNull(Cliente.errorProvincia("Madrid"));
        assertNull(Cliente.errorEmail("ana@correo.es"));
    }

    @Test
    void errorEmailConVacioDevuelveNull() {
        assertNull(Cliente.errorEmail(""));
        assertNull(Cliente.errorEmail(null));
        assertNull(Cliente.errorEmail("   "));
    }

    @Test
    void tieneLosMismosDatosConUnaCopia() throws Exception {
        Cliente original = clienteValido();
        original.setEmail("ana@correo.es");
        assertTrue(original.tieneLosMismosDatos(new Cliente(original)));
    }

    @Test
    void tieneLosMismosDatosFalseSiCambiaCualquierDato() throws Exception {
        Cliente original = clienteValido();
        Cliente otro = new Cliente(original);
        otro.setNombre("Otra Persona");
        assertFalse(original.tieneLosMismosDatos(otro));
        otro = new Cliente(original);
        otro.setNif("B77777779");
        assertFalse(original.tieneLosMismosDatos(otro));
        otro = new Cliente(original);
        otro.setDireccion("Calle 2");
        assertFalse(original.tieneLosMismosDatos(otro));
        otro = new Cliente(original);
        otro.setCp("49001");
        assertFalse(original.tieneLosMismosDatos(otro));
        otro = new Cliente(original);
        otro.setLocalidad("Zamora");
        assertFalse(original.tieneLosMismosDatos(otro));
        otro = new Cliente(original);
        otro.setProvincia("Zamora");
        assertFalse(original.tieneLosMismosDatos(otro));
        otro = new Cliente(original);
        otro.setEmail("otra@correo.es");
        assertFalse(original.tieneLosMismosDatos(otro));
    }

    @Test
    void tieneLosMismosDatosConNullEsFalse() throws Exception {
        assertFalse(clienteValido().tieneLosMismosDatos(null));
    }

    @Test
    void tieneLosMismosDatosIgnoraIdYActivo() throws Exception {
        Cliente original = clienteValido();
        original.setEmail("ana@correo.es");
        Cliente otro = new Cliente(original);
        otro.setId(99L);
        otro.setActivo(false);
        assertTrue(original.tieneLosMismosDatos(otro));
    }
}