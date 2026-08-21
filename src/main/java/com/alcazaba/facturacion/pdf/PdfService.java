package com.alcazaba.facturacion.pdf;

import com.alcazaba.facturacion.model.Empresa;
import com.alcazaba.facturacion.model.EstadoFactura;
import com.alcazaba.facturacion.model.FacturaVersion;
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
import com.lowagie.text.pdf.BaseFont;
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
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Exportacion a PDF con el diseno aprobado inspirado en la hoja de calculo:
 * A4 vertical, cabecera repetida con logo al doble del tamano configurado
 * junto a los datos de empresa con NIF destacado, bloque FACTURA/Serie-Nº/fecha a la
 * derecha, tarjetas bicolor (Facturar a / Datos de pago), tabla de lineas con
 * celdas bordeadas y total por linea con IVA incluido, resumen con fila TOTAL
 * en color, observaciones en caja y pie legal en recuadro repetido en todas
 * las paginas con Página X de Y. El color de acento es configurable.
 */
public class PdfService {

    public static final String PREF_COLOR = "color_pdf";
    public static final String COLOR_DEFECTO = "#B08D57";

    private static final Color TINTA = new Color(0x33, 0x33, 0x33);
    private static final Color GRIS = new Color(0x60, 0x5A, 0x50);
    private static final Color GRIS_CLARO = new Color(0xB9, 0xB1, 0xA4);
    private static final Color BLANCO = Color.WHITE;
    private static final Color NEGRO = Color.BLACK;
    private static final Color ROJO_ANULADA = new Color(0xB0, 0x00, 0x20);
    private static final float MARGEN_LATERAL = 40f;

    public void exportar(FacturaService.VersionCompleta vc, Empresa empresa, Path ruta) throws Exception {
        exportar(vc, empresa, ruta, null);
    }

