package cabofactu.vista.utilidades;

import javafx.scene.control.Alert;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;

/**
 * Dialogos comunes de la aplicacion.
 */
public final class Dialogos {

    private static MostradorDialogos mostrador = new DialogosReales();

    private Dialogos() {
    }

    /**
     * Sustituye la implementacion (uso exclusivo de tests). No afecta al runtime.
     */
    public static void setImpl(MostradorDialogos m) {
        mostrador = m;
    }

    /**
     * Restaura la implementacion por defecto.
     */
    public static void restoreDefault() {
        mostrador = new DialogosReales();
    }

    public static void error(String titulo, String mensaje) {
        mostrador.error(titulo, mensaje);
    }

    public static void info(String titulo, String mensaje) {
        mostrador.info(titulo, mensaje);
    }

    public static boolean confirmar(String titulo, String mensaje) {
        return mostrador.confirmar(titulo, mensaje);
    }

    public static CambiosSinGuardar confirmarCambiosSinGuardar() {
        return mostrador.confirmarCambiosSinGuardar();
    }

    public static ModoGuardarVersion modoGuardarVersion() {
        return mostrador.modoGuardarVersion();
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
}
