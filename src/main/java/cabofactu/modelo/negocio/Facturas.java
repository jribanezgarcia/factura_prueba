package cabofactu.modelo.negocio;

import cabofactu.modelo.negocio.sqlite.Conexion;
import cabofactu.modelo.dominio.Cliente;
import cabofactu.modelo.dominio.DatosPago;
import cabofactu.modelo.dominio.EstadoFactura;
import cabofactu.modelo.dominio.Factura;
import cabofactu.modelo.dominio.VersionFactura;
import cabofactu.modelo.dominio.LineaFactura;
import cabofactu.modelo.dominio.Serie;
import cabofactu.modelo.dominio.TipoRetencion;
import cabofactu.modelo.negocio.sqlite.FacturaDAO;
import cabofactu.modelo.negocio.sqlite.LineaFacturaDAO;
import cabofactu.modelo.negocio.sqlite.NumeroDisponibleDAO;
import cabofactu.modelo.negocio.sqlite.SerieDAO;
import cabofactu.modelo.negocio.sqlite.VersionFacturaDAO;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

/**
 * Orquestacion del guardado de facturas: transacciones, numeracion, cliente
 * maestro y versionado.
 */
public class Facturas {

    private final FacturaDAO facturaDAO;
    private final SerieDAO serieDAO;
    private final VersionFacturaDAO versionFacturaDAO;
    private final LineaFacturaDAO lineaFacturaDAO;
    private final Versiones versiones;
    private final Numeracion numeracion;
    private final NumeroDisponibleDAO numeroDisponibleDAO;
    private final Clock clock;

    public Facturas(FacturaDAO facturaDAO, SerieDAO serieDAO,
                          VersionFacturaDAO versionFacturaDAO,
                          LineaFacturaDAO lineaFacturaDAO, Versiones versiones,
                          Numeracion numeracion, NumeroDisponibleDAO numeroDisponibleDAO,
                          Clock clock) {
        this.facturaDAO = facturaDAO;
        this.serieDAO = serieDAO;
        this.versionFacturaDAO = versionFacturaDAO;
        this.lineaFacturaDAO = lineaFacturaDAO;
        this.versiones = versiones;
        this.numeracion = numeracion;
        this.numeroDisponibleDAO = numeroDisponibleDAO;
        this.clock = clock;
    }

    /**
     * Crea una factura nueva: transaccion que inserta factura + version v1 +
     * lineas, consume el correlativo solo si todo sale bien.
     */
    public long crearFactura(Serie serie, LocalDate fecha, Cliente cliente, List<LineaFactura> lineas,
                             int descuento, String observaciones, String referencia)
            throws Exception {
        return crearFactura(serie, fecha, cliente, lineas, descuento, observaciones, referencia, null, null);
    }

    /**
     * Igual que crearFactura pero acepta un correlativo escrito manualmente
     * (campo numero editable en creacion). Si llega null se propone el
     * siguiente correlativo de la serie.
     */
    public long crearFactura(Serie serie, LocalDate fecha, Cliente cliente, List<LineaFactura> lineas,
                             int descuento, String observaciones, String referencia, Integer correlativoPedido)
            throws Exception {
        return crearFactura(serie, fecha, cliente, lineas, descuento, observaciones, referencia,
                correlativoPedido, null);
    }

    public long crearFactura(Serie serie, LocalDate fecha, Cliente cliente, List<LineaFactura> lineas,
                             int descuento, String observaciones, String referencia,
                             Integer correlativoPedido, DatosPago datosPago)
            throws Exception {
        return crearFactura(serie, fecha, cliente, lineas, descuento, observaciones, referencia,
                correlativoPedido, datosPago, null);
    }

    public long crearFactura(Serie serie, LocalDate fecha, Cliente cliente, List<LineaFactura> lineas,
                             int descuento, String observaciones, String referencia,
                             Integer correlativoPedido, DatosPago datosPago, TipoRetencion retencion)
            throws Exception {
        Conexion.iniciarTransaccion();
        try {
            long facturaId = crearFacturaSinTransaccion(serie, fecha, cliente, lineas, descuento,
                    observaciones, referencia, correlativoPedido, datosPago, retencion);
            Conexion.confirmar();
            return facturaId;
        } catch (Exception e) {
            Conexion.deshacer();
            throw e;
        } finally {
            Conexion.terminarTransaccion();
        }
    }

