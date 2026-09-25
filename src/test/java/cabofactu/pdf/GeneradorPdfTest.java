package cabofactu.pdf;

import cabofactu.modelo.dominio.Cliente;
import cabofactu.modelo.dominio.EstadoFactura;
import cabofactu.modelo.dominio.Factura;
import cabofactu.modelo.dominio.FormatoNumero;
import cabofactu.modelo.dominio.LineaFactura;
import cabofactu.modelo.dominio.Serie;
import com.lowagie.text.PageSize;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Comprobamos las tarjetas del PDF sin generar el documento entero. */
class GeneradorPdfTest {

    private LineaFactura lineaArmario() throws Exception {
        LineaFactura l = new LineaFactura(1, new BigDecimal("3128.10"));
        l.setDescripcion("ARMARIO EMPOTRADO 248X335 4P CORREDERAS");
        l.setTipoIvaId(1L);
        l.setIvaNombre("IVA 21%");
        l.setIvaPorcentaje(21);
        return l;
    }

    private Factura facturaMuestra(List<LineaFactura> lineas) throws Exception {
        Serie serie = new Serie("C", "Cocinas", FormatoNumero.MES, false);
        Cliente cliente = new Cliente("MARIA MARTAGON AVALOS", "49122168X",
                "C/ PROFESOR MULIAN Nº 41 1º A 6", "04009", "ALMERIA", "Almería");
        cliente.setEmail("maria.martagon@correo.es");
        Factura factura = new Factura(serie, LocalDate.of(2026, 7, 14), cliente);
        factura.setNumero("C-59/7");
        factura.setEstado(EstadoFactura.EMITIDA);
        factura.setDescuento(0);
        factura.setFormaPago("");
        factura.setRealizadaPor("");
        factura.setLineas(new ArrayList<>(lineas));
        return factura;
    }

    private Factura facturaConPago() throws Exception {
        Factura factura = facturaMuestra(List.of(lineaArmario()));
        factura.getCliente().setProvincia("Almería");
        factura.setFormaPago("Transferencia");
        factura.setVencimiento(LocalDate.of(2026, 8, 14));
        factura.setRealizadaPor("AURORA");
        return factura;
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
        EstiloPdf.Colores c = new EstiloPdf.Colores(Color.decode("#B08D57"));
        float hueco = 100f;
        PdfPTable marco = new GeneradorPdf().tablaRelleno(hueco, c);
        assertEquals(1, marco.getRows().size(), "marco debe ser una sola fila");
        assertEquals(5, marco.getNumberOfColumns());
        assertEquals(0, marco.getHeaderRows(), "marco no debe repetir cabecera");
        marco.setTotalWidth(PageSize.A4.getWidth() - 2 * 40f);
        marco.setLockedWidth(true);
        marco.calculateHeights(true);
        assertEquals(hueco, marco.getTotalHeight(), 0.5, "marco debe ocupar hueco exacto");
        PdfPCell cell = marco.getRow(0).getCells()[0];
        assertEquals(Rectangle.LEFT | Rectangle.RIGHT | Rectangle.BOTTOM, cell.getBorder());
    }

    @Test
    void alturasTarjetasDistintas() throws Exception {
        Factura factura = facturaConPago();
        GeneradorPdf renderer = new GeneradorPdf();
        EstiloPdf.Colores c = new EstiloPdf.Colores(Color.decode("#B08D57"));
        // Con OpenPDF no se puede verificar el alto dibujado sin generar PDF y analizar el stream grafico
        PdfPTable tarjetas = renderer.tarjetas(factura, c);
        PdfPCell cellCliente = tarjetas.getRow(0).getCells()[0];
        PdfPCell cellPago = tarjetas.getRow(0).getCells()[2];
        assertTrue(cellCliente.getCellEvent() == null, "borde no debe estar en celda exterior cliente");
        assertTrue(cellPago.getCellEvent() == null, "borde no debe estar en celda exterior pago");
        assertTrue(cellCliente.getTable() == null, "celda debe estar en modo composite");
        assertTrue(cellPago.getTable() == null, "celda debe estar en modo composite");
        PdfPTable cliente = renderer.tarjetaCliente(factura, c);
        assertTrue(ConstructorDocumentoFactura.paymentCard(factura).isPresent());
        PdfPTable pago = renderer.tarjetaPago(ConstructorDocumentoFactura.paymentCard(factura).get(), c);
        assertTrue(cliente.getTableEvent() instanceof EstiloPdf.ContornoTabla, "cliente debe tener borde en tabla");
        assertTrue(pago.getTableEvent() instanceof EstiloPdf.ContornoTabla, "pago debe tener borde en tabla");
        float ancho = PageSize.A4.getWidth() - 2 * 40f;
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
        Factura factura = facturaConPago();
        GeneradorPdf renderer = new GeneradorPdf();
        EstiloPdf.Colores c = new EstiloPdf.Colores(Color.decode("#B08D57"));
        PdfPTable tarjetas = renderer.tarjetas(factura, c);
        float ancho = PageSize.A4.getWidth() - 2 * 40f;
        tarjetas.setTotalWidth(ancho);
        tarjetas.setLockedWidth(true);
        tarjetas.calculateHeights(true);
        float altoTarjetas = tarjetas.getTotalHeight();
        PdfPTable sinPago = renderer.tarjetasSinPago(factura, c);
        sinPago.setTotalWidth(ancho);
        sinPago.setLockedWidth(true);
        for (PdfPCell cell : sinPago.getRow(0).getCells()) {
            if (cell != null) {
                cell.setFixedHeight(altoTarjetas);
            }
        }
        sinPago.calculateHeights(true);
        assertEquals(altoTarjetas, sinPago.getTotalHeight(), 0.5, "alto reservado debe ser igual al fijado en paginas siguientes");
    }
}
