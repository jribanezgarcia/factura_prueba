package cabofactu.vista.controlador;

import cabofactu.modelo.dominio.Empresa;
import cabofactu.modelo.dominio.EstadoFactura;
import cabofactu.modelo.dominio.Factura;
import cabofactu.modelo.dominio.FiltrosHistorial;
import cabofactu.modelo.dominio.Serie;
import cabofactu.pdf.ExportadorPdf;
import cabofactu.utilidades.Formatos;
import cabofactu.vista.Pantalla;
import cabofactu.vista.Vista;
import cabofactu.vista.utilidades.Dialogos;
import cabofactu.vista.utilidades.ExportacionVarias;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import java.io.File;
import java.math.BigDecimal;
import java.net.URL;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Histórico: filtros combinables con botón Buscar, una fila por factura y
 * apertura de la factura seleccionada con doble clic.
 */
public class HistoricoController implements Pantalla, Initializable {

    private static final String PREV_EXPORT = "ultima_carpeta_export";

    @FXML
    private Button btnExportarPdf;
    @FXML
    private Button btnAnular;
    @FXML
    private Button btnBorrar;
    @FXML
    private ComboBox<String> comboSerie;
    @FXML
    private TextField txtCliente;
    @FXML
    private DatePicker fechaDesde;
    @FXML
    private DatePicker fechaHasta;
    @FXML
    private TextField txtImporteDesde;
    @FXML
    private TextField txtImporteHasta;
    @FXML
    private ComboBox<String> comboEstado;
    // Cómo funciona: JavaFX inyecta aquí el controlador del <fx:include fx:id="barra">;
    // el nombre del campo es el fx:id más "Controller".
    @FXML
    private BarraNavegacionController barraController;
    @FXML
    private TableView<Factura> tabla;
    @FXML
    private TableColumn<Factura, String> colFecha;
    @FXML
    private TableColumn<Factura, String> colNumero;
    @FXML
    private TableColumn<Factura, String> colCliente;
    @FXML
    private TableColumn<Factura, String> colNif;
    @FXML
    private TableColumn<Factura, String> colBase;
    @FXML
    private TableColumn<Factura, String> colIva;
    @FXML
    private TableColumn<Factura, String> colRetencion;
    @FXML
    private TableColumn<Factura, String> colTotal;
    @FXML
    private TableColumn<Factura, String> colEstado;

