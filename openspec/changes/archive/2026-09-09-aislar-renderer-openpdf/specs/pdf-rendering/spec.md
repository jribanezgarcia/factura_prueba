## MODIFIED Requirements

### Requirement: Separación de responsabilidades en la exportación

La capa de exportación a PDF SHALL limitarse a componer y dibujar el documento, y esas dos tareas SHALL vivir en piezas distintas: una compone el documento como datos y otra lo dibuja.

Todo el código que usa la librería de PDF SHALL concentrarse en la capa de dibujo. La fachada de exportación SHALL NOT contener ninguna referencia a esa librería: recibe la factura, delega y devuelve el fichero.

SHALL NOT calcular ningún importe ni derivar dato alguno del modelo de negocio: los importes, el desglose por tipo de IVA, la retención aplicable y la clasificación de una línea como suplido SHALL llegarle ya resueltos desde la capa de servicio.

Toda consulta que hoy resuelva la capa de exportación por su cuenta SHALL trasladarse a la capa de servicio, sin cambiar su resultado. El PDF producido SHALL ser idéntico al anterior al cambio.

#### Scenario: La exportación no deriva la retención
- **WHEN** se exporta una factura con retención
- **THEN** el importe y el porcentaje de la retención provienen de la versión de factura ya resuelta por la capa de servicio
- **AND** la capa de exportación no vuelve a buscar el tipo de retención por su cuenta

#### Scenario: La exportación no calcula el total de una línea
- **WHEN** se exporta una factura con líneas a distintos tipos de IVA
- **THEN** el total con IVA de cada línea llega ya calculado
- **AND** el PDF resultante es idéntico al que se generaba antes del cambio

#### Scenario: La fachada de exportación no conoce la librería de PDF
- **WHEN** se inspecciona la clase que expone la exportación
- **THEN** no contiene ninguna referencia a la librería de PDF
- **AND** la exportación individual y la agrupada siguen funcionando igual que antes

#### Scenario: Componer y dibujar son piezas distintas
- **WHEN** se exporta cualquier factura
- **THEN** el documento se compone primero como datos y se dibuja después
- **AND** el PDF resultante es idéntico al que se generaba antes del cambio
