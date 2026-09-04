package com.alcazaba.facturacion.ui;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.shape.SVGPath;
import javafx.stage.WindowEvent;

/**
 * Barra de navegacion superior con iconos. Aparece en todas las pantallas
 * salvo el menu principal y lleva a las vistas principales o a salir.
 */
public final class BarraNavegacion {

    private static final String RUTA_MENU = "/com/alcazaba/facturacion/ui/MenuPrincipal.fxml";
    private static final String RUTA_EDITOR = "/com/alcazaba/facturacion/ui/Editor.fxml";
    private static final String RUTA_HISTORICO = "/com/alcazaba/facturacion/ui/Historico.fxml";
    private static final String RUTA_CLIENTES = "/com/alcazaba/facturacion/ui/Clientes.fxml";
    private static final String RUTA_CONFIG = "/com/alcazaba/facturacion/ui/Configuracion.fxml";
    private static final String RUTA_BACKUP = "/com/alcazaba/facturacion/ui/Backup.fxml";

    private static final String ICONO_INICIO = "M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z";
    private static final String ICONO_NUEVA = "M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34c-.39-.39-1.02-.39-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z";
    private static final String ICONO_HISTORICO = "M19 3h-4.18C14.4 1.84 13.3 1 12 1c-1.3 0-2.4.84-2.82 2H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-7 0c.55 0 1 .45 1 1s-.45 1-1 1-1-.45-1-1 .45-1 1-1zm2 14H7v-2h7v2zm3-4H7v-2h10v2zm0-4H7V7h10v2z";
    private static final String ICONO_CLIENTES = "M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z";
    private static final String ICONO_CONFIG = "M19.14 12.94c.04-.3.06-.61.06-.94 0-.32-.02-.64-.07-.94l2.03-1.58c.18-.14.23-.41.12-.61l-1.92-3.32c-.12-.22-.37-.29-.59-.22l-2.39.96c-.5-.38-1.03-.7-1.62-.94l-.36-2.54c-.05-.24-.24-.41-.48-.41h-3.84c-.24 0-.43.17-.47.41l-.36 2.54c-.59.24-1.13.57-1.62.94l-2.39-.96c-.22-.08-.47 0-.59.22L2.74 8.87c-.12.21-.08.47.12.61l2.03 1.58c-.05.3-.09.63-.09.94s.02.64.07.94l-2.03 1.58c-.18.14-.23.41-.12.61l1.92 3.32c.12.22.37.29.59.22l2.39-.96c.5.38 1.03.7 1.62.94l.36 2.54c.05.24.24.41.48.41h3.84c.24 0 .44-.17.47-.41l.36-2.54c.59-.24 1.13-.56 1.62-.94l2.39.96c.22.08.47 0 .59-.22l1.92-3.32c.12-.22.07-.47-.12-.61l-2.01-1.58zM12 15.6c-1.98 0-3.6-1.62-3.6-3.6s1.62-3.6 3.6-3.6 3.6 1.62 3.6 3.6-1.62 3.6-3.6 3.6z";
    private static final String ICONO_BACKUP = "M19 12v7H5v-7H3v7c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2v-7h-2zm-6 .67l2.59-2.58L17 11.5l-5 5-5-5 1.41-1.41L11 12.67V3h2v9.67z";
    private static final String ICONO_SALIR = "M17 7l-1.41 1.41L18.17 11H8v2h10.17l-2.58 2.58L17 17l5-5zM4 5h8V3H4c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h8v-2H4V5z";

    private BarraNavegacion() {
    }

    public static HBox crear(Navegador nav, String actual) {
        HBox barra = new HBox(34);
        barra.getStyleClass().add("nav-bar");
        barra.setAlignment(Pos.CENTER);
        barra.getChildren().addAll(
                boton("Inicio", "Menú principal", ICONO_INICIO, () -> nav.mostrar(RUTA_MENU), "menu".equals(actual)),
                boton("Nueva", "Nueva factura", ICONO_NUEVA, () -> nav.mostrar(RUTA_EDITOR), "editor".equals(actual)),
                boton("Histórico", "Histórico", ICONO_HISTORICO, () -> nav.mostrar(RUTA_HISTORICO), "historico".equals(actual)),
                boton("Clientes", "Clientes", ICONO_CLIENTES, () -> nav.mostrar(RUTA_CLIENTES), "clientes".equals(actual)),
                boton("Configuración", "Configuración", ICONO_CONFIG, () -> nav.mostrar(RUTA_CONFIG), "configuracion".equals(actual)),
                boton("Copias", "Copia de seguridad", ICONO_BACKUP, () -> nav.mostrar(RUTA_BACKUP), "backup".equals(actual)),
                boton("Salir", "Salir", ICONO_SALIR, () -> nav.stage().fireEvent(new WindowEvent(nav.stage(), WindowEvent.WINDOW_CLOSE_REQUEST)), false));
        return barra;
    }

    private static Button boton(String etiqueta, String tooltip, String svg, Runnable accion, boolean activo) {
        Button b = new Button();
        b.getStyleClass().add("nav-button");
        if (activo) {
            b.getStyleClass().add("activo");
        }
        b.setText(etiqueta);
        b.setGraphic(icono(svg));
        b.setTooltip(new Tooltip(tooltip));
        b.setOnAction(e -> accion.run());
        return b;
    }

    private static SVGPath icono(String contenido) {
        SVGPath p = new SVGPath();
        p.setContent(contenido);
        p.getStyleClass().add("nav-icon");
        return p;
    }
}