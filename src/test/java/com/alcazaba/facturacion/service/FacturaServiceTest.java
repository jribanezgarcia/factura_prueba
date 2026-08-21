package com.alcazaba.facturacion.service;

import com.alcazaba.facturacion.db.Database;
import com.alcazaba.facturacion.model.Cliente;
import com.alcazaba.facturacion.model.DatosPago;
import com.alcazaba.facturacion.model.EstadoFactura;
import com.alcazaba.facturacion.model.FacturaVersion;
import com.alcazaba.facturacion.model.LineaFactura;
import com.alcazaba.facturacion.model.Serie;
import com.alcazaba.facturacion.repository.ClienteRepository;
import com.alcazaba.facturacion.repository.FacturaRepository;
import com.alcazaba.facturacion.repository.LineaRepository;
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

class FacturaServiceTest {

    @TempDir
    Path tempDir;

    private SerieRepository serieRepository;
    private VersionadoService versionadoService;
    private LineaRepository lineaRepository;
    private FacturaService facturaService;

    @BeforeEach
    void setUp() throws Exception {
        Database.setDataDir(tempDir);
        Database.resetConnection();
        Database.getConnection();
        serieRepository = new SerieRepository();
        FacturaRepository facturaRepository = new FacturaRepository();
        ClienteRepository clienteRepository = new ClienteRepository();
        VersionRepository versionRepository = new VersionRepository();
        lineaRepository = new LineaRepository();
        NumeroService numeroService = new NumeroService(serieRepository);
        versionadoService = new VersionadoService(versionRepository, lineaRepository);
        facturaService = new FacturaService(facturaRepository, serieRepository, clienteRepository,
                versionRepository, lineaRepository, versionadoService, numeroService);
    }

    @AfterEach
    void tearDown() {
        Database.resetConnection();
    }

    private Serie serieC() throws SQLException {
        return serieRepository.listar().stream()
                .filter(s -> "C".equals(s.getCodigo()))
                .findFirst().orElseThrow();
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

    @Test
    void guardarEditadaSobrescribeLaUltimaVersion() throws Exception {
        Serie c = serieC();
        LocalDate fecha = LocalDate.of(2026, 8, 11);
        long facturaId = facturaService.crearFactura(c, fecha, null, List.of(linea("100.00")), 0, null, null);

        FacturaVersion v1 = versionadoService.ultimaVersion(facturaId);
        assertEquals(1, versionadoService.versionesDeFactura(facturaId).size());
        assertEquals(new BigDecimal("121.00"), v1.getTotal());

        FacturaVersion v = facturaService.guardarEditada(facturaId, v1.getId(), fecha, null,
                List.of(linea("200.00")), 0, "nueva observacion", null, null);

        assertEquals(v1.getId(), v.getId());
        List<FacturaVersion> versiones = versionadoService.versionesDeFactura(facturaId);
        assertEquals(1, versiones.size());
        assertEquals(1, versiones.get(0).getVersionNum());
        assertEquals(new BigDecimal("242.00"), versiones.get(0).getTotal());
        assertEquals("nueva observacion", versiones.get(0).getObservaciones());

        List<LineaFactura> lineas = lineaRepository.getLineas(versiones.get(0).getId());
        assertEquals(1, lineas.size());
        assertEquals(new BigDecimal("200.00"), lineas.get(0).getPrecioUnitario());
    }

    @Test
    void guardarEditadaDesdeVersionAnteriorCreaNuevaVersion() throws Exception {
        Serie c = serieC();
        LocalDate fecha = LocalDate.of(2026, 8, 11);
        long facturaId = facturaService.crearFactura(c, fecha, null, List.of(linea("100.00")), 0, null, null);

        FacturaVersion v1 = versionadoService.ultimaVersion(facturaId);
        versionadoService.crearVersion(facturaId, fecha, v1.getNumero(), EstadoFactura.EMITIDA,
                0, null, null, null, List.of(linea("100.00")));

        facturaService.guardarEditada(facturaId, v1.getId(), fecha, null,
                List.of(linea("50.00")), 0, null, null, null);

        assertEquals(3, versionadoService.versionesDeFactura(facturaId).size());
    }

    @Test
    void guardaEmailClienteYDatosPagoEnLaVersion() throws Exception {
        Serie c = serieC();
        LocalDate fecha = LocalDate.of(2026, 8, 21);
        Cliente cli = new Cliente();
        cli.setNombre("MARIA MARTAGON AVALOS");
        cli.setNif("49122168X");
        cli.setEmail("maria.martagon@correo.es");
        DatosPago dp = new DatosPago("Transferencia", LocalDate.of(2026, 9, 14), "AURORA");

        long facturaId = facturaService.crearFactura(c, fecha, cli, List.of(linea("100.00")),
                0, null, null, null, dp);

        FacturaVersion v = versionadoService.ultimaVersion(facturaId);
        assertEquals("maria.martagon@correo.es", v.getCliEmail());
        assertEquals("Transferencia", v.getFormaPago());
        assertEquals(LocalDate.of(2026, 9, 14), v.getVencimiento());
        assertEquals("AURORA", v.getRealizadaPor());

        Cliente guardado = facturaService.cliente(cli.getId());
        assertEquals("maria.martagon@correo.es", guardado.getEmail());
    }

    @Test
    void anularConservaDatosPagoYEmail() throws Exception {
        Serie c = serieC();
        LocalDate fecha = LocalDate.of(2026, 8, 21);
        Cliente cli = new Cliente();
        cli.setNombre("CLIENTE PRUEBA");
        cli.setEmail("cliente@prueba.es");
        DatosPago dp = new DatosPago("Efectivo", null, "AURORA");

        long facturaId = facturaService.crearFactura(c, fecha, cli, List.of(linea("100.00")),
                0, null, null, null, dp);
        EstadoService estadoService = new EstadoService(new FacturaRepository(), serieRepository,
                new VersionRepository(), lineaRepository, versionadoService,
                new NumeroService(serieRepository), facturaService);
        estadoService.anular(facturaId);

        FacturaVersion v = versionadoService.ultimaVersion(facturaId);
        assertEquals(EstadoFactura.ANULADA, v.getEstado());
        assertEquals("Efectivo", v.getFormaPago());
        assertEquals("AURORA", v.getRealizadaPor());
        assertEquals("cliente@prueba.es", v.getCliEmail());
    }
}
