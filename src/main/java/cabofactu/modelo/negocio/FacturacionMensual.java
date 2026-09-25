package cabofactu.modelo.negocio;

import cabofactu.modelo.dominio.Cliente;
import cabofactu.modelo.dominio.Factura;
import cabofactu.modelo.dominio.LineaFactura;
import cabofactu.modelo.dominio.Serie;
import cabofactu.modelo.dominio.TipoIva;
import cabofactu.modelo.dominio.TipoRetencion;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Generamos las facturas iguales de cada mes: una Factura por mes, todas o ninguna. */
public class FacturacionMensual {

    private final Facturas facturas;

    public FacturacionMensual(Facturas facturas) {
        this.facturas = facturas;
    }

    public enum ModoDia {
        FIJO,
        PRIMER_DIA,
        ULTIMO_DIA
    }

    /** Generamos las facturas de esos meses con día fijo, sin duplicados ni huecos. */
    public Resultado generar(Cliente cliente, int anio, int mesInicio, int mesFin, Serie serie, int diaMes,
                             TipoIva iva, TipoRetencion retencion, List<LineaPlantilla> plantillas)
            throws Exception {
        return generar(cliente, anio, mesInicio, mesFin, serie, ModoDia.FIJO, diaMes,
                iva, retencion, plantillas, false, false);
    }

    /** Generamos las facturas de esos meses: las duplicadas se omiten salvo que se pidan. */
    public Resultado generar(Cliente cliente, int anio, int mesInicio, int mesFin, Serie serie,
                             ModoDia diaMode, int diaFijo, TipoIva iva, TipoRetencion retencion,
                             List<LineaPlantilla> plantillas, boolean generarDuplicados, boolean usarHuecos)
            throws Exception {
        validar(cliente, serie, iva, plantillas);

        List<Integer> mesesAGenerar = new ArrayList<>();
        List<String> omitidos = new ArrayList<>();
        for (int mes = mesInicio; mes <= mesFin; mes++) {
            if (!generarDuplicados && facturas.clienteTieneFacturaEnMes(cliente.getId(), anio, mes)) {
                omitidos.add(nombreMes(mes));
                continue;
            }
            mesesAGenerar.add(mes);
        }

        List<Integer> numeros = Series.getSeries().proponerNumeros(serie, anio, mesesAGenerar.size(), usarHuecos);
        List<Factura> facturasAGenerar = facturasParaMeses(anio, mesesAGenerar, numeros, serie, cliente,
                diaMode, diaFijo, iva, retencion, plantillas);
        facturas.altaVarias(facturasAGenerar);
        return new Resultado(mesesAGenerar.size(), omitidos);
    }

    /** Construimos una Factura por mes, con su fecha, su número pedido y su retención. */
    private List<Factura> facturasParaMeses(int anio, List<Integer> meses, List<Integer> numeros, Serie serie,
                                           Cliente cliente, ModoDia diaMode, int diaFijo, TipoIva iva,
                                           TipoRetencion retencion, List<LineaPlantilla> plantillas) throws Exception {
        List<Factura> facturasAGenerar = new ArrayList<>();
        for (int i = 0; i < meses.size(); i++) {
            int mes = meses.get(i);
            LocalDate fecha = fechaDelMes(anio, mes, diaMode, diaFijo);
            Factura factura = new Factura(serie, fecha, new Cliente(cliente));
            factura.setId(null);
            factura.setCorrelativo(numeros.get(i));
            factura.setDescuento(0);
            factura.setObservaciones("");
            factura.setLineas(lineasParaMes(plantillas, mes, iva));
            if (retencion == null) {
                factura.setRetencion(null);
            } else {
                factura.setRetencion(new TipoRetencion(retencion));
            }
            facturasAGenerar.add(factura);
        }
        return facturasAGenerar;
    }

    /** Decimos qué meses de ese rango ya tienen factura de ese cliente. */
    public List<String> detectarDuplicados(Cliente cliente, int anio, int mesInicio, int mesFin)
            throws Exception {
        List<String> duplicados = new ArrayList<>();
        for (int mes = mesInicio; mes <= mesFin; mes++) {
            if (facturas.clienteTieneFacturaEnMes(cliente.getId(), anio, mes)) {
                duplicados.add(nombreMes(mes));
            }
        }
        return duplicados;
    }

    private void validar(Cliente cliente, Serie serie, TipoIva iva, List<LineaPlantilla> plantillas)
            throws Exception {
        if (cliente == null || cliente.getId() == null) {
            throw new Exception("Seleccione un cliente existente.");
        }
        if (cliente == null) {
            throw new Exception("Indique los datos del cliente.");
        }
        if (serie == null || serie.getId() == null) {
            throw new Exception("Seleccione una serie de numeración.");
        }
        if (iva == null || iva.getId() == null) {
            throw new Exception("Seleccione un tipo de IVA.");
        }
        if (plantillas == null || plantillas.isEmpty()) {
            throw new Exception("Añada al menos una línea de concepto.");
        }
        for (LineaPlantilla p : plantillas) {
            if (p.cantidad < 1) {
                throw new Exception("La cantidad de cada línea debe ser al menos 1.");
            }
            if (p.precioUnitario == null || p.precioUnitario.signum() < 0) {
                throw new Exception("Los precios unitarios no pueden ser negativos.");
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

    private List<LineaFactura> lineasParaMes(List<LineaPlantilla> plantillas, int mes, TipoIva iva) throws Exception {
        String nombreMes = nombreMes(mes);
        List<LineaFactura> lineas = new ArrayList<>();
        int orden = 1;
        for (LineaPlantilla p : plantillas) {
            LineaFactura l = new LineaFactura(p.cantidad, p.precioUnitario);
            l.setOrden(orden++);
            String desc = p.descripcion;
            if (desc == null) {
                desc = "";
            }
            if (p.anadirMes) {
                desc = desc + " - mes de " + nombreMes;
            }
            l.setDescripcion(desc);
            l.setTipoIva(iva);
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
