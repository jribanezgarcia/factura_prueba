package cabofactu.modelo.negocio;

import cabofactu.modelo.dominio.GrupoIva;
import cabofactu.modelo.dominio.LineaFactura;
import cabofactu.modelo.dominio.ResumenFactura;
import cabofactu.modelo.dominio.TipoRetencion;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CalculosTest {

    private LineaFactura linea(int cantidad, String precio, Integer pct, String nombre, String motivo) throws Exception {
        LineaFactura l = new LineaFactura(cantidad, new BigDecimal(precio));
        l.setIvaPorcentaje(pct);
        l.setIvaNombre(nombre);
        l.setIvaMotivoExencion(motivo);
        return l;
    }

    private LineaFactura linea(int cantidad, String precio, Integer pct, String nombre) throws Exception {
        return linea(cantidad, precio, pct, nombre, null);
    }

    private GrupoIva grupo(ResumenFactura r, String nombre) {
        for (GrupoIva g : r.getGrupos()) {
            if (nombre.equals(g.getNombre())) {
                return g;
            }
        }
        throw new IllegalStateException("Falta el grupo " + nombre);
    }

    @Test
    void descuentoConUnSoloTipoDeIva() throws Exception {
        ResumenFactura r = Calculos.resumen(List.of(linea(1, "100.00", 21, "IVA 21%")), 10);
        assertEquals(new BigDecimal("90.00"), r.getBaseTotal());
        assertEquals(new BigDecimal("18.90"), r.getIvaTotal());
        assertEquals(new BigDecimal("108.90"), r.getTotal());
    }

    @Test
    void variosTiposDeIvaSinDescuento() throws Exception {
        ResumenFactura r = Calculos.resumen(
                List.of(linea(1, "100.00", 21, "IVA 21%"), linea(1, "50.00", 10, "IVA 10%")), 0);
        assertEquals(new BigDecimal("100.00"), grupo(r, "IVA 21%").getBase());
        assertEquals(new BigDecimal("21.00"), grupo(r, "IVA 21%").getCuota());
        assertEquals(new BigDecimal("50.00"), grupo(r, "IVA 10%").getBase());
        assertEquals(new BigDecimal("5.00"), grupo(r, "IVA 10%").getCuota());
        assertEquals(new BigDecimal("176.00"), r.getTotal());
    }

    @Test
    void descuentoConVariosTiposDeIvaReparteBases() throws Exception {
        ResumenFactura r = Calculos.resumen(
                List.of(linea(1, "100.00", 21, "IVA 21%"), linea(1, "50.00", 10, "IVA 10%")), 10);
        assertEquals(new BigDecimal("90.00"), grupo(r, "IVA 21%").getBase());
        assertEquals(new BigDecimal("18.90"), grupo(r, "IVA 21%").getCuota());
        assertEquals(new BigDecimal("45.00"), grupo(r, "IVA 10%").getBase());
        assertEquals(new BigDecimal("4.50"), grupo(r, "IVA 10%").getCuota());
        assertEquals(new BigDecimal("135.00"), r.getBaseTotal());
        assertEquals(new BigDecimal("158.40"), r.getTotal());
    }

    @Test
    void sinDescuentoBrutaIgualDescontada() throws Exception {
        ResumenFactura r = Calculos.resumen(
                List.of(linea(1, "100.00", 21, "IVA 21%")), 0);
        assertEquals(new BigDecimal("100.00"), r.getBaseBruta());
        assertEquals(r.getBaseBruta(), r.getBaseTotal());
        assertEquals(0, r.getImporteDescuento().compareTo(BigDecimal.ZERO));
    }

    @Test
    void descuentoDiezPorCientoSobreMilCuadra() throws Exception {
        ResumenFactura r = Calculos.resumen(
                List.of(linea(1, "1000.00", 21, "IVA 21%")), 10);
        assertEquals(new BigDecimal("1000.00"), r.getBaseBruta());
        assertEquals(new BigDecimal("900.00"), r.getBaseTotal());
        assertEquals(new BigDecimal("189.00"), r.getIvaTotal());
        assertEquals(new BigDecimal("100.00"), r.getImporteDescuento());
        assertEquals(new BigDecimal("1089.00"), r.getTotal());
    }

    @Test
    void descuentoConVariosTiposExponeBrutaYDescuento() throws Exception {
        ResumenFactura r = Calculos.resumen(
                List.of(linea(1, "100.00", 21, "IVA 21%"), linea(1, "50.00", 10, "IVA 10%")), 10);
        assertEquals(new BigDecimal("150.00"), r.getBaseBruta());
        assertEquals(new BigDecimal("135.00"), r.getBaseTotal());
        assertEquals(new BigDecimal("15.00"), r.getImporteDescuento());
    }

    @Test
    void entradaConIvaCalculaBaseHaciaAtras() throws Exception {
        assertEquals(new BigDecimal("100.00"), Calculos.baseDesdeTotalConIva(new BigDecimal("121.00"), 21));
    }

    @Test
    void precioCalculadoHaciaAtrasVuelveAlTotal() throws Exception {
        comprobarIdaYVuelta(new BigDecimal("100.00"), 3);
        comprobarIdaYVuelta(new BigDecimal("0.01"), 7);
        comprobarIdaYVuelta(new BigDecimal("1234.56"), 9);
    }

    private void comprobarIdaYVuelta(BigDecimal total, int cantidad) {
        BigDecimal precio = Calculos.precioDesdeTotal(total, cantidad);
        assertEquals(0, total.compareTo(Calculos.totalLinea(precio, cantidad)));
    }

    @Test
    void redondeoHaciaArriba() throws Exception {
        assertEquals(new BigDecimal("10.01"), Calculos.round2(new BigDecimal("10.005")));
        assertEquals(new BigDecimal("3.02"), Calculos.totalLinea(new BigDecimal("1.005"), 3));
    }

    @Test
    void ajusteDeCentimosEnLaMayorBase() throws Exception {
        ResumenFactura r = Calculos.resumen(
                List.of(linea(1, "1.01", 21, "IVA 21%"), linea(1, "1.01", 10, "IVA 10%")), 33);
        BigDecimal suma = BigDecimal.ZERO;
        for (GrupoIva g : r.getGrupos()) {
            suma = suma.add(g.getBase());
        }
        assertEquals(r.getBaseTotal(), suma);
        assertEquals(new BigDecimal("0.67"), grupo(r, "IVA 21%").getBase());
        assertEquals(new BigDecimal("0.68"), grupo(r, "IVA 10%").getBase());
    }

    @Test
    void lineaExentaSinCuota() throws Exception {
        ResumenFactura r = Calculos.resumen(
                List.of(linea(1, "100.00", null, "Exento", "Art. 20.1")), 0);
        GrupoIva g = r.getGrupos().get(0);
        assertTrue(g.isExento());
        assertEquals(new BigDecimal("100.00"), g.getBase());
        assertEquals(0, g.getCuota().compareTo(BigDecimal.ZERO));
        assertEquals("Art. 20.1", g.getMotivoExencion());
        assertEquals(new BigDecimal("100.00"), r.getTotal());
    }

    private TipoRetencion retencion(int pct) throws Exception {
        TipoRetencion t = new TipoRetencion("IRPF " + pct + "%", pct);
        t.setId(1L);
        t.setActivo(true);
        return t;
    }

    @Test
    void retencionSinDescuentoRestaDelTotal() throws Exception {
        ResumenFactura r = Calculos.resumen(
                List.of(linea(1, "1000.00", 21, "IVA 21%")), 0, retencion(15));
        assertEquals(new BigDecimal("1000.00"), r.getBaseBruta());
        assertEquals(new BigDecimal("210.00"), r.getIvaTotal());
        assertEquals(new BigDecimal("150.00"), r.getImporteRetencion());
        assertEquals(new BigDecimal("1060.00"), r.getTotal());
    }

    @Test
    void retencionUsaLaBaseImponibleDescontada() throws Exception {
        ResumenFactura r = Calculos.resumen(
                List.of(linea(1, "1000.00", 21, "IVA 21%")), 10, retencion(19));
        assertEquals(new BigDecimal("1000.00"), r.getBaseBruta());
        assertEquals(new BigDecimal("900.00"), r.getBaseTotal());
        assertEquals(new BigDecimal("189.00"), r.getIvaTotal());
        assertEquals(new BigDecimal("171.00"), r.getImporteRetencion());
        assertEquals(new BigDecimal("918.00"), r.getTotal());
    }

    @Test
    void descuentoDelCienPorCienNoDaTotalNegativo() throws Exception {
        ResumenFactura r = Calculos.resumen(
                List.of(linea(1, "1000.00", 21, "IVA 21%")), 100, retencion(15));
        assertEquals(new BigDecimal("0.00"), r.getBaseTotal());
        assertEquals(new BigDecimal("0.00"), r.getIvaTotal());
        assertEquals(new BigDecimal("0.00"), r.getImporteRetencion());
        assertEquals(new BigDecimal("0.00"), r.getTotal());
    }

    @Test
    void sinRetencionMantieneComportamientoAnterior() throws Exception {
        ResumenFactura r = Calculos.resumen(
                List.of(linea(1, "1000.00", 21, "IVA 21%")), 0, null);
        assertEquals(new BigDecimal("1210.00"), r.getTotal());
        assertEquals(0, BigDecimal.ZERO.compareTo(r.getImporteRetencion()));
    }

    private LineaFactura suplido(String importe) throws Exception {
        LineaFactura l = new LineaFactura(1, new BigDecimal(importe));
        l.setIvaNombre("Suplido");
        l.setIvaPorcentaje(null);
        l.setEsSuplido(true);
        return l;
    }

    @Test
    void suplidoSoloSumaAlTotal() throws Exception {
        ResumenFactura r = Calculos.resumen(List.of(suplido("250.00")), 0);
        assertEquals(0, r.getBaseTotal().compareTo(BigDecimal.ZERO));
        assertEquals(0, r.getIvaTotal().compareTo(BigDecimal.ZERO));
        assertEquals(new BigDecimal("250.00"), r.getTotalSuplidos());
        assertEquals(new BigDecimal("250.00"), r.getTotal());
        assertTrue(r.getGrupos().isEmpty());
    }

    @Test
    void suplidoNoEntraEnBaseNiCuotaNiRetencion() throws Exception {
        ResumenFactura r = Calculos.resumen(
                List.of(linea(1, "1000.00", 21, "IVA 21%"), suplido("250.00")), 0, retencion(15));
        assertEquals(new BigDecimal("1000.00"), r.getBaseTotal());
        assertEquals(new BigDecimal("210.00"), r.getIvaTotal());
        assertEquals(new BigDecimal("150.00"), r.getImporteRetencion());
        assertEquals(new BigDecimal("250.00"), r.getTotalSuplidos());
        assertEquals(new BigDecimal("1310.00"), r.getTotal());
        for (GrupoIva g : r.getGrupos()) {
            assertTrue(!"Suplido".equals(g.getNombre()));
        }
    }

    @Test
    void descuentoGlobalNoAfectaAlSuplido() throws Exception {
        ResumenFactura r = Calculos.resumen(
                List.of(linea(1, "1000.00", 21, "IVA 21%"), suplido("250.00")), 10, retencion(15));
        assertEquals(new BigDecimal("900.00"), r.getBaseTotal());
        assertEquals(new BigDecimal("189.00"), r.getIvaTotal());
        assertEquals(new BigDecimal("135.00"), r.getImporteRetencion());
        assertEquals(new BigDecimal("250.00"), r.getTotalSuplidos());
        assertEquals(new BigDecimal("1204.00"), r.getTotal());
    }

    @Test
    void sinSuplidosTotalSuplidosCeroYTotalInalterado() throws Exception {
        ResumenFactura r = Calculos.resumen(
                List.of(linea(1, "1000.00", 21, "IVA 21%")), 10, retencion(15));
        assertEquals(0, r.getTotalSuplidos().compareTo(BigDecimal.ZERO));
        assertEquals(new BigDecimal("954.00"), r.getTotal());
    }

    @Test
    void totalConIvaSumaLaCuotaAlTipoNormal() throws Exception {
        assertEquals(new BigDecimal("1210.00"),
                Calculos.totalConIva(linea(1, "1000.00", 21, "IVA 21%")));
    }

    @Test
    void totalConIvaDevuelveLaBaseSiEsExenta() throws Exception {
        assertEquals(new BigDecimal("200.00"),
                Calculos.totalConIva(linea(1, "200.00", null, "Exento", "Art. 20.1")));
    }

    @Test
    void totalConIvaDevuelveLaBaseSiEsSuplido() throws Exception {
        assertEquals(new BigDecimal("250.00"), Calculos.totalConIva(suplido("250.00")));
    }

    @Test
    void totalConIvaDevuelveLaBaseSiElTipoEsCero() throws Exception {
        LineaFactura l = linea(1, "100.00", 0, "IVA 0%");
        assertEquals(new BigDecimal("100.00"), Calculos.totalConIva(l));
    }

    @Test
    void suplidosDeSinSuplidosDevuelveVacio() throws Exception {
        assertTrue(Calculos.suplidosDe(
                List.of(linea(1, "1000.00", 21, "IVA 21%"))).isEmpty());
    }

    @Test
    void suplidosDeSoloSuplidosDevuelveTodas() throws Exception {
        List<LineaFactura> suplidos = Calculos.suplidosDe(
                List.of(suplido("120.00"), suplido("80.00")));
        assertEquals(2, suplidos.size());
        for (LineaFactura suplido : suplidos) {
            assertTrue(suplido.isEsSuplido());
        }
    }

    @Test
    void suplidosDeMezclaDevuelveSoloSuplidos() throws Exception {
        List<LineaFactura> suplidos = Calculos.suplidosDe(List.of(
                linea(1, "1000.00", 21, "IVA 21%"), suplido("250.00")));
        assertEquals(1, suplidos.size());
        assertEquals(new BigDecimal("250.00"), suplidos.get(0).getTotalBase());
    }

    @Test
    void suplidosDeListaNulaDevuelveVacio() throws Exception {
        assertTrue(Calculos.suplidosDe(null).isEmpty());
    }
}
