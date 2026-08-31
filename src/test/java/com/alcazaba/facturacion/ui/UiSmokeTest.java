package com.alcazaba.facturacion.ui;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import com.alcazaba.facturacion.db.Database;
import com.alcazaba.facturacion.service.Servicios;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Smoke test de la UI: arranca el toolkit JavaFX contra una base de datos
 * temporal y carga cada vista FXML a traves del Navegador (parseo FXML,
 * inyeccion @FXML y alIniciar) para detectar errores de cableado sin tener
 * que abrir la aplicacion a mano.
 */
class UiSmokeTest {

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
        if (!latch.await(30, TimeUnit.SECONDS)) {
            fail("JavaFX no arranco en 30 s");
        }
        if (error.get() != null) {
            throw new RuntimeException(error.get());
        }
    }

    @AfterAll
    static void parar() {
        Database.resetConnection();
    }

    @Test
    void cargarMenuPrincipal() {
        cargar("MenuPrincipal.fxml");
    }

    @Test
    void cargarEditor() {
        cargar("Editor.fxml");
    }

    @Test
    void cargarHistorico() {
        cargar("Historico.fxml");
    }

    @Test
    void cargarClientes() {
        cargar("Clientes.fxml");
    }

    @Test
    void cargarConfiguracion() {
        cargar("Configuracion.fxml");
    }

    @Test
    void cargarBackup() {
        cargar("Backup.fxml");
    }

    @Test
    void cargarVersiones() {
        cargar("Versiones.fxml");
    }

    @Test
    void cargarArranque() {
        cargar("Arranque.fxml");
    }

    private void cargar(String fxml) {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> error = new AtomicReference<>();
        Platform.runLater(() -> {
            try {
                Vista v = nav.mostrar("/com/alcazaba/facturacion/ui/" + fxml);
                assertNotNull(v, "El controller de " + fxml + " no se creo");
                maquetarAlMinimo(nav.stage().getScene().getRoot());
            } catch (Throwable t) {
                error.set(t);
            } finally {
                latch.countDown();
            }
        });
        await(latch, error, fxml);
    }

    private void maquetarAlMinimo(Parent raiz) {
        raiz.applyCss();
        raiz.resize(800, 600);
        raiz.layout();
    }

    private void await(CountDownLatch latch, AtomicReference<Throwable> error, String fxml) {
        try {
            if (!latch.await(30, TimeUnit.SECONDS)) {
                fail("La vista " + fxml + " no termino de cargar en 30 s");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            fail("Interrumpido cargando " + fxml);
        }
        if (error.get() != null) {
            throw new AssertionError("Error cargando " + fxml, error.get());
        }
    }
}
