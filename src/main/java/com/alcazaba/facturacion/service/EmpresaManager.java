package com.alcazaba.facturacion.service;

import com.alcazaba.facturacion.db.Database;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Normalizer;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

/**
 * Gestion de empresas: listado, creacion, conexion, eliminacion y catalogo
 * (BASE_DATA_DIR/empresas.properties) que mapea el slug al nombre visible.
 */
public final class EmpresaManager {

    private static final String CATALOGO = "empresas.properties";

    public record EmpresaInfo(String slug, String nombre) {
    }

    private EmpresaManager() {
    }

    public static List<EmpresaInfo> listarEmpresas() throws IOException {
        Properties catalogo = cargarCatalogo();
        List<EmpresaInfo> lista = new ArrayList<>();
        for (String slug : Database.getEmpresasDisponibles()) {
            String nombre = catalogo.getProperty(claveNombre(slug));
            lista.add(new EmpresaInfo(slug, nombre == null || nombre.isBlank() ? slug : nombre));
        }
        lista.sort(Comparator.comparing(EmpresaInfo::nombre));
        return lista;
    }

    /**
     * Crea una empresa nueva: carpeta, base de datos vacia con todas las
     * migraciones, entrada en el catalogo y la deja como activa.
     */
    public static EmpresaInfo crearEmpresa(String nombre) throws Exception {
        String slug = slugDe(nombre);
        if (Database.getEmpresasDisponibles().contains(slug)) {
            throw new IllegalArgumentException("Ya existe una empresa con esa carpeta de datos: " + slug);
        }
        Database.setEmpresaActiva(slug);
        Database.resetConnection();
        Database.getConnection();

        Properties catalogo = cargarCatalogo();
        catalogo.setProperty(claveNombre(slug), nombre.trim());
        guardarCatalogo(catalogo);
        PreferenciasGlobales.set(PreferenciasGlobales.ULTIMA_EMPRESA, slug);
        Sesion.inicializar(slug, Sesion.fechaTrabajo() != null ? Sesion.fechaTrabajo() : LocalDate.now());
        return new EmpresaInfo(slug, nombre.trim());
    }

    /**
     * Conecta con una empresa existente: la fija como activa, abre y migra su
     * base de datos e inicializa la sesion con la fecha de trabajo.
     */
    public static void conectar(String slug, LocalDate fecha) throws Exception {
        Database.setEmpresaActiva(slug);
        Database.resetConnection();
        Database.getConnection();
        Sesion.inicializar(slug, fecha);
        PreferenciasGlobales.set(PreferenciasGlobales.ULTIMA_EMPRESA, slug);
    }

    /**
     * Elimina una empresa distinta de la activa: quita su entrada del catalogo
     * y borra su carpeta de datos.
     */
    public static void eliminarEmpresa(String slug) throws Exception {
        if (slug.equals(Sesion.empresaSlug())) {
            throw new IllegalArgumentException("La empresa activa no se puede eliminar.");
        }
        Properties catalogo = cargarCatalogo();
        catalogo.remove(claveNombre(slug));
        guardarCatalogo(catalogo);
        Path carpeta = Database.baseDataDir().resolve(slug);
        if (Files.exists(carpeta)) {
            borrarRecursivo(carpeta);
        }
    }

    /**
     * Registra el nombre visible de una empresa ya existente (migracion o
     * rescatada sin entrada de catalogo).
     */
    public static void registrarNombre(String slug, String nombre) throws IOException {
        Properties catalogo = cargarCatalogo();
        catalogo.setProperty(claveNombre(slug), nombre);
        guardarCatalogo(catalogo);
    }

    /**
     * Slug de una empresa a partir de su nombre: minusculas, sin acentos, los
     * espacios y simbolos se sustituyen por guiones bajos.
     */
    public static String slugDe(String nombre) {
        String limpio = Normalizer.normalize(nombre == null ? "" : nombre, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "_")
                .replaceAll("^_+|_+$", "");
        return limpio.isBlank() ? "empresa" : limpio;
    }

    private static String claveNombre(String slug) {
        return slug + ".nombre";
    }

    private static Path archivoCatalogo() {
        return Database.baseDataDir().resolve(CATALOGO);
    }

    private static Properties cargarCatalogo() throws IOException {
        Properties p = new Properties();
        Path f = archivoCatalogo();
        if (Files.exists(f)) {
            try (InputStream in = Files.newInputStream(f)) {
                p.load(in);
            }
        }
        return p;
    }

    private static void guardarCatalogo(Properties p) throws IOException {
        Path f = archivoCatalogo();
        Files.createDirectories(f.getParent());
        try (OutputStream out = Files.newOutputStream(f)) {
            p.store(out, null);
        }
    }

    private static void borrarRecursivo(Path dir) throws IOException {
        if (Files.isDirectory(dir)) {
            try (var stream = Files.walk(dir)) {
                stream.sorted(Comparator.reverseOrder()).forEach(p -> {
                    try {
                        Files.deleteIfExists(p);
                    } catch (IOException ignored) {
                    }
                });
            }
        }
    }
}