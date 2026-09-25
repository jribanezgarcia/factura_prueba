package cabofactu.modelo.negocio;

import cabofactu.modelo.dominio.GrupoIva;
import cabofactu.modelo.dominio.LineaFactura;
import cabofactu.modelo.dominio.ResumenFactura;
import cabofactu.modelo.dominio.TipoRetencion;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Servicio central de calculo monetario. Siempre BigDecimal y HALF_UP.
 * Reglas:
 * - total linea = cantidad x precio (redondeado a 2 decimales).
 * - precio se deduce del total con precision interna (6 decimales).
 * - entrada "con IVA": base = totalConIVA / (1 + tipo), IVA = resto.
 * - descuento global: se reduce cada base de IVA en el mismo porcentaje y se
 *   ajustan centimos en la mayor base para que la suma cuadre con el total
 *   base descontado.
 * - el resumen expone ademas la base bruta (antes del descuento) y el
 *   importe descontado para poder pintar el cuadre en el PDF.
 * - retencion de IRPF: se aplica sobre la base imponible (despues del
 *   descuento), la misma base sobre la que se calcula el IVA.
 * - suplidos: las lineas marcadas como suplido no entran en bases, cuotas,
 *   ajuste de centimos, descuento ni base de retencion; se acumulan aparte
 *   en totalSuplidos y se suman al total.
 */
public final class Calculos {

    public static final int SCALE = 2;
    private static final BigDecimal CIEN = new BigDecimal("100");
    private static final int PRECISION_INTERNA = 6;

    private Calculos() {
    }

    public static BigDecimal round2(BigDecimal v) {
        if (v == null) {
            return BigDecimal.ZERO;
        }
        return v.setScale(SCALE, RoundingMode.HALF_UP);
    }

    public static BigDecimal totalLinea(BigDecimal precio, int cantidad) {
        if (precio == null) {
            precio = BigDecimal.ZERO;
        }
        return round2(precio.multiply(BigDecimal.valueOf(cantidad)));
    }

    /**
     * Total de la linea con el IVA incluido (base × (1 + IVA%)); las
     * exentas se muestran sin IVA. No es lo mismo que {@link #totalLinea}:
     * aquel multiplica cantidad por precio sin IVA, este parte de la base ya
     * calculada y le suma la cuota.
     */
    public static BigDecimal totalConIva(LineaFactura l) {
        BigDecimal base = totalLinea(l.getPrecioUnitario(), l.getCantidad());
        if (l.isExenta() || l.getIvaPorcentaje() == null || l.getIvaPorcentaje() == 0) {
            return base;
        }
        return base.add(ivaDeBase(base, l.getIvaPorcentaje()));
    }

    /**
     * Lineas marcadas como suplido. Se filtra por el flag congelado de cada
     * linea, no por el catalogo actual de tipos: una factura antigua sigue
     * clasificando igual aunque el tipo se haya cambiado despues.
     */
    public static List<LineaFactura> suplidosDe(List<LineaFactura> lineas) {
        List<LineaFactura> suplidos = new ArrayList<>();
        if (lineas == null) {
            return suplidos;
        }
        for (LineaFactura linea : lineas) {
            if (linea.isEsSuplido()) {
                suplidos.add(linea);
            }
        }
        return suplidos;
    }

    public static BigDecimal precioDesdeTotal(BigDecimal total, int cantidad) {
        if (cantidad <= 0 || total == null) {
            return BigDecimal.ZERO;
        }
        return total.divide(BigDecimal.valueOf(cantidad), PRECISION_INTERNA, RoundingMode.HALF_UP);
    }

    public static BigDecimal ivaDeBase(BigDecimal base, Integer porcentaje) {
        if (base == null || porcentaje == null) {
            return BigDecimal.ZERO;
        }
        return round2(base.multiply(BigDecimal.valueOf(porcentaje)).divide(CIEN, PRECISION_INTERNA, RoundingMode.HALF_UP));
    }

