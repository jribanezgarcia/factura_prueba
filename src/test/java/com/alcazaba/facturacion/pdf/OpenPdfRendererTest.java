package com.alcazaba.facturacion.pdf;

import com.alcazaba.facturacion.model.EstadoFactura;
import com.alcazaba.facturacion.model.Factura;
import com.alcazaba.facturacion.model.FacturaVersion;
import com.alcazaba.facturacion.model.LineaFactura;
import com.alcazaba.facturacion.service.FacturaService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OpenPdfRendererTest {

    private LineaFactura lineaArmario() {
        LineaFactura l = new LineaFactura();
        l.setCantidad(1);
        l.setDescripcion("ARMARIO EMPOTRADO 248X335 4P CORREDERAS");
        l.setPrecioUnitario(new BigDecimal("3128.10"));
        l.setTotalBase(new BigDecimal("3128.10"));
        l.setTipoIvaId(1L);
        l.setIvaNombre("IVA 21%");
        l.setIvaPorcentaje(21);
        return l;
    }

    private FacturaVersion versionMuestra() {
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

    @Test
    void yTarjetasNoEntraEnAreaTexto() {
        assertEquals(108f, EstiloPdf.yTarjetas(100f, 0f), 0.001);
        float b = 500f;
        for (float h : new float[]{0f, 10f, 30f, 60f, 120f}) {
            assertTrue(EstiloPdf.yTarjetas(b, h) - h >= b - 0.001,
                    "borde inferior de tarjeta no debe entrar en area de texto h=" + h);
        }
        assertEquals(b + EstiloPdf.HUECO_TARJETAS, EstiloPdf.yTarjetas(b, 0f), 0.001);
        assertEquals(b + EstiloPdf.HUECO_TARJETAS + 50f, EstiloPdf.yTarjetas(b, 50f), 0.001);
    }

    @Test
    void marcoSinRenglones() {
        EstiloPdf.Colores c = new EstiloPdf.Colores(java.awt.Color.decode("#B08D57"));
        float hueco = 100f;
        com.lowagie.text.pdf.PdfPTable marco = new OpenPdfRenderer().tablaRelleno(hueco, c);
        assertEquals(1, marco.getRows().size(), "marco debe ser una sola fila");
        assertEquals(5, marco.getNumberOfColumns());
        assertEquals(0, marco.getHeaderRows(), "marco no debe repetir cabecera");
        marco.setTotalWidth(com.lowagie.text.PageSize.A4.getWidth() - 2 * 40f);
        marco.setLockedWidth(true);
        marco.calculateHeights(true);
        assertEquals(hueco, marco.getTotalHeight(), 0.5, "marco debe ocupar hueco exacto");
        com.lowagie.text.pdf.PdfPCell cell = marco.getRow(0).getCells()[0];
        assertEquals(com.lowagie.text.Rectangle.LEFT | com.lowagie.text.Rectangle.RIGHT | com.lowagie.text.Rectangle.BOTTOM, cell.getBorder());
    }

    @Test
    void alturasTarjetasDistintas() {
        FacturaVersion v = versionMuestra();
        v.setCliNombre("MARIA MARTAGON AVALOS");
        v.setCliNif("49122168X");
        v.setCliDireccion("C/ PROFESOR MULIAN Nº 41 1º A 6");
        v.setCliCp("04009");
        v.setCliLocalidad("ALMERIA");
        v.setCliProvincia("Almería");
        v.setCliEmail("maria.martagon@correo.es");
        v.setFormaPago("Transferencia");
        v.setVencimiento(LocalDate.of(2026, 8, 14));
        v.setRealizadaPor("AURORA");
        FacturaService.VersionCompleta vc = new FacturaService.VersionCompleta(new Factura(), v, List.of(lineaArmario()), null);
        OpenPdfRenderer renderer = new OpenPdfRenderer();
        EstiloPdf.Colores c = new EstiloPdf.Colores(java.awt.Color.decode("#B08D57"));
        // Con OpenPDF no se puede verificar el alto dibujado sin generar PDF y analizar el stream grafico
        com.lowagie.text.pdf.PdfPTable tarjetas = renderer.tarjetas(vc, c);
        com.lowagie.text.pdf.PdfPCell cellCliente = tarjetas.getRow(0).getCells()[0];
        com.lowagie.text.pdf.PdfPCell cellPago = tarjetas.getRow(0).getCells()[2];
        assertTrue(cellCliente.getCellEvent() == null, "borde no debe estar en celda exterior cliente");
        assertTrue(cellPago.getCellEvent() == null, "borde no debe estar en celda exterior pago");
        assertTrue(cellCliente.getTable() == null, "celda debe estar en modo composite");
        assertTrue(cellPago.getTable() == null, "celda debe estar en modo composite");
        com.lowagie.text.pdf.PdfPTable cliente = renderer.tarjetaCliente(vc, c);
        com.lowagie.text.pdf.PdfPTable pago = renderer.tarjetaPago(InvoiceDocumentBuilder.paymentCard(v).orElseThrow(), c);
        assertTrue(cliente.getTableEvent() instanceof EstiloPdf.ContornoTabla, "cliente debe tener borde en tabla");
        assertTrue(pago.getTableEvent() instanceof EstiloPdf.ContornoTabla, "pago debe tener borde en tabla");
        float ancho = com.lowagie.text.PageSize.A4.getWidth() - 2 * 40f;
        cliente.setTotalWidth(ancho * 0.49f);
        cliente.setLockedWidth(true);
        cliente.calculateHeights(true);
        pago.setTotalWidth(ancho * 0.49f);
        pago.setLockedWidth(true);
        pago.calculateHeights(true);
        assertTrue(Math.abs(cliente.getTotalHeight() - pago.getTotalHeight()) > 5f,
                "alturas deben ser distintas: cliente " + cliente.getTotalHeight() + " vs pago " + pago.getTotalHeight());
    }

    @Test
    void invarianteD4() throws Exception {
        FacturaVersion v = versionMuestra();
        v.setCliNombre("MARIA MARTAGON AVALOS");
        v.setCliNif("49122168X");
        v.setCliDireccion("C/ PROFESOR MULIAN Nº 41 1º A 6");
        v.setCliCp("04009");
        v.setCliLocalidad("ALMERIA");
        v.setCliProvincia("Almería");
        v.setCliEmail("maria.martagon@correo.es");
        v.setFormaPago("Transferencia");
        v.setVencimiento(LocalDate.of(2026, 8, 14));
        v.setRealizadaPor("AURORA");
        FacturaService.VersionCompleta vc = new FacturaService.VersionCompleta(new Factura(), v, List.of(lineaArmario()), null);
        OpenPdfRenderer renderer = new OpenPdfRenderer();
        EstiloPdf.Colores c = new EstiloPdf.Colores(java.awt.Color.decode("#B08D57"));
        com.lowagie.text.pdf.PdfPTable tarjetas = renderer.tarjetas(vc, c);
        float ancho = com.lowagie.text.PageSize.A4.getWidth() - 2 * 40f;
        tarjetas.setTotalWidth(ancho);
        tarjetas.setLockedWidth(true);
        tarjetas.calculateHeights(true);
        float altoTarjetas = tarjetas.getTotalHeight();
        com.lowagie.text.pdf.PdfPTable sinPago = renderer.tarjetasSinPago(vc, c);
        sinPago.setTotalWidth(ancho);
        sinPago.setLockedWidth(true);
        for (com.lowagie.text.pdf.PdfPCell cell : sinPago.getRow(0).getCells()) {
            if (cell != null) cell.setFixedHeight(altoTarjetas);
        }
        sinPago.calculateHeights(true);
        assertEquals(altoTarjetas, sinPago.getTotalHeight(), 0.5, "alto reservado debe ser igual al fijado en paginas siguientes");
    }
}
