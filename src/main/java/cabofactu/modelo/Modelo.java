package cabofactu.modelo;

import cabofactu.modelo.negocio.sqlite.Conexion;
import cabofactu.modelo.negocio.sqlite.DatosException;
import cabofactu.modelo.negocio.sqlite.ClienteDAO;
import cabofactu.modelo.negocio.sqlite.ConfiguracionDAO;
import cabofactu.modelo.negocio.sqlite.CopiaSeguridadDAO;
import cabofactu.modelo.negocio.sqlite.FacturaDAO;
import cabofactu.modelo.negocio.sqlite.HistorialDAO;
import cabofactu.modelo.negocio.sqlite.TipoIvaDAO;
import cabofactu.modelo.negocio.sqlite.LineaFacturaDAO;
import cabofactu.modelo.negocio.sqlite.NumeroDisponibleDAO;
import cabofactu.modelo.negocio.sqlite.SerieDAO;
import cabofactu.modelo.negocio.sqlite.TipoRetencionDAO;
import cabofactu.modelo.negocio.sqlite.VersionFacturaDAO;

import java.sql.SQLException;
import java.time.Clock;
import cabofactu.fichero.CopiaSeguridad;
import cabofactu.modelo.negocio.Clientes;
import cabofactu.modelo.negocio.Configuracion;
import cabofactu.modelo.negocio.Estados;
import cabofactu.modelo.negocio.Facturas;
import cabofactu.modelo.negocio.FacturacionMensual;
import cabofactu.modelo.negocio.Historial;
import cabofactu.modelo.negocio.TiposIva;
import cabofactu.modelo.negocio.Numeracion;
import cabofactu.modelo.negocio.Rectificativas;
import cabofactu.modelo.negocio.Reloj;
import cabofactu.modelo.negocio.TiposRetencion;
import cabofactu.modelo.negocio.Series;
import cabofactu.modelo.negocio.Versiones;

/**
 * Contenedor de dependencias construido una sola vez en el arranque.
 * Los DAO y las reglas se inyectan a mano por constructor.
 */
public class Modelo {

    private final Reloj reloj;
    private final Clientes clientes;
    private final Series series;
    private final TiposIva tiposIva;
    private final TiposRetencion tiposRetencion;
    private final Configuracion configuracion;

    private final Numeracion numeracion;
    private final Versiones versiones;
    private final Facturas facturas;
    private final Estados estados;
    private final Rectificativas rectificativas;
    private final FacturacionMensual facturacionMensual;
    private final Historial historial;
    private final CopiaSeguridad copiaSeguridad;

    public Modelo() {
        this(Clock.systemDefaultZone());
    }

    public Modelo(Clock clock) {
        try {
            Conexion.establecerConexion();
        } catch (SQLException e) {
            throw new DatosException(e);
        }
        ClienteDAO clienteDAO = new ClienteDAO();
        SerieDAO serieDAO = new SerieDAO();
        TipoIvaDAO tipoIvaDAO = new TipoIvaDAO();
        TipoRetencionDAO tipoRetencionDAO = new TipoRetencionDAO();
        FacturaDAO facturaDAO = new FacturaDAO();
        VersionFacturaDAO versionFacturaDAO = new VersionFacturaDAO();
        LineaFacturaDAO lineaFacturaDAO = new LineaFacturaDAO();
        ConfiguracionDAO configuracionDAO = new ConfiguracionDAO();
        CopiaSeguridadDAO copiaSeguridadDAO = new CopiaSeguridadDAO();
        HistorialDAO historialDAO = new HistorialDAO();
        NumeroDisponibleDAO numeroDisponibleDAO = new NumeroDisponibleDAO();

        reloj = new Reloj(clock);
        clientes = new Clientes(clienteDAO);
        series = new Series(serieDAO, facturaDAO, clock);
        tiposIva = new TiposIva(tipoIvaDAO);
        tiposRetencion = new TiposRetencion(tipoRetencionDAO);
        configuracion = new Configuracion(configuracionDAO);

        numeracion = new Numeracion(serieDAO, numeroDisponibleDAO, clock);
        versiones = new Versiones(versionFacturaDAO, lineaFacturaDAO, clock);
        facturas = new Facturas(facturaDAO, serieDAO, clienteDAO, versionFacturaDAO, lineaFacturaDAO, versiones, numeracion, numeroDisponibleDAO, clock);
        estados = new Estados(facturaDAO, serieDAO, versionFacturaDAO, lineaFacturaDAO, versiones, numeracion, facturas);
        rectificativas = new Rectificativas(facturas, serieDAO, tipoRetencionDAO);
        facturacionMensual = new FacturacionMensual(facturas, facturaDAO, numeracion);
        historial = new Historial(historialDAO);
        copiaSeguridad = new CopiaSeguridad(copiaSeguridadDAO, facturaDAO, clock);
    }

    public Reloj getReloj() {
        return reloj;
    }

    public Clientes getClientes() {
        return clientes;
    }

    public Series getSeries() {
        return series;
    }

    public TiposIva getTiposIva() {
        return tiposIva;
    }

    public TiposRetencion getTiposRetencion() {
        return tiposRetencion;
    }

    public Configuracion getConfiguracion() {
        return configuracion;
    }

    public Numeracion getNumeracion() {
        return numeracion;
    }

    public Versiones getVersiones() {
        return versiones;
    }

    public Facturas getFacturas() {
        return facturas;
    }

    public Estados getEstados() {
        return estados;
    }

    public Rectificativas getRectificativas() {
        return rectificativas;
    }

    public FacturacionMensual getFacturacionMensual() {
        return facturacionMensual;
    }

    public Historial getHistorial() {
        return historial;
    }

    public CopiaSeguridad getCopiaSeguridad() {
        return copiaSeguridad;
    }
}
