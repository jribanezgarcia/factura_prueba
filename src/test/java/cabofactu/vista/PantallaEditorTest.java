package cabofactu.vista;

import cabofactu.modelo.dominio.Factura;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * El editor como lo ve el usuario: el número propuesto, los totales, el
 * guardado con sus avisos y la salida con cambios. Cada factura es una sola:
 * guardar una emitida pregunta y la sobrescribe.
 */
class PantallaEditorTest extends PruebaDePantalla {

    private void abrirEditor() throws Exception {
        mostrarPantalla("MenuPrincipal.fxml");
        pulsar("Nueva factura");
        esperarNodo("#txtNumero", TextField.class);
    }

    private String numero() throws Exception {
        return buscar("#txtNumero", TextField.class).getText();
    }

    private String total() throws Exception {
        return buscar("#lblTotal", Label.class).getText();
    }

    private void rellenarCliente() throws Exception {
        escribirEn("#cliNombre", "Prueba S.L.");
        escribirEn("#cliNif", "12345678Z");
        escribirEn("#cliDireccion", "Calle X 1");
        escribirEn("#cliCp", "28001");
        escribirEn("#cliLocalidad", "Madrid");
        escribirEn("#cliProvincia", "Madrid");
    }

    private void abrirHistorico() throws Exception {
        mostrarPantalla("MenuPrincipal.fxml");
        pulsar("Histórico");
        pulsar("Buscar");
    }

    private int filasHistorico() throws Exception {
        return buscar("#tabla", TableView.class).getItems().size();
    }

    private Node filaNodoHistorico(String numero) throws Exception {
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

    private void abrirFactura(String numero) throws Exception {
        doubleClickOn(filaNodoHistorico(numero));
        esperarNodo("#txtNumero", TextField.class);
    }

    @Test
    void numeroPropuestoYTotalesACero() throws Exception {
        abrirEditor();
        assertEquals("Nueva factura", buscar("#lblTitulo", Label.class).getText());
        LocalDate hoy = LocalDate.now();
        int siguiente = 1;
        if (hoy.getYear() == 2026) {
            siguiente = 6;
        }
        assertEquals(String.format("A-%d/%d", siguiente, hoy.getMonthValue()), numero());
        assertEquals(1, buscar("#tablaLineas", TableView.class).getItems().size());
        assertTrue(total().contains("0,00"));
    }

    @Test
    void anadirLineaVaciaNoMueveTotales() throws Exception {
        abrirEditor();
        pulsar("Añadir línea");
        assertEquals(2, buscar("#tablaLineas", TableView.class).getItems().size());
        assertTrue(total().contains("0,00"));
    }

    @Test
    void guardarSinClienteAvisa() throws Exception {
        abrirEditor();
        pulsar("Guardar");
        assertTrue(textoAviso().contains("Indique los datos del cliente."));
        cerrarAviso();
    }

    @Test
    void guardarSinLineasAvisa() throws Exception {
        abrirEditor();
        rellenarCliente();
        pulsar("Guardar");
        assertTrue(textoAviso().contains("al menos una línea con contenido"));
        cerrarAviso();
    }

    @Test
    void rectificarCreaRectificativaConReferencia() throws Exception {
        abrirHistorico();
        abrirFactura("A-2/9");
        pulsar("Rectificar");
        assertTrue(textoAviso().contains("Rectificativa creada"));
        cerrarAviso();
        assertEquals("R-2", numero());
        assertEquals("A-2/9", buscar("#txtReferencia", TextField.class).getText());
        assertFalse(buscar("#txtReferencia", TextField.class).isEditable());
        assertTrue(buscar("#txtNumero", TextField.class).isDisabled());
    }

    @Test
    void guardarEmitidaPreguntaYSobrescribe() throws Exception {
        abrirHistorico();
        abrirFactura("A-2/9");
        asentarVentanas();
        TextArea observaciones = buscar("#txtObservaciones", TextArea.class);
        clickOn(observaciones);
        write("observación de prueba");
        pulsar("Guardar");
        assertTrue(textoAviso().contains("ya emitida se sobrescribirá"));
        aceptarAviso();
        assertTrue(textoAviso().contains("Factura guardada."));
        cerrarAviso();
        abrirHistorico();
        assertEquals(6, filasHistorico());
    }

    @Test
    void salidaConCambiosAvisa() throws Exception {
        abrirEditor();
        escribirEn("#txtNumero", numero() + "X");
        pulsar("Volver");
        assertTrue(textoAviso().contains("sin guardar"));
        aceptarAviso();
        assertTrue(textoAviso().contains("Indique los datos del cliente."));
        cerrarAviso();
        assertTrue(numero().endsWith("X"));
    }
}
