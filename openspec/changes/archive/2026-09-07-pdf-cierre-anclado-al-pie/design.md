## Context

`exportar(...)` (`PdfService.java:104-138`) monta el documento en este orden: `tarjetas(...)`, tabla de líneas, bloque de suplidos, bloque de totales y caja de observaciones, todo con `doc.add(...)`. Al ir en el flujo, el cierre cae justo donde acaben las líneas.

El evento de página `CabeceraPie` (`PdfService.java:834`) dibuja en `onEndPage` los datos de empresa o el logo, el bloque `FACTURA / Serie-Nº / Fecha`, el separador, el pie legal, la marca `ANULADA` y el `Página X de Y`. Todo eso ya se repite en cada página; las tarjetas no, porque están en el flujo.

`margenes(...)` (`PdfService.java:715-729`) calcula el margen superior con `CabeceraLayout` y el inferior a partir del número de líneas del pie legal: `max(112, 30 + líneas × 9 + 28)`. Eso significa que **un pie legal largo ya encoge el área de texto por sí solo**, así que anclar contra `doc.bottom()` respeta el pie sin lógica adicional.

`exportarAgrupado(...)` (`PdfService.java:94-102`) genera cada factura por separado y concatena los PDF, de modo que el anclaje funciona por factura sin tocar nada.

Las maquetas aprobadas son `prototipos/pdf-cierre-anclado-al-pie.html` (anclaje y relleno) y `prototipos/pdf-multipagina-cliente-repetido.html`, variante **B**.

## Goals / Non-Goals

**Goals:**

- Que el cierre caiga siempre en el mismo sitio, independientemente del número de líneas.
- Que la tabla de líneas no acabe en el aire: llega abajo con filas vacías, como un talonario.
- Que una factura de varias páginas identifique al cliente en todas ellas.

**Non-Goals:**

- No se tocan los cálculos ni los importes.
- No se cambia el contenido, el orden ni los rótulos de ninguna tabla.
- No se toca el editor.
- No se añade nada al `Página X de Y`, que se queda como está.

## Decisions

### D1. Decisión: el relleno es una tabla aparte, no filas añadidas a la de líneas

No se puede saber de antemano cuántas páginas ocupará la tabla de líneas, así que tampoco cuántas filas de relleno hacen falta. La solución es medir **después**:

1. Construir el cierre antes de añadirlo y medirlo. Para cada tabla, `setTotalWidth(anchoUtil)` + `setLockedWidth(true)` y luego `getTotalHeight()`; `hCierre = hSuplidos + esp + hTotales + esp + hObservaciones`.
2. `doc.add(tablaLineas(...))` sin relleno.
3. `float y = writer.getVerticalPosition(true)` para saber dónde quedó el cursor.
4. `float hueco = y − doc.bottom() − hCierre`.
5. Si `hueco > 0`, añadir un `PdfPTable` de relleno con las mismas columnas `{0.7f, 4.3f, 1.4f, 1.0f, 1.9f}`, **sin cabecera**, con `n = floor(hueco / altoFila)` filas construidas con `celdaLinea("", fila, ...)`, arrancando el índice `fila` donde lo dejó la última fila real para no romper la alternancia de color. El resto (`hueco − n·altoFila`) se absorbe con un espaciador sin borde antes del cierre.
6. Si `hueco <= 0`, `doc.newPage()` y repetir el cálculo con `y = doc.top()`.
7. `doc.add(bloqueSuplidos)` si lo hay, `doc.add(bloqueTotales)` y `doc.add(cajaObservaciones)` si la hay.

Visualmente es indistinguible de que la tabla siga, porque comparte anchos, bordes, alto de fila y rayado.

Alternativa descartada: calcular el relleno antes de añadir la tabla. Exige simular la paginación o generar el PDF dos veces.

### D2. Decisión: el cálculo del relleno se extrae a un método puro

`filasDeRelleno(float y, float bottom, float hCierre, float altoFila)` devuelve el número de filas. Sin PDF de por medio se puede testear el caso normal, el de hueco cero, el negativo y el de resto no exacto, que es donde de verdad se falla.

El alto de fila se toma midiendo una fila real de la tabla, no con una constante suelta: si algún día cambia el padding de `celdaLinea(...)`, el relleno sigue cuadrando.

### D3. Decisión: las tarjetas salen del flujo y se dibujan en el evento de página

`tarjetas(...)` deja de añadirse con `doc.add(...)` y pasa a dibujarse en `onEndPage` con `writeSelectedRows(...)`, justo bajo el separador. El margen superior que devuelve `margenes(...)` crece con el alto de la tarjeta.

