package com.alcazaba.facturacion.db;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DatabaseTest {

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        Database.setDataDir(tempDir);
        Database.resetConnection();
    }

    @AfterEach
    void tearDown() {
        Database.resetConnection();
    }

    @Test
    void setEmpresaActivaApuntaASubcarpeta() throws Exception {
        Database.setEmpresaActiva("mi_empresa");
        assertTrue(Database.dbPath().startsWith(tempDir.resolve("mi_empresa")));
        Database.getConnection();
        assertTrue(Files.exists(Database.dbPath()));
    }
}
