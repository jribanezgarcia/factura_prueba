package cabofactu.modelo.negocio;

import java.time.LocalDate;

/**
 * La empresa abierta y la fecha de trabajo elegidas en el arranque. La fecha
 * de trabajo es la inicial de las facturas nuevas.
 */
public class Sesion {

    private static Sesion sesion;
    private String carpetaEmpresa;
    private LocalDate fechaTrabajo;

    private Sesion() {
    }

    public static Sesion getSesion() {
        if (sesion == null) {
            sesion = new Sesion();
        }
        return sesion;
    }

    public void iniciar(String carpetaEmpresa, LocalDate fechaTrabajo) {
        this.carpetaEmpresa = carpetaEmpresa;
        this.fechaTrabajo = fechaTrabajo;
    }

    /** Olvidamos la empresa y la fecha: no hay ninguna empresa abierta. */
    public void terminar() {
        carpetaEmpresa = null;
        fechaTrabajo = null;
    }

    public String getCarpetaEmpresa() {
        return carpetaEmpresa;
    }

    public LocalDate getFechaTrabajo() {
        return fechaTrabajo;
    }
}