    /** Entrada de total final con IVA incluido: calculamos hacia atrás su base. */
    public static BigDecimal baseDesdeTotalConIva(BigDecimal totalConIva, Integer porcentaje) {
        if (totalConIva == null) {
            totalConIva = BigDecimal.ZERO;
        }
        if (porcentaje == null) {
            return round2(totalConIva);
        }
        BigDecimal factor = CIEN.add(BigDecimal.valueOf(porcentaje)).divide(CIEN, PRECISION_INTERNA, RoundingMode.HALF_UP);
        return totalConIva.divide(factor, SCALE, RoundingMode.HALF_UP);
    }

    /**
     * Calcula el resumen completo de la factura con el descuento global aplicado
     * y el desglose por tipo de IVA.
     */
    public static ResumenFactura resumen(List<LineaFactura> lineas, int descuento) {
        return resumen(lineas, descuento, null);
    }

    /**
     * Calcula el resumen completo de la factura con descuento global, desglose
     * por tipo de IVA y retencion de IRPF aplicada sobre la base imponible.
     */
    public static ResumenFactura resumen(List<LineaFactura> lineas, int descuento, TipoRetencion retencion) {
        BigDecimal factor = factorDescuento(descuento);
        Map<String, BigDecimal> bases = basesPorClave(lineas);
        BigDecimal baseTotalDescontada = round2(sumarBases(bases).multiply(factor));
        List<GrupoIva> grupos = gruposDescontados(bases, factor);
        ajustarCentimos(grupos, baseTotalDescontada);

        ResumenFactura resumen = new ResumenFactura();
        resumen.setDescuentoPorcentaje(descuento);
        resumen.setBaseTotal(baseTotalDescontada);
        resumen.setBaseBruta(round2(sumarBases(bases)));
        resumen.setImporteDescuento(round2(sumarBases(bases).subtract(baseTotalDescontada)));
        resumen.setIvaTotal(sumarCuotas(grupos));
        resumen.setTotalSuplidos(round2(totalSuplidos(lineas)));
        ponerRetencion(resumen, retencion, baseTotalDescontada);
        resumen.setTotal(round2(baseTotalDescontada.add(resumen.getIvaTotal())
                .subtract(resumen.getImporteRetencion()).add(resumen.getTotalSuplidos())));
        resumen.getGrupos().addAll(grupos);
        return resumen;
    }

    /** El factor que deja el descuento global: 1 con descuento 0 y 0 con descuento 100. */
    private static BigDecimal factorDescuento(int descuento) {
        return CIEN.subtract(BigDecimal.valueOf(descuento)).divide(CIEN, PRECISION_INTERNA, RoundingMode.HALF_UP);
    }

    /** Sumamos las bases sin suplidos por su clave de IVA, conservando su orden. */
    private static Map<String, BigDecimal> basesPorClave(List<LineaFactura> lineas) {
        Map<String, BigDecimal> bases = new LinkedHashMap<>();
        if (lineas == null) {
            return bases;
        }
        for (LineaFactura linea : lineas) {
            if (linea.isEsSuplido()) {
                continue;
            }
            String clave = claveIva(linea);
            BigDecimal base = totalLinea(linea.getPrecioUnitario(), linea.getCantidad());
            BigDecimal acumulada = bases.get(clave);
            if (acumulada == null) {
                bases.put(clave, base);
            } else {
                bases.put(clave, acumulada.add(base));
            }
        }
        return bases;
    }

    /** Sumamos todas las bases agrupadas. */
    private static BigDecimal sumarBases(Map<String, BigDecimal> bases) {
        BigDecimal suma = BigDecimal.ZERO;
        for (BigDecimal base : bases.values()) {
            suma = suma.add(base);
        }
        return suma;
    }

    /** Sumamos aparte los suplidos: solo entran en el total de la factura. */
    private static BigDecimal totalSuplidos(List<LineaFactura> lineas) {
        BigDecimal suma = BigDecimal.ZERO;
        if (lineas == null) {
            return suma;
        }
        for (LineaFactura linea : lineas) {
            if (linea.isEsSuplido()) {
                suma = suma.add(totalLinea(linea.getPrecioUnitario(), linea.getCantidad()));
            }
        }
        return suma;
    }

