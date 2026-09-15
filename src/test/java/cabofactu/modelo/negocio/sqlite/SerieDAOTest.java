package cabofactu.modelo.negocio.sqlite;

import cabofactu.modelo.dominio.EstadoFactura;
import cabofactu.modelo.dominio.VersionFactura;
import cabofactu.modelo.dominio.Serie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SerieDAOTest {

    @TempDir
    Path tempDir;

    private SerieDAO serieDAO;
    private FacturaDAO facturaDAO;
    private VersionFacturaDAO versionFacturaDAO;

    @BeforeEach
    void setUp() throws Exception {
        Conexion.setCarpetaRaiz(tempDir);
        Conexion.cerrarConexion();
        Conexion.establecerConexion();
        serieDAO = new SerieDAO();
        facturaDAO = new FacturaDAO();
        versionFacturaDAO = new VersionFacturaDAO();
    }

    @AfterEach
    void tearDown() {
        Conexion.cerrarConexion();
    }

    private Serie serie(String codigo, int siguiente) throws Exception {
        Serie s = new Serie();
        s.setCodigo(codigo);
        s.setSiguienteCorrelativo(siguiente);
        s.setEsRectificativa(false);
        s.setReutilizarAnulados(false);
        s.setSufijoFecha(Serie.SufijoFecha.MES);
        s.setId(serieDAO.insertar(s, LocalDate.now().getYear()));
        return s;
    }

    @Test
    void instalacionLimpiaNoTieneSeriesPorDefecto() throws Exception {
        assertTrue(serieDAO.listar().isEmpty());
    }

    @Test
    void eliminaSerieSinFacturas() throws Exception {
        Serie a = serie("A", 1);
        Serie b = serie("B", 1);
        serieDAO.eliminar(a.getId());
        List<Serie> restantes = serieDAO.listar();
        assertEquals(1, restantes.size());
        assertEquals("B", restantes.get(0).getCodigo());
    }

    @Test
    void serieTieneFacturasCuandoExisteAlguna() throws Exception {
        Serie a = serie("A", 1);
        assertFalse(facturaDAO.serieTieneFacturas(a.getId()));
        long fid = facturaDAO.insertar(a.getId(), 1, null);
        VersionFactura v = new VersionFactura();
        v.setFacturaId(fid);
        v.setVersionNum(1);
        v.setNumero("A-1");
        v.setFechaFactura(LocalDate.of(2026, 1, 1));
        v.setFechaGuardado(LocalDateTime.now());
        v.setEstado(EstadoFactura.EMITIDA);
        versionFacturaDAO.insertarVersion(v);
        assertTrue(facturaDAO.serieTieneFacturas(a.getId()));
    }
}
