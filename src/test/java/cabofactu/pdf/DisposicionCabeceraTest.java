package cabofactu.pdf;

import cabofactu.modelo.dominio.Empresa;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DisposicionCabeceraTest {

    private Empresa empresaBase() throws Exception {
        Empresa empresa = new Empresa("EMPRESA PRUEBA, S.C.", "12345678Z", "Calle Mayor 1", "28001",
                "Madrid", "Madrid", "contacto@empresaprueba.es", "910000000");
        empresa.setActividad("Cocinas y armarios");
        empresa.setCabeceraModo("TEXTO");
        return empresa;
    }

    @Test
    void elTamanoDelLogoEsFijo() throws Exception {
        Empresa empresa = empresaBase();
        assertEquals(DisposicionCabecera.ANCHO_LOGO_FIJO, DisposicionCabecera.anchoLogoEfectivo(empresa), 0.01);
        assertEquals(DisposicionCabecera.ALTO_LOGO_FIJO, DisposicionCabecera.altoLogoEfectivo(empresa), 0.01);
    }

    @Test
    void elTamanoDelLogoNoDependeDeLaEntrada() throws Exception {
        Empresa empresa = empresaBase();
        assertEquals(DisposicionCabecera.ANCHO_LOGO_FIJO, DisposicionCabecera.anchoLogoEfectivo(empresa), 0.01,
                "Con valores absurdos el logo mantiene su caja fija");
        assertEquals(DisposicionCabecera.ALTO_LOGO_FIJO, DisposicionCabecera.altoLogoEfectivo(empresa), 0.01,
                "Con valores absurdos el logo mantiene su caja fija");
    }

    @Test
    void elTamanoDelLogoConCamposNulosEsFijo() throws Exception {
        Empresa empresa = empresaBase();
        assertEquals(DisposicionCabecera.ANCHO_LOGO_FIJO, DisposicionCabecera.anchoLogoEfectivo(empresa), 0.01);
        assertEquals(DisposicionCabecera.ALTO_LOGO_FIJO, DisposicionCabecera.altoLogoEfectivo(empresa), 0.01);
        assertEquals(DisposicionCabecera.ANCHO_LOGO_FIJO, DisposicionCabecera.anchoLogoEfectivo(null), 0.01);
        assertEquals(DisposicionCabecera.ALTO_LOGO_FIJO, DisposicionCabecera.altoLogoEfectivo(null), 0.01);
    }

    @Test
    void elAltoDeCabeceraCreceConLasLineas() {
        assertEquals(108f, DisposicionCabecera.altoCabeceraTexto(0), 0.01,
                "Con pocas lineas se aplica el minimo de 108");
        assertEquals(108f, DisposicionCabecera.altoCabeceraTexto(3), 0.01);
        assertEquals(116f, DisposicionCabecera.altoCabeceraTexto(4), 0.01);
        assertEquals(130f, DisposicionCabecera.altoCabeceraTexto(5), 0.01);
    }

    @Test
    void elAltoDeCabeceraConLogoUsaLaCajaFija() throws Exception {
        Empresa empresa = empresaBase();
        assertEquals(170f, DisposicionCabecera.altoCabeceraLogo(empresa, 5), 0.01,
                "26 + caja fija (120) + 24, mayor que el bloque de informacion, sin depender de offsets");
    }

    @Test
    void lineasDeEmpresaConNifDestacado() throws Exception {
        Empresa empresa = empresaBase();
        empresa.setDireccion("C/ Jesús de Perceval 28");
        empresa.setCp("04006");
        empresa.setLocalidad("Almería");
        empresa.setEmail("info@empresaprueba.es");
        empresa.setTelefono("950000000");

        List<DisposicionCabecera.LineaCabecera> lineas = DisposicionCabecera.lineasEmpresa(empresa);

        assertFalse(lineas.isEmpty());
        long nifs = lineas.stream().filter(l -> l.chipNif).count();
        assertEquals(1, nifs, "Debe haber una unica linea de NIF destacada");
        assertTrue(lineas.stream().anyMatch(l -> l.chipNif && l.texto.contains("12345678Z")));
        assertTrue(lineas.stream().anyMatch(l -> l.texto.equals("Cocinas y armarios")));
    }

    @Test
    void anchoLogoDibujadoRespetaLaCajaFija() {
        assertEquals(120f, DisposicionCabecera.anchoLogoDibujado(500, 500), 0.01);
        assertEquals(240f, DisposicionCabecera.anchoLogoDibujado(1000, 250), 0.01);
        assertEquals(30f, DisposicionCabecera.anchoLogoDibujado(100, 400), 0.01);
    }

    @Test
    void logoConPocaResolucionAvisaSoloSiAmpliaMucho() {
        assertTrue(DisposicionCabecera.logoConPocaResolucion(128, 128));
        assertFalse(DisposicionCabecera.logoConPocaResolucion(256, 256));
        assertFalse(DisposicionCabecera.logoConPocaResolucion(472, 194));
        assertFalse(DisposicionCabecera.logoConPocaResolucion(1254, 1254));
        assertFalse(DisposicionCabecera.logoConPocaResolucion(0, 128));
    }
}