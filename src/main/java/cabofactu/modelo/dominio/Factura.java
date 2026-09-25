package cabofactu.modelo.dominio;

import cabofactu.utilidades.Formatos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Una factura entera: su número, su fecha, la copia de su cliente, sus líneas,
 * su retención y sus totales. Los setters comprueban lo que reciben, así que
 * una Factura que existe es siempre una Factura válida.
 */
public class Factura {

    private Long id;
    private Serie serie;
    private int correlativo;
    private String numero;
    private LocalDate fecha;
    private EstadoFactura estado;
    private Cliente cliente;
    private int descuento;
    private String observaciones;
    private Long rectificaId;
    private String rectificaNumero;
    private String formaPago;
    private LocalDate vencimiento;
    private String realizadaPor;
    private TipoRetencion retencion;
    private List<LineaFactura> lineas;
    private BigDecimal baseTotal = BigDecimal.ZERO;
    private BigDecimal ivaTotal = BigDecimal.ZERO;
    private BigDecimal importeRetencion = BigDecimal.ZERO;
    private BigDecimal totalSuplidos = BigDecimal.ZERO;
    private BigDecimal total = BigDecimal.ZERO;

    public Factura(Serie serie, LocalDate fecha, Cliente cliente) throws Exception {
        setSerie(serie);
        setFecha(fecha);
        setCliente(cliente);
        setCorrelativo(0);
        setNumero("");
        setEstado(EstadoFactura.EMITIDA);
        setDescuento(0);
        setObservaciones("");
        setRectificaId(null);
        setRectificaNumero("");
        setFormaPago("");
        setVencimiento(null);
        setRealizadaPor("");
        setRetencion(null);
        setLineas(new ArrayList<>());
    }

