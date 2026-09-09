package com.alcazaba.facturacion.ui;

import com.alcazaba.facturacion.db.Database;
import com.alcazaba.facturacion.service.Servicios;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

class MenuLayoutTest {

    private static final double TOLERANCIA = 2.0;

    @TempDir
    static Path dataDir;

    private static Servicios servicios;
    private static Navegador nav;

    @BeforeAll
    static void arrancar() throws Exception {
        Database.setDataDir(dataDir);
        servicios = new Servicios();
        JavaFxTestSupport.arrancarFx();

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> error = new AtomicReference<>();
        Platform.runLater(() -> {
            try {
                Stage stage = new Stage();
                nav = new Navegador(stage, servicios);
            } catch (Throwable t) {
                error.set(t);
            } finally {
                latch.countDown();
            }
        });
        await(latch, error, "JavaFX no arranco en 30 s");
    }

    @AfterAll
    static void parar() {
        Database.resetConnection();
    }

    private record Medida(double contenedorCentroX, double contenedorCentroY,
                          double bloqueCentroX, double bloqueCentroY,
                          double tarjeta1MinY, double tarjeta2MinY,
                          double tarjeta1Ancho, double tarjeta2Ancho) {
    }

    private static Bounds escena(Node n) {
        return n.localToScene(n.getLayoutBounds());
    }

    private static Medida medir(double ancho, double alto) throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> error = new AtomicReference<>();
        AtomicReference<Medida> medida = new AtomicReference<>();
        Platform.runLater(() -> {
            try {
                nav.mostrar("/com/alcazaba/facturacion/ui/MenuPrincipal.fxml");
                Parent raiz = nav.stage().getScene().getRoot();
                raiz.applyCss();
                raiz.resize(ancho, alto);
                raiz.layout();
                BorderPane borde = (BorderPane) raiz;
                StackPane contenedor = (StackPane) borde.getCenter();
                assertNotNull(contenedor, "El centro del BorderPane debe ser el StackPane");
                HBox bloque = (HBox) contenedor.getChildren().get(0);
                VBox tarjeta1 = (VBox) bloque.getChildren().get(0);
                VBox tarjeta2 = (VBox) bloque.getChildren().get(1);
                Bounds bc = escena(contenedor);
                Bounds bb = escena(bloque);
                Bounds b1 = escena(tarjeta1);
                Bounds b2 = escena(tarjeta2);
                medida.set(new Medida(
                        (bc.getMinX() + bc.getMaxX()) / 2, (bc.getMinY() + bc.getMaxY()) / 2,
                        (bb.getMinX() + bb.getMaxX()) / 2, (bb.getMinY() + bb.getMaxY()) / 2,
                        b1.getMinY(), b2.getMinY(), b1.getWidth(), b2.getWidth()));
            } catch (Throwable t) {
                error.set(t);
            } finally {
                latch.countDown();
            }
        });
        await(latch, error, "MenuLayoutTest no termino en 30 s");
        return medida.get();
    }

    @Test
    void bloqueCentrado1024() throws Exception {
        Medida m = medir(1024, 768);
        assertEquals(m.contenedorCentroX(), m.bloqueCentroX(), TOLERANCIA,
                "El bloque debe centrarse en horizontal");
        assertEquals(m.contenedorCentroY(), m.bloqueCentroY(), TOLERANCIA,
                "El bloque debe centrarse en vertical");
    }

    @Test
    void bloqueCentradoRedimensionado() throws Exception {
        Medida m = medir(1280, 900);
        assertEquals(m.contenedorCentroX(), m.bloqueCentroX(), TOLERANCIA,
                "El bloque debe seguir centrado en horizontal al agrandar");
        assertEquals(m.contenedorCentroY(), m.bloqueCentroY(), TOLERANCIA,
                "El bloque debe seguir centrado en vertical al agrandar");
    }

    @Test
    void tarjetasAlineadasYSinCrecer() throws Exception {
        Medida m = medir(1024, 768);
        Medida g = medir(1280, 900);
        assertEquals(m.tarjeta1MinY(), m.tarjeta2MinY(), 1.0,
                "Las tarjetas deben seguir alineadas por su borde superior");
        assertEquals(m.tarjeta1Ancho(), g.tarjeta1Ancho(), 1.0,
                "La tarjeta de empresa no debe crecer con la ventana");
        assertEquals(m.tarjeta2Ancho(), g.tarjeta2Ancho(), 1.0,
                "La tarjeta de menu no debe crecer con la ventana");
    }

    private static void await(CountDownLatch latch, AtomicReference<Throwable> error, String mensaje) {
        try {
            if (!latch.await(30, TimeUnit.SECONDS)) {
                fail(mensaje);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            fail("Interrumpido en MenuLayoutTest");
        }
        if (error.get() != null) {
            throw new AssertionError("Error en MenuLayoutTest", error.get());
        }
    }
}