    /** Pasamos cada base agrupada a su grupo de IVA, ya con el descuento aplicado. */
    private static List<GrupoIva> gruposDescontados(Map<String, BigDecimal> bases, BigDecimal factor) {
        List<GrupoIva> grupos = new ArrayList<>();
        for (Map.Entry<String, BigDecimal> entrada : bases.entrySet()) {
            String[] partes = entrada.getKey().split("\\|", -1);
            String nombre = partes[0];
            Integer porcentaje = null;
            if (partes.length > 1 && !partes[1].isEmpty()) {
                porcentaje = Integer.valueOf(partes[1]);
            }
            String motivo = "";
            if (partes.length > 2) {
                motivo = partes[2];
            }
            GrupoIva grupo = new GrupoIva(nombre, porcentaje, motivo);
            grupo.setBase(round2(entrada.getValue().multiply(factor)));
            grupo.setBaseBruta(round2(entrada.getValue()));
            grupos.add(grupo);
        }
        return grupos;
    }

    /** Ajustamos los céntimos en la mayor base para que las bases sumen el total. */
    private static void ajustarCentimos(List<GrupoIva> grupos, BigDecimal baseTotalDescontada) {
        BigDecimal suma = BigDecimal.ZERO;
        for (GrupoIva grupo : grupos) {
            suma = suma.add(grupo.getBase());
        }
        BigDecimal diferencia = baseTotalDescontada.subtract(suma);
        if (diferencia.compareTo(BigDecimal.ZERO) == 0 || grupos.isEmpty()) {
            return;
        }
        int indice = 0;
        BigDecimal mayor = grupos.get(0).getBase();
        for (int i = 1; i < grupos.size(); i++) {
            if (grupos.get(i).getBase().compareTo(mayor) > 0) {
                mayor = grupos.get(i).getBase();
                indice = i;
            }
        }
        grupos.get(indice).setBase(round2(grupos.get(indice).getBase().add(diferencia)));
    }

    /** Calculamos cada cuota sobre su base descontada y sumamos el IVA total. */
    private static BigDecimal sumarCuotas(List<GrupoIva> grupos) {
        BigDecimal ivaTotal = BigDecimal.ZERO;
        for (GrupoIva grupo : grupos) {
            BigDecimal cuota = ivaDeBase(grupo.getBase(), grupo.getPorcentaje());
            grupo.setCuota(cuota);
            ivaTotal = ivaTotal.add(cuota);
        }
        return round2(ivaTotal);
    }

    /** Guardamos la retención elegida y su importe sobre la base imponible. */
    private static void ponerRetencion(ResumenFactura resumen, TipoRetencion retencion, BigDecimal base) {
        BigDecimal importe = BigDecimal.ZERO;
        if (retencion != null && retencion.getPorcentaje() != null) {
            importe = round2(base.multiply(BigDecimal.valueOf(retencion.getPorcentaje()))
                    .divide(CIEN, PRECISION_INTERNA, RoundingMode.HALF_UP));
            resumen.setTipoRetencionId(retencion.getId());
            resumen.setNombreRetencion(retencion.getNombre());
            resumen.setPorcentajeRetencion(retencion.getPorcentaje());
        }
        resumen.setImporteRetencion(importe);
    }

    /** La clave de un grupo de IVA: nombre, porcentaje y motivo, separados por barras. */
    private static String claveIva(LineaFactura linea) {
        String nombre = linea.getIvaNombre();
        if (nombre == null) {
            nombre = "";
        }
        String porcentaje = "";
        if (linea.getIvaPorcentaje() != null) {
            porcentaje = String.valueOf(linea.getIvaPorcentaje());
        }
        String motivo = linea.getIvaMotivoExencion();
        if (motivo == null) {
            motivo = "";
        }
        return nombre + "|" + porcentaje + "|" + motivo;
    }
}
