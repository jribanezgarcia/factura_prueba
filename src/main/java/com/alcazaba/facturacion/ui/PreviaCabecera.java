package com.alcazaba.facturacion.ui;

import com.alcazaba.facturacion.model.Empresa;
import com.alcazaba.facturacion.pdf.CabeceraLayout;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.io.File;

/**
 * Previsualizacion aproximada de la cabecera del PDF. Usa la geometria de
 * {@link CabeceraLayout}, la misma que emplea la generacion real, y se pinta a
 * escala sobre una hoja A4 recortada a la banda superior.
 */
public class PreviaCabecera extends Pane {

    private static final Color GRIS = Color.rgb(120, 120, 120);
    private static final Color BORDE = Color.rgb(205, 205, 205);

    private Empresa empresa = new Empresa();
    private Color acento = Color.web("#296796");
    private Image logo;

    public PreviaCabecera() {
        setPrefSize(260, 200);
        setMinSize(0, 0);
        widthProperty().addListener((o, a, b) -> repintar());
        heightProperty().addListener((o, a, b) -> repintar());
    }

    public void mostrar(Empresa empresa, Color acento) {
        this.empresa = empresa != null ? empresa : new Empresa();
        this.acento = acento != null ? acento : Color.web("#296796");
        this.logo = cargarLogo(this.empresa);
        repintar();
    }

    public void repintar() {
        getChildren().clear();
        double w = getWidth() > 0 ? getWidth() : getPrefWidth();
        double h = getHeight() > 0 ? getHeight() : getPrefHeight();
        if (w <= 0 || h <= 0) {
            return;
        }
        double s = w / CabeceraLayout.ANCHO_A4;
        double altoPagina = Math.min(842 * s, h);

        Rectangle pagina = new Rectangle(0, 0, w, altoPagina);
        pagina.setFill(Color.WHITE);
        pagina.setStroke(BORDE);
        pagina.setStrokeWidth(1);
        getChildren().add(pagina);

        Rectangle banda = new Rectangle(0, 0, w, Math.max(4, 5 * s));
        banda.setFill(acento);
        getChildren().add(banda);

        double izquierda = CabeceraLayout.MARGEN_LATERAL * s;
        double derecha = (CabeceraLayout.ANCHO_A4 - CabeceraLayout.MARGEN_LATERAL) * s;

        double margenSuperior;
        if (logo != null) {
            double anchoEfectivo = CabeceraLayout.ANCHO_LOGO_FIJO * s;
            double altoEfectivo = CabeceraLayout.ALTO_LOGO_FIJO * s;
            int lineas = CabeceraLayout.lineasEmpresa(empresa).size();
            margenSuperior = CabeceraLayout.altoCabeceraLogo(empresa, lineas) * s;

            ImageView img = new ImageView(logo);
            double fx = anchoEfectivo / Math.max(logo.getWidth(), 1);
            double fy = altoEfectivo / Math.max(logo.getHeight(), 1);
            double factor = Math.min(fx, fy);
            img.setFitWidth(logo.getWidth() * factor);
            img.setFitHeight(logo.getHeight() * factor);
            img.setX(Math.max(izquierda, 2));
            img.setY(Math.max(CabeceraLayout.HUECO_LOGO_SUPERIOR * s, 6));
            getChildren().add(img);

            dibujarBloqueTexto(izquierda + CabeceraLayout.ANCHO_LOGO_FIJO * s + 14 * s, 13 * s, s);
        } else {
            int lineas = CabeceraLayout.lineasEmpresa(empresa).size();
            margenSuperior = CabeceraLayout.altoCabeceraTexto(lineas) * s;
            dibujarBloqueTexto(izquierda, 15 * s, s);
        }

        Line separador = new Line(izquierda, margenSuperior, derecha, margenSuperior);
        separador.setStroke(colorBorde());
        separador.setStrokeWidth(1);
        getChildren().add(separador);

        Line iz = new Line(izquierda, 6, izquierda, altoPagina - 6);
        decorarGuia(iz);
        getChildren().add(iz);
        Line der = new Line(derecha, 6, derecha, altoPagina - 6);
        decorarGuia(der);
        getChildren().add(der);
    }

    private void dibujarBloqueTexto(double x, double tamNombrePt, double s) {
        Color oscuro = oscuro();
        Color gris = GRIS;
        String nombre = nz(empresa.getNombre());
        if (!nombre.isBlank()) {
            getChildren().add(texto(nombre, x, 34 * s, tamNombrePt * s, oscuro, true));
        }
        double y = (34 + tamNombrePt + 1) * s;
        for (CabeceraLayout.LineaCabecera linea : CabeceraLayout.lineasEmpresa(empresa)) {
            if (linea.chipNif) {
                dibujarChipNif(x, y / s, s);
            } else {
                getChildren().add(texto(linea.texto, x, y, 9 * s, gris, false));
            }
            y += 13 * s;
        }
    }

    private void dibujarChipNif(double x, double yPt, double s) {
        String txt = "NIF: " + nz(empresa.getNif());
        Text medida = texto(txt, 0, 0, 9 * s, Color.BLACK, true);
        double ancho = medida.getBoundsInLocal().getWidth();
        double lx = x - 4 * s;
        double ly = yPt * s - 3.5 * s;
        double lw = ancho + 10 * s;
        double lh = 12.5 * s;
        Rectangle fondo = new Rectangle(lx, ly, lw, lh);
        fondo.setArcWidth(4 * s);
        fondo.setArcHeight(4 * s);
        fondo.setFill(claro());
        fondo.setStroke(colorBorde());
        getChildren().add(fondo);
        getChildren().add(texto(txt, x + s, yPt * s, 9 * s, oscuro(), true));
    }

    private Text texto(String contenido, double x, double y, double tamano, Color color, boolean negrita) {
        Text t = new Text(contenido);
        t.setFont(Font.font("System", negrita ? FontWeight.BOLD : FontWeight.NORMAL, tamano));
        t.setFill(color);
        t.setX(x);
        t.setY(y);
        return t;
    }

    private void decorarGuia(Line linea) {
        linea.setStroke(BORDE);
        linea.setStrokeWidth(0.8);
        linea.getStrokeDashArray().addAll(4d, 4d);
    }

    private Image cargarLogo(Empresa e) {
        if (e == null || !"LOGO".equalsIgnoreCase(e.getCabeceraModo())
                || e.getLogoPath() == null || e.getLogoPath().isBlank()) {
            return null;
        }
        try {
            return new Image(new File(e.getLogoPath()).toURI().toString(), false);
        } catch (Exception ex) {
            return null;
        }
    }

    private Color oscuro() {
        return acento.deriveColor(0, 1, 0.55, 1);
    }

    private Color claro() {
        return acento.deriveColor(0, 0.7, 1.6, 1);
    }

    private Color colorBorde() {
        return acento.deriveColor(0, 0.6, 0.8, 1);
    }

    private String nz(String s) {
        return s == null ? "" : s;
    }
}