package cabofactu.modelo.negocio;

import cabofactu.modelo.negocio.sqlite.Conexion;
import cabofactu.modelo.dominio.Cliente;
import cabofactu.modelo.dominio.Factura;
import cabofactu.modelo.dominio.FormatoNumero;
import cabofactu.modelo.dominio.LineaFactura;
import cabofactu.modelo.dominio.ModoDia;
import cabofactu.modelo.dominio.PlantillaMensual;
import cabofactu.modelo.dominio.Serie;
import cabofactu.modelo.dominio.TipoIva;
import cabofactu.modelo.dominio.TipoRetencion;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Comprobamos la generación mensual contra una base de datos temporal. El año
 * es siempre el de la fecha de trabajo de la sesión.
 */
class FacturacionMensualTest {

    @TempDir
    Path tempDir;

    private FacturacionMensual service;
    private int anio;

    @BeforeEach
    void setUp() throws Exception {
        Conexion.setCarpetaRaiz(tempDir);
        Conexion.cerrarConexion();
        Conexion.establecerConexion();
        Sesion.getSesion().iniciar(tempDir.toString(), LocalDate.now());
        anio = Sesion.getSesion().getFechaTrabajo().getYear();
        service = FacturacionMensual.getFacturacionMensual();
    }

    @AfterEach
    void tearDown() {
        Sesion.getSesion().terminar();
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

    /** Un tipo de IVA nunca guardado, sin id: cada línea que lo lleve no se puede persistir. */
    private TipoIva ivaSinGuardar() throws Exception {
        return new TipoIva("IVA sin guardar", 21, false);
    }

    private List<LineaFactura> lineas(String descripcion, String precio) throws Exception {
        LineaFactura linea = new LineaFactura(1, new BigDecimal(precio));
        linea.setDescripcion(descripcion);
        return List.of(linea);
    }

    private List<Factura> facturas() throws Exception {
        return Facturas.getFacturas().listado(null);
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
        PlantillaMensual plantilla = new PlantillaMensual(cliente, serie, 1, 12, ModoDia.FIJO, 15, iva,
                lineas("contabilidad y laboral", "60.00"));

        int generadas = service.generar(plantilla, false);

        assertEquals(12, generadas);
        assertEquals(12, facturas().size());
    }

    @Test
    void generaTambienLosMesesQueYaTenianFactura() throws Exception {
        Serie serie = serieC();
        Cliente cliente = clientePaco();
        TipoIva iva = iva21();

        service.generar(new PlantillaMensual(cliente, serie, 1, 3, ModoDia.FIJO, 15, iva,
                lineas("servicios", "60.00")), false);

        int generadas = service.generar(new PlantillaMensual(cliente, serie, 1, 5, ModoDia.FIJO, 15, iva,
                lineas("servicios", "60.00")), false);

        assertEquals(5, generadas);
        assertEquals(8, facturas().size());
    }

    @Test
    void mesesConFacturaDevuelveLosQueYaExisten() throws Exception {
        Serie serie = serieC();
        Cliente cliente = clientePaco();
        TipoIva iva = iva21();

        service.generar(new PlantillaMensual(cliente, serie, 2, 2, ModoDia.FIJO, 15, iva,
                lineas("servicios", "60.00")), false);

        List<String> meses = service.mesesConFactura(new PlantillaMensual(cliente, serie, 1, 3,
                ModoDia.FIJO, 15, iva, lineas("servicios", "60.00")));

        assertEquals(List.of("febrero"), meses);
    }

    @Test
    void ajustaDiaAlUltimoDiaValido() throws Exception {
        Serie serie = serieC();
        Cliente cliente = clientePaco();
        TipoIva iva = iva21();

        service.generar(new PlantillaMensual(cliente, serie, 1, 3, ModoDia.FIJO, 31, iva,
                lineas("servicios", "60.00")), false);

        List<Factura> generadas = facturas();
        int diasFebrero = YearMonth.of(anio, 2).lengthOfMonth();
        assertEquals(LocalDate.of(anio, 1, 31), generadas.get(0).getFecha());
        assertEquals(LocalDate.of(anio, 2, diasFebrero), generadas.get(1).getFecha());
        assertEquals(LocalDate.of(anio, 3, 31), generadas.get(2).getFecha());
    }

    @Test
    void modoPrimerDiaDelMes() throws Exception {
        Serie serie = serieC();
        Cliente cliente = clientePaco();
        TipoIva iva = iva21();

        service.generar(new PlantillaMensual(cliente, serie, 1, 3, ModoDia.PRIMER_DIA, 31, iva,
                lineas("servicios", "60.00")), false);

        List<Factura> generadas = facturas();
        assertEquals(LocalDate.of(anio, 1, 1), generadas.get(0).getFecha());
        assertEquals(LocalDate.of(anio, 2, 1), generadas.get(1).getFecha());
        assertEquals(LocalDate.of(anio, 3, 1), generadas.get(2).getFecha());
    }

    @Test
    void modoUltimoDiaDelMes() throws Exception {
        Serie serie = serieC();
        Cliente cliente = clientePaco();
        TipoIva iva = iva21();

        service.generar(new PlantillaMensual(cliente, serie, 1, 3, ModoDia.ULTIMO_DIA, 1, iva,
                lineas("servicios", "60.00")), false);

        List<Factura> generadas = facturas();
        int diasFebrero = YearMonth.of(anio, 2).lengthOfMonth();
        assertEquals(LocalDate.of(anio, 1, 31), generadas.get(0).getFecha());
        assertEquals(LocalDate.of(anio, 2, diasFebrero), generadas.get(1).getFecha());
        assertEquals(LocalDate.of(anio, 3, 31), generadas.get(2).getFecha());
    }

    @Test
    void aplicaIvaYRetencionEnTotales() throws Exception {
        Serie serie = serieC();
        Cliente cliente = clientePaco();
        TipoIva iva = iva21();
        TipoRetencion retencion = new TipoRetencion("IRPF 15%", 15);
        PlantillaMensual plantilla = new PlantillaMensual(cliente, serie, 1, 1, ModoDia.FIJO, 15, iva,
                lineas("servicios", "100.00"));
        plantilla.setRetencion(retencion);

        service.generar(plantilla, false);

        Factura factura = facturaDeMes(facturas(), LocalDate.of(anio, 1, 15));
        assertEquals(0, new BigDecimal("21.00").compareTo(factura.getIvaTotal()));
        assertEquals(0, new BigDecimal("15.00").compareTo(factura.getImporteRetencion()));
        assertEquals(0, new BigDecimal("106.00").compareTo(factura.getTotal()));
    }

    @Test
    void anadirMesPonElNombreEnTodasLasLineas() throws Exception {
        Serie serie = serieC();
        Cliente cliente = clientePaco();
        TipoIva iva = iva21();
        PlantillaMensual plantilla = new PlantillaMensual(cliente, serie, 1, 2, ModoDia.FIJO, 15, iva,
                lineas("contabilidad y laboral", "60.00"));
        plantilla.setAnadirMes(true);

        service.generar(plantilla, false);

        List<LineaFactura> lineasEnero = Facturas.getFacturas().buscar(
                facturaDeMes(facturas(), LocalDate.of(anio, 1, 15)).getId()).getLineas();
        List<LineaFactura> lineasFebrero = Facturas.getFacturas().buscar(
                facturaDeMes(facturas(), LocalDate.of(anio, 2, 15)).getId()).getLineas();
        assertEquals("contabilidad y laboral - mes de enero", lineasEnero.get(0).getDescripcion());
        assertEquals("contabilidad y laboral - mes de febrero", lineasFebrero.get(0).getDescripcion());
    }

    @Test
    void sinAnadirMesLaDescripcionNoCambia() throws Exception {
        Serie serie = serieC();
        Cliente cliente = clientePaco();
        TipoIva iva = iva21();
        PlantillaMensual plantilla = new PlantillaMensual(cliente, serie, 1, 1, ModoDia.FIJO, 15, iva,
                lineas("servicios", "60.00"));

        service.generar(plantilla, false);

        List<LineaFactura> lineasEnero = Facturas.getFacturas().buscar(
                facturaDeMes(facturas(), LocalDate.of(anio, 1, 15)).getId()).getLineas();
        assertEquals("servicios", lineasEnero.get(0).getDescripcion());
    }

    @Test
    void noGuardaNadaSiLosDatosFallan() throws Exception {
        Serie serie = serieC();
        Cliente cliente = clientePaco();
        PlantillaMensual plantilla = new PlantillaMensual(cliente, serie, 1, 3, ModoDia.FIJO, 15,
                ivaSinGuardar(), lineas("servicios", "60.00"));

        assertThrows(Exception.class, () -> service.generar(plantilla, false));

        assertEquals(0, facturas().size());
    }

    @Test
    void usaLosNumerosLibresSiSePide() throws Exception {
        Serie serie = serieC();
        Cliente cliente = clientePaco();
        TipoIva iva = iva21();

        service.generar(new PlantillaMensual(cliente, serie, 1, 3, ModoDia.FIJO, 15, iva,
                lineas("servicios", "60.00")), false);
        Facturas.getFacturas().baja(facturaDeMes(facturas(), LocalDate.of(anio, 1, 15)).getId());

        int generadas = service.generar(new PlantillaMensual(cliente, serie, 4, 6, ModoDia.FIJO, 15, iva,
                lineas("servicios", "60.00")), true);

        assertEquals(3, generadas);
        Factura abril = facturaDeMes(facturas(), LocalDate.of(anio, 4, 15));
        assertEquals(1, Facturas.getFacturas().buscar(abril.getId()).getCorrelativo());
    }

    @Test
    void sinUsarLosLibresSigueDespuesDelMayor() throws Exception {
        Serie serie = serieC();
        Cliente cliente = clientePaco();
        TipoIva iva = iva21();

        service.generar(new PlantillaMensual(cliente, serie, 1, 3, ModoDia.FIJO, 15, iva,
                lineas("servicios", "60.00")), false);
        Facturas.getFacturas().baja(facturaDeMes(facturas(), LocalDate.of(anio, 1, 15)).getId());

        int generadas = service.generar(new PlantillaMensual(cliente, serie, 4, 6, ModoDia.FIJO, 15, iva,
                lineas("servicios", "60.00")), false);

        assertEquals(3, generadas);
        Factura abril = facturaDeMes(facturas(), LocalDate.of(anio, 4, 15));
        assertEquals(4, Facturas.getFacturas().buscar(abril.getId()).getCorrelativo());
    }
}
