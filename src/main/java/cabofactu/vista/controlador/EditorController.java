package cabofactu.vista.controlador;

import cabofactu.modelo.negocio.sqlite.Conexion;
import cabofactu.modelo.dominio.Cliente;
import cabofactu.modelo.dominio.Empresa;
import cabofactu.modelo.dominio.EstadoFactura;
import cabofactu.modelo.dominio.LineaFactura;
import cabofactu.modelo.dominio.ResumenFactura;
import cabofactu.modelo.dominio.Serie;
import cabofactu.modelo.dominio.TipoIva;
import cabofactu.modelo.dominio.TipoRetencion;
import cabofactu.pdf.ExportadorPdf;
import cabofactu.modelo.negocio.Calculos;
import cabofactu.modelo.negocio.ValidacionCliente;
import cabofactu.utilidades.Formatos;
import cabofactu.utilidades.LogoMarco;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
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
import javafx.scene.control.TextInputControl;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;

import java.io.File;
import java.math.BigDecimal;
import java.net.URL;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import cabofactu.modelo.dominio.Factura;
import cabofactu.vista.Pantalla;
import cabofactu.vista.Vista;
import cabofactu.vista.utilidades.CambiosSinGuardar;
import cabofactu.vista.utilidades.Dialogos;

/**
 * Editor de factura: cabecera con serie, fecha, número y cliente; tabla de
 * líneas editable; descuento general y desglose por tipos de IVA;
 * observaciones; validaciones al guardar; estados Emitida/Anulada; creación
 * de rectificativas y exportación a PDF.
 */
public class EditorController implements Pantalla, Initializable {

    /** Trazas temporales para diagnosticar el flujo de foco al editar líneas. */
    private static final boolean DIAGNOSTICO_FOCO = false;
    private static final String PREV_SERIE = "ultima_serie";
    private static final String PREV_CARPETA = "carpeta_facturas";
    private static final String PREV_EXPORT = "ultima_carpeta_export";

    private boolean modificado;
    private boolean cargando;
    private Long facturaAbiertaId;
    private Integer correlativoFijo;
    private EstadoFactura estadoActual;
    private Cliente clienteActual;
    private int descuento;

    private final ObservableList<LineaFactura> lineas = FXCollections.observableArrayList();
    private final ObservableList<TipoIva> tiposIva = FXCollections.observableArrayList();
    private final ObservableList<TipoRetencion> tiposRetencion = FXCollections.observableArrayList();
    private TipoRetencion retencionActual;

