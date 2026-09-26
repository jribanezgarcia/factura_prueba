package cabofactu.modelo.dominio;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Comprobamos que FiltrosHistorial se valida solo: las fechas y los importes
 * no pueden quedar al revés, sea cual sea el orden en que se pongan.
 */
class FiltrosHistorialTest {

    @Test
    void sinNingunFiltroVale() throws Exception {
        FiltrosHistorial filtros = new FiltrosHistorial(null, null, null, null, null, null, null);
        assertNull(filtros.getSerie());
        assertEquals("", filtros.getClienteTexto());
    }

    @Test
    void fechaDesdePosteriorALaHastaEnElConstructor() throws Exception {
        Exception e = assertThrows(Exception.class, () -> new FiltrosHistorial(null, null,
                LocalDate.of(2026, 6, 10), LocalDate.of(2026, 1, 1), null, null, null));
        assertEquals("La fecha desde es posterior a la fecha hasta.", e.getMessage());
    }

    @Test
    void fechaHastaAnteriorALaDesdeConLaHastaYaPuesta() throws Exception {
        FiltrosHistorial filtros = new FiltrosHistorial(null, null,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), null, null, null);
        Exception e = assertThrows(Exception.class, () -> filtros.setFechaDesde(LocalDate.of(2027, 1, 1)));
        assertEquals("La fecha desde es posterior a la fecha hasta.", e.getMessage());
    }

    @Test
    void importeDesdeMayorQueElHastaEnElConstructor() throws Exception {
        Exception e = assertThrows(Exception.class, () -> new FiltrosHistorial(null, null, null, null,
                new BigDecimal("100"), new BigDecimal("50"), null));
        assertEquals("El importe desde es mayor que el importe hasta.", e.getMessage());
    }

    @Test
    void importeHastaMenorQueElDesdeConElDesdeYaPuesto() throws Exception {
        FiltrosHistorial filtros = new FiltrosHistorial(null, null, null, null,
                new BigDecimal("50"), new BigDecimal("100"), null);
        Exception e = assertThrows(Exception.class, () -> filtros.setImporteHasta(new BigDecimal("10")));
        assertEquals("El importe desde es mayor que el importe hasta.", e.getMessage());
    }

    @Test
    void errorFechasDevuelveNullSiEstaBienOFaltaUna() {
        assertNull(FiltrosHistorial.errorFechas(null, null));
        assertNull(FiltrosHistorial.errorFechas(LocalDate.of(2026, 1, 1), null));
        assertNull(FiltrosHistorial.errorFechas(null, LocalDate.of(2026, 1, 1)));
        assertNull(FiltrosHistorial.errorFechas(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31)));
    }

    @Test
    void errorImportesDevuelveNullSiEstaBienOFaltaUno() {
        assertNull(FiltrosHistorial.errorImportes(null, null));
        assertNull(FiltrosHistorial.errorImportes(new BigDecimal("50"), null));
        assertNull(FiltrosHistorial.errorImportes(null, new BigDecimal("50")));
        assertNull(FiltrosHistorial.errorImportes(new BigDecimal("50"), new BigDecimal("100")));
    }

    @Test
    void clienteTextoSeGuardaSinEspaciosAlrededor() throws Exception {
        FiltrosHistorial filtros = new FiltrosHistorial(null, "  Ana García  ", null, null, null, null, null);
        assertEquals("Ana García", filtros.getClienteTexto());
    }
}
