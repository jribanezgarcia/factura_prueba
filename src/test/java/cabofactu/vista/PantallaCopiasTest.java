package cabofactu.vista;

import cabofactu.modelo.negocio.PreferenciasGlobales;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * La pantalla de copias como la ve el usuario: sin pasar por el diálogo de
 * archivos del sistema, solo sus estados iniciales y la vuelta al menú.
 */
class PantallaCopiasTest extends PruebaDePantalla {

    @BeforeEach
    void limpiarCarpetaRecordada() {
        PreferenciasGlobales.set(PreferenciasGlobales.CARPETA_COPIAS, "");
    }

    private void abrirCopias() throws Exception {
        mostrarPantalla("MenuPrincipal.fxml");
        pulsar("Copia de seguridad");
        esperarNodo("#lblCarpeta", Label.class);
    }

    @Test
    void bloqueCrearInicial() throws Exception {
        abrirCopias();
        assertEquals("Copia de seguridad", buscar("Copia de seguridad", Label.class).getText());
        assertTrue(buscar("#lblCarpeta", Label.class).getText()
                .contains("Todavía no se ha elegido la carpeta de las copias."));
        assertFalse(buscar("#btnCrear", Button.class).isDisabled());
    }

    @Test
    void bloqueRestaurarInicialYVolver() throws Exception {
        abrirCopias();
        assertEquals("Restaurar una copia", buscar("Restaurar una copia", Label.class).getText());
        assertEquals("(ninguna copia elegida)", buscar("#lblOrigen", Label.class).getText());
        assertTrue(buscar("#btnRestaurar", Button.class).isDisabled());
        pulsar("Volver");
        assertEquals("Empresa Demo S.L.", buscar("#lblEmpresa", Label.class).getText());
    }

    @Test
    void muestraLaCarpetaDeUnaCopiaHechaAntes() throws Exception {
        Path carpetaCopias = carpeta.resolve("copias_para_pantalla");
        Vista.getInstancia().getControlador().crearCopia(carpetaCopias);

        abrirCopias();

        assertTrue(buscar("#lblCarpeta", Label.class).getText().contains(carpetaCopias.toString()));
    }
}
