## ADDED Requirements

### Requirement: Suplidos

La aplicación SHALL permitir facturar suplidos: gastos pagados por cuenta del cliente que no forman parte de la contraprestación.

Un suplido SHALL introducirse como una línea más de la factura, eligiendo el tipo de IVA «Suplido» en el mismo desplegable que el resto de tipos.

Una línea de suplido SHALL NOT formar parte de la base imponible, SHALL NOT generar cuota de IVA, SHALL NOT entrar en la base sobre la que se calcula la retención y SHALL NOT verse afectada por el descuento global. Su importe SHALL sumarse al total de la factura.

Las líneas de suplido SHALL NOT aparecer en el desglose por tipo de IVA, que solo describe operaciones sujetas.

El total de suplidos SHALL guardarse en la versión de la factura, de modo que reabrir una factura antigua muestre los mismos importes.

#### Scenario: Factura con suplido
- **WHEN** el usuario factura una línea de 1.000,00 € al 21 % y una línea de suplido de 250,00 €, con retención del 15 %
- **THEN** la base imponible es 1.000,00 €, la cuota de IVA 210,00 €, la retención 150,00 € y el total 1.310,00 €
- **AND** el suplido no aparece en el desglose por tipo de IVA

#### Scenario: El descuento global no afecta a los suplidos
- **WHEN** la factura anterior lleva además un descuento global del 10 %
- **THEN** la base imponible pasa a 900,00 € y la cuota a 189,00 €, mientras el suplido sigue siendo 250,00 €

#### Scenario: Factura sin suplidos
- **WHEN** ninguna línea de la factura es un suplido
- **THEN** el desglose no muestra la fila de suplidos, ni en el editor ni en el PDF
- **AND** todos los importes son idénticos a los calculados antes de existir los suplidos

## MODIFIED Requirements

### Requirement: Orden del desglose de totales

El bloque de totales de la factura SHALL presentar las líneas en este orden: subtotal, descuento, base imponible, cuotas de IVA, retención, suplidos y TOTAL. El resumen del editor y el PDF exportado SHALL usar el mismo orden. Las líneas de subtotal y descuento SHALL aparecer solo cuando el descuento global sea mayor que 0; sin descuento el desglose SHALL mostrar directamente la base imponible, las cuotas de IVA, la retención si la hay, los suplidos si los hay y el TOTAL.

Cuando la factura tenga **varios tipos de IVA**, el desglose SHALL mostrar la base imponible de **cada tipo** junto a su cuota, de modo que cada cuota impresa sea comprobable a partir de una base impresa. SHALL NOT mostrarse una única base imponible agregada en lugar de las bases por tipo.

En el **editor**, ese desglose por tipo SHALL presentarse como una matriz con una fila por tipo de IVA y columnas de base imponible y cuota, más una fila de totales. Junto a la matriz SHALL mostrarse la escalera hasta el total, con las filas de subtotal, descuento, base imponible, IVA total, retención, suplidos y TOTAL FACTURA, cada una sujeta a su condición de aparición.

Las etiquetas SHALL ser: con descuento, `Subtotal` (o `Subtotal N %` si hay varios tipos, y `Subtotal exento` con su motivo) para el bloque anterior al descuento, y `Base imponible` (o `Base imponible N %`) para el bloque posterior; sin descuento, `Base imponible` (o `Base imponible N %`, y `Base exenta` con su motivo). La fila de la base imponible SHALL rotularse igual en el editor y en el PDF.

#### Scenario: Desglose por tipo en el editor
- **WHEN** el usuario edita una factura con una línea de 1.000,00 € al 21 %, otra de 500,00 € al 10 % y un descuento global del 10 %
- **THEN** la matriz muestra una fila de 900,00 € con cuota 189,00 €, otra de 450,00 € con cuota 45,00 €, y una fila de totales con 1.350,00 € y 234,00 €
- **AND** cada cuota coincide con aplicar su tipo a la base imponible de su misma fila

