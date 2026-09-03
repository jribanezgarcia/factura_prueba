package com.alcazaba.facturacion.ui;

import com.alcazaba.facturacion.service.Servicios;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.function.Consumer;

/**
 * Navegacion entre vistas FXML sobre la misma ventana.
 */
public class Navegador {

    private final Stage stage;
    private final Servicios servicios;
    private Consumer<Vista> onVistaCambio;
    private Vista vistaActual;

    public Navegador(Stage stage, Servicios servicios) {
        this.stage = stage;
        this.servicios = servicios;
    }

    public void setOnVistaCambio(Consumer<Vista> c) {
        this.onVistaCambio = c;
    }

    public Servicios servicios() {
        return servicios;
    }

    public Stage stage() {
        return stage;
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
            ThemeManager.aplicar(scene, servicios);
            stage.setScene(scene);
            VentanaConfig.para(fxml).ifPresent(cfg -> {
                cfg.aplicar(stage);
                stage.setTitle(Ventanas.PREFIJO + cfg.titulo());
            });
            Ventanas.aplicarIcono(stage);
            T vista = loader.getController();
            if (vista != null) {
                vista.setServicios(servicios);
                vista.setNavegador(this);
                vistaActual = vista;
                if (onVistaCambio != null) {
                    onVistaCambio.accept(vista);
                }
                vista.alIniciar();
            }
            root.lookupAll(".primary-button, .menu-item").forEach(n ->
                    Microinteracciones.escalaSuave((javafx.scene.Node) n));
            return vista;
        } catch (IOException e) {
            throw new RuntimeException("No se pudo cargar la vista " + fxml, e);
        }
    }
}
