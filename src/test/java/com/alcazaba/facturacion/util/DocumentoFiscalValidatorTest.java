package com.alcazaba.facturacion.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DocumentoFiscalValidatorTest {

    @Test
    void aceptaDniNieYCifValidos() {
        assertTrue(DocumentoFiscalValidator.esValido("12345678Z"));
        assertTrue(DocumentoFiscalValidator.esValido("X2482300W"));
        assertTrue(DocumentoFiscalValidator.esValido("a58818501"));
        assertTrue(DocumentoFiscalValidator.esValido(""));
    }

    @Test
    void rechazaLetraOFormatoIncorrecto() {
        assertFalse(DocumentoFiscalValidator.esValido("75238360A"));
        assertFalse(DocumentoFiscalValidator.esValido("X2482300A"));
        assertFalse(DocumentoFiscalValidator.esValido("A58818502"));
        assertFalse(DocumentoFiscalValidator.esValido("texto"));
    }

    @Test
    void importeVacioNoCreaFiltro() {
        assertNull(Formatos.parseMonedaOpcional(""));
        assertNull(Formatos.parseMonedaOpcional("  "));
    }
}
