package cabofactu.modelo.negocio;

import cabofactu.modelo.negocio.sqlite.DatosException;
import cabofactu.modelo.dominio.Cliente;
import cabofactu.modelo.dominio.DatosPago;
import cabofactu.modelo.dominio.EstadoFactura;
import cabofactu.modelo.dominio.FacturaVersion;
import cabofactu.modelo.dominio.LineaFactura;
import cabofactu.modelo.dominio.ResumenFactura;
import cabofactu.modelo.dominio.TipoRetencion;
import cabofactu.modelo.negocio.sqlite.LineaRepository;
import cabofactu.modelo.negocio.sqlite.VersionRepository;

import java.time.Clock;
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
    private final Clock clock;

    public VersionadoService(VersionRepository versionRepository, LineaRepository lineaRepository, Clock clock) {
        this.versionRepository = versionRepository;
        this.lineaRepository = lineaRepository;
        this.clock = clock;
    }

    public FacturaVersion crearVersion(long facturaId, LocalDate fecha, String numero, EstadoFactura estado,
                                       int descuento, String observaciones, String referencia,
                                       Cliente cliente, List<LineaFactura> lineas) {
        return crearVersion(facturaId, fecha, numero, estado, descuento, observaciones, referencia,
                cliente, lineas, null, null);
    }

    public FacturaVersion crearVersion(long facturaId, LocalDate fecha, String numero, EstadoFactura estado,
                                       int descuento, String observaciones, String referencia,
                                       Cliente cliente, List<LineaFactura> lineas, DatosPago datosPago) {
        return crearVersion(facturaId, fecha, numero, estado, descuento, observaciones, referencia,
                cliente, lineas, datosPago, null);
    }

    public FacturaVersion crearVersion(long facturaId, LocalDate fecha, String numero, EstadoFactura estado,
                                       int descuento, String observaciones, String referencia,
                                       Cliente cliente, List<LineaFactura> lineas, DatosPago datosPago,
                                       TipoRetencion retencion) {
        int versionNum = versionRepository.maxVersion(facturaId) + 1;
        ResumenFactura resumen = CalculoService.resumen(lineas, descuento, retencion);

        FacturaVersion v = new FacturaVersion();
        v.setFacturaId(facturaId);
        v.setVersionNum(versionNum);
        v.setNumero(numero);
        v.setFechaFactura(fecha);
        v.setFechaGuardado(LocalDateTime.now(clock));
        v.setEstado(estado);
        v.setDescuentoPorcentaje(descuento);
        v.setObservaciones(observaciones);
        v.setReferenciaRectifica(referencia);
        aplicarSnapshot(v, cliente, datosPago);
        v.setBaseTotal(resumen.getBaseTotal());
        v.setIvaTotal(resumen.getIvaTotal());
        v.setImporteRetencion(resumen.getImporteRetencion());
        v.setTipoRetencionId(resumen.getTipoRetencionId());
        v.setTipoRetencionNombre(resumen.getNombreRetencion());
        v.setTipoRetencionPorcentaje(resumen.getPorcentajeRetencion());
        v.setTotal(resumen.getTotal());
        v.setTotalSuplidos(resumen.getTotalSuplidos());

        long id = versionRepository.insertarVersion(v);
        v.setId(id);

        int orden = 1;
        for (LineaFactura l : lineas) {
            l.setOrden(orden++);
        }
        lineaRepository.insertarLineas(id, lineas);
        return v;
    }

    public List<FacturaVersion> versionesDeFactura(long facturaId) {
        return versionRepository.getVersiones(facturaId);
    }

    /**
     * Reemplaza en su lugar una version existente (la actual de la factura):
     * actualiza el snapshot y sustituye sus lineas. Las versiones anteriores
     * permanecen intactas.
     */
    public FacturaVersion sobrescribirVersion(long versionId, LocalDate fecha, String numero, EstadoFactura estado,
                                              int descuento, String observaciones, String referencia,
                                              Cliente cliente, List<LineaFactura> lineas, DatosPago datosPago) {
        return sobrescribirVersion(versionId, fecha, numero, estado, descuento, observaciones, referencia,
                cliente, lineas, datosPago, null);
    }

    public FacturaVersion sobrescribirVersion(long versionId, LocalDate fecha, String numero, EstadoFactura estado,
                                              int descuento, String observaciones, String referencia,
                                              Cliente cliente, List<LineaFactura> lineas, DatosPago datosPago,
                                              TipoRetencion retencion) {
        FacturaVersion v = versionRepository.getById(versionId);
        if (v == null) {
            throw new DatosException("La version " + versionId + " no existe");
        }
        ResumenFactura resumen = CalculoService.resumen(lineas, descuento, retencion);

        v.setNumero(numero);
        v.setFechaFactura(fecha);
        v.setFechaGuardado(LocalDateTime.now(clock));
        v.setEstado(estado);
        v.setDescuentoPorcentaje(descuento);
        v.setObservaciones(observaciones);
        v.setReferenciaRectifica(referencia);
        aplicarSnapshot(v, cliente, datosPago);
        v.setBaseTotal(resumen.getBaseTotal());
        v.setIvaTotal(resumen.getIvaTotal());
        v.setImporteRetencion(resumen.getImporteRetencion());
        v.setTipoRetencionId(resumen.getTipoRetencionId());
        v.setTipoRetencionNombre(resumen.getNombreRetencion());
        v.setTipoRetencionPorcentaje(resumen.getPorcentajeRetencion());
        v.setTotal(resumen.getTotal());
        v.setTotalSuplidos(resumen.getTotalSuplidos());

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

    public FacturaVersion ultimaVersion(long facturaId) {
        return versionRepository.ultimaVersion(facturaId);
    }
}
