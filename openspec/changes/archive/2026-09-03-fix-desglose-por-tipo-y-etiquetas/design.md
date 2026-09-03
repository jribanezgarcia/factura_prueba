## Context

`PdfService.bloqueTotales()` tiene hoy dos ramas según `conDescuento`. Sin descuento, un bucle imprime por grupo la base y su cuota. Con descuento, el mismo bucle imprime solo las bases brutas, luego la fila de descuento, luego **una** fila «Base imponible» agregada, y por último un segundo bucle con las cuotas. Ver proposal.md - Why.

## Goals / Non-Goals

**Goals:**

- Que cada cuota impresa se pueda comprobar contra una base impresa, también con varios tipos de IVA y descuento.
- Que la fila de la base imponible se llame «Base imponible» cuando eso es lo que es.
- Que el editor y el PDF usen el mismo vocabulario.
- Que el caso de un solo tipo de IVA salga byte a byte igual que hoy.

**Non-Goals:**

- No se toca ningún cálculo. `CalculoService` no se modifica.
- No se cambia el orden general del desglose fijado por `reorden-desglose-totales`.
- No se aborda que `PdfService` recalcule los totales en vez de leer los guardados (trabajo aparte).
- No se cambia el bloque de totales del editor más allá de la clase CSS sobrante.

## Decisions

### D1. Un solo bucle de pares base+cuota después del descuento

Se descarta mantener la fila agregada «Base imponible» junto a las bases por tipo: sería redundante con un solo grupo y ruidosa con varios.

La rama con descuento queda: bucle de subtotales por tipo → fila de descuento → bucle de pares `Base imponible [N %]` + `IVA N %`. Con un único grupo la etiqueta va sin porcentaje, así que el desglose impreso es exactamente el de hoy y el test `totalesConDescuentoSeMuestranRestandoYCuadran` debe seguir pasando sin tocarlo.

Con varios grupos queda así:

```
Subtotal 21%          1.000,00
Subtotal 10%            500,00
Descuento 10%          -150,00
Base imponible 21%      900,00
IVA 21%                 189,00
Base imponible 10%      450,00
IVA 10%                  45,00
TOTAL                 1.584,00
```

Cada cuota es comprobable contra la base que tiene justo encima.

### D2. La etiqueta depende de si hay descuento

`nombreBaseGrupo(g, unSoloGrupo)` pasa a recibir también si hay descuento, o se parte en dos métodos:

| Caso | Con descuento | Sin descuento |
|---|---|---|
| Un tipo | `Subtotal` → `Base imponible` | `Base imponible` |
| Varios tipos | `Subtotal 21%` → `Base imponible 21%` | `Base imponible 21%` |
| Exento | `Subtotal exento (motivo)` → `IVA exento` | `Base exenta (motivo)` |

Sin descuento no hay bloque de subtotales, así que la única fila de base de cada grupo se rotula directamente como base imponible. Esto devuelve al caso más común el término fiscal que tenía antes de `reorden-desglose-totales`, y alinea el PDF con la etiqueta fija del editor.

### D3. La clase CSS sobrante

`total-fila-primera` existe para redondear la esquina superior del bloque (`base.css:211`, `-fx-background-radius: 9px 9px 0 0`), y solo la primera fila visible debe llevarla. Con descuento la primera es `filaBaseBruta`; sin descuento es la de «Base imponible», pero esa fila es estática en el FXML.

La opción simple es quitar la clase de la fila «Base imponible» en el FXML y añadirla/quitarla desde `actualizarResumen()` según `conDescuento`, junto al `setVisible`/`setManaged` que ya se hace ahí. Así siempre hay exactamente una fila con la esquina redondeada.

### D4. Los tests que faltan

Dos tests nuevos en `PdfServiceTest`, ambos sobre el texto extraído:

- **Varios tipos con descuento**: factura con 1.000,00 al 21 % y 500,00 al 10 %, descuento 10 %. Comprobar que aparecen `Base imponible 21%` con 900,00 y `Base imponible 10%` con 450,00, que las cuotas son 189,00 y 45,00, y que el orden es subtotales → descuento → pares base/cuota → TOTAL.
- **Retención**: factura con retención, comprobando que la fila aparece con su nombre y su importe en negativo. Hoy el requisito «Retención en PDF» no tiene ningún test.

Comprobar que el test nuevo de varios tipos **falla** con el código actual antes de aplicar el cambio.

## Verificación

- Suite completa: los tests existentes de PDF no deben cambiar, en particular `totalesConDescuentoSeMuestranRestandoYCuadran` (un solo tipo) y `exportaDisenoAprobadoConTotalConIvaYTarjetas` (sin descuento, que pasará a decir «Base imponible»).
- Rasterizar dos PDF y mirarlos: uno con 21 % + 10 % + descuento, otro sin descuento.
- En el editor, aplicar y quitar un descuento comprobando que solo hay una esquina redondeada arriba del bloque.
