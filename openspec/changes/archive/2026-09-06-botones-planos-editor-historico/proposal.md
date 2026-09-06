## Why

Los botones de las barras de acciones del Editor y del Histórico son cajas blancas con borde sobre el fondo gris de la barra. El resultado es una tira de rectángulos blancos que pesa más que el contenido de la pantalla: el ojo lee primero los siete u ocho recuadros y después el icono que hay dentro.

La barra de navegación superior, que es la que se ve en todas las pantallas, ya resuelve lo mismo del modo contrario: `.nav-button` es transparente, sin borde, y lo único que se ve es la silueta del icono y su etiqueta; el fondo lo pone la barra, no el botón. Es el mismo patrón que usan las barras de herramientas planas del software actual, y es lo que el usuario quiere ver también en las barras de acciones.

El cambio es puramente de presentación: los botones ya tienen la geometría correcta (icono arriba, texto debajo, anchura fija) desde el change `botones-ribbon-editor-historico`. Lo único que sobra es la caja.

## What Changes

- Los 8 botones de la barra de acciones del Editor y los 6 del Histórico pierden su fondo y su borde: pasan a ser transparentes y adoptan el color del contenedor en el que están, exactamente como los botones de la barra de navegación superior.
- Se conserva todo lo demás: icono monocromo arriba, etiqueta debajo, anchura fija de 64 px, envoltura a dos líneas, separadores verticales entre grupos y el orden actual de los botones.
- El estado de reposo deja de tener caja. El *hover* la insinúa con un velo gris translúcido, igual que hace `.nav-button:hover`. El *pressed* usa el mismo velo, más marcado. El foco de teclado dibuja un borde de color de acento, para que la navegación con `Tab` siga siendo visible ahora que el borde en reposo desaparece.
- El botón principal (`Guardar` en el Editor, `Buscar` en el Histórico) pierde su fondo de acento. Para no perder su jerarquía, su icono y su texto pasan al color de acento del tema (`-fx-accent`) y el texto va en negrita. Es la única variante que necesita colores nuevos: sus colores actuales están pensados *sobre* el acento (blanco o casi negro) y serían ilegibles sin ese fondo.
- Las variantes secundaria y destructiva **no cambian de color**: su texto y su icono ya se eligieron para leerse sobre el fondo de la barra, no sobre el blanco del botón. Solo se les quita la caja.
- La banda gris de la `.action-bar` del Editor **se mantiene**, igual que se mantiene la banda de color de la barra de navegación. Los botones se funden con ella. Si al verlo se prefiere una barra completamente plana, la tarea 5 (opcional) lo hace con dos líneas.
- Quedan **fuera** de este change: los botones `Añadir línea` y `Eliminar línea` del Editor (son botones de tabla, no de barra de acciones, y conservan su caja blanca), los botones de formulario y de diálogo, y las pantallas Clientes, Configuración, Backup, Versiones, Arranque y el modal de generación mensual.
- No cambia ningún texto, ningún `onAction`, ningún `fx:id`, ningún FXML, ningún controlador ni ninguna lógica de negocio.

## Capabilities

### New Capabilities

Ninguna.

### Modified Capabilities

- `invoicing`: se modifica «Botones de acción con icono identificativo» para que el botón deje de tener recuadro y borde visible y para redefinir el color del icono en la variante principal; y «Estilo de zona de acciones en tema por defecto» para que los botones de la barra de acciones del Editor dejen de mostrarse con fondo blanco, dejando fuera a `Añadir línea` y `Eliminar línea`, que sí lo conservan.

## Impact

- **Un solo fichero**: `themes/base.css`, con un bloque nuevo de reglas compuestas (`.action-button.btn-ribbon`, etc.) inmediatamente después del bloque `.btn-ribbon` existente. Ninguna regla previa se modifica.
- **Los siete `tema-*.css` no se tocan.** La variante principal se resuelve con la variable `-fx-accent`, que los siete temas ya declaran en su `.root` con exactamente el mismo valor que usan hoy como fondo de `.primary-button`.
- **Ningún FXML se toca**, luego `StyleClassSeparadorTest` no puede verse afectado.
- **La geometría no cambia** (anchura, altura, padding, escala del icono y separadores se quedan como están), luego `EditorBarraAccionesTest` y `EditorTamanoMinimoTest` no deberían moverse. Aun así entran en la verificación.
- Riesgo técnico único: que la regla compuesta de `base.css` no gane a la regla simple del tema, que se carga después. `design.md` D2 explica por qué gana y deja una palanca de reserva.
