package cabofactu.modelo.dominio;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Comprobamos que TipoRetencion se valida solo: nombre y porcentaje
 * obligatorios, con el porcentaje entre 0 y 100.
 */
class TipoRetencionTest {

    @Test
    void nombreVacio() throws Exception {
        TipoRetencion tipo = new TipoRetencion("IRPF 15%", 15);
        Exception e = assertThrows(Exception.class, () -> tipo.setNombre("   "));
        assertEquals("Indique el nombre del tipo de retención.", e.getMessage());
    }

    @Test
    void porcentajeVacio() {
        assertEquals("Indique el porcentaje de la retención.",
                TipoRetencion.errorPorcentaje("   "));
    }

    @Test
    void porcentajeFueraDeRango() throws Exception {
        TipoRetencion tipo = new TipoRetencion("IRPF 15%", 15);
        Exception e = assertThrows(Exception.class, () -> tipo.setPorcentaje(150));
        assertEquals("El porcentaje debe ser un número entero entre 0 y 100.", e.getMessage());
    }

    @Test
    void porcentajeNoNumerico() {
        assertEquals("El porcentaje debe ser un número entero entre 0 y 100.",
                TipoRetencion.errorPorcentaje("quince"));
    }

    @Test
    void gettersDeTexto() throws Exception {
        TipoRetencion tipo = new TipoRetencion("IRPF 15%", 15);
        assertEquals("15%", tipo.getPorcentajeTexto());
        assertEquals("Sí", tipo.getActivoTexto());
        assertEquals("IRPF 15% (15%)", tipo.toString());
    }

    @Test
    void cadaErrorXIgualaASuSetter() throws Exception {
        TipoRetencion tipo = new TipoRetencion("IRPF 15%", 15);
        Exception e = assertThrows(Exception.class, () -> tipo.setNombre(""));
        assertEquals(TipoRetencion.errorNombre(""), e.getMessage());
        assertNull(TipoRetencion.errorNombre("IRPF 15%"));
        e = assertThrows(Exception.class, () -> tipo.setPorcentaje(-1));
        assertEquals(TipoRetencion.errorPorcentaje("-1"), e.getMessage());
        assertNull(TipoRetencion.errorPorcentaje("15"));
    }
}
