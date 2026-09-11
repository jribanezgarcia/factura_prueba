## Why

En la tabla de líneas del PDF, la descripción es la columna que más texto lleva y la que más veces se parte en varias líneas, pero Precio, IVA % y Total ocupan bastante más de lo que necesitan sus cifras. Además, las divisiones verticales de la zona de importes no coinciden entre la tabla de líneas, el bloque de suplidos y la rejilla de liquidación, que están una encima de otra.

## What Changes

- La columna Descripción SHALL ganar el ancho que ceden Precio, IVA % y Total.
- Precio y Total SHALL mostrarse centrados en su celda, como ya lo están Cantidad e IVA % y las cabeceras.
- La columna de importe del bloque de suplidos y la de la rejilla de liquidación SHALL medir lo mismo que la columna Total, de modo que sus divisiones verticales queden alineadas.
- El ancho total de la tabla no cambia, y una factura que antes cabía en una página SHALL seguir cabiendo.
- No cambia ningún importe, texto, color ni tipografía.

## Capabilities

### New Capabilities

Ninguna.

### Modified Capabilities

- `pdf-rendering`: se añade un requisito sobre el reparto de anchos y la alineación de la tabla de líneas y de las columnas de importe que quedan debajo.

## Impact

- `src/main/java/com/alcazaba/facturacion/pdf/OpenPdfRenderer.java`: seis valores de ancho y dos alineaciones.
- Ningún test cambia; `numeroPaginasPorCasos` actúa como guarda de la paginación.
