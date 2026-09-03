## 1. PDF

- [x] 1.1 Reestructurar `PdfService.bloqueTotales(...)`: con descuento, filas de base bruta por grupo, fila única de descuento, fila única de base imponible total, cuotas por grupo, retención y TOTAL; sin descuento, bloque como hoy. Verificar ampliando `PdfServiceTest.totalesConDescuentoSeMuestranRestandoYCuadran` con `Base imponible` y el orden relativo de las etiquetas.

## 2. Editor

- [x] 2.1 Añadir filas de base bruta y descuento al VBox de totales de `Editor.fxml`, encima de la base imponible, con `fx:id` para etiquetas e importes.
- [x] 2.2 En `EditorController.actualizarResumen(...)`, rellenar las filas nuevas y mostrarlas solo con descuento > 0, manteniendo el orden base bruta → descuento → base imponible → IVA → retención → TOTAL. Verificar abriendo el editor con una factura con descuento.

## 3. Verificación final

- [x] 3.1 Suite completa en verde con `mvn test`.
- [x] 3.2 Verificación manual con el usuario: factura con descuento global y retención; comprobar el resumen del editor y el PDF exportado.
