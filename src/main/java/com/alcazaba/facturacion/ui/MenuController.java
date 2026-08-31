package com.alcazaba.facturacion.ui;

import com.alcazaba.facturacion.model.Empresa;
import com.alcazaba.facturacion.service.Servicios;
import com.alcazaba.facturacion.service.Sesion;
import com.alcazaba.facturacion.util.Formatos;
import javafx.fxml.FXML;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

import java.io.File;
import java.time.LocalDate;

/**
 * Menu principal: Nueva factura, Historico, Clientes, Configuracion, Copia de
 * seguridad y Salir. Muestra la fecha de trabajo de la sesion (solo lectura).
 * El logo y los datos de empresa salen de la configuracion.
 */
public class MenuController implements Vista {

    private Servicios servicios;
    private Navegador nav;

    @FXML
    private Label fechaTrabajo;
    @FXML
    private ImageView logo;
    @FXML
    private Label lblEmpresa;
    @FXML
    private Label lblEmpresaInfo;

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
        LocalDate f = Sesion.fechaTrabajo() != null ? Sesion.fechaTrabajo() : LocalDate.now();
        fechaTrabajo.setText(Formatos.fecha(f));
        cargarEmpresa();
        atajos();
        quitarFocoInicial();
    }

    /**
     * Evita que el primer boton del menu quede resaltado al abrir la vista:
     * el foco inicial se deja en el fondo de la escena y no en los botones.
     */
    private void quitarFocoInicial() {
        if (nav.stage().getScene() != null) {
            Platform.runLater(() -> nav.stage().getScene().getRoot().requestFocus());
        }
    }

    private void cargarEmpresa() {
        try {
            Empresa e = servicios.config.getEmpresa();
            if (e.getNombre() != null && !e.getNombre().isBlank()) {
                lblEmpresa.setText(e.getNombre());
            }
            if (e.getNif() != null && !e.getNif().isBlank()) {
                lblEmpresaInfo.setText("NIF " + e.getNif());
            }
            cargarLogo(e.getLogoPath());
        } catch (Exception ignored) {
        }
    }

    private void cargarLogo(String ruta) {
        if (ruta == null || ruta.isBlank()) {
            return;
        }
        File f = new File(ruta);
        if (!f.exists()) {
            return;
        }
        Image img = new Image(f.toURI().toString());
        if (img.isError()) {
            return;
        }
        logo.setImage(img);
        logo.setFitWidth(260);
        logo.setPreserveRatio(true);
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
    private void generarMensual() {
        GenerarFacturasMensualesController.abrir(nav);
        quitarFocoInicial();
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
        nav.stage().close();
    }
}