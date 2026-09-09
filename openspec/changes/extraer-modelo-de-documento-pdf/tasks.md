> Decisiones y sus porqués: `design.md`. Referencia de diseño (leer, no copiar):
> `DocumentoFactura` y `ConstructorDocumentoFactura` del proyecto hermano en
> `C:\Users\juan\Desktop\DAM\programacion\proyectos\factualcazaba`.
>
> **El criterio de aceptación no se negocia:** `PdfServiceTest` (33 tests) pasa
> **sin modificar ni una línea**. Si uno falla, el refactor está mal: en este
> change nada puede alterar el documento. No relajes una aserción, no actualices
> un número esperado, no comentes un test. Para y dilo.
>
> **No aislar OpenPDF todavía**: sigue en `PdfService` hasta el change 3. Si al
> extraer ves dibujo mezclado con composición, separa sin mover el dibujo de
> sitio.

## 1. Fotografía de partida

- [ ] 1.1 Pasar `mvn test` y anotar que los 33 tests de `PdfServiceTest` están en verde antes de tocar nada.
- [ ] 1.2 Exportar a una carpeta temporal un PDF de cada caso representativo: factura simple, con dos tipos de IVA, con descuento global, con retención, con suplidos, de varias páginas y anulada. Guardar esos PDF como referencia para comparar al final.
- [ ] 1.3 Anotar el número de páginas de cada uno.

## 2. `InvoiceDocument`: el modelo

- [ ] 2.1 Crear el record en el mismo paquete que `PdfService`, con nombres en inglés. Solo texto compuesto y decisiones: sin `BigDecimal`, sin `LocalDate`, sin tipos de OpenPDF.
- [ ] 2.2 Los rótulos fijos («BASE IMPONIBLE», «TOTAL», «SUPLIDOS», encabezados de columna) son datos del modelo (`String`), no literales del dibujo.
- [ ] 2.3 Cada fila condicional (retención, suplidos, nota de descuento, observaciones, pie) es un `Optional`: si el dibujo necesita un `if` sobre negocio, el modelo está mal.
- [ ] 2.4 El modelo refleja 1:1 los bloques dibujados: tarjetas, tabla, marco, suplidos, totales, observaciones y pie.

## 3. `InvoiceDocumentBuilder`: Java puro

- [ ] 3.1 Crear el builder en el mismo paquete, con nombres en inglés. Construye el record a partir de `VersionCompleta` + `Empresa` + color, usando lo ya resuelto por `CalculoService` y `FacturaService`. Ni un `import` de OpenPDF.
- [ ] 3.2 Mover `importePdf` y `porcentajeRejilla` al builder con salida idéntica: el modelo solo admite texto.
- [ ] 3.3 **La paginación NO entra aquí** (`design.md - D1`). El builder compone contenido: no conoce páginas, ni alturas, ni coordenadas, ni recibe ninguna de las tres como parámetro. El anclaje del cierre y la regla de «si el cierre no cabe, pasa a página nueva» se quedan tal cual están en `exportar(...)`, porque `writer.getVerticalPosition(false)` solo tiene valor **después** de añadir la tabla y depende de cómo iText haya partido las descripciones multilínea.
- [ ] 3.4 No mover el bloque del anclaje ni alterar el orden de los `doc.add(...)` de `exportar(...)`. Es lo único que puede romper el documento en este change.
- [ ] 3.5 Pasar `PdfServiceTest`. Debe seguir en verde aunque el dibujo aún no use el modelo: el builder ya existe y sus tests ya pasan.

## 4. `PdfService` dibuja desde el modelo

- [ ] 4.1 Reescribir el interior de `exportar(...)` para componer con el builder y dibujar el modelo. Firmas y semántica intactas.
- [ ] 4.2 No mover el código OpenPDF de sitio: sigue en `PdfService` hasta el change 3. Solo cambia de dónde salen los datos que dibuja.
- [ ] 4.3 Pasar `PdfServiceTest`. Debe seguir en verde.

## 5. Tests del builder sin PDF

- [ ] 5.1 Test con dos tipos de IVA: desglose por tipo y bandas correctas en el modelo, sin generar PDF.
- [ ] 5.2 Test con descuento global: bases netas y nota de descuento en el modelo.
- [ ] 5.3 Test con retención: fila de retención con el porcentaje del snapshot.
- [ ] 5.4 Test con suplidos y test de solo suplidos: bloque propio y tabla con solo cabecera en el modelo.
- [ ] 5.5 Test de factura larga: el modelo lleva las sesenta filas en orden, con sus textos ya formateados. **Sin afirmar nada sobre páginas**: el modelo no las conoce.
- [ ] 5.6 Test de anulada: el modelo declara que la factura va marcada como anulada.
- [ ] 5.7 Cada test afirma sobre el record, nunca sobre un fichero: ni un `PdfReader` en el fichero de tests del builder.

## 6. Verificación de que nada ha cambiado

- [ ] 6.1 `mvn test` completo en verde, con `PdfServiceTest` sin modificar.
- [ ] 6.2 Regenerar los PDF del punto 1.2 y compararlos con los de referencia: mismo número de páginas y mismo texto extraído.
- [ ] 6.2b Comprobar las tres medidas testigo del anclaje, que deben salir clavadas: `TOTAL.bottom = 123,4 pt`, pie legal acabando en `94,8 pt` y holgura tarjeta→tabla de `23,2 pt` en **todas** las páginas.
- [ ] 6.3 Si algún PDF difiere, **parar**. No sigas al change 3 con una diferencia sin explicar.
- [ ] 6.4 Abrir a ojo el PDF de la factura con suplidos y el de varias páginas, que son los dos casos con más partes móviles.

## 7. La especificación

- [ ] 7.1 Revisar que `specs/pdf-rendering/spec.md` añade «Composición del documento verificable sin PDF» con sus tres escenarios y que ningún requisito existente cambia.
- [ ] 7.2 Comprobar que no queda ningún requisito duplicado y que cada escenario vive en una sola capability.

## 8. Cierre

- [ ] 8.1 Anotar en el change los defectos de maquetación que hayan aparecido y no se hayan tocado, para proponerlos aparte.
- [ ] 8.2 Aplicar y archivar por el flujo habitual.
