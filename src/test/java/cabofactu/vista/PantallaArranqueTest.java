package cabofactu.vista;

import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * El arranque como lo ve el usuario: la empresa demo seleccionada, el alta
 * con su diálogo, y el borrado con su confirmación.
 */
class PantallaArranqueTest extends PruebaDePantalla {

    private void abrirArranque() throws Exception {
        mostrarPantalla("Arranque.fxml");
        esperarNodo("#cmbEmpresa", ComboBox.class);
    }

    private ComboBox<?> empresas() throws Exception {
        return buscar("#cmbEmpresa", ComboBox.class);
    }

    @Test
    void muestraEmpresaDemoYEntraAlMenu() throws Exception {
        abrirArranque();
        assertEquals("Empresa Demo S.L.", empresas().getValue().toString());
        pulsar("Entrar");
        assertEquals("Empresa Demo S.L.", buscar("#lblEmpresa", Label.class).getText());
    }

    @Test
    void nombreVacioNoCreaNada() throws Exception {
        abrirArranque();
        pulsar("Nueva...");
        aceptarAviso();
        assertEquals(1, empresas().getItems().size());
    }

    @Test
    void crearYEliminarEmpresa() throws Exception {
        abrirArranque();
        pulsar("Nueva...");
        escribirEnAviso("Prueba Uno");
        aceptarAviso();
        assertEquals(2, empresas().getItems().size());
        assertEquals("Prueba Uno", empresas().getValue().toString());
        pulsar("Eliminar");
        assertTrue(textoAviso().contains("¿Seguro que quieres eliminar"));
        aceptarAviso();
        assertEquals(1, empresas().getItems().size());
        assertEquals("Empresa Demo S.L.", empresas().getValue().toString());
    }
}
