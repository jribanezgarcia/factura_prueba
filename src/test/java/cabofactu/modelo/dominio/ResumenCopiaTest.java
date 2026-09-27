package cabofactu.modelo.dominio;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** El resumen que se enseña antes de restaurar una copia. */
class ResumenCopiaTest {

    @TempDir
    Path tempDir;

    @Test
    void getTextoConFacturas() {
        ResumenCopia r = new ResumenCopia("Empresa Demo S.L.", "B12345674", 27,
                LocalDate.of(2026, 9, 15), "");

        String texto = r.getTexto();

        assertTrue(texto.contains("Empresa: Empresa Demo S.L."));
        assertTrue(texto.contains("NIF: B12345674"));
        assertTrue(texto.contains("Facturas: 27"));
        assertTrue(texto.contains("Última factura: 15/09/2026"));
    }

    @Test
    void getTextoSinFacturas() {
        ResumenCopia r = new ResumenCopia("Empresa Demo S.L.", "B12345674", 0, null, "");

        assertTrue(r.getTexto().contains("Última factura: ninguna"));
    }

    @Test
    void getTextoSinNif() {
        ResumenCopia r = new ResumenCopia("Empresa Demo S.L.", "", 0, null, "");

        assertTrue(r.getTexto().contains("NIF: sin NIF"));
    }

    @Test
    void getTextoConElLogoPerdido() {
        ResumenCopia r = new ResumenCopia("Empresa Demo S.L.", "B12345674", 0, null, "no_existe.png");

        assertTrue(r.getTexto().contains(
                "El logo de la copia no está en este ordenador: la empresa se quedará sin logo."));
    }

    @Test
    void isLogoPerdidoConLogoVacio() {
        ResumenCopia r = new ResumenCopia("Empresa", "B12345674", 0, null, "");

        assertFalse(r.isLogoPerdido());
    }

    @Test
    void isLogoPerdidoConUnArchivoQueExiste() throws IOException {
        Path logo = tempDir.resolve("logo.png");
        Files.writeString(logo, "imagen");
        ResumenCopia r = new ResumenCopia("Empresa", "B12345674", 0, null, logo.toString());

        assertFalse(r.isLogoPerdido());
    }

    @Test
    void isLogoPerdidoConUnArchivoQueNoExiste() {
        ResumenCopia r = new ResumenCopia("Empresa", "B12345674", 0, null,
                tempDir.resolve("no_existe.png").toString());

        assertTrue(r.isLogoPerdido());
    }

}
