package cabofactu.vista.controlador;

import cabofactu.modelo.dominio.Empresa;
import cabofactu.modelo.dominio.EmpresaDisponible;
import cabofactu.modelo.dominio.Serie;
import cabofactu.modelo.dominio.TipoIva;
import cabofactu.modelo.dominio.TipoRetencion;
import cabofactu.pdf.ExportadorPdf;
import cabofactu.pdf.DisposicionCabecera;
import cabofactu.modelo.negocio.Sesion;
import cabofactu.vista.recursos.LocalizadorRecursos;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;
import cabofactu.vista.Pantalla;
import cabofactu.vista.Vista;
import cabofactu.vista.utilidades.Dialogos;
import cabofactu.vista.utilidades.PreviaCabecera;
import cabofactu.vista.utilidades.GestorTemas;

/**
 * Configuracion por secciones con lista lateral: Empresa, Cabecera y pie, PDF
 * y apariencia se guardan con un boton global; IVA, Retenciones y Series
 * se administran con sus propias acciones.
 */
public class ConfiguracionController implements Pantalla, Initializable {

    private static final String PREV_CARPETA = "carpeta_facturas";
    private static final String PREV_EXPORT = "ultima_carpeta_export";

    private Empresa empresa;
    private TipoIva ivaElegido;
    private TipoRetencion retencionElegida;
    private Serie serieElegida;
    private boolean bloqueada;

    private final ObservableList<TipoIva> ivas = FXCollections.observableArrayList();
    private final ObservableList<TipoRetencion> retenciones = FXCollections.observableArrayList();
    private final ObservableList<Serie> series = FXCollections.observableArrayList();

    @FXML
    private ToggleGroup grupoSecciones;
    @FXML
    private ToggleButton btnEmpresa;
    @FXML
    private ToggleButton btnCabecera;
    @FXML
    private ToggleButton btnPdf;
    @FXML
    private ToggleButton btnIva;
    @FXML
    private ToggleButton btnRetenciones;
    @FXML
    private ToggleButton btnSeries;
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
    private TableView<TipoRetencion> tablaRetenciones;
    @FXML
    private TableColumn<TipoRetencion, String> colRetencionNombre;
    @FXML
    private TableColumn<TipoRetencion, String> colRetencionPorcentaje;
    @FXML
    private TableColumn<TipoRetencion, String> colRetencionActivo;

