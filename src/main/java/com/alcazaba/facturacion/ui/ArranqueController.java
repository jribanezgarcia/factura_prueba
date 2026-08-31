package com.alcazaba.facturacion.ui;

import com.alcazaba.facturacion.service.EmpresaManager;
import com.alcazaba.facturacion.service.PreferenciasGlobales;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextInputDialog;
import javafx.util.Callback;

import java.time.LocalDate;
import java.util.function.Consumer;

/**
 * Pantalla de arranque: elige la empresa, el anio del ejercicio fiscal y la
 * fecha de trabajo. Si el ejercicio es el anio en curso, la fecha de trabajo se
 * fija a hoy automaticamente; en otro caso se pide a mano dentro del ejercicio.
 * Tambien permite crear una empresa nueva desde aqui.
 */
public class ArranqueController implements Vista {

    private Consumer<EmpresaManager.EmpresaInfo> onEntrar;

    @FXML
    private ComboBox<EmpresaManager.EmpresaInfo> cmbEmpresa;
    @FXML
    private Button btnNuevaEmpresa;
    @FXML
    private ComboBox<Integer> cmbEjercicio;
    @FXML
    private DatePicker fechaTrabajo;
    @FXML
    private Label lblFechaAuto;
    @FXML
    private Button btnEntrar;
    @FXML
    private Label lblError;

    @Override
    public void alIniciar() {
        configurarListaEmpresas();
        configurarEjercicio();
        cargarEmpresas();
    }

    public void setOnEntrar(Consumer<EmpresaManager.EmpresaInfo> c) {
        this.onEntrar = c;
    }

    public LocalDate fechaTrabajo() {
        return fechaTrabajo.getValue();
    }

    private void configurarListaEmpresas() {
        cmbEmpresa.setCellFactory(v -> new ListCell<>() {
            @Override
            protected void updateItem(EmpresaManager.EmpresaInfo e, boolean vacio) {
                super.updateItem(e, vacio);
                setText(e == null ? null : e.nombre());
            }
        });
        cmbEmpresa.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(EmpresaManager.EmpresaInfo e, boolean vacio) {
                super.updateItem(e, vacio);
                setText(e == null ? null : e.nombre());
            }
        });
        cmbEmpresa.valueProperty().addListener((o, a, b) -> actualizarBoton());
    }

    private void configurarEjercicio() {
        int actual = LocalDate.now().getYear();
        for (int anio = actual - 2; anio <= actual + 2; anio++) {
            cmbEjercicio.getItems().add(anio);
        }
        cmbEjercicio.setValue(actual);
        cmbEjercicio.valueProperty().addListener((o, a, b) -> aplicarEjercicio());
    }

    /**
     * Ajusta la fecha de trabajo segun el anio del ejercicio: si es el anio en
     * curso, queda fijada a hoy (no editable); si es otro, se pide a mano y solo
     * se permiten fechas dentro de ese ejercicio.
     */
    private void aplicarEjercicio() {
        Integer ejercicio = cmbEjercicio.getValue();
        if (ejercicio == null) {
            return;
        }
        if (ejercicio == LocalDate.now().getYear()) {
            fechaTrabajo.setValue(LocalDate.now());
            fechaTrabajo.setDisable(true);
            lblFechaAuto.setText("La fecha de trabajo se fija a hoy de forma automática para el ejercicio actual.");
        } else {
            fechaTrabajo.setValue(null);
            fechaTrabajo.setDisable(false);
            lblFechaAuto.setText("Indica manualmente una fecha dentro del ejercicio " + ejercicio + ".");
            restringirAlEjercicio(ejercicio);
        }
    }

    private void restringirAlEjercicio(int ejercicio) {
        fechaTrabajo.setDayCellFactory(new Callback<>() {
            @Override
            public javafx.scene.control.DateCell call(javafx.scene.control.DatePicker param) {
                return new javafx.scene.control.DateCell() {
                    @Override
                    public void updateItem(LocalDate f, boolean vacio) {
                        super.updateItem(f, vacio);
                        setDisable(vacio || f.getYear() != ejercicio);
                    }
                };
            }
        });
    }

    private void cargarEmpresas() {
        try {
            cmbEmpresa.getItems().setAll(EmpresaManager.listarEmpresas());
            String ultima = PreferenciasGlobales.get(PreferenciasGlobales.ULTIMA_EMPRESA);
            if (ultima != null) {
                cmbEmpresa.getItems().stream()
                        .filter(e -> e.slug().equals(ultima))
                        .findFirst()
                        .ifPresent(cmbEmpresa::setValue);
            }
            if (cmbEmpresa.getValue() == null && !cmbEmpresa.getItems().isEmpty()) {
                cmbEmpresa.setValue(cmbEmpresa.getItems().get(0));
            }
            actualizarBoton();
        } catch (Exception e) {
            lblError.setText("No se pudieron cargar las empresas: " + e.getMessage());
        }
    }

    private void actualizarBoton() {
        btnEntrar.setDisable(cmbEmpresa.getItems().isEmpty() || cmbEmpresa.getValue() == null);
    }

    @FXML
    private void nuevaEmpresa() {
        TextInputDialog dialogo = new TextInputDialog();
        dialogo.setTitle("Nueva empresa");
        dialogo.setHeaderText("Crea una nueva empresa");
        dialogo.setContentText("Nombre de la empresa:");
        String nombre = dialogo.showAndWait().orElse(null);
        if (nombre == null || nombre.isBlank()) {
            return;
        }
        try {
            EmpresaManager.EmpresaInfo nueva = EmpresaManager.crearEmpresa(nombre);
            cargarEmpresas();
            cmbEmpresa.getItems().stream()
                    .filter(e -> e.slug().equals(nueva.slug()))
                    .findFirst()
                    .ifPresent(cmbEmpresa::setValue);
        } catch (Exception e) {
            lblError.setText("No se pudo crear la empresa: " + e.getMessage());
        }
    }

    @FXML
    private void entrar() {
        EmpresaManager.EmpresaInfo elegida = cmbEmpresa.getValue();
        LocalDate fecha = fechaTrabajo.getValue();
        if (fecha == null && cmbEjercicio.getValue() != null
                && cmbEjercicio.getValue().equals(LocalDate.now().getYear())) {
            fecha = LocalDate.now();
        }
        if (elegida == null) {
            lblError.setText("Selecciona una empresa.");
            return;
        }
        if (fecha == null) {
            lblError.setText("Elige una fecha de trabajo dentro del ejercicio " + cmbEjercicio.getValue() + ".");
            return;
        }
        try {
            EmpresaManager.conectar(elegida.slug(), fecha);
            if (onEntrar != null) {
                onEntrar.accept(elegida);
            }
        } catch (Exception e) {
            lblError.setText("No se pudo entrar en la empresa: " + e.getMessage());
        }
    }
}
