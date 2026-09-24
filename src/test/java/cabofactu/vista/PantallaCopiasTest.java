package cabofactu.vista;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * La pantalla de copias como la ve el usuario: sin pasar por el diálogo de
 * archivos del sistema, solo sus estados iniciales y la vuelta al menú.
 */
class PantallaCopiasTest extends PruebaDePantalla {

    private void abrirCopias() throws Exception {
        mostrarPantalla("MenuPrincipal.fxml");
        pulsar("Copia de seguridad");
        esperarNodo("#lblDestino", Label.class);
    }

    @Test
    void bloqueCrearInicial() throws Exception {
        abrirCopias();
        assertEquals("Copia de seguridad", buscar("Copia de seguridad", Label.class).getText());
        assertEquals("(ninguna carpeta seleccionada)", buscar("#lblDestino", Label.class).getText());
        assertTrue(buscar("#btnCrear", Button.class).isDisabled());
    }

    @Test
    void bloqueRestaurarInicialYVolver() throws Exception {
        abrirCopias();
        assertEquals("Restaurar una copia", buscar("Restaurar una copia", Label.class).getText());
        assertEquals("(ninguna copia seleccionada)", buscar("#lblOrigen", Label.class).getText());
        assertTrue(buscar("#btnRestaurar", Button.class).isDisabled());
        pulsar("Volver");
        assertEquals("Empresa Demo S.L.", buscar("#lblEmpresa", Label.class).getText());
    }
}
