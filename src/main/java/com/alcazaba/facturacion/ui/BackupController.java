package com.alcazaba.facturacion.ui;

import com.alcazaba.facturacion.db.Database;
import com.alcazaba.facturacion.model.Empresa;
import com.alcazaba.facturacion.service.BackupService;
import com.alcazaba.facturacion.service.EmpresaManager;
import com.alcazaba.facturacion.service.Sesion;
import com.alcazaba.facturacion.service.Servicios;
import com.alcazaba.facturacion.service.ValidationException;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import java.io.File;
import java.nio.file.Path;
import java.sql.ResultSet;
import java.sql.Statement;

public class BackupController implements Vista {

    private Servicios servicios;
    private Navegador nav;

    @FXML
    private Label lblDestino;
    @FXML
    private Label lblResultado;
    @FXML
    private Button btnCrear;
    @FXML
    private HBox barraNavegacion;

    @FXML
    private Label lblOrigen;
    @FXML
    private VBox cajaResumen;
    @FXML
    private Label lblResumen;
    @FXML
    private RadioButton rbReemplazar;
    @FXML
    private RadioButton rbCrearNueva;
    @FXML
    private ToggleGroup grupoDestino;
    @FXML
    private HBox filaNombreEmpresa;
    @FXML
    private TextField txtNombreEmpresa;
    @FXML
    private Button btnRestaurar;
    @FXML
    private Label lblResultadoRestauracion;

