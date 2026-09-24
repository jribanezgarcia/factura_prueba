package cabofactu.vista;

import cabofactu.modelo.Modelo;
import cabofactu.modelo.negocio.sqlite.Conexion;
import cabofactu.vista.controlador.FichaClienteController;
import cabofactu.vista.controlador.FichaSerieController;
import cabofactu.vista.controlador.FichaTipoIvaController;
import cabofactu.vista.controlador.FichaTipoRetencionController;
import cabofactu.vista.controlador.GenerarFacturasMensualesController;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Labeled;
import javafx.stage.Stage;
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

/**
 * Cada pantalla cabe a 1024x768 sin cortar textos: ninguna etiqueta visible
 * mide menos de lo que pide su texto. Es el fallo del botón «Completar
 * datos», de 80 px para 113 que pedía.
 */
class TextosCompletosTest {

    @TempDir
    static Path carpetaEmpresa;

    @BeforeAll
    static void arrancar() throws Exception {
        Conexion.setCarpetaRaiz(carpetaEmpresa);
        PruebasJavaFx.arrancarFx();

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> error = new AtomicReference<>();
        Platform.runLater(() -> preparar(latch, error));
        if (!latch.await(30, TimeUnit.SECONDS)) {
            fail("JavaFX no arranco en 30 s");
        }
        if (error.get() != null) {
            throw new RuntimeException(error.get());
        }
    }

    private static void preparar(CountDownLatch latch, AtomicReference<Throwable> error) {
        try {
            PruebasJavaFx.prepararVista(new Modelo(), new Stage());
        } catch (Throwable t) {
            error.set(t);
        } finally {
            latch.countDown();
        }
    }

    @AfterAll
    static void parar() {
        Conexion.cerrarConexion();
    }

    @Test
    void textosMenuPrincipal() {
        revisarMostrada("MenuPrincipal.fxml");
    }

    @Test
    void textosEditor() {
        revisarMostrada("Editor.fxml");
    }

    @Test
    void textosHistorico() {
        revisarMostrada("Historico.fxml");
    }

    @Test
    void textosClientes() {
        revisarMostrada("Clientes.fxml");
    }

    @Test
    void textosConfiguracion() {
        revisarMostrada("Configuracion.fxml");
    }

    @Test
    void textosCopias() {
        revisarMostrada("CopiaSeguridad.fxml");
    }

    @Test
    void textosVersiones() {
        revisarMostrada("Versiones.fxml");
    }

    @Test
    void textosArranque() {
        revisarMostrada("Arranque.fxml");
    }

    @Test
    void textosMensuales() {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> error = new AtomicReference<>();
        Platform.runLater(() -> revisarMensuales(latch, error));
        await(latch, error, "GenerarFacturasMensuales.fxml");
    }

    @Test
    void textosFichaCliente() {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> error = new AtomicReference<>();
        Platform.runLater(() -> revisarFichaCliente(latch, error));
        await(latch, error, "FichaCliente.fxml");
    }

    @Test
    void textosFichaTipoIva() {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> error = new AtomicReference<>();
        Platform.runLater(() -> revisarFichaTipoIva(latch, error));
        await(latch, error, "FichaTipoIva.fxml");
    }

    @Test
    void textosFichaSerie() {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> error = new AtomicReference<>();
        Platform.runLater(() -> revisarFichaSerie(latch, error));
        await(latch, error, "FichaSerie.fxml");
    }

    @Test
    void textosFichaTipoRetencion() {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> error = new AtomicReference<>();
        Platform.runLater(() -> revisarFichaTipoRetencion(latch, error));
        await(latch, error, "FichaTipoRetencion.fxml");
    }

    private void revisarMostrada(String fxml) {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> error = new AtomicReference<>();
        Platform.runLater(() -> mostrar(latch, error, fxml));
        await(latch, error, fxml);
    }

    private static void mostrar(CountDownLatch latch, AtomicReference<Throwable> error, String fxml) {
        try {
            Pantalla pantalla = Vista.getInstancia().mostrar(fxml);
            assertNotNull(pantalla, "El controller de " + fxml + " no se creo");
            Parent raiz = Vista.getInstancia().getVentana().getScene().getRoot();
            maquetarAlMinimo(raiz);
            comprobarTextos(raiz, fxml);
        } catch (Throwable t) {
            error.set(t);
        } finally {
            latch.countDown();
        }
    }

