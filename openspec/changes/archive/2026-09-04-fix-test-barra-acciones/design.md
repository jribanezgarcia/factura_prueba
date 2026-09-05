## Context

`EditorBarraAccionesTest` se escribió cuando la barra todavía era un `ToolBar`, y sus aserciones quedaron obsoletas al cambiar el contenedor a `HBox` sin adaptarlas. Ver proposal.md - Why.

## Goals / Non-Goals

**Goals:**

- Que el test detecte de verdad una regresión de la barra de acciones.
- Que el título de la factura se lea entero siempre que haya sitio.
- Que el ancho de los botones de navegación no dependa de la escala del sistema.

**Non-Goals:**

- No se vuelve al `ToolBar`: la sustitución por `HBox` está bien justificada y se mantiene.
- No se cambia ninguna etiqueta ni el relleno de los botones.
- No se toca el texto de los diálogos: eso es el change `unificar-etiquetas-dialogos`.

## Decisions

### D1. Con un `HBox` la propiedad correcta es «no comprimido», no «no oculto»

Es el punto clave de todo el change. Un `ToolBar` **esconde** los elementos que no caben, así que tenía sentido buscar el botón de desbordamiento. Un `HBox` no esconde nada: **comprime** a los hijos que pueden encogerse hasta su anchura mínima. Preguntar por el chevrón en un `HBox` no es solo inútil, es que nunca podrá ser cierto.

Las dos aserciones que sí describen el estado sano:

```java
double anchoEscena = stage.getScene().getWidth();
for (Node child : actionBar.getChildren()) {
    if (child instanceof Button b) {
        assertTrue(b.getBoundsInParent().getMaxX() <= anchoEscena,
                "El boton " + b.getText() + " se sale del ancho de la escena");
        assertTrue(b.getWidth() >= b.prefWidth(-1) - 0.5,
                "El boton " + b.getText() + " esta comprimido por debajo de su ancho preferido");
    }
}
```

La segunda es la que detectaría que alguien añade un botón más. La tolerancia de 0,5 px absorbe el redondeo del layout.

### D2. Cómo comprobar que el test vale

Un test que no se ha visto fallar no demuestra nada — es justamente cómo se llegó a esta situación. La comprobación: revertir temporalmente `base.css` a `-fx-padding: 8px 14px`, ejecutar, y confirmar que **los dos métodos fallan** por la aserción de compresión. Después restaurar los 10 px.

### D3. El tope del título depende del estado, no es una constante

El conflicto real es solo con el chip ANULADA. Como `actualizarBotonesEstado()` ya calcula `anulada`, el tope se fija ahí:

```java
lblTitulo.setMaxWidth(anulada ? 130 : 200);
```

Se descarta quitar el `maxWidth` del FXML y confiar en que el `HBox` comprima el `Label`: con `minWidth="0.0"` el `HBox` puede encogerlo, pero repartiría la reducción también entre los botones, que es exactamente lo que no queremos. Un tope explícito mantiene el título como la única pieza que cede.

El `maxWidth="130.0"` del FXML se queda como valor inicial, porque en el arranque el título es «Nueva factura», que mide bastante menos.

### D4. El ancho de los botones de navegación

Se conserva la anchura uniforme, que es una mejora visual real: alinea los iconos y evita una fila irregular. Lo que cambia es el margen.

```css
-fx-min-width: 90;
-fx-pref-width: 90;
```

Se **elimina** `-fx-max-width`, para que el botón pueda crecer si alguna etiqueta lo pide en vez de recortarla. Con 90 px la caja de contenido son 74 px contra los ~61 que mide «Configuración»: ~13 px de margen, suficiente para el 125 % de escala.

Siete botones a 90 px más seis separaciones de 22 px son 762 px sobre los ~992 disponibles: sigue cabiendo con holgura.

## Verificación

- El test corregido debe **fallar** con el padding revertido a 14 px y pasar con 10 px.
- `EditorTamanoMinimoTest` debe seguir en verde: el ancho de la barra de navegación no afecta al alto, pero conviene confirmarlo.
- A mano: abrir una factura guardada y comprobar que el título se lee entero; abrir una anulada y comprobar que el chip y todos los botones siguen cabiendo.
- A mano: mirar «Configuración» en la barra de navegación, a ser posible con Windows al 125 %.
