package cabofactu.modelo;

import cabofactu.modelo.negocio.sqlite.Database;
import cabofactu.modelo.negocio.sqlite.DatosException;
import cabofactu.modelo.negocio.sqlite.ClienteRepository;
import cabofactu.modelo.negocio.sqlite.ConfigRepository;
import cabofactu.modelo.negocio.sqlite.CopiaRepository;
import cabofactu.modelo.negocio.sqlite.FacturaRepository;
import cabofactu.modelo.negocio.sqlite.HistorialRepository;
import cabofactu.modelo.negocio.sqlite.IvaRepository;
import cabofactu.modelo.negocio.sqlite.LineaRepository;
import cabofactu.modelo.negocio.sqlite.NumeroDisponibleRepository;
import cabofactu.modelo.negocio.sqlite.SerieRepository;
import cabofactu.modelo.negocio.sqlite.TipoRetencionRepository;
import cabofactu.modelo.negocio.sqlite.VersionRepository;

import java.sql.SQLException;
import java.time.Clock;
import cabofactu.fichero.BackupService;
import cabofactu.modelo.negocio.ClienteService;
import cabofactu.modelo.negocio.ConfigService;
import cabofactu.modelo.negocio.EstadoService;
import cabofactu.modelo.negocio.FacturaService;
import cabofactu.modelo.negocio.FacturacionMensualService;
import cabofactu.modelo.negocio.HistorialService;
import cabofactu.modelo.negocio.IvaService;
import cabofactu.modelo.negocio.NumeroService;
import cabofactu.modelo.negocio.RectificativaService;
import cabofactu.modelo.negocio.Reloj;
import cabofactu.modelo.negocio.RetencionService;
import cabofactu.modelo.negocio.SerieService;
import cabofactu.modelo.negocio.VersionadoService;

/**
 * Contenedor de dependencias construido una sola vez en el arranque.
 * Repositorios y servicios se inyectan a mano por constructor.
 */
public class Servicios {

    public final Reloj reloj;
    public final ClienteService clientes;
    public final SerieService series;
    public final IvaService ivas;
    public final RetencionService retenciones;
    public final ConfigService config;

    public final NumeroService numeros;
    public final VersionadoService versionado;
    public final FacturaService factura;
    public final EstadoService estado;
    public final RectificativaService rectificativas;
    public final FacturacionMensualService facturacionMensual;
    public final HistorialService historialService;
    public final BackupService backup;

    public Servicios() {
        this(Clock.systemDefaultZone());
    }

    public Servicios(Clock clock) {
        try {
            Database.getConnection();
        } catch (SQLException e) {
            throw new DatosException(e);
        }
        ClienteRepository clienteRepository = new ClienteRepository();
        SerieRepository serieRepository = new SerieRepository();
        IvaRepository ivaRepository = new IvaRepository();
        TipoRetencionRepository tipoRetencionRepository = new TipoRetencionRepository();
        FacturaRepository facturaRepository = new FacturaRepository();
        VersionRepository versionRepository = new VersionRepository();
        LineaRepository lineaRepository = new LineaRepository();
        ConfigRepository configRepository = new ConfigRepository();
        CopiaRepository copiaRepository = new CopiaRepository();
        HistorialRepository historialRepository = new HistorialRepository();
        NumeroDisponibleRepository numeroDisponibleRepository = new NumeroDisponibleRepository();

        reloj = new Reloj(clock);
        clientes = new ClienteService(clienteRepository);
        series = new SerieService(serieRepository, facturaRepository, clock);
        ivas = new IvaService(ivaRepository);
        retenciones = new RetencionService(tipoRetencionRepository);
        config = new ConfigService(configRepository);

        numeros = new NumeroService(serieRepository, numeroDisponibleRepository, clock);
        versionado = new VersionadoService(versionRepository, lineaRepository, clock);
        factura = new FacturaService(facturaRepository, serieRepository, clienteRepository, versionRepository, lineaRepository, versionado, numeros, numeroDisponibleRepository, clock);
        estado = new EstadoService(facturaRepository, serieRepository, versionRepository, lineaRepository, versionado, numeros, factura);
        rectificativas = new RectificativaService(factura, serieRepository, tipoRetencionRepository);
        facturacionMensual = new FacturacionMensualService(factura, facturaRepository, numeros);
        historialService = new HistorialService(historialRepository);
        backup = new BackupService(copiaRepository, facturaRepository, clock);
    }
}
