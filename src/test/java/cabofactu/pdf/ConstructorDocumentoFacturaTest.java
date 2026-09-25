package cabofactu.pdf;

import cabofactu.modelo.dominio.Cliente;
import cabofactu.modelo.dominio.Empresa;
import cabofactu.modelo.dominio.EstadoFactura;
import cabofactu.modelo.dominio.Factura;
import cabofactu.modelo.dominio.FormatoNumero;
import cabofactu.modelo.dominio.LineaFactura;
import cabofactu.modelo.dominio.Serie;
import cabofactu.modelo.dominio.TipoRetencion;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Comprobamos el documento compuesto sin generar ningún PDF. */
class ConstructorDocumentoFacturaTest {

    private Empresa empresa() throws Exception {
        Empresa e = new Empresa("EMPRESA PRUEBA, S.C.", "12345678Z", "Calle Mayor 1", "28001",
                "Madrid", "Madrid", "contacto@empresaprueba.es", "910000000");
        e.setActividad("Cocinas y armarios");
        e.setCabeceraModo("TEXTO");
        e.setPieLegal("Protección de datos RGPD texto legal de prueba.");
        return e;
    }

    private Factura factura(List<LineaFactura> lineas) throws Exception {
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

    private LineaFactura linea(String desc, String base, Integer pct, boolean suplido) throws Exception {
        LineaFactura l = new LineaFactura(1, new BigDecimal(base));
        l.setDescripcion(desc);
        if (suplido) {
            l.setIvaNombre("Suplido");
        } else {
            l.setIvaNombre("IVA " + pct + "%");
        }
        l.setIvaPorcentaje(pct);
        l.setEsSuplido(suplido);
        return l;
    }

    private DocumentoFactura doc(Factura factura) throws Exception {
        return ConstructorDocumentoFactura.build(factura, empresa(), "#B08D57");
    }

    @Test
    void dosTiposDeIva() throws Exception {
        DocumentoFactura d = doc(factura(List.of(linea("CONCEPTO A", "1000.00", 21, false),
                linea("CONCEPTO B", "500.00", 10, false))));
        List<String> tipos = new ArrayList<>();
        for (DocumentoFactura.IvaRow fila : d.totals().ivaRows()) {
            tipos.add(fila.type());
        }
        assertEquals(List.of("21,00", "10,00"), tipos);
        assertEquals("1.000,00", d.totals().ivaRows().get(0).base());
        assertEquals("210,00", d.totals().ivaRows().get(0).quota());
        assertEquals("Totales", d.totals().totalsRow().type());
        assertEquals("1.500,00", d.totals().totalsRow().base());
        assertEquals("260,00", d.totals().totalsRow().quota());
        assertEquals("TOTAL", d.totals().liquidation().total().label());
        assertTrue(d.totals().liquidation().total().amount().contains("1.760,00"));
    }

    @Test
    void descuentoGlobal() throws Exception {
        Factura factura = factura(List.of(linea("ARMARIO EMPOTRADO", "3128.10", 21, false)));
        factura.setDescuento(10);
        DocumentoFactura d = doc(factura);
        assertEquals("2.815,29", d.totals().totalsRow().base());
        assertEquals("591,21", d.totals().totalsRow().quota());
        assertTrue(d.totals().discountNote().isPresent());
        assertTrue(d.totals().discountNote().get().contains("10 %"));
        assertTrue(d.totals().discountNote().get().contains("312,81"));
    }

    @Test
    void retencionDelSnapshot() throws Exception {
        Factura factura = factura(List.of(linea("ARMARIO EMPOTRADO", "3128.10", 21, false)));
        factura.setRetencion(new TipoRetencion("IRPF profesional", 15));
        DocumentoFactura d = doc(factura);
        assertTrue(d.totals().liquidation().retention().isPresent());
        assertEquals("IRPF profesional 15 %", d.totals().liquidation().retention().get().label());
        assertTrue(d.totals().liquidation().retention().get().amount().contains("469,22"));
    }

    @Test
    void suplidosYBloquePropio() throws Exception {
        DocumentoFactura d = doc(factura(List.of(linea("ARMARIO EMPOTRADO", "3128.10", 21, false),
                linea("TASAS", "250.00", null, true))));
        assertTrue(d.suplidos().isPresent());
        assertEquals(List.of("SUPLIDOS", "IMPORTE"), d.suplidos().get().headers());
        assertEquals(1, d.suplidos().get().rows().size());
        assertEquals("TASAS", d.suplidos().get().rows().get(0).description());
        assertEquals("250,00", d.suplidos().get().rows().get(0).amount());
        assertTrue(d.suplidos().get().note().contains("No sujetos a IVA ni a retención"));
        assertTrue(d.totals().liquidation().suplidos().isPresent());
    }

    @Test
    void soloSuplidosTablaConSoloCabecera() throws Exception {
        DocumentoFactura d = doc(factura(List.of(linea("TASAS", "250.00", null, true))));
        assertEquals(List.of("CANT.", "DESCRIPCIÓN", "PRECIO", "IVA %", "TOTAL"),
                d.linesTable().headers());
        assertTrue(d.linesTable().rows().isEmpty());
        assertTrue(d.suplidos().isPresent());
    }

    @Test
    void facturaLargaSesentaFilasOrdenadas() throws Exception {
        List<LineaFactura> lineas = new ArrayList<>();
        for (int i = 0; i < 60; i++) {
            lineas.add(linea("LINEA " + (i + 1) + " DESCRIPCION LARGA", "100.00", 21, false));
        }
        DocumentoFactura d = doc(factura(lineas));
        assertEquals(60, d.linesTable().rows().size());
        assertTrue(d.linesTable().rows().get(0).description().startsWith("LINEA 1 "));
        assertTrue(d.linesTable().rows().get(59).description().startsWith("LINEA 60 "));
        assertEquals("121,00", d.linesTable().rows().get(0).total());
    }

    @Test
    void anuladaMarcadaEnModelo() throws Exception {
        Factura anulada = factura(List.of(linea("ARMARIO EMPOTRADO", "3128.10", 21, false)));
        anulada.setEstado(EstadoFactura.ANULADA);
        assertTrue(doc(anulada).header().cancelled());
        assertFalse(doc(factura(List.of(linea("ARMARIO EMPOTRADO", "3128.10", 21, false)))).header().cancelled());
    }

    @Test
    void rotulosFijosEnModelo() throws Exception {
        DocumentoFactura d = doc(factura(List.of(linea("ARMARIO EMPOTRADO", "3128.10", 21, false))));
        assertEquals("FACTURAR A", d.clientCard().title());
        assertEquals(List.of("TIPO", "BASE IMPONIBLE", "CUOTA IVA"), d.totals().desgloseHeaders());
        assertEquals("LIQUIDACIÓN", d.totals().liquidation().title());
        assertEquals("C-59/7", d.header().number());
        assertEquals("14/07/2026", d.header().date());
    }
}
