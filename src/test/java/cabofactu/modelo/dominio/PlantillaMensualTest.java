package cabofactu.modelo.dominio;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Comprobamos que PlantillaMensual se valida sola, con los mensajes exactos
 * que ve el usuario en el diálogo de facturación mensual.
 */
class PlantillaMensualTest {

    private Cliente cliente() throws Exception {
        Cliente cliente = new Cliente("Paco", "12345678Z", "Calle Prueba 1", "28001", "Madrid", "Madrid");
        cliente.setId(1L);
        return cliente;
    }

    private Serie serie() throws Exception {
        return new Serie("C", "Cocinas", FormatoNumero.MES, false);
    }

    private TipoIva iva() throws Exception {
        return new TipoIva("IVA 21%", 21, false);
    }

    private List<LineaFactura> lineas() throws Exception {
        return List.of(new LineaFactura(1, new BigDecimal("60.00")));
    }

    private PlantillaMensual plantilla() throws Exception {
        return new PlantillaMensual(cliente(), serie(), 1, 12, ModoDia.FIJO, 15, iva(), lineas());
    }

    @Test
    void sinClienteAvisa() throws Exception {
        Exception e = assertThrows(Exception.class,
                () -> new PlantillaMensual(null, serie(), 1, 12, ModoDia.FIJO, 15, iva(), lineas()));
        assertEquals("Seleccione un cliente.", e.getMessage());
    }

    @Test
    void clienteSinIdAvisa() throws Exception {
        Cliente sinGuardar = new Cliente("Paco", "12345678Z", "Calle Prueba 1", "28001", "Madrid", "Madrid");
        Exception e = assertThrows(Exception.class,
                () -> new PlantillaMensual(sinGuardar, serie(), 1, 12, ModoDia.FIJO, 15, iva(), lineas()));
        assertEquals("Seleccione un cliente.", e.getMessage());
    }

    @Test
    void sinSerieAvisa() throws Exception {
        Exception e = assertThrows(Exception.class,
                () -> new PlantillaMensual(cliente(), null, 1, 12, ModoDia.FIJO, 15, iva(), lineas()));
        assertEquals("Seleccione una serie.", e.getMessage());
    }

    @Test
    void mesFueraDeRangoAvisa() throws Exception {
        Exception e = assertThrows(Exception.class,
                () -> new PlantillaMensual(cliente(), serie(), 0, 12, ModoDia.FIJO, 15, iva(), lineas()));
        assertEquals("El mes debe estar entre enero y diciembre.", e.getMessage());

        Exception otro = assertThrows(Exception.class,
                () -> new PlantillaMensual(cliente(), serie(), 1, 13, ModoDia.FIJO, 15, iva(), lineas()));
        assertEquals("El mes debe estar entre enero y diciembre.", otro.getMessage());
    }

    @Test
    void mesesAlReves() throws Exception {
        Exception e = assertThrows(Exception.class,
                () -> new PlantillaMensual(cliente(), serie(), 9, 3, ModoDia.FIJO, 15, iva(), lineas()));
        assertEquals("El mes de inicio debe ser anterior o igual al mes de fin.", e.getMessage());
    }

    @Test
    void mesesAlRevesPongasElQuePongasPrimero() throws Exception {
        PlantillaMensual plantilla = plantilla();
        plantilla.setMesFin(3);
        Exception e = assertThrows(Exception.class, () -> plantilla.setMesInicio(9));
        assertEquals("El mes de inicio debe ser anterior o igual al mes de fin.", e.getMessage());

        PlantillaMensual otra = plantilla();
        otra.setMesInicio(9);
        Exception otroError = assertThrows(Exception.class, () -> otra.setMesFin(3));
        assertEquals("El mes de inicio debe ser anterior o igual al mes de fin.", otroError.getMessage());
    }

    @Test
    void sinModoDiaAvisa() throws Exception {
        Exception e = assertThrows(Exception.class,
                () -> new PlantillaMensual(cliente(), serie(), 1, 12, null, 15, iva(), lineas()));
        assertEquals("Elija el día del mes.", e.getMessage());
    }

    @Test
    void diaFijoFueraDeRangoAvisaSoloConModoFijo() throws Exception {
        Exception e = assertThrows(Exception.class,
                () -> new PlantillaMensual(cliente(), serie(), 1, 12, ModoDia.FIJO, 32, iva(), lineas()));
        assertEquals("El día del mes debe estar entre 1 y 31.", e.getMessage());

        PlantillaMensual conPrimerDia = new PlantillaMensual(cliente(), serie(), 1, 12,
                ModoDia.PRIMER_DIA, 99, iva(), lineas());
        assertEquals(99, conPrimerDia.getDiaFijo());
    }

    @Test
    void sinTipoIvaAvisa() throws Exception {
        Exception e = assertThrows(Exception.class,
                () -> new PlantillaMensual(cliente(), serie(), 1, 12, ModoDia.FIJO, 15, null, lineas()));
        assertEquals("Seleccione un tipo de IVA.", e.getMessage());
    }

    @Test
    void sinLineasAvisa() throws Exception {
        Exception e = assertThrows(Exception.class,
                () -> new PlantillaMensual(cliente(), serie(), 1, 12, ModoDia.FIJO, 15, iva(), List.of()));
        assertEquals("Añada al menos una línea con descripción.", e.getMessage());

        Exception otro = assertThrows(Exception.class,
                () -> new PlantillaMensual(cliente(), serie(), 1, 12, ModoDia.FIJO, 15, iva(), null));
        assertEquals("Añada al menos una línea con descripción.", otro.getMessage());
    }

    @Test
    void cantidadDeMesesCuentaLosDosExtremos() throws Exception {
        assertEquals(12, plantilla().getCantidadMeses());

        PlantillaMensual unMes = new PlantillaMensual(cliente(), serie(), 3, 3, ModoDia.FIJO, 15, iva(), lineas());
        assertEquals(1, unMes.getCantidadMeses());
    }
}
