package cabofactu.vista;

import cabofactu.modelo.dominio.EstadoFactura;
import cabofactu.modelo.dominio.FilaHistorial;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * El histórico como lo ve el usuario: la búsqueda con sus filtros, la
 * apertura con doble clic, y el anulado y el borrado con sus avisos.
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
        TableView<FilaHistorial> tabla = buscar("#tabla", TableView.class);
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
                if (dato instanceof FilaHistorial) {
                    FilaHistorial filaHistorial = (FilaHistorial) dato;
                    if (filaHistorial.getNumero().equals(numero)) {
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

    @Test
    void buscarMuestraLasSeis() throws Exception {
        abrirHistorico();
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
        elegirEn("#comboSerie", "R");
        pulsar("Buscar");
        assertEquals(1, filas());
        assertEquals("R-1", textoCelda("R-1", "Número"));
    }

    @Test
    void anularDesdeHistorico() throws Exception {
        abrirHistorico();
        pulsar("Buscar");
        clickOn(filaNodo("A-3/9"));
        pulsar("Anular");
        assertTrue(textoAviso().contains("1 factura(s)"));
        aceptarAviso();
        assertTrue(textoAviso().contains("Anuladas: 1"));
        cerrarAviso();
        pulsar("Buscar");
        assertEquals(7, filas());
        assertTrue(hayVersionAnulada("A-3/9"));
    }

    private boolean hayVersionAnulada(String numero) throws Exception {
        TableView<FilaHistorial> tabla = buscar("#tabla", TableView.class);
        for (int i = 0; i < tabla.getItems().size(); i++) {
            FilaHistorial fila = tabla.getItems().get(i);
            if (fila.getNumero().equals(numero) && fila.getEstado() == EstadoFactura.ANULADA) {
                return true;
            }
        }
        return false;
    }

    @Test
    void eliminarFisico() throws Exception {
        abrirHistorico();
        pulsar("Buscar");
        clickOn(filaNodo("R-1"));
        pulsar("Eliminar");
        assertTrue(textoAviso().contains("físicamente"));
        aceptarAviso();
        assertTrue(textoAviso().contains("Eliminadas: 1"));
        cerrarAviso();
        pulsar("Buscar");
        assertEquals(5, filas());
        assertEquals(-1, filaDe("R-1"));
    }

    @Test
    void dobleClicAbreEditor() throws Exception {
        abrirHistorico();
        pulsar("Buscar");
        doubleClickOn(filaNodo("A-1/9"));
        assertEquals("Factura A-1/9 (v1)", buscar("#lblTitulo", Label.class).getText());
    }
}
