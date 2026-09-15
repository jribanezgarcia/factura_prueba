package cabofactu.utilidades;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    void importeVacioNoCreaFiltro() {
        assertNull(Formatos.parseMonedaOpcional(""));
        assertNull(Formatos.parseMonedaOpcional("  "));
    }
}
