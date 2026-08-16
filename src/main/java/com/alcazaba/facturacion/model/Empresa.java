package com.alcazaba.facturacion.model;

public class Empresa {

    private String nombre;
    private String nif;
    private String direccion;
    private String cp;
    private String localidad;
    private String provincia;
    private String actividad;
    private String email;
    private String telefono;
    private String cabeceraModo = "TEXTO";
    private String logoPath;
    private int logoX;
    private int logoY;
    private Integer logoAncho;
    private Integer logoAlto;
    private String pieLegal;

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getNif() {
        return nif;
    }

    public void setNif(String nif) {
        this.nif = nif;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getCp() {
        return cp;
    }

    public void setCp(String cp) {
        this.cp = cp;
    }

    public String getLocalidad() {
        return localidad;
    }

    public void setLocalidad(String localidad) {
        this.localidad = localidad;
    }

    public String getProvincia() {
        return provincia;
    }

    public void setProvincia(String provincia) {
        this.provincia = provincia;
    }

    public String getActividad() {
        return actividad;
    }

    public void setActividad(String actividad) {
        this.actividad = actividad;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getCabeceraModo() {
        return cabeceraModo;
    }

    public void setCabeceraModo(String cabeceraModo) {
        this.cabeceraModo = cabeceraModo;
    }

    public String getLogoPath() {
        return logoPath;
    }

    public void setLogoPath(String logoPath) {
        this.logoPath = logoPath;
    }

    public int getLogoX() {
        return logoX;
    }

    public void setLogoX(int logoX) {
        this.logoX = logoX;
    }

    public int getLogoY() {
        return logoY;
    }

    public void setLogoY(int logoY) {
        this.logoY = logoY;
    }

    public Integer getLogoAncho() {
        return logoAncho;
    }

    public void setLogoAncho(Integer logoAncho) {
        this.logoAncho = logoAncho;
    }

    public Integer getLogoAlto() {
        return logoAlto;
    }

    public void setLogoAlto(Integer logoAlto) {
        this.logoAlto = logoAlto;
    }

    public String getPieLegal() {
        return pieLegal;
    }

    public void setPieLegal(String pieLegal) {
        this.pieLegal = pieLegal;
    }
}
