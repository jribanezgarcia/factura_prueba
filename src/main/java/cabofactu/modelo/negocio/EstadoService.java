package cabofactu.modelo.negocio;

import cabofactu.modelo.negocio.sqlite.Database;
import cabofactu.modelo.dominio.Cliente;
import cabofactu.modelo.dominio.DatosPago;
import cabofactu.modelo.dominio.EstadoFactura;
import cabofactu.modelo.dominio.Factura;
import cabofactu.modelo.dominio.FacturaVersion;
import cabofactu.modelo.dominio.LineaFactura;
import cabofactu.modelo.dominio.Serie;
import cabofactu.modelo.negocio.sqlite.FacturaRepository;
import cabofactu.modelo.negocio.sqlite.LineaRepository;
import cabofactu.modelo.negocio.sqlite.SerieRepository;
import cabofactu.modelo.negocio.sqlite.VersionRepository;

import java.util.ArrayList;
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

    public void anular(long facturaId) throws ValidationException {
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
                EstadoFactura actual = facturaService.estadoActual(facturaId);
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

    public void restaurar(long facturaId) throws ValidationException {
        cambiarEstado(facturaId, null, EstadoFactura.EMITIDA);
    }

    public void restaurarVersion(long versionId) throws ValidationException {
        cambiarEstado(0, versionId, EstadoFactura.EMITIDA);
    }

    private void cambiarEstado(long facturaId, Long versionId, EstadoFactura nuevo)
            throws ValidationException {
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
        } catch (ValidationException | RuntimeException e) {
            Database.rollback();
            throw e;
        } finally {
            Database.endTransaction();
        }
    }

    private boolean numeroOcupado(long facturaId, FacturaVersion base) {
        Factura f = facturaRepository.getById(facturaId);
        Serie serie = serieRepository.getById(f.getSerieId());
        return numeroService.correlativoOcupadoPorActiva(serie, f.getCorrelativo(), base.getFechaFactura());
    }

    private Cliente snapshotCliente(long facturaId, FacturaVersion base) {
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
