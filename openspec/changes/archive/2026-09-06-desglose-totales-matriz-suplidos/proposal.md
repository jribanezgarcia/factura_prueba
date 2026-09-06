## Why

El requisito «Orden del desglose de totales» exige que el resumen del editor y el PDF usen el mismo desglose, y prohíbe mostrar una única base imponible agregada cuando la factura tiene varios tipos de IVA. El PDF lo cumple (`PdfService.bloqueTotales`), pero el editor no: `EditorController.actualizarResumen` pinta una sola fila «IVA total» agregada, aunque `CalculoService.resumen` ya calcula el desglose por tipo y lo expone en `ResumenFactura.getGrupos()`. El usuario no puede comprobar en pantalla qué cuota sale de qué base.

Además falta el concepto de **suplido**: gastos pagados por cuenta del cliente que no llevan IVA, no forman parte de la base imponible ni de la base de retención, y se suman al total. Hoy no existen en modelo, esquema ni cálculo, así que hay que facturarlos a mano como líneas exentas, lo que falsea la base de retención.

## What Changes

- El pie del editor SHALL reorganizarse: observaciones en una línea a todo el ancho arriba, y debajo una matriz de IVA a la izquierda junto a una escalera de totales a la derecha.
- La matriz SHALL tener columnas `IVA | Base imponible | Cuota IVA`, una fila por grupo de IVA y una fila `Totales`.
- La escalera SHALL mostrar, en este orden: `Subtotal`, `Descuento N %`, `Base imponible`, `IVA total`, `Retención N %`, `Suplidos` y `TOTAL FACTURA`. Subtotal y descuento solo con descuento > 0; retención solo si la hay; suplidos solo si hay importe.
- Se SHALL añadir el tipo de IVA especial «Suplido». Una línea con ese tipo queda fuera de la base imponible, de las cuotas de IVA y de la base de retención, no se ve afectada por el descuento global, y se suma al total.
- Las líneas de suplido SHALL NOT aparecer en la matriz de IVA.
- El PDF SHALL incluir la fila de suplidos entre la retención y el TOTAL.
- Los importes de las facturas sin suplidos SHALL ser idénticos a los actuales.

## Capabilities

### New Capabilities

Ninguna.

### Modified Capabilities

- `invoicing`: se modifica «Orden del desglose de totales» para fijar la matriz por tipo en el editor y la escalera de la derecha; se añade el requisito «Suplidos».

## Impact

- `src/main/resources/db/migrations/008_suplidos.sql` (nuevo) y `db/Migrations.java`: banderas `es_suplido` en `tipo_iva` y `factura_linea`, columna `total_suplidos` en `factura_version`, semilla del tipo «Suplido».
- `model/TipoIva.java`, `model/LineaFactura.java`, `model/ResumenFactura.java`: campos nuevos.
- `service/CalculoService.java`: separar suplidos del agrupado por tipo y sumarlos al total.
- `repository/IvaRepository.java`, `repository/LineaRepository.java`, `repository/VersionRepository.java`: persistencia de los campos nuevos.
- `ui/Editor.fxml` (bloque `bottom`) y `ui/EditorController.java`: nuevo pie con matriz y escalera.
- `ui/ConfiguracionController.java` y su FXML: marcar un tipo de IVA como suplido.
- `pdf/PdfService.bloqueTotales(...)`: fila de suplidos.
- `themes/base.css`: solo estructura (anchos, paddings, radios). Los 7 temas no se tocan.
- `prototipos/totales-desglose.html` (nuevo): maqueta de referencia del pie.
- Tests que pueden romperse: `EditorTamanoMinimoTest`, `EditorTotalesDescuentoTest`, `StyleClassSeparadorTest`, `PdfServiceTest` y los de `CalculoService`.
