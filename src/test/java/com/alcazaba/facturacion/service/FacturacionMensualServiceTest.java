package com.alcazaba.facturacion.service;

import com.alcazaba.facturacion.db.Database;
import com.alcazaba.facturacion.model.Cliente;
import com.alcazaba.facturacion.model.FacturaVersion;
import com.alcazaba.facturacion.model.LineaFactura;
import com.alcazaba.facturacion.model.Serie;
import com.alcazaba.facturacion.model.TipoIva;
import com.alcazaba.facturacion.model.TipoRetencion;
import com.alcazaba.facturacion.repository.ClienteRepository;
import com.alcazaba.facturacion.repository.FacturaRepository;
import com.alcazaba.facturacion.repository.LineaRepository;
import com.alcazaba.facturacion.repository.NumeroDisponibleRepository;
import com.alcazaba.facturacion.repository.SerieRepository;
import com.alcazaba.facturacion.repository.VersionRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FacturacionMensualServiceTest {

    @TempDir
    Path tempDir;

    private SerieRepository serieRepository;
    private ClienteRepository clienteRepository;
    private FacturaRepository facturaRepository;
    private VersionRepository versionRepository;
    private LineaRepository lineaRepository;
    private NumeroService numeroService;
    private FacturaService facturaService;
    private FacturacionMensualService service;

    @BeforeEach
    void setUp() throws Exception {
        Database.setDataDir(tempDir);
        Database.resetConnection();
        Database.getConnection();
        serieRepository = new SerieRepository();
        clienteRepository = new ClienteRepository();
        facturaRepository = new FacturaRepository();
        versionRepository = new VersionRepository();
        lineaRepository = new LineaRepository();
        NumeroDisponibleRepository numeroDisponibleRepository = new NumeroDisponibleRepository();
        this.numeroService = new NumeroService(serieRepository, numeroDisponibleRepository);
        VersionadoService versionadoService = new VersionadoService(versionRepository, lineaRepository);
        facturaService = new FacturaService(facturaRepository, serieRepository, clienteRepository,
                versionRepository, lineaRepository, versionadoService, this.numeroService, numeroDisponibleRepository);
        service = new FacturacionMensualService(facturaService, facturaRepository, this.numeroService);
    }

    @AfterEach
    void tearDown() {
        Database.resetConnection();
    }

    private Serie serieC() throws SQLException {
        Serie s = new Serie();
        s.setCodigo("C");
        s.setDescripcion("Cocinas");
        s.setEsRectificativa(false);
        s.setSiguienteCorrelativo(1);
        s.setReutilizarAnulados(false);
        s.setSufijoFecha(Serie.SufijoFecha.MES);
        s.setId(serieRepository.insertar(s));
        return s;
    }

    private Cliente clientePaco() throws SQLException {
        Cliente c = new Cliente();
        c.setNombre("Paco");
        c.setNif("12345678Z");
        c.setId(clienteRepository.insertar(c));
        return c;
    }

    private TipoIva iva21() {
        TipoIva iva = new TipoIva();
        iva.setId(1L);
        iva.setNombre("IVA 21%");
        iva.setPorcentaje(21);
        return iva;
    }

    private FacturacionMensualService.LineaPlantilla plantilla(String descripcion, String precio, boolean anadirMes) {
        return new FacturacionMensualService.LineaPlantilla(1, descripcion,
                new BigDecimal(precio), anadirMes);
    }

    @Test
    void generaDoceFacturasParaTodoElAnio() throws Exception {
        Serie serie = serieC();
        Cliente cliente = clientePaco();
        TipoIva iva = iva21();

        FacturacionMensualService.Resultado r = service.generar(cliente, 2026, 1, 12, serie, 15,
                iva, null, List.of(plantilla("contabilidad y laboral", "60.00", true)));

        assertEquals(12, r.getGeneradas());
        assertTrue(r.getMesesOmitidos().isEmpty());
        List<FacturaVersion> versiones = versionRepository.getVersionesPorCliente(cliente.getId());
        assertEquals(12, versiones.size());
    }

    @Test
    void omiteMesesYaFacturados() throws Exception {
        Serie serie = serieC();
        Cliente cliente = clientePaco();
        TipoIva iva = iva21();

        service.generar(cliente, 2026, 1, 3, serie, 15, iva, null,
                List.of(plantilla("servicios", "60.00", true)));

        FacturacionMensualService.Resultado r = service.generar(cliente, 2026, 1, 5, serie, 15, iva, null,
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

        FacturacionMensualService.Resultado r = service.generar(cliente, 2026, 1, 5, serie,
                FacturacionMensualService.DiaMode.FIJO, 15, iva, null,
                List.of(plantilla("servicios", "60.00", true)), true, false);

        assertEquals(5, r.getGeneradas());
        assertTrue(r.getMesesOmitidos().isEmpty());
        assertEquals(8, versionRepository.getVersionesPorCliente(cliente.getId()).size());
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

        FacturacionMensualService.Resultado r = service.generar(cliente, 2026, 1, 3, serie, 31, iva, null,
                List.of(plantilla("servicios", "60.00", false)));

        assertEquals(3, r.getGeneradas());
        List<FacturaVersion> versiones = versionRepository.getVersionesPorCliente(cliente.getId());
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
                FacturacionMensualService.DiaMode.PRIMER_DIA, 31, iva, null,
                List.of(plantilla("servicios", "60.00", false)), true, false);

        List<FacturaVersion> versiones = versionRepository.getVersionesPorCliente(cliente.getId());
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
                FacturacionMensualService.DiaMode.ULTIMO_DIA, 1, iva, null,
                List.of(plantilla("servicios", "60.00", false)), true, false);

        List<FacturaVersion> versiones = versionRepository.getVersionesPorCliente(cliente.getId());
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

        FacturaVersion v = versionRepository.getVersionesPorCliente(cliente.getId()).get(0);
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

        List<FacturaVersion> versiones = versionRepository.getVersionesPorCliente(cliente.getId());
        List<LineaFactura> lineasEnero = lineaRepository.getLineas(versiones.get(0).getId());
        List<LineaFactura> lineasFebrero = lineaRepository.getLineas(versiones.get(1).getId());
        assertEquals("contabilidad y laboral - mes de enero", lineasEnero.get(0).getDescripcion());
        assertEquals("contabilidad y laboral - mes de febrero", lineasFebrero.get(0).getDescripcion());
    }

    @Test
    void rollbackSiUnaFacturaFalla() throws Exception {
        Serie serie = serieC();
        Cliente cliente = clientePaco();
        TipoIva iva = iva21();

        NumeroDisponibleRepository numeroDisponibleRepository = new NumeroDisponibleRepository();
        FacturaService serviceQueFalla = new FacturaService(facturaRepository, serieRepository, clienteRepository,
                versionRepository, lineaRepository, new VersionadoService(versionRepository, lineaRepository),
                new NumeroService(serieRepository, numeroDisponibleRepository), numeroDisponibleRepository) {
            private int llamadas = 0;

            @Override
            long crearFacturaSinTransaccion(Serie s, LocalDate fecha, Cliente c, List<LineaFactura> lineas,
                                            int descuento, String observaciones, String referencia,
                                            Integer correlativoPedido, com.alcazaba.facturacion.model.DatosPago datosPago,
                                            TipoRetencion retencion) throws SQLException, ValidationException {
                llamadas++;
                if (llamadas == 2) {
                    throw new ValidationException("Fallo simulado en la segunda factura");
                }
                return super.crearFacturaSinTransaccion(s, fecha, c, lineas, descuento, observaciones,
                        referencia, correlativoPedido, datosPago, retencion);
            }
        };
        FacturacionMensualService servicioConFallo = new FacturacionMensualService(
                serviceQueFalla, facturaRepository, new NumeroService(serieRepository, numeroDisponibleRepository));

        assertThrows(ValidationException.class, () -> servicioConFallo.generar(cliente, 2026, 1, 3, serie, 15,
                iva, null, List.of(plantilla("servicios", "60.00", false))));

        List<FacturaVersion> versiones = versionRepository.getVersionesPorCliente(cliente.getId());
        assertEquals(0, versiones.size());
    }

    @Test
    void rellenaHuecosAlGenerarMensual() throws Exception {
        Serie serie = serieC();
        Cliente cliente = clientePaco();
        TipoIva iva = iva21();

        service.generar(cliente, 2026, 1, 3, serie, 15, iva, null,
                List.of(plantilla("servicios", "60.00", false)));

        List<FacturaVersion> iniciales = versionRepository.getVersionesPorCliente(cliente.getId());
        facturaService.borrarFactura(iniciales.get(0).getFacturaId());

        FacturacionMensualService.Resultado r = service.generar(cliente, 2026, 4, 6, serie,
                FacturacionMensualService.DiaMode.FIJO, 15, iva, null,
                List.of(plantilla("servicios", "60.00", false)), false, true);

        assertEquals(3, r.getGeneradas());
        List<FacturaVersion> versiones = versionRepository.getVersionesPorCliente(cliente.getId());
        FacturaVersion abril = versiones.stream()
                .filter(v -> v.getFechaFactura().equals(LocalDate.of(2026, 4, 15)))
                .findFirst().orElseThrow();
        assertEquals(1, facturaService.factura(abril.getFacturaId()).getCorrelativo());
    }

    private LineaFactura linea(String precio) {
        LineaFactura l = new LineaFactura();
        l.setCantidad(1);
        l.setPrecioUnitario(new BigDecimal(precio));
        l.setTotalBase(CalculoService.totalLinea(l.getPrecioUnitario(), 1));
        l.setTipoIvaId(1L);
        l.setIvaNombre("IVA 21%");
        l.setIvaPorcentaje(21);
        l.setIvaImporte(CalculoService.ivaDeBase(l.getTotalBase(), 21));
        return l;
    }
}
