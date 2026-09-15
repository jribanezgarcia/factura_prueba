package cabofactu.utilidades;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidadorEmailTest {

    @Test
    void aceptaEmailsConFormatoRazonable() {
        assertTrue(ValidadorEmail.esValido("correo@ejemplo.es"));
        assertTrue(ValidadorEmail.esValido("a@b.co"));
        assertTrue(ValidadorEmail.esValido("nombre.apellido+etiqueta@sub.dominio.es"));
        assertTrue(ValidadorEmail.esValido("123@456.xyz"));
    }

    @Test
    void aceptaElEmailEnBlanco() {
        assertTrue(ValidadorEmail.esValido(""));
        assertTrue(ValidadorEmail.esValido("   "));
        assertTrue(ValidadorEmail.esValido(null));
    }

    @Test
    void rechazaEmailsSinFormatoValido() {
        assertFalse(ValidadorEmail.esValido("sin-arroba"));
        assertFalse(ValidadorEmail.esValido("a@"));
        assertFalse(ValidadorEmail.esValido("@dominio.es"));
        assertFalse(ValidadorEmail.esValido("a@b"));
        assertFalse(ValidadorEmail.esValido("nombre con espacios@ejemplo.es"));
    }
}