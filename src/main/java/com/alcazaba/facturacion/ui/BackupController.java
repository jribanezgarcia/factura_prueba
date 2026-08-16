package com.alcazaba.facturacion.ui;

import com.alcazaba.facturacion.service.Servicios;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.DirectoryChooser;

import java.io.File;
import java.nio.file.Path;

/**
 * Copia de seguridad: elige una carpeta de destino con DirectoryChooser y
 * genera una copia del SQLite (VACUUM INTO) con timestamp en segundo plano.
 */
public class BackupController implements Vista {

    private Servicios servicios;
    private Navegador nav;

    @FXML
    private Label lblDestino;
    @FXML
    private Label lblResultado;
    @FXML
    private Button btnCrear;

    @Override
    public void setServicios(Servicios s) {
        this.servicios = s;
    }

    @Override
    public void setNavegador(Navegador n) {
        this.nav = n;
    }

    @FXML
    private void seleccionarDestino() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Carpeta de destino de la copia de seguridad");
        File dir = chooser.showDialog(nav.stage());
        if (dir != null) {
            lblDestino.setText(dir.getAbsolutePath());
            btnCrear.setDisable(false);
        }
    }

    @FXML
    private void crear() {
        String destino = lblDestino.getText();
        if (destino == null || destino.isBlank()) {
            Dialogos.error("Copia de seguridad", "Seleccione primero la carpeta de destino.");
            return;
        }
        btnCrear.setDisable(true);
        Task<Path> t = new Task<>() {
            @Override
            protected Path call() throws Exception {
                return servicios.backup.crearBackup(Path.of(destino));
            }
        };
        t.setOnSucceeded(e -> {
            btnCrear.setDisable(false);
            lblResultado.setText("Copia creada:\n" + t.getValue());
            Dialogos.info("Copia de seguridad", "Copia de seguridad creada en:\n" + t.getValue());
        });
        t.setOnFailed(e -> {
            btnCrear.setDisable(false);
            Dialogos.error("Copia de seguridad", "No se pudo crear la copia: "
                    + (t.getException() == null ? "error desconocido" : t.getException().getMessage()));
        });
        new Thread(t).start();
    }

    @FXML
    private void volver() {
        nav.mostrar("/com/alcazaba/facturacion/ui/MenuPrincipal.fxml");
    }
}
