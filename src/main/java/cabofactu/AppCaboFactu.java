package cabofactu;

import cabofactu.controlador.Controlador;
import cabofactu.modelo.Modelo;
import cabofactu.vista.Vista;
import cabofactu.vista.utilidades.ErroresInesperados;

import java.util.Locale;

/**
 * Punto de entrada de CaboFactu. Creamos el modelo, la vista y el controlador,
 * y arrancamos la aplicación.
 *
 * @author jribanezgarcia
 */
public class AppCaboFactu {

    public static void main(String[] args) {
        Locale.setDefault(new Locale("es", "ES"));
        try {
            Modelo modelo = new Modelo();
            Vista vista = Vista.getInstancia();
            Controlador controlador = new Controlador(modelo, vista);
            controlador.comenzar();
        } catch (Exception e) {
            ErroresInesperados.guardar(e);
        }
    }
}
