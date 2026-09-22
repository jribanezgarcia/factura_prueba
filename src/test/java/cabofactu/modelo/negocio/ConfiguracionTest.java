package cabofactu.modelo.negocio;

import cabofactu.modelo.dominio.Empresa;
import cabofactu.modelo.negocio.sqlite.Conexion;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Comprobamos el singleton de configuración contra una base de datos temporal:
 * la empresa solo se lee si está completa, se guarda entera y las preferencias
 * se guardan y se leen por su clave.
 */
class ConfiguracionTest {

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

    private Empresa empresaValida() throws Exception {
        return new Empresa("Talleres Ejemplo S.L.", "12345678Z", "Calle Mayor 1", "28001",
                "Madrid", "Madrid", "taller@ejemplo.es", "910000000");
    }

    private void ponerNifDirecto(String nif) throws Exception {
        try (Statement st = Conexion.establecerConexion().createStatement()) {
            st.executeUpdate("UPDATE empresa SET nif = '" + nif + "' WHERE id = 1");
        }
    }

    @Test
    void filaVaciaDevuelveNull() throws Exception {
        assertNull(Configuracion.getConfiguracion().buscarEmpresa());
    }

    @Test
    void datoNoValidoDevuelveNull() throws Exception {
        Configuracion.getConfiguracion().modificarEmpresa(empresaValida());
        ponerNifDirecto("MALO");
        assertNull(Configuracion.getConfiguracion().buscarEmpresa());
    }

    @Test
    void modificarYBuscarDevuelvenLaEmpresa() throws Exception {
        Empresa empresa = empresaValida();
        empresa.setActividad("Talleres");
        empresa.setCabeceraModo(Empresa.CABECERA_LOGO);
        empresa.setLogoPath("/tmp/logo.png");
        empresa.setPieLegal("Texto legal.");
        Configuracion.getConfiguracion().modificarEmpresa(empresa);
        Empresa leida = Configuracion.getConfiguracion().buscarEmpresa();
        assertEquals("Talleres Ejemplo S.L.", leida.getNombre());
        assertEquals("12345678Z", leida.getNif());
        assertEquals("Calle Mayor 1", leida.getDireccion());
        assertEquals("28001", leida.getCp());
        assertEquals("Madrid", leida.getLocalidad());
        assertEquals("Madrid", leida.getProvincia());
        assertEquals("taller@ejemplo.es", leida.getEmail());
        assertEquals("910000000", leida.getTelefono());
        assertEquals("Talleres", leida.getActividad());
        assertTrue(leida.isCabeceraLogo());
        assertEquals("/tmp/logo.png", leida.getLogoPath());
        assertEquals("Texto legal.", leida.getPieLegal());
    }

    @Test
    void preferenciasSeGuardanYSeLeen() throws Exception {
        assertNull(Configuracion.getConfiguracion().preferencia("tema"));
        Configuracion.getConfiguracion().guardarPreferencia("tema", "omarchy");
        assertEquals("omarchy", Configuracion.getConfiguracion().preferencia("tema"));
        Configuracion.getConfiguracion().guardarPreferencia("tema", "esmeralda");
        assertEquals("esmeralda", Configuracion.getConfiguracion().preferencia("tema"));
    }
}
