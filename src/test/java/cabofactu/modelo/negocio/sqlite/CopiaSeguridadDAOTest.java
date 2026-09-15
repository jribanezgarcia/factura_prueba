package cabofactu.modelo.negocio.sqlite;

import cabofactu.fichero.CopiaSeguridad;
import cabofactu.modelo.negocio.Empresas;
import cabofactu.modelo.negocio.Sesion;
import cabofactu.modelo.negocio.ValidacionException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Statement;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CopiaSeguridadDAOTest {

    @TempDir
    Path tempDir;

    private CopiaSeguridadDAO repo;

    @BeforeEach
    void setUp() throws Exception {
        Conexion.setCarpetaRaiz(tempDir);
        Conexion.cerrarConexion();
        Sesion.reiniciar();
        Empresas.crearEmpresa("Pruebas Copia");
        Empresas.conectar("pruebas_copia", LocalDate.now());
        repo = new CopiaSeguridadDAO();
    }

    @AfterEach
    void tearDown() {
        Conexion.cerrarConexion();
    }

    private void unaFactura() throws Exception {
        try (Statement st = Conexion.establecerConexion().createStatement()) {
            st.executeUpdate("INSERT INTO serie (id, codigo) VALUES (1, 'C')");
            st.executeUpdate("INSERT INTO factura (id, serie_id, correlativo) VALUES (1, 1, 1)");
        }
    }

    @Test
    void crearCopiaGeneraFichero() throws Exception {
        unaFactura();
        Path destino = tempDir.resolve("copias").resolve("c.db");
        Files.createDirectories(destino.getParent());
        repo.crearCopia(destino);
        assertTrue(Files.isRegularFile(destino));
    }

    @Test
    void leerResumenDevuelveNumeroDeFacturas() throws Exception {
        unaFactura();
        Path copia = tempDir.resolve("c.db");
        repo.crearCopia(copia);
        CopiaSeguridad.ResumenCopia r = repo.leerResumen(copia);
        assertEquals(1, r.numFacturas());
    }

    @Test
    void leerResumenDeTextoLanzaValidationException() throws Exception {
        Path texto = tempDir.resolve("noes.db");
        Files.writeString(texto, "esto no es una base de datos");
        assertThrows(ValidacionException.class, () -> repo.leerResumen(texto));
    }

    @Test
    void reemplazarBaseActivaBorraWalHuerfano() throws Exception {
        unaFactura();
        Path copia = tempDir.resolve("c.db");
        repo.crearCopia(copia);
        Path wal = Conexion.carpetaEmpresa().resolve("facturas.db-wal");
        Files.writeString(wal, "huerfano");
        repo.reemplazarBaseActiva(copia);
        assertFalse(Files.exists(wal));
    }
}
