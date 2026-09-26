package cabofactu.modelo.negocio;

import cabofactu.modelo.negocio.sqlite.Conexion;
import cabofactu.modelo.dominio.Cliente;
import cabofactu.modelo.dominio.EstadoFactura;
import cabofactu.modelo.dominio.Factura;
import cabofactu.modelo.dominio.FiltrosHistorial;
import cabofactu.modelo.dominio.LineaFactura;
import cabofactu.modelo.dominio.Serie;
import cabofactu.modelo.dominio.FormatoNumero;
import cabofactu.modelo.dominio.TipoRetencion;
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

/** Comprobamos las facturas sin versiones contra una base temporal. */
class FacturasTest {

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

    private Serie serieC() throws Exception {
        Serie s = new Serie("C", "Cocinas", FormatoNumero.MES, false);
        s.setId(Series.getSeries().alta(s));
        return s;
    }

    private Serie serieR() throws Exception {
        Serie s = new Serie("R", "Rectificativas", FormatoNumero.NINGUNO, true);
        s.setId(Series.getSeries().alta(s));
        return s;
    }

    private Cliente clientePrueba() throws Exception {
        return new Cliente("Cliente Prueba", "12345678Z", "Calle Prueba 1", "28001", "Madrid", "Madrid");
    }

    private LineaFactura linea(String precio) throws Exception {
        LineaFactura l = new LineaFactura(1, new BigDecimal(precio));
        l.setTipoIvaId(1L);
        l.setIvaNombre("IVA 21%");
        l.setIvaPorcentaje(21);
        return l;
    }

    private Factura facturaNueva(Serie serie, LocalDate fecha, Cliente cliente, String precio)
            throws Exception {
        Factura factura = new Factura(serie, fecha, cliente);
        List<LineaFactura> lineas = new ArrayList<>();
        lineas.add(linea(precio));
        factura.setLineas(lineas);
        return factura;
    }

    @Test
    void altaNumeraCorrelativa() throws Exception {
        Serie serie = serieC();
        LocalDate fecha = LocalDate.of(2026, 9, 1);
        long primeraId = Facturas.getFacturas().alta(facturaNueva(serie, fecha, clientePrueba(), "100.00"));
        long segundaId = Facturas.getFacturas().alta(facturaNueva(serie, fecha, clientePrueba(), "50.00"));

        assertEquals("C-1/9", Facturas.getFacturas().buscar(primeraId).getNumero());
        assertEquals("C-2/9", Facturas.getFacturas().buscar(segundaId).getNumero());
    }

    @Test
    void numeroManualOcupadoPorAnuladaSeRechaza() throws Exception {
        Serie serie = serieC();
        LocalDate fecha = LocalDate.of(2026, 9, 1);
        Factura primera = facturaNueva(serie, fecha, clientePrueba(), "100.00");
        primera.setCorrelativo(5);
        long primeraId = Facturas.getFacturas().alta(primera);
        Facturas.getFacturas().anular(primeraId);

        Factura segunda = facturaNueva(serie, fecha, clientePrueba(), "100.00");
        segunda.setCorrelativo(5);
        Exception e = assertThrows(Exception.class, () -> Facturas.getFacturas().alta(segunda));
        assertEquals("El número C-5/9 ya lo tiene otra factura de la serie C.", e.getMessage());
    }

    @Test
    void modificarDejaUnaFilaYSustituyeLasLineas() throws Exception {
        Serie serie = serieC();
        LocalDate fecha = LocalDate.of(2026, 9, 1);
        long facturaId = Facturas.getFacturas().alta(facturaNueva(serie, fecha, clientePrueba(), "100.00"));

        Factura guardada = Facturas.getFacturas().buscar(facturaId);
        List<LineaFactura> lineas = new ArrayList<>();
        lineas.add(linea("200.00"));
        guardada.setLineas(lineas);
        guardada.setObservaciones("nueva observacion");
        Facturas.getFacturas().modificar(guardada);

        List<Factura> filas = Facturas.getFacturas().listado(null);
        assertEquals(1, filas.size());
        Factura leida = Facturas.getFacturas().buscar(facturaId);
        assertEquals(1, leida.getLineas().size());
        assertEquals(new BigDecimal("200.00"), leida.getLineas().get(0).getPrecioUnitario());
        assertEquals("nueva observacion", leida.getObservaciones());
    }

