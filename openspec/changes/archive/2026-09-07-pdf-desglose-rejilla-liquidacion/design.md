## Context

`PdfService.bloqueTotales(ResumenFactura, int, Colores)` (`PdfService.java:515`) construye un `PdfPTable` de dos columnas `{3.1f, 1.7f}` al 44 % del ancho, alineado a la derecha, y va añadiendo filas sin borde con `filaResumen(...)` (`PdfService.java:620`) y `filaDescuento(...)` (`PdfService.java:607`). El recorrido depende de si hay descuento:

- sin descuento, un solo bucle sobre `r.getGrupos()` que emite `base` + `cuota` por grupo (`PdfService.java:526-533`);
- con descuento, un primer bucle que emite sólo las bases brutas, luego la fila de descuento, y **un segundo bucle completo** que vuelve a emitir base y cuota por grupo (`PdfService.java:535-544`).

De ahí salen las nueve filas del caso de tres tipos con descuento. Los rótulos los deciden `nombreBaseGrupo(...)` (`PdfService.java:588`) y `nombreBaseImponibleGrupo(...)` (`PdfService.java:602`).

Los datos ya están todos en `ResumenFactura`: `getGrupos()` con `getBase()`, `getBaseBruta()`, `getCuota()`, `getPorcentaje()`, `isExento()`, `getMotivoExencion()`; y a nivel de factura `getBaseTotal()`, `getBaseBruta()`, `getImporteDescuento()`, `getIvaTotal()`, `getImporteRetencion()`, `getNombreRetencion()`, `getPorcentajeRetencion()`, `getTotalSuplidos()` y `getTotal()`. **No hace falta tocar `CalculoService`.**

La maqueta que hay que reproducir es `prototipos/pdf-totales-rejilla-hermanas.html`, con sus tres casos.

## Goals / Non-Goals

**Goals:**

- Que el número de renglones del bloque no dependa de si hay descuento.
- Que la factura imprima una suma de bases y una suma de cuotas, hoy inexistentes.
- Que el bloque hable el mismo idioma visual que la tabla de líneas y la de suplidos: rejilla con cabecera de banda.

**Non-Goals:**

- No se toca el editor. Su matriz y su escalera se quedan como están.
- No se tocan los cálculos: ni el reparto del descuento, ni el ajuste de céntimos, ni la base de retención.
- No se añaden recargo de equivalencia, portes ni pronto pago, aunque la referencia los tenga.
- No se cambia el contenido ni la nota legal de la tabla de suplidos; solo se compacta su cuerpo.

## Decisions

### D1. Decisión: dos rejillas dentro de una tabla contenedora

El bloque es un `PdfPTable` de dos columnas `{3.3f, 3.0f}` al 100 % del ancho, sin bordes, con una rejilla anidada en cada celda. Así las dos cabeceras quedan a la misma altura sin depender de cuántas filas tenga el desglose, y el bloque entero viaja junto si hay salto de página.

Ambas celdas contenedoras van con `setBorder(Rectangle.NO_BORDER)` y `setVerticalAlignment(Element.ALIGN_TOP)`; la izquierda con `setPaddingRight(7f)` y la derecha con `setPaddingLeft(7f)`, que es la separación de 14 px de la maqueta.

Alternativa descartada: dos tablas hermanas sueltas con `setWidthPercentage` y alineación. No garantizan la misma altura de arranque ni se mantienen juntas al paginar.

### D2. Decisión: el descuento es una nota, no un bloque duplicado

Desaparece el segundo bucle. Las bases que se imprimen son siempre las netas (`grupo.getBase()`), y cuando `r.getImporteDescuento() > 0` se añade bajo la rejilla izquierda una única línea de 7 pt en `c.oscuro`:

> Bases netas tras el descuento comercial del N % (−IMPORTE s/ BRUTA).

Cuando además haya algún grupo exento con motivo, la nota continúa con ` Exención MOTIVO.` — un solo motivo, el del primer grupo exento que lo tenga, para no convertir la nota en un párrafo.

Esto abarata el bloque de nueve renglones a cinco en el caso peor, y elimina el único sitio del PDF donde una misma cifra aparecía dos veces con dos rótulos distintos.

Alternativa descartada: una columna `Descuento` en la rejilla, como hace la referencia. Es más informativa, pero obliga a repartir el descuento por tipo y a decidir dónde cae el céntimo de ajuste; el reparto ya lo hace `CalculoService` sobre las bases, y exponerlo en dos columnas invita a que no cuadre a la vista.

### D3. Geometría y estilo exactos

Los colores salen todos de `Colores` y del acento configurado, sin literales nuevos.

