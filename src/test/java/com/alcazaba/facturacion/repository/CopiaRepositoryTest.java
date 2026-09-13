package com.alcazaba.facturacion.repository;

import com.alcazaba.facturacion.db.Database;
import com.alcazaba.facturacion.service.BackupService;
import com.alcazaba.facturacion.service.EmpresaManager;
import com.alcazaba.facturacion.service.Sesion;
import com.alcazaba.facturacion.service.ValidationException;
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

class CopiaRepositoryTest {

    @TempDir
    Path tempDir;

    private CopiaRepository repo;

    @BeforeEach
    void setUp() throws Exception {
        Database.setDataDir(tempDir);
        Database.resetConnection();
        Sesion.reiniciar();
        EmpresaManager.crearEmpresa("Pruebas Copia");
        EmpresaManager.conectar("pruebas_copia", LocalDate.now());
        repo = new CopiaRepository();
    }

    @AfterEach
    void tearDown() {
        Database.resetConnection();
    }

    private void unaFactura() throws Exception {
        try (Statement st = Database.getConnection().createStatement()) {
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
        BackupService.ResumenBackup r = repo.leerResumen(copia);
        assertEquals(1, r.numFacturas());
    }

    @Test
    void leerResumenDeTextoLanzaValidationException() throws Exception {
        Path texto = tempDir.resolve("noes.db");
        Files.writeString(texto, "esto no es una base de datos");
        assertThrows(ValidationException.class, () -> repo.leerResumen(texto));
    }

    @Test
    void reemplazarBaseActivaBorraWalHuerfano() throws Exception {
        unaFactura();
        Path copia = tempDir.resolve("c.db");
        repo.crearCopia(copia);
        Path wal = Database.dataDir().resolve("facturas.db-wal");
        Files.writeString(wal, "huerfano");
        repo.reemplazarBaseActiva(copia);
        assertFalse(Files.exists(wal));
    }
}
