package com.alcazaba.facturacion.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.alcazaba.facturacion.db.Database;
import com.alcazaba.facturacion.model.LineaFactura;
import com.alcazaba.facturacion.service.Servicios;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Con y sin descuento global, exactamente una fila del bloque de totales
 * lleva la clase de la esquina superior redondeada.
 */
class EditorTotalesDescuentoTest {

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
    }

    private static Object campo(Object o, String nombre) throws Exception {
        Field f = o.getClass().getDeclaredField(nombre);
        f.setAccessible(true);
        return f.get(o);
    }

    private static void campo(Object o, String nombre, Object valor) throws Exception {
        Field f = o.getClass().getDeclaredField(nombre);
        f.setAccessible(true);
        f.set(o, valor);
    }

    @Test
    void soloLaPrimeraFilaVisibleLlevaLaEsquina() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> error = new AtomicReference<>();
        AtomicReference<EditorController> ctrlRef = new AtomicReference<>();
        AtomicReference<Parent> rootRef = new AtomicReference<>();

        Platform.runLater(() -> {
            try {
                Stage stage = new Stage();
                Navegador nav = new Navegador(stage, new Servicios());
                EditorController ctrl = nav.mostrar("/com/alcazaba/facturacion/ui/Editor.fxml");
                stage.show();
                ctrlRef.set(ctrl);
                rootRef.set((Parent) stage.getScene().getRoot());
            } catch (Throwable t) {
                error.set(t);
            } finally {
                latch.countDown();
            }
        });

        if (!latch.await(30, TimeUnit.SECONDS)) {
            fail("JavaFX no cargo el Editor en 30 s");
        }
        if (error.get() != null) {
            throw new RuntimeException(error.get());
        }

        CountDownLatch check = new CountDownLatch(1);
        AtomicReference<Throwable> checkError = new AtomicReference<>();
        Platform.runLater(() -> {
            try {
                comprobar(ctrlRef.get(), rootRef.get());
            } catch (Throwable t) {
                checkError.set(t);
            } finally {
                check.countDown();
            }
        });

        if (!check.await(30, TimeUnit.SECONDS)) {
            fail("La comprobacion no termino en 30 s");
        }
        if (checkError.get() != null) {
            throw new AssertionError("Fallo la comprobacion del bloque de totales", checkError.get());
        }
    }

    private void comprobar(EditorController ctrl, Parent root) throws Exception {
        LineaFactura l = new LineaFactura();
        l.setCantidad(1);
        l.setPrecioUnitario(new BigDecimal("1000.00"));
        l.setTotalBase(new BigDecimal("1000.00"));
        l.setIvaNombre("IVA 21%");
        l.setIvaPorcentaje(21);

        @SuppressWarnings("unchecked")
        ObservableList<LineaFactura> lineas = (ObservableList<LineaFactura>) campo(ctrl, "lineas");
        lineas.setAll(List.of(l));
        Method resumen = ctrl.getClass().getDeclaredMethod("actualizarResumen");
        resumen.setAccessible(true);

        Node bruta = root.lookup("#filaBaseBruta");
        Node imponible = root.lookup("#filaBaseImponible");

        campo(ctrl, "descuento", 10);
        resumen.invoke(ctrl);
        root.applyCss();
        root.layout();
        assertTrue(bruta.isVisible(), "Con descuento la fila de subtotal debe verse");
        assertTrue(bruta.getStyleClass().contains("total-fila-primera"),
                "Con descuento la esquina debe estar en el subtotal");
        assertFalse(imponible.getStyleClass().contains("total-fila-primera"),
                "Con descuento la base imponible no debe llevar la esquina");
        assertEquals(1, contarPrimeras(root), "Debe haber exactamente una fila con esquina");

        campo(ctrl, "descuento", 0);
        resumen.invoke(ctrl);
        root.applyCss();
        root.layout();
        assertFalse(bruta.isVisible(), "Sin descuento la fila de subtotal debe ocultarse");
        assertTrue(imponible.getStyleClass().contains("total-fila-primera"),
                "Sin descuento la esquina debe volver a la base imponible");
        assertEquals(1, contarPrimeras(root), "Debe haber exactamente una fila con esquina");
    }

    private int contarPrimeras(Parent root) {
        Set<Node> nodos = root.lookupAll(".total-fila-primera");
        return nodos.size();
    }
}
