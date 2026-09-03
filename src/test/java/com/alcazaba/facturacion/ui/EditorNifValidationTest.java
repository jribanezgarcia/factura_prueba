package com.alcazaba.facturacion.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.alcazaba.facturacion.db.Database;
import com.alcazaba.facturacion.service.Servicios;

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

/**
 * Pruebas de UI del flujo de validacion del NIF en el editor de factura
 * (tarea 2.3 de add-spanish-tax-id-validation): un NIF no vacio e invalido
 * debe marcarse en rojo, avisar y bloquear el guardado; uno vacio o valido
 * no debe bloquear ni avisar.
 *
 * La implementacion real usa Alert.showAndWait() modal (bloqueante en el hilo
 * de JavaFX). Para evitar deadlock en los tests se sustituye Dialogos por un
 * impl grabador que no abre ventana: la logica y el estilo aplicados por el
 * controlador se verifican igualmente sobre los controles reales.
 */
class EditorNifValidationTest {

    @TempDir
    static Path dataDir;

    private static Servicios servicios;
    private static Navegador nav;
    private static Stage stage;
    private static Grabador grabador;

    private static final class Grabador implements Dialogos.Impl {
        int errores;
        int infos;

        @Override
        public void error(String titulo, String mensaje) {
            errores++;
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
        public Dialogos.CambiosSinGuardar confirmarCambiosSinGuardar() {
            // Los tests navegan entre editores: descartar para que la
            // guarda de Navegador.mostrar() no bloquee ni cancele.
            return Dialogos.CambiosSinGuardar.DESCARTAR;
        }
    }

    @BeforeAll
    static void arrancar() throws Exception {
        Database.setDataDir(dataDir);
        servicios = new Servicios();
        JavaFxTestSupport.arrancarFx();
        grabador = new Grabador();
        Dialogos.setImpl(grabador);

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> err = new AtomicReference<>();
        Platform.runLater(() -> {
            try {
                stage = new Stage();
                nav = new Navegador(stage, servicios);
                nav.mostrar("/com/alcazaba/facturacion/ui/Editor.fxml");
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
    }

    @AfterAll
    static void parar() {
        Dialogos.restoreDefault();
        Database.resetConnection();
    }

    @Test
    void nifInvalidoMuestraRojoYNoGuarda() {
        AtomicReference<Throwable> err = new AtomicReference<>();
        EditorController[] ref = new EditorController[1];
        enFx(err, () -> ref[0] = nav.mostrar("/com/alcazaba/facturacion/ui/Editor.fxml"));
        EditorController ctrl = ref[0];
        TextField cliNif = campo(err, ctrl, "cliNif");

        // "75238360A" es invalido: la letra de control correcta es R.
        enFx(err, () -> cliNif.setText("75238360A"));
        // Dispara el flujo "al pulsar Enter" (OnAction) de forma determinista.
        enFx(err, () -> cliNif.fireEvent(new ActionEvent()));

        enFx(err, () -> {
            assertTrue(cliNif.getStyle().contains("#d32f2d") || cliNif.getStyle().contains("#d32f2f"),
                    "El NIF invalido debe marcarse en rojo");
            assertTrue(grabador.errores >= 1, "Debe avisar del NIF invalido");
            assertFalse((boolean) invoke(ctrl, "guardar"),
                    "No debe guardar con NIF invalido");
            assertNull(getField(ctrl, "facturaAbiertaId"),
                    "No debe haber creado ninguna factura");
        });
    }

    @Test
    void nifVacioYValidoNoAvisanNiBloquean() {
        AtomicReference<Throwable> err = new AtomicReference<>();
        EditorController[] ref = new EditorController[1];
        enFx(err, () -> ref[0] = nav.mostrar("/com/alcazaba/facturacion/ui/Editor.fxml"));
        TextField cliNif = campo(err, ref[0], "cliNif");

        // Vacío: permitido (opcional) y válido: no avisa ni marca rojo.
        enFx(err, () -> {
            int antes = grabador.errores;
            cliNif.setText("");
            cliNif.fireEvent(new ActionEvent());
            assertFalse(cliNif.getStyle().contains("#d32f2d"),
                    "El NIF vacio no debe marcarse en rojo");
            assertEquals(antes, grabador.errores, "El NIF vacio no debe avisar");
        });

        // Válido: tampoco avisa ni marca rojo.
        enFx(err, () -> {
            int antes = grabador.errores;
            cliNif.setText("12345678Z");
            cliNif.fireEvent(new ActionEvent());
            assertFalse(cliNif.getStyle().contains("#d32f2d"),
                    "El NIF valido no debe marcarse en rojo");
            assertEquals(antes, grabador.errores, "El NIF valido no debe avisar");
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
