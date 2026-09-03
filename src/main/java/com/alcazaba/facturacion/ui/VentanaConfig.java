package com.alcazaba.facturacion.ui;

import javafx.stage.Stage;

import java.util.Arrays;
import java.util.Optional;

/**
 * Configuracion de tamaño por vista FXML.
 */
public enum VentanaConfig {

    ARRANQUE("Seleccion de empresa", "/com/alcazaba/facturacion/ui/Arranque.fxml", 760, 520, 760, 520, false),
    MENU("Menu Principal", "/com/alcazaba/facturacion/ui/MenuPrincipal.fxml", 1024, 768, 1024, 768, true),
    EDITOR("Editor de factura", "/com/alcazaba/facturacion/ui/Editor.fxml", 1024, 768, 1024, 768, true),
    CONFIGURACION("Configuracion", "/com/alcazaba/facturacion/ui/Configuracion.fxml", 1024, 768, 1024, 768, true),
    HISTORICO("Historico", "/com/alcazaba/facturacion/ui/Historico.fxml", 1024, 768, 1024, 768, true),
    CLIENTES("Clientes", "/com/alcazaba/facturacion/ui/Clientes.fxml", 1024, 768, 1024, 768, true),
    VERSIONES("Versiones", "/com/alcazaba/facturacion/ui/Versiones.fxml", 1024, 768, 1024, 768, true),
    BACKUP("Copias", "/com/alcazaba/facturacion/ui/Backup.fxml", 1024, 768, 1024, 768, true),
    GENERAR_MENSUAL("Generar facturas mensuales", "/com/alcazaba/facturacion/ui/GenerarFacturasMensuales.fxml", 800, 600, 800, 600, true);

    private static final String CLAVE_CONFIG = "com.alcazaba.facturacion.ventanaConfig";

    private final String titulo;
    private final String fxml;
    private final double ancho;
    private final double alto;
    private final double minAncho;
    private final double minAlto;
    private final double maxAncho;
    private final double maxAlto;
    private final boolean redimensionable;
    private final boolean maximizado;

    VentanaConfig(String titulo, String fxml, double ancho, double alto, double minAncho, double minAlto, boolean redimensionable) {
        this(titulo, fxml, ancho, alto, minAncho, minAlto, Double.MAX_VALUE, Double.MAX_VALUE, redimensionable, false);
    }

    VentanaConfig(String titulo, String fxml, double ancho, double alto, double minAncho, double minAlto,
                  double maxAncho, double maxAlto, boolean redimensionable, boolean maximizado) {
        this.titulo = titulo;
        this.fxml = fxml;
        this.ancho = ancho;
        this.alto = alto;
        this.minAncho = minAncho;
        this.minAlto = minAlto;
        this.maxAncho = maxAncho;
        this.maxAlto = maxAlto;
        this.redimensionable = redimensionable;
        this.maximizado = maximizado;
    }

    public String fxml() {
        return fxml;
    }

    public String titulo() {
        return titulo;
    }

    public double ancho() {
        return ancho;
    }

    public double alto() {
        return alto;
    }

    public double minAncho() {
        return minAncho;
    }

    public double minAlto() {
        return minAlto;
    }

    public boolean redimensionable() {
        return redimensionable;
    }

    public boolean maximizado() {
        return maximizado;
    }

    public static Optional<VentanaConfig> para(String fxml) {
        return Arrays.stream(values())
                .filter(v -> v.fxml.equals(fxml))
                .findFirst();
    }

    public void aplicar(Stage stage) {
        VentanaConfig previa = (VentanaConfig) stage.getProperties().get(CLAVE_CONFIG);
        stage.getProperties().put(CLAVE_CONFIG, this);

        stage.setMaxWidth(Double.MAX_VALUE);
        stage.setMaxHeight(Double.MAX_VALUE);
        stage.setResizable(redimensionable);
        stage.setMinWidth(minAncho);
        stage.setMinHeight(minAlto);
        stage.setMaxWidth(maxAncho);
        stage.setMaxHeight(maxAlto);

        if (debeFijarTamano(stage, previa)) {
            stage.setWidth(ancho);
            stage.setHeight(alto);
            stage.centerOnScreen();
        }
        if (maximizado) {
            stage.setMaximized(true);
        }
    }

    private boolean debeFijarTamano(Stage stage, VentanaConfig previa) {
        if (!stage.isShowing() || previa == null) {
            return true;
        }
        return previa.ancho != ancho || previa.alto != alto;
    }
}
