package com.alcazaba.facturacion.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FormatosTest {

    @Test
    void nombreDeArchivoConMesSustituyeLaBarra() {
        assertEquals("C-59-7.pdf", Formatos.nombreArchivoPdf("C-59/7"));
    }

    @Test
    void nombreDeArchivoSinMesSeMantiene() {
        assertEquals("R-1.pdf", Formatos.nombreArchivoPdf("R-1"));
    }

    @Test
    void nombreDeArchivoVacioTieneValorSeguro() {
        assertEquals("factura.pdf", Formatos.nombreArchivoPdf(null));
        assertEquals("factura.pdf", Formatos.nombreArchivoPdf("   "));
    }
}
