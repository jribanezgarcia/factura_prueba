package com.alcazaba.facturacion.pdf;

import com.alcazaba.facturacion.model.Empresa;
import com.alcazaba.facturacion.model.EstadoFactura;
import com.alcazaba.facturacion.model.LineaFactura;
import com.alcazaba.facturacion.model.ResumenFactura;
import com.alcazaba.facturacion.service.CalculoService;
import com.alcazaba.facturacion.service.FacturaService;
import com.alcazaba.facturacion.util.Formatos;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfGState;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;

import java.awt.Color;
import java.io.FileOutputStream;
import java.nio.file.Path;

/**
 * Exportacion a PDF de una factura/rectificativa con OpenPDF: A4 vertical,
 * cabecera (texto de empresa o logo) y pie legal repetidos en todas las
 * paginas, Página X de Y, tabla de lineas con descripciones ajustadas,
 * bloque BASE/IVA/DESCUENTO/TOTAL con desglose por tipo de IVA, formato
 * espanol y marca ANULADA en facturas anuladas. El PDF refleja exactamente
 * la version indicada.
 */
public class PdfService {

    private static final Font F_TITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20);
    private static final Font F_SUBTITULO = FontFactory.getFont(FontFactory.HELVETICA, 11);
    private static final Font F_TEXTO = FontFactory.getFont(FontFactory.HELVETICA, 10);
    private static final Font F_TEXTO_BOLD = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
    private static final Font F_PEQUENO = FontFactory.getFont(FontFactory.HELVETICA, 8);
    private static final Font F_CABECERA = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);
    private static final Font F_TOTAL = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13);

    private static final Color COLOR_TITULO = new Color(0x1F, 0x2A, 0x44);
    private static final Color COLOR_TEXTO = new Color(0x33, 0x33, 0x33);
    private static final Color COLOR_GRIS = new Color(0x66, 0x66, 0x66);
    private static final Color COLOR_BORDE = new Color(0xCC, 0xCC, 0xCC);
    private static final Color COLOR_FONDO = new Color(0xF5, 0xF5, 0xF5);
    private static final Color COLOR_ANULADA = new Color(0xB0, 0x00, 0x20);

    public void exportar(FacturaService.VersionCompleta vc, Empresa empresa, Path ruta) throws Exception {
        boolean anulada = vc.version().getEstado() == EstadoFactura.ANULADA;
        boolean rectificativa = vc.version().getReferenciaRectifica() != null
                && !vc.version().getReferenciaRectifica().isBlank();
        ResumenFactura resumen = CalculoService.resumen(vc.lineas(), vc.version().getDescuentoPorcentaje());

        try (Document doc = new Document(PageSize.A4, 40, 40, 150, 110)) {
            PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream(ruta.toFile()));
            writer.setPageEvent(new CabeceraPie(empresa, anulada));
            doc.open();

            doc.add(titulo(vc, rectificativa, anulada));
            doc.add(new Paragraph(new Phrase(" ", F_TEXTO)));
            doc.add(bloqueCliente(vc));
            doc.add(new Paragraph(new Phrase(" ", F_TEXTO)));
            doc.add(tablaLineas(vc));
            doc.add(new Paragraph(new Phrase(" ", F_TEXTO)));
            doc.add(bloqueTotales(resumen, vc.version().getDescuentoPorcentaje()));
            if (vc.version().getObservaciones() != null && !vc.version().getObservaciones().isBlank()) {
                doc.add(new Paragraph(new Phrase(" ", F_TEXTO)));
                Paragraph obs = new Paragraph("Observaciones", F_TEXTO_BOLD);
                obs.add(new Phrase("\n" + vc.version().getObservaciones(), F_TEXTO));
                doc.add(obs);
            }
        }
    }

    private PdfPTable titulo(FacturaService.VersionCompleta vc, boolean rectificativa, boolean anulada) {
        PdfPTable t = new PdfPTable(1);
        t.setWidthPercentage(100);
        t.setHorizontalAlignment(Element.ALIGN_LEFT);

        Paragraph izq = new Paragraph();
        izq.add(new Chunk(rectificativa ? "RECTIFICATIVA" : "FACTURA", F_TITULO));
        izq.add(new Phrase("\nNúmero: ", F_SUBTITULO));
        izq.add(new Phrase(nz(vc.version().getNumero()), F_TEXTO_BOLD));
        izq.add(new Phrase("\nFecha: ", F_SUBTITULO));
        izq.add(new Phrase(Formatos.fecha(vc.version().getFechaFactura()), F_TEXTO_BOLD));
        if (rectificativa) {
            izq.add(new Phrase("\nRectifica a: ", F_SUBTITULO));
            izq.add(new Phrase(nz(vc.version().getReferenciaRectifica()), F_TEXTO_BOLD));
        }
        if (anulada) {
            izq.add(new Phrase("\n", F_SUBTITULO));
            izq.add(new Chunk("ESTADO: ANULADA",
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, COLOR_ANULADA)));
        }
        PdfPCell cIzq = new PdfPCell(izq);
        cIzq.setBorder(Rectangle.NO_BORDER);
        t.addCell(cIzq);

        return t;
    }

    private PdfPTable bloqueCliente(FacturaService.VersionCompleta vc) {
        PdfPTable t = new PdfPTable(1);
        t.setWidthPercentage(55);
        t.setHorizontalAlignment(Element.ALIGN_LEFT);
        t.setSpacingBefore(2);

        PdfPCell c = new PdfPCell();
        c.setBorder(Rectangle.BOX);
        c.setPadding(6);

        Paragraph p = new Paragraph("Cliente", F_CABECERA);
        p.add(new Phrase("\n" + nz(vc.version().getCliNombre()), F_TEXTO_BOLD));
        if (vc.version().getCliNif() != null && !vc.version().getCliNif().isBlank()) {
            p.add(new Phrase("\nNIF: " + vc.version().getCliNif(), F_TEXTO));
        }
        if (vc.version().getCliDireccion() != null && !vc.version().getCliDireccion().isBlank()) {
            p.add(new Phrase("\n" + vc.version().getCliDireccion(), F_TEXTO));
        }
        String poblacion = joinNoVacio(", ", vc.version().getCliCp(), vc.version().getCliLocalidad());
        if (!poblacion.isBlank()) {
            p.add(new Phrase("\n" + poblacion, F_TEXTO));
        }
        if (vc.version().getCliProvincia() != null && !vc.version().getCliProvincia().isBlank()) {
            p.add(new Phrase("\n" + vc.version().getCliProvincia(), F_TEXTO));
        }
        c.setPhrase(p);
        t.addCell(c);
        return t;
    }

    private PdfPTable tablaLineas(FacturaService.VersionCompleta vc) {
        PdfPTable t = new PdfPTable(new float[]{0.6f, 4.2f, 1.4f, 1.0f, 1.6f});
        t.setWidthPercentage(100);
        t.setHorizontalAlignment(Element.ALIGN_LEFT);

        t.addCell(header("Cant."));
        t.addCell(header("Descripción"));
        t.addCell(header("Precio"));
        t.addCell(header("IVA %"));
        t.addCell(header("Total (base)"));

        int fila = 0;
        for (LineaFactura l : vc.lineas()) {
            t.addCell(celda(String.valueOf(l.getCantidad()), fila, Element.ALIGN_RIGHT));
            t.addCell(celda(nz(l.getDescripcion()), fila, Element.ALIGN_LEFT));
            t.addCell(celda(Formatos.moneda(l.getPrecioUnitario()), fila, Element.ALIGN_RIGHT));
            t.addCell(celda(l.isExenta()
                    ? "Exento"
                    : l.getIvaPorcentaje() + "%", fila, Element.ALIGN_CENTER));
            t.addCell(celda(Formatos.moneda(l.getTotalBase()), fila, Element.ALIGN_RIGHT));
            fila++;
        }
        return t;
    }

    private PdfPCell header(String texto) {
        PdfPCell c = new PdfPCell(new Phrase(texto, F_CABECERA));
        c.setPadding(4);
        c.setBorderColor(COLOR_BORDE);
        return c;
    }

    private PdfPCell celda(String texto, int fila, int alineacion) {
        PdfPCell c = new PdfPCell(new Phrase(texto, F_TEXTO));
        c.setHorizontalAlignment(alineacion);
        c.setPadding(4);
        c.setBorderColor(COLOR_BORDE);
        if (fila % 2 == 1) {
            c.setBackgroundColor(COLOR_FONDO);
        }
        return c;
    }

    private PdfPTable bloqueTotales(ResumenFactura r, int descuento) {
        PdfPTable t = new PdfPTable(new float[]{3.2f, 1.4f});
        t.setWidthPercentage(46);
        t.setHorizontalAlignment(Element.ALIGN_RIGHT);
        t.setSpacingBefore(2);

        for (ResumenFactura.IvaGrupo g : r.getGrupos()) {
            String nombre = g.isExento()
                    ? "Base exenta" + (g.getMotivoExencion() != null && !g.getMotivoExencion().isBlank()
                            ? " (" + g.getMotivoExencion() + ")"
                            : "")
                    : "Base " + g.getPorcentaje() + "%";
            t.addCell(filaResumen(nombre, Formatos.moneda(g.getBase())));
            t.addCell(filaResumen("  IVA " + (g.isExento() ? "exento" : g.getPorcentaje() + "%"),
                    Formatos.moneda(g.getCuota())));
        }
        if (descuento > 0) {
            t.addCell(filaResumen("Descuento aplicado: " + descuento + "%", ""));
            t.addCell(filaResumen("", ""));
        }
        t.addCell(filaResumen("Base total", Formatos.moneda(r.getBaseTotal())));
        t.addCell(filaResumen("IVA total", Formatos.moneda(r.getIvaTotal())));

        PdfPCell cTotal = new PdfPCell(new Phrase("TOTAL", F_TOTAL));
        cTotal.setBorder(Rectangle.TOP);
        cTotal.setPaddingTop(4);
        t.addCell(cTotal);
        PdfPCell cImporte = new PdfPCell(new Phrase(Formatos.moneda(r.getTotal()), F_TOTAL));
        cImporte.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cImporte.setBorder(Rectangle.TOP);
        cImporte.setPaddingTop(4);
        t.addCell(cImporte);
        return t;
    }

    private PdfPCell filaResumen(String etiqueta, String valor) {
        PdfPCell c = new PdfPCell(new Phrase(etiqueta, F_TEXTO));
        c.setBorder(Rectangle.NO_BORDER);
        c.setPadding(1);
        PdfPCell c2 = new PdfPCell(new Phrase(valor, F_TEXTO));
        c2.setHorizontalAlignment(Element.ALIGN_RIGHT);
        c2.setBorder(Rectangle.NO_BORDER);
        c2.setPadding(1);
        return c;
    }

    private String joinNoVacio(String sep, String... partes) {
        StringBuilder sb = new StringBuilder();
        for (String p : partes) {
            if (p != null && !p.isBlank()) {
                if (sb.length() > 0) {
                    sb.append(sep);
                }
                sb.append(p);
            }
        }
        return sb.toString();
    }

    private String nz(String s) {
        return s == null ? "" : s;
    }

    /**
     * Repite cabecera y pie en todas las paginas y dibuja la marca ANULADA.
     */
    private static final class CabeceraPie extends PdfPageEventHelper {

        private final Empresa empresa;
        private final boolean anulada;
        private PdfTemplate total;
        private Image logo;

        CabeceraPie(Empresa empresa, boolean anulada) {
            this.empresa = empresa;
            this.anulada = anulada;
        }

        @Override
        public void onOpenDocument(PdfWriter writer, Document document) {
            total = writer.getDirectContent().createTemplate(28, 12);
            cargarLogo();
        }

        private void cargarLogo() {
            if (empresa == null || empresa.getLogoPath() == null || empresa.getLogoPath().isBlank()) {
                return;
            }
            try {
                logo = Image.getInstance(empresa.getLogoPath());
                float w = empresa.getLogoAncho() != null ? empresa.getLogoAncho() : 120f;
                float h = empresa.getLogoAlto() != null ? empresa.getLogoAlto() : 60f;
                logo.scaleToFit(w, h);
            } catch (Exception ignored) {
                logo = null;
            }
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte cb = writer.getDirectContent();
            Rectangle page = document.getPageSize();
            float left = document.leftMargin();
            float right = page.getWidth() - document.rightMargin();
            float top = page.getHeight() - document.topMargin();

            if (logo != null) {
                float x = left + (empresa != null ? empresa.getLogoX() : 0);
                float y = top + (empresa != null ? empresa.getLogoY() : 0);
                logo.setAbsolutePosition(x, y);
                cb.addImage(logo);
            } else if (empresa != null) {
                dibujarCabeceraTexto(cb, left, top);
            }
            dibujarSeparador(cb, left, top, right);
            dibujarPie(writer, document, left, right);
            if (anulada) {
                dibujarMarcaAnulada(writer, document, page);
            }
        }

        private void dibujarCabeceraTexto(PdfContentByte cb, float left, float top) {
            float y = top + 10;
            cb.beginText();
            cb.setFontAndSize(FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16).getBaseFont(), 16);
            cb.setColorFill(COLOR_TITULO);
            cb.showTextAligned(Element.ALIGN_LEFT, nz(empresa.getNombre()), left, y, 0);
            y += 18;
            cb.setFontAndSize(FontFactory.getFont(FontFactory.HELVETICA, 10).getBaseFont(), 10);
            cb.setColorFill(COLOR_TEXTO);
            for (String linea : lineasEmpresa()) {
                cb.showTextAligned(Element.ALIGN_LEFT, linea, left, y, 0);
                y += 12;
            }
            cb.endText();
        }

        private String[] lineasEmpresa() {
            StringBuilder sb = new StringBuilder();
            append(sb, empresa.getActividad());
            append(sb, "NIF: " + empresa.getNif());
            append(sb, empresa.getDireccion());
            append(sb, join(empresa.getCp(), empresa.getLocalidad()));
            append(sb, empresa.getProvincia());
            append(sb, join(empresa.getEmail(), empresa.getTelefono()));
            return sb.toString().split("\n");
        }

        private void append(StringBuilder sb, String s) {
            if (s != null && !s.isBlank()) {
                if (sb.length() > 0) {
                    sb.append('\n');
                }
                sb.append(s);
            }
        }

        private String join(String a, String b) {
            if (a == null || a.isBlank()) {
                return b == null ? "" : b;
            }
            if (b == null || b.isBlank()) {
                return a;
            }
            return a + " · " + b;
        }

        private void dibujarSeparador(PdfContentByte cb, float left, float top, float right) {
            cb.setColorStroke(COLOR_BORDE);
            cb.setLineWidth(0.8f);
            cb.moveTo(left, top + 2);
            cb.lineTo(right, top + 2);
            cb.stroke();
        }

        private void dibujarPie(PdfWriter writer, Document document, float left, float right) {
            PdfContentByte cb = writer.getDirectContent();
            float bottom = document.bottomMargin();
            float y = bottom - 10;

            String pie = empresa != null && empresa.getPieLegal() != null ? empresa.getPieLegal() : "";
            String[] lineas = pie.split("\n");
            cb.beginText();
            cb.setFontAndSize(FontFactory.getFont(FontFactory.HELVETICA, 8).getBaseFont(), 8);
            cb.setColorFill(COLOR_GRIS);
            for (int i = lineas.length - 1; i >= 0; i--) {
                cb.showTextAligned(Element.ALIGN_LEFT, lineas[i], left, y, 0);
                y -= 10;
            }
            cb.endText();

            cb.beginText();
            cb.setFontAndSize(FontFactory.getFont(FontFactory.HELVETICA, 8).getBaseFont(), 8);
            cb.setColorFill(COLOR_GRIS);
            cb.showTextAligned(Element.ALIGN_LEFT, "Página " + writer.getPageNumber() + " de ",
                    right - 30, bottom - 10, 0);
            cb.addTemplate(total, right - 30, bottom - 10);
            cb.endText();
        }

        private void dibujarMarcaAnulada(PdfWriter writer, Document document, Rectangle page) {
            PdfContentByte cb = writer.getDirectContent();
            cb.saveState();
            PdfGState gs = new PdfGState();
            gs.setFillOpacity(0.2f);
            cb.setGState(gs);
            cb.beginText();
            cb.setFontAndSize(FontFactory.getFont(FontFactory.HELVETICA_BOLD, 64).getBaseFont(), 64);
            cb.setColorFill(COLOR_ANULADA);
            cb.showTextAligned(Element.ALIGN_CENTER, "ANULADA",
                    (page.getLeft() + page.getRight()) / 2,
                    (page.getTop() + page.getBottom()) / 2, 45);
            cb.endText();
            cb.restoreState();
        }

        @Override
        public void onCloseDocument(PdfWriter writer, Document document) {
            ColumnText.showTextAligned(total, Element.ALIGN_LEFT,
                    new Phrase(String.valueOf(writer.getPageNumber()), F_PEQUENO), 0, 0, 0);
        }

        private String nz(String s) {
            return s == null ? "" : s;
        }
    }
}
