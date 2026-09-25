package cabofactu.vista.utilidades;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;
import javafx.stage.Window;
import cabofactu.vista.Ventanas;

/**
 * Dialogos comunes de la aplicacion.
 */
public final class Dialogos {

    private Dialogos() {
    }

    public static void mostrarDialogoError(String titulo, String contenido) {
        Alert aviso = new Alert(Alert.AlertType.ERROR);
        aviso.setTitle(titulo);
        aviso.setHeaderText(null);
        ponerMensaje(aviso, contenido);
        aviso.setGraphic(icono(Alert.AlertType.ERROR));
        aplicarTema(aviso.getDialogPane());
        iconoVentana(aviso);
        aviso.showAndWait();
    }

    public static void mostrarDialogoInformacion(String titulo, String contenido) {
        Alert aviso = new Alert(Alert.AlertType.INFORMATION);
        aviso.setTitle(titulo);
        aviso.setHeaderText(null);
        ponerMensaje(aviso, contenido);
        aviso.setGraphic(icono(Alert.AlertType.INFORMATION));
        aplicarTema(aviso.getDialogPane());
        iconoVentana(aviso);
        aviso.showAndWait();
    }

    public static void mostrarDialogoAdvertencia(String titulo, String contenido) {
        Alert aviso = new Alert(Alert.AlertType.WARNING);
        aviso.setTitle(titulo);
        aviso.setHeaderText(null);
        ponerMensaje(aviso, contenido);
        aviso.setGraphic(icono(Alert.AlertType.WARNING));
        aplicarTema(aviso.getDialogPane());
        iconoVentana(aviso);
        aviso.showAndWait();
    }

    public static boolean mostrarDialogoConfirmacion(String titulo, String contenido) {
        ButtonType aceptar = new ButtonType("Aceptar", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        Alert aviso = new Alert(Alert.AlertType.CONFIRMATION, "", aceptar, cancelar);
        aviso.setTitle(titulo);
        aviso.setHeaderText(null);
        ponerMensaje(aviso, contenido);
        aviso.setGraphic(icono(Alert.AlertType.CONFIRMATION));
        aplicarTema(aviso.getDialogPane());
        iconoVentana(aviso);
        aviso.showAndWait();
        return aviso.getResult() == aceptar;
    }

    public static CambiosSinGuardar mostrarDialogoCambiosSinGuardar() {
        Alert aviso = new Alert(Alert.AlertType.CONFIRMATION);
        aviso.setTitle("Cambios sin guardar");
        aviso.setHeaderText("Hay cambios sin guardar en la factura");
        aviso.setContentText("¿Qué desea hacer?");
        ButtonType guardar = new ButtonType("Guardar y salir", ButtonBar.ButtonData.YES);
        ButtonType descartar = new ButtonType("Descartar cambios", ButtonBar.ButtonData.NO);
        ButtonType cancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        aviso.getButtonTypes().setAll(guardar, descartar, cancelar);
        aviso.setGraphic(icono(Alert.AlertType.CONFIRMATION));
        aplicarTema(aviso.getDialogPane());
        iconoVentana(aviso);
        aviso.showAndWait();
        if (aviso.getResult() == guardar) {
            return CambiosSinGuardar.GUARDAR;
        }
        if (aviso.getResult() == descartar) {
            return CambiosSinGuardar.DESCARTAR;
        }
        return CambiosSinGuardar.CANCELAR;
    }

    /**
     * Ponemos el mensaje en una etiqueta propia en vez de setContentText,
     * porque JavaFX calcula el alto del aviso sin contar el margen de la
     * tarjeta y los mensajes de algo más de una línea se cortan con «...».
     */
    static void ponerMensaje(Alert alerta, String mensaje) {
        Label texto = new Label(mensaje);
        texto.setWrapText(true);
        texto.setMinHeight(Region.USE_PREF_SIZE);
        alerta.getDialogPane().setContent(texto);
    }

    /**
     * Viste un {@link DialogPane} con el tema activo y la clase de tarjeta,
     * de modo que los dialogos no salgan con el gris por defecto de JavaFX.
     */
    public static void aplicarTema(DialogPane pane) {
        pane.getStyleClass().add("dialog-card");
        pane.getStylesheets().setAll(GestorTemas.hojas());
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
    private static void iconoVentana(Alert aviso) {
        aviso.setOnShown(e -> ponerIconoVentana(aviso));
    }

    private static void ponerIconoVentana(Alert aviso) {
        Window ventana = aviso.getDialogPane().getScene().getWindow();
        if (ventana instanceof Stage) {
            Ventanas.aplicarIcono((Stage) ventana);
        }
    }
}
