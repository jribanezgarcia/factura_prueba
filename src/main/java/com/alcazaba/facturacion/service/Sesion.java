package com.alcazaba.facturacion.service;

import java.time.LocalDate;

/**
 * Estado de la sesion de trabajo: empresa activa y fecha de trabajo elegidas en
 * la pantalla de arranque. La fecha de trabajo es el valor inicial de las
 * nuevas facturas y el anio de trabajo influye en los correlativos propuestos.
 */
public final class Sesion {

    private static String empresaSlug;
    private static LocalDate fechaTrabajo;

    private Sesion() {
    }

    public static void inicializar(String slug, LocalDate fecha) {
        empresaSlug = slug;
        fechaTrabajo = fecha;
    }

    public static String empresaSlug() {
        return empresaSlug;
    }

    public static LocalDate fechaTrabajo() {
        return fechaTrabajo;
    }

    public static void reiniciar() {
        empresaSlug = null;
        fechaTrabajo = null;
    }
}