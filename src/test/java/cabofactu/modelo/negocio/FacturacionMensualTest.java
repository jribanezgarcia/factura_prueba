package cabofactu.modelo.negocio;

import cabofactu.modelo.negocio.sqlite.Conexion;
import cabofactu.modelo.dominio.Cliente;
import cabofactu.modelo.dominio.VersionFactura;
import cabofactu.modelo.dominio.LineaFactura;
import cabofactu.modelo.dominio.Serie;
import cabofactu.modelo.dominio.TipoIva;
import cabofactu.modelo.dominio.TipoRetencion;
import cabofactu.modelo.negocio.sqlite.ClienteDAO;
import cabofactu.modelo.negocio.sqlite.FacturaDAO;
import cabofactu.modelo.negocio.sqlite.LineaFacturaDAO;
import cabofactu.modelo.negocio.sqlite.NumeroDisponibleDAO;
import cabofactu.modelo.negocio.sqlite.SerieDAO;
import cabofactu.modelo.negocio.sqlite.VersionFacturaDAO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import cabofactu.modelo.dominio.DatosPago;

class FacturacionMensualTest {

    @TempDir
    Path tempDir;

    private SerieDAO serieDAO;
    private ClienteDAO clienteDAO;
    private FacturaDAO facturaDAO;
    private VersionFacturaDAO versionFacturaDAO;
    private LineaFacturaDAO lineaFacturaDAO;
    private Numeracion numeracion;
    private Facturas facturas;
    private FacturacionMensual service;

    @BeforeEach
    void setUp() throws Exception {
        Conexion.setCarpetaRaiz(tempDir);
        Conexion.cerrarConexion();
        Conexion.establecerConexion();
        serieDAO = new SerieDAO();
        clienteDAO = new ClienteDAO();
        facturaDAO = new FacturaDAO();
        versionFacturaDAO = new VersionFacturaDAO();
        lineaFacturaDAO = new LineaFacturaDAO();
        NumeroDisponibleDAO numeroDisponibleDAO = new NumeroDisponibleDAO();
        this.numeracion = new Numeracion(serieDAO, numeroDisponibleDAO, Clock.systemDefaultZone());
        Versiones versiones = new Versiones(versionFacturaDAO, lineaFacturaDAO, Clock.systemDefaultZone());
        facturas = new Facturas(facturaDAO, serieDAO, clienteDAO,
                versionFacturaDAO, lineaFacturaDAO, versiones, this.numeracion, numeroDisponibleDAO, Clock.systemDefaultZone());
        service = new FacturacionMensual(facturas, facturaDAO, this.numeracion);
    }

    @AfterEach
    void tearDown() {
        Conexion.cerrarConexion();
    }

    private Serie serieC() throws SQLException {
        Serie s = new Serie();
        s.setCodigo("C");
        s.setDescripcion("Cocinas");
        s.setEsRectificativa(false);
        s.setSiguienteCorrelativo(1);
        s.setReutilizarAnulados(false);
        s.setSufijoFecha(Serie.SufijoFecha.MES);
        s.setId(serieDAO.insertar(s, LocalDate.now().getYear()));
        return s;
    }

    private Cliente clientePaco() throws SQLException {
        Cliente c = new Cliente();
        c.setNombre("Paco");
        c.setNif("12345678Z");
        c.setDireccion("Calle Prueba 1");
        c.setCp("28001");
        c.setLocalidad("Madrid");
        c.setProvincia("Madrid");
        c.setId(clienteDAO.insertar(c));
        return c;
    }

    private TipoIva iva21() {
        TipoIva iva = new TipoIva();
        iva.setId(1L);
        iva.setNombre("IVA 21%");
        iva.setPorcentaje(21);
        return iva;
    }

    private FacturacionMensual.LineaPlantilla plantilla(String descripcion, String precio, boolean anadirMes) {
        return new FacturacionMensual.LineaPlantilla(1, descripcion,
                new BigDecimal(precio), anadirMes);
    }

    @Test
    void generaDoceFacturasParaTodoElAnio() throws Exception {
        Serie serie = serieC();
        Cliente cliente = clientePaco();
        TipoIva iva = iva21();

        FacturacionMensual.Resultado r = service.generar(cliente, 2026, 1, 12, serie, 15,
                iva, null, List.of(plantilla("contabilidad y laboral", "60.00", true)));

        assertEquals(12, r.getGeneradas());
        assertTrue(r.getMesesOmitidos().isEmpty());
        List<VersionFactura> versiones = versionFacturaDAO.getVersionesPorCliente(cliente.getId());
        assertEquals(12, versiones.size());
    }

    @Test
    void omiteMesesYaFacturados() throws Exception {
        Serie serie = serieC();
        Cliente cliente = clientePaco();
        TipoIva iva = iva21();

        service.generar(cliente, 2026, 1, 3, serie, 15, iva, null,
                List.of(plantilla("servicios", "60.00", true)));

        FacturacionMensual.Resultado r = service.generar(cliente, 2026, 1, 5, serie, 15, iva, null,
                List.of(plantilla("servicios", "60.00", true)));

        assertEquals(2, r.getGeneradas());
        assertEquals(List.of("enero", "febrero", "marzo"), r.getMesesOmitidos());
    }

