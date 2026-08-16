package com.alcazaba.facturacion.model;

import java.math.BigDecimal;

public class LineaFactura {

    private Long id;
    private int orden;
    private int cantidad = 1;
    private String descripcion;
    private BigDecimal precioUnitario = BigDecimal.ZERO;
    private BigDecimal totalBase = BigDecimal.ZERO;
    private Long tipoIvaId;
    private String ivaNombre;
    private Integer ivaPorcentaje;
    private String ivaMotivoExencion;
    private BigDecimal ivaImporte = BigDecimal.ZERO;

    public LineaFactura() {
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

    public BigDecimal getTotalBase() {
        return totalBase;
    }

    public void setTotalBase(BigDecimal totalBase) {
        this.totalBase = totalBase;
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

    public BigDecimal getIvaImporte() {
        return ivaImporte;
    }

    public void setIvaImporte(BigDecimal ivaImporte) {
        this.ivaImporte = ivaImporte;
    }

    public boolean isExenta() {
        return ivaPorcentaje == null;
    }

    public LineaFactura copia() {
        LineaFactura c = new LineaFactura();
        c.setOrden(orden);
        c.setCantidad(cantidad);
        c.setDescripcion(descripcion);
        c.setPrecioUnitario(precioUnitario);
        c.setTotalBase(totalBase);
        c.setTipoIvaId(tipoIvaId);
        c.setIvaNombre(ivaNombre);
        c.setIvaPorcentaje(ivaPorcentaje);
        c.setIvaMotivoExencion(ivaMotivoExencion);
        c.setIvaImporte(ivaImporte);
        return c;
    }
}
