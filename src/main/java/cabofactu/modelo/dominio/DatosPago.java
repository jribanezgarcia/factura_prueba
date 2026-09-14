package cabofactu.modelo.dominio;

import java.time.LocalDate;

public record DatosPago(String formaPago, LocalDate vencimiento, String realizadaPor) {
}
