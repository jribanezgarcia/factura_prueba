package com.alcazaba.facturacion.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Resultado del calculo de una factura: desglose por tipo de IVA, bases,
 * importes de IVA y totales, con el descuento global aplicado.
 */
public class ResumenFactura {

    private final List<IvaGrupo> grupos = new ArrayList<>();
    private BigDecimal baseTotal = BigDecimal.ZERO;
    private BigDecimal ivaTotal = BigDecimal.ZERO;
    private BigDecimal total = BigDecimal.ZERO;
    private int descuentoPorcentaje;

    public static class IvaGrupo {
        private String nombre;
        private Integer porcentaje;
        private String motivoExencion;
        private BigDecimal base = BigDecimal.ZERO;
        private BigDecimal cuota = BigDecimal.ZERO;

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

        public BigDecimal getCuota() {
            return cuota;
        }

        public void setCuota(BigDecimal cuota) {
            this.cuota = cuota;
        }

        public boolean isExento() {
            return porcentaje == null;
        }
    }

    public List<IvaGrupo> getGrupos() {
        return grupos;
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

    public int getDescuentoPorcentaje() {
        return descuentoPorcentaje;
    }

    public void setDescuentoPorcentaje(int descuentoPorcentaje) {
        this.descuentoPorcentaje = descuentoPorcentaje;
    }
}
