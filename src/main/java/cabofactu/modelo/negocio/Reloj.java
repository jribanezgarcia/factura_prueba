package cabofactu.modelo.negocio;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Reloj {

    private final Clock clock;

    public Reloj(Clock clock) {
        this.clock = clock;
    }

    public LocalDate hoy() {
        return LocalDate.now(clock);
    }

    public LocalDateTime ahora() {
        return LocalDateTime.now(clock);
    }

    public LocalDate fechaTrabajo() {
        LocalDate f = Sesion.fechaTrabajo();
        return f != null ? f : hoy();
    }
}
