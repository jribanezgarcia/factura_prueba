package cabofactu;

import cabofactu.modelo.negocio.sqlite.Conexion;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InstanciaUnicaTest {

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        Conexion.setCarpetaRaiz(tempDir);
    }

    @AfterEach
    void tearDown() {
        InstanciaUnica.liberar();
        Conexion.cerrarConexion();
    }

    @Test
    void adquirirCreaElFicheroDeBloqueo() throws Exception {
        assertTrue(InstanciaUnica.adquirir());
        assertTrue(Files.exists(Conexion.rutaBloqueoGlobal()));
    }

    @Test
    void adquirirDosVecesDevuelveTrue() throws Exception {
        assertTrue(InstanciaUnica.adquirir());
        assertTrue(InstanciaUnica.adquirir());
    }

    @Test
    void conElFicheroBloqueadoDevuelveFalse() throws Exception {
        FileChannel otro = FileChannel.open(Conexion.rutaBloqueoGlobal(),
                StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        FileLock suyo = otro.tryLock();
        try {
            assertFalse(InstanciaUnica.adquirir());
        } finally {
            suyo.release();
            otro.close();
        }
    }

    @Test
    void trasLiberarSePuedeVolverAAdquirir() throws Exception {
        assertTrue(InstanciaUnica.adquirir());
        InstanciaUnica.liberar();
        assertTrue(InstanciaUnica.adquirir());
    }

    @Test
    void rutaImposibleLanzaIOException() throws Exception {
        Path fichero = tempDir.resolve("fichero");
        Files.writeString(fichero, "no es una carpeta");
        Conexion.setCarpetaRaiz(fichero);
        assertThrows(IOException.class, InstanciaUnica::adquirir);
    }
}
