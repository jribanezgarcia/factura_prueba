package com.alcazaba.facturacion;

import com.alcazaba.facturacion.service.Servicios;
import com.alcazaba.facturacion.ui.ArranqueController;
import com.alcazaba.facturacion.ui.Dialogos;
import com.alcazaba.facturacion.ui.Navegador;
import com.alcazaba.facturacion.ui.Ventanas;
import com.alcazaba.facturacion.ui.Vista;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Locale;

/**
 * Punto de entrada de la aplicación. Fija el idioma español, prepara la carpeta
 * de datos, asegura que solo haya una ventana abierta, muestra la pantalla de
 * arranque (empresa y fecha de trabajo) y, al entrar, construye los servicios y
 * abre la primera pantalla. Cada paso delega en su pieza: PreparacionDatos,
 * InstanciaUnica, ArranqueController y Navegador.
 */
public class Main extends Application {

    private Vista actual;
    private Servicios servicios;
    private Navegador nav;
    private Stage stage;
    private ArranqueController arranque;

    public static void main(String[] args) {
        Locale.setDefault(new Locale("es", "ES"));
        launch(args);
    }

    /** Arranca en orden: carpeta, instancia única, ventana, demostración y arranque. */
    @Override
    public void start(Stage stage) {
        this.stage = stage;
        try {
            PreparacionDatos.crearCarpeta();
        } catch (IOException e) {
            Dialogos.error("Facturación", "No se pudo preparar la carpeta de datos:\n" + e.getMessage());
            Platform.exit();
            return;
        }
        try {
            if (!InstanciaUnica.adquirir()) {
                Dialogos.error("Facturación", "La aplicación ya está en ejecución.\nSolo puede abrirse una instancia.");
                Platform.exit();
                return;
            }
        } catch (IOException e) {
            Dialogos.error("Facturación", "No se pudo comprobar si la aplicación ya está abierta:\n" + e.getMessage());
            Platform.exit();
            return;
        }
        Platform.setImplicitExit(false);
        configurarVentana();
        boolean demoCargada = false;
        try {
            demoCargada = PreparacionDatos.cargarDemoSiNoHayEmpresas();
        } catch (Exception e) {
            Dialogos.error("Facturación", "No se pudo cargar la empresa de demostración:\n" + e.getMessage());
        }
        mostrarArranque();
        stage.show();
        boolean demo = demoCargada;
        Platform.runLater(() -> arranque.mostrarAvisoInicial(demo));
    }

    /** Pone título, icono y confirmación de cierre a la ventana. */
    private void configurarVentana() {
        stage.setTitle(Ventanas.PREFIJO + "Seleccion de empresa");
        Ventanas.aplicarIcono(stage);
        stage.setOnCloseRequest(e -> {
            if (!cerrarAplicacion()) {
                e.consume();
            }
        });
    }

    /** Muestra la pantalla de arranque y deriva la entrada a entrarEnMenu. */
    private void mostrarArranque() {
        Navegador navArranque = new Navegador(stage, servicios);
        arranque = navArranque.mostrar("/com/alcazaba/facturacion/ui/Arranque.fxml");
        arranque.setOnEntrar(e -> entrarEnMenu());
    }

    /** Construye los servicios tras conectar la empresa y abre la primera pantalla. */
    private void entrarEnMenu() {
        try {
            servicios = new Servicios();
        } catch (Exception e) {
            Dialogos.error("Facturación", "Error al inicializar la aplicación:\n" + e.getMessage());
            return;
        }
        stage.hide();
        nav = new Navegador(stage, servicios);
        nav.setOnVistaCambio(v -> this.actual = v);
        nav.mostrarInicio();
        stage.show();
    }

    /**
     * Devuelve true si la aplicacion se cierra. Si el usuario cancela, la
     * ventana debe seguir visible y quien llama consume el evento.
     */
    private boolean cerrarAplicacion() {
        if (actual != null && !actual.puedeCerrar()) {
            return false;
        }
        if (actual != null && !Dialogos.confirmar("Salir", "¿Seguro que deseas salir de la aplicación?")) {
            return false;
        }
        if (actual != null) {
            actual.alCerrar();
        }
        InstanciaUnica.liberar();
        Platform.exit();
        return true;
    }
}
