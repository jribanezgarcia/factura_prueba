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
            assertTrue(texto.contains("Base imponible"));
            assertFalse(texto.contains("Subtotal"));
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
            assertTrue(texto.contains("Subtotal"));
            assertTrue(texto.contains("IVA 21%"));
            assertTrue(texto.contains("Descuento 10%"));
            assertTrue(texto.contains("Base imponible"));
            assertTrue(texto.contains("-312,81"));
            assertTrue(texto.contains("3.406,50"));
            assertFalse(texto.contains("Base total"));
            assertFalse(texto.contains("IVA total"));
            int iBase = texto.indexOf("Subtotal");
            int iDescuento = texto.indexOf("Descuento 10%");
            int iImponible = texto.indexOf("Base imponible");
            int iIva = texto.indexOf("IVA 21%");
            int iTotal = texto.lastIndexOf("TOTAL");
            assertTrue(iBase >= 0 && iBase < iDescuento && iDescuento < iImponible
                    && iImponible < iIva && iIva < iTotal);
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
            assertTrue(texto.contains("Base imponible 21%"));
            assertTrue(texto.contains("900,00"));
            assertTrue(texto.contains("Base imponible 10%"));
            assertTrue(texto.contains("450,00"));
            assertTrue(texto.contains("189,00"));
            assertTrue(texto.contains("45,00"));
            assertTrue(texto.contains("1.584,00"));
            int iSub21 = texto.indexOf("Subtotal 21%");
            int iSub10 = texto.indexOf("Subtotal 10%");
            int iDto = texto.indexOf("Descuento 10%");
            int iBase21 = texto.indexOf("Base imponible 21%");
            int iIva21 = texto.indexOf("IVA 21%");
            int iBase10 = texto.indexOf("Base imponible 10%");
            int iIva10 = texto.indexOf("IVA 10%");
            int iTotal = texto.lastIndexOf("TOTAL");
            assertTrue(iSub21 >= 0 && iSub21 < iSub10 && iSub10 < iDto && iDto < iBase21
                    && iBase21 < iIva21 && iIva21 < iBase10 && iBase10 < iIva10 && iIva10 < iTotal);
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
            assertTrue(texto.contains("IRPF profesional 15%"));
            assertTrue(texto.contains("-469,22"));
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
}
