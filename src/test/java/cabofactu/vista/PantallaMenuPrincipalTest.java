package cabofactu.vista;

import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * La entrada al programa según esté la empresa: completa va al menú, nueva
 * cae en Configuración bloqueada hasta completar sus datos.
 */
class PantallaMenuPrincipalTest extends PruebaDePantalla {

    private void abrirArranque() throws Exception {
        mostrarPantalla("Arranque.fxml");
        esperarNodo("#cmbEmpresa", ComboBox.class);
    }

    private void crearEmpresa(String nombre) throws Exception {
        pulsar("Nueva...");
        escribirEnAviso(nombre);
        aceptarAviso();
    }

    private void rellenarEmpresa() throws Exception {
        escribirEn("#txtNif", "12345678Z");
        escribirEn("#txtDireccion", "Calle Uno 1");
        escribirEn("#txtCp", "28001");
        escribirEn("#txtLocalidad", "Madrid");
        escribirEn("#txtProvincia", "Madrid");
        escribirEn("#txtEmail", "prueba@uno.es");
        escribirEn("#txtTelefono", "900000002");
    }

    @Test
    void empresaCompletaEntraAlMenu() throws Exception {
        abrirArranque();
        pulsar("Entrar");
        assertEquals("Empresa Demo S.L.", buscar("#lblEmpresa", Label.class).getText());
        assertEquals("NIF B99999997", buscar("#lblEmpresaInfo", Label.class).getText());
    }

    @Test
    void empresaNuevaBloqueaEnConfiguracion() throws Exception {
        abrirArranque();
        crearEmpresa("Prueba Uno");
        pulsar("Entrar");
        esperarNodo("#txtNombre", TextField.class);
        assertEquals("Prueba Uno", buscar("#txtNombre", TextField.class).getText());
        assertTrue(buscar("#lblDatosPendientes", Label.class).getText().contains("completa los datos"));
        assertTrue(buscar("#btnVolver", Button.class).isDisabled());
    }

    @Test
    void completarDatosDesbloquea() throws Exception {
        abrirArranque();
        crearEmpresa("Prueba Uno");
        pulsar("Entrar");
        esperarNodo("#txtNombre", TextField.class);
        rellenarEmpresa();
        pulsar("Guardar configuración");
        assertTrue(textoAviso().contains("Datos de la empresa completados."));
        cerrarAviso();
        assertEquals("Prueba Uno", buscar("#lblEmpresa", Label.class).getText());
    }
}
