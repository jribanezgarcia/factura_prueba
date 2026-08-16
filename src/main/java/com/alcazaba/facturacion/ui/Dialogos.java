package com.alcazaba.facturacion.ui;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;

/**
 * Dialogos comunes de la aplicacion.
 */
public final class Dialogos {

    public enum CambiosSinGuardar { GUARDAR, DESCARTAR, CANCELAR }

    private Dialogos() {
    }

    public static void error(String titulo, String mensaje) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(titulo);
        a.setHeaderText(null);
        a.setContentText(mensaje);
        a.showAndWait();
    }

    public static void info(String titulo, String mensaje) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(titulo);
        a.setHeaderText(null);
        a.setContentText(mensaje);
        a.showAndWait();
    }

    public static boolean confirmar(String titulo, String mensaje) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, mensaje, ButtonType.YES, ButtonType.NO);
        a.setTitle(titulo);
        a.setHeaderText(null);
        return a.showAndWait().map(b -> b == ButtonType.YES).orElse(false);
    }

    public static CambiosSinGuardar confirmarCambiosSinGuardar() {
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
}
