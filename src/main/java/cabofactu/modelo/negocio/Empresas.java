package cabofactu.modelo.negocio;

import cabofactu.modelo.dominio.EmpresaDisponible;
import cabofactu.modelo.negocio.sqlite.Conexion;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Normalizer;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

/**
 * Las empresas de la aplicación: cada una es una carpeta con su base de datos,
 * y su nombre visible se guarda en el catálogo empresas.properties.
 */
public class Empresas {

    private static final String CATALOGO = "empresas.properties";

    private static Empresas empresas;

    private Empresas() {
    }

    public static Empresas getEmpresas() {
        if (empresas == null) {
            empresas = new Empresas();
        }
        return empresas;
    }

    /** Las empresas ordenadas por nombre, con el que guarda el catálogo. */
    public List<EmpresaDisponible> listado() throws Exception {
        Properties catalogo = cargarCatalogo();
        List<EmpresaDisponible> lista = new ArrayList<>();
        for (String carpeta : Conexion.getEmpresasDisponibles()) {
            lista.add(new EmpresaDisponible(carpeta, catalogo.getProperty(claveNombre(carpeta))));
        }
        Collections.sort(lista);
        return lista;
    }

    /**
     * Creamos una empresa nueva: carpeta, base de datos vacía con sus tablas
     * y entrada en el catálogo. No cambia la empresa abierta ni la
     * conexión en curso.
     */
    public EmpresaDisponible alta(String nombre) throws Exception {
        if (nombre == null || nombre.isBlank()) {
            throw new Exception("Indique el nombre de la empresa.");
        }
        String carpeta = carpetaDe(nombre);
        if (Conexion.getEmpresasDisponibles().contains(carpeta)) {
            throw new Exception("Ya existe una empresa con ese nombre de carpeta: " + carpeta + ".");
        }
        Path destino = Conexion.rutaBaseDe(carpeta);
        Conexion.crearBase(destino);

        Properties catalogo = cargarCatalogo();
        catalogo.setProperty(claveNombre(carpeta), nombre.trim());
        guardarCatalogo(catalogo);
        return new EmpresaDisponible(carpeta, nombre.trim());
    }

    /**
     * Quitamos una empresa que no está en uso: borramos su carpeta de datos y
     * después la sacamos del catálogo, para que si la carpeta no se puede borrar
     * la empresa siga en la lista con su nombre.
     */
    public void baja(String carpeta) throws Exception {
        if (carpeta.equals(Sesion.getSesion().getCarpetaEmpresa())) {
            throw new Exception("La empresa en uso no se puede eliminar.");
        }
        File hija = Conexion.carpetaRaiz().resolve(carpeta).toFile();
        if (hija.exists()) {
            borrarCarpeta(hija);
        }
        Properties catalogo = cargarCatalogo();
        catalogo.remove(claveNombre(carpeta));
        guardarCatalogo(catalogo);
    }

    /** Abrimos la empresa: la dejamos activa, conectamos con su base y empezamos la sesión. */
    public void abrir(String carpeta, LocalDate fecha) throws Exception {
        Conexion.setEmpresaActiva(carpeta);
        Conexion.cerrarConexion();
        Conexion.establecerConexion();
        Sesion.getSesion().iniciar(carpeta, fecha);
        PreferenciasGlobales.set(PreferenciasGlobales.ULTIMA_EMPRESA, carpeta);
        recordarTema();
    }

    /** Cerramos la empresa en uso, para poder volver al arranque. */
    public void cerrar() {
        Conexion.cerrarConexion();
        Sesion.getSesion().terminar();
    }

    /**
     * Registramos el nombre visible de una empresa ya existente (restaurada o
     * rescatada sin entrada de catálogo).
     */
    public void registrarNombre(String carpeta, String nombre) throws Exception {
        Properties catalogo = cargarCatalogo();
        catalogo.setProperty(claveNombre(carpeta), nombre);
        guardarCatalogo(catalogo);
    }

    /**
     * Copiamos el tema de la empresa activa a las preferencias globales, para
     * que las pantallas y el arranque usen el de esta empresa. Si la empresa no
     * tiene tema guardado, dejamos el valor vacío y se usa el tema por defecto.
     */
    public void recordarTema() {
        String tema = null;
        try {
            tema = Configuracion.getConfiguracion().preferencia(PreferenciasGlobales.TEMA);
        } catch (Exception ignored) {
        }
        if (tema == null) {
            tema = "";
        }
        PreferenciasGlobales.set(PreferenciasGlobales.TEMA, tema);
    }

    /**
     * Carpeta de una empresa a partir de su nombre: minúsculas, sin acentos,
     * los espacios y símbolos se sustituyen por guiones bajos.
     */
    public static String carpetaDe(String nombre) {
        if (nombre == null) {
            return "empresa";
        }
        String limpio = Normalizer.normalize(nombre, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "_")
                .replaceAll("^_+|_+$", "");
        if (limpio.isBlank()) {
            return "empresa";
        }
        return limpio;
    }

    /**
     * Borramos una carpeta con todo lo que tiene dentro. Cómo funciona: un
     * directorio no se puede borrar si no está vacío, así que primero borramos
     * cada cosa de dentro (y si es otra carpeta, la vaciamos llamando a este
     * mismo método) y al final la propia carpeta.
     */
    private void borrarCarpeta(File carpeta) throws Exception {
        File[] contenido = carpeta.listFiles();
        if (contenido != null) {
            for (File hijo : contenido) {
                if (hijo.isDirectory()) {
                    borrarCarpeta(hijo);
                } else if (!hijo.delete()) {
                    throw new Exception("No se pudo borrar " + hijo.getName() + ". ¿Está abierta la empresa?");
                }
            }
        }
        if (!carpeta.delete()) {
            throw new Exception("No se pudo borrar la carpeta " + carpeta.getName() + ".");
        }
    }

    private String claveNombre(String carpeta) {
        return carpeta + ".nombre";
    }

    private Path archivoCatalogo() {
        return Conexion.carpetaRaiz().resolve(CATALOGO);
    }

    private Properties cargarCatalogo() throws IOException {
        Properties catalogo = new Properties();
        Path fichero = archivoCatalogo();
        if (Files.exists(fichero)) {
            try (InputStream entrada = Files.newInputStream(fichero)) {
                catalogo.load(entrada);
            }
        }
        return catalogo;
    }

    private void guardarCatalogo(Properties catalogo) throws IOException {
        Path fichero = archivoCatalogo();
        Files.createDirectories(fichero.getParent());
        try (OutputStream salida = Files.newOutputStream(fichero)) {
            catalogo.store(salida, null);
        }
    }
}
