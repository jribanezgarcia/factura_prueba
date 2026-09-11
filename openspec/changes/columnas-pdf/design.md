## Context

En `OpenPdfRenderer.java` las columnas se reparten con pesos relativos. La tabla de líneas (Cant., Descripción, Precio, IVA %, Total) suma 9.3 y se crea en dos sitios que tienen que coincidir: `tablaLineasVacia` y `tablaRelleno`, que dibuja el marco de columnas hasta el cierre. El bloque de suplidos suma 8.3 y la rejilla de liquidación 3.6, dentro de su propia columna del contenedor de totales.

## Goals / Non-Goals

**Goals:** más ancho para la descripción; importes centrados; divisiones verticales de importe alineadas en las tres piezas.

**Non-Goals:** cambiar la rejilla de desglose de IVA, las cabeceras, la tipografía o el cálculo de la paginación.

## Decisions

### D1. Tabla de líneas

`{0.7f, 4.3f, 1.4f, 1.0f, 1.9f}` → `{0.7f, 5.55f, 1.0f, 0.75f, 1.3f}`, en `tablaLineasVacia` (línea 319) **y** en `tablaRelleno` (línea 596). La suma sigue en 9.3. Si solo se cambiara uno, el marco de columnas no casaría con la tabla.

La descripción gana un 29 %.

### D2. Por qué no más

Ceder la mitad exacta (`{0.7, 6.45, 0.7, 0.5, 0.95}`) se probó en otro clon: importes como `3.128,10` dejan de caber, se parten en dos renglones y la factura de 20 líneas de `numeroPaginasPorCasos` pasa de una página a dos. Los anchos de D1 son el máximo que cede sin partir ningún importe.

### D3. Alineación

En `tablaLineas`, `l.price()` (línea 335) y `l.total()` (línea 337) pasan de `Element.ALIGN_RIGHT` a `Element.ALIGN_CENTER`. Cantidad e IVA % ya estaban centrados, igual que todas las cabeceras. Descripción sigue a la izquierda. Los importes del bloque de suplidos y de la liquidación siguen a la derecha: son columnas de suma y no se tocan.

### D4. Columnas de importe alineadas

- Suplidos (línea 345): `{6.4f, 1.9f}` → `{8.0f, 1.3f}`. Suma 9.3 como la tabla de líneas, con lo que su columna de importe mide exactamente lo mismo que Total.
- Liquidación (línea 463): `{2.2f, 1.4f}` → `{2.54f, 1.06f}`. Esta rejilla vive dentro de la columna derecha del contenedor de totales, así que no puede sumar 9.3; con estos pesos su columna de importe queda a menos de medio punto del ancho de Total, lo que no se aprecia.

## Risks / Trade-offs

- **Paginación.** Es el riesgo real. `numeroPaginasPorCasos` lo vigila, y la prueba del otro clon lo pasó con estos valores.
- **Importes muy largos.** Un total de siete cifras con decimales podría no caber en 1.3. Hoy no hay facturas así; si aparece, se partiría en dos renglones sin perder datos.
