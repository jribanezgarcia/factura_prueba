package com.alcazaba.facturacion.pdf;

import com.alcazaba.facturacion.model.Empresa;
import com.alcazaba.facturacion.model.EstadoFactura;
import com.alcazaba.facturacion.model.Factura;
import com.alcazaba.facturacion.model.FacturaVersion;
import com.alcazaba.facturacion.model.LineaFactura;
import com.alcazaba.facturacion.service.FacturaService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InvoiceDocumentBuilderTest {

    private Empresa empresa() {
        Empresa e = new Empresa();
        e.setNombre("COMERCIAL ALCAZABA, S.C.");
        e.setNif("B04444444");
        e.setActividad("Cocinas y armarios");
        e.setCabeceraModo("TEXTO");
        e.setPieLegal("Protección de datos RGPD texto legal de prueba.");
        return e;
    }

    private FacturaVersion version() {
        FacturaVersion v = new FacturaVersion();
        v.setNumero("C-59/7");
        v.setFechaFactura(LocalDate.of(2026, 7, 14));
        v.setFechaGuardado(LocalDateTime.of(2026, 7, 14, 12, 0));
        v.setEstado(EstadoFactura.EMITIDA);
        v.setDescuentoPorcentaje(0);
        v.setCliNombre("MARIA MARTAGON AVALOS");
        v.setCliNif("49122168X");
        v.setCliDireccion("C/ PROFESOR MULIAN Nº 41 1º A 6");
        v.setCliCp("04009");
        v.setCliLocalidad("ALMERIA");
        v.setCliEmail("maria.martagon@correo.es");
        v.setFormaPago("");
        v.setRealizadaPor("");
        return v;
    }

    private LineaFactura linea(String desc, String base, Integer pct, boolean suplido) {
        LineaFactura l = new LineaFactura();
        l.setCantidad(1);
        l.setDescripcion(desc);
        l.setPrecioUnitario(new BigDecimal(base));
        l.setTotalBase(new BigDecimal(base));
        l.setIvaNombre(suplido ? "Suplido" : "IVA " + pct + "%");
        l.setIvaPorcentaje(pct);
        l.setEsSuplido(suplido);
        return l;
    }

    private InvoiceDocument doc(FacturaVersion v, List<LineaFactura> lineas) {
        return InvoiceDocumentBuilder.build(
                new FacturaService.VersionCompleta(new Factura(), v, lineas, null), empresa(), "#B08D57");
    }

    @Test
    void dosTiposDeIva() {
        InvoiceDocument d = doc(version(),
                List.of(linea("CONCEPTO A", "1000.00", 21, false), linea("CONCEPTO B", "500.00", 10, false)));
        assertEquals(List.of("21,00", "10,00"),
                d.totals().ivaRows().stream().map(InvoiceDocument.IvaRow::type).toList());
        assertEquals("1.000,00", d.totals().ivaRows().get(0).base());
        assertEquals("210,00", d.totals().ivaRows().get(0).quota());
        assertEquals("Totales", d.totals().totalsRow().type());
        assertEquals("1.500,00", d.totals().totalsRow().base());
        assertEquals("260,00", d.totals().totalsRow().quota());
        assertEquals("TOTAL", d.totals().liquidation().total().label());
        assertTrue(d.totals().liquidation().total().amount().contains("1.760,00"));
    }

    @Test
    void descuentoGlobal() {
        FacturaVersion v = version();
        v.setDescuentoPorcentaje(10);
        InvoiceDocument d = doc(v, List.of(linea("ARMARIO EMPOTRADO", "3128.10", 21, false)));
        assertEquals("2.815,29", d.totals().totalsRow().base());
        assertEquals("591,21", d.totals().totalsRow().quota());
        assertTrue(d.totals().discountNote().isPresent());
        assertTrue(d.totals().discountNote().orElseThrow().contains("10 %"));
        assertTrue(d.totals().discountNote().orElseThrow().contains("312,81"));
    }

    @Test
    void retencionDelSnapshot() {
        FacturaVersion v = version();
        v.setTipoRetencionId(1L);
        v.setTipoRetencionNombre("IRPF profesional");
        v.setTipoRetencionPorcentaje(15);
        InvoiceDocument d = doc(v, List.of(linea("ARMARIO EMPOTRADO", "3128.10", 21, false)));
        assertTrue(d.totals().liquidation().retention().isPresent());
        assertEquals("IRPF profesional 15 %", d.totals().liquidation().retention().orElseThrow().label());
        assertTrue(d.totals().liquidation().retention().orElseThrow().amount().contains("469,22"));
    }

    @Test
    void suplidosYBloquePropio() {
        InvoiceDocument d = doc(version(), List.of(linea("ARMARIO EMPOTRADO", "3128.10", 21, false),
                linea("TASAS", "250.00", null, true)));
        assertTrue(d.suplidos().isPresent());
        assertEquals(List.of("SUPLIDOS", "IMPORTE"), d.suplidos().orElseThrow().headers());
        assertEquals(1, d.suplidos().orElseThrow().rows().size());
        assertEquals("TASAS", d.suplidos().orElseThrow().rows().get(0).description());
        assertEquals("250,00", d.suplidos().orElseThrow().rows().get(0).amount());
        assertTrue(d.suplidos().orElseThrow().note().contains("No sujetos a IVA ni a retención"));
        assertTrue(d.totals().liquidation().suplidos().isPresent());
    }

    @Test
    void soloSuplidosTablaConSoloCabecera() {
        InvoiceDocument d = doc(version(), List.of(linea("TASAS", "250.00", null, true)));
        assertEquals(List.of("CANT.", "DESCRIPCIÓN", "PRECIO", "IVA %", "TOTAL"),
                d.linesTable().headers());
        assertTrue(d.linesTable().rows().isEmpty());
        assertTrue(d.suplidos().isPresent());
    }

    @Test
    void facturaLargaSesentaFilasOrdenadas() {
        List<LineaFactura> lineas = new ArrayList<>();
        for (int i = 0; i < 60; i++) {
            lineas.add(linea("LINEA " + (i + 1) + " DESCRIPCION LARGA", "100.00", 21, false));
        }
        InvoiceDocument d = doc(version(), lineas);
        assertEquals(60, d.linesTable().rows().size());
        assertTrue(d.linesTable().rows().get(0).description().startsWith("LINEA 1 "));
        assertTrue(d.linesTable().rows().get(59).description().startsWith("LINEA 60 "));
        assertEquals("121,00", d.linesTable().rows().get(0).total());
    }

    @Test
    void anuladaMarcadaEnModelo() {
        FacturaVersion v = version();
        v.setEstado(EstadoFactura.ANULADA);
        assertTrue(doc(v, List.of(linea("ARMARIO EMPOTRADO", "3128.10", 21, false))).header().cancelled());
        assertFalse(doc(version(), List.of(linea("ARMARIO EMPOTRADO", "3128.10", 21, false))).header().cancelled());
    }

    @Test
    void rotulosFijosEnModelo() {
        InvoiceDocument d = doc(version(), List.of(linea("ARMARIO EMPOTRADO", "3128.10", 21, false)));
        assertEquals("FACTURAR A", d.clientCard().title());
        assertEquals(List.of("TIPO", "BASE IMPONIBLE", "CUOTA IVA"), d.totals().desgloseHeaders());
        assertEquals("LIQUIDACIÓN", d.totals().liquidation().title());
        assertEquals("C-59/7", d.header().number());
        assertEquals("14/07/2026", d.header().date());
    }
}
