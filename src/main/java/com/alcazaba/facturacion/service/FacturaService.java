package com.alcazaba.facturacion.service;

import com.alcazaba.facturacion.db.Database;
import com.alcazaba.facturacion.model.Cliente;
import com.alcazaba.facturacion.model.DatosPago;
import com.alcazaba.facturacion.model.EstadoFactura;
import com.alcazaba.facturacion.model.Factura;
import com.alcazaba.facturacion.model.FacturaVersion;
import com.alcazaba.facturacion.model.LineaFactura;
import com.alcazaba.facturacion.model.Serie;
import com.alcazaba.facturacion.repository.ClienteRepository;
import com.alcazaba.facturacion.repository.FacturaRepository;
import com.alcazaba.facturacion.repository.LineaRepository;
import com.alcazaba.facturacion.repository.SerieRepository;
import com.alcazaba.facturacion.repository.VersionRepository;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Orquestacion del guardado de facturas: transacciones, numeracion, cliente
 * maestro y versionado.
 */
public class FacturaService {

    private final FacturaRepository facturaRepository;
    private final SerieRepository serieRepository;
    private final ClienteRepository clienteRepository;
    private final VersionRepository versionRepository;
    private final LineaRepository lineaRepository;
    private final VersionadoService versionadoService;
    private final NumeroService numeroService;

    public FacturaService(FacturaRepository facturaRepository, SerieRepository serieRepository,
                          ClienteRepository clienteRepository, VersionRepository versionRepository,
                          LineaRepository lineaRepository, VersionadoService versionadoService,
                          NumeroService numeroService) {
        this.facturaRepository = facturaRepository;
        this.serieRepository = serieRepository;
        this.clienteRepository = clienteRepository;
        this.versionRepository = versionRepository;
        this.lineaRepository = lineaRepository;
        this.versionadoService = versionadoService;
        this.numeroService = numeroService;
    }

    /**
     * Crea una factura nueva: transaccion que inserta factura + version v1 +
     * lineas, consume el correlativo solo si todo sale bien.
     */
    public long crearFactura(Serie serie, LocalDate fecha, Cliente cliente, List<LineaFactura> lineas,
                             int descuento, String observaciones, String referencia)
            throws SQLException, ValidationException {
        return crearFactura(serie, fecha, cliente, lineas, descuento, observaciones, referencia, null, null);
    }

    /**
     * Igual que crearFactura pero acepta un correlativo escrito manualmente
     * (campo numero editable en creacion). Si llega null se propone el
     * siguiente correlativo de la serie.
     */
    public long crearFactura(Serie serie, LocalDate fecha, Cliente cliente, List<LineaFactura> lineas,
                             int descuento, String observaciones, String referencia, Integer correlativoPedido)
            throws SQLException, ValidationException {
        return crearFactura(serie, fecha, cliente, lineas, descuento, observaciones, referencia,
                correlativoPedido, null);
    }

    public long crearFactura(Serie serie, LocalDate fecha, Cliente cliente, List<LineaFactura> lineas,
                             int descuento, String observaciones, String referencia,
                             Integer correlativoPedido, DatosPago datosPago)
            throws SQLException, ValidationException {
        validar(lineas, descuento);
        if (correlativoPedido != null && correlativoPedido < 1) {
            throw new ValidationException("El correlativo debe ser al menos 1");
        }

        Database.beginTransaction();
        try {
            int correlativo = correlativoPedido != null
                    ? correlativoPedido
                    : numeroService.siguienteCorrelativo(serie, fecha);
            if (numeroService.correlativoOcupadoPorActiva(serie, correlativo, fecha)) {
                throw new ValidationException(
                        "El correlativo " + correlativo + " ya esta ocupado por una factura activa de la serie " + serie.getCodigo());
            }

            if (cliente != null && cliente.getId() == null && !isVacio(cliente)) {
                long clienteId = clienteRepository.insertar(cliente);
                cliente.setId(clienteId);
            }

            long facturaId = facturaRepository.insertar(serie.getId(), correlativo,
                    cliente == null ? null : cliente.getId());

            String numero = numeroService.formarNumero(serie, correlativo, fecha);
            versionadoService.crearVersion(facturaId, fecha, numero, EstadoFactura.EMITIDA,
                    descuento, observaciones, referencia, cliente, lineas, datosPago);

            serieRepository.actualizarSiguiente(serie.getId(), Math.max(serie.getSiguienteCorrelativo(), correlativo + 1));
            int nuevoAnio = Math.max(serieRepository.getSiguiente(serie.getId(), fecha.getYear()), correlativo + 1);
            serieRepository.actualizarSiguiente(serie.getId(), fecha.getYear(), nuevoAnio);
            Database.commit();
            return facturaId;
        } catch (SQLException | ValidationException | RuntimeException e) {
            Database.rollback();
            throw e;
        } finally {
            Database.endTransaction();
        }
    }

    public FacturaVersion guardarEditada(long facturaId, Long versionAbiertaId, LocalDate fecha, Cliente cliente,
                                         List<LineaFactura> lineas, int descuento,
                                         String observaciones, String referencia, DatosPago datosPago)
            throws SQLException, ValidationException {
        return guardarEditada(facturaId, versionAbiertaId, fecha, cliente, lineas, descuento,
                observaciones, referencia, datosPago, false);
    }

