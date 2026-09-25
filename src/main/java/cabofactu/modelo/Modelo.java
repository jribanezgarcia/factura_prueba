package cabofactu.modelo;

import cabofactu.modelo.negocio.sqlite.CopiaSeguridadDAO;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import cabofactu.fichero.CopiaSeguridad;
import cabofactu.modelo.dominio.Cliente;
import cabofactu.modelo.dominio.Empresa;
import cabofactu.modelo.dominio.EmpresaDisponible;
import cabofactu.modelo.dominio.Factura;
import cabofactu.modelo.dominio.FiltrosHistorial;
import cabofactu.modelo.dominio.Serie;
import cabofactu.modelo.dominio.TipoIva;
import cabofactu.modelo.dominio.TipoRetencion;
import cabofactu.modelo.negocio.Clientes;
import cabofactu.modelo.negocio.Configuracion;
import cabofactu.modelo.negocio.Empresas;
import cabofactu.modelo.negocio.Facturas;
import cabofactu.modelo.negocio.FacturacionMensual;
import cabofactu.modelo.negocio.Reloj;
import cabofactu.modelo.negocio.TiposRetencion;
import cabofactu.modelo.negocio.TiposIva;
import cabofactu.modelo.negocio.Series;

/**
 * Puerta de entrada única al negocio: cada operación delega en su singleton
 * con una sola línea.
 */
public class Modelo {

    private final Reloj reloj;

    private final FacturacionMensual facturacionMensual;
    private final CopiaSeguridad copiaSeguridad;

    public Modelo() {
        this(Clock.systemDefaultZone());
    }

