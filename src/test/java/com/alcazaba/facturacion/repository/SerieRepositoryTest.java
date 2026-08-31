package com.alcazaba.facturacion.repository;

import com.alcazaba.facturacion.db.Database;
import com.alcazaba.facturacion.model.EstadoFactura;
import com.alcazaba.facturacion.model.FacturaVersion;
import com.alcazaba.facturacion.model.Serie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SerieRepositoryTest {

    @TempDir
    Path tempDir;

    private SerieRepository serieRepository;
    private FacturaRepository facturaRepository;
    private VersionRepository versionRepository;

    @BeforeEach
    void setUp() throws Exception {
        Database.setDataDir(tempDir);
        Database.resetConnection();
        Database.getConnection();
        serieRepository = new SerieRepository();
        facturaRepository = new FacturaRepository();
        versionRepository = new VersionRepository();
    }

    @AfterEach
    void tearDown() {
        Database.resetConnection();
    }

    private Serie serie(String codigo, int siguiente) throws Exception {
        Serie s = new Serie();
        s.setCodigo(codigo);
        s.setSiguienteCorrelativo(siguiente);
        s.setEsRectificativa(false);
        s.setReutilizarAnulados(false);
        s.setSufijoFecha(Serie.SufijoFecha.MES);
        s.setId(serieRepository.insertar(s));
        return s;
    }

    @Test
    void instalacionLimpiaNoTieneSeriesPorDefecto() throws Exception {
        assertTrue(serieRepository.listar().isEmpty());
    }

    @Test
    void eliminaSerieSinFacturas() throws Exception {
        Serie a = serie("A", 1);
        Serie b = serie("B", 1);
        serieRepository.eliminar(a.getId());
        List<Serie> restantes = serieRepository.listar();
        assertEquals(1, restantes.size());
        assertEquals("B", restantes.get(0).getCodigo());
    }

    @Test
    void serieTieneFacturasCuandoExisteAlguna() throws Exception {
        Serie a = serie("A", 1);
        assertFalse(facturaRepository.serieTieneFacturas(a.getId()));
        long fid = facturaRepository.insertar(a.getId(), 1, null);
        FacturaVersion v = new FacturaVersion();
        v.setFacturaId(fid);
        v.setVersionNum(1);
        v.setNumero("A-1");
        v.setFechaFactura(LocalDate.of(2026, 1, 1));
        v.setFechaGuardado(LocalDateTime.now());
        v.setEstado(EstadoFactura.EMITIDA);
        versionRepository.insertarVersion(v);
        assertTrue(facturaRepository.serieTieneFacturas(a.getId()));
    }
}
