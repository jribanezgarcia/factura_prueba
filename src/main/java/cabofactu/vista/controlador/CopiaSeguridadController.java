package cabofactu.vista.controlador;

import cabofactu.modelo.dominio.Empresa;
import cabofactu.modelo.dominio.EmpresaDisponible;
import cabofactu.fichero.CopiaSeguridad;
import cabofactu.modelo.negocio.Sesion;
import cabofactu.modelo.negocio.ValidacionException;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
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
import java.net.URL;
import java.nio.file.Path;
import java.util.ResourceBundle;
import cabofactu.vista.Pantalla;
import cabofactu.vista.Vista;
import cabofactu.vista.utilidades.Dialogos;

public class CopiaSeguridadController implements Pantalla, Initializable {

    @FXML
    private Label lblDestino;
    @FXML
    private Label lblResultado;
    @FXML
    private Button btnCrear;
    // Cómo funciona: JavaFX inyecta aquí el controlador del <fx:include fx:id="barra">;
    // el nombre del campo es el fx:id más "Controller".
    @FXML
    private BarraNavegacionController barraController;

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
    private CopiaSeguridad.ResumenCopia resumen;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        barraController.marcarActivo("copiaSeguridad");
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
        File dir = chooser.showDialog(Vista.getInstancia().getVentana());
        if (dir != null) {
            lblDestino.setText(dir.getAbsolutePath());
            btnCrear.setDisable(false);
        }
    }

    @FXML
    private void crear() {
        String destino = lblDestino.getText();
        if (destino == null || destino.isBlank()) {
            Dialogos.mostrarDialogoError("Copia de seguridad", "Seleccione primero la carpeta de destino.");
            return;
        }
        btnCrear.setDisable(true);
        Task<Path> t = new Task<>() {
            @Override
            protected Path call() throws Exception {
                return Vista.getInstancia().getControlador().getModelo().getCopiaSeguridad().crearCopia(Path.of(destino));
            }
        };
        t.setOnSucceeded(e -> {
            btnCrear.setDisable(false);
            lblResultado.setText("Copia creada:\n" + t.getValue());
            Dialogos.mostrarDialogoInformacion("Copia de seguridad", "Copia de seguridad creada en:\n" + t.getValue());
        });
        t.setOnFailed(e -> {
            btnCrear.setDisable(false);
            Dialogos.mostrarDialogoError("Copia de seguridad", "No se pudo crear la copia: "
                    + (t.getException() == null ? "error desconocido" : t.getException().getMessage()));
        });
        new Thread(t).start();
    }

    @FXML
    private void seleccionarOrigen() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Seleccionar copia a restaurar");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Bases de datos SQLite", "*.db"));
        File archivo = chooser.showOpenDialog(Vista.getInstancia().getVentana());
        if (archivo == null) {
            return;
        }
        origenSeleccionado = archivo.toPath();
        lblOrigen.setText(archivo.getName());
        btnRestaurar.setDisable(true);
        lblResultadoRestauracion.setText("");
        cajaResumen.setVisible(false);
        cajaResumen.setManaged(false);

        Task<CopiaSeguridad.ResumenCopia> t = new Task<>() {
            @Override
            protected CopiaSeguridad.ResumenCopia call() throws Exception {
                return Vista.getInstancia().getControlador().getModelo().getCopiaSeguridad().leerResumen(origenSeleccionado);
            }
        };
        t.setOnSucceeded(e -> {
            resumen = t.getValue();
            mostrarResumen(resumen);
            aplicarReglaNif(resumen);
        });
        t.setOnFailed(e -> {
            String msg = t.getException() instanceof ValidacionException
                    ? t.getException().getMessage()
                    : "No se pudo leer la copia: " + t.getException().getMessage();
            Dialogos.mostrarDialogoError("Restaurar copia", msg);
            origenSeleccionado = null;
            lblOrigen.setText("(ninguna copia seleccionada)");
        });
        new Thread(t).start();
    }

    private void mostrarResumen(CopiaSeguridad.ResumenCopia r) {
        StringBuilder sb = new StringBuilder();
        sb.append("Empresa: ").append(r.nombreEmpresa()).append("\n");
        sb.append("NIF: ").append(r.nif().isEmpty() ? "(sin NIF)" : r.nif()).append("\n");
        sb.append("Facturas: ").append(r.numFacturas()).append("\n");
        sb.append("Última fecha: ");
        if (r.ultimaFecha() == null) {
            sb.append("(ninguna)");
        } else {
            sb.append(r.ultimaFecha());
        }
        if (!r.logoExiste() && !r.logoPath().isEmpty()) {
            sb.append("\n⚠ El logo del backup no se encontrará en esta máquina.");
        }
        lblResumen.setText(sb.toString());
        cajaResumen.setVisible(true);
        cajaResumen.setManaged(true);
    }

    private void aplicarReglaNif(CopiaSeguridad.ResumenCopia r) {
        String nifBackup = normalizarNif(r.nif());
        String nifActiva = normalizarNif(obtenerNifActiva());

        boolean activaVacia = nifActiva.isEmpty() && contarFacturasActivas() == 0;
        if (activaVacia || nifBackup.equals(nifActiva)) {
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
            Empresa emp = Vista.getInstancia().getControlador().getModelo().getConfiguracion().getEmpresa();
            return emp == null ? "" : emp.getNif() == null ? "" : emp.getNif();
        } catch (Exception e) {
            return "";
        }
    }

    private int contarFacturasActivas() {
        try {
            return Vista.getInstancia().getControlador().getModelo().getCopiaSeguridad().facturasEmpresaActiva();
        } catch (Exception e) {
            return 0;
        }
    }

    private String nombreEmpresaActiva() {
        String carpeta = Sesion.getSesion().getCarpetaEmpresa();
        try {
            for (EmpresaDisponible empresa : Vista.getInstancia().getControlador().listadoEmpresas()) {
                if (empresa.getCarpeta().equals(carpeta)) {
                    return empresa.getNombre();
                }
            }
        } catch (Exception ignored) {
        }
        return carpeta;
    }

    @FXML
    private void restaurar() {
        if (origenSeleccionado == null) {
            return;
        }
        boolean reemplazar = grupoDestino.getSelectedToggle() == rbReemplazar;
        String nombre = reemplazar ? null : txtNombreEmpresa.getText();
        if (!reemplazar && (nombre == null || nombre.isBlank())) {
            Dialogos.mostrarDialogoError("Restaurar copia", "Introduce un nombre para la nueva empresa.");
            return;
        }

        String empresaActiva = nombreEmpresaActiva();
        String msg = reemplazar
                ? "¿Reemplazar los datos de la empresa activa (" + empresaActiva + ") con la copia seleccionada?\n"
                        + "Se guardará una copia de rescate antes de continuar. Esta operación no se puede deshacer desde la aplicación."
                : "¿Crear una nueva empresa \"" + nombre + "\" con los datos de la copia?";
        if (!Dialogos.mostrarDialogoConfirmacion("Restaurar copia", msg)) {
            return;
        }

        btnRestaurar.setDisable(true);
        lblResultadoRestauracion.setText("Restaurando...");

        Task<?> t = new Task<>() {
            @Override
            protected Object call() throws Exception {
                if (reemplazar) {
                    Path rescate = Vista.getInstancia().getControlador().getModelo().getCopiaSeguridad().restaurarEnEmpresaActiva(origenSeleccionado);
                    return new Object[]{"reemplazar", rescate};
                } else {
                    EmpresaDisponible nueva = Vista.getInstancia().getControlador().getModelo().getCopiaSeguridad().restaurarComoEmpresaNueva(origenSeleccionado, nombre);
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
                Dialogos.mostrarDialogoInformacion("Restaurar copia",
                        "Copia restaurada. Copia de rescate guardada en:\n" + rescate);
                Vista.getInstancia().mostrarInicio();
            } else {
                EmpresaDisponible nueva = (EmpresaDisponible) resultado[1];
                lblResultadoRestauracion.setText("");
                boolean cambiar = Dialogos.mostrarDialogoConfirmacion("Empresa creada",
                        "Empresa \"" + nueva.getNombre() + "\" creada correctamente.\n¿Quieres cambiar a ella ahora?");
                if (cambiar) {
                    try {
                        Vista.getInstancia().getControlador().abrirEmpresa(nueva.getCarpeta(), Sesion.getSesion().getFechaTrabajo());
                    } catch (Exception ex) {
                        Dialogos.mostrarDialogoError("Restaurar copia", "No se pudo conectar: " + ex.getMessage());
                        return;
                    }
                    Vista.getInstancia().mostrarInicio();
                } else {
                    Vista.getInstancia().mostrar("CopiaSeguridad.fxml");
                }
            }
        });
        t.setOnFailed(e -> {
            btnRestaurar.setDisable(false);
            Dialogos.mostrarDialogoError("Restaurar copia",
                    "No se pudo restaurar: " + (t.getException() == null ? "error desconocido" : t.getException().getMessage()));
            lblResultadoRestauracion.setText("");
        });
        new Thread(t).start();
    }

    @FXML
    private void volver() {
        Vista.getInstancia().mostrar("MenuPrincipal.fxml");
    }
}
