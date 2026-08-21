package com.alcazaba.facturacion.service;

import com.alcazaba.facturacion.model.Cliente;
import com.alcazaba.facturacion.model.DatosPago;
import com.alcazaba.facturacion.model.EstadoFactura;
import com.alcazaba.facturacion.model.FacturaVersion;
import com.alcazaba.facturacion.model.LineaFactura;
import com.alcazaba.facturacion.model.ResumenFactura;
import com.alcazaba.facturacion.repository.LineaRepository;
import com.alcazaba.facturacion.repository.VersionRepository;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Creacion de versiones con snapshot completo. Las versiones anteriores nunca
 * se modifican: cada guardado (incluido anular/restaurar) inserta una nueva.
 * Al editar la ultima version de una factura emitida, el guardado la
 * sobrescribe en su lugar (ver sobrescribirVersion) en vez de crear vN+1.
 */
public class VersionadoService {

    private final VersionRepository versionRepository;
    private final LineaRepository lineaRepository;

    public VersionadoService(VersionRepository versionRepository, LineaRepository lineaRepository) {
        this.versionRepository = versionRepository;
        this.lineaRepository = lineaRepository;
    }

    public FacturaVersion crearVersion(long facturaId, LocalDate fecha, String numero, EstadoFactura estado,
                                       int descuento, String observaciones, String referencia,
                                       Cliente cliente, List<LineaFactura> lineas) throws SQLException {
        return crearVersion(facturaId, fecha, numero, estado, descuento, observaciones, referencia,
                cliente, lineas, null);
    }

    public FacturaVersion crearVersion(long facturaId, LocalDate fecha, String numero, EstadoFactura estado,
                                       int descuento, String observaciones, String referencia,
                                       Cliente cliente, List<LineaFactura> lineas, DatosPago datosPago) throws SQLException {
        int versionNum = versionRepository.maxVersion(facturaId) + 1;
        ResumenFactura resumen = CalculoService.resumen(lineas, descuento);

        FacturaVersion v = new FacturaVersion();
        v.setFacturaId(facturaId);
        v.setVersionNum(versionNum);
        v.setNumero(numero);
        v.setFechaFactura(fecha);
        v.setFechaGuardado(LocalDateTime.now());
        v.setEstado(estado);
        v.setDescuentoPorcentaje(descuento);
        v.setObservaciones(observaciones);
        v.setReferenciaRectifica(referencia);
        aplicarSnapshot(v, cliente, datosPago);
        v.setBaseTotal(resumen.getBaseTotal());
        v.setIvaTotal(resumen.getIvaTotal());
        v.setTotal(resumen.getTotal());

        long id = versionRepository.insertarVersion(v);
        v.setId(id);

        int orden = 1;
        for (LineaFactura l : lineas) {
            l.setOrden(orden++);
        }
        lineaRepository.insertarLineas(id, lineas);
        return v;
    }

    public List<FacturaVersion> versionesDeFactura(long facturaId) throws SQLException {
        return versionRepository.getVersiones(facturaId);
    }

    /**
     * Reemplaza en su lugar una version existente (la actual de la factura):
     * actualiza el snapshot y sustituye sus lineas. Las versiones anteriores
     * permanecen intactas.
     */
    public FacturaVersion sobrescribirVersion(long versionId, LocalDate fecha, String numero, EstadoFactura estado,
                                              int descuento, String observaciones, String referencia,
                                              Cliente cliente, List<LineaFactura> lineas, DatosPago datosPago) throws SQLException {
        FacturaVersion v = versionRepository.getById(versionId);
        if (v == null) {
            throw new java.sql.SQLException("La version " + versionId + " no existe");
        }
        ResumenFactura resumen = CalculoService.resumen(lineas, descuento);

        v.setNumero(numero);
        v.setFechaFactura(fecha);
        v.setFechaGuardado(LocalDateTime.now());
        v.setEstado(estado);
        v.setDescuentoPorcentaje(descuento);
        v.setObservaciones(observaciones);
        v.setReferenciaRectifica(referencia);
        aplicarSnapshot(v, cliente, datosPago);
        v.setBaseTotal(resumen.getBaseTotal());
        v.setIvaTotal(resumen.getIvaTotal());
        v.setTotal(resumen.getTotal());

        versionRepository.actualizarVersion(v);
        lineaRepository.eliminarPorVersion(versionId);

        int orden = 1;
        for (LineaFactura l : lineas) {
            l.setOrden(orden++);
        }
        lineaRepository.insertarLineas(versionId, lineas);
        return v;
    }

    private void aplicarSnapshot(FacturaVersion v, Cliente cliente, DatosPago datosPago) {
        if (cliente != null) {
            v.setCliNombre(cliente.getNombre());
            v.setCliNif(cliente.getNif());
            v.setCliDireccion(cliente.getDireccion());
            v.setCliCp(cliente.getCp());
            v.setCliLocalidad(cliente.getLocalidad());
            v.setCliProvincia(cliente.getProvincia());
            v.setCliEmail(nzTexto(cliente.getEmail()));
        }
        if (datosPago != null) {
            v.setFormaPago(nzTexto(datosPago.formaPago()));
            v.setVencimiento(datosPago.vencimiento());
            v.setRealizadaPor(nzTexto(datosPago.realizadaPor()));
        }
    }

    private String nzTexto(String s) {
        return s == null ? "" : s;
    }

    public FacturaVersion ultimaVersion(long facturaId) throws SQLException {
        return versionRepository.ultimaVersion(facturaId);
    }
}
