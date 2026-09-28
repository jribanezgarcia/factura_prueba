package cabofactu.vista;

import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * La sección Empresa como la ve el usuario: los datos cargados, sus avisos
 * al guardar, y el cambio de empresa con su confirmación. También la lista
 * lateral con sus grupos y secciones, y las cabeceras de cada una.
 */
class PantallaConfiguracionTest extends PruebaDePantalla {

    private static final String[] IDS_SECCIONES = {
            "btnDatosFiscales", "btnLogotipo", "btnIva", "btnRetenciones", "btnSeries",
            "btnDisenoPdf", "btnApariencia", "btnCarpetas"
    };

    private void abrirEmpresa() throws Exception {
        mostrarPantalla("Configuracion.fxml");
        esperarNodo("#txtNombre", TextField.class);
    }

    private String valor(String campo) throws Exception {
        return buscar(campo, TextField.class).getText();
    }

    private void abrirSeccion(String idBoton) throws Exception {
        clickOn(buscar("#" + idBoton, ToggleButton.class));
        asentarVentanas();
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
        pulsar("Guardar cambios");
        assertTrue(textoAviso().contains("Formato NIF/NIE incorrecto"));
        cerrarAviso();
        TextField nif = buscar("#txtNif", TextField.class);
        assertTrue(nif.getStyleClass().contains("campo-error"));
    }

    @Test
    void guardarValidoPersiste() throws Exception {
        abrirEmpresa();
        escribirEn("#txtTelefono", "900000001");
        pulsar("Guardar cambios");
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

    @Test
    void barraLateralMuestraGruposYSeccionesEnOrdenConIcono() throws Exception {
        abrirEmpresa();
        ToggleButton primero = buscar("#btnDatosFiscales", ToggleButton.class);
        Node contenedor = primero.getParent();
        assertTrue(contenedor instanceof VBox);
        VBox lista = (VBox) contenedor;
        List<String> orden = new ArrayList<>();
        for (Node hijo : lista.getChildren()) {
            if (hijo instanceof Label) {
                orden.add(((Label) hijo).getText());
            }
            if (hijo instanceof ToggleButton) {
                orden.add(((ToggleButton) hijo).getText());
            }
        }
        List<String> esperado = List.of(
                "DATOS DE LA EMPRESA", "Datos fiscales", "Logotipo",
                "FISCALIDAD", "Tipos de IVA", "Retenciones IRPF", "Series de numeración",
                "FACTURA EN PDF", "Diseño del PDF",
                "PREFERENCIAS", "Apariencia", "Carpetas");
        assertEquals(esperado, orden);
        for (String idBoton : IDS_SECCIONES) {
            comprobarIcono(idBoton);
        }
    }

    private void comprobarIcono(String idBoton) throws Exception {
        List<Node> iconos = nodosVisibles("#" + idBoton + " .icono-seccion");
        assertFalse(iconos.isEmpty(), "Sin icono: " + idBoton);
        assertTrue(iconos.get(0) instanceof SVGPath);
    }

    @Test
    void cadaSeccionMuestraSuTituloYSuAyuda() throws Exception {
        abrirEmpresa();
        comprobarCabecera("btnDatosFiscales", "Datos fiscales",
                "Nombre, NIF, domicilio y contacto que figurarán en cada factura emitida.");
        comprobarCabecera("btnLogotipo", "Logotipo",
                "La imagen de tu empresa en el menú principal, el editor y la cabecera del PDF.");
        comprobarCabecera("btnIva", "Tipos de IVA",
                "Tipos impositivos disponibles al facturar, incluidos los exentos y los suplidos.");
        comprobarCabecera("btnRetenciones", "Retenciones IRPF",
                "Porcentajes de retención para profesionales y actividades sujetas a IRPF.");
        comprobarCabecera("btnSeries", "Series de numeración",
                "Las series con las que numeras tus facturas: ordinarias, rectificativas y otras.");
        comprobarCabecera("btnDisenoPdf", "Diseño del PDF",
                "Personaliza la cabecera, el pie legal y el color de tus facturas en PDF.");
        comprobarCabecera("btnApariencia", "Apariencia",
                "El tema de colores de la aplicación. Cada empresa guarda el suyo.");
        comprobarCabecera("btnCarpetas", "Carpetas",
                "Ubicación de los documentos que genera la aplicación.");
    }

    private void comprobarCabecera(String idBoton, String titulo, String ayuda) throws Exception {
        abrirSeccion(idBoton);
        assertEquals(titulo, buscar(".titulo-seccion", Label.class).getText());
        assertEquals(ayuda, buscar(".ayuda-seccion", Label.class).getText());
    }

    @Test
    void disenoPdfMuestraVistaPreviaYNoElTema() throws Exception {
        abrirEmpresa();
        abrirSeccion("btnDisenoPdf");
        assertFalse(nodosVisibles("#previaCabecera").isEmpty());
        assertTrue(nodosVisibles("#comboTema").isEmpty());
    }

    @Test
    void quitarLogotipoVaciaRutaYPrevia() throws Exception {
        abrirEmpresa();
        abrirSeccion("btnLogotipo");
        assertFalse(buscar("#txtLogoPath", TextField.class).getText().isBlank());
        pulsar("Quitar logotipo");
        assertTrue(buscar("#txtLogoPath", TextField.class).getText().isBlank());
        assertNull(buscar("#imgLogoPrevia", ImageView.class).getImage());
    }
}
