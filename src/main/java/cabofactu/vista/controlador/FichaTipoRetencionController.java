package cabofactu.vista.controlador;

import cabofactu.modelo.dominio.TipoRetencion;
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
 * Ficha de un tipo de retención. Sirve para añadir y para editar: si el
 * registro que recibe es null, el formulario está en modo añadir.
 */
public class FichaTipoRetencionController implements Pantalla, Initializable {

    @FXML
    private Label lblTitulo;
    @FXML
    private TextField txtNombre;
    @FXML
    private TextField txtPorcentaje;
    @FXML
    private CheckBox chkActivo;
    @FXML
    private Label lblAviso;
    @FXML
    private Button btnGuardar;

    private TipoRetencion registro;
    /** Tipo tal como estaba al abrir la ficha; null en modo añadir. */
    private TipoRetencion original;
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
    public TipoRetencion getRegistro() {
        return registro;
    }

    public void setRegistro(TipoRetencion registro) {
        this.registro = null;
        this.original = registro;
        if (this.original == null) {
            lblTitulo.setText("Alta de tipo de retención");
            btnGuardar.setText("Añadir");
            chkActivo.setSelected(true);
        } else {
            lblTitulo.setText("Datos del tipo de retención");
            btnGuardar.setText("Guardar");
            txtNombre.setText(this.original.getNombre());
            txtPorcentaje.setText(String.valueOf(this.original.getPorcentaje()));
            chkActivo.setSelected(this.original.isActivo());
            try {
                boolean enUso = Vista.getInstancia().getControlador()
                        .tipoRetencionEnUso(this.original.getId());
                if (enUso) {
                    txtPorcentaje.setDisable(true);
                }
                lblAviso.setVisible(enUso);
                lblAviso.setManaged(enUso);
            } catch (Exception e) {
                Dialogos.mostrarDialogoError("Retención", "No se pudo comprobar el tipo: " + e.getMessage());
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
            Dialogos.mostrarDialogoError("Datos del tipo de retención", error);
            return;
        }
        try {
            if (original == null) {
                registro = tipoDeLosCampos();
            } else {
                registro = actualizarRegistro();
            }
            cerrarVentana(event);
        } catch (Exception e) {
            Dialogos.mostrarDialogoError("Datos del tipo de retención", e.getMessage());
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

    private TipoRetencion tipoDeLosCampos() throws Exception {
        TipoRetencion tipo = new TipoRetencion(txtNombre.getText().trim(),
                Integer.parseInt(txtPorcentaje.getText().trim()));
        tipo.setActivo(chkActivo.isSelected());
        return tipo;
    }

    /** Trabajamos sobre una copia para que un dato mal escrito no estropee la fila de la tabla. */
    private TipoRetencion actualizarRegistro() throws Exception {
        TipoRetencion copia = new TipoRetencion(original);
        copia.setNombre(txtNombre.getText().trim());
        copia.setPorcentaje(Integer.parseInt(txtPorcentaje.getText().trim()));
        copia.setActivo(chkActivo.isSelected());
        return copia;
    }

    /**
     * Marcamos en rojo todos los campos incorrectos y devolvemos el mensaje del
     * primero, o null si está todo bien.
     */
    private String marcarCamposMalos() {
        quitarMarcas();
        String primero = null;
        primero = revisar(txtNombre, TipoRetencion.errorNombre(txtNombre.getText()), primero);
        primero = revisar(txtPorcentaje, TipoRetencion.errorPorcentaje(txtPorcentaje.getText()), primero);
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
        return txtNombre.getText() + "|" + txtPorcentaje.getText() + "|" + chkActivo.isSelected();
    }

    /** Si hay algo escrito sin guardar, preguntamos antes de tirarlo. */
    private boolean confirmarDescartar() {
        if (fotoInicial.equals(fotoDeLosCampos())) {
            return true;
        }
        return Dialogos.mostrarDialogoConfirmacion("Ficha de tipo de retención",
                "Hay cambios sin guardar en la ficha del tipo de retención.\n\n¿Quieres descartarlos?");
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
