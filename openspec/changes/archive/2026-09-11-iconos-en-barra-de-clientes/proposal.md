## Why

Hay tres pantallas construidas igual —tabla de registros con una barra de acciones encima—: el Editor, el Histórico y Clientes. Las dos primeras usan botones con icono; Clientes es la única que no, y desentona junto a ellas.

La barra de Clientes es además casi un subconjunto de la del Histórico: `Eliminar` y `Volver` son la misma acción en las dos pantallas, y el requisito «Botones de acción con icono identificativo» ya obliga a que una misma acción lleve el mismo icono en todas partes. Dos de los cuatro iconos ya existen.

## What Changes

- Los botones `Nuevo`, `Editar`, `Eliminar` y `Volver` de la pantalla de Clientes SHALL mostrar un icono identificativo encima de su etiqueta, con el mismo tratamiento que los del Editor y el Histórico.
- `Eliminar` y `Volver` SHALL usar exactamente los mismos iconos que ya usan en el Histórico.
- `Eliminar` SHALL ir en el color del tema, como los demás botones de la barra y como el `Eliminar` del Histórico.
- Ningún botón de barra SHALL mostrarse en rojo: también el `Anular` del Editor, el otro único botón rojo de la aplicación, pasa al color del tema, igual que el `Anular` del Histórico.
- Los botones SHALL compartir fila con el campo de búsqueda, alineados a la derecha.
- Los botones SHALL agruparse por afinidad y separarse con líneas verticales: escritura, destructiva y navegación.
- Dentro de la barra de Clientes, los cuatro iconos SHALL ocupar una caja del mismo tamaño, de modo que sus etiquetas queden alineadas aunque los dibujos midan distinto.
- Estos cuatro botones SHALL dejar de estar cubiertos por el requisito de sombreado de los botones de solo texto.
- No cambia ninguna acción, ningún texto de etiqueta ni el comportamiento de la pantalla.

## Capabilities

### New Capabilities

Ninguna.

### Modified Capabilities

- `invoicing`: se amplía a Clientes el requisito de botones de acción con icono, que hoy alcanza solo al Editor y al Histórico, y se retira Clientes del requisito de sombreado de los botones de solo texto, donde figura expresamente. Se añade además el requisito de iconos de tamaño uniforme dentro de una barra, con alcance inicial en Clientes. Y se retira el color de peligro de los tres requisitos que lo exigían, incluido el de estilo de zona de acciones.

## Impact

- `src/main/resources/com/alcazaba/facturacion/ui/Clientes.fxml`: los cuatro botones pasan a `btn-ribbon` con `SVGPath`, en la misma fila que el buscador, y `Eliminar` pasa de `danger-button` a `default-button`.
- `src/main/resources/com/alcazaba/facturacion/themes/base.css`: una style-class nueva, `caja-icono`, con el tamaño de la caja de los iconos.
- `src/main/resources/com/alcazaba/facturacion/ui/Editor.fxml`: `Anular` pasa de `action-danger-button` a `action-button`.
- Ningún `.java`.