    @Test
    void generaDuplicadosSiSeIndicaExplicitamente() throws Exception {
        Serie serie = serieC();
        Cliente cliente = clientePaco();
        TipoIva iva = iva21();

        service.generar(cliente, 2026, 1, 3, serie, 15, iva, null,
                List.of(plantilla("servicios", "60.00", true)));

        FacturacionMensual.Resultado r = service.generar(cliente, 2026, 1, 5, serie,
                FacturacionMensual.ModoDia.FIJO, 15, iva, null,
                List.of(plantilla("servicios", "60.00", true)), true, false);

        assertEquals(5, r.getGeneradas());
        assertTrue(r.getMesesOmitidos().isEmpty());
        assertEquals(8, versionFacturaDAO.getVersionesPorCliente(cliente.getId()).size());
    }

    @Test
    void detectaDuplicadosCorrectamente() throws Exception {
        Serie serie = serieC();
        Cliente cliente = clientePaco();
        TipoIva iva = iva21();

        service.generar(cliente, 2026, 2, 2, serie, 15, iva, null,
                List.of(plantilla("servicios", "60.00", true)));

        List<String> duplicados = service.detectarDuplicados(cliente, 2026, 1, 3);
        assertEquals(List.of("febrero"), duplicados);
    }

    @Test
    void ajustaDiaAlUltimoDiaValido() throws Exception {
        Serie serie = serieC();
        Cliente cliente = clientePaco();
        TipoIva iva = iva21();

        FacturacionMensual.Resultado r = service.generar(cliente, 2026, 1, 3, serie, 31, iva, null,
                List.of(plantilla("servicios", "60.00", false)));

        assertEquals(3, r.getGeneradas());
        List<VersionFactura> versiones = versionFacturaDAO.getVersionesPorCliente(cliente.getId());
        assertEquals(LocalDate.of(2026, 1, 31), versiones.get(0).getFechaFactura());
        assertEquals(LocalDate.of(2026, 2, 28), versiones.get(1).getFechaFactura());
        assertEquals(LocalDate.of(2026, 3, 31), versiones.get(2).getFechaFactura());
    }

    @Test
    void modoPrimerDiaDelMes() throws Exception {
        Serie serie = serieC();
        Cliente cliente = clientePaco();
        TipoIva iva = iva21();

        service.generar(cliente, 2026, 1, 3, serie,
                FacturacionMensual.ModoDia.PRIMER_DIA, 31, iva, null,
                List.of(plantilla("servicios", "60.00", false)), true, false);

        List<VersionFactura> versiones = versionFacturaDAO.getVersionesPorCliente(cliente.getId());
        assertEquals(LocalDate.of(2026, 1, 1), versiones.get(0).getFechaFactura());
        assertEquals(LocalDate.of(2026, 2, 1), versiones.get(1).getFechaFactura());
        assertEquals(LocalDate.of(2026, 3, 1), versiones.get(2).getFechaFactura());
    }

    @Test
    void modoUltimoDiaDelMes() throws Exception {
        Serie serie = serieC();
        Cliente cliente = clientePaco();
        TipoIva iva = iva21();

        service.generar(cliente, 2026, 1, 3, serie,
                FacturacionMensual.ModoDia.ULTIMO_DIA, 1, iva, null,
                List.of(plantilla("servicios", "60.00", false)), true, false);

        List<VersionFactura> versiones = versionFacturaDAO.getVersionesPorCliente(cliente.getId());
        assertEquals(LocalDate.of(2026, 1, 31), versiones.get(0).getFechaFactura());
        assertEquals(LocalDate.of(2026, 2, 28), versiones.get(1).getFechaFactura());
        assertEquals(LocalDate.of(2026, 3, 31), versiones.get(2).getFechaFactura());
    }

    @Test
    void aplicaIvaYRetencionEnTotales() throws Exception {
        Serie serie = serieC();
        Cliente cliente = clientePaco();
        TipoIva iva = iva21();
        TipoRetencion retencion = new TipoRetencion();
        retencion.setNombre("IRPF 15%");
        retencion.setPorcentaje(15);

        service.generar(cliente, 2026, 1, 1, serie, 15, iva, retencion,
                List.of(plantilla("servicios", "100.00", false)));

        VersionFactura v = versionFacturaDAO.getVersionesPorCliente(cliente.getId()).get(0);
        assertEquals(0, new BigDecimal("21.00").compareTo(v.getIvaTotal()));
        assertEquals(0, new BigDecimal("15.00").compareTo(v.getImporteRetencion()));
        assertEquals(0, new BigDecimal("106.00").compareTo(v.getTotal()));
    }

