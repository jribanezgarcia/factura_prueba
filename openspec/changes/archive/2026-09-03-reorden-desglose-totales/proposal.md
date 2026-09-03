## Why

El desglose de totales mezcla bases: el PDF muestra la base bruta junto a la cuota de IVA calculada sobre la base descontada, y el descuento aparece después del IVA. El lector no puede seguir la resta paso a paso. Además, el editor ni siquiera muestra la base bruta ni el descuento aplicado, así que con descuento global el usuario no ve el cuadre completo.

## What Changes

- El bloque de totales del PDF SHALL presentar las líneas en este orden exacto:
  1. Subtotal (bruto, antes del descuento)
  2. Descuento N % (−importe)
  3. Base imponible
  4. IVA N % (cuota)
  5. Retención nombre N % (−importe)
  6. TOTAL
- El resumen del editor SHALL mostrar las mismas líneas en el mismo orden: subtotal, descuento, base imponible, IVA, retención, TOTAL.
- Las filas de subtotal y descuento SHALL aparecer solo cuando haya descuento global (> 0); sin descuento el desglose queda como hoy (base, IVA, retención si la hay, TOTAL).
- No cambian los importes: solo presentación y orden.

## Capabilities

### New Capabilities

Ninguna.

### Modified Capabilities

- `invoicing`: nuevo requisito de presentación «Orden del desglose de totales» que fija el orden de las líneas en PDF y editor.

## Impact

- `pdf/PdfService.bloqueTotales(...)`: reordenar filas y añadir línea de base imponible total.
- `ui/Editor.fxml` + `ui/EditorController.actualizarResumen(...)`: añadir filas de base bruta y descuento con el mismo orden.
- Sin cambios en `CalculoService` (los datos ya existen: `baseBruta`, `importeDescuento`, `baseTotal`), esquema, modelos ni tests de cálculo.
