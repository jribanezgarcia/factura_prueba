package com.alcazaba.facturacion.service;

import com.alcazaba.facturacion.model.Serie;
import com.alcazaba.facturacion.repository.SerieRepository;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Set;

/**
 * Reglas de numeracion por series.
 * - Formato MES: CODIGO-CORRELATIVO/MES (p. ej. C-59/8) o CORRELATIVO/MES (p. ej. 59/8)
 * - Formato ANIO: CODIGO-CORRELATIVO-ANIO (p. ej. C-59-2026) o CORRELATIVO-ANIO (p. ej. 59-2026)
 * - Formato NINGUNO: CODIGO-CORRELATIVO (p. ej. C-59) o CORRELATIVO (p. ej. 59)
 * - Serie rectificativa: siempre CODIGO-CORRELATIVO (p. ej. R-1) o CORRELATIVO, sin fecha.
 * - El guion separa codigo y correlativo; la barra separa correlativo y mes.
 */
public class NumeroService {

    private final SerieRepository serieRepository;

    public NumeroService(SerieRepository serieRepository) {
        this.serieRepository = serieRepository;
    }

    public String formarNumero(Serie serie, int correlativo, LocalDate fecha) {
        if (serie.isEsRectificativa()) {
            String codigo = serie.getCodigo();
            return (codigo != null && !codigo.isBlank()) ? codigo + "-" + correlativo : String.valueOf(correlativo);
        }
        String codigo = serie.getCodigo();
        boolean tieneCodigo = codigo != null && !codigo.isBlank();
        Serie.SufijoFecha sufijo = serie.getSufijoFecha() != null ? serie.getSufijoFecha() : Serie.SufijoFecha.MES;
        String prefijo = tieneCodigo ? codigo + "-" : "";
        return switch (sufijo) {
            case MES -> prefijo + correlativo + "/" + fecha.getMonthValue();
            case ANIO -> prefijo + correlativo + "-" + fecha.getYear();
            case NINGUNO -> prefijo + correlativo;
        };
    }

    /**
     * Propone el siguiente correlativo libre de la serie: si reutiliza anulados,
     * el menor correlativo de facturas anuladas que no este ocupado por una activa;
     * en caso contrario, el siguiente_correlativo configurado.
     */
    public int siguienteCorrelativo(Serie serie) throws SQLException {
        if (serie.isReutilizarAnulados()) {
            Set<Integer> anuladas = serieRepository.correlativosAnuladas(serie.getId());
            Set<Integer> activos = serieRepository.correlativosActivos(serie.getId());
            int min = Integer.MAX_VALUE;
            for (int c : anuladas) {
                if (c >= 1 && !activos.contains(c) && c < min) {
                    min = c;
                }
            }
            if (min != Integer.MAX_VALUE) {
                return min;
            }
        }
        return serie.getSiguienteCorrelativo();
    }

    public boolean correlativoOcupadoPorActiva(Serie serie, int correlativo) throws SQLException {
        return serieRepository.correlativosActivos(serie.getId()).contains(correlativo);
    }

    /**
     * Extrae el correlativo de un numero escrito manualmente (el campo numero
     * es editable en creacion). Formatos segun sufijo_fecha:
     * MES: C-59/8 o 59/8
     * ANIO: C-59-2026 o 59-2026
     * NINGUNO: C-59 o 59
     * Rectificativa: C-1 o 1
     * Devuelve null si el numero no se ajusta a la serie indicada.
     */
    public Integer parseCorrelativo(Serie serie, String numero) {
        if (numero == null || serie == null) {
            return null;
        }
        String t = numero.trim();
        String codigo = serie.getCodigo();
        boolean tieneCodigo = codigo != null && !codigo.isBlank();
        String pref = tieneCodigo ? codigo + "-" : "";
        if (!t.startsWith(pref)) {
            return null;
        }
        String resto = t.substring(pref.length());
        if (serie.isEsRectificativa()) {
            try {
                return Integer.parseInt(resto);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        Serie.SufijoFecha sufijo = serie.getSufijoFecha() != null ? serie.getSufijoFecha() : Serie.SufijoFecha.MES;
        String corr;
        if (sufijo == Serie.SufijoFecha.MES) {
            int slash = resto.indexOf('/');
            corr = slash >= 0 ? resto.substring(0, slash) : resto;
        } else if (sufijo == Serie.SufijoFecha.ANIO) {
            int dash = resto.indexOf('-');
            corr = dash >= 0 ? resto.substring(0, dash) : resto;
        } else { // NINGUNO
            corr = resto;
        }
        try {
            return Integer.parseInt(corr);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
