package cabofactu.vista.controlador;

import cabofactu.modelo.dominio.FormatoNumero;
import cabofactu.modelo.dominio.Serie;
import cabofactu.vista.Pantalla;
import cabofactu.vista.Vista;
import cabofactu.vista.utilidades.Dialogos;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

/**
 * Ficha de una serie. Sirve para añadir y para editar: si el registro que
 * recibe es null, el formulario está en modo añadir.
 */
public class FichaSerieController implements Pantalla, Initializable {

    @FXML
    private Label lblTitulo;
    @FXML
    private TextField txtCodigo;
    @FXML
    private TextField txtDescripcion;
    @FXML
    private ComboBox<FormatoNumero> comboFormato;
    @FXML
    private CheckBox chkRectificativa;
    @FXML
    private Label lblEjemplo;
    @FXML
    private Button btnGuardar;

    private Serie registro;
    /** Serie tal como estaba al abrir la ficha; null en modo añadir. */
    private Serie original;
    /** Contenido de los campos al abrir la ficha, para saber si se han tocado. */
    private String fotoInicial;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        comboFormato.getItems().setAll(FormatoNumero.values());
        txtCodigo.textProperty().addListener((propiedad, anterior, nuevo) -> actualizarEjemplo());
        comboFormato.valueProperty().addListener((propiedad, anterior, nuevo) -> actualizarEjemplo());
        chkRectificativa.selectedProperty().addListener((propiedad, anterior, nuevo) -> actualizarEjemplo());
        txtCodigo.requestFocus();
    }

    /**
     * La ficha solo devuelve registro si el usuario pulsa Guardar. Si la cierra
     * con Cancelar o con la X de la ventana, getRegistro() devuelve null.
     */
    public Serie getRegistro() {
        return registro;
    }

    public void setRegistro(Serie registro) {
        this.registro = null;
        this.original = registro;
        rellenar(registro, registro == null);
        actualizarEjemplo();
        fotoInicial = fotoDeLosCampos();
    }

    /**
     * Volvemos a poner lo que había escrito para reintentar tras un fallo al
     * guardar. La serie de partida sigue siendo la misma, y la foto inicial
     * también: si ahora se cancela, preguntamos igual que la primera vez.
     */
    public void reintentarCon(Serie original, Serie intento) {
        this.registro = null;
        this.original = original;
        rellenar(original, original == null);
        fotoInicial = fotoDeLosCampos();
        rellenar(intento, original == null);
        actualizarEjemplo();
    }

    private void rellenar(Serie valores, boolean esAlta) {
        if (esAlta) {
            lblTitulo.setText("Alta de serie");
            btnGuardar.setText("Añadir");
            comboFormato.setValue(FormatoNumero.MES);
        } else {
            lblTitulo.setText("Datos de la serie");
            btnGuardar.setText("Guardar");
        }
        if (valores != null) {
            txtCodigo.setText(valores.getCodigo());
            txtDescripcion.setText(valores.getDescripcion());
            comboFormato.setValue(valores.getFormato());
            chkRectificativa.setSelected(valores.isEsRectificativa());
        }
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
            Dialogos.mostrarDialogoError("Datos de la serie", error);
            return;
        }
        try {
            if (original == null) {
                registro = serieDeLosCampos();
            } else {
                registro = actualizarRegistro();
            }
            cerrarVentana(event);
        } catch (Exception e) {
            Dialogos.mostrarDialogoError("Datos de la serie", e.getMessage());
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

    /** Enseñamos cómo quedará el número con lo que hay escrito ahora mismo. */
    private void actualizarEjemplo() {
        try {
            Serie prueba = new Serie(txtCodigo.getText().trim(), "", comboFormato.getValue(), chkRectificativa.isSelected());
            lblEjemplo.setText("Previsualización: " + Vista.getInstancia().getControlador()
                    .formarNumero(prueba, 56, LocalDate.of(2026, 7, 15)));
        } catch (Exception e) {
            lblEjemplo.setText("");
        }
    }

    private Serie serieDeLosCampos() throws Exception {
        Serie serie = new Serie(txtCodigo.getText().trim(), txtDescripcion.getText().trim(),
                comboFormato.getValue(), chkRectificativa.isSelected());
        return serie;
    }

    /** Trabajamos sobre una copia para que un dato mal escrito no estropee la fila de la tabla. */
    private Serie actualizarRegistro() throws Exception {
        Serie copia = new Serie(original);
        copia.setCodigo(txtCodigo.getText().trim());
        copia.setDescripcion(txtDescripcion.getText().trim());
        copia.setFormato(comboFormato.getValue());
        copia.setEsRectificativa(chkRectificativa.isSelected());
        return copia;
    }

    /**
     * Marcamos en rojo todos los campos incorrectos y devolvemos el mensaje del
     * primero, o null si está todo bien.
     */
    private String marcarCamposMalos() {
        quitarMarcas();
        String primero = null;
        primero = revisar(txtCodigo, Serie.errorCodigo(txtCodigo.getText()), primero);
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
        return txtCodigo.getText() + "|" + txtDescripcion.getText() + "|"
                + String.valueOf(comboFormato.getValue()) + "|" + chkRectificativa.isSelected();
    }

    /** Si hay algo escrito sin guardar, preguntamos antes de tirarlo. */
    private boolean confirmarDescartar() {
        if (fotoInicial.equals(fotoDeLosCampos())) {
            return true;
        }
        return Dialogos.mostrarDialogoConfirmacion("Ficha de serie",
                "Hay cambios sin guardar en la ficha de la serie.\n\n¿Quieres descartarlos?");
    }

    private void quitarMarcas() {
        txtCodigo.getStyleClass().remove("campo-error");
    }

    private void cerrarVentana(ActionEvent event) {
        Node origen = (Node) event.getSource();
        origen.getScene().getWindow().hide();
    }
}
