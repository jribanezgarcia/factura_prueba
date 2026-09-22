package cabofactu.modelo.dominio;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Comprobamos que TipoIva se valida solo: nombre obligatorio, porcentaje
 * entre 0 y 100 o vacío (exento), y el suplido deja el porcentaje en null.
 */
class TipoIvaTest {

    @Test
    void nombreVacio() throws Exception {
        TipoIva tipo = new TipoIva("IVA 21%", 21, false);
        Exception e = assertThrows(Exception.class, () -> tipo.setNombre("   "));
        assertEquals("Indique el nombre del tipo de IVA.", e.getMessage());
    }

    @Test
    void porcentajeFueraDeRango() throws Exception {
        TipoIva tipo = new TipoIva("IVA 21%", 21, false);
        Exception e = assertThrows(Exception.class, () -> tipo.setPorcentaje(150));
        assertEquals("El porcentaje debe ser un número entero entre 0 y 100, o quedarse vacío si es exento.",
                e.getMessage());
    }

    @Test
    void porcentajeNoNumerico() {
        assertEquals("El porcentaje debe ser un número entero entre 0 y 100, o quedarse vacío si es exento.",
                TipoIva.errorPorcentaje("veintiuno"));
    }

    @Test
    void porcentajeVacioEsExento() throws Exception {
        TipoIva tipo = new TipoIva("Exento", null, false);
        assertTrue(tipo.isExento());
        assertNull(TipoIva.errorPorcentaje("   "));
    }

    @Test
    void suplidoDejaElPorcentajeEnNull() throws Exception {
        TipoIva tipo = new TipoIva("Suplido", 21, false);
        tipo.setEsSuplido(true);
        assertTrue(tipo.isEsSuplido());
        assertNull(tipo.getPorcentaje());
    }

    @Test
    void gettersDeTexto() throws Exception {
        TipoIva porcentaje = new TipoIva("IVA 21%", 21, false);
        assertEquals("21%", porcentaje.getPorcentajeTexto());
        assertEquals("No", porcentaje.getSuplidoTexto());
        assertEquals("Sí", porcentaje.getActivoTexto());
        assertEquals("IVA 21%", porcentaje.toString());
        TipoIva exento = new TipoIva("Exento", null, false);
        assertEquals("Exento", exento.getPorcentajeTexto());
        TipoIva suplido = new TipoIva("Suplido", null, true);
        assertEquals("Suplido", suplido.getPorcentajeTexto());
        assertEquals("Sí", suplido.getSuplidoTexto());
    }

    @Test
    void cadaErrorXIgualaASuSetter() throws Exception {
        TipoIva tipo = new TipoIva("IVA 21%", 21, false);
        Exception e = assertThrows(Exception.class, () -> tipo.setNombre(""));
        assertEquals(TipoIva.errorNombre(""), e.getMessage());
        assertNull(TipoIva.errorNombre("IVA 21%"));
        assertNull(TipoIva.errorPorcentaje("21"));
        assertNull(TipoIva.errorPorcentaje(""));
    }
}
