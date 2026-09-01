package com.alcazaba.facturacion.service;

import com.alcazaba.facturacion.model.Serie;
import com.alcazaba.facturacion.repository.NumeroDisponibleRepository;
import com.alcazaba.facturacion.repository.SerieRepository;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
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
    private final NumeroDisponibleRepository numeroDisponibleRepository;

    public NumeroService(SerieRepository serieRepository, NumeroDisponibleRepository numeroDisponibleRepository) {
        this.serieRepository = serieRepository;
        this.numeroDisponibleRepository = numeroDisponibleRepository;
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
     * Propone el siguiente correlativo libre de la serie para el anio en curso.
     */
    public int siguienteCorrelativo(Serie serie) throws SQLException {
        return siguienteCorrelativo(serie, LocalDate.now());
    }

    /**
     * Propone el siguiente correlativo libre de la serie para el anio de la
     * fecha indicada: si reutiliza anulados, el menor correlativo de facturas
     * anuladas de ese anio que no este ocupado por una activa del mismo anio;
     * en caso contrario, el siguiente guardado para ese anio. Nunca propone un
     * correlativo ocupado por una factura activa del anio.
     */
    public int siguienteCorrelativo(Serie serie, LocalDate fecha) throws SQLException {
        int anio = fecha != null ? fecha.getYear() : LocalDate.now().getYear();
        List<Integer> huecos = huecosDisponibles(serie, fecha);
        if (!huecos.isEmpty()) {
            return huecos.get(0);
        }
        if (serie.isReutilizarAnulados()) {
            Set<Integer> anuladas = serieRepository.correlativosAnuladas(serie.getId(), anio);
            Set<Integer> activos = serieRepository.correlativosActivos(serie.getId(), anio);
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
        int siguiente = serieRepository.getSiguiente(serie.getId(), anio);
        for (int ocupado : serieRepository.correlativosActivos(serie.getId(), anio)) {
            siguiente = Math.max(siguiente, ocupado + 1);
        }
        return siguiente;
    }

    /**
     * Correlativos liberados por borrado de facturas para la serie y anio
     * indicados, excluyendo los que esten ocupados por facturas activas.
     */
    public List<Integer> huecosDisponibles(Serie serie, LocalDate fecha) throws SQLException {
        int anio = fecha != null ? fecha.getYear() : LocalDate.now().getYear();
        Set<Integer> activos = serieRepository.correlativosActivos(serie.getId(), anio);
        List<Integer> todos = numeroDisponibleRepository.listar(serie.getId(), anio);
        List<Integer> disponibles = new ArrayList<>();
        for (int c : todos) {
            if (c >= 1 && !activos.contains(c)) {
                disponibles.add(c);
            }
        }
        return disponibles;
    }

    /**
     * Propone una lista ordenada de {@code cantidad} correlativos libres para
     * la serie y el año. Si {@code usarHuecos} es true, se rellenan primero
     * los huecos registrados en numero_disponible y luego se continua con los
     * siguientes números que no esten ocupados por facturas activas.
     */
    public List<Integer> proponerNumeros(Serie serie, int anio, int cantidad, boolean usarHuecos)
            throws SQLException {
        if (cantidad <= 0) {
            return List.of();
        }
        Set<Integer> ocupados = serieRepository.correlativosActivos(serie.getId(), anio);
        java.util.SortedSet<Integer> disponibles = new java.util.TreeSet<>();
        if (usarHuecos) {
            disponibles.addAll(huecosDisponibles(serie, LocalDate.of(anio, 1, 1)));
        }
        int siguiente = serieRepository.getSiguiente(serie.getId(), anio);
        int c = siguiente;
        while (disponibles.size() < cantidad) {
            if (!ocupados.contains(c)) {
                disponibles.add(c);
            }
            c++;
        }
        return new ArrayList<>(disponibles).subList(0, cantidad);
    }

    public boolean correlativoOcupadoPorActiva(Serie serie, int correlativo) throws SQLException {
        return correlativoOcupadoPorActiva(serie, correlativo, LocalDate.now());
    }

    public boolean correlativoOcupadoPorActiva(Serie serie, int correlativo, LocalDate fecha) throws SQLException {
        int anio = fecha != null ? fecha.getYear() : LocalDate.now().getYear();
        return serieRepository.correlativosActivos(serie.getId(), anio).contains(correlativo);
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
