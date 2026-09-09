package com.alcazaba.facturacion.pdf;

import com.alcazaba.facturacion.model.Empresa;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfGState;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;

import java.util.List;

// ------------------------------------------------------------------
// Cabecera y pie repetidos en todas las paginas
// ------------------------------------------------------------------

final class CabeceraPiePdf extends PdfPageEventHelper {

    private final Empresa empresa;
    private final Image logo;
    private final InvoiceDocument.Header header;
    private final EstiloPdf.Colores c;
    private final PdfPTable tarjetas;
    private final PdfPTable tarjetasSinPago;
    private final float altoTarjetas;
    private final boolean conPago;
    private PdfTemplate totalPaginas;
    private int paginasReales;

    CabeceraPiePdf(Empresa empresa, Image logo, InvoiceDocument.Header header, EstiloPdf.Colores colores,
                PdfPTable tarjetas, PdfPTable tarjetasSinPago, float altoTarjetas, boolean conPago) {
        this.empresa = empresa;
        this.logo = logo;
        this.header = header;
        this.c = colores;
        this.tarjetas = tarjetas;
        this.tarjetasSinPago = tarjetasSinPago;
        this.altoTarjetas = altoTarjetas;
        this.conPago = conPago;
    }

    @Override
    public void onOpenDocument(PdfWriter writer, Document document) {
        totalPaginas = writer.getDirectContent().createTemplate(28, 12);
        paginasReales = 0;
    }

    @Override
    public void onEndPage(PdfWriter writer, Document document) {
        paginasReales++;
        PdfContentByte cb = writer.getDirectContent();
        Rectangle pagina = document.getPageSize();
        float izquierda = document.leftMargin();
        float derecha = pagina.getWidth() - document.rightMargin();
        float bordeSuperiorContenido = pagina.getHeight() - document.topMargin();
        float bordeInferiorContenido = document.bottomMargin();

        dibujarBloqueFactura(cb, derecha, pagina.getHeight());
        if (logo != null) {
            dibujarLogo(cb, izquierda, bordeSuperiorContenido);
            float xInfo = izquierda + CabeceraLayout.ANCHO_LOGO_FIJO + 14f;
            dibujarDatosEmpresa(cb, xInfo, pagina.getHeight() - 34, 13f,
                    Math.max(derecha - EstiloPdf.RESERVA_FACTURA - xInfo, 80f));
        } else {
            dibujarDatosEmpresa(cb, izquierda, pagina.getHeight() - 34, 15f,
                    Math.max(derecha - EstiloPdf.RESERVA_FACTURA - izquierda, 80f));
        }
        dibujarSeparador(cb, izquierda, derecha, bordeSuperiorContenido, altoTarjetas);
        dibujarTarjetas(cb, izquierda, bordeSuperiorContenido);
        dibujarPaginacion(writer, cb, derecha);
        if (header.cancelled()) {
            dibujarMarcaAnulada(writer, cb, pagina);
        }
    }

    private void dibujarTarjetas(PdfContentByte cb, float izquierda, float bordeSuperiorContenido) {
        if (tarjetas == null) return;
        PdfPTable tabla = (paginasReales == 1 || !conPago) ? tarjetas : tarjetasSinPago;
        if (tabla == null) tabla = tarjetas;
        float y = EstiloPdf.yTarjetas(bordeSuperiorContenido, altoTarjetas);
        tabla.writeSelectedRows(0, -1, izquierda, y, cb);
    }

    private void dibujarLogo(PdfContentByte cb, float izquierda, float bordeSuperiorContenido) {
        logo.scaleToFit(CabeceraLayout.ANCHO_LOGO_FIJO, CabeceraLayout.ALTO_LOGO_FIJO);
        logo.setAbsolutePosition(izquierda, bordeSuperiorContenido + CabeceraLayout.HUECO_LOGO_INFERIOR + altoTarjetas + EstiloPdf.HUECO_TARJETAS);
        try {
            cb.addImage(logo);
        } catch (Exception ignored) {
        }
    }

