package cabofactu.modelo.negocio;

import cabofactu.modelo.dominio.Cliente;
import cabofactu.modelo.dominio.EstadoFactura;
import cabofactu.modelo.dominio.Factura;
import cabofactu.modelo.dominio.FiltrosHistorial;
import cabofactu.modelo.dominio.FormatoNumero;
import cabofactu.modelo.dominio.LineaFactura;
import cabofactu.modelo.dominio.Serie;
import cabofactu.modelo.negocio.sqlite.Conexion;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Comprobamos el singleton de series contra una base de datos temporal: alta,
 * listado, modificar, baja y la numeración calculada a partir de las facturas.
 */
class SeriesTest {

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() throws Exception {
        Conexion.setCarpetaRaiz(tempDir);
        Conexion.cerrarConexion();
        Conexion.establecerConexion();
    }

    @AfterEach
    void tearDown() {
        Conexion.cerrarConexion();
    }

    private Serie serie(String codigo, FormatoNumero formato, boolean rectificativa) throws Exception {
        Serie s = new Serie(codigo, "Serie " + codigo, formato, rectificativa);
        s.setId(Series.getSeries().alta(s));
        return s;
    }

    private Serie serieMes(String codigo) throws Exception {
        return serie(codigo, FormatoNumero.MES, false);
    }

    private void facturaConEstado(long serieId, int correlativo, EstadoFactura estado) throws Exception {
        facturaConEstado(serieId, correlativo, estado, LocalDate.of(2026, 8, 11));
    }

    private void facturaConEstado(long serieId, int correlativo, EstadoFactura estado, LocalDate fecha)
            throws Exception {
        Serie serie = Series.getSeries().buscar(serieId);
        Cliente cliente = new Cliente("Cliente Serie", "12345678Z", "Calle Prueba 1", "28001", "Madrid", "Madrid");
        Factura factura = new Factura(serie, fecha, cliente);
        factura.setCorrelativo(correlativo);
        List<LineaFactura> lineas = new ArrayList<>();
        lineas.add(lineaNueva());
        factura.setLineas(lineas);
        long facturaId = Facturas.getFacturas().alta(factura);
        if (estado == EstadoFactura.ANULADA) {
            Facturas.getFacturas().anular(facturaId);
        }
    }

    private LineaFactura lineaNueva() {
        LineaFactura linea = new LineaFactura();
        linea.setCantidad(1);
        linea.setPrecioUnitario(new BigDecimal("100.00"));
        linea.setTipoIvaId(1L);
        linea.setIvaNombre("IVA 21%");
        linea.setIvaPorcentaje(21);
        return linea;
    }

    private void borrarFacturaDe(long serieId, int correlativo) throws Exception {
        List<Factura> filas = Facturas.getFacturas().listado(new FiltrosHistorial());
        for (Factura fila : filas) {
            if (fila.getSerie().getId() == serieId && fila.getCorrelativo() == correlativo
                    && fila.getAnio() == 2026) {
                Facturas.getFacturas().baja(fila.getId());
                return;
            }
        }
    }

    @Test
    void altaYListado() throws Exception {
        Serie a = serieMes("B");
        serieMes("A");
        List<Serie> lista = Series.getSeries().listado();
        assertEquals(2, lista.size());
        assertEquals("A", lista.get(0).getCodigo());
        assertEquals("B", lista.get(1).getCodigo());
        Serie buscada = Series.getSeries().buscar(a.getId());
        assertEquals("B", buscada.getCodigo());
        assertEquals(FormatoNumero.MES, buscada.getFormato());
        assertNull(Series.getSeries().buscar(9999L));
    }

    @Test
    void instalacionLimpiaNoTieneSeriesPorDefecto() throws Exception {
        assertTrue(Series.getSeries().listado().isEmpty());
    }

    @Test
    void modificarGuardaLosCambios() throws Exception {
        Serie s = serieMes("A");
        s.setDescripcion("Otra");
        Series.getSeries().modificar(s);
        assertEquals("Otra", Series.getSeries().buscar(s.getId()).getDescripcion());
    }

