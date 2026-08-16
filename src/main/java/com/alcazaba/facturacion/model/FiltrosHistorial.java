package com.alcazaba.facturacion.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Filtros combinables del historico.
 */
public class FiltrosHistorial {

    private String serieCodigo;
    private String clienteTexto;
    private LocalDate fechaDesde;
    private LocalDate fechaHasta;
    private BigDecimal importeDesde;
    private BigDecimal importeHasta;
    private EstadoFactura estado;

    public String getSerieCodigo() {
        return serieCodigo;
    }

    public void setSerieCodigo(String serieCodigo) {
        this.serieCodigo = serieCodigo;
    }

    public String getClienteTexto() {
        return clienteTexto;
    }

    public void setClienteTexto(String clienteTexto) {
        this.clienteTexto = clienteTexto;
    }

    public LocalDate getFechaDesde() {
        return fechaDesde;
    }

    public void setFechaDesde(LocalDate fechaDesde) {
        this.fechaDesde = fechaDesde;
    }

    public LocalDate getFechaHasta() {
        return fechaHasta;
    }

    public void setFechaHasta(LocalDate fechaHasta) {
        this.fechaHasta = fechaHasta;
    }

    public BigDecimal getImporteDesde() {
        return importeDesde;
    }

    public void setImporteDesde(BigDecimal importeDesde) {
        this.importeDesde = importeDesde;
    }

    public BigDecimal getImporteHasta() {
        return importeHasta;
    }

    public void setImporteHasta(BigDecimal importeHasta) {
        this.importeHasta = importeHasta;
    }

    public EstadoFactura getEstado() {
        return estado;
    }

    public void setEstado(EstadoFactura estado) {
        this.estado = estado;
    }
}
