package com.alcazaba.facturacion.service;

import com.alcazaba.facturacion.model.Serie;
import com.alcazaba.facturacion.repository.SerieRepository;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Set;

/**
 * Reglas de numeracion por series.
 * - Series normales: CODIGO-CORRELATIVO/MES (p. ej. C-59/8); el mes deriva de
 *   la fecha y el correlativo es la identidad.
 * - Serie rectificativa: CODIGO-CORRELATIVO (p. ej. R-1), sin mes.
 * - El separador del numero es la barra; el guion separa codigo y correlativo.
 */
public class NumeroService {

    private final SerieRepository serieRepository;

    public NumeroService(SerieRepository serieRepository) {
        this.serieRepository = serieRepository;
    }

    public String formarNumero(Serie serie, int correlativo, LocalDate fecha) {
        if (serie.isEsRectificativa()) {
            return serie.getCodigo() + "-" + correlativo;
        }
        return serie.getCodigo() + "-" + correlativo + "/" + fecha.getMonthValue();
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
     * es editable en creacion). Formatos: C-59/8 o R-1. Devuelve null si el
     * numero no se ajusta a la serie indicada.
     */
    public Integer parseCorrelativo(Serie serie, String numero) {
        if (numero == null || serie == null) {
            return null;
        }
        String t = numero.trim();
        String pref = serie.getCodigo() + "-";
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
        int slash = resto.indexOf('/');
        String corr = slash >= 0 ? resto.substring(0, slash) : resto;
        try {
            return Integer.parseInt(corr);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
