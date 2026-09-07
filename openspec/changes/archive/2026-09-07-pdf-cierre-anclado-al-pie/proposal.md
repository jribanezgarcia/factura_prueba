## Why

El bloque de totales fluye detrás de la tabla de líneas (`PdfService.java:131`), así que su altura en la hoja depende de cuántas líneas tenga la factura: en una de dos líneas queda colgado a media página con medio folio vacío debajo, y en una de veinte casi toca el pie. Vistas seguidas —que es como se archivan y como las ve el cliente— las facturas no cierran nunca igual.

Con el mismo motivo, una factura de varias páginas pierde al cliente a partir de la segunda: el evento de página repite los datos de empresa, el bloque `FACTURA / Serie-Nº / Fecha`, el separador, el pie legal y el `Página X de Y`, pero las dos tarjetas van en el flujo (`PdfService.java:121`), así que solo salen en la primera. Si las hojas se sueltan, no hay forma de saber a quién se factura.

Y el pie legal se dibuja a 7,5 pt, más grande de lo necesario para un texto que casi nadie lee y que en el caso del aviso de protección de datos ocupa muchas líneas, robándole alto a la factura.

## What Changes

- El cierre de la factura —bloque de totales y observaciones— SHALL anclarse al pie de la última página, en lugar de fluir detrás de las líneas.
- La tabla de líneas SHALL completarse con **filas vacías del mismo estilo**, continuando el rayado alterno, hasta alcanzar el cierre. Es el aspecto de talonario: la tabla llega siempre abajo.
- La tabla de suplidos SHALL seguir pegada detrás de las líneas, o sea por debajo del relleno y por encima del bloque de totales.
- La tarjeta `FACTURAR A` SHALL repetirse en la página 2 y siguientes, con **el mismo ancho y el mismo alto que en la primera**, dejando en blanco el hueco de `DATOS DE PAGO`, que solo aparece en la página 1.
- El pie legal SHALL pasar de 7,5 pt a **6,5 pt**.
- No cambia ningún importe, ni el contenido de ninguna tabla, ni el orden de los conceptos.

## Capabilities

### New Capabilities

Ninguna.

### Modified Capabilities

- `invoicing`: se modifica «Exportación a PDF». Su apartado de observaciones y pie exige hoy lo contrario de lo que se quiere («El cierre del documento SHALL mantenerse compacto… para evitar una página que contenga únicamente el bloque de totales»), y su apartado de tarjetas no contempla que se repitan.

## Impact

- `src/main/java/com/alcazaba/facturacion/pdf/PdfService.java`:
  - `exportar(...)` (`PdfService.java:104-138`) se reordena: el cierre se mide antes de añadirlo y se ancla al pie.
  - Nuevos `tablaRelleno(...)` y `filasDeRelleno(...)`, este último puro y testeable sin generar PDF.
  - `tarjetas(...)` sale del flujo y pasa a dibujarse en el evento de página `CabeceraPie`; el margen superior de `margenes(...)` (`PdfService.java:715`) crece con su alto.
  - `dibujarPieLegal(...)` y `margenes(...)` comparten hoy el literal `7.5f` duplicado: se extrae a constante y se baja a 6,5.
- `src/test/java/com/alcazaba/facturacion/pdf/PdfServiceTest.java`: tests del cálculo del relleno y de los casos límite. Pueden romperse los que cuentan páginas, como `paginacionReflejaPaginasReales`, porque la tarjeta repetida consume alto.
- `prototipos/pdf-cierre-anclado-al-pie.html` y `prototipos/pdf-multipagina-cliente-repetido.html` (nuevos): maquetas aprobadas, hoja A4 completa.
- No se tocan `CalculoService`, `ResumenFactura`, el editor ni ningún fichero de tema.
