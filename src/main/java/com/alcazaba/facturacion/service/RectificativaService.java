package com.alcazaba.facturacion.service;

import com.alcazaba.facturacion.model.Cliente;
import com.alcazaba.facturacion.model.Factura;
import com.alcazaba.facturacion.model.FacturaVersion;
import com.alcazaba.facturacion.model.LineaFactura;
import com.alcazaba.facturacion.model.Serie;
import com.alcazaba.facturacion.model.TipoRetencion;
import com.alcazaba.facturacion.repository.SerieRepository;
import com.alcazaba.facturacion.repository.TipoRetencionRepository;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Creacion de rectificativas: se parte de una factura existente, se copian
 * cliente, lineas, cantidades, descripciones, precios, IVA, descuento y
 * observaciones, y se guardan en la serie de rectificativas (R) con una
 * referencia automatica editable a la factura que se rectifica.
 */
public class RectificativaService {

    private final FacturaService facturaService;
    private final SerieRepository serieRepository;
    private final TipoRetencionRepository tipoRetencionRepository;

    public RectificativaService(FacturaService facturaService, SerieRepository serieRepository,
                                TipoRetencionRepository tipoRetencionRepository) {
        this.facturaService = facturaService;
        this.serieRepository = serieRepository;
        this.tipoRetencionRepository = tipoRetencionRepository;
    }

    /**
     * Serie configurada como rectificativa. Si hubiera varias, se usa la
     * primera; si no existe ninguna, error.
     */
    public Serie serieRectificativa() throws SQLException, ValidationException {
        for (Serie s : serieRepository.listar()) {
            if (s.isEsRectificativa()) {
                return s;
            }
        }
        throw new ValidationException("No hay ninguna serie de rectificativas configurada");
    }

    /**
     * Crea una rectificativa a partir de una version concreta de otra factura
     * (que puede ser, a su vez, una rectificativa). Si la referencia viene en
     * blanco se genera automaticamente con el numero de la factura original.
     */
    public long crearRectificativa(long versionOrigenId, LocalDate fecha, String referencia)
            throws SQLException, ValidationException {
        FacturaService.VersionCompleta origen = facturaService.abrirVersion(versionOrigenId);
        if (origen == null) {
            throw new ValidationException("La factura de origen no existe");
        }

        Serie serieR = serieRectificativa();
        String ref = (referencia == null || referencia.isBlank())
                ? origen.version().getNumero()
                : referencia.trim();

        List<LineaFactura> lineas = new ArrayList<>();
        if (origen.lineas() != null) {
            for (LineaFactura l : origen.lineas()) {
                lineas.add(l.copia());
            }
        }

        TipoRetencion retencion = retencionDeVersion(origen.version());
        return facturaService.crearFactura(serieR, fecha, clienteDeVersion(origen), lineas,
                origen.version().getDescuentoPorcentaje(), origen.version().getObservaciones(), ref,
                null, null, retencion);
    }

    private TipoRetencion retencionDeVersion(FacturaVersion v) throws SQLException {
        Long trId = v.getTipoRetencionId();
        if (trId == null) {
            return null;
        }
        TipoRetencion t = tipoRetencionRepository.getById(trId);
        if (t != null) {
            return t;
        }
        TipoRetencion snapshot = new TipoRetencion();
        snapshot.setId(trId);
        snapshot.setNombre(nzTexto(v.getTipoRetencionNombre()));
        snapshot.setPorcentaje(v.getTipoRetencionPorcentaje() != null ? v.getTipoRetencionPorcentaje() : 0);
        snapshot.setActivo(false);
        return snapshot;
    }

    private String nzTexto(String s) {
        return s == null ? "" : s;
    }

    /**
     * Cliente para la rectificativa reconstruido desde el snapshot de la
     * version (nunca se pierde por borrados o cambios del maestro), conservando
     * el id del maestro para que ediciones posteriores actualicen la ficha.
     */
    private Cliente clienteDeVersion(FacturaService.VersionCompleta origen) {
        Factura f = origen.factura();
        FacturaVersion v = origen.version();
        Cliente c = new Cliente();
        c.setId(f == null ? null : f.getClienteId());
        c.setNombre(v.getCliNombre());
        c.setNif(v.getCliNif());
        c.setDireccion(v.getCliDireccion());
        c.setCp(v.getCliCp());
        c.setLocalidad(v.getCliLocalidad());
        c.setProvincia(v.getCliProvincia());
        return c;
    }
}
