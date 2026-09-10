## Context

`base.css:133-141` da radio, borde de 1px, padding `8px 10px` y `13px` a las cinco clases de botón (`primary-button`, `default-button`, `danger-button`, `action-button`, `action-danger-button`). Cada `tema-*.css` les pone después fondo blanco, color de texto y color de borde.

`base.css:170-212` define encima la variante plana de la barra de acciones: cinco selectores de dos clases (`.primary-button.btn-ribbon` y compañeros) que anulan fondo y borde, más `:hover`, `:pressed` y `:focused`. Gana al tema por especificidad: dos clases contra una. El mecanismo se reutiliza aquí; el aspecto resultante, no.

Inventario: 34 botones muestran solo texto. 32 entran en este change. Quedan fuera los dos de `Arranque.fxml:19,26`.

## Goals / Non-Goals

**Goals:**

- Que un botón de solo texto tenga superficie propia sin recuadro blanco ni borde.
- Que el sombreado sea el mismo en todos los botones de una pantalla, y que acompañe al tema activo sin teñirlo.
- Que la acción principal siga leyéndose como tal.

**Non-Goals:**

- No se tocan los dos botones de `Arranque.fxml`. Es una pantalla fija de 760x520, aparte del resto.
- No se tocan los botones de los diálogos: son `ButtonType` que JavaFX crea dentro del `DialogPane` y no admiten style-class sin `lookupButton`.
- No se cambia ninguna maquetación, ningún `fx:id` ni ningún `onAction`.
- No se revisa el alcance del requisito de zona de acciones más allá de los dos botones de tabla del Editor, aunque hoy hable solo del tema por defecto describiendo algo que `base.css` aplica a todos.

## Decisions

### D1. Decisión: sombreado, no transparencia

La primera versión de esta idea dejaba el botón transparente, calcando la variante plana de `btn-ribbon`. Se descartó al verlo: el botón con icono aguanta la transparencia porque el icono ya le da presencia, y el de solo texto no, se lee como una etiqueta suelta.

La variante se llama `btn-suave` y sí pinta fondo, siempre el mismo, sin borde. De `btn-ribbon` conserva el mecanismo (selector de dos clases para ganar al tema) y los estados de interacción, no el aspecto.

Todos los botones van en negrita, no solo el principal: sobre un fondo tan tenue el texto normal pierde cuerpo.

### D2. Decisión: el lavado se declara por tema, no se calcula

El lavado buscado es un gris neutro con una pizca del acento del tema: leído como gris, pero sin desentonar. JavaFX no sabe expresarlo. Puede escribirse un `rgba(...)` literal, y puede usarse un color del tema entero, pero no existe forma de decir «el acento de este tema al 4%»: no hay función de mezcla ni de opacidad sobre un color heredado.

Por eso cada tema declara la pareja ya resuelta:

- `-fx-boton-lavado`, para todos los botones.
- `-fx-boton-lavado-fuerte`, para el principal.

Son un gris `(128,128,128)` teñido con el acento del tema, al 10% y al 15% de opacidad. El gris 128 es el mismo que ya usan los `:hover` de `btn-ribbon`, y se elige porque aclara sobre fondo oscuro y oscurece sobre fondo claro, así que la misma idea vale para los 7 temas.

Valores calculados:

| Tema | `-fx-boton-lavado` | `-fx-boton-lavado-fuerte` |
|---|---|---|
| biblioteca8 | `rgba(104, 121, 134, 0.10)` | `rgba(72, 112, 142, 0.15)` |
| esmeralda | `rgba(97, 144, 128, 0.10)` | `rgba(56, 164, 129, 0.15)` |
| negro-dorado | `rgba(151, 141, 108, 0.10)` | `rgba(182, 158, 81, 0.15)` |
| neon | `rgba(131, 118, 160, 0.10)` | `rgba(135, 105, 204, 0.15)` |
| omarchy | `rgba(127, 129, 152, 0.10)` | `rgba(126, 129, 185, 0.15)` |
| sakura | `rgba(143, 116, 127, 0.10)` | `rgba(162, 101, 127, 0.15)` |
| terracota | `rgba(146, 116, 105, 0.10)` | `rgba(170, 100, 74, 0.15)` |

Alternativa descartada: apilar un gris encima de `-fx-faint-focus-color`, que ya existe en los 7 y no obligaría a tocarlos. Sale más cargado que lo aprobado y el tono resultante no se controla, solo se sufre.

### D3. Decisión: el principal se marca por intensidad y por color de texto

Con negrita en todos, la negrita deja de señalar nada. Al principal le quedan dos señales: el lavado fuerte y el texto de acento. Las dos juntas, porque cada una por separado se pierde según el tema.

Los botones de peligro conservan su texto rojo sobre el lavado suave, como hoy.

### D4. Decisión: entran los dos botones de línea de la generación mensual

`GenerarFacturasMensuales.fxml:72,73` no declaran ningún estilo y muestran el gris por defecto de JavaFX. Son gemelos funcionales de los del Editor y quedaban descolgados. Se les da primero `action-button` y luego `btn-suave`.

Los dos de `Arranque.fxml` siguen fuera: esa pantalla no comparte ni tamaño ni estructura con las demás.

## Riesgos

`StyleClassSeparadorTest` recorre todos los FXML y falla si un `styleClass` separa clases con espacio en vez de con coma. La forma correcta es `styleClass="primary-button, btn-suave"`.

El árbol de trabajo ya tiene aplicada la versión transparente anterior a esta decisión, en `base.css` y en los 6 FXML, sin confirmar. Este change no parte de un árbol limpio: hay que ajustar lo que ya está, no añadirlo encima.

## Verificación

- `mvn test` en verde. Ningún test consulta las clases de estos botones: los de layout buscan por `fx:id` y por estructura.
- Recorrer las 6 vistas en biblioteca8: todos los botones sobre el mismo sombreado, sin borde, en negrita, y el principal más marcado y en color de acento.
- Repetir en negro dorado y en sakura, los dos temas donde el lavado se aleja más del gris, para confirmar que sigue leyéndose como gris y no como un color.
- En el Editor, la barra superior y los botones de línea deben leerse como el mismo lenguaje visual.
- En Generar mensuales, los cuatro botones deben verse iguales entre sí: hasta ahora dos llevaban el gris de la plataforma y dos el estilo del tema.
- `Configuracion` es la vista con más botones y la primera en delatar apelmazamiento. Si se ve apretado, subir el `-fx-spacing` del HBox contenedor, nunca el padding del botón.
