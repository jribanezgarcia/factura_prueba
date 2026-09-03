package com.alcazaba.facturacion.ui;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.fail;

import com.alcazaba.facturacion.db.Database;
import com.alcazaba.facturacion.service.Servicios;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * `Navegador.mostrar()` no debe abandonar una vista cuyo `puedeCerrar()`
 * devuelve `false`: la escena queda intacta y el retorno es `null`.
 */
class NavegacionCambiosSinGuardarTest {

    private static final String VISTA = "/com/alcazaba/facturacion/ui/VistaPrueba.fxml";

    @TempDir
    static Path dataDir;

    @BeforeAll
    static void arrancar() throws Exception {
        Database.setDataDir(dataDir);
        JavaFxTestSupport.arrancarFx();
    }

    @AfterEach
    void cerrarDb() {
        Database.resetConnection();
        VistaPrueba.bloquear = false;
    }

    @Test
    void vistaBloqueadaNoSeAbandona() throws Exception {
        Object[] r = navegar(true);
        assertNull(r[0], "Navegar desde una vista bloqueada debe devolver null");
        assertSame(r[1], r[2], "La escena del Stage no debe cambiar");
    }

    @Test
    void vistaDesbloqueadaNavegaConNormalidad() throws Exception {
        Object[] r = navegar(false);
        assertNotNull(r[0], "Navegar desde una vista desbloqueada debe devolver la vista");
    }

    private Object[] navegar(boolean bloquear) throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> error = new AtomicReference<>();
        Object[] resultado = new Object[3];

        Platform.runLater(() -> {
            try {
                VistaPrueba.bloquear = bloquear;
                Stage stage = new Stage();
                Navegador nav = new Navegador(stage, new Servicios());
                nav.mostrar(VISTA);
                Scene escenaOrigen = stage.getScene();
                Object retorno = nav.mostrar(VISTA);
                resultado[0] = retorno;
                resultado[1] = escenaOrigen;
                resultado[2] = stage.getScene();
                stage.hide();
            } catch (Throwable t) {
                error.set(t);
            } finally {
                latch.countDown();
            }
        });

        if (!latch.await(30, TimeUnit.SECONDS)) {
            fail("La navegacion no termino en 30 s");
        }
        if (error.get() != null) {
            throw new RuntimeException(error.get());
        }
        return resultado;
    }
}
