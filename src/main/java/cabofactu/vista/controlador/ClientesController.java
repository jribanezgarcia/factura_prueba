package cabofactu.vista.controlador;

import cabofactu.modelo.dominio.Cliente;
import cabofactu.modelo.Modelo;
import cabofactu.modelo.negocio.ValidacionCliente;
import cabofactu.modelo.negocio.ValidacionException;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.HBox;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.event.ActionEvent;

import java.util.ArrayList;
import java.util.List;
import cabofactu.vista.Navegador;
import cabofactu.vista.Vista;
import cabofactu.vista.utilidades.BarraNavegacion;
import cabofactu.vista.utilidades.Dialogos;

/**
 * Lista de clientes con alta/edicion de ficha (5.1) y reglas de borrado e
 * inactivacion (5.3): borrado fisico solo sin facturas; si las hay, se ofrece
 * marcar como inactivo. Los inactivos se muestran en la lista y no se ofrecen
 * al crear facturas nuevas.
 */
public class ClientesController implements Vista {

    private Modelo modelo;
    private Navegador nav;
    private final ObservableList<Cliente> todos = FXCollections.observableArrayList();

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
    @FXML
    private HBox barraNavegacion;

    private TextField txtNombre;
    private TextField txtNif;
    private TextField txtDireccion;
    private TextField txtCp;
    private TextField txtLocalidad;
    private TextField txtProvincia;
    private TextField txtEmail;

    @Override
    public void setModelo(Modelo m) {
        this.modelo = m;
    }

    @Override
    public void setNavegador(Navegador n) {
        this.nav = n;
    }