    @Test
    void descripcionIncluyeNombreDelMes() throws Exception {
        Serie serie = serieC();
        Cliente cliente = clientePaco();
        TipoIva iva = iva21();

        service.generar(cliente, 2026, 1, 2, serie, 15, iva, null,
                List.of(plantilla("contabilidad y laboral", "60.00", true)));

        List<VersionFactura> versiones = versionFacturaDAO.getVersionesPorCliente(cliente.getId());
        List<LineaFactura> lineasEnero = lineaFacturaDAO.getLineas(versiones.get(0).getId());
        List<LineaFactura> lineasFebrero = lineaFacturaDAO.getLineas(versiones.get(1).getId());
        assertEquals("contabilidad y laboral - mes de enero", lineasEnero.get(0).getDescripcion());
        assertEquals("contabilidad y laboral - mes de febrero", lineasFebrero.get(0).getDescripcion());
    }

    @Test
    void rollbackSiUnaFacturaFalla() throws Exception {
        Serie serie = serieC();
        Cliente cliente = clientePaco();
        TipoIva iva = iva21();

        NumeroDisponibleDAO numeroDisponibleDAO = new NumeroDisponibleDAO();
        Facturas serviceQueFalla = new Facturas(facturaDAO, serieDAO, clienteDAO,
                versionFacturaDAO, lineaFacturaDAO, new Versiones(versionFacturaDAO, lineaFacturaDAO, Clock.systemDefaultZone()),
                new Numeracion(serieDAO, numeroDisponibleDAO, Clock.systemDefaultZone()), numeroDisponibleDAO, Clock.systemDefaultZone()) {
            private int llamadas = 0;

            @Override
            long crearFacturaSinTransaccion(Serie s, LocalDate fecha, Cliente c, List<LineaFactura> lineas,
                                            int descuento, String observaciones, String referencia,
                                            Integer correlativoPedido, DatosPago datosPago,
                                            TipoRetencion retencion) throws ValidacionException {
                llamadas++;
                if (llamadas == 2) {
                    throw new ValidacionException("Fallo simulado en la segunda factura");
                }
                return super.crearFacturaSinTransaccion(s, fecha, c, lineas, descuento, observaciones,
                        referencia, correlativoPedido, datosPago, retencion);
            }
        };
        FacturacionMensual servicioConFallo = new FacturacionMensual(
                serviceQueFalla, facturaDAO, new Numeracion(serieDAO, numeroDisponibleDAO, Clock.systemDefaultZone()));

        assertThrows(ValidacionException.class, () -> servicioConFallo.generar(cliente, 2026, 1, 3, serie, 15,
                iva, null, List.of(plantilla("servicios", "60.00", false))));

        List<VersionFactura> versiones = versionFacturaDAO.getVersionesPorCliente(cliente.getId());
        assertEquals(0, versiones.size());
    }

    @Test
    void rellenaHuecosAlGenerarMensual() throws Exception {
        Serie serie = serieC();
        Cliente cliente = clientePaco();
        TipoIva iva = iva21();

        service.generar(cliente, 2026, 1, 3, serie, 15, iva, null,
                List.of(plantilla("servicios", "60.00", false)));

        List<VersionFactura> iniciales = versionFacturaDAO.getVersionesPorCliente(cliente.getId());
        facturas.borrarFactura(iniciales.get(0).getFacturaId());

        FacturacionMensual.Resultado r = service.generar(cliente, 2026, 4, 6, serie,
                FacturacionMensual.ModoDia.FIJO, 15, iva, null,
                List.of(plantilla("servicios", "60.00", false)), false, true);

        assertEquals(3, r.getGeneradas());
        List<VersionFactura> versiones = versionFacturaDAO.getVersionesPorCliente(cliente.getId());
        VersionFactura abril = versiones.stream()
                .filter(v -> v.getFechaFactura().equals(LocalDate.of(2026, 4, 15)))
                .findFirst().orElseThrow();
        assertEquals(1, facturas.factura(abril.getFacturaId()).getCorrelativo());
    }

    @Test
    void noGeneraConClienteSinCodigoPostal() throws Exception {
        Serie serie = serieC();
        Cliente cliente = clientePaco();
        cliente.setCp("");
        TipoIva iva = iva21();

        ValidacionException e = assertThrows(ValidacionException.class, () -> service.generar(cliente, 2026, 1, 3,
                serie, 15, iva, null, List.of(plantilla("servicios", "60.00", true))));
        assertEquals("El código postal es obligatorio.", e.getMessage());
        List<VersionFactura> versiones = versionFacturaDAO.getVersionesPorCliente(cliente.getId());
        assertEquals(0, versiones.size());
    }

    private LineaFactura linea(String precio) {
        LineaFactura l = new LineaFactura();
        l.setCantidad(1);
        l.setPrecioUnitario(new BigDecimal(precio));
        l.setTotalBase(Calculos.totalLinea(l.getPrecioUnitario(), 1));
        l.setTipoIvaId(1L);
        l.setIvaNombre("IVA 21%");
        l.setIvaPorcentaje(21);
        l.setIvaImporte(Calculos.ivaDeBase(l.getTotalBase(), 21));
        return l;
    }
}
