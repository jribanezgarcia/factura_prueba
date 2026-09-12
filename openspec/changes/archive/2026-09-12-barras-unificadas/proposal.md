## Why

El Editor tiene su barra de acciones sobre una franja de un tono más oscuro que la tarjeta (`action-bar`), con los botones en su propia fila. Clientes y el Histórico no: sus botones cuelgan directamente de la tarjeta clara, mezclados con el buscador en Clientes y debajo de los filtros en el Histórico. Las tres pantallas tienen la misma estructura —tarjeta superior con campos y acciones— pero se leen distinto.

El usuario quiere las tres iguales, tomando el Editor como referencia: los iconos siempre arriba, sobre la franja oscura, y los campos debajo.

## What Changes

- En Clientes y en el Histórico, los botones de acción SHALL ir en una fila propia, la primera de la tarjeta, sobre la misma franja `action-bar` que usa el Editor.
- El campo de búsqueda de Clientes y los filtros del Histórico SHALL quedar debajo de esa franja, dentro de la tarjeta y alineados a la izquierda.
- Los filtros del Histórico conservan sus dos filas y sus posiciones; solo cambian de sitio respecto a los botones.
- El Editor no cambia: ya es el modelo.
- No cambia ningún botón, ningún icono, ningún texto ni ninguna acción.

## Capabilities

### New Capabilities

Ninguna.

### Modified Capabilities

- `invoicing`: se añade el requisito «Barra de acciones sobre franja propia en Editor, Clientes e Histórico» y se ajusta «Estilo de zona de acciones en tema por defecto», cuyos escenarios daban por hecho que la fila de botones compartía el fondo claro de la tarjeta.

## Impact

- `src/main/resources/com/alcazaba/facturacion/ui/Clientes.fxml`: la `HBox` única se parte en dos, la de botones con `action-bar`.
- `src/main/resources/com/alcazaba/facturacion/ui/Historico.fxml`: la fila de botones sube por encima del `GridPane` de filtros y recibe `action-bar`.
- `src/main/resources/com/alcazaba/facturacion/themes/base.css`: solo si la franja necesita ocupar todo el ancho de la tarjeta.
- Ningún `.java`, ningún tema, ningún `Editor.fxml`.
