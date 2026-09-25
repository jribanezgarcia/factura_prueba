package cabofactu.vista.utilidades;

import cabofactu.modelo.dominio.Cliente;
import javafx.scene.control.ComboBox;
import javafx.util.StringConverter;

/**
 * Enseñamos el cliente como «Nombre (NIF)» en el buscador y recuperamos el
 * elegido comparando lo escrito con su nombre o su NIF.
 */
public class ConversorCliente extends StringConverter<Cliente> {

    private final ComboBox<Cliente> combo;

    public ConversorCliente(ComboBox<Cliente> combo) {
        this.combo = combo;
    }

    @Override
    public String toString(Cliente cliente) {
        if (cliente == null) {
            return "";
        }
        return cliente.getNombreNif();
    }

    @Override
    public Cliente fromString(String texto) {
        if (texto == null) {
            return null;
        }
        String limpio = texto.trim();
        if (limpio.isEmpty()) {
            return null;
        }
        for (Cliente cliente : combo.getItems()) {
            if (cliente.getNombre() != null && cliente.getNombre().equalsIgnoreCase(limpio)) {
                return cliente;
            }
            if (cliente.getNif() != null && cliente.getNif().equalsIgnoreCase(limpio)) {
                return cliente;
            }
        }
        return null;
    }
}