    /** Copiamos una factura entera, con sus objetos dentro, para poder editarla sin tocar la original. */
    public Factura(Factura otra) throws Exception {
        this(new Serie(otra.getSerie()), otra.getFecha(), new Cliente(otra.getCliente()));
        setId(otra.getId());
        setCorrelativo(otra.getCorrelativo());
        setNumero(otra.getNumero());
        setEstado(otra.getEstado());
        setDescuento(otra.getDescuento());
        setObservaciones(otra.getObservaciones());
        setRectificaId(otra.getRectificaId());
        setRectificaNumero(otra.getRectificaNumero());
        setFormaPago(otra.getFormaPago());
        setVencimiento(otra.getVencimiento());
        setRealizadaPor(otra.getRealizadaPor());
        if (otra.getRetencion() == null) {
            setRetencion(null);
        } else {
            setRetencion(new TipoRetencion(otra.getRetencion()));
        }
        List<LineaFactura> lineas = new ArrayList<>();
        if (otra.getLineas() != null) {
            for (LineaFactura linea : otra.getLineas()) {
                lineas.add(new LineaFactura(linea));
            }
        }
        setLineas(lineas);
        setBaseTotal(otra.getBaseTotal());
        setIvaTotal(otra.getIvaTotal());
        setImporteRetencion(otra.getImporteRetencion());
        setTotalSuplidos(otra.getTotalSuplidos());
        setTotal(otra.getTotal());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Serie getSerie() {
        return serie;
    }

    public void setSerie(Serie serie) throws Exception {
        if (serie == null) {
            throw new Exception("Seleccione la serie.");
        }
        this.serie = serie;
    }

    public int getCorrelativo() {
        return correlativo;
    }

    public void setCorrelativo(int correlativo) throws Exception {
        if (correlativo < 0) {
            throw new Exception("El correlativo no puede ser negativo.");
        }
        this.correlativo = correlativo;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = textoOpcional(numero);
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) throws Exception {
        if (fecha == null) {
            throw new Exception("Indique la fecha de la factura.");
        }
        this.fecha = fecha;
    }

    public EstadoFactura getEstado() {
        return estado;
    }

    public void setEstado(EstadoFactura estado) throws Exception {
        if (estado == null) {
            throw new Exception("Indique el estado de la factura.");
        }
        this.estado = estado;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) throws Exception {
        if (cliente == null) {
            throw new Exception("Indique el cliente de la factura.");
        }
        this.cliente = cliente;
    }

    public int getDescuento() {
        return descuento;
    }

    public void setDescuento(int descuento) throws Exception {
        if (descuento < 0 || descuento > 100) {
            throw new Exception("El descuento debe estar entre 0 y 100.");
        }
        this.descuento = descuento;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = textoOpcional(observaciones);
    }

    public Long getRectificaId() {
        return rectificaId;
    }

    public void setRectificaId(Long rectificaId) {
        this.rectificaId = rectificaId;
    }

    public String getRectificaNumero() {
        return rectificaNumero;
    }

    public void setRectificaNumero(String rectificaNumero) {
        this.rectificaNumero = textoOpcional(rectificaNumero);
    }

    public String getFormaPago() {
        return formaPago;
    }

    public void setFormaPago(String formaPago) {
        this.formaPago = textoOpcional(formaPago);
    }

    public LocalDate getVencimiento() {
        return vencimiento;
    }

    public void setVencimiento(LocalDate vencimiento) {
        this.vencimiento = vencimiento;
    }

    public String getRealizadaPor() {
        return realizadaPor;
    }

    public void setRealizadaPor(String realizadaPor) {
        this.realizadaPor = textoOpcional(realizadaPor);
    }

    public TipoRetencion getRetencion() {
        return retencion;
    }

    public void setRetencion(TipoRetencion retencion) {
        this.retencion = retencion;
    }

    public List<LineaFactura> getLineas() {
        return lineas;
    }

    public void setLineas(List<LineaFactura> lineas) {
        if (lineas == null) {
            this.lineas = new ArrayList<>();
        } else {
            this.lineas = lineas;
        }
    }

    public BigDecimal getBaseTotal() {
        return baseTotal;
    }

    public void setBaseTotal(BigDecimal baseTotal) {
        this.baseTotal = importeOpcional(baseTotal);
    }

    public BigDecimal getIvaTotal() {
        return ivaTotal;
    }

    public void setIvaTotal(BigDecimal ivaTotal) {
        this.ivaTotal = importeOpcional(ivaTotal);
    }

    public BigDecimal getImporteRetencion() {
        return importeRetencion;
    }

    public void setImporteRetencion(BigDecimal importeRetencion) {
        this.importeRetencion = importeOpcional(importeRetencion);
    }

    public BigDecimal getTotalSuplidos() {
        return totalSuplidos;
    }

    public void setTotalSuplidos(BigDecimal totalSuplidos) {
        this.totalSuplidos = importeOpcional(totalSuplidos);
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = importeOpcional(total);
    }

    @Override
    public boolean equals(Object otro) {
        if (this == otro) {
            return true;
        }
        if (otro == null || getClass() != otro.getClass()) {
            return false;
        }
        Factura factura = (Factura) otro;
        if (id == null) {
            return factura.id == null;
        }
        return id.equals(factura.id);
    }

    @Override
    public int hashCode() {
        if (id == null) {
            return 0;
        }
        return id.hashCode();
    }

    @Override
    public String toString() {
        return numero;
    }

    /** El año sale siempre de la fecha: no lo guardamos como un campo aparte. */
    public int getAnio() {
        return fecha.getYear();
    }

    /** La fecha en formato español, para la columna Fecha del histórico. */
    public String getFechaTexto() {
        return Formatos.fecha(fecha);
    }

    /** El nombre de la copia del cliente, para la columna Cliente del histórico. */
    public String getClienteNombre() {
        if (cliente == null) {
            return "";
        }
        return cliente.getNombre();
    }

    /** El NIF de la copia del cliente, para la columna NIF del histórico. */
    public String getClienteNif() {
        if (cliente == null) {
            return "";
        }
        return cliente.getNif();
    }

    /** La base imponible en formato español, para la columna Base del histórico. */
    public String getBaseTexto() {
        return Formatos.moneda(baseTotal);
    }

    /** El IVA total en formato español, para la columna IVA del histórico. */
    public String getIvaTexto() {
        return Formatos.moneda(ivaTotal);
    }

    /** El importe de la retención en formato español, para la columna Retención del histórico. */
    public String getRetencionTexto() {
        return Formatos.moneda(importeRetencion);
    }

    /** El total en formato español, para la columna Total del histórico. */
    public String getTotalTexto() {
        return Formatos.moneda(total);
    }

    /** «Emitida» o «Anulada», para la columna Estado del histórico. */
    public String getEstadoTexto() {
        if (estado == null) {
            return "";
        }
        return estado.label();
    }

    private String textoOpcional(String valor) {
        if (valor == null || valor.isBlank()) {
            return "";
        }
        return valor.trim();
    }

    private BigDecimal importeOpcional(BigDecimal valor) {
        if (valor == null) {
            return BigDecimal.ZERO;
        }
        return valor;
    }
}
