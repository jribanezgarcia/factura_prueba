package com.alcazaba.facturacion.service;

import com.alcazaba.facturacion.db.Database;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Copias de seguridad de la base de datos SQLite mediante VACUUM INTO, que
 * produce una copia consistente en caliente. El archivo lleva un timestamp
 * (facturas_AAAAMMDD_HHMMSS.db).
 */
public class BackupService {

    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    /**
     * Crea la copia en la carpeta indicada y devuelve la ruta generada.
     */
    public Path crearBackup(Path carpetaDestino) throws SQLException, IOException {
        Files.createDirectories(carpetaDestino);
        String nombre = "facturas_" + LocalDateTime.now().format(STAMP) + ".db";
        Path archivo = carpetaDestino.resolve(nombre);

        String ruta = archivo.toString().replace("'", "''");
        try (Statement st = Database.getConnection().createStatement()) {
            st.execute("VACUUM INTO '" + ruta + "'");
        }
        return archivo;
    }
}
