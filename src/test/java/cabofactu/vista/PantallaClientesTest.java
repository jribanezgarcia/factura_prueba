package cabofactu.vista;

import cabofactu.modelo.dominio.Cliente;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * La pantalla de clientes como la ve el usuario: la tabla con su conteo, la
 * ficha con sus avisos, y el borrado con sus dos confirmaciones.
 */
class PantallaClientesTest extends PruebaDePantalla {

    private void abrirClientes() throws Exception {
        mostrarPantalla("Clientes.fxml");
        esperarNodo("#tabla", TableView.class);
    }

    private TableView<Cliente> tabla() throws Exception {
        return buscar("#tabla", TableView.class);
    }

    private int filaDe(String nombre) throws Exception {
        TableView<Cliente> tabla = tabla();
        for (int i = 0; i < tabla.getItems().size(); i++) {
            if (tabla.getItems().get(i).getNombre().equals(nombre)) {
                return i;
            }
        }
        return -1;
    }

    private Node filaNodo(String nombre) throws Exception {
        for (Node nodo : nodosVisibles("#tabla .table-row-cell")) {
            if (nodo instanceof TableRow) {
                TableRow<?> fila = (TableRow<?>) nodo;
                Object dato = fila.getItem();
                if (dato instanceof Cliente) {
                    Cliente cliente = (Cliente) dato;
                    if (cliente.getNombre().equals(nombre)) {
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

    private String conteo() throws Exception {
        return buscar("#lblConteo", Label.class).getText();
    }

    private void rellenarFicha(String nombre, String nif) throws Exception {
        escribirEn("#txtNombre", nombre);
        escribirEn("#txtNif", nif);
        escribirEn("#txtDireccion", "Calle Nueva 1");
        escribirEn("#txtCp", "28001");
        escribirEn("#txtLocalidad", "Madrid");
        escribirEn("#txtProvincia", "Madrid");
    }

    @Test
    void tablaMuestraLosDosClientes() throws Exception {
        abrirClientes();
        assertEquals(2, tabla().getItems().size());
        assertEquals("2 cliente(s)", conteo());
        assertEquals("B88888888", textoCelda("Cliente Ejemplo S.L.", "NIF"));
        assertEquals("Madrid", textoCelda("Cliente Ejemplo S.L.", "Localidad"));
        assertEquals("Activo", textoCelda("Otro Cliente S.L.", "Estado"));
    }

    @Test
    void nuevoValido() throws Exception {
        abrirClientes();
        pulsar("Nuevo");
        rellenarFicha("Nuevo Cliente S.L.", "12345678Z");
        pulsar("Añadir");
        assertEquals(3, tabla().getItems().size());
        assertEquals("3 cliente(s)", conteo());
        assertEquals("12345678Z", textoCelda("Nuevo Cliente S.L.", "NIF"));
    }

    @Test
    void nifRepetidoAvisaYMantieneLaFichaAbierta() throws Exception {
        abrirClientes();
        pulsar("Nuevo");
        rellenarFicha("Cliente Repetido S.L.", "B88888888");
        pulsar("Añadir");
        assertTrue(textoAviso().contains("Ya existe un cliente con el NIF B88888888: Cliente Ejemplo S.L."));
        cerrarAviso();
        TextField nif = buscar("#txtNif", TextField.class);
        assertTrue(nif.getStyleClass().contains("campo-error"));
        assertEquals("Cliente Repetido S.L.", buscar("#txtNombre", TextField.class).getText());
        assertEquals(2, tabla().getItems().size());
        pulsar("Cancelar");
        aceptarAviso();
    }

    @Test
    void recuperarClienteInactivoConservaSuFicha() throws Exception {
        abrirClientes();
        pulsarFila("Cliente Ejemplo S.L.");
        pulsar("Eliminar");
        assertTrue(textoAviso().contains("tiene facturas"));
        aceptarAviso();
        assertEquals("Inactivo", textoCelda("Cliente Ejemplo S.L.", "Estado"));

        pulsar("Nuevo");
        rellenarFicha("Cliente Ejemplo recuperado S.L.", "B88888888");
        pulsar("Añadir");
        assertTrue(textoAviso().contains("Cliente Ejemplo S.L."));
        assertTrue(textoAviso().contains("volver a darlo de alta"));
        aceptarAviso();

        assertEquals(2, tabla().getItems().size());
        assertEquals(-1, filaDe("Cliente Ejemplo S.L."));
        assertEquals("Activo", textoCelda("Cliente Ejemplo recuperado S.L.", "Estado"));
    }

    @Test
    void nifMaloAvisaYSeMarcaEnRojo() throws Exception {
        abrirClientes();
        pulsar("Nuevo");
        rellenarFicha("Cliente Malo S.L.", "123");
        pulsar("Añadir");
        assertTrue(textoAviso().contains("Formato NIF/NIE incorrecto"));
        cerrarAviso();
        TextField nif = buscar("#txtNif", TextField.class);
        assertTrue(nif.getStyleClass().contains("campo-error"));
        pulsar("Cancelar");
        assertTrue(textoAviso().contains("descartar"));
        aceptarAviso();
        assertEquals(2, tabla().getItems().size());
    }

    @Test
    void emailMaloAvisaYSeMarcaEnRojo() throws Exception {
        abrirClientes();
        pulsar("Nuevo");
        rellenarFicha("Con Email Malo S.L.", "12345678Z");
        escribirEn("#txtEmail", "mal");
        pulsar("Añadir");
        assertTrue(textoAviso().contains("correo electrónico"));
        cerrarAviso();
        TextField email = buscar("#txtEmail", TextField.class);
        assertTrue(email.getStyleClass().contains("campo-error"));
        pulsar("Cancelar");
        assertTrue(textoAviso().contains("descartar"));
        aceptarAviso();
        assertEquals(2, tabla().getItems().size());
    }

    @Test
    void dobleClicEdita() throws Exception {
        abrirClientes();
        doubleClickOn(filaNodo("Otro Cliente S.L."));
        escribirEn("#txtNombre", "Otro Cliente editado S.L.");
        pulsar("Guardar");
        assertEquals("Otro Cliente editado S.L.", textoCelda("Otro Cliente editado S.L.", "Nombre"));
        assertEquals(-1, filaDe("Otro Cliente S.L."));
    }

    @Test
    void eliminarConFacturasInactiva() throws Exception {
        abrirClientes();
        pulsarFila("Cliente Ejemplo S.L.");
        pulsar("Eliminar");
        assertTrue(textoAviso().contains("tiene facturas"));
        aceptarAviso();
        assertEquals("Inactivo", textoCelda("Cliente Ejemplo S.L.", "Estado"));
        assertEquals(2, tabla().getItems().size());
    }

    @Test
    void eliminarSinFacturasBorra() throws Exception {
        abrirClientes();
        pulsar("Nuevo");
        rellenarFicha("Temporal S.L.", "87654321X");
        pulsar("Añadir");
        pulsarFila("Temporal S.L.");
        pulsar("Eliminar");
        assertTrue(textoAviso().contains("definitivamente"));
        aceptarAviso();
        assertEquals(-1, filaDe("Temporal S.L."));
        assertEquals(2, tabla().getItems().size());
    }

    @Test
    void eliminarSinSeleccionAvisa() throws Exception {
        abrirClientes();
        pulsar("Eliminar");
        assertTrue(textoAviso().contains("Seleccione un cliente"));
        cerrarAviso();
    }
}
