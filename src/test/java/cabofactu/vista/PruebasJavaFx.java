package cabofactu.vista;

import cabofactu.controlador.Controlador;
import cabofactu.modelo.Modelo;
import javafx.stage.Stage;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;

/**
 * Arranca el toolkit JavaFX una unica vez por JVM, de modo que varias clases
 * de test pueden usarlo sin colisionar (Platform.startup solo puede llamarse
 * una vez). No se ejecuta Platform.exit; las ventanas se ocultan y el toolkit
 * se mantiene vivo hasta que el JVM de surefire termina.
 */
public final class PruebasJavaFx {

    private static boolean arrancado;

    private PruebasJavaFx() {
    }

    public static synchronized void arrancarFx() throws Exception {
        if (arrancado) {
            return;
        }
        try {
            CountDownLatch l = new CountDownLatch(1);
            Platform.startup(() -> l.countDown());
            if (!l.await(30, TimeUnit.SECONDS)) {
                throw new IllegalStateException("El toolkit JavaFX no arranco en 30 s");
            }
        } catch (IllegalStateException e) {
            // Ya arrancado por otra clase de test en el mismo JVM
        }
        Platform.setImplicitExit(false);
        arrancado = true;
    }

    /** Preparamos la Vista como al arrancar: con su controlador, el modelo y una ventana. */
    public static void prepararVista(Modelo modelo, Stage ventana) {
        new Controlador(modelo, Vista.getInstancia());
        Vista.getInstancia().setVentana(ventana);
    }
}
