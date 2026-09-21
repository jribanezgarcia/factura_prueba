package cabofactu.vista.controlador;

import cabofactu.modelo.dominio.Cliente;
import cabofactu.vista.Pantalla;
import cabofactu.vista.Vista;
import cabofactu.vista.recursos.LocalizadorRecursos;
import cabofactu.vista.utilidades.Dialogos;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

// Cómo funciona: en este controlador solo vive la tabla. La ficha está en
// FichaCliente.fxml con su propio controlador, y se abre como ventana modal;
// aquí esperamos a que se cierre para leer su resultado con getRegistro().

/**
 * Lista de clientes con el listado, el buscador y las reglas de borrado e
 * inactivación: borrado físico solo sin facturas; si las hay, se ofrece
 * marcar como inactivo. Los inactivos se muestran en la lista y no se
 * ofrecen al crear facturas nuevas.
 */
public class ClientesController implements Pantalla, Initializable {

    @FXML
    private TextField txtBusqueda;
    @FXML
    private Label lblConteo;
    @FXML
    private TableView<Cliente> tabla;
    @FXML
    private TableColumn<Cliente, String> colNombre;
    @FXML
    private TableColumn<Cliente, String> colNif;
    @FXML
    private TableColumn<Cliente, String> colLocalidad;
    @FXML
    private TableColumn<Cliente, String> colEstado;
    // JavaFX inyecta aquí el controlador del <fx:include fx:id="barra">;
    // el nombre del campo es el fx:id más "Controller".
    @FXML
    private BarraNavegacionController barraController;

    private Cliente registro;
    private String filtro;
    private ObservableList<Cliente> listaClientes;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        barraController.marcarActivo("clientes");
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colNif.setCellValueFactory(new PropertyValueFactory<>("nif"));
        colLocalidad.setCellValueFactory(new PropertyValueFactory<>("localidad"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estadoTexto"));
        tabla.setPlaceholder(new Label("No hay clientes."));
        listaClientes = FXCollections.observableArrayList();
        tabla.setItems(listaClientes);
        filtro = "";
        refrescarTabla();
    }

    @Override
    public void alMostrar() {
        Vista.getInstancia().getVentana().getScene().getAccelerators().put(
                new KeyCodeCombination(KeyCode.ESCAPE), () -> volver(null));
    }

    @FXML
    void seleccionar(MouseEvent event) {
        registro = tabla.getSelectionModel().getSelectedItem();
        if (event.getClickCount() == 2 && registro != null) {
            editar();
        }
    }

    @FXML
    void buscar(KeyEvent event) {
        filtro = txtBusqueda.getText().trim().toLowerCase();
        refrescarTabla();
    }

    @FXML
    void anadirCliente(ActionEvent event) {
        try {
            FichaClienteController ficha = abrirFicha(null, "Nuevo cliente");
            Cliente nuevo = ficha.getRegistro();
            if (nuevo != null) {
                Vista.getInstancia().getControlador().altaCliente(nuevo);
                refrescarTabla();
            }
        } catch (Exception e) {
            Dialogos.mostrarDialogoError("Clientes", e.getMessage());
        }
    }

    @FXML
    void editarCliente(ActionEvent event) {
        if (registro == null) {
            Dialogos.mostrarDialogoAdvertencia("Clientes", "Seleccione un cliente de la lista.");
            return;
        }
        editar();
    }

    @FXML
    void borrarCliente(ActionEvent event) {
        if (registro == null) {
            Dialogos.mostrarDialogoAdvertencia("Clientes", "Seleccione un cliente de la lista.");
            return;
        }
        try {
            if (Vista.getInstancia().getControlador().clienteTieneFacturas(registro.getId())) {
                if (Dialogos.mostrarDialogoConfirmacion("Cliente con facturas",
                        "El cliente \"" + registro.getNombre() + "\" tiene facturas asociadas y no puede "
                                + "eliminarse.\n\n¿Desea marcarlo como inactivo?")) {
                    Vista.getInstancia().getControlador().desactivarCliente(registro.getId());
                    refrescarTabla();
                }
                return;
            }
            if (Dialogos.mostrarDialogoConfirmacion("Eliminar cliente",
                    "¿Eliminar definitivamente el cliente \"" + registro.getNombre() + "\"?")) {
                Vista.getInstancia().getControlador().bajaCliente(registro.getId());
                refrescarTabla();
            }
        } catch (Exception e) {
            Dialogos.mostrarDialogoError("Clientes", e.getMessage());
        }
    }

    @FXML
    void volver(ActionEvent event) {
        Vista.getInstancia().mostrar("MenuPrincipal.fxml");
    }

    private void editar() {
        try {
            FichaClienteController ficha = abrirFicha(registro, "Editar cliente");
            Cliente modificado = ficha.getRegistro();
            if (modificado != null) {
                Vista.getInstancia().getControlador().modificarCliente(modificado);
                refrescarTabla();
            }
        } catch (Exception e) {
            Dialogos.mostrarDialogoError("Clientes", e.getMessage());
        }
    }

    /**
     * Abrimos la ficha en modo añadir (registro null) o en modo editar, y
     * esperamos a que se cierre.
     */
    private FichaClienteController abrirFicha(Cliente registro, String titulo) throws Exception {
        FXMLLoader cargador = new FXMLLoader(LocalizadorRecursos.class.getResource("FichaCliente.fxml"));
        Parent raiz = cargador.load();
        FichaClienteController ficha = cargador.getController();
        ficha.setRegistro(registro);
        Stage modal = Vista.getInstancia().crearVentanaModal(raiz, titulo, ficha);
        modal.showAndWait();
        return ficha;
    }

    private void refrescarTabla() {
        try {
            registro = null;
            tabla.getSelectionModel().clearSelection();
            List<Cliente> todos = Vista.getInstancia().getControlador().listadoClientes(false);
            listaClientes.setAll(filtrados(todos));
            lblConteo.setText(listaClientes.size() + " cliente(s)");
        } catch (Exception e) {
            Dialogos.mostrarDialogoError("Clientes", e.getMessage());
        }
    }

    private List<Cliente> filtrados(List<Cliente> todos) {
        List<Cliente> salida = new ArrayList<>();
        for (Cliente c : todos) {
            if (filtro.isEmpty()
                    || (c.getNombre() != null && c.getNombre().toLowerCase().contains(filtro))
                    || (c.getNif() != null && c.getNif().toLowerCase().contains(filtro))) {
                salida.add(c);
            }
        }
        return salida;
    }
}