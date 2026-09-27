package cabofactu.vista.controlador;

import cabofactu.modelo.dominio.Empresa;
import cabofactu.modelo.dominio.EmpresaDisponible;
import cabofactu.modelo.dominio.ResumenCopia;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.util.ResourceBundle;
import cabofactu.vista.Pantalla;
import cabofactu.vista.Vista;
import cabofactu.vista.utilidades.Dialogos;

/**
 * Pantalla de copias de seguridad: crear una copia de la empresa activa y
 * restaurar una, sobre la propia empresa o como una empresa nueva.
 */
public class CopiaSeguridadController implements Pantalla, Initializable {

    @FXML
    private Label lblCarpeta;
    @FXML
    private Button btnCrear;
    // Cómo funciona: JavaFX inyecta aquí el controlador del <fx:include fx:id="barra">;
    // el nombre del campo es el fx:id más "Controller".
    @FXML
    private BarraNavegacionController barraController;

    @FXML
    private Label lblOrigen;
    @FXML
    private VBox cajaResumen;
    @FXML
    private Label lblResumen;
    @FXML
    private RadioButton rbReemplazar;
    @FXML
    private RadioButton rbCrearNueva;
    @FXML
    private ToggleGroup grupoDestino;
    @FXML
    private HBox filaNombreEmpresa;
    @FXML
    private TextField txtNombreEmpresa;
    @FXML
    private Button btnRestaurar;

