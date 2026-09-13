## Why

El requisito «Líneas de factura» dice que la descripción puede ser larga y ocupar varias líneas, pero la tabla del Editor no lo cumple: cuando el texto no cabe en la columna, la fila sigue midiendo lo mismo y el texto se recorta. Al escribir, el cursor arrastra el contenido hacia la derecha y el principio de la descripción deja de verse, de modo que no hay manera de repasar de un vistazo lo que se acaba de teclear.

El PDF ya imprime la descripción entera, partiéndola en varias líneas por su cuenta. El desajuste es solo de la pantalla: lo que se ve en el Editor no es lo que sale impreso.

## What Changes

- La columna Descripción de la tabla de líneas parte el texto largo en varias líneas y la fila crece hasta mostrarlo entero.
- Las filas de descripción corta conservan su alto actual, y las columnas Cant., Precio, Total e IVA siguen en una sola línea.
- Al ensanchar la ventana, la columna de descripción se ensancha y la misma descripción ocupa menos líneas.
- El texto se parte **mientras se escribe**, no solo al confirmar: el editor de la celda envuelve igual que la celda ya confirmada, y la fila crece a la vez.
- No se añaden saltos de línea a mano: el texto se parte solo al llegar al borde de la columna. ENTER conserva su significado actual, confirmar y avanzar a la columna siguiente, y ESCAPE sigue cancelando.
- No hay tope de alto de fila.

## Capabilities

### New Capabilities

Ninguna.

### Modified Capabilities

No se modifica ningún requisito: `skip_specs: true`. El requisito «Líneas de factura» ya dice que «La descripción SHALL poder ser larga y ocupar varias líneas»; este cambio lo implementa. Sus cuatro escenarios describen el flujo de teclado y el borrado de líneas, que no cambian.

## Impact

- `src/main/java/com/alcazaba/facturacion/ui/EditorController.java`: la clase interna `CeldaDescripcion`, el tipo del editor en `CeldaEditable` y el `lookup` de `editarCeldaSegura`.
- `src/test/java/com/alcazaba/facturacion/ui/EditorFlujoTecladoTest.java`: cómo alcanza el editor de la descripción. Lo que comprueba no cambia.
- `src/main/resources/com/alcazaba/facturacion/themes/base.css`: previsiblemente nada; solo si la medición de la tarea 1 demuestra que el alto de fila queda recortado.
- Ningún FXML, ningún tema, ningún modelo, ninguna consulta, nada del PDF.
- Clientes, Histórico y la matriz de IVA no cambian.