    @Test
    void modificarRecalculaLosTotales() throws Exception {
        Serie serie = serieC();
        LocalDate fecha = LocalDate.of(2026, 8, 11);
        Factura factura = new Factura(serie, fecha, clientePrueba());
        List<LineaFactura> lineas = new ArrayList<>();
        lineas.add(linea("100.00"));
        factura.setLineas(lineas);
        long facturaId = Facturas.getFacturas().alta(factura);

        Factura guardada = Facturas.getFacturas().buscar(facturaId);
        guardada.getLineas().get(0).setPrecioUnitario(new BigDecimal("200.00"));
        Facturas.getFacturas().modificar(guardada);

        Factura leida = Facturas.getFacturas().buscar(facturaId);
        assertEquals(new BigDecimal("200.00"), leida.getBaseTotal());
        assertEquals(new BigDecimal("42.00"), leida.getIvaTotal());
        assertEquals(new BigDecimal("242.00"), leida.getTotal());
    }

    @Test
    void modificarAsociaElClienteExistenteSinDuplicarlo() throws Exception {
        Serie serie = serieC();
        LocalDate fecha = LocalDate.of(2026, 8, 11);
        Cliente existente = clientePrueba();
        existente.setId(Clientes.getClientes().alta(existente));
        Cliente original = new Cliente("Cliente Original", "87654321X", "Calle Prueba 2", "28001", "Madrid", "Madrid");
        Factura factura = new Factura(serie, fecha, original);
        List<LineaFactura> lineas = new ArrayList<>();
        lineas.add(linea("100.00"));
        factura.setLineas(lineas);
        long facturaId = Facturas.getFacturas().alta(factura);

        Factura guardada = Facturas.getFacturas().buscar(facturaId);
        Cliente escritoAMano = new Cliente("Cliente escrito a mano", "12345678Z", "Calle Prueba 1", "28001", "Madrid", "Madrid");
        guardada.setCliente(escritoAMano);
        Facturas.getFacturas().modificar(guardada);

        Factura leida = Facturas.getFacturas().buscar(facturaId);
        assertEquals(existente.getId(), leida.getCliente().getId());
        assertEquals("12345678Z", leida.getCliente().getNif());
        assertEquals(2, Clientes.getClientes().listado(false).size());
    }

    @Test
    void modificarCambiandoDeMesRehaceElNumero() throws Exception {
        Serie serie = serieC();
        long facturaId = Facturas.getFacturas().alta(
                facturaNueva(serie, LocalDate.of(2026, 9, 1), clientePrueba(), "100.00"));

        Factura guardada = Facturas.getFacturas().buscar(facturaId);
        guardada.setFecha(LocalDate.of(2026, 10, 5));
        Facturas.getFacturas().modificar(guardada);

        Factura leida = Facturas.getFacturas().buscar(facturaId);
        assertEquals(1, leida.getCorrelativo());
        assertEquals("C-1/10", leida.getNumero());
    }

    @Test
    void modificarCambiandoDeAnioSeRechaza() throws Exception {
        Serie serie = serieC();
        long facturaId = Facturas.getFacturas().alta(
                facturaNueva(serie, LocalDate.of(2026, 9, 1), clientePrueba(), "100.00"));

        Factura guardada = Facturas.getFacturas().buscar(facturaId);
        guardada.setFecha(LocalDate.of(2027, 1, 5));
        Exception e = assertThrows(Exception.class, () -> Facturas.getFacturas().modificar(guardada));
        assertEquals("Una factura emitida no puede cambiar de año. Si es de otro año, anúlala y crea una nueva.",
                e.getMessage());
        assertEquals("C-1/9", Facturas.getFacturas().buscar(facturaId).getNumero());
    }

