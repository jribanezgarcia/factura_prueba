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
            assertTrue(texto.contains("C-59/7"));
            assertTrue(texto.contains("14/07/2026"));
            assertTrue(texto.contains("NIF: B04444444"));
            assertTrue(texto.contains("FACTURAR A"));
            assertTrue(texto.contains("DATOS DE PAGO"));
            assertTrue(texto.contains("MARIA MARTAGON AVALOS"));
            assertTrue(texto.contains("maria.martagon@correo.es"));
            assertFalse(texto.contains("Forma de pago"));
            assertFalse(texto.contains("Vencimiento"));
            assertTrue(texto.contains("3.785,00"));
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
            assertTrue(texto.contains("Base"));
            assertTrue(texto.contains("IVA 21%"));
            assertTrue(texto.contains("Descuento 10%"));
            assertTrue(texto.contains("-312,81"));
            assertTrue(texto.contains("3.406,50"));
            assertFalse(texto.contains("Base total"));
            assertFalse(texto.contains("IVA total"));
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
            assertTrue(texto.contains("Transferencia"));
            assertTrue(texto.contains("14/08/2026"));
            assertTrue(texto.contains("AURORA"));
        }
    }
}
