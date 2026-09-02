## Why

Al exportar varias facturas seleccionadas como «Un único PDF agrupado», la aplicación lanza `ExceptionConverter: Stream Closed` y no genera el archivo. La causa es un conflicto de recursos en `PdfService.concatenar()`: el `FileOutputStream` se cierra en el try-with-resources antes de que `document.close()` pueda hacer flush de `PdfCopy`, que necesita el stream abierto para escribir la tabla de cross-references y el trailer del PDF.

## What Changes

- Corregir el ciclo de vida del `FileOutputStream` en `PdfService.concatenar()` para que se cierre después de `document.close()`, no antes.
- Añadir test de regresión que verifica que dos facturas se fusionan correctamente en un solo PDF.

## Capabilities

### New Capabilities

(ninguna)

### Modified Capabilities

(ninguna — el requisito «Exportación a PDF» ya contempla la exportación agrupada; el fallo es de implementación, no de comportamiento especificado)

## Impact

- `PdfService.concatenar()` (línea 129): único archivo modificado.
- `PdfServiceTest`: test nuevo de regresión.
- No se tocan specs, FXML, controladores ni dependencias.
