package com.alcazaba.facturacion.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CodigoPostalValidatorTest {

    @Test
    void aceptaCodigosPostalesValidosIncluidosLosLimitesDeProvincia() {
        assertTrue(CodigoPostalValidator.esValido("01001"));
        assertTrue(CodigoPostalValidator.esValido("28001"));
        assertTrue(CodigoPostalValidator.esValido("08030"));
        assertTrue(CodigoPostalValidator.esValido("52000"));
    }

    @Test
    void rechazaProvinciasFueraDelRango() {
        assertFalse(CodigoPostalValidator.esValido("00001"));
        assertFalse(CodigoPostalValidator.esValido("53000"));
    }

    @Test
    void rechazaFormatoIncorrecto() {
        assertFalse(CodigoPostalValidator.esValido("1234"));
        assertFalse(CodigoPostalValidator.esValido("123456"));
        assertFalse(CodigoPostalValidator.esValido("1234a"));
        assertFalse(CodigoPostalValidator.esValido("abcde"));
    }

    @Test
    void rechazaElCodigoEnBlanco() {
        assertFalse(CodigoPostalValidator.esValido(""));
        assertFalse(CodigoPostalValidator.esValido("   "));
        assertFalse(CodigoPostalValidator.esValido(null));
    }
}