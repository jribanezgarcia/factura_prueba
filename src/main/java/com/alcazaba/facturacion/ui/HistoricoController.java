package com.alcazaba.facturacion.ui;

import com.alcazaba.facturacion.model.Empresa;
import com.alcazaba.facturacion.model.EstadoFactura;
import com.alcazaba.facturacion.model.FiltrosHistorial;
import com.alcazaba.facturacion.model.HistorialFila;
import com.alcazaba.facturacion.model.Serie;
import com.alcazaba.facturacion.pdf.PdfService;
import com.alcazaba.facturacion.service.FacturaService;
import com.alcazaba.facturacion.service.Servicios;
import com.alcazaba.facturacion.util.Formatos;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import java.io.File;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Historico: filtros combinables (serie, cliente/NIF, fechas, importes y
 * estado) con boton Buscar, una fila por version, y apertura de la version
 * seleccionada.
 */
public class HistoricoController implements Vista {

    private static final String PREV_EXPORT = "ultima_carpeta_export";

    private Servicios servicios;
    private Navegador nav;

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
    @FXML
    private HBox barraNavegacion;
    @FXML
    private TableView<HistorialFila> tabla;
    @FXML
    private TableColumn<HistorialFila, String> colFecha;
    @FXML
    private TableColumn<HistorialFila, String> colNumero;
    @FXML
    private TableColumn<HistorialFila, String> colVersion;
    @FXML
    private TableColumn<HistorialFila, String> colCliente;
    @FXML
    private TableColumn<HistorialFila, String> colNif;
    @FXML
    private TableColumn<HistorialFila, String> colBase;
    @FXML
    private TableColumn<HistorialFila, String> colIva;
    @FXML
    private TableColumn<HistorialFila, String> colRetencion;
    @FXML
    private TableColumn<HistorialFila, String> colTotal;
    @FXML
    private TableColumn<HistorialFila, String> colEstado;

    @Override
    public void setServicios(Servicios s) {
        this.servicios = s;
    }

    @Override
    public void setNavegador(Navegador n) {
        this.nav = n;
    }

    @Override
    public void alIniciar() {
        barraNavegacion.getChildren().add(BarraNavegacion.crear(nav, "historico"));
        try {
            comboSerie.getItems().add("(Todas)");
            for (Serie s : servicios.series.listar()) {
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
            javafx.scene.control.TableRow<HistorialFila> fila = new javafx.scene.control.TableRow<>();
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
        MenuItem itemBorrar = new MenuItem("Borrar facturas seleccionadas");
        itemBorrar.setOnAction(e -> borrarSeleccionadas());
        menu.getItems().addAll(itemExportar, itemAnular, itemBorrar);
        tabla.setContextMenu(menu);

        nav.stage().getScene().getAccelerators().put(
                new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN), this::buscar);
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
            tabla.setItems(FXCollections.observableArrayList(servicios.historialService.buscar(f)));
        } catch (Exception e) {
            Dialogos.error("Histórico", "Error al buscar: " + e.getMessage());
        }
    }

    private void abrirVersion(HistorialFila fila) {
        EditorController editor = nav.mostrar("/com/alcazaba/facturacion/ui/Editor.fxml");
        editor.cargarVersion(fila.getVersionId());
    }

    @FXML
    private void generarMensual() {
        GenerarFacturasMensualesController.abrir(nav);
    }

    @FXML
    private void anularSeleccionadas() {
        List<HistorialFila> seleccion = new ArrayList<>(tabla.getSelectionModel().getSelectedItems());
        if (seleccion.isEmpty()) {
            Dialogos.info("Borrar/Anular", "Selecciona al menos una factura del histórico.");
            return;
        }
        Set<Long> facturaIds = new LinkedHashSet<>();
        for (HistorialFila fila : seleccion) {
            facturaIds.add(fila.getFacturaId());
        }
        if (!Dialogos.confirmar("Borrar/Anular",
                "Se anularán " + facturaIds.size() + " factura(s).\n"
                        + "Las ya anuladas no se modificarán.\n\n¿Continuar?")) {
            return;
        }
        try {
            var resultado = servicios.estado.anularFacturas(new ArrayList<>(facturaIds));
            StringBuilder msg = new StringBuilder();
            msg.append("Anuladas: ").append(resultado.getAnuladas()).append("\n");
            msg.append("Ya anuladas: ").append(resultado.getYaAnuladas());
            if (resultado.getFallos() > 0) {
                msg.append("\n\nFallos:\n").append(String.join("\n", resultado.getErrores()));
            }
            Dialogos.info("Borrar/Anular", msg.toString());
            buscar();
        } catch (Exception e) {
            Dialogos.error("Borrar/Anular", "Error al anular: " + e.getMessage());
        }
    }

