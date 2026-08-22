package com.alcazaba.facturacion.ui;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;

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
            a.showAndWait();
        }

        @Override
        public void info(String titulo, String mensaje) {
            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setTitle(titulo);
            a.setHeaderText(null);
            a.setContentText(mensaje);
            a.showAndWait();
        }

        @Override
        public boolean confirmar(String titulo, String mensaje) {
            Alert a = new Alert(Alert.AlertType.CONFIRMATION, mensaje, ButtonType.YES, ButtonType.NO);
            a.setTitle(titulo);
            a.setHeaderText(null);
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
}
