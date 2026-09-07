package com.alcazaba.facturacion.pdf;

import com.alcazaba.facturacion.model.Empresa;
import com.alcazaba.facturacion.model.EstadoFactura;
import com.alcazaba.facturacion.model.Factura;
import com.alcazaba.facturacion.model.FacturaVersion;
import com.alcazaba.facturacion.model.LineaFactura;
import com.alcazaba.facturacion.service.FacturaService;
import com.lowagie.text.pdf.PdfDictionary;
import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.parser.PdfTextExtractor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PdfServiceTest {

    @TempDir
    Path tempDir;

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

    private Empresa empresaTexto() {
        Empresa empresa = new Empresa();
        empresa.setNombre("COMERCIAL ALCAZABA, S.C.");
        empresa.setNif("B04444444");
        empresa.setActividad("Cocinas y armarios");
        empresa.setDireccion("Avda. Alhambra nº 18");
        empresa.setCp("04007");
        empresa.setLocalidad("Almería");
        empresa.setProvincia("Almería");
        empresa.setEmail("cial.alcazaba@hotmail.com");
        empresa.setCabeceraModo("TEXTO");
        empresa.setPieLegal("Protección de datos RGPD texto legal de prueba.");
        return empresa;
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

    private LineaFactura linea(String base, Integer porcentaje) {
        LineaFactura l = new LineaFactura();
        l.setCantidad(1);
        l.setDescripcion("CONCEPTO " + porcentaje + "%");
        l.setPrecioUnitario(new BigDecimal(base));
        l.setTotalBase(new BigDecimal(base));
        l.setIvaNombre("IVA " + porcentaje + "%");
        l.setIvaPorcentaje(porcentaje);
        return l;
    }

    private static int contarApariciones(String texto, String aguja) {
        int n = 0;
        int idx = texto.indexOf(aguja);
        while (idx >= 0) {
            n++;
            idx = texto.indexOf(aguja, idx + aguja.length());
        }
        return n;
    }

    private String textoDe(PdfReader reader) throws IOException {
        StringBuilder sb = new StringBuilder();
        PdfTextExtractor extractor = new PdfTextExtractor(reader);
        for (int p = 1; p <= reader.getNumberOfPages(); p++) {
            sb.append(extractor.getTextFromPage(p)).append('\n');
        }
        return sb.toString();
    }

    @Test
    void exportaDisenoAprobadoConTotalConIvaYTarjetas() throws Exception {
        FacturaService.VersionCompleta vc = new FacturaService.VersionCompleta(
                new Factura(), versionMuestra(), List.of(lineaArmario()), null);

        Path destino = tempDir.resolve("C-59-7.pdf");
        new PdfService().exportar(vc, empresaTexto(), destino, "#B08D57");

        assertTrue(Files.exists(destino));
        assertTrue(Files.size(destino) > 500);

        try (PdfReader reader = new PdfReader(destino.toString())) {
            String texto = textoDe(reader);
            assertTrue(texto.contains("FACTURA"));
            assertTrue(texto.contains("SERIE / Nº"));
            assertTrue(texto.contains("FECHA"));
            assertTrue(texto.contains("C-59/7"));
            assertTrue(texto.contains("14/07/2026"));
            assertTrue(texto.contains("NIF: B04444444"));
            assertTrue(texto.contains("FACTURAR A"));
            assertTrue(texto.contains("Nombre"));
            assertTrue(texto.contains("Dirección"));
            assertTrue(texto.contains("Código postal"));
            assertTrue(texto.contains("Población"));
            assertTrue(texto.contains("04009"));
            assertTrue(texto.contains("ALMERIA"));
            assertTrue(texto.contains("Email"));
            assertFalse(texto.contains("Provincia"));
            assertFalse(texto.contains("DATOS DE PAGO"));
            assertTrue(texto.contains("MARIA MARTAGON AVALOS"));
            assertTrue(texto.contains("maria.martagon@correo.es"));
            assertFalse(texto.contains("Forma de pago"));
            assertFalse(texto.contains("Vencimiento"));
            assertTrue(texto.contains("3.785,00"));
            assertTrue(texto.contains("TIPO"));
            assertTrue(texto.contains("BASE IMPONIBLE"));
            assertTrue(texto.contains("CUOTA IVA"));
            assertTrue(texto.contains("21,00"));
            assertTrue(texto.contains("Totales"));
            assertTrue(texto.contains("LIQUIDACIÓN"));
            assertTrue(texto.contains("Base imponible"));
            assertTrue(texto.contains("Total IVA repercutido"));
            assertFalse(texto.contains("Subtotal"));
            assertFalse(texto.contains("Base imponible 21%"));
            assertTrue(texto.contains("RGPD"));
        }
    }

    @Test
    void facturaAnuladaIncluyeLaMarca() throws Exception {
        FacturaVersion v = versionMuestra();
        v.setEstado(EstadoFactura.ANULADA);
        FacturaService.VersionCompleta vc = new FacturaService.VersionCompleta(
                new Factura(), v, List.of(lineaArmario()), null);

        Path destino = tempDir.resolve("anulada.pdf");
        new PdfService().exportar(vc, empresaTexto(), destino, null);

        try (PdfReader reader = new PdfReader(destino.toString())) {
            String texto = textoDe(reader);
            assertTrue(texto.contains("ANULADA"));
        }
    }

    @Test
    void totalesConDescuentoSeMuestranRestandoYCuadran() throws Exception {
        FacturaVersion v = versionMuestra();
        v.setDescuentoPorcentaje(10);
        FacturaService.VersionCompleta vc = new FacturaService.VersionCompleta(
                new Factura(), v, List.of(lineaArmario()), null);

        Path destino = tempDir.resolve("descuento.pdf");
        new PdfService().exportar(vc, empresaTexto(), destino, "#B08D57");

        try (PdfReader reader = new PdfReader(destino.toString())) {
            String texto = textoDe(reader);
            assertTrue(texto.contains("21,00"));
            assertTrue(texto.contains("2.815,29"));
            assertTrue(texto.contains("591,21"));
            assertTrue(texto.contains("Totales"));
            assertTrue(texto.contains("descuento comercial del 10 %"));
            assertTrue(texto.contains("−312,81"));
            assertTrue(texto.contains("3.128,10"));
            assertTrue(texto.contains("3.406,50"));
            assertFalse(texto.contains("Subtotal"));
            assertFalse(texto.contains("Descuento 10%"));
            assertFalse(texto.contains("Base imponible 21%"));
            assertFalse(texto.contains("Base total"));
            assertFalse(texto.contains("IVA total"));
            int iTipo = texto.indexOf("TIPO");
            int iTotales = texto.indexOf("Totales");
            int iNota = texto.indexOf("descuento comercial");
            int iLiquida = texto.indexOf("LIQUIDACIÓN");
            int iIva = texto.indexOf("Total IVA repercutido");
            int iTotal = texto.lastIndexOf("TOTAL");
            assertTrue(iTipo >= 0 && iTipo < iTotales && iTotales < iNota && iNota < iLiquida
                    && iLiquida < iIva && iIva < iTotal);
        }
    }

    @Test
    void desgloseConVariosTiposYDescuentoMuestraBasePorTipo() throws Exception {
        FacturaVersion v = versionMuestra();
        v.setDescuentoPorcentaje(10);
        FacturaService.VersionCompleta vc = new FacturaService.VersionCompleta(
                new Factura(), v, List.of(linea("1000.00", 21), linea("500.00", 10)), null);

        Path destino = tempDir.resolve("varios-tipos.pdf");
        new PdfService().exportar(vc, empresaTexto(), destino, "#B08D57");

        try (PdfReader reader = new PdfReader(destino.toString())) {
            String texto = textoDe(reader);
            assertTrue(texto.contains("21,00"));
            assertTrue(texto.contains("900,00"));
            assertTrue(texto.contains("10,00"));
            assertTrue(texto.contains("450,00"));
            assertTrue(texto.contains("189,00"));
            assertTrue(texto.contains("45,00"));
            assertTrue(texto.contains("Totales"));
            assertTrue(texto.contains("1.350,00"));
            assertTrue(texto.contains("234,00"));
            assertTrue(texto.contains("1.584,00"));
            assertFalse(texto.contains("Subtotal"));
            assertFalse(texto.contains("Descuento 10%"));
            assertFalse(texto.contains("Base imponible 21%"));
            assertFalse(texto.contains("Base imponible 10%"));
            int i21 = texto.indexOf("21,00");
            int i10 = texto.indexOf("10,00 450,00");
            int iTotales = texto.indexOf("Totales 1.350,00");
            int iIva = texto.indexOf("Total IVA repercutido");
            int iTotal = texto.lastIndexOf("TOTAL");
            assertTrue(i21 >= 0 && i21 < i10 && i10 < iTotales
                    && iTotales < iIva && iIva < iTotal);
        }
    }

    @Test
    void elPdfNoUsaRotulosDeLaEscalera() throws Exception {
        FacturaVersion v = versionMuestra();
        v.setDescuentoPorcentaje(10);
        FacturaService.VersionCompleta vc = new FacturaService.VersionCompleta(
                new Factura(), v, List.of(linea("1000.00", 21), linea("500.00", 10),
                        lineaExenta("ASESORAMIENTO", "200.00")), null);

        Path destino = tempDir.resolve("sin-rotulos-escalera.pdf");
        new PdfService().exportar(vc, empresaTexto(), destino, "#B08D57");

        try (PdfReader reader = new PdfReader(destino.toString())) {
            String texto = textoDe(reader);
            assertTrue(texto.contains("descuento comercial del 10 %"));
            assertFalse(texto.contains("Subtotal"));
            assertFalse(texto.contains("Base imponible 21%"));
            assertFalse(texto.contains("Base imponible 10%"));
            assertFalse(texto.contains("Base exenta"));
            assertFalse(texto.contains("Subtotal exento"));
            assertEquals(1, contarApariciones(texto, "descuento comercial"));
        }
    }

    @Test
    void exentoMuestraGuionYSumaEnTotales() throws Exception {
        FacturaService.VersionCompleta vc = new FacturaService.VersionCompleta(
                new Factura(), versionMuestra(),
                List.of(linea("1000.00", 21), lineaExenta("ASESORAMIENTO", "200.00")), null);

        Path destino = tempDir.resolve("exento.pdf");
        new PdfService().exportar(vc, empresaTexto(), destino, "#B08D57");

        try (PdfReader reader = new PdfReader(destino.toString())) {
            String texto = textoDe(reader);
            assertTrue(texto.contains("Exento"));
            assertTrue(texto.contains("—"));
            assertTrue(texto.contains("Totales"));
            assertTrue(texto.contains("1.200,00"));
            assertTrue(texto.contains("210,00"));
            assertFalse(texto.contains("Base exenta"));
        }
    }

    @Test
    void elSimboloDeMonedaApareceUnaSolaVez() throws Exception {
        FacturaVersion v = versionMuestra();
        v.setDescuentoPorcentaje(10);
        v.setTipoRetencionId(1L);
        v.setTipoRetencionNombre("IRPF profesional");
        v.setTipoRetencionPorcentaje(15);
        FacturaService.VersionCompleta vc = new FacturaService.VersionCompleta(
                new Factura(), v,
                List.of(lineaArmario(), lineaSuplido("TASAS", "250.00")), null);

        Path destino = tempDir.resolve("un-simbolo.pdf");
        new PdfService().exportar(vc, empresaTexto(), destino, "#B08D57");

        try (PdfReader reader = new PdfReader(destino.toString())) {
            String texto = textoDe(reader);
            assertEquals(1, contarApariciones(texto, "€"),
                    "El símbolo € debe aparecer una sola vez, en la banda TOTAL");
            assertTrue(texto.contains("TOTAL"));
        }
    }

    @Test
    void retencionApareceComoFilaPropiaEnElPdf() throws Exception {
        FacturaVersion v = versionMuestra();
        v.setTipoRetencionId(1L);
        v.setTipoRetencionNombre("IRPF profesional");
        v.setTipoRetencionPorcentaje(15);
        FacturaService.VersionCompleta vc = new FacturaService.VersionCompleta(
                new Factura(), v, List.of(lineaArmario()), null);

        Path destino = tempDir.resolve("retencion.pdf");
        new PdfService().exportar(vc, empresaTexto(), destino, "#B08D57");

        try (PdfReader reader = new PdfReader(destino.toString())) {
            String texto = textoDe(reader);
            assertTrue(texto.contains("IRPF profesional 15 %"));
            assertTrue(texto.contains("−469,22"));
        }
    }

    @Test
    void suplidosAparecenEntreRetencionYTotal() throws Exception {
        FacturaVersion v = versionMuestra();
        v.setTipoRetencionId(1L);
        v.setTipoRetencionNombre("IRPF profesional");
        v.setTipoRetencionPorcentaje(15);
        LineaFactura suplido = new LineaFactura();
        suplido.setCantidad(1);
        suplido.setDescripcion("TASAS");
        suplido.setPrecioUnitario(new BigDecimal("250.00"));
        suplido.setTotalBase(new BigDecimal("250.00"));
        suplido.setIvaNombre("Suplido");
        suplido.setIvaPorcentaje(null);
        suplido.setEsSuplido(true);
        FacturaService.VersionCompleta vc = new FacturaService.VersionCompleta(
                new Factura(), v, List.of(lineaArmario(), suplido), null);

        Path destino = tempDir.resolve("suplidos.pdf");
        new PdfService().exportar(vc, empresaTexto(), destino, "#B08D57");

        try (PdfReader reader = new PdfReader(destino.toString())) {
            String texto = textoDe(reader);
            assertTrue(texto.contains("Suplidos +250,00"));
            assertTrue(texto.contains("3.565,78"));
            int iBloque = texto.indexOf("SUPLIDOS");
            int iRetencion = texto.indexOf("IRPF profesional 15 %");
            int iSuplidosTotales = texto.lastIndexOf("Suplidos +250,00");
            int iTotal = texto.lastIndexOf("TOTAL");
            assertTrue(iBloque >= 0 && iBloque < iRetencion && iRetencion < iSuplidosTotales
                    && iSuplidosTotales < iTotal);
        }
    }

    private LineaFactura lineaExenta(String descripcion, String base) {
        LineaFactura l = new LineaFactura();
        l.setCantidad(1);
        l.setDescripcion(descripcion);
        l.setPrecioUnitario(new BigDecimal(base));
        l.setTotalBase(new BigDecimal(base));
        l.setIvaNombre("Exento");
        l.setIvaPorcentaje(null);
        l.setIvaMotivoExencion("Art. 20.1");
        l.setIvaImporte(BigDecimal.ZERO);
        return l;
    }

    private LineaFactura lineaSuplido(String descripcion, String importe) {
        LineaFactura l = new LineaFactura();
        l.setCantidad(1);
        l.setDescripcion(descripcion);
        l.setPrecioUnitario(new BigDecimal(importe));
        l.setTotalBase(new BigDecimal(importe));
        l.setIvaNombre("Suplido");
        l.setIvaPorcentaje(null);
        l.setEsSuplido(true);
        return l;
    }

    @Test
    void suplidoTieneBloquePropioYNoSeRotulaExento() throws Exception {
        FacturaService.VersionCompleta vc = new FacturaService.VersionCompleta(
                new Factura(), versionMuestra(),
                List.of(lineaArmario(), lineaExenta("ASESORAMIENTO EXENTO", "200.00"),
                        lineaSuplido("TASAS MUNICIPALES SUPLIDAS", "250.00")), null);

        Path destino = tempDir.resolve("bloque-suplidos.pdf");
        new PdfService().exportar(vc, empresaTexto(), destino, "#B08D57");

        try (PdfReader reader = new PdfReader(destino.toString())) {
            String texto = textoDe(reader);
            assertTrue(texto.contains("SUPLIDOS"));
            assertTrue(texto.contains("No sujetos a IVA ni a retención"));
            assertTrue(texto.contains("250,00"));
            assertTrue(texto.contains("4.235,00"));
            assertTrue(texto.contains("Exento"));
            int iBloque = texto.indexOf("SUPLIDOS");
            int iExenta = texto.indexOf("ASESORAMIENTO EXENTO");
            int iSuplido = texto.indexOf("TASAS MUNICIPALES SUPLIDAS");
            assertTrue(iExenta >= 0 && iExenta < iBloque);
            assertTrue(iBloque >= 0 && iBloque < iSuplido);
        }
    }

    @Test
    void sinSuplidosNoHayBloqueNiNota() throws Exception {
        FacturaService.VersionCompleta vc = new FacturaService.VersionCompleta(
                new Factura(), versionMuestra(), List.of(lineaArmario()), null);

        Path destino = tempDir.resolve("sin-suplidos.pdf");
        new PdfService().exportar(vc, empresaTexto(), destino, "#B08D57");

        try (PdfReader reader = new PdfReader(destino.toString())) {
            String texto = textoDe(reader);
            assertFalse(texto.contains("SUPLIDOS"));
            assertFalse(texto.contains("No sujetos a IVA ni a retención"));
            assertTrue(texto.contains("DESCRIPCIÓN"));
            assertTrue(texto.contains("3.785,00"));
        }
    }

    @Test
    void soloSuplidosMuestraTablaVacia() throws Exception {
        FacturaService.VersionCompleta vc = new FacturaService.VersionCompleta(
                new Factura(), versionMuestra(), List.of(lineaSuplido("TASAS", "250.00")), null);

        Path destino = tempDir.resolve("solo-suplidos.pdf");
        new PdfService().exportar(vc, empresaTexto(), destino, "#B08D57");

        try (PdfReader reader = new PdfReader(destino.toString())) {
            String texto = textoDe(reader);
            assertTrue(texto.contains("DESCRIPCIÓN"));
            assertTrue(texto.contains("CANT."));
            assertTrue(texto.contains("SUPLIDOS"));
            assertTrue(texto.contains("TASAS"));
            assertTrue(texto.contains("250,00"));
        }
    }

    @Test
    void paginacionReflejaPaginasReales() throws Exception {
        List<LineaFactura> lineas = new ArrayList<>();
        for (int i = 0; i < 60; i++) {
            LineaFactura l = lineaArmario();
            l.setDescripcion("LINEA " + (i + 1) + " DESCRIPCION LARGA PARA OCUPAR VARIAS PAGINAS");
            lineas.add(l);
        }
        FacturaService.VersionCompleta vc = new FacturaService.VersionCompleta(
                new Factura(), versionMuestra(), lineas, null);

        Path destino = tempDir.resolve("larga.pdf");
        new PdfService().exportar(vc, empresaTexto(), destino, null);

        try (PdfReader reader = new PdfReader(destino.toString())) {
            int paginas = reader.getNumberOfPages();
            assertTrue(paginas >= 2);
            String texto = textoDe(reader);
            assertTrue(texto.contains("Página 1 de "));
            assertTrue(texto.contains("Página " + paginas + " de "));
        }
    }

    @Test
    void fuenteEmbebidaSegunDisponibilidad() throws Exception {
        FacturaService.VersionCompleta vc = new FacturaService.VersionCompleta(
                new Factura(), versionMuestra(), List.of(lineaArmario()), null);
        Path destino = tempDir.resolve("fuente.pdf");
        new PdfService().exportar(vc, empresaTexto(), destino, null);

        try (PdfReader reader = new PdfReader(destino.toString())) {
            PdfDictionary recursos = reader.getPageN(1).getAsDict(PdfName.RESOURCES);
            PdfDictionary fuentes = recursos.getAsDict(PdfName.FONT);
            boolean calibri = false;
            if (fuentes != null) {
                for (PdfName key : fuentes.getKeys()) {
                    PdfDictionary f = fuentes.getAsDict(key);
                    if (f != null && f.getAsName(PdfName.BASEFONT) != null
                            && f.getAsName(PdfName.BASEFONT).toString().contains("Calibri")) {
                        calibri = true;
                        break;
                    }
                }
            }
            assertTrue(PdfService.CALIBRI == null || calibri);
            assertTrue(PdfService.CALIBRI != null || !calibri);
        }
    }

    @Test
    void nombreEmpresaLargoNoSolapaFactura() throws Exception {
        Empresa e = empresaTexto();
        e.setNombre("COMERCIAL ALCAZABA SOCIEDAD COLECTIVA DE COCINAS Y MUEBLES DE ALMERIA Y ANEXOS S.C.");
        FacturaService.VersionCompleta vc = new FacturaService.VersionCompleta(
                new Factura(), versionMuestra(), List.of(lineaArmario()), null);

        Path destino = tempDir.resolve("nombre-largo.pdf");
        new PdfService().exportar(vc, e, destino, null);

        try (PdfReader reader = new PdfReader(destino.toString())) {
            String texto = textoDe(reader);
            assertTrue(texto.contains("C-59/7"));
            assertTrue(texto.contains("14/07/2026"));
        }
    }

    @Test
    void datosDePagoRellenosAparecenEnElPdf() throws Exception {
        FacturaVersion v = versionMuestra();
        v.setFormaPago("Transferencia");
        java.time.LocalDate vencimiento = LocalDate.of(2026, 8, 14);
        v.setVencimiento(vencimiento);
        v.setRealizadaPor("AURORA");
        FacturaService.VersionCompleta vc = new FacturaService.VersionCompleta(
                new Factura(), v, List.of(lineaArmario()), null);

        Path destino = tempDir.resolve("pago.pdf");
        new PdfService().exportar(vc, empresaTexto(), destino, "#96744A");

        try (PdfReader reader = new PdfReader(destino.toString())) {
            String texto = textoDe(reader);
            assertTrue(texto.contains("DATOS DE PAGO"));
            assertTrue(texto.contains("Transferencia"));
            assertTrue(texto.contains("14/08/2026"));
            assertTrue(texto.contains("AURORA"));
        }
    }

    @Test
    void datosDePagoVaciosOcultanLaTarjeta() throws Exception {
        FacturaService.VersionCompleta vc = new FacturaService.VersionCompleta(
                new Factura(), versionMuestra(), List.of(lineaArmario()), null);

        Path destino = tempDir.resolve("sin-pago.pdf");
        new PdfService().exportar(vc, empresaTexto(), destino, "#B08D57");

        try (PdfReader reader = new PdfReader(destino.toString())) {
            String texto = textoDe(reader);
            assertFalse(texto.contains("DATOS DE PAGO"));
            assertFalse(texto.contains("Forma de pago"));
            assertTrue(texto.contains("FACTURAR A"));
        }
    }

    @Test
    void codigoPostalYProvinciaFilasPropias() throws Exception {
        FacturaVersion v = versionMuestra();
        v.setCliLocalidad("ALMERIA");
        v.setCliProvincia("ALMERÍA");
        FacturaService.VersionCompleta vc = new FacturaService.VersionCompleta(
                new Factura(), v, List.of(lineaArmario()), null);

        Path destino = tempDir.resolve("cp-provincia.pdf");
        new PdfService().exportar(vc, empresaTexto(), destino, "#B08D57");

        try (PdfReader reader = new PdfReader(destino.toString())) {
            String texto = textoDe(reader);
            assertTrue(texto.contains("Código postal"));
            assertTrue(texto.contains("04009"));
            assertTrue(texto.contains("Provincia"));
            assertTrue(texto.contains("ALMERÍA"));
        }
    }

    @Test
    void exportarAgrupadoUneDosFacturasEnUnSoloPdf() throws Exception {
        FacturaService.VersionCompleta vc1 = new FacturaService.VersionCompleta(
                new Factura(), versionMuestra(), List.of(lineaArmario()), null);
        FacturaVersion v2 = versionMuestra();
        v2.setNumero("C-59/8");
        v2.setCliNombre("OTRO CLIENTE");
        FacturaService.VersionCompleta vc2 = new FacturaService.VersionCompleta(
                new Factura(), v2, List.of(lineaArmario()), null);

        Path destino = tempDir.resolve("agrupado.pdf");
        new PdfService().exportarAgrupado(List.of(vc1, vc2), empresaTexto(), destino, "#B08D57");

        assertTrue(Files.exists(destino));
        assertTrue(Files.size(destino) > 500);
        try (PdfReader reader = new PdfReader(destino.toString())) {
            assertTrue(reader.getNumberOfPages() >= 2);
        }
    }

    @Test
    void yTarjetasNoEntraEnAreaTexto() {
        assertEquals(108f, PdfService.yTarjetas(100f, 0f), 0.001);
        float b = 500f;
        for (float h : new float[]{0f, 10f, 30f, 60f, 120f}) {
            assertTrue(PdfService.yTarjetas(b, h) - h >= b - 0.001,
                    "borde inferior de tarjeta no debe entrar en area de texto h=" + h);
        }
        assertEquals(b + PdfService.HUECO_TARJETAS, PdfService.yTarjetas(b, 0f), 0.001);
        assertEquals(b + PdfService.HUECO_TARJETAS + 50f, PdfService.yTarjetas(b, 50f), 0.001);
    }

    private FacturaVersion versionConPago() {
        FacturaVersion v = versionMuestra();
        v.setFormaPago("Transferencia");
        v.setRealizadaPor("Juan");
        v.setVencimiento(LocalDate.of(2026, 10, 1));
        return v;
    }

    private List<LineaFactura> lineasN(int n) {
        List<LineaFactura> out = new ArrayList<>();
        for (int i = 0; i < n; i++) out.add(lineaArmario());
        return out;
    }

    private String textoPagina(PdfReader reader, int pagina) throws IOException {
        return new PdfTextExtractor(reader).getTextFromPage(pagina);
    }

    @Test
    void numeroPaginasPorCasos() throws Exception {
        assertEquals(1, paginasDe(lineasN(2), null, null));
        assertEquals(1, paginasDe(lineasN(10), null, null));
        assertEquals(1, paginasDe(lineasN(20), null, null));
        assertEquals(1, paginasDe(List.of(lineaArmario(), lineaSuplido("TASAS", "250.00")), null, null));
        assertEquals(1, paginasDe(List.of(lineaSuplido("TASAS", "250.00")), null, null));
    }

    private int paginasDe(List<LineaFactura> lineas, String obs, FacturaVersion ver) throws Exception {
        FacturaVersion v = ver != null ? ver : versionMuestra();
        FacturaService.VersionCompleta vc = new FacturaService.VersionCompleta(new Factura(), v, lineas, null);
        if (obs != null) v.setObservaciones(obs);
        Path destino = tempDir.resolve("pag-" + System.nanoTime() + ".pdf");
        new PdfService().exportar(vc, empresaTexto(), destino, "#B08D57");
        try (PdfReader r = new PdfReader(destino.toString())) { return r.getNumberOfPages(); }
    }

    @Test
    void cierreEnUltimaPagina() throws Exception {
        List<LineaFactura> lineas = new ArrayList<>();
        for (int i = 0; i < 60; i++) {
            LineaFactura l = lineaArmario();
            l.setDescripcion("LINEA " + (i + 1) + " DESCRIPCION LARGA PARA OCUPAR VARIAS PAGINAS");
            lineas.add(l);
        }
        FacturaVersion v = versionMuestra();
        FacturaService.VersionCompleta vc = new FacturaService.VersionCompleta(new Factura(), v, lineas, null);
        Path destino = tempDir.resolve("cierre-ultima.pdf");
        new PdfService().exportar(vc, empresaTexto(), destino, "#B08D57");
        try (PdfReader r = new PdfReader(destino.toString())) {
            int n = r.getNumberOfPages();
            assertTrue(n >= 2, "factura larga debe tener al menos 2 paginas");
            for (int p = 1; p < n; p++) {
                String t = textoPagina(r, p);
                assertFalse(t.contains("LIQUIDACIÓN"), "LIQUIDACION no debe estar en pagina " + p);
                assertFalse(t.contains("€"), "banda TOTAL con euro no debe estar en pagina " + p);
            }
            String ultima = textoPagina(r, n);
            assertTrue(ultima.contains("LIQUIDACIÓN"), "LIQUIDACION debe estar en ultima pagina");
            assertTrue(ultima.contains("€"), "banda TOTAL con euro debe estar en ultima pagina");
        }
    }

    @Test
    void tarjetaPorPagina() throws Exception {
        List<LineaFactura> lineas = new ArrayList<>();
        for (int i = 0; i < 60; i++) {
            LineaFactura l = lineaArmario();
            l.setDescripcion("LINEA " + (i + 1) + " DESCRIPCION LARGA PARA OCUPAR VARIAS PAGINAS");
            lineas.add(l);
        }
        FacturaVersion v = versionConPago();
        FacturaService.VersionCompleta vc = new FacturaService.VersionCompleta(new Factura(), v, lineas, null);
        Path destino = tempDir.resolve("tarjeta-pagina.pdf");
        new PdfService().exportar(vc, empresaTexto(), destino, "#B08D57");
        try (PdfReader r = new PdfReader(destino.toString())) {
            int n = r.getNumberOfPages();
            assertTrue(n >= 2, "debe tener al menos 2 paginas");
            String all = textoDe(r);
            assertEquals(n, contarApariciones(all, "FACTURAR A"), "FACTURAR A una vez por pagina");
            assertEquals(1, contarApariciones(all, "DATOS DE PAGO"), "DATOS DE PAGO solo pagina 1");
            for (int p = 1; p <= n; p++) {
                String tp = textoPagina(r, p);
                assertTrue(tp.contains("FACTURAR A"), "pagina " + p + " debe tener FACTURAR A");
                if (p == 1) assertTrue(tp.contains("DATOS DE PAGO"));
                else assertFalse(tp.contains("DATOS DE PAGO"), "pagina " + p + " no debe tener DATOS DE PAGO");
            }
        }
    }

    @Test
    void ningunaPaginaEnBlanco() throws Exception {
        List<LineaFactura> lineas = new ArrayList<>();
        for (int i = 0; i < 60; i++) {
            LineaFactura l = lineaArmario();
            l.setDescripcion("LINEA " + (i + 1) + " LARGA");
            lineas.add(l);
        }
        FacturaVersion v = versionConPago();
        FacturaService.VersionCompleta vc = new FacturaService.VersionCompleta(new Factura(), v, lineas, null);
        Path destino = tempDir.resolve("no-blanco.pdf");
        new PdfService().exportar(vc, empresaTexto(), destino, "#B08D57");
        try (PdfReader r = new PdfReader(destino.toString())) {
            int n = r.getNumberOfPages();
            for (int p = 1; p <= n; p++) {
                String t = textoPagina(r, p).trim();
                assertFalse(t.isBlank(), "pagina " + p + " no debe estar vacia");
                // cada pagina debe tener al menos cabecera+contenido: al menos 30 caracteres extraibles
                assertTrue(t.length() > 30, "pagina " + p + " parece vacia: '" + t.substring(0, Math.min(30, t.length())) + "'");
            }
        }
    }

    @Test
    void marcoSinRenglones() {
        PdfService.Colores c = new PdfService.Colores(java.awt.Color.decode("#B08D57"));
        float hueco = 100f;
        com.lowagie.text.pdf.PdfPTable marco = new PdfService().tablaRelleno(hueco, c);
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
    void pieLegalApareceUnaSolaVez() throws Exception {
        String pieReal = Files.readString(Path.of("capturas_pantalla/pie_factura.txt")).trim();
        Empresa emp = empresaTexto();
        emp.setPieLegal(pieReal);
        List<LineaFactura> lineas = new ArrayList<>();
        for (int i = 0; i < 60; i++) {
            LineaFactura l = lineaArmario();
            l.setDescripcion("LINEA " + (i + 1) + " DESCRIPCION LARGA PARA OCUPAR VARIAS PAGINAS");
            lineas.add(l);
        }
        FacturaService.VersionCompleta vc = new FacturaService.VersionCompleta(new Factura(), versionMuestra(), lineas, null);
        Path destino = tempDir.resolve("pie-unico.pdf");
        new PdfService().exportar(vc, emp, destino, "#B08D57");
        try (PdfReader r = new PdfReader(destino.toString())) {
            int n = r.getNumberOfPages();
            assertTrue(n >= 2);
            String fragmento = "Protección de Datos";
            for (int p = 1; p < n; p++) {
                String t = textoPagina(r, p);
                assertFalse(t.contains(fragmento), "pie no debe estar en pagina " + p);
            }
            assertTrue(textoPagina(r, n).contains(fragmento), "pie debe estar en ultima pagina");
        }
    }

    @Test
    void seGanaEspacioConPieLargo() throws Exception {
        String pieReal = Files.readString(Path.of("capturas_pantalla/pie_factura.txt")).trim();
        Empresa empLargo = empresaTexto();
        empLargo.setPieLegal(pieReal);
        assertEquals(1, paginasDe(lineasN(20), null, null));
        FacturaVersion v = versionMuestra();
        int paginasLargo = paginasDeConEmpresa(lineasN(20), empLargo, v);
        assertEquals(1, paginasLargo, "20 lineas con pie largo debe seguir en 1 pagina");
        int paginas10Largo = paginasDeConEmpresa(lineasN(10), empLargo, v);
        assertEquals(1, paginas10Largo);
    }

    private int paginasDeConEmpresa(List<LineaFactura> lineas, Empresa emp, FacturaVersion ver) throws Exception {
        FacturaService.VersionCompleta vc = new FacturaService.VersionCompleta(new Factura(), ver, lineas, null);
        Path destino = tempDir.resolve("pag-emp-" + System.nanoTime() + ".pdf");
        new PdfService().exportar(vc, emp, destino, "#B08D57");
        try (PdfReader r = new PdfReader(destino.toString())) { return r.getNumberOfPages(); }
    }

    @Test
    void cabeceraRepetida() throws Exception {
        List<LineaFactura> lineas = new ArrayList<>();
        for (int i = 0; i < 60; i++) {
            LineaFactura l = lineaArmario();
            l.setDescripcion("LINEA " + (i + 1) + " DESCRIPCION LARGA");
            lineas.add(l);
        }
        FacturaService.VersionCompleta vc = new FacturaService.VersionCompleta(new Factura(), versionMuestra(), lineas, null);
        Path destino = tempDir.resolve("cabecera-rep.pdf");
        new PdfService().exportar(vc, empresaTexto(), destino, "#B08D57");
        try (PdfReader r = new PdfReader(destino.toString())) {
            assertTrue(r.getNumberOfPages() >= 2);
            String p2 = textoPagina(r, 2);
            assertTrue(p2.contains("DESCRIPCIÓN"), "pagina 2 debe repetir cabecera DESCRIPCIÓN");
            assertTrue(p2.contains("CANT."), "pagina 2 debe repetir CANT.");
        }
    }

    @Test
    void modoLogoGeneraPdfCorrecto() throws Exception {
        Empresa emp = empresaTexto();
        emp.setCabeceraModo("LOGO");
        emp.setLogoPath("logos/image-1788446954273.png");
        FacturaVersion v = versionMuestra();
        FacturaService.VersionCompleta vc = new FacturaService.VersionCompleta(new Factura(), v, lineasN(2), null);
        Path destino = tempDir.resolve("logo-2.pdf");
        new PdfService().exportar(vc, emp, destino, "#B08D57");
        try (PdfReader r = new PdfReader(destino.toString())) {
            assertEquals(1, r.getNumberOfPages());
            String ultima = textoPagina(r, r.getNumberOfPages());
            assertTrue(ultima.contains("LIQUIDACIÓN"));
        }
        List<LineaFactura> lineas = new ArrayList<>();
        for (int i = 0; i < 60; i++) {
            LineaFactura l = lineaArmario();
            l.setDescripcion("LINEA " + (i + 1) + " LARGA");
            lineas.add(l);
        }
        FacturaService.VersionCompleta vc2 = new FacturaService.VersionCompleta(new Factura(), versionMuestra(), lineas, null);
        Path destino2 = tempDir.resolve("logo-60.pdf");
        new PdfService().exportar(vc2, emp, destino2, "#B08D57");
        try (PdfReader r = new PdfReader(destino2.toString())) {
            assertTrue(r.getNumberOfPages() >= 2);
            String ultima = textoPagina(r, r.getNumberOfPages());
            assertTrue(ultima.contains("LIQUIDACIÓN"));
        }
    }

    @Test
    void anuladaVariasPaginas() throws Exception {
        List<LineaFactura> lineas = new ArrayList<>();
        for (int i = 0; i < 60; i++) {
            LineaFactura l = lineaArmario();
            l.setDescripcion("LINEA " + (i + 1) + " LARGA PARA ANULADA");
            lineas.add(l);
        }
        FacturaVersion v = versionMuestra();
        v.setEstado(EstadoFactura.ANULADA);
        FacturaService.VersionCompleta vc = new FacturaService.VersionCompleta(new Factura(), v, lineas, null);
        Path destino = tempDir.resolve("anulada-multi.pdf");
        new PdfService().exportar(vc, empresaTexto(), destino, "#B08D57");
        try (PdfReader r = new PdfReader(destino.toString())) {
            int n = r.getNumberOfPages();
            assertTrue(n >= 2);
            for (int p = 1; p <= n; p++) {
                String t = textoPagina(r, p);
                assertTrue(t.contains("ANULADA"), "pagina " + p + " debe tener ANULADA");
                assertTrue(t.contains("FACTURAR A"), "pagina " + p + " tarjeta debe ser extraible");
                assertFalse(t.isBlank());
            }
        }
    }

    @Test
    void cierreSolitarioConTabla() throws Exception {
        String pieReal = Files.readString(Path.of("capturas_pantalla/pie_factura.txt")).trim();
        Empresa emp = empresaTexto();
        emp.setPieLegal(pieReal);
        FacturaVersion v = versionMuestra();
        v.setObservaciones("Observacion larga que ocupa espacio y ayuda a forzar el salto de pagina del cierre. ".repeat(10));
        List<LineaFactura> lineas = new ArrayList<>();
        for (int i = 0; i < 28; i++) {
            LineaFactura l = lineaArmario();
            l.setDescripcion("LINEA " + (i + 1) + " DESCRIPCION LARGA PARA FORZAR PAGINA");
            lineas.add(l);
        }
        FacturaService.VersionCompleta vc = new FacturaService.VersionCompleta(new Factura(), v, lineas, null);
        Path destino = tempDir.resolve("cierre-solitario.pdf");
        new PdfService().exportar(vc, emp, destino, "#B08D57");
        try (PdfReader r = new PdfReader(destino.toString())) {
            int n = r.getNumberOfPages();
            assertTrue(n >= 2, "debe tener al menos 2 paginas para forzar cierre solitario");
            String ultima = textoPagina(r, n);
            assertTrue(ultima.contains("DESCRIPCIÓN"), "ultima pagina debe tener cabecera DESCRIPCIÓN");
            assertTrue(ultima.contains("LIQUIDACIÓN"), "ultima pagina debe tener cierre LIQUIDACIÓN");
            assertTrue(ultima.contains("€"), "ultima pagina debe tener banda TOTAL");
            String penultima = n > 1 ? textoPagina(r, n - 1) : "";
            assertFalse(penultima.contains("LIQUIDACIÓN"), "penultima no debe tener cierre");
        }
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
        PdfService svc = new PdfService();
        PdfService.Colores c = new PdfService.Colores(java.awt.Color.decode("#B08D57"));
        // Con OpenPDF no se puede verificar el alto dibujado sin generar PDF y analizar el stream grafico
        com.lowagie.text.pdf.PdfPTable tarjetas = svc.tarjetas(vc, c);
        com.lowagie.text.pdf.PdfPCell cellCliente = tarjetas.getRow(0).getCells()[0];
        com.lowagie.text.pdf.PdfPCell cellPago = tarjetas.getRow(0).getCells()[2];
        assertTrue(cellCliente.getCellEvent() == null, "borde no debe estar en celda exterior cliente");
        assertTrue(cellPago.getCellEvent() == null, "borde no debe estar en celda exterior pago");
        assertTrue(cellCliente.getTable() == null, "celda debe estar en modo composite");
        assertTrue(cellPago.getTable() == null, "celda debe estar en modo composite");
        com.lowagie.text.pdf.PdfPTable cliente = svc.tarjetaCliente(vc, c);
        com.lowagie.text.pdf.PdfPTable pago = svc.tarjetaPago(svc.filasDatosPago(vc), c);
        assertTrue(cliente.getTableEvent() instanceof PdfService.ContornoTabla, "cliente debe tener borde en tabla");
        assertTrue(pago.getTableEvent() instanceof PdfService.ContornoTabla, "pago debe tener borde en tabla");
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
        PdfService svc = new PdfService();
        PdfService.Colores c = new PdfService.Colores(java.awt.Color.decode("#B08D57"));
        com.lowagie.text.pdf.PdfPTable tarjetas = svc.tarjetas(vc, c);
        float ancho = com.lowagie.text.PageSize.A4.getWidth() - 2 * 40f;
        tarjetas.setTotalWidth(ancho);
        tarjetas.setLockedWidth(true);
        tarjetas.calculateHeights(true);
        float altoTarjetas = tarjetas.getTotalHeight();
        com.lowagie.text.pdf.PdfPTable sinPago = svc.tarjetasSinPago(vc, c);
        sinPago.setTotalWidth(ancho);
        sinPago.setLockedWidth(true);
        for (com.lowagie.text.pdf.PdfPCell cell : sinPago.getRow(0).getCells()) {
            if (cell != null) cell.setFixedHeight(altoTarjetas);
        }
        sinPago.calculateHeights(true);
        assertEquals(altoTarjetas, sinPago.getTotalHeight(), 0.5, "alto reservado debe ser igual al fijado en paginas siguientes");
    }
}
