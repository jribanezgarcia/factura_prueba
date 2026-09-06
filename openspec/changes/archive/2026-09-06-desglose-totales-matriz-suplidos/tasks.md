> Los valores exactos (columnas, etiquetas, clases CSS, SQL de la migración) están en `design.md - D5`.

## 1. Esquema

- [x] 1.1 Crear `src/main/resources/db/migrations/008_suplidos.sql` con las tres columnas nuevas y la semilla del tipo «Suplido», según `design.md - D5`.
- [x] 1.2 Registrar el script en `Migrations.SCRIPTS` (`src/main/java/com/alcazaba/facturacion/db/Migrations.java`).

## 2. Modelo

- [x] 2.1 Añadir `esSuplido` a `src/main/java/com/alcazaba/facturacion/model/TipoIva.java` y distinguirlo de «Exento» en `label()`.
- [x] 2.2 Añadir `esSuplido` a `src/main/java/com/alcazaba/facturacion/model/LineaFactura.java`, incluido en `copia()`.
- [x] 2.3 Añadir `totalSuplidos` a `src/main/java/com/alcazaba/facturacion/model/ResumenFactura.java`.

## 3. Cálculo

- [x] 3.1 En `src/main/java/com/alcazaba/facturacion/service/CalculoService.java`, separar las líneas de suplido antes del agrupado por `ClaveIva` y acumularlas en `totalSuplidos`, dejándolas fuera de bases, cuotas, ajuste de céntimos, descuento y base de retención.
- [x] 3.2 Sumar `totalSuplidos` al total y actualizar el javadoc de reglas de la clase.
- [x] 3.3 Tests: suplido solo, suplido con descuento, suplido con retención, y regresión de una factura sin suplidos.

## 4. Persistencia

- [x] 4.1 `repository/IvaRepository.java`: `es_suplido` en `insertar`, `actualizar` y `map`.
- [x] 4.2 `repository/LineaRepository.java`: `es_suplido` en el INSERT y en la lectura.
- [x] 4.3 `repository/VersionRepository.java`: `total_suplidos` en el INSERT y en el mapeo.

## 5. Editor

- [x] 5.1 Reescribir el bloque `bottom` de `src/main/resources/com/alcazaba/facturacion/ui/Editor.fxml`: observaciones a todo el ancho arriba, matriz y escalera debajo, según `design.md - D5`.
- [x] 5.2 En `EditorController.actualizarResumen(...)`, repoblar la matriz desde `r.getGrupos()` con su fila `Totales`, omitiendo los suplidos.
- [x] 5.3 Añadir la fila `Suplidos` a la escalera con el patrón `visible`/`managed` de las filas condicionales existentes, y renombrar la fila final a `TOTAL FACTURA`.
- [x] 5.4 Añadir a `themes/base.css` solo lo estructural del pie nuevo. No tocar los 7 ficheros de tema.
- [x] 5.5 Ajustar `EditorTamanoMinimoTest`, `EditorTotalesDescuentoTest` y `StyleClassSeparadorTest` a la estructura nueva.

## 6. Configuración

- [x] 6.1 Casilla «Es suplido» en el formulario de tipo de IVA de `ui/ConfiguracionController.java` y su FXML, con el porcentaje deshabilitado al marcarla.
- [x] 6.2 Columna correspondiente en `tablaIva`.

## 7. PDF

- [x] 7.1 Fila `Suplidos` en `PdfService.bloqueTotales(...)`, entre la retención y el TOTAL, condicionada a importe > 0.
- [x] 7.2 Ampliar `PdfServiceTest` con el orden relativo de la fila nueva.

## 8. Prototipo

- [x] 8.1 Guardar `prototipos/totales-desglose.html` con la maqueta del pie acordado, siguiendo la convención de `prototipos/02-editor.html`.

## 9. Especificación

- [x] 9.1 Comprobar que el delta de `specs/invoicing/spec.md` refleja lo implementado.

## 10. Verificación manual

- [x] 10.1 Suite completa en verde con `mvn test`.
- [x] 10.2 A 1024×768: factura con líneas al 21 %, al 10 %, exenta y un suplido, descuento del 10 % y retención del 15 %. Pie completo sin scroll, cada cuota cuadra con su base, y el total sale de base + IVA − retención + suplidos.
- [x] 10.3 Quitar el descuento y luego el suplido: desaparecen sus filas de la escalera.
- [x] 10.4 PDF de esa factura con los mismos importes y el mismo orden que el editor.
- [x] 10.5 Factura antigua del histórico con importes intactos tras la migración.
- [x] 10.6 Los 7 temas colorean matriz y escalera sin retoques.
