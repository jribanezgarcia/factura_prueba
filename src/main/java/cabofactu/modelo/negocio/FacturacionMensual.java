package cabofactu.modelo.negocio;

import cabofactu.modelo.negocio.sqlite.Conexion;
import cabofactu.modelo.dominio.Cliente;
import cabofactu.modelo.dominio.LineaFactura;
import cabofactu.modelo.dominio.Serie;
import cabofactu.modelo.dominio.TipoIva;
import cabofactu.modelo.dominio.TipoRetencion;
import cabofactu.modelo.negocio.sqlite.FacturaDAO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FacturacionMensual {

    private final Facturas facturas;
    private final FacturaDAO facturaDAO;

    public FacturacionMensual(Facturas facturas, FacturaDAO facturaDAO) {
        this.facturas = facturas;
        this.facturaDAO = facturaDAO;
    }

    public enum ModoDia {
        FIJO,
        PRIMER_DIA,
        ULTIMO_DIA
    }

    public Resultado generar(Cliente cliente, int anio, int mesInicio, int mesFin, Serie serie, int diaMes,
                             TipoIva iva, TipoRetencion retencion, List<LineaPlantilla> plantillas)
            throws Exception {
        return generar(cliente, anio, mesInicio, mesFin, serie, ModoDia.FIJO, diaMes,
                iva, retencion, plantillas, false, false);
    }

    public Resultado generar(Cliente cliente, int anio, int mesInicio, int mesFin, Serie serie,
                             ModoDia diaMode, int diaFijo, TipoIva iva, TipoRetencion retencion,
                             List<LineaPlantilla> plantillas, boolean generarDuplicados, boolean usarHuecos)
            throws Exception {
        validar(cliente, serie, iva, plantillas);

        List<Integer> mesesAGenerar = new ArrayList<>();
        List<String> omitidos = new ArrayList<>();
        for (int mes = mesInicio; mes <= mesFin; mes++) {
            if (!generarDuplicados && facturaDAO.clienteTieneFacturaEnMes(cliente.getId(), anio, mes)) {
                omitidos.add(nombreMes(mes));
                continue;
            }
            mesesAGenerar.add(mes);
        }

        List<Integer> numeros = Series.getSeries().proponerNumeros(serie, anio, mesesAGenerar.size(), usarHuecos);

        Conexion.iniciarTransaccion();
        try {
            int generadas = 0;
            for (int i = 0; i < mesesAGenerar.size(); i++) {
                int mes = mesesAGenerar.get(i);
                LocalDate fecha = fechaDelMes(anio, mes, diaMode, diaFijo);
                List<LineaFactura> lineas = lineasParaMes(plantillas, mes, iva);
                facturas.crearFacturaSinTransaccion(serie, fecha, cliente, lineas, 0, "", "",
                        numeros.get(i), null, retencion);
                generadas++;
            }
            Conexion.confirmar();
            return new Resultado(generadas, omitidos);
        } catch (Exception e) {
            Conexion.deshacer();
            throw e;
        } finally {
            Conexion.terminarTransaccion();
        }
    }

    public List<String> detectarDuplicados(Cliente cliente, int anio, int mesInicio, int mesFin)
            {
        List<String> duplicados = new ArrayList<>();
        for (int mes = mesInicio; mes <= mesFin; mes++) {
            if (facturaDAO.clienteTieneFacturaEnMes(cliente.getId(), anio, mes)) {
                duplicados.add(nombreMes(mes));
            }
        }
        return duplicados;
    }

    private void validar(Cliente cliente, Serie serie, TipoIva iva, List<LineaPlantilla> plantillas)
            throws ValidacionException {
        if (cliente == null || cliente.getId() == null) {
            throw new ValidacionException("Seleccione un cliente existente.");
        }
        ValidacionCliente.comprobar(cliente);
        if (serie == null || serie.getId() == null) {
            throw new ValidacionException("Seleccione una serie de numeración.");
        }
        if (iva == null || iva.getId() == null) {
            throw new ValidacionException("Seleccione un tipo de IVA.");
        }
        if (plantillas == null || plantillas.isEmpty()) {
            throw new ValidacionException("Añada al menos una línea de concepto.");
        }
        for (LineaPlantilla p : plantillas) {
            if (p.cantidad < 1) {
                throw new ValidacionException("La cantidad de cada línea debe ser al menos 1.");
            }
            if (p.precioUnitario == null || p.precioUnitario.signum() < 0) {
                throw new ValidacionException("Los precios unitarios no pueden ser negativos.");
            }
        }
    }

    private LocalDate fechaDelMes(int anio, int mes, ModoDia mode, int diaFijo) {
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
            l.setTotalBase(Calculos.totalLinea(p.precioUnitario, p.cantidad));
            l.setTipoIvaId(iva.getId());
            l.setIvaNombre(iva.getNombre());
            l.setIvaPorcentaje(iva.getPorcentaje());
            l.setIvaMotivoExencion(iva.getMotivoExencion());
            l.setIvaImporte(Calculos.ivaDeBase(l.getTotalBase(), iva.getPorcentaje()));
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
