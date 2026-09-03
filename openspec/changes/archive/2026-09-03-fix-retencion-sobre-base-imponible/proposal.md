## Why

La retención de IRPF se calcula hoy sobre la **base bruta** de la factura, mientras que el IVA se calcula sobre la **base descontada**. Son dos bases distintas en el mismo documento, y el resultado es una factura que no cuadra consigo misma.

Con base 1.000,00 €, descuento global del 10 %, IVA 21 % y retención 19 %, la aplicación produce:

```
Base bruta                      1.000,00
Descuento 10%                    -100,00
Base imponible                    900,00
IVA 21% (sobre 900)               189,00
Retención 19% (sobre 1.000)      -190,00
TOTAL                             899,00
```

Un lector de la factura ve base 900,00 y retención 190,00: un tipo implícito del 21,1 %, que no corresponde a ningún tipo de retención legal. Cuando el destinatario declare la base de retención y la retención practicada, las dos cifras no encajan con ningún porcentaje.

La base imponible de una factura es la que queda después de los descuentos concedidos en el momento de la operación — por eso el IVA ya la usa correctamente. La retención de IRPF se practica sobre esos mismos rendimientos íntegros, es decir, sobre la misma base imponible.

El comportamiento actual se especificó así por error en el change `2026-08-31-retencion-irpf` y quedó fijado tanto en el requisito «Retención de IRPF» del spec como en el test `CalculoServiceTest.retencionConDescuentoUsaBaseBruta`, que asserta el valor incorrecto. El usuario ha confirmado que fue un error suyo al redactar el requisito.

Efecto secundario del mismo fallo: con `descuento = 100` (valor que `FacturaService.validar` admite) la base descontada queda en 0 y el IVA en 0, pero la retención sigue calculándose sobre la base bruta, así que el **total sale negativo**. Al pasar la retención a la base imponible, el total de ese caso queda en 0,00 y el problema desaparece sin necesidad de una validación aparte.

## What Changes

- La retención de IRPF SHALL calcularse sobre la **base imponible** de la factura (después del descuento global), la misma base sobre la que se calcula el IVA.
- La fórmula del total no cambia: `Total = Base − Descuento + IVA − Retención`.
- Las facturas **sin descuento global no cambian de importe**: base bruta y base imponible coinciden, así que la retención sale igual que hoy.
- Las rectificativas aplican la misma regla, ya que reutilizan el mismo cálculo.
- No se recalculan facturas ya guardadas: el importe de retención está congelado en `factura_version.importe_retencion`.

## Capabilities

### New Capabilities

Ninguna.

### Modified Capabilities

- `invoicing`: se modifican los requisitos «Retención de IRPF» y «Retención en rectificativas» para que la base de cálculo sea la base imponible en lugar de la base bruta.

## Impact

- `service/CalculoService`: una única línea de cálculo (`resumen(...)`), que es el punto por el que pasan el editor, el versionado y el PDF. No hay ningún otro sitio donde se calcule la retención.
- `service/CalculoServiceTest`: el test `retencionConDescuentoUsaBaseBruta` cambia de nombre y de valores esperados; se añade el caso de descuento del 100 %.
- No cambian el esquema de base de datos, los modelos, la interfaz ni el PDF.
- `FacturaServiceTest` y `FacturacionMensualServiceTest` tienen tests de retención con descuento 0, así que sus valores no varían.

### Riesgo conocido: reexportación de facturas antiguas

`PdfService.exportar` (`PdfService.java:106`) **recalcula** los totales llamando a `CalculoService.resumen(...)` en lugar de leer los importes guardados en la versión. Por tanto, tras este cambio, reexportar el PDF de una factura ya emitida **que llevara descuento global y retención a la vez** producirá un total distinto del PDF entregado en su día. Lo mismo si se reabre esa factura y se vuelve a guardar.

Las facturas sin descuento, o sin retención, no se ven afectadas.

Corregir que el PDF use los importes persistidos es un problema independiente y anterior a este change; queda anotado como trabajo aparte y no se aborda aquí para no mezclar dos cambios.
