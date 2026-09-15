package cabofactu.vista;

import cabofactu.modelo.Modelo;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.function.Consumer;
import cabofactu.vista.utilidades.Botones;
import cabofactu.vista.utilidades.Microinteracciones;
import cabofactu.vista.utilidades.ThemeManager;

/**
 * Navegacion entre vistas FXML sobre la misma ventana.
 */
public class Navegador {

    private final Stage stage;
    private final Modelo modelo;
    private Consumer<Vista> onVistaCambio;
    private Vista vistaActual;

    public Navegador(Stage stage, Modelo modelo) {
        this.stage = stage;
        this.modelo = modelo;
    }

    public void setOnVistaCambio(Consumer<Vista> c) {
        this.onVistaCambio = c;
    }

    public Modelo modelo() {
        return modelo;
    }

    public Stage stage() {
        return stage;
    }

    public void mostrarInicio() {
        if (modelo.getConfiguracion().empresaCompleta()) {
            mostrar("/cabofactu/vista/recursos/MenuPrincipal.fxml");
        } else {
            mostrar("/cabofactu/vista/recursos/Configuracion.fxml");
        }
    }

    /**
     * Carga la vista y la muestra en la ventana, previa confirmacion de la
     * vista actual. Si la vista actual cancela la salida
     * (`puedeCerrar() == false`), no se navega y se devuelve `null`.
     */
    public <T extends Vista> T mostrar(String fxml) {
        if (vistaActual != null && !vistaActual.puedeCerrar()) {
            return null;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            ThemeManager.aplicar(scene, modelo);
            stage.setScene(scene);
            VentanaConfig.para(fxml).ifPresent(cfg -> {
                cfg.aplicar(stage);
                stage.setTitle(Ventanas.PREFIJO + cfg.titulo());
            });
            Ventanas.aplicarIcono(stage);
            T vista = loader.getController();
            if (vista != null) {
                vista.setModelo(modelo);
                vista.setNavegador(this);
                vistaActual = vista;
                if (onVistaCambio != null) {
                    onVistaCambio.accept(vista);
                }
                vista.alIniciar();
            }
            root.applyCss();
            Botones.igualarGrupos(root);
            root.lookupAll(".primary-button, .menu-item").forEach(n ->
                    Microinteracciones.escalaSuave((javafx.scene.Node) n));
            return vista;
        } catch (IOException e) {
            throw new RuntimeException("No se pudo cargar la vista " + fxml, e);
        }
    }
}
