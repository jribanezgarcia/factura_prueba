package com.alcazaba.facturacion.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class FacturaVersion {

    private Long id;
    private Long facturaId;
    private int versionNum;
    private String numero;
    private LocalDate fechaFactura;
    private LocalDateTime fechaGuardado;
    private EstadoFactura estado;
    private int descuentoPorcentaje;
    private String observaciones;
    private String referenciaRectifica;
    private String cliNombre;
    private String cliNif;
    private String cliDireccion;
    private String cliCp;
    private String cliLocalidad;
    private String cliProvincia;
    private BigDecimal baseTotal = BigDecimal.ZERO;
    private BigDecimal ivaTotal = BigDecimal.ZERO;
    private BigDecimal total = BigDecimal.ZERO;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getFacturaId() {
        return facturaId;
    }

    public void setFacturaId(Long facturaId) {
        this.facturaId = facturaId;
    }

    public int getVersionNum() {
        return versionNum;
    }

    public void setVersionNum(int versionNum) {
        this.versionNum = versionNum;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public LocalDate getFechaFactura() {
        return fechaFactura;
    }

    public void setFechaFactura(LocalDate fechaFactura) {
        this.fechaFactura = fechaFactura;
    }

    public LocalDateTime getFechaGuardado() {
        return fechaGuardado;
    }

    public void setFechaGuardado(LocalDateTime fechaGuardado) {
        this.fechaGuardado = fechaGuardado;
    }

    public EstadoFactura getEstado() {
        return estado;
    }

    public void setEstado(EstadoFactura estado) {
        this.estado = estado;
    }

    public int getDescuentoPorcentaje() {
        return descuentoPorcentaje;
    }

    public void setDescuentoPorcentaje(int descuentoPorcentaje) {
        this.descuentoPorcentaje = descuentoPorcentaje;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public String getReferenciaRectifica() {
        return referenciaRectifica;
    }

    public void setReferenciaRectifica(String referenciaRectifica) {
        this.referenciaRectifica = referenciaRectifica;
    }

    public String getCliNombre() {
        return cliNombre;
    }

    public void setCliNombre(String cliNombre) {
        this.cliNombre = cliNombre;
    }

    public String getCliNif() {
        return cliNif;
    }

    public void setCliNif(String cliNif) {
        this.cliNif = cliNif;
    }

    public String getCliDireccion() {
        return cliDireccion;
    }

    public void setCliDireccion(String cliDireccion) {
        this.cliDireccion = cliDireccion;
    }

    public String getCliCp() {
        return cliCp;
    }

    public void setCliCp(String cliCp) {
        this.cliCp = cliCp;
    }

    public String getCliLocalidad() {
        return cliLocalidad;
    }

    public void setCliLocalidad(String cliLocalidad) {
        this.cliLocalidad = cliLocalidad;
    }

    public String getCliProvincia() {
        return cliProvincia;
    }

    public void setCliProvincia(String cliProvincia) {
        this.cliProvincia = cliProvincia;
    }

    public BigDecimal getBaseTotal() {
        return baseTotal;
    }

    public void setBaseTotal(BigDecimal baseTotal) {
        this.baseTotal = baseTotal;
    }

    public BigDecimal getIvaTotal() {
        return ivaTotal;
    }

    public void setIvaTotal(BigDecimal ivaTotal) {
        this.ivaTotal = ivaTotal;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }
}
