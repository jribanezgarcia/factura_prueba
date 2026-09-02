package com.alcazaba.facturacion.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmailValidatorTest {

    @Test
    void aceptaEmailsConFormatoRazonable() {
        assertTrue(EmailValidator.esValido("correo@ejemplo.es"));
        assertTrue(EmailValidator.esValido("a@b.co"));
        assertTrue(EmailValidator.esValido("nombre.apellido+etiqueta@sub.dominio.es"));
        assertTrue(EmailValidator.esValido("123@456.xyz"));
    }

    @Test
    void aceptaElEmailEnBlanco() {
        assertTrue(EmailValidator.esValido(""));
        assertTrue(EmailValidator.esValido("   "));
        assertTrue(EmailValidator.esValido(null));
    }

    @Test
    void rechazaEmailsSinFormatoValido() {
        assertFalse(EmailValidator.esValido("sin-arroba"));
        assertFalse(EmailValidator.esValido("a@"));
        assertFalse(EmailValidator.esValido("@dominio.es"));
        assertFalse(EmailValidator.esValido("a@b"));
        assertFalse(EmailValidator.esValido("nombre con espacios@ejemplo.es"));
    }
}