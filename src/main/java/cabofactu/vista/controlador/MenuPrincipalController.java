package cabofactu.vista.controlador;

import cabofactu.modelo.dominio.Empresa;
import cabofactu.utilidades.Formatos;
import cabofactu.utilidades.LogoMarco;
import cabofactu.vista.Pantalla;
import cabofactu.vista.Vista;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.StackPane;

import java.io.File;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;
import cabofactu.vista.Vista;

/**
 * Menu principal: Nueva factura, Historico, Clientes, Configuracion, Copia de
 * seguridad y Salir. Muestra la fecha de trabajo de la sesion (solo lectura).
 * El logo y los datos de empresa salen de la configuracion.
 */
public class MenuPrincipalController implements Pantalla, Initializable {

    @FXML
    private Label fechaTrabajo;
    @FXML
    private ImageView logo;
    @FXML
    private StackPane logoBox;
    @FXML
    private Label lblEmpresa;
    @FXML
    private Label lblEmpresaInfo;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        LocalDate f = Vista.getInstancia().getControlador().getModelo().getReloj().fechaTrabajo();
        fechaTrabajo.setText(Formatos.fecha(f));
        cargarEmpresa();
    }

    @Override
    public void alMostrar() {
        atajos();
        quitarFocoInicial();
    }

    /**
     * Evita que el primer boton del menu quede resaltado al abrir la vista:
     * el foco inicial se deja en el fondo de la escena y no en los botones.
     */
    private void quitarFocoInicial() {
        if (Vista.getInstancia().getVentana().getScene() != null) {
            Platform.runLater(() -> Vista.getInstancia().getVentana().getScene().getRoot().requestFocus());
        }
    }

    private void cargarEmpresa() {
        try {
            Empresa e = Vista.getInstancia().getControlador().getModelo().getConfiguracion().getEmpresa();
            if (e.getNombre() != null && !e.getNombre().isBlank()) {
                lblEmpresa.setText(e.getNombre());
            }
            if (e.getNif() != null && !e.getNif().isBlank()) {
                lblEmpresaInfo.setText("NIF " + e.getNif());
            }
            cargarLogo(e.getLogoPath());
        } catch (Exception ignored) {
        }
    }

    private void cargarLogo(String ruta) {
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
        logo.setFitWidth(260);
        logo.setFitHeight(100);
        logo.setPreserveRatio(true);
        LogoMarco.aplicar(logoBox, img);
    }

    private void atajos() {
        Vista.getInstancia().getVentana().getScene().getAccelerators().put(
                new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN), () -> nuevaFactura());
        Vista.getInstancia().getVentana().getScene().getAccelerators().put(
                new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN), () -> historico());
    }

    @FXML
    private void nuevaFactura() {
        Vista.getInstancia().mostrar("Editor.fxml");
    }

    @FXML
    private void generarMensual() {
        GenerarFacturasMensualesController.abrir();
        quitarFocoInicial();
    }

    @FXML
    private void historico() {
        Vista.getInstancia().mostrar("Historico.fxml");
    }

    @FXML
    private void clientes() {
        Vista.getInstancia().mostrar("Clientes.fxml");
    }

    @FXML
    private void configuracion() {
        Vista.getInstancia().mostrar("Configuracion.fxml");
    }

    @FXML
    private void copiaSeguridad() {
        Vista.getInstancia().mostrar("CopiaSeguridad.fxml");
    }

    @FXML
    private void salir() {
        Vista.getInstancia().salir();
    }
}