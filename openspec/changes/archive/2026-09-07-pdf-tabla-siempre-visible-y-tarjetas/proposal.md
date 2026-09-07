## Why

Al revisar los nueve PDF del change anterior aparecieron tres huecos, y los tres son el mismo defecto: **hoja en blanco donde debería haber tabla**.

En una factura de **solo suplidos** no se imprime la tabla de líneas —el requisito «Suplidos» lo exige expresamente— así que entre las tarjetas y el bloque de suplidos queda media hoja vacía.

En una factura larga cuyo cierre **no cabe en la última página**, la página nueva lleva el cierre al pie y nada más: otra media hoja en blanco. Es exactamente lo que se quitó de en medio al poner el marco de columnas, reaparecido por otra puerta.

Y las **exportaciones de prueba usan una empresa a medio rellenar**: `empresaTexto()` (`PdfServiceTest.java:46`) solo tiene nombre, NIF, actividad y pie legal, así que las cabeceras que se revisan no se parecen a las de una factura real, donde COMERCIAL ALCAZABA tiene además dirección, código postal, población, provincia, email y teléfono.

Aparte, dos cosas del acabado de las tarjetas que se ven en cuanto se mira una factura con logo: se **estiran hasta igualar a la más alta**, de modo que «Datos de pago» con tres filas arrastra centímetro y medio vacío frente a «Facturar a» con siete; y la columna de etiquetas se lleva un tercio del ancho para palabras de una línea, dejando un **pasillo** entre el rótulo y su valor.

## What Changes

- Cuando todas las líneas sean suplidos, la tabla de líneas SHALL imprimirse igualmente, con su cabecera de columnas y sin filas de datos.
- La página a la que salta un cierre que no cabe SHALL llevar también cabecera de columnas y marco hasta el cierre.
- Cada tarjeta SHALL ocupar el alto que necesite, sin estirarse hasta igualar a la otra.
- La columna de etiquetas de las tarjetas SHALL ajustarse a la etiqueta más larga, sin pasillo.
- La empresa de las pruebas SHALL llevar todos sus datos rellenos.
- No cambia ningún importe ni el anclaje del cierre.

## Capabilities

### New Capabilities

Ninguna.

### Modified Capabilities

- `invoicing`: se modifica «Exportación a PDF» —el apartado de tarjetas y el del cierre anclado— y «Suplidos», cuyo texto exige hoy justo lo contrario: «la tabla de líneas SHALL omitirse en lugar de imprimirse con solo la cabecera».

## Impact

- `src/main/java/com/alcazaba/facturacion/pdf/PdfService.java`:
  - `exportar(...)`: la tabla de líneas deja de estar condicionada a `hayOperaciones` (`PdfService.java:123, 146, 185`); se imprime siempre, con o sin filas.
  - La rama de «el cierre no cabe» (`PdfService.java:192-198`) pasa a añadir tabla y marco en la página nueva.
  - `tarjetas(...)` (`PdfService.java:287`) y `tarjetaCliente(...)` (`PdfService.java:313`): alturas independientes y columna de etiquetas ajustada.
  - `tarjetasSinPago(...)` (`PdfService.java:899`) y el `setFixedHeight(altoTarjetas)` de `PdfService.java:141` son justo lo que **no** se puede tocar: ver `design.md - D4`.
- `src/test/java/com/alcazaba/facturacion/pdf/PdfServiceTest.java`:
  - `empresaTexto()` (`PdfServiceTest.java:46`) con todos los campos.
  - `soloSuplidosOmiteLaTablaDeLineas` (`PdfServiceTest.java:425`) se invierte y se renombra.
  - Los tests de número de páginas llevan números escritos a mano y **van a romperse**: al pasar la cabecera de empresa de tres a cinco líneas el área útil se reduce. Hay que recalcularlos, no relajarlos.
- `prototipos/pdf-cabecera-y-tarjetas.html`: maqueta aprobada del apartado de tarjetas.
- No se toca la cabecera de empresa: se decidió dejar su alto como está.
