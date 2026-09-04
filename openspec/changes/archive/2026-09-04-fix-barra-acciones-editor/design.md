## Context

`Editor.fxml:17-31` era un `ToolBar` con logo, título, chip ANULADA, un `Region` que empuja y ocho botones. `ToolBarSkin` añade un botón de desbordamiento con la clase `tool-bar-overflow-button` y le pasa los elementos que no caben. Ver proposal.md - Why.

**Descubierto durante la implementación:** `ToolBarSkin` ignora `maxWidth` de sus items — el algoritmo de layout del ToolBar asigna a cada hijo su tamaño preferido y crea el overflow cuando el total excede el ancho disponible, sin respetar restricciones de ancho máximo. Se sustituyó el `ToolBar` por un `HBox` que sí respeta `maxWidth` de sus hijos.

## Goals / Non-Goals

**Goals:**

- Que la barra de acciones no pueda esconder botones, ni al aparecer Anular ni con un número de factura largo.
- Que el criterio de etiquetas sea el mismo en todas las pantallas.
- Que los botones que no se pueden usar salgan deshabilitados en vez de contestar con un aviso.

**Non-Goals:**

- No se quita ningún botón de la barra: fue una decisión explícita del usuario.
- No se agrupan acciones en un menú «Más» (ver D3).
- No se renombran botones de otras pantallas: eso va en el change siguiente.

## Decisions

### D1. Ganar ancho por dos vías, no por una

Solo acortar etiquetas deja la barra otra vez cerca del límite, y el fallo volvería con otra escala de fuente del sistema. Se combinan tres medidas:

| Medida | Ganancia estimada |
|---|---|
| «Crear rectificativa» a «Rectificativa» | ~42 px |
| «Nueva factura» a «Nueva» | ~56 px |
| Padding lateral de botón de 14 px a 10 px, por 8 botones | ~64 px |

Total ~160 px frente a los ~85 px que faltaban. El margen sobrante es deliberado: es lo que evita que el fallo reaparezca al añadir un botón o al cambiar la fuente del sistema.

El padding va en la regla compartida `.primary-button, .default-button, .danger-button, .action-button, .action-danger-button` de `base.css:127-135`, así que todas las pantallas quedan igual de compactas sin tocarlas una a una. Los colores y bordes los siguen poniendo los siete temas y no se tocan.

### D2. El título necesita tope

Aunque sobre ancho, `lblTitulo` crece sin límite con el número de factura. Un `maxWidth` con la elipsis por defecto de `Label` recorta el texto en lugar de desplazar los botones. Es lo que convierte el arreglo en estable en vez de en «cabe hoy».

El valor final es `maxWidth="130.0"` (no 200 como se estimó inicialmente). La razón: cuando la factura está anulada, el chip ANULADA (`lblEstado`) aparece entre el título y el `Region` empujador, consumiendo ~80 px extra. Con 200 px de título + 80 px de chip + 110 px de logo = 390 px a la izquierda, y los ocho botones compactos (~640 px), el total supera los ~960 px útiles. Bajando el título a 130 px, el total queda en ~958 px con margen.

`minWidth="0.0"` permite que el `Label` se encoja cuando el chip ANULADA le quita sitio. El `textOverrun` por defecto de `Label` ya es `ELLIPSIS`, así que no hay que declararlo.

El chip ANULADA también recibe `maxWidth="80.0"` para que no crezca más allá de su texto visible.

### D3. Por qué no un menú «Más»

Agrupar Versiones, Rectificativa y Nueva en un `MenuButton` sería lo elegante, pero `.action-button` está definido en los siete temas (`tema-*.css:27-29`) y un `MenuButton` no hereda ese estilo: Modena impone su propio `-fx-text-fill` en `.menu-button > .label`, la flecha se colorea aparte y el popup `.context-menu` se queda sin tematizar. Serían siete ficheros tocados para un problema que se resuelve con etiquetas y padding.

### D4. La aserción correcta del test

El `HBox` sustituye al `ToolBar`, así que ya no existe el botón de desbordamiento `.tool-bar-overflow-button`. La aserción correcta ahora es comprobar que ningún `Button` de la barra tiene bounds vacíos (lo que indicaría que fue empujado fuera del área visible):

