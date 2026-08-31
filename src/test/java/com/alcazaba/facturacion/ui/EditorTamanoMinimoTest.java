package com.alcazaba.facturacion.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.alcazaba.facturacion.db.Database;
import com.alcazaba.facturacion.service.Servicios;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Verifica que el Editor de facturas cabe completo en su tamaño minimo
 * predefinido y que los elementos principales son visibles.
 */
class EditorTamanoMinimoTest {

    @TempDir
    static Path dataDir;

    @BeforeAll
    static void arrancar() throws Exception {
        Database.setDataDir(dataDir);
        JavaFxTestSupport.arrancarFx();
    }

    @AfterEach
    void cerrarDb() {
        Database.resetConnection();
    }

    @Test
    void editorCabeEnTamanoMinimo() throws Exception {
        Servicios servicios = new Servicios();
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> error = new AtomicReference<>();
        AtomicReference<Stage> stageRef = new AtomicReference<>();

        Platform.runLater(() -> {
            try {
                Stage stage = new Stage();
                Navegador nav = new Navegador(stage, servicios);
                nav.mostrar("/com/alcazaba/facturacion/ui/Editor.fxml");
                stage.show();
                stageRef.set(stage);
            } catch (Throwable t) {
                error.set(t);
            } finally {
                latch.countDown();
            }
        });

        if (!latch.await(30, TimeUnit.SECONDS)) {
            fail("JavaFX no cargo el Editor en 30 s");
        }
        if (error.get() != null) {
            throw new RuntimeException(error.get());
        }

        Stage stage = stageRef.get();
        // El Editor se abre maximizado; se desmaximiza para verificar el tamaño minimo/restaurado.
        if (stage.isMaximized()) {
            CountDownLatch unmaxLatch = new CountDownLatch(1);
            Platform.runLater(() -> {
                stage.setMaximized(false);
                unmaxLatch.countDown();
            });
            unmaxLatch.await(5, TimeUnit.SECONDS);
        }
        assertEquals(1000.0, stage.getMinWidth(), 0.01, "El ancho minimo del Editor debe ser 1000");
        assertEquals(760.0, stage.getMinHeight(), 0.01, "El alto minimo del Editor debe ser 760");
        assertEquals(1000.0, stage.getWidth(), 0.01, "El ancho restaurado del Editor debe ser 1000");
        assertEquals(760.0, stage.getHeight(), 0.01, "El alto restaurado del Editor debe ser 760");

        CountDownLatch layoutLatch = new CountDownLatch(1);
        AtomicReference<Throwable> layoutError = new AtomicReference<>();
        Platform.runLater(() -> {
            try {
                Parent root = (Parent) stage.getScene().getRoot();
                root.applyCss();
                root.layout();

                Node nav = root.lookup(".nav-bar");
                assertNotNull(nav, "La barra de navegacion debe existir");
                assertTrue(nav.isVisible(), "La barra de navegacion debe ser visible");

                Node tabla = root.lookup("#tablaLineas");
                assertNotNull(tabla, "La tabla de lineas debe existir");
                assertTrue(tabla.isVisible(), "La tabla de lineas debe ser visible");

                Bounds navBounds = nav.localToScene(nav.getBoundsInLocal());
                Bounds tablaBounds = tabla.localToScene(tabla.getBoundsInLocal());
                assertTrue(navBounds.getMinY() >= 0,
                        "La barra de navegacion no debe quedar fuera por arriba");
                assertTrue(tablaBounds.getMaxY() <= stage.getHeight(),
                        "La tabla no debe salirse por debajo de la ventana");
            } catch (Throwable t) {
                layoutError.set(t);
            } finally {
                layoutLatch.countDown();
            }
        });

        if (!layoutLatch.await(30, TimeUnit.SECONDS)) {
            fail("El layout del Editor no termino en 30 s");
        }
        if (layoutError.get() != null) {
            throw new AssertionError("Fallo la comprobacion de layout del Editor", layoutError.get());
        }
    }
}
