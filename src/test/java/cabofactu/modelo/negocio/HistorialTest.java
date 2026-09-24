package cabofactu.modelo.negocio;

import cabofactu.modelo.negocio.sqlite.Conexion;
import cabofactu.modelo.dominio.Cliente;
import cabofactu.modelo.dominio.FiltrosHistorial;
import cabofactu.modelo.dominio.FilaHistorial;
import cabofactu.modelo.dominio.LineaFactura;
import cabofactu.modelo.dominio.Serie;
import cabofactu.modelo.dominio.FormatoNumero;
import cabofactu.modelo.negocio.sqlite.FacturaDAO;
import cabofactu.modelo.negocio.sqlite.HistorialDAO;
import cabofactu.modelo.negocio.sqlite.LineaFacturaDAO;
import cabofactu.modelo.negocio.sqlite.VersionFacturaDAO;
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

class HistorialTest {

    @TempDir
    Path tempDir;

    private Facturas facturas;
    private Historial historial;

    @BeforeEach
    void setUp() throws Exception {
        Conexion.setCarpetaRaiz(tempDir);
        Conexion.cerrarConexion();
        Conexion.establecerConexion();

        FacturaDAO facturaDAO = new FacturaDAO();
        VersionFacturaDAO versionFacturaDAO = new VersionFacturaDAO();
        LineaFacturaDAO lineaFacturaDAO = new LineaFacturaDAO();
        Versiones versiones = new Versiones(versionFacturaDAO, lineaFacturaDAO, Clock.systemDefaultZone());
        facturas = new Facturas(facturaDAO,
                versionFacturaDAO, lineaFacturaDAO, versiones, Clock.systemDefaultZone());
        historial = new Historial(new HistorialDAO());
    }

    @AfterEach
    void tearDown() {
        Conexion.cerrarConexion();
    }

    private Cliente clientePrueba() throws Exception {
        return new Cliente("Cliente Prueba", "12345678Z", "Calle Prueba 1", "28001", "Madrid", "Madrid");
    }

    @Test
    void buscaOrdenadoPorNumeroDeFactura() throws Exception {
        Serie c = serieC();
        facturas.crearFactura(c, LocalDate.of(2026, 9, 1), clientePrueba(),
                List.of(linea("200.00")), 0, null, null, 2);
        facturas.crearFactura(c, LocalDate.of(2026, 10, 1), clientePrueba(),
                List.of(linea("100.00")), 0, null, null, 1);

        List<FilaHistorial> filas = historial.buscar(new FiltrosHistorial());

        assertEquals(2, filas.size());
        assertEquals("C-1/10", filas.get(0).getNumero());
        assertEquals("C-2/9", filas.get(1).getNumero());
    }

    private Serie serieC() throws Exception {
        Serie s = new Serie("C", "Cocinas", FormatoNumero.MES, false);
        s.setId(Series.getSeries().alta(s));
        return s;
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
}
