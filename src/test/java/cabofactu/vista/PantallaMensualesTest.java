package cabofactu.vista;

import cabofactu.modelo.dominio.Cliente;
import cabofactu.modelo.dominio.Factura;
import cabofactu.modelo.dominio.FormatoNumero;
import cabofactu.modelo.dominio.LineaFactura;
import cabofactu.modelo.dominio.ModoDia;
import cabofactu.modelo.dominio.PlantillaMensual;
import cabofactu.modelo.dominio.Serie;
import cabofactu.modelo.dominio.TipoIva;
import cabofactu.modelo.negocio.Facturas;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * El diálogo de facturas mensuales como lo ve el usuario: el año fijo, lo que
 * dice que va a generar y sus avisos.
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

    private int anioDeTrabajo() {
        return Vista.getInstancia().getControlador().fechaTrabajo().getYear();
    }

    /** Un cliente propio de la prueba, para no chocar con los datos de la demo. */
    private Cliente altaClienteDePrueba() throws Exception {
        Cliente cliente = new Cliente("Cliente Mensual", "12345678Z", "Calle Prueba 1", "28001", "Madrid", "Madrid");
        cliente.setId(Vista.getInstancia().getControlador().altaCliente(cliente));
        return cliente;
    }

    /** Una serie propia de la prueba, para que su numeración empiece siempre en 1. */
    private Serie altaSerieDePrueba() throws Exception {
        Serie serie = new Serie("M", "Mensual", FormatoNumero.MES, false);
        serie.setId(Vista.getInstancia().getControlador().altaSerie(serie));
        return serie;
    }

    private TipoIva ivaDeLaDemo() throws Exception {
        for (TipoIva iva : Vista.getInstancia().getControlador().listadoTiposIva(true)) {
            if (iva.getNombre().equals("IVA 21%")) {
                return iva;
            }
        }
        throw new IllegalStateException("Falta el IVA 21% de la demo");
    }

    private List<LineaFactura> unaLineaDeServicios() throws Exception {
        LineaFactura linea = new LineaFactura(1, new BigDecimal("60.00"));
        linea.setDescripcion("servicios");
        return List.of(linea);
    }

    private Factura facturaDeCorrelativo(Serie serie, int correlativo) throws Exception {
        for (Factura factura : Facturas.getFacturas().listado(null)) {
            if (factura.getSerie().getId().equals(serie.getId()) && factura.getCorrelativo() == correlativo) {
                return factura;
            }
        }
        throw new IllegalStateException("Falta el correlativo " + correlativo);
    }

    private int contarFacturasDeSerie(Serie serie) throws Exception {
        int total = 0;
        for (Factura factura : Facturas.getFacturas().listado(null)) {
            if (factura.getSerie().getId().equals(serie.getId())) {
                total++;
            }
        }
        return total;
    }

    @Test
    void infoInicialDiceDoce() throws Exception {
        abrirMensuales();
        assertEquals(String.format("Se generarán 12 facturas en %d.", anioDeTrabajo()), info());
        cancelar();
    }

    @Test
    void anioFijoSinSpinner() throws Exception {
        abrirMensuales();
        assertEquals(String.valueOf(anioDeTrabajo()), buscar("#lblAnio", Label.class).getText());
        assertTrue(nodosVisibles("#spinnerAnio").isEmpty());
        cancelar();
    }

    @Test
    void casillaAnadirMesVieneMarcada() throws Exception {
        abrirMensuales();
        assertTrue(buscar("#chkAnadirMes", CheckBox.class).isSelected());
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
        assertEquals("No se generará ninguna factura.", info());
        pulsar("Generar");
        assertTrue(textoAviso().contains("anterior o igual al mes de fin"));
        cerrarAviso();
        cancelar();
    }

    @Test
    void avisaDeMesesConFacturaYCancelarNoGeneraNada() throws Exception {
        Cliente cliente = altaClienteDePrueba();
        Serie serie = altaSerieDePrueba();
        TipoIva iva = ivaDeLaDemo();
        Vista.getInstancia().getControlador().generarFacturasMensuales(
                new PlantillaMensual(cliente, serie, 2, 2, ModoDia.FIJO, 15, iva, unaLineaDeServicios()), false);

        abrirMensuales();
        elegirEn("#comboCliente", cliente.toString());
        elegirEn("#comboSerie", serie.toString());
        elegirEn("#comboIva", iva.toString());
        elegirEn("#comboMesInicio", "enero");
        elegirEn("#comboMesFin", "marzo");
        escribirEnCelda(0, "#colDescripcion", "servicios");
        pulsar("Generar");

        assertTrue(textoAviso().contains("febrero"));
        cancelarAviso();

        assertEquals(1, contarFacturasDeSerie(serie));
        cancelar();
    }

    @Test
    void avisaDeNumerosLibresYSigueSinUsarlosAlCancelar() throws Exception {
        Cliente cliente = altaClienteDePrueba();
        Serie serie = altaSerieDePrueba();
        TipoIva iva = ivaDeLaDemo();
        Vista.getInstancia().getControlador().generarFacturasMensuales(
                new PlantillaMensual(cliente, serie, 1, 2, ModoDia.FIJO, 15, iva, unaLineaDeServicios()), false);
        Vista.getInstancia().getControlador().bajaFactura(facturaDeCorrelativo(serie, 1).getId());

        abrirMensuales();
        elegirEn("#comboCliente", cliente.toString());
        elegirEn("#comboSerie", serie.toString());
        elegirEn("#comboIva", iva.toString());
        elegirEn("#comboMesInicio", "marzo");
        elegirEn("#comboMesFin", "marzo");
        escribirEnCelda(0, "#colDescripcion", "servicios");
        pulsar("Generar");

        assertFalse(nodosVisibles("Usar los números libres").isEmpty());
        assertFalse(nodosVisibles("Continuar sin ellos").isEmpty());
        cancelarAviso();
        cerrarAviso();

        assertEquals(3, facturaDeCorrelativo(serie, 3).getCorrelativo());
    }
}
