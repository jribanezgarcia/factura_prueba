package com.alcazaba.facturacion.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import com.alcazaba.facturacion.db.Database;
import com.alcazaba.facturacion.service.Servicios;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Verifica que al navegar desde una vista pequena (Arranque, 760x520) hacia el
 * Menu principal (1024x768) con la ventana ya visible, el Stage crece hasta el
 * minimo de la nueva vista en lugar de conservar el tamano anterior.
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
                stage.setWidth(760);
                stage.setHeight(520);
                Navegador nav = new Navegador(stage, servicios);
                nav.mostrar("/com/alcazaba/facturacion/ui/MenuPrincipal.fxml");
                stage.show();
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
        assertEquals(1024.0, stage.getWidth(), 0.01, "El ancho del Menu debe subir a 1024");
        assertEquals(768.0, stage.getHeight(), 0.01, "El alto del Menu debe subir a 768");
    }
}
