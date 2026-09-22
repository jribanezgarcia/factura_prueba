package cabofactu.modelo.negocio;

import cabofactu.modelo.dominio.TipoRetencion;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Comprobamos el singleton de tipos de retención contra una base de datos
 * temporal: alta, listados, búsqueda, modificar con su guarda y si está en uso.
 */
class TiposRetencionTest {

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

    private TipoRetencion irpf15() throws Exception {
        return new TipoRetencion("IRPF 15%", 15);
    }

    private long altaIrpf15() throws Exception {
        return TiposRetencion.getTiposRetencion().alta(irpf15());
    }

    private void usarEnFactura(long tipoId) throws Exception {
        try (Statement st = Conexion.establecerConexion().createStatement()) {
            st.executeUpdate("INSERT INTO serie (codigo, descripcion) VALUES ('C', 'Serie C')");
            st.executeUpdate("INSERT INTO factura (serie_id, correlativo) VALUES (1, 1)");
            st.executeUpdate("INSERT INTO factura_version (factura_id, version_num, numero, fecha_factura, "
                    + "fecha_guardado, estado, tipo_retencion_id) VALUES (1, 1, 'C-1/8', '2026-01-01', "
                    + "'2026-01-01', 'EMITIDA', " + tipoId + ")");
        }
    }

    @Test
    void altaYListado() throws Exception {
        long id = altaIrpf15();
        TipoRetencion guardada = TiposRetencion.getTiposRetencion().buscar(id);
        assertNotNull(guardada);
        assertEquals("IRPF 15%", guardada.getNombre());
        assertEquals(15, guardada.getPorcentaje());
        List<TipoRetencion> activas = TiposRetencion.getTiposRetencion().listado(true);
        assertEquals(1, activas.size());
    }

    @Test
    void listadoSoloActivos() throws Exception {
        altaIrpf15();
        TipoRetencion inactiva = new TipoRetencion("Inactiva", 7);
        inactiva.setActivo(false);
        TiposRetencion.getTiposRetencion().alta(inactiva);
        assertEquals(1, TiposRetencion.getTiposRetencion().listado(true).size());
        assertEquals(2, TiposRetencion.getTiposRetencion().listado(false).size());
    }

    @Test
    void buscarInexistenteDevuelveNull() throws Exception {
        assertNull(TiposRetencion.getTiposRetencion().buscar(9999L));
    }

    @Test
    void modificarGuardaElNombre() throws Exception {
        long id = altaIrpf15();
        TipoRetencion tipo = TiposRetencion.getTiposRetencion().buscar(id);
        tipo.setNombre("IRPF profesional");
        TiposRetencion.getTiposRetencion().modificar(tipo);
        assertEquals("IRPF profesional", TiposRetencion.getTiposRetencion().buscar(id).getNombre());
    }

    @Test
    void modificarInexistenteFalla() throws Exception {
        TipoRetencion tipo = irpf15();
        tipo.setId(9999L);
        Exception e = assertThrows(Exception.class,
                () -> TiposRetencion.getTiposRetencion().modificar(tipo));
        assertEquals("No se ha encontrado el tipo de retención.", e.getMessage());
    }

    @Test
    void noSePuedeCambiarElPorcentajeEnUso() throws Exception {
        long id = altaIrpf15();
        usarEnFactura(id);
        assertTrue(TiposRetencion.getTiposRetencion().enUso(id));
        TipoRetencion tipo = TiposRetencion.getTiposRetencion().buscar(id);
        tipo.setPorcentaje(19);
        Exception e = assertThrows(Exception.class,
                () -> TiposRetencion.getTiposRetencion().modificar(tipo));
        assertEquals("El porcentaje de un tipo que ya aparece en facturas no se puede modificar.",
                e.getMessage());
    }

    @Test
    void porcentajeLibreSinUso() throws Exception {
        long id = altaIrpf15();
        assertFalse(TiposRetencion.getTiposRetencion().enUso(id));
        TipoRetencion tipo = TiposRetencion.getTiposRetencion().buscar(id);
        tipo.setPorcentaje(19);
        TiposRetencion.getTiposRetencion().modificar(tipo);
        assertEquals(19, TiposRetencion.getTiposRetencion().buscar(id).getPorcentaje());
    }

    @Test
    void bajaEliminaElTipo() throws Exception {
        long id = altaIrpf15();
        TiposRetencion.getTiposRetencion().baja(id);
        assertNull(TiposRetencion.getTiposRetencion().buscar(id));
    }

    @Test
    void bajaEnUsoFalla() throws Exception {
        long id = altaIrpf15();
        usarEnFactura(id);
        Exception e = assertThrows(Exception.class, () -> TiposRetencion.getTiposRetencion().baja(id));
        assertEquals("El tipo ya aparece en facturas y no se puede eliminar. Desactívalo en su ficha.",
                e.getMessage());
    }
}
