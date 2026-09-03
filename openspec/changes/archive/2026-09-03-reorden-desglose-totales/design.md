## Context

`PdfService.bloqueTotales(...)` (`PdfService.java:471`) pinta hoy, con descuento: base bruta por grupo + cuota de IVA (calculada sobre la base descontada), y después las líneas de descuento y retención. Base bruta y cuota mezclan dos bases distintas en líneas contiguas. `EditorController.actualizarResumen(...)` (`EditorController.java:823`) muestra base imponible, IVA, retención y total, sin base bruta ni descuento.

`ResumenFactura` ya expone todo lo necesario (`baseBruta`, `importeDescuento`, `baseTotal`, `ivaTotal`, grupos con `base` y `baseBruta`). Ver proposal.md - Why.

## Goals / Non-Goals

**Goals:**

- Que PDF y editor muestren el mismo orden: base bruta → descuento → base imponible → IVA → retención → TOTAL.
- Que las filas de base bruta y descuento solo aparezcan con descuento > 0.

**Non-Goals:**

- No cambian los importes ni `CalculoService`.
- No se recalculan versiones históricas ni cambia el esquema.

## Decisions

### D1. Reestructurar `bloqueTotales`, no reordenar filas sueltas

Con descuento, el bloque pasa a: una fila de subtotal por grupo de IVA (`Subtotal` o `Subtotal N %`; `Subtotal exento` con motivo si exento), una fila única de `Descuento N %`, una fila única de `Base imponible` (total), una fila de cuota por grupo, retención y TOTAL. Sin descuento, el bloque queda como hoy. Alternativa descartada: mover solo la fila de descuento, porque dejaría el subtotal pegado a una cuota calculada sobre otra base.

### D2. Editor con el mismo orden y filas condicionales

Se añaden dos filas al VBox de totales de `Editor.fxml` (subtotal y descuento) encima de la base imponible, con `visible/managed` condicionados a descuento > 0, el mismo patrón que ya usan las filas de retención. La fila de base imponible mantiene su `fx:id` y su texto.

### D3. Etiquetas

PDF: `Subtotal` / `Subtotal N %` por grupo (reutiliza `nombreBaseGrupo`), `Descuento N %`, `Base imponible`, cuotas como hoy, retención como hoy, `TOTAL`. Editor: `Subtotal`, `Descuento N %`, `Base imponible` (existente), `IVA total` (existente), retención (existente), `TOTAL` (existente). La columna `Base` del histórico pasa a `Base imponible`, que es lo que muestra (`base_total` guardada).

## Verificación

- `PdfServiceTest.totalesConDescuentoSeMuestranRestandoYCuadran`: sigue en verde; se amplía asserteando `Base imponible` y el orden relativo de las etiquetas en el texto extraído.
- Suite completa en verde.
- Comprobación manual: factura con descuento y retención en editor y PDF.
