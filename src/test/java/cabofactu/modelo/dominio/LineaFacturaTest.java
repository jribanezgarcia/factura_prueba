package cabofactu.modelo.dominio;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Comprobamos que la línea se valida sola: cantidad desde 1, precio no
 * negativo, el tipo se copia entero y los textos salen con formato.
 */
class LineaFacturaTest {

    @Test
    void cantidadCeroAvisa() throws Exception {
        LineaFactura linea = new LineaFactura(1, new BigDecimal("10.00"));
        Exception e = assertThrows(Exception.class, () -> linea.setCantidad(0));
        assertEquals("La cantidad debe ser 1 o más.", e.getMessage());
    }

    @Test
    void precioNegativoAvisa() throws Exception {
        LineaFactura linea = new LineaFactura(1, new BigDecimal("10.00"));
        Exception e = assertThrows(Exception.class, () -> linea.setPrecioUnitario(new BigDecimal("-1.00")));
        assertEquals("El precio no puede ser negativo.", e.getMessage());
    }

    @Test
    void constructorVacioNoExiste() throws Exception {
        LineaFactura linea = new LineaFactura(2, new BigDecimal("10.00"));
        assertEquals(2, linea.getCantidad());
        assertEquals(new BigDecimal("10.00"), linea.getPrecioUnitario());
    }

    @Test
    void setTipoIvaCopiaLosCincoDatos() throws Exception {
        TipoIva tipo = new TipoIva("IVA 21%", 21, false);
        tipo.setId(7L);
        tipo.setMotivoExencion("Motivo");
        LineaFactura linea = new LineaFactura(1, new BigDecimal("10.00"));
        linea.setTipoIva(tipo);
        assertEquals(7L, linea.getTipoIvaId());
        assertEquals("IVA 21%", linea.getIvaNombre());
        assertEquals(21, linea.getIvaPorcentaje());
        assertEquals("Motivo", linea.getIvaMotivoExencion());
        assertFalse(linea.isEsSuplido());
    }

    @Test
    void tieneContenido() throws Exception {
        LineaFactura vacia = new LineaFactura(1, BigDecimal.ZERO);
        assertFalse(vacia.tieneContenido());
        LineaFactura conDescripcion = new LineaFactura(1, BigDecimal.ZERO);
        conDescripcion.setDescripcion("Concepto");
        assertTrue(conDescripcion.tieneContenido());
        LineaFactura conPrecio = new LineaFactura(1, new BigDecimal("5.00"));
        assertTrue(conPrecio.tieneContenido());
    }

    @Test
    void gettersDeTexto() throws Exception {
        LineaFactura linea = new LineaFactura(2, new BigDecimal("10.00"));
        linea.setDescripcion("Concepto");
        assertEquals("2", linea.getCantidadTexto());
        assertTrue(linea.getPrecioUnitarioTexto().contains("10,00"));
        assertTrue(linea.getTotalBaseTexto().contains("20,00"));
    }
}
