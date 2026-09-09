package com.alcazaba.facturacion.pdf;

import com.alcazaba.facturacion.model.Empresa;
import com.alcazaba.facturacion.service.FacturaService;
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
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfGState;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPCellEvent;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPTableEvent;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfCopy;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfWriter;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Exportacion a PDF con el diseno aprobado inspirado en la hoja de calculo:
 * A4 vertical, cabecera repetida con logo al doble del tamano configurado
 * junto a los datos de empresa con NIF destacado, bloque FACTURA/Serie-Nº/fecha a la
 * derecha, tarjetas bicolor (Facturar a / Datos de pago), tabla de lineas con
 * celdas bordeadas y total por linea con IVA incluido, bloque de totales en dos
 * rejillas hermanas con banda TOTAL
 * en color, observaciones en caja y pie legal en recuadro repetido en todas
 * las paginas con Página X de Y. El color de acento es configurable.
 */
public class PdfService {

    public static final String PREF_COLOR = "color_pdf";
    public static final String COLOR_DEFECTO = "#B08D57";

    private static final Color TINTA = new Color(0x00, 0x00, 0x00);
    private static final Color GRIS = new Color(0x55, 0x55, 0x55);
    private static final Color GRIS_CLARO = new Color(0x77, 0x77, 0x77);
    private static final Color BLANCO = Color.WHITE;
    private static final Color NEGRO = Color.BLACK;
    private static final Color ROJO_ANULADA = new Color(0xB0, 0x00, 0x20);
    private static final Color ROJO_DESCUENTO = new Color(0x8A, 0x2B, 0x2B);
    private static final Color VALOR_SUAVE = new Color(0x55, 0x55, 0x55);
    private static final float MARGEN_LATERAL = CabeceraLayout.MARGEN_LATERAL;

    /**
     * Reserva a la derecha para el bloque FACTURA/Serie-Nº/fecha en cabecera.
     */
    private static final float RESERVA_FACTURA = 170f;
    private static final float RADIO_TARJETA = 7f;
    private static final float RADIO_CAJA = 6f;
    private static final float RADIO_CHIP = 2f;

    private static final float PIE_LEGAL_TAM = 6.5f;
    private static final float PIE_LEGAL_INTERLINEADO = PIE_LEGAL_TAM + 1.5f;
    static final float HUECO_TARJETAS = 8f;
    static final float ESP_SUPLIDOS = 4f;
    static final float ESP_OBS = 6f;
    static final float ESP_PIE = 6f;
    private static final float MARGEN_INFERIOR = 36f;
    private static final float MARGEN_ETIQUETA = 6f;

    public void exportar(FacturaService.VersionCompleta vc, Empresa empresa, Path ruta) throws Exception {
        exportar(vc, empresa, ruta, null);
    }

    public void exportar(FacturaService.VersionCompleta vc, Empresa empresa, Path ruta, String colorHex) throws Exception {
        try (FileOutputStream fos = new FileOutputStream(ruta.toFile())) {
            exportar(vc, empresa, fos, colorHex);
        }
    }

    public void exportarAgrupado(List<FacturaService.VersionCompleta> versiones, Empresa empresa, Path ruta) throws Exception {
        exportarAgrupado(versiones, empresa, ruta, null);
    }

