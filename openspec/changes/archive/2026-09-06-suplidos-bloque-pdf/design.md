## Context

`PdfService.tablaLineas(...)` (`PdfService.java:401-422`) recorre `vc.lineas()` y rotula la columna «IVA %» con `l.isExenta() ? "Exento" : l.getIvaPorcentaje() + " %"` (`PdfService.java:416-417`). Un suplido tiene `ivaPorcentaje == null`, así que cae en la rama «Exento».

`bloqueTotales(...)` (`PdfService.java:471`) ya imprime la fila `Suplidos` entre la retención y el TOTAL, y `CalculoService` ya los deja fuera de base, cuotas y retención. Ver proposal.md - Why.

## Goals / Non-Goals

**Goals:**

- Que el PDF no llame «exento» a un suplido.
- Que los suplidos se lean como lo que son: un bloque aparte de las operaciones facturadas.
- Que quede constancia en el documento del régimen que justifica no repercutir IVA.

**Non-Goals:**

- No cambia la forma de introducirlos: en el editor siguen siendo una línea más con el tipo «Suplido», que es lo cómodo para el usuario.
- No cambia el bloque de totales ni ningún importe.
- No se recalculan ni reimprimen facturas ya emitidas.

## Decisions

### D1. Decisión: bloque propio tras la tabla de líneas, no una sección al final

El bloque de suplidos se sitúa entre la tabla de líneas y el bloque de totales (`PdfService.java:118`, donde hoy se añade `tablaLineas(vc, colores)`). Así el lector recorre el documento en el mismo orden en que se forma el importe: operaciones facturadas, suplidos, totales.

Alternativa descartada: dejarlos tras el bloque de totales. El total ya los incluye, así que aparecerían después de la cifra que ayudan a explicar.

### D2. Decisión: la tabla de líneas se omite si todas las líneas son suplidos

Si tras filtrar no queda ninguna línea, se omite la tabla entera en vez de imprimir la cabecera sola. Una factura de solo suplidos es rara pero posible, y una cabecera huérfana sugiere que falta contenido.

### D3. Valores exactos

Filtrado en `tablaLineas(...)`: recorrer solo las líneas con `!l.isEsSuplido()`.

Bloque de suplidos, con el mismo estilo que la tabla de líneas (reutilizar `celdaCabeceraColumna(...)` y `celdaLinea(...)`, `PdfService.java:424-445`):

- Dos columnas, proporciones `{6.4f, 1.9f}`, `setWidthPercentage(100)`, `setSpacingBefore(6)`.
- Cabeceras: `SUPLIDOS` (izquierda) e `IMPORTE` (derecha).
- Una fila por línea de suplido: descripción a la izquierda, `Formatos.moneda(l.getTotalBase())` a la derecha.
- Nota inmediatamente debajo, fuente de 7 pt en el tono `c.oscuro`, sin borde, con este texto literal: «Suplidos pagados en nombre y por cuenta del cliente, facturados a su nombre. No sujetos a IVA ni a retención.»

El bloque se añade al documento solo si hay al menos una línea con `isEsSuplido()`.

## Verificación

- `PdfServiceTest`: una factura con un suplido y una línea normal; el texto extraído contiene `SUPLIDOS` y la nota, la descripción del suplido no aparece dentro de la tabla de líneas, y ninguna línea de suplido se rotula «Exento».
- `PdfServiceTest`: factura sin suplidos; el texto no contiene `SUPLIDOS` ni la nota.
- `PdfServiceTest.paginacionReflejaPaginasReales` sigue en verde.
- Comprobación manual: exportar una factura con suplido y leer el PDF.