La clave de por qué esto sale barato es la variante elegida: en las páginas siguientes `FACTURAR A` conserva **el mismo ancho y el mismo alto** que en la primera y solo se deja en blanco el hueco de `DATOS DE PAGO`. Como el alto reservado es idéntico en todas las páginas, **no hace falta ajustar los márgenes página a página**, que era el punto delicado.

En la página 1 se dibujan las dos tarjetas; a partir de la 2, solo la izquierda. La condición es el número de página, que el evento ya lleva en `paginasReales`.

Alternativa descartada: `FACTURAR A` a ancho completo en las páginas siguientes. Se ve mejor aprovechada la hoja, pero cambia el alto de la tarjeta entre páginas y obliga a `setMargins(...)` antes de cada salto.

### D4. Decisión: el cuerpo del pie legal se extrae a una constante

El `7.5f` está escrito en `dibujarPieLegal(...)` y otra vez en `margenes(...)`, que lo usa para calcular cuántas líneas ocupará el pie y de ahí el margen inferior. Cambiar solo uno descuadra la caja. Se extrae a una constante privada y se baja a `6.5f`, con lo que el margen inferior se ajusta solo.

### D5. Suplidos y observaciones

La tabla de suplidos va **después** del relleno, pegada al bloque de totales: sigue leyéndose como continuación del detalle. Las observaciones siguen debajo del bloque, como hoy, y crecen con el texto.

Consecuencia asumida y ya discutida: en las facturas con observaciones largas el bloque sube lo que ocupe la caja. Con observaciones de una línea, que es lo normal, el cierre queda clavado.

## Postmortem del primer intento

Se descartó entero. Lo que salió mal y por qué:

| Fallo observado | Causa exacta |
|---|---|
| La tarjeta se dibujaba **encima de la tabla**, tapando hasta cuatro líneas de la factura | `writeSelectedRows(0, -1, x, bordeSuperiorContenido, cb)`: ese método pinta **hacia abajo** desde la `y` dada, y se le pasó el borde superior del área de texto, o sea justo donde empieza la tabla |
| Franja en blanco enorme bajo la cabecera | Consecuencia de lo anterior: el margen reservado para la tarjeta quedaba vacío porque la tarjeta se pintaba más abajo |
| La factura de solo suplidos no anclaba nada | Todo el anclaje estaba dentro de un `if (hayOperaciones)`, así que sin tabla de líneas no se ejecutaba |
| Páginas fantasma y una página final con cuarenta renglones vacíos | `writer.getVerticalPosition(true)` fuerza línea nueva y puede provocar un salto de página; y el propio spec pedía rellenar de filas vacías la página a la que salta el cierre |
| El relleno con el rayado más apretado que la tabla | `altoFilaLinea(...)` medía una fila de celdas **vacías**, que es más baja que una fila con texto |
| La suite en verde con el PDF roto | No había ni un test de número de páginas, ni de en qué página cae el cierre, ni del invariante de la coordenada de la tarjeta |

Dos de esos fallos son de diseño, no de implementación, y se corrigen aquí:

- **D1 se corrige:** cuando el cierre no cabe en la página, la página nueva **no** se rellena de filas vacías. Se completa la página anterior hasta abajo y el cierre viaja solo, anclado al pie.
- **D6 (nueva):** anclar y rellenar son cosas distintas. Anclar es empujar el cierre con un espaciador del alto del hueco, y se hace **siempre**; las filas vacías son solo cómo se pinta ese espaciador cuando hay tabla de líneas. Separarlas es lo que arregla el caso de solo suplidos.

También se aprende que las tareas de la forma «verificar que X» sin criterio comprobable se marcan
como hechas sin verificar nada: cada una debe llevar su invariante o su test.

## Risks / Trade-offs

- **La tarjeta repetida consume alto en cada página.** Una factura que hoy cabe justo en dos páginas puede pasar a tres. Es el precio de identificar al cliente en todas las hojas.
- **Pie legal muy largo.** El margen inferior crece con el número de líneas y no tiene tope: con un texto legal desmedido el área útil se estrecha hasta que el cierre no cabe. El salto de página del paso 6 lo salva, pero conviene comprobar hasta dónde aguanta.
- **Facturas de solo suplidos.** No hay tabla de líneas que estirar, así que el cierre se ancla igual y queda hueco. Es coherente y no merece un caso especial.
- **Tests que cuentan páginas.** `paginacionReflejaPaginasReales` y compañía pueden romperse porque la tarjeta repetida y el relleno cambian la paginación. Hay que ajustarlos, no relajarlos.
