package com.alcazaba.facturacion.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Verifica que la barra de acciones del Editor no oculta ningun boton
 * cuando btnAnular es visible y el titulo es largo.
 */
class EditorBarraAccionesTest {

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
    void barraNoDesbordaConAnularVisibleYTituloLargo() throws Exception {
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
        assertEquals(1024.0, stage.getWidth(), 0.01);
        assertEquals(768.0, stage.getHeight(), 0.01);

        CountDownLatch layoutLatch = new CountDownLatch(1);
        AtomicReference<Throwable> layoutError = new AtomicReference<>();
        Platform.runLater(() -> {
            try {
                Parent root = (Parent) stage.getScene().getRoot();

                Button btnAnular = (Button) root.lookup("#btnAnular");
                assertNotNull(btnAnular, "btnAnular debe existir");
                btnAnular.setVisible(true);
                btnAnular.setManaged(true);

                Label lblTitulo = (Label) root.lookup("#lblTitulo");
                assertNotNull(lblTitulo, "lblTitulo debe existir");
                lblTitulo.setText("Factura R-12/2026 (v3)");

                root.applyCss();
                root.layout();

                double anchoEscena = stage.getScene().getWidth();

                Node overflow = root.lookup(".tool-bar-overflow-button");
                assertFalse(overflow != null && overflow.isVisible(),
                        "No debe aparecer el boton de desbordamiento de ToolBar");

                Node actionBarNode = root.lookup(".action-bar");
                assertNotNull(actionBarNode, "La action-bar debe existir");
                HBox actionBar = (HBox) actionBarNode;
                for (Node child : actionBar.getChildren()) {
                    if (child instanceof Button) {
                        Bounds bounds = child.localToScene(child.getBoundsInLocal());
                        assertFalse(bounds.isEmpty(),
                                "El boton " + ((Button) child).getText()
                                        + " no debe tener bounds vacios");
                    }
                }
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
            throw new AssertionError("Fallo la comprobacion de la barra de acciones",
                    layoutError.get());
        }
    }

    @Test
    void barraNoDesbordaConFacturaAnuladaYTituloLargo() throws Exception {
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
        assertEquals(1024.0, stage.getWidth(), 0.01);
        assertEquals(768.0, stage.getHeight(), 0.01);

        CountDownLatch layoutLatch = new CountDownLatch(1);
        AtomicReference<Throwable> layoutError = new AtomicReference<>();
        Platform.runLater(() -> {
            try {
                Parent root = (Parent) stage.getScene().getRoot();

                Button btnAnular = (Button) root.lookup("#btnAnular");
                assertNotNull(btnAnular, "btnAnular debe existir");
                btnAnular.setVisible(false);
                btnAnular.setManaged(false);

                Button btnRestaurar = (Button) root.lookup("#btnRestaurar");
                assertNotNull(btnRestaurar, "btnRestaurar debe existir");
                btnRestaurar.setVisible(true);
                btnRestaurar.setManaged(true);

                Label lblEstado = (Label) root.lookup("#lblEstado");
                assertNotNull(lblEstado, "lblEstado debe existir");
                lblEstado.setVisible(true);
                lblEstado.setManaged(true);

                Label lblTitulo = (Label) root.lookup("#lblTitulo");
                assertNotNull(lblTitulo, "lblTitulo debe existir");
                lblTitulo.setText("Factura R-12/2026 (v3)");

                root.applyCss();
                root.layout();

                Node overflow = root.lookup(".tool-bar-overflow-button");
                assertFalse(overflow != null && overflow.isVisible(),
                        "No debe aparecer el boton de desbordamiento de ToolBar"
                                + " con factura anulada y titulo largo");

                Node actionBarNode = root.lookup(".action-bar");
                assertNotNull(actionBarNode, "La action-bar debe existir");
                HBox actionBar = (HBox) actionBarNode;
                for (Node child : actionBar.getChildren()) {
                    if (child instanceof Button) {
                        Bounds bounds = child.localToScene(child.getBoundsInLocal());
                        assertFalse(bounds.isEmpty(),
                                "El boton " + ((Button) child).getText()
                                        + " no debe tener bounds vacios");
                    }
                }
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
            throw new AssertionError(
                    "Fallo la comprobacion de la barra con factura anulada",
                    layoutError.get());
        }
    }
}
