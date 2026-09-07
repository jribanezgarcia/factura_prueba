## Why

El bloque de totales del PDF es una escalera de filas sueltas alineada a la derecha (`PdfService.bloqueTotales`, `PdfService.java:515`). Con un solo tipo de IVA se lee bien, pero con varios se descontrola: por cada tipo se imprimen dos filas, y si además hay descuento global el bloque se duplica entero, porque el código recorre los grupos una vez antes del descuento y otra después (`PdfService.java:526-544`). Una factura con 21 %, 10 % y exento con descuento imprime nueve renglones de `Subtotal N %` / `Base imponible N %` / `IVA N %` antes de llegar al total, y ninguno de ellos totaliza nada: no hay ni una suma de bases ni una suma de cuotas.

Las etiquetas tampoco ayudan. `nombreBaseGrupo(...)` (`PdfService.java:588`) tiene que decidir entre `Subtotal`, `Subtotal N %`, `Subtotal exento`, `Base imponible`, `Base imponible N %` y `Base exenta` según haya descuento y según haya uno o varios grupos: seis rótulos posibles para la misma fila.

La factura del programa de referencia resuelve lo mismo con una rejilla: cabecera en banda maciza, una fila por tipo, columnas fijas y el total en caja recuadrada al pie. Se adopta ese lenguaje.

## What Changes

- El bloque de totales del PDF SHALL pasar a **dos rejillas hermanas** a la misma altura, en lugar de la escalera de filas.
- La rejilla izquierda SHALL ser el desglose de IVA, con columnas `Tipo | Base imponible | Cuota IVA`, **una fila por tipo** —las exentas incluidas, con guion en la cuota— y una última fila `Totales` que suma bases y cuotas.
- La rejilla derecha SHALL ser la liquidación, con las filas `Base imponible`, `Total IVA repercutido`, la retención si la hay y los suplidos si los hay, rematada por la banda `TOTAL`.
- El descuento global SHALL dejar de duplicar el bloque: cuando sea mayor que cero, aparece como **nota en cuerpo menor bajo la rejilla izquierda**, con porcentaje, importe descontado y base bruta. Las bases de la rejilla son siempre las netas.
- Los rótulos `Subtotal`, `Subtotal N %`, `Subtotal exento`, `Base imponible N %` y `Base exenta` SHALL desaparecer del PDF, junto con los métodos que los deciden.
- El resumen del editor SHALL quedar **intacto**: matriz de IVA a la izquierda y escalera a la derecha, tal como está hoy.
- Ningún importe cambia. Sólo cambia cómo se presentan.

## Capabilities

### New Capabilities

Ninguna.

### Modified Capabilities

- `invoicing`: se modifica «Exportación a PDF», cuyo apartado de resumen de totales describe hoy la escalera de filas `Base` / `IVA n%` / `Descuento n%`; y se modifica «Orden del desglose de totales», que hoy obliga al editor y al PDF a usar el mismo orden y los mismos rótulos, cosa que deja de ser cierta al cambiar sólo el PDF.

## Impact

- `src/main/java/com/alcazaba/facturacion/pdf/PdfService.java`: `bloqueTotales(...)` se reescribe como rejilla doble; desaparecen `nombreBaseGrupo(...)`, `nombreBaseImponibleGrupo(...)` y `filaDescuento(...)`, y `filaResumen(...)` se sustituye por celdas de rejilla; `bloqueSuplidos(...)` pasa a una variante compacta (cuerpo y cabecera más bajos) sin cambiar contenido ni nota legal. La llamada de `PdfService.java:128` mantiene su firma.
- `src/test/java/com/alcazaba/facturacion/pdf/PdfServiceTest.java`: rompen `exportaDisenoAprobadoConTotalConIvaYTarjetas`, `totalesConDescuentoSeMuestranRestandoYCuadran`, `desgloseConVariosTiposYDescuentoMuestraBasePorTipo`, `retencionApareceComoFilaPropiaEnElPdf` y `suplidosAparecenEntreRetencionYTotal`, todos por buscar los rótulos viejos.
- `prototipos/pdf-totales-rejilla-hermanas.html` (nuevo): maqueta de referencia con los tres casos que hay que reproducir.
- `prototipos/pdf-totales-r1-diez-propuestas.html`, `pdf-totales-r2-variantes.html`, `pdf-totales-r3-rejilla.html` (nuevos): las propuestas descartadas, por si en una revisión posterior se retoma alguna.
- No se tocan `CalculoService`, `ResumenFactura`, `Editor.fxml`, `EditorController` ni ningún fichero de tema.
