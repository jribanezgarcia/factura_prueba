package com.alcazaba.facturacion.ui;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.shape.SVGPath;

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
    private static final String ICONO_NUEVA = "M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6z";
    private static final String ICONO_HISTORICO = "M15.5 14h-.79l-.28-.27C15.41 12.59 16 11.11 16 9.5 16 5.91 13.09 3 9.5 3S3 5.91 3 9.5 5.91 16 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z";
    private static final String ICONO_CLIENTES = "M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z";
    private static final String ICONO_CONFIG = "M19.14 12.94c.04-.3.06-.61.06-.94 0-.32-.02-.64-.07-.94l2.03-1.58c.18-.14.23-.41.12-.61l-1.92-3.32c-.12-.22-.37-.29-.59-.22l-2.39.96c-.5-.38-1.03-.7-1.62-.94l-.36-2.54c-.05-.24-.24-.41-.48-.41h-3.84c-.24 0-.43.17-.47.41l-.36 2.54c-.59.24-1.13.57-1.62.94l-2.39-.96c-.22-.08-.47 0-.59.22L2.74 8.87c-.12.21-.08.47.12.61l2.03 1.58c-.05.3-.09.63-.09.94s.02.64.07.94l-2.03 1.58c-.18.14-.23.41-.12.61l1.92 3.32c.12.22.37.29.59.22l2.39-.96c.5.38 1.03.7 1.62.94l.36 2.54c.05.24.24.41.48.41h3.84c.24 0 .44-.17.47-.41l.36-2.54c.59-.24 1.13-.56 1.62-.94l2.39.96c.22.08.47 0 .59-.22l1.92-3.32c.12-.22.07-.47-.12-.61l-2.01-1.58zM12 15.6c-1.98 0-3.6-1.62-3.6-3.6s1.62-3.6 3.6-3.6 3.6 1.62 3.6 3.6-1.62 3.6-3.6 3.6z";
    private static final String ICONO_BACKUP = "M21 4H3c-1.1 0-2 .9-2 2v1h22V6c0-1.1-.9-2-2-2zM21 8H3v10c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V8zM8 14h8v2H8v-2z";
    private static final String ICONO_SALIR = "M17 7l-1.41 1.41L18.17 11H8v2h10.17l-2.58 2.58L17 17l5-5zM4 5h8V3H4c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h8v-2H4V5z";

    private BarraNavegacion() {
    }

    public static HBox crear(Navegador nav, String actual) {
        HBox barra = new HBox(34);
        barra.getStyleClass().add("nav-bar");
        barra.setAlignment(Pos.CENTER);
        barra.getChildren().addAll(
                boton("Menú principal", ICONO_INICIO, () -> nav.mostrar(RUTA_MENU), "menu".equals(actual)),
                boton("Nueva factura", ICONO_NUEVA, () -> nav.mostrar(RUTA_EDITOR), "editor".equals(actual)),
                boton("Histórico", ICONO_HISTORICO, () -> nav.mostrar(RUTA_HISTORICO), "historico".equals(actual)),
                boton("Clientes", ICONO_CLIENTES, () -> nav.mostrar(RUTA_CLIENTES), "clientes".equals(actual)),
                boton("Configuración", ICONO_CONFIG, () -> nav.mostrar(RUTA_CONFIG), "configuracion".equals(actual)),
                boton("Copia de seguridad", ICONO_BACKUP, () -> nav.mostrar(RUTA_BACKUP), "backup".equals(actual)),
                boton("Salir", ICONO_SALIR, () -> nav.stage().close(), false));
        return barra;
    }

    private static Button boton(String tooltip, String svg, Runnable accion, boolean activo) {
        Button b = new Button();
        b.getStyleClass().add("nav-button");
        if (activo) {
            b.getStyleClass().add("activo");
        }
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