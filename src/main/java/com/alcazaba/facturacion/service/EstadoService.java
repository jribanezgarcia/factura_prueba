package com.alcazaba.facturacion.service;

import com.alcazaba.facturacion.db.Database;
import com.alcazaba.facturacion.model.Cliente;
import com.alcazaba.facturacion.model.DatosPago;
import com.alcazaba.facturacion.model.EstadoFactura;
import com.alcazaba.facturacion.model.Factura;
import com.alcazaba.facturacion.model.FacturaVersion;
import com.alcazaba.facturacion.model.LineaFactura;
import com.alcazaba.facturacion.model.Serie;
import com.alcazaba.facturacion.repository.FacturaRepository;
import com.alcazaba.facturacion.repository.LineaRepository;
import com.alcazaba.facturacion.repository.SerieRepository;
import com.alcazaba.facturacion.repository.VersionRepository;

import java.sql.SQLException;
import java.util.List;

/**
 * Estados Emitida/Anulada. Anular y restaurar crean una nueva version sin
 * modificar las anteriores. La restauracion se bloquea si el numero esta
 * ocupado por otra factura activa.
 */
public class EstadoService {

    private final FacturaRepository facturaRepository;
    private final SerieRepository serieRepository;
    private final VersionRepository versionRepository;
    private final LineaRepository lineaRepository;
    private final VersionadoService versionadoService;
    private final NumeroService numeroService;
    private final FacturaService facturaService;

    public EstadoService(FacturaRepository facturaRepository, SerieRepository serieRepository,
                         VersionRepository versionRepository, LineaRepository lineaRepository,
                         VersionadoService versionadoService, NumeroService numeroService,
                         FacturaService facturaService) {
        this.facturaRepository = facturaRepository;
        this.serieRepository = serieRepository;
        this.versionRepository = versionRepository;
        this.lineaRepository = lineaRepository;
        this.versionadoService = versionadoService;
        this.numeroService = numeroService;
        this.facturaService = facturaService;
    }

    public void anular(long facturaId) throws SQLException, ValidationException {
        cambiarEstado(facturaId, null, EstadoFactura.ANULADA);
    }

    public void restaurar(long facturaId) throws SQLException, ValidationException {
        cambiarEstado(facturaId, null, EstadoFactura.EMITIDA);
    }

    public void restaurarVersion(long versionId) throws SQLException, ValidationException {
        cambiarEstado(0, versionId, EstadoFactura.EMITIDA);
    }

    private void cambiarEstado(long facturaId, Long versionId, EstadoFactura nuevo)
            throws SQLException, ValidationException {
        FacturaVersion base;
        long fId;
        if (versionId != null) {
            base = versionRepository.getById(versionId);
            if (base == null) {
                throw new ValidationException("La version no existe");
            }
            fId = base.getFacturaId();
        } else {
            fId = facturaId;
            base = versionRepository.ultimaVersion(fId);
            if (base == null) {
                throw new ValidationException("La factura no existe");
            }
        }

        Database.beginTransaction();
        try {
            EstadoFactura actual = facturaService.estadoActual(fId);
            if (nuevo == EstadoFactura.ANULADA && actual != EstadoFactura.EMITIDA) {
                throw new ValidationException("Solo se pueden anular facturas en estado Emitida");
            }
            if (nuevo == EstadoFactura.EMITIDA && actual != EstadoFactura.ANULADA) {
                throw new ValidationException("Solo se pueden restaurar facturas en estado Anulada");
            }
            if (nuevo == EstadoFactura.EMITIDA && numeroOcupado(fId, base)) {
                throw new ValidationException(
                        "No se puede restaurar: el numero " + base.getNumero()
                                + " esta ocupado por otra factura activa");
            }

            List<LineaFactura> lineas = lineaRepository.getLineas(base.getId());
            Cliente cliente = snapshotCliente(fId, base);

            versionadoService.crearVersion(fId, base.getFechaFactura(), base.getNumero(), nuevo,
                    base.getDescuentoPorcentaje(), base.getObservaciones(), base.getReferenciaRectifica(),
                    cliente, lineas,
                    new DatosPago(base.getFormaPago(), base.getVencimiento(), base.getRealizadaPor()));
            Database.commit();
        } catch (SQLException | ValidationException | RuntimeException e) {
            Database.rollback();
            throw e;
        } finally {
            Database.endTransaction();
        }
    }

    private boolean numeroOcupado(long facturaId, FacturaVersion base) throws SQLException {
        Factura f = facturaRepository.getById(facturaId);
        Serie serie = serieRepository.getById(f.getSerieId());
        return numeroService.correlativoOcupadoPorActiva(serie, f.getCorrelativo(), base.getFechaFactura());
    }

    private Cliente snapshotCliente(long facturaId, FacturaVersion base) throws SQLException {
        Factura f = facturaRepository.getById(facturaId);
        Cliente c = new Cliente();
        c.setId(f.getClienteId());
        c.setNombre(base.getCliNombre());
        c.setNif(base.getCliNif());
        c.setDireccion(base.getCliDireccion());
        c.setCp(base.getCliCp());
        c.setLocalidad(base.getCliLocalidad());
        c.setProvincia(base.getCliProvincia());
        c.setEmail(base.getCliEmail());
        return c;
    }
}
