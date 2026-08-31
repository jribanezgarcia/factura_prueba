package com.alcazaba.facturacion.service;

import com.alcazaba.facturacion.model.LineaFactura;
import com.alcazaba.facturacion.model.ResumenFactura;
import com.alcazaba.facturacion.model.TipoRetencion;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CalculoServiceTest {

    private LineaFactura linea(int cantidad, String precio, Integer pct, String nombre, String motivo) {
        LineaFactura l = new LineaFactura();
        l.setCantidad(cantidad);
        l.setPrecioUnitario(new BigDecimal(precio));
        l.setTotalBase(CalculoService.totalLinea(l.getPrecioUnitario(), cantidad));
        l.setIvaPorcentaje(pct);
        l.setIvaNombre(nombre);
        l.setIvaMotivoExencion(motivo);
        l.setIvaImporte(CalculoService.ivaDeBase(l.getTotalBase(), pct));
        return l;
    }

    private LineaFactura linea(int cantidad, String precio, Integer pct, String nombre) {
        return linea(cantidad, precio, pct, nombre, null);
    }

    private ResumenFactura.IvaGrupo grupo(ResumenFactura r, String nombre) {
        return r.getGrupos().stream().filter(g -> nombre.equals(g.getNombre())).findFirst().orElseThrow();
    }

    @Test
    void descuentoConUnSoloTipoDeIva() {
        ResumenFactura r = CalculoService.resumen(List.of(linea(1, "100.00", 21, "IVA 21%")), 10);
        assertEquals(new BigDecimal("90.00"), r.getBaseTotal());
        assertEquals(new BigDecimal("18.90"), r.getIvaTotal());
        assertEquals(new BigDecimal("108.90"), r.getTotal());
    }

    @Test
    void variosTiposDeIvaSinDescuento() {
        ResumenFactura r = CalculoService.resumen(
                List.of(linea(1, "100.00", 21, "IVA 21%"), linea(1, "50.00", 10, "IVA 10%")), 0);
        assertEquals(new BigDecimal("100.00"), grupo(r, "IVA 21%").getBase());
        assertEquals(new BigDecimal("21.00"), grupo(r, "IVA 21%").getCuota());
        assertEquals(new BigDecimal("50.00"), grupo(r, "IVA 10%").getBase());
        assertEquals(new BigDecimal("5.00"), grupo(r, "IVA 10%").getCuota());
        assertEquals(new BigDecimal("176.00"), r.getTotal());
    }

    @Test
    void descuentoConVariosTiposDeIvaReparteBases() {
        ResumenFactura r = CalculoService.resumen(
                List.of(linea(1, "100.00", 21, "IVA 21%"), linea(1, "50.00", 10, "IVA 10%")), 10);
        assertEquals(new BigDecimal("90.00"), grupo(r, "IVA 21%").getBase());
        assertEquals(new BigDecimal("18.90"), grupo(r, "IVA 21%").getCuota());
        assertEquals(new BigDecimal("45.00"), grupo(r, "IVA 10%").getBase());
        assertEquals(new BigDecimal("4.50"), grupo(r, "IVA 10%").getCuota());
        assertEquals(new BigDecimal("135.00"), r.getBaseTotal());
        assertEquals(new BigDecimal("158.40"), r.getTotal());
    }

    @Test
    void sinDescuentoBrutaIgualDescontada() {
        ResumenFactura r = CalculoService.resumen(
                List.of(linea(1, "100.00", 21, "IVA 21%")), 0);
        assertEquals(new BigDecimal("100.00"), r.getBaseBruta());
        assertEquals(r.getBaseBruta(), r.getBaseTotal());
        assertEquals(0, r.getImporteDescuento().compareTo(BigDecimal.ZERO));
    }

    @Test
    void descuentoDiezPorCientoSobreMilCuadra() {
        ResumenFactura r = CalculoService.resumen(
                List.of(linea(1, "1000.00", 21, "IVA 21%")), 10);
        assertEquals(new BigDecimal("1000.00"), r.getBaseBruta());
        assertEquals(new BigDecimal("900.00"), r.getBaseTotal());
        assertEquals(new BigDecimal("189.00"), r.getIvaTotal());
        assertEquals(new BigDecimal("100.00"), r.getImporteDescuento());
        assertEquals(new BigDecimal("1089.00"), r.getTotal());
    }

    @Test
    void descuentoConVariosTiposExponeBrutaYDescuento() {
        ResumenFactura r = CalculoService.resumen(
                List.of(linea(1, "100.00", 21, "IVA 21%"), linea(1, "50.00", 10, "IVA 10%")), 10);
        assertEquals(new BigDecimal("150.00"), r.getBaseBruta());
        assertEquals(new BigDecimal("135.00"), r.getBaseTotal());
        assertEquals(new BigDecimal("15.00"), r.getImporteDescuento());
    }

    @Test
    void entradaConIvaCalculaBaseHaciaAtras() {
        CalculoService.ResultadoConIva r = CalculoService.calcularDesdeTotalConIva(new BigDecimal("121.00"), 21);
        assertEquals(new BigDecimal("100.00"), r.base());
        assertEquals(new BigDecimal("21.00"), r.iva());
    }

    @Test
    void redondeoHaciaArriba() {
        assertEquals(new BigDecimal("10.01"), CalculoService.round2(new BigDecimal("10.005")));
        assertEquals(new BigDecimal("3.02"), CalculoService.totalLinea(new BigDecimal("1.005"), 3));
    }

    @Test
    void ajusteDeCentimosEnLaMayorBase() {
        ResumenFactura r = CalculoService.resumen(
                List.of(linea(1, "1.01", 21, "IVA 21%"), linea(1, "1.01", 10, "IVA 10%")), 33);
        BigDecimal suma = r.getGrupos().stream().map(ResumenFactura.IvaGrupo::getBase)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(r.getBaseTotal(), suma);
        assertEquals(new BigDecimal("0.67"), grupo(r, "IVA 21%").getBase());
        assertEquals(new BigDecimal("0.68"), grupo(r, "IVA 10%").getBase());
    }

    @Test
    void lineaExentaSinCuota() {
        ResumenFactura r = CalculoService.resumen(
                List.of(linea(1, "100.00", null, "Exento", "Art. 20.1")), 0);
        ResumenFactura.IvaGrupo g = r.getGrupos().get(0);
        assertTrue(g.isExento());
        assertEquals(new BigDecimal("100.00"), g.getBase());
        assertEquals(0, g.getCuota().compareTo(BigDecimal.ZERO));
        assertEquals("Art. 20.1", g.getMotivoExencion());
        assertEquals(new BigDecimal("100.00"), r.getTotal());
    }

    private TipoRetencion retencion(int pct) {
        TipoRetencion t = new TipoRetencion();
        t.setId(1L);
        t.setNombre("IRPF " + pct + "%");
        t.setPorcentaje(pct);
        t.setActivo(true);
        return t;
    }

    @Test
    void retencionSobreBaseBrutaRestaDelTotal() {
        ResumenFactura r = CalculoService.resumen(
                List.of(linea(1, "1000.00", 21, "IVA 21%")), 0, retencion(15));
        assertEquals(new BigDecimal("1000.00"), r.getBaseBruta());
        assertEquals(new BigDecimal("210.00"), r.getIvaTotal());
        assertEquals(new BigDecimal("150.00"), r.getImporteRetencion());
        assertEquals(new BigDecimal("1060.00"), r.getTotal());
    }

    @Test
    void retencionConDescuentoUsaBaseBruta() {
        ResumenFactura r = CalculoService.resumen(
                List.of(linea(1, "1000.00", 21, "IVA 21%")), 10, retencion(19));
        assertEquals(new BigDecimal("1000.00"), r.getBaseBruta());
        assertEquals(new BigDecimal("900.00"), r.getBaseTotal());
        assertEquals(new BigDecimal("189.00"), r.getIvaTotal());
        assertEquals(new BigDecimal("190.00"), r.getImporteRetencion());
        assertEquals(new BigDecimal("899.00"), r.getTotal());
    }

    @Test
    void sinRetencionMantieneComportamientoAnterior() {
        ResumenFactura r = CalculoService.resumen(
                List.of(linea(1, "1000.00", 21, "IVA 21%")), 0, null);
        assertEquals(new BigDecimal("1210.00"), r.getTotal());
        assertEquals(0, BigDecimal.ZERO.compareTo(r.getImporteRetencion()));
    }
}
