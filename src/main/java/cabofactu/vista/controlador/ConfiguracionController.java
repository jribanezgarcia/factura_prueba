package cabofactu.vista.controlador;

import cabofactu.modelo.dominio.Empresa;
import cabofactu.modelo.dominio.EmpresaDisponible;
import cabofactu.modelo.dominio.Serie;
import cabofactu.modelo.dominio.TipoIva;
import cabofactu.modelo.dominio.TipoRetencion;
import cabofactu.pdf.ExportadorPdf;
import cabofactu.pdf.DisposicionCabecera;
import cabofactu.modelo.negocio.Sesion;
import cabofactu.utilidades.Formatos;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.control.RadioButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import cabofactu.vista.Pantalla;
import cabofactu.vista.Vista;
import cabofactu.vista.utilidades.Dialogos;
import cabofactu.vista.utilidades.PreviaCabecera;
import cabofactu.vista.utilidades.GestorTemas;

/**
 * Configuracion por secciones con lista lateral: Empresa, Cabecera y pie, PDF
 * y apariencia se guardan con un boton global; IVA, Retenciones y Series
 * se administran fila a fila con sus propias acciones.
 */
public class ConfiguracionController implements Pantalla, Initializable {

    private static final String PREV_CARPETA = "carpeta_facturas";
    private static final String PREV_EXPORT = "ultima_carpeta_export";

    private Empresa empresa = new Empresa();
    private TipoIva ivaSeleccionado;
    private TipoRetencion retencionSeleccionada;
    private Serie serieSeleccionada;

    private final ObservableList<TipoIva> ivas = FXCollections.observableArrayList();
    private final ObservableList<TipoRetencion> retenciones = FXCollections.observableArrayList();
    private final ObservableList<Serie> series = FXCollections.observableArrayList();

    @FXML
    private ListView<ItemSeccion> listaSecciones;
    @FXML
    private StackPane pilaSecciones;
    @FXML
    private VBox seccionEmpresa;
    @FXML
    private VBox seccionCabeceraPie;
    @FXML
    private VBox seccionPdfApariencia;
    @FXML
    private VBox seccionIva;
    @FXML
    private VBox seccionRetenciones;
    @FXML
    private VBox seccionSeries;
    @FXML
    private HBox barraGuardar;
    @FXML
    private PreviaCabecera previaCabecera;
    @FXML
    private ToggleGroup grupoCabecera;
    @FXML
    private TextField txtNombre;
    @FXML
    private TextField txtNif;
    @FXML
    private TextField txtDireccion;
    @FXML
    private TextField txtCp;
    @FXML
    private TextField txtLocalidad;
    @FXML
    private TextField txtProvincia;
    @FXML
    private TextField txtActividad;
    @FXML
    private TextField txtEmail;
    @FXML
    private TextField txtTelefono;
    @FXML
    private RadioButton rbTexto;
    @FXML
    private RadioButton rbLogo;
    @FXML
    private TextField txtLogoPath;
    @FXML
    private TextArea txtPieLegal;
    @FXML
    private TextField txtCarpetaAuto;
    @FXML
    private TextField txtUltimaCarpeta;
    // Cómo funciona: JavaFX inyecta aquí el controlador del <fx:include fx:id="barra">;
    // el nombre del campo es el fx:id más "Controller".
    @FXML
    private BarraNavegacionController barraController;
    @FXML
    private Label lblDatosPendientes;
    @FXML
    private Button btnVolver;
    private boolean datosPendientes;
    @FXML
    private ComboBox<String> comboTema;
    @FXML
    private ColorPicker colorPdf;

    @FXML
    private TableView<TipoIva> tablaIva;
    @FXML
    private TableColumn<TipoIva, String> colIvaNombre;
    @FXML
    private TableColumn<TipoIva, String> colIvaPorcentaje;
    @FXML
    private TableColumn<TipoIva, String> colIvaSuplido;
    @FXML
    private TableColumn<TipoIva, String> colIvaMotivo;
    @FXML
    private TableColumn<TipoIva, String> colIvaActivo;
    @FXML
    private TextField txtIvaNombre;
    @FXML
    private TextField txtIvaPorcentaje;
    @FXML
    private TextField txtIvaMotivo;
    @FXML
    private CheckBox chkIvaSuplido;
    @FXML
    private Label lblIvaAviso;

