package cabofactu.modelo.negocio;

import cabofactu.modelo.dominio.TipoIva;
import cabofactu.modelo.negocio.sqlite.Conexion;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Comprobamos el singleton de tipos de IVA contra una base de datos temporal:
 * alta, listados, búsqueda, modificar con sus guardas y si está en uso.
 */
class TiposIvaTest {

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

    private TipoIva ivaPrueba() throws Exception {
        return new TipoIva("IVA prueba", 21, false);
    }

    private long altaIvaPrueba() throws Exception {
        return TiposIva.getTiposIva().alta(ivaPrueba());
    }

    private void usarEnFactura(long tipoId) throws Exception {
        try (Statement st = Conexion.establecerConexion().createStatement()) {
            st.executeUpdate("INSERT INTO serie (codigo, descripcion) VALUES ('C', 'Serie C')");
            st.executeUpdate("INSERT INTO factura (serie_id, correlativo) VALUES (1, 1)");
            st.executeUpdate("INSERT INTO factura_version (factura_id, version_num, numero, fecha_factura, "
                    + "fecha_guardado, estado) VALUES (1, 1, 'C-1/8', '2026-01-01', '2026-01-01', 'EMITIDA')");
            st.executeUpdate("INSERT INTO factura_linea (factura_version_id, orden, tipo_iva_id) "
                    + "VALUES (1, 1, " + tipoId + ")");
        }
    }

    @Test
    void altaYListado() throws Exception {
        long id = altaIvaPrueba();
        TipoIva guardado = TiposIva.getTiposIva().buscar(id);
        assertEquals("IVA prueba", guardado.getNombre());
        assertEquals(21, guardado.getPorcentaje());
        assertFalse(guardado.isEsSuplido());
        List<TipoIva> lista = TiposIva.getTiposIva().listado(false);
        assertEquals(5, lista.size());
        TipoIva ultimo = lista.get(lista.size() - 1);
        assertEquals(id, ultimo.getId());
    }

    @Test
    void altaConNombreRepetidoFalla() throws Exception {
        TipoIva repetido = new TipoIva("IVA 10%", 10, false);
        Exception e = assertThrows(Exception.class, () -> TiposIva.getTiposIva().alta(repetido));
        assertEquals("Ya existe un tipo de IVA con el nombre IVA 10%.", e.getMessage());
        assertEquals(4, TiposIva.getTiposIva().listado(false).size());
    }

    @Test
    void listadoSoloActivos() throws Exception {
        altaIvaPrueba();
        TipoIva inactivo = ivaPrueba();
        inactivo.setNombre("IVA viejo");
        inactivo.setActivo(false);
        TiposIva.getTiposIva().alta(inactivo);
        assertEquals(5, TiposIva.getTiposIva().listado(true).size());
        assertEquals(6, TiposIva.getTiposIva().listado(false).size());
    }

    @Test
    void buscarInexistenteDevuelveNull() throws Exception {
        assertNull(TiposIva.getTiposIva().buscar(9999L));
    }

    @Test
    void modificarGuardaElNombre() throws Exception {
        long id = altaIvaPrueba();
        TipoIva tipo = TiposIva.getTiposIva().buscar(id);
        tipo.setNombre("IVA general");
        TiposIva.getTiposIva().modificar(tipo);
        assertEquals("IVA general", TiposIva.getTiposIva().buscar(id).getNombre());
    }

    @Test
    void modificarConSuPropioNombreFunciona() throws Exception {
        long id = altaIvaPrueba();
        TipoIva tipo = TiposIva.getTiposIva().buscar(id);
        tipo.setPorcentaje(10);
        TiposIva.getTiposIva().modificar(tipo);
        assertEquals("IVA prueba", TiposIva.getTiposIva().buscar(id).getNombre());
    }

    @Test
    void modificarInexistenteFalla() throws Exception {
        TipoIva tipo = ivaPrueba();
        tipo.setId(9999L);
        Exception e = assertThrows(Exception.class, () -> TiposIva.getTiposIva().modificar(tipo));
        assertEquals("No se ha encontrado el tipo de IVA.", e.getMessage());
    }

    @Test
    void noSePuedeConvertirEnSuplido() throws Exception {
        long id = altaIvaPrueba();
        TipoIva tipo = TiposIva.getTiposIva().buscar(id);
        tipo.setEsSuplido(true);
        Exception e = assertThrows(Exception.class, () -> TiposIva.getTiposIva().modificar(tipo));
        assertEquals("Un tipo existente no puede convertirse en suplido ni dejar de serlo.", e.getMessage());
    }

    @Test
    void noSePuedePasarAPorcentaje() throws Exception {
        TipoIva exento = new TipoIva("Exento prueba", null, false);
        long id = TiposIva.getTiposIva().alta(exento);
        TipoIva tipo = TiposIva.getTiposIva().buscar(id);
        tipo.setPorcentaje(21);
        Exception e = assertThrows(Exception.class, () -> TiposIva.getTiposIva().modificar(tipo));
        assertEquals("Un tipo existente no puede pasar de porcentaje a exento ni al revés.", e.getMessage());
    }

    @Test
    void noSePuedeCambiarElPorcentajeEnUso() throws Exception {
        long id = altaIvaPrueba();
        usarEnFactura(id);
        assertTrue(TiposIva.getTiposIva().enUso(id));
        TipoIva tipo = TiposIva.getTiposIva().buscar(id);
        tipo.setPorcentaje(10);
        Exception e = assertThrows(Exception.class, () -> TiposIva.getTiposIva().modificar(tipo));
        assertEquals("El porcentaje de un tipo que ya aparece en facturas no se puede modificar.",
                e.getMessage());
    }

    @Test
    void porcentajeLibreSinUso() throws Exception {
        long id = altaIvaPrueba();
        assertFalse(TiposIva.getTiposIva().enUso(id));
        TipoIva tipo = TiposIva.getTiposIva().buscar(id);
        tipo.setPorcentaje(10);
        TiposIva.getTiposIva().modificar(tipo);
        assertEquals(10, TiposIva.getTiposIva().buscar(id).getPorcentaje());
    }

    @Test
    void bajaEliminaElTipo() throws Exception {
        long id = altaIvaPrueba();
        TiposIva.getTiposIva().baja(id);
        assertNull(TiposIva.getTiposIva().buscar(id));
    }

    @Test
    void bajaEnUsoFalla() throws Exception {
        long id = altaIvaPrueba();
        usarEnFactura(id);
        Exception e = assertThrows(Exception.class, () -> TiposIva.getTiposIva().baja(id));
        assertEquals("El tipo ya aparece en facturas y no se puede eliminar. Desactívalo en su ficha.",
                e.getMessage());
    }
}
