package com.alcazaba.facturacion;

import com.alcazaba.facturacion.db.Database;
import com.alcazaba.facturacion.service.Servicios;
import com.alcazaba.facturacion.ui.Dialogos;
import com.alcazaba.facturacion.ui.Navegador;
import com.alcazaba.facturacion.ui.Vista;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Locale;

/**
 * Punto de entrada. Prepara la carpeta de datos, garantiza la instancia unica
 * (FileChannel.tryLock sobre facturas.lock), aplica la configuracion regional
 * espanola y abre el menu principal.
 */
public class Main extends Application {

    private static final String PREV_X = "ventana_x";
    private static final String PREV_Y = "ventana_y";
    private static final String PREV_W = "ventana_w";
    private static final String PREV_H = "ventana_h";
    private static final double ANCHO_INICIAL = 800;
    private static final double ALTO_INICIAL = 600;

    private FileChannel lockChannel;
    private FileLock lock;
    private Vista actual;
    private Servicios servicios;

    public static void main(String[] args) {
        Locale.setDefault(new Locale("es", "ES"));
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        try {
            Files.createDirectories(Database.dataDir());
        } catch (IOException e) {
            Dialogos.error("Facturación", "No se pudo crear la carpeta de datos:\n" + e.getMessage());
            Platform.exit();
            return;
        }
        if (!adquirirLock()) {
            Dialogos.error("Facturación", "La aplicación ya está en ejecución.\nSolo puede abrirse una instancia.");
            Platform.exit();
            return;
        }
        try {
            Database.getConnection();
        } catch (Exception e) {
            Dialogos.error("Facturación", "No se pudo abrir la base de datos:\n" + e.getMessage());
            Platform.exit();
            return;
        }
        try {
            servicios = new Servicios();
        } catch (Exception e) {
            Dialogos.error("Facturación", "Error al inicializar la aplicación:\n" + e.getMessage());
            Platform.exit();
            return;
        }

        Navegador nav = new Navegador(stage, servicios);
        nav.setOnVistaCambio(v -> this.actual = v);

        stage.setTitle("Facturación");
        stage.setMinWidth(800);
        stage.setMinHeight(600);
        aplicarPreferenciasVentana(stage);
        stage.setOnCloseRequest(e -> {
            if (actual != null && !actual.puedeCerrar()) {
                e.consume();
                return;
            }
            if (!Dialogos.confirmar("Salir", "¿Seguro que deseas salir de la aplicación?")) {
                e.consume();
                return;
            }
            if (actual != null) {
                actual.alCerrar();
            }
            guardarPreferenciasVentana(stage);
            liberarLock();
        });

        nav.mostrar("/com/alcazaba/facturacion/ui/MenuPrincipal.fxml");
        stage.show();
    }

    private boolean adquirirLock() {
        try {
            lockChannel = FileChannel.open(Database.lockPath(),
                    StandardOpenOption.CREATE, StandardOpenOption.WRITE);
            lock = lockChannel.tryLock();
            return lock != null;
        } catch (IOException e) {
            return false;
        }
    }

    private void liberarLock() {
        try {
            if (lock != null && lock.isValid()) {
                lock.release();
            }
        } catch (IOException ignored) {
        }
        try {
            if (lockChannel != null) {
                lockChannel.close();
            }
        } catch (IOException ignored) {
        }
    }

    private void aplicarPreferenciasVentana(Stage stage) {
        try {
            Double x = doublePreferencia(PREV_X);
            Double y = doublePreferencia(PREV_Y);
            Double w = doublePreferencia(PREV_W);
            Double h = doublePreferencia(PREV_H);
            if (w != null && h != null) {
                stage.setWidth(w);
                stage.setHeight(h);
            } else {
                stage.setWidth(ANCHO_INICIAL);
                stage.setHeight(ALTO_INICIAL);
                stage.centerOnScreen();
            }
            if (x != null && y != null) {
                stage.setX(x);
                stage.setY(y);
            }
        } catch (Exception ignored) {
        }
    }

    private void guardarPreferenciasVentana(Stage stage) {
        try {
            servicios.config.setPreferencia(PREV_X, String.valueOf(stage.getX()));
            servicios.config.setPreferencia(PREV_Y, String.valueOf(stage.getY()));
            servicios.config.setPreferencia(PREV_W, String.valueOf(stage.getWidth()));
            servicios.config.setPreferencia(PREV_H, String.valueOf(stage.getHeight()));
        } catch (Exception ignored) {
        }
    }

    private Double doublePreferencia(String clave) throws java.sql.SQLException {
        String valor = servicios.config.getPreferencia(clave);
        return valor == null || valor.isBlank() ? null : Double.valueOf(valor);
    }
}
