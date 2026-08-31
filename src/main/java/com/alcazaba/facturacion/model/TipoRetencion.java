package com.alcazaba.facturacion.model;

public class TipoRetencion {

    private Long id;
    private String nombre;
    private Integer porcentaje;
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

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public String label() {
        return porcentaje + "%";
    }

    @Override
    public String toString() {
        return nombre != null && !nombre.isBlank() ? nombre + " (" + label() + ")" : label();
    }
}