    /**
     * Guarda cambios sobre una factura existente. Si la version abierta es la
     * actual (ultima), se sobrescribe en su lugar; con comoNuevaVersion se crea
     * siempre una version nueva (vN+1) dejando la anterior intacta. Si se edito
     * una version anterior, se crea una nueva version con esos datos
     * (snapshot). El correlativo queda fijo; el mes del numero sigue a la fecha.
     */
    public FacturaVersion guardarEditada(long facturaId, Long versionAbiertaId, LocalDate fecha, Cliente cliente,
                                         List<LineaFactura> lineas, int descuento,
                                         String observaciones, String referencia, DatosPago datosPago,
                                         boolean comoNuevaVersion)
            throws SQLException, ValidationException {
        validar(lineas, descuento);

        Database.beginTransaction();
        try {
            Factura factura = facturaRepository.getById(facturaId);
            if (factura == null) {
                throw new ValidationException("La factura no existe");
            }
            EstadoFactura estado = estadoActual(facturaId);
            if (estado != EstadoFactura.EMITIDA) {
                throw new ValidationException("Solo se pueden editar facturas en estado Emitida");
            }
            Serie serie = serieRepository.getById(factura.getSerieId());

            if (cliente != null && cliente.getId() == null && !isVacio(cliente)) {
                long clienteId = clienteRepository.insertar(cliente);
                cliente.setId(clienteId);
            }
            facturaRepository.actualizarCliente(facturaId, cliente == null ? null : cliente.getId());
            if (cliente != null && cliente.getId() != null) {
                clienteRepository.actualizar(cliente);
            }

            String numero = numeroService.formarNumero(serie, factura.getCorrelativo(), fecha);
            FacturaVersion ultima = versionRepository.ultimaVersion(facturaId);
            FacturaVersion guardada;
            if (!comoNuevaVersion && versionAbiertaId != null && ultima != null
                    && versionAbiertaId.longValue() == ultima.getId().longValue()) {
                guardada = versionadoService.sobrescribirVersion(ultima.getId(), fecha, numero, EstadoFactura.EMITIDA,
                        descuento, observaciones, referencia, cliente, lineas, datosPago);
            } else {
                guardada = versionadoService.crearVersion(facturaId, fecha, numero, EstadoFactura.EMITIDA,
                        descuento, observaciones, referencia, cliente, lineas, datosPago);
            }
            Database.commit();
            return guardada;
        } catch (SQLException | ValidationException | RuntimeException e) {
            Database.rollback();
            throw e;
        } finally {
            Database.endTransaction();
        }
    }

    public EstadoFactura estadoActual(long facturaId) throws SQLException {
        FacturaVersion v = versionRepository.ultimaVersion(facturaId);
        return v == null ? null : v.getEstado();
    }

    public Serie serieDeFactura(long facturaId) throws SQLException {
        Factura f = facturaRepository.getById(facturaId);
        return f == null ? null : serieRepository.getById(f.getSerieId());
    }

    public Factura factura(long facturaId) throws SQLException {
        return facturaRepository.getById(facturaId);
    }

    public Cliente cliente(long clienteId) throws SQLException {
        return clienteId == 0 ? null : clienteRepository.getById(clienteId);
    }

    /**
     * Abre una version concreta con sus lineas y el cliente maestro (si existe).
     */
    public VersionCompleta abrirVersion(long versionId) throws SQLException {
        FacturaVersion v = versionRepository.getById(versionId);
        if (v == null) {
            return null;
        }
        List<LineaFactura> lineas = lineaRepository.getLineas(versionId);
        Factura f = facturaRepository.getById(v.getFacturaId());
        Cliente cliente = f == null || f.getClienteId() == null ? null : clienteRepository.getById(f.getClienteId());
        return new VersionCompleta(f, v, lineas, cliente);
    }

    public int maxVersion(long facturaId) throws SQLException {
        return versionRepository.maxVersion(facturaId);
    }

    private void validar(List<LineaFactura> lineas, int descuento) throws ValidationException {
        if (lineas == null || lineas.isEmpty()) {
            throw new ValidationException("La factura debe tener al menos una linea");
        }
        for (LineaFactura l : lineas) {
            if (l.getCantidad() < 1) {
                throw new ValidationException("La cantidad de cada linea debe ser al menos 1");
            }
            if (nz(l.getPrecioUnitario()).signum() < 0 || nz(l.getTotalBase()).signum() < 0) {
                throw new ValidationException("Los importes no pueden ser negativos");
            }
            if (l.getTipoIvaId() == null) {
                throw new ValidationException("Cada linea debe tener un tipo de IVA");
            }
        }
        if (descuento < 0 || descuento > 100) {
            throw new ValidationException("El descuento debe estar entre 0 y 100");
        }
    }

    private boolean isVacio(Cliente c) {
        return c == null || isBlank(c.getNombre()) && isBlank(c.getNif()) && isBlank(c.getDireccion());
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    public record VersionCompleta(Factura factura, FacturaVersion version, List<LineaFactura> lineas, Cliente cliente) {
    }
}
