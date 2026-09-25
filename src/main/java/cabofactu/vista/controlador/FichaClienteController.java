package cabofactu.vista.controlador;

import cabofactu.modelo.dominio.Cliente;
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
import java.util.List;
import java.util.ResourceBundle;

/**
 * Ficha de un cliente. Sirve para añadir y para editar: si el registro que
 * recibe es null, el formulario está en modo añadir.
 */
public class FichaClienteController implements Pantalla, Initializable {

    @FXML
    private Label lblTitulo;
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
    private TextField txtEmail;
    @FXML
    private CheckBox chkActivo;
    @FXML
    private Button btnGuardar;

    private Cliente registro;
    /** Cliente tal como estaba al abrir la ficha; null en modo añadir. */
    private Cliente original;
    /** Contenido de los campos al abrir la ficha, para saber si se han tocado. */
    private String fotoInicial;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        txtNombre.requestFocus();
    }

    /**
     * La ficha solo devuelve registro si el usuario pulsa Guardar. Si la cierra
     * con Cancelar o con la X de la ventana, getRegistro() devuelve null.
     */
    public Cliente getRegistro() {
        return registro;
    }

    public void setRegistro(Cliente registro) {
        this.registro = null;
        this.original = registro;
        if (this.original == null) {
            lblTitulo.setText("Alta de cliente");
            btnGuardar.setText("Añadir");
            chkActivo.setSelected(true);
        } else {
            lblTitulo.setText("Datos del cliente");
            btnGuardar.setText("Guardar");
            txtNombre.setText(this.original.getNombre());
            txtNif.setText(this.original.getNif());
            txtDireccion.setText(this.original.getDireccion());
            txtCp.setText(this.original.getCp());
            txtLocalidad.setText(this.original.getLocalidad());
            txtProvincia.setText(this.original.getProvincia());
            txtEmail.setText(this.original.getEmail());
            chkActivo.setSelected(this.original.isActivo());
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
            Dialogos.mostrarDialogoError("Datos del cliente", error);
            return;
        }
        try {
            Cliente cliente;
            if (original == null) {
                cliente = clienteDeLosCampos();
            } else {
                cliente = actualizarRegistro();
            }
            if (!nifLibre(cliente)) {
                return;
            }
            registro = cliente;
            cerrarVentana(event);
        } catch (Exception e) {
            Dialogos.mostrarDialogoError("Datos del cliente", e.getMessage());
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

    private Cliente clienteDeLosCampos() throws Exception {
        Cliente cliente = new Cliente(
                txtNombre.getText().trim(),
                txtNif.getText().trim(),
                txtDireccion.getText().trim(),
                txtCp.getText().trim(),
                txtLocalidad.getText().trim(),
                txtProvincia.getText().trim());
        cliente.setEmail(txtEmail.getText().trim());
        cliente.setActivo(chkActivo.isSelected());
        return cliente;
    }

    /** Trabajamos sobre una copia para que un dato mal escrito no estropee la fila de la tabla. */
    private Cliente actualizarRegistro() throws Exception {
        Cliente copia = new Cliente(original);
        copia.setNombre(txtNombre.getText().trim());
        copia.setNif(txtNif.getText().trim());
        copia.setDireccion(txtDireccion.getText().trim());
        copia.setCp(txtCp.getText().trim());
        copia.setLocalidad(txtLocalidad.getText().trim());
        copia.setProvincia(txtProvincia.getText().trim());
        copia.setEmail(txtEmail.getText().trim());
        copia.setActivo(chkActivo.isSelected());
        return copia;
    }

    /**
     * Miramos si otro cliente tiene ya este NIF. Si es uno dado de baja y estamos
     * dando de alta, ofrecemos recuperarlo: el cliente pasa a llevar su id y vuelve
     * a estar activo. Devolvemos false si no se puede guardar.
     */
    private boolean nifLibre(Cliente cliente) throws Exception {
        Cliente otro = Vista.getInstancia().getControlador().buscarClientePorNif(cliente.getNif());
        if (otro == null || otro.getId().equals(cliente.getId())) {
            return true;
        }
        if (original == null && !otro.isActivo()) {
            boolean recuperar = Dialogos.mostrarDialogoConfirmacion("Cliente dado de baja", String.format(
                    "El cliente %s, con el NIF %s, está dado de baja.%n%n"
                            + "¿Quieres volver a darlo de alta con los datos que acabas de escribir?",
                    otro.getNombre(), otro.getNif()));
            if (recuperar) {
                cliente.setId(otro.getId());
                cliente.setActivo(true);
            }
            return recuperar;
        }
        txtNif.getStyleClass().add("campo-error");
        Dialogos.mostrarDialogoError("Datos del cliente", String.format(
                "Ya existe un cliente con el NIF %s: %s.", otro.getNif(), otro.getNombre()));
        return false;
    }

    /**
     * Marcamos en rojo todos los campos incorrectos y devolvemos el mensaje del
     * primero, o null si está todo bien.
     */
    private String marcarCamposMalos() {
        quitarMarcas();
        String primero = null;
        primero = revisar(txtNombre, Cliente.errorNombre(txtNombre.getText()), primero);
        primero = revisar(txtNif, Cliente.errorNif(txtNif.getText()), primero);
        primero = revisar(txtDireccion, Cliente.errorDireccion(txtDireccion.getText()), primero);
        primero = revisar(txtCp, Cliente.errorCp(txtCp.getText()), primero);
        primero = revisar(txtLocalidad, Cliente.errorLocalidad(txtLocalidad.getText()), primero);
        primero = revisar(txtProvincia, Cliente.errorProvincia(txtProvincia.getText()), primero);
        primero = revisar(txtEmail, Cliente.errorEmail(txtEmail.getText()), primero);
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
        return txtNombre.getText() + "|" + txtNif.getText() + "|" + txtDireccion.getText() + "|"
                + txtCp.getText() + "|" + txtLocalidad.getText() + "|" + txtProvincia.getText() + "|"
                + txtEmail.getText() + "|" + chkActivo.isSelected();
    }

    /** Si hay algo escrito sin guardar, preguntamos antes de tirarlo. */
    private boolean confirmarDescartar() {
        if (fotoInicial.equals(fotoDeLosCampos())) {
            return true;
        }
        return Dialogos.mostrarDialogoConfirmacion("Ficha de cliente",
                "Hay cambios sin guardar en la ficha del cliente.\n\n¿Quieres descartarlos?");
    }

    private void quitarMarcas() {
        for (TextField campo : List.of(txtNombre, txtNif, txtDireccion, txtCp, txtLocalidad, txtProvincia, txtEmail)) {
            campo.getStyleClass().remove("campo-error");
        }
    }

    private void cerrarVentana(ActionEvent event) {
        Node origen = (Node) event.getSource();
        origen.getScene().getWindow().hide();
    }
}