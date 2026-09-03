package com.alcazaba.facturacion.ui;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * Dialogos comunes de la aplicacion.
 */
public final class Dialogos {

    public enum CambiosSinGuardar { GUARDAR, DESCARTAR, CANCELAR }

    public enum ModoGuardarVersion { SOBRESCRIBIR, NUEVA_VERSION, CANCELAR }

    /**
     * Implementacion de los dialogos. La implementacion por defecto muestra las
     * ventanas reales de la aplicacion. Los tests pueden sustituirla mediante
     * {@link #setImpl(Impl)} para evitar los modales bloqueantes (showAndWait)
     * del hilo de JavaFX; la llamada no altera el comportamiento del runtime.
     */
    public interface Impl {
        void error(String titulo, String mensaje);

        void info(String titulo, String mensaje);

        boolean confirmar(String titulo, String mensaje);

        CambiosSinGuardar confirmarCambiosSinGuardar();

        /**
         * Pregunta como guardar la edicion de una factura ya guardada. El
         * mapeo por defecto reutiliza confirmar para no romper las
         * implementaciones de test existentes: true sobrescribe y false
         * cancela.
         */
        default ModoGuardarVersion modoGuardarVersion() {
            return confirmar("Guardar cambios",
                    "La factura ya está guardada. ¿Desea sobrescribir la versión actual con los cambios?")
                    ? ModoGuardarVersion.SOBRESCRIBIR : ModoGuardarVersion.CANCELAR;
        }
    }

    private static final Impl IMPLEMENTACION_POR_DEFECTO = new Impl() {
        @Override
        public void error(String titulo, String mensaje) {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setTitle(titulo);
            a.setHeaderText(null);
            a.setContentText(mensaje);
            a.setGraphic(icono(Alert.AlertType.ERROR));
            aplicarTema(a.getDialogPane());
            iconoVentana(a);
            a.showAndWait();
        }

        @Override
        public void info(String titulo, String mensaje) {
            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setTitle(titulo);
            a.setHeaderText(null);
            a.setContentText(mensaje);
            a.setGraphic(icono(Alert.AlertType.INFORMATION));
            aplicarTema(a.getDialogPane());
            iconoVentana(a);
            a.showAndWait();
        }

        @Override
        public boolean confirmar(String titulo, String mensaje) {
            Alert a = new Alert(Alert.AlertType.CONFIRMATION, mensaje, ButtonType.YES, ButtonType.NO);
            a.setTitle(titulo);
            a.setHeaderText(null);
            a.setGraphic(icono(Alert.AlertType.CONFIRMATION));
            aplicarTema(a.getDialogPane());
            iconoVentana(a);
            return a.showAndWait().map(b -> b == ButtonType.YES).orElse(false);
        }

        @Override
        public CambiosSinGuardar confirmarCambiosSinGuardar() {
            Alert a = new Alert(Alert.AlertType.CONFIRMATION);
            a.setTitle("Cambios sin guardar");
            a.setHeaderText("Hay cambios sin guardar en la factura");
            a.setContentText("¿Qué desea hacer?");
            ButtonType guardar = new ButtonType("Guardar y salir", ButtonBar.ButtonData.YES);
            ButtonType descartar = new ButtonType("Descartar cambios", ButtonBar.ButtonData.NO);
            ButtonType cancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
            a.getButtonTypes().setAll(guardar, descartar, cancelar);
            a.setGraphic(icono(Alert.AlertType.CONFIRMATION));
            aplicarTema(a.getDialogPane());
            iconoVentana(a);
            a.showAndWait();
            if (a.getResult() == guardar) {
                return CambiosSinGuardar.GUARDAR;
            }
            if (a.getResult() == descartar) {
                return CambiosSinGuardar.DESCARTAR;
            }
            return CambiosSinGuardar.CANCELAR;
        }

        @Override
        public ModoGuardarVersion modoGuardarVersion() {
            Alert a = new Alert(Alert.AlertType.CONFIRMATION);
            a.setTitle("Guardar cambios");
            a.setHeaderText("La factura ya está guardada");
            a.setContentText("¿Cómo desea guardar los cambios?");
            ButtonType sobrescribir = new ButtonType("Sobrescribir versión actual", ButtonBar.ButtonData.YES);
            ButtonType nueva = new ButtonType("Guardar como nueva versión", ButtonBar.ButtonData.NO);
            ButtonType cancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
            a.getButtonTypes().setAll(sobrescribir, nueva, cancelar);
            a.setGraphic(icono(Alert.AlertType.CONFIRMATION));
            aplicarTema(a.getDialogPane());
            iconoVentana(a);
            a.showAndWait();
            if (a.getResult() == sobrescribir) {
                return ModoGuardarVersion.SOBRESCRIBIR;
            }
            if (a.getResult() == nueva) {
                return ModoGuardarVersion.NUEVA_VERSION;
            }
            return ModoGuardarVersion.CANCELAR;
        }
    };

