package com.alcazaba.facturacion.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import com.alcazaba.facturacion.db.Database;
import com.alcazaba.facturacion.service.Servicios;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Reproduce el timing real de la transicion Arranque -> Menu: el Stage ya esta
 * visible y no redimensionable (como en Arranque, 760x520), luego entrarEnMenu
 * pide 1024x768 antes de cargar la nueva escena. Verifica que, despues del
 * pulse posterior al layout, la ventana termina a 1024x768.
 */
class VentanaTransicionTest {

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
    void menuSubeHasta1024AlPasarDeArranque() throws Exception {
        Servicios servicios = new Servicios();
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> error = new AtomicReference<>();
        AtomicReference<Stage> stageRef = new AtomicReference<>();

        Platform.runLater(() -> {
            try {
                Stage stage = new Stage();
                stage.setResizable(false);
                stage.setWidth(760);
                stage.setHeight(520);
                stage.setScene(new Scene(new Pane()));
                stage.show();

                stage.setWidth(1024);
                stage.setHeight(768);
                Navegador nav = new Navegador(stage, servicios);
                nav.mostrar("/com/alcazaba/facturacion/ui/MenuPrincipal.fxml");
                stageRef.set(stage);
            } catch (Throwable t) {
                error.set(t);
            } finally {
                latch.countDown();
            }
        });

        if (!latch.await(30, TimeUnit.SECONDS)) {
            fail("JavaFX no cargo el Menu en 30 s");
        }
        if (error.get() != null) {
            throw new RuntimeException(error.get());
        }

        Stage stage = stageRef.get();
        assertEquals(1024.0, stage.getMinWidth(), 0.01, "El ancho minimo del Menu debe ser 1024");
        assertEquals(768.0, stage.getMinHeight(), 0.01, "El alto minimo del Menu debe ser 768");

        CountDownLatch layoutLatch = new CountDownLatch(1);
        AtomicReference<Throwable> layoutError = new AtomicReference<>();
        Platform.runLater(() -> {
            try {
                stage.getScene().getRoot().applyCss();
                stage.getScene().getRoot().layout();
            } catch (Throwable t) {
                layoutError.set(t);
            } finally {
                layoutLatch.countDown();
            }
        });

        if (!layoutLatch.await(30, TimeUnit.SECONDS)) {
            fail("El layout del Menu no termino en 30 s");
        }
        if (layoutError.get() != null) {
            throw new AssertionError("Fallo el layout del Menu", layoutError.get());
        }

        CountDownLatch sizeLatch = new CountDownLatch(1);
        AtomicReference<Throwable> sizeError = new AtomicReference<>();
        Platform.runLater(() -> Platform.runLater(() -> {
            try {
                assertEquals(1024.0, stage.getWidth(), 0.01,
                        "El ancho del Menu debe subir a 1024 tras el layout");
                assertEquals(768.0, stage.getHeight(), 0.01,
                        "El alto del Menu debe subir a 768 tras el layout");
            } catch (Throwable t) {
                sizeError.set(t);
            } finally {
                sizeLatch.countDown();
            }
        }));

        if (!sizeLatch.await(30, TimeUnit.SECONDS)) {
            fail("El tamano del Menu no se estabilizo en 30 s");
        }
        if (sizeError.get() != null) {
            throw new AssertionError("El tamano final del Menu no es 1024x768", sizeError.get());
        }
    }
}