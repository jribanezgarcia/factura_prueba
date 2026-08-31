package com.alcazaba.facturacion.ui;

import javafx.animation.ScaleTransition;
import javafx.scene.Node;
import javafx.util.Duration;

public final class Microinteracciones {

    private static final double ESCALA_HOVER = 1.02;
    private static final Duration DURACION = Duration.millis(100);

    private Microinteracciones() {
    }

    public static void escalaSuave(Node nodo) {
        nodo.setOnMouseEntered(e -> animar(nodo, ESCALA_HOVER));
        nodo.setOnMouseExited(e -> animar(nodo, 1.0));
    }

    private static void animar(Node nodo, double escala) {
        ScaleTransition t = new ScaleTransition(DURACION, nodo);
        t.setToX(escala);
        t.setToY(escala);
        t.play();
    }
}
