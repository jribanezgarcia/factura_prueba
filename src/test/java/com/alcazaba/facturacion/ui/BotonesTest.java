package com.alcazaba.facturacion.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.alcazaba.facturacion.db.Database;
import com.alcazaba.facturacion.service.Servicios;
import com.alcazaba.facturacion.ui.ConfiguracionController.ItemSeccion;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class BotonesTest {

    private static final int ANCHO_ESCENA = 1024;
    private static final int ALTO_ESCENA = 768;

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
        await(latch, error);
    }

    @AfterAll
    static void parar() {
        Database.resetConnection();
    }

    @Test
    void botonesIvaMidenLoMismoYNingunoBajaDe80() {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> error = new AtomicReference<>();
        Platform.runLater(() -> {
            try {
                Vista v = nav.mostrar("/com/alcazaba/facturacion/ui/Configuracion.fxml");
                assertNotNull(v, "El controller de Configuracion.fxml no se creo");
                Parent raiz = nav.stage().getScene().getRoot();

                ListView<ItemSeccion> lista = (ListView<ItemSeccion>) raiz.lookup("#listaSecciones");
                assertNotNull(lista, "Debe existir la lista lateral de secciones");

                Parent seccionIva = (Parent) raiz.lookup("#seccionIva");
                assertNotNull(seccionIva, "Debe existir la seccion de IVA");
                seleccionar(lista, raiz, seccionIva);

                Button nuevo = boton(seccionIva, "Nuevo");
                Button guardar = boton(seccionIva, "Guardar");
                Button inactivar = boton(seccionIva, "Inactivar/Activar");
                assertEquals(nuevo.getWidth(), guardar.getWidth(), 0.5,
                        "Nuevo y Guardar deben medir lo mismo");
                assertEquals(nuevo.getWidth(), inactivar.getWidth(), 0.5,
                        "Nuevo e Inactivar/Activar deben medir lo mismo");

                for (int i = 0; i < lista.getItems().size(); i++) {
                    ItemSeccion item = lista.getItems().get(i);
                    if (item.grupo) {
                        continue;
                    }
                    seleccionar(lista, raiz, item.panel);
                    for (Button b : botonesSoloTexto(raiz)) {
                        assertTrue(b.getWidth() >= 79.5,
                                "El boton \"" + b.getText() + "\" mide menos de 80: " + b.getWidth());
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

    private static void seleccionar(ListView<ItemSeccion> lista, Parent raiz, Node panel) {
        for (int i = 0; i < lista.getItems().size(); i++) {
            if (lista.getItems().get(i).panel == panel) {
                lista.getSelectionModel().select(i);
                break;
            }
        }
        raiz.applyCss();
        raiz.resize(ANCHO_ESCENA, ALTO_ESCENA);
        raiz.layout();
    }

    private static Button boton(Parent panel, String texto) {
        for (Node n : panel.lookupAll(".button")) {
            if (n instanceof Button && texto.equals(((Button) n).getText())) {
                return (Button) n;
            }
        }
        fail("No se encontro el boton \"" + texto + "\"");
        return null;
    }

    private static List<Button> botonesSoloTexto(Parent raiz) {
        List<Button> botones = new ArrayList<>();
        for (Node n : raiz.lookupAll(".button")) {
            if (!(n instanceof Button) || !visible(n)) {
                continue;
            }
            Button b = (Button) n;
            if (b.getStyleClass().contains("btn-ribbon")
                    || b.getStyleClass().contains("nav-button")
                    || b.getStyleClass().contains("menu-item")) {
                continue;
            }
            if (!b.getStyleClass().contains("primary-button")
                    && !b.getStyleClass().contains("default-button")
                    && !b.getStyleClass().contains("action-button")) {
                continue;
            }
            botones.add(b);
        }
        return botones;
    }

    private static boolean visible(Node n) {
        for (Node p = n; p != null; p = p.getParent()) {
            if (!p.isVisible()) {
                return false;
            }
        }
        return true;
    }

    private static void await(CountDownLatch latch, AtomicReference<Throwable> error) {
        try {
            if (!latch.await(30, TimeUnit.SECONDS)) {
                fail("BotonesTest no termino en 30 s");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            fail("Interrumpido en BotonesTest");
        }
        if (error.get() != null) {
            throw new AssertionError("Error en BotonesTest", error.get());
        }
    }
}
