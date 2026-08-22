# Fidelidad del PDF al prototipo: cabecera, tipografía, redondeos, espaciados y colores

## Why

Comparando un PDF real con el prototipo aprobado (`prototipos/pdf-fix-v2.html`) el documento no es fiel: el nombre de la empresa se solapa con el bloque FACTURA y tapa Serie/Nº y fecha, la fuente (Helvetica) no es la del prototipo (Calibri), las tarjetas y cajas carecen de los bordes redondeados, los espaciados difieren (la banda TOTAL casi pega a la fila anterior) y varios tonos no coinciden. Además el pie `Página X de Y` dibuja el dígito total solapado con «de».

## What Changes

- Cabecera: el bloque de datos de empresa (nombre, actividad, contacto) SHALL quedar confinado a su columna sin invadir nunca el bloque FACTURA; si el nombre o una línea no caben en el ancho disponible se reduce el tamaño automáticamente. Serie/Nº y fecha SHALL ser siempre visibles.
- Tipografía: el PDF SHALL usar Calibri embebida (desde las fuentes del sistema Windows) con fallback a Helvetica si no está disponible.
- Bordes redondeados: las dos tarjetas (Facturar a / Datos de pago), la caja de observaciones y el chip del NIF SHALL tener esquinas redondeadas como el prototipo.
- Espaciados: separación visible entre la última fila del resumen y la banda TOTAL, paddings de tarjetas/tablas según prototipo, y pie con «Página X de Y» sin solapes (posición correcta del dígito total).
- Colores: tonos ajustados al prototipo manteniendo la derivación desde el color de acento configurable: nombre de empresa marrón oscuro, etiquetas de datos marrón claro, valores de Datos de pago en tono suave, subdescripciones grises.

## Capabilities

### New Capabilities

- (ninguna)

### Modified Capabilities

- `invoicing`: requisitos modificados dentro de «Exportación a PDF» (cabecera sin solapes con garantía de visibilidad de Serie/Nº/fecha, tipografía Calibri, bordes redondeados, espaciados del resumen y pie sin solape).

## Impact

- `pdf/PdfService.java`: cabecera repetida (`dibujarDatosEmpresa`, `dibujarBloqueFactura`, `margenes`), carga de fuentes, dibujo de tarjetas/chip/cajas (eventos de celda para esquinas redondeadas), `bloqueTotales` y `dibujarPieLegal`.
- Sin cambios de modelo ni servicio. Suite completa `mvn test` debe seguir en verde (se actualizan/añaden aserciones de visibilidad).
