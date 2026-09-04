package com.alcazaba.facturacion.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.alcazaba.facturacion.db.Database;
import com.alcazaba.facturacion.model.Cliente;
import com.alcazaba.facturacion.model.LineaFactura;
import com.alcazaba.facturacion.model.Serie;
import com.alcazaba.facturacion.model.TipoIva;
import com.alcazaba.facturacion.service.Servicios;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Verifica que abrir una factura cuyo tipo de IVA esta inactivo no reescribe
 * las lineas, no marca la vista como modificada y muestra el tipo correcto
 * en el desplegable.
 */
class EditorIvaInactivoTest {

    @TempDir
    static Path dataDir;

    private static Servicios servicios;
    private static Navegador nav;
    private static Stage stage;
    private static final Grabador grabador = new Grabador();

    private static final class Grabador implements Dialogos.Impl {
        int errores;
        int infos;

        @Override
        public void error(String titulo, String mensaje) { errores++; }

        @Override
        public void info(String titulo, String mensaje) { infos++; }

        @Override
        public boolean confirmar(String titulo, String mensaje) { return true; }

        @Override
        public Dialogos.CambiosSinGuardar confirmarCambiosSinGuardar() {
            return Dialogos.CambiosSinGuardar.DESCARTAR;
        }
    }

    @BeforeAll
    static void arrancar() throws Exception {
        Database.setDataDir(dataDir);
        servicios = new Servicios();
        JavaFxTestSupport.arrancarFx();
        Dialogos.setImpl(grabador);

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> err = new AtomicReference<>();
        Platform.runLater(() -> {
            try {
                stage = new Stage();
                nav = new Navegador(stage, servicios);
                nav.mostrar("/com/alcazaba/facturacion/ui/Editor.fxml");
                stage.show();
            } catch (Throwable t) {
                err.set(t);
            } finally {
                latch.countDown();
            }
        });
        awaitFx(latch, err);
    }

    @AfterAll
    static void parar() {
        Dialogos.restoreDefault();
        Database.resetConnection();
    }

    @Test
    void facturaConIvaInactivoConservaLineasYTipo() throws Exception {
        // 1. Tipo de IVA 10 %
        TipoIva iva = new TipoIva();
        iva.setNombre("IVA 10%");
        iva.setPorcentaje(10);
        iva.setActivo(true);
        long ivaId = servicios.ivas.insertar(iva);
        iva.setId(ivaId);

        // 2. Cliente
        Cliente cli = new Cliente();
        cli.setNombre("Cliente IVA Inactivo");
        cli.setNif("12345678A");
        long clienteId = servicios.clientes.insertar(cli);
        cli.setId(clienteId);

        // 3. Serie
        Serie serie = new Serie();
        serie.setCodigo("IVA");
        serie.setDescripcion("Test IVA");
        long serieId = servicios.series.insertar(serie);
        serie.setId(serieId);

        // 4. Linea con el tipo 10 %
        LineaFactura linea = new LineaFactura();
        linea.setCantidad(1);
        linea.setDescripcion("Articulo 10%");
        linea.setPrecioUnitario(new BigDecimal("100.00"));
        linea.setTotalBase(new BigDecimal("100.00"));
        linea.setTipoIvaId(ivaId);
        linea.setIvaNombre("IVA 10%");
        linea.setIvaPorcentaje(10);
        linea.setIvaImporte(new BigDecimal("10.00"));

        // 5. Crear factura
        long facturaId = servicios.factura.crearFactura(serie, LocalDate.of(2026, 9, 1),
                cli, List.of(linea), 0, null, null);

        // 6. Inactivar el tipo 10 %
        servicios.ivas.setActivo(ivaId, false);

        // 7. Abrir la factura en el editor y forzar el layout de la tabla
        AtomicReference<Throwable> err = new AtomicReference<>();
        EditorController[] ref = new EditorController[1];
        enFx(err, () -> ref[0] = nav.mostrar("/com/alcazaba/facturacion/ui/Editor.fxml"));
        EditorController ctrl = ref[0];

        enFx(err, () -> ctrl.cargarFactura(facturaId));

        enFx(err, () -> {
            Parent root = (Parent) stage.getScene().getRoot();
            root.applyCss();
            root.layout();
        });

        enFx(err, () -> {
            // 8. Comprobar que la linea conserva su tipo IVA e importe
            @SuppressWarnings("unchecked")
            ObservableList<LineaFactura> lineasCtrl =
                    (ObservableList<LineaFactura>) getField(ctrl, "lineas");
            assertNotNull(lineasCtrl, "lineas no debe ser null");
            assertFalse(lineasCtrl.isEmpty(), "Debe haber al menos una linea");

            LineaFactura l = lineasCtrl.get(0);
            assertEquals(ivaId, l.getTipoIvaId(),
                    "La linea debe conservar su tipo IVA original");
            assertEquals(0, new BigDecimal("10.00").compareTo(l.getIvaImporte()),
                    "La linea debe conservar su importe IVA original");

            // 9. El combo de la celda de IVA debe mostrar el tipo 10 %, no otro
            Parent root = (Parent) stage.getScene().getRoot();
            ComboBox<?> combo = (ComboBox<?>) root.lookup(".table-view .combo-box");
            assertNotNull(combo, "Debe haber un combo de IVA visible");
            Object seleccion = combo.getValue();
            assertNotNull(seleccion, "El combo no debe estar vacio");
            assertTrue(seleccion instanceof TipoIva, "El combo debe contener un TipoIva");
            TipoIva tipoMostrado = (TipoIva) seleccion;
            assertEquals(ivaId, tipoMostrado.getId(),
                    "El combo debe mostrar el tipo 10 %, no otro");
            assertEquals(Integer.valueOf(10), tipoMostrado.getPorcentaje(),
                    "El porcentaje mostrado debe ser 10");

            // 10. La vista NO debe estar marcada como modificada
            boolean mod = (boolean) getField(ctrl, "modificado");
            assertFalse(mod,
                    "Abrir la factura no debe marcarla como modificada");
        });
    }

    @SuppressWarnings("unchecked")
    private static <T> T getField(Object obj, String nombre) {
        try {
            Field f = obj.getClass().getDeclaredField(nombre);
            f.setAccessible(true);
            return (T) f.get(obj);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    private static void enFx(AtomicReference<Throwable> err, Runnable r) {
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
            throw new AssertionError("Fallo en el escenario de IVA inactivo del editor", err.get());
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
