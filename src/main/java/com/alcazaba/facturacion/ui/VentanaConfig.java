package com.alcazaba.facturacion.ui;

import javafx.stage.Stage;

import java.util.Arrays;
import java.util.Optional;

/**
 * Configuracion de tamaño por vista FXML.
 */
public enum VentanaConfig {

    ARRANQUE("/com/alcazaba/facturacion/ui/Arranque.fxml", 760, 520, 760, 520, 760, 520, false),
    MENU("/com/alcazaba/facturacion/ui/MenuPrincipal.fxml", 760, 600, 760, 600, true),
    EDITOR("/com/alcazaba/facturacion/ui/Editor.fxml", 1000, 760, 1000, 760, true),
    CONFIGURACION("/com/alcazaba/facturacion/ui/Configuracion.fxml", 1000, 620, 1000, 620, true),
    HISTORICO("/com/alcazaba/facturacion/ui/Historico.fxml", 1000, 600, 1000, 600, true),
    CLIENTES("/com/alcazaba/facturacion/ui/Clientes.fxml", 1000, 600, 1000, 600, true),
    VERSIONES("/com/alcazaba/facturacion/ui/Versiones.fxml", 900, 500, 900, 500, true),
    BACKUP("/com/alcazaba/facturacion/ui/Backup.fxml", 720, 450, 720, 450, true),
    GENERAR_MENSUAL("/com/alcazaba/facturacion/ui/GenerarFacturasMensuales.fxml", 920, 680, 920, 680, true);

    private final String fxml;
    private final double ancho;
    private final double alto;
    private final double minAncho;
    private final double minAlto;
    private final double maxAncho;
    private final double maxAlto;
    private final boolean redimensionable;

    VentanaConfig(String fxml, double ancho, double alto, double minAncho, double minAlto, boolean redimensionable) {
        this(fxml, ancho, alto, minAncho, minAlto, Double.MAX_VALUE, Double.MAX_VALUE, redimensionable);
    }

    VentanaConfig(String fxml, double ancho, double alto, double minAncho, double minAlto,
                  double maxAncho, double maxAlto, boolean redimensionable) {
        this.fxml = fxml;
        this.ancho = ancho;
        this.alto = alto;
        this.minAncho = minAncho;
        this.minAlto = minAlto;
        this.maxAncho = maxAncho;
        this.maxAlto = maxAlto;
        this.redimensionable = redimensionable;
    }

    public String fxml() {
        return fxml;
    }

    public boolean redimensionable() {
        return redimensionable;
    }

    public static Optional<VentanaConfig> para(String fxml) {
        return Arrays.stream(values())
                .filter(v -> v.fxml.equals(fxml))
                .findFirst();
    }

    public void aplicar(Stage stage) {
        stage.setResizable(redimensionable);
        stage.setMinWidth(minAncho);
        stage.setMinHeight(minAlto);
        stage.setMaxWidth(maxAncho);
        stage.setMaxHeight(maxAlto);
        stage.setWidth(ancho);
        stage.setHeight(alto);
        stage.centerOnScreen();
    }
}
