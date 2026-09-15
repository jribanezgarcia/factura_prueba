package cabofactu.vista.controlador;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import cabofactu.modelo.negocio.sqlite.Conexion;
import cabofactu.modelo.Modelo;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import cabofactu.vista.PruebasJavaFx;
import cabofactu.vista.Navegador;
import cabofactu.vista.utilidades.CambiosSinGuardar;
import cabofactu.vista.utilidades.Dialogos;
import cabofactu.vista.utilidades.MostradorDialogos;

/**
 * Pruebas de UI del flujo de validacion del NIF en el editor de factura:
 * al salir del campo con un NIF invalido se marca en rojo sin avisar;
 * el aviso sale con Enter o al guardar, una sola vez.
 *
 * La implementacion real usa Alert.showAndWait() modal (bloqueante en el hilo
 * de JavaFX). Para evitar deadlock en los tests se sustituye Dialogos por un
 * impl grabador que no abre ventana: la logica y el estilo aplicados por el
 * controlador se verifican igualmente sobre los controles reales.
 */
class EditorValidacionNifTest {

    @TempDir
    static Path carpetaEmpresa;

    private static Modelo modelo;
    private static Navegador nav;
    private static Stage stage;
    private static Grabador grabador;

    private static final class Grabador implements MostradorDialogos {
        int errores;
        int infos;
        String ultimoError;

        @Override
        public void error(String titulo, String mensaje) {
            errores++;
            ultimoError = mensaje;
        }

        @Override
        public void info(String titulo, String mensaje) {
            infos++;
        }

        @Override
        public boolean confirmar(String titulo, String mensaje) {
            return true;
        }

        @Override
        public CambiosSinGuardar confirmarCambiosSinGuardar() {
            // Los tests navegan entre editores: descartar para que la
            // guarda de Navegador.mostrar() no bloquee ni cancele.
            return CambiosSinGuardar.DESCARTAR;
        }
    }

    @BeforeAll
    static void arrancar() throws Exception {
        Conexion.setCarpetaRaiz(carpetaEmpresa);
        modelo = new Modelo();
        PruebasJavaFx.arrancarFx();
        grabador = new Grabador();
        Dialogos.setImpl(grabador);

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> err = new AtomicReference<>();
        Platform.runLater(() -> {
            try {
                stage = new Stage();
                nav = new Navegador(stage, modelo);
                nav.mostrar("/cabofactu/vista/recursos/Editor.fxml");
                stage.show();
            } catch (Throwable t) {
                err.set(t);
            } finally {
                latch.countDown();
            }
        });
        awaitFx(latch, err);
    }

    @BeforeEach
    void resetGrabador() {
        grabador.errores = 0;
        grabador.infos = 0;
        grabador.ultimoError = null;
    }

    @AfterAll
    static void parar() {
        Dialogos.restoreDefault();
        Conexion.cerrarConexion();
    }

    @Test
    void nifInvalidoMuestraRojoYNoGuarda() {
        AtomicReference<Throwable> err = new AtomicReference<>();
        EditorController[] ref = new EditorController[1];
        enFx(err, () -> ref[0] = nav.mostrar("/cabofactu/vista/recursos/Editor.fxml"));
        EditorController ctrl = ref[0];
        TextField cliNif = campo(err, ctrl, "cliNif");
        TextField cliNombre = campo(err, ctrl, "cliNombre");

        // "75238360A" es invalido: la letra de control correcta es R.
        enFx(err, () -> {
            cliNombre.setText("Cliente");
            cliNif.setText("75238360A");
            cliNif.requestFocus();
        });
        enFx(err, () -> cliNombre.requestFocus()); // el NIF pierde el foco
        enFx(err, () -> {
            assertFalse(cliNif.getStyle().contains("#d32f2f"),
                    "Al salir del campo no hay rojo ni aviso");
            assertEquals(0, grabador.errores, "Al salir del campo no hay aviso");
        });

        enFx(err, () -> {
            assertFalse((boolean) invoke(ctrl, "guardar"),
                    "No debe guardar con NIF invalido");
            assertEquals(1, grabador.errores, "Al guardar hay un solo aviso");
            assertEquals("La letra no es correcta.", grabador.ultimoError);
            assertTrue(cliNif.getStyle().contains("#d32f2f"),
                    "Al guardar el campo queda en rojo");
            assertNull(getField(ctrl, "facturaAbiertaId"),
                    "No debe haber creado ninguna factura");
        });
    }

