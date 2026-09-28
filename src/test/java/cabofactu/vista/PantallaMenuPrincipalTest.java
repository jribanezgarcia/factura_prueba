package cabofactu.vista;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
        pulsar("Nueva…");
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
        assertTrue(buscar("#lblDatosPendientes", Label.class).getText().contains("Completa los datos fiscales"));
        assertTrue(buscar("#btnVolver", Button.class).isDisabled());
    }

    @Test
    void completarDatosDesbloquea() throws Exception {
        abrirArranque();
        crearEmpresa("Prueba Uno");
        pulsar("Entrar");
        esperarNodo("#txtNombre", TextField.class);
        rellenarEmpresa();
        pulsar("Guardar cambios");
        assertTrue(textoAviso().contains("Datos de la empresa completados."));
        cerrarAviso();
        assertEquals("Prueba Uno", buscar("#lblEmpresa", Label.class).getText());
    }

    @Test
    void opcionesDelMenuSinFondoNiBorde() throws Exception {
        mostrarPantalla("MenuPrincipal.fxml");
        asentarVentanas();
        List<Node> opciones = nodosVisibles(".opcion-menu");
        assertFalse(opciones.isEmpty());
        for (Node nodo : opciones) {
            Button boton = (Button) nodo;
            assertTrue(sinFondo(boton), "Tiene fondo: " + boton);
            assertTrue(sinBorde(boton), "Tiene borde: " + boton);
        }
    }

    private boolean sinFondo(Button boton) {
        Background fondo = boton.getBackground();
        if (fondo == null) {
            return true;
        }
        for (BackgroundFill relleno : fondo.getFills()) {
            if (!esTransparente(relleno.getFill())) {
                return false;
            }
        }
        return true;
    }

    private boolean sinBorde(Button boton) {
        Border borde = boton.getBorder();
        if (borde == null) {
            return true;
        }
        for (BorderStroke trazo : borde.getStrokes()) {
            if (!esTransparente(trazo.getTopStroke()) || !esTransparente(trazo.getBottomStroke())
                    || !esTransparente(trazo.getLeftStroke()) || !esTransparente(trazo.getRightStroke())) {
                return false;
            }
        }
        return true;
    }

    private boolean esTransparente(Paint relleno) {
        if (relleno instanceof Color) {
            Color color = (Color) relleno;
            return color.getOpacity() == 0;
        }
        return false;
    }
}
