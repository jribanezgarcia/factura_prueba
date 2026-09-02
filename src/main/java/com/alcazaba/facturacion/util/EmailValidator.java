package com.alcazaba.facturacion.util;

/** Valida un email con un patrón razonable. Vacío es opcional y válido. */
public final class EmailValidator {

    private static final String PATRON = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

    private EmailValidator() {
    }

    public static boolean esValido(String valor) {
        if (valor == null || valor.isBlank()) {
            return true;
        }
        return valor.trim().matches(PATRON);
    }
}