#### Scenario: Factura con descuento en el PDF
- **WHEN** el usuario exporta el PDF de una factura con base 200,00 €, descuento global del 10 %, IVA 21 % y retención del 15 %
- **THEN** el bloque de totales muestra, en este orden: Subtotal 200,00 €, Descuento 10 % −20,00 €, Base imponible 180,00 €, IVA 21 % 37,80 €, retención 15 % −27,00 € y TOTAL 190,80 €

#### Scenario: Factura con varios tipos de IVA y descuento
- **WHEN** el usuario exporta el PDF de una factura con una línea de 1.000,00 € al 21 %, otra de 500,00 € al 10 % y un descuento global del 10 %
- **THEN** el bloque de totales muestra, en este orden: Subtotal 21 % 1.000,00 €, Subtotal 10 % 500,00 €, Descuento 10 % −150,00 €, Base imponible 21 % 900,00 €, IVA 21 % 189,00 €, Base imponible 10 % 450,00 €, IVA 10 % 45,00 € y TOTAL 1.584,00 €
- **AND** cada cuota coincide con aplicar su tipo a la base imponible impresa inmediatamente encima

#### Scenario: Factura con descuento en el editor
- **WHEN** el usuario edita una factura con base 200,00 €, descuento global del 10 %, IVA 21 % y retención del 15 %
- **THEN** la escalera muestra, en este orden: subtotal 200,00 €, descuento −20,00 €, base imponible 180,00 €, IVA total 37,80 €, retención −27,00 € y TOTAL FACTURA 190,80 €

#### Scenario: Factura sin descuento
- **WHEN** la factura no tiene descuento global
- **THEN** el desglose no muestra líneas de subtotal ni de descuento, ni en el editor ni en el PDF
- **AND** la fila de la base se rotula «Base imponible», tanto en el editor como en el PDF

#### Scenario: Los importes no cambian
- **WHEN** se presenta el desglose de cualquier factura sin suplidos
- **THEN** todos los importes son idénticos a los calculados antes del cambio

### Requirement: Editor legible sin scroll en facturas cortas

El Editor de facturas SHALL mostrar completa una factura de pocas lineas en el tamaño mínimo de ventana de 1024x768, sin que el usuario tenga que desplazarse. El resumen de totales SHALL permanecer visible en todo momento, con independencia del número de líneas. La tabla de líneas SHALL ser el único elemento que crece con la ventana y SHALL desplazarse internamente cuando las líneas no quepan.

El campo de observaciones SHALL ocupar una línea a todo el ancho por encima del bloque de totales, y SHALL crecer en altura cuando el texto lo requiera, hasta un máximo que no comprometa la visibilidad de los totales.

#### Scenario: Factura corta sin scroll
- **WHEN** el usuario abre una factura nueva con la ventana en 1024x768
- **THEN** la cabecera, la tabla de lineas, las observaciones y el resumen de totales son visibles sin desplazarse

#### Scenario: Pie con matriz y escalera dentro de 1024×768
- **WHEN** el usuario edita a 1024×768 una factura con tres tipos de IVA distintos, descuento, retención y un suplido
- **THEN** la matriz completa, la escalera completa y la línea de observaciones caben en el pie sin scroll

#### Scenario: Totales siempre visibles
- **WHEN** una factura tiene más lineas de las que caben en la tabla
- **THEN** solo se desplaza la tabla, y el desglose de base imponible, IVA, retencion y total sigue visible al pie

#### Scenario: Cabecera repartida en dos bloques
- **WHEN** el usuario abre el Editor
- **THEN** los datos de la factura y los del cliente se muestran en dos bloques contiguos, y los campos de cliente, nombre y direccion disponen del ancho suficiente para su contenido habitual

#### Scenario: La tabla aprovecha el alto disponible
- **WHEN** el usuario amplia o maximiza la ventana del Editor
- **THEN** la tabla de lineas absorbe todo el alto adicional y el resto de zonas conserva su tamaño
