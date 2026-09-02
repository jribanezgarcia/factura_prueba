## Context

See proposal.md - Why. `PdfService.exportarAgrupado` genera cada factura en memoria (`byte[]`) y delega la fusión a `concatenar(pdfs, ruta)`. El bug es exclusivamente de orden de cierre de recursos en `concatenar`: el `FileOutputStream` está en try-with-resources, que lo cierra al salir del bloque, mientras `document.close()` (el finally) es lo que hace flush de `PdfCopy` sobre ese mismo stream.

## Goals / Non-Goals

**Goals:**
- Que la exportación agrupada genere el PDF correctamente sin `Stream Closed`.
- Dejar el stream abierto hasta que `Document`/`PdfCopy` termine de escribir.

**Non-Goals:**
- No cambiar el comportamiento de exportación individual ni el orden/paginado del PDF agrupado.
- No tocar specs (no hay cambio de comportamiento a nivel de requisito).
- No modificar controladores ni FXML.

## Decisions

- **Eliminar el try-with-resources del `FileOutputStream`** en `concatenar` y crear el stream inline en el constructor de `PdfCopy`. El `document.close()` del finally cierra `PdfCopy` → `flush()` → stream. Esta es la forma canónica en OpenPDF/iText cuando `PdfCopy` gestiona el flujo.
  - Alternativa descartada: cerrar explícitamente el stream tras `document.close()` con `finally` adicional; más código y equivalente, el `Document.close()` ya libera el `PdfWriter`.

## Risks / Trade-offs

- [Si `document.open()` o `addPage` falla, el stream debe quedarse abierto] → El `try/finally` con `document.close()` en el finally cubre ese camino; `Document.close()` se invoca siempre y libera el writer.
- [`skip_specs` implica que no hay delta de spec] → Corregir el archivo de código y el test; al archivar se confirma que no hay requisitos que sincronizar.