package com.alcazaba.facturacion.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.alcazaba.facturacion.db.Database;
import com.alcazaba.facturacion.service.Servicios;
import javafx.application.Platform;
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
 * Cubre las dos causas del fallo de tamano de ventana: que Arranque no imponga
 * maximos que hereden las vistas siguientes, y que navegar no desmaximice ni
 * reduzca la ventana del usuario. El sintoma en pantalla (ventana clavada a
 * 760x520) solo se daba en el primary stage de la aplicacion y no es
 * reproducible con un Stage creado en un test.
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
        AtomicReference<Stage> stageRef = new AtomicReference<>();

        enFx("JavaFX no cargo el Menu", () -> {
            Stage stage = new Stage();
            Navegador navArranque = new Navegador(stage, servicios);
            navArranque.mostrar("/com/alcazaba/facturacion/ui/Arranque.fxml");
            stage.show();
            stageRef.set(stage);
        });

        Stage stage = stageRef.get();
        assertEquals(760.0, stage.getWidth(), 0.01, "Arranque debe medir 760 de ancho");
        assertEquals(520.0, stage.getHeight(), 0.01, "Arranque debe medir 520 de alto");
        assertEquals(Double.MAX_VALUE, stage.getMaxWidth(), 0.01,
                "Arranque no debe imponer un maximo de ancho que herede la vista siguiente");
        assertEquals(Double.MAX_VALUE, stage.getMaxHeight(), 0.01,
                "Arranque no debe imponer un maximo de alto que herede la vista siguiente");
        assertFalse(stage.isResizable(), "Arranque no debe ser redimensionable");

        enFx("JavaFX no cargo el Menu", () -> {
            Navegador nav = new Navegador(stage, servicios);
            nav.mostrar("/com/alcazaba/facturacion/ui/MenuPrincipal.fxml");
        });

        assertEquals(1024.0, stage.getMinWidth(), 0.01, "El ancho minimo del Menu debe ser 1024");
        assertEquals(768.0, stage.getMinHeight(), 0.01, "El alto minimo del Menu debe ser 768");
        assertEquals(1024.0, stage.getWidth(), 0.01, "El Menu debe crecer a 1024 de ancho");
        assertEquals(768.0, stage.getHeight(), 0.01, "El Menu debe crecer a 768 de alto");
        assertEquals(Double.MAX_VALUE, stage.getMaxWidth(), 0.01,
                "El maximo de ancho de Arranque no debe sobrevivir al Menu");
        assertEquals(Double.MAX_VALUE, stage.getMaxHeight(), 0.01,
                "El maximo de alto de Arranque no debe sobrevivir al Menu");
        assertTrue(stage.isResizable(), "El Menu debe ser redimensionable");

        Thread.sleep(500);
        AtomicReference<double[]> finalRef = new AtomicReference<>();
        enFx("La ventana no se estabilizo", () ->
                finalRef.set(new double[]{stage.getWidth(), stage.getHeight()}));
        assertEquals(1024.0, finalRef.get()[0], 0.01,
                "La ventana nativa debe quedarse en 1024 de ancho, no volver al tamaño de Arranque");
        assertEquals(768.0, finalRef.get()[1], 0.01,
                "La ventana nativa debe quedarse en 768 de alto, no volver al tamaño de Arranque");

        enFx("No se pudo cerrar la ventana", stage::hide);
    }

    @Test
    void navegarEntreVistasConservaElTamanoDelUsuario() throws Exception {
        Servicios servicios = new Servicios();
        AtomicReference<Stage> stageRef = new AtomicReference<>();

        enFx("JavaFX no cargo el Menu", () -> {
            Stage stage = new Stage();
            Navegador nav = new Navegador(stage, servicios);
            nav.mostrar("/com/alcazaba/facturacion/ui/MenuPrincipal.fxml");
            stage.show();
            stage.setWidth(1300);
            stage.setHeight(900);
            stageRef.set(stage);
        });

        Stage stage = stageRef.get();

        enFx("JavaFX no cargo el Historico", () -> {
            Navegador nav = new Navegador(stage, servicios);
            nav.mostrar("/com/alcazaba/facturacion/ui/Historico.fxml");
        });

        assertEquals(1300.0, stage.getWidth(), 0.01, "El Historico no debe reducir el ancho del usuario");
        assertEquals(900.0, stage.getHeight(), 0.01, "El Historico no debe reducir el alto del usuario");

        enFx("No se pudo cerrar la ventana", stage::hide);
    }

    @Test
    void navegarNoDesmaximizaLaVentana() throws Exception {
        Servicios servicios = new Servicios();
        AtomicReference<Stage> stageRef = new AtomicReference<>();

        enFx("JavaFX no cargo el Menu", () -> {
            Stage stage = new Stage();
            Navegador nav = new Navegador(stage, servicios);
            nav.mostrar("/com/alcazaba/facturacion/ui/MenuPrincipal.fxml");
            stage.show();
            stage.setMaximized(true);
            stageRef.set(stage);
        });

        Stage stage = stageRef.get();
        assertTrue(stage.isMaximized(), "La ventana debe quedar maximizada antes de navegar");

        enFx("JavaFX no cargo el Historico", () -> {
            Navegador nav = new Navegador(stage, servicios);
            nav.mostrar("/com/alcazaba/facturacion/ui/Historico.fxml");
        });

        assertTrue(stage.isMaximized(), "Navegar a otra vista no debe desmaximizar la ventana");

        enFx("No se pudo cerrar la ventana", stage::hide);
    }

    private void enFx(String mensajeTimeout, Runnable accion) throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> error = new AtomicReference<>();
        Platform.runLater(() -> {
            try {
                accion.run();
            } catch (Throwable t) {
                error.set(t);
            } finally {
                latch.countDown();
            }
        });
        if (!latch.await(30, TimeUnit.SECONDS)) {
            fail(mensajeTimeout + " en 30 s");
        }
        if (error.get() != null) {
            throw new AssertionError(mensajeTimeout, error.get());
        }
    }
}
