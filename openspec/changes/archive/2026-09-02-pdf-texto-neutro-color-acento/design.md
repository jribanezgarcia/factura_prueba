## Context

`PdfService` usa una paleta de "tinta" fija (`TINTA` `#3A332B`, `GRIS` `#5F5548`, `GRIS_CLARO` `#A2937F`, `VALOR_SUAVE` `#C4BAAC`) para casi todo el texto por defecto del PDF, lo que produce un tinte marrón-arena constante e independiente del color de acento configurable (`color_pdf`, por defecto `#B08D57`). Ver proposal.md - Why para la motivación.

## Goals / Non-Goals

**Goals:**
- Eliminar el tinte marrón-arena de todo el texto por defecto del PDF, pasándolo a negro/gris neutro manteniendo una jerarquía visual (valores en negro, etiquetas/pie en gris neutro).
- Que el bloque `SERIE / Nº` y `FECHA` (rótulo y valor) use el color de acento configurado.

**Non-Goals:**
- No cambiar los tonos derivados del acento que sí son configurables (`c.oscuro`, `c.claro`, `c.clarisimo`, `c.bordeTabla`) ni el color de fondo/acento de las cabeceras de tarjeta.
- No cambiar rojos de anulada/descuento ni el blanco.
- No alterar la preferencia persistida `color_pdf` ni la forma de elegir el color (se sigue eligiendo solo con el `ColorPicker` existente).

## Decisions

### D1. Sustituir la paleta de tinta fija por constantes neutras

Se reemplazan las cuatro constantes de tinta por una paleta neutra única, de modo que cualquier texto no marcado como acento queda en gris/negro neutro sin tinte:

- `TINTA` (valores de tarjeta FACTURAR A, líneas de tabla, observaciones, valores de totales, placeholder) → **negro** `#000000`.
- `GRIS` (datos de empresa de la cabecera, etiquetas de totales, "Página X de Y", etiqueta "Observaciones") → **gris neutro** `#555555`.
- `GRIS_CLARO` (etiquetas de FACTURAR A, "—" de tarjeta vacía) → **gris neutro claro** `#777777`.
- `VALOR_SUAVE` → **gris neutro** `#555555` (mismo rol que GRIS).

Motivo: se elimina cualquier componente de color en R/G/B desigual (neutro = R≈G≈B). Alternativa descartada: derivar el texto del acento como en los rojos; el usuario pidió explícitamente neutro, no tintado.

### D2. SERIE / Nº y FECHA en color de acento

`dibujarRotulo` (usado para los rótulos "SERIE / Nº" y "FECHA") y los valores (número y fecha) pasan a usar un tono del color de acento. Se usará `c.oscuro` (acento oscurecido) cuando el texto deba leerse sobre blanco para garantizar contraste, en vez del acento puro `c.base`.

Motivo: el acento puro `#B08D57` sobre blanco tiene poco contraste para texto; `c.oscuro` (mezcla 35 % negro) deriva del acento (cumple "color de acento") y mantiene legibilidad. Alternativa: usar `c.base` directo; descartada por contraste.

## Risks / Trade-offs

- [Contraste del acento en SERIE/FECHA] → Se usa `c.oscuro` (acento oscurecido) en lugar del acento puro para legibilidad sobre blanco.
- [Tests PDF existentes que asuman colores] → Se revisa `PdfServiceTest`/`CabeceraLayoutTest`; los tests de layout/geometría no dependen de color, los de texto no cambian.

## Migration Plan

Sin cambios de esquema ni datos. La preferencia `color_pdf` sigue siendo hex. Cambio puramente de presentación del PDF; rollback trivial restaurando el valor de las constantes.

## Open Questions

Ninguna.
