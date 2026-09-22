package cabofactu.vista;

import cabofactu.controlador.Controlador;
import cabofactu.vista.controlador.ArranqueController;
import cabofactu.vista.recursos.LocalizadorRecursos;
import cabofactu.vista.utilidades.Botones;
import cabofactu.vista.utilidades.Dialogos;
import cabofactu.vista.utilidades.GestorTemas;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;

/**
 * Vista de la aplicación. Es un singleton: solo existe un objeto, que se pide
 * con Vista.getInstancia() desde cualquier pantalla. Guarda el controlador, la
 * ventana en la que se muestran las pantallas y la pantalla actual.
 */
public class Vista {

    private static final String MENU = "MenuPrincipal.fxml";
    private static final String ARRANQUE = "Arranque.fxml";
    private static final String CONFIGURACION = "Configuracion.fxml";

    private static Vista instancia;
    private Controlador controlador;
    private Stage ventana;
    private Pantalla pantallaActual;

    private Vista() {
    }

    public static Vista getInstancia() {
        if (instancia == null) {
            instancia = new Vista();
        }
        return instancia;
    }

    public void setControlador(Controlador controlador) {
        if (controlador == null) {
            throw new IllegalArgumentException("El controlador no puede ser nulo.");
        }
        this.controlador = controlador;
    }

    public Controlador getControlador() {
        return controlador;
    }

    public Stage getVentana() {
        return ventana;
    }

    public Pantalla getPantallaActual() {
        return pantallaActual;
    }

    /**
     * Preparamos una ventana modal con el tema y el icono de la aplicación.
     * La devolvemos sin mostrar, para que quien la abre pueda darle sus datos
     * al controlador antes de llamar a showAndWait(). Al pulsar la X le
     * preguntamos a la pantalla del formulario si se puede cerrar.
     */
    public Stage crearVentanaModal(Parent raiz, String titulo, Pantalla pantalla) {
        Scene escena = new Scene(raiz);
        escena.getStylesheets().setAll(GestorTemas.hojas());
        Stage modal = new Stage();
        modal.initModality(Modality.APPLICATION_MODAL);
        modal.initOwner(ventana);
        modal.setTitle(Ventanas.PREFIJO + titulo);
        modal.setResizable(false);
        modal.setScene(escena);
        Ventanas.aplicarIcono(modal);
        modal.setOnCloseRequest(evento -> pedirCierreModal(evento, pantalla));
        return modal;
    }

    /** Si la pantalla del formulario no quiere cerrarse, anulamos el cierre. */
    private void pedirCierreModal(WindowEvent evento, Pantalla pantalla) {
        if (pantalla != null && !pantalla.puedeCerrar()) {
            evento.consume();
        }
    }

    /** Cambiamos la ventana donde se muestran las pantallas; todavía no tiene ninguna. */
    public void setVentana(Stage ventana) {
        this.ventana = ventana;
        this.pantallaActual = null;
    }

    /** Arrancamos JavaFX; el método vuelve cuando se cierra la última ventana. */
    public void comenzar() {
        LanzadorVentanaPrincipal.comenzar();
    }

    /** Menú principal si la empresa está completa; si no, Configuración, que se queda bloqueada. */
    public void mostrarInicio() {
        try {
            if (controlador.buscarEmpresa() == null) {
                mostrar(CONFIGURACION);
            } else {
                mostrar(MENU);
            }
        } catch (Exception e) {
            Dialogos.mostrarDialogoError("Empresa", e.getMessage());
        }
    }

    /** Ponemos la pantalla de arranque en esa ventana, sin mostrarla todavía. */
    public ArranqueController prepararArranque(Stage ventanaArranque) {
        setVentana(ventanaArranque);
        return (ArranqueController) mostrar(ARRANQUE);
    }

    /**
     * Cerramos la empresa en uso y volvemos a la pantalla de arranque. Abrimos la
     * ventana de arranque antes de cerrar la principal para que nunca se queden
     * cero ventanas, porque entonces JavaFX cerraría la aplicación.
     */
    public void volverAlArranque() {
        Stage principal = ventana;
        controlador.cerrarEmpresa();
        Stage arranque = new Stage();
        prepararArranque(arranque);
        arranque.show();
        principal.close();
    }

    /**
     * Cargamos la pantalla del FXML y la ponemos en la ventana. Si la pantalla
     * actual tiene cambios sin guardar y el usuario decide quedarse, no cambiamos
     * y devolvemos null. Si no, devolvemos su controlador, por si hay que pasarle
     * algún dato.
     *
     * Cómo funciona: al hacer loader.load(), JavaFX crea el controlador y llama a
     * su initialize(), donde la pantalla carga sus datos. En ese momento la
     * pantalla todavía no está en la ventana; por eso los atajos de teclado se
     * registran después, en alMostrar().
     */
    public Pantalla mostrar(String fxml) {
        if (pantallaActual != null && !pantallaActual.puedeCerrar()) {
            return null;
        }
        try {
            FXMLLoader loader = new FXMLLoader(LocalizadorRecursos.class.getResource(fxml));
            Parent raiz = loader.load();
            Scene escena = new Scene(raiz);
            GestorTemas.aplicar(escena);
            ventana.setScene(escena);
            ConfiguracionVentana configuracion = ConfiguracionVentana.para(fxml);
            if (configuracion != null) {
                configuracion.aplicar(ventana);
                ventana.setTitle(Ventanas.PREFIJO + configuracion.titulo());
            }
            Ventanas.aplicarIcono(ventana);
            Pantalla pantalla = loader.getController();
            pantallaActual = pantalla;
            if (pantalla != null) {
                pantalla.alMostrar();
            }
            raiz.applyCss();
            Botones.igualarGrupos(raiz);
            return pantalla;
        } catch (IOException e) {
            throw new RuntimeException("No se pudo cargar la pantalla " + fxml, e);
        }
    }

    /**
     * Abrimos la ventana principal (1024x768) con la primera pantalla. Al pulsar la
     * X o Salir preguntamos antes de cerrar.
     */
    public void abrirVentanaPrincipal() {
        Stage principal = new Stage();
        principal.setOnCloseRequest(e -> pedirCierre(e));
        setVentana(principal);
        mostrarInicio();
        principal.show();
    }

    /** Si el usuario no quiere salir, anulamos el cierre de la ventana. */
    private void pedirCierre(WindowEvent evento) {
        if (!puedeSalir()) {
            evento.consume();
        }
    }

    /** Las mismas preguntas de antes de salir: cambios sin guardar y confirmación. */
    private boolean puedeSalir() {
        if (pantallaActual != null && !pantallaActual.puedeCerrar()) {
            return false;
        }
        if (!Dialogos.mostrarDialogoConfirmacion("Salir", "¿Seguro que deseas salir de la aplicación?")) {
            return false;
        }
        if (pantallaActual != null) {
            pantallaActual.alCerrar();
        }
        return true;
    }

    /** Pedimos cerrar la ventana principal, igual que al pulsar la X. */
    public void salir() {
        ventana.fireEvent(new WindowEvent(ventana, WindowEvent.WINDOW_CLOSE_REQUEST));
    }
}