    private List<Serie> series;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        barraController.marcarActivo("historico");
        cargarSeries();
        cargarEstados();
        prepararTabla();
        ponerAnioDeTrabajo();
        buscar();
    }

    @Override
    public void alMostrar() {
        Vista.getInstancia().getVentana().getScene().getAccelerators().put(
                new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN), () -> buscar());
    }

    @FXML
    private void buscar() {
        quitarMarcasFiltros();
        BigDecimal importeDesde = leerImporte(txtImporteDesde);
        BigDecimal importeHasta = leerImporte(txtImporteHasta);
        String mensaje = null;
        mensaje = primero(mensaje, comprobarImporteDesde(importeDesde));
        mensaje = primero(mensaje, comprobarImporteHasta(importeHasta));
        mensaje = primero(mensaje, marcarSiFechasAlReves());
        mensaje = primero(mensaje, marcarSiImportesAlReves(importeDesde, importeHasta));
        if (mensaje != null) {
            Dialogos.mostrarDialogoError("Histórico", mensaje);
            return;
        }
        ejecutarBusqueda(importeDesde, importeHasta);
    }

    @FXML
    private void generarMensual() {
        GenerarFacturasMensualesController.abrir();
    }

    @FXML
    private void exportarPdf() {
        List<Factura> seleccion = new ArrayList<>(tabla.getSelectionModel().getSelectedItems());
        if (seleccion.isEmpty()) {
            Dialogos.mostrarDialogoInformacion("Exportar PDF", "Selecciona al menos una factura del histórico.");
            return;
        }
        if (seleccion.size() == 1) {
            exportarUna(seleccion.get(0));
            return;
        }
        ExportacionVarias eleccion = Dialogos.mostrarDialogoExportarVarias(seleccion.size());
        if (eleccion == ExportacionVarias.POR_FACTURA) {
            exportarPorFactura(seleccion);
        } else if (eleccion == ExportacionVarias.AGRUPADA) {
            exportarAgrupadas(seleccion);
        }
    }

    @FXML
    private void anularSeleccionadas() {
        List<Factura> seleccion = new ArrayList<>(tabla.getSelectionModel().getSelectedItems());
        if (seleccion.isEmpty()) {
            Dialogos.mostrarDialogoInformacion("Anular", "Selecciona al menos una factura del histórico.");
            return;
        }
        if (!Dialogos.mostrarDialogoConfirmacion("Anular",
                String.format("Se van a anular %d factura(s).%nLas ya anuladas no se modificarán.%n%n¿Continuar?",
                        seleccion.size()))) {
            return;
        }
        Dialogos.mostrarDialogoInformacion("Anular", resumenAnular(seleccion));
        buscar();
    }

    @FXML
    private void eliminarSeleccionadas() {
        List<Factura> seleccion = new ArrayList<>(tabla.getSelectionModel().getSelectedItems());
        if (seleccion.isEmpty()) {
            Dialogos.mostrarDialogoInformacion("Eliminar", "Selecciona al menos una factura del histórico.");
            return;
        }
        int lineas;
        try {
            lineas = totalLineas(seleccion);
        } catch (Exception e) {
            Dialogos.mostrarDialogoError("Eliminar", e.getMessage());
            return;
        }
        if (!Dialogos.mostrarDialogoConfirmacion("Eliminar",
                String.format("Se van a eliminar %d factura(s) con %d línea(s) en total.%n%n¿Continuar?",
                        seleccion.size(), lineas))) {
            return;
        }
        Dialogos.mostrarDialogoInformacion("Eliminar", resumenEliminar(seleccion));
        buscar();
    }

    @FXML
    private void volver() {
        Vista.getInstancia().mostrar("MenuPrincipal.fxml");
    }

    @FXML
    void pulsarTabla(MouseEvent evento) {
        if (evento.getButton() != MouseButton.PRIMARY || evento.getClickCount() != 2) {
            return;
        }
        Factura seleccionada = tabla.getSelectionModel().getSelectedItem();
        if (seleccionada != null) {
            abrirFactura(seleccionada);
        }
    }

    private void abrirFactura(Factura fila) {
        EditorController editor = (EditorController) Vista.getInstancia().mostrar("Editor.fxml");
        if (editor == null) {
            return;
        }
        editor.cargarFactura(fila.getId());
    }

    private void cargarSeries() {
        comboSerie.getItems().add("Todas");
        try {
            series = Vista.getInstancia().getControlador().listadoSeries();
        } catch (Exception e) {
            series = new ArrayList<>();
        }
        for (Serie serie : series) {
            comboSerie.getItems().add(serie.toString());
        }
        comboSerie.getSelectionModel().selectFirst();
    }

    private void cargarEstados() {
        comboEstado.getItems().addAll("Todos", EstadoFactura.EMITIDA.label(), EstadoFactura.ANULADA.label());
        comboEstado.getSelectionModel().selectFirst();
    }

    private void prepararTabla() {
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fechaTexto"));
        colNumero.setCellValueFactory(new PropertyValueFactory<>("numero"));
        colCliente.setCellValueFactory(new PropertyValueFactory<>("clienteNombre"));
        colNif.setCellValueFactory(new PropertyValueFactory<>("clienteNif"));
        colBase.setCellValueFactory(new PropertyValueFactory<>("baseTexto"));
        colIva.setCellValueFactory(new PropertyValueFactory<>("ivaTexto"));
        colRetencion.setCellValueFactory(new PropertyValueFactory<>("retencionTexto"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("totalTexto"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estadoTexto"));
        tabla.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    private void ponerAnioDeTrabajo() {
        int anio = Vista.getInstancia().getControlador().fechaTrabajo().getYear();
        fechaDesde.setValue(LocalDate.of(anio, 1, 1));
        fechaHasta.setValue(LocalDate.of(anio, 12, 31));
    }

    private void quitarMarcasFiltros() {
        txtImporteDesde.getStyleClass().remove("campo-error");
        txtImporteHasta.getStyleClass().remove("campo-error");
        fechaDesde.getStyleClass().remove("campo-error");
        fechaHasta.getStyleClass().remove("campo-error");
    }

    /** El texto vacío significa que no hay filtro; si no, null es que no es un número. */
    private BigDecimal leerImporte(TextField campo) {
        if (campo.getText() == null || campo.getText().isBlank()) {
            return null;
        }
        return Formatos.parseEntrada(campo.getText());
    }

    private String comprobarImporteDesde(BigDecimal importeDesde) {
        if (importeDesde != null || txtImporteDesde.getText().isBlank()) {
            return null;
        }
        txtImporteDesde.getStyleClass().add("campo-error");
        return "El importe desde no es un importe válido.";
    }

    private String comprobarImporteHasta(BigDecimal importeHasta) {
        if (importeHasta != null || txtImporteHasta.getText().isBlank()) {
            return null;
        }
        txtImporteHasta.getStyleClass().add("campo-error");
        return "El importe hasta no es un importe válido.";
    }

    private String marcarSiFechasAlReves() {
        String error = FiltrosHistorial.errorFechas(fechaDesde.getValue(), fechaHasta.getValue());
        if (error == null) {
            return null;
        }
        fechaDesde.getStyleClass().add("campo-error");
        fechaHasta.getStyleClass().add("campo-error");
        return error;
    }

    private String marcarSiImportesAlReves(BigDecimal importeDesde, BigDecimal importeHasta) {
        String error = FiltrosHistorial.errorImportes(importeDesde, importeHasta);
        if (error == null) {
            return null;
        }
        txtImporteDesde.getStyleClass().add("campo-error");
        txtImporteHasta.getStyleClass().add("campo-error");
        return error;
    }

    private String primero(String actual, String candidato) {
        if (actual != null) {
            return actual;
        }
        return candidato;
    }

    private void ejecutarBusqueda(BigDecimal importeDesde, BigDecimal importeHasta) {
        try {
            FiltrosHistorial filtros = new FiltrosHistorial(serieSeleccionada(), txtCliente.getText(),
                    fechaDesde.getValue(), fechaHasta.getValue(), importeDesde, importeHasta, estadoSeleccionado());
            tabla.setItems(FXCollections.observableArrayList(
                    Vista.getInstancia().getControlador().listadoFacturas(filtros)));
        } catch (Exception e) {
            Dialogos.mostrarDialogoError("Histórico", e.getMessage());
        }
    }

    private Serie serieSeleccionada() {
        int posicion = comboSerie.getSelectionModel().getSelectedIndex();
        if (posicion > 0) {
            return series.get(posicion - 1);
        }
        return null;
    }

    private EstadoFactura estadoSeleccionado() {
        int posicion = comboEstado.getSelectionModel().getSelectedIndex();
        if (posicion == 1) {
            return EstadoFactura.EMITIDA;
        }
        if (posicion == 2) {
            return EstadoFactura.ANULADA;
        }
        return null;
    }

    private int totalLineas(List<Factura> seleccion) throws Exception {
        int total = 0;
        for (Factura factura : seleccion) {
            total += Vista.getInstancia().getControlador().numeroDeLineasFactura(factura.getId());
        }
        return total;
    }

    /** Anulamos cada factura no anulada ya, y devolvemos el resumen con el número de cada fallo. */
    private String resumenAnular(List<Factura> seleccion) {
        int anuladas = 0;
        int yaAnuladas = 0;
        String errores = "";
        for (Factura factura : seleccion) {
            if (factura.getEstado() == EstadoFactura.ANULADA) {
                yaAnuladas++;
                continue;
            }
            try {
                Vista.getInstancia().getControlador().anularFactura(factura.getId());
                anuladas++;
            } catch (Exception e) {
                errores = errores + String.format("%n%s: %s", factura.getNumero(), e.getMessage());
            }
        }
        String resumen = String.format("Anuladas: %d%nYa anuladas: %d", anuladas, yaAnuladas);
        if (!errores.isEmpty()) {
            resumen = resumen + String.format("%n%nNo se han podido anular:%s", errores);
        }
        return resumen;
    }

    /** Eliminamos cada factura, y devolvemos el resumen con el número de cada fallo. */
    private String resumenEliminar(List<Factura> seleccion) {
        int eliminadas = 0;
        String errores = "";
        for (Factura factura : seleccion) {
            try {
                Vista.getInstancia().getControlador().bajaFactura(factura.getId());
                eliminadas++;
            } catch (Exception e) {
                errores = errores + String.format("%n%s: %s", factura.getNumero(), e.getMessage());
            }
        }
        String resumen = String.format("Eliminadas: %d", eliminadas);
        if (!errores.isEmpty()) {
            resumen = resumen + String.format("%n%nNo se han podido eliminar:%s", errores);
        }
        return resumen;
    }

    private void exportarUna(Factura fila) {
        Factura factura;
        Empresa empresa;
        try {
            factura = Vista.getInstancia().getControlador().buscarFactura(fila.getId());
            empresa = Vista.getInstancia().getControlador().buscarEmpresa();
        } catch (Exception e) {
            Dialogos.mostrarDialogoError("Exportar PDF", e.getMessage());
            return;
        }
        FileChooser elegidor = new FileChooser();
        elegidor.setTitle("Exportar PDF");
        File carpeta = carpetaExportacion();
        if (carpeta != null) {
            elegidor.setInitialDirectory(carpeta);
        }
        elegidor.setInitialFileName(Formatos.nombreArchivoPdf(factura.getNumero()));
        File destino = elegidor.showSaveDialog(Vista.getInstancia().getVentana());
        if (destino == null) {
            return;
        }
        Path ruta = destino.toPath();
        String color = colorPdfPreferido();
        Vista.getInstancia().getVentana().getScene().setCursor(Cursor.WAIT);
        try {
            new ExportadorPdf().exportar(factura, empresa, ruta, color);
            recordarCarpeta(ruta.getParent());
            Dialogos.mostrarDialogoInformacion("Exportar PDF", "PDF generado en:\n" + ruta);
        } catch (Exception e) {
            Dialogos.mostrarDialogoError("Exportar PDF", e.getMessage());
        } finally {
            Vista.getInstancia().getVentana().getScene().setCursor(Cursor.DEFAULT);
        }
    }

    private void exportarPorFactura(List<Factura> seleccion) {
        DirectoryChooser elegidor = new DirectoryChooser();
        elegidor.setTitle("Carpeta donde guardar los PDF");
        File carpeta = carpetaExportacion();
        if (carpeta != null) {
            elegidor.setInitialDirectory(carpeta);
        }
        File destino = elegidor.showDialog(Vista.getInstancia().getVentana());
        if (destino == null) {
            return;
        }
        Empresa empresa;
        try {
            empresa = Vista.getInstancia().getControlador().buscarEmpresa();
        } catch (Exception e) {
            Dialogos.mostrarDialogoError("Exportar PDF", e.getMessage());
            return;
        }
        String color = colorPdfPreferido();
        Vista.getInstancia().getVentana().getScene().setCursor(Cursor.WAIT);
        try {
            String mensaje = generarPorFactura(seleccion, empresa, destino, color);
            recordarCarpeta(destino.toPath());
            Dialogos.mostrarDialogoInformacion("Exportar PDF", mensaje);
        } finally {
            Vista.getInstancia().getVentana().getScene().setCursor(Cursor.DEFAULT);
        }
    }

    private String generarPorFactura(List<Factura> seleccion, Empresa empresa, File destino, String color) {
        int generados = 0;
        List<String> fallos = new ArrayList<>();
        for (Factura fila : seleccion) {
            try {
                Factura factura = Vista.getInstancia().getControlador().buscarFactura(fila.getId());
                Path ruta = destino.toPath().resolve(Formatos.nombreArchivoPdf(factura.getNumero()));
                new ExportadorPdf().exportar(factura, empresa, ruta, color);
                generados++;
            } catch (Exception e) {
                fallos.add(String.format("%s: %s", fila.getNumero(), e.getMessage()));
            }
        }
        String mensaje = String.format("%d PDF generados en:%n%s", generados, destino.toPath());
        if (!fallos.isEmpty()) {
            mensaje = mensaje + "\n\nNo se han podido generar:\n" + String.join("\n", fallos);
        }
        return mensaje;
    }

    private void exportarAgrupadas(List<Factura> seleccion) {
        FileChooser elegidor = new FileChooser();
        elegidor.setTitle("Guardar PDF agrupado");
        File carpeta = carpetaExportacion();
        if (carpeta != null) {
            elegidor.setInitialDirectory(carpeta);
        }
        elegidor.setInitialFileName("facturas.pdf");
        File destino = elegidor.showSaveDialog(Vista.getInstancia().getVentana());
        if (destino == null) {
            return;
        }
        Empresa empresa;
        List<Factura> facturas;
        try {
            empresa = Vista.getInstancia().getControlador().buscarEmpresa();
            facturas = cargarCompletas(seleccion);
        } catch (Exception e) {
            Dialogos.mostrarDialogoError("Exportar PDF", e.getMessage());
            return;
        }
        Path ruta = destino.toPath();
        String color = colorPdfPreferido();
        Vista.getInstancia().getVentana().getScene().setCursor(Cursor.WAIT);
        try {
            new ExportadorPdf().exportarAgrupado(facturas, empresa, ruta, color);
            recordarCarpeta(ruta.getParent());
            Dialogos.mostrarDialogoInformacion("Exportar PDF", "PDF agrupado generado en:\n" + ruta);
        } catch (Exception e) {
            Dialogos.mostrarDialogoError("Exportar PDF", e.getMessage());
        } finally {
            Vista.getInstancia().getVentana().getScene().setCursor(Cursor.DEFAULT);
        }
    }

    private List<Factura> cargarCompletas(List<Factura> seleccion) throws Exception {
        List<Factura> facturas = new ArrayList<>();
        for (Factura fila : seleccion) {
            facturas.add(Vista.getInstancia().getControlador().buscarFactura(fila.getId()));
        }
        return facturas;
    }

    private File carpetaExportacion() {
        try {
            String carpeta = Vista.getInstancia().getControlador().preferencia(PREV_EXPORT);
            if (carpeta != null && !carpeta.isBlank()) {
                File archivo = new File(carpeta);
                if (archivo.isDirectory()) {
                    return archivo;
                }
            }
        } catch (Exception ignored) {
            // si falla, solo se pierde la carpeta propuesta
        }
        return null;
    }

    private void recordarCarpeta(Path carpeta) {
        if (carpeta == null) {
            return;
        }
        try {
            Vista.getInstancia().getControlador().guardarPreferencia(PREV_EXPORT, carpeta.toString());
        } catch (Exception ignored) {
            // si falla, solo se pierde la carpeta propuesta
        }
    }

    private String colorPdfPreferido() {
        try {
            return Vista.getInstancia().getControlador().preferencia(ExportadorPdf.PREF_COLOR);
        } catch (Exception e) {
            return null;
        }
    }
}
