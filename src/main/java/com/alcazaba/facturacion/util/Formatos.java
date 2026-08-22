package com.alcazaba.facturacion.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Formato espanol de importes y fechas.
 */
public final class Formatos {

    private static final Locale ES = new Locale("es", "ES");
    private static final DecimalFormat MONEDA = new DecimalFormat("###,##0.00\u00a0\u20ac", DecimalFormatSymbols.getInstance(ES));
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy", ES);
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", ES);

    private Formatos() {
    }

    public static String moneda(BigDecimal valor) {
        if (valor == null) {
            return "";
        }
        return MONEDA.format(valor);
    }

    public static String fecha(LocalDate fecha) {
        return fecha == null ? "" : FECHA.format(fecha);
    }

    public static String fechaHora(java.time.LocalDateTime fechaHora) {
        return fechaHora == null ? "" : FECHA_HORA.format(fechaHora);
    }

    public static BigDecimal parseMoneda(String texto) {
        if (texto == null || texto.isBlank()) {
            return BigDecimal.ZERO;
        }
        String limpio = texto.trim()
                .replace("\u00a0", "")
                .replace("\u20ac", "")
                .replace(".", "")
                .replace(",", ".");
        try {
            return new BigDecimal(limpio);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    /** Devuelve null cuando no hay filtro de importe informado. */
    public static BigDecimal parseMonedaOpcional(String texto) {
        return texto == null || texto.isBlank() ? null : parseMoneda(texto);
    }

    /** Nombre de archivo PDF para un numero de factura: la barra se sustituye por guion. */
    public static String nombreArchivoPdf(String numeroFactura) {
        if (numeroFactura == null || numeroFactura.isBlank()) {
            return "factura.pdf";
        }
        return numeroFactura.trim().replace('/', '-') + ".pdf";
    }

    /**
     * Parseo de importes tecleados en celdas de linea: admite coma o punto como
     * separador decimal y devuelve null si no es numerico.
     */
    public static BigDecimal parseEntrada(String texto) {
        if (texto == null || texto.isBlank()) {
            return BigDecimal.ZERO;
        }
        String t = texto.trim().replace("\u00a0", "").replace("\u20ac", "");
        if (t.contains(",")) {
            t = t.replace(".", "").replace(",", ".");
        }
        try {
            return new BigDecimal(t);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