    @Test
    void modificarAnuladaSeRechaza() throws Exception {
        Serie serie = serieC();
        long facturaId = Facturas.getFacturas().alta(
                facturaNueva(serie, LocalDate.of(2026, 9, 1), clientePrueba(), "100.00"));
        Facturas.getFacturas().anular(facturaId);

        Factura guardada = Facturas.getFacturas().buscar(facturaId);
        Exception e = assertThrows(Exception.class, () -> Facturas.getFacturas().modificar(guardada));
        assertEquals("Una factura anulada no se puede editar.", e.getMessage());
    }

    @Test
    void anularYRestaurarCambianLaMismaFila() throws Exception {
        Serie serie = serieC();
        long facturaId = Facturas.getFacturas().alta(
                facturaNueva(serie, LocalDate.of(2026, 9, 1), clientePrueba(), "100.00"));
        String numero = Facturas.getFacturas().buscar(facturaId).getNumero();

        Facturas.getFacturas().anular(facturaId);
        Factura anulada = Facturas.getFacturas().buscar(facturaId);
        assertEquals(EstadoFactura.ANULADA, anulada.getEstado());
        assertEquals(numero, anulada.getNumero());
        assertEquals(1, Facturas.getFacturas().listado(null).size());

        Facturas.getFacturas().restaurar(facturaId);
        Factura restaurada = Facturas.getFacturas().buscar(facturaId);
        assertEquals(EstadoFactura.EMITIDA, restaurada.getEstado());
        assertEquals(numero, restaurada.getNumero());
    }

    @Test
    void anularSoloEmitidasYRestaurarSoloAnuladas() throws Exception {
        Serie serie = serieC();
        LocalDate fecha = LocalDate.of(2026, 9, 1);
        long primeraId = Facturas.getFacturas().alta(facturaNueva(serie, fecha, clientePrueba(), "100.00"));
        long segundaId = Facturas.getFacturas().alta(facturaNueva(serie, fecha, clientePrueba(), "100.00"));
        Facturas.getFacturas().anular(segundaId);

        Facturas.getFacturas().anular(primeraId);
        assertEquals(EstadoFactura.ANULADA, Facturas.getFacturas().buscar(primeraId).getEstado());
        Exception e = assertThrows(Exception.class, () -> Facturas.getFacturas().anular(segundaId));
        assertEquals("Solo se pueden anular facturas en estado Emitida.", e.getMessage());

        Facturas.getFacturas().restaurar(segundaId);
        assertEquals(EstadoFactura.EMITIDA, Facturas.getFacturas().buscar(segundaId).getEstado());
        e = assertThrows(Exception.class, () -> Facturas.getFacturas().restaurar(segundaId));
        assertEquals("Solo se pueden restaurar facturas en estado Anulada.", e.getMessage());
    }

    @Test
    void rectificarCreaLaRectificativaEnLaSerieR() throws Exception {
        Serie serie = serieC();
        Serie rectificativas = serieR();
        Factura original = facturaNueva(serie, LocalDate.of(2026, 9, 1), clientePrueba(), "200.00");
        original.setDescuento(10);
        original.setObservaciones("original");
        long originalId = Facturas.getFacturas().alta(original);
        String numeroOriginal = Facturas.getFacturas().buscar(originalId).getNumero();

        long rectificativaId = Facturas.getFacturas().rectificar(originalId, LocalDate.of(2026, 9, 6));
        Factura rectificativa = Facturas.getFacturas().buscar(rectificativaId);

        assertEquals(rectificativas.getId(), rectificativa.getSerie().getId());
        assertEquals("R-1", rectificativa.getNumero());
        assertEquals(originalId, rectificativa.getRectificaId());
        assertEquals(numeroOriginal, rectificativa.getRectificaNumero());
        assertEquals("12345678Z", rectificativa.getCliente().getNif());
        assertEquals(1, rectificativa.getLineas().size());
        assertEquals(new BigDecimal("200.00"), rectificativa.getLineas().get(0).getPrecioUnitario());
        assertEquals(10, rectificativa.getDescuento());
        assertEquals("original", rectificativa.getObservaciones());
    }

