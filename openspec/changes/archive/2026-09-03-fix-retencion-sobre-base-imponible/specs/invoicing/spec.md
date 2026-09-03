## MODIFIED Requirements

### Requirement: Retención de IRPF

La aplicación SHALL permitir aplicar una retención de IRPF a las facturas. La empresa SHALL poder configurar una lista de tipos de retención (nombre y porcentaje), gestionada de forma similar a los tipos de IVA. Cada factura SHALL poder seleccionar un tipo de retención configurado o ninguno. La retención SHALL calcularse sobre la **base imponible** de la factura, es decir, sobre la misma base sobre la que se calcula el IVA, después de aplicar el descuento global. El total de la factura SHALL ser `Base − Descuento + IVA − Retención`. La retención seleccionada y su importe SHALL guardarse en cada versión de la factura. Si no se selecciona ningún tipo de retención, el comportamiento SHALL ser el actual: `Total = Base − Descuento + IVA`. El total de una factura SHALL NOT ser negativo por efecto de la retención.

#### Scenario: Factura con retención del 15%
- **WHEN** el usuario crea una factura con base 1.000,00 €, descuento 0 %, IVA 21 % y selecciona una retención del 15 %
- **THEN** el importe de retención es 150,00 € y el total es 1.060,00 €

#### Scenario: Factura con descuento y retención
- **WHEN** el usuario crea una factura con base 1.000,00 €, descuento global del 10 %, IVA 21 % y retención del 15 %
- **THEN** la base imponible es 900,00 €, el IVA es 189,00 €, la retención es 135,00 € (el 15 % de 900,00) y el total es 954,00 €

#### Scenario: La retención usa la misma base que el IVA
- **WHEN** una factura tiene descuento global y retención
- **THEN** el importe de retención se calcula sobre la base imponible descontada, la misma sobre la que se calcula la cuota de IVA, de modo que el porcentaje de retención que se deduce del resumen coincide con el tipo seleccionado

#### Scenario: Descuento del 100 % con retención
- **WHEN** el usuario crea una factura con base 1.000,00 €, descuento global del 100 % y una retención del 15 %
- **THEN** la base imponible es 0,00 €, el IVA es 0,00 €, la retención es 0,00 € y el total es 0,00 €, nunca un importe negativo

#### Scenario: Factura sin retención
- **WHEN** el usuario crea una factura y no selecciona ningún tipo de retención
- **THEN** el cálculo del total no incluye retención y el comportamiento es el actual

#### Scenario: Seleccionar tipo de retención en el editor
- **WHEN** el usuario abre el editor de una factura nueva
- **THEN** puede elegir entre los tipos de retención configurados para la empresa o dejar la factura sin retención

### Requirement: Retención en rectificativas

Al crear una rectificativa desde una factura, la aplicación SHALL copiar el tipo de retención de la factura original. El usuario SHALL poder modificar o quitar la retención en la rectificativa antes de guardarla. El cálculo del total de la rectificativa SHALL aplicar la misma fórmula de retención sobre la base imponible.

#### Scenario: Rectificativa hereda retención
- **WHEN** el usuario crea una rectificativa desde una factura que tiene una retención del 15 %
- **THEN** la rectificativa se crea con el mismo tipo de retención del 15 %, editable antes de guardar
