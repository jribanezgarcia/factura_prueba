## ADDED Requirements

### Requirement: Reparto de anchos de la tabla de líneas

En la tabla de líneas del PDF, la columna Descripción SHALL ser, con diferencia, la más ancha, y las columnas Precio, IVA % y Total SHALL medir solo lo necesario para mostrar sus valores habituales en un único renglón. Un importe de hasta cuatro cifras enteras con sus decimales SHALL caber en su celda sin partirse. La columna Precio SHALL tener además holgura suficiente para un precio unitario de siete cifras enteras (`1.000.000,00`) sin partirse ni quedar pegado a los bordes de su celda.

Las columnas Cantidad, Precio, IVA % y Total SHALL mostrar su contenido centrado en la celda, igual que sus cabeceras. La Descripción SHALL ir alineada a la izquierda.

El marco de columnas que prolonga la tabla hasta el cierre SHALL usar exactamente el mismo reparto que la tabla, de modo que sus divisiones verticales continúen las de las filas de datos.

La columna de importe del bloque de suplidos y la columna de importe de la rejilla de liquidación SHALL medir exactamente lo mismo que la columna Total, de modo que las divisiones verticales de importe de las tres piezas queden alineadas en la página. Los importes de suplidos y de liquidación SHALL seguir alineados a la derecha.

Las dos cajas del bloque de totales SHALL encajar en la rejilla de la tabla de líneas. La rejilla de liquidación SHALL ocupar exactamente el ancho de las columnas Precio, IVA % y Total, con su borde izquierdo sobre la división de Precio y su borde derecho sobre el de la tabla. La rejilla de desglose de IVA SHALL arrancar en el borde izquierdo de la tabla, SHALL tener su columna de tipo del mismo ancho que la columna Cantidad y SHALL terminar poco antes de la división de Precio, dejando entre las dos cajas solo un blanco mínimo. Ninguna de las dos cajas SHALL quedar metida respecto a los bordes de la tabla.

El importe de la banda `TOTAL`, con su símbolo de moneda, SHALL mostrarse en un único renglón para cualquier total de hasta siete cifras enteras (`9.999.999,99 €`), sin reducir su tamaño de letra.

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
- **THEN** la división vertical de la columna de importe del bloque de suplidos y la de la rejilla de liquidación caen exactamente debajo de la de la columna Total

#### Scenario: Las cajas de totales encajan en la rejilla
- **WHEN** el usuario exporta cualquier factura
- **THEN** el borde izquierdo de la liquidación cae sobre la división de Precio, la división entre tipo y base del desglose cae sobre la de Cantidad, y las dos cajas llegan a los bordes de la tabla con solo un blanco mínimo entre ellas

#### Scenario: El total de siete cifras cabe en la banda
- **WHEN** el usuario exporta una factura cuyo total es `1.063.505,15 €`
- **THEN** la banda `TOTAL` muestra la cifra y el símbolo en un único renglón, al mismo tamaño de letra de siempre

#### Scenario: El reparto no añade páginas
- **WHEN** el usuario exporta una factura de 20 líneas que cabía en una página
- **THEN** sigue ocupando una sola página
