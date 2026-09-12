## Context

Estado tras el change `barras-unificadas`:

| | Editor | Clientes | Histórico |
|---|---|---|---|
| Espaciado bajo la barra de navegación | `VBox spacing="8"` | `12` | `12` |
| Relleno de la tarjeta | `card-editor` → 12px | `zona-contenido` → 16px | `zona-contenido` → 16px |
| Contenido de la franja | logo, título, `Region hgrow`, botones | botones | botones |
| Alineación de los botones | derecha (los empuja el `Region`) | izquierda | izquierda |

La franja es la misma `HBox styleClass="action-bar"` en las tres, con su relleno de 8px y su espaciado de 8px en `base.css`.

Medido a 1024×768, la franja ya sale idéntica en las tres pantallas: 69px de alto, 966 de ancho y arranca en y=103. Es decir, **la diferencia de espaciado y relleno no llega a desplazarla**, porque el alto de la franja lo acaban marcando los botones y el ancho lo marca la tarjeta, que ocupa todo el hueco. Lo que sí se ve es dónde caen los botones dentro de ella.

## Goals / Non-Goals

**Goals:** que los iconos caigan en el mismo punto de la pantalla en el Editor, Clientes y el Histórico, y que la franja tenga el mismo alto y el mismo encaje en las tres.

**Non-Goals:** tocar el Editor, los siete temas, los trazados, las etiquetas o las acciones; unificar las pantallas que no tienen barra de iconos (Configuración, Copia de seguridad, Versiones y la generación mensual usan botones de solo texto, `btn-suave`, y quedan fuera).

## Decisions

### D1. Un `Region` que empuja, no `alignment`

En Clientes y en el Histórico los botones pasan a la derecha con un `Region HBox.hgrow="ALWAYS"` delante de todos ellos, que es el mecanismo del Editor. `alignment="CENTER_RIGHT"` daría el mismo resultado visual hoy, pero el `Region` deja el hueco disponible para lo que pueda ir a la izquierda —el conteo de clientes, un título de pantalla— sin volver a mover los botones. La `HBox` conserva `alignment="CENTER_LEFT"`, que es lo que centra los botones verticalmente.

### D2. El Editor manda en el espaciado y en el relleno

Clientes y el Histórico pasan a `VBox spacing="8"` y a `card-editor` en lugar de `zona-contenido`. Se alinea hacia el Editor, no al contrario, porque el Editor es la pantalla apretada: es la que tiene que caber sin scroll a 1024×768 y ya está medida para 8 y 12. Subirla a 12 y 16 le quitaría 8px de alto útil.

Esto **no corrige ningún desplazamiento**: medido, hoy no lo hay. Se hace para que las tres pantallas queden declaradas igual y no vuelvan a divergir, decisión del usuario del 12/09/2026. El efecto visible es que el buscador de Clientes y los filtros del Histórico se acercan 4px al borde de su tarjeta.

`card-editor` solo declara `-fx-padding: 12px`, así que el cambio de clase no arrastra nada más.

### D3. Alto fijo de la franja en `base.css`

`.action-bar` recibe un alto mínimo y preferido iguales, para que lo marque la regla y no el contenido de cada pantalla. El valor es **69px**, que es exactamente el que la franja mide hoy en las tres pantallas: un botón `btn-ribbon` de 55px más el relleno de 8+8 de la franja. Fijarlo no cambia nada de lo que se ve; impide que cambie mañana. Se declara `-fx-min-height: 69px` y `-fx-pref-height: 69px`, sin `max-height`, de modo que si algún día una etiqueta se envuelve en dos líneas la franja pueda crecer antes que recortar el texto.

El logo del Editor mide 40px de alto y el título envuelve hasta tres líneas dentro de ese hueco, así que 69px no aprieta nada de lo que ya hay.

### D4. Los iconos ya comparten caja y escala

Las tres barras usan `caja-icono` (22×22) y la escala 1.05 de `.btn-ribbon .icono-boton`, de modo que el tamaño ya es común y no hay nada que cambiar. Queda como comprobación en las tareas, no como modificación: si a ojo alguno se ve distinto será por su trazado, y eso es un change aparte.

### D5. Las pantallas de solo texto quedan fuera

Configuración, Copia de seguridad, Versiones y la generación mensual no tienen barra de iconos: sus botones son `btn-suave`, colocados junto a los campos a los que pertenecen. Meterles una franja de acciones sería rediseñarlas, no unificarlas.