| Elemento | Valor |
|---|---|
| Rejilla izquierda | 3 columnas `{1.0f, 2.0f, 1.7f}`, 100 % de su celda |
| Rejilla derecha | 2 columnas `{2.2f, 1.4f}`, 100 % de su celda |
| Cabecera: fondo | `c.base` |
| Cabecera: texto | negrita 7 pt, `BLANCO`, mayúsculas, `Chunk.setCharacterSpacing(0.5f)` |
| Cabecera: alineación | centrada; padding 4 pt |
| Cuerpo: fondo | `c.clarisimo` |
| Cuerpo: texto | 8.5 pt, `TINTA`; padding 3.5 pt |
| Bordes | sólo verticales entre columnas y contorno exterior, `c.bordeTabla` |
| Fila `Totales` | fondo `c.claro`, negrita, borde superior `c.bordeTabla` |
| Fila de retención | 8.5 pt cursiva, `ROJO_DESCUENTO`, etiqueta e importe |
| Nota del descuento | 7 pt, `c.oscuro`, sin borde, 3 pt de separación superior |
| Banda `TOTAL` | fondo `c.base`; rótulo e importe en negrita 11 pt `BLANCO`, padding 5 pt, centrados verticalmente; importe a la derecha |

El contorno «sólo verticales» se consigue como en la tabla de líneas: cada celda con `setBorder(Rectangle.LEFT | Rectangle.RIGHT)` y color `c.bordeTabla`, más una fila de cabecera con borde inferior y la última celda con borde inferior. Si en la implementación resulta más limpio, se acepta el contorno completo de `celdaLinea(...)`, siempre que el peso visual sea el de la maqueta.

### D4. Decisión: la columna `Tipo` imprime el número, no el rótulo

Cada fila de la rejilla izquierda muestra en `Tipo` el porcentaje formateado como número español con dos decimales — `21,00`, `10,00` — y `Exento` cuando el grupo no tiene porcentaje. Es lo que hace la factura de referencia y evita repetir la palabra «IVA» en cada fila cuando la cabecera de la columna ya lo dice.

La cuota de un grupo exento se imprime como `—`, no como `0,00`: un guion dice «aquí no hay cuota», un cero dice «la cuota es cero», y para un exento lo primero es lo correcto.

### D5. Decisión: la liquidación siempre muestra base y total de IVA

Las dos primeras filas de la rejilla derecha, `Base imponible` y `Total IVA repercutido`, aparecen **siempre**, aunque la factura tenga un único tipo y coincidan con la fila `Totales` de la izquierda. Esa repetición es deliberada: es el resumen que se pedía y es lo que hace comprobable la banda del total sin cruzar de rejilla.

Las filas de retención y de suplidos aparecen sólo si su importe es mayor que cero, con la misma condición que hoy (`PdfService.java:545-550`).

### D6. Rótulos

| Fila | Rótulo |
|---|---|
| Cabecera izquierda | `TIPO`, `BASE IMPONIBLE`, `CUOTA IVA` |
| Última fila izquierda | `Totales` |
| Cabecera derecha | `LIQUIDACIÓN` (una celda con `setColspan(2)`) |
| Liquidación | `Base imponible`, `Total IVA repercutido` |
| Retención | `getNombreRetencion() + " " + getPorcentajeRetencion() + " %"`, o `Retención N %` si el nombre viene vacío — mismo criterio que `PdfService.java:540-543` |
| Suplidos | `Suplidos` |
| Banda | `TOTAL` |

Los importes de las rejillas se imprimen sin símbolo de moneda; el `€` aparece solo en la banda `TOTAL`, que usa `Formatos.moneda(...)`. La retención se imprime con el signo menos delante; los suplidos con un `+` delante, para que la banda del total se pueda comprobar en vertical.

### D7. Decisión: el editor no se toca en este change

El requisito vigente obliga a que editor y PDF compartan orden y rótulos. Se relaja: ambos siguen obligados a mostrar la base de cada tipo junto a su cuota, pero cada uno con su forma —matriz y escalera en pantalla, rejilla doble en papel—. Unificar los dos exigiría rehacer el pie del editor, que se rediseñó hace dos días en `2026-09-06-desglose-totales-matriz-suplidos`, y no es lo que se ha pedido.

Queda anotado como candidato para una revisión posterior, junto con las propuestas descartadas de `prototipos/pdf-totales-r1-diez-propuestas.html` y siguientes.

## Risks / Trade-offs

- **El descuento pierde protagonismo.** Pasa de fila roja en el cuerpo del bloque a nota de 7 pt bajo la rejilla. Es el precio de no duplicar el bloque; si al ver el PDF real resulta demasiado discreto, la salida es subir la nota a 8 pt y dejarla en cursiva roja, sin tocar la estructura.
- **`Base imponible` aparece dos veces** en el caso de un solo tipo: en la fila `Totales` de la izquierda y en la liquidación de la derecha. Es la consecuencia aceptada en D5.
- **Los tests del PDF buscan cadenas.** Cinco de `PdfServiceTest` fallan por rótulos, no por importes. Hay que reescribir sus aserciones sobre los rótulos nuevos y, sobre todo, mantener las que comprueban que las cifras cuadran.
- **Anchos de columna.** `BASE IMPONIBLE` es el rótulo más largo de la rejilla izquierda y `Total IVA repercutido` el de la derecha; con el reparto de D3 caben, pero conviene comprobarlo con el acento configurado y con Calibri ausente (fallback a Helvetica, más ancha).
