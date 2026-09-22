package cabofactu.vista.utilidades;

import cabofactu.modelo.negocio.PreferenciasGlobales;
import cabofactu.vista.Vista;
import javafx.scene.Scene;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Sistema de temas. Cada tema es un fichero CSS con sus colores que se aplica
 * junto a base.css (estructura comun). El tema activo se recuerda en la tabla
 * de preferencias y se aplica al cargar cada vista.
 */
public final class GestorTemas {

    public static final String PREF_TEMA = "tema";
    public static final String POR_DEFECTO = "biblioteca8";

    private static final Map<String, String> TEMAS = new LinkedHashMap<>();
    private static String activo = POR_DEFECTO;

    static {
        TEMAS.put("biblioteca8", "Biblioteca8");
        TEMAS.put("omarchy", "Omarchy");
        TEMAS.put("esmeralda", "Esmeralda");
        TEMAS.put("terracota", "Terracota");
        TEMAS.put("negro-dorado", "Negro y dorado");
        TEMAS.put("sakura", "Sakura");
        TEMAS.put("neon", "Neon");
    }

    private GestorTemas() {
    }

    public static List<String> temas() {
        return new ArrayList<>(TEMAS.keySet());
    }

    public static String etiqueta(String tema) {
        return TEMAS.getOrDefault(tema, tema);
    }

    /** Los nombres visibles de los temas, para el desplegable de Configuración. */
    public static List<String> nombres() {
        return new ArrayList<>(TEMAS.values());
    }

    /** La clave de un nombre visible («Negro y dorado» a `negro-dorado`). */
    public static String claveDe(String nombre) {
        for (Map.Entry<String, String> entrada : TEMAS.entrySet()) {
            if (entrada.getValue().equals(nombre)) {
                return entrada.getKey();
            }
        }
        return POR_DEFECTO;
    }

    public static String temaActivo() {
        return activo;
    }

    public static void aplicar(Scene scene) {
        String tema = POR_DEFECTO;
        String guardado = PreferenciasGlobales.get(PREF_TEMA);
        if (guardado != null && TEMAS.containsKey(guardado)) {
            tema = guardado;
        }
        seleccionar(scene, tema);
    }

    public static void seleccionar(Scene scene, String tema) {
        if (TEMAS.containsKey(tema)) {
            activo = tema;
        }
        scene.getStylesheets().setAll(hojas());
    }

    public static List<String> hojas() {
        List<String> hojas = new ArrayList<>();
        hojas.add(css("base"));
        hojas.add(css(activo));
        return hojas;
    }

    /** Guardamos el tema en la empresa activa y lo recordamos para el arranque. */
    public static void guardar() {
        try {
            Vista.getInstancia().getControlador().guardarPreferencia(PREF_TEMA, activo);
        } catch (Exception ignored) {
        }
        PreferenciasGlobales.set(PREF_TEMA, activo);
    }

    private static String css(String nombre) {
        return GestorTemas.class.getResource(
                "/cabofactu/vista/recursos/temas/" + (nombre.equals("base") ? "base" : "tema-" + nombre) + ".css")
                .toExternalForm();
    }
}