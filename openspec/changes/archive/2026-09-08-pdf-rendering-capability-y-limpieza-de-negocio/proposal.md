## Why

`PdfService.java` tiene **1.295 líneas** y unos sesenta métodos, y dentro conviven cuatro cosas que no se parecen en nada: la paleta y las fuentes, la aritmética de maquetación (márgenes, alto de tarjetas, medición de tablas), la construcción de celdas de OpenPDF, y **consultas de negocio que no le tocan**.

Esas consultas son el problema concreto de este change. `retencionDeVersion(...)` (`PdfService.java:653`) reconstruye a mano el tipo de retención a partir de la versión. `totalConIva(...)` (`PdfService.java:664`) recalcula el total de una línea. `suplidosDe(...)` (`PdfService.java:577`) clasifica qué líneas son suplidos. Son tres reglas de negocio viviendo en la capa de dibujo, con su propia copia de la verdad: si mañana cambia el criterio de suplido o la forma de resolver la retención, hay dos sitios que actualizar y solo uno lo va a recordar.

Es además el fichero que más ha cambiado del proyecto —nueve changes archivados lo tocan— así que cada retoque de maquetación obliga a leer código de negocio de paso.

Aparte, la spec refleja mal el reparto. La capability `invoicing` acumula hoy **cuarenta y siete requisitos**, y entre ellos está «Exportación a PDF», el más largo con diferencia (unas doscientas líneas), más otros cinco que hablan del papel. Buscar una regla de facturación obliga a atravesar la descripción de un documento A4.

Este es el **primero de tres changes encadenados**. Los otros dos extraen el modelo de documento y el renderizador; aquí se prepara el terreno y se ordena la especificación.

## What Changes

- Se crea la capability `pdf-rendering` y se le trasladan los seis requisitos que describen el documento impreso, con su texto intacto.
- Dos requisitos que mezclaban negocio y papel —«Orden del desglose de totales» y «Suplidos»— se parten: la regla de cálculo y la presentación en el editor se quedan en `invoicing`; el párrafo del PDF y sus escenarios se van a la capability nueva.
- La capa de exportación SHALL dejar de derivar datos del modelo de negocio: `retencionDeVersion`, `totalConIva` y `suplidosDe` se trasladan a la capa de servicio.
- **Ningún PDF cambia.** Mismos importes, mismas coordenadas, mismo número de páginas. El criterio de aceptación es que los 33 tests de `PdfServiceTest` pasen **sin tocar ni una línea**.

## Capabilities

### New Capabilities

- `pdf-rendering`: todo lo relativo a la composición y el dibujo del documento PDF —diseño de página, cabecera, tarjetas, tabla de líneas, rejillas de totales, suplidos, observaciones, pie legal, exportación individual y agrupada— más el requisito de separación de responsabilidades que impide que esa capa vuelva a calcular nada.

### Modified Capabilities

- `invoicing`: se retiran seis requisitos, cuatro que se mueven enteros y dos que se recrean con títulos nuevos («Desglose de totales por tipo de IVA», «Suplidos en la facturación») sin sus fragmentos de PDF. No cambia ninguna regla de negocio, ni un solo importe, ni el comportamiento del editor.

## Impact

- `src/main/java/com/alcazaba/facturacion/pdf/PdfService.java`:
  - `retencionDeVersion(...)` (`:653`), `totalConIva(...)` (`:664`) y `suplidosDe(...)` (`:577`) desaparecen de aquí.
- `src/main/java/com/alcazaba/facturacion/service/`: reciben esas tres consultas. `CalculoService` es el destino natural de `totalConIva` y de la clasificación de suplidos; la resolución de la retención encaja en `FacturaService`, junto al resto de lo que ya arma una `VersionCompleta`.
- `src/main/java/com/alcazaba/facturacion/model/`: `ResumenFactura` o `FacturaVersion` pueden tener que exponer lo que hoy el PDF deduce. Ver `design.md - D3`.
- `src/test/java/com/alcazaba/facturacion/pdf/PdfServiceTest.java`: **no se toca**. Es la red que prueba que el refactor no cambia nada.
- Tests nuevos en `service/` para las tres consultas trasladadas, que hoy solo se ejercitan de rebote abriendo un PDF.
- `openspec/specs/invoicing/spec.md` y la nueva `openspec/specs/pdf-rendering/spec.md`.

## Non-Goals

- No se extrae todavía el modelo de documento ni el renderizador: son los changes 2 y 3.
- No se corrige ningún defecto de maquetación, aunque aparezca por el camino. Si sale uno, se anota y se propone aparte.
- No se toca el editor, ni el histórico, ni la configuración.
