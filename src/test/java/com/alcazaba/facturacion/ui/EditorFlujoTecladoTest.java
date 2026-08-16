package com.alcazaba.facturacion.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.alcazaba.facturacion.db.Database;
import com.alcazaba.facturacion.model.LineaFactura;
import com.alcazaba.facturacion.service.Servicios;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Reproduce el flujo de teclado del editor: Enter en la descripcion debe
 * mover la edicion a la columna Precio y el foco debe permanecer dentro de
 * la tabla (nunca saltar al boton Guardar).
 */
class EditorFlujoTecladoTest {

    @TempDir
    static Path dataDir;

    private static Servicios servicios;
    private static Navegador nav;
    private static Stage stage;

    @BeforeAll
    static void arrancar() throws Exception {
        Database.setDataDir(dataDir);
        servicios = new Servicios();
        JavaFxTestSupport.arrancarFx();

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> error = new AtomicReference<>();
        Platform.runLater(() -> {
            try {
                stage = new Stage();
                nav = new Navegador(stage, servicios);
            } catch (Throwable t) {
                error.set(t);
            } finally {
                latch.countDown();
            }
        });
        if (!latch.await(30, TimeUnit.SECONDS)) {
            fail("JavaFX no arranco en 30 s");
        }
        if (error.get() != null) {
            throw new RuntimeException(error.get());
        }
    }

    @AfterAll
    static void parar() {
        Database.resetConnection();
    }

    @Test
    void enterEnDescripcionVaAPrecioConFocoEnLaTabla() {
        AtomicReference<Throwable> err = new AtomicReference<>();
        EditorController[] ctrlRef = new EditorController[1];

        enFx(err, () -> {
            ctrlRef[0] = nav.mostrar("/com/alcazaba/facturacion/ui/Editor.fxml");
            stage.show();
            stage.requestFocus();
        });
        enFx(err, () -> {
        });
        enFx(err, () -> {
        });

        EditorController ctrl = ctrlRef[0];
        @SuppressWarnings("unchecked")
        TableView<LineaFactura> tabla = campo(err, ctrl, "tablaLineas");
        @SuppressWarnings("unchecked")
        TableColumn<LineaFactura, ?> colDesc = campo(err, ctrl, "colDescripcion");
        @SuppressWarnings("unchecked")
        TableColumn<LineaFactura, ?> colPrecio = campo(err, ctrl, "colPrecio");
        Button btnGuardar = campo(err, ctrl, "btnGuardar");

        enFx(err, () -> {
            if (tabla.getItems().isEmpty()) {
                tabla.getItems().add(new LineaFactura());
            }
        });
        enFx(err, () -> {
            btnGuardar.requestFocus();
        });
        enFx(err, () -> {
        });
        enFx(err, () -> tabla.edit(0, colDesc));
        enFx(err, () -> {
        });
        enFx(err, () -> {
        });

        enFx(err, () -> {
            TextField editor = (TextField) tabla.lookup(".text-field");
            assertNotNull(editor, "No se encontro el editor de la celda de descripcion");
            editor.setText("Puerta de roble");
            editor.requestFocus();
        });
        enFx(err, () -> {
        });

        enFx(err, () -> {
            TextField editor = (TextField) tabla.lookup(".text-field");
            assertNotNull(editor, "No se encontro el editor antes de Enter");
            editor.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "",
                    KeyCode.ENTER, false, false, false, false));
        });
        enFx(err, () -> {
        });
        enFx(err, () -> {
        });

        enFx(err, () -> {
            TablePosition<LineaFactura, ?> celda = tabla.getEditingCell();
            assertNotNull(celda, "Tras Enter en Descripcion no hay celda en edicion");
            assertEquals(0, celda.getRow(), "La edicion debe continuar en la fila 0");
            assertEquals(colPrecio, celda.getTableColumn(),
                    "El Enter en Descripcion debe mover la edicion a la columna Precio");

            Node foco = stage.getScene().getFocusOwner();
            assertNotNull(foco, "Tras Enter en Descripcion no hay foco");
            assertNotSame(btnGuardar, foco, "El foco no debe saltar al boton Guardar");
            assertTrue(foco instanceof TextField,
                    "El foco debe estar en el editor de una celda, no en " + foco.getClass().getSimpleName());
            assertTrue(esDescendienteDe(foco, tabla),
                    "El foco debe permanecer dentro de la tabla de lineas");
        });
    }

    private void enFx(AtomicReference<Throwable> err, Runnable r) {
        CountDownLatch l = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                r.run();
            } catch (Throwable t) {
                err.set(t);
            } finally {
                l.countDown();
            }
        });
        try {
            if (!l.await(30, TimeUnit.SECONDS)) {
                fail("Un paso del escenario no termino en 30 s");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            fail("Interrumpido esperando un paso del escenario");
        }
        if (err.get() != null) {
            throw new AssertionError("Fallo en el escenario de teclado", err.get());
        }
    }

    private boolean esDescendienteDe(Node n, Node ancestro) {
        Node p = n;
        while (p != null) {
            if (p == ancestro) {
                return true;
            }
            p = p.getParent();
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private static <T> T campo(AtomicReference<Throwable> err, EditorController ctrl, String nombre) {
        try {
            Field f = ctrl.getClass().getDeclaredField(nombre);
            f.setAccessible(true);
            return (T) f.get(ctrl);
        } catch (Throwable t) {
            err.set(t);
            return null;
        }
    }
}