    @FXML
    private TableView<Serie> tablaSeries;
    @FXML
    private TableColumn<Serie, String> colSerieCodigo;
    @FXML
    private TableColumn<Serie, String> colSerieDescripcion;
    @FXML
    private TableColumn<Serie, String> colSerieRectifica;
    @FXML
    private TableColumn<Serie, String> colSerieFormato;
    @FXML
    private TableColumn<Serie, String> colSerieSiguiente;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        barraController.marcarActivo("configuracion");
        cargarTema();
        cargarEmpresa();
        cargarPreferenciasPdf();
        cargarIvas();
        cargarRetenciones();
        cargarSeries();
        cablearPrevia();
        mostrarSeccion(seccionEmpresa, btnEmpresa, true);
    }

    private void cargarTema() {
        comboTema.getItems().setAll(GestorTemas.nombres());
        comboTema.setValue(GestorTemas.etiqueta(GestorTemas.temaActivo()));
        comboTema.valueProperty().addListener((propiedad, anterior, nuevo) -> {
            if (nuevo != null) {
                GestorTemas.seleccionar(Vista.getInstancia().getVentana().getScene(),
                        GestorTemas.claveDe(nuevo));
            }
        });
    }

    // ------------------------------------------------------------------
    // Empresa / Cabecera / Pie
    // ------------------------------------------------------------------

    private void cargarEmpresa() {
        try {
            empresa = Vista.getInstancia().getControlador().buscarEmpresa();
        } catch (Exception e) {
            empresa = null;
        }
        if (empresa == null) {
            bloqueada = true;
            txtNombre.setText(nombreVisibleEmpresaActiva());
            lblDatosPendientes.setVisible(true);
            lblDatosPendientes.setManaged(true);
            barraController.bloquearSalvoSalir();
            btnVolver.setDisable(true);
            return;
        }
        bloqueada = false;
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
        if (empresa.isCabeceraLogo()) {
            rbLogo.setSelected(true);
        } else {
            rbTexto.setSelected(true);
        }
    }

    private String nombreVisibleEmpresaActiva() {
        try {
            String carpeta = Sesion.getSesion().getCarpetaEmpresa();
            for (EmpresaDisponible disponible : Vista.getInstancia().getControlador().listadoEmpresas()) {
                if (disponible.getCarpeta().equals(carpeta)) {
                    return disponible.getNombre();
                }
            }
        } catch (Exception ignored) {
        }
        return "";
    }

    private void cargarPreferenciasPdf() {
        try {
            String auto = Vista.getInstancia().getControlador().preferencia(PREV_CARPETA);
            txtCarpetaAuto.setText(nz(auto));
            String ultima = Vista.getInstancia().getControlador().preferencia(PREV_EXPORT);
            txtUltimaCarpeta.setText(nz(ultima));
            colorPdf.setValue(colorGuardado(Vista.getInstancia().getControlador().preferencia(ExportadorPdf.PREF_COLOR)));
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
    void guardar(ActionEvent event) {
        String error = marcarCamposMalos();
        if (error != null) {
            mostrarSeccion(seccionEmpresa, btnEmpresa, true);
            Dialogos.mostrarDialogoError("Datos de la empresa", error);
            return;
        }
        try {
            Vista.getInstancia().getControlador().modificarEmpresa(empresaDeLosCampos());
            guardarPreferenciasPdf();
            GestorTemas.guardar();
            if (bloqueada) {
                Dialogos.mostrarDialogoInformacion("Configuración", "Datos de la empresa completados.");
                Vista.getInstancia().mostrar("MenuPrincipal.fxml");
            } else {
                Dialogos.mostrarDialogoInformacion("Configuración", "Configuración guardada.");
            }
        } catch (Exception e) {
            Dialogos.mostrarDialogoError("Configuración", e.getMessage());
        }
    }

    /**
     * Marcamos en rojo todos los campos incorrectos y devolvemos el mensaje del
     * primero, o null si está todo bien.
     */
    private String marcarCamposMalos() {
        quitarMarcas();
        String primero = null;
        primero = revisar(txtNombre, Empresa.errorNombre(txtNombre.getText()), primero);
        primero = revisar(txtNif, Empresa.errorNif(txtNif.getText()), primero);
        primero = revisar(txtDireccion, Empresa.errorDireccion(txtDireccion.getText()), primero);
        primero = revisar(txtCp, Empresa.errorCp(txtCp.getText()), primero);
        primero = revisar(txtLocalidad, Empresa.errorLocalidad(txtLocalidad.getText()), primero);
        primero = revisar(txtProvincia, Empresa.errorProvincia(txtProvincia.getText()), primero);
        primero = revisar(txtEmail, Empresa.errorEmail(txtEmail.getText()), primero);
        primero = revisar(txtTelefono, Empresa.errorTelefono(txtTelefono.getText()), primero);
        return primero;
    }

    /** Marcamos el campo si tiene error y nos quedamos con el primer mensaje. */
    private String revisar(TextField campo, String error, String primero) {
        if (error == null) {
            return primero;
        }
        campo.getStyleClass().add("campo-error");
        if (primero == null) {
            return error;
        }
        return primero;
    }

    private void quitarMarcas() {
        txtNombre.getStyleClass().remove("campo-error");
        txtNif.getStyleClass().remove("campo-error");
        txtDireccion.getStyleClass().remove("campo-error");
        txtCp.getStyleClass().remove("campo-error");
        txtLocalidad.getStyleClass().remove("campo-error");
        txtProvincia.getStyleClass().remove("campo-error");
        txtEmail.getStyleClass().remove("campo-error");
        txtTelefono.getStyleClass().remove("campo-error");
    }

    /** Construimos la empresa con lo que hay en los campos: ya están todos comprobados. */
    private Empresa empresaDeLosCampos() throws Exception {
        Empresa construida = new Empresa(
                txtNombre.getText().trim(),
                txtNif.getText().trim(),
                txtDireccion.getText().trim(),
                txtCp.getText().trim(),
                txtLocalidad.getText().trim(),
                txtProvincia.getText().trim(),
                txtEmail.getText().trim(),
                txtTelefono.getText().trim());
        construida.setActividad(txtActividad.getText().trim());
        if (rbLogo.isSelected()) {
            construida.setCabeceraModo(Empresa.CABECERA_LOGO);
        } else {
            construida.setCabeceraModo(Empresa.CABECERA_TEXTO);
        }
        construida.setLogoPath(txtLogoPath.getText().trim());
        construida.setPieLegal(txtPieLegal.getText());
        return construida;
    }

    /** Guardamos la carpeta automática y el color del PDF. */
    private void guardarPreferenciasPdf() throws Exception {
        Vista.getInstancia().getControlador().guardarPreferencia(PREV_CARPETA, trim(txtCarpetaAuto));
        Color c = colorPdf.getValue();
        String hex = String.format("#%02X%02X%02X",
                (int) Math.round(c.getRed() * 255),
                (int) Math.round(c.getGreen() * 255),
                (int) Math.round(c.getBlue() * 255));
        Vista.getInstancia().getControlador().guardarPreferencia(ExportadorPdf.PREF_COLOR, hex);
    }

    // ------------------------------------------------------------------
    // Secciones laterales
    // ------------------------------------------------------------------

    @FXML
    void verEmpresa(ActionEvent event) {
        mostrarSeccion(seccionEmpresa, btnEmpresa, true);
    }

    @FXML
    void verCabecera(ActionEvent event) {
        mostrarSeccion(seccionCabeceraPie, btnCabecera, true);
    }

    @FXML
    void verPdf(ActionEvent event) {
        mostrarSeccion(seccionPdfApariencia, btnPdf, true);
    }

    @FXML
    void verIva(ActionEvent event) {
        mostrarSeccion(seccionIva, btnIva, false);
    }

    @FXML
    void verRetenciones(ActionEvent event) {
        mostrarSeccion(seccionRetenciones, btnRetenciones, false);
    }

    @FXML
    void verSeries(ActionEvent event) {
        mostrarSeccion(seccionSeries, btnSeries, false);
    }

    /**
     * Enseñamos una sección y ocultamos las demás. Volvemos a marcar su botón
     * porque en un grupo de ToggleButton se puede desmarcar el que ya estaba
     * pulsado, y entonces no quedaría ninguna sección a la vista.
     */
    private void mostrarSeccion(VBox seccion, ToggleButton boton, boolean conGuardar) {
        for (Node panel : pilaSecciones.getChildren()) {
            panel.setVisible(false);
            panel.setManaged(false);
        }
        seccion.setVisible(true);
        seccion.setManaged(true);
        boton.setSelected(true);
        barraGuardar.setVisible(conGuardar);
        barraGuardar.setManaged(conGuardar);
    }

    // ------------------------------------------------------------------
    // Vista previa de cabecera
    // ------------------------------------------------------------------

    private void cablearPrevia() {
        grupoCabecera.selectedToggleProperty().addListener((propiedad, anterior, nuevo) -> repintarPrevia());
        txtNombre.textProperty().addListener((propiedad, anterior, nuevo) -> repintarPrevia());
        txtLogoPath.textProperty().addListener((propiedad, anterior, nuevo) -> repintarPrevia());
        colorPdf.valueProperty().addListener((propiedad, anterior, nuevo) -> repintarPrevia());
        repintarPrevia();
    }

    /** Pintamos la cabecera con los datos de los campos, o el aviso si están incompletos. */
    private void repintarPrevia() {
        try {
            previaCabecera.mostrar(empresaDeLosCampos(), colorPdf.getValue());
        } catch (Exception e) {
            previaCabecera.mostrar(null, colorPdf.getValue());
        }
    }

    // ------------------------------------------------------------------
    // IVA
    // ------------------------------------------------------------------

    private void cargarIvas() {
        colIvaNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colIvaPorcentaje.setCellValueFactory(new PropertyValueFactory<>("porcentajeTexto"));
        colIvaSuplido.setCellValueFactory(new PropertyValueFactory<>("suplidoTexto"));
        colIvaMotivo.setCellValueFactory(new PropertyValueFactory<>("motivoExencion"));
        colIvaActivo.setCellValueFactory(new PropertyValueFactory<>("activoTexto"));
        refrescarIvas();
    }

    private void refrescarIvas() {
        try {
            ivaElegido = null;
            tablaIva.getSelectionModel().clearSelection();
            ivas.setAll(Vista.getInstancia().getControlador().listadoTiposIva(false));
            tablaIva.setItems(ivas);
        } catch (Exception e) {
            Dialogos.mostrarDialogoError("Configuración", "No se pudieron cargar los tipos de IVA: " + e.getMessage());
        }
    }

    /** Guardamos la fila elegida; con doble clic la abrimos para editarla. */
    @FXML
    private void seleccionarIva(MouseEvent evento) {
        ivaElegido = tablaIva.getSelectionModel().getSelectedItem();
        if (evento.getClickCount() == 2 && ivaElegido != null) {
            editarIva();
        }
    }

    @FXML
    private void nuevoIva() {
        abrirFichaIva(null, "Alta de tipo de IVA");
    }

    @FXML
    private void editarIva() {
        if (ivaElegido == null) {
            Dialogos.mostrarDialogoAdvertencia("IVA", "Seleccione un tipo de IVA de la tabla.");
            return;
        }
        abrirFichaIva(ivaElegido, "Datos del tipo de IVA");
    }

    @FXML
    private void eliminarIva() {
        if (ivaElegido == null) {
            Dialogos.mostrarDialogoAdvertencia("IVA", "Seleccione un tipo de IVA de la tabla.");
            return;
        }
        if (!Dialogos.mostrarDialogoConfirmacion("Eliminar tipo de IVA",
                "¿Eliminar definitivamente el tipo \"" + nz(ivaElegido.getNombre()) + "\"?")) {
            return;
        }
        try {
            Vista.getInstancia().getControlador().bajaTipoIva(ivaElegido.getId());
            refrescarIvas();
        } catch (Exception e) {
            Dialogos.mostrarDialogoError("IVA", e.getMessage());
        }
    }

    /**
     * Abrimos la ficha en modo añadir (registro null) o en modo editar, y
     * esperamos a que se cierre.
     */
    private void abrirFichaIva(TipoIva registro, String titulo) {
        try {
            FXMLLoader cargador = new FXMLLoader(LocalizadorRecursos.class.getResource("FichaTipoIva.fxml"));
            Parent raiz = cargador.load();
            FichaTipoIvaController ficha = cargador.getController();
            ficha.setRegistro(registro);
            Stage modal = Vista.getInstancia().crearVentanaModal(raiz, titulo, ficha);
            modal.showAndWait();
            TipoIva guardado = ficha.getRegistro();
            if (guardado == null) {
                return;
            }
            if (registro == null) {
                guardado.setId(Vista.getInstancia().getControlador().altaTipoIva(guardado));
            } else {
                Vista.getInstancia().getControlador().modificarTipoIva(guardado);
            }
            refrescarIvas();
        } catch (Exception e) {
            Dialogos.mostrarDialogoError("IVA", "No se pudo guardar: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // Retenciones
    // ------------------------------------------------------------------

    private void cargarRetenciones() {
        colRetencionNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colRetencionPorcentaje.setCellValueFactory(new PropertyValueFactory<>("porcentajeTexto"));
        colRetencionActivo.setCellValueFactory(new PropertyValueFactory<>("activoTexto"));
        refrescarRetenciones();
    }

    private void refrescarRetenciones() {
        try {
            retencionElegida = null;
            tablaRetenciones.getSelectionModel().clearSelection();
            retenciones.setAll(Vista.getInstancia().getControlador().listadoTiposRetencion(false));
            tablaRetenciones.setItems(retenciones);
        } catch (Exception e) {
            Dialogos.mostrarDialogoError("Configuración", "No se pudieron cargar los tipos de retención: " + e.getMessage());
        }
    }

    /** Guardamos la fila elegida; con doble clic la abrimos para editarla. */
    @FXML
    private void seleccionarRetencion(MouseEvent evento) {
        retencionElegida = tablaRetenciones.getSelectionModel().getSelectedItem();
        if (evento.getClickCount() == 2 && retencionElegida != null) {
            editarRetencion();
        }
    }

    @FXML
    private void nuevoRetencion() {
        abrirFichaRetencion(null, "Alta de tipo de retención");
    }

    @FXML
    private void editarRetencion() {
        if (retencionElegida == null) {
            Dialogos.mostrarDialogoAdvertencia("Retención", "Seleccione un tipo de retención de la tabla.");
            return;
        }
        abrirFichaRetencion(retencionElegida, "Datos del tipo de retención");
    }

    @FXML
    private void eliminarRetencion() {
        if (retencionElegida == null) {
            Dialogos.mostrarDialogoAdvertencia("Retención", "Seleccione un tipo de retención de la tabla.");
            return;
        }
        if (!Dialogos.mostrarDialogoConfirmacion("Eliminar tipo de retención",
                "¿Eliminar definitivamente el tipo \"" + nz(retencionElegida.getNombre()) + "\"?")) {
            return;
        }
        try {
            Vista.getInstancia().getControlador().bajaTipoRetencion(retencionElegida.getId());
            refrescarRetenciones();
        } catch (Exception e) {
            Dialogos.mostrarDialogoError("Retención", e.getMessage());
        }
    }

    /**
     * Abrimos la ficha en modo añadir (registro null) o en modo editar, y
     * esperamos a que se cierre.
     */
    private void abrirFichaRetencion(TipoRetencion registro, String titulo) {
        try {
            FXMLLoader cargador = new FXMLLoader(LocalizadorRecursos.class.getResource("FichaTipoRetencion.fxml"));
            Parent raiz = cargador.load();
            FichaTipoRetencionController ficha = cargador.getController();
            ficha.setRegistro(registro);
            Stage modal = Vista.getInstancia().crearVentanaModal(raiz, titulo, ficha);
            modal.showAndWait();
            TipoRetencion guardado = ficha.getRegistro();
            if (guardado == null) {
                return;
            }
            if (registro == null) {
                guardado.setId(Vista.getInstancia().getControlador().altaTipoRetencion(guardado));
            } else {
                Vista.getInstancia().getControlador().modificarTipoRetencion(guardado);
            }
            refrescarRetenciones();
        } catch (Exception e) {
            Dialogos.mostrarDialogoError("Retención", "No se pudo guardar: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // Series
    // ------------------------------------------------------------------

    private void cargarSeries() {
        colSerieCodigo.setCellValueFactory(new PropertyValueFactory<>("codigoTexto"));
        colSerieDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        colSerieRectifica.setCellValueFactory(new PropertyValueFactory<>("rectificativaTexto"));
        colSerieFormato.setCellValueFactory(new PropertyValueFactory<>("formatoTexto"));
        colSerieSiguiente.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(siguienteTexto(c.getValue())));
        refrescarSeries();
    }

    private void refrescarSeries() {
        try {
            serieElegida = null;
            tablaSeries.getSelectionModel().clearSelection();
            colSerieSiguiente.setText("Siguiente (" + anioTrabajo() + ")");
            series.setAll(Vista.getInstancia().getControlador().listadoSeries());
            tablaSeries.setItems(series);
        } catch (Exception e) {
            Dialogos.mostrarDialogoError("Configuración", "No se pudieron cargar las series: " + e.getMessage());
        }
    }

    private int anioTrabajo() {
        return Vista.getInstancia().getControlador().getModelo().getReloj().fechaTrabajo().getYear();
    }

    /** El siguiente número de la serie para el año de trabajo, o vacío si no se puede calcular. */
    private String siguienteTexto(Serie serie) {
        try {
            LocalDate fecha = Vista.getInstancia().getControlador().getModelo().getReloj().fechaTrabajo();
            return String.valueOf(Vista.getInstancia().getControlador().siguienteCorrelativo(serie, fecha));
        } catch (Exception e) {
            return "";
        }
    }

    /** Guardamos la fila elegida; con doble clic la abrimos para editarla. */
    @FXML
    private void seleccionarSerie(MouseEvent evento) {
        serieElegida = tablaSeries.getSelectionModel().getSelectedItem();
        if (evento.getClickCount() == 2 && serieElegida != null) {
            editarSerie();
        }
    }

    @FXML
    private void nuevaSerie() {
        abrirFichaSerie(null, "Alta de serie");
    }

    @FXML
    private void editarSerie() {
        if (serieElegida == null) {
            Dialogos.mostrarDialogoAdvertencia("Series", "Seleccione una serie de la tabla.");
            return;
        }
        abrirFichaSerie(serieElegida, "Datos de la serie");
    }

    @FXML
    private void eliminarSerie() {
        if (serieElegida == null) {
            Dialogos.mostrarDialogoAdvertencia("Series", "Seleccione una serie de la tabla.");
            return;
        }
        if (!Dialogos.mostrarDialogoConfirmacion("Eliminar serie",
                "¿Eliminar definitivamente la serie \"" + serieElegida.getCodigoTexto() + "\"?")) {
            return;
        }
        try {
            Vista.getInstancia().getControlador().bajaSerie(serieElegida.getId());
            refrescarSeries();
        } catch (Exception e) {
            Dialogos.mostrarDialogoError("Series", e.getMessage());
        }
    }

    /**
     * Abrimos la ficha en modo añadir (registro null) o en modo editar, y
     * esperamos a que se cierre. Si el guardado falla (por ejemplo, por un
     * código duplicado), la volvemos a abrir con lo que había escrito para
     * corregirlo sin empezar de cero.
     */
    private void abrirFichaSerie(Serie registro, String titulo) {
        boolean esAlta = registro == null;
        Serie intento = registro;
        boolean reintentando = false;
        while (true) {
            Serie guardado = null;
            try {
                FXMLLoader cargador = new FXMLLoader(LocalizadorRecursos.class.getResource("FichaSerie.fxml"));
                Parent raiz = cargador.load();
                FichaSerieController ficha = cargador.getController();
                if (reintentando) {
                    ficha.reintentarCon(intento, esAlta);
                } else {
                    ficha.setRegistro(intento);
                }
                Stage modal = Vista.getInstancia().crearVentanaModal(raiz, titulo, ficha);
                modal.showAndWait();
                guardado = ficha.getRegistro();
                if (guardado == null) {
                    return;
                }
                if (esAlta) {
                    guardado.setId(Vista.getInstancia().getControlador().altaSerie(guardado));
                } else {
                    Vista.getInstancia().getControlador().modificarSerie(guardado);
                }
                refrescarSeries();
                return;
            } catch (Exception e) {
                Dialogos.mostrarDialogoError("Series", "No se pudo guardar: " + e.getMessage());
                if (guardado == null) {
                    return;
                }
                intento = guardado;
                reintentando = true;
            }
        }
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
