package cabofactu.modelo.negocio;

import cabofactu.modelo.negocio.sqlite.Conexion;
import cabofactu.modelo.dominio.Cliente;
import cabofactu.modelo.dominio.DatosPago;
import cabofactu.modelo.dominio.EstadoFactura;
import cabofactu.modelo.dominio.VersionFactura;
import cabofactu.modelo.dominio.LineaFactura;
import cabofactu.modelo.dominio.Serie;
import cabofactu.modelo.dominio.FormatoNumero;
import cabofactu.modelo.dominio.TipoRetencion;
import cabofactu.modelo.negocio.sqlite.FacturaDAO;
import cabofactu.modelo.negocio.sqlite.LineaFacturaDAO;
import cabofactu.modelo.negocio.sqlite.VersionFacturaDAO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FacturasTest {

    @TempDir
    Path tempDir;

    private Versiones versionesNegocio;
    private LineaFacturaDAO lineaFacturaDAO;
    private FacturaDAO facturaDAO;
    private Facturas facturas;

    @BeforeEach
    void setUp() throws Exception {
        Conexion.setCarpetaRaiz(tempDir);
        Conexion.cerrarConexion();
        Conexion.establecerConexion();
        facturaDAO = new FacturaDAO();
        VersionFacturaDAO versionFacturaDAO = new VersionFacturaDAO();
        lineaFacturaDAO = new LineaFacturaDAO();
        versionesNegocio = new Versiones(versionFacturaDAO, lineaFacturaDAO, Clock.systemDefaultZone());
        facturas = new Facturas(facturaDAO,
                versionFacturaDAO, lineaFacturaDAO, versionesNegocio, Clock.systemDefaultZone());
    }

    @AfterEach
    void tearDown() {
        Conexion.cerrarConexion();
    }

    private Serie serieC() throws Exception {
        Serie s = new Serie("C", "Cocinas", FormatoNumero.MES, false);
        s.setId(Series.getSeries().alta(s));
        return s;
    }

    private Cliente clientePrueba() throws Exception {
        return new Cliente("Cliente Prueba", "12345678Z", "Calle Prueba 1", "28001", "Madrid", "Madrid");
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

    @Test
    void guardarEditadaSobrescribeLaUltimaVersion() throws Exception {
        Serie c = serieC();
        LocalDate fecha = LocalDate.of(2026, 8, 11);
        long facturaId = facturas.crearFactura(c, fecha, clientePrueba(), List.of(linea("100.00")), 0, null, null);

        VersionFactura v1 = versionesNegocio.ultimaVersion(facturaId);
        assertEquals(1, versionesNegocio.versionesDeFactura(facturaId).size());
        assertEquals(new BigDecimal("121.00"), v1.getTotal());

        VersionFactura v = facturas.guardarEditada(facturaId, v1.getId(), fecha, clientePrueba(),
                List.of(linea("200.00")), 0, "nueva observacion", null, null);

        assertEquals(v1.getId(), v.getId());
        List<VersionFactura> versiones = versionesNegocio.versionesDeFactura(facturaId);
        assertEquals(1, versiones.size());
        assertEquals(1, versiones.get(0).getVersionNum());
        assertEquals(new BigDecimal("242.00"), versiones.get(0).getTotal());
        assertEquals("nueva observacion", versiones.get(0).getObservaciones());

        List<LineaFactura> lineas = lineaFacturaDAO.getLineas(versiones.get(0).getId());
        assertEquals(1, lineas.size());
        assertEquals(new BigDecimal("200.00"), lineas.get(0).getPrecioUnitario());
    }

    @Test
    void guardarEditadaDesdeVersionAnteriorCreaNuevaVersion() throws Exception {
        Serie c = serieC();
        LocalDate fecha = LocalDate.of(2026, 8, 11);
        long facturaId = facturas.crearFactura(c, fecha, clientePrueba(), List.of(linea("100.00")), 0, null, null);

        VersionFactura v1 = versionesNegocio.ultimaVersion(facturaId);
        versionesNegocio.crearVersion(facturaId, fecha, v1.getNumero(), EstadoFactura.EMITIDA,
                0, null, null, null, List.of(linea("100.00")));

        facturas.guardarEditada(facturaId, v1.getId(), fecha, clientePrueba(),
                List.of(linea("50.00")), 0, null, null, null);

        assertEquals(3, versionesNegocio.versionesDeFactura(facturaId).size());
    }

    @Test
    void guardarComoNuevaVersionConservaLaAnterior() throws Exception {
        Serie c = serieC();
        LocalDate fecha = LocalDate.of(2026, 8, 11);
        long facturaId = facturas.crearFactura(c, fecha, clientePrueba(), List.of(linea("100.00")), 0, null, null);

        VersionFactura v1 = versionesNegocio.ultimaVersion(facturaId);
        assertEquals(new BigDecimal("121.00"), v1.getTotal());

        VersionFactura v2 = facturas.guardarEditada(facturaId, v1.getId(), fecha, clientePrueba(),
                List.of(linea("300.00")), 0, "modificacion", null, null, true);

        assertEquals(2, v2.getVersionNum());
        List<VersionFactura> versiones = versionesNegocio.versionesDeFactura(facturaId);
        assertEquals(2, versiones.size());
        VersionFactura primera = versiones.stream().filter(v -> v.getVersionNum() == 1).findFirst().orElseThrow();
        assertEquals(v1.getId(), primera.getId());
        assertEquals(new BigDecimal("121.00"), primera.getTotal());
        VersionFactura segunda = versiones.stream().filter(v -> v.getVersionNum() == 2).findFirst().orElseThrow();
        assertEquals(v2.getId(), segunda.getId());
        assertEquals(new BigDecimal("363.00"), segunda.getTotal());
        assertEquals("modificacion", segunda.getObservaciones());
    }

    @Test
    void guardaEmailClienteYDatosPagoEnLaVersion() throws Exception {
        Serie c = serieC();
        LocalDate fecha = LocalDate.of(2026, 8, 21);
        Cliente cli = new Cliente("MARIA MARTAGON AVALOS", "49122168X", "Calle Prueba 1",
                "28001", "Madrid", "Madrid");
        cli.setEmail("maria.martagon@correo.es");
        DatosPago dp = new DatosPago("Transferencia", LocalDate.of(2026, 9, 14), "AURORA");

        long facturaId = facturas.crearFactura(c, fecha, cli, List.of(linea("100.00")),
                0, null, null, null, dp);

        VersionFactura v = versionesNegocio.ultimaVersion(facturaId);
        assertEquals("maria.martagon@correo.es", v.getCliEmail());
        assertEquals("Transferencia", v.getFormaPago());
        assertEquals(LocalDate.of(2026, 9, 14), v.getVencimiento());
        assertEquals("AURORA", v.getRealizadaPor());

        Cliente guardado = facturas.cliente(cli.getId());
        assertEquals("maria.martagon@correo.es", guardado.getEmail());
    }

    @Test
    void crearFacturaConRetencionGuardaImporteYNombre() throws Exception {
        Serie c = serieC();
        LocalDate fecha = LocalDate.of(2026, 8, 21);
        TipoRetencion irpf = new TipoRetencion("IRPF 15%", 15);

        long facturaId = facturas.crearFactura(c, fecha, clientePrueba(), List.of(linea("1000.00")),
                0, null, null, null, null, irpf);

        VersionFactura v = versionesNegocio.ultimaVersion(facturaId);
        assertEquals(0, new BigDecimal("150.00").compareTo(v.getImporteRetencion()));
        assertEquals("IRPF 15%", v.getTipoRetencionNombre());
        assertEquals(15, v.getTipoRetencionPorcentaje());
        assertEquals(0, new BigDecimal("1060.00").compareTo(v.getTotal()));
    }

    @Test
    void anularConservaDatosPagoYEmail() throws Exception {
        Serie c = serieC();
        LocalDate fecha = LocalDate.of(2026, 8, 21);
        Cliente cli = new Cliente("CLIENTE PRUEBA", "12345678Z", "Calle Prueba 1", "28001", "Madrid", "Madrid");
        cli.setEmail("cliente@prueba.es");
        DatosPago dp = new DatosPago("Efectivo", null, "AURORA");

        long facturaId = facturas.crearFactura(c, fecha, cli, List.of(linea("100.00")),
                0, null, null, null, dp);
        Estados estados = new Estados(new FacturaDAO(),
                new VersionFacturaDAO(), lineaFacturaDAO, versionesNegocio, facturas);
        estados.anular(facturaId);

        VersionFactura v = versionesNegocio.ultimaVersion(facturaId);
        assertEquals(EstadoFactura.ANULADA, v.getEstado());
        assertEquals("Efectivo", v.getFormaPago());
        assertEquals("AURORA", v.getRealizadaPor());
        assertEquals("cliente@prueba.es", v.getCliEmail());
    }

    @Test
    void retencionDeVersionRespetaElSnapshotAunqueCambieElCatalogo() throws Exception {
        VersionFactura v = new VersionFactura();
        v.setTipoRetencionId(7L);
        v.setTipoRetencionNombre("IRPF profesional");
        v.setTipoRetencionPorcentaje(15);
        TipoRetencion t = Facturas.retencionDeVersion(v);
        assertEquals(7L, t.getId());
        assertEquals("IRPF profesional", t.getNombre());
        assertEquals(15, t.getPorcentaje());
    }

    @Test
    void retencionDeVersionSinTipoDevuelveNull() throws Exception {
        assertNull(Facturas.retencionDeVersion(new VersionFactura()));
    }

    @Test
    void borrarFacturaEliminaRegistrosYLiberaNumero() throws Exception {
        Serie c = serieC();
        LocalDate fecha = LocalDate.of(2026, 8, 21);
        facturas.crearFactura(c, fecha, clientePrueba(), List.of(linea("100.00")),
                0, null, null);
        long facturaId = facturas.crearFactura(c, fecha, clientePrueba(), List.of(linea("100.00")),
                0, null, null);
        facturas.crearFactura(c, fecha, clientePrueba(), List.of(linea("100.00")),
                0, null, null);
        int correlativo = facturas.factura(facturaId).getCorrelativo();

        Facturas.ResumenBorrado r = facturas.resumenBorrado(facturaId);
        assertEquals(1, r.versiones());
        assertEquals(1, r.lineas());

        facturas.borrarFactura(facturaId);

        assertNull(facturas.factura(facturaId));
        assertTrue(Series.getSeries().huecos(c, fecha).contains(correlativo));
    }

    @Test
    void crearVersionSellaFechaGuardadoConElReloj() throws Exception {
        Clock fijo = Clock.fixed(Instant.parse("2031-06-15T10:00:00Z"), ZoneId.of("Europe/Madrid"));
        Versiones conReloj = new Versiones(new VersionFacturaDAO(), lineaFacturaDAO, fijo);
        Serie c = serieC();
        LocalDate fecha = LocalDate.of(2031, 6, 15);
        long facturaId = facturas.crearFactura(c, fecha, clientePrueba(), List.of(linea("100.00")), 0, null, null);
        VersionFactura v = conReloj.crearVersion(facturaId, fecha, "C-2/6", EstadoFactura.EMITIDA,
                0, null, null, null, List.of(linea("100.00")));

        assertEquals(LocalDateTime.of(2031, 6, 15, 12, 0), v.getFechaGuardado());
    }

    @Test
    void noCreaFacturaSinCliente() throws Exception {
        Serie c = serieC();
        LocalDate fecha = LocalDate.of(2026, 8, 21);
        ValidacionException e = assertThrows(ValidacionException.class, () ->
                facturas.crearFactura(c, fecha, null, List.of(linea("100.00")), 0, null, null));
        assertEquals("Indique los datos del cliente.", e.getMessage());
        assertEquals(0, facturaDAO.contar());
    }
}