    @Test
    void codigoRepetidoFalla() throws Exception {
        serieMes("A");
        Serie otra = new Serie("a", "Otra", FormatoNumero.MES, false);
        Exception e = assertThrows(Exception.class, () -> Series.getSeries().alta(otra));
        assertEquals("Ya existe una serie con el código A.", e.getMessage());
    }

    @Test
    void dosSeriesSinCodigoFallan() throws Exception {
        serie("", FormatoNumero.MES, false);
        Serie otra = new Serie("", "Otra", FormatoNumero.MES, false);
        Exception e = assertThrows(Exception.class, () -> Series.getSeries().alta(otra));
        assertEquals("Solo puede haber una serie sin código. Ponle un código para distinguirla.",
                e.getMessage());
    }

    @Test
    void bajaEliminaLaSerie() throws Exception {
        Serie a = serieMes("A");
        serieMes("B");
        Series.getSeries().baja(a.getId());
        List<Serie> restantes = Series.getSeries().listado();
        assertEquals(1, restantes.size());
        assertEquals("B", restantes.get(0).getCodigo());
    }

    @Test
    void bajaConFacturasFalla() throws Exception {
        Serie a = serieMes("A");
        facturaConEstado(a.getId(), 1, EstadoFactura.EMITIDA);
        assertTrue(Series.getSeries().tieneFacturas(a.getId()));
        Exception e = assertThrows(Exception.class, () -> Series.getSeries().baja(a.getId()));
        assertEquals("La serie tiene facturas (activas o históricas) y no se puede eliminar. "
                + "El histórico no se elimina.", e.getMessage());
    }

    @Test
    void formarNumeroTresFormatos() throws Exception {
        Serie mes = serie("C", FormatoNumero.MES, false);
        assertEquals("C-59/8", Series.getSeries().formarNumero(mes, 59, LocalDate.of(2026, 8, 11)));
        Serie anio = serie("C1", FormatoNumero.ANIO, false);
        assertEquals("C1-56-2026", Series.getSeries().formarNumero(anio, 56, LocalDate.of(2026, 7, 15)));
        Serie ninguno = serie("R1", FormatoNumero.NINGUNO, false);
        assertEquals("R1-56", Series.getSeries().formarNumero(ninguno, 56, LocalDate.of(2026, 7, 15)));
    }

    @Test
    void formarNumeroSinCodigoYRectificativa() throws Exception {
        Serie sinCodigo = serie("", FormatoNumero.ANIO, false);
        assertEquals("56-2026", Series.getSeries().formarNumero(sinCodigo, 56, LocalDate.of(2026, 7, 15)));
        Serie rectificativa = serie("R", FormatoNumero.MES, true);
        assertEquals("R-1", Series.getSeries().formarNumero(rectificativa, 1, LocalDate.of(2026, 8, 11)));
    }

    @Test
    void parseCorrelativoTresFormatos() throws Exception {
        Serie mes = serie("C2", FormatoNumero.MES, false);
        assertEquals(58, Series.getSeries().parseCorrelativo(mes, "C2-58/8"));
        Serie anio = serie("C3", FormatoNumero.ANIO, false);
        assertEquals(56, Series.getSeries().parseCorrelativo(anio, "C3-56-2026"));
        Serie ninguno = serie("R2", FormatoNumero.NINGUNO, false);
        assertEquals(56, Series.getSeries().parseCorrelativo(ninguno, "R2-56"));
    }

    @Test
    void parseCorrelativoQueNoEncajaDevuelveNull() throws Exception {
        Serie mes = serie("C2", FormatoNumero.MES, false);
        assertNull(Series.getSeries().parseCorrelativo(mes, "X-58/8"));
        assertNull(Series.getSeries().parseCorrelativo(mes, "C2-hola/8"));
        assertNull(Series.getSeries().parseCorrelativo(mes, null));
    }

    @Test
    void siguienteSinFacturasEsUno() throws Exception {
        Serie s = serieMes("K");
        assertEquals(1, Series.getSeries().siguienteCorrelativo(s, LocalDate.of(2026, 3, 1)));
    }

