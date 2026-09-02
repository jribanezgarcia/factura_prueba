package com.alcazaba.facturacion.util;

import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.paint.Color;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Rellena el recuadro de un logo (StackPane) con los colores del propio logo
 * para que imagen y caja se vean como una sola pieza sea cual sea el tema.
 * Clasifica la imagen muestreando solo el marco exterior y decide entre:
 * plano (color exacto en fondo y borde), difuminado (respaldo ampliado y
 * desenfocado recortado a la caja) o transparente (no toca nada).
 */
public final class LogoMarco {

    private static final double FRACCION_MARCO = 0.06;
    private static final double ALFA_MINIMO = 0.9;
    private static final double OPACOS_MINIMOS = 0.60;
    private static final double CUBO_DOMINANTE_MINIMO = 0.60;
    private static final double DESBORDE = 60.0;
    private static final double RADIO_CLIP = 10.0;
    private static final double RADIO_DESENFOQUE = 25.0;
    private static final Object MARCA_RESPALDO = new Object();

    public enum Tipo { PLANO, DIFUMINADO, TRANSPARENTE }

    public static final class Resultado {
        public final Tipo tipo;
        public final Color color;

        private Resultado(Tipo tipo, Color color) {
            this.tipo = tipo;
            this.color = color;
        }
    }

    private LogoMarco() {
    }

    /** Clasifica una imagen y devuelve el caso junto con el color detectado. */
    public static Resultado clasificar(Image imagen) {
        if (imagen == null || imagen.getWidth() < 3 || imagen.getHeight() < 3) {
            return new Resultado(Tipo.TRANSPARENTE, null);
        }
        PixelReader pr = imagen.getPixelReader();
        if (pr == null) {
            return new Resultado(Tipo.TRANSPARENTE, null);
        }
        int w = (int) imagen.getWidth();
        int h = (int) imagen.getHeight();
        int paso = Math.max(1, Math.min(w, h) / 24);
        int bAncho = Math.max(1, (int) Math.round(w * FRACCION_MARCO));
        int bAlto = Math.max(1, (int) Math.round(h * FRACCION_MARCO));

        List<int[]> opacos = new ArrayList<>();
        var muestras = new Muestras(pr, paso, opacos);

        muestras.banda(0, bAlto, 0, w);
        muestras.banda(h - bAlto, h, 0, w);
        muestras.banda(bAlto, h - bAlto, 0, bAncho);
        muestras.banda(bAlto, h - bAlto, w - bAncho, w);

        if (opacos.isEmpty()) {
            return new Resultado(Tipo.TRANSPARENTE, null);
        }
        if (opacos.size() / (double) muestras.total() < OPACOS_MINIMOS) {
            return new Resultado(Tipo.TRANSPARENTE, null);
        }

        Map<Integer, long[]> cubos = new HashMap<>(); // clave cubo -> {count,sumR,sumG,sumB}
        for (int[] rgb : opacos) {
            int cubo = ((rgb[0] >> 3) << 10) | ((rgb[1] >> 3) << 5) | (rgb[2] >> 3);
            long[] acc = cubos.computeIfAbsent(cubo, k -> new long[4]);
            acc[0]++;
            acc[1] += rgb[0];
            acc[2] += rgb[1];
            acc[3] += rgb[2];
        }
        long[] mejor = null;
        long mejorCuenta = -1;
        for (long[] acc : cubos.values()) {
            if (acc[0] > mejorCuenta) {
                mejorCuenta = acc[0];
                mejor = acc;
            }
        }
        if (mejor != null && mejor[0] / (double) opacos.size() >= CUBO_DOMINANTE_MINIMO) {
            int r = (int) Math.round(mejor[1] / (double) mejor[0]);
            int g = (int) Math.round(mejor[2] / (double) mejor[0]);
            int b = (int) Math.round(mejor[3] / (double) mejor[0]);
            return new Resultado(Tipo.PLANO, Color.rgb(r, g, b));
        }
        return new Resultado(Tipo.DIFUMINADO, null);
    }

    private static final class Muestras {
        private final PixelReader pr;
        private final int paso;
        private final List<int[]> opacos;
        private int total;

        Muestras(PixelReader pr, int paso, List<int[]> opacos) {
            this.pr = pr;
            this.paso = paso;
            this.opacos = opacos;
        }

        int total() {
            return total;
        }

        void banda(int y0, int y1, int x0, int x1) {
            for (int y = y0; y < y1; y += paso) {
                for (int x = x0; x < x1; x += paso) {
                    total++;
                    int argb = pr.getArgb(x, y);
                    if ((argb >>> 24) / 255.0 >= ALFA_MINIMO) {
                        opacos.add(new int[]{(argb >> 16) & 0xff, (argb >> 8) & 0xff, argb & 0xff});
                    }
                }
            }
        }
    }

    /** Aplica el relleno al recuadro segun la imagen, tras limpiar lo anterior. */
    public static void aplicar(StackPane pane, Image imagen) {
        limpiar(pane);
        if (imagen == null || imagen.isError()) {
            return;
        }
        Resultado r = clasificar(imagen);
        if (r.tipo == Tipo.TRANSPARENTE) {
            return;
        }
        if (r.tipo == Tipo.PLANO) {
            aplicarPlano(pane, r.color);
        } else {
            aplicarDifuminado(pane, imagen);
        }
    }

    /** Deshace estilo inline, clip y respaldo dejando el recuadro del tema. */
    public static void limpiar(StackPane pane) {
        pane.setStyle("");
        pane.setClip(null);
        pane.getChildren().removeIf(n -> n.getUserData() == MARCA_RESPALDO);
    }

    private static void aplicarPlano(StackPane pane, Color color) {
        fijarTamano(pane);
        String hex = String.format("#%02X%02X%02X",
                (int) Math.round(color.getRed() * 255),
                (int) Math.round(color.getGreen() * 255),
                (int) Math.round(color.getBlue() * 255));
        pane.setStyle("-fx-background-color: " + hex + "; -fx-border-color: " + hex + ";");
    }

    private static void aplicarDifuminado(StackPane pane, Image imagen) {
        fijarTamano(pane);
        double ancho = pane.getWidth() > 0 ? pane.getWidth() : pane.getPrefWidth();
        double alto = pane.getHeight() > 0 ? pane.getHeight() : pane.getPrefHeight();

        ImageView respaldo = new ImageView(imagen);
        respaldo.setUserData(MARCA_RESPALDO);
        respaldo.setMouseTransparent(true);
        respaldo.setManaged(false);
        respaldo.setPreserveRatio(false);
        respaldo.setFitWidth(ancho + DESBORDE);
        respaldo.setFitHeight(alto + DESBORDE);
        respaldo.setEffect(new GaussianBlur(RADIO_DESENFOQUE));

        Rectangle clip = new Rectangle(ancho, alto);
        clip.setArcWidth(RADIO_CLIP * 2);
        clip.setArcHeight(RADIO_CLIP * 2);
        clip.widthProperty().bind(pane.widthProperty());
        clip.heightProperty().bind(pane.heightProperty());
        pane.setClip(clip);

        pane.getChildren().add(0, respaldo);
    }

    private static void fijarTamano(StackPane pane) {
        double w = pane.getPrefWidth();
        double h = pane.getPrefHeight();
        if (w <= 0 || h <= 0) {
            return;
        }
        pane.setMinSize(w, h);
        pane.setMaxSize(w, h);
    }
}
