package com.alcazaba.facturacion.ui;

import com.alcazaba.facturacion.db.Database;
import com.alcazaba.facturacion.model.Cliente;
import com.alcazaba.facturacion.model.DatosPago;
import com.alcazaba.facturacion.model.Empresa;
import com.alcazaba.facturacion.model.EstadoFactura;
import com.alcazaba.facturacion.model.FacturaVersion;
import com.alcazaba.facturacion.model.LineaFactura;
import com.alcazaba.facturacion.model.ResumenFactura;
import com.alcazaba.facturacion.model.Serie;
import com.alcazaba.facturacion.model.TipoIva;
import com.alcazaba.facturacion.model.TipoRetencion;
import com.alcazaba.facturacion.pdf.PdfService;
import com.alcazaba.facturacion.service.CalculoService;
import com.alcazaba.facturacion.service.FacturaService;
import com.alcazaba.facturacion.service.Servicios;
import com.alcazaba.facturacion.service.Sesion;
import com.alcazaba.facturacion.service.ValidationException;
import com.alcazaba.facturacion.util.DocumentoFiscalValidator;
import com.alcazaba.facturacion.util.Formatos;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Editor de factura completo (6.2-6.6, 7.x, 8.1): cabecera con serie, fecha,
 * numero propuesto/editable y cliente buscable; tabla de lineas editable con
 * flujo Enter y Supr; recalculado precio-total con o sin IVA; descuento
 * general y desglose por tipos de IVA; observaciones; validaciones al guardar;
 * estados Emitida/Anulada; creacion de rectificativas y exportacion a PDF.
 */
public class EditorController implements Vista {

    /** Trazas temporales para diagnosticar el flujo de foco al editar líneas. */
    private static final boolean DIAGNOSTICO_FOCO = false;
    private static final String PREV_SERIE = "ultima_serie";
    private static final String PREV_CARPETA = "carpeta_facturas";
    private static final String PREV_EXPORT = "ultima_carpeta_export";

    private Servicios servicios;
    private Navegador nav;
    private boolean modificado;
    private boolean cargando;
    private Long facturaAbiertaId;
    private Long versionAbiertaId;
    private Integer correlativoFijo;
    private EstadoFactura estadoActual;
    private Cliente clienteActual;
    private int descuento;
    private boolean corrigiendoNif;

    private final ObservableList<LineaFactura> lineas = FXCollections.observableArrayList();
    private final ObservableList<TipoIva> tiposIva = FXCollections.observableArrayList();
    private final ObservableList<TipoRetencion> tiposRetencion = FXCollections.observableArrayList();
    private TipoRetencion retencionActual;

