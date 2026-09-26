package cabofactu.modelo.dominio;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Filtros combinables del histórico. Todos son optativos, y las fechas y los
 * importes se comprueban entre sí para que la fecha o el importe desde no
 * queden por detrás del hasta.
 */
public class FiltrosHistorial {

    private Serie serie;
    private String clienteTexto;
    private LocalDate fechaDesde;
    private LocalDate fechaHasta;
    private BigDecimal importeDesde;
    private BigDecimal importeHasta;
    private EstadoFactura estado;

    public FiltrosHistorial(Serie serie, String clienteTexto, LocalDate fechaDesde, LocalDate fechaHasta,
                             BigDecimal importeDesde, BigDecimal importeHasta, EstadoFactura estado) throws Exception {
        setSerie(serie);
        setClienteTexto(clienteTexto);
        setFechaDesde(fechaDesde);
        setFechaHasta(fechaHasta);
        setImporteDesde(importeDesde);
        setImporteHasta(importeHasta);
        setEstado(estado);
    }

    public Serie getSerie() {
        return serie;
    }

    public void setSerie(Serie serie) {
        this.serie = serie;
    }

    public String getClienteTexto() {
        return clienteTexto;
    }

    public void setClienteTexto(String clienteTexto) {
        if (clienteTexto == null) {
            this.clienteTexto = "";
        } else {
            this.clienteTexto = clienteTexto.trim();
        }
    }

    public LocalDate getFechaDesde() {
        return fechaDesde;
    }

    public void setFechaDesde(LocalDate fechaDesde) throws Exception {
        String error = errorFechas(fechaDesde, this.fechaHasta);
        if (error != null) {
            throw new Exception(error);
        }
        this.fechaDesde = fechaDesde;
    }

    public LocalDate getFechaHasta() {
        return fechaHasta;
    }

    public void setFechaHasta(LocalDate fechaHasta) throws Exception {
        String error = errorFechas(this.fechaDesde, fechaHasta);
        if (error != null) {
            throw new Exception(error);
        }
        this.fechaHasta = fechaHasta;
    }

    public BigDecimal getImporteDesde() {
        return importeDesde;
    }

    public void setImporteDesde(BigDecimal importeDesde) throws Exception {
        String error = errorImportes(importeDesde, this.importeHasta);
        if (error != null) {
            throw new Exception(error);
        }
        this.importeDesde = importeDesde;
    }

    public BigDecimal getImporteHasta() {
        return importeHasta;
    }

    public void setImporteHasta(BigDecimal importeHasta) throws Exception {
        String error = errorImportes(this.importeDesde, importeHasta);
        if (error != null) {
            throw new Exception(error);
        }
        this.importeHasta = importeHasta;
    }

    public EstadoFactura getEstado() {
        return estado;
    }

    public void setEstado(EstadoFactura estado) {
        this.estado = estado;
    }

    /** El error si la fecha desde es posterior a la fecha hasta, o null si está bien o falta una de las dos. */
    public static String errorFechas(LocalDate desde, LocalDate hasta) {
        if (desde != null && hasta != null && hasta.isBefore(desde)) {
            return "La fecha desde es posterior a la fecha hasta.";
        }
        return null;
    }

    /** El error si el importe desde es mayor que el importe hasta, o null si está bien o falta uno de los dos. */
    public static String errorImportes(BigDecimal desde, BigDecimal hasta) {
        if (desde != null && hasta != null && desde.compareTo(hasta) > 0) {
            return "El importe desde es mayor que el importe hasta.";
        }
        return null;
    }
}
