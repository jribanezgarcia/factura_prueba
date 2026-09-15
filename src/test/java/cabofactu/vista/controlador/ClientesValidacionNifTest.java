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
import cabofactu.modelo.negocio.ValidacionException;

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
import cabofactu.vista.PruebasJavaFx;
import cabofactu.vista.Navegador;
import cabofactu.vista.utilidades.CambiosSinGuardar;
import cabofactu.vista.utilidades.Dialogos;
import cabofactu.vista.utilidades.MostradorDialogos;

/**
 * Pruebas de UI del flujo de validacion del NIF en la ficha de cliente
 * (tarea 2.3 de add-spanish-tax-id-validation): un NIF no vacio e invalido
 * impide cerrar/guardar la ficha; uno valido permite guardar e insertar.
 *
 * La ficha se construye sin mostrarse (construirFicha) y se abre con show()
 * (no bloqueante) para poder interactuar con los controles reales. Los modales
 * de Dialogos se neutralizan con un Impl grabador.
 */
class ClientesValidacionNifTest {

    @TempDir
    static Path carpetaEmpresa;

    private static Modelo modelo;
    private static Navegador nav;
    private static Stage stage;
    private static Grabador grabador;

    private static final class Grabador implements MostradorDialogos {
        int errores;
        int infos;
        String ultimoError;

        @Override
        public void error(String titulo, String mensaje) {
            errores++;
            ultimoError = mensaje;
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
        public CambiosSinGuardar confirmarCambiosSinGuardar() {
            return CambiosSinGuardar.CANCELAR;
        }
    }

    @BeforeAll
    static void arrancar() throws Exception {
        Conexion.setCarpetaRaiz(carpetaEmpresa);
        modelo = new Modelo();
        PruebasJavaFx.arrancarFx();
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
        grabador.ultimoError = null;
    }

    @AfterAll
    static void parar() {
        Dialogos.restoreDefault();
        Conexion.cerrarConexion();
    }

    @Test
    void nifConFormatoIncorrectoAvisaSoloAlGuardar() {
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
            assertNotNull(nombre, "No se encontro el campo Nombre");
            assertNotNull(nif, "No se encontro el campo NIF");
            nombre.setText("Cliente prueba");
            nif.setText("123"); // sin forma de documento
            nif.requestFocus();
        });
        enFx(err, () -> {
            TextField nombre = (TextField) dialogo.getDialogPane().lookup("#txtNombreFicha");
            nombre.requestFocus(); // el NIF pierde el foco
        });
        enFx(err, () -> {
            TextField nif = (TextField) dialogo.getDialogPane().lookup("#txtNifFicha");
            assertFalse(nif.getStyle().contains("#d32f2f"), "Al salir del campo no hay rojo ni aviso");
            assertEquals(0, grabador.errores, "Al salir del campo no hay aviso");
        });
        enFx(err, () -> {
            Button guardar = (Button) dialogo.getDialogPane().lookup("#btnGuardarFicha");
            assertFalse(guardar.isDisable(), "El boton Guardar debe estar habilitado con nombre");
            guardar.fire(); // consumido: un solo aviso y sin resultado
        });
        enFx(err, () -> {
            assertEquals(1, grabador.errores, "Al guardar hay un solo aviso");
            assertEquals("Formato NIF/NIE incorrecto. Debe ser como 12345678Z (DNI), X1234567L (NIE) o B12345674 (CIF).",
                    grabador.ultimoError);
            TextField nif = (TextField) dialogo.getDialogPane().lookup("#txtNifFicha");
            assertTrue(nif.getStyle().contains("#d32f2f"), "Al guardar el campo queda en rojo");
            assertNull(dialogo.getResult(), "No debe producir cliente con NIF invalido");
        });
        enFx(err, () -> dialogo.hide());

        // No se inserta nada (independiente del orden de ejecucion).
        assertEquals(antes, totalClientes(),
                "Un NIF invalido no debe insertar cliente");
    }

    @Test
    void nifConLetraIncorrectaAvisaSoloAlGuardar() {
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
            nombre.setText("Cliente prueba");
            nif.setText("12345678A"); // forma bien, letra mal
            nif.requestFocus();
        });
        enFx(err, () -> {
            TextField nombre = (TextField) dialogo.getDialogPane().lookup("#txtNombreFicha");
            nombre.requestFocus(); // el NIF pierde el foco
        });
        enFx(err, () -> {
            TextField nif = (TextField) dialogo.getDialogPane().lookup("#txtNifFicha");
            assertFalse(nif.getStyle().contains("#d32f2f"), "Al salir del campo no hay rojo ni aviso");
            assertEquals(0, grabador.errores, "Al salir del campo no hay aviso");
        });
        enFx(err, () -> {
            Button guardar = (Button) dialogo.getDialogPane().lookup("#btnGuardarFicha");
            guardar.fire(); // consumido: un solo aviso y sin resultado
        });
        enFx(err, () -> {
            assertEquals(1, grabador.errores, "Al guardar hay un solo aviso");
            assertEquals("La letra no es correcta.", grabador.ultimoError);
            TextField nif = (TextField) dialogo.getDialogPane().lookup("#txtNifFicha");
            assertTrue(nif.getStyle().contains("#d32f2f"), "Al guardar el campo queda en rojo");
            assertNull(dialogo.getResult(), "No debe producir cliente con NIF invalido");
        });
        enFx(err, () -> dialogo.hide());

        assertEquals(antes, totalClientes(),
                "Un NIF invalido no debe insertar cliente");
    }

    @Test
    void nifVacioAvisaAlGuardar() {
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
            nombre.setText("Cliente prueba");
        });
        enFx(err, () -> {
            Button guardar = (Button) dialogo.getDialogPane().lookup("#btnGuardarFicha");
            guardar.fire(); // consumido: un solo aviso y sin resultado
        });
        enFx(err, () -> {
            assertEquals(1, grabador.errores, "Al guardar hay un solo aviso");
            assertEquals("El NIF/NIE es obligatorio.", grabador.ultimoError);
            TextField nif = (TextField) dialogo.getDialogPane().lookup("#txtNifFicha");
            assertTrue(nif.getStyle().contains("#d32f2f"), "Al guardar el campo queda en rojo");
            assertNull(dialogo.getResult(), "No debe producir cliente sin NIF");
        });
        enFx(err, () -> dialogo.hide());

        assertEquals(antes, totalClientes(),
                "Un NIF vacio no debe insertar cliente");
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
            assertNotNull(nombre);
            assertNotNull(nif);
            nombre.setText("Cliente valido");
            nif.setText("12345678Z"); // valido
            rellenarObligatorios(dialogo, "Calle Valida 1", "28001", "Madrid", "Madrid");
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

    private void rellenarObligatorios(Dialog<Cliente> dialogo, String direccion, String cp,
            String localidad, String provincia) {
        for (String[] par : new String[][]{{"#txtDireccionFicha", direccion}, {"#txtCpFicha", cp},
                {"#txtLocalidadFicha", localidad}, {"#txtProvinciaFicha", provincia}}) {
            TextField campo = (TextField) dialogo.getDialogPane().lookup(par[0]);
            assertNotNull(campo, "No se encontro el campo " + par[0]);
            campo.setText(par[1]);
        }
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
        } catch (ValidacionException | RuntimeException e) {
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
