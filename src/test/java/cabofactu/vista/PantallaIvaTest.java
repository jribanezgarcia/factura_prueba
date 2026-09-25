package cabofactu.vista;

import cabofactu.modelo.dominio.TipoIva;
import javafx.scene.Node;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * La sección IVA como la ve el usuario: la tabla con los tipos, la ficha con
 * sus avisos, y el borrado con su confirmación.
 */
class PantallaIvaTest extends PruebaDePantalla {

    private void abrirIva() throws Exception {
        mostrarPantalla("Configuracion.fxml");
        pulsar("IVA");
        esperarNodo("#tablaIva", TableView.class);
    }

    private TableView<TipoIva> tabla() throws Exception {
        return buscar("#tablaIva", TableView.class);
    }

    private int filaDe(String nombre) throws Exception {
        TableView<TipoIva> tabla = tabla();
        for (int i = 0; i < tabla.getItems().size(); i++) {
            if (tabla.getItems().get(i).getNombre().equals(nombre)) {
                return i;
            }
        }
        return -1;
    }

    private Node filaNodo(String nombre) throws Exception {
        for (Node nodo : nodosVisibles("#tablaIva .table-row-cell")) {
            if (nodo instanceof TableRow) {
                TableRow<?> fila = (TableRow<?>) nodo;
                Object dato = fila.getItem();
                if (dato instanceof TipoIva) {
                    TipoIva tipo = (TipoIva) dato;
                    if (tipo.getNombre().equals(nombre)) {
                        return nodo;
                    }
                }
            }
        }
        throw new AssertionError("No se encontró la fila " + nombre);
    }

    private void pulsarFila(String nombre) throws Exception {
        clickOn(filaNodo(nombre));
    }

    private String textoCelda(String nombre, String columna) throws Exception {
        int fila = filaDe(nombre);
        for (Node nodo : nodosVisibles("#tablaIva .table-cell")) {
            if (nodo instanceof TableCell) {
                TableCell<?, ?> celda = (TableCell<?, ?>) nodo;
                if (celda.getIndex() == fila && celda.getTableColumn().getText().equals(columna)) {
                    return celda.getText();
                }
            }
        }
        return "";
    }

    @Test
    void tablaMuestraLosCuatroTipos() throws Exception {
        abrirIva();
        assertEquals(4, tabla().getItems().size());
        assertEquals("21%", textoCelda("IVA 21%", "Tipo"));
        assertEquals("No", textoCelda("IVA 21%", "Suplido"));
        assertEquals("Exento", textoCelda("Exento", "Tipo"));
        assertEquals("Sí", textoCelda("Suplido", "Suplido"));
    }

    @Test
    void nuevoValido() throws Exception {
        abrirIva();
        pulsarBoton("#seccionIva", "Nuevo");
        escribirEn("#txtNombre", "IVA 5%");
        escribirEn("#txtPorcentaje", "5");
        pulsar("Añadir");
        assertEquals(5, tabla().getItems().size());
        assertEquals("5%", textoCelda("IVA 5%", "Tipo"));
    }

    @Test
    void nombreRepetidoAvisaYSeMarcaEnRojo() throws Exception {
        abrirIva();
        pulsarBoton("#seccionIva", "Nuevo");
        escribirEn("#txtNombre", "IVA 21%");
        escribirEn("#txtPorcentaje", "21");
        pulsar("Añadir");
        assertTrue(textoAviso().contains("Ya existe un tipo de IVA con el nombre IVA 21%."));
        cerrarAviso();
        TextField nombre = buscar("#txtNombre", TextField.class);
        assertTrue(nombre.getStyleClass().contains("campo-error"));
        assertEquals(4, tabla().getItems().size());
        pulsar("Cancelar");
        assertTrue(textoAviso().contains("descartar"));
        aceptarAviso();
    }

    @Test
    void porcentajeMaloAvisaYSeMarcaEnRojo() throws Exception {
        abrirIva();
        pulsarBoton("#seccionIva", "Nuevo");
        escribirEn("#txtNombre", "Malo");
        escribirEn("#txtPorcentaje", "abc");
        pulsar("Añadir");
        assertTrue(textoAviso().contains("entre 0 y 100"));
        cerrarAviso();
        TextField porcentaje = buscar("#txtPorcentaje", TextField.class);
        assertTrue(porcentaje.getStyleClass().contains("campo-error"));
        pulsar("Cancelar");
        assertTrue(textoAviso().contains("descartar"));
        aceptarAviso();
        assertEquals(4, tabla().getItems().size());
    }

    @Test
    void editarNombreGuarda() throws Exception {
        abrirIva();
        doubleClickOn(filaNodo("IVA 10%"));
        escribirEn("#txtNombre", "IVA 10% editado");
        pulsar("Guardar");
        assertEquals("10%", textoCelda("IVA 10% editado", "Tipo"));
    }

    @Test
    void eliminarEnUsoAvisa() throws Exception {
        abrirIva();
        pulsarFila("IVA 21%");
        pulsarBoton("#seccionIva", "Eliminar");
        assertTrue(textoAviso().contains("definitivamente"));
        aceptarAviso();
        assertTrue(textoAviso().contains("facturas"));
        cerrarAviso();
        assertTrue(filaDe("IVA 21%") >= 0);
    }

    @Test
    void eliminarLibreBorra() throws Exception {
        abrirIva();
        pulsarFila("Exento");
        pulsarBoton("#seccionIva", "Eliminar");
        aceptarAviso();
        assertEquals(-1, filaDe("Exento"));
        assertEquals(3, tabla().getItems().size());
    }

    @Test
    void eliminarSinSeleccionAvisa() throws Exception {
        abrirIva();
        pulsarBoton("#seccionIva", "Eliminar");
        assertTrue(textoAviso().contains("Seleccione un tipo de IVA"));
        cerrarAviso();
    }
}
