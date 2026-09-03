## MODIFIED Requirements

### Requirement: Orden del desglose de totales

El bloque de totales de la factura SHALL presentar las líneas en este orden: subtotal, descuento, base imponible, cuotas de IVA, retención y TOTAL. El resumen del editor y el PDF exportado SHALL usar el mismo orden. Las líneas de subtotal y descuento SHALL aparecer solo cuando el descuento global sea mayor que 0; sin descuento el desglose SHALL mostrar directamente la base imponible, las cuotas de IVA, la retención si la hay y el TOTAL.

Cuando la factura tenga **varios tipos de IVA**, el desglose SHALL mostrar la base imponible de **cada tipo** junto a su cuota, de modo que cada cuota impresa sea comprobable a partir de una base impresa. SHALL NOT mostrarse una única base imponible agregada en lugar de las bases por tipo.

Las etiquetas SHALL ser: con descuento, `Subtotal` (o `Subtotal N %` si hay varios tipos, y `Subtotal exento` con su motivo) para el bloque anterior al descuento, y `Base imponible` (o `Base imponible N %`) para el bloque posterior; sin descuento, `Base imponible` (o `Base imponible N %`, y `Base exenta` con su motivo). La fila de la base imponible SHALL rotularse igual en el editor y en el PDF.

#### Scenario: Factura con descuento en el PDF
- **WHEN** el usuario exporta el PDF de una factura con base 200,00 €, descuento global del 10 %, IVA 21 % y retención del 15 %
- **THEN** el bloque de totales muestra, en este orden: Subtotal 200,00 €, Descuento 10 % −20,00 €, Base imponible 180,00 €, IVA 21 % 37,80 €, retención 15 % −27,00 € y TOTAL 190,80 €

#### Scenario: Factura con varios tipos de IVA y descuento
- **WHEN** el usuario exporta el PDF de una factura con una línea de 1.000,00 € al 21 %, otra de 500,00 € al 10 % y un descuento global del 10 %
- **THEN** el bloque de totales muestra, en este orden: Subtotal 21 % 1.000,00 €, Subtotal 10 % 500,00 €, Descuento 10 % −150,00 €, Base imponible 21 % 900,00 €, IVA 21 % 189,00 €, Base imponible 10 % 450,00 €, IVA 10 % 45,00 € y TOTAL 1.584,00 €
- **AND** cada cuota coincide con aplicar su tipo a la base imponible impresa inmediatamente encima

#### Scenario: Factura con descuento en el editor
- **WHEN** el usuario edita una factura con base 200,00 €, descuento global del 10 %, IVA 21 % y retención del 15 %
- **THEN** el resumen muestra, en este orden: subtotal 200,00 €, descuento −20,00 €, base imponible 180,00 €, IVA 37,80 €, retención −27,00 € y total 190,80 €

#### Scenario: Factura sin descuento
- **WHEN** la factura no tiene descuento global
- **THEN** el desglose no muestra líneas de subtotal ni de descuento, ni en el editor ni en el PDF
- **AND** la fila de la base se rotula «Base imponible», tanto en el editor como en el PDF

#### Scenario: Los importes no cambian
- **WHEN** se presenta el desglose reordenado de cualquier factura
- **THEN** todos los importes son idénticos a los calculados antes del reorden