    private Path origen;
    private ResumenCopia resumen;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        barraController.marcarActivo("copiaSeguridad");
        mostrarCarpeta();
        limpiarRestauracion();
    }

    @FXML
    private void crearCopia() {
        DirectoryChooser selector = new DirectoryChooser();
        selector.setTitle("Carpeta para la copia de seguridad");
        Path carpetaRecordada = Vista.getInstancia().getControlador().carpetaCopias();
        if (carpetaRecordada != null) {
            selector.setInitialDirectory(carpetaRecordada.toFile());
        }
        File carpeta = selector.showDialog(Vista.getInstancia().getVentana());
        if (carpeta == null) {
            return;
        }
        esperar();
        try {
            Path ruta = Vista.getInstancia().getControlador().crearCopia(carpeta.toPath());
            Dialogos.mostrarDialogoInformacion("Copia de seguridad", String.format("Copia creada en:%n%s", ruta));
            mostrarCarpeta();
        } catch (Exception e) {
            Dialogos.mostrarDialogoError("Copia de seguridad", e.getMessage());
        } finally {
            terminarEspera();
        }
    }

    @FXML
    private void elegirCopia() {
        FileChooser selector = new FileChooser();
        selector.setTitle("Elegir la copia que se va a restaurar");
        selector.getExtensionFilters().add(new FileChooser.ExtensionFilter("Copias de CaboFactu", "*.db"));
        Path carpetaRecordada = Vista.getInstancia().getControlador().carpetaCopias();
        if (carpetaRecordada != null) {
            selector.setInitialDirectory(carpetaRecordada.toFile());
        }
        File archivo = selector.showOpenDialog(Vista.getInstancia().getVentana());
        if (archivo == null) {
            return;
        }
        esperar();
        try {
            origen = archivo.toPath();
            resumen = Vista.getInstancia().getControlador().leerCopia(origen);
            lblOrigen.setText(archivo.getName());
            lblResumen.setText(resumen.getTexto());
            cajaResumen.setVisible(true);
            cajaResumen.setManaged(true);
            prepararDestino();
        } catch (Exception e) {
            Dialogos.mostrarDialogoError("Copia de seguridad", e.getMessage());
            limpiarRestauracion();
        } finally {
            terminarEspera();
        }
    }

    /** Activamos las opciones de destino según si la copia es de la misma empresa. */
    private void prepararDestino() throws Exception {
        boolean puedeReemplazar = Vista.getInstancia().getControlador().puedeReemplazarEmpresa(resumen);
        rbReemplazar.setDisable(!puedeReemplazar);
        rbCrearNueva.setDisable(false);
        if (puedeReemplazar) {
            grupoDestino.selectToggle(rbReemplazar);
        } else {
            grupoDestino.selectToggle(rbCrearNueva);
        }
        cambiarDestino(null);
        btnRestaurar.setDisable(false);
    }

    @FXML
    private void cambiarDestino(ActionEvent event) {
        boolean nueva = grupoDestino.getSelectedToggle() == rbCrearNueva;
        filaNombreEmpresa.setVisible(nueva);
        filaNombreEmpresa.setManaged(nueva);
    }

    @FXML
    private void restaurar() {
        try {
            if (grupoDestino.getSelectedToggle() == rbReemplazar) {
                reemplazarEmpresa();
            } else {
                crearEmpresaDesdeCopia();
            }
        } catch (Exception e) {
            Dialogos.mostrarDialogoError("Restaurar copia", e.getMessage());
        } finally {
            terminarEspera();
        }
    }

    /** Reemplazamos los datos de la empresa activa con los de la copia elegida. */
    private void reemplazarEmpresa() throws Exception {
        Empresa activa = Vista.getInstancia().getControlador().buscarEmpresa();
        String nombreEmpresa = "";
        if (activa != null) {
            nombreEmpresa = activa.getNombre();
        }
        boolean confirmado = Dialogos.mostrarDialogoConfirmacion("Restaurar copia", String.format(
                "¿Reemplazar los datos de %s con los de la copia?%n%nAntes guardaremos una copia de rescate del estado actual.",
                nombreEmpresa));
        if (!confirmado) {
            return;
        }
        esperar();
        Path rescate = Vista.getInstancia().getControlador().restaurarCopia(origen);
        Dialogos.mostrarDialogoInformacion("Restaurar copia",
                String.format("Copia restaurada. La copia de rescate está en:%n%s", rescate));
        Vista.getInstancia().mostrarInicio();
    }

    /** Creamos una empresa nueva con los datos de la copia elegida. */
    private void crearEmpresaDesdeCopia() throws Exception {
        String nombre = txtNombreEmpresa.getText().trim();
        if (nombre.isBlank()) {
            throw new Exception("Escriba el nombre de la nueva empresa.");
        }
        boolean confirmado = Dialogos.mostrarDialogoConfirmacion("Restaurar copia",
                String.format("¿Crear la empresa «%s» con los datos de la copia?", nombre));
        if (!confirmado) {
            return;
        }
        esperar();
        EmpresaDisponible nueva = Vista.getInstancia().getControlador().restaurarCopiaComoEmpresa(origen, nombre);
        boolean cambiar = Dialogos.mostrarDialogoConfirmacion("Restaurar copia", String.format(
                "Se ha creado la empresa «%s».%n%n¿Quieres cambiar a ella? Se cerrará esta empresa y volverás a la pantalla de arranque con la nueva elegida.",
                nombre));
        if (cambiar) {
            Vista.getInstancia().getControlador().recordarUltimaEmpresa(nueva.getCarpeta());
            Vista.getInstancia().volverAlArranque();
        } else {
            limpiarRestauracion();
        }
    }

    @FXML
    private void volver() {
        Vista.getInstancia().mostrar("MenuPrincipal.fxml");
    }

    private void mostrarCarpeta() {
        Path carpeta = Vista.getInstancia().getControlador().carpetaCopias();
        if (carpeta == null) {
            lblCarpeta.setText("Todavía no se ha elegido la carpeta de las copias.");
        } else {
            lblCarpeta.setText(String.format("Las copias se guardan en %s", carpeta));
        }
    }

    private void limpiarRestauracion() {
        origen = null;
        resumen = null;
        lblOrigen.setText("(ninguna copia elegida)");
        cajaResumen.setVisible(false);
        cajaResumen.setManaged(false);
        filaNombreEmpresa.setVisible(false);
        filaNombreEmpresa.setManaged(false);
        txtNombreEmpresa.setText("");
        btnRestaurar.setDisable(true);
    }

    private void esperar() {
        Vista.getInstancia().getVentana().getScene().setCursor(Cursor.WAIT);
    }

    private void terminarEspera() {
        Vista.getInstancia().getVentana().getScene().setCursor(Cursor.DEFAULT);
    }
}
