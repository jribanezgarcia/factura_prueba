package com.alcazaba.facturacion.pdf;

import com.alcazaba.facturacion.model.Empresa;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CabeceraLayoutTest {

    private Empresa empresaBase() {
        Empresa empresa = new Empresa();
        empresa.setNombre("COMERCIAL ALCAZABA, S.C.");
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
        assertEquals(CabeceraLayout.ANCHO_LOGO_FIJO, CabeceraLayout.anchoLogoEfectivo(empresa), 0.01);
        assertEquals(CabeceraLayout.ALTO_LOGO_FIJO, CabeceraLayout.altoLogoEfectivo(empresa), 0.01);
    }

    @Test
    void elTamanoDelLogoNoDependeDeLaEntrada() {
        Empresa empresa = empresaBase();
        empresa.setLogoAncho(4000);
        empresa.setLogoAlto(250);
        assertEquals(CabeceraLayout.ANCHO_LOGO_FIJO, CabeceraLayout.anchoLogoEfectivo(empresa), 0.01,
                "Con valores absurdos el logo mantiene su caja fija");
        assertEquals(CabeceraLayout.ALTO_LOGO_FIJO, CabeceraLayout.altoLogoEfectivo(empresa), 0.01,
                "Con valores absurdos el logo mantiene su caja fija");
    }

    @Test
    void elTamanoDelLogoConCamposNulosEsFijo() {
        Empresa empresa = empresaBase();
        assertEquals(CabeceraLayout.ANCHO_LOGO_FIJO, CabeceraLayout.anchoLogoEfectivo(empresa), 0.01);
        assertEquals(CabeceraLayout.ALTO_LOGO_FIJO, CabeceraLayout.altoLogoEfectivo(empresa), 0.01);
        assertEquals(CabeceraLayout.ANCHO_LOGO_FIJO, CabeceraLayout.anchoLogoEfectivo(null), 0.01);
        assertEquals(CabeceraLayout.ALTO_LOGO_FIJO, CabeceraLayout.altoLogoEfectivo(null), 0.01);
    }

    @Test
    void elAltoDeCabeceraCreceConLasLineas() {
        assertEquals(108f, CabeceraLayout.altoCabeceraTexto(0), 0.01,
                "Con pocas lineas se aplica el minimo de 108");
        assertEquals(108f, CabeceraLayout.altoCabeceraTexto(3), 0.01);
        assertEquals(112f, CabeceraLayout.altoCabeceraTexto(4), 0.01);
        assertEquals(125f, CabeceraLayout.altoCabeceraTexto(5), 0.01);
    }

    @Test
    void elAltoDeCabeceraConLogoUsaLaCajaFija() {
        Empresa empresa = empresaBase();
        empresa.setLogoAncho(4000);
        empresa.setLogoAlto(250);
        assertEquals(170f, CabeceraLayout.altoCabeceraLogo(empresa, 5), 0.01,
                "26 + caja fija (120) + 24, mayor que el bloque de informacion, sin depender de offsets");
    }

    @Test
    void lineasDeEmpresaConNifDestacado() {
        Empresa empresa = empresaBase();
        empresa.setDireccion("C/ Jesús de Perceval 28");
        empresa.setCp("04006");
        empresa.setLocalidad("Almería");
        empresa.setEmail("info@alcazaba.es");
        empresa.setTelefono("950000000");

        List<CabeceraLayout.LineaCabecera> lineas = CabeceraLayout.lineasEmpresa(empresa);

        assertFalse(lineas.isEmpty());
        long nifs = lineas.stream().filter(l -> l.chipNif).count();
        assertEquals(1, nifs, "Debe haber una unica linea de NIF destacada");
        assertTrue(lineas.stream().anyMatch(l -> l.chipNif && l.texto.contains("B04444444")));
        assertTrue(lineas.stream().anyMatch(l -> l.texto.equals("Cocinas y armarios")));
    }
}