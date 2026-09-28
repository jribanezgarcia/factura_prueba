package cabofactu.vista.utilidades;

import javafx.scene.control.DateCell;

import java.time.LocalDate;

/**
 * Celda del calendario de un DatePicker que desactiva los días que no
 * pertenecen al ejercicio fiscal elegido.
 *
 * Cómo funciona: JavaFX pinta el calendario llamando a updateItem con cada
 * fecha visible; aquí miramos su año y desactivamos la celda si no es el
 * del ejercicio, para que no se pueda elegir un día de otro año.
 */
public class CeldaFechaEjercicio extends DateCell {

    private final int ejercicio;

    public CeldaFechaEjercicio(int ejercicio) {
        this.ejercicio = ejercicio;
    }

    @Override
    public void updateItem(LocalDate fecha, boolean vacio) {
        super.updateItem(fecha, vacio);
        setDisable(vacio || fecha.getYear() != ejercicio);
    }
}
