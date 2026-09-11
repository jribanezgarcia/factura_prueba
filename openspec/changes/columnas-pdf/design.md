## Context

En `OpenPdfRenderer.java` las columnas se reparten con pesos relativos. La tabla de líneas (Cant., Descripción, Precio, IVA %, Total) suma 9.3 y se crea en dos sitios que tienen que coincidir: `tablaLineasVacia` y `tablaRelleno`, que dibuja el marco de columnas hasta el cierre. El bloque de suplidos suma 8.3 y la rejilla de liquidación 3.6, dentro de su propia columna del contenedor de totales.

## Goals / Non-Goals

**Goals:** más ancho para la descripción; importes centrados; divisiones verticales de importe alineadas en las tres piezas.

**Non-Goals:** cambiar la rejilla de desglose de IVA, las cabeceras, la tipografía o el cálculo de la paginación.

## Decisions

### D1. Tabla de líneas

`{0.7f, 4.3f, 1.4f, 1.0f, 1.9f}` → `{0.7f, 5.15f, 1.25f, 0.75f, 1.45f}`, en `tablaLineasVacia` (línea 319) **y** en `tablaRelleno` (línea 596). La suma sigue en 9.3. Si solo se cambiara uno, el marco de columnas no casaría con la tabla.

La descripción gana un 20 %. Total mide 80,3 pt, lo que necesita la banda `TOTAL` para un importe de siete cifras con su símbolo (ver D5).

Primero se aplicó Precio a `1.0f` y Descripción a `5.55f`. Al revisar el PDF, Precio quedaba demasiado justo: 47,4 pt útiles, y «1.000.000,00» mide 47,8 pt a 9 pt en Calibri. Por eso se ensancha a `1.25f`, unos 61 pt útiles, a costa de Descripción. Total no cambia, así que la alineación de D4 se mantiene.

### D2. Por qué no más

Ceder la mitad exacta (`{0.7, 6.45, 0.7, 0.5, 0.95}`) se probó en otro clon: importes como `3.128,10` dejan de caber, se parten en dos renglones y la factura de 20 líneas de `numeroPaginasPorCasos` pasa de una página a dos. Los anchos de D1 son el máximo que cede sin partir ningún importe.

### D3. Alineación

En `tablaLineas`, `l.price()` (línea 335) y `l.total()` (línea 337) pasan de `Element.ALIGN_RIGHT` a `Element.ALIGN_CENTER`. Cantidad e IVA % ya estaban centrados, igual que todas las cabeceras. Descripción sigue a la izquierda. Los importes del bloque de suplidos y de la liquidación siguen a la derecha: son columnas de suma y no se tocan.

### D4. Columnas de importe alineadas

- Suplidos (línea 345): `{6.4f, 1.9f}` → `{7.85f, 1.45f}`. Suma 9.3 como la tabla de líneas, con lo que su columna de importe mide exactamente lo mismo que Total.
- Liquidación (línea 463): `{2.2f, 1.4f}` → `{2.0f, 1.45f}`, es decir, Precio + IVA % y Total. Solo es exacto porque D5 hace que la caja de liquidación mida justo esas tres columnas.

### D5. Totales encajados en la rejilla

Al revisar el PDF de una factura de 1.060.000,00 €, la banda `TOTAL` dejaba el `€` solo en un segundo renglón: la celda de importe tenía 60 pt útiles y «1.060.000,00 €» mide 66,8 a 11 pt en negrita. Se generaron seis propuestas como PDF real y el usuario eligió esta: la columna de importes crece (D1) y las dos cajas de totales se colocan sobre la rejilla de la tabla.

En `bloqueTotales` (líneas 411-427):

- Contenedor: `{3.3f, 3.0f}` → `{5.85f, 3.45f}`. Son Cant. + Descripción y Precio + IVA % + Total, en los mismos pesos que la tabla.
- `celdaIzquierda`: `setPaddingLeft(0f)` y `setPaddingRight(4f)` en lugar de `7f`. Los 4 pt son el blanco mínimo entre las cajas.
- `celdaDerecha`: `setPaddingLeft(0f)` en lugar de `7f`, y `setPaddingRight(0f)`.

Hoy las celdas del contenedor conservan el relleno por defecto de 2 pt en el lado exterior, y por eso las cajas quedan 2 pt metidas respecto a la tabla. Con relleno 0 llegan justo a sus bordes.

En `rejillaDesgloseIva` (línea 435): `{1.0f, 2.0f, 1.7f}` → `{38.78f, 152.08f, 129.26f}`. Son puntos, no pesos: la caja mide 324,12 − 4 = 320,12 pt; TIPO se queda con los 38,78 de Cant., y Base y Cuota reparten el resto en la misma proporción que tenían (1 : 0,85). Como `PdfPTable` los trata como relativos, basta con que sumen el ancho de la caja.

La banda `TOTAL` no cambia: con Total a 80,3 pt su importe tiene 70 útiles, donde cabe hasta «9.999.999,99 €».

Se descartó sacar el `€` al rótulo, bajar la banda a 10 pt o fundirla en una celda: las tres cabían, pero cambiaban el aspecto de la banda o dejaban las cajas sin encajar.

## Risks / Trade-offs

- **Paginación.** Es el riesgo real. `numeroPaginasPorCasos` lo vigila, y la prueba del otro clon lo pasó con estos valores.
- **Importes muy largos.** Con D1 y D5 cabe hasta `9.999.999,99 €` en la banda y `1.000.000,00` en Precio. Por encima se partiría en dos renglones sin perder datos.
- **Caja de liquidación más estrecha.** Pasa de unos 236 pt a 191. Sus rótulos más largos («Total IVA repercutido», «IRPF profesional 15 %») caben con holgura en los 111 pt de la columna de rótulos.
