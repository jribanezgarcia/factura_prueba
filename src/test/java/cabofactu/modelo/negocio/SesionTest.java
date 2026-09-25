package cabofactu.modelo.negocio;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Comprobamos que sin fecha de trabajo elegida sale la de hoy.
 */
class SesionTest {

    @Test
    void sinFechaDevuelveHoy() {
        Sesion.getSesion().terminar();
        assertEquals(LocalDate.now(), Sesion.getSesion().getFechaTrabajo());
    }
}
