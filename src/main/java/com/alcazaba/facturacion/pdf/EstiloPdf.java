package com.alcazaba.facturacion.pdf;

import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPCellEvent;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPTableEvent;

import java.awt.Color;
import java.nio.file.Path;

class EstiloPdf {

    static final Color TINTA = new Color(0x00, 0x00, 0x00);
    static final Color GRIS = new Color(0x55, 0x55, 0x55);
    static final Color GRIS_CLARO = new Color(0x77, 0x77, 0x77);
    static final Color BLANCO = Color.WHITE;
    static final Color NEGRO = Color.BLACK;
    static final Color ROJO_ANULADA = new Color(0xB0, 0x00, 0x20);
    static final Color ROJO_DESCUENTO = new Color(0x8A, 0x2B, 0x2B);
    static final Color VALOR_SUAVE = new Color(0x55, 0x55, 0x55);
    static final float MARGEN_LATERAL = CabeceraLayout.MARGEN_LATERAL;

    /**
     * Reserva a la derecha para el bloque FACTURA/Serie-Nº/fecha en cabecera.
     */
    static final float RESERVA_FACTURA = 170f;
    static final float RADIO_TARJETA = 7f;
    static final float RADIO_CAJA = 6f;
    static final float RADIO_CHIP = 2f;

    static final float PIE_LEGAL_TAM = 6.5f;
    static final float PIE_LEGAL_INTERLINEADO = PIE_LEGAL_TAM + 1.5f;
    static final float HUECO_TARJETAS = 8f;
    static final float ESP_SUPLIDOS = 4f;
    static final float ESP_OBS = 6f;
    static final float ESP_PIE = 6f;
    static final float MARGEN_INFERIOR = 36f;
    static final float MARGEN_ETIQUETA = 6f;

    static final BaseFont CALIBRI = cargarFuenteSistema("calibri.ttf");
    static final BaseFont CALIBRI_NEGrita = cargarFuenteSistema("calibrib.ttf");
    static final BaseFont CALIBRI_CURSIVA = cargarFuenteSistema("calibrii.ttf");
    static final BaseFont CALIBRI_NEGrita_CURSIVA = cargarFuenteSistema("calibriz.ttf");

    private static BaseFont cargarFuenteSistema(String archivo) {
        try {
            String windir = System.getenv("WINDIR");
            Path ruta = Path.of(windir == null || windir.isBlank() ? "C:\\Windows" : windir,
                    "Fonts", archivo);
            return BaseFont.createFont(ruta.toString(), BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        } catch (Exception e) {
            return null;
        }
    }

    static BaseFont baseRegular() {
        return CALIBRI != null ? CALIBRI : FontFactory.getFont(FontFactory.HELVETICA).getBaseFont();
    }

    static BaseFont baseNegrita() {
        if (CALIBRI != null) {
            return CALIBRI_NEGrita != null ? CALIBRI_NEGrita : CALIBRI;
        }
        return FontFactory.getFont(FontFactory.HELVETICA_BOLD).getBaseFont();
    }

    static BaseFont baseCursiva() {
        if (CALIBRI != null) {
            return CALIBRI_CURSIVA != null ? CALIBRI_CURSIVA : CALIBRI;
        }
        return FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE).getBaseFont();
    }

    static Font fuente(boolean negrita, float tamano, Color color) {
        return new Font(negrita ? baseNegrita() : baseRegular(), tamano, Font.NORMAL, color);
    }

    static float yTarjetas(float bordeSuperiorContenido, float altoTarjetas) {
        return bordeSuperiorContenido + HUECO_TARJETAS + altoTarjetas;
    }

    /**
     * Tonos derivados del color de acento configurable.
     */
    static final class Colores {

        final Color base;
        final Color oscuro;
        final Color claro;
        final Color clarisimo;
        final Color bordeTabla;

        Colores(Color base) {
            this.base = base;
            this.oscuro = mezclar(base, NEGRO, 0.35f);
            this.claro = mezclar(base, BLANCO, 0.85f);
            this.clarisimo = mezclar(base, BLANCO, 0.95f);
            this.bordeTabla = mezclar(base, BLANCO, 0.45f);
        }

        private static Color mezclar(Color a, Color b, float pesoB) {
            int r = Math.round(a.getRed() + (b.getRed() - a.getRed()) * pesoB);
            int g = Math.round(a.getGreen() + (b.getGreen() - a.getGreen()) * pesoB);
            int bl = Math.round(a.getBlue() + (b.getBlue() - a.getBlue()) * pesoB);
            return new Color(r, g, bl);
        }
    }

    static final class RotuloTarjeta implements PdfPCellEvent {

