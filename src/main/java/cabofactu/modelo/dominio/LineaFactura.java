package cabofactu.modelo.dominio;

import cabofactu.modelo.negocio.Calculos;

import java.math.BigDecimal;

/**
 * Una línea de factura: cantidad, descripción, precio e IVA congelado. El
 * total de la línea no se guarda: sale siempre de la cantidad y el precio.
 */
public class LineaFactura {

    private Long id;
    private int orden;
    private int cantidad = 1;
    private String descripcion;
    private BigDecimal precioUnitario = BigDecimal.ZERO;
    private Long tipoIvaId;
    private String ivaNombre;
    private Integer ivaPorcentaje;
    private String ivaMotivoExencion;
    private boolean esSuplido;

    public LineaFactura() {
    }

    /** Copiamos una línea entera, sin su id, para poder reutilizarla en otra factura. */
    public LineaFactura(LineaFactura otra) {
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

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(BigDecimal precioUnitario) {
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
}
