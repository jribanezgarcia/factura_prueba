package com.alcazaba.facturacion.model;

public class Serie {

    private Long id;
    private String codigo;
    private String descripcion;
    private boolean esRectificativa;
    private int siguienteCorrelativo;
    private boolean reutilizarAnulados;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public boolean isEsRectificativa() {
        return esRectificativa;
    }

    public void setEsRectificativa(boolean esRectificativa) {
        this.esRectificativa = esRectificativa;
    }

    public int getSiguienteCorrelativo() {
        return siguienteCorrelativo;
    }

    public void setSiguienteCorrelativo(int siguienteCorrelativo) {
        this.siguienteCorrelativo = siguienteCorrelativo;
    }

    public boolean isReutilizarAnulados() {
        return reutilizarAnulados;
    }

    public void setReutilizarAnulados(boolean reutilizarAnulados) {
        this.reutilizarAnulados = reutilizarAnulados;
    }

    @Override
    public String toString() {
        return codigo + (descripcion != null && !descripcion.isBlank() ? " (" + descripcion + ")" : "");
    }
}
