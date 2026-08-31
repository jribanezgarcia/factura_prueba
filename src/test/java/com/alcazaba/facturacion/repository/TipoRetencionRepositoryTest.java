package com.alcazaba.facturacion.repository;

import com.alcazaba.facturacion.db.Database;
import com.alcazaba.facturacion.model.TipoRetencion;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TipoRetencionRepositoryTest {

    @TempDir
    Path tempDir;

    private TipoRetencionRepository repository;

    @BeforeEach
    void setUp() throws Exception {
        Database.setDataDir(tempDir);
        Database.resetConnection();
        Database.getConnection();
        repository = new TipoRetencionRepository();
    }

    @AfterEach
    void tearDown() {
        Database.resetConnection();
    }

    @Test
    void insertarYListar() throws Exception {
        TipoRetencion t = new TipoRetencion();
        t.setNombre("IRPF 15%");
        t.setPorcentaje(15);
        t.setActivo(true);
        long id = repository.insertar(t);

        TipoRetencion guardada = repository.getById(id);
        assertNotNull(guardada);
        assertEquals("IRPF 15%", guardada.getNombre());
        assertEquals(15, guardada.getPorcentaje());

        List<TipoRetencion> activas = repository.listar(true);
        assertEquals(1, activas.size());

        repository.setActivo(id, false);
        assertTrue(repository.listar(true).isEmpty());
        assertFalse(repository.getById(id).isActivo());
    }

    @Test
    void actualizarModificaNombreYPorcentaje() throws Exception {
        TipoRetencion t = new TipoRetencion();
        t.setNombre("IRPF 19%");
        t.setPorcentaje(19);
        t.setActivo(true);
        t.setId(repository.insertar(t));

        t.setNombre("IRPF 20%");
        t.setPorcentaje(20);
        repository.actualizar(t);

        TipoRetencion actualizada = repository.getById(t.getId());
        assertEquals("IRPF 20%", actualizada.getNombre());
        assertEquals(20, actualizada.getPorcentaje());
    }

    @Test
    void enUsoDetectaHistorico() throws Exception {
        TipoRetencion t = new TipoRetencion();
        t.setNombre("IRPF 15%");
        t.setPorcentaje(15);
        t.setActivo(true);
        t.setId(repository.insertar(t));

        assertFalse(repository.enUso(t.getId()));
    }

    @Test
    void listarSoloActivosExcluyeInactivas() throws Exception {
        TipoRetencion activa = new TipoRetencion();
        activa.setNombre("Activa");
        activa.setPorcentaje(15);
        activa.setActivo(true);
        repository.insertar(activa);

        TipoRetencion inactiva = new TipoRetencion();
        inactiva.setNombre("Inactiva");
        inactiva.setPorcentaje(7);
        inactiva.setActivo(false);
        repository.insertar(inactiva);

        assertEquals(1, repository.listar(true).size());
        assertEquals(2, repository.listar(false).size());
    }

    @Test
    void getByIdInexistenteDevuelveNull() throws Exception {
        assertNull(repository.getById(9999L));
    }
}
