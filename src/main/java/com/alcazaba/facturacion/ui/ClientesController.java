package com.alcazaba.facturacion.ui;

import com.alcazaba.facturacion.model.Cliente;
import com.alcazaba.facturacion.service.Servicios;
import com.alcazaba.facturacion.util.DocumentoFiscalValidator;
import javafx.application.Platform;
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
import javafx.scene.layout.GridPane;
import javafx.event.ActionEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

/**
 * Lista de clientes con alta/edicion de ficha (5.1) y reglas de borrado e
 * inactivacion (5.3): borrado fisico solo sin facturas; si las hay, se ofrece
 * marcar como inactivo. Los inactivos se muestran en la lista y no se ofrecen
 * al crear facturas nuevas.
 */
public class ClientesController implements Vista {

    private Servicios servicios;
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

    @Override
    public void setServicios(Servicios s) {
        this.servicios = s;
    }

    @Override
    public void setNavegador(Navegador n) {
        this.nav = n;
    }

    @Override
    public void alIniciar() {
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
            todos.setAll(servicios.clientes.listar(false));
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
            servicios.clientes.insertar(c);
            recargar();
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
            servicios.clientes.actualizar(c);
            recargar();
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
            if (servicios.clientes.tieneFacturas(seleccion.getId())) {
                if (Dialogos.confirmar("Cliente con facturas",
                        "El cliente \"" + seleccion.getNombre() + "\" tiene facturas asociadas y no puede eliminarse.\n\n"
                                + "¿Desea marcarlo como inactivo?")) {
                    servicios.clientes.setActivo(seleccion.getId(), false);
                    recargar();
                }
                return;
            }
            if (Dialogos.confirmar("Eliminar cliente",
                    "¿Eliminar definitivamente el cliente \"" + seleccion.getNombre() + "\"?")) {
                servicios.clientes.borrarFisico(seleccion.getId());
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

        ButtonType guardar = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialogo.getDialogPane().getButtonTypes().addAll(guardar, ButtonType.CANCEL);

        TextField txtNombre = new TextField();
        txtNombre.setPromptText("Nombre o razón social");
        txtNombre.setId("txtNombreFicha");
        TextField txtNif = new TextField();
        txtNif.setId("txtNifFicha");
        TextField txtDireccion = new TextField();
        TextField txtCp = new TextField();
        TextField txtLocalidad = new TextField();
        TextField txtProvincia = new TextField();
        CheckBox chkActivo = new CheckBox("Cliente activo");

        if (original != null) {
            txtNombre.setText(original.getNombre());
            txtNif.setText(original.getNif());
            txtDireccion.setText(original.getDireccion());
            txtCp.setText(original.getCp());
            txtLocalidad.setText(original.getLocalidad());
            txtProvincia.setText(original.getProvincia());
            chkActivo.setSelected(original.isActivo());
        } else {
            chkActivo.setSelected(true);
        }

        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(8);
        grid.addRow(0, new Label("Nombre*"), txtNombre);
        grid.addRow(1, new Label("NIF"), txtNif);
        grid.addRow(2, new Label("Dirección"), txtDireccion);
        grid.addRow(3, new Label("CP"), txtCp);
        grid.addRow(4, new Label("Localidad"), txtLocalidad);
        grid.addRow(5, new Label("Provincia"), txtProvincia);
        grid.add(chkActivo, 0, 6, 2, 1);
        dialogo.getDialogPane().setContent(grid);

        Node botonGuardar = dialogo.getDialogPane().lookupButton(guardar);
        botonGuardar.setId("btnGuardarFicha");
        botonGuardar.setDisable(true);
        txtNombre.textProperty().addListener((o, a, b) ->
                botonGuardar.setDisable(b == null || b.trim().isEmpty()));

        BooleanSupplier nifValido = () -> DocumentoFiscalValidator.esValido(txtNif.getText());
        boolean[] avisandoNif = {false};
        Runnable avisarNifInvalido = () -> {
            if (avisandoNif[0]) {
                return;
            }
            avisandoNif[0] = true;
            txtNif.setStyle("-fx-border-color: #d32f2f; -fx-border-width: 2;");
            Dialogos.error("NIF no válido", "Revise el DNI, NIE o NIF/CIF introducido.");
            avisandoNif[0] = false;
            Platform.runLater(txtNif::requestFocus);
        };
        txtNif.setOnAction(e -> {
            if (!nifValido.getAsBoolean()) {
                avisarNifInvalido.run();
            }
        });
        txtNif.focusedProperty().addListener((o, anterior, tieneFoco) -> {
            if (!tieneFoco && !avisandoNif[0] && !nifValido.getAsBoolean()) {
                avisarNifInvalido.run();
            } else if (tieneFoco || nifValido.getAsBoolean()) {
                txtNif.setStyle("");
            }
        });
        botonGuardar.addEventFilter(ActionEvent.ACTION, e -> {
            if (!nifValido.getAsBoolean()) {
                e.consume();
                avisarNifInvalido.run();
            }
        });

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
            c.setActivo(chkActivo.isSelected());
            return c;
        });

        return dialogo;
    }

    @FXML
    private void volver() {
        nav.mostrar("/com/alcazaba/facturacion/ui/MenuPrincipal.fxml");
    }
}
