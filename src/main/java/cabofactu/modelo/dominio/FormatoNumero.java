package cabofactu.modelo.dominio;

/** Cómo se forma el número de una serie. El texto es el que se ve en el desplegable. */
public enum FormatoNumero {

    MES("Código-Número/Mes (ej: C-56/7)"),
    ANIO("Código-Número-Año (ej: C-56-2026)"),
    NINGUNO("Código-Número (ej: C-56)");

    private final String texto;

    FormatoNumero(String texto) {
        this.texto = texto;
    }

    @Override
    public String toString() {
        return texto;
    }
}
