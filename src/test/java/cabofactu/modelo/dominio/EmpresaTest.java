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
 * Comprobamos que Empresa se valida sola: el constructor y los setters
 * comprueban los datos y lanzan Exception con el mensaje para el usuario.
 */
class EmpresaTest {

    private Empresa empresaValida() throws Exception {
        return new Empresa("Talleres Ejemplo S.L.", "12345678Z", "Calle Mayor 1", "28001",
                "Madrid", "Madrid", "taller@ejemplo.es", "910000000");
    }

    @Test
    void nombreVacio() throws Exception {
        Exception e = assertThrows(Exception.class, () -> empresaValida().setNombre("   "));
        assertEquals("Indique el nombre o razón social de la empresa.", e.getMessage());
    }

    @Test
    void nifVacio() throws Exception {
        Exception e = assertThrows(Exception.class, () -> empresaValida().setNif(""));
        assertEquals("El NIF/NIE es obligatorio.", e.getMessage());
    }

    @Test
    void nifConFormatoMalo() throws Exception {
        Exception e = assertThrows(Exception.class, () -> empresaValida().setNif("123"));
        assertEquals("Formato NIF/NIE incorrecto. Debe ser como 12345678Z (DNI), "
                + "X1234567L (NIE) o B12345674 (CIF).", e.getMessage());
    }

    @Test
    void nifConLetraMala() throws Exception {
        Exception e = assertThrows(Exception.class, () -> empresaValida().setNif("12345678A"));
        assertEquals("La letra no es correcta.", e.getMessage());
    }

    @Test
    void nifEnMinusculasSeGuardaEnMayusculas() throws Exception {
        Empresa e = empresaValida();
        e.setNif("12345678z");
        assertEquals("12345678Z", e.getNif());
    }

    @Test
    void direccionVacia() throws Exception {
        Exception e = assertThrows(Exception.class, () -> empresaValida().setDireccion(""));
        assertEquals("La dirección de la empresa es obligatoria.", e.getMessage());
    }

    @Test
    void cpVacio() throws Exception {
        Exception e = assertThrows(Exception.class, () -> empresaValida().setCp(""));
        assertEquals("El código postal es obligatorio.", e.getMessage());
    }

    @Test
    void cpQueEmpiezaPor53() throws Exception {
        Exception e = assertThrows(Exception.class, () -> empresaValida().setCp("53001"));
        assertEquals("El código postal debe tener cinco dígitos y comenzar entre 01 y 52.",
                e.getMessage());
    }

    @Test
    void localidadVacia() throws Exception {
        Exception e = assertThrows(Exception.class, () -> empresaValida().setLocalidad(""));
        assertEquals("La localidad de la empresa es obligatoria.", e.getMessage());
    }

    @Test
    void provinciaVacia() throws Exception {
        Exception e = assertThrows(Exception.class, () -> empresaValida().setProvincia(""));
        assertEquals("La provincia de la empresa es obligatoria.", e.getMessage());
    }

    @Test
    void emailVacioFalla() throws Exception {
        Exception e = assertThrows(Exception.class, () -> empresaValida().setEmail("   "));
        assertEquals("El email de la empresa es obligatorio.", e.getMessage());
    }

    @Test
    void emailMalEscrito() throws Exception {
        Exception e = assertThrows(Exception.class, () -> empresaValida().setEmail("taller@ejemplo"));
        assertEquals("Revise el formato del correo electrónico.", e.getMessage());
    }

    @Test
    void telefonoVacio() throws Exception {
        Exception e = assertThrows(Exception.class, () -> empresaValida().setTelefono(""));
        assertEquals("El teléfono de la empresa es obligatorio.", e.getMessage());
    }

    @Test
    void opcionalesVaciosSeGuardanVacios() throws Exception {
        Empresa e = empresaValida();
        e.setActividad("   ");
        e.setLogoPath(null);
        e.setPieLegal("");
        assertEquals("", e.getActividad());
        assertEquals("", e.getLogoPath());
        assertEquals("", e.getPieLegal());
        assertEquals(Empresa.CABECERA_TEXTO, e.getCabeceraModo());
        assertFalse(e.isCabeceraLogo());
    }