        private final String titulo;
        private final Colores c;
        private final boolean clara;

        RotuloTarjeta(String titulo, Colores c, boolean clara) {
            this.titulo = titulo;
            this.c = c;
            this.clara = clara;
        }

        @Override
        public void cellLayout(PdfPCell celula, Rectangle rect, PdfContentByte[] canvases) {
            PdfContentByte cb = canvases[PdfPTable.TEXTCANVAS];
            float x = rect.getLeft();
            float y = rect.getBottom();
            float w = rect.getWidth();
            float h = rect.getHeight();
            cb.saveState();
            cb.setColorFill(clara ? BLANCO : c.base);
            cb.roundRectangle(x - 0.4f, y, w + 0.8f, h, RADIO_TARJETA);
            cb.fill();
            cb.rectangle(x - 0.4f, y - 0.4f, w + 0.8f, RADIO_TARJETA + 0.4f);
            cb.fill();
            if (clara) {
                cb.setColorStroke(c.bordeTabla);
                cb.setLineWidth(0.7f);
                cb.moveTo(x, y);
                cb.lineTo(x + w, y);
                cb.stroke();
            }
            cb.restoreState();

            BaseFont bf = baseNegrita();
            cb.beginText();
            cb.setFontAndSize(bf, 8.5f);
            cb.setColorFill(clara ? c.oscuro : BLANCO);
            cb.showTextAligned(Element.ALIGN_LEFT, titulo, x + 8, y + (h - 8.5f) / 2 - 1f, 0);
            cb.endText();
        }
    }

    /**
     * Traza un contorno con esquinas redondeadas sobre la celda ya renderizada.
     */
    static final class ContornoRedondeado implements PdfPCellEvent {

        private final float radio;
        private final Color borde;

        ContornoRedondeado(float radio, Color borde) {
            this.radio = radio;
            this.borde = borde;
        }

        @Override
        public void cellLayout(PdfPCell celula, Rectangle rect, PdfContentByte[] canvases) {
            PdfContentByte cb = canvases[PdfPTable.TEXTCANVAS];
            cb.saveState();
            cb.setColorStroke(borde);
            cb.setLineWidth(0.9f);
            cb.roundRectangle(rect.getLeft() - 0.4f, rect.getBottom() - 0.4f,
                    rect.getWidth() + 0.8f, rect.getHeight() + 0.8f, radio);
            cb.stroke();
            cb.restoreState();
        }
    }

    static final class ContornoTabla implements PdfPTableEvent {
        private final float radio;
        private final Color borde;
        ContornoTabla(float radio, Color borde) {
            this.radio = radio;
            this.borde = borde;
        }
        @Override
        public void tableLayout(PdfPTable table, float[][] widths, float[] heights, int headerRows, int rowStart, PdfContentByte[] canvases) {
            float x1 = widths[0][0];
            float x2 = widths[0][widths[0].length - 1];
            float y1 = heights[heights.length - 1];
            float y2 = heights[0];
            float w = x2 - x1;
            float h = y2 - y1;
            PdfContentByte cb = canvases[PdfPTable.LINECANVAS];
            cb.saveState();
            cb.setColorStroke(borde);
            cb.setLineWidth(0.9f);
            cb.roundRectangle(x1 - 0.4f, y1 - 0.4f, w + 0.8f, h + 0.8f, radio);
            cb.stroke();
            cb.restoreState();
        }
    }

    static final class FondoPieLegal implements PdfPCellEvent {
        private final float radio;
        private final Colores c;
        FondoPieLegal(float radio, Colores c) {
            this.radio = radio;
            this.c = c;
        }
        @Override
        public void cellLayout(PdfPCell cell, Rectangle rect, PdfContentByte[] canvases) {
            PdfContentByte cb = canvases[PdfPTable.BACKGROUNDCANVAS];
            cb.saveState();
            cb.setColorFill(c.clarisimo);
            cb.roundRectangle(rect.getLeft() - 0.4f, rect.getBottom() - 0.4f, rect.getWidth() + 0.8f, rect.getHeight() + 0.8f, radio);
            cb.fill();
            cb.setColorStroke(c.bordeTabla);
            cb.setLineWidth(0.7f);
            cb.roundRectangle(rect.getLeft() - 0.4f, rect.getBottom() - 0.4f, rect.getWidth() + 0.8f, rect.getHeight() + 0.8f, radio);
            cb.stroke();
            cb.setColorFill(c.base);
            cb.roundRectangle(rect.getLeft() - 0.4f, rect.getBottom() - 0.4f, 3f, rect.getHeight() + 0.8f, radio);
            cb.fill();
            cb.restoreState();
        }
    }
}
