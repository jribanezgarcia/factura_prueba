package cabofactu.vista.controlador;

import cabofactu.vista.Vista;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

/**
 * Barra de navegación superior con iconos. Aparece en todas las pantallas
 * salvo el menú principal y lleva a las vistas principales o a salir.
 */
public class BarraNavegacionController {

    @FXML
    private Button btnInicio;
    @FXML
    private Button btnNueva;
    @FXML
    private Button btnHistorico;
    @FXML
    private Button btnClientes;
    @FXML
    private Button btnConfiguracion;
    @FXML
    private Button btnCopias;
    @FXML
    private Button btnSalir;

    @FXML
    void irInicio(ActionEvent event) {
        Vista.getInstancia().mostrar("MenuPrincipal.fxml");
    }

    @FXML
    void irNueva(ActionEvent event) {
        Vista.getInstancia().mostrar("Editor.fxml");
    }

    @FXML
    void irHistorico(ActionEvent event) {
        Vista.getInstancia().mostrar("Historico.fxml");
    }

    @FXML
    void irClientes(ActionEvent event) {
        Vista.getInstancia().mostrar("Clientes.fxml");
    }

    @FXML
    void irConfiguracion(ActionEvent event) {
        Vista.getInstancia().mostrar("Configuracion.fxml");
    }

    @FXML
    void irCopias(ActionEvent event) {
        Vista.getInstancia().mostrar("CopiaSeguridad.fxml");
    }

    @FXML
    void salir(ActionEvent event) {
        Vista.getInstancia().salir();
    }

    /** Marcamos con la raya de color el botón de la pantalla en la que estamos. */
    public void marcarActivo(String pantalla) {
        switch (pantalla) {
            case "editor" -> btnNueva.getStyleClass().add("activo");
            case "historico" -> btnHistorico.getStyleClass().add("activo");
            case "clientes" -> btnClientes.getStyleClass().add("activo");
            case "configuracion" -> btnConfiguracion.getStyleClass().add("activo");
            case "copiaSeguridad" -> btnCopias.getStyleClass().add("activo");
            default -> {
            }
        }
    }

    /** Desactivamos todos los botones menos Salir. */
    public void bloquearSalvoSalir() {
        btnInicio.setDisable(true);
        btnNueva.setDisable(true);
        btnHistorico.setDisable(true);
        btnClientes.setDisable(true);
        btnConfiguracion.setDisable(true);
        btnCopias.setDisable(true);
    }
}
