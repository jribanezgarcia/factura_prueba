package cabofactu.vista.controlador;

import cabofactu.modelo.dominio.TipoIva;
import cabofactu.vista.Pantalla;
import cabofactu.vista.Vista;
import cabofactu.vista.utilidades.Dialogos;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Ficha de un tipo de IVA. Sirve para añadir y para editar: si el registro que
 * recibe es null, el formulario está en modo añadir.
 */
public class FichaTipoIvaController implements Pantalla, Initializable {

    @FXML
    private Label lblTitulo;
    @FXML
    private TextField txtNombre;
    @FXML
    private TextField txtPorcentaje;
    @FXML
    private CheckBox chkSuplido;
    @FXML
    private TextField txtMotivo;
    @FXML
    private CheckBox chkActivo;
    @FXML
    private Label lblAviso;
    @FXML
    private Button btnGuardar;

    private TipoIva registro;
    /** Tipo tal como estaba al abrir la ficha; null en modo añadir. */
    private TipoIva original;
    /** Contenido de los campos al abrir la ficha, para saber si se han tocado. */
    private String fotoInicial;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        chkSuplido.selectedProperty().addListener((propiedad, anterior, nuevo) -> actualizarPorcentajeSuplido());
        txtNombre.requestFocus();
    }

    /**
     * La ficha solo devuelve registro si el usuario pulsa Guardar. Si la cierra
     * con Cancelar o con la X de la ventana, getRegistro() devuelve null.
     */
    public TipoIva getRegistro() {
        return registro;
    }

    public void setRegistro(TipoIva registro) {
        this.registro = null;
        this.original = registro;
        if (this.original == null) {
            lblTitulo.setText("Alta de tipo de IVA");
            btnGuardar.setText("Añadir");
            chkActivo.setSelected(true);
        } else {
            lblTitulo.setText("Datos del tipo de IVA");
            btnGuardar.setText("Guardar");
            txtNombre.setText(this.original.getNombre());
            if (this.original.getPorcentaje() == null) {
                txtPorcentaje.setText("");
            } else {
                txtPorcentaje.setText(String.valueOf(this.original.getPorcentaje()));
            }
            chkSuplido.setSelected(this.original.isEsSuplido());
            txtMotivo.setText(this.original.getMotivoExencion());
            chkActivo.setSelected(this.original.isActivo());
            try {
                boolean enUso = Vista.getInstancia().getControlador()
                        .tipoIvaEnUso(this.original.getId());
                chkSuplido.setDisable(true);
                if (this.original.isExento() || this.original.isEsSuplido() || enUso) {
                    txtPorcentaje.setDisable(true);
                }
                boolean aviso = enUso && !this.original.isExento() && !this.original.isEsSuplido();
                lblAviso.setVisible(aviso);
                lblAviso.setManaged(aviso);
            } catch (Exception e) {
                Dialogos.mostrarDialogoError("IVA", "No se pudo comprobar el tipo: " + e.getMessage());
            }
        }
        fotoInicial = fotoDeLosCampos();
    }

    /** Al cerrar con la X preguntamos lo mismo que al pulsar Cancelar. */
    @Override
    public boolean puedeCerrar() {
        return confirmarDescartar();
    }

    @FXML
    void guardar(ActionEvent event) {
        String error = marcarCamposMalos();
        if (error != null) {
            Dialogos.mostrarDialogoError("Datos del tipo de IVA", error);
            return;
        }
        try {
            TipoIva tipo;
            if (original == null) {
                tipo = tipoDeLosCampos();
            } else {
                tipo = actualizarRegistro();
            }
            if (!nombreLibre(tipo)) {
                return;
            }
            registro = tipo;
            cerrarVentana(event);
        } catch (Exception e) {
            Dialogos.mostrarDialogoError("Datos del tipo de IVA", e.getMessage());
        }
    }

    @FXML
    void cancelar(ActionEvent event) {
        if (!confirmarDescartar()) {
            return;
        }
        registro = null;
        cerrarVentana(event);
    }

    /** Al marcar «Es suplido» vaciamos el porcentaje y lo desactivamos. */
    private void actualizarPorcentajeSuplido() {
        if (chkSuplido.isSelected()) {
            txtPorcentaje.clear();
            txtPorcentaje.setDisable(true);
        } else {
            txtPorcentaje.setDisable(false);
        }
    }

    private TipoIva tipoDeLosCampos() throws Exception {
        String textoPorcentaje = txtPorcentaje.getText().trim();
        Integer porcentaje = null;
        if (!textoPorcentaje.isBlank()) {
            porcentaje = Integer.parseInt(textoPorcentaje);
        }
        TipoIva tipo = new TipoIva(txtNombre.getText().trim(), porcentaje, chkSuplido.isSelected());
        tipo.setMotivoExencion(txtMotivo.getText().trim());
        tipo.setActivo(chkActivo.isSelected());
        return tipo;
    }

    /** Trabajamos sobre una copia para que un dato mal escrito no estropee la fila de la tabla. */
    private TipoIva actualizarRegistro() throws Exception {
        TipoIva copia = new TipoIva(original);
        copia.setNombre(txtNombre.getText().trim());
        String textoPorcentaje = txtPorcentaje.getText().trim();
        if (textoPorcentaje.isBlank()) {
            copia.setPorcentaje(null);
        } else {
            copia.setPorcentaje(Integer.parseInt(textoPorcentaje));
        }
        copia.setMotivoExencion(txtMotivo.getText().trim());
        copia.setActivo(chkActivo.isSelected());
        return copia;
    }

    /** Miramos si otro tipo de IVA tiene ya este nombre. Devolvemos false si no se puede guardar. */
    private boolean nombreLibre(TipoIva tipo) throws Exception {
        TipoIva otro = Vista.getInstancia().getControlador().buscarTipoIvaPorNombre(tipo.getNombre());
        if (otro == null || otro.getId().equals(tipo.getId())) {
            return true;
        }
        txtNombre.getStyleClass().add("campo-error");
        Dialogos.mostrarDialogoError("Datos del tipo de IVA", String.format(
                "Ya existe un tipo de IVA con el nombre %s.", otro.getNombre()));
        return false;
    }

    /**
     * Marcamos en rojo todos los campos incorrectos y devolvemos el mensaje del
     * primero, o null si está todo bien.
     */
    private String marcarCamposMalos() {
        quitarMarcas();
        String primero = null;
        primero = revisar(txtNombre, TipoIva.errorNombre(txtNombre.getText()), primero);
        primero = revisar(txtPorcentaje, TipoIva.errorPorcentaje(txtPorcentaje.getText()), primero);
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

    /**
     * Cómo funciona: juntamos el contenido de los campos en un solo texto. Guardamos
     * ese texto al abrir la ficha y lo volvemos a formar al cerrarla; si no coinciden,
     * es que el usuario ha tocado algo.
     */
    private String fotoDeLosCampos() {
        return txtNombre.getText() + "|" + txtPorcentaje.getText() + "|" + chkSuplido.isSelected() + "|"
                + txtMotivo.getText() + "|" + chkActivo.isSelected();
    }

    /** Si hay algo escrito sin guardar, preguntamos antes de tirarlo. */
    private boolean confirmarDescartar() {
        if (fotoInicial.equals(fotoDeLosCampos())) {
            return true;
        }
        return Dialogos.mostrarDialogoConfirmacion("Ficha de tipo de IVA",
                "Hay cambios sin guardar en la ficha del tipo de IVA.\n\n¿Quieres descartarlos?");
    }

    private void quitarMarcas() {
        txtNombre.getStyleClass().remove("campo-error");
        txtPorcentaje.getStyleClass().remove("campo-error");
    }

    private void cerrarVentana(ActionEvent event) {
        Node origen = (Node) event.getSource();
        origen.getScene().getWindow().hide();
    }
}
