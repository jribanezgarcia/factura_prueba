package cabofactu.vista;

import cabofactu.controlador.Controlador;
import cabofactu.vista.controlador.ArranqueController;
import cabofactu.vista.utilidades.Dialogos;
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
        Controlador controlador = Vista.getInstancia().getControlador();
        try {
            controlador.prepararDatos();
        } catch (Exception e) {
            Dialogos.mostrarDialogoError("Facturación", e.getMessage());
            Platform.exit();
            return;
        }
        boolean demoCargada = false;
        try {
            demoCargada = controlador.cargarDemostracion();
        } catch (Exception e) {
            Dialogos.mostrarDialogoError("Facturación",
                    "No se pudo cargar la empresa de demostración:\n" + e.getMessage());
        }
        ArranqueController arranque = Vista.getInstancia().prepararArranque(stage);
        boolean demo = demoCargada;
        stage.setOnShown(e -> arranque.mostrarAvisoInicial(demo));
        stage.show();
    }
}
