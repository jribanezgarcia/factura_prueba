package cabofactu.vista.utilidades;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import cabofactu.vista.PruebasJavaFx;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.text.Text;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/** Comprobamos que los avisos muestran el mensaje entero, sin cortes. */
class DialogosTextoCompletoTest {

    @BeforeAll
    static void arrancar() throws Exception {
        PruebasJavaFx.arrancarFx();
    }

    @Test
    void mensajeDeCodigoPostalSaleEntero() {
        comprobarTextoCompleto("El código postal debe tener cinco dígitos y comenzar entre 01 y 52.");
    }

    @Test
    void mensajeDeCambiosSinGuardarSaleEntero() {
        comprobarTextoCompleto("Hay cambios sin guardar que se descartarán. ¿Desea continuar ya?");
    }

    private void comprobarTextoCompleto(String mensaje) {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> error = new AtomicReference<>();
        AtomicReference<String> visto = new AtomicReference<>("");
        Platform.runLater(() -> mostrarAviso(mensaje, visto, error, latch));
        try {
            if (!latch.await(30, TimeUnit.SECONDS)) {
                fail("El aviso no termino de mostrarse en 30 s");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            fail("Interrumpido esperando el aviso");
        }
        if (error.get() != null) {
            throw new AssertionError("Fallo comprobando el texto del aviso", error.get());
        }
        assertEquals(mensaje, visto.get());
    }

    private static void mostrarAviso(String mensaje, AtomicReference<String> visto,
            AtomicReference<Throwable> error, CountDownLatch latch) {
        Alert aviso = new Alert(Alert.AlertType.ERROR);
        try {
            aviso.setTitle("Prueba");
            aviso.setHeaderText(null);
            Dialogos.ponerMensaje(aviso, mensaje);
            Dialogos.aplicarTema(aviso.getDialogPane());
            aviso.show();
            aviso.getDialogPane().applyCss();
            aviso.getDialogPane().layout();
            Label etiqueta = (Label) aviso.getDialogPane().getContent();
            StringBuilder junta = new StringBuilder();
            for (Node nodo : etiqueta.lookupAll(".text")) {
                junta.append(((Text) nodo).getText());
            }
            visto.set(junta.toString());
        } catch (Throwable t) {
            error.set(t);
        } finally {
            aviso.hide();
            latch.countDown();
        }
    }
}