    /**
     * Crea una factura nueva sin gestionar la transaccion. El llamador debe
     * envolver la llamada en iniciarTransaccion/confirmar/deshacer cuando genere varias
     * facturas de una sola vez.
     */
    long crearFacturaSinTransaccion(Serie serie, LocalDate fecha, Cliente cliente, List<LineaFactura> lineas,
                                    int descuento, String observaciones, String referencia,
                                     Integer correlativoPedido, DatosPago datosPago, TipoRetencion retencion)
            throws Exception {
        ValidacionCliente.comprobar(cliente);
        validar(lineas, descuento);
        if (correlativoPedido != null && correlativoPedido < 1) {
            throw new ValidacionException("El correlativo debe ser al menos 1");
        }

        int correlativo = correlativoPedido != null
                ? correlativoPedido
                : numeracion.siguienteCorrelativo(serie, fecha);
        if (numeracion.correlativoOcupadoPorActiva(serie, correlativo, fecha)) {
            throw new ValidacionException(
                    "El correlativo " + correlativo + " ya esta ocupado por una factura activa de la serie " + serie.getCodigo());
        }
        numeroDisponibleDAO.eliminar(serie.getId(), fecha.getYear(), correlativo);

        if (cliente != null && cliente.getId() == null && !isVacio(cliente)) {
            long clienteId = Clientes.getClientes().alta(cliente);
            cliente.setId(clienteId);
        }

        long facturaId = facturaDAO.insertar(serie.getId(), correlativo,
                cliente == null ? null : cliente.getId());

        String numero = numeracion.formarNumero(serie, correlativo, fecha);
        versiones.crearVersion(facturaId, fecha, numero, EstadoFactura.EMITIDA,
                descuento, observaciones, referencia, cliente, lineas, datosPago, retencion);

        serieDAO.actualizarSiguiente(serie.getId(), Math.max(serie.getSiguienteCorrelativo(), correlativo + 1));
        int nuevoAnio = Math.max(serieDAO.getSiguiente(serie.getId(), fecha.getYear()), correlativo + 1);
        serieDAO.actualizarSiguiente(serie.getId(), fecha.getYear(), nuevoAnio);
        return facturaId;
    }

    public VersionFactura guardarEditada(long facturaId, Long versionAbiertaId, LocalDate fecha, Cliente cliente,
                                         List<LineaFactura> lineas, int descuento,
                                         String observaciones, String referencia, DatosPago datosPago)
            throws Exception {
        return guardarEditada(facturaId, versionAbiertaId, fecha, cliente, lineas, descuento,
                observaciones, referencia, datosPago, false);
    }

    /**
     * Guarda cambios sobre una factura existente. Si la version abierta es la
     * actual (ultima), se sobrescribe en su lugar; con comoNuevaVersion se crea
     * siempre una version nueva (vN+1) dejando la anterior intacta. Si se edito
     * una version anterior, se crea una nueva version con esos datos
     * (snapshot). El correlativo queda fijo; el mes del numero sigue a la fecha.
     */
    public VersionFactura guardarEditada(long facturaId, Long versionAbiertaId, LocalDate fecha, Cliente cliente,
                                         List<LineaFactura> lineas, int descuento,
String observaciones, String referencia, DatosPago datosPago,
                                          boolean comoNuevaVersion)
            throws Exception {
        return guardarEditada(facturaId, versionAbiertaId, fecha, cliente, lineas, descuento,
                observaciones, referencia, datosPago, comoNuevaVersion, null);
    }

    public VersionFactura guardarEditada(long facturaId, Long versionAbiertaId, LocalDate fecha, Cliente cliente,
                                         List<LineaFactura> lineas, int descuento,
String observaciones, String referencia, DatosPago datosPago,
                                           boolean comoNuevaVersion, TipoRetencion retencion)
            throws Exception {
        ValidacionCliente.comprobar(cliente);
        validar(lineas, descuento);

        Conexion.iniciarTransaccion();
        try {
            Factura factura = facturaDAO.getById(facturaId);
            if (factura == null) {
                throw new ValidacionException("La factura no existe");
            }
            EstadoFactura estado = estadoActual(facturaId);
            if (estado != EstadoFactura.EMITIDA) {
                throw new ValidacionException("Solo se pueden editar facturas en estado Emitida");
            }
            Serie serie = serieDAO.getById(factura.getSerieId());

            if (cliente != null && cliente.getId() == null && !isVacio(cliente)) {
                long clienteId = Clientes.getClientes().alta(cliente);
                cliente.setId(clienteId);
            }
            facturaDAO.actualizarCliente(facturaId, cliente == null ? null : cliente.getId());

            String numero = numeracion.formarNumero(serie, factura.getCorrelativo(), fecha);
            VersionFactura ultima = versionFacturaDAO.ultimaVersion(facturaId);
            VersionFactura guardada;
            if (!comoNuevaVersion && versionAbiertaId != null && ultima != null
                    && versionAbiertaId.longValue() == ultima.getId().longValue()) {
                guardada = versiones.sobrescribirVersion(ultima.getId(), fecha, numero, EstadoFactura.EMITIDA,
                        descuento, observaciones, referencia, cliente, lineas, datosPago, retencion);
            } else {
                guardada = versiones.crearVersion(facturaId, fecha, numero, EstadoFactura.EMITIDA,
                        descuento, observaciones, referencia, cliente, lineas, datosPago, retencion);
            }
            Conexion.confirmar();
            return guardada;
        } catch (Exception e) {
            Conexion.deshacer();
            throw e;
        } finally {
            Conexion.terminarTransaccion();
        }
    }

