package cabofactu.modelo.dominio;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Comprobamos las etiquetas de la matriz: cada tipo con su porcentaje, los
 * exentos con su motivo y la fila de totales.
 */
class GrupoIvaTest {

    @Test
    void etiquetaDeTipoConPorcentaje() {
        GrupoIva grupo = new GrupoIva("IVA 21%", 21, "");
        grupo.setBase(new BigDecimal("900.00"));
        grupo.setCuota(new BigDecimal("189.00"));
        assertEquals("IVA 21%", grupo.getEtiqueta());
        assertTrue(grupo.getBaseTexto().contains("900,00"));
        assertTrue(grupo.getCuotaTexto().contains("189,00"));
    }

    @Test
    void etiquetaDeExentoConMotivo() {
        GrupoIva grupo = new GrupoIva("Exento", null, "Art. 20.1");
        assertEquals("Exento (Art. 20.1)", grupo.getEtiqueta());
        assertTrue(grupo.isExento());
    }

    @Test
    void filaTotales() {
        ResumenFactura resumen = new ResumenFactura();
        resumen.setBaseTotal(new BigDecimal("1350.00"));
        resumen.setIvaTotal(new BigDecimal("234.00"));
        GrupoIva totales = resumen.getFilaTotales();
        assertEquals("Totales", totales.getEtiqueta());
        assertTrue(totales.getBaseTexto().contains("1.350,00"));
        assertTrue(totales.getCuotaTexto().contains("234,00"));
    }
}
