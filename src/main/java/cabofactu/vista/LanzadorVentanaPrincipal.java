package cabofactu.vista;

import cabofactu.vista.utilidades.Dialogos;
import cabofactu.vista.utilidades.ErroresInesperados;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

/**
 * Arranca JavaFX y abre la ventana de arranque (empresa y fecha de trabajo).
 * Comprobamos aquí la carpeta de datos y la instancia única porque hasta que
 * JavaFX no arranca no se pueden mostrar avisos.
 */
public class LanzadorVentanaPrincipal extends Application {

    public static void comenzar() {
        launch(LanzadorVentanaPrincipal.class);
    }

    @Override
    public void start(Stage stage) {
        ErroresInesperados.registrar();
        try {
            Vista.getInstancia().getControlador().prepararDatos();
        } catch (Exception e) {
            Dialogos.mostrarDialogoError("Facturación", e.getMessage());
            Platform.exit();
            return;
        }
        Vista.getInstancia().mostrarArranque(stage);
    }
}
