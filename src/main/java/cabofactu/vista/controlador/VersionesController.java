package cabofactu.vista.controlador;

import cabofactu.modelo.dominio.EstadoFactura;
import cabofactu.modelo.dominio.VersionFactura;
import cabofactu.utilidades.Formatos;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseButton;

import java.util.List;
import java.net.URL;
import java.util.ResourceBundle;
import cabofactu.vista.Pantalla;
import cabofactu.vista.Vista;
import cabofactu.vista.utilidades.Dialogos;

/**
 * Versionado: lista de versiones de una factura. Cualquier version puede
 * abrirse (en la ultima si la factura esta Emitida se puede editar y al
 * guardar se crea una nueva version sin modificar la historica).
 */
public class VersionesController implements Pantalla, Initializable {

    @FXML
    private Label lblTitulo;
    // Cómo funciona: JavaFX inyecta aquí el controlador del <fx:include fx:id="barra">;
    // el nombre del campo es el fx:id más "Controller".
    @FXML
    private BarraNavegacionController barraController;
    @FXML
    private TableView<VersionFactura> tabla;
    @FXML
    private TableColumn<VersionFactura, String> colVersion;
    @FXML
    private TableColumn<VersionFactura, String> colFecha;
    @FXML
    private TableColumn<VersionFactura, String> colGuardado;
    @FXML
    private TableColumn<VersionFactura, String> colEstado;
    @FXML
    private TableColumn<VersionFactura, String> colTotal;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        barraController.marcarActivo("versiones");
        colVersion.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>("v" + c.getValue().getVersionNum()));
        colFecha.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(Formatos.fecha(c.getValue().getFechaFactura())));
        colGuardado.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(Formatos.fechaHora(c.getValue().getFechaGuardado())));
        colEstado.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(etiquetaEstado(c.getValue().getEstado())));
        colTotal.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(Formatos.moneda(c.getValue().getTotal())));

        tabla.setPlaceholder(new Label("La factura aún no tiene versiones."));
        tabla.setRowFactory(tv -> {
            TableRow<VersionFactura> fila = new TableRow<>();
            fila.setOnMouseClicked(e -> {
                if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2 && !fila.isEmpty()) {
                    abrirVersion(fila.getItem());
                }
            });
            return fila;
        });
    }

    @Override
    public void alMostrar() {
        Vista.getInstancia().getVentana().getScene().getAccelerators().put(
                new KeyCodeCombination(KeyCode.ESCAPE), () -> volver());
    }

    /**
     * Carga las versiones de la factura indicada y prepara el titulo.
     */
    public void cargarFactura(long facturaId) {
        try {
            List<VersionFactura> versiones = Vista.getInstancia().getControlador().getModelo().getVersiones().versionesDeFactura(facturaId);
            String numero = versiones.isEmpty() ? "" : versiones.get(versiones.size() - 1).getNumero();
            lblTitulo.setText("Versiones de la factura " + numero);
            tabla.setItems(FXCollections.observableArrayList(versiones));
        } catch (Exception e) {
            Dialogos.mostrarDialogoError("Versiones", "Error al cargar las versiones: " + e.getMessage());
        }
    }

    private void abrirVersion(VersionFactura v) {
        EditorController editor = (EditorController) Vista.getInstancia().mostrar("Editor.fxml");
        if (editor == null) {
            return;
        }
        editor.cargarVersion(v.getId());
    }

    private String etiquetaEstado(EstadoFactura e) {
        return e == EstadoFactura.ANULADA ? "Anulada" : e == EstadoFactura.EMITIDA ? "Emitida" : "";
    }

    @FXML
    private void volver() {
        Vista.getInstancia().mostrar("MenuPrincipal.fxml");
    }
}
