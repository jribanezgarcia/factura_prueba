package com.alcazaba.facturacion.ui;

import com.alcazaba.facturacion.model.Cliente;
import com.alcazaba.facturacion.model.Serie;
import com.alcazaba.facturacion.model.TipoIva;
import com.alcazaba.facturacion.model.TipoRetencion;
import com.alcazaba.facturacion.service.FacturacionMensualService;
import com.alcazaba.facturacion.service.Servicios;
import com.alcazaba.facturacion.service.ValidationException;
import com.alcazaba.facturacion.util.Formatos;
import javafx.beans.property.BooleanProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Spinner;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class GenerarFacturasMensualesController {

    private Servicios servicios;
    private Stage stage;

    private final ObservableList<LineaDialogo> lineas = FXCollections.observableArrayList();

    @FXML
    private ComboBox<Cliente> comboCliente;
    @FXML
    private ComboBox<Serie> comboSerie;
    @FXML
    private Spinner<Integer> spinnerAnio;
    @FXML
    private ComboBox<Integer> comboMesInicio;
    @FXML
    private ComboBox<Integer> comboMesFin;
    @FXML
    private Spinner<Integer> spinnerDia;
    @FXML
    private RadioButton radioDiaFijo;
    @FXML
    private RadioButton radioPrimerDia;
    @FXML
    private RadioButton radioUltimoDia;
    @FXML
    private ComboBox<TipoIva> comboIva;
    @FXML
    private ComboBox<TipoRetencion> comboRetencion;
    @FXML
    private TableView<LineaDialogo> tablaLineas;
    @FXML
    private TableColumn<LineaDialogo, Number> colCantidad;
    @FXML
    private TableColumn<LineaDialogo, String> colDescripcion;
    @FXML
    private TableColumn<LineaDialogo, BigDecimal> colPrecio;
    @FXML
    private TableColumn<LineaDialogo, Boolean> colAnadirMes;
    @FXML
    private Button btnGenerar;
    @FXML
    private Button btnAnadirLinea;
    @FXML
    private Button btnEliminarLinea;
    @FXML
    private Label lblInfo;

    public void setServicios(Servicios servicios) {
        this.servicios = servicios;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public static void abrir(Navegador nav) {
        try {
            FXMLLoader loader = new FXMLLoader(GenerarFacturasMensualesController.class.getResource(
                    "/com/alcazaba/facturacion/ui/GenerarFacturasMensuales.fxml"));
            Parent root = loader.load();
            GenerarFacturasMensualesController c = loader.getController();
            c.setServicios(nav.servicios());
            Stage dialog = new Stage();
            dialog.initOwner(nav.stage());
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle("Generar facturas mensuales");
            Scene scene = new Scene(root);
            ThemeManager.aplicar(scene, nav.servicios());
            dialog.setScene(scene);
            VentanaConfig.para("/com/alcazaba/facturacion/ui/GenerarFacturasMensuales.fxml")
                    .ifPresent(cfg -> cfg.aplicar(dialog));
            c.setStage(dialog);
            c.alIniciar();
            dialog.showAndWait();
        } catch (IOException e) {
            Dialogos.error("Diálogo", "No se pudo abrir el diálogo: " + e.getMessage());
        }
    }

    @FXML
    public void initialize() {
        configurarTabla();
    }

    public void alIniciar() {
        cargarClientes();
        cargarSeries();
        cargarMeses();
        cargarIvas();
        cargarRetenciones();
        configurarSpinners();
        configurarDiaDelMes();
        comboMesInicio.setValue(1);
        comboMesFin.setValue(12);
        lineas.add(new LineaDialogo(1, "", BigDecimal.ZERO, true));
        actualizarInfoBoton();
    }

    private void cargarClientes() {
        try {
            List<Cliente> activos = servicios.clientes.listar(true);
            comboCliente.getItems().setAll(activos);
            comboCliente.setConverter(new StringConverter<>() {
                @Override
                public String toString(Cliente c) {
                    return c == null ? "" : c.nombreNif();
                }

                @Override
                public Cliente fromString(String s) {
                    return null;
                }
            });
        } catch (Exception e) {
            Dialogos.error("Clientes", "Error al cargar clientes: " + e.getMessage());
        }
    }

    private void cargarSeries() {
        try {
            List<Serie> series = new ArrayList<>();
            for (Serie s : servicios.series.listar()) {
                if (!s.isEsRectificativa()) {
                    series.add(s);
                }
            }
            comboSerie.getItems().setAll(series);
            comboSerie.setConverter(new StringConverter<>() {
                @Override
                public String toString(Serie s) {
                    return s == null ? "" : s.toString();
                }

                @Override
                public Serie fromString(String s) {
                    return null;
                }
            });
        } catch (Exception e) {
            Dialogos.error("Series", "Error al cargar series: " + e.getMessage());
        }
    }

    private void cargarMeses() {
        List<Integer> meses = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            meses.add(i);
        }
        comboMesInicio.getItems().setAll(meses);
        comboMesFin.getItems().setAll(meses);
        StringConverter<Integer> converter = new StringConverter<>() {
            @Override
            public String toString(Integer m) {
                return m == null ? "" : Month.of(m).getDisplayName(java.time.format.TextStyle.FULL, new Locale("es", "ES"));
            }

            @Override
            public Integer fromString(String s) {
                return null;
            }
        };
        comboMesInicio.setConverter(converter);
        comboMesFin.setConverter(converter);
    }

    private void cargarIvas() {
        try {
            comboIva.getItems().setAll(servicios.ivas.listar(true));
            comboIva.setConverter(new StringConverter<>() {
                @Override
                public String toString(TipoIva t) {
                    return t == null ? "" : t.toString();
                }

                @Override
                public TipoIva fromString(String s) {
                    return null;
                }
            });
        } catch (Exception e) {
            Dialogos.error("IVA", "Error al cargar tipos de IVA: " + e.getMessage());
        }
    }

    private void cargarRetenciones() {
        try {
            TipoRetencion sin = new TipoRetencion();
            sin.setId(null);
            sin.setNombre("Sin retención");
            sin.setPorcentaje(0);
            List<TipoRetencion> items = new ArrayList<>();
            items.add(sin);
            items.addAll(servicios.retenciones.listar(true));
            comboRetencion.getItems().setAll(items);
            comboRetencion.setConverter(new StringConverter<>() {
                @Override
                public String toString(TipoRetencion t) {
                    return t == null ? "" : t.toString();
                }

                @Override
                public TipoRetencion fromString(String s) {
                    return null;
                }
            });
            comboRetencion.setValue(sin);
        } catch (Exception e) {
            Dialogos.error("Retenciones", "Error al cargar retenciones: " + e.getMessage());
        }
    }

    private void configurarSpinners() {
        int anioActual = LocalDate.now().getYear();
        spinnerAnio.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(anioActual - 5, anioActual + 10, anioActual));
        spinnerDia.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 31, 15));
        spinnerDia.setEditable(true);
    }

    private void configurarDiaDelMes() {
        ToggleGroup grupo = new ToggleGroup();
        radioDiaFijo.setToggleGroup(grupo);
        radioPrimerDia.setToggleGroup(grupo);
        radioUltimoDia.setToggleGroup(grupo);
        radioDiaFijo.setSelected(true);
        actualizarEstadoDia();
        grupo.selectedToggleProperty().addListener((o, a, b) -> actualizarEstadoDia());
    }

    private void actualizarEstadoDia() {
        boolean fijo = radioDiaFijo.isSelected();
        spinnerDia.setDisable(!fijo);
    }

    private void configurarTabla() {
        tablaLineas.setItems(lineas);
        tablaLineas.setEditable(true);

        colCantidad.setCellValueFactory(c -> c.getValue().cantidadProperty());
        colCantidad.setCellFactory(c -> new TextFieldTableCell<>(new StringConverter<>() {
            @Override
            public String toString(Number n) {
                return n == null ? "1" : String.valueOf(n.intValue());
            }

            @Override
            public Number fromString(String s) {
                try {
                    return Integer.parseInt(s.trim());
                } catch (NumberFormatException e) {
                    return 1;
                }
            }
        }));
        colCantidad.setOnEditCommit(e -> {
            int v = e.getNewValue() == null ? 1 : Math.max(1, e.getNewValue().intValue());
            e.getRowValue().setCantidad(v);
            actualizarInfoBoton();
        });

        colDescripcion.setCellValueFactory(c -> c.getValue().descripcionProperty());
        colDescripcion.setCellFactory(TextFieldTableCell.forTableColumn());
        colDescripcion.setOnEditCommit(e -> e.getRowValue().setDescripcion(e.getNewValue()));

        colPrecio.setCellValueFactory(c -> c.getValue().precioProperty());
        colPrecio.setCellFactory(c -> new TextFieldTableCell<>(new StringConverter<>() {
            @Override
            public String toString(BigDecimal b) {
                return b == null ? "0,00" : Formatos.moneda(b);
            }

            @Override
            public BigDecimal fromString(String s) {
                BigDecimal v = Formatos.parseEntrada(s);
                return v == null ? BigDecimal.ZERO : v;
            }
        }));
        colPrecio.setOnEditCommit(e -> {
            BigDecimal v = e.getNewValue() == null ? BigDecimal.ZERO : e.getNewValue();
            e.getRowValue().setPrecioUnitario(v);
            actualizarInfoBoton();
        });

        colAnadirMes.setCellValueFactory(c -> c.getValue().anadirMesProperty());
        colAnadirMes.setCellFactory(CheckBoxTableCell.forTableColumn(colAnadirMes));
    }

    @FXML
    private void anadirLinea() {
        lineas.add(new LineaDialogo(1, "", BigDecimal.ZERO, true));
        actualizarInfoBoton();
    }

    @FXML
    private void eliminarLinea() {
        LineaDialogo sel = tablaLineas.getSelectionModel().getSelectedItem();
        if (sel == null) {
            return;
        }
        lineas.remove(sel);
        if (lineas.isEmpty()) {
            lineas.add(new LineaDialogo(1, "", BigDecimal.ZERO, true));
        }
        actualizarInfoBoton();
    }

    @FXML
    private void generar() {
        Cliente cliente = comboCliente.getValue();
        if (cliente == null) {
            Dialogos.error("Generar", "Seleccione un cliente.");
            return;
        }
        Serie serie = comboSerie.getValue();
        if (serie == null) {
            Dialogos.error("Generar", "Seleccione una serie.");
            return;
        }
        TipoIva iva = comboIva.getValue();
        if (iva == null) {
            Dialogos.error("Generar", "Seleccione un tipo de IVA.");
            return;
        }
        Integer mesInicio = comboMesInicio.getValue();
        Integer mesFin = comboMesFin.getValue();
        if (mesInicio == null || mesFin == null || mesInicio > mesFin) {
            Dialogos.error("Generar", "El mes de inicio debe ser anterior o igual al mes de fin.");
            return;
        }
        List<FacturacionMensualService.LineaPlantilla> plantillas = new ArrayList<>();
        for (LineaDialogo l : lineas) {
            if (l.getDescripcion() == null || l.getDescripcion().isBlank()) {
                continue;
            }
            plantillas.add(new FacturacionMensualService.LineaPlantilla(
                    l.getCantidad(), l.getDescripcion().trim(), l.getPrecioUnitario(), l.isAnadirMes()));
        }
        if (plantillas.isEmpty()) {
            Dialogos.error("Generar", "Añada al menos una línea con descripción.");
            return;
        }
        TipoRetencion retencion = comboRetencion.getValue();
        if (retencion == null || retencion.getId() == null) {
            retencion = null;
        }

        boolean generarDuplicados = false;
        List<String> duplicados = List.of();
        try {
            duplicados = servicios.facturacionMensual.detectarDuplicados(
                    cliente, spinnerAnio.getValue(), mesInicio, mesFin);
            if (!duplicados.isEmpty()) {
                String mensaje = "Ya existen facturas para este cliente en:\n\n"
                        + String.join(", ", duplicados)
                        + "\n\n¿Deseas generar las facturas de todos modos?";
                if (!Dialogos.confirmar("Meses con facturas", mensaje)) {
                    return;
                }
                generarDuplicados = true;
            }
        } catch (Exception e) {
            Dialogos.error("Generar", "Error al comprobar duplicados: " + e.getMessage());
            return;
        }

        FacturacionMensualService.DiaMode diaMode;
        int diaFijo = 15;
        if (radioPrimerDia.isSelected()) {
            diaMode = FacturacionMensualService.DiaMode.PRIMER_DIA;
        } else if (radioUltimoDia.isSelected()) {
            diaMode = FacturacionMensualService.DiaMode.ULTIMO_DIA;
        } else {
            diaMode = FacturacionMensualService.DiaMode.FIJO;
            Integer v = spinnerDia.getValue();
            diaFijo = v == null ? 15 : Math.max(1, Math.min(31, v));
        }

        int cantidadMeses = mesFin - mesInicio + 1;
        int mesesAGenerar = generarDuplicados ? cantidadMeses : cantidadMeses;
        if (!generarDuplicados) {
            mesesAGenerar = cantidadMeses - duplicados.size();
        }

        boolean usarHuecos = false;
        try {
            if (mesesAGenerar > 0) {
                List<Integer> conHuecos = servicios.numeros.proponerNumeros(
                        serie, spinnerAnio.getValue(), mesesAGenerar, true);
                List<Integer> sinHuecos = servicios.numeros.proponerNumeros(
                        serie, spinnerAnio.getValue(), mesesAGenerar, false);
                if (!conHuecos.equals(sinHuecos)) {
                    String numeros = conHuecos.stream()
                            .map(String::valueOf)
                            .collect(Collectors.joining(", "));
                    usarHuecos = Dialogos.confirmar("Huecos de numeración",
                            "Hay huecos disponibles en la numeración. ¿Quieres usarlos?\n\n"
                                    + "Números propuestos: " + numeros);
                }
            }
        } catch (Exception e) {
            Dialogos.error("Generar", "Error al calcular la numeración: " + e.getMessage());
            return;
        }

        try {
            FacturacionMensualService.Resultado r = servicios.facturacionMensual.generar(
                    cliente, spinnerAnio.getValue(), mesInicio, mesFin, serie,
                    diaMode, diaFijo, iva, retencion, plantillas, generarDuplicados, usarHuecos);
            StringBuilder msg = new StringBuilder();
            msg.append("Se han generado ").append(r.getGeneradas()).append(" facturas.");
            if (!r.getMesesOmitidos().isEmpty()) {
                msg.append("\n\nMeses ya existentes omitidos:\n");
                msg.append(String.join(", ", r.getMesesOmitidos()));
            }
            Dialogos.info("Generar facturas mensuales", msg.toString());
            if (stage != null) {
                stage.close();
            }
        } catch (ValidationException e) {
            Dialogos.error("Generar", e.getMessage());
        } catch (Exception e) {
            Dialogos.error("Generar", "Error al generar las facturas: " + e.getMessage());
        }
    }

    @FXML
    private void cancelar() {
        if (stage != null) {
            stage.close();
        }
    }

    private void actualizarInfoBoton() {
        int mesInicio = comboMesInicio.getValue() == null ? 1 : comboMesInicio.getValue();
        int mesFin = comboMesFin.getValue() == null ? 12 : comboMesFin.getValue();
        int meses = Math.max(0, mesFin - mesInicio + 1);
        btnGenerar.setText("Generar " + meses + " factura" + (meses == 1 ? "" : "s"));
    }

    public static class LineaDialogo {
        private final IntegerProperty cantidad = new SimpleIntegerProperty(1);
        private final StringProperty descripcion = new SimpleStringProperty("");
        private final ObjectProperty<BigDecimal> precioUnitario = new SimpleObjectProperty<>(BigDecimal.ZERO);
        private final BooleanProperty anadirMes = new SimpleBooleanProperty(false);

        public LineaDialogo() {
        }

        public LineaDialogo(int cantidad, String descripcion, BigDecimal precioUnitario, boolean anadirMes) {
            setCantidad(cantidad);
            setDescripcion(descripcion);
            setPrecioUnitario(precioUnitario);
            setAnadirMes(anadirMes);
        }

        public int getCantidad() {
            return cantidad.get();
        }

        public void setCantidad(int cantidad) {
            this.cantidad.set(Math.max(1, cantidad));
        }

        public IntegerProperty cantidadProperty() {
            return cantidad;
        }

        public String getDescripcion() {
            return descripcion.get();
        }

        public void setDescripcion(String descripcion) {
            this.descripcion.set(descripcion == null ? "" : descripcion);
        }

        public StringProperty descripcionProperty() {
            return descripcion;
        }

        public BigDecimal getPrecioUnitario() {
            return precioUnitario.get();
        }

        public void setPrecioUnitario(BigDecimal precioUnitario) {
            this.precioUnitario.set(precioUnitario == null ? BigDecimal.ZERO : precioUnitario);
        }

        public ObjectProperty<BigDecimal> precioProperty() {
            return precioUnitario;
        }

        public boolean isAnadirMes() {
            return anadirMes.get();
        }

        public void setAnadirMes(boolean anadirMes) {
            this.anadirMes.set(anadirMes);
        }

        public BooleanProperty anadirMesProperty() {
            return anadirMes;
        }
    }
}
