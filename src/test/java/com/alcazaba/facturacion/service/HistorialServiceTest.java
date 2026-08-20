package com.alcazaba.facturacion.service;

import com.alcazaba.facturacion.db.Database;
import com.alcazaba.facturacion.model.FiltrosHistorial;
import com.alcazaba.facturacion.model.HistorialFila;
import com.alcazaba.facturacion.model.LineaFactura;
import com.alcazaba.facturacion.model.Serie;
import com.alcazaba.facturacion.repository.ClienteRepository;
import com.alcazaba.facturacion.repository.FacturaRepository;
import com.alcazaba.facturacion.repository.HistorialRepository;
import com.alcazaba.facturacion.repository.LineaRepository;
import com.alcazaba.facturacion.repository.SerieRepository;
import com.alcazaba.facturacion.repository.VersionRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HistorialServiceTest {

    @TempDir
    Path tempDir;

    private SerieRepository serieRepository;
    private FacturaService facturaService;
    private HistorialService historialService;

    @BeforeEach
    void setUp() throws Exception {
        Database.setDataDir(tempDir);
        Database.resetConnection();
        Database.getConnection();

        serieRepository = new SerieRepository();
        FacturaRepository facturaRepository = new FacturaRepository();
        ClienteRepository clienteRepository = new ClienteRepository();
        VersionRepository versionRepository = new VersionRepository();
        LineaRepository lineaRepository = new LineaRepository();
        NumeroService numeroService = new NumeroService(serieRepository);
        VersionadoService versionadoService = new VersionadoService(versionRepository, lineaRepository);
        facturaService = new FacturaService(facturaRepository, serieRepository, clienteRepository,
                versionRepository, lineaRepository, versionadoService, numeroService);
        historialService = new HistorialService(new HistorialRepository());
    }

    @AfterEach
    void tearDown() {
        Database.resetConnection();
    }

    @Test
    void buscaOrdenadoPorNumeroDeFactura() throws Exception {
        Serie c = serieC();
        facturaService.crearFactura(c, LocalDate.of(2026, 9, 1), null,
                List.of(linea("200.00")), 0, null, null, 2);
        facturaService.crearFactura(c, LocalDate.of(2026, 10, 1), null,
                List.of(linea("100.00")), 0, null, null, 1);

        List<HistorialFila> filas = historialService.buscar(new FiltrosHistorial());

        assertEquals(2, filas.size());
        assertEquals("C-1/10", filas.get(0).getNumero());
        assertEquals("C-2/9", filas.get(1).getNumero());
    }

    private Serie serieC() throws Exception {
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
}
