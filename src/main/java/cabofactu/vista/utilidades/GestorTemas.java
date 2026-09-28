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
 * junto a base.css (estructura común). Cada empresa guarda su propio tema, y
 * lo copiamos a las preferencias globales para que la pantalla de arranque
 * salga con el último elegido.
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
        TEMAS.put("neon", "Neón");
    }

    private GestorTemas() {
    }

    /** Las claves de los temas, en el orden en que se guardan. */
    public static List<String> temas() {
        return new ArrayList<>(TEMAS.keySet());
    }

    /** El nombre visible de un tema a partir de su clave. */
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

    /** La clave del tema aplicado ahora mismo. */
    public static String temaActivo() {
        return activo;
    }

    /** Aplicamos a la escena el tema guardado en las preferencias globales, o el tema por defecto. */
    public static void aplicar(Scene scene) {
        String tema = POR_DEFECTO;
        String guardado = PreferenciasGlobales.get(PREF_TEMA);
        if (guardado != null && TEMAS.containsKey(guardado)) {
            tema = guardado;
        }
        seleccionar(scene, tema);
    }

    /** Hacemos activo el tema indicado y lo aplicamos a la escena. */
    public static void seleccionar(Scene scene, String tema) {
        if (TEMAS.containsKey(tema)) {
            activo = tema;
        }
        scene.getStylesheets().setAll(hojas());
    }

    /** Las dos hojas de estilos del tema activo: base.css y la del tema. */
    public static List<String> hojas() {
        List<String> hojas = new ArrayList<>();
        hojas.add(css("base"));
        hojas.add(css(activo));
        return hojas;
    }

    /** Guardamos el tema en la empresa activa y lo recordamos para el arranque. */
    public static void guardar() throws Exception {
        Vista.getInstancia().getControlador().guardarPreferencia(PREF_TEMA, activo);
        PreferenciasGlobales.set(PREF_TEMA, activo);
    }

    private static String css(String nombre) {
        String fichero;
        if (nombre.equals("base")) {
            fichero = "base";
        } else {
            fichero = "tema-" + nombre;
        }
        return GestorTemas.class.getResource("/cabofactu/vista/recursos/temas/" + fichero + ".css").toExternalForm();
    }
}