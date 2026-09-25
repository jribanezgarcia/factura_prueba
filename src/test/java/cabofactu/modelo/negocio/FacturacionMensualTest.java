package cabofactu.modelo.negocio;

import cabofactu.modelo.negocio.sqlite.Conexion;
import cabofactu.modelo.dominio.Cliente;
import cabofactu.modelo.dominio.Factura;
import cabofactu.modelo.dominio.FiltrosHistorial;
import cabofactu.modelo.dominio.LineaFactura;
import cabofactu.modelo.dominio.Serie;
import cabofactu.modelo.dominio.FormatoNumero;
import cabofactu.modelo.dominio.TipoIva;
import cabofactu.modelo.dominio.TipoRetencion;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Comprobamos la generación mensual contra una base de datos temporal. */
class FacturacionMensualTest {

    @TempDir
    Path tempDir;

    private FacturacionMensual service;

    @BeforeEach
    void setUp() throws Exception {
        Conexion.setCarpetaRaiz(tempDir);
        Conexion.cerrarConexion();
        Conexion.establecerConexion();
        service = new FacturacionMensual(Facturas.getFacturas());
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

    private Cliente clientePaco() throws Exception {
        Cliente c = new Cliente("Paco", "12345678Z", "Calle Prueba 1", "28001", "Madrid", "Madrid");
        c.setId(Clientes.getClientes().alta(c));
        return c;
    }

    private TipoIva iva21() throws Exception {
        TipoIva iva = new TipoIva("IVA 21%", 21, false);
        iva.setId(1L);
        return iva;
    }

    private FacturacionMensual.LineaPlantilla plantilla(String descripcion, String precio, boolean anadirMes) {
        return new FacturacionMensual.LineaPlantilla(1, descripcion,
                new BigDecimal(precio), anadirMes);
    }

    private List<Factura> facturas() throws Exception {
        return Facturas.getFacturas().listado(new FiltrosHistorial());
    }

    private Factura facturaDeMes(List<Factura> facturas, LocalDate fecha) {
        for (Factura factura : facturas) {
            if (factura.getFecha().equals(fecha)) {
                return factura;
            }
        }
        throw new IllegalStateException("Falta la factura de " + fecha);
    }

    @Test
    void generaDoceFacturasParaTodoElAnio() throws Exception {
        Serie serie = serieC();
        Cliente cliente = clientePaco();
        TipoIva iva = iva21();

        FacturacionMensual.Resultado r = service.generar(cliente, 2026, 1, 12, serie, 15,
                iva, null, List.of(plantilla("contabilidad y laboral", "60.00", true)));

        assertEquals(12, r.getGeneradas());
        assertTrue(r.getMesesOmitidos().isEmpty());
        assertEquals(12, facturas().size());
    }

    @Test
    void omiteMesesYaFacturados() throws Exception {
        Serie serie = serieC();
        Cliente cliente = clientePaco();
        TipoIva iva = iva21();

        service.generar(cliente, 2026, 1, 3, serie, 15, iva, null,
                List.of(plantilla("servicios", "60.00", true)));

        FacturacionMensual.Resultado r = service.generar(cliente, 2026, 1, 5, serie, 15, iva, null,
                List.of(plantilla("servicios", "60.00", true)));

        assertEquals(2, r.getGeneradas());
        assertEquals(List.of("enero", "febrero", "marzo"), r.getMesesOmitidos());
    }

    @Test
    void generaDuplicadosSiSeIndicaExplicitamente() throws Exception {
        Serie serie = serieC();
        Cliente cliente = clientePaco();
        TipoIva iva = iva21();

        service.generar(cliente, 2026, 1, 3, serie, 15, iva, null,
                List.of(plantilla("servicios", "60.00", true)));

        FacturacionMensual.Resultado r = service.generar(cliente, 2026, 1, 5, serie,
                FacturacionMensual.ModoDia.FIJO, 15, iva, null,
                List.of(plantilla("servicios", "60.00", true)), true, false);

        assertEquals(5, r.getGeneradas());
        assertTrue(r.getMesesOmitidos().isEmpty());
        assertEquals(8, facturas().size());
    }

    @Test
    void detectaDuplicadosCorrectamente() throws Exception {
        Serie serie = serieC();
        Cliente cliente = clientePaco();
        TipoIva iva = iva21();

        service.generar(cliente, 2026, 2, 2, serie, 15, iva, null,
                List.of(plantilla("servicios", "60.00", true)));

        List<String> duplicados = service.detectarDuplicados(cliente, 2026, 1, 3);
        assertEquals(List.of("febrero"), duplicados);
    }

    @Test
    void ajustaDiaAlUltimoDiaValido() throws Exception {
        Serie serie = serieC();
        Cliente cliente = clientePaco();
        TipoIva iva = iva21();

        FacturacionMensual.Resultado r = service.generar(cliente, 2026, 1, 3, serie, 31, iva, null,
                List.of(plantilla("servicios", "60.00", false)));

        assertEquals(3, r.getGeneradas());
        List<Factura> generadas = facturas();
        assertEquals(LocalDate.of(2026, 1, 31), generadas.get(0).getFecha());
        assertEquals(LocalDate.of(2026, 2, 28), generadas.get(1).getFecha());
        assertEquals(LocalDate.of(2026, 3, 31), generadas.get(2).getFecha());
    }

    @Test
    void modoPrimerDiaDelMes() throws Exception {
        Serie serie = serieC();
        Cliente cliente = clientePaco();
        TipoIva iva = iva21();

        service.generar(cliente, 2026, 1, 3, serie,
                FacturacionMensual.ModoDia.PRIMER_DIA, 31, iva, null,
                List.of(plantilla("servicios", "60.00", false)), true, false);

        List<Factura> generadas = facturas();
        assertEquals(LocalDate.of(2026, 1, 1), generadas.get(0).getFecha());
        assertEquals(LocalDate.of(2026, 2, 1), generadas.get(1).getFecha());
        assertEquals(LocalDate.of(2026, 3, 1), generadas.get(2).getFecha());
    }

    @Test
    void modoUltimoDiaDelMes() throws Exception {
        Serie serie = serieC();
        Cliente cliente = clientePaco();
        TipoIva iva = iva21();

        service.generar(cliente, 2026, 1, 3, serie,
                FacturacionMensual.ModoDia.ULTIMO_DIA, 1, iva, null,
                List.of(plantilla("servicios", "60.00", false)), true, false);

        List<Factura> generadas = facturas();
        assertEquals(LocalDate.of(2026, 1, 31), generadas.get(0).getFecha());
        assertEquals(LocalDate.of(2026, 2, 28), generadas.get(1).getFecha());
        assertEquals(LocalDate.of(2026, 3, 31), generadas.get(2).getFecha());
    }

    @Test
    void aplicaIvaYRetencionEnTotales() throws Exception {
        Serie serie = serieC();
        Cliente cliente = clientePaco();
        TipoIva iva = iva21();
        TipoRetencion retencion = new TipoRetencion("IRPF 15%", 15);

        service.generar(cliente, 2026, 1, 1, serie, 15, iva, retencion,
                List.of(plantilla("servicios", "100.00", false)));

        Factura factura = facturaDeMes(facturas(), LocalDate.of(2026, 1, 15));
        assertEquals(0, new BigDecimal("21.00").compareTo(factura.getIvaTotal()));
        assertEquals(0, new BigDecimal("15.00").compareTo(factura.getImporteRetencion()));
        assertEquals(0, new BigDecimal("106.00").compareTo(factura.getTotal()));
    }

    @Test
    void descripcionIncluyeNombreDelMes() throws Exception {
        Serie serie = serieC();
        Cliente cliente = clientePaco();
        TipoIva iva = iva21();

        service.generar(cliente, 2026, 1, 2, serie, 15, iva, null,
                List.of(plantilla("contabilidad y laboral", "60.00", true)));

        List<LineaFactura> lineasEnero = Facturas.getFacturas().buscar(
                facturaDeMes(facturas(), LocalDate.of(2026, 1, 15)).getId()).getLineas();
        List<LineaFactura> lineasFebrero = Facturas.getFacturas().buscar(
                facturaDeMes(facturas(), LocalDate.of(2026, 2, 15)).getId()).getLineas();
        assertEquals("contabilidad y laboral - mes de enero", lineasEnero.get(0).getDescripcion());
        assertEquals("contabilidad y laboral - mes de febrero", lineasFebrero.get(0).getDescripcion());
    }

    @Test
    void noGuardaNadaSiLosDatosFallan() throws Exception {
        Serie serie = serieC();
        Cliente cliente = clientePaco();
        TipoIva iva = iva21();

        assertThrows(Exception.class, () -> service.generar(cliente, 2026, 1, 3, serie, 15,
                iva, null, List.of(plantilla("servicios", "-60.00", false))));

        assertEquals(0, facturas().size());
    }

    @Test
    void rellenaHuecosAlGenerarMensual() throws Exception {
        Serie serie = serieC();
        Cliente cliente = clientePaco();
        TipoIva iva = iva21();

        service.generar(cliente, 2026, 1, 3, serie, 15, iva, null,
                List.of(plantilla("servicios", "60.00", false)));

        List<Factura> iniciales = facturas();
        Facturas.getFacturas().baja(facturaDeMes(iniciales, LocalDate.of(2026, 1, 15)).getId());

        FacturacionMensual.Resultado r = service.generar(cliente, 2026, 4, 6, serie,
                FacturacionMensual.ModoDia.FIJO, 15, iva, null,
                List.of(plantilla("servicios", "60.00", false)), false, true);

        assertEquals(3, r.getGeneradas());
        Factura abril = facturaDeMes(facturas(), LocalDate.of(2026, 4, 15));
        assertEquals(1, Facturas.getFacturas().buscar(abril.getId()).getCorrelativo());
    }
}
