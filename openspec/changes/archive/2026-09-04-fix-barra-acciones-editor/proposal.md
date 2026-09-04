## Why

Al guardar una factura desaparecen botones de la barra de acciones del editor. El usuario lo describe como que «se contraen las demás» cuando sale el botón Anular.

La hilera no es un `HBox`: es un **`ToolBar`** (`Editor.fxml:17`). Cuando el contenido no cabe, un `ToolBar` no comprime ni recorta — **mueve los últimos elementos a un menú de desbordamiento** accesible por un chevrón `»` diminuto en el extremo derecho. Los botones no se contraen: se esconden ahí, donde es muy fácil no encontrarlos.

Dentro hay **ocho** botones: Guardar, Exportar PDF, Versiones, Crear rectificativa, Anular, Restaurar, Nueva factura y Volver.

El disparador es doble, y por eso el fallo salta justo al guardar:

1. aparece `btnAnular`, que `actualizarBotonesEstado()` solo muestra con la factura abierta y en estado EMITIDA;
2. `lblTitulo` pasa de «Nueva factura» a «Factura C-59/7 (v1)» (`EditorController:974`), bastante más ancho y **sin ningún límite de anchura**.

Estimando a 1024 px (padding del BorderPane 16+16, de `.card` 16+16 y de `.action-bar` 8+8 → unos 944 px útiles; botones con `-fx-padding: 8px 14px` a 13 px y `-fx-spacing: 8px`), el bloque izquierdo se lleva ~275 px y los seis botones de una factura nueva ya suman ~670 px sobre ~668 disponibles. Es decir, **la barra ya está al límite antes de guardar**; el séptimo botón y el título más largo la pasan con holgura.

De paso, dos botones de esa barra están mal declarados: `btnVersiones` y `btnRectificativa` (`Editor.fxml:26-27`) tienen `fx:id` pero **no** tienen campo `@FXML` en el controlador, así que `actualizarBotonesEstado()` no puede gobernarlos. En una factura nueva salen habilitados y al pulsarlos solo responden con un `Dialogos.info` de «abra primero la factura».

## What Changes

- Se acortan las etiquetas más largas del editor: «Crear rectificativa» → «Rectificativa», «Nueva factura» → «Nueva», y «Eliminar línea (Supr)» → «Eliminar línea» con el atajo movido al tooltip.
- Se reduce el padding lateral de los botones de acción de `14px` a `10px`. Al estar en la regla compartida de `base.css`, se aplica a **todas** las pantallas por igual, que es la uniformidad que el usuario pide expresamente.
- `lblTitulo` pasa a tener anchura máxima con elipsis, para que un número de factura largo no pueda volver a empujar los botones fuera.
- `btnVersiones` y `btnRectificativa` reciben su campo `@FXML` y se deshabilitan cuando no hay factura abierta, en lugar de responder con un aviso.
- **No se quita ningún botón**: fue una decisión explícita del usuario al plantearle la alternativa.

## Capabilities

### New Capabilities

Ninguna.

### Modified Capabilities

- `invoicing`: se modifica «Menú y navegación», que nombra literalmente los botones de la barra del editor, y se añade un requisito de que la barra de acciones no pueda ocultar botones.

## Impact

- `ui/Editor.fxml`: etiquetas de tres botones, `maxWidth` del título (130 px), `maxWidth` del chip ANULADA (80 px), y sustitución de `ToolBar` por `HBox` (el `ToolBarSkin` ignora `maxWidth` de sus items).
- `themes/base.css`: padding lateral de la regla compartida de botones. Afecta visualmente a todas las pantallas (más compactos), sin cambiar colores ni bordes.
- `ui/EditorController`: dos campos `@FXML` nuevos y su estado en `actualizarBotonesEstado()`.
- `EditorBarraAccionesTest`: test nuevo con dos métodos (caso Anular visible y caso factura anulada).
- No se toca lógica de negocio, ni cálculo, ni persistencia, ni PDF.
