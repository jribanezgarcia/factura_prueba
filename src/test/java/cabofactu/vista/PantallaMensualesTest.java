package cabofactu.vista;

import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * El diálogo de facturas mensuales como lo ve el usuario: lo que dice que
 * va a generar y sus avisos. Generar de verdad pide rellenar líneas en sus
 * celdas, y eso se reescribe en modulo-facturas.
 */
class PantallaMensualesTest extends PruebaDePantalla {

    private void abrirMensuales() throws Exception {
        mostrarPantalla("MenuPrincipal.fxml");
        pulsar("Facturar mes");
        esperarNodo("#comboCliente", ComboBox.class);
    }

    private void elegirEn(String combo, String valor) throws Exception {
        asentarVentanas();
        clickOn(buscar(combo, ComboBox.class));
        pulsar(valor);
    }

    private void elegirTodo() throws Exception {
        elegirEn("#comboCliente", "Cliente Ejemplo S.L. (B88888888)");
        elegirEn("#comboSerie", "A (Serie general)");
        elegirEn("#comboIva", "IVA 21%");
    }

    private String info() throws Exception {
        return buscar("#lblInfo", Label.class).getText();
    }

    private void cancelar() throws Exception {
        pulsar("Cancelar");
        assertTrue(nodosVisibles("#comboCliente").isEmpty());
    }

    @Test
    void infoInicialDiceDoce() throws Exception {
        abrirMensuales();
        assertEquals("Se generarán 12 facturas", info());
        cancelar();
    }

    @Test
    void sinClienteAvisa() throws Exception {
        abrirMensuales();
        pulsar("Generar");
        assertTrue(textoAviso().contains("Seleccione un cliente."));
        cerrarAviso();
        cancelar();
    }

    @Test
    void sinLineasAvisa() throws Exception {
        abrirMensuales();
        elegirTodo();
        pulsar("Generar");
        assertTrue(textoAviso().contains("Añada al menos una línea con descripción."));
        cerrarAviso();
        cancelar();
    }

    @Test
    void mesesInvertidosAvisan() throws Exception {
        abrirMensuales();
        elegirTodo();
        elegirEn("#comboMesInicio", "septiembre");
        elegirEn("#comboMesFin", "enero");
        assertEquals("No se generará ninguna factura", info());
        pulsar("Generar");
        assertTrue(textoAviso().contains("anterior o igual al mes de fin"));
        cerrarAviso();
        cancelar();
    }
}
