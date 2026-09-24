package cabofactu.vista;

import cabofactu.modelo.dominio.TipoRetencion;
import javafx.scene.Node;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * La sección Retenciones como la ve el usuario: la tabla con los tipos, la
 * ficha con sus avisos, y el borrado con su confirmación.
 */
class PantallaRetencionesTest extends PruebaDePantalla {

    private void abrirRetenciones() throws Exception {
        mostrarPantalla("Configuracion.fxml");
        pulsar("Retenciones");
        esperarNodo("#tablaRetenciones", TableView.class);
    }

    private TableView<TipoRetencion> tabla() throws Exception {
        return buscar("#tablaRetenciones", TableView.class);
    }

    private int filaDe(String nombre) throws Exception {
        TableView<TipoRetencion> tabla = tabla();
        for (int i = 0; i < tabla.getItems().size(); i++) {
            if (tabla.getItems().get(i).getNombre().equals(nombre)) {
                return i;
            }
        }
        return -1;
    }

    private Node filaNodo(String nombre) throws Exception {
        for (Node nodo : nodosVisibles("#tablaRetenciones .table-row-cell")) {
            if (nodo instanceof TableRow) {
                TableRow<?> fila = (TableRow<?>) nodo;
                Object dato = fila.getItem();
                if (dato instanceof TipoRetencion) {
                    TipoRetencion tipo = (TipoRetencion) dato;
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
        for (Node nodo : nodosVisibles("#tablaRetenciones .table-cell")) {
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
    void tablaMuestraLaRetencion() throws Exception {
        abrirRetenciones();
        assertEquals(1, tabla().getItems().size());
        assertEquals("IRPF profesional", textoCelda("IRPF profesional", "Nombre"));
        assertEquals("15%", textoCelda("IRPF profesional", "Porcentaje"));
        assertEquals("Sí", textoCelda("IRPF profesional", "Activo"));
    }

    @Test
    void nuevoValido() throws Exception {
        abrirRetenciones();
        pulsarBoton("#seccionRetenciones", "Nuevo");
        escribirEn("#txtNombre", "IRPF alquiler");
        escribirEn("#txtPorcentaje", "19");
        pulsar("Añadir");
        assertEquals(2, tabla().getItems().size());
        assertEquals("19%", textoCelda("IRPF alquiler", "Porcentaje"));
    }

    @Test
    void porcentajeVacioAvisaYSeMarcaEnRojo() throws Exception {
        abrirRetenciones();
        pulsarBoton("#seccionRetenciones", "Nuevo");
        escribirEn("#txtNombre", "Sin porcentaje");
        pulsar("Añadir");
        assertTrue(textoAviso().contains("Indique el porcentaje"));
        cerrarAviso();
        TextField porcentaje = buscar("#txtPorcentaje", TextField.class);
        assertTrue(porcentaje.getStyleClass().contains("campo-error"));
        pulsar("Cancelar");
        assertTrue(textoAviso().contains("descartar"));
        aceptarAviso();
        assertEquals(1, tabla().getItems().size());
    }

    @Test
    void editarNombreGuarda() throws Exception {
        abrirRetenciones();
        doubleClickOn(filaNodo("IRPF profesional"));
        escribirEn("#txtNombre", "IRPF profesional editado");
        pulsar("Guardar");
        assertEquals("15%", textoCelda("IRPF profesional editado", "Porcentaje"));
    }

    @Test
    void eliminarEnUsoAvisa() throws Exception {
        abrirRetenciones();
        pulsarFila("IRPF profesional");
        pulsarBoton("#seccionRetenciones", "Eliminar");
        assertTrue(textoAviso().contains("definitivamente"));
        aceptarAviso();
        assertTrue(textoAviso().contains("facturas"));
        cerrarAviso();
        assertTrue(filaDe("IRPF profesional") >= 0);
    }

    @Test
    void eliminarLibreBorra() throws Exception {
        abrirRetenciones();
        pulsarBoton("#seccionRetenciones", "Nuevo");
        escribirEn("#txtNombre", "Temporal");
        escribirEn("#txtPorcentaje", "5");
        pulsar("Añadir");
        pulsarFila("Temporal");
        pulsarBoton("#seccionRetenciones", "Eliminar");
        aceptarAviso();
        assertEquals(-1, filaDe("Temporal"));
        assertEquals(1, tabla().getItems().size());
    }
}
