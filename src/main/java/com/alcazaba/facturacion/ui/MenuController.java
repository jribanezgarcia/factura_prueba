package com.alcazaba.facturacion.ui;

import com.alcazaba.facturacion.service.Servicios;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

import java.time.LocalDate;

/**
 * Menu principal: Nueva factura, Historico, Clientes, Configuracion, Copia de
 * seguridad y Salir. Muestra la fecha de trabajo, que se usa al crear facturas
 * y se recuerda entre sesiones.
 */
public class MenuController implements Vista {

    private static final String PREV_FECHA = "fecha_trabajo";

    private Servicios servicios;
    private Navegador nav;

    @FXML
    private DatePicker fechaTrabajo;

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
        cargarFechaTrabajo();
        atajos();
    }

    private void cargarFechaTrabajo() {
        try {
            String guardada = servicios.config.getPreferencia(PREV_FECHA);
            fechaTrabajo.setValue(guardada != null && !guardada.isBlank()
                    ? LocalDate.parse(guardada)
                    : LocalDate.now());
        } catch (Exception e) {
            fechaTrabajo.setValue(LocalDate.now());
        }
        fechaTrabajo.valueProperty().addListener((o, a, b) -> guardarFechaTrabajo());
    }

    private void guardarFechaTrabajo() {
        try {
            if (fechaTrabajo.getValue() != null) {
                servicios.config.setPreferencia(PREV_FECHA, fechaTrabajo.getValue().toString());
            }
        } catch (Exception ignored) {
        }
    }

    public LocalDate fechaTrabajo() {
        return fechaTrabajo.getValue();
    }

    private void atajos() {
        nav.stage().getScene().getAccelerators().put(
                new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN), this::nuevaFactura);
        nav.stage().getScene().getAccelerators().put(
                new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN), this::historico);
    }

    @FXML
    private void nuevaFactura() {
        nav.mostrar("/com/alcazaba/facturacion/ui/Editor.fxml");
    }

    @FXML
    private void historico() {
        nav.mostrar("/com/alcazaba/facturacion/ui/Historico.fxml");
    }

    @FXML
    private void clientes() {
        nav.mostrar("/com/alcazaba/facturacion/ui/Clientes.fxml");
    }

    @FXML
    private void configuracion() {
        nav.mostrar("/com/alcazaba/facturacion/ui/Configuracion.fxml");
    }

    @FXML
    private void backup() {
        nav.mostrar("/com/alcazaba/facturacion/ui/Backup.fxml");
    }

    @FXML
    private void salir() {
        Platform.exit();
    }
}
