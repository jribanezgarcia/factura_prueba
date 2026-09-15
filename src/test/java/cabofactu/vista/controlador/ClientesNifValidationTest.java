package cabofactu.vista.controlador;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import cabofactu.modelo.negocio.sqlite.Conexion;
import cabofactu.modelo.dominio.Cliente;
import cabofactu.modelo.Modelo;

import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import cabofactu.vista.JavaFxTestSupport;
import cabofactu.vista.Navegador;
import cabofactu.vista.utilidades.Dialogos;

/**
 * Pruebas de UI del flujo de validacion del NIF en la ficha de cliente
 * (tarea 2.3 de add-spanish-tax-id-validation): un NIF no vacio e invalido
 * impide cerrar/guardar la ficha; uno valido permite guardar e insertar.
 *
 * La ficha se construye sin mostrarse (construirFicha) y se abre con show()
 * (no bloqueante) para poder interactuar con los controles reales. Los modales
 * de Dialogos se neutralizan con un Impl grabador.
 */
class ClientesNifValidationTest {

    @TempDir
    static Path carpetaEmpresa;

    private static Modelo modelo;
    private static Navegador nav;
    private static Stage stage;
    private static Grabador grabador;

    private static final class Grabador implements Dialogos.Impl {
        int errores;
        int infos;

        @Override
        public void error(String titulo, String mensaje) {
            errores++;
        }

        @Override
        public void info(String titulo, String mensaje) {
            infos++;
        }

        @Override
        public boolean confirmar(String titulo, String mensaje) {
            return true;
        }

        @Override
        public Dialogos.CambiosSinGuardar confirmarCambiosSinGuardar() {
            return Dialogos.CambiosSinGuardar.CANCELAR;
        }
    }

    @BeforeAll
    static void arrancar() throws Exception {
        Conexion.setCarpetaRaiz(carpetaEmpresa);
        modelo = new Modelo();
        JavaFxTestSupport.arrancarFx();
        grabador = new Grabador();
        Dialogos.setImpl(grabador);

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> err = new AtomicReference<>();
        Platform.runLater(() -> {
            try {
                stage = new Stage();
                nav = new Navegador(stage, modelo);
                nav.mostrar("/cabofactu/vista/recursos/Clientes.fxml");
                stage.show();
            } catch (Throwable t) {
                err.set(t);
            } finally {
                latch.countDown();
            }
        });
        awaitFx(latch, err);
    }

    @BeforeEach
    void resetGrabador() {
        grabador.errores = 0;
        grabador.infos = 0;
    }

    @AfterAll
    static void parar() {
        Dialogos.restoreDefault();
        Conexion.cerrarConexion();
    }

    @Test
    void nifInvalidoEnFichaNoGuarda() {
        AtomicReference<Throwable> err = new AtomicReference<>();
        ClientesController[] ref = new ClientesController[1];
        enFx(err, () -> ref[0] = nav.mostrar("/cabofactu/vista/recursos/Clientes.fxml"));
        ClientesController ctrl = ref[0];
        int antes = totalClientes();

        Dialog<Cliente>[] dref = new Dialog[1];
        enFx(err, () -> dref[0] = ctrl.construirFicha(null));
        Dialog<Cliente> dialogo = dref[0];
        enFx(err, () -> dialogo.show());
        enFx(err, () -> { }); // dejar que el dialogo se materialice
        enFx(err, () -> {
            TextField nombre = (TextField) dialogo.getDialogPane().lookup("#txtNombreFicha");
            TextField nif = (TextField) dialogo.getDialogPane().lookup("#txtNifFicha");
            Button guardar = (Button) dialogo.getDialogPane().lookup("#btnGuardarFicha");
            assertNotNull(nombre, "No se encontro el campo Nombre");
            assertNotNull(nif, "No se encontro el campo NIF");
            assertNotNull(guardar, "No se encontro el boton Guardar");
            nombre.setText("Cliente prueba");
            nif.setText("75238360A"); // invalido
        });
        enFx(err, () -> {
            Button guardar = (Button) dialogo.getDialogPane().lookup("#btnGuardarFicha");
            assertFalse(guardar.isDisable(), "El boton Guardar debe estar habilitado con nombre");
            guardar.fire(); // debe ser consumido por el filtro de NIF invalido
        });
        enFx(err, () -> {
            assertTrue(grabador.errores >= 1, "Debe avisar del NIF invalido");
            assertNull(dialogo.getResult(), "No debe producir cliente con NIF invalido");
        });
        enFx(err, () -> dialogo.hide());

        // No se inserta nada (independiente del orden de ejecucion).
        assertEquals(antes, totalClientes(),
                "Un NIF invalido no debe insertar cliente");
    }

    @Test
    void nifValidoEnFichaPermiteGuardar() {
        AtomicReference<Throwable> err = new AtomicReference<>();
        ClientesController[] ref = new ClientesController[1];
        enFx(err, () -> ref[0] = nav.mostrar("/cabofactu/vista/recursos/Clientes.fxml"));
        ClientesController ctrl = ref[0];
        int antes = totalClientes();

        Dialog<Cliente>[] dref = new Dialog[1];
        enFx(err, () -> dref[0] = ctrl.construirFicha(null));
        Dialog<Cliente> dialogo = dref[0];
        enFx(err, () -> dialogo.show());
        enFx(err, () -> { }); // dejar que el dialogo se materialice
        enFx(err, () -> {
            TextField nombre = (TextField) dialogo.getDialogPane().lookup("#txtNombreFicha");
            TextField nif = (TextField) dialogo.getDialogPane().lookup("#txtNifFicha");
            TextField cp = (TextField) dialogo.getDialogPane().lookup("#txtCpFicha");
            assertNotNull(nombre);
            assertNotNull(nif);
            assertNotNull(cp);
            nombre.setText("Cliente valido");
            nif.setText("12345678Z"); // valido
            cp.setText("28001"); // obligatorio y valido
        });
        enFx(err, () -> {
            Button guardar = (Button) dialogo.getDialogPane().lookup("#btnGuardarFicha");
            guardar.fire(); // no consumido: cierra y produce el cliente
        });
        enFx(err, () -> {
            Cliente c = dialogo.getResult();
            assertNotNull(c, "Debe producir el cliente con NIF valido");
            assertEquals("12345678Z", c.getNif());
            assertEquals("Cliente valido", c.getNombre());
        });

        enFx(err, () -> {
            Cliente c = dialogo.getResult();
            insertar(c);
        });
        assertEquals(antes + 1, totalClientes(),
                "El cliente con NIF valido debe insertarse");
    }

    private static int totalClientes() {
        try {
            return modelo.getClientes().listar(false).size();
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

    private static void insertar(Cliente c) {
        try {
            modelo.getClientes().insertar(c);
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

    private void enFx(AtomicReference<Throwable> err, Runnable r) {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                r.run();
            } catch (Throwable t) {
                err.set(t);
            } finally {
                latch.countDown();
            }
        });
        try {
            if (!latch.await(30, TimeUnit.SECONDS)) {
                fail("Un paso del escenario no termino en 30 s");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            fail("Interrumpido esperando un paso del escenario");
        }
        if (err.get() != null) {
            throw new AssertionError("Fallo en el escenario de NIF de clientes", err.get());
        }
    }

    private static void awaitFx(CountDownLatch latch, AtomicReference<Throwable> err) {
        try {
            if (!latch.await(30, TimeUnit.SECONDS)) {
                fail("JavaFX no arranco en 30 s");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            fail("Interrumpido");
        }
        if (err.get() != null) {
            throw new RuntimeException(err.get());
        }
    }
}
