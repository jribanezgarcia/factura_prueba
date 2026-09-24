package cabofactu.vista;

import cabofactu.controlador.Controlador;
import cabofactu.modelo.Modelo;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.testfx.api.FxToolkit;
import org.testfx.util.WaitForAsyncUtils;

/**
 * Arranca el toolkit JavaFX una unica vez por JVM, con el mismo arranque que
 * usa TestFX, de modo que las pruebas de pantalla y las que solo cargan FXML
 * conviven sin pisarse. No se ejecuta Platform.exit; las ventanas se ocultan
 * y el toolkit se mantiene vivo hasta que el JVM de surefire termina.
 */
public final class PruebasJavaFx {

    private static boolean arrancado;

    private PruebasJavaFx() {
    }

    public static synchronized void arrancarFx() throws Exception {
        if (arrancado) {
            return;
        }
        FxToolkit.registerPrimaryStage();
        Platform.setImplicitExit(false);
        WaitForAsyncUtils.printException = true;
        arrancado = true;
    }

    /** Preparamos la Vista como al arrancar: con su controlador, el modelo y una ventana. */
    public static void prepararVista(Modelo modelo, Stage ventana) {
        new Controlador(modelo, Vista.getInstancia());
        Vista.getInstancia().setVentana(ventana);
    }
}
