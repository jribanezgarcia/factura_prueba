## ADDED Requirements

### Requirement: Composición del documento verificable sin PDF

La composición del documento de factura SHALL ser verificable sin generar ningún PDF: a partir de los mismos datos de entrada (versión de factura, empresa y color) SHALL poder obtenerse el contenido ya compuesto —textos, rótulos y filas— como datos, antes de dibujarlo.

Ese contenido SHALL NOT incluir paginación, alturas ni coordenadas: en qué página cae cada cosa depende de cómo se reparta el texto al dibujar, y se decide al componer el PDF, no antes.

Los rótulos fijos del documento («BASE IMPONIBLE», «TOTAL», «SUPLIDOS», encabezados de columna y demás textos que no dependen de los datos) SHALL formar parte del modelo del documento, no del código de dibujo: el mismo rótulo SHALL aparecer idéntico se dibuje como se dibuje.

La extracción SHALL NOT cambiar el documento: el PDF generado a partir del modelo SHALL ser idéntico al que se generaba antes —mismos importes, mismas coordenadas, mismo número de páginas.

#### Scenario: El documento se compone sin generar PDF
- **WHEN** se pide el documento compuesto de una factura con descuento global y retención
- **THEN** el resultado contiene los textos, los rótulos y los importes ya formateados sin haberse generado ningún fichero
- **AND** no contiene número de páginas, alturas ni coordenadas

#### Scenario: Los rótulos viven en el modelo
- **WHEN** se compone el documento de cualquier factura
- **THEN** los rótulos fijos vienen en el documento compuesto y el dibujo los reproduce tal cual

#### Scenario: El PDF generado desde el modelo no cambia
- **WHEN** se genera el PDF a partir del documento compuesto
- **THEN** es idéntico al que se generaba antes del cambio en importes, coordenadas y páginas
