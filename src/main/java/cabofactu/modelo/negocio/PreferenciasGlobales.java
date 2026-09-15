package cabofactu.modelo.negocio;

import cabofactu.modelo.negocio.sqlite.Conexion;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Preferencias compartidas entre empresas (tema, ultima empresa).
 * Se guardan en BASE_DATA_DIR/preferencias.properties, fuera de la BD.
 */
public final class PreferenciasGlobales {

    public static final String ULTIMA_EMPRESA = "ultima_empresa";
    public static final String TEMA = "tema";

    private PreferenciasGlobales() {
    }

    public static String get(String clave) {
        return cargar().getProperty(clave);
    }

    public static String get(String clave, String porDefecto) {
        String v = get(clave);
        return v == null || v.isBlank() ? porDefecto : v;
    }

    public static Double getDouble(String clave) {
        String v = get(clave);
        if (v == null || v.isBlank()) {
            return null;
        }
        try {
            return Double.valueOf(v.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static void set(String clave, String valor) {
        Properties p = cargar();
        p.setProperty(clave, valor);
        guardar(p);
    }

    private static Path archivo() {
        return Conexion.carpetaRaiz().resolve("preferencias.properties");
    }

    private static Properties cargar() {
        Properties p = new Properties();
        Path f = archivo();
        if (Files.exists(f)) {
            try (InputStream in = Files.newInputStream(f)) {
                p.load(in);
            } catch (IOException ignored) {
            }
        }
        return p;
    }

    private static void guardar(Properties p) {
        Path f = archivo();
        try {
            Files.createDirectories(f.getParent());
            try (OutputStream out = Files.newOutputStream(f)) {
                p.store(out, null);
            }
        } catch (IOException ignored) {
        }
    }
}