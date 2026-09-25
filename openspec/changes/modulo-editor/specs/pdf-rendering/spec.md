## MODIFIED Requirements

### Requirement: Orden del desglose en el PDF

En el **PDF**, el desglose SHALL presentarse como las dos rejillas hermanas descritas en el requisito «Exportación de facturas a PDF»: el desglose de IVA a la izquierda, con una fila por tipo y una última fila `Totales`, y la liquidación a la derecha, terminada en la banda `TOTAL`. El descuento global SHALL resolverse como nota bajo la rejilla izquierda, sin duplicar el bloque de bases. El PDF SHALL NOT imprimir los rótulos `Subtotal`, `Subtotal N %`, `Subtotal exento`, `Base imponible N %` ni `Base exenta`.

#### Scenario: Factura con descuento en el PDF
- **WHEN** el usuario exporta el PDF de una factura con base 200,00, descuento global del 10 %, IVA 21 % y retención del 15 %
- **THEN** la rejilla de IVA muestra la fila `21,00 | 180,00 | 37,80` y la fila `Totales | 180,00 | 37,80`, con la nota del descuento debajo
- **AND** la liquidación muestra `Base imponible 180,00`, `Total IVA repercutido 37,80`, la retención −27,00 y la banda `TOTAL 190,80 €`

#### Scenario: Factura con varios tipos de IVA y descuento
- **WHEN** el usuario exporta el PDF de una factura con una línea de 1.000,00 al 21 %, otra de 500,00 al 10 % y un descuento global del 10 %
- **THEN** la rejilla de IVA muestra una fila por tipo con su base y su cuota, y la fila `Totales` con 1.350,00 y 234,00
- **AND** los importes son los mismos que muestra el editor para esa factura

#### Scenario: El PDF no usa los rótulos de la escalera
- **WHEN** el usuario exporta cualquier factura, con descuento o sin él
- **THEN** el PDF no contiene los textos `Subtotal`, `Subtotal exento`, `Base imponible 21%` ni `Base exenta`
- **AND** el descuento, si lo hay, aparece una sola vez, en la nota bajo la rejilla de IVA

### Requirement: Suplidos en el PDF

En el PDF, las líneas de suplido SHALL NOT aparecer en la tabla de líneas junto a las operaciones facturadas, y SHALL NOT rotularse como exentas: un suplido no es una operación exenta de IVA. SHALL presentarse en un bloque propio, situado tras la tabla de líneas y antes del bloque de totales, con la descripción y el importe de cada suplido. Ese bloque SHALL llevar una nota que deje constancia de que se han pagado en nombre y por cuenta del cliente, facturados a su nombre, y de que no están sujetos a IVA ni a retención. El bloque SHALL aparecer solo cuando la factura tenga al menos un suplido.

Cuando todas las líneas de la factura sean suplidos, la tabla de líneas SHALL imprimirse igualmente con su cabecera de columnas y sin filas de datos, de modo que la hoja no quede vacía entre las tarjetas y el bloque de suplidos.

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
- **THEN** el PDF imprime la tabla de líneas con su cabecera de columnas y sin filas de datos
- **AND** el bloque de suplidos recoge todas las líneas