    private void dibujarDatosEmpresa(PdfContentByte cb, float x, float yInicial, float tamNombre,
                                     float anchoDisponible) {
        float y = yInicial;
        String nombre = nz(empresa.getNombre());
        BaseFont bfNombre = EstiloPdf.baseNegrita();
        float tamano = ajustarTamano(nombre, bfNombre, tamNombre, anchoDisponible);
        cb.beginText();
        cb.setFontAndSize(bfNombre, tamano);
        cb.setColorFill(c.oscuro);
        cb.showTextAligned(Element.ALIGN_LEFT, nombre, x, y, 0);
        cb.endText();
        y -= tamNombre + 1;
        for (CabeceraLayout.LineaCabecera linea : lineasEmpresa(empresa)) {
            if (linea.chipNif) {
                dibujarChipNif(cb, x, y, anchoDisponible);
            } else {
                BaseFont bf = EstiloPdf.baseRegular();
                float t = ajustarTamano(linea.texto, bf, 9f, anchoDisponible);
                cb.beginText();
                cb.setFontAndSize(bf, t);
                cb.setColorFill(EstiloPdf.GRIS);
                cb.showTextAligned(Element.ALIGN_LEFT, linea.texto, x, y, 0);
                cb.endText();
            }
            y -= 13;
        }
    }

    /**
     * Reduce el tamaño de la fuente por pasos hasta que el texto cabe en el
     * ancho disponible (minimo 9pt para seguir siendo legible).
     */
    private float ajustarTamano(String texto, BaseFont bf, float tamanoInicial, float anchoMaximo) {
        float t = tamanoInicial;
        while (t > 9f && bf.getWidthPoint(texto, t) > anchoMaximo) {
            t -= 0.5f;
        }
        return Math.max(t, 9f);
    }

    private void dibujarChipNif(PdfContentByte cb, float x, float yBase, float anchoDisponible) {
        String texto = "NIF: " + nz(empresa.getNif());
        BaseFont bf = EstiloPdf.baseNegrita();
        float t = ajustarTamano(texto, bf, 9f, Math.max(anchoDisponible - 10f, 40f));
        float ancho = bf.getWidthPoint(texto, t);
        cb.setColorFill(c.claro);
        cb.roundRectangle(x - 4, yBase - 3.5f, ancho + 10, 12.5f, EstiloPdf.RADIO_CHIP);
        cb.fill();
        cb.setColorStroke(c.bordeTabla);
        cb.setLineWidth(0.6f);
        cb.roundRectangle(x - 4, yBase - 3.5f, ancho + 10, 12.5f, EstiloPdf.RADIO_CHIP);
        cb.stroke();
        cb.beginText();
        cb.setFontAndSize(bf, t);
        cb.setColorFill(c.oscuro);
        cb.showTextAligned(Element.ALIGN_LEFT, texto, x + 1, yBase, 0);
        cb.endText();
    }

