## Why

Es el segundo de los tres changes del refactor de `PdfService`. El primero ya sacó las consultas de negocio de la capa de dibujo y está archivado; ahora toca separar *qué* se imprime de *cómo* se dibuja, para que la composición del documento se pueda probar sin generar ni reabrir ningún PDF.

## What Changes

- Nuevo `InvoiceDocument`: record con el documento ya formateado a texto. Sin `BigDecimal`, sin `LocalDate`, sin tipos de OpenPDF. Los rótulos fijos («BASE IMPONIBLE», «TOTAL», «SUPLIDOS», encabezados de columna) son datos del modelo, no literales del código de dibujo. Cada fila condicional, un `Optional`.
- Nuevo `InvoiceDocumentBuilder`: Java puro que construye ese record a partir de `VersionCompleta` + `Empresa` + color. `importePdf` y `porcentajeRejilla` son formato y entran aquí. **La paginación no**: el builder compone contenido y no conoce páginas ni alturas, porque la posición donde acaba la tabla solo se sabe después de dibujarla (`design.md - D1`).
- `PdfService` pasa a dibujar lo que el builder resuelve, sin cambiar su API pública ni una coma del documento. El código OpenPDF sigue viviendo en `PdfService`: aislarlo en `OpenPdfRenderer` es el change 3.
- Refactor puro: ni un importe, ni una coordenada, ni una página distinta. `PdfServiceTest` no se toca.

## Capabilities

### New Capabilities

Ninguna.

### Modified Capabilities

- `pdf-rendering`: requisito nuevo — la composición del documento SHALL ser verificable sin generar un PDF, y los rótulos fijos SHALL formar parte del modelo. Ningún requisito existente cambia de comportamiento.

## Impact

- `src/main/java/com/alcazaba/facturacion/pdf/InvoiceDocument.java` (nuevo) e `InvoiceDocumentBuilder.java` (nuevo), en el mismo paquete y con nombres en inglés, coherentes con `PdfService` y `CalculoService`.
- `src/main/java/com/alcazaba/facturacion/pdf/PdfService.java`: consume el modelo que construye el builder; solo fontanería interna, sin cambios de firma ni de semántica.
- Tests nuevos sobre el builder que no generan PDF: dos tipos de IVA, descuento global, retención, suplidos, solo suplidos, varias páginas y anulada.
- No se toca el editor, ni el histórico, ni la configuración. `OpenPdfRenderer` queda para el change 3.