```java
HBox actionBar = (HBox) root.lookup(".action-bar");
for (Node child : actionBar.getChildren()) {
    if (child instanceof Button) {
        Bounds bounds = child.localToScene(child.getBoundsInLocal());
        assertFalse(bounds.isEmpty());
    }
}
```

`EditorTamanoMinimoTest` es la plantilla: monta el editor a 1024x768 en el hilo de JavaFX, hace `applyCss()` y `layout()`, y comprueba límites sobre el layout real. El test nuevo tiene **dos métodos**:

1. **`barraNoDesbordaConAnularVisibleYTituloLargo`**: fuerza `btnAnular` a visible/managed y pone un título largo → reproduce el caso de guardar una factura.
2. **`barraNoDesbordaConFacturaAnuladaYTituloLargo`**: oculta `btnAnular`, muestra `btnRestaurar` y `lblEstado` (chip ANULADA), y pone un título largo → reproduce el caso de factura anulada.

### D5. Valores exactos

Para que la implementación no tenga que interpretar nada. Todo lo demás de la barra —logo 110x40, `.action-bar` con `-fx-padding: 8px` y `-fx-spacing: 8px`, radios y colores de los temas— **no se toca**.

**Contenedor de la barra**: se sustituye `ToolBar` por `HBox` con `styleClass="action-bar"` y `alignment="CENTER_LEFT"`. El `ToolBarSkin` ignora `maxWidth` de sus items; el `HBox` sí lo respeta.

**Etiquetas de los botones**, en su orden de aparición en el FXML:

| `fx:id` | Texto actual | Texto nuevo |
|---|---|---|
| `btnGuardar` | Guardar | Guardar *(sin cambio)* |
| `btnExportar` | Exportar PDF | Exportar PDF *(sin cambio)* |
| `btnVersiones` | Versiones | Versiones *(sin cambio)* |
| `btnRectificativa` | Crear rectificativa | **Rectificativa** |
| `btnAnular` | Anular | Anular *(sin cambio)* |
| `btnRestaurar` | Restaurar | Restaurar *(sin cambio)* |
| *(sin id)* | Nueva factura | **Nueva** |
| *(sin id)* | Volver | Volver *(sin cambio)* |
| `btnEliminarLinea` | Eliminar línea (Supr) | **Eliminar línea**, con `Tooltip` «Eliminar línea (Supr)» |

**Padding de los botones**, en `base.css`, regla compartida `.primary-button, .default-button, .danger-button, .action-button, .action-danger-button`:

```css
-fx-padding: 8px 10px;   /* antes: 8px 14px */
```

Es el único valor que cambia de esa regla: `-fx-background-radius`, `-fx-border-radius`, `-fx-border-width`, `-fx-font-size` y `-fx-cursor` se quedan como están.

**Tope del título**, en `Editor.fxml` sobre `lblTitulo`:

```xml
maxWidth="130.0" minWidth="0.0"
```

Hacen falta las dos: `maxWidth` pone el techo y `minWidth="0.0"` permite que el `Label` se encoja cuando el chip ANULADA le quita sitio. El `textOverrun` por defecto de `Label` ya es `ELLIPSIS`, así que no hay que declararlo.

El 130 sale de la cuenta a 1024 px considerando el chip ANULADA: ~960 px útiles en la barra, menos ~640 px de los ocho botones compactados, menos 118 px de logo, menos ~80 px del chip → quedan ~130 px para el título. Si el título fuera más ancho con el chip visible, los botones se desbordarían.

**Tope del chip ANULADA**, en `Editor.fxml` sobre `lblEstado`:

```xml
maxWidth="80.0"
```

Evita que el chip crezca más allá de su texto visible cuando el Label calcula su tamaño preferido.

## Verificación

- El test nuevo debe **fallar** con el FXML actual antes de aplicar el cambio.
- A mano: crear una factura, guardarla y comprobar que al aparecer Anular siguen viéndose todos los botones y no sale el chevrón. Repetir con un número largo y con una factura anulada (chip ANULADA y Restaurar).
- Revisar que las demás pantallas siguen bien con los botones más compactos, sobre todo Histórico y Configuración, que son las que más botones tienen.
