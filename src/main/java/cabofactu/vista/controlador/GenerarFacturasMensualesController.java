package cabofactu.vista.controlador;

import cabofactu.modelo.dominio.Cliente;
import cabofactu.modelo.dominio.LineaFactura;
import cabofactu.modelo.dominio.ModoDia;
import cabofactu.modelo.dominio.PlantillaMensual;
import cabofactu.modelo.dominio.Serie;
import cabofactu.modelo.dominio.TipoIva;
import cabofactu.modelo.dominio.TipoRetencion;
import cabofactu.utilidades.Formatos;
import cabofactu.vista.Vista;
import cabofactu.vista.recursos.LocalizadorRecursos;
import cabofactu.vista.utilidades.Dialogos;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Stage;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Diálogo de facturación mensual: genera una factura por cada mes del rango
 * elegido, siempre en el año de trabajo.
 */
public class GenerarFacturasMensualesController implements Initializable {

    private Stage stage;

    private final ObservableList<LineaFactura> lineas = FXCollections.observableArrayList();

    @FXML
    private ComboBox<Cliente> comboCliente;
    @FXML
    private ComboBox<Serie> comboSerie;
    @FXML
    private Label lblAnio;
    @FXML
    private ComboBox<String> comboMesInicio;
    @FXML
    private ComboBox<String> comboMesFin;
    @FXML
    private Spinner<Integer> spinnerDia;
    @FXML
    private RadioButton radioDiaFijo;
    @FXML
    private RadioButton radioPrimerDia;
    @FXML
    private RadioButton radioUltimoDia;
    @FXML
    private ComboBox<TipoIva> comboIva;
    @FXML
    private ComboBox<TipoRetencion> comboRetencion;
    @FXML
    private TableView<LineaFactura> tablaLineas;
    @FXML
    private TableColumn<LineaFactura, String> colCantidad;
    @FXML
    private TableColumn<LineaFactura, String> colDescripcion;
    @FXML
    private TableColumn<LineaFactura, String> colPrecio;
    @FXML
    private CheckBox chkAnadirMes;
    @FXML
    private Button btnGenerar;
    @FXML
    private Button btnAnadirLinea;
    @FXML
    private Button btnEliminarLinea;
    @FXML
    private Label lblInfo;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    /** Abrimos el diálogo modal, como pide el menú principal y el histórico. */
    public static void abrir() {
        try {
            FXMLLoader cargador = new FXMLLoader(LocalizadorRecursos.class.getResource("GenerarFacturasMensuales.fxml"));
            Parent raiz = cargador.load();
            GenerarFacturasMensualesController controlador = cargador.getController();
            Stage modal = Vista.getInstancia().crearVentanaModal(raiz, "Generar facturas mensuales", null);
            controlador.setStage(modal);
            modal.showAndWait();
        } catch (IOException e) {
            Dialogos.mostrarDialogoError("Diálogo", "No se pudo abrir el diálogo: " + e.getMessage());
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        lblAnio.setText(String.valueOf(anioDeTrabajo()));
        cargarClientes();
        cargarSeries();
        cargarMeses();
        cargarIvas();
        cargarRetenciones();
        prepararTabla();
        empezar();
    }

    private void cargarClientes() {
        try {
            comboCliente.getItems().setAll(Vista.getInstancia().getControlador().listadoClientes(true));
        } catch (Exception e) {
            Dialogos.mostrarDialogoError("Clientes", "Error al cargar clientes: " + e.getMessage());
        }
    }

    private void cargarSeries() {
        try {
            List<Serie> series = new ArrayList<>();
            for (Serie serie : Vista.getInstancia().getControlador().listadoSeries()) {
                if (!serie.isEsRectificativa()) {
                    series.add(serie);
                }
            }
            comboSerie.getItems().setAll(series);
        } catch (Exception e) {
            Dialogos.mostrarDialogoError("Series", "Error al cargar series: " + e.getMessage());
        }
    }

    private void cargarMeses() {
        List<String> nombres = new ArrayList<>();
        for (int mes = 1; mes <= 12; mes++) {
            nombres.add(Formatos.nombreMes(mes));
        }
        comboMesInicio.getItems().setAll(nombres);
        comboMesFin.getItems().setAll(nombres);
        comboMesInicio.valueProperty().addListener((propiedad, anterior, nuevo) -> actualizarInfo());
        comboMesFin.valueProperty().addListener((propiedad, anterior, nuevo) -> actualizarInfo());
    }

    private void cargarIvas() {
        try {
            comboIva.getItems().setAll(Vista.getInstancia().getControlador().listadoTiposIva(true));
        } catch (Exception e) {
            Dialogos.mostrarDialogoError("IVA", "Error al cargar tipos de IVA: " + e.getMessage());
        }
    }

    private void cargarRetenciones() {
        try {
            TipoRetencion sinRetencion = new TipoRetencion("Sin retención", 0);
            List<TipoRetencion> items = new ArrayList<>();
            items.add(sinRetencion);
            items.addAll(Vista.getInstancia().getControlador().listadoTiposRetencion(true));
            comboRetencion.getItems().setAll(items);
            comboRetencion.setValue(sinRetencion);
        } catch (Exception e) {
            Dialogos.mostrarDialogoError("Retenciones", "Error al cargar retenciones: " + e.getMessage());
        }
    }

    private void prepararTabla() {
        tablaLineas.setItems(lineas);
        tablaLineas.setEditable(true);
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidadTexto"));
        colCantidad.setCellFactory(TextFieldTableCell.forTableColumn());
        colDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        colDescripcion.setCellFactory(TextFieldTableCell.forTableColumn());
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precioUnitarioTexto"));
        colPrecio.setCellFactory(TextFieldTableCell.forTableColumn());
    }

    /** Ponemos los valores de siempre: todo el año, día 15, una línea vacía y «Añadir mes» marcada. */
    private void empezar() {
        comboMesInicio.getSelectionModel().select(0);
        comboMesFin.getSelectionModel().select(11);
        radioDiaFijo.setSelected(true);
        spinnerDia.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 31, 15));
        spinnerDia.setEditable(true);
        chkAnadirMes.setSelected(true);
        try {
            lineas.setAll(new LineaFactura(1, BigDecimal.ZERO));
        } catch (Exception e) {
            // la cantidad y el precio de la línea inicial siempre son válidos
        }
        actualizarInfo();
    }

    @FXML
    private void cambiarModoDia() {
        spinnerDia.setDisable(!radioDiaFijo.isSelected());
    }

    @FXML
    private void anadirLinea() {
        try {
            lineas.add(new LineaFactura(1, BigDecimal.ZERO));
        } catch (Exception e) {
            // la cantidad y el precio de la línea nueva siempre son válidos
        }
    }

    @FXML
    private void eliminarLinea() {
        LineaFactura seleccionada = tablaLineas.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            return;
        }
        lineas.remove(seleccionada);
        if (lineas.isEmpty()) {
            anadirLinea();
        }
    }

    @FXML
    private void cambiarCantidad(TableColumn.CellEditEvent<LineaFactura, String> evento) {
        try {
            evento.getRowValue().setCantidad(Integer.parseInt(evento.getNewValue().trim()));
        } catch (Exception e) {
            // el texto no es un número válido, o no llega a 1: dejamos la línea como estaba
        }
        tablaLineas.refresh();
        actualizarInfo();
    }

    @FXML
    private void cambiarDescripcion(TableColumn.CellEditEvent<LineaFactura, String> evento) {
        evento.getRowValue().setDescripcion(evento.getNewValue());
        tablaLineas.refresh();
        actualizarInfo();
    }

    @FXML
    private void cambiarPrecio(TableColumn.CellEditEvent<LineaFactura, String> evento) {
        BigDecimal valor = Formatos.parseEntrada(evento.getNewValue());
        if (valor != null) {
            try {
                evento.getRowValue().setPrecioUnitario(valor);
            } catch (Exception e) {
                // el precio no puede ser negativo: dejamos la línea como estaba
            }
        }
        tablaLineas.refresh();
        actualizarInfo();
    }

    @FXML
    private void generar() {
        PlantillaMensual plantilla;
        try {
            plantilla = leerPlantilla();
        } catch (Exception e) {
            Dialogos.mostrarDialogoError("Generar", e.getMessage());
            return;
        }
        try {
            if (!confirmarMesesConFactura(plantilla)) {
                return;
            }
            boolean usarLibres = preguntarNumerosLibres(plantilla);
            int generadas = Vista.getInstancia().getControlador().generarFacturasMensuales(plantilla, usarLibres);
            Dialogos.mostrarDialogoInformacion("Generar facturas mensuales",
                    String.format("Se han generado %d facturas.", generadas));
            if (stage != null) {
                stage.close();
            }
        } catch (Exception e) {
            Dialogos.mostrarDialogoError("Generar", e.getMessage());
        }
    }

    @FXML
    private void cancelar() {
        if (stage != null) {
            stage.close();
        }
    }

    /** Juntamos las líneas con descripción y creamos la plantilla con lo elegido en el diálogo. */
    private PlantillaMensual leerPlantilla() throws Exception {
        List<LineaFactura> lineasConDescripcion = new ArrayList<>();
        for (LineaFactura linea : lineas) {
            if (linea.getDescripcion() != null && !linea.getDescripcion().isBlank()) {
                linea.setDescripcion(linea.getDescripcion().trim());
                lineasConDescripcion.add(linea);
            }
        }
        PlantillaMensual plantilla = new PlantillaMensual(comboCliente.getValue(), comboSerie.getValue(),
                mesSeleccionado(comboMesInicio), mesSeleccionado(comboMesFin), modoDiaElegido(),
                diaFijoElegido(), comboIva.getValue(), lineasConDescripcion);
        TipoRetencion retencion = comboRetencion.getValue();
        if (retencion != null && retencion.getId() != null) {
            plantilla.setRetencion(retencion);
        }
        plantilla.setAnadirMes(chkAnadirMes.isSelected());
        return plantilla;
    }

    /** Si el cliente ya tiene factura en algún mes del rango, preguntamos si generar de todos modos. */
    private boolean confirmarMesesConFactura(PlantillaMensual plantilla) throws Exception {
        List<String> meses = Vista.getInstancia().getControlador().mesesConFacturaMensual(plantilla);
        if (meses.isEmpty()) {
            return true;
        }
        String mensaje = "Ya existen facturas para este cliente en:\n\n"
                + String.join(", ", meses)
                + "\n\n¿Deseas generar las facturas de todos modos?";
        return Dialogos.mostrarDialogoConfirmacion("Meses con facturas", mensaje);
    }

    /** Si hay números libres distintos de los propuestos, preguntamos si usarlos. */
    private boolean preguntarNumerosLibres(PlantillaMensual plantilla) throws Exception {
        int anio = anioDeTrabajo();
        int cantidad = plantilla.getCantidadMeses();
        List<Integer> conLibres = Vista.getInstancia().getControlador().proponerNumeros(
                plantilla.getSerie(), anio, cantidad, true);
        List<Integer> sinLibres = Vista.getInstancia().getControlador().proponerNumeros(
                plantilla.getSerie(), anio, cantidad, false);
        if (conLibres.equals(sinLibres)) {
            return false;
        }
        String libres = "";
        for (Integer numero : conLibres) {
            if (!sinLibres.contains(numero)) {
                if (!libres.isEmpty()) {
                    libres = libres + ", ";
                }
                libres = libres + numero;
            }
        }
        return Dialogos.mostrarDialogoNumerosLibres(plantilla.getSerie().toString(), libres);
    }

    private void actualizarInfo() {
        int mesInicio = mesSeleccionado(comboMesInicio);
        int mesFin = mesSeleccionado(comboMesFin);
        if (mesFin < mesInicio) {
            lblInfo.setText("No se generará ninguna factura.");
            return;
        }
        int anio = anioDeTrabajo();
        int meses = mesFin - mesInicio + 1;
        if (meses == 1) {
            lblInfo.setText(String.format("Se generará 1 factura en %d.", anio));
        } else {
            lblInfo.setText(String.format("Se generarán %d facturas en %d.", meses, anio));
        }
    }

    private ModoDia modoDiaElegido() {
        if (radioPrimerDia.isSelected()) {
            return ModoDia.PRIMER_DIA;
        }
        if (radioUltimoDia.isSelected()) {
            return ModoDia.ULTIMO_DIA;
        }
        return ModoDia.FIJO;
    }

    private int diaFijoElegido() {
        Integer valor = spinnerDia.getValue();
        if (valor == null) {
            return 15;
        }
        return valor;
    }

    /** La posición del mes elegido en el desplegable, de 1 a 12; 0 si no hay ninguno elegido. */
    private int mesSeleccionado(ComboBox<String> combo) {
        return combo.getSelectionModel().getSelectedIndex() + 1;
    }

    private int anioDeTrabajo() {
        return Vista.getInstancia().getControlador().fechaTrabajo().getYear();
    }
}
