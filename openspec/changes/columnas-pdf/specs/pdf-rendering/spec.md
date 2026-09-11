## ADDED Requirements

### Requirement: Reparto de anchos de la tabla de líneas

En la tabla de líneas del PDF, la columna Descripción SHALL ser, con diferencia, la más ancha, y las columnas Precio, IVA % y Total SHALL medir solo lo necesario para mostrar sus valores habituales en un único renglón. Un importe de hasta cuatro cifras enteras con sus decimales SHALL caber en su celda sin partirse.

Las columnas Cantidad, Precio, IVA % y Total SHALL mostrar su contenido centrado en la celda, igual que sus cabeceras. La Descripción SHALL ir alineada a la izquierda.

El marco de columnas que prolonga la tabla hasta el cierre SHALL usar exactamente el mismo reparto que la tabla, de modo que sus divisiones verticales continúen las de las filas de datos.

La columna de importe del bloque de suplidos SHALL medir lo mismo que la columna Total, y la columna de importe de la rejilla de liquidación SHALL medir lo mismo o prácticamente lo mismo, de modo que las divisiones verticales de importe de las tres piezas queden alineadas en la página. Los importes de suplidos y de liquidación SHALL seguir alineados a la derecha.

El reparto de anchos SHALL NOT hacer que una factura necesite más páginas que antes.

#### Scenario: La descripción aprovecha el ancho
- **WHEN** el usuario exporta una factura con descripciones largas
- **THEN** la columna Descripción es la más ancha de la tabla y las de Precio, IVA % y Total solo ocupan lo que piden sus cifras

#### Scenario: Importes centrados en la tabla de líneas
- **WHEN** el usuario exporta cualquier factura
- **THEN** los valores de Cantidad, Precio, IVA % y Total aparecen centrados en su celda

#### Scenario: Un importe de cuatro cifras no se parte
- **WHEN** el usuario exporta una factura con una línea cuyo total es `3.128,10`
- **THEN** ese importe aparece en un único renglón

#### Scenario: Las divisiones de importe quedan alineadas
- **WHEN** el usuario exporta una factura con suplidos
- **THEN** la división vertical de la columna de importe del bloque de suplidos cae justo debajo de la de la columna Total, y la de la rejilla de liquidación queda alineada con ellas

#### Scenario: El reparto no añade páginas
- **WHEN** el usuario exporta una factura de 20 líneas que cabía en una página
- **THEN** sigue ocupando una sola página