    @Test
    void rectificarSinSerieRAvisa() throws Exception {
        Serie serie = serieC();
        long facturaId = Facturas.getFacturas().alta(
                facturaNueva(serie, LocalDate.of(2026, 9, 1), clientePrueba(), "100.00"));

        Exception e = assertThrows(Exception.class,
                () -> Facturas.getFacturas().rectificar(facturaId, LocalDate.of(2026, 9, 6)));
        assertEquals("No hay ninguna serie de rectificativas. Créala en Configuración.", e.getMessage());
    }

    @Test
    void borrarRectificadaSeRechaza() throws Exception {
        Serie serie = serieC();
        serieR();
        long facturaId = Facturas.getFacturas().alta(
                facturaNueva(serie, LocalDate.of(2026, 9, 1), clientePrueba(), "100.00"));
        long rectificativaId = Facturas.getFacturas().rectificar(facturaId, LocalDate.of(2026, 9, 6));
        String numeroOriginal = Facturas.getFacturas().buscar(facturaId).getNumero();
        String numeroRectificativa = Facturas.getFacturas().buscar(rectificativaId).getNumero();

        Exception e = assertThrows(Exception.class, () -> Facturas.getFacturas().baja(facturaId));
        assertEquals("Tiene la rectificativa " + numeroRectificativa + " y no se puede eliminar.", e.getMessage());
        assertEquals(numeroOriginal, Facturas.getFacturas().buscar(facturaId).getNumero());
    }

    @Test
    void borrarSeLlevaLasLineas() throws Exception {
        Serie serie = serieC();
        long facturaId = Facturas.getFacturas().alta(
                facturaNueva(serie, LocalDate.of(2026, 9, 1), clientePrueba(), "100.00"));
        assertEquals(1, Facturas.getFacturas().numeroDeLineas(facturaId));

        Facturas.getFacturas().baja(facturaId);

        assertNull(Facturas.getFacturas().buscar(facturaId));
        assertEquals(0, Facturas.getFacturas().numeroDeLineas(facturaId));
    }

    @Test
    void borrarLiberaElNumero() throws Exception {
        Serie serie = serieC();
        LocalDate fecha = LocalDate.of(2026, 9, 1);
        Facturas.getFacturas().alta(facturaNueva(serie, fecha, clientePrueba(), "100.00"));
        long facturaId = Facturas.getFacturas().alta(facturaNueva(serie, fecha, clientePrueba(), "100.00"));
        Facturas.getFacturas().alta(facturaNueva(serie, fecha, clientePrueba(), "100.00"));
        int correlativo = Facturas.getFacturas().buscar(facturaId).getCorrelativo();

        Facturas.getFacturas().baja(facturaId);

        assertNull(Facturas.getFacturas().buscar(facturaId));
        assertTrue(Series.getSeries().huecos(serie, fecha).contains(correlativo));
    }

