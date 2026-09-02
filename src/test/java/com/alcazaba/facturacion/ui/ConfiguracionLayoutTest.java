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
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ConfiguracionLayoutTest {

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
    void sieteSeccionesCabenYLaBarraSoloEnLasTresPrimeras() {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> error = new AtomicReference<>();
        Platform.runLater(() -> {
            try {
                Vista v = nav.mostrar("/com/alcazaba/facturacion/ui/Configuracion.fxml");
                assertNotNull(v, "El controller de Configuracion.fxml no se creo");
                Parent raiz = nav.stage().getScene().getRoot();
                raiz.applyCss();
                raiz.resize(ANCHO_ESCENA, ALTO_ESCENA);
                raiz.layout();

                ListView<ItemSeccion> lista = (ListView<ItemSeccion>) raiz.lookup("#listaSecciones");
                assertNotNull(lista, "Debe existir la lista lateral de secciones");
                HBox barra = (HBox) raiz.lookup("#barraGuardar");
                assertNotNull(barra, "Debe existir la barra de guardado");

                int secciones = 0;
                for (int i = 0; i < lista.getItems().size(); i++) {
                    ItemSeccion item = lista.getItems().get(i);
                    if (item.grupo) {
                        continue;
                    }
                    secciones++;
                    lista.getSelectionModel().select(i);
                    raiz.applyCss();
                    raiz.layout();

                    assertTrue(item.panel.isVisible() && item.panel.isManaged(),
                            "La seccion \"" + item.texto + "\" debe mostrarse al seleccionarla");
                    assertEquals(item.guardar, barra.isVisible(),
                            "La barra de guardado debe mostrarse solo en \"" + item.texto + "\": " + item.guardar);

                    List<Node> regiones = new ArrayList<>();
                    regiones.add(item.panel);
                    regiones.addAll(item.panel.lookupAll("*"));
                    for (Node n : regiones) {
                        if (!(n instanceof Region) || !n.isVisible()) {
                            continue;
                        }
                        Bounds b = n.localToScene(n.getBoundsInLocal());
                        assertTrue(b.getMinY() >= -0.5 && b.getMaxY() <= ALTO_ESCENA + 0.5,
                                "En \"" + item.texto + "\" el control '" + n.getId() + "' se sale por debajo: "
                                        + b.getMinY() + ".." + b.getMaxY());
                        assertTrue(b.getMinX() >= -0.5 && b.getMaxX() <= ANCHO_ESCENA + 0.5,
                                "En \"" + item.texto + "\" el control '" + n.getId() + "' se sale por la derecha: "
                                        + b.getMinX() + ".." + b.getMaxX());
                    }
                }
                assertEquals(7, secciones, "La lista debe contener siete secciones");
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
                fail("ConfiguracionLayoutTest no termino en 30 s");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            fail("Interrumpido en ConfiguracionLayoutTest");
        }
        if (error.get() != null) {
            throw new AssertionError("Error en ConfiguracionLayoutTest", error.get());
        }
    }
}