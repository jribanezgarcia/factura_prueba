package com.alcazaba.facturacion.model;

public class TipoIva {

    private Long id;
    private String nombre;
    private Integer porcentaje;
    private String motivoExencion;
    private boolean activo = true;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public boolean isExento() {
        return porcentaje == null;
    }

    public String label() {
        if (isExento()) {
            return "Exento";
        }
        return porcentaje + "%";
    }

    @Override
    public String toString() {
        return nombre != null && !nombre.isBlank() ? nombre : label();
    }
}
