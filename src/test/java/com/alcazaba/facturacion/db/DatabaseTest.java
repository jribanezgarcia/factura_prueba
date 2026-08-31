package com.alcazaba.facturacion.db;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
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

    @Test
    void migraInstalacionUnArchivo() throws Exception {
        Path legacy = tempDir.resolve("facturas.db");
        withSchema(legacy);
        String slug = Database.migrarInstalacionUnArchivo();
        assertTrue(slug.equals(Database.SLUG_EMPRESA_INICIAL));
        assertFalse(Files.exists(legacy));
        Path destino = tempDir.resolve(Database.SLUG_EMPRESA_INICIAL).resolve("facturas.db");
        assertTrue(Files.exists(destino));
        assertTrue(Database.getEmpresasDisponibles().contains(Database.SLUG_EMPRESA_INICIAL));
    }

    @Test
    void noMigraSiYaHayEmpresas() throws Exception {
        Files.createDirectories(tempDir.resolve("otra"));
        withSchema(tempDir.resolve("otra").resolve("facturas.db"));
        Path legacy = tempDir.resolve("facturas.db");
        withSchema(legacy);
        assertNull(Database.migrarInstalacionUnArchivo());
        assertTrue(Files.exists(legacy));
    }

    private void withSchema(Path dbPath) throws Exception {
        Files.createDirectories(dbPath.getParent());
        try (Connection c = java.sql.DriverManager.getConnection("jdbc:sqlite:" + dbPath);
             Statement st = c.createStatement()) {
            st.execute("CREATE TABLE serie (id INTEGER PRIMARY KEY AUTOINCREMENT)");
        }
    }
}
