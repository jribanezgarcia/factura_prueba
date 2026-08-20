package com.alcazaba.facturacion.ui;

import com.alcazaba.facturacion.model.EstadoFactura;
import com.alcazaba.facturacion.model.FiltrosHistorial;
import com.alcazaba.facturacion.model.HistorialFila;
import com.alcazaba.facturacion.model.Serie;
import com.alcazaba.facturacion.service.Servicios;
import com.alcazaba.facturacion.util.Formatos;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;

import java.time.LocalDate;

/**
 * Historico: filtros combinables (serie, cliente/NIF, fechas, importes y
 * estado) con boton Buscar, una fila por version, y apertura de la version
 * seleccionada.
 */
public class HistoricoController implements Vista {

    private Servicios servicios;
    private Navegador nav;

    @FXML
    private ComboBox<String> comboSerie;
    @FXML
    private TextField txtCliente;
    @FXML
    private DatePicker fechaDesde;
    @FXML
    private DatePicker fechaHasta;
    @FXML
    private TextField txtImporteDesde;
    @FXML
    private TextField txtImporteHasta;
    @FXML
    private ComboBox<EstadoFactura> comboEstado;
    @FXML
    private HBox barraNavegacion;
    @FXML
    private TableView<HistorialFila> tabla;
    @FXML
    private TableColumn<HistorialFila, String> colFecha;
    @FXML
    private TableColumn<HistorialFila, String> colNumero;
    @FXML
    private TableColumn<HistorialFila, String> colVersion;
    @FXML
    private TableColumn<HistorialFila, String> colCliente;
    @FXML
    private TableColumn<HistorialFila, String> colNif;
    @FXML
    private TableColumn<HistorialFila, String> colBase;
    @FXML
    private TableColumn<HistorialFila, String> colIva;
    @FXML
    private TableColumn<HistorialFila, String> colTotal;
    @FXML
    private TableColumn<HistorialFila, String> colEstado;

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
        barraNavegacion.getChildren().add(BarraNavegacion.crear(nav, "historico"));
        try {
            comboSerie.getItems().add("(Todas)");
            for (Serie s : servicios.series.listar()) {
                comboSerie.getItems().add(s.getCodigo());
            }
            comboSerie.setValue("(Todas)");
        } catch (Exception e) {
            comboSerie.getItems().add("(Todas)");
            comboSerie.setValue("(Todas)");
        }
        comboEstado.getItems().setAll(null, EstadoFactura.EMITIDA, EstadoFactura.ANULADA);
        comboEstado.setValue(null);

        colFecha.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(Formatos.fecha(c.getValue().getFechaFactura())));
        colNumero.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getNumero()));
        colVersion.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(String.valueOf(c.getValue().getVersionNum())));
        colCliente.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getCliente()));
        colNif.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getNif()));
        colBase.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(Formatos.moneda(c.getValue().getBase())));
        colIva.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(Formatos.moneda(c.getValue().getIva())));
        colTotal.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(Formatos.moneda(c.getValue().getTotal())));
        colEstado.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(etiquetaEstado(c.getValue().getEstado())));

        tabla.setPlaceholder(new javafx.scene.control.Label("Sin resultados. Pulsa Buscar."));
        tabla.setRowFactory(tv -> {
            javafx.scene.control.TableRow<HistorialFila> fila = new javafx.scene.control.TableRow<>();
            fila.setOnMouseClicked(e -> {
                if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2 && !fila.isEmpty()) {
                    abrirVersion(fila.getItem());
                }
            });
            return fila;
        });

        nav.stage().getScene().getAccelerators().put(
                new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN), this::buscar);
    }

    private String etiquetaEstado(EstadoFactura e) {
        return e == EstadoFactura.ANULADA ? "Anulada" : e == EstadoFactura.EMITIDA ? "Emitida" : "";
    }

    @FXML
    private void buscar() {
        try {
            FiltrosHistorial f = new FiltrosHistorial();
            String serie = comboSerie.getValue();
            f.setSerieCodigo(serie == null || serie.startsWith("(") ? null : serie);
            f.setClienteTexto(txtCliente.getText());
            f.setFechaDesde(fechaDesde.getValue());
            f.setFechaHasta(fechaHasta.getValue());
            f.setImporteDesde(Formatos.parseMonedaOpcional(txtImporteDesde.getText()));
            f.setImporteHasta(Formatos.parseMonedaOpcional(txtImporteHasta.getText()));
            f.setEstado(comboEstado.getValue());
            tabla.setItems(FXCollections.observableArrayList(servicios.historialService.buscar(f)));
        } catch (Exception e) {
            Dialogos.error("Histórico", "Error al buscar: " + e.getMessage());
        }
    }

    private void abrirVersion(HistorialFila fila) {
        EditorController editor = nav.mostrar("/com/alcazaba/facturacion/ui/Editor.fxml");
        editor.cargarVersion(fila.getVersionId());
    }

    @FXML
    private void volver() {
        nav.mostrar("/com/alcazaba/facturacion/ui/MenuPrincipal.fxml");
    }
}
