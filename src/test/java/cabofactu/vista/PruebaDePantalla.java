package cabofactu.vista;

import cabofactu.modelo.Modelo;
import cabofactu.modelo.negocio.Empresas;
import cabofactu.modelo.negocio.sqlite.CargarDemo;
import cabofactu.modelo.negocio.sqlite.Conexion;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * Base de las pruebas de pantalla: cada prueba empieza en una empresa de
 * demostración recién hecha, con su carpeta temporal, y maneja los controles
 * como lo haría el usuario. Al terminar se cierran las ventanas que queden.
 *
 * Cómo funciona: las búsquedas se hacen en el hilo del test, pero si una
 * ventana se está mostrando o no solo se mira en el hilo de JavaFX. Leído
 * desde fuera, ese dato sale obsoleto y las pruebas fallan unas veces sí y
 * otras no.
 */
class PruebaDePantalla extends ApplicationTest {

    @TempDir
    Path carpeta;

    private Stage ventana;

    @Override
    public void start(Stage ventana) throws Exception {
        this.ventana = ventana;
    }

    @BeforeEach
    void prepararEmpresa() throws Exception {
        Conexion.setCarpetaRaiz(carpeta);
        Conexion.cerrarConexion();
        CargarDemo.cargar();
        Empresas.getEmpresas().abrir(CargarDemo.CARPETA, LocalDate.now());
        PruebasJavaFx.prepararVista(new Modelo(), ventana);
    }

    @AfterEach
    void cerrarTodo() throws Exception {
        Conexion.cerrarConexion();
        FutureTask<Void> tarea = new FutureTask<>(() -> ocultarVentanas());
        Platform.runLater(tarea);
        tarea.get(30, TimeUnit.SECONDS);
    }

    private static Void ocultarVentanas() {
        for (Window abierta : new ArrayList<>(Window.getWindows())) {
            abierta.hide();
        }
        return null;
    }

    protected Pantalla mostrarPantalla(String fxml) throws Exception {
        FutureTask<Pantalla> tarea = new FutureTask<>(() -> enseñarPantalla(fxml));
        Platform.runLater(tarea);
        return tarea.get(30, TimeUnit.SECONDS);
    }

    private static Pantalla enseñarPantalla(String fxml) {
        Pantalla pantalla = Vista.getInstancia().mostrar(fxml);
        Vista.getInstancia().getVentana().show();
        return pantalla;
    }

    /**
     * Los nodos de esa búsqueda que se están viendo: en ventanas mostrándose
     * y sin ninguna sección oculta por encima. Las fichas y los avisos ya
     * cerrados siguen apareciendo en las búsquedas, y los fx:id se repiten
     * entre secciones, aunque compartan textos y controles.
     * Todo se mira en el hilo de JavaFX para no recorrer el grafo a medias
     * mientras se está moviendo.
     */
    protected List<Node> nodosVisibles(String consulta) throws Exception {
        FutureTask<List<Node>> tarea = new FutureTask<>(() -> buscarVisibles(consulta));
        Platform.runLater(tarea);
        return tarea.get(30, TimeUnit.SECONDS);
    }

    private static List<Node> buscarVisibles(String consulta) {
        List<Node> visibles = new ArrayList<>();
        for (Window ventana : new ArrayList<>(Window.getWindows())) {
            if (ventana.isShowing() && ventana.getScene() != null
                    && ventana.getScene().getRoot() != null) {
                Parent raiz = ventana.getScene().getRoot();
                if (consulta.startsWith("#") || consulta.startsWith(".")) {
                    for (Node nodo : raiz.lookupAll(consulta)) {
                        if (seVe(nodo)) {
                            visibles.add(nodo);
                        }
                    }
                } else {
                    for (Node nodo : raiz.lookupAll("*")) {
                        if (nodo instanceof Labeled && seVe(nodo)) {
                            Labeled etiqueta = (Labeled) nodo;
                            if (consulta.equals(etiqueta.getText())) {
                                visibles.add(nodo);
                            }
                        }
                    }
                }
            }
        }
        return visibles;
    }

    private static boolean seVe(Node nodo) {
        Node actual = nodo;
        while (actual != null) {
            if (!actual.isVisible()) {
                return false;
            }
            actual = actual.getParent();
        }
        return true;
    }

