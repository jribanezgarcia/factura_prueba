package cabofactu.vista.utilidades;

import cabofactu.modelo.negocio.sqlite.Conexion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Comprobamos que los errores inesperados quedan anotados en errores.log,
 * con la fecha, el tipo, el mensaje y el detalle de la pila.
 */
class ErroresInesperadosTest {

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        Conexion.setCarpetaRaiz(tempDir);
    }

    private List<String> lineasDelLog() throws Exception {
        return Files.readAllLines(tempDir.resolve("errores.log"));
    }

    @Test
    void guardarAnotaFechaTipoMensajeYPila() throws Exception {
        ErroresInesperados.guardar(new IllegalStateException("Fallo de prueba"));
        List<String> lineas = lineasDelLog();
        assertTrue(lineas.get(0).contains("IllegalStateException: Fallo de prueba"));
        boolean tienePila = false;
        for (String linea : lineas) {
            if (linea.contains("at ")) {
                tienePila = true;
            }
        }
        assertTrue(tienePila);
    }

    @Test
    void dosErroresSeguidosDejanDosEntradas() throws Exception {
        ErroresInesperados.guardar(new Exception("Primero"));
        ErroresInesperados.guardar(new Exception("Segundo"));
        List<String> lineas = lineasDelLog();
        int cabeceras = 0;
        for (String linea : lineas) {
            if (linea.matches("^\\d{4}-\\d{2}-\\d{2}.*")) {
                cabeceras++;
            }
        }
        assertEquals(2, cabeceras);
    }

    @Test
    void tratarFueraDelHiloDeJavaFxGuardaSinMostrarAviso() throws Exception {
        ErroresInesperados.tratar(new Exception("Sin pantalla"));
        List<String> lineas = lineasDelLog();
        assertTrue(lineas.get(0).contains("Exception: Sin pantalla"));
    }

    @Test
    void errorSinMensajeSeApuntaConSuTipo() throws Exception {
        ErroresInesperados.guardar(new NullPointerException());
        List<String> lineas = lineasDelLog();
        assertTrue(lineas.get(0).contains("NullPointerException: NullPointerException"));
    }
}
