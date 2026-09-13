package com.alcazaba.facturacion;

import com.alcazaba.facturacion.db.CargarDemo;
import com.alcazaba.facturacion.db.Database;
import com.alcazaba.facturacion.service.EmpresaManager;
import com.alcazaba.facturacion.service.PreferenciasGlobales;

import java.io.IOException;
import java.nio.file.Files;

/**
 * Deja lista la carpeta de datos antes de mostrar la pantalla de arranque:
 * crea la carpeta si no existe y, en una instalación nueva, carga la empresa
 * de demostración para poder probar el programa.
 */
public final class PreparacionDatos {

    private PreparacionDatos() {
    }

    /**
     * Crea la carpeta raíz de datos (%APPDATA%\Facturacion) si no existe.
     * Sin ella la aplicación no puede funcionar.
     *
     * @throws IOException si no se puede crear
     */
    public static void crearCarpeta() throws IOException {
        Files.createDirectories(Database.baseDataDir());
    }

    /**
     * Si no existe ninguna empresa, carga la de demostración y la deja como
     * última empresa usada para que salga preseleccionada. Si ya hay alguna
     * empresa no hace nada, así nunca pisa datos ni duplica la demostración.
     *
     * @return true si ha cargado la demostración en esta llamada
     * @throws Exception si falla la carga
     */
    public static boolean cargarDemoSiNoHayEmpresas() throws Exception {
        if (!EmpresaManager.listarEmpresas().isEmpty()) {
            return false;
        }
        CargarDemo.cargar();
        PreferenciasGlobales.set(PreferenciasGlobales.ULTIMA_EMPRESA, CargarDemo.SLUG);
        return true;
    }
}
