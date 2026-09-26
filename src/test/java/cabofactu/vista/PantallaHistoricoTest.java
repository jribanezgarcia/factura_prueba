package cabofactu.vista;

import cabofactu.modelo.dominio.EstadoFactura;
import cabofactu.modelo.dominio.Factura;
import cabofactu.modelo.dominio.FiltrosHistorial;
import cabofactu.modelo.negocio.Facturas;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.PickResult;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * El histórico como lo ve el usuario: la búsqueda con sus filtros, la
 * apertura con doble clic, y el anulado y el borrado con sus avisos. Cada
 * factura sale una sola vez, aunque se haya editado o anulado.
 */
class PantallaHistoricoTest extends PruebaDePantalla {

    private void abrirHistorico() throws Exception {
        mostrarPantalla("MenuPrincipal.fxml");
        pulsar("Histórico");
        esperarNodo("#tabla", TableView.class);
    }

    private int filas() throws Exception {
        return buscar("#tabla", TableView.class).getItems().size();
    }

    private int filaDe(String numero) throws Exception {
        TableView<Factura> tabla = buscar("#tabla", TableView.class);
        for (int i = 0; i < tabla.getItems().size(); i++) {
            if (tabla.getItems().get(i).getNumero().equals(numero)) {
                return i;
            }
        }
        return -1;
    }

    private Node filaNodo(String numero) throws Exception {
        for (Node nodo : nodosVisibles("#tabla .table-row-cell")) {
            if (nodo instanceof TableRow) {
                TableRow<?> fila = (TableRow<?>) nodo;
                Object dato = fila.getItem();
                if (dato instanceof Factura) {
                    Factura factura = (Factura) dato;
                    if (factura.getNumero().equals(numero)) {
                        return nodo;
                    }
                }
            }
        }
        throw new AssertionError("No se encontró la fila " + numero);
    }

    private String textoCelda(String numero, String columna) throws Exception {
        int fila = filaDe(numero);
        for (Node nodo : nodosVisibles("#tabla .table-cell")) {
            if (nodo instanceof TableCell) {
                TableCell<?, ?> celda = (TableCell<?, ?>) nodo;
                if (celda.getIndex() == fila && celda.getTableColumn().getText().equals(columna)) {
                    return celda.getText();
                }
            }
        }
        return "";
    }

    private void elegirEn(String combo, String valor) throws Exception {
        clickOn(buscar(combo, ComboBox.class));
        pulsar(valor);
    }

    /**
     * Ponemos las fechas del filtro directamente en los DatePicker, en el hilo
     * de JavaFX. Con las dos a null quitamos el filtro de fecha, necesario
     * porque la demostración es de 2026 y la fecha de trabajo de los tests es
     * la de hoy.
     */
    private void ponerFechas(LocalDate desde, LocalDate hasta) throws Exception {
        DatePicker campoDesde = buscar("#fechaDesde", DatePicker.class);
        DatePicker campoHasta = buscar("#fechaHasta", DatePicker.class);
        FutureTask<Void> tarea = new FutureTask<>(() -> {
            campoDesde.setValue(desde);
            campoHasta.setValue(hasta);
            return null;
        });
        Platform.runLater(tarea);
        tarea.get(30, TimeUnit.SECONDS);
    }

    private void quitarFechas() throws Exception {
        ponerFechas(null, null);
    }

    private void seleccionarFilas(String numero1, String numero2) throws Exception {
        TableView<Factura> tabla = buscar("#tabla", TableView.class);
        int indice1 = filaDe(numero1);
        int indice2 = filaDe(numero2);
        FutureTask<Void> tarea = new FutureTask<>(() -> {
            tabla.getSelectionModel().select(indice1);
            tabla.getSelectionModel().select(indice2);
            return null;
        });
        Platform.runLater(tarea);
        tarea.get(30, TimeUnit.SECONDS);
    }

    @Test
    void alEntrarVeYaLasFacturasDelAnioDeTrabajo() throws Exception {
        abrirHistorico();
        int anio = LocalDate.now().getYear();
        DatePicker desde = buscar("#fechaDesde", DatePicker.class);
        DatePicker hasta = buscar("#fechaHasta", DatePicker.class);
        assertEquals(LocalDate.of(anio, 1, 1), desde.getValue());
        assertEquals(LocalDate.of(anio, 12, 31), hasta.getValue());
        FiltrosHistorial filtros = new FiltrosHistorial(null, null, desde.getValue(), hasta.getValue(),
                null, null, null);
        assertEquals(Facturas.getFacturas().listado(filtros).size(), filas());
    }

    @Test
    void buscarMuestraLasSeis() throws Exception {
        abrirHistorico();
        quitarFechas();
        pulsar("Buscar");
        assertEquals(6, filas());
        assertTrue(textoCelda("A-1/9", "Total").contains("1.760,00"));
        assertTrue(textoCelda("A-4/9", "Total").contains("1.310,00"));
        assertEquals("Anulada", textoCelda("A-5/9", "Estado"));
        assertEquals("Emitida", textoCelda("R-1", "Estado"));
    }

    @Test
    void filtrarPorSerieR() throws Exception {
        abrirHistorico();
        quitarFechas();
        elegirEn("#comboSerie", "R (Rectificativas)");
        pulsar("Buscar");
        assertEquals(1, filas());
        assertEquals("R-1", textoCelda("R-1", "Número"));
    }

