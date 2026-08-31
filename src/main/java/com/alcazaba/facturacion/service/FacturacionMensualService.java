package com.alcazaba.facturacion.service;

import com.alcazaba.facturacion.db.Database;
import com.alcazaba.facturacion.model.Cliente;
import com.alcazaba.facturacion.model.LineaFactura;
import com.alcazaba.facturacion.model.Serie;
import com.alcazaba.facturacion.model.TipoIva;
import com.alcazaba.facturacion.model.TipoRetencion;
import com.alcazaba.facturacion.repository.FacturaRepository;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FacturacionMensualService {

    private final FacturaService facturaService;
    private final FacturaRepository facturaRepository;
    private final NumeroService numeroService;

    public FacturacionMensualService(FacturaService facturaService, FacturaRepository facturaRepository,
                                     NumeroService numeroService) {
        this.facturaService = facturaService;
        this.facturaRepository = facturaRepository;
        this.numeroService = numeroService;
    }

    public enum DiaMode {
        FIJO,
        PRIMER_DIA,
        ULTIMO_DIA
    }

    public Resultado generar(Cliente cliente, int anio, int mesInicio, int mesFin, Serie serie, int diaMes,
                             TipoIva iva, TipoRetencion retencion, List<LineaPlantilla> plantillas)
            throws SQLException, ValidationException {
        return generar(cliente, anio, mesInicio, mesFin, serie, DiaMode.FIJO, diaMes,
                iva, retencion, plantillas, false, false);
    }

    public Resultado generar(Cliente cliente, int anio, int mesInicio, int mesFin, Serie serie,
                             DiaMode diaMode, int diaFijo, TipoIva iva, TipoRetencion retencion,
                             List<LineaPlantilla> plantillas, boolean generarDuplicados, boolean usarHuecos)
            throws SQLException, ValidationException {
        validar(cliente, serie, iva, plantillas);

        List<Integer> mesesAGenerar = new ArrayList<>();
        List<String> omitidos = new ArrayList<>();
        for (int mes = mesInicio; mes <= mesFin; mes++) {
            if (!generarDuplicados && facturaRepository.clienteTieneFacturaEnMes(cliente.getId(), anio, mes)) {
                omitidos.add(nombreMes(mes));
                continue;
            }
            mesesAGenerar.add(mes);
        }

        List<Integer> numeros = numeroService.proponerNumeros(serie, anio, mesesAGenerar.size(), usarHuecos);

        Database.beginTransaction();
        try {
            int generadas = 0;
            for (int i = 0; i < mesesAGenerar.size(); i++) {
                int mes = mesesAGenerar.get(i);
                LocalDate fecha = fechaDelMes(anio, mes, diaMode, diaFijo);
                List<LineaFactura> lineas = lineasParaMes(plantillas, mes, iva);
                facturaService.crearFacturaSinTransaccion(serie, fecha, cliente, lineas, 0, "", "",
                        numeros.get(i), null, retencion);
                generadas++;
            }
            Database.commit();
            return new Resultado(generadas, omitidos);
        } catch (SQLException | ValidationException | RuntimeException e) {
            Database.rollback();
            throw e;
        } finally {
            Database.endTransaction();
        }
    }

    public List<String> detectarDuplicados(Cliente cliente, int anio, int mesInicio, int mesFin)
            throws SQLException {
        List<String> duplicados = new ArrayList<>();
        for (int mes = mesInicio; mes <= mesFin; mes++) {
            if (facturaRepository.clienteTieneFacturaEnMes(cliente.getId(), anio, mes)) {
                duplicados.add(nombreMes(mes));
            }
        }
        return duplicados;
    }

    private void validar(Cliente cliente, Serie serie, TipoIva iva, List<LineaPlantilla> plantillas)
            throws ValidationException {
        if (cliente == null || cliente.getId() == null) {
            throw new ValidationException("Seleccione un cliente existente.");
        }
        if (serie == null || serie.getId() == null) {
            throw new ValidationException("Seleccione una serie de numeración.");
        }
        if (iva == null || iva.getId() == null) {
            throw new ValidationException("Seleccione un tipo de IVA.");
        }
        if (plantillas == null || plantillas.isEmpty()) {
            throw new ValidationException("Añada al menos una línea de concepto.");
        }
        for (LineaPlantilla p : plantillas) {
            if (p.cantidad < 1) {
                throw new ValidationException("La cantidad de cada línea debe ser al menos 1.");
            }
            if (p.precioUnitario == null || p.precioUnitario.signum() < 0) {
                throw new ValidationException("Los precios unitarios no pueden ser negativos.");
            }
        }
    }

    private LocalDate fechaDelMes(int anio, int mes, DiaMode mode, int diaFijo) {
        YearMonth ym = YearMonth.of(anio, mes);
        int dia = switch (mode) {
            case PRIMER_DIA -> 1;
            case ULTIMO_DIA -> ym.lengthOfMonth();
            case FIJO -> Math.max(1, Math.min(diaFijo, ym.lengthOfMonth()));
        };
        return LocalDate.of(anio, mes, dia);
    }

    private List<LineaFactura> lineasParaMes(List<LineaPlantilla> plantillas, int mes, TipoIva iva) {
        String nombreMes = nombreMes(mes);
        List<LineaFactura> lineas = new ArrayList<>();
        int orden = 1;
        for (LineaPlantilla p : plantillas) {
            LineaFactura l = new LineaFactura();
            l.setOrden(orden++);
            l.setCantidad(p.cantidad);
            String desc = p.descripcion == null ? "" : p.descripcion;
            if (p.anadirMes) {
                desc = desc + " - mes de " + nombreMes;
            }
            l.setDescripcion(desc);
            l.setPrecioUnitario(p.precioUnitario);
            l.setTotalBase(CalculoService.totalLinea(p.precioUnitario, p.cantidad));
            l.setTipoIvaId(iva.getId());
            l.setIvaNombre(iva.getNombre());
            l.setIvaPorcentaje(iva.getPorcentaje());
            l.setIvaMotivoExencion(iva.getMotivoExencion());
            l.setIvaImporte(CalculoService.ivaDeBase(l.getTotalBase(), iva.getPorcentaje()));
            lineas.add(l);
        }
        return lineas;
    }

    private String nombreMes(int mes) {
        return Month.of(mes).getDisplayName(TextStyle.FULL, new Locale("es", "ES"));
    }

    public static class LineaPlantilla {
        private int cantidad;
        private String descripcion;
        private BigDecimal precioUnitario;
        private boolean anadirMes;

        public LineaPlantilla() {
        }

        public LineaPlantilla(int cantidad, String descripcion, BigDecimal precioUnitario, boolean anadirMes) {
            this.cantidad = cantidad;
            this.descripcion = descripcion;
            this.precioUnitario = precioUnitario;
            this.anadirMes = anadirMes;
        }

        public int getCantidad() {
            return cantidad;
        }

        public void setCantidad(int cantidad) {
            this.cantidad = cantidad;
        }

        public String getDescripcion() {
            return descripcion;
        }

        public void setDescripcion(String descripcion) {
            this.descripcion = descripcion;
        }

        public BigDecimal getPrecioUnitario() {
            return precioUnitario;
        }

        public void setPrecioUnitario(BigDecimal precioUnitario) {
            this.precioUnitario = precioUnitario;
        }

        public boolean isAnadirMes() {
            return anadirMes;
        }

        public void setAnadirMes(boolean anadirMes) {
            this.anadirMes = anadirMes;
        }
    }

    public static class Resultado {
        private final int generadas;
        private final List<String> mesesOmitidos;

        public Resultado(int generadas, List<String> mesesOmitidos) {
            this.generadas = generadas;
            this.mesesOmitidos = mesesOmitidos;
        }

        public int getGeneradas() {
            return generadas;
        }

        public List<String> getMesesOmitidos() {
            return mesesOmitidos;
        }
    }
}
