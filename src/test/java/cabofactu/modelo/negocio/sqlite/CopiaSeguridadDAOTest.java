package cabofactu.modelo.negocio.sqlite;

import cabofactu.fichero.CopiaSeguridad;
import cabofactu.modelo.negocio.Empresas;
import cabofactu.modelo.negocio.Sesion;
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
        Empresas.getEmpresas().cerrar();
        Empresas.getEmpresas().alta("Pruebas Copia");
        Empresas.getEmpresas().abrir("pruebas_copia", LocalDate.now());
        repo = new CopiaSeguridadDAO();
    }

    @AfterEach
    void tearDown() {
        Conexion.cerrarConexion();
    }

    private void unaFactura() throws Exception {
        try (Statement st = Conexion.establecerConexion().createStatement()) {
            st.executeUpdate("INSERT INTO serie (id, codigo) VALUES (1, 'C')");
            st.executeUpdate("INSERT INTO factura (id, serie_id, anio, correlativo, numero, fecha, estado, "
                    + "cli_nombre, cli_nif, descuento, base_total, iva_total, total) VALUES (1, 1, 2026, 1, "
                    + "'C-1/9', '2026-09-01', 'EMITIDA', 'Pruebas', 'B12345674', 0, '100.00', '21.00', '121.00')");
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
        assertThrows(Exception.class, () -> repo.leerResumen(texto));
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