    private void dibujarBloqueFactura(PdfContentByte cb, float derecha, float altoPagina) {
        float y = altoPagina - 38;
        String titulo = header.corrective() ? "RECTIFICATIVA" : "FACTURA";
        cb.beginText();
        cb.setFontAndSize(EstiloPdf.baseNegrita(), 18);
        cb.setColorFill(c.oscuro);
        cb.showTextAligned(Element.ALIGN_RIGHT, titulo, derecha, y, 0);
        cb.endText();
        y -= 22;
        dibujarRotulo(cb, "SERIE / Nº", derecha, y);
        y -= 9;
        cb.beginText();
        cb.setFontAndSize(EstiloPdf.baseNegrita(), 10);
        cb.setColorFill(c.oscuro);
        cb.showTextAligned(Element.ALIGN_RIGHT, header.number(), derecha, y, 0);
        cb.endText();
        y -= 14;
        dibujarRotulo(cb, "FECHA", derecha, y);
        y -= 9;
        cb.beginText();
        cb.setFontAndSize(EstiloPdf.baseNegrita(), 10);
        cb.setColorFill(c.oscuro);
        cb.showTextAligned(Element.ALIGN_RIGHT, header.date(), derecha, y, 0);
        cb.endText();
        if (header.corrective()) {
            y -= 12;
            cb.beginText();
            cb.setFontAndSize(EstiloPdf.baseRegular(), 9);
            cb.setColorFill(EstiloPdf.GRIS);
            cb.showTextAligned(Element.ALIGN_RIGHT, "Rectifica a: " + header.correctsReference().orElse(""),
                    derecha, y, 0);
            cb.endText();
        }
        if (header.cancelled()) {
            y -= 15;
            cb.beginText();
            cb.setFontAndSize(EstiloPdf.baseNegrita(), 11);
            cb.setColorFill(EstiloPdf.ROJO_ANULADA);
            cb.showTextAligned(Element.ALIGN_RIGHT, "ANULADA", derecha, y, 0);
            cb.endText();
        }
    }

    private void dibujarRotulo(PdfContentByte cb, String texto, float derecha, float y) {
        cb.beginText();
        cb.setFontAndSize(EstiloPdf.baseNegrita(), 6.5f);
        cb.setColorFill(c.oscuro);
        cb.showTextAligned(Element.ALIGN_RIGHT, texto, derecha, y, 0);
        cb.endText();
    }

    private void dibujarSeparador(PdfContentByte cb, float izquierda, float derecha, float bordeSuperior, float altoTarjetas) {
        float ySep = EstiloPdf.yTarjetas(bordeSuperior, altoTarjetas) + 4;
        cb.setColorStroke(c.bordeTabla);
        cb.setLineWidth(0.9f);
        cb.moveTo(izquierda, ySep);
        cb.lineTo(derecha, ySep);
        cb.stroke();
    }

    private void dibujarPaginacion(PdfWriter writer, PdfContentByte cb, float derecha) {
        BaseFont bfPie = EstiloPdf.baseRegular();
        float wHueco = bfPie.getWidthPoint("00", 8);
        float xTotal = derecha - wHueco;
        cb.beginText();
        cb.setFontAndSize(bfPie, 8);
        cb.setColorFill(EstiloPdf.GRIS);
        cb.showTextAligned(Element.ALIGN_RIGHT, "Página " + writer.getPageNumber() + " de ",
                xTotal - 2, 18, 0);
        cb.endText();
        cb.addTemplate(totalPaginas, xTotal, 18);
    }

    private void dibujarMarcaAnulada(PdfWriter writer, PdfContentByte cb, Rectangle pagina) {
        cb.saveState();
        PdfGState gs = new PdfGState();
        gs.setFillOpacity(0.18f);
        cb.setGState(gs);
        cb.beginText();
        cb.setFontAndSize(FontFactory.getFont(FontFactory.HELVETICA_BOLD, 64).getBaseFont(), 64);
        cb.setColorFill(EstiloPdf.ROJO_ANULADA);
        cb.showTextAligned(Element.ALIGN_CENTER, "ANULADA",
                (pagina.getLeft() + pagina.getRight()) / 2,
                (pagina.getTop() + pagina.getBottom()) / 2, 45);
        cb.endText();
        cb.restoreState();
    }

    @Override
    public void onCloseDocument(PdfWriter writer, Document document) {
        ColumnText.showTextAligned(totalPaginas, Element.ALIGN_LEFT,
                new Phrase(String.valueOf(paginasReales), new Font(EstiloPdf.baseRegular(), 8)), 0, 0, 0);
    }

    private static String nz(String s) {
        return s == null ? "" : s;
    }

    private static List<CabeceraLayout.LineaCabecera> lineasEmpresa(Empresa empresa) {
        return CabeceraLayout.lineasEmpresa(empresa);
    }
}
