package cabofactu.modelo;

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

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import cabofactu.fichero.CopiaSeguridad;
import cabofactu.modelo.dominio.Cliente;
import cabofactu.modelo.dominio.EmpresaDisponible;
import cabofactu.modelo.negocio.Clientes;
import cabofactu.modelo.negocio.Configuracion;
import cabofactu.modelo.negocio.Empresas;
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
        series = new Series(serieDAO, facturaDAO, clock);
        tiposIva = new TiposIva(tipoIvaDAO);
        tiposRetencion = new TiposRetencion(tipoRetencionDAO);
        configuracion = new Configuracion(configuracionDAO);

        numeracion = new Numeracion(serieDAO, numeroDisponibleDAO, clock);
        versiones = new Versiones(versionFacturaDAO, lineaFacturaDAO, clock);
        facturas = new Facturas(facturaDAO, serieDAO, versionFacturaDAO, lineaFacturaDAO, versiones, numeracion, numeroDisponibleDAO, clock);
        estados = new Estados(facturaDAO, serieDAO, versionFacturaDAO, lineaFacturaDAO, versiones, numeracion, facturas);
        rectificativas = new Rectificativas(facturas, serieDAO, tipoRetencionDAO);
        facturacionMensual = new FacturacionMensual(facturas, facturaDAO, numeracion);
        historial = new Historial(historialDAO);
        copiaSeguridad = new CopiaSeguridad(copiaSeguridadDAO, facturaDAO, clock);
    }

    public Reloj getReloj() {
        return reloj;
    }

    public List<Cliente> listadoClientes(boolean soloActivos) throws Exception {
        return Clientes.getClientes().listado(soloActivos);
    }

    public List<Cliente> listadoClientes(String texto, boolean soloActivos) throws Exception {
        return Clientes.getClientes().listado(texto, soloActivos);
    }

    public Cliente buscarCliente(long id) throws Exception {
        return Clientes.getClientes().buscar(id);
    }

    public long altaCliente(Cliente cliente) throws Exception {
        return Clientes.getClientes().alta(cliente);
    }

    public void modificarCliente(Cliente cliente) throws Exception {
        Clientes.getClientes().modificar(cliente);
    }

    public void bajaCliente(long id) throws Exception {
        Clientes.getClientes().baja(id);
    }

    public void desactivarCliente(long id) throws Exception {
        Clientes.getClientes().desactivar(id);
    }

    public boolean clienteTieneFacturas(long id) throws Exception {
        return Clientes.getClientes().tieneFacturas(id);
    }

    public List<EmpresaDisponible> listadoEmpresas() throws Exception {
        return Empresas.getEmpresas().listado();
    }

    public EmpresaDisponible altaEmpresa(String nombre) throws Exception {
        return Empresas.getEmpresas().alta(nombre);
    }

    public void bajaEmpresa(String carpeta) throws Exception {
        Empresas.getEmpresas().baja(carpeta);
    }

    public void abrirEmpresa(String carpeta, LocalDate fecha) throws Exception {
        Empresas.getEmpresas().abrir(carpeta, fecha);
    }

    public void cerrarEmpresa() {
        Empresas.getEmpresas().cerrar();
    }

    public List<String> datosPendientesEmpresa() {
        return configuracion.datosPendientes();
    }

    public void comprobarDatosEmpresa() throws Exception {
        configuracion.comprobarEmpresaCompleta();
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
