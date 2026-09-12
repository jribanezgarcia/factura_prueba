## MODIFIED Requirements

### Requirement: Barra de acciones sobre franja propia en Editor, Clientes e Histórico

En el Editor, en Clientes y en el Histórico, los botones de acción SHALL ocupar una fila propia sobre una franja de fondo que se distinga del resto de la tarjeta que la contiene, y esa fila SHALL ser la primera de la tarjeta. Las tres pantallas SHALL usar la misma franja, de modo que su color lo fije cada tema una sola vez.

Los campos de la pantalla —el buscador de Clientes y los filtros del Histórico— SHALL quedar debajo de esa franja, dentro de la misma tarjeta, alineados a la izquierda. Ningún campo SHALL compartir fila con los botones de acción.

Los botones SHALL ir pegados al borde derecho de la franja en las tres pantallas, en el mismo orden en que están hoy, y SHALL conservar su icono, su etiqueta y su acción. El hueco libre de la franja SHALL quedar a su izquierda.

La franja SHALL verse igual en las tres pantallas, de modo que al cambiar de una a otra los iconos no se desplacen: SHALL tener el mismo alto, SHALL arrancar a la misma distancia de la barra de navegación y SHALL medir lo mismo de ancho. Su alto SHALL estar fijado por el estilo y SHALL NOT depender de lo que contenga cada pantalla, aunque SHALL poder crecer si una etiqueta necesita más de una línea.

Las tres tarjetas SHALL declarar el mismo espaciado respecto a la barra de navegación y el mismo relleno interior, de modo que la franja no pueda divergir entre pantallas al cambiar cualquiera de ellas.

Los iconos de las tres barras SHALL ocupar la misma caja y llevar la misma escala, según el requisito «Iconos de tamaño uniforme dentro de una barra».

#### Scenario: Los botones de Clientes van en su propia fila
- **WHEN** el usuario abre Clientes
- **THEN** los botones Nuevo, Editar, Eliminar y Volver aparecen en la primera fila de la tarjeta, sobre la franja de acciones, y el campo de búsqueda queda debajo de ellos

#### Scenario: Los botones del Histórico van encima de los filtros
- **WHEN** el usuario abre el Histórico
- **THEN** los botones de acción aparecen en la primera fila de la tarjeta, sobre la franja de acciones, y los filtros de serie, cliente, fechas, importes y estado quedan debajo

#### Scenario: Las tres pantallas se ven iguales
- **WHEN** el usuario pasa del Editor a Clientes y al Histórico con cualquier tema
- **THEN** en las tres la fila de iconos se ve sobre una franja del mismo color, distinta del fondo de la tarjeta

#### Scenario: Los filtros arrancan a la izquierda
- **WHEN** el usuario abre el Histórico en el tamaño mínimo de ventana
- **THEN** los filtros arrancan pegados al borde izquierdo de la tarjeta, en sus dos filas de siempre, sin quedar centrados ni empujados a la derecha

#### Scenario: Los iconos no se mueven al cambiar de pantalla
- **WHEN** el usuario pasa del Editor a Clientes y de Clientes al Histórico
- **THEN** el último botón de la barra queda en el mismo punto de la pantalla en las tres, y la franja arranca y termina a la misma altura

#### Scenario: Los botones pegados a la derecha en Clientes
- **WHEN** el usuario abre Clientes
- **THEN** los cuatro botones quedan pegados al borde derecho de la franja, con el hueco libre a su izquierda, igual que en el Editor

#### Scenario: Los botones pegados a la derecha en el Histórico
- **WHEN** el usuario abre el Histórico
- **THEN** los botones de acción quedan pegados al borde derecho de la franja, con el hueco libre a su izquierda

#### Scenario: El alto de la franja no lo marca el contenido
- **WHEN** el usuario compara la franja del Editor, que lleva el logo y el título, con la de Clientes, que solo lleva botones
- **THEN** las dos miden exactamente lo mismo de alto

#### Scenario: Los iconos se ven del mismo tamaño en las tres barras
- **WHEN** el usuario mira los iconos del Editor, de Clientes y del Histórico
- **THEN** todos ocupan el mismo espacio y se ven del mismo tamaño, sin que ninguno destaque por ser mayor o menor
