## MODIFIED Requirements

### Requirement: Suplidos

La aplicación SHALL permitir facturar suplidos: gastos pagados por cuenta del cliente que no forman parte de la contraprestación.

Un suplido SHALL introducirse como una línea más de la factura, eligiendo el tipo de IVA «Suplido» en el mismo desplegable que el resto de tipos.

Una línea de suplido SHALL NOT formar parte de la base imponible, SHALL NOT generar cuota de IVA, SHALL NOT entrar en la base sobre la que se calcula la retención y SHALL NOT verse afectada por el descuento global. Su importe SHALL sumarse al total de la factura.

Las líneas de suplido SHALL NOT aparecer en el desglose por tipo de IVA, que solo describe operaciones sujetas.

En el PDF, las líneas de suplido SHALL NOT aparecer en la tabla de líneas junto a las operaciones facturadas, y SHALL NOT rotularse como exentas: un suplido no es una operación exenta de IVA. SHALL presentarse en un bloque propio, situado tras la tabla de líneas y antes del bloque de totales, con la descripción y el importe de cada suplido. Ese bloque SHALL llevar una nota que deje constancia de que se han pagado en nombre y por cuenta del cliente, facturados a su nombre, y de que no están sujetos a IVA ni a retención. El bloque SHALL aparecer solo cuando la factura tenga al menos un suplido.

Cuando todas las líneas de la factura sean suplidos, la tabla de líneas SHALL omitirse en lugar de imprimirse con solo la cabecera.

El total de suplidos SHALL guardarse en la versión de la factura, de modo que reabrir una factura antigua muestre los mismos importes.

#### Scenario: Factura con suplido
- **WHEN** el usuario factura una línea de 1.000,00 € al 21 % y una línea de suplido de 250,00 €, con retención del 15 %
- **THEN** la base imponible es 1.000,00 €, la cuota de IVA 210,00 €, la retención 150,00 € y el total 1.310,00 €
- **AND** el suplido no aparece en el desglose por tipo de IVA

#### Scenario: El descuento global no afecta a los suplidos
- **WHEN** la factura anterior lleva además un descuento global del 10 %
- **THEN** la base imponible pasa a 900,00 € y la cuota a 189,00 €, mientras el suplido sigue siendo 250,00 €

#### Scenario: El suplido tiene su propio bloque en el PDF
- **WHEN** el usuario exporta el PDF de una factura con una línea al 21 % y un suplido de 250,00 €
- **THEN** la tabla de líneas muestra solo la línea al 21 %
- **AND** un bloque titulado «SUPLIDOS» muestra la descripción del suplido y su importe de 250,00 €
- **AND** ese bloque lleva la nota de que se han pagado en nombre y por cuenta del cliente y no están sujetos a IVA ni a retención
- **AND** la fila «Suplidos» del bloque de totales sigue mostrando 250,00 €

#### Scenario: Un suplido nunca se rotula como exento
- **WHEN** el usuario exporta el PDF de una factura que contiene un suplido
- **THEN** ninguna fila del documento describe ese suplido como una operación exenta de IVA

#### Scenario: Factura de solo suplidos
- **WHEN** todas las líneas de la factura son suplidos
- **THEN** el PDF no imprime la tabla de líneas, ni siquiera su cabecera
- **AND** el bloque de suplidos recoge todas las líneas

#### Scenario: Factura sin suplidos
- **WHEN** ninguna línea de la factura es un suplido
- **THEN** el desglose no muestra la fila de suplidos, ni en el editor ni en el PDF
- **AND** el PDF no incluye el bloque de suplidos ni su nota
- **AND** todos los importes son idénticos a los calculados antes de existir los suplidos