    public Modelo(Clock clock) {
        reloj = new Reloj(clock);
        facturacionMensual = new FacturacionMensual(Facturas.getFacturas());
        copiaSeguridad = new CopiaSeguridad(new CopiaSeguridadDAO(), Facturas.getFacturas(), clock);
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

    /** Buscamos el cliente que tiene ese NIF, esté activo o no. */
    public Cliente buscarClientePorNif(String nif) throws Exception {
        return Clientes.getClientes().buscarPorNif(nif);
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

    public Empresa buscarEmpresa() throws Exception {
        return Configuracion.getConfiguracion().buscarEmpresa();
    }

    public void modificarEmpresa(Empresa empresa) throws Exception {
        Configuracion.getConfiguracion().modificarEmpresa(empresa);
    }

    public String preferencia(String clave) throws Exception {
        return Configuracion.getConfiguracion().preferencia(clave);
    }

    public void guardarPreferencia(String clave, String valor) throws Exception {
        Configuracion.getConfiguracion().guardarPreferencia(clave, valor);
    }

    public List<TipoIva> listadoTiposIva(boolean soloActivos) throws Exception {
        return TiposIva.getTiposIva().listado(soloActivos);
    }

    public TipoIva buscarTipoIva(long id) throws Exception {
        return TiposIva.getTiposIva().buscar(id);
    }

    /** Buscamos el tipo de IVA que tiene ese nombre. */
    public TipoIva buscarTipoIvaPorNombre(String nombre) throws Exception {
        return TiposIva.getTiposIva().buscarPorNombre(nombre);
    }

    public long altaTipoIva(TipoIva tipo) throws Exception {
        return TiposIva.getTiposIva().alta(tipo);
    }

    public void modificarTipoIva(TipoIva tipo) throws Exception {
        TiposIva.getTiposIva().modificar(tipo);
    }

    public void bajaTipoIva(long id) throws Exception {
        TiposIva.getTiposIva().baja(id);
    }

    public boolean tipoIvaEnUso(long id) throws Exception {
        return TiposIva.getTiposIva().enUso(id);
    }

    public List<TipoRetencion> listadoTiposRetencion(boolean soloActivos) throws Exception {
        return TiposRetencion.getTiposRetencion().listado(soloActivos);
    }

    public TipoRetencion buscarTipoRetencion(long id) throws Exception {
        return TiposRetencion.getTiposRetencion().buscar(id);
    }

    /** Buscamos el tipo de retención que tiene ese nombre. */
    public TipoRetencion buscarTipoRetencionPorNombre(String nombre) throws Exception {
        return TiposRetencion.getTiposRetencion().buscarPorNombre(nombre);
    }

    public long altaTipoRetencion(TipoRetencion tipo) throws Exception {
        return TiposRetencion.getTiposRetencion().alta(tipo);
    }

    public void modificarTipoRetencion(TipoRetencion tipo) throws Exception {
        TiposRetencion.getTiposRetencion().modificar(tipo);
    }

    public void bajaTipoRetencion(long id) throws Exception {
        TiposRetencion.getTiposRetencion().baja(id);
    }

    public boolean tipoRetencionEnUso(long id) throws Exception {
        return TiposRetencion.getTiposRetencion().enUso(id);
    }

    public List<Serie> listadoSeries() throws Exception {
        return Series.getSeries().listado();
    }

    public Serie buscarSerie(long id) throws Exception {
        return Series.getSeries().buscar(id);
    }

    public long altaSerie(Serie serie) throws Exception {
        return Series.getSeries().alta(serie);
    }

    public void modificarSerie(Serie serie) throws Exception {
        Series.getSeries().modificar(serie);
    }

    public void bajaSerie(long id) throws Exception {
        Series.getSeries().baja(id);
    }

    public boolean serieTieneFacturas(long id) throws Exception {
        return Series.getSeries().tieneFacturas(id);
    }

    public int siguienteCorrelativo(Serie serie, LocalDate fecha) throws Exception {
        return Series.getSeries().siguienteCorrelativo(serie, fecha);
    }

    public List<Integer> huecosDeSerie(Serie serie, LocalDate fecha) throws Exception {
        return Series.getSeries().huecos(serie, fecha);
    }

    public List<Integer> proponerNumeros(Serie serie, int anio, int cantidad, boolean usarHuecos)
            throws Exception {
        return Series.getSeries().proponerNumeros(serie, anio, cantidad, usarHuecos);
    }

    public String formarNumero(Serie serie, int correlativo, LocalDate fecha) {
        return Series.getSeries().formarNumero(serie, correlativo, fecha);
    }

    public Integer parseCorrelativo(Serie serie, String numero) {
        return Series.getSeries().parseCorrelativo(serie, numero);
    }

    public boolean correlativoOcupado(Serie serie, int correlativo, LocalDate fecha) throws Exception {
        return Series.getSeries().correlativoOcupado(serie, correlativo, fecha);
    }

    public long altaFactura(Factura factura) throws Exception {
        return Facturas.getFacturas().alta(factura);
    }

    public void modificarFactura(Factura factura) throws Exception {
        Facturas.getFacturas().modificar(factura);
    }

    public void bajaFactura(long id) throws Exception {
        Facturas.getFacturas().baja(id);
    }

    public Factura buscarFactura(long id) throws Exception {
        return Facturas.getFacturas().buscar(id);
    }

    public List<Factura> listadoFacturas(FiltrosHistorial filtros) throws Exception {
        return Facturas.getFacturas().listado(filtros);
    }

    public void anularFactura(long id) throws Exception {
        Facturas.getFacturas().anular(id);
    }

    public void restaurarFactura(long id) throws Exception {
        Facturas.getFacturas().restaurar(id);
    }

    public long rectificarFactura(long facturaId, LocalDate fecha) throws Exception {
        return Facturas.getFacturas().rectificar(facturaId, fecha);
    }

    public int numeroDeLineasFactura(long id) throws Exception {
        return Facturas.getFacturas().numeroDeLineas(id);
    }

    public FacturacionMensual.Resultado generarFacturasMensuales(Cliente cliente, int anio, int mesInicio,
            int mesFin, Serie serie, FacturacionMensual.ModoDia diaMode, int diaFijo, TipoIva iva,
            TipoRetencion retencion, List<FacturacionMensual.LineaPlantilla> plantillas,
            boolean generarDuplicados, boolean usarHuecos) throws Exception {
        return facturacionMensual.generar(cliente, anio, mesInicio, mesFin, serie, diaMode, diaFijo,
                iva, retencion, plantillas, generarDuplicados, usarHuecos);
    }

    public List<String> mensualesDuplicadas(Cliente cliente, int anio, int mesInicio, int mesFin)
            throws Exception {
        return facturacionMensual.detectarDuplicados(cliente, anio, mesInicio, mesFin);
    }

    public CopiaSeguridad getCopiaSeguridad() {
        return copiaSeguridad;
    }
}