    @Test
    void siguienteEsElMayorMasUno() throws Exception {
        Serie s = serieMes("K");
        facturaConEstado(s.getId(), 1, EstadoFactura.EMITIDA);
        facturaConEstado(s.getId(), 2, EstadoFactura.EMITIDA);
        facturaConEstado(s.getId(), 3, EstadoFactura.EMITIDA);
        assertEquals(4, Series.getSeries().siguienteCorrelativo(s, LocalDate.of(2026, 3, 1)));
    }

    @Test
    void siguientePorAnioEsIndependiente() throws Exception {
        Serie s = serieMes("K");
        facturaConEstado(s.getId(), 5, EstadoFactura.EMITIDA);
        assertEquals(6, Series.getSeries().siguienteCorrelativo(s, LocalDate.of(2026, 3, 1)));
        assertEquals(1, Series.getSeries().siguienteCorrelativo(s, LocalDate.of(2025, 3, 1)));
    }

    @Test
    void anuladaNoLiberaSuNumero() throws Exception {
        Serie s = serieMes("K");
        facturaConEstado(s.getId(), 5, EstadoFactura.ANULADA);
        assertEquals(6, Series.getSeries().siguienteCorrelativo(s, LocalDate.of(2026, 3, 1)));
        assertFalse(Series.getSeries().huecos(s, LocalDate.of(2026, 3, 1)).contains(5));
    }

    @Test
    void huecosTrasBorrar() throws Exception {
        Serie s = serieMes("K");
        facturaConEstado(s.getId(), 1, EstadoFactura.EMITIDA);
        facturaConEstado(s.getId(), 2, EstadoFactura.EMITIDA);
        facturaConEstado(s.getId(), 3, EstadoFactura.EMITIDA);
        borrarFacturaDe(s.getId(), 2);
        assertEquals(List.of(2), Series.getSeries().huecos(s, LocalDate.of(2026, 3, 1)));
        assertEquals(4, Series.getSeries().siguienteCorrelativo(s, LocalDate.of(2026, 3, 1)));
    }

    @Test
    void proponerNumerosConHuecos() throws Exception {
        Serie s = serieMes("K");
        facturaConEstado(s.getId(), 1, EstadoFactura.EMITIDA);
        facturaConEstado(s.getId(), 3, EstadoFactura.EMITIDA);
        facturaConEstado(s.getId(), 5, EstadoFactura.EMITIDA);
        assertEquals(List.of(2, 4, 6, 7), Series.getSeries().proponerNumeros(s, 2026, 4, true));
    }

    @Test
    void proponerNumerosSinHuecos() throws Exception {
        Serie s = serieMes("K");
        facturaConEstado(s.getId(), 1, EstadoFactura.EMITIDA);
        facturaConEstado(s.getId(), 3, EstadoFactura.EMITIDA);
        assertEquals(List.of(4, 5), Series.getSeries().proponerNumeros(s, 2026, 2, false));
    }

    @Test
    void correlativoOcupado() throws Exception {
        Serie s = serieMes("K");
        facturaConEstado(s.getId(), 7, EstadoFactura.EMITIDA);
        facturaConEstado(s.getId(), 8, EstadoFactura.ANULADA);
        assertTrue(Series.getSeries().correlativoOcupado(s, 7, LocalDate.of(2026, 3, 1)));
        assertTrue(Series.getSeries().correlativoOcupado(s, 8, LocalDate.of(2026, 3, 1)));
        assertFalse(Series.getSeries().correlativoOcupado(s, 9, LocalDate.of(2026, 3, 1)));
    }

    @Test
    void sinSerieRectificativaDevuelveNull() throws Exception {
        serieMes("K");
        assertNull(Series.getSeries().rectificativa());
    }

    @Test
    void rectificativaDevuelveLaPrimeraSerieDeRectificativas() throws Exception {
        serieMes("K");
        Serie r = serie("R", FormatoNumero.NINGUNO, true);
        assertEquals("R", Series.getSeries().rectificativa().getCodigo());
        assertEquals(r.getId(), Series.getSeries().rectificativa().getId());
    }
}
