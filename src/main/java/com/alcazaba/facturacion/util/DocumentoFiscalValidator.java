package com.alcazaba.facturacion.util;

/** Valida DNI, NIE y NIF/CIF españoles. Un valor vacío es opcional y válido. */
public final class DocumentoFiscalValidator {

    private static final String LETRAS_DNI = "TRWAGMYFPDXBNJZSQVHLCKE";

    private DocumentoFiscalValidator() {
    }

    public static boolean esValido(String valor) {
        if (valor == null || valor.isBlank()) {
            return true;
        }
        String nif = valor.replaceAll("\\s+", "").toUpperCase();
        if (nif.matches("\\d{8}[A-Z]")) {
            return letraDni(nif.substring(0, 8)) == nif.charAt(8);
        }
        if (nif.matches("[XYZ]\\d{7}[A-Z]")) {
            String numero = switch (nif.charAt(0)) {
                case 'X' -> "0";
                case 'Y' -> "1";
                default -> "2";
            } + nif.substring(1, 8);
            return letraDni(numero) == nif.charAt(8);
        }
        return esCifValido(nif);
    }

    private static char letraDni(String numero) {
        return LETRAS_DNI.charAt(Integer.parseInt(numero) % 23);
    }

    private static boolean esCifValido(String nif) {
        if (!nif.matches("[ABCDEFGHJKLMNPQRSUVW]\\d{7}[0-9A-J]")) {
            return false;
        }
        int suma = 0;
        for (int i = 1; i <= 7; i++) {
            int digito = nif.charAt(i) - '0';
            if (i % 2 == 1) {
                int doble = digito * 2;
                suma += doble / 10 + doble % 10;
            } else {
                suma += digito;
            }
        }
        int control = (10 - suma % 10) % 10;
        char esperadoDigito = (char) ('0' + control);
        char esperadoLetra = "JABCDEFGHI".charAt(control);
        char prefijo = nif.charAt(0);
        char recibido = nif.charAt(8);
        if ("PQRSNW".indexOf(prefijo) >= 0) {
            return recibido == esperadoLetra;
        }
        if ("ABEH".indexOf(prefijo) >= 0) {
            return recibido == esperadoDigito;
        }
        return recibido == esperadoDigito || recibido == esperadoLetra;
    }
}
