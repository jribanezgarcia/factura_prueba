package com.alcazaba.facturacion.service;

import com.alcazaba.facturacion.model.LineaFactura;
import com.alcazaba.facturacion.model.ResumenFactura;
import com.alcazaba.facturacion.model.TipoRetencion;

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
public final class CalculoService {

    public static final int SCALE = 2;
    private static final BigDecimal CIEN = new BigDecimal("100");
    private static final int PRECISION_INTERNA = 6;

    private CalculoService() {
    }

    public static BigDecimal round2(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v.setScale(SCALE, RoundingMode.HALF_UP);
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
        BigDecimal base = l.getTotalBase() == null ? BigDecimal.ZERO : l.getTotalBase();
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
        if (lineas == null) {
            return List.of();
        }
        return lineas.stream().filter(LineaFactura::isEsSuplido).toList();
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

    /**
     * Entrada de total final con IVA incluido. Calcula hacia atras base e IVA.
     */
    public static ResultadoConIva calcularDesdeTotalConIva(BigDecimal totalConIva, Integer porcentaje) {
        if (totalConIva == null) {
            totalConIva = BigDecimal.ZERO;
        }
        BigDecimal base;
        if (porcentaje == null) {
            base = round2(totalConIva);
        } else {
            BigDecimal factor = CIEN.add(BigDecimal.valueOf(porcentaje)).divide(CIEN, PRECISION_INTERNA, RoundingMode.HALF_UP);
            base = totalConIva.divide(factor, SCALE, RoundingMode.HALF_UP);
        }
        BigDecimal iva = totalConIva.subtract(base);
        return new ResultadoConIva(base, iva);
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
        BigDecimal factor = CIEN.subtract(BigDecimal.valueOf(descuento)).divide(CIEN, PRECISION_INTERNA, RoundingMode.HALF_UP);

        Map<ClaveIva, BigDecimal> bases = new LinkedHashMap<>();
        Map<ClaveIva, BigDecimal> cuotasSinDescuento = new LinkedHashMap<>();
        BigDecimal baseTotalSinDescuento = BigDecimal.ZERO;
        BigDecimal totalSuplidos = BigDecimal.ZERO;

        if (lineas != null) {
            for (LineaFactura l : lineas) {
                if (l.isEsSuplido()) {
                    totalSuplidos = totalSuplidos.add(nz(l.getTotalBase()));
                    continue;
                }
                ClaveIva clave = new ClaveIva(l.getIvaNombre(), l.getIvaPorcentaje(), l.getIvaMotivoExencion());
                bases.merge(clave, nz(l.getTotalBase()), BigDecimal::add);
                cuotasSinDescuento.merge(clave, nz(l.getIvaImporte()), BigDecimal::add);
                baseTotalSinDescuento = baseTotalSinDescuento.add(nz(l.getTotalBase()));
            }
        }

        BigDecimal baseTotalDescontada = round2(baseTotalSinDescuento.multiply(factor));

        ResumenFactura resumen = new ResumenFactura();
        resumen.setDescuentoPorcentaje(descuento);
        resumen.setBaseTotal(baseTotalDescontada);
        resumen.setBaseBruta(round2(baseTotalSinDescuento));
        resumen.setImporteDescuento(round2(baseTotalSinDescuento.subtract(baseTotalDescontada)));

        List<ResumenFactura.IvaGrupo> grupos = new ArrayList<>();
        List<BigDecimal> basesDescontadas = new ArrayList<>();

        for (Map.Entry<ClaveIva, BigDecimal> e : bases.entrySet()) {
            ClaveIva clave = e.getKey();
            BigDecimal baseDescontada = round2(e.getValue().multiply(factor));
            basesDescontadas.add(baseDescontada);

            ResumenFactura.IvaGrupo g = new ResumenFactura.IvaGrupo();
            g.setNombre(clave.nombre);
            g.setPorcentaje(clave.porcentaje);
            g.setMotivoExencion(clave.motivo);
            g.setBase(baseDescontada);
            g.setBaseBruta(round2(e.getValue()));
            grupos.add(g);
        }

        // Ajuste de centimos: la suma de bases descontadas debe cuadrar con el total base descontado.
        BigDecimal suma = basesDescontadas.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal diferencia = baseTotalDescontada.subtract(suma);
        if (diferencia.compareTo(BigDecimal.ZERO) != 0 && !grupos.isEmpty()) {
            int idx = indiceMayorBase(basesDescontadas);
            grupos.get(idx).setBase(round2(grupos.get(idx).getBase().add(diferencia)));
        }

        BigDecimal ivaTotal = BigDecimal.ZERO;
        for (ResumenFactura.IvaGrupo g : grupos) {
            BigDecimal cuota = ivaDeBase(g.getBase(), g.getPorcentaje());
            g.setCuota(cuota);
            ivaTotal = ivaTotal.add(cuota);
        }
        ivaTotal = round2(ivaTotal);

        BigDecimal importeRetencion = BigDecimal.ZERO;
        if (retencion != null && retencion.getPorcentaje() != null) {
            importeRetencion = round2(baseTotalDescontada.multiply(BigDecimal.valueOf(retencion.getPorcentaje())).divide(CIEN, PRECISION_INTERNA, RoundingMode.HALF_UP));
            resumen.setTipoRetencionId(retencion.getId());
            resumen.setNombreRetencion(retencion.getNombre());
            resumen.setPorcentajeRetencion(retencion.getPorcentaje());
        }
        resumen.setImporteRetencion(importeRetencion);
        resumen.setIvaTotal(ivaTotal);
        resumen.setTotalSuplidos(round2(totalSuplidos));
        resumen.setTotal(round2(baseTotalDescontada.add(ivaTotal).subtract(importeRetencion).add(resumen.getTotalSuplidos())));
        resumen.getGrupos().addAll(grupos);
        return resumen;
    }

    private static int indiceMayorBase(List<BigDecimal> bases) {
        int idx = 0;
        BigDecimal max = bases.isEmpty() ? BigDecimal.ZERO : bases.get(0);
        for (int i = 1; i < bases.size(); i++) {
            if (bases.get(i).compareTo(max) > 0) {
                max = bases.get(i);
                idx = i;
            }
        }
        return idx;
    }

    private static BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    public record ResultadoConIva(BigDecimal base, BigDecimal iva) {
    }

    private record ClaveIva(String nombre, Integer porcentaje, String motivo) {
    }
}
