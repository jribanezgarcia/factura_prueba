package cabofactu.modelo.dominio;

/**
 * Una serie de numeración: su código, su descripción, el formato del número y
 * si es de rectificativas. Los setters comprueban lo que reciben, así que una
 * Serie que existe es siempre una Serie válida.
 */
public class Serie {

    private Long id;
    private String codigo;
    private String descripcion;
    private FormatoNumero formato;
    private boolean esRectificativa;

    public Serie(String codigo, String descripcion, FormatoNumero formato, boolean esRectificativa)
            throws Exception {
        setCodigo(codigo);
        setDescripcion(descripcion);
        setFormato(formato);
        setEsRectificativa(esRectificativa);
    }

    /** Copiamos una serie entera, para poder editarla sin tocar la original. */
    public Serie(Serie otra) throws Exception {
        this(otra.getCodigo(), otra.getDescripcion(), otra.getFormato(), otra.isEsRectificativa());
        setId(otra.getId());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) throws Exception {
        String error = errorCodigo(codigo);
        if (error != null) {
            throw new Exception(error);
        }
        if (codigo == null || codigo.isBlank()) {
            this.codigo = "";
        } else {
            this.codigo = codigo.trim().toUpperCase();
        }
    }

    public String getDescripcion() {
        return descripcion;
    }

    /** La descripción es opcional: si no se escribe, la guardamos vacía. */
    public void setDescripcion(String descripcion) {
        if (descripcion == null || descripcion.isBlank()) {
            this.descripcion = "";
        } else {
            this.descripcion = descripcion.trim();
        }
    }

    public FormatoNumero getFormato() {
        return formato;
    }

    public void setFormato(FormatoNumero formato) throws Exception {
        String error = errorFormato(formato);
        if (error != null) {
            throw new Exception(error);
        }
        this.formato = formato;
    }

    public boolean isEsRectificativa() {
        return esRectificativa;
    }

    public void setEsRectificativa(boolean esRectificativa) {
        this.esRectificativa = esRectificativa;
    }

    @Override
    public boolean equals(Object otro) {
        if (this == otro) {
            return true;
        }
        if (otro == null || getClass() != otro.getClass()) {
            return false;
        }
        Serie serie = (Serie) otro;
        if (id == null) {
            return serie.id == null;
        }
        return id.equals(serie.id);
    }

    @Override
    public int hashCode() {
        if (id == null) {
            return 0;
        }
        return id.hashCode();
    }

    @Override
    public String toString() {
        if (descripcion != null && !descripcion.isBlank()) {
            return codigo + " (" + descripcion + ")";
        }
        return codigo;
    }

    /** «(sin código)» cuando está vacío, para la columna Código de la tabla. */
    public String getCodigoTexto() {
        if (codigo == null || codigo.isBlank()) {
            return "(sin código)";
        }
        return codigo;
    }

    /** «Sí» o «No», para la columna Rectificativa de la tabla. */
    public String getRectificativaTexto() {
        if (esRectificativa) {
            return "Sí";
        }
        return "No";
    }

    /** El texto del formato, para la columna Formato de la tabla. */
    public String getFormatoTexto() {
        if (formato == null) {
            return "";
        }
        return formato.toString();
    }

    /** Decimos qué le pasa al código, o null si está bien. Puede estar vacío. */
    public static String errorCodigo(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        if (!valor.trim().matches("[A-Za-z0-9]+")) {
            return "El código de la serie solo puede tener letras y números.";
        }
        return null;
    }

    /** Decimos qué le pasa al formato, o null si está bien. No puede faltar. */
    public static String errorFormato(FormatoNumero valor) {
        if (valor == null) {
            return "Indique el formato del número.";
        }
        return null;
    }
}
