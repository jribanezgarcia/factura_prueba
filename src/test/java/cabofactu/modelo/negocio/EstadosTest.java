package cabofactu.modelo.negocio;

import cabofactu.modelo.negocio.sqlite.Conexion;
import cabofactu.modelo.dominio.Cliente;
import cabofactu.modelo.dominio.EstadoFactura;
import cabofactu.modelo.dominio.VersionFactura;
import cabofactu.modelo.dominio.LineaFactura;
import cabofactu.modelo.dominio.Serie;
import cabofactu.modelo.dominio.FormatoNumero;
import cabofactu.modelo.negocio.sqlite.FacturaDAO;
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

/** Comprobamos la anulación de facturas contra una base de datos temporal. */
class EstadosTest {

    @TempDir
    Path tempDir;

    private VersionFacturaDAO versionFacturaDAO;
    private LineaFacturaDAO lineaFacturaDAO;
    private Facturas facturas;
    private Estados estados;

    @BeforeEach
    void setUp() throws Exception {
        Conexion.setCarpetaRaiz(tempDir);
        Conexion.cerrarConexion();
        Conexion.establecerConexion();
        FacturaDAO facturaDAO = new FacturaDAO();
        versionFacturaDAO = new VersionFacturaDAO();
        lineaFacturaDAO = new LineaFacturaDAO();
        Versiones versiones = new Versiones(versionFacturaDAO, lineaFacturaDAO, Clock.systemDefaultZone());
        facturas = new Facturas(facturaDAO,
                versionFacturaDAO, lineaFacturaDAO, versiones, Clock.systemDefaultZone());
        estados = new Estados(facturaDAO,
                versionFacturaDAO, lineaFacturaDAO, versiones, facturas);
    }

    @AfterEach
    void tearDown() {
        Conexion.cerrarConexion();
    }

    private Serie serieC() throws Exception {
        Serie s = new Serie("C", "Cocinas", FormatoNumero.MES, false);
        s.setId(Series.getSeries().alta(s));
        return s;
    }

    private Cliente clientePrueba() throws Exception {
        return new Cliente("Cliente Prueba", "12345678Z", "Calle Prueba 1", "28001", "Madrid", "Madrid");
    }

    private Cliente clienteOtro() throws Exception {
        return new Cliente("Cliente Otro", "87654321X", "Calle Prueba 2", "28002", "Madrid", "Madrid");
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

    @Test
    void anularFacturasAnulaSoloLasEmitidas() throws Exception {
        Serie c = serieC();
        long f1 = facturas.crearFactura(c, LocalDate.of(2026, 1, 15), clientePrueba(), List.of(linea("100.00")), 0, null, null);
        long f2 = facturas.crearFactura(c, LocalDate.of(2026, 2, 15), clienteOtro(), List.of(linea("100.00")), 0, null, null);
        estados.anular(f2);

        Estados.AnulacionResultado r = estados.anularFacturas(List.of(f1, f2));

        assertEquals(1, r.getAnuladas());
        assertEquals(1, r.getYaAnuladas());
        assertEquals(0, r.getFallos());
        VersionFactura v1 = versionFacturaDAO.ultimaVersion(f1);
        assertEquals(EstadoFactura.ANULADA, v1.getEstado());
    }
}