    @FXML
    private void borrarSeleccionadas() {
        List<HistorialFila> seleccion = new ArrayList<>(tabla.getSelectionModel().getSelectedItems());
        if (seleccion.isEmpty()) {
            Dialogos.info("Borrar", "Selecciona al menos una factura del histórico.");
            return;
        }
        Set<Long> facturaIds = new LinkedHashSet<>();
        for (HistorialFila fila : seleccion) {
            facturaIds.add(fila.getFacturaId());
        }
        int totalVersiones = 0;
        int totalLineas = 0;
        try {
            for (long id : facturaIds) {
                FacturaService.ResumenBorrado r = servicios.factura.resumenBorrado(id);
                totalVersiones += r.versiones();
                totalLineas += r.lineas();
            }
        } catch (Exception e) {
            Dialogos.error("Borrar", "Error al calcular el resumen: " + e.getMessage());
            return;
        }
        if (!Dialogos.confirmar("Borrar",
                "Se van a borrar físicamente " + facturaIds.size() + " factura(s).\n"
                        + "Se eliminarán " + totalVersiones + " versión(es) y " + totalLineas + " línea(s).\n\n"
                        + "¿Continuar?")) {
            return;
        }
        int borradas = 0;
        int fallos = 0;
        List<String> errores = new ArrayList<>();
        for (long id : facturaIds) {
            try {
                servicios.factura.borrarFactura(id);
                borradas++;
            } catch (Exception e) {
                fallos++;
                errores.add("Factura " + id + ": " + e.getMessage());
            }
        }
        StringBuilder msg = new StringBuilder();
        msg.append("Borradas: ").append(borradas).append("\n");
        msg.append("Fallos: ").append(fallos);
        if (!errores.isEmpty()) {
            msg.append("\n\nErrores:\n").append(String.join("\n", errores));
        }
        Dialogos.info("Borrar", msg.toString());
        buscar();
    }

    @FXML
    private void exportarPdf() {
        List<HistorialFila> seleccion = new ArrayList<>(tabla.getSelectionModel().getSelectedItems());
        if (seleccion.isEmpty()) {
            Dialogos.info("Exportar PDF", "Selecciona al menos una factura del histórico.");
            return;
        }
        try {
            if (seleccion.size() == 1) {
                exportarUna(seleccion.get(0));
            } else {
                preguntarYExportarVarias(seleccion);
            }
        } catch (Exception e) {
            Dialogos.error("Exportar PDF", "Error: " + e.getMessage());
        }
    }

    private void exportarUna(HistorialFila fila) throws Exception {
        FacturaService.VersionCompleta vc = servicios.factura.abrirVersion(fila.getVersionId());
        if (vc == null) {
            Dialogos.error("Exportar PDF", "No se encontró la versión de la factura seleccionada.");
            return;
        }
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Exportar PDF");
        File carpeta = carpetaExportacion();
        if (carpeta != null) {
            chooser.setInitialDirectory(carpeta);
        }
        chooser.setInitialFileName(Formatos.nombreArchivoPdf(vc.version().getNumero()));
        File f = chooser.showSaveDialog(nav.stage());
        if (f == null) {
            return;
        }
        generarPdfs(List.of(vc), List.of(f.toPath()), f.toPath().getParent());
    }

