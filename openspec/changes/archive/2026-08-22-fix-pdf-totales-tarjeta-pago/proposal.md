# Fix PDF: totales limpios, tarjeta de pago clara y paginación

## Why

Revisando un PDF real exportado desde la app se detectaron 3 problemas frente al diseño aprobado (`prototipos/pdf-fix-v2.html`): el bloque de totales muestra filas duplicadas (`Base 21%/IVA 21%` y además `Base total/IVA total`), la cabecera de la tarjeta «Datos de pago» usa fondo marrón igual que «Facturar a» cuando debía ser clara, y cuando los totales no caben al final saltan enteros a una segunda página casi vacía que el pie cuenta.

## What Changes

- Bloque de totales del PDF sin filas repetidas: queda `Base` → `IVA n%` (cuota) → `Descuento n%` solo si existe (fila restando, en rojo suave) → `TOTAL` destacado en color de acento. Con varios tipos de IVA, cada par Base/IVA y un único descuento. Cuadre visible: Base − Descuento + IVA = TOTAL.
- Para pintar ese cuadre, el resumen calculado expondrá también las bases antes de aplicar el descuento global (y su importe), manteniendo intacto el desglose fiscal actual.
- Cabecera de la tarjeta «DATOS DE PAGO» en blanco con borde fino inferior y texto marrón; la tarjeta «FACTURAR A» mantiene su cabecera bicolor aprobada.
- Bloque de totales más compacto y mejor reparto del salto de página (las tablas pueden repartir filas entre páginas en lugar de saltar en bloque); el contador `Página X de Y` sigue reflejando páginas reales.

## Capabilities

### New Capabilities

- (ninguna)

### Modified Capabilities

- `invoicing`: requisitos modificados dentro de «Exportación a PDF» (composición del bloque de totales, cabecera de la tarjeta «Datos de pago», compactación/paginación del final del documento). El cálculo fiscal y el editor no cambian.

## Impact

- `pdf/PdfService.java`: `bloqueTotales`, `tarjetaPago`/`cabeceraTarjeta`, espaciados y comportamiento de división de tablas.
- `model/ResumenFactura.java` + `service/CalculoService.java`: exposición de bases pre-descuento e importe de descuento (con test).
- Sin cambios de datos, API externa ni dependencias. Suite completa `mvn test` debe seguir en verde.
