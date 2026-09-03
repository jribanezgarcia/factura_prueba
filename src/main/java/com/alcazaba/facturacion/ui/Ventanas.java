package com.alcazaba.facturacion.ui;

import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * Utilidades de identidad visual de la aplicacion: el prefijo de marca del
 * titulo y el icono propio de las ventanas.
 */
public final class Ventanas {

    /** Prefijo de marca del titulo de todas las ventanas. */
    public static final String PREFIJO = "CaboFactu\u00AE ";

    private static final String ICONO = "/com/alcazaba/facturacion/images/icono-aplicacion.png";

    private Ventanas() {
    }

    /** Aplica el icono de la aplicacion a una ventana (una sola vez). Silencioso si falta el recurso. */
    public static void aplicarIcono(Stage stage) {
        if (!stage.getIcons().isEmpty()) {
            return;
        }
        try (var in = Ventanas.class.getResourceAsStream(ICONO)) {
            if (in != null) {
                stage.getIcons().add(new Image(in));
            }
        } catch (Exception ignored) {
        }
    }
}
