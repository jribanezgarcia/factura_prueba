package com.alcazaba.facturacion.ui;

import com.alcazaba.facturacion.service.PreferenciasGlobales;
import com.alcazaba.facturacion.service.Servicios;
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
public final class ThemeManager {

    public static final String PREV_TEMA = "tema";
    public static final String DEFAULT = "biblioteca8";

    private static final Map<String, String> TEMAS = new LinkedHashMap<>();
    private static String activo = DEFAULT;

    static {
        TEMAS.put("biblioteca8", "Biblioteca8");
        TEMAS.put("omarchy", "Omarchy");
        TEMAS.put("esmeralda", "Esmeralda");
        TEMAS.put("terracota", "Terracota");
        TEMAS.put("negro-dorado", "Negro y dorado");
        TEMAS.put("sakura", "Sakura");
        TEMAS.put("neon", "Neon");
    }

    private ThemeManager() {
    }

    public static List<String> temas() {
        return new ArrayList<>(TEMAS.keySet());
    }

    public static String etiqueta(String tema) {
        return TEMAS.getOrDefault(tema, tema);
    }

    public static String temaActivo() {
        return activo;
    }

    public static void aplicar(Scene scene, Servicios servicios) {
        String tema = DEFAULT;
        String guardado = PreferenciasGlobales.get(PREV_TEMA);
        if (guardado != null && TEMAS.containsKey(guardado)) {
            tema = guardado;
        }
        seleccionar(scene, tema);
    }

    public static void seleccionar(Scene scene, String tema) {
        if (TEMAS.containsKey(tema)) {
            activo = tema;
        }
        List<String> hojas = new ArrayList<>();
        hojas.add(css("base"));
        hojas.add(css(activo));
        scene.getStylesheets().setAll(hojas);
    }

    public static void guardar(Servicios servicios) {
        PreferenciasGlobales.set(PREV_TEMA, activo);
    }

    private static String css(String nombre) {
        return ThemeManager.class.getResource(
                "/com/alcazaba/facturacion/themes/" + (nombre.equals("base") ? "base" : "tema-" + nombre) + ".css")
                .toExternalForm();
    }
}