package cabofactu.modelo.dominio;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * La empresa del arranque: carpeta obligatoria, nombre que cae en la
 * carpeta, igualdad por carpeta y orden por nombre.
 */
class EmpresaDisponibleTest {

    @Test
    void carpetaVaciaFalla() {
        assertThrows(Exception.class, () -> new EmpresaDisponible("", "Demo"));
        assertThrows(Exception.class, () -> new EmpresaDisponible(null, "Demo"));
        assertThrows(Exception.class, () -> new EmpresaDisponible("   ", "Demo"));
    }

    @Test
    void sinNombreUsaLaCarpeta() throws Exception {
        assertEquals("demo", new EmpresaDisponible("demo", null).getNombre());
        assertEquals("demo", new EmpresaDisponible("demo", "   ").getNombre());
        assertEquals("Empresa Demo S.L.", new EmpresaDisponible("demo", "  Empresa Demo S.L.  ").getNombre());
    }

    @Test
    void textoEsElNombre() throws Exception {
        assertEquals("Mi Empresa", new EmpresaDisponible("mi_empresa", "Mi Empresa").toString());
    }

    @Test
    void igualdadPorCarpeta() throws Exception {
        EmpresaDisponible una = new EmpresaDisponible("demo", "Demo");
        EmpresaDisponible otra = new EmpresaDisponible("demo", "Otro nombre");
        assertTrue(una.equals(otra));
        assertEquals(una.hashCode(), otra.hashCode());
        assertNotEquals(una, new EmpresaDisponible("otra", "Demo"));
    }

    @Test
    void seOrdenaPorNombreSinDistinguirMayusculas() throws Exception {
        List<EmpresaDisponible> lista = new ArrayList<>();
        lista.add(new EmpresaDisponible("zeta", "Zeta"));
        lista.add(new EmpresaDisponible("alfa", "alfa"));
        lista.add(new EmpresaDisponible("beta", "Beta"));
        Collections.sort(lista);
        assertEquals("alfa", lista.get(0).getCarpeta());
        assertEquals("beta", lista.get(1).getCarpeta());
        assertEquals("zeta", lista.get(2).getCarpeta());
    }
}