    @Override
    public void alIniciar() {
        barraNavegacion.getChildren().add(BarraNavegacion.crear(nav, "clientes"));
        colNombre.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getNombre()));
        colNif.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getNif()));
        colLocalidad.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getLocalidad()));
        colEstado.setCellValueFactory(c ->
                new ReadOnlyObjectWrapper<>(c.getValue().isActivo() ? "Activo" : "Inactivo"));

        tabla.setPlaceholder(new Label("No hay clientes."));
        tabla.setRowFactory(tv -> {
            TableRow<Cliente> fila = new TableRow<>();
            fila.setOnMouseClicked(e -> {
                if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2 && !fila.isEmpty()) {
                    editar();
                }
            });
            return fila;
        });

        txtBusqueda.textProperty().addListener((o, a, b) -> filtrar());
        recargar();

        nav.stage().getScene().getAccelerators().put(
                new KeyCodeCombination(KeyCode.ESCAPE), this::volver);
    }

    private void recargar() {
        try {
            todos.setAll(modelo.getClientes().listar(false));
            filtrar();
        } catch (Exception e) {
            Dialogos.error("Clientes", "Error al cargar clientes: " + e.getMessage());
        }
    }

    private void filtrar() {
        String texto = txtBusqueda.getText() == null ? "" : txtBusqueda.getText().trim().toLowerCase();
        List<Cliente> filtrados = new ArrayList<>();
        for (Cliente c : todos) {
            if (texto.isEmpty()
                    || (c.getNombre() != null && c.getNombre().toLowerCase().contains(texto))
                    || (c.getNif() != null && c.getNif().toLowerCase().contains(texto))) {
                filtrados.add(c);
            }
        }
        tabla.setItems(FXCollections.observableArrayList(filtrados));
        lblConteo.setText(filtrados.size() + " cliente(s)");
    }

    @FXML
    private void nuevo() {
        Cliente c = fichaCliente(null);
        if (c == null) {
            return;
        }
        try {
            modelo.getClientes().insertar(c);
            recargar();
        } catch (ValidacionException e) {
            Dialogos.error("Datos del cliente", e.getMessage());
        } catch (Exception e) {
            Dialogos.error("Clientes", "No se pudo guardar el cliente: " + e.getMessage());
        }
    }

    @FXML
    private void editar() {
        Cliente seleccion = tabla.getSelectionModel().getSelectedItem();
        if (seleccion == null) {
            Dialogos.info("Clientes", "Seleccione un cliente de la lista.");
            return;
        }
        Cliente c = fichaCliente(seleccion);
        if (c == null) {
            return;
        }
        try {
            modelo.getClientes().actualizar(c);
            recargar();
        } catch (ValidacionException e) {
            Dialogos.error("Datos del cliente", e.getMessage());
        } catch (Exception e) {
            Dialogos.error("Clientes", "No se pudo actualizar el cliente: " + e.getMessage());
        }
    }

    @FXML
    private void eliminar() {
        Cliente seleccion = tabla.getSelectionModel().getSelectedItem();
        if (seleccion == null) {
            Dialogos.info("Clientes", "Seleccione un cliente de la lista.");
            return;
        }
        try {
            if (modelo.getClientes().tieneFacturas(seleccion.getId())) {
                if (Dialogos.confirmar("Cliente con facturas",
                        "El cliente \"" + seleccion.getNombre() + "\" tiene facturas asociadas y no puede eliminarse.\n\n"
                                + "¿Desea marcarlo como inactivo?")) {
                    modelo.getClientes().setActivo(seleccion.getId(), false);
                    recargar();
                }
                return;
            }
            if (Dialogos.confirmar("Eliminar cliente",
                    "¿Eliminar definitivamente el cliente \"" + seleccion.getNombre() + "\"?")) {
                modelo.getClientes().borrarFisico(seleccion.getId());
                recargar();
            }
        } catch (Exception e) {
            Dialogos.error("Clientes", "No se pudo eliminar el cliente: " + e.getMessage());
        }
    }

    private Cliente fichaCliente(Cliente original) {
        Dialog<Cliente> dialogo = construirFicha(original);
        return dialogo.showAndWait().orElse(null);
    }

    Dialog<Cliente> construirFicha(Cliente original) {
        Dialog<Cliente> dialogo = new Dialog<>();
        dialogo.setTitle(original == null ? "Nuevo cliente" : "Editar cliente");
        dialogo.setHeaderText(original == null ? "Alta de cliente" : "Datos del cliente");
        dialogo.initOwner(nav.stage());
        dialogo.getDialogPane().setPrefWidth(375);

        ButtonType guardar = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialogo.getDialogPane().getButtonTypes().addAll(guardar, ButtonType.CANCEL);

        txtNombre = new TextField();
        txtNombre.setPromptText("Nombre o razón social");
        txtNombre.setId("txtNombreFicha");
        txtNif = new TextField();
        txtNif.setId("txtNifFicha");
        txtDireccion = new TextField();
        txtDireccion.setId("txtDireccionFicha");
        txtCp = new TextField();
        txtCp.setId("txtCpFicha");
        txtLocalidad = new TextField();
        txtLocalidad.setId("txtLocalidadFicha");
        txtProvincia = new TextField();
        txtProvincia.setId("txtProvinciaFicha");
        txtEmail = new TextField();
        txtEmail.setId("txtEmailFicha");
        txtEmail.setPromptText("correo@ejemplo.es");
        CheckBox chkActivo = new CheckBox("Cliente activo");

        for (TextField t : List.of(txtNombre, txtNif, txtDireccion, txtCp, txtLocalidad, txtProvincia, txtEmail)) {
            t.setMaxWidth(Double.MAX_VALUE);
        }

        if (original != null) {
            txtNombre.setText(original.getNombre());
            txtNif.setText(original.getNif());
            txtDireccion.setText(original.getDireccion());
            txtCp.setText(original.getCp());
            txtLocalidad.setText(original.getLocalidad());
            txtProvincia.setText(original.getProvincia());
            txtEmail.setText(original.getEmail() == null ? "" : original.getEmail());
            chkActivo.setSelected(original.isActivo());
        } else {
            chkActivo.setSelected(true);
        }

        ColumnConstraints etiqueta = new ColumnConstraints();
        ColumnConstraints campo = new ColumnConstraints();
        campo.setHgrow(Priority.ALWAYS);
        campo.setFillWidth(true);

        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(8);
        grid.getColumnConstraints().addAll(etiqueta, campo);
        grid.addRow(0, new Label("Nombre*"), txtNombre);
        grid.addRow(1, new Label("NIF*"), txtNif);
        grid.addRow(2, new Label("Dirección*"), txtDireccion);
        grid.addRow(3, new Label("CP*"), txtCp);
        grid.addRow(4, new Label("Localidad*"), txtLocalidad);
        grid.addRow(5, new Label("Provincia*"), txtProvincia);
        grid.addRow(6, new Label("Email"), txtEmail);
        grid.add(chkActivo, 0, 7, 2, 1);
        dialogo.getDialogPane().setContent(grid);
        Dialogos.aplicarTema(dialogo.getDialogPane());

        Node botonGuardar = dialogo.getDialogPane().lookupButton(guardar);
        botonGuardar.setId("btnGuardarFicha");
        botonGuardar.setDisable(true);
        txtNombre.textProperty().addListener((o, a, b) ->
                botonGuardar.setDisable(b == null || b.trim().isEmpty()));

        botonGuardar.addEventFilter(ActionEvent.ACTION, e -> comprobarAntesDeGuardar(e));

        dialogo.setResultConverter(b -> {
            if (b != guardar) {
                return null;
            }
            Cliente c = original == null ? new Cliente() : original;
            c.setNombre(txtNombre.getText().trim());
            c.setNif(txtNif.getText().trim());
            c.setDireccion(txtDireccion.getText().trim());
            c.setCp(txtCp.getText().trim());
            c.setLocalidad(txtLocalidad.getText().trim());
            c.setProvincia(txtProvincia.getText().trim());
            String email = txtEmail.getText() == null ? "" : txtEmail.getText().trim();
            c.setEmail(email);
            c.setActivo(chkActivo.isSelected());
            return c;
        });

        return dialogo;
    }

    private void marcarCampo(TextField campo, String error) {
        if (error != null) {
            campo.setStyle("-fx-border-color: #d32f2f; -fx-border-width: 2;");
        } else {
            campo.setStyle("");
        }
    }

    private void marcarCamposFicha() {
        marcarCampo(txtNombre, ValidacionCliente.errorNombre(txtNombre.getText()));
        marcarCampo(txtNif, ValidacionCliente.errorNif(txtNif.getText()));
        marcarCampo(txtDireccion, ValidacionCliente.errorDireccion(txtDireccion.getText()));
        marcarCampo(txtCp, ValidacionCliente.errorCodigoPostal(txtCp.getText()));
        marcarCampo(txtLocalidad, ValidacionCliente.errorLocalidad(txtLocalidad.getText()));
        marcarCampo(txtProvincia, ValidacionCliente.errorProvincia(txtProvincia.getText()));
        marcarCampo(txtEmail, ValidacionCliente.errorEmail(txtEmail.getText()));
    }

    private boolean avisarPrimerErrorFicha() {
        String error = ValidacionCliente.errorNombre(txtNombre.getText());
        if (error != null) {
            Dialogos.error("Datos del cliente", error);
            return true;
        }
        error = ValidacionCliente.errorNif(txtNif.getText());
        if (error != null) {
            Dialogos.error("NIF no válido", error);
            return true;
        }
        error = ValidacionCliente.errorDireccion(txtDireccion.getText());
        if (error != null) {
            Dialogos.error("Datos del cliente", error);
            return true;
        }
        error = ValidacionCliente.errorCodigoPostal(txtCp.getText());
        if (error != null) {
            Dialogos.error("Código postal no válido", error);
            return true;
        }
        error = ValidacionCliente.errorLocalidad(txtLocalidad.getText());
        if (error != null) {
            Dialogos.error("Datos del cliente", error);
            return true;
        }
        error = ValidacionCliente.errorProvincia(txtProvincia.getText());
        if (error != null) {
            Dialogos.error("Datos del cliente", error);
            return true;
        }
        error = ValidacionCliente.errorEmail(txtEmail.getText());
        if (error != null) {
            Dialogos.error("Correo electrónico no válido", error);
            return true;
        }
        return false;
    }

    private void comprobarAntesDeGuardar(ActionEvent e) {
        marcarCamposFicha();
        if (avisarPrimerErrorFicha()) {
            e.consume();
        }
    }

    @FXML
    private void volver() {
        nav.mostrar("/cabofactu/vista/recursos/MenuPrincipal.fxml");
    }
}
