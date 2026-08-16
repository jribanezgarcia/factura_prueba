package com.alcazaba.facturacion.service;

import com.alcazaba.facturacion.db.Database;
import com.alcazaba.facturacion.model.EstadoFactura;
import com.alcazaba.facturacion.model.FacturaVersion;
import com.alcazaba.facturacion.model.Serie;
import com.alcazaba.facturacion.repository.FacturaRepository;
import com.alcazaba.facturacion.repository.SerieRepository;
import com.alcazaba.facturacion.repository.VersionRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NumeroServiceTest {

    @TempDir
    Path tempDir;

    private SerieRepository serieRepository;
    private FacturaRepository facturaRepository;
    private VersionRepository versionRepository;
    private NumeroService numeroService;

    @BeforeEach
    void setUp() throws Exception {
        Database.setDataDir(tempDir);
        Database.resetConnection();
        Database.getConnection();
        serieRepository = new SerieRepository();
        facturaRepository = new FacturaRepository();
        versionRepository = new VersionRepository();
        numeroService = new NumeroService(serieRepository);
    }

    @AfterEach
    void tearDown() {
        Database.resetConnection();
    }

    private Serie serie(String codigo, boolean rectificativa, int siguiente, boolean reutilizar)
            throws SQLException {
        Serie s = new Serie();
        s.setCodigo(codigo);
        s.setEsRectificativa(rectificativa);
        s.setSiguienteCorrelativo(siguiente);
        s.setReutilizarAnulados(reutilizar);
        s.setId(serieRepository.insertar(s));
        return s;
    }

    private void facturaConEstado(long serieId, int correlativo, EstadoFactura estado) throws SQLException {
        long fid = facturaRepository.insertar(serieId, correlativo, null);
        FacturaVersion v = new FacturaVersion();
        v.setFacturaId(fid);
        v.setVersionNum(1);
        v.setNumero("X-" + correlativo);
        v.setFechaFactura(LocalDate.of(2026, 8, 11));
        v.setFechaGuardado(LocalDateTime.now());
        v.setEstado(estado);
        versionRepository.insertarVersion(v);
    }

    @Test
    void elMesDerivaDeLaFecha() throws SQLException {
        Serie c = serie("K", false, 59, false);
        assertEquals("K-59/8", numeroService.formarNumero(c, 59, LocalDate.of(2026, 8, 11)));
        assertEquals("K-59/7", numeroService.formarNumero(c, 59, LocalDate.of(2026, 7, 1)));
    }

    @Test
    void laRectificativaNoLlevaMes() throws SQLException {
        Serie r = serie("X", true, 1, false);
        assertEquals("X-1", numeroService.formarNumero(r, 1, LocalDate.of(2026, 8, 11)));
    }

    @Test
    void siguienteSinReutilizacion() throws SQLException {
        Serie c = serie("K", false, 58, false);
        assertEquals(58, numeroService.siguienteCorrelativo(c));
    }

    @Test
    void reutilizaAnuladaLibre() throws SQLException {
        Serie c = serie("K", false, 10, true);
        facturaConEstado(c.getId(), 5, EstadoFactura.ANULADA);
        assertEquals(5, numeroService.siguienteCorrelativo(c));
    }

    @Test
    void noReutilizaCorrelativoOcupadoPorActiva() throws SQLException {
        Serie c = serie("K", false, 10, true);
        facturaConEstado(c.getId(), 5, EstadoFactura.ANULADA);
        facturaConEstado(c.getId(), 5, EstadoFactura.EMITIDA);
        assertEquals(10, numeroService.siguienteCorrelativo(c));
    }

    @Test
    void unicidadContraActivas() throws SQLException {
        Serie c = serie("K", false, 1, false);
        facturaConEstado(c.getId(), 7, EstadoFactura.EMITIDA);
        facturaConEstado(c.getId(), 8, EstadoFactura.ANULADA);
        assertTrue(numeroService.correlativoOcupadoPorActiva(c, 7));
        assertFalse(numeroService.correlativoOcupadoPorActiva(c, 8));
        assertFalse(numeroService.correlativoOcupadoPorActiva(c, 9));
    }
}
