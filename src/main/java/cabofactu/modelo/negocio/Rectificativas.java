package cabofactu.modelo.negocio;

import cabofactu.modelo.dominio.Cliente;
import cabofactu.modelo.dominio.Factura;
import cabofactu.modelo.dominio.VersionFactura;
import cabofactu.modelo.dominio.LineaFactura;
import cabofactu.modelo.dominio.Serie;
import cabofactu.modelo.dominio.TipoRetencion;
import cabofactu.modelo.negocio.sqlite.SerieDAO;
import cabofactu.modelo.negocio.sqlite.TipoRetencionDAO;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Creacion de rectificativas: se parte de una factura existente, se copian
 * cliente, lineas, cantidades, descripciones, precios, IVA, descuento y
 * observaciones, y se guardan en la serie de rectificativas (R) con una
 * referencia automatica editable a la factura que se rectifica.
 */
public class Rectificativas {

    private final Facturas facturas;
    private final SerieDAO serieDAO;
    private final TipoRetencionDAO tipoRetencionDAO;

    public Rectificativas(Facturas facturas, SerieDAO serieDAO,
                                TipoRetencionDAO tipoRetencionDAO) {
        this.facturas = facturas;
        this.serieDAO = serieDAO;
        this.tipoRetencionDAO = tipoRetencionDAO;
    }

    /**
     * Serie configurada como rectificativa. Si hubiera varias, se usa la
     * primera; si no existe ninguna, error.
     */
    public Serie serieRectificativa() throws ValidacionException {
        for (Serie s : serieDAO.listar()) {
            if (s.isEsRectificativa()) {
                return s;
            }
        }
        throw new ValidacionException("No hay ninguna serie de rectificativas configurada");
    }

    /**
     * Crea una rectificativa a partir de una version concreta de otra factura
     * (que puede ser, a su vez, una rectificativa). Si la referencia viene en
     * blanco se genera automaticamente con el numero de la factura original.
     */
    public long crearRectificativa(long versionOrigenId, LocalDate fecha, String referencia)
            throws Exception {
        Facturas.VersionCompleta origen = facturas.abrirVersion(versionOrigenId);
        if (origen == null) {
            throw new ValidacionException("La factura de origen no existe");
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
        return facturas.crearFactura(serieR, fecha, clienteDeVersion(origen), lineas,
                origen.version().getDescuentoPorcentaje(), origen.version().getObservaciones(), ref,
                null, null, retencion);
    }

    private TipoRetencion retencionDeVersion(VersionFactura v) {
        Long trId = v.getTipoRetencionId();
        if (trId == null) {
            return null;
        }
        TipoRetencion t = tipoRetencionDAO.getById(trId);
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
    private Cliente clienteDeVersion(Facturas.VersionCompleta origen) throws Exception {
        VersionFactura v = origen.version();
        Cliente cliente = new Cliente(v.getCliNombre(), v.getCliNif(), v.getCliDireccion(),
                v.getCliCp(), v.getCliLocalidad(), v.getCliProvincia());
        if (origen.factura() != null) {
            cliente.setId(origen.factura().getClienteId());
        }
        cliente.setEmail(v.getCliEmail());
        return cliente;
    }
}
