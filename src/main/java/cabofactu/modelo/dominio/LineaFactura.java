package cabofactu.modelo.dominio;

import cabofactu.modelo.negocio.Calculos;
import cabofactu.utilidades.Formatos;

import java.math.BigDecimal;

/**
 * Una línea de factura: cantidad, descripción, precio e IVA congelado. El
 * total de la línea no se guarda: sale siempre de la cantidad y el precio.
 */
public class LineaFactura {

    private Long id;
    private int orden;
    private int cantidad = 1;
    private String descripcion = "";
    private BigDecimal precioUnitario = BigDecimal.ZERO;
    private Long tipoIvaId;
    private String ivaNombre;
    private Integer ivaPorcentaje;
    private String ivaMotivoExencion;
    private boolean esSuplido;

    public LineaFactura(int cantidad, BigDecimal precioUnitario) throws Exception {
        setCantidad(cantidad);
        setPrecioUnitario(precioUnitario);
        setDescripcion("");
    }

    /** Copiamos una línea entera, sin su id, para poder reutilizarla en otra factura. */
    public LineaFactura(LineaFactura otra) throws Exception {
        setOrden(otra.getOrden());
        setCantidad(otra.getCantidad());
        setDescripcion(otra.getDescripcion());
        setPrecioUnitario(otra.getPrecioUnitario());
        setTipoIvaId(otra.getTipoIvaId());
        setIvaNombre(otra.getIvaNombre());
        setIvaPorcentaje(otra.getIvaPorcentaje());
        setIvaMotivoExencion(otra.getIvaMotivoExencion());
        setEsSuplido(otra.isEsSuplido());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getOrden() {
        return orden;
    }

    public void setOrden(int orden) {
        this.orden = orden;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) throws Exception {
        if (cantidad < 1) {
            throw new Exception("La cantidad debe ser 1 o más.");
        }
        this.cantidad = cantidad;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        if (descripcion == null) {
            this.descripcion = "";
        } else {
            this.descripcion = descripcion;
        }
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(BigDecimal precioUnitario) throws Exception {
        if (precioUnitario == null || precioUnitario.signum() < 0) {
            throw new Exception("El precio no puede ser negativo.");
        }
        this.precioUnitario = precioUnitario;
    }

    /** La base de la línea es cantidad por precio, redondeada a dos decimales. */
    public BigDecimal getTotalBase() {
        return Calculos.totalLinea(precioUnitario, cantidad);
    }

    public Long getTipoIvaId() {
        return tipoIvaId;
    }

    public void setTipoIvaId(Long tipoIvaId) {
        this.tipoIvaId = tipoIvaId;
    }

    public String getIvaNombre() {
        return ivaNombre;
    }

    public void setIvaNombre(String ivaNombre) {
        this.ivaNombre = ivaNombre;
    }

    public Integer getIvaPorcentaje() {
        return ivaPorcentaje;
    }

    public void setIvaPorcentaje(Integer ivaPorcentaje) {
        this.ivaPorcentaje = ivaPorcentaje;
    }

    public String getIvaMotivoExencion() {
        return ivaMotivoExencion;
    }

    public void setIvaMotivoExencion(String ivaMotivoExencion) {
        this.ivaMotivoExencion = ivaMotivoExencion;
    }

    /** Una línea es exenta cuando su tipo de IVA no lleva porcentaje. */
    public boolean isExenta() {
        return ivaPorcentaje == null;
    }

    public boolean isEsSuplido() {
        return esSuplido;
    }

    public void setEsSuplido(boolean esSuplido) {
        this.esSuplido = esSuplido;
    }

    /** Copiamos los cinco datos del tipo elegido, como al cambiar el IVA en el editor. */
    public void setTipoIva(TipoIva tipo) {
        if (tipo == null) {
            setTipoIvaId(null);
            setIvaNombre("");
            setIvaPorcentaje(null);
            setIvaMotivoExencion("");
            setEsSuplido(false);
            return;
        }
        setTipoIvaId(tipo.getId());
        setIvaNombre(tipo.getNombre());
        setIvaPorcentaje(tipo.getPorcentaje());
        setIvaMotivoExencion(tipo.getMotivoExencion());
        setEsSuplido(tipo.isEsSuplido());
    }

    /** Decimos si la línea tiene algo escrito: descripción o precio mayor que cero. */
    public boolean tieneContenido() {
        if (descripcion != null && !descripcion.isBlank()) {
            return true;
        }
        if (precioUnitario != null && precioUnitario.signum() > 0) {
            return true;
        }
        return false;
    }

    public String getCantidadTexto() {
        return String.valueOf(cantidad);
    }

    public String getPrecioUnitarioTexto() {
        return Formatos.moneda(precioUnitario);
    }

    public String getTotalBaseTexto() {
        return Formatos.moneda(getTotalBase());
    }
}
