package cabofactu.vista;

import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * La sección Empresa como la ve el usuario: los datos cargados, sus avisos
 * al guardar, y el cambio de empresa con su confirmación.
 */
class PantallaConfiguracionTest extends PruebaDePantalla {

    private void abrirEmpresa() throws Exception {
        mostrarPantalla("Configuracion.fxml");
        esperarNodo("#txtNombre", TextField.class);
    }

    private String valor(String campo) throws Exception {
        return buscar(campo, TextField.class).getText();
    }

    @Test
    void muestraLosDatosDemo() throws Exception {
        abrirEmpresa();
        assertEquals("Empresa Demo S.L.", valor("#txtNombre"));
        assertEquals("B99999997", valor("#txtNif"));
        assertEquals("demo@irreal.es", valor("#txtEmail"));
        assertEquals("900000000", valor("#txtTelefono"));
    }

    @Test
    void nifMaloAvisaYSeMarcaEnRojo() throws Exception {
        abrirEmpresa();
        escribirEn("#txtNif", "123");
        pulsar("Guardar configuración");
        assertTrue(textoAviso().contains("Formato NIF/NIE incorrecto"));
        cerrarAviso();
        TextField nif = buscar("#txtNif", TextField.class);
        assertTrue(nif.getStyleClass().contains("campo-error"));
    }

    @Test
    void guardarValidoPersiste() throws Exception {
        abrirEmpresa();
        escribirEn("#txtTelefono", "900000001");
        pulsar("Guardar configuración");
        assertTrue(textoAviso().contains("Configuración guardada."));
        cerrarAviso();
        mostrarPantalla("Configuracion.fxml");
        assertEquals("900000001", valor("#txtTelefono"));
    }

    @Test
    void cambiarDeEmpresaPideConfirmacion() throws Exception {
        abrirEmpresa();
        pulsar("Cambiar de empresa");
        assertTrue(textoAviso().contains("pantalla de arranque"));
        aceptarAviso();
        esperarNodo("#cmbEmpresa", ComboBox.class);
    }
}
