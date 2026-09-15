package cabofactu.utilidades;

/** Valida un email con un patrón razonable. Vacío es opcional y válido. */
public final class ValidadorEmail {

    private static final String PATRON = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

    private ValidadorEmail() {
    }

    public static boolean esValido(String valor) {
        if (valor == null || valor.isBlank()) {
            return true;
        }
        return valor.trim().matches(PATRON);
    }
}