    @Test
    void nifVacioAvisaAlGuardarYValidoNoAvisa() {
        AtomicReference<Throwable> err = new AtomicReference<>();
        EditorController[] ref = new EditorController[1];
        enFx(err, () -> ref[0] = nav.mostrar("/cabofactu/vista/recursos/Editor.fxml"));
        EditorController ctrl = ref[0];
        TextField cliNif = campo(err, ctrl, "cliNif");
        TextField cliNombre = campo(err, ctrl, "cliNombre");

        // Válido en un editor limpio: ni rojo ni aviso al pulsar Enter.
        enFx(err, () -> {
            int antes = grabador.errores;
            cliNif.setText("12345678Z");
            cliNif.fireEvent(new ActionEvent());
            assertFalse(cliNif.getStyle().contains("#d32f2f"),
                    "El NIF valido no debe marcarse en rojo");
            assertEquals(antes, grabador.errores, "El NIF valido no debe avisar");
        });

        // Vacío en otro editor limpio: al salir nada...
        enFx(err, () -> ref[0] = nav.mostrar("/cabofactu/vista/recursos/Editor.fxml"));
        EditorController limpio = ref[0];
        TextField nifVacio = campo(err, limpio, "cliNif");
        TextField nombreVacio = campo(err, limpio, "cliNombre");

        // Vacío con nombre: al salir nada...
        enFx(err, () -> {
            nombreVacio.setText("Cliente");
            nifVacio.setText("");
            nifVacio.requestFocus();
        });
        enFx(err, () -> nombreVacio.requestFocus()); // el NIF pierde el foco
        enFx(err, () -> {
            assertFalse(nifVacio.getStyle().contains("#d32f2f"),
                    "Al salir del campo no hay rojo ni aviso");
            assertEquals(0, grabador.errores, "Al salir del campo no hay aviso");
        });

        // ...y al guardar avisa una vez que es obligatorio.
        enFx(err, () -> {
            assertFalse((boolean) invoke(limpio, "guardar"),
                    "No debe guardar sin NIF");
            assertEquals(1, grabador.errores, "Al guardar hay un solo aviso");
            assertEquals("El NIF/NIE es obligatorio.", grabador.ultimoError);
            assertTrue(nifVacio.getStyle().contains("#d32f2f"),
                    "Al guardar el campo queda en rojo");
        });
    }

    private void enFx(AtomicReference<Throwable> err, Runnable r) {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                r.run();
            } catch (Throwable t) {
                err.set(t);
            } finally {
                latch.countDown();
            }
        });
        try {
            if (!latch.await(30, TimeUnit.SECONDS)) {
                fail("Un paso del escenario no termino en 30 s");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            fail("Interrumpido esperando un paso del escenario");
        }
        if (err.get() != null) {
            throw new AssertionError("Fallo en el escenario de NIF del editor", err.get());
        }
    }

    private static void awaitFx(CountDownLatch latch, AtomicReference<Throwable> err) {
        try {
            if (!latch.await(30, TimeUnit.SECONDS)) {
                fail("JavaFX no arranco en 30 s");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            fail("Interrumpido");
        }
        if (err.get() != null) {
            throw new RuntimeException(err.get());
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T campo(AtomicReference<Throwable> err, EditorController ctrl, String nombre) {
        try {
            Field f = EditorController.class.getDeclaredField(nombre);
            f.setAccessible(true);
            return (T) f.get(ctrl);
        } catch (Throwable t) {
            err.set(t);
            return null;
        }
    }

    private static Object invoke(EditorController ctrl, String metodo) {
        try {
            Method m = EditorController.class.getDeclaredMethod(metodo);
            m.setAccessible(true);
            return m.invoke(ctrl);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    private static Object getField(EditorController ctrl, String nombre) {
        try {
            Field f = EditorController.class.getDeclaredField(nombre);
            f.setAccessible(true);
            return f.get(ctrl);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }
}