    public EstadoFactura estadoActual(long facturaId) {
        VersionFactura v = versionFacturaDAO.ultimaVersion(facturaId);
        return v == null ? null : v.getEstado();
    }

    public Serie serieDeFactura(long facturaId) {
        Factura f = facturaDAO.getById(facturaId);
        return f == null ? null : serieDAO.getById(f.getSerieId());
    }

    public Factura factura(long facturaId) {
        return facturaDAO.getById(facturaId);
    }

    public ResumenBorrado resumenBorrado(long facturaId) {
        int versiones = 0;
        int lineas = 0;
        for (VersionFactura v : versionFacturaDAO.getVersiones(facturaId)) {
            versiones++;
            lineas += lineaFacturaDAO.getLineas(v.getId()).size();
        }
        return new ResumenBorrado(versiones, lineas);
    }

    public void borrarFactura(long facturaId) throws ValidacionException {
        Factura f = facturaDAO.getById(facturaId);
        if (f == null) {
            throw new ValidacionException("La factura no existe");
        }
        Serie serie = serieDAO.getById(f.getSerieId());
        VersionFactura ultima = versionFacturaDAO.ultimaVersion(facturaId);
        int anio = ultima != null && ultima.getFechaFactura() != null
                ? ultima.getFechaFactura().getYear()
                : LocalDate.now(clock).getYear();

        Conexion.iniciarTransaccion();
        try {
            for (VersionFactura v : versionFacturaDAO.getVersiones(facturaId)) {
                lineaFacturaDAO.eliminarPorVersion(v.getId());
            }
            versionFacturaDAO.eliminarPorFactura(facturaId);
            facturaDAO.eliminar(facturaId);
            numeroDisponibleDAO.insertar(serie.getId(), anio, f.getCorrelativo());
            Conexion.confirmar();
        } catch (RuntimeException e) {
            Conexion.deshacer();
            throw e;
        } finally {
            Conexion.terminarTransaccion();
        }
    }

    public record ResumenBorrado(int versiones, int lineas) {
    }

    public Cliente cliente(long clienteId) throws Exception {
        if (clienteId == 0) {
            return null;
        }
        return Clientes.getClientes().buscar(clienteId);
    }

    /**
     * Abre una version concreta con sus lineas y el cliente maestro (si existe).
     */
    public VersionCompleta abrirVersion(long versionId) throws Exception {
        VersionFactura v = versionFacturaDAO.getById(versionId);
        if (v == null) {
            return null;
        }
        List<LineaFactura> lineas = lineaFacturaDAO.getLineas(versionId);
        Factura f = facturaDAO.getById(v.getFacturaId());
        Cliente cliente = null;
        if (f != null && f.getClienteId() != null) {
            cliente = Clientes.getClientes().buscar(f.getClienteId());
        }
        return new VersionCompleta(f, v, lineas, cliente);
    }

    public int maxVersion(long facturaId) {
        return versionFacturaDAO.maxVersion(facturaId);
    }

    private void validar(List<LineaFactura> lineas, int descuento) throws ValidacionException {
        if (lineas == null || lineas.isEmpty()) {
            throw new ValidacionException("La factura debe tener al menos una linea");
        }
        for (LineaFactura l : lineas) {
            if (l.getCantidad() < 1) {
                throw new ValidacionException("La cantidad de cada linea debe ser al menos 1");
            }
            if (nz(l.getPrecioUnitario()).signum() < 0 || nz(l.getTotalBase()).signum() < 0) {
                throw new ValidacionException("Los importes no pueden ser negativos");
            }
            if (l.getTipoIvaId() == null) {
                throw new ValidacionException("Cada linea debe tener un tipo de IVA");
            }
        }
        if (descuento < 0 || descuento > 100) {
            throw new ValidacionException("El descuento debe estar entre 0 y 100");
        }
    }

    private boolean isVacio(Cliente c) {
        return c == null || isBlank(c.getNombre()) && isBlank(c.getNif()) && isBlank(c.getDireccion());
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    /**
     * Retencion de una version ya resuelta desde su snapshot congelado
     * (id, nombre y porcentaje guardados al emitir). El snapshot manda
     * siempre sobre el catalogo actual: una factura antigua muestra el
     * porcentaje que tenia al emitirse aunque el tipo se haya cambiado
     * despues. No consulta ningun DAO.
     */
    public static TipoRetencion retencionDeVersion(VersionFactura v) throws Exception {
        if (v.getTipoRetencionId() == null) {
            return null;
        }
        String nombre = v.getTipoRetencionNombre();
        if (nombre == null || nombre.isBlank()) {
            nombre = "Retención";
        }
        int porcentaje = 0;
        if (v.getTipoRetencionPorcentaje() != null) {
            porcentaje = v.getTipoRetencionPorcentaje();
        }
        TipoRetencion t = new TipoRetencion(nombre, porcentaje);
        t.setId(v.getTipoRetencionId());
        return t;
    }

    public record VersionCompleta(Factura factura, VersionFactura version, List<LineaFactura> lineas, Cliente cliente) {
    }
}
