package cabofactu.modelo.dominio;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Comprobamos que Serie se valida sola: el constructor y los setters
 * comprueban los datos y lanzan Exception con el mensaje para el usuario.
 */
class SerieTest {

    private Serie serieValida() throws Exception {
        return new Serie("C", "Serie general", FormatoNumero.MES, false);
    }

    @Test
    void codigoVacioVale() throws Exception {
        Serie s = serieValida();
        s.setCodigo("   ");
        assertEquals("", s.getCodigo());
        assertEquals("(sin código)", s.getCodigoTexto());
    }

    @Test
    void codigoConSimbolosFalla() throws Exception {
        Serie s = serieValida();
        Exception e = assertThrows(Exception.class, () -> s.setCodigo("B%"));
        assertEquals("El código de la serie solo puede tener letras y números.", e.getMessage());
    }

    @Test
    void codigoConEspaciosFalla() throws Exception {
        Serie s = serieValida();
        Exception e = assertThrows(Exception.class, () -> s.setCodigo("A B"));
        assertEquals("El código de la serie solo puede tener letras y números.", e.getMessage());
    }

    @Test
    void codigoSeGuardaEnMayusculas() throws Exception {
        Serie s = serieValida();
        s.setCodigo("ab12");
        assertEquals("AB12", s.getCodigo());
    }

    @Test
    void formatoNuloFalla() throws Exception {
        Serie s = serieValida();
        Exception e = assertThrows(Exception.class, () -> s.setFormato(null));
        assertEquals("Indique el formato del número.", e.getMessage());
    }

    @Test
    void descripcionVaciaSeGuardaVacia() throws Exception {
        Serie s = serieValida();
        s.setDescripcion("   ");
        assertEquals("", s.getDescripcion());
    }

    @Test
    void gettersDeTexto() throws Exception {
        Serie s = serieValida();
        assertEquals("C", s.getCodigoTexto());
        assertEquals("No", s.getRectificativaTexto());
        assertEquals(FormatoNumero.MES.toString(), s.getFormatoTexto());
        assertEquals("C (Serie general)", s.toString());
        Serie rectificativa = new Serie("R", "", FormatoNumero.NINGUNO, true);
        assertEquals("Sí", rectificativa.getRectificativaTexto());
    }

    @Test
    void constructorCopia() throws Exception {
        Serie original = serieValida();
        original.setId(7L);
        Serie copia = new Serie(original);
        assertNotSame(original, copia);
        assertEquals(original.getId(), copia.getId());
        assertEquals(original.getCodigo(), copia.getCodigo());
        assertEquals(original.getDescripcion(), copia.getDescripcion());
        assertEquals(original.getFormato(), copia.getFormato());
        assertEquals(original.isEsRectificativa(), copia.isEsRectificativa());
        assertEquals(original, copia);
        copia.setCodigo("D");
        assertNotEquals(original.getCodigo(), copia.getCodigo());
    }

    @Test
    void equalsPorId() throws Exception {
        Serie una = serieValida();
        una.setId(3L);
        Serie otra = new Serie("D", "Otra", FormatoNumero.ANIO, false);
        otra.setId(3L);
        assertEquals(una, otra);
        assertEquals(una.hashCode(), otra.hashCode());
        otra.setId(4L);
        assertNotEquals(una, otra);
    }

    @Test
    void cadaErrorXIgualaASuSetter() throws Exception {
        Serie s = serieValida();
        Exception e = assertThrows(Exception.class, () -> s.setCodigo("B%"));
        assertEquals(Serie.errorCodigo("B%"), e.getMessage());
        assertNull(Serie.errorCodigo(""));
        assertNull(Serie.errorCodigo("C12"));
        e = assertThrows(Exception.class, () -> s.setFormato(null));
        assertEquals(Serie.errorFormato(null), e.getMessage());
        assertNull(Serie.errorFormato(FormatoNumero.MES));
        assertFalse(s.isEsRectificativa());
    }
}
