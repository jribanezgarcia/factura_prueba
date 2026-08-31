package com.alcazaba.facturacion.service;

import com.alcazaba.facturacion.db.Database;
import com.alcazaba.facturacion.model.EstadoFactura;
import com.alcazaba.facturacion.model.FacturaVersion;
import com.alcazaba.facturacion.model.Serie;
import com.alcazaba.facturacion.repository.FacturaRepository;
import com.alcazaba.facturacion.repository.NumeroDisponibleRepository;
import com.alcazaba.facturacion.repository.SerieRepository;
import com.alcazaba.facturacion.repository.VersionRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NumeroServiceTest {

    @TempDir
    Path tempDir;

    private SerieRepository serieRepository;
    private FacturaRepository facturaRepository;
    private VersionRepository versionRepository;
    private NumeroDisponibleRepository numeroDisponibleRepository;
    private NumeroService numeroService;

    @BeforeEach
    void setUp() throws Exception {
        Database.setDataDir(tempDir);
        Database.resetConnection();
        Database.getConnection();
        serieRepository = new SerieRepository();
        facturaRepository = new FacturaRepository();
        versionRepository = new VersionRepository();
        numeroDisponibleRepository = new NumeroDisponibleRepository();
        numeroService = new NumeroService(serieRepository, numeroDisponibleRepository);
    }

    @AfterEach
    void tearDown() {
        Database.resetConnection();
        try {
            Files.deleteIfExists(Database.dbPath());
            Files.deleteIfExists(Database.lockPath());
        } catch (IOException ignored) {
        }
    }

    private Serie serie(String codigo, boolean rectificativa, int siguiente, boolean reutilizar, Serie.SufijoFecha sufijo)
            throws SQLException {
        Serie s = new Serie();
        s.setCodigo(codigo);
        s.setEsRectificativa(rectificativa);
        s.setSiguienteCorrelativo(siguiente);
        s.setReutilizarAnulados(reutilizar);
        s.setSufijoFecha(sufijo);
        s.setId(serieRepository.insertar(s));
        return s;
    }

    private Serie serie(String codigo, boolean rectificativa, int siguiente, boolean reutilizar)
            throws SQLException {
        return serie(codigo, rectificativa, siguiente, reutilizar, Serie.SufijoFecha.MES);
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

    @Test
    void formatoANIOConCodigo() throws SQLException {
        Serie c = serie("C1", false, 56, false, Serie.SufijoFecha.ANIO);
        assertEquals("C1-56-2026", numeroService.formarNumero(c, 56, LocalDate.of(2026, 7, 15)));
    }

    @Test
    void formatoANIOSinCodigo() throws SQLException {
        Serie c = serie("", false, 56, false, Serie.SufijoFecha.ANIO);
        assertEquals("56-2026", numeroService.formarNumero(c, 56, LocalDate.of(2026, 7, 15)));
    }

    @Test
    void formatoNINGUNOConCodigo() throws SQLException {
        Serie c = serie("R1", false, 56, false, Serie.SufijoFecha.NINGUNO);
        assertEquals("R1-56", numeroService.formarNumero(c, 56, LocalDate.of(2026, 7, 15)));
    }

    @Test
    void formatoNINGUNOSinCodigo() throws SQLException {
        Serie c = serie("", false, 56, false, Serie.SufijoFecha.NINGUNO);
        assertEquals("56", numeroService.formarNumero(c, 56, LocalDate.of(2026, 7, 15)));
    }

    @Test
    void rectificativaSinCodigo() throws SQLException {
        Serie r = serie("", true, 1, false);
        assertEquals("1", numeroService.formarNumero(r, 1, LocalDate.of(2026, 8, 11)));
    }

    @Test
    void parseCorrelativoFormatoMES() throws SQLException {
        Serie c = serie("C2", false, 58, false, Serie.SufijoFecha.MES);
        assertEquals(58, numeroService.parseCorrelativo(c, "C2-58/8"));
    }

    @Test
    void parseCorrelativoFormatoANIOConCodigo() throws SQLException {
        Serie c = serie("C3", false, 56, false, Serie.SufijoFecha.ANIO);
        assertEquals(56, numeroService.parseCorrelativo(c, "C3-56-2026"));
    }

    @Test
    void parseCorrelativoFormatoANIOSinCodigo() throws SQLException {
        Serie c = serie("", false, 56, false, Serie.SufijoFecha.ANIO);
        assertEquals(56, numeroService.parseCorrelativo(c, "56-2026"));
    }

    @Test
    void parseCorrelativoFormatoNINGUNOConCodigo() throws SQLException {
        Serie c = serie("R2", false, 56, false, Serie.SufijoFecha.NINGUNO);
        assertEquals(56, numeroService.parseCorrelativo(c, "R2-56"));
    }

    @Test
    void parseCorrelativoFormatoNINGUNOSinCodigo() throws SQLException {
        Serie c = serie("", false, 56, false, Serie.SufijoFecha.NINGUNO);
        assertEquals(56, numeroService.parseCorrelativo(c, "56"));
    }

    @Test
    void correlativoPorAnioEsIndependiente() throws SQLException {
        Serie c = serie("K", false, 5, false);
        assertEquals(5, numeroService.siguienteCorrelativo(c, LocalDate.of(2026, 3, 1)));
        assertEquals(1, numeroService.siguienteCorrelativo(c, LocalDate.of(2025, 3, 1)));
    }

    @Test
    void reutilizaAnuladaDelMismoAnio() throws SQLException {
        Serie c = serie("K", false, 10, true);
        facturaConEstado(c.getId(), 5, EstadoFactura.ANULADA);
        assertEquals(5, numeroService.siguienteCorrelativo(c, LocalDate.of(2026, 3, 1)));
    }

    @Test
    void noReutilizaAnuladaDeOtroAnio() throws SQLException {
        Serie c = serie("K", false, 10, true);
        facturaConEstado(c.getId(), 5, EstadoFactura.ANULADA);
        assertEquals(1, numeroService.siguienteCorrelativo(c, LocalDate.of(2025, 3, 1)));
    }

    @Test
    void huecosDisponiblesExcluyeOcupadosPorActivas() throws SQLException {
        Serie c = serie("L", false, 10, false);
        numeroDisponibleRepository.insertar(c.getId(), 2026, 3);
        numeroDisponibleRepository.insertar(c.getId(), 2026, 5);
        facturaConEstado(c.getId(), 5, EstadoFactura.EMITIDA);

        List<Integer> huecos = numeroService.huecosDisponibles(c, LocalDate.of(2026, 3, 1));

        assertEquals(List.of(3), huecos);
    }
}
