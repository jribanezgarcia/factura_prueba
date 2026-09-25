package cabofactu.vista.controlador;

import cabofactu.modelo.dominio.Cliente;
import cabofactu.modelo.dominio.Empresa;
import cabofactu.modelo.dominio.EstadoFactura;
import cabofactu.modelo.dominio.Factura;
import cabofactu.modelo.dominio.GrupoIva;
import cabofactu.modelo.dominio.LineaFactura;
import cabofactu.modelo.dominio.ResumenFactura;
import cabofactu.modelo.dominio.Serie;
import cabofactu.modelo.dominio.TipoIva;
import cabofactu.modelo.dominio.TipoRetencion;
import cabofactu.modelo.negocio.Calculos;
import cabofactu.modelo.negocio.sqlite.Conexion;
import cabofactu.pdf.ExportadorPdf;
import cabofactu.utilidades.Formatos;
import cabofactu.utilidades.LogoMarco;
import cabofactu.vista.Pantalla;
import cabofactu.vista.Vista;
import cabofactu.vista.utilidades.CambiosSinGuardar;
import cabofactu.vista.utilidades.ConversorCliente;
import cabofactu.vista.utilidades.Dialogos;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
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
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
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
 * Editor de factura: cabecera con serie, fecha, número y cliente; tabla de
 * líneas editable; descuento general y desglose por tipos de IVA;
 * observaciones; validaciones al guardar; estados Emitida/Anulada; creación
 * de rectificativas y exportación a PDF.
 */
public class EditorController implements Pantalla, Initializable {

    private static final String PREF_SERIE = "ultima_serie";
    private static final String PREF_CARPETA = "carpeta_facturas";
    private static final String PREF_EXPORTACION = "ultima_carpeta_export";

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
    private TableView<GrupoIva> matrizIva;
    @FXML
    private TableColumn<GrupoIva, String> colMatrizIva;
    @FXML
    private TableColumn<GrupoIva, String> colMatrizBase;
    @FXML
    private TableColumn<GrupoIva, String> colMatrizCuota;
    @FXML
    private HBox filaSuplidos;
    @FXML
    private Label lblSuplidos;
    @FXML
    private ImageView logo;
    @FXML
    private StackPane logoBox;
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
    private TableColumn<LineaFactura, String> colIva;
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

    private boolean modificado;
    private boolean cargando;
    private Long facturaAbiertaId;
    private Integer correlativoFijo;
    private EstadoFactura estadoActual;
    private Cliente clienteActual;
    private int descuento;
    private String numeroPropuesto = "";

