package cabofactu.vista;

import javafx.stage.Stage;

/**
 * Configuracion de tamaño por vista FXML.
 */
public enum ConfiguracionVentana {

    ARRANQUE("Seleccion de empresa", "Arranque.fxml", 760, 520, 760, 520, false),
    MENU("Menu Principal", "MenuPrincipal.fxml", 1024, 768, 1024, 768, true),
    EDITOR("Editor de factura", "Editor.fxml", 1024, 768, 1024, 768, true),
    CONFIGURACION("Configuracion", "Configuracion.fxml", 1024, 768, 1024, 768, true),
    HISTORICO("Historico", "Historico.fxml", 1024, 768, 1024, 768, true),
    CLIENTES("Clientes", "Clientes.fxml", 1024, 768, 1024, 768, true),
    COPIA_SEGURIDAD("Copias", "CopiaSeguridad.fxml", 1024, 768, 1024, 768, true),
    GENERAR_MENSUAL("Generar facturas mensuales", "GenerarFacturasMensuales.fxml", 800, 600, 800, 600, true);

    private static final String CLAVE_CONFIG = "cabofactu.ventanaConfig";

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

    ConfiguracionVentana(String titulo, String fxml, double ancho, double alto, double minAncho, double minAlto, boolean redimensionable) {
        this(titulo, fxml, ancho, alto, minAncho, minAlto, Double.MAX_VALUE, Double.MAX_VALUE, redimensionable, false);
    }

    ConfiguracionVentana(String titulo, String fxml, double ancho, double alto, double minAncho, double minAlto,
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

    /** Devolvemos la configuración de esa pantalla, o null si no tiene. */
    public static ConfiguracionVentana para(String fxml) {
        for (ConfiguracionVentana v : values()) {
            if (v.fxml.equals(fxml)) {
                return v;
            }
        }
        return null;
    }

    public void aplicar(Stage stage) {
        ConfiguracionVentana previa = (ConfiguracionVentana) stage.getProperties().get(CLAVE_CONFIG);
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

    private boolean debeFijarTamano(Stage stage, ConfiguracionVentana previa) {
        if (!stage.isShowing() || previa == null) {
            return true;
        }
        return previa.ancho != ancho || previa.alto != alto;
    }
}
