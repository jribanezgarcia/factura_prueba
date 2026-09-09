## REMOVED Requirements

> Los cuatro requisitos siguientes no desaparecen: se **mueven enteros** a la
> capability `pdf-rendering`, con su texto intacto. Aqui se retiran para que no
> queden duplicados en dos capabilities.

### Requirement: Vista previa de la cabecera del PDF

**Reason**: se traslada a la capability `pdf-rendering`.
**Migration**: ver «Vista previa de la cabecera del PDF» en `pdf-rendering`.

### Requirement: Retención en PDF

**Reason**: se traslada a la capability `pdf-rendering`. La regla de calculo de
la retencion se queda en `invoicing`, en el requisito «Retención de IRPF».
**Migration**: ver «Retención en PDF» en `pdf-rendering`.

### Requirement: Exportación a PDF

**Reason**: se traslada a la capability `pdf-rendering`.
**Migration**: ver «Exportación a PDF» en `pdf-rendering`.

### Requirement: Exportación múltiple a PDF desde el histórico

**Reason**: se traslada a la capability `pdf-rendering`.
**Migration**: ver «Exportación múltiple a PDF desde el histórico» en `pdf-rendering`.

> Los dos requisitos siguientes mezclaban negocio y papel. Se retiran con sus
> títulos de hoy y se recrean debajo con títulos nuevos, **sin su parrafo de
> PDF y sin sus escenarios de PDF**, que pasan a `pdf-rendering` como «Orden
> del desglose en el PDF» y «Suplidos en el PDF». No se reescribe ni una frase:
> solo se quita. Los títulos cambian porque el CLI (openspec 1.10.0) impide
> soltar escenarios en un `MODIFIED` y tambien impide `REMOVED` + `ADDED` con el
> mismo nombre; ver `design.md - D7`.

### Requirement: Orden del desglose de totales

**Reason**: se parte: la regla de negocio se recrea como «Desglose de totales por tipo de IVA» y el parrafo de PDF se va a «Orden del desglose en el PDF» en `pdf-rendering`.
**Migration**: ver «Desglose de totales por tipo de IVA» (este spec) y «Orden del desglose en el PDF» (`pdf-rendering`).

### Requirement: Suplidos

**Reason**: se parte: la regla de negocio se recrea como «Suplidos en la facturación» y los parrafos de PDF se van a «Suplidos en el PDF» en `pdf-rendering`.
**Migration**: ver «Suplidos en la facturación» (este spec) y «Suplidos en el PDF» (`pdf-rendering`).

## ADDED Requirements

### Requirement: Desglose de totales por tipo de IVA

> Recrea «Orden del desglose de totales» sin su parrafo de PDF ni su escenario
> de PDF («Factura con descuento en el PDF»), que estan en «Orden del desglose
> en el PDF» de `pdf-rendering`. El título es nuevo porque el primero es una
> regla general que sostiene tambien al requisito del PDF, no solo al editor.

Cuando la factura tenga **varios tipos de IVA**, el desglose SHALL mostrar la base imponible de **cada tipo** junto a su cuota, de modo que cada cuota impresa sea comprobable a partir de una base impresa. SHALL NOT mostrarse una única base imponible agregada en lugar de las bases por tipo. El desglose SHALL incluir además una suma de las bases y una suma de las cuotas.

En el **editor**, ese desglose por tipo SHALL presentarse como una matriz con una fila por tipo de IVA y columnas de base imponible y cuota, más una fila de totales. Junto a la matriz SHALL mostrarse la escalera hasta el total, con las filas de subtotal, descuento, base imponible, IVA total, retención, suplidos y TOTAL FACTURA, cada una sujeta a su condición de aparición. Las filas de subtotal y descuento SHALL aparecer solo cuando el descuento global sea mayor que 0.

El editor y el PDF SHALL partir del mismo cálculo y SHALL mostrar los mismos importes para la misma factura. No están obligados a compartir disposición ni rótulos.

#### Scenario: Desglose por tipo en el editor
- **WHEN** el usuario edita una factura con una línea de 1.000,00 € al 21 %, otra de 500,00 € al 10 % y un descuento global del 10 %
- **THEN** la matriz muestra una fila de 900,00 € con cuota 189,00 €, otra de 450,00 € con cuota 45,00 €, y una fila de totales con 1.350,00 € y 234,00 €
- **AND** cada cuota coincide con aplicar su tipo a la base imponible de su misma fila

#### Scenario: Factura con descuento en el editor
- **WHEN** el usuario edita una factura con base 200,00 €, descuento global del 10 %, IVA 21 % y retención del 15 %
- **THEN** la escalera muestra, en este orden: subtotal 200,00 €, descuento −20,00 €, base imponible 180,00 €, IVA total 37,80 €, retención −27,00 € y TOTAL FACTURA 190,80 €

#### Scenario: Factura sin descuento
- **WHEN** la factura no tiene descuento global
- **THEN** la escalera del editor no muestra las filas de subtotal ni de descuento, y su primera fila visible es la base imponible
- **AND** bajo la rejilla de IVA del PDF no aparece ninguna nota de descuento

#### Scenario: Los importes no cambian
- **WHEN** se presenta el desglose de cualquier factura
- **THEN** todos los importes son idénticos a los calculados antes del cambio

### Requirement: Suplidos en la facturación

> Recrea «Suplidos» sin sus parrafos de PDF ni su escenario de PDF («El
> suplido tiene su propio bloque en el PDF»), que estan en «Suplidos en el PDF»
> de `pdf-rendering`. El título es nuevo porque lo que queda no es solo fiscal:
> incluye como se introduce un suplido y que su total se guarda en la version.

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
- **AND** el PDF no incluye el bloque de suplidos ni su nota
- **AND** todos los importes son idénticos a los calculados antes de existir los suplidos
