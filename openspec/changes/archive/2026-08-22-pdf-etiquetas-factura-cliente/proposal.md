# Etiquetas en el bloque FACTURA y campos etiquetados en «Facturar a»

## Why

Comparando con el prototipo aprobado, el bloque derecho muestra el número y la fecha sin sus rótulos («Serie / Nº», «Fecha») y la tarjeta «Facturar a» apila los datos sin indicar qué campo es cada uno; además falta hacer visible el código postal dentro de Población.

## What Changes

- Bloque FACTURA: bajo el título SHALL aparecer `SERIE / Nº` como rótulo pequeño sobre el número completo, y `FECHA` como rótulo pequeño sobre la fecha.
- Tarjeta «Facturar a»: los datos del cliente SHALL presentarse como pares etiqueta→valor (Nombre / NIF / Dirección / Población / Email); Población SHALL incluir el código postal (y la provincia entre paréntesis cuando exista). Las filas con campo vacío no aparecen.

## Capabilities

### New Capabilities

- (ninguna)

### Modified Capabilities

- `invoicing`: requisito modificado dentro de «Exportación a PDF» (rótulos del bloque FACTURA y campos etiquetados de «Facturar a»).

## Impact

- `pdf/PdfService.java`: `dibujarBloqueFactura` y `tarjetaCliente`. Sin cambios de modelo ni servicio.
