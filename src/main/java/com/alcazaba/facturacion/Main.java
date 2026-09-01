package com.alcazaba.facturacion;

import com.alcazaba.facturacion.db.Database;
import com.alcazaba.facturacion.service.EmpresaManager;
import com.alcazaba.facturacion.service.PreferenciasGlobales;
import com.alcazaba.facturacion.service.Servicios;
import com.alcazaba.facturacion.ui.ArranqueController;
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
 * global (FileChannel.tryLock sobre el lock de BASE_DATA_DIR), aplica la
 * configuracion regional espanola, muestra la pantalla de arranque (empresa +
 * fecha de trabajo) y solo despues de confirmar conecta la empresa, construye
 * los servicios y abre el menu principal.
 */
public class Main extends Application {

    private static final double ANCHO_INICIAL = 1024;
    private static final double ALTO_INICIAL = 768;

    private FileChannel lockChannel;
    private FileLock lock;
    private Vista actual;
    private Servicios servicios;
    private Navegador nav;
    private Stage stage;
    private Double anchoGuardado;
    private Double altoGuardado;

    public static void main(String[] args) {
        Locale.setDefault(new Locale("es", "ES"));
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        if (!prepararDatos()) {
            return;
        }
        if (!adquirirLock()) {
            Dialogos.error("Facturación", "La aplicación ya está en ejecución.\nSolo puede abrirse una instancia.");
            Platform.exit();
            return;
        }
        Platform.setImplicitExit(false);
        configurarVentana();
        mostrarArranque();
        stage.show();
    }

    /**
     * Crea la raiz de datos, hace la migracion de instalacion (un archivo a
     * carpetas por empresa) y, si no queda ninguna empresa, crea la inicial.
     */
    private boolean prepararDatos() {
        try {
            Files.createDirectories(Database.baseDataDir());
            String migrada = Database.migrarInstalacionUnArchivo();
            if (migrada != null) {
                EmpresaManager.registrarNombre(migrada, "Comercial Alcazaba");
                PreferenciasGlobales.set(PreferenciasGlobales.ULTIMA_EMPRESA, migrada);
            }
            if (cmbEmpresaVacia()) {
                EmpresaManager.crearEmpresa("Comercial Alcazaba");
            }
            return true;
        } catch (Exception e) {
            Dialogos.error("Facturación", "No se pudo preparar la carpeta de datos:\n" + e.getMessage());
            Platform.exit();
            return false;
        }
    }

    private boolean cmbEmpresaVacia() throws IOException {
        return EmpresaManager.listarEmpresas().isEmpty();
    }

    private void configurarVentana() {
        stage.setTitle("Facturación");
        aplicarPreferenciasVentana(stage);
        stage.setOnCloseRequest(e -> cerrarAplicacion());
    }

    private void mostrarArranque() {
        Navegador navArranque = new Navegador(stage, servicios);
        ArranqueController arranque = navArranque.mostrar("/com/alcazaba/facturacion/ui/Arranque.fxml");
        arranque.setOnEntrar(e -> entrarEnMenu());
    }

    private void entrarEnMenu() {
        try {
            servicios = new Servicios();
        } catch (Exception e) {
            Dialogos.error("Facturación", "Error al inicializar la aplicación:\n" + e.getMessage());
            return;
        }
        stage.hide();
        nav = new Navegador(stage, servicios);
        nav.setOnVistaCambio(v -> this.actual = v);
        nav.mostrar("/com/alcazaba/facturacion/ui/MenuPrincipal.fxml");
        restaurarTamanoGuardado();
        stage.show();
    }

    /**
     * El tamano de la vista lo fija VentanaConfig al cargarla; si la sesion
     * anterior dejo una ventana mas grande que el minimo, se recupera aqui.
     * Se llama con la ventana oculta, antes de volver a mostrarla.
     */
    private void restaurarTamanoGuardado() {
        if (anchoGuardado != null && anchoGuardado > stage.getMinWidth()) {
            stage.setWidth(anchoGuardado);
        }
        if (altoGuardado != null && altoGuardado > stage.getMinHeight()) {
            stage.setHeight(altoGuardado);
        }
    }

    private void cerrarAplicacion() {
        if (actual != null && !actual.puedeCerrar()) {
            return;
        }
        if (actual == null || Dialogos.confirmar("Salir", "¿Seguro que deseas salir de la aplicación?")) {
            if (actual != null) {
                actual.alCerrar();
            }
            guardarPreferenciasVentana(stage);
            liberarLock();
            Platform.exit();
        }
    }

    private boolean adquirirLock() {
        try {
            lockChannel = FileChannel.open(Database.lockPathGlobal(),
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
            Double x = PreferenciasGlobales.getDouble(PreferenciasGlobales.VENTANA_X);
            Double y = PreferenciasGlobales.getDouble(PreferenciasGlobales.VENTANA_Y);
            Double w = PreferenciasGlobales.getDouble(PreferenciasGlobales.VENTANA_W);
            Double h = PreferenciasGlobales.getDouble(PreferenciasGlobales.VENTANA_H);
            anchoGuardado = w;
            altoGuardado = h;
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
        PreferenciasGlobales.set(PreferenciasGlobales.VENTANA_X, String.valueOf(stage.getX()));
        PreferenciasGlobales.set(PreferenciasGlobales.VENTANA_Y, String.valueOf(stage.getY()));
        PreferenciasGlobales.set(PreferenciasGlobales.VENTANA_W, String.valueOf(stage.getWidth()));
        PreferenciasGlobales.set(PreferenciasGlobales.VENTANA_H, String.valueOf(stage.getHeight()));
    }
}
