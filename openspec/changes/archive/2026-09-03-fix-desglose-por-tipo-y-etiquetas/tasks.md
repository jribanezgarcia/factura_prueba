## 1. Tests que reproducen los fallos

- [x] 1.1 Añadir en `PdfServiceTest` el test `desgloseConVariosTiposYDescuentoMuestraBasePorTipo`: factura con una línea de 1.000,00 al 21 % y otra de 500,00 al 10 %, descuento global del 10 %. Esperar en el texto del PDF `Base imponible 21%` con 900,00, `Base imponible 10%` con 450,00, `IVA 21%` con 189,00, `IVA 10%` con 45,00 y TOTAL 1.584,00, en ese orden.
- [x] 1.2 Ejecutar y confirmar que **falla** con el código actual (hoy solo se imprime una fila «Base imponible» agregada con 1.350,00).
- [x] 1.3 Añadir `retencionApareceComoFilaPropiaEnElPdf`: factura con retención, comprobando que la fila lleva el nombre del tipo con su porcentaje y el importe en negativo. Cubre el requisito «Retención en PDF», hoy sin ningún test.

## 2. Desglose por tipo en el PDF

- [x] 2.1 En `PdfService.bloqueTotales()`, sustituir la fila agregada «Base imponible» y el segundo bucle de cuotas por un único bucle que imprima, por cada grupo, la base imponible del grupo (`g.getBase()`) seguida de su cuota.
- [x] 2.2 Comprobar que con un solo tipo de IVA el resultado es idéntico al actual y que `totalesConDescuentoSeMuestranRestandoYCuadran` sigue pasando sin tocarlo.

## 3. Etiquetas

- [x] 3.1 Hacer que `nombreBaseGrupo(...)` distinga si hay descuento: con descuento el primer bloque es `Subtotal` / `Subtotal N%` / `Subtotal exento (motivo)`; sin descuento es `Base imponible` / `Base imponible N%` / `Base exenta (motivo)`.
- [x] 3.2 Etiquetar las filas del segundo bloque (tras el descuento) como `Base imponible` / `Base imponible N%`.
- [x] 3.3 Actualizar `exportaDisenoAprobadoConTotalConIvaYTarjetas` si assertea sobre esa etiqueta, y añadirle un assert de que el PDF sin descuento dice «Base imponible» y **no** dice «Subtotal».

## 4. Editor

- [x] 4.1 Quitar `total-fila-primera` de la fila estática «Base imponible» en `Editor.fxml`.
- [x] 4.2 En `EditorController.actualizarResumen()`, añadir o quitar `total-fila-primera` de la fila que corresponda según `conDescuento`, junto a los `setVisible`/`setManaged` que ya se hacen ahí, de modo que siempre haya exactamente una fila con la esquina superior redondeada.

## 5. Especificación

- [x] 5.1 MODIFIED «Orden del desglose de totales»: cubrir el caso de varios tipos de IVA (base imponible y cuota por cada tipo) y fijar las etiquetas según haya descuento o no. Añadir escenario de factura con dos tipos y descuento.

## 6. Verificación final

- [x] 6.1 Suite completa en verde con `mvn clean test`.
- [x] 6.2 Rasterizar y mirar dos PDF: uno con 21 % + 10 % + descuento y otro sin descuento.
- [x] 6.3 En el editor, aplicar y quitar un descuento comprobando que solo hay una esquina redondeada en la parte superior del bloque de totales.
