## Why

El change `reorden-desglose-totales` puso el desglose en el orden correcto y resolvió el caso de un solo tipo de IVA, pero dejó tres cosas abiertas.

### 1. Con varios tipos de IVA y descuento, el desglose no permite verificar las cuotas

`PdfService.bloqueTotales()` imprime, en la rama con descuento, una única fila «Base imponible» con el total agregado (`r.getBaseTotal()`) y a continuación las cuotas de cada tipo. Las bases imponibles **por tipo** no aparecen:

```
Subtotal 21%          1.000,00
Subtotal 10%            500,00
Descuento 10%          -150,00
Base imponible        1.350,00
IVA 21%                 189,00   ← el 21 % de 900, pero 900 no está en el documento
IVA 10%                  45,00   ← el 10 % de 450, pero 450 no está en el documento
```

Ninguna cuota se puede comprobar a partir de lo impreso. Esto incumple un requisito que **ya existe** en el spec: «En la exportación a PDF, el resumen de la factura SHALL desglosar cada tipo de IVA por separado (base y cuota)» (requisito «IVA»).

El dato ya está calculado: `ResumenFactura.IvaGrupo.getBase()` es la base descontada de cada grupo, con el ajuste de céntimos que garantiza que la suma cuadra con `getBaseTotal()`. Solo falta imprimirla.

### 2. Sin descuento, la fila de la base ha perdido su nombre fiscal

`nombreBaseGrupo()` devuelve ahora «Subtotal» siempre, sin distinguir si hay descuento. En una factura sin descuento —el caso más común— esa fila **es** la base imponible, y tras el cambio el PDF no dice «Base imponible» en ningún sitio; antes decía «Base». «Subtotal» no es el término fiscal.

Además el editor y el PDF se contradicen en ese caso: el editor oculta la fila de subtotal y rotula la base como «Base imponible», mientras que el PDF la rotula «Subtotal».

### 3. Esquina redondeada en mitad del bloque de totales del editor

`Editor.fxml` marca con `total-fila-primera` tanto la nueva fila de subtotal como la fila «Base imponible» que ya existía. Como `.total-fila` tiene fondo propio en los temas y `.totales-compacta .total-fila-primera` aplica `-fx-background-radius: 9px 9px 0 0`, al activar un descuento aparece una esquina redondeada a media altura del bloque.

### Y no hay red de seguridad

`PdfServiceTest` no tiene ningún test con varios tipos de IVA ni con retención. El cambio de etiqueta del punto 2 pasó desapercibido precisamente porque el test del caso sin descuento no comprueba esa fila.

## What Changes

- Con descuento, el PDF SHALL imprimir la **base imponible de cada tipo** seguida de su cuota, en lugar de una única base agregada. Con un solo tipo el resultado es idéntico al actual.
- Sin descuento, la fila de la base SHALL rotularse «Base imponible» (o «Base imponible N %» si hay varios tipos), no «Subtotal». La etiqueta «Subtotal» queda reservada al bloque previo al descuento.
- El editor deja de aplicar `total-fila-primera` a dos filas a la vez.
- Se añaden tests de PDF para el desglose con varios tipos de IVA y para la fila de retención.
- No cambia ningún importe: solo qué se imprime y cómo se rotula.

## Capabilities

### New Capabilities

Ninguna.

### Modified Capabilities

- `invoicing`: se modifica el requisito «Orden del desglose de totales» para cubrir el caso de varios tipos de IVA y fijar las etiquetas según haya descuento o no.

## Impact

- `pdf/PdfService`: `bloqueTotales()` y `nombreBaseGrupo()`.
- `ui/Editor.fxml`: una clase CSS de más en una fila.
- `pdf/PdfServiceTest`: dos tests nuevos.
- No se toca `CalculoService`, ni el esquema, ni los modelos, ni la persistencia. Los importes calculados y guardados son exactamente los mismos.