    @FXML
    private Label lblTitulo;
    @FXML
    private Label lblEstado;
    @FXML
    private HBox filaBaseBruta;
    @FXML
    private Label lblBaseBruta;
    @FXML
    private HBox filaDescuento;
    @FXML
    private Label lblDescuentoNombre;
    @FXML
    private Label lblDescuentoImporte;
    @FXML
    private HBox filaBaseImponible;
    @FXML
    private Label lblBaseTotal;
    @FXML
    private Label lblIvaTotal;
    @FXML
    private Label lblRetencionNombre;
    @FXML
    private Label lblRetencionImporte;
    @FXML
    private HBox filaRetencion;
    @FXML
    private Label lblTotal;
    @FXML
    private TableView<ResumenFactura.IvaGrupo> matrizIva;
    @FXML
    private TableColumn<ResumenFactura.IvaGrupo, String> colMatrizIva;
    @FXML
    private TableColumn<ResumenFactura.IvaGrupo, String> colMatrizBase;
    @FXML
    private TableColumn<ResumenFactura.IvaGrupo, String> colMatrizCuota;
    @FXML
    private HBox filaSuplidos;
    @FXML
    private Label lblSuplidos;
    @FXML
    private ImageView logo;
    @FXML
    private StackPane logoBox;
    // Cómo funciona: JavaFX inyecta aquí el controlador del <fx:include fx:id="barra">;
    // el nombre del campo es el fx:id más "Controller".
    @FXML
    private BarraNavegacionController barraController;
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
    @FXML
    private Button btnRectificativa;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        barraController.marcarActivo("editor");
        cargando = true;
        try {
            cargarLogo();
            cargarSeries();
            cargarFechaInicial();
            cargarTiposIva();
            cargarTiposRetencion();
            configurarBusquedaCliente();
            configurarDetalleCliente();
            configurarTabla();
            configurarMatriz();
            configurarCambios();
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

    @Override
    public void alMostrar() {
        atajos();
    }

    private void cargarLogo() {
        try {
            Empresa empresa = Vista.getInstancia().getControlador().buscarEmpresa();
            String ruta = empresa.getLogoPath();
            if (ruta == null || ruta.isBlank()) {
                LogoMarco.limpiar(logoBox);
                return;
            }
            File f = new File(ruta);
            if (!f.exists()) {
                LogoMarco.limpiar(logoBox);
                return;
            }
            Image img = new Image(f.toURI().toString());
            if (img.isError()) {
                LogoMarco.limpiar(logoBox);
                return;
            }
            logo.setImage(img);
            logo.setFitWidth(92);
            logo.setFitHeight(38);
            logo.setPreserveRatio(true);
            LogoMarco.aplicar(logoBox, img);
        } catch (Exception ignored) {
        }
    }

    /** Cargamos la factura indicada en el editor, para verla, editarla o exportarla. */
    public void cargarFactura(long facturaId) {
        try {
            Factura factura = Vista.getInstancia().getControlador().buscarFactura(facturaId);
            if (factura == null) {
                Dialogos.mostrarDialogoError("Factura", "No se pudo abrir la factura.");
                return;
            }
            cargando = true;
            try {
                facturaAbiertaId = factura.getId();
                correlativoFijo = factura.getCorrelativo();
                estadoActual = factura.getEstado();

                Serie serie = factura.getSerie();
                comboSerie.setValue(serie);
                comboSerie.setDisable(true);
                actualizarVisibilidadReferencia(serie);
                fecha.setValue(factura.getFecha());
                txtNumero.setText(factura.getNumero());

                Cliente cli = factura.getCliente();
                comboCliente.setValue(cli);
                cargarDatosCliente(cli);

                lineas.setAll(factura.getLineas());
                asegurarTiposIvaEnLista(lineas);
                descuento = factura.getDescuento();
                txtDescuento.setText(String.valueOf(descuento));
                if (factura.getRetencion() == null) {
                    asegurarRetencionEnLista(null, null, null);
                    seleccionarRetencionPorId(null);
                } else {
                    asegurarRetencionEnLista(factura.getRetencion().getId(), factura.getRetencion().getNombre(),
                            factura.getRetencion().getPorcentaje());
                    seleccionarRetencionPorId(factura.getRetencion().getId());
                }
                txtObservaciones.setText(nz(factura.getObservaciones()));
                txtReferencia.setText(nz(factura.getRectificaNumero()));
                txtReferencia.setEditable(false);
                txtFormaPago.setText(nz(factura.getFormaPago()));
                vencimiento.setValue(factura.getVencimiento());
                txtRealizadaPor.setText(nz(factura.getRealizadaPor()));

                lblTitulo.setText("Factura " + factura.getNumero());
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
            Dialogos.mostrarDialogoError("Factura", "Error al abrir la factura: " + e.getMessage());
        }
    }

    @Override
    public boolean puedeCerrar() {
        if (!modificado) {
            return true;
        }
        CambiosSinGuardar r = Dialogos.mostrarDialogoCambiosSinGuardar();
        if (r == CambiosSinGuardar.GUARDAR) {
            return guardar();
        }
        return r == CambiosSinGuardar.DESCARTAR;
    }

    // ------------------------------------------------------------------
    // Carga inicial
    // ------------------------------------------------------------------

    private void cargarSeries() {
        try {
            List<Serie> series = Vista.getInstancia().getControlador().listadoSeries();
            comboSerie.getItems().setAll(series);
            Serie inicial = null;
            String ultima = Vista.getInstancia().getControlador().preferencia(PREV_SERIE);
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
            Dialogos.mostrarDialogoError("Factura", "No se pudieron cargar las series: " + e.getMessage());
        }
    }

    private void cargarFechaInicial() {
        LocalDate fechaInicial = Vista.getInstancia().getControlador().getModelo().getReloj().fechaTrabajo();
        fecha.setValue(fechaInicial);
    }

    private void cargarTiposIva() {
        try {
            tiposIva.setAll(Vista.getInstancia().getControlador().listadoTiposIva(true));
        } catch (Exception e) {
            tiposIva.clear();
        }
    }

    private void asegurarTiposIvaEnLista(ObservableList<LineaFactura> lineasCargadas) {
        for (LineaFactura l : lineasCargadas) {
            if (l.getTipoIvaId() == null) {
                continue;
            }
            boolean yaEsta = false;
            for (TipoIva t : tiposIva) {
                if (t.getId() != null && t.getId().equals(l.getTipoIvaId())) {
                    yaEsta = true;
                    break;
                }
            }
            if (yaEsta) {
                continue;
            }
            TipoIva existente = null;
            try {
                existente = Vista.getInstancia().getControlador().buscarTipoIva(l.getTipoIvaId());
            } catch (Exception ignored) {
            }
            if (existente != null) {
                tiposIva.add(existente);
            } else {
                try {
                    String nombreIva = l.getIvaNombre();
                    if (nombreIva == null || nombreIva.isBlank()) {
                        nombreIva = "Tipo de IVA";
                    }
                    TipoIva snapshot = new TipoIva(nombreIva, l.getIvaPorcentaje(), l.isEsSuplido());
                    snapshot.setId(l.getTipoIvaId());
                    snapshot.setMotivoExencion(l.getIvaMotivoExencion());
                    snapshot.setActivo(false);
                    tiposIva.add(snapshot);
                } catch (Exception ignored) {
                }
            }
        }
    }

    private void cargarTiposRetencion() {
        try {
            List<TipoRetencion> activas = Vista.getInstancia().getControlador().listadoTiposRetencion(true);
            TipoRetencion sin = new TipoRetencion("Sin retención", 0);
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
            try {
                TipoRetencion sin = new TipoRetencion("Sin retención", 0);
                tiposRetencion.add(sin);
                comboRetencion.setItems(tiposRetencion);
                comboRetencion.setValue(sin);
            } catch (Exception ignorada) {
            }
        }
    }

    private void configurarBusquedaCliente() {
        comboCliente.setEditable(true);
        comboCliente.setConverter(new StringConverter<>() {
            @Override
            public String toString(Cliente c) {
                return c == null ? "" : c.getNombreNif();
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
                comboCliente.getItems().setAll(Vista.getInstancia().getControlador().listadoClientes(texto, true));
            } catch (Exception e) {
                Dialogos.mostrarDialogoError("Cliente", "Error al buscar clientes: " + e.getMessage());
            }
        });
        comboCliente.setOnShowing(e -> {
            if (cargando) {
                return;
            }
            try {
                String texto = comboCliente.getEditor().getText();
                if (texto == null || texto.isBlank()) {
                    comboCliente.getItems().setAll(Vista.getInstancia().getControlador().listadoClientes(true));
                } else {
                    comboCliente.getItems().setAll(Vista.getInstancia().getControlador().listadoClientes(texto, true));
                }
            } catch (Exception ex) {
                Dialogos.mostrarDialogoError("Cliente", "Error al cargar clientes: " + ex.getMessage());
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
    }

    private void marcarCampo(TextField campo, String error) {
        if (error != null) {
            campo.setStyle("-fx-border-color: #d32f2f; -fx-border-width: 2;");
        } else {
            campo.setStyle("");
        }
    }

    private void marcarCamposCliente() {
        marcarCampo(cliNombre, ValidacionCliente.errorNombre(cliNombre.getText()));
        marcarCampo(cliNif, ValidacionCliente.errorNif(cliNif.getText()));
        marcarCampo(cliDireccion, ValidacionCliente.errorDireccion(cliDireccion.getText()));
        marcarCampo(cliCp, ValidacionCliente.errorCodigoPostal(cliCp.getText()));
        marcarCampo(cliLocalidad, ValidacionCliente.errorLocalidad(cliLocalidad.getText()));
        marcarCampo(cliProvincia, ValidacionCliente.errorProvincia(cliProvincia.getText()));
        marcarCampo(cliEmail, ValidacionCliente.errorEmail(cliEmail.getText()));
    }

    private boolean avisarPrimerErrorCliente() {
        String error = ValidacionCliente.errorNombre(cliNombre.getText());
        if (error != null) {
            Dialogos.mostrarDialogoError("Datos del cliente", error);
            return true;
        }
        error = ValidacionCliente.errorNif(cliNif.getText());
        if (error != null) {
            Dialogos.mostrarDialogoError("NIF no válido", error);
            return true;
        }
        error = ValidacionCliente.errorDireccion(cliDireccion.getText());
        if (error != null) {
            Dialogos.mostrarDialogoError("Datos del cliente", error);
            return true;
        }
        error = ValidacionCliente.errorCodigoPostal(cliCp.getText());
        if (error != null) {
            Dialogos.mostrarDialogoError("Código postal no válido", error);
            return true;
        }
        error = ValidacionCliente.errorLocalidad(cliLocalidad.getText());
        if (error != null) {
            Dialogos.mostrarDialogoError("Datos del cliente", error);
            return true;
        }
        error = ValidacionCliente.errorProvincia(cliProvincia.getText());
        if (error != null) {
            Dialogos.mostrarDialogoError("Datos del cliente", error);
            return true;
        }
        error = ValidacionCliente.errorEmail(cliEmail.getText());
        if (error != null) {
            Dialogos.mostrarDialogoError("Correo electrónico no válido", error);
            return true;
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
            int parrafos = Math.max(1, txtObservaciones.getParagraphs().size());
            txtObservaciones.setPrefRowCount(Math.min(3, parrafos));
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

        if (DIAGNOSTICO_FOCO && Vista.getInstancia().getVentana() != null
                && Vista.getInstancia().getVentana().getScene() != null) {
            Vista.getInstancia().getVentana().getScene().focusOwnerProperty().addListener((o, anterior, actual) ->
                    trazarFoco("Scene.focusOwner", actual));
        }
    }

    private void configurarMatriz() {
        matrizIva.setPlaceholder(new Label("Sin desglose."));
        matrizIva.setFocusTraversable(false);
        matrizIva.setMouseTransparent(true);
        matrizIva.setFixedCellSize(22);
        colMatrizIva.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(etiquetaMatriz(c.getValue())));
        colMatrizBase.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(Formatos.moneda(c.getValue().getBase())));
        colMatrizCuota.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(Formatos.moneda(c.getValue().getCuota())));
    }

    private static String etiquetaMatriz(ResumenFactura.IvaGrupo g) {
        if ("Totales".equals(g.getNombre())) {
            return "Totales";
        }
        if (g.isExento()) {
            String motivo = g.getMotivoExencion();
            return "Exento" + (motivo != null && !motivo.isBlank() ? " (" + motivo + ")" : "");
        }
        String nombre = g.getNombre() != null ? g.getNombre().trim() : "";
        if (nombre.endsWith("%")) {
            return g.getNombre();
        }
        return (nombre.isEmpty() ? "" : nombre + " ") + g.getPorcentaje() + "%";
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
            l.setEsSuplido(t.isEsSuplido());
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
        return null;
    }

    private LineaFactura lineaDeCelda(TableCell<LineaFactura, ?> celda) {
        TableRow<LineaFactura> fila = celda.getTableRow();
        return fila == null ? null : fila.getItem();
    }

    // Recálculo por tipo de cambio (6.4)
    private void aplicarCantidad(LineaFactura l, int v) {
        l.setCantidad(v);
        marcarModificado();
    }

    private void aplicarPrecio(LineaFactura l, BigDecimal v) {
        l.setPrecioUnitario(v);
        marcarModificado();
    }

    private void aplicarTotal(LineaFactura l, BigDecimal t) {
        if (chkTotalConIva.isSelected()) {
            BigDecimal base = Calculos.baseDesdeTotalConIva(t, l.getIvaPorcentaje());
            l.setPrecioUnitario(Calculos.precioDesdeTotal(base, l.getCantidad()));
        } else {
            l.setPrecioUnitario(Calculos.precioDesdeTotal(t, l.getCantidad()));
        }
        marcarModificado();
    }

    private void aplicarIva(LineaFactura l, TipoIva t) {
        l.setTipoIvaId(t.getId());
        l.setIvaNombre(t.getNombre());
        l.setIvaPorcentaje(t.getPorcentaje());
        l.setIvaMotivoExencion(t.getMotivoExencion());
        l.setEsSuplido(t.isEsSuplido());
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
            Node editor = tablaLineas.lookup(".text-field");
            if (editor == null) {
                editor = tablaLineas.lookup(".text-area");
            }
            if (editor instanceof TextInputControl tic) {
                tic.requestFocus();
                trazarFoco("editarCeldaSegura: editor enfocado", tic);
            } else {
                trazarFoco("editarCeldaSegura: editor no encontrado", null);
                tablaLineas.requestFocus();
            }
            Platform.runLater(() -> trazarFoco("editarCeldaSegura: comprobación posterior", 
                    Vista.getInstancia().getVentana().getScene().getFocusOwner()));
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
        ResumenFactura r = Calculos.resumen(lineas, descuento, retencionActual);
        boolean conDescuento = r.getImporteDescuento() != null && r.getImporteDescuento().compareTo(BigDecimal.ZERO) > 0;
        filaBaseBruta.setVisible(conDescuento);
        filaBaseBruta.setManaged(conDescuento);
        filaDescuento.setVisible(conDescuento);
        filaDescuento.setManaged(conDescuento);
        togglePrimera(filaBaseBruta, conDescuento);
        togglePrimera(filaBaseImponible, !conDescuento);
        if (conDescuento) {
            lblBaseBruta.setText(Formatos.moneda(r.getBaseBruta()));
            lblDescuentoNombre.setText("Descuento " + descuento + "%");
            lblDescuentoImporte.setText("-" + Formatos.moneda(r.getImporteDescuento()));
        }
        lblBaseTotal.setText(Formatos.moneda(r.getBaseTotal()));
        lblIvaTotal.setText(Formatos.moneda(r.getIvaTotal()));
        boolean conSuplidos = r.getTotalSuplidos() != null && r.getTotalSuplidos().compareTo(BigDecimal.ZERO) > 0;
        filaSuplidos.setVisible(conSuplidos);
        filaSuplidos.setManaged(conSuplidos);
        if (conSuplidos) {
            lblSuplidos.setText(Formatos.moneda(r.getTotalSuplidos()));
        }
        boolean conRetencion = r.getImporteRetencion() != null && r.getImporteRetencion().compareTo(BigDecimal.ZERO) > 0;
        filaRetencion.setVisible(conRetencion);
        filaRetencion.setManaged(conRetencion);
        if (conRetencion) {
            String nombre = r.getNombreRetencion() != null && !r.getNombreRetencion().isBlank()
                    ? r.getNombreRetencion()
                    : "Retención " + r.getPorcentajeRetencion() + "%";
            lblRetencionNombre.setText(nombre);
            lblRetencionImporte.setText("-" + Formatos.moneda(r.getImporteRetencion()));
        }
        lblTotal.setText(Formatos.moneda(r.getTotal()));
        ObservableList<ResumenFactura.IvaGrupo> filasMatriz = FXCollections.observableArrayList(r.getGrupos());
        ResumenFactura.IvaGrupo totales = new ResumenFactura.IvaGrupo();
        totales.setNombre("Totales");
        totales.setBase(r.getBaseTotal());
        totales.setCuota(r.getIvaTotal());
        filasMatriz.add(totales);
        matrizIva.setItems(filasMatriz);
        matrizIva.setPrefHeight(26 + 22 * filasMatriz.size() + 2);
    }

    private static void togglePrimera(HBox fila, boolean primera) {
        if (primera) {
            if (!fila.getStyleClass().contains("total-fila-primera")) {
                fila.getStyleClass().add("total-fila-primera");
            }
        } else {
            fila.getStyleClass().remove("total-fila-primera");
        }
    }

    // ------------------------------------------------------------------
    // Guardado
    // ------------------------------------------------------------------

    @FXML
    private boolean guardar() {
        marcarCamposCliente();
        Cliente cli;
        try {
            cli = clienteDeFormulario();
        } catch (Exception e) {
            Dialogos.mostrarDialogoError("Datos del cliente", e.getMessage());
            return false;
        }
        if (cli == null) {
            Dialogos.mostrarDialogoError("Datos del cliente", "Indique los datos del cliente.");
            return false;
        }
        if (avisarPrimerErrorCliente()) {
            return false;
        }
        if (facturaAbiertaId != null && estadoActual != EstadoFactura.EMITIDA) {
            Dialogos.mostrarDialogoInformacion("Guardar", "Una factura anulada no se puede editar.");
            return false;
        }
        LocalDate f = fecha.getValue();
        if (f == null) {
            Dialogos.mostrarDialogoError("Guardar", "Indique la fecha de la factura.");
            return false;
        }
        List<LineaFactura> lis = lineasGuardables();
        if (lis.isEmpty()) {
            Dialogos.mostrarDialogoError("Guardar", "La factura debe tener al menos una línea con contenido.");
            return false;
        }
        String obs = txtObservaciones.getText();
        String formaPago = txtFormaPago.getText().trim();
        String realizadaPor = txtRealizadaPor.getText().trim();
        boolean actualizarFicha = pedirActualizarFicha(cli);
        try {
            if (facturaAbiertaId == null) {
                Serie serie = comboSerie.getValue();
                if (serie == null) {
                    Dialogos.mostrarDialogoError("Guardar", "Seleccione la serie.");
                    return false;
                }
                Integer hueco = pedirHueco(serie, f);
                if (hueco != null) {
                    txtNumero.setText(Vista.getInstancia().getControlador().formarNumero(serie, hueco, f));
                }
                Integer corr = Vista.getInstancia().getControlador().parseCorrelativo(serie, txtNumero.getText());
                if (corr == null) {
                    Dialogos.mostrarDialogoError("Guardar", "El número no se ajusta al formato de la serie "
                            + serie.getCodigo() + " (p. ej. " + serie.getCodigo() + "-1).");
                    return false;
                }
                Factura nueva = new Factura(serie, f, cli);
                nueva.setCorrelativo(corr);
                nueva.setDescuento(descuento);
                nueva.setObservaciones(obs);
                nueva.setFormaPago(formaPago);
                nueva.setVencimiento(vencimiento.getValue());
                nueva.setRealizadaPor(realizadaPor);
                nueva.setRetencion(retencionActual);
                nueva.setLineas(lis);
                long id = Vista.getInstancia().getControlador().altaFactura(nueva);
                guardarSeriePreferida(serie);
                cargarFactura(id);
                Dialogos.mostrarDialogoInformacion("Guardar", "Factura guardada.");
            } else {
                Factura factura = Vista.getInstancia().getControlador().buscarFactura(facturaAbiertaId);
                if (factura == null) {
                    Dialogos.mostrarDialogoError("Guardar", "No se ha encontrado la factura.");
                    return false;
                }
                factura.setFecha(f);
                factura.setCliente(cli);
                factura.setDescuento(descuento);
                factura.setObservaciones(obs);
                factura.setFormaPago(formaPago);
                factura.setVencimiento(vencimiento.getValue());
                factura.setRealizadaPor(realizadaPor);
                factura.setRetencion(retencionActual);
                factura.setLineas(lis);
                if (!Dialogos.mostrarDialogoConfirmacion("Guardar factura", String.format("¿Guardar los cambios de la factura %s?%n%nLa factura ya emitida se sobrescribirá.", factura.getNumero()))) {
                    return false;
                }
                Vista.getInstancia().getControlador().modificarFactura(factura);
                cargarFactura(facturaAbiertaId);
                Dialogos.mostrarDialogoInformacion("Guardar", "Factura guardada.");
            }
            if (actualizarFicha) {
                Vista.getInstancia().getControlador().modificarCliente(cli);
                clienteActual = cli;
            }
            return true;
        } catch (Exception e) {
            Dialogos.mostrarDialogoError("Guardar", "Error al guardar: " + e.getMessage());
            return false;
        }
    }

    /**
     * Si el cliente ya existe y sus datos no son los de su ficha, preguntamos si
     * queremos guardarlos también allí. La factura se guarda con ellos en cualquier caso.
     */
    private boolean pedirActualizarFicha(Cliente cli) {
        if (cli.getId() == null || clienteActual == null) {
            return false;
        }
        if (cli.tieneLosMismosDatos(clienteActual)) {
            return false;
        }
        return Dialogos.mostrarDialogoConfirmacion("Datos del cliente",
                "Has cambiado los datos de «" + clienteActual.getNombre() + "» en esta factura.\n\n"
                        + "¿Quieres guardar también esos cambios en su ficha de cliente?");
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

    private Cliente clienteDeFormulario() throws Exception {
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
        Cliente c;
        if (clienteActual != null) {
            c = new Cliente(clienteActual);
            c.setNombre(nombre);
            c.setNif(nif);
            c.setDireccion(dir);
            c.setCp(cp);
            c.setLocalidad(loc);
            c.setProvincia(prov);
        } else {
            c = new Cliente(nombre, nif, dir, cp, loc, prov);
            c.setActivo(true);
        }
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
        if (anulada) {
            lblTitulo.setMaxWidth(130);
        } else {
            lblTitulo.setMaxWidth(200);
        }
        txtNumero.setDisable(abierta);
        btnExportar.setDisable(!abierta);
        btnRectificativa.setDisable(!abierta);
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
        if (modificado && !Dialogos.mostrarDialogoConfirmacion("Cambios sin guardar",
                "Hay cambios sin guardar que se descartarán. ¿Continuar?")) {
            return;
        }
        if (!Dialogos.mostrarDialogoConfirmacion("Anular factura",
                "¿Anular la factura?")) {
            return;
        }
        try {
            Vista.getInstancia().getControlador().anularFactura(facturaAbiertaId);
            Dialogos.mostrarDialogoInformacion("Anular", "Factura anulada.");
            cargarFactura(facturaAbiertaId);
        } catch (Exception e) {
            Dialogos.mostrarDialogoError("Anular", "Error al anular: " + e.getMessage());
        }
    }

    @FXML
    private void restaurar() {
        if (facturaAbiertaId == null) {
            return;
        }
        if (!Dialogos.mostrarDialogoConfirmacion("Restaurar factura",
                "¿Restaurar la factura a estado Emitida?")) {
            return;
        }
        try {
            Vista.getInstancia().getControlador().restaurarFactura(facturaAbiertaId);
            Dialogos.mostrarDialogoInformacion("Restaurar", "Factura restaurada.");
            cargarFactura(facturaAbiertaId);
        } catch (Exception e) {
            Dialogos.mostrarDialogoError("Restaurar", "Error al restaurar: " + e.getMessage());
        }
    }

    @FXML
    private void crearRectificativa() {
        if (facturaAbiertaId == null) {
            Dialogos.mostrarDialogoInformacion("Rectificativa", "Abra primero la factura a rectificar.");
            return;
        }
        if (modificado && !Dialogos.mostrarDialogoConfirmacion("Cambios sin guardar",
                "Hay cambios sin guardar que se descartarán. ¿Continuar?")) {
            return;
        }
        try {
            long nueva = Vista.getInstancia().getControlador().rectificarFactura(facturaAbiertaId,
                    Vista.getInstancia().getControlador().getModelo().getReloj().fechaTrabajo());
            cargarFactura(nueva);
            Dialogos.mostrarDialogoInformacion("Rectificativa", "Rectificativa creada.");
        } catch (Exception e) {
            Dialogos.mostrarDialogoError("Rectificativa", "Error al crear la rectificativa: " + e.getMessage());
        }
    }

    @FXML
    private void exportarPdf() {
        if (facturaAbiertaId == null) {
            Dialogos.mostrarDialogoInformacion("Exportar PDF", "Guarde primero la factura para poder exportarla.");
            return;
        }
        try {
            Factura factura = Vista.getInstancia().getControlador().buscarFactura(facturaAbiertaId);
            if (factura == null) {
                return;
            }
            Empresa empresa = Vista.getInstancia().getControlador().buscarEmpresa();
            Path sugerido = proponerDestinoPdf(factura);
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Exportar PDF");
            if (sugerido.getParent() != null && sugerido.getParent().toFile().exists()) {
                chooser.setInitialDirectory(sugerido.getParent().toFile());
            }
            chooser.setInitialFileName(sugerido.getFileName().toString());
            File f = chooser.showSaveDialog(Vista.getInstancia().getVentana());
            if (f == null) {
                return;
            }
            Path ruta = f.toPath();
            final String colorPdf = colorPdfPreferido();
            btnExportar.setDisable(true);
            Task<Path> t = new Task<>() {
                @Override
                protected Path call() throws Exception {
                    new ExportadorPdf().exportar(factura, empresa, ruta, colorPdf);
                    return ruta;
                }
            };
            t.setOnSucceeded(e -> {
                btnExportar.setDisable(false);
                try {
                    if (ruta.getParent() != null) {
                        Vista.getInstancia().getControlador().guardarPreferencia(PREV_EXPORT, ruta.getParent().toString());
                    }
                } catch (Exception ignored) {
                }
                Dialogos.mostrarDialogoInformacion("Exportar PDF", "PDF generado en:\n" + ruta);
            });
            t.setOnFailed(e -> {
                btnExportar.setDisable(false);
                Dialogos.mostrarDialogoError("Exportar PDF", "No se pudo generar el PDF: "
                        + (t.getException() == null ? "error desconocido" : t.getException().getMessage()));
            });
            new Thread(t).start();
        } catch (Exception e) {
            Dialogos.mostrarDialogoError("Exportar PDF", "Error: " + e.getMessage());
        }
    }

    private String colorPdfPreferido() {
        try {
            return Vista.getInstancia().getControlador().preferencia(ExportadorPdf.PREF_COLOR);
        } catch (Exception e) {
            return null;
        }
    }

    private Path proponerDestinoPdf(Factura factura) {
        String carpeta = "Facturas";
        try {
            String pref = Vista.getInstancia().getControlador().preferencia(PREV_CARPETA);
            if (pref != null && !pref.isBlank()) {
                carpeta = pref;
            }
        } catch (Exception ignored) {
        }
        Path base = Path.of(carpeta);
        if (!base.isAbsolute()) {
            base = Conexion.carpetaEmpresa().resolve(base);
        }
        String nombre = Formatos.nombreArchivoPdf(factura.getNumero());
        Serie serie = factura.getSerie();
        if (serie == null) {
            return base.resolve(nombre);
        }
        return base.resolve(String.valueOf(factura.getFecha().getYear()))
                .resolve(serie.getCodigo())
                .resolve(nombre);
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
        try {
            String nombreRetencion = nombre;
            if (nombreRetencion == null || nombreRetencion.isBlank()) {
                nombreRetencion = "Retención";
            }
            int pct = 0;
            if (porcentaje != null) {
                pct = porcentaje;
            }
            TipoRetencion snapshot = new TipoRetencion(nombreRetencion, pct);
            snapshot.setId(id);
            snapshot.setActivo(false);
            tiposRetencion.add(snapshot);
        } catch (Exception ignored) {
        }
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
        marcarCampo(cliNombre, null);
        marcarCampo(cliNif, null);
        marcarCampo(cliDireccion, null);
        marcarCampo(cliCp, null);
        marcarCampo(cliLocalidad, null);
        marcarCampo(cliProvincia, null);
        marcarCampo(cliEmail, null);
    }

    private void recalcularNumero() {
        Serie s = comboSerie.getValue();
        LocalDate f = fecha.getValue();
        if (s == null) {
            return;
        }
        if (facturaAbiertaId != null && correlativoFijo != null && f != null) {
            txtNumero.setText(Vista.getInstancia().getControlador().formarNumero(s, correlativoFijo, f));
        } else if (f != null) {
            try {
                int correlativo = Vista.getInstancia().getControlador().siguienteCorrelativo(s, f);
                txtNumero.setText(Vista.getInstancia().getControlador().formarNumero(s, correlativo, f));
            } catch (Exception e) {
                txtNumero.setText("");
            }
        }
    }

    private Integer pedirHueco(Serie serie, LocalDate fecha) {
        try {
            List<Integer> huecos = Vista.getInstancia().getControlador().huecosDeSerie(serie, fecha);
            if (huecos.isEmpty()) {
                return null;
            }
            int siguiente = Vista.getInstancia().getControlador().siguienteCorrelativo(serie, fecha);
            List<Integer> menores = new ArrayList<>();
            for (int hueco : huecos) {
                if (hueco < siguiente) {
                    menores.add(hueco);
                }
            }
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
                Vista.getInstancia().getControlador().guardarPreferencia(PREV_SERIE, s.getCodigo());
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
        Vista.getInstancia().getVentana().getScene().getAccelerators().put(
                new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN), () -> guardar());
        Vista.getInstancia().getVentana().getScene().getAccelerators().put(
                new KeyCodeCombination(KeyCode.P, KeyCombination.CONTROL_DOWN), () -> exportarPdf());
        Vista.getInstancia().getVentana().getScene().getAccelerators().put(
                new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN), () -> nuevaFactura());
        Vista.getInstancia().getVentana().getScene().getAccelerators().put(
                new KeyCodeCombination(KeyCode.ESCAPE), () -> volver());
    }

    @FXML
    private void nuevaFactura() {
        Vista.getInstancia().mostrar("Editor.fxml");
    }

    @FXML
    private void volver() {
        Vista.getInstancia().mostrar("MenuPrincipal.fxml");
    }

    // ------------------------------------------------------------------
    // Celdas editables
    // ------------------------------------------------------------------

    private abstract class CeldaEditable extends TableCell<LineaFactura, String> {
        protected final TextInputControl editor = crearEditor();
        private boolean committing;

        protected TextInputControl crearEditor() {
            return new TextField();
        }

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
        private final Label etiqueta = new Label();

        CeldaDescripcion() {
            etiqueta.setWrapText(true);
            etiqueta.prefWidthProperty().bind(colDescripcion.widthProperty().subtract(8));
            colDescripcion.widthProperty().addListener((o, anterior, ancho) -> {
                ajustarAltoEtiqueta();
                tablaLineas.requestLayout();
            });
            etiqueta.layoutBoundsProperty().addListener((o, anterior, bounds) -> ajustarAltoEtiqueta());
        }

        private void ajustarAltoEtiqueta() {
            double w = etiqueta.getPrefWidth();
            String t = etiqueta.getText();
            if (t == null || t.isEmpty() || Double.isNaN(w) || w <= 0) {
                if (etiqueta.getMinHeight() != Region.USE_PREF_SIZE) {
                    etiqueta.setMinHeight(Region.USE_PREF_SIZE);
                    etiqueta.setMaxHeight(Region.USE_PREF_SIZE);
                }
                return;
            }
            double necesidad = etiqueta.prefHeight(w);
            if (necesidad > 0 && etiqueta.getMinHeight() != necesidad) {
                etiqueta.setMinHeight(necesidad);
                etiqueta.setMaxHeight(necesidad);
            }
        }

        @Override
        protected TextInputControl crearEditor() {
            TextArea area = new TextArea();
            area.setWrapText(true);
            area.setPrefRowCount(1);
            area.setMinHeight(Region.USE_PREF_SIZE);
            area.skinProperty().addListener((o, anterior, skin) -> atarAltoEditor(area));
            area.layoutBoundsProperty().addListener((o, anterior, bounds) -> atarAltoEditor(area));
            area.textProperty().addListener((o, anterior, texto) -> {
                TableRow<?> fila = getTableRow();
                if (fila != null) {
                    fila.requestLayout();
                }
            });
            return area;
        }

        private void atarAltoEditor(TextArea area) {
            if (area.prefHeightProperty().isBound()) {
                return;
            }
            Node texto = area.lookup(".text");
            Node contenido = area.lookup(".content");
            Node scroll = area.lookup(".scroll-pane");
            if (texto == null || !(contenido instanceof Region) || !(scroll instanceof Region)) {
                return;
            }
            // +1 px contra el redondeo de fracciones de píxel: sin él, la igualdad
            // exacta entre contenido y ventana hace parpadear la barra vertical.
            double extra = vertical((Region) contenido) + vertical((Region) scroll)
                    + vertical(area) + 1;
            area.prefHeightProperty().bind(Bindings.createDoubleBinding(
                    () -> texto.getBoundsInParent().getHeight() + extra,
                    texto.boundsInParentProperty()));
        }

        private static double vertical(Region nodo) {
            double total = nodo.getPadding().getTop() + nodo.getPadding().getBottom();
            if (nodo.getBorder() != null) {
                total += nodo.getBorder().getInsets().getTop() + nodo.getBorder().getInsets().getBottom();
            }
            return total;
        }

        @Override
        protected void updateItem(String item, boolean empty) {
            if (isEditing()) {
                return;
            }
            super.updateItem(item, empty);
            if (empty) {
                setText(null);
                setGraphic(null);
            } else {
                LineaFactura l = lineaDeCelda(this);
                etiqueta.setText(l == null ? "" : mostrar(l));
                ajustarAltoEtiqueta();
                setText(null);
                setGraphic(etiqueta);
            }
        }

        @Override
        protected double computePrefHeight(double width) {
            if (isEditing() && getGraphic() == editor) {
                double altoEditor = editor.getPrefHeight();
                if (altoEditor > 0) {
                    return altoEditor + getPadding().getTop() + getPadding().getBottom();
                }
            }
            double w = etiqueta.getPrefWidth();
            if (Double.isNaN(w) || w <= 0) {
                return super.computePrefHeight(width);
            }
            double r = etiqueta.prefHeight(w) + getPadding().getTop() + getPadding().getBottom();
            return r;
        }

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
        private final javafx.event.EventHandler<javafx.event.ActionEvent> handler = e -> {
            LineaFactura l = lineaDeCelda(this);
            TipoIva t = combo.getValue();
            if (l != null && t != null && !Objects.equals(t.getId(), l.getTipoIvaId())) {
                aplicarIva(l, t);
                refrescarLineas();
                actualizarResumen();
            }
        };

        CeldaIva() {
            combo.setItems(tiposIva);
            combo.setMaxWidth(Double.MAX_VALUE);
            combo.setOnAction(handler);
        }

        @Override
        protected void updateItem(TipoIva item, boolean empty) {
            super.updateItem(item, empty);
            LineaFactura l = lineaDeCelda(this);
            setGraphic(empty || l == null ? null : combo);
            if (l != null) {
                combo.setOnAction(null);
                combo.setValue(tipoIvaDe(l));
                combo.setOnAction(handler);
            }
        }
    }
}
