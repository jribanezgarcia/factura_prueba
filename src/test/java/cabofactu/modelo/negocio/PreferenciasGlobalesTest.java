package cabofactu.modelo.negocio;

import cabofactu.modelo.negocio.sqlite.Database;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class PreferenciasGlobalesTest {

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        Database.setDataDir(tempDir);
    }

    @AfterEach
    void tearDown() {
        Database.resetConnection();
    }

    @Test
    void valoresAusentesDevuelvenNuloODefecto() {
        assertNull(PreferenciasGlobales.get("clave_inexistente"));
        assertEquals("por-defecto", PreferenciasGlobales.get("clave_inexistente", "por-defecto"));
        assertNull(PreferenciasGlobales.getDouble("clave_inexistente"));
    }

    @Test
    void lecturaEscrituraEsIdempotente() {
        PreferenciasGlobales.set(PreferenciasGlobales.ULTIMA_EMPRESA, "comercial");
        PreferenciasGlobales.set("clave_decimal", "120.5");
        PreferenciasGlobales.set("clave_entera", "900");
        PreferenciasGlobales.set(PreferenciasGlobales.TEMA, "omarchy");

        assertEquals("comercial", PreferenciasGlobales.get(PreferenciasGlobales.ULTIMA_EMPRESA));
        assertEquals(120.5, PreferenciasGlobales.getDouble("clave_decimal"));
        assertEquals(900.0, PreferenciasGlobales.getDouble("clave_entera"));
        assertEquals("omarchy", PreferenciasGlobales.get(PreferenciasGlobales.TEMA));

        PreferenciasGlobales.set(PreferenciasGlobales.ULTIMA_EMPRESA, "comercial");
        assertEquals("comercial", PreferenciasGlobales.get(PreferenciasGlobales.ULTIMA_EMPRESA));
    }
}
