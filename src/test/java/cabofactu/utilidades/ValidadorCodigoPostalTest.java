package cabofactu.utilidades;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidadorCodigoPostalTest {

    @Test
    void aceptaCodigosPostalesValidosIncluidosLosLimitesDeProvincia() {
        assertTrue(ValidadorCodigoPostal.esValido("01001"));
        assertTrue(ValidadorCodigoPostal.esValido("28001"));
        assertTrue(ValidadorCodigoPostal.esValido("08030"));
        assertTrue(ValidadorCodigoPostal.esValido("52000"));
    }

    @Test
    void rechazaProvinciasFueraDelRango() {
        assertFalse(ValidadorCodigoPostal.esValido("00001"));
        assertFalse(ValidadorCodigoPostal.esValido("53000"));
    }

    @Test
    void rechazaFormatoIncorrecto() {
        assertFalse(ValidadorCodigoPostal.esValido("1234"));
        assertFalse(ValidadorCodigoPostal.esValido("123456"));
        assertFalse(ValidadorCodigoPostal.esValido("1234a"));
        assertFalse(ValidadorCodigoPostal.esValido("abcde"));
    }

    @Test
    void rechazaElCodigoEnBlanco() {
        assertFalse(ValidadorCodigoPostal.esValido(""));
        assertFalse(ValidadorCodigoPostal.esValido("   "));
        assertFalse(ValidadorCodigoPostal.esValido(null));
    }
}