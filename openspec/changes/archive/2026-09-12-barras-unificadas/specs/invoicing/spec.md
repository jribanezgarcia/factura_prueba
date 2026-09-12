## ADDED Requirements

### Requirement: Barra de acciones sobre franja propia en Editor, Clientes e Histórico

En el Editor, en Clientes y en el Histórico, los botones de acción SHALL ocupar una fila propia sobre una franja de fondo que se distinga del resto de la tarjeta que la contiene, y esa fila SHALL ser la primera de la tarjeta. Las tres pantallas SHALL usar la misma franja, de modo que su color lo fije cada tema una sola vez.

Los campos de la pantalla —el buscador de Clientes y los filtros del Histórico— SHALL quedar debajo de esa franja, dentro de la misma tarjeta, alineados a la izquierda. Ningún campo SHALL compartir fila con los botones de acción.

Los botones SHALL arrancar por la izquierda de la franja, en el mismo orden en que están hoy, y SHALL conservar su icono, su etiqueta y su acción.

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

## MODIFIED Requirements

### Requirement: Estilo de zona de acciones en tema por defecto

En el tema por defecto (Biblioteca8), la zona de acciones de las pantallas SHALL distinguirse visualmente sin que resalte: las tarjetas superiores del Histórico, de Clientes y del Editor (Nueva factura), que contienen los campos de búsqueda o de factura y la franja de acciones, SHALL tener un fondo gris claro `#F6F6F6`. La franja de acciones que va dentro de esas tarjetas SHALL mostrarse en el gris algo más oscuro que ya usa el Editor, según el requisito «Barra de acciones sobre franja propia en Editor, Clientes e Histórico».

Los botones de la barra de acciones del Editor y del Histórico SHALL NOT mostrarse con fondo blanco: SHALL ser planos y adoptar el color del contenedor en el que están, según el requisito «Botones de acción con icono identificativo». Guardar SHALL mantener su condición de acción principal mediante el color de acento en su icono y su etiqueta, en lugar de mediante un fondo de acento. Anular SHALL mostrarse a plena intensidad en el color del tema, igual que los demás botones, de modo que un Anular habilitado SHALL NOT confundirse con un botón deshabilitado, que aparece atenuado.

Los botones de tabla del Editor (Añadir línea y Eliminar línea) SHALL mostrarse sobre el sombreado suave de los botones de solo texto, según el requisito «Sombreado uniforme de los botones de solo texto». Que no formen parte de la barra de acciones SHALL NOT hacer que conserven un fondo blanco con recuadro.

Este estilo de tarjetas SHALL aplicarse solo en el tema por defecto (Biblioteca8); el resto de temas no cambian.

#### Scenario: Tarjeta del Histórico con fondo gris claro
- **WHEN** el usuario abre el Histórico con el tema por defecto
- **THEN** la tarjeta que contiene los filtros muestra un fondo gris claro `#F6F6F6`, y la franja con la fila de botones se ve sobre ella en un gris algo más oscuro

#### Scenario: Tarjeta de Clientes con fondo gris claro
- **WHEN** el usuario abre Clientes con el tema por defecto
- **THEN** la tarjeta que contiene el campo de búsqueda muestra el mismo fondo gris claro `#F6F6F6`, y la franja con la fila de botones se ve sobre ella en un gris algo más oscuro

#### Scenario: Tarjeta del Editor con fondo gris claro
- **WHEN** el usuario abre el Editor (Nueva factura) con el tema por defecto
- **THEN** la tarjeta superior que contiene la cabecera de la factura muestra el mismo fondo gris claro `#F6F6F6`

#### Scenario: Botones del Editor en blanco y negro
- **WHEN** el usuario abre el Editor con el tema por defecto
- **THEN** los botones de tabla Añadir línea y Eliminar línea ya no se muestran con fondo blanco y texto negro: se ven sobre el sombreado suave, sin recuadro y con el texto en peso normal

#### Scenario: La barra de acciones pierde el fondo blanco de sus botones
- **WHEN** el usuario abre el Editor con el tema por defecto
- **THEN** los botones Guardar, Nueva, Exportar, Versiones, Rectificar, Anular, Restaurar y Volver se ven sin recuadro blanco, con el color de la barra de fondo

#### Scenario: Botones del Editor que conservan su estilo
- **WHEN** el usuario abre el Editor con el tema por defecto
- **THEN** el botón Guardar se ve igual que los demás botones de la barra, sin negrita ni distintivo propio, y el botón Anular se ve en el mismo color que los demás

#### Scenario: Anular habilitado no parece deshabilitado
- **WHEN** el usuario mira el botón Anular habilitado junto a un botón deshabilitado con el tema por defecto
- **THEN** el Anular se ve a plena intensidad en el color del tema y se distingue a simple vista del botón deshabilitado, que aparece atenuado

#### Scenario: El resto de temas no cambian
- **WHEN** el usuario abre el Histórico, Clientes o el Editor con un tema distinto del por defecto
- **THEN** los colores de esas zonas son los propios de cada tema, sin los cambios del tema por defecto
