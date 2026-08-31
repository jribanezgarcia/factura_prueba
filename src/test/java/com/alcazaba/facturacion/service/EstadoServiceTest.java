package com.alcazaba.facturacion.service;

import com.alcazaba.facturacion.db.Database;
import com.alcazaba.facturacion.model.EstadoFactura;
import com.alcazaba.facturacion.model.FacturaVersion;
import com.alcazaba.facturacion.model.LineaFactura;
import com.alcazaba.facturacion.model.Serie;
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

class EstadoServiceTest {

    @TempDir
    Path tempDir;

    private SerieRepository serieRepository;
    private VersionRepository versionRepository;
    private LineaRepository lineaRepository;
    private FacturaService facturaService;
    private EstadoService estadoService;

    @BeforeEach
    void setUp() throws Exception {
        Database.setDataDir(tempDir);
        Database.resetConnection();
        Database.getConnection();
        serieRepository = new SerieRepository();
        FacturaRepository facturaRepository = new FacturaRepository();
        ClienteRepository clienteRepository = new ClienteRepository();
        versionRepository = new VersionRepository();
        lineaRepository = new LineaRepository();
        NumeroDisponibleRepository numeroDisponibleRepository = new NumeroDisponibleRepository();
        NumeroService numeroService = new NumeroService(serieRepository, numeroDisponibleRepository);
        VersionadoService versionadoService = new VersionadoService(versionRepository, lineaRepository);
        facturaService = new FacturaService(facturaRepository, serieRepository, clienteRepository,
                versionRepository, lineaRepository, versionadoService, numeroService, numeroDisponibleRepository);
        estadoService = new EstadoService(facturaRepository, serieRepository, versionRepository,
                lineaRepository, versionadoService, numeroService, facturaService);
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
    void anularFacturasAnulaSoloLasEmitidas() throws Exception {
        Serie c = serieC();
        long f1 = facturaService.crearFactura(c, LocalDate.of(2026, 1, 15), null, List.of(linea("100.00")), 0, null, null);
        long f2 = facturaService.crearFactura(c, LocalDate.of(2026, 2, 15), null, List.of(linea("100.00")), 0, null, null);
        estadoService.anular(f2);

        EstadoService.AnulacionResultado r = estadoService.anularFacturas(List.of(f1, f2));

        assertEquals(1, r.getAnuladas());
        assertEquals(1, r.getYaAnuladas());
        assertEquals(0, r.getFallos());
        FacturaVersion v1 = versionRepository.ultimaVersion(f1);
        assertEquals(EstadoFactura.ANULADA, v1.getEstado());
    }
}