    @FXML
    private Label lblTitulo;
    @FXML
    private Label lblEstado;
    @FXML
    private Label lblBaseTotal;
    @FXML
    private Label lblIvaTotal;
    @FXML
    private Label lblRetencionNombre;
    @FXML
    private Label lblRetencionImporte;
    @FXML
    private Label lblTotal;
    @FXML
    private ImageView logo;
    @FXML
    private HBox barraNavegacion;
    @FXML
    private Label lblReferencia;
    @FXML
    private ComboBox<Serie> comboSerie;
    @FXML
    private DatePicker fecha;
    @FXML
    private TextField txtNumero;
    @FXML
    private ComboBox<Cliente> comboCliente;
    @FXML
    private TextField cliNombre;
    @FXML
    private TextField cliNif;
    @FXML
    private TextField cliDireccion;
    @FXML
    private TextField cliCp;
    @FXML
    private TextField cliLocalidad;
    @FXML
    private TextField cliProvincia;
    @FXML
    private TextField cliEmail;
    @FXML
    private TextField txtReferencia;
    @FXML
    private TextField txtFormaPago;
    @FXML
    private DatePicker vencimiento;
    @FXML
    private TextField txtRealizadaPor;
    @FXML
    private TextField txtDescuento;
    @FXML
    private ComboBox<TipoRetencion> comboRetencion;
    @FXML
    private TextArea txtObservaciones;
    @FXML
    private CheckBox chkTotalConIva;
    @FXML
    private TableView<LineaFactura> tablaLineas;
    @FXML
    private TableColumn<LineaFactura, String> colCantidad;
    @FXML
    private TableColumn<LineaFactura, String> colDescripcion;
    @FXML
    private TableColumn<LineaFactura, String> colPrecio;
    @FXML
    private TableColumn<LineaFactura, String> colTotal;
    @FXML
    private TableColumn<LineaFactura, TipoIva> colIva;
    @FXML
    private Button btnGuardar;
    @FXML
    private Button btnExportar;
    @FXML
    private Button btnAnular;
    @FXML
    private Button btnRestaurar;
    @FXML
    private Button btnAnadirLinea;
    @FXML
    private Button btnEliminarLinea;

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
        cargando = true;
        try {
            barraNavegacion.getChildren().add(BarraNavegacion.crear(nav, "editor"));
            cargarLogo();
            cargarSeries();
            cargarFechaInicial();
            cargarTiposIva();
            cargarTiposRetencion();
            configurarBusquedaCliente();
            configurarDetalleCliente();
            configurarTabla();
            configurarCambios();
            atajos();
            actualizarVisibilidadReferencia(comboSerie.getValue());
            actualizarBotonesEstado();
            if (lineas.isEmpty()) {
                lineas.add(nuevaLinea());
            }
            recalcularNumero();
        } finally {
            cargando = false;
        }
        actualizarResumen();
    }

    private void cargarLogo() {
        try {
            Empresa empresa = servicios.config.getEmpresa();
            String ruta = empresa.getLogoPath();
            if (ruta == null || ruta.isBlank()) {
                return;
            }
            File f = new File(ruta);
            if (!f.exists()) {
                return;
            }
            Image img = new Image(f.toURI().toString());
            if (img.isError()) {
                return;
            }
            logo.setImage(img);
            logo.setFitWidth(92);
            logo.setPreserveRatio(true);
        } catch (Exception ignored) {
        }
    }

    /**
     * Carga la version indicada en el editor (para ver, editar o exportar).
     */
    public void cargarVersion(long versionId) {
        try {
            FacturaService.VersionCompleta vc = servicios.factura.abrirVersion(versionId);
            if (vc == null) {
                Dialogos.error("Factura", "No se pudo abrir la versión.");
                return;
            }
            cargando = true;
            try {
                facturaAbiertaId = vc.factura().getId();
                versionAbiertaId = versionId;
                correlativoFijo = vc.factura().getCorrelativo();
                estadoActual = servicios.factura.estadoActual(facturaAbiertaId);

                Serie serie = servicios.series.getById(vc.factura().getSerieId());
                comboSerie.setValue(serie);
                comboSerie.setDisable(true);
                actualizarVisibilidadReferencia(serie);
                fecha.setValue(vc.version().getFechaFactura());
                txtNumero.setText(vc.version().getNumero());

                Cliente cli = vc.cliente();
                if (cli == null) {
                    cli = new Cliente();
                    cli.setId(vc.factura().getClienteId());
                }
                comboCliente.setValue(cli);
                cargarDatosCliente(cli);

                lineas.setAll(vc.lineas());
                descuento = vc.version().getDescuentoPorcentaje();
                txtDescuento.setText(String.valueOf(descuento));
                asegurarRetencionEnLista(vc.version().getTipoRetencionId(),
                        vc.version().getTipoRetencionNombre(), vc.version().getTipoRetencionPorcentaje());
                seleccionarRetencionPorId(vc.version().getTipoRetencionId());
                txtObservaciones.setText(nz(vc.version().getObservaciones()));
                txtReferencia.setText(nz(vc.version().getReferenciaRectifica()));
                txtFormaPago.setText(nz(vc.version().getFormaPago()));
                vencimiento.setValue(vc.version().getVencimiento());
                txtRealizadaPor.setText(nz(vc.version().getRealizadaPor()));

                lblTitulo.setText("Factura " + vc.version().getNumero()
                        + " (v" + vc.version().getVersionNum() + ")");
                lblEstado.setVisible(estadoActual == EstadoFactura.ANULADA);
                lblEstado.setManaged(estadoActual == EstadoFactura.ANULADA);
                setEditable(estadoActual == EstadoFactura.EMITIDA);
                actualizarBotonesEstado();
                modificado = false;
            } finally {
                cargando = false;
            }
            actualizarResumen();
        } catch (Exception e) {
            Dialogos.error("Factura", "Error al abrir la factura: " + e.getMessage());
        }
    }

    /** Carga la ultima version (estado actual) de la factura. */
    public void cargarFactura(long facturaId) {
        try {
            FacturaVersion v = servicios.versionado.ultimaVersion(facturaId);
            if (v == null) {
                Dialogos.error("Factura", "No se pudo abrir la factura.");
                return;
            }
            cargarVersion(v.getId());
        } catch (Exception e) {
            Dialogos.error("Factura", "Error al abrir la factura: " + e.getMessage());
        }
    }

    @Override
    public boolean puedeCerrar() {
        if (!modificado) {
            return true;
        }
        Dialogos.CambiosSinGuardar r = Dialogos.confirmarCambiosSinGuardar();
        if (r == Dialogos.CambiosSinGuardar.GUARDAR) {
            return guardar();
        }
        return r == Dialogos.CambiosSinGuardar.DESCARTAR;
    }

    // ------------------------------------------------------------------
    // Carga inicial
    // ------------------------------------------------------------------

    private void cargarSeries() {
        try {
            List<Serie> series = servicios.series.listar();
            comboSerie.getItems().setAll(series);
            Serie inicial = null;
            String ultima = servicios.config.getPreferencia(PREV_SERIE);
            if (ultima != null) {
                for (Serie s : series) {
                    if (ultima.equals(s.getCodigo())) {
                        inicial = s;
                        break;
                    }
                }
            }
            if (inicial == null) {
                for (Serie s : series) {
                    if (!s.isEsRectificativa()) {
                        inicial = s;
                        break;
                    }
                }
            }
            if (inicial == null && !series.isEmpty()) {
                inicial = series.get(0);
            }
            comboSerie.setValue(inicial);
        } catch (Exception e) {
            Dialogos.error("Factura", "No se pudieron cargar las series: " + e.getMessage());
        }
    }

    private void cargarFechaInicial() {
        LocalDate fechaInicial = Sesion.fechaTrabajo() != null ? Sesion.fechaTrabajo() : LocalDate.now();
        fecha.setValue(fechaInicial);
    }

    private void cargarTiposIva() {
        try {
            tiposIva.setAll(servicios.ivas.listar(true));
        } catch (Exception e) {
            tiposIva.clear();
        }
    }

    private void cargarTiposRetencion() {
        try {
            List<TipoRetencion> activas = servicios.retenciones.listar(true);
            TipoRetencion sin = new TipoRetencion();
            sin.setId(null);
            sin.setNombre("Sin retención");
            sin.setPorcentaje(0);
            tiposRetencion.setAll(sin);
            tiposRetencion.addAll(activas);
            comboRetencion.setItems(tiposRetencion);
            comboRetencion.setConverter(new StringConverter<>() {
                @Override
                public String toString(TipoRetencion t) {
                    return t == null ? "" : t.toString();
                }

                @Override
                public TipoRetencion fromString(String s) {
                    return null;
                }
            });
            comboRetencion.setValue(sin);
        } catch (Exception e) {
            tiposRetencion.clear();
            TipoRetencion sin = new TipoRetencion();
            sin.setId(null);
            sin.setNombre("Sin retención");
            sin.setPorcentaje(0);
            tiposRetencion.add(sin);
            comboRetencion.setItems(tiposRetencion);
            comboRetencion.setValue(sin);
        }
    }

    private void configurarBusquedaCliente() {
        comboCliente.setEditable(true);
        comboCliente.setConverter(new StringConverter<>() {
            @Override
            public String toString(Cliente c) {
                return c == null ? "" : c.nombreNif();
            }

            @Override
            public Cliente fromString(String s) {
                return clientePorTexto(s);
            }
        });
        comboCliente.getEditor().textProperty().addListener((o, a, b) -> {
            if (cargando) {
                return;
            }
            if (comboCliente.getValue() instanceof Cliente) {
                String display = comboCliente.getValue().toString();
                if (display != null && display.equals(b)) {
                    return;
                }
            }
            String texto = b == null ? "" : b.trim();
            if (texto.isEmpty()) {
                comboCliente.getItems().clear();
                return;
            }
            try {
                comboCliente.getItems().setAll(servicios.clientes.buscar(texto, true));
            } catch (Exception e) {
                Dialogos.error("Cliente", "Error al buscar clientes: " + e.getMessage());
            }
        });
        comboCliente.setOnShowing(e -> {
            if (cargando) {
                return;
            }
            try {
                String texto = comboCliente.getEditor().getText();
                if (texto == null || texto.isBlank()) {
                    comboCliente.getItems().setAll(servicios.clientes.listar(true));
                } else {
                    comboCliente.getItems().setAll(servicios.clientes.buscar(texto, true));
                }
            } catch (Exception ex) {
                Dialogos.error("Cliente", "Error al cargar clientes: " + ex.getMessage());
            }
        });
        comboCliente.setOnAction(e -> {
            if (cargando) {
                return;
            }
            Cliente c = comboCliente.getValue() instanceof Cliente cli
                    ? cli
                    : clientePorTexto(comboCliente.getEditor().getText());
            if (c != null) {
                cargarDatosCliente(c);
                marcarModificado();
            }
        });
    }

    private Cliente clientePorTexto(String s) {
        if (s == null) {
            return null;
        }
        String texto = s.trim();
        for (Cliente c : comboCliente.getItems()) {
            if (c.getNombre().equalsIgnoreCase(texto)
                    || (c.getNif() != null && c.getNif().equalsIgnoreCase(texto))) {
                return c;
            }
        }
        return null;
    }

    private void configurarDetalleCliente() {
        for (TextField t : new TextField[]{cliNombre, cliNif, cliDireccion, cliCp, cliLocalidad, cliProvincia, cliEmail}) {
            t.textProperty().addListener((o, a, b) -> {
                if (!cargando) {
                    marcarModificado();
                }
            });
        }
        cliNif.setOnAction(e -> validarNifCliente(true));
        cliNif.focusedProperty().addListener((o, anterior, tieneFoco) -> {
            if (!tieneFoco && !cargando && !corrigiendoNif) {
                validarNifCliente(true);
            }
        });
    }

    private boolean validarNifCliente(boolean avisar) {
        if (DocumentoFiscalValidator.esValido(cliNif.getText())) {
            cliNif.setStyle("");
            return true;
        }
        cliNif.setStyle("-fx-border-color: #d32f2f; -fx-border-width: 2;");
        if (avisar && !corrigiendoNif) {
            corrigiendoNif = true;
            Dialogos.error("NIF no válido", "Revise el DNI, NIE o NIF/CIF introducido.");
            corrigiendoNif = false;
            Platform.runLater(cliNif::requestFocus);
        }
        return false;
    }

    private void configurarCambios() {
        comboSerie.valueProperty().addListener((o, a, b) -> {
            if (cargando) {
                return;
            }
            guardarSeriePreferida(b);
            actualizarVisibilidadReferencia(b);
            recalcularNumero();
            marcarModificado();
        });
        fecha.valueProperty().addListener((o, a, b) -> {
            if (cargando) {
                return;
            }
            recalcularNumero();
            marcarModificado();
        });
        txtNumero.textProperty().addListener((o, a, b) -> {
            if (cargando) {
                return;
            }
            marcarModificado();
        });
        txtDescuento.textProperty().addListener((o, a, b) -> {
            if (cargando) {
                return;
            }
            Integer v = parseEntero(b);
            if (v == null || v < 0 || v > 100) {
                return;
            }
            descuento = v;
            actualizarResumen();
            marcarModificado();
        });
        comboRetencion.valueProperty().addListener((o, a, b) -> {
            if (cargando) {
                return;
            }
            retencionActual = (b == null || b.getId() == null) ? null : b;
            actualizarResumen();
            marcarModificado();
        });
        txtObservaciones.textProperty().addListener((o, a, b) -> {
            if (!cargando) {
                marcarModificado();
            }
        });
        txtFormaPago.textProperty().addListener((o, a, b) -> {
            if (!cargando) {
                marcarModificado();
            }
        });
        vencimiento.valueProperty().addListener((o, a, b) -> {
            if (!cargando) {
                marcarModificado();
            }
        });
        txtRealizadaPor.textProperty().addListener((o, a, b) -> {
            if (!cargando) {
                marcarModificado();
            }
        });
    }

    // ------------------------------------------------------------------
    // Tabla de lineas
    // ------------------------------------------------------------------

    private void configurarTabla() {
        tablaLineas.setEditable(true);
        tablaLineas.setItems(lineas);
        tablaLineas.setPlaceholder(new Label("Sin líneas."));

        colCantidad.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(mostrarCantidad(c.getValue())));
        colDescripcion.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(nz(c.getValue().getDescripcion())));
        colPrecio.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(Formatos.moneda(c.getValue().getPrecioUnitario())));
        colTotal.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(Formatos.moneda(c.getValue().getTotalBase())));
        colIva.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(tipoIvaDe(c.getValue())));

        colCantidad.setCellFactory(c -> new CeldaCantidad());
        colDescripcion.setCellFactory(c -> new CeldaDescripcion());
        colPrecio.setCellFactory(c -> new CeldaPrecio());
        colTotal.setCellFactory(c -> new CeldaTotal());
        colIva.setCellFactory(c -> new CeldaIva());

        tablaLineas.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.DELETE) {
                eliminarLinea();
                e.consume();
            } else if (e.getCode() == KeyCode.ENTER) {
                TablePosition<LineaFactura, ?> pos = tablaLineas.getFocusModel().getFocusedCell();
                if (pos != null && pos.getRow() >= 0 && tablaLineas.getEditingCell() == null) {
                    TableColumn<LineaFactura, ?> col = pos.getTableColumn();
                    if (col != null && col.isEditable()) {
                        tablaLineas.edit(pos.getRow(), col);
                    }
                }
                e.consume();
            }
        });

        if (DIAGNOSTICO_FOCO && nav != null && nav.stage() != null
                && nav.stage().getScene() != null) {
            nav.stage().getScene().focusOwnerProperty().addListener((o, anterior, actual) ->
                    trazarFoco("Scene.focusOwner", actual));
        }
    }

    private void trazarFoco(String paso, Object foco) {
        if (!DIAGNOSTICO_FOCO) {
            return;
        }
        TablePosition<LineaFactura, ?> edicion = tablaLineas == null ? null : tablaLineas.getEditingCell();
        String celda = edicion == null ? "sin edición"
                : "fila=" + edicion.getRow() + ", col=" + edicion.getColumn();
        System.out.println("[FOCO] " + paso + " | foco="
                + (foco == null ? "null" : foco.getClass().getSimpleName())
                + " | " + celda);
    }

    private String mostrarCantidad(LineaFactura l) {
        return String.valueOf(l == null ? 1 : l.getCantidad());
    }

    private void refrescarLineas() {
        tablaLineas.refresh();
    }

    private LineaFactura nuevaLinea() {
        LineaFactura l = new LineaFactura();
        l.setCantidad(1);
        TipoIva t = tipoIvaDefault();
        if (t != null) {
            l.setTipoIvaId(t.getId());
            l.setIvaNombre(t.getNombre());
            l.setIvaPorcentaje(t.getPorcentaje());
            l.setIvaMotivoExencion(t.getMotivoExencion());
        }
        return l;
    }

    private TipoIva tipoIvaDefault() {
        for (TipoIva t : tiposIva) {
            if (t.getPorcentaje() != null && t.getPorcentaje() == 21) {
                return t;
            }
        }
        for (TipoIva t : tiposIva) {
            if (t.getPorcentaje() != null) {
                return t;
            }
        }
        return tiposIva.isEmpty() ? null : tiposIva.get(0);
    }

    private TipoIva tipoIvaDe(LineaFactura l) {
        if (l == null) {
            return null;
        }
        if (l.getTipoIvaId() != null) {
            for (TipoIva t : tiposIva) {
                if (t.getId().equals(l.getTipoIvaId())) {
                    return t;
                }
            }
        }
        if (l.getIvaPorcentaje() != null) {
            for (TipoIva t : tiposIva) {
                if (Objects.equals(t.getPorcentaje(), l.getIvaPorcentaje())) {
                    return t;
                }
            }
        }
        return tiposIva.isEmpty() ? null : tiposIva.get(0);
    }

    private LineaFactura lineaDeCelda(TableCell<LineaFactura, ?> celda) {
        TableRow<LineaFactura> fila = celda.getTableRow();
        return fila == null ? null : fila.getItem();
    }

    // Recálculo por tipo de cambio (6.4)
    private void aplicarCantidad(LineaFactura l, int v) {
        l.setCantidad(v);
        BigDecimal total = CalculoService.totalLinea(l.getPrecioUnitario(), v);
        l.setTotalBase(total);
        l.setIvaImporte(CalculoService.ivaDeBase(total, l.getIvaPorcentaje()));
        marcarModificado();
    }

    private void aplicarPrecio(LineaFactura l, BigDecimal v) {
        l.setPrecioUnitario(v);
        BigDecimal total = CalculoService.totalLinea(v, l.getCantidad());
        l.setTotalBase(total);
        l.setIvaImporte(CalculoService.ivaDeBase(total, l.getIvaPorcentaje()));
        marcarModificado();
    }

    private void aplicarTotal(LineaFactura l, BigDecimal t) {
        if (chkTotalConIva.isSelected()) {
            CalculoService.ResultadoConIva r = CalculoService.calcularDesdeTotalConIva(t, l.getIvaPorcentaje());
            l.setTotalBase(r.base());
            l.setIvaImporte(r.iva());
            l.setPrecioUnitario(CalculoService.precioDesdeTotal(r.base(), l.getCantidad()));
        } else {
            l.setTotalBase(t);
            l.setIvaImporte(CalculoService.ivaDeBase(t, l.getIvaPorcentaje()));
            l.setPrecioUnitario(CalculoService.precioDesdeTotal(t, l.getCantidad()));
        }
        marcarModificado();
    }

    private void aplicarIva(LineaFactura l, TipoIva t) {
        l.setTipoIvaId(t.getId());
        l.setIvaNombre(t.getNombre());
        l.setIvaPorcentaje(t.getPorcentaje());
        l.setIvaMotivoExencion(t.getMotivoExencion());
        l.setIvaImporte(CalculoService.ivaDeBase(l.getTotalBase(), t.getPorcentaje()));
        marcarModificado();
    }

    private void avanzarDesde(TablePosition pos) {
        if (pos == null) {
            return;
        }
        int row = pos.getRow();
        int col = pos.getColumn();
        if (row < 0 || row >= lineas.size()) {
            return;
        }
        int targetRow = row;
        int targetCol = col + 1;
        if (targetCol > 3) {
            targetCol = 0;
            targetRow = row + 1;
            if (targetRow >= lineas.size()) {
                if (lineaConContenido(lineas.get(row))) {
                    lineas.add(nuevaLinea());
                    targetRow = lineas.size() - 1;
                } else {
                    targetRow = row;
                }
            }
        }
        final int r = targetRow;
        final TableColumn<LineaFactura, ?> c = tablaLineas.getColumns().get(targetCol);
        Platform.runLater(() -> editarCeldaSegura(r, c));
    }

    private void editarCeldaSegura(int r, TableColumn<LineaFactura, ?> c) {
        trazarFoco("editarCeldaSegura: antes de edit(" + r + ", " + c.getText() + ")", null);
        tablaLineas.scrollTo(r);
        tablaLineas.edit(r, c);
        Platform.runLater(() -> {
            TablePosition<?, ?> ed = tablaLineas.getEditingCell();
            if (ed == null || ed.getRow() != r || ed.getTableColumn() != c) {
                trazarFoco("editarCeldaSegura: edición no iniciada", null);
                tablaLineas.requestFocus();
                return;
            }
            Object editor = tablaLineas.lookup(".text-field");
            if (editor instanceof TextField tf) {
                tf.requestFocus();
                trazarFoco("editarCeldaSegura: editor enfocado", tf);
            } else {
                trazarFoco("editarCeldaSegura: editor no encontrado", null);
                tablaLineas.requestFocus();
            }
            Platform.runLater(() -> trazarFoco("editarCeldaSegura: comprobación posterior", 
                    nav.stage().getScene().getFocusOwner()));
        });
    }

    private boolean lineaConContenido(LineaFactura l) {
        return (l.getDescripcion() != null && !l.getDescripcion().isBlank())
                || l.getTotalBase().signum() > 0
                || l.getPrecioUnitario().signum() > 0;
    }

    @FXML
    private void anadirLinea() {
        lineas.add(nuevaLinea());
        refrescarLineas();
        actualizarResumen();
        marcarModificado();
        final int row = lineas.size() - 1;
        Platform.runLater(() -> tablaLineas.edit(row, colCantidad));
    }

    @FXML
    private void eliminarLinea() {
        LineaFactura sel = tablaLineas.getSelectionModel().getSelectedItem();
        if (sel == null) {
            return;
        }
        lineas.remove(sel);
        if (lineas.isEmpty()) {
            lineas.add(nuevaLinea());
        }
        refrescarLineas();
        actualizarResumen();
        marcarModificado();
    }

    private void actualizarResumen() {
        ResumenFactura r = CalculoService.resumen(lineas, descuento, retencionActual);
        lblBaseTotal.setText(Formatos.moneda(r.getBaseTotal()));
        lblIvaTotal.setText(Formatos.moneda(r.getIvaTotal()));
        boolean conRetencion = r.getImporteRetencion() != null && r.getImporteRetencion().compareTo(BigDecimal.ZERO) > 0;
        lblRetencionNombre.setVisible(conRetencion);
        lblRetencionNombre.setManaged(conRetencion);
        lblRetencionImporte.setVisible(conRetencion);
        lblRetencionImporte.setManaged(conRetencion);
        if (conRetencion) {
            String nombre = r.getNombreRetencion() != null && !r.getNombreRetencion().isBlank()
                    ? r.getNombreRetencion()
                    : "Retención " + r.getPorcentajeRetencion() + "%";
            lblRetencionNombre.setText(nombre);
            lblRetencionImporte.setText("-" + Formatos.moneda(r.getImporteRetencion()));
        }
        lblTotal.setText(Formatos.moneda(r.getTotal()));
    }

    // ------------------------------------------------------------------
    // Guardado
    // ------------------------------------------------------------------

    @FXML
    private boolean guardar() {
        if (!validarNifCliente(true)) {
            return false;
        }
        if (facturaAbiertaId != null && estadoActual != EstadoFactura.EMITIDA) {
            Dialogos.info("Guardar", "Una factura anulada no se puede editar.");
            return false;
        }
        LocalDate f = fecha.getValue();
        if (f == null) {
            Dialogos.error("Guardar", "Indique la fecha de la factura.");
            return false;
        }
        List<LineaFactura> lis = lineasGuardables();
        if (lis.isEmpty()) {
            Dialogos.error("Guardar", "La factura debe tener al menos una línea con contenido.");
            return false;
        }
        Cliente cli = clienteDeFormulario();
        if (cli != null && (cli.getNombre() == null || cli.getNombre().isBlank())) {
            Dialogos.error("Guardar", "Indique el nombre del cliente.");
            return false;
        }
        String obs = txtObservaciones.getText();
        String ref = txtReferencia.getText();
        DatosPago dp = new DatosPago(txtFormaPago.getText() == null ? "" : txtFormaPago.getText().trim(),
                vencimiento.getValue(),
                txtRealizadaPor.getText() == null ? "" : txtRealizadaPor.getText().trim());
        try {
            if (facturaAbiertaId == null) {
                Serie serie = comboSerie.getValue();
                if (serie == null) {
                    Dialogos.error("Guardar", "Seleccione la serie.");
                    return false;
                }
                Integer hueco = pedirHueco(serie, f);
                if (hueco != null) {
                    txtNumero.setText(servicios.numeros.formarNumero(serie, hueco, f));
                }
                Integer corr = servicios.numeros.parseCorrelativo(serie, txtNumero.getText());
                if (corr == null) {
                    Dialogos.error("Guardar", "El número no se ajusta al formato de la serie "
                            + serie.getCodigo() + " (p. ej. " + serie.getCodigo() + "-1).");
                    return false;
                }
                long id = servicios.factura.crearFactura(serie, f, cli, lis, descuento, obs, ref, corr, dp, retencionActual);
                guardarSeriePreferida(serie);
                cargarFactura(id);
                Dialogos.info("Guardar", "Factura guardada.");
            } else {
                Dialogos.ModoGuardarVersion modo = Dialogos.modoGuardarVersion();
                if (modo == Dialogos.ModoGuardarVersion.CANCELAR) {
                    return false;
                }
                FacturaVersion v = servicios.factura.guardarEditada(facturaAbiertaId, versionAbiertaId,
                        f, cli, lis, descuento, obs, ref, dp,
                        modo == Dialogos.ModoGuardarVersion.NUEVA_VERSION, retencionActual);
                txtNumero.setText(v.getNumero());
                lblTitulo.setText("Factura " + v.getNumero() + " (v" + v.getVersionNum() + ")");
                modificado = false;
                Dialogos.info("Guardar", "Factura guardada.");
            }
            return true;
        } catch (ValidationException e) {
            Dialogos.error("Guardar", e.getMessage());
            return false;
        } catch (Exception e) {
            Dialogos.error("Guardar", "Error al guardar: " + e.getMessage());
            return false;
        }
    }

    private List<LineaFactura> lineasGuardables() {
        List<LineaFactura> out = new ArrayList<>();
        for (LineaFactura l : lineas) {
            if (lineaConContenido(l)) {
                out.add(l);
            }
        }
        return out;
    }

    private Cliente clienteDeFormulario() {
        String nombre = cliNombre.getText() == null ? "" : cliNombre.getText().trim();
        String nif = cliNif.getText() == null ? "" : cliNif.getText().trim();
        String dir = cliDireccion.getText() == null ? "" : cliDireccion.getText().trim();
        String cp = cliCp.getText() == null ? "" : cliCp.getText().trim();
        String loc = cliLocalidad.getText() == null ? "" : cliLocalidad.getText().trim();
        String prov = cliProvincia.getText() == null ? "" : cliProvincia.getText().trim();
        String mail = cliEmail.getText() == null ? "" : cliEmail.getText().trim();
        boolean vacio = nombre.isEmpty() && nif.isEmpty() && dir.isEmpty() && cp.isEmpty()
                && loc.isEmpty() && prov.isEmpty() && mail.isEmpty();
        if (clienteActual == null && vacio) {
            return null;
        }
        Cliente c = clienteActual != null ? clienteActual : new Cliente();
        c.setNombre(nombre);
        c.setNif(nif);
        c.setDireccion(dir);
        c.setCp(cp);
        c.setLocalidad(loc);
        c.setProvincia(prov);
        c.setEmail(mail);
        return c;
    }

    // ------------------------------------------------------------------
    // Estados, rectificativas y PDF
    // ------------------------------------------------------------------

    private void actualizarBotonesEstado() {
        boolean abierta = facturaAbiertaId != null;
        boolean emitida = abierta && estadoActual == EstadoFactura.EMITIDA;
        boolean anulada = abierta && estadoActual == EstadoFactura.ANULADA;
        btnAnular.setVisible(emitida);
        btnAnular.setManaged(emitida);
        btnRestaurar.setVisible(anulada);
        btnRestaurar.setManaged(anulada);
        btnExportar.setDisable(versionAbiertaId == null);
    }

    private void setEditable(boolean e) {
        btnGuardar.setDisable(!e);
        btnAnadirLinea.setDisable(!e);
        btnEliminarLinea.setDisable(!e);
        fecha.setDisable(!e);
        txtNumero.setDisable(!e);
        comboCliente.setDisable(!e);
        cliNombre.setDisable(!e);
        cliNif.setDisable(!e);
        cliDireccion.setDisable(!e);
        cliCp.setDisable(!e);
        cliLocalidad.setDisable(!e);
        cliProvincia.setDisable(!e);
        txtReferencia.setDisable(!e);
        txtFormaPago.setDisable(!e);
        vencimiento.setDisable(!e);
        txtRealizadaPor.setDisable(!e);
        txtDescuento.setDisable(!e);
        comboRetencion.setDisable(!e);
        txtObservaciones.setDisable(!e);
        chkTotalConIva.setDisable(!e);
        tablaLineas.setEditable(e);
        for (TableColumn<LineaFactura, ?> col : tablaLineas.getColumns()) {
            col.setEditable(e);
        }
        tablaLineas.setDisable(!e);
    }

    @FXML
    private void anular() {
        if (facturaAbiertaId == null) {
            return;
        }
        if (modificado && !Dialogos.confirmar("Cambios sin guardar",
                "Hay cambios sin guardar que se descartarán. ¿Continuar?")) {
            return;
        }
        if (!Dialogos.confirmar("Anular factura",
                "¿Anular la factura? Se creará una nueva versión con estado Anulada.")) {
            return;
        }
        try {
            servicios.estado.anular(facturaAbiertaId);
            Dialogos.info("Anular", "Factura anulada.");
            cargarFactura(facturaAbiertaId);
        } catch (ValidationException e) {
            Dialogos.error("Anular", e.getMessage());
        } catch (Exception e) {
            Dialogos.error("Anular", "Error al anular: " + e.getMessage());
        }
    }

    @FXML
    private void restaurar() {
        if (facturaAbiertaId == null) {
            return;
        }
        if (!Dialogos.confirmar("Restaurar factura",
                "¿Restaurar la factura a estado Emitida? Se creará una nueva versión.")) {
            return;
        }
        try {
            servicios.estado.restaurar(facturaAbiertaId);
            Dialogos.info("Restaurar", "Factura restaurada.");
            cargarFactura(facturaAbiertaId);
        } catch (ValidationException e) {
            Dialogos.error("Restaurar", e.getMessage());
        } catch (Exception e) {
            Dialogos.error("Restaurar", "Error al restaurar: " + e.getMessage());
        }
    }

    @FXML
    private void crearRectificativa() {
        if (versionAbiertaId == null) {
            Dialogos.info("Rectificativa", "Abra primero la factura a rectificar.");
            return;
        }
        if (modificado && !Dialogos.confirmar("Cambios sin guardar",
                "Hay cambios sin guardar que se descartarán. ¿Continuar?")) {
            return;
        }
        try {
            long nueva = servicios.rectificativas.crearRectificativa(versionAbiertaId,
                    Sesion.fechaTrabajo() != null ? Sesion.fechaTrabajo() : LocalDate.now(), null);
            cargarFactura(nueva);
            Dialogos.info("Rectificativa", "Rectificativa creada. Puede editar la referencia antes de guardar.");
        } catch (ValidationException e) {
            Dialogos.error("Rectificativa", e.getMessage());
        } catch (Exception e) {
            Dialogos.error("Rectificativa", "Error al crear la rectificativa: " + e.getMessage());
        }
    }

    @FXML
    private void exportarPdf() {
        if (versionAbiertaId == null) {
            Dialogos.info("Exportar PDF", "Guarde primero la factura para poder exportarla.");
            return;
        }
        try {
            FacturaService.VersionCompleta vc = servicios.factura.abrirVersion(versionAbiertaId);
            if (vc == null) {
                return;
            }
            Empresa empresa = servicios.config.getEmpresa();
            Path sugerido = proponerDestinoPdf(vc);
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Exportar PDF");
            if (sugerido.getParent() != null && sugerido.getParent().toFile().exists()) {
                chooser.setInitialDirectory(sugerido.getParent().toFile());
            }
            chooser.setInitialFileName(sugerido.getFileName().toString());
            File f = chooser.showSaveDialog(nav.stage());
            if (f == null) {
                return;
            }
            Path ruta = f.toPath();
            final String colorPdf = colorPdfPreferido();
            btnExportar.setDisable(true);
            Task<Path> t = new Task<>() {
                @Override
                protected Path call() throws Exception {
                    new PdfService().exportar(vc, empresa, ruta, colorPdf);
                    return ruta;
                }
            };
            t.setOnSucceeded(e -> {
                btnExportar.setDisable(false);
                try {
                    if (ruta.getParent() != null) {
                        servicios.config.setPreferencia(PREV_EXPORT, ruta.getParent().toString());
                    }
                } catch (Exception ignored) {
                }
                Dialogos.info("Exportar PDF", "PDF generado en:\n" + ruta);
            });
            t.setOnFailed(e -> {
                btnExportar.setDisable(false);
                Dialogos.error("Exportar PDF", "No se pudo generar el PDF: "
                        + (t.getException() == null ? "error desconocido" : t.getException().getMessage()));
            });
            new Thread(t).start();
        } catch (Exception e) {
            Dialogos.error("Exportar PDF", "Error: " + e.getMessage());
        }
    }

    private String colorPdfPreferido() {
        try {
            return servicios.config.getPreferencia(PdfService.PREF_COLOR);
        } catch (Exception e) {
            return null;
        }
    }

    private Path proponerDestinoPdf(FacturaService.VersionCompleta vc) throws java.sql.SQLException {
        String carpeta = "Facturas";
        try {
            String pref = servicios.config.getPreferencia(PREV_CARPETA);
            if (pref != null && !pref.isBlank()) {
                carpeta = pref;
            }
        } catch (Exception ignored) {
        }
        Path base = Path.of(carpeta);
        if (!base.isAbsolute()) {
            base = Database.dataDir().resolve(base);
        }
        Serie serie = servicios.series.getById(vc.factura().getSerieId());
        String nombre = Formatos.nombreArchivoPdf(vc.version().getNumero());
        return base.resolve(String.valueOf(vc.version().getFechaFactura().getYear()))
                .resolve(serie.getCodigo())
                .resolve(nombre);
    }

    @FXML
    private void verVersiones() {
        if (facturaAbiertaId == null) {
            Dialogos.info("Versiones", "Guarde primero la factura para tener versiones.");
            return;
        }
        if (!puedeCerrar()) {
            return;
        }
        VersionesController vc = nav.mostrar("/com/alcazaba/facturacion/ui/Versiones.fxml");
        vc.cargarFactura(facturaAbiertaId);
    }

    // ------------------------------------------------------------------
    // Utilidades
    // ------------------------------------------------------------------

    private void asegurarRetencionEnLista(Long id, String nombre, Integer porcentaje) {
        if (id == null) {
            return;
        }
        for (TipoRetencion t : tiposRetencion) {
            if (id.equals(t.getId())) {
                return;
            }
        }
        TipoRetencion snapshot = new TipoRetencion();
        snapshot.setId(id);
        snapshot.setNombre(nz(nombre));
        snapshot.setPorcentaje(porcentaje != null ? porcentaje : 0);
        snapshot.setActivo(false);
        tiposRetencion.add(snapshot);
    }

    private void seleccionarRetencionPorId(Long id) {
        for (TipoRetencion t : tiposRetencion) {
            if (id == null ? t.getId() == null : id.equals(t.getId())) {
                comboRetencion.setValue(t);
                retencionActual = t.getId() == null ? null : t;
                return;
            }
        }
        TipoRetencion sin = tiposRetencion.isEmpty() ? null : tiposRetencion.get(0);
        comboRetencion.setValue(sin);
        retencionActual = null;
    }

    private void cargarDatosCliente(Cliente c) {
        this.clienteActual = c;
        cliNombre.setText(c == null ? "" : nz(c.getNombre()));
        cliNif.setText(c == null ? "" : nz(c.getNif()));
        cliDireccion.setText(c == null ? "" : nz(c.getDireccion()));
        cliCp.setText(c == null ? "" : nz(c.getCp()));
        cliLocalidad.setText(c == null ? "" : nz(c.getLocalidad()));
        cliProvincia.setText(c == null ? "" : nz(c.getProvincia()));
        cliEmail.setText(c == null ? "" : nz(c.getEmail()));
    }

    private void recalcularNumero() {
        Serie s = comboSerie.getValue();
        LocalDate f = fecha.getValue();
        if (s == null) {
            return;
        }
        if (facturaAbiertaId != null && correlativoFijo != null && f != null) {
            txtNumero.setText(servicios.numeros.formarNumero(s, correlativoFijo, f));
        } else if (f != null) {
            try {
                int correlativo = servicios.numeros.siguienteCorrelativo(s, f);
                txtNumero.setText(servicios.numeros.formarNumero(s, correlativo, f));
            } catch (Exception e) {
                txtNumero.setText("");
            }
        }
    }

    private Integer pedirHueco(Serie serie, LocalDate fecha) {
        try {
            List<Integer> huecos = servicios.numeros.huecosDisponibles(serie, fecha);
            if (huecos.isEmpty()) {
                return null;
            }
            int siguiente = servicios.numeros.siguienteCorrelativo(serie, fecha);
            List<Integer> menores = huecos.stream().filter(h -> h < siguiente).toList();
            if (menores.isEmpty()) {
                return null;
            }
            int hueco = menores.get(0);
            ChoiceDialog<String> dialog = new ChoiceDialog<>(
                    "Usar hueco " + hueco,
                    "Usar hueco " + hueco,
                    "Continuar con " + siguiente);
            dialog.setTitle("Número de factura");
            dialog.setHeaderText(null);
            dialog.setContentText("Hay un hueco disponible en la numeración:");
            Optional<String> resultado = dialog.showAndWait();
            if (resultado.isPresent() && resultado.get().startsWith("Usar hueco")) {
                return hueco;
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private void guardarSeriePreferida(Serie s) {
        try {
            if (s != null) {
                servicios.config.setPreferencia(PREV_SERIE, s.getCodigo());
            }
        } catch (Exception ignored) {
        }
    }

    private void actualizarVisibilidadReferencia(Serie s) {
        boolean r = s != null && s.isEsRectificativa();
        lblReferencia.setVisible(r);
        lblReferencia.setManaged(r);
        txtReferencia.setVisible(r);
        txtReferencia.setManaged(r);
    }

    private void marcarModificado() {
        this.modificado = true;
    }

    private Integer parseEntero(String t) {
        if (t == null || t.isBlank()) {
            return 0;
        }
        try {
            return Integer.parseInt(t.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String nz(String s) {
        return s == null ? "" : s;
    }

    private void atajos() {
        nav.stage().getScene().getAccelerators().put(
                new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN), this::guardar);
        nav.stage().getScene().getAccelerators().put(
                new KeyCodeCombination(KeyCode.P, KeyCombination.CONTROL_DOWN), this::exportarPdf);
        nav.stage().getScene().getAccelerators().put(
                new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN), this::nuevaFactura);
        nav.stage().getScene().getAccelerators().put(
                new KeyCodeCombination(KeyCode.ESCAPE), this::volver);
    }

    @FXML
    private void nuevaFactura() {
        if (puedeCerrar()) {
            nav.mostrar("/com/alcazaba/facturacion/ui/Editor.fxml");
        }
    }

    @FXML
    private void volver() {
        if (puedeCerrar()) {
            nav.mostrar("/com/alcazaba/facturacion/ui/MenuPrincipal.fxml");
        }
    }

    // ------------------------------------------------------------------
    // Celdas editables
    // ------------------------------------------------------------------

    private abstract class CeldaEditable extends TableCell<LineaFactura, String> {
        protected final TextField editor = new TextField();
        private boolean committing;

        CeldaEditable() {
            editor.setOnKeyPressed(e -> {
                if (e.getCode() == KeyCode.ENTER) {
                    commitYAvanzar();
                    e.consume();
                } else if (e.getCode() == KeyCode.ESCAPE) {
                    cancelEdit();
                    e.consume();
                }
            });
            editor.focusedProperty().addListener((o, a, b) -> {
                if (!b) {
                    commitSolo();
                }
            });
        }

        private void commitYAvanzar() {
            if (committing) {
                return;
            }
            committing = true;
            TablePosition<LineaFactura, ?> pos = tablaLineas.getEditingCell();
            trazarFoco("Enter: antes de confirmar", editor);
            tablaLineas.requestFocus();
            // No refrescar aquí: refresh() descarta la celda editada y, en la ventana real,
            // dejaba a JavaFX elegir el siguiente foco antes de que se abriera la nueva celda.
            commitValor(false);
            avanzarDesde(pos);
            trazarFoco("Enter: avance programado", null);
            committing = false;
        }

        private void commitSolo() {
            if (committing || !isEditing()) {
                return;
            }
            committing = true;
            commitValor(true);
            committing = false;
        }

        private void commitValor(boolean refrescar) {
            LineaFactura l = lineaDeCelda(this);
            if (l != null) {
                aplicar(l, editor.getText());
            }
            commitEdit(getItem());
            if (refrescar) {
                refrescarLineas();
            }
            actualizarResumen();
        }

        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (empty) {
                setText(null);
                setGraphic(null);
            } else {
                LineaFactura l = lineaDeCelda(this);
                setText(l == null ? "" : mostrar(l));
                setGraphic(null);
            }
        }

        @Override
        public void startEdit() {
            super.startEdit();
            LineaFactura l = lineaDeCelda(this);
            editor.setText(l == null ? "" : mostrar(l));
            setGraphic(editor);
            setText(null);
            editor.requestFocus();
            editor.selectAll();
        }

        abstract String mostrar(LineaFactura l);

        abstract void aplicar(LineaFactura l, String texto);
    }

    private final class CeldaCantidad extends CeldaEditable {
        @Override
        String mostrar(LineaFactura l) {
            return String.valueOf(l.getCantidad());
        }

        @Override
        void aplicar(LineaFactura l, String texto) {
            try {
                int v = Integer.parseInt(texto.trim());
                if (v < 1) {
                    v = 1;
                }
                aplicarCantidad(l, v);
            } catch (NumberFormatException ignored) {
            }
        }
    }

    private final class CeldaDescripcion extends CeldaEditable {
        @Override
        String mostrar(LineaFactura l) {
            return nz(l.getDescripcion());
        }

        @Override
        void aplicar(LineaFactura l, String texto) {
            l.setDescripcion(texto);
            marcarModificado();
        }
    }

    private final class CeldaPrecio extends CeldaEditable {
        @Override
        String mostrar(LineaFactura l) {
            return l.getPrecioUnitario() == null ? "0,00" : Formatos.moneda(l.getPrecioUnitario());
        }

        @Override
        void aplicar(LineaFactura l, String texto) {
            BigDecimal v = Formatos.parseEntrada(texto);
            if (v != null && v.signum() >= 0) {
                aplicarPrecio(l, v);
            }
        }
    }

    private final class CeldaTotal extends CeldaEditable {
        @Override
        String mostrar(LineaFactura l) {
            return l.getTotalBase() == null ? "0,00" : Formatos.moneda(l.getTotalBase());
        }

        @Override
        void aplicar(LineaFactura l, String texto) {
            BigDecimal v = Formatos.parseEntrada(texto);
            if (v != null && v.signum() >= 0) {
                aplicarTotal(l, v);
            }
        }
    }

    private final class CeldaIva extends TableCell<LineaFactura, TipoIva> {
        private final ComboBox<TipoIva> combo = new ComboBox<>();

        CeldaIva() {
            combo.setItems(tiposIva);
            combo.setMaxWidth(Double.MAX_VALUE);
            combo.setOnAction(e -> {
                LineaFactura l = lineaDeCelda(this);
                TipoIva t = combo.getValue();
                if (l != null && t != null && !Objects.equals(t.getId(), l.getTipoIvaId())) {
                    aplicarIva(l, t);
                    refrescarLineas();
                    actualizarResumen();
                }
            });
        }

        @Override
        protected void updateItem(TipoIva item, boolean empty) {
            super.updateItem(item, empty);
            LineaFactura l = lineaDeCelda(this);
            setGraphic(empty || l == null ? null : combo);
            if (l != null) {
                combo.setValue(tipoIvaDe(l));
            }
        }
    }
}
