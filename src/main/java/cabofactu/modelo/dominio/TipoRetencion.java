package cabofactu.modelo.dominio;

/**
 * Un tipo de retención de IRPF del catálogo de la empresa. Los setters
 * comprueban lo que reciben, así que un TipoRetencion que existe es siempre
 * un TipoRetencion válido.
 */
public class TipoRetencion {

    private Long id;
    private String nombre;
    private Integer porcentaje;
    private boolean activo = true;

    public TipoRetencion(String nombre, int porcentaje) throws Exception {
        setNombre(nombre);
        setPorcentaje(porcentaje);
    }

    /** Copiamos un tipo entero, para poder editarlo sin tocar el original. */
    public TipoRetencion(TipoRetencion otro) throws Exception {
        this(otro.getNombre(), otro.getPorcentaje());
        setId(otro.getId());
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

    public void setPorcentaje(int porcentaje) throws Exception {
        String error = errorPorcentaje(String.valueOf(porcentaje));
        if (error != null) {
            throw new Exception(error);
        }
        this.porcentaje = porcentaje;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    /** «15%», para la columna Porcentaje de la tabla. */
    public String getPorcentajeTexto() {
        return porcentaje + "%";
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
        return nombre + " (" + getPorcentajeTexto() + ")";
    }

    /** Decimos qué le pasa al nombre, o null si está bien. */
    public static String errorNombre(String valor) {
        if (valor == null || valor.isBlank()) {
            return "Indique el nombre del tipo de retención.";
        }
        return null;
    }

    /** Decimos qué le pasa al porcentaje escrito en la ficha, o null si está bien. */
    public static String errorPorcentaje(String texto) {
        if (texto == null || texto.isBlank()) {
            return "Indique el porcentaje de la retención.";
        }
        try {
            int porcentaje = Integer.parseInt(texto.trim());
            if (porcentaje < 0 || porcentaje > 100) {
                return "El porcentaje debe ser un número entero entre 0 y 100.";
            }
        } catch (NumberFormatException e) {
            return "El porcentaje debe ser un número entero entre 0 y 100.";
        }
        return null;
    }
}
