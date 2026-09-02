## Why

El texto por defecto del PDF de la factura sale siempre con un tinte marrón-arena (constantes `TINTA`, `GRIS`, `GRIS_CLARO` fijas) en lugar de un negro o gris neutro, y no depende del tema ni del color de acento configurado. Esto hace que el documento se vea apagado/sepia por defecto.

## What Changes

- En el PDF, el texto por defecto (datos de la tarjeta «FACTURAR A», datos de empresa de la cabecera, líneas de la tabla, observaciones, totales y pie) pasa de los tonos fijos marrón-arena a **negro y gris neutro**, manteniendo jerarquía (valores en negro, etiquetas/rotulos secundarios en gris neutro).
- El bloque **`SERIE / Nº` y `FECHA`** (rótulo y valor) de la cabecera pasa a usar el **color de acento** configurado.
- No se pierden los rojos de anulada/descuento, el blanco, ni los tonos derivados del acento.
- **BREAKING**: No se rompe el formato de la preferencia `color_pdf` (sigue siendo hex del selector); solo cambia el aspecto de los PDF por defecto.

## Capabilities

### New Capabilities
- (ninguna)

### Modified Capabilities
- `invoicing`: cambia el comportamiento del requisito «Exportación a PDF» (colores del texto por defecto neutros y SERIE/Nº–FECHA en color de acento).

## Impact

- `src/main/java/com/alcazaba/facturacion/pdf/PdfService.java`: sustitución de las constantes de tinta fija por colores neutros y uso del acento en SERIE/Nº–FECHA.
- Tests PDF existentes (`CabeceraLayoutTest`, `PdfServiceTest`) revisados; posibles tests de color nuevos.
- Spec `openspec/specs/invoicing/spec.md`: ajuste del requisito «Exportación a PDF».
