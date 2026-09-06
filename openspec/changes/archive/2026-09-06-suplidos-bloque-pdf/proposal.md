## Why

En el PDF, una línea de suplido se lista entre las demás y la columna «IVA %» la rotula **«Exento»**: `PdfService.tablaLineas(...)` decide esa etiqueta con `l.isExenta()`, que es `ivaPorcentaje == null`, y un suplido también tiene porcentaje nulo. El documento que se entrega al cliente afirma algo falso. Un suplido no es una operación exenta de IVA: no es una operación, es el reintegro de un gasto pagado en nombre y por cuenta del cliente (art. 78.Tres.3º LIVA).

De ahí que tampoco deba figurar entre las líneas que forman la base imponible. El cálculo y el bloque de totales ya lo tratan bien desde el change `desglose-totales-matriz-suplidos`; lo que falta es la presentación en el cuerpo del documento.

## What Changes

- La tabla de líneas del PDF SHALL NOT incluir las líneas marcadas como suplido.
- El PDF SHALL presentar los suplidos en un bloque propio, situado tras la tabla de líneas y antes del bloque de totales, con la descripción y el importe de cada uno.
- El bloque SHALL llevar una nota que deje constancia del régimen: pagados en nombre y por cuenta del cliente y facturados a su nombre, no sujetos a IVA ni a retención.
- El bloque SHALL aparecer solo cuando la factura tenga al menos un suplido.
- Cuando todas las líneas de la factura sean suplidos, la tabla de líneas SHALL omitirse en lugar de imprimirse con solo la cabecera.
- No cambian los importes ni el bloque de totales, que ya muestra su fila `Suplidos`.

## Capabilities

### New Capabilities

Ninguna.

### Modified Capabilities

- `invoicing`: se modifica el requisito «Suplidos» para fijar cómo se presentan en el PDF.

## Impact

- `pdf/PdfService.tablaLineas(...)`: filtrar las líneas de suplido.
- `pdf/PdfService`: método nuevo para el bloque de suplidos y su nota, añadido al documento entre la tabla de líneas y los totales.
- `pdf/PdfServiceTest`: el test `suplidosAparecenEntreRetencionYTotal` sigue siendo válido; se añaden casos para el bloque nuevo. Vigilar `paginacionReflejaPaginasReales`, porque el bloque añade alto.
- Sin cambios en `CalculoService`, esquema, modelos, repositorios ni editor.