    @Test
    void importeMalEscritoAvisaYMarcaElCampo() throws Exception {
        abrirHistorico();
        escribirEn("#txtImporteDesde", "12,5x");
        pulsar("Buscar");
        assertTrue(textoAviso().contains("no es un importe válido"));
        cerrarAviso();
        TextField campo = buscar("#txtImporteDesde", TextField.class);
        assertTrue(campo.getStyleClass().contains("campo-error"));
    }

    @Test
    void fechasAlRevesAvisaYMarcaLasDos() throws Exception {
        abrirHistorico();
        ponerFechas(LocalDate.of(2026, 12, 31), LocalDate.of(2026, 1, 1));
        pulsar("Buscar");
        assertTrue(textoAviso().contains("posterior a la fecha hasta"));
        cerrarAviso();
        DatePicker desde = buscar("#fechaDesde", DatePicker.class);
        DatePicker hasta = buscar("#fechaHasta", DatePicker.class);
        assertTrue(desde.getStyleClass().contains("campo-error"));
        assertTrue(hasta.getStyleClass().contains("campo-error"));
    }

    @Test
    void anularDesdeHistorico() throws Exception {
        abrirHistorico();
        quitarFechas();
        pulsar("Buscar");
        clickOn(filaNodo("A-3/9"));
        pulsar("Anular");
        assertTrue(textoAviso().contains("1 factura(s)"));
        aceptarAviso();
        assertTrue(textoAviso().contains("Anuladas: 1"));
        cerrarAviso();
        pulsar("Buscar");
        assertEquals(6, filas());
        assertTrue(hayAnulada("A-3/9"));
    }

    private boolean hayAnulada(String numero) throws Exception {
        TableView<Factura> tabla = buscar("#tabla", TableView.class);
        for (int i = 0; i < tabla.getItems().size(); i++) {
            Factura fila = tabla.getItems().get(i);
            if (fila.getNumero().equals(numero) && fila.getEstado() == EstadoFactura.ANULADA) {
                return true;
            }
        }
        return false;
    }

    @Test
    void eliminarFisico() throws Exception {
        abrirHistorico();
        quitarFechas();
        pulsar("Buscar");
        clickOn(filaNodo("R-1"));
        pulsar("Eliminar");
        assertTrue(textoAviso().contains("línea(s)"));
        aceptarAviso();
        assertTrue(textoAviso().contains("Eliminadas: 1"));
        cerrarAviso();
        pulsar("Buscar");
        assertEquals(5, filas());
        assertEquals(-1, filaDe("R-1"));
    }

    @Test
    void borrarRectificadaAvisa() throws Exception {
        abrirHistorico();
        quitarFechas();
        pulsar("Buscar");
        clickOn(filaNodo("A-1/9"));
        pulsar("Eliminar");
        assertTrue(textoAviso().contains("línea(s)"));
        aceptarAviso();
        assertTrue(textoAviso().contains("A-1/9"));
        assertTrue(textoAviso().contains("Tiene la rectificativa R-1 y no se puede eliminar"));
        cerrarAviso();
        pulsar("Buscar");
        assertEquals(6, filas());
        assertTrue(filaDe("A-1/9") >= 0);
    }

    @Test
    void dobleClicAbreEditor() throws Exception {
        abrirHistorico();
        quitarFechas();
        pulsar("Buscar");
        doubleClickOn(filaNodo("A-1/9"));
        assertEquals("Factura A-1/9", buscar("#lblTitulo", Label.class).getText());
    }

    @Test
    void menuDelClicDerecho() throws Exception {
        abrirHistorico();
        quitarFechas();
        pulsar("Buscar");
        rightClickOn(filaNodo("A-1/9"));
        dispararMenuContextual();
        assertFalse(nodosVisibles("Exportar a PDF").isEmpty());
        assertFalse(nodosVisibles("Anular facturas seleccionadas").isEmpty());
        assertFalse(nodosVisibles("Eliminar facturas seleccionadas").isEmpty());
    }

    /**
     * En TestFX headless el robot no sintetiza el ContextMenuEvent de la
     * plataforma a partir del clic derecho, así que lo disparamos a mano sobre
     * la tabla para que se abra el menú que ya lleva su contextMenu del FXML.
     */
    private void dispararMenuContextual() throws Exception {
        TableView<Factura> tabla = buscar("#tabla", TableView.class);
        FutureTask<Void> tarea = new FutureTask<>(() -> {
            PickResult resultado = new PickResult(tabla, 5, 5);
            Event.fireEvent(tabla, new ContextMenuEvent(ContextMenuEvent.CONTEXT_MENU_REQUESTED,
                    5, 5, 200, 200, false, resultado));
            return null;
        });
        Platform.runLater(tarea);
        tarea.get(30, TimeUnit.SECONDS);
    }

    @Test
    void exportarVariasPreguntaElFormato() throws Exception {
        abrirHistorico();
        quitarFechas();
        pulsar("Buscar");
        seleccionarFilas("A-2/9", "A-3/9");
        pulsar("Exportar");
        assertFalse(nodosVisibles("Un PDF por factura").isEmpty());
        assertFalse(nodosVisibles("Todas en un PDF").isEmpty());
        assertFalse(nodosVisibles("Cancelar").isEmpty());
        cancelarAviso();
    }
}