    /**
     * Buscamos un control por su fx:id o su texto, solo entre las ventanas que
     * se están mostrando.
     */
    protected <T extends Node> T buscar(String consulta, Class<T> tipo) throws Exception {
        for (Node nodo : nodosVisibles(consulta)) {
            if (tipo.isInstance(nodo)) {
                return tipo.cast(nodo);
            }
        }
        throw new AssertionError("No se encontró " + consulta);
    }

    /** Pulsamos el botón con ese texto, esté en la ventana que esté, si se ve. */
    protected void pulsar(String texto) throws Exception {
        for (Node nodo : nodosVisibles(texto)) {
            if (esPulsable(nodo, texto)) {
                clickOn(nodo);
                return;
            }
            if (nodo instanceof Labeled && texto.equals(((Labeled) nodo).getText())
                    && botonQueContiene(nodo) != null) {
                clickOn(botonQueContiene(nodo));
                return;
            }
        }
        fail("No se encontró el botón " + texto);
    }

    /**
     * El botón que contiene a ese control, si hay uno por encima. Los botones
     * del menú llevan su texto en una etiqueta de dentro, sin texto propio.
     */
    private static ButtonBase botonQueContiene(Node nodo) {
        Node actual = nodo.getParent();
        while (actual != null) {
            if (actual instanceof ButtonBase) {
                return (ButtonBase) actual;
            }
            actual = actual.getParent();
        }
        return null;
    }

    private boolean esPulsable(Node nodo, String texto) {
        if (nodo instanceof ButtonBase) {
            ButtonBase boton = (ButtonBase) nodo;
            return texto.equals(boton.getText());
        }
        if (nodo instanceof ListCell) {
            ListCell<?> celda = (ListCell<?>) nodo;
            Object textoCelda = celda.getText();
            return texto.equals(textoCelda) && !esCeldaDeBoton(celda);
        }
        return false;
    }

    /**
     * Las celdas que enseñan el valor elegido dentro del desplegable no se
     * pulsan: solo las de la lista que se abre al pulsarlo, que viven dentro
     * de un ListView. Si no, al elegir valor se pulsa el propio desplegable.
     */
    private static boolean esCeldaDeBoton(ListCell<?> celda) {
        Node actual = celda.getParent();
        while (actual != null) {
            if (actual instanceof ListView) {
                return false;
            }
            if (actual instanceof ComboBox) {
                return true;
            }
            actual = actual.getParent();
        }
        return false;
    }

    /**
     * Pulsamos el botón con ese texto dentro de esa sección, para no confundirlo
     * con los botones iguales de las secciones que están ocultas.
     */
    protected void pulsarBoton(String seccion, String texto) throws Exception {
        asentarVentanas();
        for (Node nodo : nodosVisibles(seccion + " .button")) {
            if (nodo instanceof Button) {
                Button boton = (Button) nodo;
                if (boton.getText().equals(texto)) {
                    clickOn(boton);
                    return;
                }
            }
        }
        fail("No se encontró el botón " + texto);
    }

    /** Escribimos en el campo sustituyendo lo que tuviera, como al editar. */
    protected void escribirEn(String campo, String texto) throws Exception {
        asentarVentanas();
        escribirEnCampo(buscar(campo, TextField.class), texto);
    }

    /**
     * Escribimos en el campo del aviso que pide un texto, como el nombre de
     * la empresa nueva. Solo hay un campo por aviso.
     */
    protected void escribirEnAviso(String texto) throws Exception {
        escribirEnCampo(buscar(".dialog-pane .text-field", TextField.class), texto);
    }

    private void escribirEnCampo(TextField caja, String texto) throws Exception {
        clickOn(caja);
        FutureTask<Void> tarea = new FutureTask<>(() -> seleccionarTodo(caja));
        Platform.runLater(tarea);
        tarea.get(30, TimeUnit.SECONDS);
        write(texto);
    }

    private static Void seleccionarTodo(TextField caja) {
        caja.selectAll();
        return null;
    }