    private Path origenSeleccionado;
    private BackupService.ResumenBackup resumen;

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
        barraNavegacion.getChildren().add(BarraNavegacion.crear(nav, "backup"));
        grupoDestino.selectedToggleProperty().addListener((obs, old, sel) -> {
            boolean nueva = sel == rbCrearNueva;
            filaNombreEmpresa.setVisible(nueva);
            filaNombreEmpresa.setManaged(nueva);
        });
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
    private void seleccionarOrigen() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Seleccionar copia a restaurar");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Bases de datos SQLite", "*.db"));
        File archivo = chooser.showOpenDialog(nav.stage());
        if (archivo == null) {
            return;
        }
        origenSeleccionado = archivo.toPath();
        lblOrigen.setText(archivo.getName());
        btnRestaurar.setDisable(true);
        lblResultadoRestauracion.setText("");
        cajaResumen.setVisible(false);
        cajaResumen.setManaged(false);

        Task<BackupService.ResumenBackup> t = new Task<>() {
            @Override
            protected BackupService.ResumenBackup call() throws Exception {
                return servicios.backup.leerResumen(origenSeleccionado);
            }
        };
        t.setOnSucceeded(e -> {
            resumen = t.getValue();
            mostrarResumen(resumen);
            aplicarReglaNif(resumen);
        });
        t.setOnFailed(e -> {
            String msg = t.getException() instanceof ValidationException
                    ? t.getException().getMessage()
                    : "No se pudo leer la copia: " + t.getException().getMessage();
            Dialogos.error("Restaurar copia", msg);
            origenSeleccionado = null;
            lblOrigen.setText("(ninguna copia seleccionada)");
        });
        new Thread(t).start();
    }

    private void mostrarResumen(BackupService.ResumenBackup r) {
        StringBuilder sb = new StringBuilder();
        sb.append("Empresa: ").append(r.nombreEmpresa()).append("\n");
        sb.append("NIF: ").append(r.nif().isEmpty() ? "(sin NIF)" : r.nif()).append("\n");
        sb.append("Facturas: ").append(r.numFacturas()).append("\n");
        sb.append("Última fecha: ").append(r.ultimaFecha() == null ? "(ninguna)" : r.ultimaFecha()).append("\n");
        sb.append("Versión de esquema: ").append(r.userVersion());
        if (r.tablasCoinciden() && r.userVersion() > com.alcazaba.facturacion.db.Migrations.ultimaVersion()) {
            sb.append(" (versión más nueva, tablas compatibles)");
        }
        if (!r.logoExiste() && !r.logoPath().isEmpty()) {
            sb.append("\n⚠ El logo del backup no se encontrará en esta máquina.");
        }
        lblResumen.setText(sb.toString());
        cajaResumen.setVisible(true);
        cajaResumen.setManaged(true);
    }

    private void aplicarReglaNif(BackupService.ResumenBackup r) {
        String nifBackup = normalizarNif(r.nif());
        String nifActiva = normalizarNif(obtenerNifActiva());

        if (nifBackup.isEmpty() || nifActiva.isEmpty()) {
            int facturasActivas = contarFacturasActivas();
            if (nifActiva.isEmpty() && facturasActivas == 0) {
                rbReemplazar.setDisable(false);
                rbCrearNueva.setDisable(false);
                grupoDestino.selectToggle(rbReemplazar);
            } else if (nifBackup.equals(nifActiva) || (nifActiva.isEmpty() && facturasActivas == 0)) {
                rbReemplazar.setDisable(false);
                rbCrearNueva.setDisable(false);
                grupoDestino.selectToggle(rbReemplazar);
            } else {
                rbReemplazar.setDisable(true);
                rbCrearNueva.setDisable(false);
                grupoDestino.selectToggle(rbCrearNueva);
            }
        } else if (nifBackup.equals(nifActiva)) {
            rbReemplazar.setDisable(false);
            rbCrearNueva.setDisable(false);
            grupoDestino.selectToggle(rbReemplazar);
        } else {
            rbReemplazar.setDisable(true);
            rbCrearNueva.setDisable(false);
            grupoDestino.selectToggle(rbCrearNueva);
        }
        btnRestaurar.setDisable(false);
    }

    private String normalizarNif(String nif) {
        if (nif == null) {
            return "";
        }
        return nif.replaceAll("[\\s\\-]", "").toUpperCase();
    }

    private String obtenerNifActiva() {
        try {
            Empresa emp = servicios.config.getEmpresa();
            return emp == null ? "" : emp.getNif() == null ? "" : emp.getNif();
        } catch (Exception e) {
            return "";
        }
    }

    private int contarFacturasActivas() {
        try (Statement st = Database.getConnection().createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM factura")) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    @FXML
    private void restaurar() {
        if (origenSeleccionado == null) {
            return;
        }
        boolean reemplazar = grupoDestino.getSelectedToggle() == rbReemplazar;
        String nombre = reemplazar ? null : txtNombreEmpresa.getText();
        if (!reemplazar && (nombre == null || nombre.isBlank())) {
            Dialogos.error("Restaurar copia", "Introduce un nombre para la nueva empresa.");
            return;
        }

        String empresaActiva = Sesion.empresaSlug();
        String msg = reemplazar
                ? "¿Reemplazar los datos de la empresa activa (" + empresaActiva + ") con la copia seleccionada?\n"
                        + "Se guardará una copia de rescate antes de continuar. Esta operación no se puede deshacer desde la aplicación."
                : "¿Crear una nueva empresa \"" + nombre + "\" con los datos de la copia?";
        if (!Dialogos.confirmar("Restaurar copia", msg)) {
            return;
        }

        btnRestaurar.setDisable(true);
        lblResultadoRestauracion.setText("Restaurando...");

        Task<?> t = new Task<>() {
            @Override
            protected Object call() throws Exception {
                if (reemplazar) {
                    Path rescate = servicios.backup.restaurarEnEmpresaActiva(origenSeleccionado);
                    return new Object[]{"reemplazar", rescate};
                } else {
                    EmpresaManager.EmpresaInfo nueva = servicios.backup.restaurarComoEmpresaNueva(origenSeleccionado, nombre);
                    return new Object[]{"nueva", nueva};
                }
            }
        };
        t.setOnSucceeded(e -> {
            btnRestaurar.setDisable(false);
            Object[] resultado = (Object[]) t.getValue();
            String tipo = (String) resultado[0];
            if ("reemplazar".equals(tipo)) {
                Path rescate = (Path) resultado[1];
                lblResultadoRestauracion.setText("");
                Dialogos.info("Restaurar copia",
                        "Copia restaurada. Copia de rescate guardada en:\n" + rescate);
                nav.mostrar("/com/alcazaba/facturacion/ui/MenuPrincipal.fxml");
            } else {
                EmpresaManager.EmpresaInfo nueva = (EmpresaManager.EmpresaInfo) resultado[1];
                lblResultadoRestauracion.setText("");
                boolean cambiar = Dialogos.confirmar("Empresa creada",
                        "Empresa \"" + nueva.nombre() + "\" creada correctamente.\n¿Quieres cambiar a ella ahora?");
                if (cambiar) {
                    try {
                        EmpresaManager.conectar(nueva.slug(), Sesion.fechaTrabajo());
                    } catch (Exception ex) {
                        Dialogos.error("Restaurar copia", "No se pudo conectar: " + ex.getMessage());
                        return;
                    }
                }
                nav.mostrar("/com/alcazaba/facturacion/ui/MenuPrincipal.fxml");
            }
        });
        t.setOnFailed(e -> {
            btnRestaurar.setDisable(false);
            Dialogos.error("Restaurar copia",
                    "No se pudo restaurar: " + (t.getException() == null ? "error desconocido" : t.getException().getMessage()));
            lblResultadoRestauracion.setText("");
        });
        new Thread(t).start();
    }

    @FXML
    private void volver() {
        nav.mostrar("/com/alcazaba/facturacion/ui/MenuPrincipal.fxml");
    }
}
