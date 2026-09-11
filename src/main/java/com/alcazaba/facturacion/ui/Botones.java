package com.alcazaba.facturacion.ui;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.DialogPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;

import java.util.ArrayList;
import java.util.List;

/**
 * Iguala el ancho de los botones de solo texto que comparten fila,
 * como hacen los botones de los dialogos de aviso y confirmacion.
 */
public final class Botones {

    private Botones() {
    }

    /**
     * Recorre el arbol y fija el mismo ancho preferido a los botones de
     * cada grupo. Un grupo son dos o mas botones hijos directos del mismo
     * HBox o FlowPane, sin clases de icono, navegacion o menu y fuera de
     * cualquier DialogPane. Debe llamarse con el CSS ya aplicado.
     */
    public static void igualarGrupos(Parent raiz) {
        List<List<Button>> grupos = new ArrayList<>();
        recolectar(raiz, grupos);
        for (List<Button> grupo : grupos) {
            igualar(grupo);
            for (Button b : grupo) {
                b.textProperty().addListener((o, anterior, nuevo) -> igualar(grupo));
            }
        }
    }

    private static void recolectar(Node nodo, List<List<Button>> grupos) {
        if (!(nodo instanceof Parent)) {
            return;
        }
        Parent contenedor = (Parent) nodo;
        if (contenedor instanceof HBox || contenedor instanceof FlowPane) {
            List<Button> grupo = new ArrayList<>();
            for (Node hijo : contenedor.getChildrenUnmodifiable()) {
                if (hijo instanceof Button && aceptado((Button) hijo)) {
                    grupo.add((Button) hijo);
                }
            }
            if (grupo.size() >= 2) {
                grupos.add(grupo);
            }
        }
        for (Node hijo : contenedor.getChildrenUnmodifiable()) {
            recolectar(hijo, grupos);
        }
    }

    private static boolean aceptado(Button b) {
        if (b.getStyleClass().contains("btn-ribbon")
                || b.getStyleClass().contains("nav-button")
                || b.getStyleClass().contains("menu-item")) {
            return false;
        }
        for (Node p = b.getParent(); p != null; p = p.getParent()) {
            if (p instanceof DialogPane) {
                return false;
            }
        }
        return true;
    }

    private static void igualar(List<Button> grupo) {
        for (Button b : grupo) {
            b.setPrefWidth(Region.USE_COMPUTED_SIZE);
        }
        double max = 0;
        for (Button b : grupo) {
            max = Math.max(max, b.prefWidth(-1));
        }
        for (Button b : grupo) {
            b.setPrefWidth(max);
        }
    }
}
