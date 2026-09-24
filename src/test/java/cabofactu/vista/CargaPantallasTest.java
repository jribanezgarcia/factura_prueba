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
import cabofactu.vista.controlador.FichaClienteController;
import cabofactu.vista.controlador.FichaSerieController;
import cabofactu.vista.controlador.FichaTipoIvaController;
import cabofactu.vista.controlador.FichaTipoRetencionController;
import cabofactu.vista.controlador.GenerarFacturasMensualesController;

/**
 * Carga cada pantalla FXML a través de la Vista (parseo FXML e initialize)
 * para detectar errores de cableado sin tener que abrir la aplicación a mano.
 */
class CargaPantallasTest {

    @TempDir
    static Path carpetaEmpresa;

    @BeforeAll
    static void arrancar() throws Exception {
        Conexion.setCarpetaRaiz(carpetaEmpresa);
        PruebasJavaFx.arrancarFx();

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> error = new AtomicReference<>();
        Platform.runLater(() -> {
            try {
                PruebasJavaFx.prepararVista(new Modelo(), new Stage());
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
    void cargarCopias() {
        cargar("CopiaSeguridad.fxml");
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
                        GenerarFacturasMensualesController.class.getResource(
                                "/cabofactu/vista/recursos/GenerarFacturasMensuales.fxml"));
                Parent root = loader.load();
                GenerarFacturasMensualesController c = loader.getController();
                assertNotNull(c, "El controller de GenerarFacturasMensuales.fxml no se creo");
                c.setStage(new Stage());
                maquetarAlMinimo(root);
            } catch (Throwable t) {
                error.set(t);
            } finally {
                latch.countDown();
            }
        });
        await(latch, error, "GenerarFacturasMensuales.fxml");
    }

    @Test
    void cargarFichaCliente() {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> error = new AtomicReference<>();
        Platform.runLater(() -> {
            try {
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                        FichaClienteController.class.getResource(
                                "/cabofactu/vista/recursos/FichaCliente.fxml"));
                Parent root = loader.load();
                FichaClienteController c = loader.getController();
                assertNotNull(c, "El controller de FichaCliente.fxml no se creo");
                c.setRegistro(null);
                maquetarAlMinimo(root);
            } catch (Throwable t) {
                error.set(t);
            } finally {
                latch.countDown();
            }
        });
        await(latch, error, "FichaCliente.fxml");
    }

    @Test
    void cargarFichaTipoIva() {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> error = new AtomicReference<>();
        Platform.runLater(() -> {
            try {
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                        FichaTipoIvaController.class.getResource(
                                "/cabofactu/vista/recursos/FichaTipoIva.fxml"));
                Parent root = loader.load();
                FichaTipoIvaController c = loader.getController();
                assertNotNull(c, "El controller de FichaTipoIva.fxml no se creo");
                c.setRegistro(null);
                maquetarAlMinimo(root);
            } catch (Throwable t) {
                error.set(t);
            } finally {
                latch.countDown();
            }
        });
        await(latch, error, "FichaTipoIva.fxml");
    }

    @Test
    void cargarFichaSerie() {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> error = new AtomicReference<>();
        Platform.runLater(() -> {
            try {
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                        FichaSerieController.class.getResource(
                                "/cabofactu/vista/recursos/FichaSerie.fxml"));
                Parent root = loader.load();
                FichaSerieController c = loader.getController();
                assertNotNull(c, "El controller de FichaSerie.fxml no se creo");
                c.setRegistro(null);
                maquetarAlMinimo(root);
            } catch (Throwable t) {
                error.set(t);
            } finally {
                latch.countDown();
            }
        });
        await(latch, error, "FichaSerie.fxml");
    }

    @Test
    void cargarFichaTipoRetencion() {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> error = new AtomicReference<>();
        Platform.runLater(() -> {
            try {
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                        FichaTipoRetencionController.class.getResource(
                                "/cabofactu/vista/recursos/FichaTipoRetencion.fxml"));
                Parent root = loader.load();
                FichaTipoRetencionController c = loader.getController();
                assertNotNull(c, "El controller de FichaTipoRetencion.fxml no se creo");
                c.setRegistro(null);
                maquetarAlMinimo(root);
            } catch (Throwable t) {
                error.set(t);
            } finally {
                latch.countDown();
            }
        });
        await(latch, error, "FichaTipoRetencion.fxml");
    }

    private void cargar(String fxml) {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> error = new AtomicReference<>();
        Platform.runLater(() -> {
            try {
                Pantalla pantalla = Vista.getInstancia().mostrar(fxml);
                assertNotNull(pantalla, "El controller de " + fxml + " no se creo");
                maquetarAlMinimo(Vista.getInstancia().getVentana().getScene().getRoot());
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
