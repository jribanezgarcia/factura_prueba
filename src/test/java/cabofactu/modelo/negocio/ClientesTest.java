package cabofactu.modelo.negocio;

import cabofactu.modelo.dominio.Cliente;
import cabofactu.modelo.dominio.Serie;
import cabofactu.modelo.dominio.FormatoNumero;
import cabofactu.modelo.negocio.sqlite.Conexion;
import cabofactu.modelo.negocio.sqlite.FacturaDAO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Comprobamos el singleton de clientes contra una base de datos temporal:
 * alta, listados, búsqueda, modificar, desactivar, baja y si tiene facturas.
 */
class ClientesTest {

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

    private Cliente clienteAna() throws Exception {
        return new Cliente("Ana García", "12345678Z", "Calle Mayor 1", "28013", "Madrid", "Madrid");
    }

    private Serie serieC() throws Exception {
        Serie s = new Serie("C", "Cocinas", FormatoNumero.MES, false);
        s.setId(Series.getSeries().alta(s));
        return s;
    }

    @Test
    void altaYListado() throws Exception {
        long id = Clientes.getClientes().alta(clienteAna());
        List<Cliente> lista = Clientes.getClientes().listado(false);
        assertEquals(1, lista.size());
        assertEquals(id, lista.get(0).getId());
        assertEquals("Ana García", lista.get(0).getNombre());
        assertEquals("12345678Z", lista.get(0).getNif());
    }

    @Test
    void altaYBusquedaPorNombre() throws Exception {
        Clientes.getClientes().alta(clienteAna());
        List<Cliente> encontrados = Clientes.getClientes().listado("ana", true);
        assertEquals(1, encontrados.size());
        assertEquals("Ana García", encontrados.get(0).getNombre());
    }

    @Test
    void altaYBusquedaPorNif() throws Exception {
        Clientes.getClientes().alta(clienteAna());
        List<Cliente> encontrados = Clientes.getClientes().listado("3456", true);
        assertEquals(1, encontrados.size());
        assertEquals("12345678Z", encontrados.get(0).getNif());
    }

    @Test
    void modificar() throws Exception {
        long id = Clientes.getClientes().alta(clienteAna());
        Cliente guardado = Clientes.getClientes().buscar(id);
        guardado.setNombre("Ana García López");
        Clientes.getClientes().modificar(guardado);
        assertEquals("Ana García López", Clientes.getClientes().buscar(id).getNombre());
    }

    @Test
    void desactivarDejaDeSalirEnElListadoDeActivos() throws Exception {
        long id = Clientes.getClientes().alta(clienteAna());
        Clientes.getClientes().desactivar(id);
        assertTrue(Clientes.getClientes().listado(true).isEmpty());
        assertEquals(1, Clientes.getClientes().listado(false).size());
        assertFalse(Clientes.getClientes().buscar(id).isActivo());
    }

    @Test
    void baja() throws Exception {
        long id = Clientes.getClientes().alta(clienteAna());
        Clientes.getClientes().baja(id);
        assertNull(Clientes.getClientes().buscar(id));
        assertTrue(Clientes.getClientes().listado(false).isEmpty());
    }

    @Test
    void tieneFacturas() throws Exception {
        long clienteId = Clientes.getClientes().alta(clienteAna());
        assertFalse(Clientes.getClientes().tieneFacturas(clienteId));
        Serie s = serieC();
        new FacturaDAO().insertar(s.getId(), 1, clienteId);
        assertTrue(Clientes.getClientes().tieneFacturas(clienteId));
    }

    @Test
    void buscarDeUnIdQueNoExisteDevuelveNull() throws Exception {
        assertNull(Clientes.getClientes().buscar(9999L));
    }
}