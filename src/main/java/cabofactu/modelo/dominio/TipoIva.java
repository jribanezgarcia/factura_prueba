package cabofactu.modelo.dominio;

/**
 * Un tipo de IVA del catálogo de la empresa: con porcentaje, exento (con su
 * motivo) o suplido. Los setters comprueban lo que reciben, así que un TipoIva
 * que existe es siempre un TipoIva válido.
 */
public class TipoIva {

    private Long id;
    private String nombre;
    private Integer porcentaje;
    private String motivoExencion;
    private boolean activo = true;
    private boolean esSuplido;

    public TipoIva(String nombre, Integer porcentaje, boolean esSuplido) throws Exception {
        setNombre(nombre);
        setPorcentaje(porcentaje);
        setEsSuplido(esSuplido);
        setMotivoExencion("");
    }

    /** Copiamos un tipo entero, para poder editarlo sin tocar el original. */
    public TipoIva(TipoIva otro) throws Exception {
        this(otro.getNombre(), otro.getPorcentaje(), otro.isEsSuplido());
        setId(otro.getId());
        setMotivoExencion(otro.getMotivoExencion());
        setActivo(otro.isActivo());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) throws Exception {
        String error = errorNombre(nombre);
        if (error != null) {
            throw new Exception(error);
        }
        this.nombre = nombre.trim();
    }

    public Integer getPorcentaje() {
        return porcentaje;
    }

    public void setPorcentaje(Integer porcentaje) throws Exception {
        String texto = "";
        if (porcentaje != null) {
            texto = String.valueOf(porcentaje);
        }
        String error = errorPorcentaje(texto);
        if (error != null) {
            throw new Exception(error);
        }
        this.porcentaje = porcentaje;
    }

    public String getMotivoExencion() {
        return motivoExencion;
    }

    /** El motivo de exención es opcional: si no se escribe, lo guardamos vacío. */
    public void setMotivoExencion(String motivoExencion) {
        if (motivoExencion == null || motivoExencion.isBlank()) {
            this.motivoExencion = "";
        } else {
            this.motivoExencion = motivoExencion.trim();
        }
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public boolean isExento() {
        return porcentaje == null;
    }

    public boolean isEsSuplido() {
        return esSuplido;
    }

    /** Un suplido no lleva porcentaje: al marcarlo lo dejamos en null. */
    public void setEsSuplido(boolean esSuplido) {
        this.esSuplido = esSuplido;
        if (esSuplido) {
            this.porcentaje = null;
        }
    }

    /** «Suplido», «Exento» o «21%», para la columna Tipo de la tabla. */
    public String getPorcentajeTexto() {
        if (isEsSuplido()) {
            return "Suplido";
        }
        if (isExento()) {
            return "Exento";
        }
        return porcentaje + "%";
    }

    /** «Sí» o «No», para la columna Suplido de la tabla. */
    public String getSuplidoTexto() {
        if (esSuplido) {
            return "Sí";
        }
        return "No";
    }

    /** «Sí» o «No», para la columna Activo de la tabla. */
    public String getActivoTexto() {
        if (activo) {
            return "Sí";
        }
        return "No";
    }

    @Override
    public String toString() {
        return nombre;
    }

    /** Decimos qué le pasa al nombre, o null si está bien. */
    public static String errorNombre(String valor) {
        if (valor == null || valor.isBlank()) {
            return "Indique el nombre del tipo de IVA.";
        }
        return null;
    }

    /**
     * Decimos qué le pasa al porcentaje escrito en la ficha, o null si está
     * bien. Vacío vale: es un tipo exento.
     */
    public static String errorPorcentaje(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        try {
            int porcentaje = Integer.parseInt(texto.trim());
            if (porcentaje < 0 || porcentaje > 100) {
                return "El porcentaje debe ser un número entero entre 0 y 100, o quedarse vacío si es exento.";
            }
        } catch (NumberFormatException e) {
            return "El porcentaje debe ser un número entero entre 0 y 100, o quedarse vacío si es exento.";
        }
        return null;
    }
}
