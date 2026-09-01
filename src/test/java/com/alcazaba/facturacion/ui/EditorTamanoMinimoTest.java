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
        assertEquals(1024.0, stage.getMinWidth(), 0.01, "El ancho minimo del Editor debe ser 1024");
        assertEquals(768.0, stage.getMinHeight(), 0.01, "El alto minimo del Editor debe ser 768");
        assertEquals(1024.0, stage.getWidth(), 0.01, "El ancho inicial del Editor debe ser 1024");
        assertEquals(768.0, stage.getHeight(), 0.01, "El alto inicial del Editor debe ser 768");

        CountDownLatch layoutLatch = new CountDownLatch(1);
        AtomicReference<Throwable> layoutError = new AtomicReference<>();
        Platform.runLater(() -> {
            try {
                Parent root = (Parent) stage.getScene().getRoot();
                root.applyCss();
                root.layout();
                double altoUtil = stage.getScene().getHeight();

                Node nav = root.lookup(".nav-bar");
                assertNotNull(nav, "La barra de navegacion debe existir");
                assertTrue(nav.isVisible(), "La barra de navegacion debe ser visible");

                Node tabla = root.lookup("#tablaLineas");
                assertNotNull(tabla, "La tabla de lineas debe existir");
                assertTrue(tabla.isVisible(), "La tabla de lineas debe ser visible");

                Bounds navBounds = nav.localToScene(nav.getBoundsInLocal());
                assertTrue(navBounds.getMinY() >= 0,
                        "La barra de navegacion no debe quedar fuera por arriba");

                assertNotNull(root.lookup(".card-editor"),
                        "La tarjeta de cabecera debe llevar la clase card-editor");
                assertNotNull(root.lookup(".totales-compacta"),
                        "El bloque de totales debe llevar la clase totales-compacta");

                Node total = root.lookup("#lblTotal");
                assertNotNull(total, "El importe total debe existir");
                Bounds totalBounds = total.localToScene(total.getBoundsInLocal());
                assertTrue(totalBounds.getMaxY() <= altoUtil,
                        "El total debe caber dentro de la ventana sin scroll: termina en "
                                + totalBounds.getMaxY() + " y el alto util es " + altoUtil);

                Node observaciones = root.lookup("#txtObservaciones");
                assertNotNull(observaciones, "El campo de observaciones debe existir");
                Bounds obsBounds = observaciones.localToScene(observaciones.getBoundsInLocal());
                assertTrue(obsBounds.getMaxY() <= altoUtil,
                        "Observaciones debe caber dentro de la ventana sin scroll");

                assertTrue(tabla.getBoundsInLocal().getHeight() >= 200,
                        "La tabla de lineas debe conservar al menos 200 px de alto, tiene "
                                + tabla.getBoundsInLocal().getHeight());
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
