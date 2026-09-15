package cabofactu.pdf;

import cabofactu.modelo.dominio.Empresa;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DisposicionCabeceraTest {

    private Empresa empresaBase() {
        Empresa empresa = new Empresa();
        empresa.setNombre("EMPRESA PRUEBA, S.C.");
        empresa.setNif("B04444444");
        empresa.setActividad("Cocinas y armarios");
        empresa.setCabeceraModo("TEXTO");
        return empresa;
    }

    @Test
    void elTamanoDelLogoEsFijo() {
        Empresa empresa = empresaBase();
        empresa.setLogoAncho(120);
        empresa.setLogoAlto(60);
        assertEquals(DisposicionCabecera.ANCHO_LOGO_FIJO, DisposicionCabecera.anchoLogoEfectivo(empresa), 0.01);
        assertEquals(DisposicionCabecera.ALTO_LOGO_FIJO, DisposicionCabecera.altoLogoEfectivo(empresa), 0.01);
    }

    @Test
    void elTamanoDelLogoNoDependeDeLaEntrada() {
        Empresa empresa = empresaBase();
        empresa.setLogoAncho(4000);
        empresa.setLogoAlto(250);
        assertEquals(DisposicionCabecera.ANCHO_LOGO_FIJO, DisposicionCabecera.anchoLogoEfectivo(empresa), 0.01,
                "Con valores absurdos el logo mantiene su caja fija");
        assertEquals(DisposicionCabecera.ALTO_LOGO_FIJO, DisposicionCabecera.altoLogoEfectivo(empresa), 0.01,
                "Con valores absurdos el logo mantiene su caja fija");
    }

    @Test
    void elTamanoDelLogoConCamposNulosEsFijo() {
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
    void elAltoDeCabeceraConLogoUsaLaCajaFija() {
        Empresa empresa = empresaBase();
        empresa.setLogoAncho(4000);
        empresa.setLogoAlto(250);
        assertEquals(170f, DisposicionCabecera.altoCabeceraLogo(empresa, 5), 0.01,
                "26 + caja fija (120) + 24, mayor que el bloque de informacion, sin depender de offsets");
    }

    @Test
    void lineasDeEmpresaConNifDestacado() {
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
        assertTrue(lineas.stream().anyMatch(l -> l.chipNif && l.texto.contains("B04444444")));
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