    private void preguntarYExportarVarias(List<HistorialFila> filas) throws Exception {
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

    private void exportarVarias(List<HistorialFila> filas) throws Exception {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Carpeta donde guardar los PDF");
        File carpeta = carpetaExportacion();
        if (carpeta != null) {
            chooser.setInitialDirectory(carpeta);
        }
        File destino = chooser.showDialog(nav.stage());
        if (destino == null) {
            return;
        }
        List<FacturaService.VersionCompleta> versiones = new ArrayList<>();
        List<Path> rutas = new ArrayList<>();
        for (HistorialFila fila : filas) {
            FacturaService.VersionCompleta vc = servicios.factura.abrirVersion(fila.getVersionId());
            if (vc != null) {
                versiones.add(vc);
                rutas.add(destino.toPath().resolve(Formatos.nombreArchivoPdf(vc.version().getNumero())));
            }
        }
        if (versiones.isEmpty()) {
            Dialogos.error("Exportar PDF", "No se pudo cargar ninguna de las versiones seleccionadas.");
            return;
        }
        generarPdfs(versiones, rutas, destino.toPath());
    }

    private void exportarAgrupado(List<HistorialFila> filas) throws Exception {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Guardar PDF agrupado");
        File carpeta = carpetaExportacion();
        if (carpeta != null) {
            chooser.setInitialDirectory(carpeta);
        }
        chooser.setInitialFileName("facturas.pdf");
        File destino = chooser.showSaveDialog(nav.stage());
        if (destino == null) {
            return;
        }
        List<FacturaService.VersionCompleta> versiones = new ArrayList<>();
        for (HistorialFila fila : filas) {
            FacturaService.VersionCompleta vc = servicios.factura.abrirVersion(fila.getVersionId());
            if (vc != null) {
                versiones.add(vc);
            }
        }
        if (versiones.isEmpty()) {
            Dialogos.error("Exportar PDF", "No se pudo cargar ninguna de las versiones seleccionadas.");
            return;
        }
        btnExportarPdf.setDisable(true);
        Empresa empresa = servicios.config.getEmpresa();
        String color = colorPdfPreferido();
        Task<Void> tarea = new Task<>() {
            @Override
            protected Void call() throws Exception {
                new PdfService().exportarAgrupado(versiones, empresa, destino.toPath(), color);
                return null;
            }
        };
        tarea.setOnSucceeded(e -> {
            btnExportarPdf.setDisable(false);
            recordarCarpeta(destino.toPath().getParent());
            Dialogos.info("Exportar PDF", "PDF agrupado generado:\n" + destino.toPath());
        });
        tarea.setOnFailed(e -> {
            btnExportarPdf.setDisable(false);
            Dialogos.error("Exportar PDF", "No se pudo generar el PDF: "
                    + (tarea.getException() == null ? "error desconocido" : tarea.getException().getMessage()));
        });
        new Thread(tarea).start();
    }

    private void generarPdfs(List<FacturaService.VersionCompleta> versiones, List<Path> rutas, Path carpetaRecordar) {
        Empresa empresa;
        try {
            empresa = servicios.config.getEmpresa();
        } catch (Exception e) {
            Dialogos.error("Exportar PDF", "No se pudieron leer los datos de la empresa: " + e.getMessage());
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
                        new PdfService().exportar(versiones.get(i), empresa, rutas.get(i), color);
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
            Dialogos.info("Exportar PDF", msg.toString());
        });
        tarea.setOnFailed(e -> {
            btnExportarPdf.setDisable(false);
            Dialogos.error("Exportar PDF", "No se pudo generar el PDF: "
                    + (tarea.getException() == null ? "error desconocido" : tarea.getException().getMessage()));
        });
        new Thread(tarea).start();
    }

    private File carpetaExportacion() {
        try {
            String pref = servicios.config.getPreferencia(PREV_EXPORT);
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
            servicios.config.setPreferencia(PREV_EXPORT, carpeta.toString());
        } catch (Exception ignored) {
        }
    }

    private String colorPdfPreferido() {
        try {
            return servicios.config.getPreferencia(PdfService.PREF_COLOR);
        } catch (Exception e) {
            return null;
        }
    }

    @FXML
    private void volver() {
        nav.mostrar("/com/alcazaba/facturacion/ui/MenuPrincipal.fxml");
    }
}
