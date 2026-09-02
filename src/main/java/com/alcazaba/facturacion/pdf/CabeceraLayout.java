package com.alcazaba.facturacion.pdf;

import com.alcazaba.facturacion.model.Empresa;

import java.util.ArrayList;
import java.util.List;

/**
 * Geometria de la cabecera del PDF compartida entre la generacion del
 * documento y la previsualizacion de Configuracion: una unica fuente de
 * verdad para que el tamano efectivo del logo y el alto de cabecera que se
 * muestran al usuario coincidan con lo que se imprime.
 */
public final class CabeceraLayout {

    public static final float ANCHO_A4 = 595f;
    public static final float MARGEN_LATERAL = 40f;

    private static final float ANCHO_LOGO_DEFECTO = 120f;
    private static final float ALTO_LOGO_DEFECTO = 60f;
    private static final float ANCHO_LOGO_MAXIMO = 480f;
    private static final float ALTO_LOGO_MAXIMO = 170f;
    private static final float ALTO_CABECERA_MINIMO = 108f;

    private CabeceraLayout() {
    }

    public static float anchoLogoEfectivo(Empresa empresa) {
        float base = empresa != null && empresa.getLogoAncho() != null ? empresa.getLogoAncho() : ANCHO_LOGO_DEFECTO;
        return Math.min(base * 2f, ANCHO_LOGO_MAXIMO);
    }

    public static float altoLogoEfectivo(Empresa empresa) {
        float base = empresa != null && empresa.getLogoAlto() != null ? empresa.getLogoAlto() : ALTO_LOGO_DEFECTO;
        return Math.min(base * 2f, ALTO_LOGO_MAXIMO);
    }

    public static int offsetLogoX(Empresa empresa) {
        return empresa != null ? empresa.getLogoX() : 0;
    }

    public static int offsetLogoY(Empresa empresa) {
        return empresa != null ? empresa.getLogoY() : 0;
    }

    /**
     * Alto de la cabecera en modo texto: 42 pt de arranque mas 13 pt por
     * linea de empresa, con un minimo de 108 pt.
     */
    public static float altoCabeceraTexto(int lineas) {
        return Math.max(42f + lineas * 13f + 18f, ALTO_CABECERA_MINIMO);
    }

    /**
     * Alto de la cabecera en modo logo: la altura del logo con su
     * desplazamiento o el bloque de informacion de la empresa, el que sea
     * mayor, con un minimo de 108 pt.
     */
    public static float altoCabeceraLogo(Empresa empresa, int lineas) {
        float alto = altoLogoEfectivo(empresa);
        float desplazamiento = Math.max(offsetLogoY(empresa), 0);
        float superior = 26f + alto + desplazamiento + 24f;
        float altoInfo = 17f + lineas * 13f;
        superior = Math.max(superior, 34f + altoInfo + 8f);
        return Math.max(superior, ALTO_CABECERA_MINIMO);
    }

    public static List<LineaCabecera> lineasEmpresa(Empresa empresa) {
        List<LineaCabecera> lineas = new ArrayList<>();
        if (empresa == null) {
            return lineas;
        }
        if (!nz(empresa.getActividad()).isBlank()) {
            lineas.add(new LineaCabecera(empresa.getActividad(), false));
        }
        if (!nz(empresa.getNif()).isBlank()) {
            lineas.add(new LineaCabecera("NIF: " + empresa.getNif(), true));
        }
        if (!nz(empresa.getDireccion()).isBlank()) {
            lineas.add(new LineaCabecera(empresa.getDireccion(), false));
        }
        String poblacion = joinNoVacio(" ", nz(empresa.getCp()), nz(empresa.getLocalidad()));
        if (!poblacion.isBlank()) {
            lineas.add(new LineaCabecera(poblacion, false));
        }
        String contacto = joinNoVacio("  ·  ", nz(empresa.getEmail()), nz(empresa.getTelefono()));
        if (!contacto.isBlank()) {
            lineas.add(new LineaCabecera(contacto, false));
        }
        return lineas;
    }

    private static String joinNoVacio(String sep, String... partes) {
        StringBuilder sb = new StringBuilder();
        for (String p : partes) {
            if (p != null && !p.isBlank()) {
                if (sb.length() > 0) {
                    sb.append(sep);
                }
                sb.append(p);
            }
        }
        return sb.toString();
    }

    private static String nz(String s) {
        return s == null ? "" : s;
    }

    public static final class LineaCabecera {

        public final String texto;
        public final boolean chipNif;

        LineaCabecera(String texto, boolean chipNif) {
            this.texto = texto;
            this.chipNif = chipNif;
        }
    }
}