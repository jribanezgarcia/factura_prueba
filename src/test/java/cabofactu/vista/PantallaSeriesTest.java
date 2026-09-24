package cabofactu.vista;

import cabofactu.modelo.dominio.Serie;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyCode;
import javafx.scene.control.TextField;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * La sección Series como la ve el usuario: la tabla con el siguiente
 * calculado, la ficha con su ejemplo y sus avisos, y el borrado.
 */
class PantallaSeriesTest extends PruebaDePantalla {

    private void abrirSeries() throws Exception {
        mostrarPantalla("Configuracion.fxml");
        pulsar("Series");
        esperarNodo("#tablaSeries", TableView.class);
    }

    private TableView<Serie> tabla() throws Exception {
        return buscar("#tablaSeries", TableView.class);
    }

    private int filaDe(String codigo) throws Exception {
        TableView<Serie> tabla = tabla();
        for (int i = 0; i < tabla.getItems().size(); i++) {
            if (tabla.getItems().get(i).getCodigo().equals(codigo)) {
                return i;
            }
        }
        return -1;
    }

    private Node filaNodo(String codigo) throws Exception {
        for (Node nodo : nodosVisibles("#tablaSeries .table-row-cell")) {
            if (nodo instanceof TableRow) {
                TableRow<?> fila = (TableRow<?>) nodo;
                Object dato = fila.getItem();
                if (dato instanceof Serie) {
                    Serie serie = (Serie) dato;
                    if (serie.getCodigo().equals(codigo)) {
                        return nodo;
                    }
                }
            }
        }
        throw new AssertionError("No se encontró la fila " + codigo);
    }

    private void pulsarFila(String codigo) throws Exception {
        clickOn(filaNodo(codigo));
    }

    private String textoCelda(String codigo, String columna) throws Exception {
        int fila = filaDe(codigo);
        for (Node nodo : nodosVisibles("#tablaSeries .table-cell")) {
            if (nodo instanceof TableCell) {
                TableCell<?, ?> celda = (TableCell<?, ?>) nodo;
                if (celda.getIndex() == fila && celda.getTableColumn().getText().equals(columna)) {
                    return celda.getText();
                }
            }
        }
        return "";
    }

    private void abrirFichaNueva() throws Exception {
        abrirSeries();
        pulsarBoton("#seccionSeries", "Nuevo");
        esperarNodo("#txtCodigo", TextField.class);
    }

    @Test
    void elCodigoMaloAvisaYSeMarcaEnRojo() throws Exception {
        abrirFichaNueva();
        escribirEn("#txtCodigo", "B%");
        pulsar("Añadir");
        cerrarAviso();
        TextField codigo = buscar("#txtCodigo", TextField.class);
        assertTrue(codigo.getStyleClass().contains("campo-error"));
        pulsar("Cancelar");
        push(KeyCode.ENTER);
    }

    @Test
    void columnasYSiguienteCalculado() throws Exception {
        abrirSeries();
        assertEquals(2, tabla().getItems().size());
        assertEquals("A", textoCelda("A", "Código"));
        assertEquals("Serie general", textoCelda("A", "Descripción"));
        assertEquals("No", textoCelda("A", "Rectificativa"));
        assertEquals("R", textoCelda("R", "Código"));
        assertEquals("Sí", textoCelda("R", "Rectificativa"));
        assertEquals("6", textoCelda("A", "Siguiente (2026)"));
        Label titulo = buscar("Siguiente (2026)", Label.class);
        assertEquals("Siguiente (2026)", titulo.getText());
    }

    @Test
    void formatoYEjemploCambian() throws Exception {
        abrirFichaNueva();
        Label ejemplo = buscar("#lblEjemplo", Label.class);
        assertEquals("Previsualización: 56/7", ejemplo.getText());
        escribirEn("#txtCodigo", "C");
        assertEquals("Previsualización: C-56/7", ejemplo.getText());
        clickOn(buscar("#comboFormato", ComboBox.class));
        pulsar("Código-Número-Año (ej: C-56-2026)");
        assertEquals("Previsualización: C-56-2026", ejemplo.getText());
        clickOn(buscar("#chkRectificativa", CheckBox.class));
        assertEquals("Previsualización: C-56", ejemplo.getText());
        pulsar("Cancelar");
        push(KeyCode.ENTER);
    }

    @Test
    void codigoRepetidoAvisaYSigueAbierta() throws Exception {
        abrirFichaNueva();
        escribirEn("#txtCodigo", "A");
        pulsar("Añadir");
        assertTrue(textoAviso().contains("Ya existe una serie"));
        cerrarAviso();
        esperarNodo("#txtCodigo", TextField.class);
        pulsar("Cancelar");
        assertTrue(textoAviso().contains("descartar"));
        aceptarAviso();
        assertEquals(2, tabla().getItems().size());
    }

    @Test
    void dosSeriesSinCodigo() throws Exception {
        abrirFichaNueva();
        pulsar("Añadir");
        assertEquals(3, tabla().getItems().size());
        assertEquals("(sin código)", textoCelda("", "Código"));
        pulsarBoton("#seccionSeries", "Nuevo");
        pulsar("Añadir");
        assertTrue(textoAviso().contains("sin código"));
        cerrarAviso();
        pulsar("Cancelar");
    }

    @Test
    void dobleClicEdita() throws Exception {
        abrirSeries();
        doubleClickOn(filaNodo("A"));
        escribirEn("#txtDescripcion", "Serie general editada");
        pulsar("Guardar");
        assertEquals("Serie general editada", textoCelda("A", "Descripción"));
    }

    @Test
    void editarConCodigoRepetidoSeCorrigeYGuarda() throws Exception {
        abrirSeries();
        doubleClickOn(filaNodo("A"));
        escribirEn("#txtCodigo", "R");
        pulsar("Guardar");
        assertTrue(textoAviso().contains("Ya existe una serie"));
        cerrarAviso();
        escribirEn("#txtCodigo", "B");
        pulsar("Guardar");
        assertTrue(filaDe("B") >= 0);
        assertEquals(-1, filaDe("A"));
    }

    @Test
    void cancelarConCambiosPregunta() throws Exception {
        abrirFichaNueva();
        escribirEn("#txtCodigo", "B");
        pulsar("Cancelar");
        assertTrue(textoAviso().contains("descartar"));
        aceptarAviso();
        assertEquals(2, tabla().getItems().size());
    }

    @Test
    void eliminarSinFacturas() throws Exception {
        abrirFichaNueva();
        escribirEn("#txtCodigo", "B");
        escribirEn("#txtDescripcion", "Serie B");
        pulsar("Añadir");
        pulsarFila("B");
        pulsarBoton("#seccionSeries", "Eliminar");
        aceptarAviso();
        assertEquals(-1, filaDe("B"));
    }

    @Test
    void eliminarConFacturasAvisa() throws Exception {
        abrirSeries();
        pulsarFila("A");
        pulsarBoton("#seccionSeries", "Eliminar");
        aceptarAviso();
        assertTrue(textoAviso().contains("facturas"));
        cerrarAviso();
        assertTrue(filaDe("A") >= 0);
    }
}