    private static void revisarMensuales(CountDownLatch latch, AtomicReference<Throwable> error) {
        try {
            FXMLLoader cargador = new FXMLLoader(
                    GenerarFacturasMensualesController.class.getResource(
                            "/cabofactu/vista/recursos/GenerarFacturasMensuales.fxml"));
            Parent raiz = cargador.load();
            GenerarFacturasMensualesController controlador = cargador.getController();
            assertNotNull(controlador, "El controller de GenerarFacturasMensuales.fxml no se creo");
            controlador.setStage(new Stage());
            maquetarAlMinimo(raiz);
            comprobarTextos(raiz, "GenerarFacturasMensuales.fxml");
        } catch (Throwable t) {
            error.set(t);
        } finally {
            latch.countDown();
        }
    }

    private static void revisarFichaCliente(CountDownLatch latch, AtomicReference<Throwable> error) {
        try {
            FXMLLoader cargador = new FXMLLoader(
                    FichaClienteController.class.getResource(
                            "/cabofactu/vista/recursos/FichaCliente.fxml"));
            Parent raiz = cargador.load();
            FichaClienteController controlador = cargador.getController();
            assertNotNull(controlador, "El controller de FichaCliente.fxml no se creo");
            controlador.setRegistro(null);
            maquetarAlMinimo(raiz);
            comprobarTextos(raiz, "FichaCliente.fxml");
        } catch (Throwable t) {
            error.set(t);
        } finally {
            latch.countDown();
        }
    }

    private static void revisarFichaTipoIva(CountDownLatch latch, AtomicReference<Throwable> error) {
        try {
            FXMLLoader cargador = new FXMLLoader(
                    FichaTipoIvaController.class.getResource(
                            "/cabofactu/vista/recursos/FichaTipoIva.fxml"));
            Parent raiz = cargador.load();
            FichaTipoIvaController controlador = cargador.getController();
            assertNotNull(controlador, "El controller de FichaTipoIva.fxml no se creo");
            controlador.setRegistro(null);
            maquetarAlMinimo(raiz);
            comprobarTextos(raiz, "FichaTipoIva.fxml");
        } catch (Throwable t) {
            error.set(t);
        } finally {
            latch.countDown();
        }
    }

    private static void revisarFichaSerie(CountDownLatch latch, AtomicReference<Throwable> error) {
        try {
            FXMLLoader cargador = new FXMLLoader(
                    FichaSerieController.class.getResource(
                            "/cabofactu/vista/recursos/FichaSerie.fxml"));
            Parent raiz = cargador.load();
            FichaSerieController controlador = cargador.getController();
            assertNotNull(controlador, "El controller de FichaSerie.fxml no se creo");
            controlador.setRegistro(null);
            maquetarAlMinimo(raiz);
            comprobarTextos(raiz, "FichaSerie.fxml");
        } catch (Throwable t) {
            error.set(t);
        } finally {
            latch.countDown();
        }
    }

    private static void revisarFichaTipoRetencion(CountDownLatch latch, AtomicReference<Throwable> error) {
        try {
            FXMLLoader cargador = new FXMLLoader(
                    FichaTipoRetencionController.class.getResource(
                            "/cabofactu/vista/recursos/FichaTipoRetencion.fxml"));
            Parent raiz = cargador.load();
            FichaTipoRetencionController controlador = cargador.getController();
            assertNotNull(controlador, "El controller de FichaTipoRetencion.fxml no se creo");
            controlador.setRegistro(null);
            maquetarAlMinimo(raiz);
            comprobarTextos(raiz, "FichaTipoRetencion.fxml");
        } catch (Throwable t) {
            error.set(t);
        } finally {
            latch.countDown();
        }
    }

    private static void maquetarAlMinimo(Parent raiz) {
        raiz.applyCss();
        raiz.resize(1024, 768);
        raiz.layout();
    }

    private static void comprobarTextos(Parent raiz, String fxml) {
        List<String> cortados = new ArrayList<>();
        for (Node nodo : raiz.lookupAll("*")) {
            if (nodo instanceof Labeled && seVe(nodo)) {
                Labeled etiqueta = (Labeled) nodo;
                if (etiqueta.getWidth() < etiqueta.prefWidth(-1)) {
                    cortados.add(etiqueta.getText() + " (" + etiqueta.getWidth()
                            + " de " + etiqueta.prefWidth(-1) + ")");
                }
            }
        }
        assertTrue(cortados.isEmpty(), "Textos cortados en " + fxml + ": " + cortados);
    }

    private static boolean seVe(Node nodo) {
        Node actual = nodo;
        while (actual != null) {
            if (!actual.isVisible()) {
                return false;
            }
            actual = actual.getParent();
        }
        return true;
    }

    private void await(CountDownLatch latch, AtomicReference<Throwable> error, String fxml) {
        try {
            if (!latch.await(30, TimeUnit.SECONDS)) {
                fail("La vista " + fxml + " no termino de cargarse en 30 s");
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
