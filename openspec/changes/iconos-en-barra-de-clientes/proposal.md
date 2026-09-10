## Why

Hay tres pantallas construidas igual —tabla de registros con una barra de acciones encima—: el Editor, el Histórico y Clientes. Las dos primeras usan botones con icono; Clientes es la única que no, y desentona junto a ellas.

La barra de Clientes es además casi un subconjunto de la del Histórico: `Eliminar` y `Volver` son la misma acción en las dos pantallas, y el requisito «Botones de acción con icono identificativo» ya obliga a que una misma acción lleve el mismo icono en todas partes. Dos de los cuatro iconos ya existen.

## What Changes

- Los botones `Nuevo`, `Editar`, `Eliminar` y `Volver` de la pantalla de Clientes SHALL mostrar un icono identificativo encima de su etiqueta, con el mismo tratamiento que los del Editor y el Histórico.
- `Eliminar` y `Volver` SHALL usar exactamente los mismos iconos que ya usan en el Histórico.
- Los botones SHALL ocupar su propia fila, separados del campo de búsqueda, como en el Histórico.
- Los botones SHALL agruparse por afinidad y separarse con líneas verticales: escritura, destructiva y navegación.
- Estos cuatro botones SHALL dejar de estar cubiertos por el requisito de sombreado de los botones de solo texto.
- No cambia ninguna acción, ningún texto de etiqueta ni el comportamiento de la pantalla.

## Capabilities

### New Capabilities

Ninguna.

### Modified Capabilities

- `invoicing`: se amplía a Clientes el requisito de botones de acción con icono, que hoy alcanza solo al Editor y al Histórico, y se retira Clientes del requisito de sombreado de los botones de solo texto, donde figura expresamente.

## Impact

- `src/main/resources/com/alcazaba/facturacion/ui/Clientes.fxml`: los cuatro botones pasan a `btn-ribbon` con `SVGPath`, y salen a una fila propia.
- Ningún CSS, ningún `.java` y ninguna otra pantalla.