    @Test
    void listadoDaUnaFilaPorFacturaYFiltra() throws Exception {
        Serie serie = serieC();
        Serie otraSerie = serieR();
        Cliente otro = new Cliente("Cliente Otro", "87654321X", "Calle Prueba 2", "28002", "Madrid", "Madrid");
        Factura primera = facturaNueva(serie, LocalDate.of(2026, 9, 1), clientePrueba(), "100.00");
        primera.setCorrelativo(2);
        Factura segunda = facturaNueva(serie, LocalDate.of(2026, 10, 1), otro, "100.00");
        segunda.setCorrelativo(1);
        long primeraId = Facturas.getFacturas().alta(primera);
        Facturas.getFacturas().alta(segunda);

        Factura guardada = Facturas.getFacturas().buscar(primeraId);
        guardada.getLineas().get(0).setPrecioUnitario(new BigDecimal("200.00"));
        Facturas.getFacturas().modificar(guardada);
        Facturas.getFacturas().anular(primeraId);

        List<Factura> filas = Facturas.getFacturas().listado(null);
        assertEquals(2, filas.size());
        assertEquals("C-1/10", filas.get(0).getNumero());
        assertEquals("C-2/9", filas.get(1).getNumero());

        FiltrosHistorial porOtraSerie = new FiltrosHistorial(otraSerie, null, null, null, null, null, null);
        assertEquals(0, Facturas.getFacturas().listado(porOtraSerie).size());
        FiltrosHistorial porLaSerie = new FiltrosHistorial(serie, null, null, null, null, null, null);
        assertEquals(2, Facturas.getFacturas().listado(porLaSerie).size());

        FiltrosHistorial porEstado = new FiltrosHistorial(null, null, null, null, null, null, EstadoFactura.ANULADA);
        List<Factura> anuladas = Facturas.getFacturas().listado(porEstado);
        assertEquals(1, anuladas.size());
        assertEquals("C-2/9", anuladas.get(0).getNumero());

        FiltrosHistorial porCliente = new FiltrosHistorial(null, "87654321X", null, null, null, null, null);
        List<Factura> delOtro = Facturas.getFacturas().listado(porCliente);
        assertEquals(1, delOtro.size());
        assertEquals("C-1/10", delOtro.get(0).getNumero());
    }

    @Test
    void altaVariasEsTodoONada() throws Exception {
        Serie serie = serieC();
        LocalDate fecha = LocalDate.of(2026, 9, 1);
        Factura primera = facturaNueva(serie, fecha, clientePrueba(), "100.00");
        primera.setCorrelativo(1);
        Factura segunda = facturaNueva(serie, fecha, clientePrueba(), "100.00");
        segunda.setCorrelativo(1);
        List<Factura> facturas = new ArrayList<>();
        facturas.add(primera);
        facturas.add(segunda);

        assertThrows(Exception.class, () -> Facturas.getFacturas().altaVarias(facturas));
        assertEquals(0, Facturas.getFacturas().listado(null).size());
        assertEquals(0, Facturas.getFacturas().contar());
    }

    @Test
    void clienteTieneFacturaEnMes() throws Exception {
        Serie serie = serieC();
        Cliente cliente = clientePrueba();
        Factura factura = facturaNueva(serie, LocalDate.of(2026, 2, 15), cliente, "100.00");
        Facturas.getFacturas().alta(factura);
        long clienteId = Facturas.getFacturas().buscar(
                Facturas.getFacturas().listado(null).get(0).getId())
                .getCliente().getId();

        assertTrue(Facturas.getFacturas().clienteTieneFacturaEnMes(clienteId, 2026, 2));
        assertFalse(Facturas.getFacturas().clienteTieneFacturaEnMes(clienteId, 2026, 3));
    }

    @Test
    void dosFacturasReutilizanElClienteEscritoAMano() throws Exception {
        Serie serie = serieC();
        LocalDate fecha = LocalDate.of(2026, 8, 11);
        long primeraId = Facturas.getFacturas().alta(facturaNueva(serie, fecha, clientePrueba(), "100.00"));
        long segundaId = Facturas.getFacturas().alta(facturaNueva(serie, fecha, clientePrueba(), "50.00"));
        List<Cliente> clientes = Clientes.getClientes().listado(false);

        assertEquals(1, clientes.size());
        assertEquals(clientes.get(0).getId(), Facturas.getFacturas().buscar(primeraId).getCliente().getId());
        assertEquals(clientes.get(0).getId(), Facturas.getFacturas().buscar(segundaId).getCliente().getId());
    }

