package cabofactu.modelo.negocio;

import cabofactu.modelo.negocio.sqlite.Database;
import cabofactu.modelo.dominio.FiltrosHistorial;
import cabofactu.modelo.dominio.HistorialFila;
import cabofactu.modelo.dominio.LineaFactura;
import cabofactu.modelo.dominio.Serie;
import cabofactu.modelo.negocio.sqlite.ClienteRepository;
import cabofactu.modelo.negocio.sqlite.FacturaRepository;
import cabofactu.modelo.negocio.sqlite.HistorialRepository;
import cabofactu.modelo.negocio.sqlite.LineaRepository;
import cabofactu.modelo.negocio.sqlite.NumeroDisponibleRepository;
import cabofactu.modelo.negocio.sqlite.SerieRepository;
import cabofactu.modelo.negocio.sqlite.VersionRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.Clock;
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
        NumeroDisponibleRepository numeroDisponibleRepository = new NumeroDisponibleRepository();
        NumeroService numeroService = new NumeroService(serieRepository, numeroDisponibleRepository, Clock.systemDefaultZone());
        VersionadoService versionadoService = new VersionadoService(versionRepository, lineaRepository, Clock.systemDefaultZone());
        facturaService = new FacturaService(facturaRepository, serieRepository, clienteRepository,
                versionRepository, lineaRepository, versionadoService, numeroService, numeroDisponibleRepository, Clock.systemDefaultZone());
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
        Serie s = new Serie();
        s.setCodigo("C");
        s.setDescripcion("Cocinas");
        s.setEsRectificativa(false);
        s.setSiguienteCorrelativo(1);
        s.setReutilizarAnulados(false);
        s.setSufijoFecha(Serie.SufijoFecha.MES);
        s.setId(serieRepository.insertar(s, LocalDate.now().getYear()));
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
}
