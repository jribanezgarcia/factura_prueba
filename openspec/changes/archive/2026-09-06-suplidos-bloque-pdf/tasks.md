> Los valores exactos (proporciones, cabeceras, texto de la nota) están en `design.md - D3`.

## 1. Tabla de líneas

- [x] 1.1 En `src/main/java/com/alcazaba/facturacion/pdf/PdfService.java`, hacer que `tablaLineas(...)` recorra solo las líneas con `!l.isEsSuplido()`.
- [x] 1.2 En el armado del documento (`PdfService.java:118`), omitir la tabla de líneas cuando no quede ninguna línea tras el filtrado.

## 2. Bloque de suplidos

- [x] 2.1 Añadir a `PdfService` un método que construya el bloque de suplidos según `design.md - D3`, reutilizando `celdaCabeceraColumna(...)` y `celdaLinea(...)`.
- [x] 2.2 Añadir la nota del régimen debajo del bloque, con el texto literal de `design.md - D3`.
- [x] 2.3 Añadir el bloque al documento entre la tabla de líneas y el bloque de totales, solo si hay al menos una línea de suplido.

## 3. Tests

- [x] 3.1 Test: factura con una línea normal y un suplido. El texto extraído contiene `SUPLIDOS`, la nota y el importe del suplido; la descripción del suplido no aparece en la tabla de líneas; ninguna línea se rotula «Exento» por ser suplido.
- [x] 3.2 Test: factura sin suplidos. El texto no contiene `SUPLIDOS` ni la nota, y la tabla de líneas queda como hoy.
- [x] 3.3 Test: factura de solo suplidos. No se imprime la cabecera de la tabla de líneas.
- [x] 3.4 Comprobar que `suplidosAparecenEntreRetencionYTotal` y `paginacionReflejaPaginasReales` siguen en verde.

## 4. Verificación final

- [x] 4.1 Suite completa en verde con `mvn test`.
- [ ] 4.2 Verificación manual: exportar el PDF de una factura con una línea al 21 %, una exenta y un suplido, con retención; comprobar que el suplido solo aparece en su bloque y en la fila de totales, y que ninguna línea dice «Exento» por ser suplido.