    @Test
    void modoCabeceraLogo() throws Exception {
        Empresa e = empresaValida();
        e.setCabeceraModo(Empresa.CABECERA_LOGO);
        assertTrue(e.isCabeceraLogo());
    }

    @Test
    void modoCabeceraNoValido() throws Exception {
        Exception e = assertThrows(Exception.class, () -> empresaValida().setCabeceraModo("IMAGEN"));
        assertEquals("Modo de cabecera no válido.", e.getMessage());
    }

    @Test
    void constructorCopia() throws Exception {
        Empresa original = empresaValida();
        original.setActividad("Talleres");
        original.setCabeceraModo(Empresa.CABECERA_LOGO);
        original.setLogoPath("/tmp/logo.png");
        original.setPieLegal("Texto legal.");
        Empresa copia = new Empresa(original);
        assertNotSame(original, copia);
        assertEquals(original.getNombre(), copia.getNombre());
        assertEquals(original.getNif(), copia.getNif());
        assertEquals(original.getDireccion(), copia.getDireccion());
        assertEquals(original.getCp(), copia.getCp());
        assertEquals(original.getLocalidad(), copia.getLocalidad());
        assertEquals(original.getProvincia(), copia.getProvincia());
        assertEquals(original.getEmail(), copia.getEmail());
        assertEquals(original.getTelefono(), copia.getTelefono());
        assertEquals(original.getActividad(), copia.getActividad());
        assertEquals(original.getCabeceraModo(), copia.getCabeceraModo());
        assertEquals(original.getLogoPath(), copia.getLogoPath());
        assertEquals(original.getPieLegal(), copia.getPieLegal());
        assertEquals(original, copia);
        copia.setNombre("Otra Empresa");
        assertNotEquals(original.getNombre(), copia.getNombre());
    }

    @Test
    void equalsPorNif() throws Exception {
        Empresa una = empresaValida();
        Empresa otra = new Empresa("Otro Nombre", "12345678Z", "Otra 2", "28002",
                "Getafe", "Madrid", "otro@ejemplo.es", "920000000");
        assertEquals(una, otra);
        assertEquals(una.hashCode(), otra.hashCode());
        otra.setNif("87654321X");
        assertNotEquals(una, otra);
    }

    @Test
    void cadaErrorXIgualaASuSetter() {
        assertEquals("Indique el nombre o razón social de la empresa.", Empresa.errorNombre(""));
        assertNull(Empresa.errorNombre("Talleres"));
        assertEquals("El NIF/NIE es obligatorio.", Empresa.errorNif(""));
        assertEquals("Formato NIF/NIE incorrecto. Debe ser como 12345678Z (DNI), "
                + "X1234567L (NIE) o B12345674 (CIF).", Empresa.errorNif("123"));
        assertEquals("La letra no es correcta.", Empresa.errorNif("12345678A"));
        assertNull(Empresa.errorNif("12345678Z"));
        assertEquals("La dirección de la empresa es obligatoria.", Empresa.errorDireccion(""));
        assertNull(Empresa.errorDireccion("Calle 1"));
        assertEquals("El código postal es obligatorio.", Empresa.errorCp(""));
        assertEquals("El código postal debe tener cinco dígitos y comenzar entre 01 y 52.",
                Empresa.errorCp("53001"));
        assertNull(Empresa.errorCp("28001"));
        assertEquals("La localidad de la empresa es obligatoria.", Empresa.errorLocalidad(""));
        assertNull(Empresa.errorLocalidad("Madrid"));
        assertEquals("La provincia de la empresa es obligatoria.", Empresa.errorProvincia(""));
        assertNull(Empresa.errorProvincia("Madrid"));
        assertEquals("El email de la empresa es obligatorio.", Empresa.errorEmail("   "));
        assertEquals("Revise el formato del correo electrónico.", Empresa.errorEmail("taller@ejemplo"));
        assertNull(Empresa.errorEmail("taller@ejemplo.es"));
        assertEquals("El teléfono de la empresa es obligatorio.", Empresa.errorTelefono(""));
        assertNull(Empresa.errorTelefono("910000000"));
    }
}
