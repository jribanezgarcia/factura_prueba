package com.alcazaba.facturacion.pdf;

import com.alcazaba.facturacion.model.Empresa;
import com.alcazaba.facturacion.service.FacturaService;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
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
final class OpenPdfRenderer {


    void exportar(FacturaService.VersionCompleta vc, Empresa empresa, OutputStream out, String colorHex) throws Exception {
        EstiloPdf.Colores colores = new EstiloPdf.Colores(colorDe(colorHex));
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
        float hCierre = (haySuplidos ? EstiloPdf.ESP_SUPLIDOS + altoSuplidos + EstiloPdf.ESP_SUPLIDOS : 0)
                + altoTotales
                + (hayObs ? EstiloPdf.ESP_OBS + altoObs : 0)
                + (hayPie ? EstiloPdf.ESP_PIE + altoPie : 0);

        float[] margenes = margenes(empresa, logo, colores, altoTarjetas);

        try (Document doc = new Document(PageSize.A4, EstiloPdf.MARGEN_LATERAL, EstiloPdf.MARGEN_LATERAL, margenes[0], margenes[1])) {
            PdfWriter writer = PdfWriter.getInstance(doc, out);
            writer.setPageEvent(new CabeceraPiePdf(empresa, logo, invoice.header(), colores,
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
                doc.add(espaciador(EstiloPdf.ESP_SUPLIDOS));
                doc.add(bloqueSup);
                doc.add(espaciador(EstiloPdf.ESP_SUPLIDOS));
            }
            doc.add(bloqueTot);
            if (hayObs) {
                doc.add(espaciador(EstiloPdf.ESP_OBS));
                doc.add(cajaObs);
            }
            if (hayPie) {
                doc.add(espaciador(EstiloPdf.ESP_PIE));
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



    // ------------------------------------------------------------------
    // Tarjetas bicolor
    // ------------------------------------------------------------------

    PdfPTable tarjetas(FacturaService.VersionCompleta vc, EstiloPdf.Colores c) {
        List<String[]> pagoFilas = new ArrayList<>();
    for (InvoiceDocument.FieldRow fila : InvoiceDocumentBuilder.paymentRows(vc.version())) {
        pagoFilas.add(new String[]{fila.label(), fila.value()});
    }
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

    private PdfPCell celdaTarjeta(PdfPTable interior, EstiloPdf.Colores c) {
        PdfPCell celula = new PdfPCell();
        celula.setBorder(Rectangle.NO_BORDER);
        celula.setPadding(0);
        celula.setVerticalAlignment(Element.ALIGN_TOP);
        celula.addElement(interior);
        return celula;
    }

    PdfPTable tarjetaCliente(FacturaService.VersionCompleta vc, EstiloPdf.Colores c) {
        return tarjetaCliente(InvoiceDocumentBuilder.clientCard(vc.version()), c);
    }

    private PdfPTable tarjetaCliente(InvoiceDocument.ClientCard card, EstiloPdf.Colores c) {
        PdfPTable t = new PdfPTable(1);
        t.setWidthPercentage(100);
        t.setTableEvent(new EstiloPdf.ContornoTabla(EstiloPdf.RADIO_TARJETA, c.bordeTabla));
        t.addCell(cabeceraTarjeta(card.title(), c, false));
        PdfPCell cuerpo = new PdfPCell();
        cuerpo.setBackgroundColor(EstiloPdf.BLANCO);
        cuerpo.setBorder(Rectangle.NO_BORDER);
        cuerpo.setPadding(8);
        cuerpo.setPaddingTop(6);

        if (card.rows().isEmpty()) {
            cuerpo.setPhrase(new Phrase(card.emptyMarker(), EstiloPdf.fuente(false, 9.5f, EstiloPdf.GRIS_CLARO)));
            t.addCell(cuerpo);
            return t;
        }

        float anchoEtiqueta = 0;
        BaseFont bfEtiq = EstiloPdf.baseRegular();
        for (InvoiceDocument.FieldRow fila : card.rows()) {
            float w = bfEtiq.getWidthPoint(fila.label(), 8.5f);
            if (w > anchoEtiqueta) anchoEtiqueta = w;
        }
        anchoEtiqueta += EstiloPdf.MARGEN_ETIQUETA;
        float anchoInterior = anchoContenido() * 0.49f - 16f;
        float anchoValor = anchoInterior - anchoEtiqueta;
        if (anchoValor < 40) anchoValor = 40;
        PdfPTable filasTabla = new PdfPTable(new float[]{anchoEtiqueta, anchoValor});
        filasTabla.setWidthPercentage(100);
        for (InvoiceDocument.FieldRow fila : card.rows()) {
            PdfPCell etiqueta = new PdfPCell(new Phrase(fila.label(), EstiloPdf.fuente(false, 8.5f, EstiloPdf.GRIS_CLARO)));
            etiqueta.setBorder(Rectangle.NO_BORDER);
            etiqueta.setPadding(1.5f);
            filasTabla.addCell(etiqueta);
            Font fuenteValor = "Nombre".equals(fila.label())
                    ? EstiloPdf.fuente(true, 10.5f, EstiloPdf.TINTA)
                    : EstiloPdf.fuente(false, 9.5f, EstiloPdf.TINTA);
            PdfPCell valor = new PdfPCell(new Phrase(fila.value(), fuenteValor));
            valor.setBorder(Rectangle.NO_BORDER);
            valor.setPadding(1.5f);
            filasTabla.addCell(valor);
        }
        cuerpo.addElement(filasTabla);
        t.addCell(cuerpo);
        return t;
    }

    PdfPTable tarjetaPago(List<String[]> filas, EstiloPdf.Colores c) {
        List<InvoiceDocument.FieldRow> rows = new ArrayList<>();
        for (String[] fila : filas) {
            rows.add(new InvoiceDocument.FieldRow(fila[0], fila[1]));
        }
        return tarjetaPago(new InvoiceDocument.PaymentCard(InvoiceDocumentBuilder.paymentCardTitle(), rows), c);
    }

    PdfPTable tarjetaPago(InvoiceDocument.PaymentCard card, EstiloPdf.Colores c) {
        PdfPTable t = new PdfPTable(1);
        t.setWidthPercentage(100);
        t.setTableEvent(new EstiloPdf.ContornoTabla(EstiloPdf.RADIO_TARJETA, c.bordeTabla));
        t.addCell(cabeceraTarjeta(card.title(), c, true));
        PdfPCell cuerpo = new PdfPCell();
        cuerpo.setBackgroundColor(EstiloPdf.BLANCO);
        cuerpo.setBorder(Rectangle.NO_BORDER);
        cuerpo.setPadding(8);
        cuerpo.setPaddingTop(6);

        float anchoEtiqueta = 0;
        BaseFont bfEtiq = EstiloPdf.baseRegular();
        for (InvoiceDocument.FieldRow fila : card.rows()) {
            float w = bfEtiq.getWidthPoint(fila.label(), 8.5f);
            if (w > anchoEtiqueta) anchoEtiqueta = w;
        }
        anchoEtiqueta += EstiloPdf.MARGEN_ETIQUETA;
        float anchoInterior = anchoContenido() * 0.49f - 16f;
        float anchoValor = anchoInterior - anchoEtiqueta;
        if (anchoValor < 40) anchoValor = 40;
        PdfPTable filasTabla = new PdfPTable(new float[]{anchoEtiqueta, anchoValor});
        filasTabla.setWidthPercentage(100);
        for (InvoiceDocument.FieldRow fila : card.rows()) {
            PdfPCell etiqueta = new PdfPCell(new Phrase(fila.label(), EstiloPdf.fuente(false, 8.5f, EstiloPdf.GRIS_CLARO)));
            etiqueta.setBorder(Rectangle.NO_BORDER);
            etiqueta.setPadding(1.5f);
            filasTabla.addCell(etiqueta);
            PdfPCell valor = new PdfPCell(new Phrase(fila.value(), EstiloPdf.fuente(false, 9.5f, EstiloPdf.GRIS)));
            valor.setBorder(Rectangle.NO_BORDER);
            valor.setPadding(1.5f);
            filasTabla.addCell(valor);
        }
        cuerpo.addElement(filasTabla);
        t.addCell(cuerpo);
        return t;
    }
    private PdfPCell cabeceraTarjeta(String titulo, EstiloPdf.Colores c, boolean clara) {
        PdfPCell celula = new PdfPCell(new Phrase(" ", EstiloPdf.fuente(false, 1f, EstiloPdf.BLANCO)));
        celula.setBorder(Rectangle.NO_BORDER);
        celula.setMinimumHeight(17f);
        celula.setPaddingLeft(8);
        celula.setPaddingTop(5);
        celula.setCellEvent(new EstiloPdf.RotuloTarjeta(titulo, c, clara));
        return celula;
    }




    // ------------------------------------------------------------------
    // Tabla de lineas estilo hoja de calculo
    // ------------------------------------------------------------------

    private PdfPTable tablaLineasVacia(EstiloPdf.Colores c) {
        PdfPTable t = new PdfPTable(new float[]{0.7f, 5.15f, 1.25f, 0.75f, 1.45f});
        t.setWidthPercentage(100);
        t.addCell(celdaCabeceraColumna("CANT.", c));
        t.addCell(celdaCabeceraColumna("DESCRIPCIÓN", c));
        t.addCell(celdaCabeceraColumna("PRECIO", c));
        t.addCell(celdaCabeceraColumna("IVA %", c));
        t.addCell(celdaCabeceraColumna("TOTAL", c));
        return t;
    }

    private PdfPTable tablaLineas(InvoiceDocument.LinesTable lines, EstiloPdf.Colores c) {
        PdfPTable t = tablaLineasVacia(c);
        int fila = 0;
        for (InvoiceDocument.LineRow l : lines.rows()) {
            t.addCell(celdaLinea(l.quantity(), fila, Element.ALIGN_CENTER, c));
            t.addCell(celdaLinea(l.description(), fila, Element.ALIGN_LEFT, c));
            t.addCell(celdaLinea(l.price(), fila, Element.ALIGN_CENTER, c));
            t.addCell(celdaLinea(l.iva(), fila, Element.ALIGN_CENTER, c));
            t.addCell(celdaLinea(l.total(), fila, Element.ALIGN_CENTER, c));
            fila++;
        }
        if (fila > 0) t.setHeaderRows(1);
        return t;
    }

    private PdfPTable bloqueSuplidos(InvoiceDocument.SuplidosBlock suplidos, EstiloPdf.Colores c) {
        PdfPTable t = new PdfPTable(new float[]{7.85f, 1.45f});
        t.setWidthPercentage(100);

        t.addCell(celdaCabeceraColumnaCompacta(suplidos.headers().get(0), c));
        t.addCell(celdaCabeceraColumnaCompacta(suplidos.headers().get(1), c));

        int fila = 0;
        for (InvoiceDocument.SuplidoRow l : suplidos.rows()) {
            t.addCell(celdaLineaCompacta(l.description(), fila, Element.ALIGN_LEFT, c));
            t.addCell(celdaLineaCompacta(l.amount(), fila, Element.ALIGN_RIGHT, c));
            fila++;
        }

        PdfPCell nota = new PdfPCell(new Phrase(suplidos.note(), EstiloPdf.fuente(false, 7f, c.oscuro)));
        nota.setColspan(2);
        nota.setBorder(Rectangle.NO_BORDER);
        nota.setPaddingTop(3f);
        t.addCell(nota);
        return t;
    }

    private PdfPCell celdaCabeceraColumnaCompacta(String texto, EstiloPdf.Colores c) {
        PdfPCell celula = new PdfPCell(new Phrase(texto, EstiloPdf.fuente(true, 7f, c.oscuro)));
        celula.setBackgroundColor(c.claro);
        celula.setHorizontalAlignment(Element.ALIGN_CENTER);
        celula.setPadding(3);
        celula.setBorderColor(c.bordeTabla);
        return celula;
    }

    private PdfPCell celdaLineaCompacta(String texto, int fila, int alineacion, EstiloPdf.Colores c) {
        PdfPCell celula = new PdfPCell(new Phrase(texto, EstiloPdf.fuente(false, 8f, EstiloPdf.TINTA)));
        celula.setHorizontalAlignment(alineacion);
        celula.setPadding(2.5f);
        celula.setBorderColor(c.bordeTabla);
        if (fila % 2 == 1) {
            celula.setBackgroundColor(c.clarisimo);
        }
        return celula;
    }

    private PdfPCell celdaCabeceraColumna(String texto, EstiloPdf.Colores c) {
        PdfPCell celula = new PdfPCell(new Phrase(texto, EstiloPdf.fuente(true, 8f, c.oscuro)));
        celula.setBackgroundColor(c.claro);
        celula.setHorizontalAlignment(Element.ALIGN_CENTER);
        celula.setPadding(4);
        celula.setBorderColor(c.bordeTabla);
        return celula;
    }

    private PdfPCell celdaLinea(String texto, int fila, int alineacion, EstiloPdf.Colores c) {
        PdfPCell celula = new PdfPCell(new Phrase(texto, EstiloPdf.fuente(false, 9f, EstiloPdf.TINTA)));
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

    private PdfPTable bloqueTotales(InvoiceDocument.TotalsBlock totals, EstiloPdf.Colores c) {
        PdfPTable contenedor = new PdfPTable(new float[]{5.85f, 3.45f});
        contenedor.setWidthPercentage(100);

        PdfPCell celdaIzquierda = new PdfPCell();
        celdaIzquierda.setBorder(Rectangle.NO_BORDER);
        celdaIzquierda.setVerticalAlignment(Element.ALIGN_TOP);
        celdaIzquierda.setPaddingLeft(0f);
        celdaIzquierda.setPaddingRight(4f);
        celdaIzquierda.addElement(rejillaDesgloseIva(totals, c));
        if (totals.discountNote().isPresent()) {
            celdaIzquierda.addElement(notaDescuento(totals.discountNote().orElseThrow(), c));
        }

        PdfPCell celdaDerecha = new PdfPCell();
        celdaDerecha.setBorder(Rectangle.NO_BORDER);
        celdaDerecha.setVerticalAlignment(Element.ALIGN_TOP);
        celdaDerecha.setPaddingLeft(0f);
        celdaDerecha.setPaddingRight(0f);
        celdaDerecha.addElement(rejillaLiquidacion(totals.liquidation(), c));

        contenedor.addCell(celdaIzquierda);
        contenedor.addCell(celdaDerecha);
        return contenedor;
    }

    private PdfPTable rejillaDesgloseIva(InvoiceDocument.TotalsBlock totals, EstiloPdf.Colores c) {
        PdfPTable t = new PdfPTable(new float[]{38.78f, 152.08f, 129.26f});
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

    private Paragraph notaDescuento(String nota, EstiloPdf.Colores c) {
        Paragraph p = new Paragraph();
        p.add(new Chunk(nota, EstiloPdf.fuente(false, 7f, c.oscuro)));
        p.setLeading(8.5f);
        p.setSpacingBefore(3f);
        p.setAlignment(Element.ALIGN_LEFT);
        return p;
    }

    private PdfPTable rejillaLiquidacion(InvoiceDocument.Liquidation liquidation, EstiloPdf.Colores c) {
        PdfPTable t = new PdfPTable(new float[]{2.0f, 1.45f});
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

        PdfPCell etiquetaTotal = new PdfPCell(new Phrase(liquidation.total().label(), EstiloPdf.fuente(true, 11f, EstiloPdf.BLANCO)));
        etiquetaTotal.setBackgroundColor(c.base);
        etiquetaTotal.setPadding(5f);
        etiquetaTotal.setBorderColor(c.oscuro);
        etiquetaTotal.setBorder(Rectangle.LEFT | Rectangle.TOP | Rectangle.BOTTOM);
        etiquetaTotal.setVerticalAlignment(Element.ALIGN_MIDDLE);
        PdfPCell importeTotal = new PdfPCell(new Phrase(liquidation.total().amount(), EstiloPdf.fuente(true, 11f, EstiloPdf.BLANCO)));
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

    private void filaLiquidacion(PdfPTable t, String etiqueta, String importe, EstiloPdf.Colores c, boolean retencion) {
        t.addCell(celdaCuerpoRejilla(etiqueta, Element.ALIGN_LEFT, c, false, retencion));
        t.addCell(celdaCuerpoRejilla(importe, Element.ALIGN_RIGHT, c, false, retencion));
    }

    private PdfPCell celdaCabeceraRejilla(String texto, EstiloPdf.Colores c) {
        Chunk chunk = new Chunk(texto, EstiloPdf.fuente(true, 7f, EstiloPdf.BLANCO));
        chunk.setCharacterSpacing(0.5f);
        PdfPCell celula = new PdfPCell(new Phrase(chunk));
        celula.setBackgroundColor(c.base);
        celula.setHorizontalAlignment(Element.ALIGN_CENTER);
        celula.setPadding(4f);
        celula.setBorderColor(c.bordeTabla);
        celula.setBorder(Rectangle.TOP | Rectangle.BOTTOM | Rectangle.LEFT | Rectangle.RIGHT);
        return celula;
    }

    private PdfPCell celdaCuerpoRejilla(String texto, int alineacion, EstiloPdf.Colores c, boolean totales, boolean retencion) {
        Font f = retencion
                ? new Font(EstiloPdf.baseCursiva(), 8.5f, Font.NORMAL, EstiloPdf.ROJO_DESCUENTO)
                : EstiloPdf.fuente(totales, 8.5f, EstiloPdf.TINTA);
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

    private PdfPTable cajaObservaciones(String observaciones, EstiloPdf.Colores c) {
        PdfPTable t = new PdfPTable(1);
        t.setWidthPercentage(100);
        PdfPCell celula = new PdfPCell();
        celula.setBackgroundColor(c.clarisimo);
        celula.setBorder(Rectangle.NO_BORDER);
        celula.setPadding(8);
        celula.setCellEvent(new EstiloPdf.ContornoRedondeado(EstiloPdf.RADIO_CAJA, c.bordeTabla));
        Paragraph p = new Paragraph("Observaciones", EstiloPdf.fuente(true, 8.5f, c.oscuro));
        p.add(new Phrase("\n" + observaciones, EstiloPdf.fuente(false, 9f, EstiloPdf.TINTA)));
        celula.setPhrase(p);
        t.addCell(celula);
        return t;
    }

    PdfPTable cajaPieLegal(String pie, EstiloPdf.Colores c) {
        PdfPTable t = new PdfPTable(1);
        t.setWidthPercentage(100);
        t.setSplitLate(true);
        t.setKeepTogether(true);
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(c.clarisimo);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(8);
        cell.setPaddingLeft(10);
        cell.setCellEvent(new EstiloPdf.FondoPieLegal(EstiloPdf.RADIO_CAJA, c));
        Paragraph p = new Paragraph();
        p.setLeading(EstiloPdf.PIE_LEGAL_INTERLINEADO);
        p.add(new Chunk(pie, EstiloPdf.fuente(false, EstiloPdf.PIE_LEGAL_TAM, EstiloPdf.GRIS)));
        cell.setPhrase(p);
        t.addCell(cell);
        return t;
    }


    // ------------------------------------------------------------------
    // Margenes dinamicos segun cabecera y pie legal
    // ------------------------------------------------------------------

    private float[] margenes(Empresa empresa, Image logo, EstiloPdf.Colores c, float altoTarjetas) {
        float superior;
        int lineas = CabeceraLayout.lineasEmpresa(empresa).size();
        if (logo != null) {
            superior = CabeceraLayout.altoCabeceraLogo(empresa, lineas);
        } else {
            superior = CabeceraLayout.altoCabeceraTexto(lineas);
        }
        superior += altoTarjetas + EstiloPdf.HUECO_TARJETAS;
        return new float[]{superior, EstiloPdf.MARGEN_INFERIOR};
    }


    private static float anchoContenido() {
        return PageSize.A4.getWidth() - 2 * EstiloPdf.MARGEN_LATERAL;
    }

    private static void medir(PdfPTable t, float ancho) {
        t.setTotalWidth(ancho);
        t.setLockedWidth(true);
        t.calculateHeights(true);
    }

    PdfPTable tablaRelleno(float hueco, EstiloPdf.Colores c) {
        PdfPTable t = new PdfPTable(new float[]{0.7f, 5.15f, 1.25f, 0.75f, 1.45f});
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
        PdfPCell celula = new PdfPCell(new Phrase(" ", EstiloPdf.fuente(false, 1f, EstiloPdf.BLANCO)));
        celula.setBorder(Rectangle.NO_BORDER);
        celula.setFixedHeight(alto);
        celula.setPadding(0);
        t.addCell(celula);
        return t;
    }

    PdfPTable tarjetasSinPago(FacturaService.VersionCompleta vc, EstiloPdf.Colores c) {
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
            return new Color(Integer.parseInt(PdfService.COLOR_DEFECTO.substring(1), 16));
        }
    }

    void exportarAgrupado(List<FacturaService.VersionCompleta> versiones, Empresa empresa, Path ruta, String colorHex) throws Exception {
        List<byte[]> pdfs = new ArrayList<>();
        for (FacturaService.VersionCompleta vc : versiones) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            exportar(vc, empresa, baos, colorHex);
            pdfs.add(baos.toByteArray());
        }
        concatenar(pdfs, ruta);
    }
}