    /**
     * Forzamos el maquetado de las ventanas abiertas para que los clics caigan
     * en su sitio. Una ventana recién abierta está mostrándose pero aún sin
     * medir, y el robot pulsaría en las coordenadas viejas.
     */
    protected void asentarVentanas() throws Exception {
        FutureTask<Void> tarea = new FutureTask<>(() -> maquetarVentanas());
        Platform.runLater(tarea);
        tarea.get(30, TimeUnit.SECONDS);
    }

    private static Void maquetarVentanas() {
        for (Window abierta : new ArrayList<>(Window.getWindows())) {
            if (abierta.isShowing() && abierta.getScene() != null
                    && abierta.getScene().getRoot() != null) {
                abierta.getScene().getRoot().applyCss();
                abierta.getScene().getRoot().layout();
            }
        }
        return null;
    }

    /** Esperamos a que aparezca un nodo, por ejemplo al abrirse una ventana. */
    protected void esperarNodo(String consulta, Class<? extends Node> tipo) throws Exception {
        WaitForAsyncUtils.waitFor(30, TimeUnit.SECONDS, () -> hayNodo(consulta, tipo));
    }

    private boolean hayNodo(String consulta, Class<? extends Node> tipo) throws Exception {
        for (Node nodo : nodosVisibles(consulta)) {
            if (tipo.isInstance(nodo)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Cerramos el aviso que esté abierto pulsando su botón por defecto, y
     * esperamos a que se cierre. Los clics del robot no llegan a los botones
     * de los avisos en modo headless; la tecla sí. Hay que esperar al cierre
     * porque los eventos que se lanzan durante la transición se pierden.
     */
    protected void cerrarAviso() throws Exception {
        contestarAviso();
    }

    /**
     * Aceptamos la pregunta del aviso con su botón de Aceptar, que es el de
     * por defecto, y esperamos a que se cierre. Los avisos solo se contestan
     * desde aquí o desde cerrarAviso, nunca pulsando sus botones a mano.
     */
    protected void aceptarAviso() throws Exception {
        contestarAviso();
    }

    private void contestarAviso() throws Exception {
        WaitForAsyncUtils.waitFor(30, TimeUnit.SECONDS, () -> hayAviso());
        String texto = textoAvisoSinEsperar();
        clickOn(buscar(".dialog-pane", DialogPane.class));
        push(KeyCode.ENTER);
        // Tras contestar puede venir otro aviso encadenado sin hueco entre
        // medias, así que seguimos tanto si se cierra como si cambia el texto.
        WaitForAsyncUtils.waitFor(30, TimeUnit.SECONDS, () -> !hayAviso()
                || !textoAvisoSinEsperar().equals(texto));
    }

    /**
     * Cerramos el aviso pulsando su botón de cancelar, con la tecla Esc, y
     * esperamos a que se cierre o a que cambie el texto por otro aviso.
     */
    protected void cancelarAviso() throws Exception {
        WaitForAsyncUtils.waitFor(30, TimeUnit.SECONDS, () -> hayAviso());
        String texto = textoAvisoSinEsperar();
        clickOn(buscar(".dialog-pane", DialogPane.class));
        push(KeyCode.ESCAPE);
        WaitForAsyncUtils.waitFor(30, TimeUnit.SECONDS, () -> !hayAviso()
                || !textoAvisoSinEsperar().equals(texto));
    }

    /**
     * Escribimos en una celda de la tabla de líneas como lo haría el usuario:
     * pulsamos la celda, Enter para abrirla, escribimos y Enter para aplicar y
     * saltar. La columna se pide por su fx:id.
     */
    protected void escribirEnCelda(int fila, String columna, String texto) throws Exception {
        asentarVentanas();
        if (!editandoCeldaEn(fila, columna)) {
            Node celda = buscarCeldaLinea(fila, columna);
            clickOn(celda);
            push(KeyCode.ENTER);
            if (!editandoCelda()) {
                doubleClickOn(celda);
                push(KeyCode.ENTER);
            }
        }
        write(texto);
        push(KeyCode.ENTER);
    }

    private boolean editandoCeldaEn(int fila, String columna) throws Exception {
        FutureTask<Boolean> tarea = new FutureTask<>(() -> hayEdicionEn(fila, columna));
        Platform.runLater(tarea);
        return tarea.get(30, TimeUnit.SECONDS);
    }

    private static Boolean hayEdicionEn(int fila, String columna) {
        String id = columna;
        if (id.startsWith("#")) {
            id = id.substring(1);
        }
        for (Window abierta : new ArrayList<>(Window.getWindows())) {
            if (!abierta.isShowing() || abierta.getScene() == null
                    || abierta.getScene().getRoot() == null) {
                continue;
            }
            for (Node nodo : abierta.getScene().getRoot().lookupAll("#tablaLineas")) {
                if (nodo instanceof TableView && seVe(nodo)) {
                    TableView<?> tabla = (TableView<?>) nodo;
                    if (tabla.getEditingCell() == null) {
                        return false;
                    }
                    if (tabla.getEditingCell().getRow() != fila) {
                        return false;
                    }
                    TableColumn<?, ?> col = tabla.getEditingCell().getTableColumn();
                    if (col == null || !id.equals(col.getId())) {
                        return false;
                    }
                    return true;
                }
            }
        }
        return false;
    }

    private Node buscarCeldaLinea(int fila, String columna) throws Exception {
        FutureTask<Node> tarea = new FutureTask<>(() -> celdaLinea(fila, columna));
        Platform.runLater(tarea);
        return tarea.get(30, TimeUnit.SECONDS);
    }

    private static Node celdaLinea(int fila, String columna) {
        String id = columna;
        if (id.startsWith("#")) {
            id = id.substring(1);
        }
        for (Window abierta : new ArrayList<>(Window.getWindows())) {
            if (!abierta.isShowing() || abierta.getScene() == null
                    || abierta.getScene().getRoot() == null) {
                continue;
            }
            for (Node nodo : abierta.getScene().getRoot().lookupAll("#tablaLineas .table-cell")) {
                if (!(nodo instanceof TableCell)) {
                    continue;
                }
                TableCell<?, ?> celda = (TableCell<?, ?>) nodo;
                if (celda.getIndex() != fila) {
                    continue;
                }
                TableColumn<?, ?> tablaColumna = celda.getTableColumn();
                if (tablaColumna == null) {
                    continue;
                }
                if (id.equals(tablaColumna.getId()) && seVe(nodo)) {
                    return nodo;
                }
            }
        }
        throw new AssertionError("No se encontró la celda " + fila + " " + columna);
    }

    private static TableView<?> buscarTablaLineas(Window abierta) {
        for (Node nodo : abierta.getScene().getRoot().lookupAll("#tablaLineas")) {
            if (nodo instanceof TableView && seVe(nodo)) {
                return (TableView<?>) nodo;
            }
        }
        return null;
    }

    private boolean editandoCelda() throws Exception {
        FutureTask<Boolean> tarea = new FutureTask<>(() -> hayEdicion());
        Platform.runLater(tarea);
        return tarea.get(30, TimeUnit.SECONDS);
    }

    private static Boolean hayEdicion() {
        for (Window abierta : new ArrayList<>(Window.getWindows())) {
            if (!abierta.isShowing() || abierta.getScene() == null
                    || abierta.getScene().getRoot() == null) {
                continue;
            }
            for (Node nodo : abierta.getScene().getRoot().lookupAll("#tablaLineas")) {
                if (nodo instanceof TableView && seVe(nodo)) {
                    TableView<?> tabla = (TableView<?>) nodo;
                    return tabla.getEditingCell() != null;
                }
            }
        }
        return false;
    }

    private boolean hayAviso() throws Exception {
        return !nodosVisibles(".dialog-pane").isEmpty();
    }

    /**
     * El texto que muestra el aviso abierto, juntando sus etiquetas. El mensaje
     * va en una etiqueta propia, no en el contentText del diálogo. Espera a que
     * el aviso esté abierto, porque a veces tarda en salir.
     */
    protected String textoAviso() throws Exception {
        WaitForAsyncUtils.waitFor(30, TimeUnit.SECONDS, () -> hayAviso());
        return textoAvisoSinEsperar();
    }

    private String textoAvisoSinEsperar() throws Exception {
        String texto = "";
        for (Node nodo : nodosVisibles(".dialog-pane .label")) {
            if (nodo instanceof Label) {
                Label etiqueta = (Label) nodo;
                texto = texto + etiqueta.getText();
            }
        }
        return texto;
    }
}
