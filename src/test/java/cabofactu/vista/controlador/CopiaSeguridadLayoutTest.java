package cabofactu.vista.controlador;

import cabofactu.modelo.negocio.sqlite.Conexion;
import cabofactu.modelo.Modelo;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.Region;
import javafx.scene.control.ScrollPane;
import javafx.stage.Stage;
import cabofactu.vista.PruebasJavaFx;
import cabofactu.vista.Navegador;
import cabofactu.vista.Vista;

class CopiaSeguridadLayoutTest {

    private static final int ANCHO_ESCENA = 1024;
    private static final int ALTO_ESCENA = 768;

    @TempDir
    static Path carpetaEmpresa;

    private static Modelo modelo;
    private static Navegador nav;

    @BeforeAll
    static void arrancar() throws Exception {
        Conexion.setCarpetaRaiz(carpetaEmpresa);
        modelo = new Modelo();
        PruebasJavaFx.arrancarFx();

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> error = new AtomicReference<>();
        Platform.runLater(() -> {
            try {
                Stage stage = new Stage();
                nav = new Navegador(stage, modelo);
            } catch (Throwable t) {
                error.set(t);
            } finally {
                latch.countDown();
            }
        });
        await(latch, error);
    }

    @AfterAll
    static void parar() {
        Conexion.cerrarConexion();
    }

    @Test
    void backupConDosTarjetasCabeSinDesbordar() {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> error = new AtomicReference<>();
        Platform.runLater(() -> {
            try {
                Vista v = nav.mostrar("/cabofactu/vista/recursos/CopiaSeguridad.fxml");
                assertNotNull(v, "El controller de CopiaSeguridad.fxml no se creo");
                Parent raiz = nav.stage().getScene().getRoot();
                raiz.applyCss();
                raiz.resize(ANCHO_ESCENA, ALTO_ESCENA);
                raiz.layout();
                List<Node> regiones = new ArrayList<>(raiz.lookupAll("*"));
                Bounds raizB = raiz.localToScene(raiz.getBoundsInLocal());
                double maxY = raizB.getMaxY();
                double maxX = raizB.getMaxX();
                ScrollPane scroll = (ScrollPane) raiz.lookup(".scroll-pane");
                assertNotNull(scroll, "Debe existir un ScrollPane con las tarjetas");
                for (Node n : regiones) {
                    if (n == raiz || !(n instanceof Region) || !n.isVisible()) {
                        continue;
                    }
                    boolean enContenidoScroll = false;
                    if (scroll != null) {
                        Node contenido = scroll.getContent();
                        for (Node an = n; an != null; an = an.getParent()) {
                            if (an == contenido) {
                                enContenidoScroll = true;
                                break;
                            }
                        }
                    }
                    Bounds b = n.localToScene(n.getBoundsInLocal());
                    assertTrue(b.getMinX() >= -0.5 && b.getMaxX() <= maxX + 0.5,
                            "El control '" + n.getId() + "' se sale por la derecha del borde de la ventana (" + maxX + "): "
                                    + b.getMinX() + ".." + b.getMaxX());
                    if (!enContenidoScroll) {
                        assertTrue(b.getMinY() >= -0.5 && b.getMaxY() <= maxY + 0.5,
                                "El control '" + n.getId() + "' se sale por debajo del borde de la ventana (" + maxY + "): "
                                        + b.getMinY() + ".." + b.getMaxY());
                    }
                }
            } catch (Throwable t) {
                error.set(t);
            } finally {
                latch.countDown();
            }
        });
        await(latch, error);
    }

    private static void await(CountDownLatch latch, AtomicReference<Throwable> error) {
        try {
            if (!latch.await(30, TimeUnit.SECONDS)) {
                fail("CopiaSeguridadLayoutTest no termino en 30 s");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            fail("Interrumpido en CopiaSeguridadLayoutTest");
        }
        if (error.get() != null) {
            throw new AssertionError("Error en CopiaSeguridadLayoutTest", error.get());
        }
    }
}
