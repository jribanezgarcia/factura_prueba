package com.alcazaba.facturacion.util;

/** Valida códigos postales españoles: cinco dígitos con las dos primeras cifras entre 01 y 52. El código es obligatorio: vacío NO válido. */
public final class CodigoPostalValidator {

    private CodigoPostalValidator() {
    }

    public static boolean esValido(String valor) {
        if (valor == null) {
            return false;
        }
        String cp = valor.trim();
        if (!cp.matches("\\d{5}")) {
            return false;
        }
        int provincia = Integer.parseInt(cp.substring(0, 2));
        return provincia >= 1 && provincia <= 52;
    }
}