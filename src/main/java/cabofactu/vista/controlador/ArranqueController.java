package cabofactu.vista.controlador;

import cabofactu.modelo.dominio.EmpresaDisponible;
import cabofactu.modelo.negocio.sqlite.CargarDemo;
import cabofactu.modelo.negocio.PreferenciasGlobales;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import cabofactu.vista.Pantalla;
import cabofactu.vista.Vista;
import cabofactu.vista.utilidades.Dialogos;

/**
 * Pantalla de arranque: elige la empresa, el anio del ejercicio fiscal y la
 * fecha de trabajo. Si el ejercicio es el anio en curso, la fecha de trabajo se
 * fija a hoy automaticamente; en otro caso se pide a mano dentro del ejercicio.
 * Tambien permite crear una empresa nueva y eliminar cualquiera de la lista.
 */
public class ArranqueController implements Pantalla, Initializable {

    @FXML
    private ComboBox<EmpresaDisponible> cmbEmpresa;
    @FXML
    private Button btnNuevaEmpresa;
    @FXML
    private Button btnEliminarEmpresa;
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
    @FXML
    private Label lblAyudaEmpresa;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurarListaEmpresas();
        configurarEjercicio();
        cargarEmpresas();
    }

    public void mostrarAvisoInicial(boolean demoRecienCargada) {
        if (demoRecienCargada) {
            Dialogos.mostrarDialogoInformacion("Bienvenido", "Se ha cargado una empresa de demostración con datos ficticios "
                    + "para que puedas probar el programa.\n\nCuando quieras trabajar con tu empresa, "
                    + "créala con «Nueva…». El menú te avisará de los datos fiscales y de contacto que le falten. "
                    + "La empresa de demostración se puede eliminar desde esta misma pantalla con «Eliminar».");
        } else if (cmbEmpresa.getItems().isEmpty()) {
            Dialogos.mostrarDialogoInformacion("Bienvenido", "Para iniciar el programa crea tu empresa con «Nueva…». "
                    + "El menú te avisará de los datos que le falten.");
        }
    }

    public LocalDate fechaTrabajo() {
        return fechaTrabajo.getValue();
    }

    private void configurarListaEmpresas() {
        cmbEmpresa.valueProperty().addListener((propiedad, anterior, nuevo) -> actualizarBoton());
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
            List<EmpresaDisponible> empresas = Vista.getInstancia().getControlador().listadoEmpresas();
            cmbEmpresa.getItems().setAll(empresas);
            String ultima = PreferenciasGlobales.get(PreferenciasGlobales.ULTIMA_EMPRESA);
            if (ultima != null) {
                for (EmpresaDisponible empresa : empresas) {
                    if (empresa.getCarpeta().equals(ultima)) {
                        cmbEmpresa.setValue(empresa);
                    }
                }
            }
            if (cmbEmpresa.getValue() == null && !cmbEmpresa.getItems().isEmpty()) {
                cmbEmpresa.setValue(cmbEmpresa.getItems().get(0));
            }
            if (cmbEmpresa.getItems().isEmpty()) {
                lblAyudaEmpresa.setText("Crea tu empresa con «Nueva…» para empezar.");
                lblAyudaEmpresa.setVisible(true);
                lblAyudaEmpresa.setManaged(true);
            } else if (cmbEmpresa.getItems().size() == 1
                    && cmbEmpresa.getItems().get(0).getCarpeta().equals(CargarDemo.CARPETA)) {
                lblAyudaEmpresa.setText("Empresa de demostración con datos ficticios. Crea la tuya con «Nueva…».");
                lblAyudaEmpresa.setVisible(true);
                lblAyudaEmpresa.setManaged(true);
            } else {
                lblAyudaEmpresa.setVisible(false);
                lblAyudaEmpresa.setManaged(false);
            }
            actualizarBoton();
        } catch (Exception e) {
            lblError.setText("No se pudieron cargar las empresas: " + e.getMessage());
        }
    }

    /** Activamos Entrar y Eliminar solo si hay una empresa elegida. */
    private void actualizarBoton() {
        boolean hayElegida = cmbEmpresa.getValue() != null;
        btnEntrar.setDisable(!hayElegida);
        btnEliminarEmpresa.setDisable(!hayElegida);
    }

    @FXML
    private void nuevaEmpresa() {
        TextInputDialog dialogo = new TextInputDialog();
        dialogo.setTitle("Nueva empresa");
        dialogo.setHeaderText("Crea una nueva empresa");
        dialogo.setContentText("Nombre de la empresa:");
        Optional<String> respuesta = dialogo.showAndWait();
        if (!respuesta.isPresent()) {
            return;
        }
        String nombre = respuesta.get();
        if (nombre.isBlank()) {
            return;
        }
        try {
            EmpresaDisponible nueva = Vista.getInstancia().getControlador().altaEmpresa(nombre);
            cargarEmpresas();
            cmbEmpresa.setValue(nueva);
        } catch (Exception e) {
            lblError.setText("No se pudo crear la empresa: " + e.getMessage());
        }
    }

    @FXML
    private void eliminarEmpresa() {
        EmpresaDisponible elegida = cmbEmpresa.getValue();
        if (elegida == null) {
            lblError.setText("Selecciona la empresa que quieres eliminar.");
            return;
        }
        if (!Dialogos.mostrarDialogoConfirmacion("Eliminar empresa",
                "¿Seguro que quieres eliminar «" + elegida.getNombre() + "»?\n\n"
                        + "Se borrará su carpeta de datos con sus facturas, clientes y configuración. "
                        + "No se puede deshacer: si la necesitas, haz antes una copia de seguridad.")) {
            return;
        }
        try {
            Vista.getInstancia().getControlador().bajaEmpresa(elegida.getCarpeta());
            cargarEmpresas();
        } catch (Exception e) {
            lblError.setText("No se pudo eliminar la empresa: " + e.getMessage());
        }
    }

    @FXML
    private void entrar() {
        EmpresaDisponible elegida = cmbEmpresa.getValue();
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
            Vista.getInstancia().getControlador().abrirEmpresa(elegida.getCarpeta(), fecha);
            Stage ventanaArranque = (Stage) btnEntrar.getScene().getWindow();
            Vista.getInstancia().abrirVentanaPrincipal();
            ventanaArranque.close();
        } catch (Exception e) {
            lblError.setText("No se pudo entrar en la empresa: " + e.getMessage());
        }
    }
}