    @FXML
    private TableView<TipoRetencion> tablaRetenciones;
    @FXML
    private TableColumn<TipoRetencion, String> colRetencionNombre;
    @FXML
    private TableColumn<TipoRetencion, String> colRetencionPorcentaje;
    @FXML
    private TableColumn<TipoRetencion, String> colRetencionActivo;
    @FXML
    private TextField txtRetencionNombre;
    @FXML
    private TextField txtRetencionPorcentaje;
    @FXML
    private Label lblRetencionAviso;

    @FXML
    private TableView<Serie> tablaSeries;
    @FXML
    private TableColumn<Serie, String> colSerieCodigo;
    @FXML
    private TableColumn<Serie, String> colSerieDescripcion;
    @FXML
    private TableColumn<Serie, String> colSerieRectifica;
    @FXML
    private TableColumn<Serie, String> colSerieSiguiente;
    @FXML
    private TableColumn<Serie, String> colSerieReutilizar;
    @FXML
    private TextField txtSerieCodigo;
    @FXML
    private TextField txtSerieDescripcion;
    @FXML
    private CheckBox chkSerieRectifica;
    @FXML
    private CheckBox chkSerieReutilizar;
    @FXML
    private TextField txtSerieSiguiente;
    @FXML
    private ComboBox<Serie.SufijoFecha> comboSerieFormato;
    @FXML
    private Label lblSerieEjemplo;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        barraController.marcarActivo("configuracion");
        cargarTema();
        try {
            empresa = Vista.getInstancia().getControlador().getModelo().getConfiguracion().getEmpresa();
        } catch (Exception e) {
            empresa = new Empresa();
        }
        cargarEmpresa();
        datosPendientes = !Vista.getInstancia().getControlador().getModelo().getConfiguracion().datosPendientes(empresa).isEmpty();
        if (datosPendientes) {
            if (txtNombre.getText() == null || txtNombre.getText().isBlank()) {
                txtNombre.setText(nombreVisibleEmpresaActiva());
            }
            lblDatosPendientes.setVisible(true);
            lblDatosPendientes.setManaged(true);
        }
        cargarIvas();
        cargarRetenciones();
        cargarSeries();
        cargarPdfs();
        cablearPrevia();
        configurarSecciones();
    }

    private void cargarTema() {
        comboTema.getItems().setAll(GestorTemas.temas());
        comboTema.setConverter(new StringConverter<>() {
            @Override
            public String toString(String tema) {
                return tema == null ? "" : GestorTemas.etiqueta(tema);
            }

            @Override
            public String fromString(String s) {
                return s;
            }
        });
        comboTema.setValue(GestorTemas.temaActivo());
        comboTema.valueProperty().addListener((o, a, b) -> {
            if (b != null) {
                GestorTemas.seleccionar(Vista.getInstancia().getVentana().getScene(), b);
            }
        });
    }

    // ------------------------------------------------------------------
    // Empresa / Cabecera / Pie
    // ------------------------------------------------------------------

    private void cargarEmpresa() {
        txtNombre.setText(nz(empresa.getNombre()));
        txtNif.setText(nz(empresa.getNif()));
        txtDireccion.setText(nz(empresa.getDireccion()));
        txtCp.setText(nz(empresa.getCp()));
        txtLocalidad.setText(nz(empresa.getLocalidad()));
        txtProvincia.setText(nz(empresa.getProvincia()));
        txtActividad.setText(nz(empresa.getActividad()));
        txtEmail.setText(nz(empresa.getEmail()));
        txtTelefono.setText(nz(empresa.getTelefono()));
        txtLogoPath.setText(nz(empresa.getLogoPath()));
        txtPieLegal.setText(nz(empresa.getPieLegal()));
        if ("LOGO".equalsIgnoreCase(empresa.getCabeceraModo())) {
            rbLogo.setSelected(true);
        } else {
            rbTexto.setSelected(true);
        }
    }

    private void recogerEmpresa() {
        empresa.setNombre(trim(txtNombre));
        empresa.setNif(trim(txtNif));
        empresa.setDireccion(trim(txtDireccion));
        empresa.setCp(trim(txtCp));
        empresa.setLocalidad(trim(txtLocalidad));
        empresa.setProvincia(trim(txtProvincia));
        empresa.setActividad(trim(txtActividad));
        empresa.setEmail(trim(txtEmail));
        empresa.setTelefono(trim(txtTelefono));
        empresa.setCabeceraModo(rbLogo.isSelected() ? "LOGO" : "TEXTO");
        empresa.setLogoPath(trim(txtLogoPath));
        empresa.setPieLegal(txtPieLegal.getText());
    }

    @FXML
    private void seleccionarLogo() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Seleccionar logo");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"));
        File f = chooser.showOpenDialog(Vista.getInstancia().getVentana());
        if (f != null) {
            txtLogoPath.setText(f.getAbsolutePath());
            Image imagen = new Image(f.toURI().toString());
            if (DisposicionCabecera.logoConPocaResolucion(imagen.getWidth(), imagen.getHeight())) {
                int ancho = (int) imagen.getWidth();
                int alto = (int) imagen.getHeight();
                long minimo = Math.round(DisposicionCabecera.anchoLogoDibujado((float) imagen.getWidth(), (float) imagen.getHeight()) * 1.5);
                Dialogos.mostrarDialogoInformacion("Logo", "La imagen es pequeña (" + ancho + " × " + alto
                        + " píxeles) y puede verse borrosa al imprimir la factura. "
                        + "Para un buen resultado, usa una imagen de al menos " + minimo + " píxeles de ancho.");
            }
        }
    }

    private String nombreVisibleEmpresaActiva() {
        try {
            String carpeta = Sesion.getSesion().getCarpetaEmpresa();
            for (EmpresaDisponible empresa : Vista.getInstancia().getControlador().listadoEmpresas()) {
                if (empresa.getCarpeta().equals(carpeta)) {
                    return empresa.getNombre();
                }
            }
        } catch (Exception ignored) {
        }
        return "";
    }

    private void cargarPdfs() {
        try {
            String auto = Vista.getInstancia().getControlador().getModelo().getConfiguracion().getPreferencia(PREV_CARPETA);
            txtCarpetaAuto.setText(nz(auto));
            String ultima = Vista.getInstancia().getControlador().getModelo().getConfiguracion().getPreferencia(PREV_EXPORT);
            txtUltimaCarpeta.setText(nz(ultima));
            colorPdf.setValue(colorGuardado(Vista.getInstancia().getControlador().getModelo().getConfiguracion().getPreferencia(ExportadorPdf.PREF_COLOR)));
        } catch (Exception e) {
            Dialogos.mostrarDialogoError("Configuración", "No se pudieron cargar las carpetas de PDF: " + e.getMessage());
        }
    }

    private Color colorGuardado(String hex) {
        try {
            if (hex != null && !hex.isBlank()) {
                return Color.web(hex.trim());
            }
        } catch (Exception ignored) {
        }
        return Color.web(ExportadorPdf.COLOR_DEFECTO);
    }

    @FXML
    private void seleccionarCarpetaAuto() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Carpeta automática de almacenamiento de PDF");
        File dir = chooser.showDialog(Vista.getInstancia().getVentana());
        if (dir != null) {
            txtCarpetaAuto.setText(dir.getAbsolutePath());
        }
    }

    @FXML
    private void guardar() {
        try {
            recogerEmpresa();
            List<String> faltan = Vista.getInstancia().getControlador().getModelo().getConfiguracion().datosPendientes(empresa);
            if (!faltan.isEmpty()) {
                Dialogos.mostrarDialogoError("Configuración",
                        "Faltan datos obligatorios o no son válidos:\n" + String.join(", ", faltan));
                return;
            }
            Vista.getInstancia().getControlador().getModelo().getConfiguracion().saveEmpresa(empresa);
            Vista.getInstancia().getControlador().getModelo().getConfiguracion().setPreferencia(PREV_CARPETA, trim(txtCarpetaAuto));
            Color c = colorPdf.getValue();
            String hex = String.format("#%02X%02X%02X",
                    (int) Math.round(c.getRed() * 255),
                    (int) Math.round(c.getGreen() * 255),
                    (int) Math.round(c.getBlue() * 255));
            Vista.getInstancia().getControlador().getModelo().getConfiguracion().setPreferencia(ExportadorPdf.PREF_COLOR, hex);
            GestorTemas.guardar(Vista.getInstancia().getControlador().getModelo());
            if (datosPendientes) {
                Dialogos.mostrarDialogoInformacion("Configuración", "Datos de la empresa completados.");
                Vista.getInstancia().mostrar("MenuPrincipal.fxml");
            } else {
                Dialogos.mostrarDialogoInformacion("Configuración", "Configuración guardada.");
            }
        } catch (Exception e) {
            Dialogos.mostrarDialogoError("Configuración", "No se pudo guardar: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // Secciones laterales
    // ------------------------------------------------------------------

    public static final class ItemSeccion {
        final String texto;
        public final Node panel;
        public final boolean grupo;
        final boolean guardar;

        ItemSeccion(String texto, Node panel, boolean grupo, boolean guardar) {
            this.texto = texto;
            this.panel = panel;
            this.grupo = grupo;
            this.guardar = guardar;
        }
    }

    private void configurarSecciones() {
        List<ItemSeccion> items = new ArrayList<>();
        items.add(new ItemSeccion("CONFIGURACIÓN GENERAL", null, true, false));
        items.add(new ItemSeccion("Empresa", seccionEmpresa, false, true));
        items.add(new ItemSeccion("Cabecera y pie", seccionCabeceraPie, false, true));
        items.add(new ItemSeccion("PDF y apariencia", seccionPdfApariencia, false, true));
        items.add(new ItemSeccion("CATÁLOGOS", null, true, false));
        items.add(new ItemSeccion("IVA", seccionIva, false, false));
        items.add(new ItemSeccion("Retenciones", seccionRetenciones, false, false));
        items.add(new ItemSeccion("Series", seccionSeries, false, false));
        listaSecciones.getItems().setAll(items);
        listaSecciones.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(ItemSeccion item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().remove("grupo-secciones");
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setDisable(false);
                } else if (item.grupo) {
                    setText(item.texto);
                    setGraphic(null);
                    getStyleClass().add("grupo-secciones");
                    setDisable(true);
                } else {
                    setText(item.texto);
                    setGraphic(null);
                    setDisable(false);
                }
            }
        });
        listaSecciones.getSelectionModel().selectedItemProperty().addListener((o, a, b) -> {
            if (b != null && !b.grupo) {
                mostrarSeccion(b);
            }
        });
        listaSecciones.getSelectionModel().select(1);
    }

    private void mostrarSeccion(ItemSeccion item) {
        for (Node n : pilaSecciones.getChildren()) {
            n.setVisible(false);
            n.setManaged(false);
        }
        item.panel.setVisible(true);
        item.panel.setManaged(true);
        barraGuardar.setVisible(item.guardar);
        barraGuardar.setManaged(item.guardar);
    }

    // ------------------------------------------------------------------
    // Vista previa de cabecera
    // ------------------------------------------------------------------

    private void cablearPrevia() {
        grupoCabecera.selectedToggleProperty().addListener((o, a, b) -> repintarPrevia());
        txtLogoPath.textProperty().addListener((o, a, b) -> repintarPrevia());
        colorPdf.valueProperty().addListener((o, a, b) -> repintarPrevia());
        repintarPrevia();
    }

    private void repintarPrevia() {
        Empresa e = new Empresa();
        e.setNombre(trim(txtNombre));
        e.setNif(trim(txtNif));
        e.setActividad(trim(txtActividad));
        e.setDireccion(trim(txtDireccion));
        e.setCp(trim(txtCp));
        e.setLocalidad(trim(txtLocalidad));
        e.setProvincia(trim(txtProvincia));
        e.setEmail(trim(txtEmail));
        e.setTelefono(trim(txtTelefono));
        e.setCabeceraModo(rbLogo.isSelected() ? "LOGO" : "TEXTO");
        e.setLogoPath(trim(txtLogoPath));
        previaCabecera.mostrar(e, colorPdf.getValue());
    }

    // ------------------------------------------------------------------
    // IVA
    // ------------------------------------------------------------------

    private void cargarIvas() {
        colIvaNombre.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(nz(c.getValue().getNombre())));
        colIvaPorcentaje.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().label()));
        colIvaSuplido.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().isEsSuplido() ? "Sí" : "No"));
        colIvaMotivo.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(nz(c.getValue().getMotivoExencion())));
        colIvaActivo.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().isActivo() ? "Sí" : "No"));
        tablaIva.getSelectionModel().selectedItemProperty().addListener((o, a, b) -> seleccionarIva(b));
        chkIvaSuplido.selectedProperty().addListener((o, a, b) ->
                txtIvaPorcentaje.setDisable(b || (ivaSeleccionado != null && enUsoIva(ivaSeleccionado))));
        refrescarIvas();
    }

    private void refrescarIvas() {
        try {
            ivas.setAll(Vista.getInstancia().getControlador().getModelo().getTiposIva().listar(false));
            tablaIva.setItems(ivas);
        } catch (Exception e) {
            Dialogos.mostrarDialogoError("Configuración", "No se pudieron cargar los tipos de IVA: " + e.getMessage());
        }
    }

    private void seleccionarIva(TipoIva t) {
        ivaSeleccionado = t;
        if (t == null) {
            return;
        }
        txtIvaNombre.setText(nz(t.getNombre()));
        txtIvaPorcentaje.setText(t.isExento() ? "" : String.valueOf(t.getPorcentaje()));
        txtIvaMotivo.setText(nz(t.getMotivoExencion()));
        chkIvaSuplido.setSelected(t.isEsSuplido());
        boolean enUso = enUsoIva(t);
        txtIvaPorcentaje.setDisable(enUso || t.isEsSuplido());
        lblIvaAviso.setVisible(enUso);
        lblIvaAviso.setManaged(enUso);
    }

    private boolean enUsoIva(TipoIva t) {
        try {
            return t.getId() != null && Vista.getInstancia().getControlador().getModelo().getTiposIva().enUso(t.getId());
        } catch (Exception e) {
            return false;
        }
    }

    @FXML
    private void nuevoIva() {
        ivaSeleccionado = null;
        txtIvaNombre.clear();
        txtIvaPorcentaje.clear();
        txtIvaMotivo.clear();
        chkIvaSuplido.setSelected(false);
        txtIvaPorcentaje.setDisable(false);
        lblIvaAviso.setVisible(false);
        lblIvaAviso.setManaged(false);
        txtIvaNombre.requestFocus();
    }

    @FXML
    private void guardarIva() {
        String nombre = trim(txtIvaNombre);
        if (nombre.isBlank()) {
            Dialogos.mostrarDialogoError("IVA", "Indique el nombre del tipo de IVA.");
            return;
        }
        Integer porcentaje = null;
        boolean suplido = chkIvaSuplido.isSelected();
        String pct = trim(txtIvaPorcentaje);
        if (!pct.isBlank()) {
            try {
                porcentaje = Integer.parseInt(pct);
                if (porcentaje < 0 || porcentaje > 100) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException e) {
                Dialogos.mostrarDialogoError("IVA", "El porcentaje debe ser un entero entre 0 y 100 (o dejar vacío para exento).");
                return;
            }
        }
        if (suplido) {
            porcentaje = null;
        }
        if (porcentaje == null && ivaSeleccionado != null && !ivaSeleccionado.isExento()) {
            Dialogos.mostrarDialogoError("IVA", "Un tipo ya usado en el histórico no puede pasarse a exento.");
            return;
        }
        if (porcentaje != null && ivaSeleccionado != null && ivaSeleccionado.isExento()) {
            Dialogos.mostrarDialogoError("IVA", "Un tipo de exención ya usado en el histórico no puede convertirse a porcentaje.");
            return;
        }
        if (suplido && ivaSeleccionado != null && !ivaSeleccionado.isEsSuplido()) {
            Dialogos.mostrarDialogoError("IVA", "Un tipo existente no puede convertirse a suplido.");
            return;
        }
        if (!suplido && ivaSeleccionado != null && ivaSeleccionado.isEsSuplido()) {
            Dialogos.mostrarDialogoError("IVA", "Un suplido existente no puede dejar de ser suplido.");
            return;
        }
        try {
            TipoIva t = ivaSeleccionado != null ? ivaSeleccionado : new TipoIva();
            t.setNombre(nombre);
            t.setPorcentaje(porcentaje);
            t.setMotivoExencion(trim(txtIvaMotivo));
            t.setEsSuplido(suplido);
            if (t.getId() == null) {
                t.setActivo(true);
                t.setId(Vista.getInstancia().getControlador().getModelo().getTiposIva().insertar(t));
            } else {
                Vista.getInstancia().getControlador().getModelo().getTiposIva().actualizar(t);
            }
            refrescarIvas();
            nuevoIva();
        } catch (Exception e) {
            Dialogos.mostrarDialogoError("IVA", "No se pudo guardar: " + e.getMessage());
        }
    }

    @FXML
    private void inactivarIva() {
        TipoIva t = tablaIva.getSelectionModel().getSelectedItem();
        if (t == null) {
            Dialogos.mostrarDialogoError("IVA", "Seleccione un tipo de IVA de la tabla.");
            return;
        }
        String accion = t.isActivo() ? "inactivar" : "reactivar";
        if (!Dialogos.mostrarDialogoConfirmacion("IVA", "¿" + (t.isActivo() ? "Inactivar" : "Reactivar")
                + " el tipo \"" + nz(t.getNombre()) + "\"?")) {
            return;
        }
        try {
            Vista.getInstancia().getControlador().getModelo().getTiposIva().setActivo(t.getId(), !t.isActivo());
            refrescarIvas();
        } catch (Exception e) {
            Dialogos.mostrarDialogoError("IVA", "No se pudo " + accion + ": " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // Retenciones
    // ------------------------------------------------------------------

    private void cargarRetenciones() {
        colRetencionNombre.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(nz(c.getValue().getNombre())));
        colRetencionPorcentaje.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().label()));
        colRetencionActivo.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().isActivo() ? "Sí" : "No"));
        tablaRetenciones.getSelectionModel().selectedItemProperty().addListener((o, a, b) -> seleccionarRetencion(b));
        refrescarRetenciones();
    }

    private void refrescarRetenciones() {
        try {
            retenciones.setAll(Vista.getInstancia().getControlador().getModelo().getTiposRetencion().listar(false));
            tablaRetenciones.setItems(retenciones);
        } catch (Exception e) {
            Dialogos.mostrarDialogoError("Configuración", "No se pudieron cargar los tipos de retención: " + e.getMessage());
        }
    }

    private void seleccionarRetencion(TipoRetencion t) {
        retencionSeleccionada = t;
        if (t == null) {
            return;
        }
        txtRetencionNombre.setText(nz(t.getNombre()));
        txtRetencionPorcentaje.setText(String.valueOf(t.getPorcentaje()));
        boolean enUso = enUsoRetencion(t);
        txtRetencionPorcentaje.setDisable(enUso);
        lblRetencionAviso.setVisible(enUso);
        lblRetencionAviso.setManaged(enUso);
    }

    private boolean enUsoRetencion(TipoRetencion t) {
        try {
            return t.getId() != null && Vista.getInstancia().getControlador().getModelo().getTiposRetencion().enUso(t.getId());
        } catch (Exception e) {
            return false;
        }
    }

    @FXML
    private void nuevoRetencion() {
        retencionSeleccionada = null;
        txtRetencionNombre.clear();
        txtRetencionPorcentaje.clear();
        txtRetencionPorcentaje.setDisable(false);
        lblRetencionAviso.setVisible(false);
        lblRetencionAviso.setManaged(false);
        txtRetencionNombre.requestFocus();
    }

    @FXML
    private void guardarRetencion() {
        String nombre = trim(txtRetencionNombre);
        if (nombre.isBlank()) {
            Dialogos.mostrarDialogoError("Retención", "Indique el nombre del tipo de retención.");
            return;
        }
        int porcentaje;
        try {
            porcentaje = Integer.parseInt(trim(txtRetencionPorcentaje));
            if (porcentaje < 0 || porcentaje > 100) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            Dialogos.mostrarDialogoError("Retención", "El porcentaje debe ser un entero entre 0 y 100.");
            return;
        }
        if (retencionSeleccionada != null && enUsoRetencion(retencionSeleccionada)) {
            int actual = retencionSeleccionada.getPorcentaje() != null ? retencionSeleccionada.getPorcentaje() : 0;
            if (porcentaje != actual) {
                Dialogos.mostrarDialogoError("Retención", "El porcentaje de un tipo ya usado en el histórico no se puede modificar.");
                return;
            }
        }
        try {
            TipoRetencion t = retencionSeleccionada != null ? retencionSeleccionada : new TipoRetencion();
            t.setNombre(nombre);
            t.setPorcentaje(porcentaje);
            if (t.getId() == null) {
                t.setActivo(true);
                t.setId(Vista.getInstancia().getControlador().getModelo().getTiposRetencion().insertar(t));
            } else {
                Vista.getInstancia().getControlador().getModelo().getTiposRetencion().actualizar(t);
            }
            refrescarRetenciones();
            nuevoRetencion();
        } catch (Exception e) {
            Dialogos.mostrarDialogoError("Retención", "No se pudo guardar: " + e.getMessage());
        }
    }

    @FXML
    private void inactivarRetencion() {
        TipoRetencion t = tablaRetenciones.getSelectionModel().getSelectedItem();
        if (t == null) {
            Dialogos.mostrarDialogoError("Retención", "Seleccione un tipo de retención de la tabla.");
            return;
        }
        String accion = t.isActivo() ? "inactivar" : "reactivar";
        if (!Dialogos.mostrarDialogoConfirmacion("Retención", "¿" + (t.isActivo() ? "Inactivar" : "Reactivar")
                + " el tipo \"" + nz(t.getNombre()) + "\"?")) {
            return;
        }
        try {
            Vista.getInstancia().getControlador().getModelo().getTiposRetencion().setActivo(t.getId(), !t.isActivo());
            refrescarRetenciones();
        } catch (Exception e) {
            Dialogos.mostrarDialogoError("Retención", "No se pudo " + accion + ": " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // Series
    // ------------------------------------------------------------------

    private void cargarSeries() {
        colSerieCodigo.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(nz(c.getValue().getCodigo())));
        colSerieDescripcion.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(nz(c.getValue().getDescripcion())));
        colSerieRectifica.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().isEsRectificativa() ? "Sí" : "No"));
        colSerieSiguiente.setText("Siguiente (" + anioTrabajo() + ")");
        colSerieSiguiente.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(String.valueOf(c.getValue().getSiguienteCorrelativo())));
        colSerieReutilizar.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().isReutilizarAnulados() ? "Sí" : "No"));
        comboSerieFormato.getItems().setAll(Serie.SufijoFecha.values());
        comboSerieFormato.setConverter(new StringConverter<>() {
            @Override
            public String toString(Serie.SufijoFecha f) {
                if (f == null) return "";
                return switch (f) {
                    case MES -> "Código-Número/Mes (ej: C-56/7)";
                    case ANIO -> "Número-Año (ej: 56-2026)";
                    case NINGUNO -> "Solo número (ej: 56)";
                };
            }
            @Override
            public Serie.SufijoFecha fromString(String s) { return null; }
        });
        comboSerieFormato.valueProperty().addListener((o, a, b) -> actualizarEjemploFormato());
        tablaSeries.getSelectionModel().selectedItemProperty().addListener((o, a, b) -> seleccionarSerie(b));
        refrescarSeries();
    }

    private void refrescarSeries() {
        try {
            List<Serie> lista = Vista.getInstancia().getControlador().getModelo().getSeries().listar();
            int anio = anioTrabajo();
            for (Serie s : lista) {
                s.setSiguienteCorrelativo(Vista.getInstancia().getControlador().getModelo().getSeries().getSiguiente(s.getId(), anio));
            }
            series.setAll(lista);
            tablaSeries.setItems(series);
        } catch (Exception e) {
            Dialogos.mostrarDialogoError("Configuración", "No se pudieron cargar las series: " + e.getMessage());
        }
    }

    private int anioTrabajo() {
        return Vista.getInstancia().getControlador().getModelo().getReloj().fechaTrabajo().getYear();
    }

    private void seleccionarSerie(Serie s) {
        serieSeleccionada = s;
        if (s == null) {
            return;
        }
        txtSerieCodigo.setText(nz(s.getCodigo()));
        txtSerieDescripcion.setText(nz(s.getDescripcion()));
        chkSerieRectifica.setSelected(s.isEsRectificativa());
        chkSerieReutilizar.setSelected(s.isReutilizarAnulados());
        txtSerieSiguiente.setText(String.valueOf(s.getSiguienteCorrelativo()));
        comboSerieFormato.setValue(s.getSufijoFecha() != null ? s.getSufijoFecha() : Serie.SufijoFecha.MES);
        actualizarEjemploFormato();
    }

    private void actualizarEjemploFormato() {
        Serie s = serieSeleccionada != null ? serieSeleccionada : new Serie();
        String codigo = trim(txtSerieCodigo).toUpperCase();
        if (codigo.isBlank()) codigo = "";
        Serie.SufijoFecha formato = comboSerieFormato.getValue() != null ? comboSerieFormato.getValue() : Serie.SufijoFecha.MES;
        String ejemplo;
        if (s.isEsRectificativa()) {
            ejemplo = (codigo.isBlank() ? "" : codigo + "-") + "1";
        } else {
            String prefijo = codigo.isBlank() ? "" : codigo + "-";
            ejemplo = switch (formato) {
                case MES -> prefijo + "56/7";
                case ANIO -> prefijo + "56-2026";
                case NINGUNO -> prefijo + "56";
            };
        }
        lblSerieEjemplo.setText("Ejemplo: " + ejemplo);
    }

    @FXML
    private void nuevoSerie() {
        serieSeleccionada = null;
        txtSerieCodigo.clear();
        txtSerieDescripcion.clear();
        chkSerieRectifica.setSelected(false);
        chkSerieReutilizar.setSelected(false);
        txtSerieSiguiente.setText("1");
        comboSerieFormato.setValue(Serie.SufijoFecha.MES);
        actualizarEjemploFormato();
        txtSerieCodigo.requestFocus();
    }

    @FXML
    private void guardarSerie() {
        String codigo = trim(txtSerieCodigo);
        int siguiente;
        try {
            siguiente = Integer.parseInt(trim(txtSerieSiguiente));
            if (siguiente < 1) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            Dialogos.mostrarDialogoError("Series", "El siguiente número debe ser un entero mayor o igual que 1.");
            return;
        }
        try {
            if (codigo.isBlank()) {
                boolean otraSinCodigo = series.stream().anyMatch(x ->
                        (x.getCodigo() == null || x.getCodigo().isBlank())
                                && (serieSeleccionada == null || !serieSeleccionada.getId().equals(x.getId())));
                if (otraSinCodigo) {
                    Dialogos.mostrarDialogoError("Series", "Solo puede haber una serie sin código. Ponle un código o una descripción para distinguirla.");
                    return;
                }
            } else {
                for (Serie s : series) {
                    if (s.getCodigo() != null && s.getCodigo().equalsIgnoreCase(codigo)
                            && (serieSeleccionada == null || !serieSeleccionada.getId().equals(s.getId()))) {
                        Dialogos.mostrarDialogoError("Series", "Ya existe una serie con el código \"" + codigo + "\".");
                        return;
                    }
                }
            }
            Serie s = serieSeleccionada != null ? serieSeleccionada : new Serie();
            s.setCodigo(codigo.isBlank() ? "" : codigo.toUpperCase());
            s.setDescripcion(trim(txtSerieDescripcion));
            s.setEsRectificativa(chkSerieRectifica.isSelected());
            s.setReutilizarAnulados(chkSerieReutilizar.isSelected());
            s.setSiguienteCorrelativo(siguiente);
            s.setSufijoFecha(comboSerieFormato.getValue() != null ? comboSerieFormato.getValue() : Serie.SufijoFecha.MES);
            if (s.getId() == null) {
                s.setId(Vista.getInstancia().getControlador().getModelo().getSeries().insertar(s));
            } else {
                Vista.getInstancia().getControlador().getModelo().getSeries().actualizar(s);
            }
            int nuevoAnio = Math.max(Vista.getInstancia().getControlador().getModelo().getSeries().getSiguiente(s.getId(), anioTrabajo()), siguiente);
            Vista.getInstancia().getControlador().getModelo().getSeries().actualizarSiguiente(s.getId(), anioTrabajo(), nuevoAnio);
            refrescarSeries();
            nuevoSerie();
        } catch (Exception e) {
            Dialogos.mostrarDialogoError("Series", "No se pudo guardar: " + e.getMessage());
        }
    }

    @FXML
    private void eliminarSerie() {
        Serie s = tablaSeries.getSelectionModel().getSelectedItem();
        if (s == null) {
            Dialogos.mostrarDialogoError("Series", "Seleccione una serie de la tabla.");
            return;
        }
        try {
            if (Vista.getInstancia().getControlador().getModelo().getSeries().tieneFacturas(s.getId())) {
                Dialogos.mostrarDialogoError("Series", "La serie \"" + codigoOBlanco(s)
                        + "\" no puede eliminarse: tiene facturas (activas o históricas). El histórico no se elimina.");
                return;
            }
        } catch (Exception e) {
            Dialogos.mostrarDialogoError("Series", "No se pudo comprobar la serie: " + e.getMessage());
            return;
        }
        String etiqueta = (s.getCodigo() == null || s.getCodigo().isBlank())
                ? (s.getDescripcion() == null || s.getDescripcion().isBlank() ? "esta serie" : s.getDescripcion())
                : s.getCodigo();
        if (!Dialogos.mostrarDialogoConfirmacion("Eliminar serie", "¿Seguro que deseas eliminar la serie \"" + etiqueta + "\"?")) {
            return;
        }
        try {
            Vista.getInstancia().getControlador().getModelo().getSeries().eliminar(s.getId());
            refrescarSeries();
        } catch (Exception e) {
            Dialogos.mostrarDialogoError("Series", "No se pudo eliminar la serie: " + e.getMessage());
        }
    }

    private String codigoOBlanco(Serie s) {
        String c = s.getCodigo();
        return (c == null || c.isBlank()) ? "(sin código)" : c;
    }

    // ------------------------------------------------------------------
    // Cambiar de empresa
    // ------------------------------------------------------------------

    /** Volvemos al arranque para elegir otra empresa, avisando de lo que no se haya guardado. */
    @FXML
    private void cambiarDeEmpresa() {
        if (Dialogos.mostrarDialogoConfirmacion("Cambiar de empresa",
                "Se cerrará esta empresa y volverás a la pantalla de arranque.\n\n"
                        + "Lo que no hayas guardado en Configuración se perderá. ¿Continuar?")) {
            Vista.getInstancia().volverAlArranque();
        }
    }

    // ------------------------------------------------------------------

    @FXML
    private void volver() {
        Vista.getInstancia().mostrar("MenuPrincipal.fxml");
    }

    private String trim(TextField f) {
        return f.getText() == null ? "" : f.getText().trim();
    }

    private String nz(String s) {
        return s == null ? "" : s;
    }
}
