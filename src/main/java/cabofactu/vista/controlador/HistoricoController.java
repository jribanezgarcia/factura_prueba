package cabofactu.vista.controlador;

import cabofactu.modelo.dominio.Empresa;
import cabofactu.modelo.dominio.EstadoFactura;
import cabofactu.modelo.dominio.FiltrosHistorial;
import cabofactu.modelo.dominio.FilaHistorial;
import cabofactu.modelo.dominio.Serie;
import cabofactu.pdf.ExportadorPdf;
import cabofactu.modelo.negocio.Facturas;
import cabofactu.utilidades.Formatos;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseButton;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import cabofactu.modelo.dominio.Factura;
import cabofactu.vista.Pantalla;
import cabofactu.vista.Vista;
import cabofactu.vista.utilidades.Dialogos;

/**
 * Historico: filtros combinables (serie, cliente/NIF, fechas, importes y
 * estado) con boton Buscar, una fila por version, y apertura de la version
 * seleccionada.
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
    private ComboBox<EstadoFactura> comboEstado;
    // Cómo funciona: JavaFX inyecta aquí el controlador del <fx:include fx:id="barra">;
    // el nombre del campo es el fx:id más "Controller".
    @FXML
    private BarraNavegacionController barraController;
    @FXML
    private TableView<FilaHistorial> tabla;
    @FXML
    private TableColumn<FilaHistorial, String> colFecha;
    @FXML
    private TableColumn<FilaHistorial, String> colNumero;
    @FXML
    private TableColumn<FilaHistorial, String> colVersion;
    @FXML
    private TableColumn<FilaHistorial, String> colCliente;
    @FXML
    private TableColumn<FilaHistorial, String> colNif;
    @FXML
    private TableColumn<FilaHistorial, String> colBase;
    @FXML
    private TableColumn<FilaHistorial, String> colIva;
    @FXML
    private TableColumn<FilaHistorial, String> colRetencion;
    @FXML
    private TableColumn<FilaHistorial, String> colTotal;
    @FXML
    private TableColumn<FilaHistorial, String> colEstado;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        barraController.marcarActivo("historico");
        try {
            comboSerie.getItems().add("(Todas)");
            for (Serie s : Vista.getInstancia().getControlador().listadoSeries()) {
                comboSerie.getItems().add(s.getCodigo());
            }
            comboSerie.setValue("(Todas)");
        } catch (Exception e) {
            comboSerie.getItems().add("(Todas)");
            comboSerie.setValue("(Todas)");
        }
        comboEstado.getItems().setAll(null, EstadoFactura.EMITIDA, EstadoFactura.ANULADA);
        comboEstado.setValue(null);

        colFecha.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(Formatos.fecha(c.getValue().getFechaFactura())));
        colNumero.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getNumero()));
        colVersion.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(String.valueOf(c.getValue().getVersionNum())));
        colCliente.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getCliente()));
        colNif.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getNif()));
        colBase.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(Formatos.moneda(c.getValue().getBase())));
        colIva.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(Formatos.moneda(c.getValue().getIva())));
        colRetencion.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(
                c.getValue().getRetencion() != null ? Formatos.moneda(c.getValue().getRetencion()) : ""));
        colTotal.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(Formatos.moneda(c.getValue().getTotal())));
        colEstado.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(etiquetaEstado(c.getValue().getEstado())));

        tabla.setPlaceholder(new javafx.scene.control.Label("Sin resultados. Pulsa Buscar."));
        tabla.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        tabla.setRowFactory(tv -> {
            TableRow<FilaHistorial> fila = new TableRow<>();
            fila.setOnMouseClicked(e -> {
                if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2 && !fila.isEmpty()) {
                    abrirVersion(fila.getItem());
                }
            });
            return fila;
        });

        ContextMenu menu = new ContextMenu();
        MenuItem itemExportar = new MenuItem("Exportar a PDF");
        itemExportar.setOnAction(e -> exportarPdf());
        MenuItem itemAnular = new MenuItem("Anular facturas seleccionadas");
        itemAnular.setOnAction(e -> anularSeleccionadas());
        MenuItem itemBorrar = new MenuItem("Eliminar facturas seleccionadas");
        itemBorrar.setOnAction(e -> borrarSeleccionadas());
        menu.getItems().addAll(itemExportar, itemAnular, itemBorrar);
        tabla.setContextMenu(menu);
    }

    @Override
    public void alMostrar() {
        Vista.getInstancia().getVentana().getScene().getAccelerators().put(
                new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN), () -> buscar());
    }

    private String etiquetaEstado(EstadoFactura e) {
        return e == EstadoFactura.ANULADA ? "Anulada" : e == EstadoFactura.EMITIDA ? "Emitida" : "";
    }

    @FXML
    private void buscar() {
        try {
            FiltrosHistorial f = new FiltrosHistorial();
            String serie = comboSerie.getValue();
            f.setSerieCodigo(serie == null || serie.startsWith("(") ? null : serie);
            f.setClienteTexto(txtCliente.getText());
            f.setFechaDesde(fechaDesde.getValue());
            f.setFechaHasta(fechaHasta.getValue());
            f.setImporteDesde(Formatos.parseMonedaOpcional(txtImporteDesde.getText()));
            f.setImporteHasta(Formatos.parseMonedaOpcional(txtImporteHasta.getText()));
            f.setEstado(comboEstado.getValue());
            tabla.setItems(FXCollections.observableArrayList(Vista.getInstancia().getControlador().getModelo().getHistorial().buscar(f)));
        } catch (Exception e) {
            Dialogos.mostrarDialogoError("Histórico", "Error al buscar: " + e.getMessage());
        }
    }

    private void abrirVersion(FilaHistorial fila) {
        EditorController editor = (EditorController) Vista.getInstancia().mostrar("Editor.fxml");
        if (editor == null) {
            return;
        }
        editor.cargarVersion(fila.getVersionId());
    }

    @FXML
    private void generarMensual() {
        GenerarFacturasMensualesController.abrir();
    }

    @FXML
    private void anularSeleccionadas() {
        List<FilaHistorial> seleccion = new ArrayList<>(tabla.getSelectionModel().getSelectedItems());
        if (seleccion.isEmpty()) {
            Dialogos.mostrarDialogoInformacion("Anular", "Selecciona al menos una factura del histórico.");
            return;
        }
        Set<Long> facturaIds = new LinkedHashSet<>();
        for (FilaHistorial fila : seleccion) {
            facturaIds.add(fila.getFacturaId());
        }
        if (!Dialogos.mostrarDialogoConfirmacion("Anular",
                "Se anularán " + facturaIds.size() + " factura(s).\n"
                        + "Las ya anuladas no se modificarán.\n\n¿Continuar?")) {
            return;
        }
        try {
            var resultado = Vista.getInstancia().getControlador().getModelo().getEstados().anularFacturas(new ArrayList<>(facturaIds));
            StringBuilder msg = new StringBuilder();
            msg.append("Anuladas: ").append(resultado.getAnuladas()).append("\n");
            msg.append("Ya anuladas: ").append(resultado.getYaAnuladas());
            if (resultado.getFallos() > 0) {
                msg.append("\n\nFallos:\n").append(String.join("\n", resultado.getErrores()));
            }
            Dialogos.mostrarDialogoInformacion("Anular", msg.toString());
            buscar();
        } catch (Exception e) {
            Dialogos.mostrarDialogoError("Anular", "Error al anular: " + e.getMessage());
        }
    }

    @FXML
    private void borrarSeleccionadas() {
        List<FilaHistorial> seleccion = new ArrayList<>(tabla.getSelectionModel().getSelectedItems());
        if (seleccion.isEmpty()) {
            Dialogos.mostrarDialogoInformacion("Eliminar", "Selecciona al menos una factura del histórico.");
            return;
        }
        Set<Long> facturaIds = new LinkedHashSet<>();
        for (FilaHistorial fila : seleccion) {
            facturaIds.add(fila.getFacturaId());
        }
        int totalVersiones = 0;
        int totalLineas = 0;
        try {
            for (long id : facturaIds) {
                Facturas.ResumenBorrado r = Vista.getInstancia().getControlador().getModelo().getFacturas().resumenBorrado(id);
                totalVersiones += r.versiones();
                totalLineas += r.lineas();
            }
        } catch (Exception e) {
            Dialogos.mostrarDialogoError("Eliminar", "Error al calcular el resumen: " + e.getMessage());
            return;
        }
        if (!Dialogos.mostrarDialogoConfirmacion("Eliminar",
                "Se van a eliminar físicamente " + facturaIds.size() + " factura(s).\n"
                        + "Se eliminarán " + totalVersiones + " versión(es) y " + totalLineas + " línea(s).\n\n"
                        + "¿Continuar?")) {
            return;
        }
        int borradas = 0;
        int fallos = 0;
        List<String> errores = new ArrayList<>();
        for (long id : facturaIds) {
            try {
                Vista.getInstancia().getControlador().getModelo().getFacturas().borrarFactura(id);
                borradas++;
            } catch (Exception e) {
                fallos++;
                errores.add("Factura " + id + ": " + e.getMessage());
            }
        }
        StringBuilder msg = new StringBuilder();
        msg.append("Eliminadas: ").append(borradas).append("\n");
        msg.append("Fallos: ").append(fallos);
        if (!errores.isEmpty()) {
            msg.append("\n\nErrores:\n").append(String.join("\n", errores));
        }
        Dialogos.mostrarDialogoInformacion("Eliminar", msg.toString());
        buscar();
    }

    @FXML
    private void exportarPdf() {
        List<FilaHistorial> seleccion = new ArrayList<>(tabla.getSelectionModel().getSelectedItems());
        if (seleccion.isEmpty()) {
            Dialogos.mostrarDialogoInformacion("Exportar PDF", "Selecciona al menos una factura del histórico.");
            return;
        }
        try {
            if (seleccion.size() == 1) {
                exportarUna(seleccion.get(0));
            } else {
                preguntarYExportarVarias(seleccion);
            }
        } catch (Exception e) {
            Dialogos.mostrarDialogoError("Exportar PDF", "Error: " + e.getMessage());
        }
    }

    private void exportarUna(FilaHistorial fila) throws Exception {
        Facturas.VersionCompleta vc = Vista.getInstancia().getControlador().getModelo().getFacturas().abrirVersion(fila.getVersionId());
        if (vc == null) {
            Dialogos.mostrarDialogoError("Exportar PDF", "No se encontró la versión de la factura seleccionada.");
            return;
        }
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Exportar PDF");
        File carpeta = carpetaExportacion();
        if (carpeta != null) {
            chooser.setInitialDirectory(carpeta);
        }
        chooser.setInitialFileName(Formatos.nombreArchivoPdf(vc.version().getNumero()));
        File f = chooser.showSaveDialog(Vista.getInstancia().getVentana());
        if (f == null) {
            return;
        }
        generarPdfs(List.of(vc), List.of(f.toPath()), f.toPath().getParent());
    }

    private void preguntarYExportarVarias(List<FilaHistorial> filas) throws Exception {
        ChoiceDialog<String> dialog = new ChoiceDialog<>("Un PDF por factura",
                "Un PDF por factura", "Un único PDF agrupado");
        dialog.setTitle("Exportar PDF");
        dialog.setHeaderText(null);
        dialog.setContentText("¿Cómo quieres exportar las " + filas.size() + " facturas seleccionadas?");
        Optional<String> resultado = dialog.showAndWait();
        if (resultado.isEmpty()) {
            return;
        }
        if ("Un PDF por factura".equals(resultado.get())) {
            exportarVarias(filas);
        } else {
            exportarAgrupado(filas);
        }
    }

    private void exportarVarias(List<FilaHistorial> filas) throws Exception {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Carpeta donde guardar los PDF");
        File carpeta = carpetaExportacion();
        if (carpeta != null) {
            chooser.setInitialDirectory(carpeta);
        }
        File destino = chooser.showDialog(Vista.getInstancia().getVentana());
        if (destino == null) {
            return;
        }
        List<Facturas.VersionCompleta> versiones = new ArrayList<>();
        List<Path> rutas = new ArrayList<>();
        for (FilaHistorial fila : filas) {
            Facturas.VersionCompleta vc = Vista.getInstancia().getControlador().getModelo().getFacturas().abrirVersion(fila.getVersionId());
            if (vc != null) {
                versiones.add(vc);
                rutas.add(destino.toPath().resolve(Formatos.nombreArchivoPdf(vc.version().getNumero())));
            }
        }
        if (versiones.isEmpty()) {
            Dialogos.mostrarDialogoError("Exportar PDF", "No se pudo cargar ninguna de las versiones seleccionadas.");
            return;
        }
        generarPdfs(versiones, rutas, destino.toPath());
    }

    private void exportarAgrupado(List<FilaHistorial> filas) throws Exception {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Guardar PDF agrupado");
        File carpeta = carpetaExportacion();
        if (carpeta != null) {
            chooser.setInitialDirectory(carpeta);
        }
        chooser.setInitialFileName("facturas.pdf");
        File destino = chooser.showSaveDialog(Vista.getInstancia().getVentana());
        if (destino == null) {
            return;
        }
        List<Facturas.VersionCompleta> versiones = new ArrayList<>();
        for (FilaHistorial fila : filas) {
            Facturas.VersionCompleta vc = Vista.getInstancia().getControlador().getModelo().getFacturas().abrirVersion(fila.getVersionId());
            if (vc != null) {
                versiones.add(vc);
            }
        }
        if (versiones.isEmpty()) {
            Dialogos.mostrarDialogoError("Exportar PDF", "No se pudo cargar ninguna de las versiones seleccionadas.");
            return;
        }
        btnExportarPdf.setDisable(true);
        Empresa empresa = Vista.getInstancia().getControlador().buscarEmpresa();
        String color = colorPdfPreferido();
        Task<Void> tarea = new Task<>() {
            @Override
            protected Void call() throws Exception {
                new ExportadorPdf().exportarAgrupado(versiones, empresa, destino.toPath(), color);
                return null;
            }
        };
        tarea.setOnSucceeded(e -> {
            btnExportarPdf.setDisable(false);
            recordarCarpeta(destino.toPath().getParent());
            Dialogos.mostrarDialogoInformacion("Exportar PDF", "PDF agrupado generado:\n" + destino.toPath());
        });
        tarea.setOnFailed(e -> {
            btnExportarPdf.setDisable(false);
            Dialogos.mostrarDialogoError("Exportar PDF", "No se pudo generar el PDF: "
                    + (tarea.getException() == null ? "error desconocido" : tarea.getException().getMessage()));
        });
        new Thread(tarea).start();
    }

    private void generarPdfs(List<Facturas.VersionCompleta> versiones, List<Path> rutas, Path carpetaRecordar) {
        Empresa empresa;
        try {
            empresa = Vista.getInstancia().getControlador().buscarEmpresa();
        } catch (Exception e) {
            Dialogos.mostrarDialogoError("Exportar PDF", "No se pudieron leer los datos de la empresa: " + e.getMessage());
            return;
        }
        final String color = colorPdfPreferido();
        btnExportarPdf.setDisable(true);
        Task<int[]> tarea = new Task<>() {
            @Override
            protected int[] call() {
                int generados = 0;
                List<String> fallos = new ArrayList<>();
                for (int i = 0; i < versiones.size(); i++) {
                    try {
                        new ExportadorPdf().exportar(versiones.get(i), empresa, rutas.get(i), color);
                        generados++;
                    } catch (Exception e) {
                        fallos.add(rutas.get(i).getFileName() + ": " + e.getMessage());
                    }
                }
                updateMessage(String.join("\n", fallos));
                return new int[]{generados, rutas.size() - generados};
            }
        };
        tarea.setOnSucceeded(e -> {
            btnExportarPdf.setDisable(false);
            recordarCarpeta(carpetaRecordar);
            int[] r = tarea.getValue();
            StringBuilder msg = new StringBuilder();
            if (r[1] == 0 && r[0] == 1) {
                msg.append("PDF generado en:\n").append(rutas.get(0));
            } else {
                msg.append(r[0]).append(" PDF generados en:\n").append(carpetaRecordar);
            }
            String fallos = tarea.getMessage();
            if (fallos != null && !fallos.isBlank()) {
                msg.append("\n\nNo se pudieron generar:\n").append(fallos);
            }
            Dialogos.mostrarDialogoInformacion("Exportar PDF", msg.toString());
        });
        tarea.setOnFailed(e -> {
            btnExportarPdf.setDisable(false);
            Dialogos.mostrarDialogoError("Exportar PDF", "No se pudo generar el PDF: "
                    + (tarea.getException() == null ? "error desconocido" : tarea.getException().getMessage()));
        });
        new Thread(tarea).start();
    }

    private File carpetaExportacion() {
        try {
            String pref = Vista.getInstancia().getControlador().preferencia(PREV_EXPORT);
            if (pref != null && !pref.isBlank()) {
                File f = new File(pref);
                if (f.isDirectory()) {
                    return f;
                }
            }
        } catch (Exception ignored) {
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
        }
    }

    private String colorPdfPreferido() {
        try {
            return Vista.getInstancia().getControlador().preferencia(ExportadorPdf.PREF_COLOR);
        } catch (Exception e) {
            return null;
        }
    }

    @FXML
    private void volver() {
        Vista.getInstancia().mostrar("MenuPrincipal.fxml");
    }
}
