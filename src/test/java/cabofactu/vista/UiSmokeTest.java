package cabofactu.vista;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import cabofactu.modelo.negocio.sqlite.Conexion;
import cabofactu.modelo.Modelo;
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
import cabofactu.vista.controlador.GenerarFacturasMensualesController;

/**
 * Smoke test de la UI: arranca el toolkit JavaFX contra una base de datos
 * temporal y carga cada vista FXML a traves del Navegador (parseo FXML,
 * inyeccion @FXML y alIniciar) para detectar errores de cableado sin tener
 * que abrir la aplicacion a mano.
 */
class UiSmokeTest {

    @TempDir
    static Path carpetaEmpresa;

    private static Modelo modelo;
    private static Navegador nav;

    @BeforeAll
    static void arrancar() throws Exception {
        Conexion.setCarpetaRaiz(carpetaEmpresa);
        modelo = new Modelo();
        JavaFxTestSupport.arrancarFx();

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
        if (!latch.await(30, TimeUnit.SECONDS)) {
            fail("JavaFX no arranco en 30 s");
        }
        if (error.get() != null) {
            throw new RuntimeException(error.get());
        }
    }

    @AfterAll
    static void parar() {
        Conexion.cerrarConexion();
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

    @Test
    void cargarGenerarFacturasMensuales() {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> error = new AtomicReference<>();
        Platform.runLater(() -> {
            try {
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                        getClass().getResource("/cabofactu/vista/recursos/GenerarFacturasMensuales.fxml"));
                Parent root = loader.load();
                GenerarFacturasMensualesController c = loader.getController();
                assertNotNull(c, "El controller de GenerarFacturasMensuales.fxml no se creo");
                c.setModelo(modelo);
                c.setStage(new Stage());
                c.alIniciar();
                maquetarAlMinimo(root);
            } catch (Throwable t) {
                error.set(t);
            } finally {
                latch.countDown();
            }
        });
        await(latch, error, "GenerarFacturasMensuales.fxml");
    }

    private void cargar(String fxml) {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> error = new AtomicReference<>();
        Platform.runLater(() -> {
            try {
                Vista v = nav.mostrar("/cabofactu/vista/recursos/" + fxml);
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
        raiz.resize(1024, 768);
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