    private final ObservableList<LineaFactura> lineas = FXCollections.observableArrayList();
    private final ObservableList<TipoIva> tiposIva = FXCollections.observableArrayList();
    private final ObservableList<TipoRetencion> tiposRetencion = FXCollections.observableArrayList();
    private TipoRetencion retencionActual;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cargando = true;
        cargarLogo();
        cargarSeries();
        ponerFechaInicial();
        cargarTiposIva();
        cargarRetenciones();
        prepararBuscadorCliente();
        prepararTablaLineas();
        prepararMatriz();
        vigilarCambios();
        empezarFacturaNueva();
        cargando = false;
        actualizarTotales();
    }

    @Override
    public void alMostrar() {
        ponerAtajos();
    }

    /** Cargamos la factura indicada en el editor, para verla, editarla o exportarla. */
    public void cargarFactura(long facturaId) {
        Factura factura = null;
        try {
            factura = Vista.getInstancia().getControlador().buscarFactura(facturaId);
        } catch (Exception e) {
            Dialogos.mostrarDialogoError("Factura", e.getMessage());
            return;
        }
        if (factura == null) {
            Dialogos.mostrarDialogoError("Factura", "No se pudo abrir la factura.");
            return;
        }
        cargando = true;
        try {
            facturaAbiertaId = factura.getId();
            correlativoFijo = factura.getCorrelativo();
            estadoActual = factura.getEstado();
            comboSerie.setValue(factura.getSerie());
            actualizarVisibilidadReferencia(factura.getSerie());
            fecha.setValue(factura.getFecha());
            txtNumero.setText(factura.getNumero());
            numeroPropuesto = txtNumero.getText();
            Cliente cli = factura.getCliente();
            clienteActual = cli;
            cargarDatosCliente(cli);
            lineas.setAll(factura.getLineas());
            asegurarTiposIvaEnLista();
            descuento = factura.getDescuento();
            txtDescuento.setText(String.valueOf(descuento));
            colocarRetencionDeFactura(factura);
            txtObservaciones.setText(textoSinNulo(factura.getObservaciones()));
            txtReferencia.setText(textoSinNulo(factura.getRectificaNumero()));
            txtReferencia.setEditable(false);
            txtFormaPago.setText(textoSinNulo(factura.getFormaPago()));
            vencimiento.setValue(factura.getVencimiento());
            txtRealizadaPor.setText(textoSinNulo(factura.getRealizadaPor()));
            lblTitulo.setText("Factura " + factura.getNumero());
            modificado = false;
        } finally {
            cargando = false;
        }
        aplicarEstado();
        actualizarTotales();
    }

    @Override
    public boolean puedeCerrar() {
        if (!modificado) {
            return true;
        }
        CambiosSinGuardar respuesta = Dialogos.mostrarDialogoCambiosSinGuardar();
        if (respuesta == CambiosSinGuardar.GUARDAR) {
            return guardar();
        }
        if (respuesta == CambiosSinGuardar.DESCARTAR) {
            return true;
        }
        return false;
    }

    @FXML
    private boolean guardar() {
        Cliente cliente = comprobarCliente();
        if (cliente == null) {
            return false;
        }
        LocalDate dia = comprobarFecha();
        if (dia == null) {
            return false;
        }
        List<LineaFactura> guardables = lineasGuardables();
        if (guardables.isEmpty()) {
            Dialogos.mostrarDialogoError("Guardar", "La factura debe tener al menos una línea con contenido.");
            return false;
        }
        if (facturaAbiertaId != null && estadoActual != EstadoFactura.EMITIDA) {
            Dialogos.mostrarDialogoInformacion("Guardar", "Una factura anulada no se puede editar.");
            return false;
        }
        if (!confirmarCliente(cliente)) {
            return false;
        }
        boolean actualizarFicha = pedirActualizarFicha(cliente);
        boolean hecho;
        if (facturaAbiertaId == null) {
            hecho = guardarNueva(cliente, dia, guardables);
        } else {
            hecho = guardarEmitida(cliente, dia, guardables);
        }
        if (!hecho) {
            return false;
        }
        if (actualizarFicha) {
            actualizarFichaCliente(cliente);
        }
        return true;
    }

    @FXML
    private void nuevaFactura() {
        Vista.getInstancia().mostrar("Editor.fxml");
    }

    @FXML
    private void exportarPdf() {
        if (facturaAbiertaId == null) {
            Dialogos.mostrarDialogoInformacion("Exportar PDF", "Guarde primero la factura para poder exportarla.");
            return;
        }
        Factura factura = null;
        try {
            factura = Vista.getInstancia().getControlador().buscarFactura(facturaAbiertaId);
        } catch (Exception e) {
            Dialogos.mostrarDialogoError("Exportar PDF", e.getMessage());
            return;
        }
        if (factura == null) {
            return;
        }
        Empresa empresa = null;
        try {
            empresa = Vista.getInstancia().getControlador().buscarEmpresa();
        } catch (Exception e) {
            Dialogos.mostrarDialogoError("Exportar PDF", e.getMessage());
            return;
        }
        Path sugerido = proponerDestinoPdf(factura);
        FileChooser elegidor = new FileChooser();
        elegidor.setTitle("Exportar PDF");
        if (sugerido.getParent() != null && sugerido.getParent().toFile().exists()) {
            elegidor.setInitialDirectory(sugerido.getParent().toFile());
        }
        elegidor.setInitialFileName(sugerido.getFileName().toString());
        File elegido = elegidor.showSaveDialog(Vista.getInstancia().getVentana());
        if (elegido == null) {
            return;
        }
        Path ruta = elegido.toPath();
        String color = colorPdfPreferido();
        Vista.getInstancia().getVentana().getScene().setCursor(Cursor.WAIT);
        try {
            new ExportadorPdf().exportar(factura, empresa, ruta, color);
            guardarPreferenciaCarpeta(ruta);
            Dialogos.mostrarDialogoInformacion("Exportar PDF", "PDF generado en:\n" + ruta);
        } catch (Exception e) {
            Dialogos.mostrarDialogoError("Exportar PDF", e.getMessage());
        } finally {
            Vista.getInstancia().getVentana().getScene().setCursor(Cursor.DEFAULT);
        }
    }

    @FXML
    private void crearRectificativa() {
        if (facturaAbiertaId == null) {
            Dialogos.mostrarDialogoInformacion("Rectificativa", "Abra primero la factura a rectificar.");
            return;
        }
        if (modificado) {
            boolean seguir = Dialogos.mostrarDialogoConfirmacion("Cambios sin guardar",
                    "Hay cambios sin guardar que se descartarán. ¿Continuar?");
            if (!seguir) {
                return;
            }
        }
        try {
            long nueva = Vista.getInstancia().getControlador().rectificarFactura(facturaAbiertaId,
                    Vista.getInstancia().getControlador().fechaTrabajo());
            cargarFactura(nueva);
            Dialogos.mostrarDialogoInformacion("Rectificativa", "Rectificativa creada.");
        } catch (Exception e) {
            Dialogos.mostrarDialogoError("Rectificativa", e.getMessage());
        }
    }

    @FXML
    private void anular() {
        if (facturaAbiertaId == null) {
            return;
        }
        if (modificado) {
            boolean seguir = Dialogos.mostrarDialogoConfirmacion("Cambios sin guardar",
                    "Hay cambios sin guardar que se descartarán. ¿Continuar?");
            if (!seguir) {
                return;
            }
        }
        boolean confirmar = Dialogos.mostrarDialogoConfirmacion("Anular factura", "¿Anular la factura?");
        if (!confirmar) {
            return;
        }
        try {
            Vista.getInstancia().getControlador().anularFactura(facturaAbiertaId);
            Dialogos.mostrarDialogoInformacion("Anular", "Factura anulada.");
            cargarFactura(facturaAbiertaId);
        } catch (Exception e) {
            Dialogos.mostrarDialogoError("Anular", e.getMessage());
        }
    }

    @FXML
    private void restaurar() {
        if (facturaAbiertaId == null) {
            return;
        }
        boolean confirmar = Dialogos.mostrarDialogoConfirmacion("Restaurar factura",
                "¿Restaurar la factura a estado Emitida?");
        if (!confirmar) {
            return;
        }
        try {
            Vista.getInstancia().getControlador().restaurarFactura(facturaAbiertaId);
            Dialogos.mostrarDialogoInformacion("Restaurar", "Factura restaurada.");
            cargarFactura(facturaAbiertaId);
        } catch (Exception e) {
            Dialogos.mostrarDialogoError("Restaurar", e.getMessage());
        }
    }

    @FXML
    private void volver() {
        Vista.getInstancia().mostrar("MenuPrincipal.fxml");
    }

    @FXML
    private void anadirLinea() {
        LineaFactura nueva = nuevaLinea();
        if (nueva == null) {
            return;
        }
        lineas.add(nueva);
        tablaLineas.refresh();
        actualizarTotales();
        cambiado();
        int fila = lineas.size() - 1;
        // La tabla termina de insertar la fila después del pulso actual; abrimos el editor en el siguiente.
        Platform.runLater(() -> editarCelda(fila, colCantidad));
    }

    @FXML
    private void eliminarLinea() {
        LineaFactura elegida = tablaLineas.getSelectionModel().getSelectedItem();
        if (elegida == null) {
            return;
        }
        lineas.remove(elegida);
        if (lineas.isEmpty()) {
            LineaFactura nueva = nuevaLinea();
            if (nueva != null) {
                lineas.add(nueva);
            }
        }
        tablaLineas.refresh();
        actualizarTotales();
        cambiado();
    }

    private Cliente comprobarCliente() {
        String primero = marcarCamposMalos();
        if (todosVacios()) {
            Dialogos.mostrarDialogoError("Datos del cliente", "Indique los datos del cliente.");
            return null;
        }
        if (primero != null) {
            Dialogos.mostrarDialogoError("Datos del cliente", primero);
            return null;
        }
        try {
            return clienteDeFormulario();
        } catch (Exception e) {
            Dialogos.mostrarDialogoError("Datos del cliente", e.getMessage());
            return null;
        }
    }

    private LocalDate comprobarFecha() {
        LocalDate dia = fecha.getValue();
        if (dia == null) {
            Dialogos.mostrarDialogoError("Guardar", "Indique la fecha de la factura.");
            return null;
        }
        return dia;
    }

    /**
     * Confirmamos el cliente antes de guardar: si su NIF no está en la lista,
     * avisamos de que se guardará junto con la factura. Si se cancela, no se
     * guarda nada y el editor sigue como estaba.
     */
    private boolean confirmarCliente(Cliente cliente) {
        if (cliente.getId() != null) {
            return true;
        }
        Cliente existente = null;
        try {
            existente = Vista.getInstancia().getControlador().buscarClientePorNif(cliente.getNif());
        } catch (Exception e) {
            Dialogos.mostrarDialogoError("Datos del cliente", e.getMessage());
            return false;
        }
        if (existente != null) {
            cliente.setId(existente.getId());
            return true;
        }
        boolean aceptar = Dialogos.mostrarDialogoConfirmacion("Cliente nuevo",
                String.format("El cliente %s (%s) no está en tu lista.%n%nSe guardará en ella junto con la factura.",
                        cliente.getNombre(), cliente.getNif()));
        return aceptar;
    }

    /**
     * Preguntamos si actualizamos su ficha, solo cuando el cliente es el mismo
     * que elegimos y hemos tocado algún dato. La factura se guarda con los
     * datos nuevos en cualquier caso.
     */
    private boolean pedirActualizarFicha(Cliente cliente) {
        if (cliente.getId() == null) {
            return false;
        }
        if (clienteActual == null) {
            return false;
        }
        if (!cliente.getId().equals(clienteActual.getId())) {
            return false;
        }
        if (cliente.tieneLosMismosDatos(clienteActual)) {
            return false;
        }
        return Dialogos.mostrarDialogoConfirmacion("Datos del cliente",
                "Has cambiado los datos de «" + clienteActual.getNombre() + "» en esta factura.\n\n"
                        + "¿Quieres guardar también esos cambios en su ficha de cliente?");
    }

    private boolean guardarNueva(Cliente cliente, LocalDate dia, List<LineaFactura> guardables) {
        Serie serie = comboSerie.getValue();
        if (serie == null) {
            Dialogos.mostrarDialogoError("Guardar", "Seleccione la serie.");
            return false;
        }
        Integer libre = numeroLibre(serie, dia);
        String propuesto = txtNumero.getText().trim();
        boolean esPropuesto = propuesto.equals(numeroPropuesto);
        if (libre != null && esPropuesto) {
            String textoLibre = Vista.getInstancia().getControlador().formarNumero(serie, libre, dia);
            boolean usar = Dialogos.mostrarDialogoNumeroLibre(textoLibre, propuesto);
            if (usar) {
                txtNumero.setText(textoLibre);
            }
        }
        Integer correlativo = Vista.getInstancia().getControlador().parseCorrelativo(serie, txtNumero.getText());
        if (correlativo == null) {
            Dialogos.mostrarDialogoError("Guardar", String.format("El número no se ajusta al formato de la serie %s (p. ej. %s-1).", serie.getCodigo(), serie.getCodigo()));
            return false;
        }
        try {
            Factura nueva = new Factura(serie, dia, cliente);
            nueva.setCorrelativo(correlativo);
            nueva.setDescuento(descuento);
            nueva.setObservaciones(txtObservaciones.getText());
            nueva.setFormaPago(txtFormaPago.getText().trim());
            nueva.setVencimiento(vencimiento.getValue());
            nueva.setRealizadaPor(txtRealizadaPor.getText().trim());
            nueva.setRetencion(retencionActual);
            nueva.setLineas(guardables);
            long id = Vista.getInstancia().getControlador().altaFactura(nueva);
            guardarSeriePreferida(serie);
            cargarFactura(id);
            Dialogos.mostrarDialogoInformacion("Guardar", "Factura guardada.");
            return true;
        } catch (Exception e) {
            Dialogos.mostrarDialogoError("Guardar", e.getMessage());
            return false;
        }
    }

    private boolean guardarEmitida(Cliente cliente, LocalDate dia, List<LineaFactura> guardables) {
        Factura factura = null;
        try {
            factura = Vista.getInstancia().getControlador().buscarFactura(facturaAbiertaId);
        } catch (Exception e) {
            Dialogos.mostrarDialogoError("Guardar", e.getMessage());
            return false;
        }
        if (factura == null) {
            Dialogos.mostrarDialogoError("Guardar", "No se ha encontrado la factura.");
            return false;
        }
        try {
            factura.setFecha(dia);
            factura.setCliente(cliente);
            factura.setDescuento(descuento);
        } catch (Exception e) {
            Dialogos.mostrarDialogoError("Guardar", e.getMessage());
            return false;
        }
        factura.setObservaciones(txtObservaciones.getText());
        factura.setFormaPago(txtFormaPago.getText().trim());
        factura.setVencimiento(vencimiento.getValue());
        factura.setRealizadaPor(txtRealizadaPor.getText().trim());
        factura.setRetencion(retencionActual);
        factura.setLineas(guardables);
        boolean confirmar = Dialogos.mostrarDialogoConfirmacion("Guardar factura",
                String.format("¿Guardar los cambios de la factura %s?%n%nLa factura ya emitida se sobrescribirá.",
                        factura.getNumero()));
        if (!confirmar) {
            return false;
        }
        try {
            Vista.getInstancia().getControlador().modificarFactura(factura);
            cargarFactura(facturaAbiertaId);
            Dialogos.mostrarDialogoInformacion("Guardar", "Factura guardada.");
            return true;
        } catch (Exception e) {
            Dialogos.mostrarDialogoError("Guardar", e.getMessage());
            return false;
        }
    }

    private void actualizarFichaCliente(Cliente cliente) {
        try {
            Vista.getInstancia().getControlador().modificarCliente(cliente);
            clienteActual = cliente;
        } catch (Exception e) {
            Dialogos.mostrarDialogoError("Datos del cliente", e.getMessage());
        }
    }

    private void prepararBuscadorCliente() {
        comboCliente.setEditable(true);
        comboCliente.setConverter(new ConversorCliente(comboCliente));
        comboCliente.getEditor().textProperty().addListener((propiedad, anterior, nuevo) -> textoBuscador(nuevo));
        comboCliente.setOnShowing(evento -> mostrarDesplegable());
        comboCliente.setOnAction(evento -> elegirDeBuscador());
    }

    private void textoBuscador(String nuevo) {
        if (cargando) {
            return;
        }
        if (comboCliente.getValue() != null) {
            String mostrado = comboCliente.getValue().toString();
            if (mostrado != null && mostrado.equals(nuevo)) {
                return;
            }
        }
        String texto = "";
        if (nuevo != null) {
            texto = nuevo.trim();
        }
        if (texto.isEmpty()) {
            comboCliente.getItems().clear();
            return;
        }
        buscarClientes(texto);
    }

    private void buscarClientes(String texto) {
        try {
            comboCliente.getItems().setAll(
                    Vista.getInstancia().getControlador().listadoClientes(texto, true));
        } catch (Exception e) {
            Dialogos.mostrarDialogoError("Cliente", e.getMessage());
        }
    }

    private void mostrarDesplegable() {
        if (cargando) {
            return;
        }
        try {
            String texto = comboCliente.getEditor().getText();
            if (texto == null || texto.isBlank()) {
                comboCliente.getItems().setAll(
                        Vista.getInstancia().getControlador().listadoClientes(true));
            } else {
                comboCliente.getItems().setAll(
                        Vista.getInstancia().getControlador().listadoClientes(texto.trim(), true));
            }
        } catch (Exception e) {
            Dialogos.mostrarDialogoError("Cliente", e.getMessage());
        }
    }

    private void elegirDeBuscador() {
        if (cargando) {
            return;
        }
        Cliente elegido = null;
        Object valor = comboCliente.getValue();
        if (valor instanceof Cliente) {
            elegido = (Cliente) valor;
        } else {
            elegido = clientePorTexto(comboCliente.getEditor().getText());
        }
        if (elegido != null) {
            cargarDatosCliente(elegido);
            clienteActual = elegido;
            cambiado();
        }
    }

    private Cliente clientePorTexto(String texto) {
        if (texto == null) {
            return null;
        }
        String limpio = texto.trim();
        if (limpio.isEmpty()) {
            return null;
        }
        for (Cliente cliente : comboCliente.getItems()) {
            if (cliente.getNombre() != null && cliente.getNombre().equalsIgnoreCase(limpio)) {
                return cliente;
            }
            if (cliente.getNif() != null && cliente.getNif().equalsIgnoreCase(limpio)) {
                return cliente;
            }
        }
        return null;
    }

    private void cargarDatosCliente(Cliente cliente) {
        clienteActual = cliente;
        if (cliente == null) {
            cliNombre.setText("");
            cliNif.setText("");
            cliDireccion.setText("");
            cliCp.setText("");
            cliLocalidad.setText("");
            cliProvincia.setText("");
            cliEmail.setText("");
        } else {
            cliNombre.setText(textoSinNulo(cliente.getNombre()));
            cliNif.setText(textoSinNulo(cliente.getNif()));
            cliDireccion.setText(textoSinNulo(cliente.getDireccion()));
            cliCp.setText(textoSinNulo(cliente.getCp()));
            cliLocalidad.setText(textoSinNulo(cliente.getLocalidad()));
            cliProvincia.setText(textoSinNulo(cliente.getProvincia()));
            cliEmail.setText(textoSinNulo(cliente.getEmail()));
        }
        quitarMarcas();
    }

    private void quitarMarcas() {
        cliNombre.getStyleClass().remove("campo-error");
        cliNif.getStyleClass().remove("campo-error");
        cliDireccion.getStyleClass().remove("campo-error");
        cliCp.getStyleClass().remove("campo-error");
        cliLocalidad.getStyleClass().remove("campo-error");
        cliProvincia.getStyleClass().remove("campo-error");
        cliEmail.getStyleClass().remove("campo-error");
    }

    /**
     * Marcamos en rojo todos los campos incorrectos y devolvemos el mensaje del
     * primero, o null si está todo bien.
     */
    private String marcarCamposMalos() {
        quitarMarcas();
        String primero = null;
        primero = revisarCampo(cliNombre, Cliente.errorNombre(cliNombre.getText().trim()), primero);
        primero = revisarCampo(cliNif, Cliente.errorNif(cliNif.getText().trim()), primero);
        primero = revisarCampo(cliDireccion, Cliente.errorDireccion(cliDireccion.getText().trim()), primero);
        primero = revisarCampo(cliCp, Cliente.errorCp(cliCp.getText().trim()), primero);
        primero = revisarCampo(cliLocalidad, Cliente.errorLocalidad(cliLocalidad.getText().trim()), primero);
        primero = revisarCampo(cliProvincia, Cliente.errorProvincia(cliProvincia.getText().trim()), primero);
        primero = revisarCampo(cliEmail, Cliente.errorEmail(cliEmail.getText().trim()), primero);
        return primero;
    }

    private String revisarCampo(TextField campo, String error, String primero) {
        if (error == null) {
            return primero;
        }
        if (!campo.getStyleClass().contains("campo-error")) {
            campo.getStyleClass().add("campo-error");
        }
        if (primero == null) {
            return error;
        }
        return primero;
    }

    private boolean todosVacios() {
        String todo = cliNombre.getText().trim() + cliNif.getText().trim() + cliDireccion.getText().trim()
                + cliCp.getText().trim() + cliLocalidad.getText().trim() + cliProvincia.getText().trim()
                + cliEmail.getText().trim();
        return todo.isBlank();
    }

    /**
     * El NIF identifica al cliente de la factura: si es el mismo que el del
     * cliente elegido, conservamos su id; si no, se queda sin id y al guardar
     * veremos si ya está en la lista o es nuevo.
     */
    private Cliente clienteDeFormulario() throws Exception {
        String nombre = cliNombre.getText().trim();
        String nif = cliNif.getText().trim();
        String direccion = cliDireccion.getText().trim();
        String cp = cliCp.getText().trim();
        String localidad = cliLocalidad.getText().trim();
        String provincia = cliProvincia.getText().trim();
        String email = cliEmail.getText().trim();
        Cliente cliente = new Cliente(nombre, nif, direccion, cp, localidad, provincia);
        cliente.setEmail(email);
        if (clienteActual != null && cliente.getNif().equals(clienteActual.getNif())) {
            cliente.setId(clienteActual.getId());
            cliente.setActivo(clienteActual.isActivo());
        }
        return cliente;
    }

    private void recalcularNumero() {
        Serie serie = comboSerie.getValue();
        LocalDate dia = fecha.getValue();
        if (serie == null) {
            return;
        }
        if (facturaAbiertaId != null && correlativoFijo != null && dia != null) {
            txtNumero.setText(Vista.getInstancia().getControlador().formarNumero(serie, correlativoFijo, dia));
        } else if (dia != null) {
            try {
                int correlativo = Vista.getInstancia().getControlador().siguienteCorrelativo(serie, dia);
                txtNumero.setText(Vista.getInstancia().getControlador().formarNumero(serie, correlativo, dia));
            } catch (Exception e) {
                txtNumero.setText("");
            }
        }
        numeroPropuesto = txtNumero.getText();
    }

    private Integer numeroLibre(Serie serie, LocalDate dia) {
        List<Integer> huecos = null;
        try {
            huecos = Vista.getInstancia().getControlador().huecosDeSerie(serie, dia);
        } catch (Exception e) {
            return null;
        }
        if (huecos == null || huecos.isEmpty()) {
            return null;
        }
        return huecos.get(0);
    }

    private void guardarSeriePreferida(Serie serie) {
        try {
            if (serie != null) {
                Vista.getInstancia().getControlador().guardarPreferencia(PREF_SERIE, serie.getCodigo());
            }
        } catch (Exception e) {
            return;
        }
    }

    private void actualizarVisibilidadReferencia(Serie serie) {
        boolean rectificativa = serie != null && serie.isEsRectificativa();
        lblReferencia.setVisible(rectificativa);
        lblReferencia.setManaged(rectificativa);
        txtReferencia.setVisible(rectificativa);
        txtReferencia.setManaged(rectificativa);
    }

    private void prepararTablaLineas() {
        tablaLineas.setEditable(true);
        tablaLineas.setItems(lineas);
        tablaLineas.setPlaceholder(new Label("Sin líneas."));
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidadTexto"));
        colDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precioUnitarioTexto"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("totalBaseTexto"));
        colIva.setCellValueFactory(new PropertyValueFactory<>("ivaNombre"));
        colCantidad.setCellFactory(columna -> new CeldaCantidad());
        colDescripcion.setCellFactory(columna -> new CeldaDescripcion());
        colPrecio.setCellFactory(columna -> new CeldaPrecio());
        colTotal.setCellFactory(columna -> new CeldaTotal());
        colIva.setCellFactory(columna -> new CeldaIva());
        tablaLineas.setOnKeyPressed(evento -> teclaEnTabla(evento));
    }

    private LineaFactura nuevaLinea() {
        LineaFactura hecha = null;
        try {
            hecha = new LineaFactura(1, BigDecimal.ZERO);
        } catch (Exception e) {
            return null;
        }
        TipoIva defecto = tipoIvaDefault();
        if (defecto != null) {
            hecha.setTipoIva(defecto);
        }
        return hecha;
    }

    private TipoIva tipoIvaDefault() {
        for (TipoIva tipo : tiposIva) {
            if (tipo.getPorcentaje() != null && tipo.getPorcentaje() == 21) {
                return tipo;
            }
        }
        for (TipoIva tipo : tiposIva) {
            if (tipo.getPorcentaje() != null) {
                return tipo;
            }
        }
        if (tiposIva.isEmpty()) {
            return null;
        }
        return tiposIva.get(0);
    }

    private TipoIva tipoIvaDe(LineaFactura linea) {
        if (linea == null) {
            return null;
        }
        if (linea.getTipoIvaId() != null) {
            for (TipoIva tipo : tiposIva) {
                if (tipo.getId() != null && tipo.getId().equals(linea.getTipoIvaId())) {
                    return tipo;
                }
            }
        }
        if (linea.getIvaPorcentaje() != null) {
            for (TipoIva tipo : tiposIva) {
                if (linea.getIvaPorcentaje().equals(tipo.getPorcentaje())) {
                    return tipo;
                }
            }
        }
        return null;
    }

    private LineaFactura filaDe(TableCell<LineaFactura, ?> celda) {
        TableRow<LineaFactura> fila = celda.getTableRow();
        if (fila == null) {
            return null;
        }
        return fila.getItem();
    }

    private void avanzarDesde(TablePosition posicion) {
        if (posicion == null) {
            return;
        }
        int fila = posicion.getRow();
        int columna = posicion.getColumn();
        if (fila < 0 || fila >= lineas.size()) {
            return;
        }
        int destinoFila = fila;
        int destinoColumna = columna + 1;
        if (destinoColumna > 3) {
            destinoColumna = 0;
            destinoFila = fila + 1;
            if (destinoFila >= lineas.size()) {
                if (lineas.get(fila).tieneContenido()) {
                    LineaFactura nueva = nuevaLinea();
                    if (nueva == null) {
                        return;
                    }
                    lineas.add(nueva);
                    destinoFila = lineas.size() - 1;
                } else {
                    destinoFila = fila;
                }
            }
        }
        int filaFinal = destinoFila;
        TableColumn<LineaFactura, ?> columnaFinal = tablaLineas.getColumns().get(destinoColumna);
        // La tabla termina de cerrar la celda anterior después de commitEdit,
        // así que abrimos la siguiente en el siguiente pulso.
        Platform.runLater(() -> editarCelda(filaFinal, columnaFinal));
    }

    private void editarCelda(int fila, TableColumn<LineaFactura, ?> columna) {
        tablaLineas.scrollTo(fila);
        tablaLineas.edit(fila, columna);
    }

    private void teclaEnTabla(KeyEvent evento) {
        if (evento.getCode() == KeyCode.DELETE) {
            eliminarLinea();
            evento.consume();
            return;
        }
        if (evento.getCode() == KeyCode.ENTER) {
            TablePosition<LineaFactura, ?> celda = tablaLineas.getFocusModel().getFocusedCell();
            if (celda != null && celda.getRow() >= 0 && tablaLineas.getEditingCell() == null) {
                TableColumn<LineaFactura, ?> columna = celda.getTableColumn();
                if (columna != null && columna.isEditable()) {
                    tablaLineas.edit(celda.getRow(), columna);
                }
            }
            evento.consume();
        }
    }

    private List<LineaFactura> lineasGuardables() {
        List<LineaFactura> guardables = new ArrayList<>();
        for (LineaFactura linea : lineas) {
            if (linea.tieneContenido()) {
                guardables.add(linea);
            }
        }
        return guardables;
    }

    private void prepararMatriz() {
        matrizIva.setPlaceholder(new Label("Sin desglose."));
        matrizIva.setFocusTraversable(false);
        matrizIva.setMouseTransparent(true);
        matrizIva.setFixedCellSize(22);
        colMatrizIva.setCellValueFactory(new PropertyValueFactory<>("etiqueta"));
        colMatrizBase.setCellValueFactory(new PropertyValueFactory<>("baseTexto"));
        colMatrizCuota.setCellValueFactory(new PropertyValueFactory<>("cuotaTexto"));
    }

    private void actualizarTotales() {
        ResumenFactura resumen = Calculos.resumen(lineas, descuento, retencionActual);
        boolean conDescuento = resumen.getImporteDescuento() != null
                && resumen.getImporteDescuento().compareTo(BigDecimal.ZERO) > 0;
        filaBaseBruta.setVisible(conDescuento);
        filaBaseBruta.setManaged(conDescuento);
        filaDescuento.setVisible(conDescuento);
        filaDescuento.setManaged(conDescuento);
        ponerPrimera(filaBaseBruta, conDescuento);
        ponerPrimera(filaBaseImponible, !conDescuento);
        if (conDescuento) {
            lblBaseBruta.setText(Formatos.moneda(resumen.getBaseBruta()));
            lblDescuentoNombre.setText("Descuento " + descuento + "%");
            lblDescuentoImporte.setText("-" + Formatos.moneda(resumen.getImporteDescuento()));
        }
        lblBaseTotal.setText(Formatos.moneda(resumen.getBaseTotal()));
        lblIvaTotal.setText(Formatos.moneda(resumen.getIvaTotal()));
        boolean conSuplidos = resumen.getTotalSuplidos() != null && resumen.getTotalSuplidos().compareTo(BigDecimal.ZERO) > 0;
        filaSuplidos.setVisible(conSuplidos);
        filaSuplidos.setManaged(conSuplidos);
        if (conSuplidos) {
            lblSuplidos.setText(Formatos.moneda(resumen.getTotalSuplidos()));
        }
        boolean conRetencion = resumen.getImporteRetencion() != null
                && resumen.getImporteRetencion().compareTo(BigDecimal.ZERO) > 0;
        filaRetencion.setVisible(conRetencion);
        filaRetencion.setManaged(conRetencion);
        if (conRetencion) {
            lblRetencionNombre.setText(nombreRetencion(resumen));
            lblRetencionImporte.setText("-" + Formatos.moneda(resumen.getImporteRetencion()));
        }
        lblTotal.setText(Formatos.moneda(resumen.getTotal()));
        ObservableList<GrupoIva> filas = FXCollections.observableArrayList(resumen.getGrupos());
        filas.add(resumen.getFilaTotales());
        matrizIva.setItems(filas);
        matrizIva.setPrefHeight(26 + 22 * filas.size() + 2);
    }

    private String nombreRetencion(ResumenFactura resumen) {
        if (resumen.getNombreRetencion() != null && !resumen.getNombreRetencion().isBlank()) {
            return resumen.getNombreRetencion();
        }
        return "Retención " + resumen.getPorcentajeRetencion() + "%";
    }

    private void ponerPrimera(HBox fila, boolean primera) {
        if (primera) {
            if (!fila.getStyleClass().contains("total-fila-primera")) {
                fila.getStyleClass().add("total-fila-primera");
            }
            return;
        }
        fila.getStyleClass().remove("total-fila-primera");
    }

    private void cargarLogo() {
        try {
            Empresa empresa = Vista.getInstancia().getControlador().buscarEmpresa();
            String ruta = empresa.getLogoPath();
            if (ruta == null || ruta.isBlank()) {
                LogoMarco.limpiar(logoBox);
                return;
            }
            File visto = new File(ruta);
            if (!visto.exists()) {
                LogoMarco.limpiar(logoBox);
                return;
            }
            Image imagen = new Image(visto.toURI().toString());
            if (imagen.isError()) {
                LogoMarco.limpiar(logoBox);
                return;
            }
            logo.setImage(imagen);
            logo.setFitWidth(92);
            logo.setFitHeight(38);
            logo.setPreserveRatio(true);
            LogoMarco.aplicar(logoBox, imagen);
        } catch (Exception e) {
            return;
        }
    }

    private void cargarSeries() {
        List<Serie> series = null;
        try {
            series = Vista.getInstancia().getControlador().listadoSeries();
        } catch (Exception e) {
            Dialogos.mostrarDialogoError("Factura", e.getMessage());
            return;
        }
        comboSerie.getItems().setAll(series);
        Serie inicial = buscarSeriePreferida(series);
        if (inicial == null) {
            inicial = buscarSerieNormal(series);
        }
        if (inicial == null && !series.isEmpty()) {
            inicial = series.get(0);
        }
        comboSerie.setValue(inicial);
    }

    private Serie buscarSeriePreferida(List<Serie> series) {
        String ultima = "";
        try {
            ultima = Vista.getInstancia().getControlador().preferencia(PREF_SERIE);
        } catch (Exception e) {
            return null;
        }
        if (ultima == null) {
            return null;
        }
        for (Serie serie : series) {
            if (ultima.equals(serie.getCodigo())) {
                return serie;
            }
        }
        return null;
    }

    private Serie buscarSerieNormal(List<Serie> series) {
        for (Serie serie : series) {
            if (!serie.isEsRectificativa()) {
                return serie;
            }
        }
        return null;
    }

    private void ponerFechaInicial() {
        fecha.setValue(Vista.getInstancia().getControlador().fechaTrabajo());
    }

    private void cargarTiposIva() {
        try {
            tiposIva.setAll(Vista.getInstancia().getControlador().listadoTiposIva(true));
        } catch (Exception e) {
            tiposIva.clear();
        }
    }

    private void asegurarTiposIvaEnLista() {
        for (LineaFactura linea : lineas) {
            if (linea.getTipoIvaId() == null) {
                continue;
            }
            if (tipoIvaEnLista(linea.getTipoIvaId())) {
                continue;
            }
            traerTipoIva(linea);
        }
    }

    private boolean tipoIvaEnLista(Long id) {
        for (TipoIva tipo : tiposIva) {
            if (tipo.getId() != null && tipo.getId().equals(id)) {
                return true;
            }
        }
        return false;
    }

    private void traerTipoIva(LineaFactura linea) {
        TipoIva encontrado = null;
        try {
            encontrado = Vista.getInstancia().getControlador().buscarTipoIva(linea.getTipoIvaId());
        } catch (Exception e) {
            encontrado = null;
        }
        if (encontrado != null) {
            tiposIva.add(encontrado);
            return;
        }
        try {
            String nombre = linea.getIvaNombre();
            if (nombre == null || nombre.isBlank()) {
                nombre = "Tipo de IVA";
            }
            TipoIva copia = new TipoIva(nombre, linea.getIvaPorcentaje(), linea.isEsSuplido());
            copia.setId(linea.getTipoIvaId());
            copia.setMotivoExencion(linea.getIvaMotivoExencion());
            copia.setActivo(false);
            tiposIva.add(copia);
        } catch (Exception e) {
            return;
        }
    }

    private void cargarRetenciones() {
        try {
            List<TipoRetencion> activas = Vista.getInstancia().getControlador().listadoTiposRetencion(true);
            TipoRetencion ninguna = new TipoRetencion("Sin retención", 0);
            tiposRetencion.setAll(ninguna);
            tiposRetencion.addAll(activas);
            comboRetencion.setItems(tiposRetencion);
            comboRetencion.setValue(ninguna);
            retencionActual = null;
        } catch (Exception e) {
            tiposRetencion.clear();
            try {
                TipoRetencion ninguna = new TipoRetencion("Sin retención", 0);
                tiposRetencion.add(ninguna);
                comboRetencion.setItems(tiposRetencion);
                comboRetencion.setValue(ninguna);
                retencionActual = null;
            } catch (Exception error) {
                return;
            }
        }
    }

    private void colocarRetencionDeFactura(Factura factura) {
        if (factura.getRetencion() == null) {
            traerRetencion(null, null, null);
            elegirRetencion(null);
            return;
        }
        traerRetencion(factura.getRetencion().getId(), factura.getRetencion().getNombre(),
                factura.getRetencion().getPorcentaje());
        elegirRetencion(factura.getRetencion().getId());
    }

    private void traerRetencion(Long id, String nombre, Integer porcentaje) {
        if (id == null) {
            return;
        }
        for (TipoRetencion tipo : tiposRetencion) {
            if (id.equals(tipo.getId())) {
                return;
            }
        }
        try {
            String texto = nombre;
            if (texto == null || texto.isBlank()) {
                texto = "Retención";
            }
            int tanto = 0;
            if (porcentaje != null) {
                tanto = porcentaje;
            }
            TipoRetencion copia = new TipoRetencion(texto, tanto);
            copia.setId(id);
            copia.setActivo(false);
            tiposRetencion.add(copia);
        } catch (Exception e) {
            return;
        }
    }

    private void elegirRetencion(Long id) {
        for (TipoRetencion tipo : tiposRetencion) {
            if (id == null && tipo.getId() == null) {
                comboRetencion.setValue(tipo);
                retencionActual = null;
                return;
            }
            if (id != null && id.equals(tipo.getId())) {
                comboRetencion.setValue(tipo);
                if (tipo.getId() == null) {
                    retencionActual = null;
                } else {
                    retencionActual = tipo;
                }
                return;
            }
        }
        if (tiposRetencion.isEmpty()) {
            return;
        }
        TipoRetencion primera = tiposRetencion.get(0);
        comboRetencion.setValue(primera);
        retencionActual = null;
    }

    private void vigilarCambios() {
        comboSerie.valueProperty().addListener((propiedad, anterior, nuevo) -> cambiarSerie(nuevo));
        fecha.valueProperty().addListener((propiedad, anterior, nuevo) -> cambiarFecha());
        txtNumero.textProperty().addListener((propiedad, anterior, nuevo) -> cambiado());
        txtDescuento.textProperty().addListener((propiedad, anterior, nuevo) -> cambiarDescuento(nuevo));
        comboRetencion.valueProperty().addListener((propiedad, anterior, nuevo) -> cambiarRetencion(nuevo));
        txtObservaciones.textProperty().addListener((propiedad, anterior, nuevo) -> ajustarObservaciones());
        txtFormaPago.textProperty().addListener((propiedad, anterior, nuevo) -> cambiado());
        vencimiento.valueProperty().addListener((propiedad, anterior, nuevo) -> cambiado());
        txtRealizadaPor.textProperty().addListener((propiedad, anterior, nuevo) -> cambiado());
        cliNombre.textProperty().addListener((propiedad, anterior, nuevo) -> cambiado());
        cliNif.textProperty().addListener((propiedad, anterior, nuevo) -> cambiado());
        cliDireccion.textProperty().addListener((propiedad, anterior, nuevo) -> cambiado());
        cliCp.textProperty().addListener((propiedad, anterior, nuevo) -> cambiado());
        cliLocalidad.textProperty().addListener((propiedad, anterior, nuevo) -> cambiado());
        cliProvincia.textProperty().addListener((propiedad, anterior, nuevo) -> cambiado());
        cliEmail.textProperty().addListener((propiedad, anterior, nuevo) -> cambiado());
    }

    private void cambiarSerie(Serie nuevo) {
        if (cargando) {
            return;
        }
        guardarSeriePreferida(nuevo);
        actualizarVisibilidadReferencia(nuevo);
        recalcularNumero();
        cambiado();
    }

    private void cambiarFecha() {
        if (cargando) {
            return;
        }
        recalcularNumero();
        cambiado();
    }

    private void cambiarDescuento(String nuevo) {
        if (cargando) {
            return;
        }
        Integer valor = parseEntero(nuevo);
        if (valor == null || valor < 0 || valor > 100) {
            return;
        }
        descuento = valor;
        actualizarTotales();
        cambiado();
    }

    private void cambiarRetencion(TipoRetencion nuevo) {
        if (cargando) {
            return;
        }
        if (nuevo == null || nuevo.getId() == null) {
            retencionActual = null;
        } else {
            retencionActual = nuevo;
        }
        actualizarTotales();
        cambiado();
    }

    private void ajustarObservaciones() {
        int parrafos = 1;
        if (txtObservaciones.getParagraphs() != null) {
            parrafos = txtObservaciones.getParagraphs().size();
        }
        if (parrafos < 1) {
            parrafos = 1;
        }
        if (parrafos > 3) {
            parrafos = 3;
        }
        txtObservaciones.setPrefRowCount(parrafos);
        cambiado();
    }

    private void cambiado() {
        if (!cargando) {
            modificado = true;
        }
    }

    /**
     * Dejamos el editor en un estado coherente: qué botones se ven, qué campos
     * se pueden tocar y si el número se puede escribir. Solo tocamos txtNumero
     * aquí. La llamamos al empezar una factura nueva y al abrir una emitida.
     */
    private void aplicarEstado() {
        boolean abierta = facturaAbiertaId != null;
        boolean emitida = abierta && estadoActual == EstadoFactura.EMITIDA;
        boolean anulada = abierta && estadoActual == EstadoFactura.ANULADA;
        boolean editable = !abierta || emitida;
        aplicarBotones(abierta, emitida, anulada);
        aplicarCampos(editable, abierta);
        boolean numeroBloqueado = abierta || !editable;
        txtNumero.setDisable(numeroBloqueado);
        btnExportar.setDisable(!abierta);
        btnRectificativa.setDisable(!abierta);
    }

    private void aplicarBotones(boolean abierta, boolean emitida, boolean anulada) {
        btnAnular.setVisible(emitida);
        btnAnular.setManaged(emitida);
        btnRestaurar.setVisible(anulada);
        btnRestaurar.setManaged(anulada);
        if (anulada) {
            lblTitulo.setMaxWidth(130);
        } else {
            lblTitulo.setMaxWidth(200);
        }
        lblEstado.setVisible(anulada);
        lblEstado.setManaged(anulada);
    }

    private void aplicarCampos(boolean editable, boolean abierta) {
        btnGuardar.setDisable(!editable);
        btnAnadirLinea.setDisable(!editable);
        btnEliminarLinea.setDisable(!editable);
        comboSerie.setDisable(abierta);
        fecha.setDisable(!editable);
        comboCliente.setDisable(!editable);
        cliNombre.setDisable(!editable);
        cliNif.setDisable(!editable);
        cliDireccion.setDisable(!editable);
        cliCp.setDisable(!editable);
        cliLocalidad.setDisable(!editable);
        cliProvincia.setDisable(!editable);
        cliEmail.setDisable(!editable);
        txtReferencia.setDisable(!editable);
        txtFormaPago.setDisable(!editable);
        vencimiento.setDisable(!editable);
        txtRealizadaPor.setDisable(!editable);
        txtDescuento.setDisable(!editable);
        comboRetencion.setDisable(!editable);
        txtObservaciones.setDisable(!editable);
        chkTotalConIva.setDisable(!editable);
        tablaLineas.setEditable(editable);
        for (TableColumn<LineaFactura, ?> columna : tablaLineas.getColumns()) {
            columna.setEditable(editable);
        }
        tablaLineas.setDisable(!editable);
    }

    private void empezarFacturaNueva() {
        facturaAbiertaId = null;
        correlativoFijo = null;
        estadoActual = null;
        clienteActual = null;
        descuento = 0;
        txtDescuento.setText("0");
        retencionActual = null;
        if (!tiposRetencion.isEmpty()) {
            comboRetencion.setValue(tiposRetencion.get(0));
        }
        txtObservaciones.setText("");
        txtReferencia.setText("");
        txtFormaPago.setText("");
        vencimiento.setValue(null);
        txtRealizadaPor.setText("");
        cargarDatosCliente(null);
        lineas.clear();
        LineaFactura primera = nuevaLinea();
        if (primera != null) {
            lineas.add(primera);
        }
        lblTitulo.setText("Nueva factura");
        recalcularNumero();
        modificado = false;
        aplicarEstado();
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
            String preferida = Vista.getInstancia().getControlador().preferencia(PREF_CARPETA);
            if (preferida != null && !preferida.isBlank()) {
                carpeta = preferida;
            }
        } catch (Exception e) {
            carpeta = "Facturas";
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

    private void guardarPreferenciaCarpeta(Path ruta) {
        try {
            if (ruta.getParent() != null) {
                Vista.getInstancia().getControlador().guardarPreferencia(PREF_EXPORTACION,
                        ruta.getParent().toString());
            }
        } catch (Exception e) {
            return;
        }
    }

    private Integer parseEntero(String texto) {
        if (texto == null || texto.isBlank()) {
            return 0;
        }
        try {
            return Integer.parseInt(texto.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String textoSinNulo(String texto) {
        if (texto == null) {
            return "";
        }
        return texto;
    }

    private void ponerAtajos() {
        Vista.getInstancia().getVentana().getScene().getAccelerators().put(
                new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN), () -> guardar());
        Vista.getInstancia().getVentana().getScene().getAccelerators().put(
                new KeyCodeCombination(KeyCode.P, KeyCombination.CONTROL_DOWN), () -> exportarPdf());
        Vista.getInstancia().getVentana().getScene().getAccelerators().put(
                new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN), () -> nuevaFactura());
        Vista.getInstancia().getVentana().getScene().getAccelerators().put(
                new KeyCodeCombination(KeyCode.ESCAPE), () -> volver());
    }

    /**
     * Cómo funciona: la tabla no sabe editar sola; con setCellFactory le decimos
     * qué celda poner en cada columna. Cada celda pasa por updateItem para
     * enseñarse, por startEdit para abrir su editor, por commitEdit para cerrar
     * aceptando lo escrito y por cancelEdit para cerrar sin guardar. Nosotros
     * solo aplicamos el texto a la LineaFactura de la fila y refrescamos.
     */
    private abstract class CeldaTexto extends TableCell<LineaFactura, String> {

        protected final TextInputControl editor;
        private boolean aplicando = false;

        protected CeldaTexto(TextInputControl editor) {
            this.editor = editor;
            editor.setOnKeyPressed(evento -> teclaEnEditor(evento));
            editor.focusedProperty().addListener((propiedad, anterior, nuevo) -> focoEnEditor(nuevo));
        }

        private void teclaEnEditor(KeyEvent evento) {
            if (evento.getCode() == KeyCode.ENTER) {
                confirmarYSaltar();
                evento.consume();
                return;
            }
            if (evento.getCode() == KeyCode.ESCAPE) {
                cancelEdit();
                evento.consume();
            }
        }

        private void focoEnEditor(Boolean nuevo) {
            if (nuevo != null && !nuevo) {
                confirmarSinSaltar();
            }
        }

        private void confirmarYSaltar() {
            if (aplicando) {
                return;
            }
            aplicando = true;
            TablePosition<LineaFactura, ?> posicion = tablaLineas.getEditingCell();
            guardarTexto();
            avanzarDesde(posicion);
            aplicando = false;
        }

        private void confirmarSinSaltar() {
            if (aplicando || !isEditing()) {
                return;
            }
            aplicando = true;
            guardarTexto();
            aplicando = false;
        }

        private void guardarTexto() {
            LineaFactura linea = filaDe(this);
            if (linea != null) {
                try {
                    aplicar(linea, editor.getText());
                } catch (Exception e) {
                    // Si lo escrito no vale, la línea se queda como estaba.
                }
            }
            commitEdit(getItem());
            tablaLineas.refresh();
            actualizarTotales();
            cambiado();
        }

        @Override
        protected void updateItem(String texto, boolean vacia) {
            super.updateItem(texto, vacia);
            if (vacia) {
                setText(null);
                setGraphic(null);
                return;
            }
            if (isEditing()) {
                setText(null);
                setGraphic(editor);
                return;
            }
            if (texto == null) {
                setText("");
            } else {
                setText(texto);
            }
            setGraphic(null);
        }

        @Override
        public void startEdit() {
            super.startEdit();
            String actual = getItem();
            if (actual == null) {
                actual = "";
            }
            editor.setText(actual);
            setText(null);
            setGraphic(editor);
            editor.selectAll();
            // Sin runLater el foco no llega al editor recién puesto como graphic.
            Platform.runLater(() -> editor.requestFocus());
        }

        @Override
        public void cancelEdit() {
            super.cancelEdit();
            setGraphic(null);
            String actual = getItem();
            if (actual == null) {
                setText("");
            } else {
                setText(actual);
            }
        }

        abstract void aplicar(LineaFactura linea, String texto) throws Exception;
    }

    private final class CeldaCantidad extends CeldaTexto {

        CeldaCantidad() {
            super(new TextField());
        }

        @Override
        void aplicar(LineaFactura linea, String texto) throws Exception {
            int valor = Integer.parseInt(texto.trim());
            linea.setCantidad(valor);
        }
    }

    private final class CeldaPrecio extends CeldaTexto {

        CeldaPrecio() {
            super(new TextField());
        }

        @Override
        void aplicar(LineaFactura linea, String texto) throws Exception {
            BigDecimal valor = Formatos.parseEntrada(texto);
            if (valor == null) {
                throw new Exception("El precio no es válido.");
            }
            linea.setPrecioUnitario(valor);
        }
    }

    private final class CeldaTotal extends CeldaTexto {

        CeldaTotal() {
            super(new TextField());
        }

        @Override
        void aplicar(LineaFactura linea, String texto) throws Exception {
            BigDecimal valor = Formatos.parseEntrada(texto);
            if (valor == null) {
                throw new Exception("El total no es válido.");
            }
            if (chkTotalConIva.isSelected()) {
                BigDecimal base = Calculos.baseDesdeTotalConIva(valor, linea.getIvaPorcentaje());
                linea.setPrecioUnitario(Calculos.precioDesdeTotal(base, linea.getCantidad()));
                return;
            }
            linea.setPrecioUnitario(Calculos.precioDesdeTotal(valor, linea.getCantidad()));
        }
    }

    private final class CeldaDescripcion extends CeldaTexto {

        private final TextArea area;
        private final Text etiqueta = new Text();

        CeldaDescripcion() {
            super(new TextArea());
            area = (TextArea) editor;
            area.setWrapText(true);
            area.setPrefRowCount(3);
            etiqueta.wrappingWidthProperty().bind(colDescripcion.widthProperty().subtract(10));
        }

        @Override
        protected void updateItem(String texto, boolean vacia) {
            if (isEditing()) {
                return;
            }
            super.updateItem(texto, vacia);
            if (vacia) {
                setText(null);
                setGraphic(null);
                return;
            }
            String actual = getItem();
            if (actual == null) {
                actual = "";
            }
            etiqueta.setText(actual);
            setText(null);
            setGraphic(etiqueta);
        }

        @Override
        void aplicar(LineaFactura linea, String texto) throws Exception {
            linea.setDescripcion(texto);
        }
    }

    private final class CeldaIva extends TableCell<LineaFactura, String> {

        private final ComboBox<TipoIva> combo = new ComboBox<>();
        private boolean cargandoCelda = false;

        CeldaIva() {
            combo.setItems(tiposIva);
            combo.setMaxWidth(Double.MAX_VALUE);
            combo.valueProperty().addListener((propiedad, anterior, nuevo) -> cambiarIva(nuevo));
        }

        private void cambiarIva(TipoIva nuevo) {
            if (cargandoCelda) {
                return;
            }
            if (nuevo == null) {
                return;
            }
            LineaFactura linea = filaDe(this);
            if (linea == null) {
                return;
            }
            if (nuevo.getId() != null && nuevo.getId().equals(linea.getTipoIvaId())) {
                return;
            }
            linea.setTipoIva(nuevo);
            tablaLineas.refresh();
            actualizarTotales();
            cambiado();
        }

        @Override
        protected void updateItem(String texto, boolean vacia) {
            super.updateItem(texto, vacia);
            if (vacia) {
                setText(null);
                setGraphic(null);
                return;
            }
            LineaFactura linea = filaDe(this);
            if (linea == null) {
                setText(null);
                setGraphic(null);
                return;
            }
            cargandoCelda = true;
            combo.setValue(tipoIvaDe(linea));
            cargandoCelda = false;
            setText(null);
            setGraphic(combo);
        }
    }
}