    @Test
    void altaGuardaEmailPagosYRetencion() throws Exception {
        Serie serie = serieC();
        LocalDate fecha = LocalDate.of(2026, 8, 21);
        Cliente cli = new Cliente("MARIA MARTAGON AVALOS", "49122168X", "Calle Prueba 1",
                "28001", "Madrid", "Madrid");
        cli.setEmail("maria.martagon@correo.es");
        Factura factura = new Factura(serie, fecha, cli);
        List<LineaFactura> lineas = new ArrayList<>();
        lineas.add(linea("1000.00"));
        factura.setLineas(lineas);
        factura.setFormaPago("Transferencia");
        factura.setVencimiento(LocalDate.of(2026, 9, 14));
        factura.setRealizadaPor("AURORA");
        factura.setRetencion(new TipoRetencion("IRPF 15%", 15));
        long facturaId = Facturas.getFacturas().alta(factura);

        Factura leida = Facturas.getFacturas().buscar(facturaId);
        assertEquals("maria.martagon@correo.es", leida.getCliente().getEmail());
        assertEquals("Transferencia", leida.getFormaPago());
        assertEquals(LocalDate.of(2026, 9, 14), leida.getVencimiento());
        assertEquals("AURORA", leida.getRealizadaPor());
        assertEquals(0, new BigDecimal("150.00").compareTo(leida.getImporteRetencion()));
        assertEquals("IRPF 15%", leida.getRetencion().getNombre());
        assertEquals(15, leida.getRetencion().getPorcentaje());
        assertEquals(0, new BigDecimal("1060.00").compareTo(leida.getTotal()));
    }

    @Test
    void anularConservaPagosYEmail() throws Exception {
        Serie serie = serieC();
        LocalDate fecha = LocalDate.of(2026, 8, 21);
        Cliente cli = new Cliente("CLIENTE PRUEBA", "12345678Z", "Calle Prueba 1", "28001", "Madrid", "Madrid");
        cli.setEmail("cliente@prueba.es");
        Factura factura = new Factura(serie, fecha, cli);
        List<LineaFactura> lineas = new ArrayList<>();
        lineas.add(linea("100.00"));
        factura.setLineas(lineas);
        factura.setFormaPago("Efectivo");
        factura.setRealizadaPor("AURORA");
        long facturaId = Facturas.getFacturas().alta(factura);

        Facturas.getFacturas().anular(facturaId);

        Factura leida = Facturas.getFacturas().buscar(facturaId);
        assertEquals(EstadoFactura.ANULADA, leida.getEstado());
        assertEquals("Efectivo", leida.getFormaPago());
        assertEquals("AURORA", leida.getRealizadaPor());
        assertEquals("cliente@prueba.es", leida.getCliente().getEmail());
    }

    @Test
    void noCreaFacturaSinCliente() throws Exception {
        Serie serie = serieC();
        LocalDate fecha = LocalDate.of(2026, 8, 21);
        Exception e = assertThrows(Exception.class, () -> new Factura(serie, fecha, null));
        assertEquals("Indique los datos del cliente.", e.getMessage());
        assertEquals(0, Facturas.getFacturas().contar());
    }

    @Test
    void guardarSinClienteAvisa() throws Exception {
        Serie serie = serieC();
        LocalDate fecha = LocalDate.of(2026, 8, 21);
        Factura factura = new Factura(serie, fecha, clientePrueba());
        factura.setLineas(List.of(linea("100.00")));
        Exception e = assertThrows(Exception.class, () -> factura.setCliente(null));
        assertEquals("Indique los datos del cliente.", e.getMessage());
        assertEquals(0, Facturas.getFacturas().contar());
    }
}