    private static volatile Impl impl = IMPLEMENTACION_POR_DEFECTO;

    private Dialogos() {
    }

    /**
     * Sustituye la implementacion (uso exclusivo de tests). No afecta al runtime.
     */
    static void setImpl(Impl i) {
        impl = i;
    }

    /**
     * Restaura la implementacion por defecto.
     */
    static void restoreDefault() {
        impl = IMPLEMENTACION_POR_DEFECTO;
    }

    public static void error(String titulo, String mensaje) {
        impl.error(titulo, mensaje);
    }

    public static void info(String titulo, String mensaje) {
        impl.info(titulo, mensaje);
    }

    public static boolean confirmar(String titulo, String mensaje) {
        return impl.confirmar(titulo, mensaje);
    }

    public static CambiosSinGuardar confirmarCambiosSinGuardar() {
        return impl.confirmarCambiosSinGuardar();
    }

    public static ModoGuardarVersion modoGuardarVersion() {
        return impl.modoGuardarVersion();
    }

    /**
     * Viste un {@link DialogPane} con el tema activo y la clase de tarjeta,
     * de modo que los dialogos no salgan con el gris por defecto de JavaFX.
     */
    public static void aplicarTema(DialogPane pane) {
        pane.getStyleClass().add("dialog-card");
        pane.getStylesheets().setAll(ThemeManager.hojas());
    }

    /**
     * Glifo del icono de aviso segun el tipo del dialogo, coloreado con el
     * acento del tema activo mediante la clase CSS "dialog-icon".
     */
    private static SVGPath icono(Alert.AlertType tipo) {
        SVGPath p = new SVGPath();
        p.getStyleClass().add("dialog-icon");
        if (tipo == Alert.AlertType.ERROR) {
            p.setContent("M1 21h22L12 2 1 21zm12-3h-2v-2h2v2zm0-4h-2v-4h2v4z");
        } else if (tipo == Alert.AlertType.CONFIRMATION) {
            p.setContent("M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 17h-2v-2h2v2zm2.07-7.75l-.9.92C13.45 12.9 13 13.5 13 15h-2v-.5c0-1.1.45-2.1 1.17-2.83l1.24-1.26c.37-.36.59-.86.59-1.41 0-1.1-.9-2-2-2s-2 .9-2 2H8c0-2.21 1.79-4 4-4s4 1.79 4 4c0 .88-.36 1.68-.93 2.25z");
        } else {
            p.setContent("M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-6h2v6zm0-8h-2V7h2v2z");
        }
        return p;
    }

    /**
     * Aplica el icono de aplicacion a la ventana propia del dialogo una vez
     * mostrado, reutilizando {@link Ventanas#aplicarIcono(Stage)} (idempotente
     * y silencioso si falta el recurso).
     */
    private static void iconoVentana(Alert a) {
        a.setOnShown(e -> {
            Window w = a.getDialogPane().getScene().getWindow();
            if (w instanceof Stage s) {
                Ventanas.aplicarIcono(s);
            }
        });
    }
}