    public void exportarAgrupado(List<FacturaService.VersionCompleta> versiones, Empresa empresa, Path ruta, String colorHex) throws Exception {
        List<byte[]> pdfs = new ArrayList<>();
        for (FacturaService.VersionCompleta vc : versiones) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            exportar(vc, empresa, baos, colorHex);
            pdfs.add(baos.toByteArray());
        }
        concatenar(pdfs, ruta);
    }

    private void exportar(FacturaService.VersionCompleta vc, Empresa empresa, OutputStream out, String colorHex) throws Exception {
        Colores colores = new Colores(colorDe(colorHex));
        InvoiceDocument invoice = InvoiceDocumentBuilder.build(vc, empresa, colorHex);

        Image logo = cargarLogo(empresa);

        Optional<InvoiceDocument.SuplidosBlock> suplidos = invoice.suplidos();
        boolean haySuplidos = suplidos.isPresent();
        Optional<String> obs = invoice.observations();
        boolean hayObs = obs.isPresent();

        PdfPTable tarjetasTabla = tarjetas(vc, colores);
        boolean conPago = invoice.paymentCard().isPresent();
        PdfPTable tarjetasSinPagoTabla = null;
        if (conPago) {
            tarjetasSinPagoTabla = tarjetasSinPago(vc, colores);
        }
        float ancho = anchoContenido();
        medir(tarjetasTabla, ancho);
        float altoTarjetas = tarjetasTabla.getTotalHeight();
        if (tarjetasSinPagoTabla != null) {
            tarjetasSinPagoTabla.setTotalWidth(ancho);
            tarjetasSinPagoTabla.setLockedWidth(true);
            for (PdfPCell c : tarjetasSinPagoTabla.getRow(0).getCells()) {
                if (c != null) c.setFixedHeight(altoTarjetas);
            }
            tarjetasSinPagoTabla.calculateHeights(true);
        }

        PdfPTable tablaLineasTabla = tablaLineas(invoice.linesTable(), colores);

        PdfPTable bloqueSup = haySuplidos ? bloqueSuplidos(suplidos.orElseThrow(), colores) : null;
        PdfPTable bloqueTot = bloqueTotales(invoice.totals(), colores);
        PdfPTable cajaObs = hayObs ? cajaObservaciones(obs.orElseThrow(), colores) : null;

        medir(bloqueTot, ancho);
        float altoSuplidos = 0;
        if (bloqueSup != null) {
            medir(bloqueSup, ancho);
            altoSuplidos = bloqueSup.getTotalHeight();
        }
        float altoObs = 0;
        if (cajaObs != null) {
            medir(cajaObs, ancho);
            altoObs = cajaObs.getTotalHeight();
        }
        float altoTotales = bloqueTot.getTotalHeight();
        Optional<String> pieLegal = invoice.legalFooter();
        boolean hayPie = pieLegal.isPresent();
        PdfPTable cajaPie = hayPie ? cajaPieLegal(pieLegal.orElseThrow(), colores) : null;
        float altoPie = 0;
        if (cajaPie != null) {
            medir(cajaPie, ancho);
            altoPie = cajaPie.getTotalHeight();
        }
        float hCierre = (haySuplidos ? ESP_SUPLIDOS + altoSuplidos + ESP_SUPLIDOS : 0)
                + altoTotales
                + (hayObs ? ESP_OBS + altoObs : 0)
                + (hayPie ? ESP_PIE + altoPie : 0);

        float[] margenes = margenes(empresa, logo, colores, altoTarjetas);

        try (Document doc = new Document(PageSize.A4, MARGEN_LATERAL, MARGEN_LATERAL, margenes[0], margenes[1])) {
            PdfWriter writer = PdfWriter.getInstance(doc, out);
            writer.setPageEvent(new CabeceraPie(empresa, logo, invoice.header(), colores,
                    tarjetasTabla, conPago ? tarjetasSinPagoTabla : tarjetasTabla, altoTarjetas, conPago));
            doc.open();

            doc.add(tablaLineasTabla);
            float y = writer.getVerticalPosition(false);
            float hueco = y - doc.bottom() - hCierre;

            if (hueco > 0) {
                doc.add(tablaRelleno(hueco, colores));
            } else {
                float restante = y - doc.bottom();
                if (restante > 0.01f) {
                    doc.add(tablaRelleno(restante, colores));
                }
                doc.newPage();
                PdfPTable tablaVacia = tablaLineasVacia(colores);
                doc.add(tablaVacia);
                y = writer.getVerticalPosition(false);
                float hueco2 = y - doc.bottom() - hCierre;
                if (hueco2 > 0.01f) {
                    doc.add(tablaRelleno(hueco2, colores));
                }
            }

            if (haySuplidos) {
                doc.add(espaciador(ESP_SUPLIDOS));
                doc.add(bloqueSup);
                doc.add(espaciador(ESP_SUPLIDOS));
            }
            doc.add(bloqueTot);
            if (hayObs) {
                doc.add(espaciador(ESP_OBS));
                doc.add(cajaObs);
            }
            if (hayPie) {
                doc.add(espaciador(ESP_PIE));
                doc.add(cajaPie);
            }
        }
    }

    private void concatenar(List<byte[]> pdfs, Path ruta) throws Exception {
        Document document = new Document();
        PdfCopy copy = new PdfCopy(document, new FileOutputStream(ruta.toFile()));
        document.open();
        try {
            for (byte[] pdf : pdfs) {
                PdfReader reader = new PdfReader(pdf);
                for (int i = 1; i <= reader.getNumberOfPages(); i++) {
                    copy.addPage(copy.getImportedPage(reader, i));
                }
                reader.close();
            }
        } finally {
            document.close();
        }
    }

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

    private static BaseFont baseRegular() {
        return CALIBRI != null ? CALIBRI : FontFactory.getFont(FontFactory.HELVETICA).getBaseFont();
    }

    private static BaseFont baseNegrita() {
        if (CALIBRI != null) {
            return CALIBRI_NEGrita != null ? CALIBRI_NEGrita : CALIBRI;
        }
        return FontFactory.getFont(FontFactory.HELVETICA_BOLD).getBaseFont();
    }

    private static BaseFont baseCursiva() {
        if (CALIBRI != null) {
            return CALIBRI_CURSIVA != null ? CALIBRI_CURSIVA : CALIBRI;
        }
        return FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE).getBaseFont();
    }


    // ------------------------------------------------------------------
    // Tarjetas bicolor
    // ------------------------------------------------------------------

    PdfPTable tarjetas(FacturaService.VersionCompleta vc, Colores c) {
        List<String[]> pagoFilas = filasDatosPago(vc);
        PdfPTable exterior = new PdfPTable(new float[]{49f, 2f, 49f});
        exterior.setWidthPercentage(100);
        PdfPCell cellCliente = celdaTarjeta(tarjetaCliente(vc, c), c);
        cellCliente.setVerticalAlignment(Element.ALIGN_TOP);
        exterior.addCell(cellCliente);
        PdfPCell hueco = new PdfPCell(new Phrase(" "));
        hueco.setBorder(Rectangle.NO_BORDER);
        if (pagoFilas.isEmpty()) {
            hueco.setColspan(2);
            exterior.addCell(hueco);
        } else {
            exterior.addCell(hueco);
            PdfPCell cellPago = celdaTarjeta(tarjetaPago(pagoFilas, c), c);
            cellPago.setVerticalAlignment(Element.ALIGN_TOP);
            exterior.addCell(cellPago);
        }
        return exterior;
    }

    private PdfPCell celdaTarjeta(PdfPTable interior, Colores c) {
        PdfPCell celula = new PdfPCell();
        celula.setBorder(Rectangle.NO_BORDER);
        celula.setPadding(0);
        celula.setVerticalAlignment(Element.ALIGN_TOP);
        celula.addElement(interior);
        return celula;
    }

    PdfPTable tarjetaCliente(FacturaService.VersionCompleta vc, Colores c) {
        return tarjetaCliente(InvoiceDocumentBuilder.clientCard(vc.version()), c);
    }

    private PdfPTable tarjetaCliente(InvoiceDocument.ClientCard card, Colores c) {
        PdfPTable t = new PdfPTable(1);
        t.setWidthPercentage(100);
        t.setTableEvent(new ContornoTabla(RADIO_TARJETA, c.bordeTabla));
        t.addCell(cabeceraTarjeta(card.title(), c, false));
        PdfPCell cuerpo = new PdfPCell();
        cuerpo.setBackgroundColor(BLANCO);
        cuerpo.setBorder(Rectangle.NO_BORDER);
        cuerpo.setPadding(8);
        cuerpo.setPaddingTop(6);

        if (card.rows().isEmpty()) {
            cuerpo.setPhrase(new Phrase(card.emptyMarker(), fuente(false, 9.5f, GRIS_CLARO)));
            t.addCell(cuerpo);
            return t;
        }

        float anchoEtiqueta = 0;
        BaseFont bfEtiq = baseRegular();
        for (InvoiceDocument.FieldRow fila : card.rows()) {
            float w = bfEtiq.getWidthPoint(fila.label(), 8.5f);
            if (w > anchoEtiqueta) anchoEtiqueta = w;
        }
        anchoEtiqueta += MARGEN_ETIQUETA;
        float anchoInterior = anchoContenido() * 0.49f - 16f;
        float anchoValor = anchoInterior - anchoEtiqueta;
        if (anchoValor < 40) anchoValor = 40;
        PdfPTable filasTabla = new PdfPTable(new float[]{anchoEtiqueta, anchoValor});
        filasTabla.setWidthPercentage(100);
        for (InvoiceDocument.FieldRow fila : card.rows()) {
            PdfPCell etiqueta = new PdfPCell(new Phrase(fila.label(), fuente(false, 8.5f, GRIS_CLARO)));
            etiqueta.setBorder(Rectangle.NO_BORDER);
            etiqueta.setPadding(1.5f);
            filasTabla.addCell(etiqueta);
            Font fuenteValor = "Nombre".equals(fila.label())
                    ? fuente(true, 10.5f, TINTA)
                    : fuente(false, 9.5f, TINTA);
            PdfPCell valor = new PdfPCell(new Phrase(fila.value(), fuenteValor));
            valor.setBorder(Rectangle.NO_BORDER);
            valor.setPadding(1.5f);
            filasTabla.addCell(valor);
        }
        cuerpo.addElement(filasTabla);
        t.addCell(cuerpo);
        return t;
    }

    List<String[]> filasDatosPago(FacturaService.VersionCompleta vc) {
        List<String[]> filas = new ArrayList<>();
        for (InvoiceDocument.FieldRow fila : InvoiceDocumentBuilder.paymentRows(vc.version())) {
            filas.add(new String[]{fila.label(), fila.value()});
        }
        return filas;
    }

    PdfPTable tarjetaPago(List<String[]> filas, Colores c) {
        List<InvoiceDocument.FieldRow> rows = new ArrayList<>();
        for (String[] fila : filas) {
            rows.add(new InvoiceDocument.FieldRow(fila[0], fila[1]));
        }
        return tarjetaPago(new InvoiceDocument.PaymentCard(InvoiceDocumentBuilder.paymentCardTitle(), rows), c);
    }

    private PdfPTable tarjetaPago(InvoiceDocument.PaymentCard card, Colores c) {
        PdfPTable t = new PdfPTable(1);
        t.setWidthPercentage(100);
        t.setTableEvent(new ContornoTabla(RADIO_TARJETA, c.bordeTabla));
        t.addCell(cabeceraTarjeta(card.title(), c, true));
        PdfPCell cuerpo = new PdfPCell();
        cuerpo.setBackgroundColor(BLANCO);
        cuerpo.setBorder(Rectangle.NO_BORDER);
        cuerpo.setPadding(8);
        cuerpo.setPaddingTop(6);

        float anchoEtiqueta = 0;
        BaseFont bfEtiq = baseRegular();
        for (InvoiceDocument.FieldRow fila : card.rows()) {
            float w = bfEtiq.getWidthPoint(fila.label(), 8.5f);
            if (w > anchoEtiqueta) anchoEtiqueta = w;
        }
        anchoEtiqueta += MARGEN_ETIQUETA;
        float anchoInterior = anchoContenido() * 0.49f - 16f;
        float anchoValor = anchoInterior - anchoEtiqueta;
        if (anchoValor < 40) anchoValor = 40;
        PdfPTable filasTabla = new PdfPTable(new float[]{anchoEtiqueta, anchoValor});
        filasTabla.setWidthPercentage(100);
        for (InvoiceDocument.FieldRow fila : card.rows()) {
            PdfPCell etiqueta = new PdfPCell(new Phrase(fila.label(), fuente(false, 8.5f, GRIS_CLARO)));
            etiqueta.setBorder(Rectangle.NO_BORDER);
            etiqueta.setPadding(1.5f);
            filasTabla.addCell(etiqueta);
            PdfPCell valor = new PdfPCell(new Phrase(fila.value(), fuente(false, 9.5f, VALOR_SUAVE)));
            valor.setBorder(Rectangle.NO_BORDER);
            valor.setPadding(1.5f);
            filasTabla.addCell(valor);
        }
        cuerpo.addElement(filasTabla);
        t.addCell(cuerpo);
        return t;
    }

    /**
     * Cabecera de tarjeta pintada por evento: fondo con las esquinas superiores
     * redondeadas y titulo dibujado encima. La variante clara va en blanco con
     * borde fino inferior y texto marron oscuro.
     */
    private PdfPCell cabeceraTarjeta(String titulo, Colores c, boolean clara) {
        PdfPCell celula = new PdfPCell(new Phrase(" ", fuente(false, 1f, BLANCO)));
        celula.setBorder(Rectangle.NO_BORDER);
        celula.setMinimumHeight(17f);
        celula.setPaddingLeft(8);
        celula.setPaddingTop(5);
        celula.setCellEvent(new RotuloTarjeta(titulo, c, clara));
        return celula;
    }

    private final class RotuloTarjeta implements PdfPCellEvent {

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

    // ------------------------------------------------------------------
    // Tabla de lineas estilo hoja de calculo
    // ------------------------------------------------------------------

    private PdfPTable tablaLineasVacia(Colores c) {
        PdfPTable t = new PdfPTable(new float[]{0.7f, 4.3f, 1.4f, 1.0f, 1.9f});
        t.setWidthPercentage(100);
        t.addCell(celdaCabeceraColumna("CANT.", c));
        t.addCell(celdaCabeceraColumna("DESCRIPCIÓN", c));
        t.addCell(celdaCabeceraColumna("PRECIO", c));
        t.addCell(celdaCabeceraColumna("IVA %", c));
        t.addCell(celdaCabeceraColumna("TOTAL", c));
        return t;
    }

    private PdfPTable tablaLineas(InvoiceDocument.LinesTable lines, Colores c) {
        PdfPTable t = tablaLineasVacia(c);
        int fila = 0;
        for (InvoiceDocument.LineRow l : lines.rows()) {
            t.addCell(celdaLinea(l.quantity(), fila, Element.ALIGN_CENTER, c));
            t.addCell(celdaLinea(l.description(), fila, Element.ALIGN_LEFT, c));
            t.addCell(celdaLinea(l.price(), fila, Element.ALIGN_RIGHT, c));
            t.addCell(celdaLinea(l.iva(), fila, Element.ALIGN_CENTER, c));
            t.addCell(celdaLinea(l.total(), fila, Element.ALIGN_RIGHT, c));
            fila++;
        }
        if (fila > 0) t.setHeaderRows(1);
        return t;
    }

    private PdfPTable bloqueSuplidos(InvoiceDocument.SuplidosBlock suplidos, Colores c) {
        PdfPTable t = new PdfPTable(new float[]{6.4f, 1.9f});
        t.setWidthPercentage(100);

        t.addCell(celdaCabeceraColumnaCompacta(suplidos.headers().get(0), c));
        t.addCell(celdaCabeceraColumnaCompacta(suplidos.headers().get(1), c));

        int fila = 0;
        for (InvoiceDocument.SuplidoRow l : suplidos.rows()) {
            t.addCell(celdaLineaCompacta(l.description(), fila, Element.ALIGN_LEFT, c));
            t.addCell(celdaLineaCompacta(l.amount(), fila, Element.ALIGN_RIGHT, c));
            fila++;
        }

        PdfPCell nota = new PdfPCell(new Phrase(suplidos.note(), fuente(false, 7f, c.oscuro)));
        nota.setColspan(2);
        nota.setBorder(Rectangle.NO_BORDER);
        nota.setPaddingTop(3f);
        t.addCell(nota);
        return t;
    }

    private PdfPCell celdaCabeceraColumnaCompacta(String texto, Colores c) {
        PdfPCell celula = new PdfPCell(new Phrase(texto, fuente(true, 7f, c.oscuro)));
        celula.setBackgroundColor(c.claro);
        celula.setHorizontalAlignment(Element.ALIGN_CENTER);
        celula.setPadding(3);
        celula.setBorderColor(c.bordeTabla);
        return celula;
    }

    private PdfPCell celdaLineaCompacta(String texto, int fila, int alineacion, Colores c) {
        PdfPCell celula = new PdfPCell(new Phrase(texto, fuente(false, 8f, TINTA)));
        celula.setHorizontalAlignment(alineacion);
        celula.setPadding(2.5f);
        celula.setBorderColor(c.bordeTabla);
        if (fila % 2 == 1) {
            celula.setBackgroundColor(c.clarisimo);
        }
        return celula;
    }

    private PdfPCell celdaCabeceraColumna(String texto, Colores c) {
        PdfPCell celula = new PdfPCell(new Phrase(texto, fuente(true, 8f, c.oscuro)));
        celula.setBackgroundColor(c.claro);
        celula.setHorizontalAlignment(Element.ALIGN_CENTER);
        celula.setPadding(4);
        celula.setBorderColor(c.bordeTabla);
        return celula;
    }

    private PdfPCell celdaLinea(String texto, int fila, int alineacion, Colores c) {
        PdfPCell celula = new PdfPCell(new Phrase(texto, fuente(false, 9f, TINTA)));
        celula.setHorizontalAlignment(alineacion);
        celula.setPadding(4);
        celula.setBorderColor(c.bordeTabla);
        if (fila % 2 == 1) {
            celula.setBackgroundColor(c.clarisimo);
        }
        return celula;
    }

    // ------------------------------------------------------------------
    // Totales
    // ------------------------------------------------------------------

    private PdfPTable bloqueTotales(InvoiceDocument.TotalsBlock totals, Colores c) {
        PdfPTable contenedor = new PdfPTable(new float[]{3.3f, 3.0f});
        contenedor.setWidthPercentage(100);

        PdfPCell celdaIzquierda = new PdfPCell();
        celdaIzquierda.setBorder(Rectangle.NO_BORDER);
        celdaIzquierda.setVerticalAlignment(Element.ALIGN_TOP);
        celdaIzquierda.setPaddingRight(7f);
        celdaIzquierda.addElement(rejillaDesgloseIva(totals, c));
        if (totals.discountNote().isPresent()) {
            celdaIzquierda.addElement(notaDescuento(totals.discountNote().orElseThrow(), c));
        }

        PdfPCell celdaDerecha = new PdfPCell();
        celdaDerecha.setBorder(Rectangle.NO_BORDER);
        celdaDerecha.setVerticalAlignment(Element.ALIGN_TOP);
        celdaDerecha.setPaddingLeft(7f);
        celdaDerecha.addElement(rejillaLiquidacion(totals.liquidation(), c));

        contenedor.addCell(celdaIzquierda);
        contenedor.addCell(celdaDerecha);
        return contenedor;
    }

    private PdfPTable rejillaDesgloseIva(InvoiceDocument.TotalsBlock totals, Colores c) {
        PdfPTable t = new PdfPTable(new float[]{1.0f, 2.0f, 1.7f});
        t.setWidthPercentage(100);
        t.addCell(celdaCabeceraRejilla(totals.desgloseHeaders().get(0), c));
        t.addCell(celdaCabeceraRejilla(totals.desgloseHeaders().get(1), c));
        t.addCell(celdaCabeceraRejilla(totals.desgloseHeaders().get(2), c));

        for (InvoiceDocument.IvaRow g : totals.ivaRows()) {
            t.addCell(celdaCuerpoRejilla(g.type(), Element.ALIGN_CENTER, c, false, false));
            t.addCell(celdaCuerpoRejilla(g.base(), Element.ALIGN_RIGHT, c, false, false));
            t.addCell(celdaCuerpoRejilla(g.quota(), Element.ALIGN_RIGHT, c, false, false));
        }

        t.addCell(celdaCuerpoRejilla(totals.totalsRow().type(), Element.ALIGN_CENTER, c, true, false));
        t.addCell(celdaCuerpoRejilla(totals.totalsRow().base(), Element.ALIGN_RIGHT, c, true, false));
        t.addCell(celdaCuerpoRejilla(totals.totalsRow().quota(), Element.ALIGN_RIGHT, c, true, false));
        return t;
    }

    private Paragraph notaDescuento(String nota, Colores c) {
        Paragraph p = new Paragraph();
        p.add(new Chunk(nota, fuente(false, 7f, c.oscuro)));
        p.setLeading(8.5f);
        p.setSpacingBefore(3f);
        p.setAlignment(Element.ALIGN_LEFT);
        return p;
    }

    private PdfPTable rejillaLiquidacion(InvoiceDocument.Liquidation liquidation, Colores c) {
        PdfPTable t = new PdfPTable(new float[]{2.2f, 1.4f});
        t.setWidthPercentage(100);

        PdfPCell cabecera = celdaCabeceraRejilla(liquidation.title(), c);
        cabecera.setColspan(2);
        t.addCell(cabecera);

        filaLiquidacion(t, liquidation.baseLabel(), liquidation.baseAmount(), c, false);
        filaLiquidacion(t, liquidation.ivaLabel(), liquidation.ivaAmount(), c, false);

        if (liquidation.retention().isPresent()) {
            InvoiceDocument.RetentionRow retention = liquidation.retention().orElseThrow();
            filaLiquidacion(t, retention.label(), retention.amount(), c, true);
        }
        if (liquidation.suplidos().isPresent()) {
            InvoiceDocument.SuplidosTotalRow suplidos = liquidation.suplidos().orElseThrow();
            filaLiquidacion(t, suplidos.label(), suplidos.amount(), c, false);
        }

        PdfPCell etiquetaTotal = new PdfPCell(new Phrase(liquidation.total().label(), fuente(true, 11f, BLANCO)));
        etiquetaTotal.setBackgroundColor(c.base);
        etiquetaTotal.setPadding(5f);
        etiquetaTotal.setBorderColor(c.oscuro);
        etiquetaTotal.setBorder(Rectangle.LEFT | Rectangle.TOP | Rectangle.BOTTOM);
        etiquetaTotal.setVerticalAlignment(Element.ALIGN_MIDDLE);
        PdfPCell importeTotal = new PdfPCell(new Phrase(liquidation.total().amount(), fuente(true, 11f, BLANCO)));
        importeTotal.setBackgroundColor(c.base);
        importeTotal.setHorizontalAlignment(Element.ALIGN_RIGHT);
        importeTotal.setPadding(5f);
        importeTotal.setBorderColor(c.oscuro);
        importeTotal.setBorder(Rectangle.RIGHT | Rectangle.TOP | Rectangle.BOTTOM);
        importeTotal.setVerticalAlignment(Element.ALIGN_MIDDLE);
        t.addCell(etiquetaTotal);
        t.addCell(importeTotal);
        return t;
    }

    private void filaLiquidacion(PdfPTable t, String etiqueta, String importe, Colores c, boolean retencion) {
        t.addCell(celdaCuerpoRejilla(etiqueta, Element.ALIGN_LEFT, c, false, retencion));
        t.addCell(celdaCuerpoRejilla(importe, Element.ALIGN_RIGHT, c, false, retencion));
    }

    private PdfPCell celdaCabeceraRejilla(String texto, Colores c) {
        Chunk chunk = new Chunk(texto, fuente(true, 7f, BLANCO));
        chunk.setCharacterSpacing(0.5f);
        PdfPCell celula = new PdfPCell(new Phrase(chunk));
        celula.setBackgroundColor(c.base);
        celula.setHorizontalAlignment(Element.ALIGN_CENTER);
        celula.setPadding(4f);
        celula.setBorderColor(c.bordeTabla);
        celula.setBorder(Rectangle.TOP | Rectangle.BOTTOM | Rectangle.LEFT | Rectangle.RIGHT);
        return celula;
    }

    private PdfPCell celdaCuerpoRejilla(String texto, int alineacion, Colores c, boolean totales, boolean retencion) {
        Font f = retencion
                ? new Font(baseCursiva(), 8.5f, Font.NORMAL, ROJO_DESCUENTO)
                : fuente(totales, 8.5f, TINTA);
        PdfPCell celula = new PdfPCell(new Phrase(texto, f));
        celula.setHorizontalAlignment(alineacion);
        celula.setPadding(3.5f);
        celula.setBackgroundColor(totales ? c.claro : c.clarisimo);
        celula.setBorderColor(c.bordeTabla);
        celula.setBorder(totales ? Rectangle.LEFT | Rectangle.RIGHT | Rectangle.TOP | Rectangle.BOTTOM : Rectangle.LEFT | Rectangle.RIGHT);
        if (totales) {
            celula.setBorderWidthTop(0.7f);
        }
        return celula;
    }

    private PdfPTable cajaObservaciones(String observaciones, Colores c) {
        PdfPTable t = new PdfPTable(1);
        t.setWidthPercentage(100);
        PdfPCell celula = new PdfPCell();
        celula.setBackgroundColor(c.clarisimo);
        celula.setBorder(Rectangle.NO_BORDER);
        celula.setPadding(8);
        celula.setCellEvent(new ContornoRedondeado(RADIO_CAJA, c.bordeTabla));
        Paragraph p = new Paragraph("Observaciones", fuente(true, 8.5f, c.oscuro));
        p.add(new Phrase("\n" + observaciones, fuente(false, 9f, TINTA)));
        celula.setPhrase(p);
        t.addCell(celula);
        return t;
    }

    PdfPTable cajaPieLegal(String pie, Colores c) {
        PdfPTable t = new PdfPTable(1);
        t.setWidthPercentage(100);
        t.setSplitLate(true);
        t.setKeepTogether(true);
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(c.clarisimo);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(8);
        cell.setPaddingLeft(10);
        cell.setCellEvent(new FondoPieLegal(RADIO_CAJA, c));
        Paragraph p = new Paragraph();
        p.setLeading(PIE_LEGAL_INTERLINEADO);
        p.add(new Chunk(pie, fuente(false, PIE_LEGAL_TAM, GRIS)));
        cell.setPhrase(p);
        t.addCell(cell);
        return t;
    }

    private static final class FondoPieLegal implements PdfPCellEvent {
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

    // ------------------------------------------------------------------
    // Margenes dinamicos segun cabecera y pie legal
    // ------------------------------------------------------------------

    private float[] margenes(Empresa empresa, Image logo, Colores c, float altoTarjetas) {
        float superior;
        int lineas = CabeceraLayout.lineasEmpresa(empresa).size();
        if (logo != null) {
            superior = CabeceraLayout.altoCabeceraLogo(empresa, lineas);
        } else {
            superior = CabeceraLayout.altoCabeceraTexto(lineas);
        }
        superior += altoTarjetas + HUECO_TARJETAS;
        return new float[]{superior, MARGEN_INFERIOR};
    }

    static float yTarjetas(float bordeSuperiorContenido, float altoTarjetas) {
        return bordeSuperiorContenido + HUECO_TARJETAS + altoTarjetas;
    }

    private static float anchoContenido() {
        return PageSize.A4.getWidth() - 2 * MARGEN_LATERAL;
    }

    private static void medir(PdfPTable t, float ancho) {
        t.setTotalWidth(ancho);
        t.setLockedWidth(true);
        t.calculateHeights(true);
    }

    PdfPTable tablaRelleno(float hueco, Colores c) {
        PdfPTable t = new PdfPTable(new float[]{0.7f, 4.3f, 1.4f, 1.0f, 1.9f});
        t.setWidthPercentage(100);
        for (int col = 0; col < 5; col++) {
            PdfPCell cell = new PdfPCell(new Phrase(" "));
            cell.setFixedHeight(hueco);
            cell.setBorder(Rectangle.LEFT | Rectangle.RIGHT | Rectangle.BOTTOM);
            cell.setBorderColor(c.bordeTabla);
            cell.setBorderWidth(0.7f);
            t.addCell(cell);
        }
        return t;
    }

    PdfPTable espaciador(float alto) {
        PdfPTable t = new PdfPTable(1);
        t.setWidthPercentage(100);
        PdfPCell celula = new PdfPCell(new Phrase(" ", fuente(false, 1f, BLANCO)));
        celula.setBorder(Rectangle.NO_BORDER);
        celula.setFixedHeight(alto);
        celula.setPadding(0);
        t.addCell(celula);
        return t;
    }

    PdfPTable tarjetasSinPago(FacturaService.VersionCompleta vc, Colores c) {
        PdfPTable exterior = new PdfPTable(new float[]{49f, 2f, 49f});
        exterior.setWidthPercentage(100);
        exterior.addCell(celdaTarjeta(tarjetaCliente(vc, c), c));
        PdfPCell hueco = new PdfPCell(new Phrase(" "));
        hueco.setBorder(Rectangle.NO_BORDER);
        hueco.setPadding(0);
        hueco.setColspan(2);
        exterior.addCell(hueco);
        return exterior;
    }

    private Image cargarLogo(Empresa empresa) {
        if (empresa == null || !"LOGO".equalsIgnoreCase(empresa.getCabeceraModo())
                || empresa.getLogoPath() == null || empresa.getLogoPath().isBlank()) {
            return null;
        }
        try {
            return Image.getInstance(empresa.getLogoPath());
        } catch (Exception e) {
            return null;
        }
    }

    private List<CabeceraLayout.LineaCabecera> lineasEmpresa(Empresa empresa) {
        return CabeceraLayout.lineasEmpresa(empresa);
    }

    private List<String> partir(String texto, BaseFont bf, float size, float ancho) {
        List<String> out = new ArrayList<>();
        if (texto == null || texto.isBlank()) {
            return out;
        }
        for (String parrafo : texto.split("\n", -1)) {
            if (parrafo.isBlank()) {
                continue;
            }
            StringBuilder linea = new StringBuilder();
            for (String palabra : parrafo.trim().split("\\s+")) {
                String prueba = linea.length() == 0 ? palabra : linea + " " + palabra;
                if (bf.getWidthPoint(prueba, size) <= ancho) {
                    linea.setLength(0);
                    linea.append(prueba);
                } else {
                    if (linea.length() > 0) {
                        out.add(linea.toString());
                    }
                    linea = new StringBuilder(palabra);
                }
            }
            if (linea.length() > 0) {
                out.add(linea.toString());
            }
        }
        return out;
    }

    private Font fuente(boolean negrita, float tamano, Color color) {
        return new Font(negrita ? baseNegrita() : baseRegular(), tamano, Font.NORMAL, color);
    }

    private Color colorDe(String hex) {
        try {
            String h = (hex == null ? "" : hex.trim());
            if (!h.startsWith("#")) {
                h = "#" + h;
            }
            if (h.length() != 7) {
                throw new IllegalArgumentException();
            }
            return new Color(Integer.parseInt(h.substring(1), 16));
        } catch (Exception e) {
            return new Color(Integer.parseInt(COLOR_DEFECTO.substring(1), 16));
        }
    }

    private void espacio(Document doc, float alto) throws Exception {
        doc.add(new Paragraph(new Phrase(" ", fuente(false, alto, TINTA))));
    }

    private String nz(String s) {
        return s == null ? "" : s;
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

    // ------------------------------------------------------------------
    // Cabecera y pie repetidos en todas las paginas
    // ------------------------------------------------------------------

    private final class CabeceraPie extends PdfPageEventHelper {

        private final Empresa empresa;
        private final Image logo;
        private final InvoiceDocument.Header header;
        private final Colores c;
        private final PdfPTable tarjetas;
        private final PdfPTable tarjetasSinPago;
        private final float altoTarjetas;
        private final boolean conPago;
        private PdfTemplate totalPaginas;
        private int paginasReales;

        CabeceraPie(Empresa empresa, Image logo, InvoiceDocument.Header header, Colores colores,
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
                        Math.max(derecha - RESERVA_FACTURA - xInfo, 80f));
            } else {
                dibujarDatosEmpresa(cb, izquierda, pagina.getHeight() - 34, 15f,
                        Math.max(derecha - RESERVA_FACTURA - izquierda, 80f));
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
            float y = yTarjetas(bordeSuperiorContenido, altoTarjetas);
            tabla.writeSelectedRows(0, -1, izquierda, y, cb);
        }

        private void dibujarLogo(PdfContentByte cb, float izquierda, float bordeSuperiorContenido) {
            logo.scaleToFit(CabeceraLayout.ANCHO_LOGO_FIJO, CabeceraLayout.ALTO_LOGO_FIJO);
            logo.setAbsolutePosition(izquierda, bordeSuperiorContenido + CabeceraLayout.HUECO_LOGO_INFERIOR + altoTarjetas + HUECO_TARJETAS);
            try {
                cb.addImage(logo);
            } catch (Exception ignored) {
            }
        }

        private void dibujarDatosEmpresa(PdfContentByte cb, float x, float yInicial, float tamNombre,
                                         float anchoDisponible) {
            float y = yInicial;
            String nombre = nz(empresa.getNombre());
            BaseFont bfNombre = baseNegrita();
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
                    BaseFont bf = baseRegular();
                    float t = ajustarTamano(linea.texto, bf, 9f, anchoDisponible);
                    cb.beginText();
                    cb.setFontAndSize(bf, t);
                    cb.setColorFill(GRIS);
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
            BaseFont bf = baseNegrita();
            float t = ajustarTamano(texto, bf, 9f, Math.max(anchoDisponible - 10f, 40f));
            float ancho = bf.getWidthPoint(texto, t);
            cb.setColorFill(c.claro);
            cb.roundRectangle(x - 4, yBase - 3.5f, ancho + 10, 12.5f, RADIO_CHIP);
            cb.fill();
            cb.setColorStroke(c.bordeTabla);
            cb.setLineWidth(0.6f);
            cb.roundRectangle(x - 4, yBase - 3.5f, ancho + 10, 12.5f, RADIO_CHIP);
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
            cb.setFontAndSize(baseNegrita(), 18);
            cb.setColorFill(c.oscuro);
            cb.showTextAligned(Element.ALIGN_RIGHT, titulo, derecha, y, 0);
            cb.endText();
            y -= 22;
            dibujarRotulo(cb, "SERIE / Nº", derecha, y);
            y -= 9;
            cb.beginText();
            cb.setFontAndSize(baseNegrita(), 10);
            cb.setColorFill(c.oscuro);
            cb.showTextAligned(Element.ALIGN_RIGHT, header.number(), derecha, y, 0);
            cb.endText();
            y -= 14;
            dibujarRotulo(cb, "FECHA", derecha, y);
            y -= 9;
            cb.beginText();
            cb.setFontAndSize(baseNegrita(), 10);
            cb.setColorFill(c.oscuro);
            cb.showTextAligned(Element.ALIGN_RIGHT, header.date(), derecha, y, 0);
            cb.endText();
            if (header.corrective()) {
                y -= 12;
                cb.beginText();
                cb.setFontAndSize(baseRegular(), 9);
                cb.setColorFill(GRIS);
                cb.showTextAligned(Element.ALIGN_RIGHT, "Rectifica a: " + header.correctsReference().orElse(""),
                        derecha, y, 0);
                cb.endText();
            }
            if (header.cancelled()) {
                y -= 15;
                cb.beginText();
                cb.setFontAndSize(baseNegrita(), 11);
                cb.setColorFill(ROJO_ANULADA);
                cb.showTextAligned(Element.ALIGN_RIGHT, "ANULADA", derecha, y, 0);
                cb.endText();
            }
        }

        private void dibujarRotulo(PdfContentByte cb, String texto, float derecha, float y) {
            cb.beginText();
            cb.setFontAndSize(baseNegrita(), 6.5f);
            cb.setColorFill(c.oscuro);
            cb.showTextAligned(Element.ALIGN_RIGHT, texto, derecha, y, 0);
            cb.endText();
        }

        private void dibujarSeparador(PdfContentByte cb, float izquierda, float derecha, float bordeSuperior, float altoTarjetas) {
            float ySep = yTarjetas(bordeSuperior, altoTarjetas) + 4;
            cb.setColorStroke(c.bordeTabla);
            cb.setLineWidth(0.9f);
            cb.moveTo(izquierda, ySep);
            cb.lineTo(derecha, ySep);
            cb.stroke();
        }

        private void dibujarPaginacion(PdfWriter writer, PdfContentByte cb, float derecha) {
            BaseFont bfPie = baseRegular();
            float wHueco = bfPie.getWidthPoint("00", 8);
            float xTotal = derecha - wHueco;
            cb.beginText();
            cb.setFontAndSize(bfPie, 8);
            cb.setColorFill(GRIS);
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
            cb.setColorFill(ROJO_ANULADA);
            cb.showTextAligned(Element.ALIGN_CENTER, "ANULADA",
                    (pagina.getLeft() + pagina.getRight()) / 2,
                    (pagina.getTop() + pagina.getBottom()) / 2, 45);
            cb.endText();
            cb.restoreState();
        }

        @Override
        public void onCloseDocument(PdfWriter writer, Document document) {
            ColumnText.showTextAligned(totalPaginas, Element.ALIGN_LEFT,
                    new Phrase(String.valueOf(paginasReales), new Font(baseRegular(), 8)), 0, 0, 0);
        }
    }
}
