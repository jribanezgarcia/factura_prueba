## ADDED Requirements

### Requirement: Orden del desglose de totales

El bloque de totales de la factura SHALL presentar las líneas en este orden: subtotal, descuento, base imponible, cuotas de IVA, retención y TOTAL. El resumen del editor y el PDF exportado SHALL usar el mismo orden. Las líneas de subtotal y descuento SHALL aparecer solo cuando el descuento global sea mayor que 0; sin descuento el desglose SHALL mostrar directamente la base, las cuotas de IVA, la retención si la hay y el TOTAL.

#### Scenario: Factura con descuento en el PDF
- **WHEN** el usuario exporta el PDF de una factura con base 200,00 €, descuento global del 10 %, IVA 21 % y retención del 15 %
- **THEN** el bloque de totales muestra, en este orden: Subtotal 200,00 €, Descuento 10 % −20,00 €, Base imponible 180,00 €, IVA 21 % 37,80 €, retención 15 % −27,00 € y TOTAL 190,80 €

#### Scenario: Factura con descuento en el editor
- **WHEN** el usuario edita una factura con base 200,00 €, descuento global del 10 %, IVA 21 % y retención del 15 %
- **THEN** el resumen muestra, en este orden: subtotal 200,00 €, descuento −20,00 €, base imponible 180,00 €, IVA 37,80 €, retención −27,00 € y total 190,80 €

#### Scenario: Factura sin descuento
- **WHEN** la factura no tiene descuento global
- **THEN** el desglose no muestra líneas de subtotal ni de descuento, ni en el editor ni en el PDF

#### Scenario: Los importes no cambian
- **WHEN** se presenta el desglose reordenado de cualquier factura
- **THEN** todos los importes son idénticos a los calculados antes del reorden
