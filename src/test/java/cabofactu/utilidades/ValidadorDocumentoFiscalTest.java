package cabofactu.utilidades;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Comprobamos el validador de DNI, NIE y NIF/CIF: resultado global, forma y letra por separado. */
class ValidadorDocumentoFiscalTest {

    @Test
    void aceptaDniNieYCifValidos() {
        assertTrue(ValidadorDocumentoFiscal.esValido("12345678Z"));
        assertTrue(ValidadorDocumentoFiscal.esValido("X2482300W"));
        assertTrue(ValidadorDocumentoFiscal.esValido("a58818501"));
        assertTrue(ValidadorDocumentoFiscal.esValido(""));
    }

    @Test
    void rechazaLetraOFormatoIncorrecto() {
        assertFalse(ValidadorDocumentoFiscal.esValido("75238360A"));
        assertFalse(ValidadorDocumentoFiscal.esValido("X2482300A"));
        assertFalse(ValidadorDocumentoFiscal.esValido("A58818502"));
        assertFalse(ValidadorDocumentoFiscal.esValido("texto"));
    }

    @Test
    void formatoCorrectoDistingueLaForma() {
        assertTrue(ValidadorDocumentoFiscal.formatoCorrecto("12345678Z"));
        assertTrue(ValidadorDocumentoFiscal.formatoCorrecto("X1234567L"));
        assertTrue(ValidadorDocumentoFiscal.formatoCorrecto("B12345674"));
        assertTrue(ValidadorDocumentoFiscal.formatoCorrecto("12345678A"));
        assertTrue(ValidadorDocumentoFiscal.formatoCorrecto("B12345678"));
        assertFalse(ValidadorDocumentoFiscal.formatoCorrecto("123"));
        assertFalse(ValidadorDocumentoFiscal.formatoCorrecto("ABCD"));
        assertFalse(ValidadorDocumentoFiscal.formatoCorrecto("1234567"));
        assertFalse(ValidadorDocumentoFiscal.formatoCorrecto(""));
        String nulo = null;
        assertFalse(ValidadorDocumentoFiscal.formatoCorrecto(nulo));
    }

    @Test
    void letraCorrectaCompruebaElCaracterFinal() {
        assertTrue(ValidadorDocumentoFiscal.letraCorrecta("12345678Z"));
        assertTrue(ValidadorDocumentoFiscal.letraCorrecta("X1234567L"));
        assertTrue(ValidadorDocumentoFiscal.letraCorrecta("B12345674"));
        assertFalse(ValidadorDocumentoFiscal.letraCorrecta("12345678A"));
        assertFalse(ValidadorDocumentoFiscal.letraCorrecta("B12345678"));
        assertFalse(ValidadorDocumentoFiscal.letraCorrecta("123"));
        assertFalse(ValidadorDocumentoFiscal.letraCorrecta(""));
        String nulo = null;
        assertFalse(ValidadorDocumentoFiscal.letraCorrecta(nulo));
    }

    @Test
    void importeVacioNoCreaFiltro() {
        assertNull(Formatos.parseMonedaOpcional(""));
        assertNull(Formatos.parseMonedaOpcional("  "));
    }
}
