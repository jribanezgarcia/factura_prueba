package cabofactu.modelo.negocio;

import cabofactu.modelo.negocio.sqlite.Conexion;
import cabofactu.modelo.dominio.Cliente;
import cabofactu.modelo.dominio.DatosPago;
import cabofactu.modelo.dominio.EstadoFactura;
import cabofactu.modelo.dominio.Factura;
import cabofactu.modelo.dominio.VersionFactura;
import cabofactu.modelo.dominio.LineaFactura;
import cabofactu.modelo.dominio.Serie;
import cabofactu.modelo.negocio.sqlite.FacturaDAO;
import cabofactu.modelo.negocio.sqlite.LineaFacturaDAO;
import cabofactu.modelo.negocio.sqlite.SerieDAO;
import cabofactu.modelo.negocio.sqlite.VersionFacturaDAO;

import java.util.ArrayList;
import java.util.List;

/**
 * Estados Emitida/Anulada. Anular y restaurar crean una nueva version sin
 * modificar las anteriores. La restauracion se bloquea si el numero esta
 * ocupado por otra factura activa.
 */
public class Estados {

    private final FacturaDAO facturaDAO;
    private final SerieDAO serieDAO;
    private final VersionFacturaDAO versionFacturaDAO;
    private final LineaFacturaDAO lineaFacturaDAO;
    private final Versiones versiones;
    private final Numeracion numeracion;
    private final Facturas facturas;

    public Estados(FacturaDAO facturaDAO, SerieDAO serieDAO,
                         VersionFacturaDAO versionFacturaDAO, LineaFacturaDAO lineaFacturaDAO,
                         Versiones versiones, Numeracion numeracion,
                         Facturas facturas) {
        this.facturaDAO = facturaDAO;
        this.serieDAO = serieDAO;
        this.versionFacturaDAO = versionFacturaDAO;
        this.lineaFacturaDAO = lineaFacturaDAO;
        this.versiones = versiones;
        this.numeracion = numeracion;
        this.facturas = facturas;
    }

    public void anular(long facturaId) throws ValidacionException {
        cambiarEstado(facturaId, null, EstadoFactura.ANULADA);
    }

    public AnulacionResultado anularFacturas(List<Long> facturaIds) {
        int anuladas = 0;
        int yaAnuladas = 0;
        int fallos = 0;
        List<String> errores = new ArrayList<>();
        for (Long facturaId : facturaIds) {
            if (facturaId == null) {
                continue;
            }
            try {
                EstadoFactura actual = facturas.estadoActual(facturaId);
                if (actual == EstadoFactura.ANULADA) {
                    yaAnuladas++;
                    continue;
                }
                anular(facturaId);
                anuladas++;
            } catch (Exception e) {
                fallos++;
                errores.add("Factura " + facturaId + ": " + e.getMessage());
            }
        }
        return new AnulacionResultado(anuladas, yaAnuladas, fallos, errores);
    }

    public void restaurar(long facturaId) throws ValidacionException {
        cambiarEstado(facturaId, null, EstadoFactura.EMITIDA);
    }

    public void restaurarVersion(long versionId) throws ValidacionException {
        cambiarEstado(0, versionId, EstadoFactura.EMITIDA);
    }

    private void cambiarEstado(long facturaId, Long versionId, EstadoFactura nuevo)
            throws ValidacionException {
        VersionFactura base;
        long fId;
        if (versionId != null) {
            base = versionFacturaDAO.getById(versionId);
            if (base == null) {
                throw new ValidacionException("La version no existe");
            }
            fId = base.getFacturaId();
        } else {
            fId = facturaId;
            base = versionFacturaDAO.ultimaVersion(fId);
            if (base == null) {
                throw new ValidacionException("La factura no existe");
            }
        }

        Conexion.iniciarTransaccion();
        try {
            EstadoFactura actual = facturas.estadoActual(fId);
            if (nuevo == EstadoFactura.ANULADA && actual != EstadoFactura.EMITIDA) {
                throw new ValidacionException("Solo se pueden anular facturas en estado Emitida");
            }
            if (nuevo == EstadoFactura.EMITIDA && actual != EstadoFactura.ANULADA) {
                throw new ValidacionException("Solo se pueden restaurar facturas en estado Anulada");
            }
            if (nuevo == EstadoFactura.EMITIDA && numeroOcupado(fId, base)) {
                throw new ValidacionException(
                        "No se puede restaurar: el numero " + base.getNumero()
                                + " esta ocupado por otra factura activa");
            }

            List<LineaFactura> lineas = lineaFacturaDAO.getLineas(base.getId());
            Cliente cliente = snapshotCliente(fId, base);

            versiones.crearVersion(fId, base.getFechaFactura(), base.getNumero(), nuevo,
                    base.getDescuentoPorcentaje(), base.getObservaciones(), base.getReferenciaRectifica(),
                    cliente, lineas,
                    new DatosPago(base.getFormaPago(), base.getVencimiento(), base.getRealizadaPor()));
            Conexion.confirmar();
        } catch (ValidacionException | RuntimeException e) {
            Conexion.deshacer();
            throw e;
        } finally {
            Conexion.terminarTransaccion();
        }
    }

    private boolean numeroOcupado(long facturaId, VersionFactura base) {
        Factura f = facturaDAO.getById(facturaId);
        Serie serie = serieDAO.getById(f.getSerieId());
        return numeracion.correlativoOcupadoPorActiva(serie, f.getCorrelativo(), base.getFechaFactura());
    }

    private Cliente snapshotCliente(long facturaId, VersionFactura base) {
        Factura f = facturaDAO.getById(facturaId);
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

    public static class AnulacionResultado {
        private final int anuladas;
        private final int yaAnuladas;
        private final int fallos;
        private final List<String> errores;

        public AnulacionResultado(int anuladas, int yaAnuladas, int fallos, List<String> errores) {
            this.anuladas = anuladas;
            this.yaAnuladas = yaAnuladas;
            this.fallos = fallos;
            this.errores = errores;
        }

        public int getAnuladas() {
            return anuladas;
        }

        public int getYaAnuladas() {
            return yaAnuladas;
        }

        public int getFallos() {
            return fallos;
        }

        public List<String> getErrores() {
            return errores;
        }
    }
}
