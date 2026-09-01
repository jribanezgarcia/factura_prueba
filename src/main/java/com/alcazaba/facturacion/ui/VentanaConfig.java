package com.alcazaba.facturacion.ui;

import javafx.stage.Stage;

import java.util.Arrays;
import java.util.Optional;

/**
 * Configuracion de tamaño por vista FXML.
 */
public enum VentanaConfig {

    ARRANQUE("/com/alcazaba/facturacion/ui/Arranque.fxml", 760, 520, 760, 520, 760, 520, false),
    MENU("/com/alcazaba/facturacion/ui/MenuPrincipal.fxml", 800, 600, 800, 600, true),
    EDITOR("/com/alcazaba/facturacion/ui/Editor.fxml", 800, 600, 800, 600, true),
    CONFIGURACION("/com/alcazaba/facturacion/ui/Configuracion.fxml", 800, 600, 800, 600, true),
    HISTORICO("/com/alcazaba/facturacion/ui/Historico.fxml", 800, 600, 800, 600, true),
    CLIENTES("/com/alcazaba/facturacion/ui/Clientes.fxml", 800, 600, 800, 600, true),
    VERSIONES("/com/alcazaba/facturacion/ui/Versiones.fxml", 800, 600, 800, 600, true),
    BACKUP("/com/alcazaba/facturacion/ui/Backup.fxml", 800, 600, 800, 600, true),
    GENERAR_MENSUAL("/com/alcazaba/facturacion/ui/GenerarFacturasMensuales.fxml", 800, 600, 800, 600, true);

    private final String fxml;
    private final double ancho;
    private final double alto;
    private final double minAncho;
    private final double minAlto;
    private final double maxAncho;
    private final double maxAlto;
    private final boolean redimensionable;
    private final boolean maximizado;

    VentanaConfig(String fxml, double ancho, double alto, double minAncho, double minAlto, boolean redimensionable) {
        this(fxml, ancho, alto, minAncho, minAlto, Double.MAX_VALUE, Double.MAX_VALUE, redimensionable, false);
    }

    VentanaConfig(String fxml, double ancho, double alto, double minAncho, double minAlto,
                  double maxAncho, double maxAlto, boolean redimensionable) {
        this(fxml, ancho, alto, minAncho, minAlto, maxAncho, maxAlto, redimensionable, false);
    }

    VentanaConfig(String fxml, double ancho, double alto, double minAncho, double minAlto,
                  boolean redimensionable, boolean maximizado) {
        this(fxml, ancho, alto, minAncho, minAlto, Double.MAX_VALUE, Double.MAX_VALUE, redimensionable, maximizado);
    }

    VentanaConfig(String fxml, double ancho, double alto, double minAncho, double minAlto,
                  double maxAncho, double maxAlto, boolean redimensionable, boolean maximizado) {
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
        if (stage.isShowing()) {
            aplicarSinRedimensionar(stage);
        } else {
            aplicarCompleto(stage);
        }
    }

    private void aplicarSinRedimensionar(Stage stage) {
        stage.setResizable(redimensionable);
        stage.setMinWidth(minAncho);
        stage.setMinHeight(minAlto);
        stage.setMaxWidth(maxAncho);
        stage.setMaxHeight(maxAlto);
        stage.setMaximized(maximizado);
    }

    private void aplicarCompleto(Stage stage) {
        stage.setResizable(redimensionable);
        stage.setMinWidth(minAncho);
        stage.setMinHeight(minAlto);
        stage.setMaxWidth(maxAncho);
        stage.setMaxHeight(maxAlto);
        stage.setWidth(ancho);
        stage.setHeight(alto);
        stage.centerOnScreen();
        stage.setMaximized(maximizado);
    }
}
