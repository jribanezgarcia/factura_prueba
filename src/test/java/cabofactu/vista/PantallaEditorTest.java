package cabofactu.vista;

import cabofactu.modelo.dominio.Cliente;
import cabofactu.modelo.dominio.Factura;
import cabofactu.modelo.dominio.LineaFactura;
import cabofactu.modelo.dominio.Serie;
import cabofactu.modelo.dominio.TipoIva;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

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

    private void elegirEn(String combo, String valor) throws Exception {
        asentarVentanas();
        clickOn(buscar(combo, ComboBox.class));
        pulsar(valor);
    }

    private void rellenarLinea() throws Exception {
        escribirEnCelda(0, "colDescripcion", "Concepto");
        escribirEnCelda(0, "colPrecio", "10");
    }

    private void rellenarClienteNuevo() throws Exception {
        escribirEn("#cliNombre", "Nuevo Cliente S.L.");
        escribirEn("#cliNif", "12345678Z");
        escribirEn("#cliDireccion", "Calle Nueva 1");
        escribirEn("#cliCp", "28001");
        escribirEn("#cliLocalidad", "Madrid");
        escribirEn("#cliProvincia", "Madrid");
    }

    private void rellenarClienteDemo() throws Exception {
        escribirEn("#cliNombre", "Cliente Ejemplo S.L.");
        escribirEn("#cliNif", "B88888888");
        escribirEn("#cliDireccion", "Calle Irreal 10");
        escribirEn("#cliCp", "28000");
        escribirEn("#cliLocalidad", "Madrid");
        escribirEn("#cliProvincia", "Madrid");
    }

    private Serie serieA() throws Exception {
        List<Serie> series = Vista.getInstancia().getControlador().listadoSeries();
        for (Serie serie : series) {
            if ("A".equals(serie.getCodigo())) {
                return serie;
            }
        }
        throw new AssertionError("No hay serie A");
    }

    private TipoIva ivaParaLinea() throws Exception {
        List<TipoIva> tipos = Vista.getInstancia().getControlador().listadoTiposIva(true);
        for (TipoIva tipo : tipos) {
            if (tipo.getPorcentaje() != null && tipo.getPorcentaje() == 21) {
                return tipo;
            }
        }
        return tipos.get(0);
    }

    private int contarFacturas() throws Exception {
        return Vista.getInstancia().getControlador().listadoFacturas(null).size();
    }

    private boolean clienteEnLista(String nif) throws Exception {
        List<Cliente> lista = Vista.getInstancia().getControlador().listadoClientes(true);
        for (Cliente cliente : lista) {
            if (nif.equals(cliente.getNif())) {
                return true;
            }
        }
        return false;
    }

    private int[] prepararHueco() throws Exception {
        LocalDate hoy = LocalDate.now();
        Serie serie = serieA();
        Cliente cliente = Vista.getInstancia().getControlador().listadoClientes(true).get(0);
        TipoIva iva = ivaParaLinea();
        LineaFactura primera = new LineaFactura(1, new BigDecimal("10.00"));
        primera.setDescripcion("Concepto");
        primera.setTipoIva(iva);
        Factura facturaUno = new Factura(serie, hoy, new Cliente(cliente));
        facturaUno.setLineas(new ArrayList<>(List.of(primera)));
        long idUno = Vista.getInstancia().getControlador().altaFactura(facturaUno);
        int hueco = facturaUno.getCorrelativo();
        LineaFactura segunda = new LineaFactura(1, new BigDecimal("10.00"));
        segunda.setDescripcion("Concepto");
        segunda.setTipoIva(iva);
        Factura facturaDos = new Factura(serie, hoy, new Cliente(cliente));
        facturaDos.setLineas(new ArrayList<>(List.of(segunda)));
        Vista.getInstancia().getControlador().altaFactura(facturaDos);
        Vista.getInstancia().getControlador().bajaFactura(idUno);
        int propuesto = Vista.getInstancia().getControlador().siguienteCorrelativo(serie, hoy);
        int[] numeros = new int[2];
        numeros[0] = hueco;
        numeros[1] = propuesto;
        return numeros;
    }

    private double alturaFila(int fila) throws Exception {
        FutureTask<Double> tarea = new FutureTask<>(() -> alturaFilaFx(fila));
        Platform.runLater(tarea);
        return tarea.get(30, TimeUnit.SECONDS);
    }

    private static Double alturaFilaFx(int fila) {
        for (javafx.stage.Window abierta : new ArrayList<>(javafx.stage.Window.getWindows())) {
            if (!abierta.isShowing() || abierta.getScene() == null
                    || abierta.getScene().getRoot() == null) {
                continue;
            }
            for (Node nodo : abierta.getScene().getRoot().lookupAll("#tablaLineas .table-row-cell")) {
                if (nodo instanceof TableRow) {
                    TableRow<?> filaNodo = (TableRow<?>) nodo;
                    if (filaNodo.getIndex() == fila) {
                        return filaNodo.getHeight();
                    }
                }
            }
        }
        return 0.0;
    }

    @Test
    void flujoDeTecladoCreaLinea() throws Exception {
        abrirEditor();
        escribirEnCelda(0, "colCantidad", "2");
        escribirEnCelda(0, "colDescripcion", "Concepto");
        escribirEnCelda(0, "colPrecio", "10");
        escribirEnCelda(0, "colTotal", "20");
        assertEquals(2, buscar("#tablaLineas", TableView.class).getItems().size());
        assertTrue(total().contains("24,20"));
    }

    @Test
    void descripcionLargaCreceLaFila() throws Exception {
        abrirEditor();
        StringBuilder larga = new StringBuilder();
        for (int i = 0; i < 300; i++) {
            larga.append("a");
        }
        escribirEnCelda(0, "colDescripcion", larga.toString());
        asentarVentanas();
        assertTrue(alturaFila(0) > alturaFila(1));
    }

    @Test
    void clienteNuevoAvisaYGuarda() throws Exception {
        abrirEditor();
        rellenarClienteNuevo();
        rellenarLinea();
        pulsar("Guardar");
        assertTrue(textoAviso().contains("no está en tu lista"));
        aceptarAviso();
        assertTrue(textoAviso().contains("Factura guardada."));
        cerrarAviso();
        assertTrue(clienteEnLista("12345678Z"));
    }

    @Test
    void clienteNuevoCanceladoNoGuarda() throws Exception {
        int antes = contarFacturas();
        abrirEditor();
        rellenarClienteNuevo();
        rellenarLinea();
        pulsar("Guardar");
        assertTrue(textoAviso().contains("no está en tu lista"));
        cancelarAviso();
        assertEquals("Nuevo Cliente S.L.", buscar("#cliNombre", TextField.class).getText());
        assertEquals(antes, contarFacturas());
        assertFalse(clienteEnLista("12345678Z"));
    }

    @Test
    void otroNifEsOtroCliente() throws Exception {
        abrirHistorico();
        abrirFactura("A-2/9");
        escribirEn("#cliNif", "87654321X");
        pulsar("Guardar");
        String aviso = textoAviso();
        assertTrue(aviso.contains("no está en tu lista"));
        assertFalse(aviso.contains("ficha"));
        aceptarAviso();
        assertTrue(textoAviso().contains("ya emitida se sobrescribirá"));
        aceptarAviso();
        assertTrue(textoAviso().contains("Factura guardada."));
        cerrarAviso();
        Cliente elegido = Vista.getInstancia().getControlador().buscarClientePorNif("B88888888");
        assertEquals("Cliente Ejemplo S.L.", elegido.getNombre());
        assertEquals("ejemplo@irreal.es", elegido.getEmail());
    }

    @Test
    void numeroOcupadoAManoAvisa() throws Exception {
        abrirEditor();
        rellenarClienteDemo();
        rellenarLinea();
        pulsar("Guardar");
        assertTrue(textoAviso().contains("Factura guardada."));
        cerrarAviso();
        String usado = numero();
        pulsar("Nueva");
        esperarNodo("#txtNumero", TextField.class);
        rellenarClienteDemo();
        rellenarLinea();
        escribirEn("#txtNumero", usado);
        pulsar("Guardar");
        assertTrue(textoAviso().contains("ya lo tiene otra factura"));
        cerrarAviso();
    }

    @Test
    void numeroLibreUsar() throws Exception {
        int[] numeros = prepararHueco();
        LocalDate hoy = LocalDate.now();
        Serie serie = serieA();
        String libre = Vista.getInstancia().getControlador().formarNumero(serie, numeros[0], hoy);
        abrirEditor();
        rellenarClienteDemo();
        rellenarLinea();
        pulsar("Guardar");
        assertTrue(textoAviso().contains(libre));
        aceptarAviso();
        assertTrue(textoAviso().contains("Factura guardada."));
        cerrarAviso();
        assertEquals(libre, numero());
    }

    @Test
    void numeroLibreContinuar() throws Exception {
        int[] numeros = prepararHueco();
        LocalDate hoy = LocalDate.now();
        Serie serie = serieA();
        String propuesto = Vista.getInstancia().getControlador().formarNumero(serie, numeros[1], hoy);
        abrirEditor();
        rellenarClienteDemo();
        rellenarLinea();
        pulsar("Guardar");
        assertTrue(textoAviso().contains("está libre"));
        cancelarAviso();
        assertTrue(textoAviso().contains("Factura guardada."));
        cerrarAviso();
        assertEquals(propuesto, numero());
    }

    @Test
    void numeroEscritoAManoNoPregunta() throws Exception {
        int[] numeros = prepararHueco();
        LocalDate hoy = LocalDate.now();
        Serie serie = serieA();
        String manual = Vista.getInstancia().getControlador().formarNumero(serie, numeros[1] + 10, hoy);
        abrirEditor();
        rellenarClienteDemo();
        rellenarLinea();
        escribirEn("#txtNumero", manual);
        pulsar("Guardar");
        assertTrue(textoAviso().contains("Factura guardada."));
        cerrarAviso();
        assertEquals(manual, numero());
    }
}
