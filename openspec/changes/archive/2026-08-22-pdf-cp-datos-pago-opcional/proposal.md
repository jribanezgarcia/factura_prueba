# Código postal como fila propia y «Datos de pago» opcional

## Why

Ajustes finales de fidelidad decididos por el usuario sobre el PDF: el código postal debe verse como campo propio (hoy va fundido dentro de Población junto a la provincia) y la tarjeta «Datos de pago» no debe aparecer cuando no hay ningún dato de pago.

## What Changes

- Tarjeta «Facturar a»: cada dato SHALL ir en su propia fila etiquetada — Nombre, NIF, Dirección, Código postal, Población (solo localidad), Provincia y Email; las filas sin dato no aparecen.
- Tarjeta «DATOS DE PAGO»: si forma de pago, vencimiento y realizada por están vacíos, la tarjeta SHALL NOT aparecer; «Facturar A» SHALL mantener su anchura actual con el resto en blanco.

## Capabilities

### New Capabilities

- (ninguna)

### Modified Capabilities

- `invoicing`: requisito modificado dentro de «Exportación a PDF» (filas de «Facturar a», condicional de la tarjeta «Datos de pago»).

## Impact

- `pdf/PdfService.java` (`tarjetaCliente`, `tarjetas`, refactor del listado de filas de pago) y tests. Sin cambios de modelo ni servicio.