    public void exportar(FacturaService.VersionCompleta vc, Empresa empresa, Path ruta, String colorHex) throws Exception {
        Colores colores = new Colores(colorDe(colorHex));
        boolean anulada = vc.version().getEstado() == EstadoFactura.ANULADA;
        boolean rectificativa = vc.version().getReferenciaRectifica() != null
                && !vc.version().getReferenciaRectifica().isBlank();
        ResumenFactura resumen = CalculoService.resumen(vc.lineas(), vc.version().getDescuentoPorcentaje());

        Image logo = cargarLogo(empresa);
        float[] margenes = margenes(empresa, logo, colores);

        try (Document doc = new Document(PageSize.A4, MARGEN_LATERAL, MARGEN_LATERAL, margenes[0], margenes[1])) {
            PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream(ruta.toFile()));
            writer.setPageEvent(new CabeceraPie(empresa, logo, anulada, rectificativa, vc.version(), colores));
            doc.open();

            doc.add(tarjetas(vc, colores));
            espacio(doc);
            doc.add(tablaLineas(vc, colores));
            espacio(doc);
            doc.add(bloqueTotales(resumen, vc.version().getDescuentoPorcentaje(), colores));
            String obs = vc.version().getObservaciones();
            if (obs != null && !obs.isBlank()) {
                espacio(doc);
                doc.add(cajaObservaciones(obs, colores));
            }
        }
    }

    // ------------------------------------------------------------------
    // Tarjetas bicolor
    // ------------------------------------------------------------------

    private PdfPTable tarjetas(FacturaService.VersionCompleta vc, Colores c) {
        PdfPTable exterior = new PdfPTable(new float[]{49f, 2f, 49f});
        exterior.setWidthPercentage(100);
        exterior.addCell(celdaTarjeta(tarjetaCliente(vc, c), c));
        PdfPCell hueco = new PdfPCell(new Phrase(" "));
        hueco.setBorder(Rectangle.NO_BORDER);
        exterior.addCell(hueco);
        exterior.addCell(celdaTarjeta(tarjetaPago(vc, c), c));
        return exterior;
    }

    private PdfPCell celdaTarjeta(PdfPTable interior, Colores c) {
        PdfPCell celula = new PdfPCell(interior);
        celula.setBorder(Rectangle.BOX);
        celula.setBorderColor(c.bordeTabla);
        celula.setPadding(0);
        return celula;
    }

    private PdfPTable tarjetaCliente(FacturaService.VersionCompleta vc, Colores c) {
        PdfPTable t = new PdfPTable(1);
        t.setWidthPercentage(100);
        t.addCell(cabeceraTarjeta("FACTURAR A", c));
        PdfPCell cuerpo = new PdfPCell();
        cuerpo.setBackgroundColor(BLANCO);
        cuerpo.setBorder(Rectangle.NO_BORDER);
        cuerpo.setPadding(8);
        cuerpo.setPaddingTop(6);
        Paragraph p = new Paragraph();
        p.add(new Chunk(nz(vc.version().getCliNombre()), fuente(true, 11, TINTA)));
        p.add(new Phrase("\n"));
        if (!nz(vc.version().getCliNif()).isBlank()) {
            p.add(new Phrase("NIF: " + vc.version().getCliNif(), fuente(false, 9.5f, GRIS)));
            p.add(new Phrase("\n"));
        }
        boolean primero = true;
        if (!nz(vc.version().getCliDireccion()).isBlank()) {
            p.add(lineaDato(primero, vc.version().getCliDireccion()));
            primero = false;
        }
        String poblacion = joinNoVacio(" ", vc.version().getCliCp(), vc.version().getCliLocalidad());
        if (!poblacion.isBlank()) {
            p.add(lineaDato(primero, poblacion));
            primero = false;
        }
        if (!nz(vc.version().getCliProvincia()).isBlank()) {
            p.add(lineaDato(primero, vc.version().getCliProvincia()));
            primero = false;
        }
        if (!nz(vc.version().getCliEmail()).isBlank()) {
            p.add(new Phrase((primero ? "" : "\n") + vc.version().getCliEmail(), fuente(false, 9.5f, c.oscuro)));
        }
        cuerpo.setPhrase(p);
        t.addCell(cuerpo);
        return t;
    }

    private Phrase lineaDato(boolean primera, String texto) {
        return new Phrase((primera ? "" : "\n") + texto, fuente(false, 9.5f, GRIS));
    }

    private PdfPTable tarjetaPago(FacturaService.VersionCompleta vc, Colores c) {
        PdfPTable t = new PdfPTable(1);
        t.setWidthPercentage(100);
        t.addCell(cabeceraTarjeta("DATOS DE PAGO", c));
        PdfPCell cuerpo = new PdfPCell();
        cuerpo.setBackgroundColor(BLANCO);
        cuerpo.setBorder(Rectangle.NO_BORDER);
        cuerpo.setPadding(8);
        cuerpo.setPaddingTop(6);

        FacturaVersion v = vc.version();
        List<String[]> filas = new ArrayList<>();
        if (!nz(v.getFormaPago()).isBlank()) {
            filas.add(new String[]{"Forma de pago", v.getFormaPago()});
        }
        if (v.getVencimiento() != null) {
            filas.add(new String[]{"Vencimiento", Formatos.fecha(v.getVencimiento())});
        }
        if (!nz(v.getRealizadaPor()).isBlank()) {
            filas.add(new String[]{"Realizada por", v.getRealizadaPor()});
        }

        if (filas.isEmpty()) {
            cuerpo.setPhrase(new Phrase("—", fuente(false, 9.5f, GRIS_CLARO)));
            t.addCell(cuerpo);
            return t;
        }

        PdfPTable filasTabla = new PdfPTable(new float[]{34f, 66f});
        filasTabla.setWidthPercentage(100);
        for (String[] fila : filas) {
            PdfPCell etiqueta = new PdfPCell(new Phrase(fila[0], fuente(false, 8.5f, GRIS_CLARO)));
            etiqueta.setBorder(Rectangle.NO_BORDER);
            etiqueta.setPadding(1.5f);
            filasTabla.addCell(etiqueta);
            PdfPCell valor = new PdfPCell(new Phrase(fila[1], fuente(false, 9.5f, TINTA)));
            valor.setBorder(Rectangle.NO_BORDER);
            valor.setPadding(1.5f);
            filasTabla.addCell(valor);
        }
        cuerpo.addElement(filasTabla);
        t.addCell(cuerpo);
        return t;
    }

    private PdfPCell cabeceraTarjeta(String titulo, Colores c) {
        PdfPCell celula = new PdfPCell(new Phrase(titulo, fuente(true, 8.5f, BLANCO)));
        celula.setBackgroundColor(c.base);
        celula.setBorder(Rectangle.NO_BORDER);
        celula.setPadding(5);
        celula.setPaddingLeft(8);
        return celula;
    }

    // ------------------------------------------------------------------
    // Tabla de lineas estilo hoja de calculo
    // ------------------------------------------------------------------

    private PdfPTable tablaLineas(FacturaService.VersionCompleta vc, Colores c) {
        PdfPTable t = new PdfPTable(new float[]{0.7f, 4.3f, 1.4f, 1.0f, 1.9f});
        t.setWidthPercentage(100);

        t.addCell(celdaCabeceraColumna("CANT.", c));
        t.addCell(celdaCabeceraColumna("DESCRIPCIÓN", c));
        t.addCell(celdaCabeceraColumna("PRECIO", c));
        t.addCell(celdaCabeceraColumna("IVA %", c));
        t.addCell(celdaCabeceraColumna("TOTAL", c));

        int fila = 0;
        for (LineaFactura l : vc.lineas()) {
            t.addCell(celdaLinea(String.valueOf(l.getCantidad()), fila, Element.ALIGN_CENTER, c));
            t.addCell(celdaLinea(nz(l.getDescripcion()), fila, Element.ALIGN_LEFT, c));
            t.addCell(celdaLinea(Formatos.moneda(l.getPrecioUnitario()), fila, Element.ALIGN_RIGHT, c));
            t.addCell(celdaLinea(l.isExenta() ? "Exento"
                    : l.getIvaPorcentaje() + " %", fila, Element.ALIGN_CENTER, c));
            t.addCell(celdaLinea(Formatos.moneda(totalConIva(l)), fila, Element.ALIGN_RIGHT, c));
            fila++;
        }
        return t;
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

    /**
     * Total de la linea con el IVA incluido (base × (1 + IVA%)); las
     * exentas se muestran sin IVA.
     */
    private BigDecimal totalConIva(LineaFactura l) {
        BigDecimal base = l.getTotalBase() == null ? BigDecimal.ZERO : l.getTotalBase();
        if (l.isExenta() || l.getIvaPorcentaje() == null || l.getIvaPorcentaje() == 0) {
            return base;
        }
        return base.add(CalculoService.ivaDeBase(base, l.getIvaPorcentaje()));
    }

    // ------------------------------------------------------------------
    // Totales
    // ------------------------------------------------------------------

    private PdfPTable bloqueTotales(ResumenFactura r, int descuento, Colores c) {
        PdfPTable t = new PdfPTable(new float[]{3.1f, 1.7f});
        t.setWidthPercentage(46);
        t.setHorizontalAlignment(Element.ALIGN_RIGHT);
        t.setSpacingBefore(2);

        for (ResumenFactura.IvaGrupo g : r.getGrupos()) {
            String nombreBase = g.isExento()
                    ? "Base exenta" + (g.getMotivoExencion() != null && !g.getMotivoExencion().isBlank()
                            ? " (" + g.getMotivoExencion() + ")"
                            : "")
                    : "Base " + g.getPorcentaje() + "%";
            t.addCell(filaResumen(nombreBase, Formatos.moneda(g.getBase())));
            t.addCell(filaResumen(g.isExento() ? "IVA exento" : "IVA " + g.getPorcentaje() + "%",
                    Formatos.moneda(g.getCuota())));
        }
        if (descuento > 0) {
            PdfPCell desc = new PdfPCell(new Phrase("Descuento aplicado: " + descuento + "%",
                    fuente(false, 9f, GRIS)));
            desc.setColspan(2);
            desc.setBorder(Rectangle.NO_BORDER);
            desc.setPadding(1);
            desc.setHorizontalAlignment(Element.ALIGN_RIGHT);
            t.addCell(desc);
        }
        t.addCell(filaResumen("Base total", Formatos.moneda(r.getBaseTotal())));
        t.addCell(filaResumen("IVA total", Formatos.moneda(r.getIvaTotal())));

        PdfPCell etiquetaTotal = new PdfPCell(new Phrase("TOTAL", fuente(true, 12, BLANCO)));
        etiquetaTotal.setBackgroundColor(c.base);
        etiquetaTotal.setBorderColor(c.oscuro);
        etiquetaTotal.setPadding(6);
        PdfPCell importeTotal = new PdfPCell(new Phrase(Formatos.moneda(r.getTotal()), fuente(true, 12, BLANCO)));
        importeTotal.setBackgroundColor(c.base);
        importeTotal.setBorderColor(c.oscuro);
        importeTotal.setHorizontalAlignment(Element.ALIGN_RIGHT);
        importeTotal.setPadding(6);
        t.addCell(etiquetaTotal);
        t.addCell(importeTotal);
        return t;
    }

    private PdfPCell filaResumen(String etiqueta, String valor) {
        PdfPCell celula = new PdfPCell(new Phrase(etiqueta, fuente(false, 9f, GRIS)));
        celula.setBorder(Rectangle.NO_BORDER);
        celula.setPadding(1.5f);
        PdfPCell valorCelula = new PdfPCell(new Phrase(valor, fuente(false, 9f, TINTA)));
        valorCelula.setHorizontalAlignment(Element.ALIGN_RIGHT);
        valorCelula.setBorder(Rectangle.NO_BORDER);
        valorCelula.setPadding(1.5f);
        return valorCelula;
    }

    private PdfPTable cajaObservaciones(String observaciones, Colores c) {
        PdfPTable t = new PdfPTable(1);
        t.setWidthPercentage(100);
        PdfPCell celula = new PdfPCell();
        celula.setBackgroundColor(c.clarisimo);
        celula.setBorderColor(c.bordeTabla);
        celula.setPadding(8);
        Paragraph p = new Paragraph("Observaciones", fuente(true, 8.5f, c.oscuro));
        p.add(new Phrase("\n" + observaciones, fuente(false, 9f, TINTA)));
        celula.setPhrase(p);
        t.addCell(celula);
        return t;
    }

    // ------------------------------------------------------------------
    // Margenes dinamicos segun cabecera y pie legal
    // ------------------------------------------------------------------

    private float[] margenes(Empresa empresa, Image logo, Colores c) {
        float superior;
        if (logo != null) {
            float alto = altoLogoEfectivo(empresa);
            float desplazamiento = Math.max(offsetLogoY(empresa), 0);
            superior = 26f + alto + desplazamiento + 24f;
            float altoInfo = 17f + lineasEmpresa(empresa).size() * 13f;
            superior = Math.max(superior, 34f + altoInfo + 8f);
        } else {
            int lineas = lineasEmpresa(empresa).size();
            superior = 42f + lineas * 13f + 18f;
        }
        superior = Math.max(superior, 108f);

        BaseFont bfPie = FontFactory.getFont(FontFactory.HELVETICA, 7.5f).getBaseFont();
        float anchoUtil = PageSize.A4.getWidth() - 2 * MARGEN_LATERAL - 16f;
        List<String> lineasPie = partir(empresa != null ? nz(empresa.getPieLegal()) : "", bfPie, 7.5f, anchoUtil);
        float inferior = Math.max(112f, 30f + lineasPie.size() * 9f + 12f + 16f);
        return new float[]{superior, inferior};
    }

    private float anchoLogoEfectivo(Empresa empresa) {
        float base = empresa != null && empresa.getLogoAncho() != null ? empresa.getLogoAncho() : 120f;
        return Math.min(base * 2f, 480f);
    }

    private float altoLogoEfectivo(Empresa empresa) {
        float base = empresa != null && empresa.getLogoAlto() != null ? empresa.getLogoAlto() : 60f;
        return Math.min(base * 2f, 170f);
    }

    private int offsetLogoX(Empresa empresa) {
        return empresa != null ? empresa.getLogoX() : 0;
    }

    private int offsetLogoY(Empresa empresa) {
        return empresa != null ? empresa.getLogoY() : 0;
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

    private List<LineaCabecera> lineasEmpresa(Empresa empresa) {
        List<LineaCabecera> lineas = new ArrayList<>();
        if (empresa == null) {
            return lineas;
        }
        if (!nz(empresa.getActividad()).isBlank()) {
            lineas.add(new LineaCabecera(empresa.getActividad(), false));
        }
        if (!nz(empresa.getNif()).isBlank()) {
            lineas.add(new LineaCabecera("NIF: " + empresa.getNif(), true));
        }
        if (!nz(empresa.getDireccion()).isBlank()) {
            lineas.add(new LineaCabecera(empresa.getDireccion(), false));
        }
        String poblacion = joinNoVacio(" ", nz(empresa.getCp()), nz(empresa.getLocalidad()));
        if (!poblacion.isBlank()) {
            lineas.add(new LineaCabecera(poblacion, false));
        }
        String contacto = joinNoVacio("  ·  ", nz(empresa.getEmail()), nz(empresa.getTelefono()));
        if (!contacto.isBlank()) {
            lineas.add(new LineaCabecera(contacto, false));
        }
        return lineas;
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
        return FontFactory.getFont(negrita ? FontFactory.HELVETICA_BOLD : FontFactory.HELVETICA,
                tamano, color);
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

    private void espacio(Document doc) throws Exception {
        doc.add(new Paragraph(new Phrase(" ", fuente(false, 9f, TINTA))));
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

    private static final class LineaCabecera {
        final String texto;
        final boolean chipNif;

        LineaCabecera(String texto, boolean chipNif) {
            this.texto = texto;
            this.chipNif = chipNif;
        }
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
        private final boolean anulada;
        private final boolean rectificativa;
        private final FacturaVersion version;
        private final Colores c;
        private PdfTemplate totalPaginas;

        CabeceraPie(Empresa empresa, Image logo, boolean anulada, boolean rectificativa,
                    FacturaVersion version, Colores colores) {
            this.empresa = empresa;
            this.logo = logo;
            this.anulada = anulada;
            this.rectificativa = rectificativa;
            this.version = version;
            this.c = colores;
        }

        @Override
        public void onOpenDocument(PdfWriter writer, Document document) {
            totalPaginas = writer.getDirectContent().createTemplate(28, 12);
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte cb = writer.getDirectContent();
            Rectangle pagina = document.getPageSize();
            float izquierda = document.leftMargin();
            float derecha = pagina.getWidth() - document.rightMargin();
            float bordeSuperiorContenido = pagina.getHeight() - document.topMargin();
            float bordeInferiorContenido = document.bottomMargin();

            dibujarBloqueFactura(cb, derecha, pagina.getHeight());
            if (logo != null) {
                dibujarLogo(cb, izquierda, bordeSuperiorContenido);
                float xInfo = izquierda + offsetLogoX(empresa)
                        + Math.min(anchoLogoEfectivo(empresa), 330f) + 14f;
                dibujarDatosEmpresa(cb, xInfo, pagina.getHeight() - 34, 13f);
            } else {
                dibujarCabeceraTexto(cb, izquierda, pagina.getHeight());
            }
            dibujarSeparador(cb, izquierda, derecha, bordeSuperiorContenido);
            dibujarPieLegal(writer, cb, izquierda, derecha, bordeInferiorContenido);
            if (anulada) {
                dibujarMarcaAnulada(writer, cb, pagina);
            }
        }

        private void dibujarLogo(PdfContentByte cb, float izquierda, float bordeSuperiorContenido) {
            float ancho = anchoLogoEfectivo(empresa);
            float alto = altoLogoEfectivo(empresa);
            logo.scaleToFit(ancho, alto);
            float x = izquierda + offsetLogoX(empresa);
            float y = bordeSuperiorContenido + Math.max(offsetLogoY(empresa), 0);
            logo.setAbsolutePosition(x, y);
            try {
                cb.addImage(logo);
            } catch (Exception ignored) {
            }
        }

        private void dibujarCabeceraTexto(PdfContentByte cb, float izquierda, float altoPagina) {
            dibujarDatosEmpresa(cb, izquierda, altoPagina - 34, 15f);
        }

        private void dibujarDatosEmpresa(PdfContentByte cb, float x, float yInicial, float tamNombre) {
            float y = yInicial;
            cb.beginText();
            cb.setFontAndSize(FontFactory.getFont(FontFactory.HELVETICA_BOLD, tamNombre).getBaseFont(), tamNombre);
            cb.setColorFill(TINTA);
            cb.showTextAligned(Element.ALIGN_LEFT, nz(empresa.getNombre()), x, y, 0);
            cb.endText();
            y -= tamNombre + 1;
            for (LineaCabecera linea : lineasEmpresa(empresa)) {
                if (linea.chipNif) {
                    dibujarChipNif(cb, x, y);
                } else {
                    cb.beginText();
                    cb.setFontAndSize(FontFactory.getFont(FontFactory.HELVETICA, 9).getBaseFont(), 9);
                    cb.setColorFill(GRIS);
                    cb.showTextAligned(Element.ALIGN_LEFT, linea.texto, x, y, 0);
                    cb.endText();
                }
                y -= 13;
            }
        }

        private void dibujarChipNif(PdfContentByte cb, float x, float yBase) {
            String texto = "NIF: " + nz(empresa.getNif());
            BaseFont bf = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9).getBaseFont();
            float ancho = bf.getWidthPoint(texto, 9);
            cb.setColorFill(c.claro);
            cb.rectangle(x - 4, yBase - 3.5f, ancho + 10, 12.5f);
            cb.fill();
            cb.setColorStroke(c.bordeTabla);
            cb.setLineWidth(0.6f);
            cb.rectangle(x - 4, yBase - 3.5f, ancho + 10, 12.5f);
            cb.stroke();
            cb.beginText();
            cb.setFontAndSize(bf, 9);
            cb.setColorFill(c.oscuro);
            cb.showTextAligned(Element.ALIGN_LEFT, texto, x + 1, yBase, 0);
            cb.endText();
        }

        private void dibujarBloqueFactura(PdfContentByte cb, float derecha, float altoPagina) {
            float y = altoPagina - 36;
            String titulo = rectificativa ? "RECTIFICATIVA" : "FACTURA";
            cb.beginText();
            cb.setFontAndSize(FontFactory.getFont(FontFactory.HELVETICA_BOLD, 15).getBaseFont(), 15);
            cb.setColorFill(c.oscuro);
            cb.showTextAligned(Element.ALIGN_RIGHT, titulo, derecha, y, 0);
            cb.endText();
            y -= 17;
            cb.beginText();
            cb.setFontAndSize(FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10).getBaseFont(), 10);
            cb.setColorFill(TINTA);
            cb.showTextAligned(Element.ALIGN_RIGHT, nz(version.getNumero()), derecha, y, 0);
            cb.endText();
            y -= 13;
            cb.beginText();
            cb.setFontAndSize(FontFactory.getFont(FontFactory.HELVETICA, 9).getBaseFont(), 9);
            cb.setColorFill(GRIS);
            cb.showTextAligned(Element.ALIGN_RIGHT, Formatos.fecha(version.getFechaFactura()), derecha, y, 0);
            cb.endText();
            if (rectificativa) {
                y -= 12;
                cb.beginText();
                cb.setFontAndSize(FontFactory.getFont(FontFactory.HELVETICA, 9).getBaseFont(), 9);
                cb.setColorFill(GRIS);
                cb.showTextAligned(Element.ALIGN_RIGHT, "Rectifica a: " + nz(version.getReferenciaRectifica()),
                        derecha, y, 0);
                cb.endText();
            }
            if (anulada) {
                y -= 15;
                cb.beginText();
                cb.setFontAndSize(FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11).getBaseFont(), 11);
                cb.setColorFill(ROJO_ANULADA);
                cb.showTextAligned(Element.ALIGN_RIGHT, "ANULADA", derecha, y, 0);
                cb.endText();
            }
        }

        private void dibujarSeparador(PdfContentByte cb, float izquierda, float derecha, float bordeSuperior) {
            cb.setColorStroke(c.bordeTabla);
            cb.setLineWidth(0.9f);
            cb.moveTo(izquierda, bordeSuperior + 4);
            cb.lineTo(derecha, bordeSuperior + 4);
            cb.stroke();
        }

        private void dibujarPieLegal(PdfWriter writer, PdfContentByte cb, float izquierda, float derecha,
                                     float bordeInferior) {
            String pie = empresa != null && empresa.getPieLegal() != null ? empresa.getPieLegal() : "";
            BaseFont bf = FontFactory.getFont(FontFactory.HELVETICA, 7.5f).getBaseFont();
            float anchoUtil = derecha - izquierda - 12f;
            List<String> lineas = partir(pie, bf, 7.5f, anchoUtil);

            float cajaInferior = 30f;
            float alturaCaja = lineas.size() * 9f + 10f;
            cb.setColorFill(c.clarisimo);
            cb.rectangle(izquierda - 4, cajaInferior, derecha - izquierda + 8, alturaCaja);
            cb.fill();
            cb.setColorStroke(c.bordeTabla);
            cb.setLineWidth(0.7f);
            cb.rectangle(izquierda - 4, cajaInferior, derecha - izquierda + 8, alturaCaja);
            cb.stroke();

            float y = cajaInferior + alturaCaja - 7f;
            for (String linea : lineas) {
                cb.beginText();
                cb.setFontAndSize(bf, 7.5f);
                cb.setColorFill(GRIS);
                cb.showTextAligned(Element.ALIGN_LEFT, linea, izquierda + 2, y, 0);
                cb.endText();
                y -= 9f;
            }

            cb.beginText();
            cb.setFontAndSize(FontFactory.getFont(FontFactory.HELVETICA, 8).getBaseFont(), 8);
            cb.setColorFill(GRIS);
            cb.showTextAligned(Element.ALIGN_RIGHT, "Página " + writer.getPageNumber() + " de ",
                    derecha, 18, 0);
            cb.addTemplate(totalPaginas, derecha - bf.getWidthPoint("de " + writer.getPageNumber(), 8) - 2, 18);
            cb.endText();
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
                    new Phrase(String.valueOf(writer.getPageNumber()),
                            FontFactory.getFont(FontFactory.HELVETICA, 8)), 0, 0, 0);
        }
    }
}
