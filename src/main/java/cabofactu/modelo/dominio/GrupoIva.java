package cabofactu.modelo.dominio;

import cabofactu.utilidades.Formatos;

import java.math.BigDecimal;

/**
 * Agrupamos en un solo sitio la base y la cuota de cada tipo de IVA, para
 * pintar la matriz del editor y la rejilla del PDF con los mismos importes.
 */
public class GrupoIva {

    private String nombre;
    private Integer porcentaje;
    private String motivoExencion;
    private BigDecimal base = BigDecimal.ZERO;
    private BigDecimal baseBruta = BigDecimal.ZERO;
    private BigDecimal cuota = BigDecimal.ZERO;

    public GrupoIva(String nombre, Integer porcentaje, String motivoExencion) {
        setNombre(nombre);
        setPorcentaje(porcentaje);
        setMotivoExencion(motivoExencion);
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Integer getPorcentaje() {
        return porcentaje;
    }

    public void setPorcentaje(Integer porcentaje) {
        this.porcentaje = porcentaje;
    }

    public String getMotivoExencion() {
        return motivoExencion;
    }

    public void setMotivoExencion(String motivoExencion) {
        this.motivoExencion = motivoExencion;
    }

    public BigDecimal getBase() {
        return base;
    }

    public void setBase(BigDecimal base) {
        this.base = base;
    }

    public BigDecimal getBaseBruta() {
        return baseBruta;
    }

    public void setBaseBruta(BigDecimal baseBruta) {
        this.baseBruta = baseBruta;
    }

    public BigDecimal getCuota() {
        return cuota;
    }

    public void setCuota(BigDecimal cuota) {
        this.cuota = cuota;
    }

    public boolean isExento() {
        return porcentaje == null;
    }

    /** «Totales», «Exento (motivo)» o «nombre 21%», para la primera columna de la matriz. */
    public String getEtiqueta() {
        if ("Totales".equals(nombre)) {
            return "Totales";
        }
        if (isExento()) {
            if (motivoExencion != null && !motivoExencion.isBlank()) {
                return "Exento (" + motivoExencion + ")";
            }
            return "Exento";
        }
        String limpio = "";
        if (nombre != null) {
            limpio = nombre.trim();
        }
        if (limpio.endsWith("%")) {
            return nombre;
        }
        if (limpio.isEmpty()) {
            return porcentaje + "%";
        }
        return limpio + " " + porcentaje + "%";
    }

    public String getBaseTexto() {
        return Formatos.moneda(base);
    }

    public String getCuotaTexto() {
        return Formatos.moneda(cuota);
    }
}
