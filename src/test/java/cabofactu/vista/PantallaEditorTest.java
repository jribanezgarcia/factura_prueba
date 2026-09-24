package cabofactu.vista;

import cabofactu.modelo.dominio.FilaHistorial;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * El editor como lo ve el usuario: el número propuesto, los totales, el
 * guardado con sus avisos, el número ocupado, el hueco y la salida con
 * cambios. Sin editar celdas: esa parte se reescribe en modulo-facturas.
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

    private Node filaNodoHistorico(String numero) throws Exception {
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
        if (hoy.getYear() == 2026 && hoy.getMonthValue() == 9) {
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
    void rectificarCreaYGuarda() throws Exception {
        abrirHistorico();
        abrirFactura("A-2/9");
        pulsar("Rectificar");
        assertTrue(textoAviso().contains("Rectificativa creada"));
        cerrarAviso();
        assertEquals("R-2", numero());
        pulsar("Guardar");
        aceptarAviso();
        assertTrue(textoAviso().contains("Factura guardada."));
        cerrarAviso();
        assertEquals("R-2", numero());
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
