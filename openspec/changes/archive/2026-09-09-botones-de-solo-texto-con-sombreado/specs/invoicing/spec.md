## ADDED Requirements

### Requirement: Sombreado uniforme de los botones de solo texto

Los botones que muestran únicamente texto, sin icono, SHALL presentarse sobre un sombreado suave y uniforme, sin borde y sin fondo blanco. El sombreado SHALL ser el mismo en todos los botones de una misma pantalla, salvo el de la acción principal.

El sombreado SHALL ser un gris neutro teñido levemente con el color de acento del tema activo, de modo que se lea como gris y acompañe al tema sin competir con él. Cada tema de apariencia SHALL declarar su propio valor, tanto el sombreado normal como el de la acción principal.

Estos botones SHALL mostrar su texto en negrita.

El botón de acción principal de cada pantalla SHALL distinguirse del resto por dos señales a la vez: un sombreado más intenso y su texto en el color de acento del tema. Los botones de peligro SHALL conservar su color de texto rojo sobre el sombreado normal.

El botón SHALL reaccionar a la interacción: al situar el puntero encima su sombreado SHALL oscurecerse, al mantenerlo pulsado SHALL oscurecerse más, y al recibir el foco de teclado SHALL mostrar un borde con el color de acento del tema activo.

Este requisito SHALL aplicarse a los botones de solo texto de Clientes, Configuración, Copia de seguridad, Versiones, el Editor y la generación de facturas mensuales, incluidos los botones de línea de esta última, que hasta ahora mostraban el gris por defecto de la plataforma. SHALL NOT aplicarse a los botones de la pantalla de arranque ni a los de los diálogos de aviso y confirmación, que la plataforma construye por su cuenta. La maquetación, el comportamiento y las acciones de todos ellos SHALL permanecer sin cambios: la modificación es exclusivamente de apariencia.

#### Scenario: Los botones de una pantalla comparten sombreado
- **WHEN** el usuario abre Clientes, Configuración, Copia de seguridad o Versiones con cualquier tema
- **THEN** todos los botones de esa pantalla se ven sobre el mismo sombreado suave, sin borde ni fondo blanco, y con el texto en negrita

#### Scenario: El sombreado se lee como gris en cada tema
- **WHEN** el usuario cambia entre los temas de apariencia disponibles
- **THEN** el sombreado de los botones cambia con el tema pero sigue leyéndose como un gris neutro, no como un color saturado

#### Scenario: La acción principal se distingue por dos señales
- **WHEN** el usuario mira los botones Nuevo, Guardar, Crear copia, Restaurar o Generar junto a los demás botones de su pantalla
- **THEN** el botón principal muestra un sombreado más intenso y su texto en el color de acento del tema

#### Scenario: Los botones de peligro conservan su rojo
- **WHEN** el usuario mira el botón Eliminar de Clientes o de Configuración
- **THEN** su texto sigue siendo rojo, sobre el mismo sombreado que los demás botones de la pantalla

#### Scenario: El botón reacciona al puntero y al foco
- **WHEN** el usuario sitúa el puntero sobre uno de esos botones, lo mantiene pulsado y después recorre la pantalla con el tabulador
- **THEN** el sombreado se oscurece al pasar por encima, se oscurece más mientras está pulsado, y el botón que recibe el foco muestra un borde con el color de acento

#### Scenario: Los botones de línea de la generación mensual dejan de desentonar
- **WHEN** el usuario abre la pantalla de generación de facturas mensuales
- **THEN** los botones Añadir línea y Eliminar línea se ven igual que Cancelar y Generar, sin el gris por defecto de la plataforma

#### Scenario: Arranque y los diálogos no cambian
- **WHEN** el usuario abre la pantalla de arranque o un diálogo de aviso o de confirmación
- **THEN** sus botones conservan el aspecto que tenían y el flujo de confirmación y cancelación no cambia

## MODIFIED Requirements

### Requirement: Estilo de zona de acciones en tema por defecto

En el tema por defecto (Biblioteca8), la zona de acciones de las pantallas SHALL distinguirse visualmente sin que resalte: las tarjetas superiores del Histórico, de Clientes y del Editor (Nueva factura), que contienen los campos de búsqueda o de factura y los botones de acción, SHALL tener un fondo gris claro `#F6F6F6`.

Los botones de la barra de acciones del Editor y del Histórico SHALL NOT mostrarse con fondo blanco: SHALL ser planos y adoptar el color del contenedor en el que están, según el requisito «Botones de acción con icono identificativo». Guardar SHALL mantener su condición de acción principal mediante el color de acento en su icono y su etiqueta, en lugar de mediante un fondo de acento. Anular SHALL mantener su color de peligro, de modo que un Anular habilitado SHALL NOT confundirse con un botón deshabilitado.

Los botones de tabla del Editor (Añadir línea y Eliminar línea) SHALL mostrarse sobre el sombreado suave de los botones de solo texto, según el requisito «Sombreado uniforme de los botones de solo texto». Que no formen parte de la barra de acciones SHALL NOT hacer que conserven un fondo blanco con recuadro.

Este estilo de tarjetas SHALL aplicarse solo en el tema por defecto (Biblioteca8); el resto de temas no cambian.

#### Scenario: Tarjeta del Histórico con fondo gris claro
- **WHEN** el usuario abre el Histórico con el tema por defecto
- **THEN** la tarjeta que contiene los campos de búsqueda y la fila de botones muestra un fondo gris claro `#F6F6F6`

#### Scenario: Tarjeta de Clientes con fondo gris claro
- **WHEN** el usuario abre Clientes con el tema por defecto
- **THEN** la tarjeta que contiene el campo de búsqueda y la fila de botones muestra el mismo fondo gris claro `#F6F6F6`

#### Scenario: Tarjeta del Editor con fondo gris claro
- **WHEN** el usuario abre el Editor (Nueva factura) con el tema por defecto
- **THEN** la tarjeta superior que contiene la cabecera de la factura muestra el mismo fondo gris claro `#F6F6F6`

#### Scenario: Botones del Editor en blanco y negro
- **WHEN** el usuario abre el Editor con el tema por defecto
- **THEN** los botones de tabla Añadir línea y Eliminar línea ya no se muestran con fondo blanco y texto negro: se ven sobre el sombreado suave, sin recuadro y con el texto en negrita

#### Scenario: La barra de acciones pierde el fondo blanco de sus botones
- **WHEN** el usuario abre el Editor con el tema por defecto
- **THEN** los botones Guardar, Nueva, Exportar PDF, Versiones, Rectificativa, Anular, Restaurar y Volver se ven sin recuadro blanco, con el color de la barra de fondo

#### Scenario: Botones del Editor que conservan su estilo
- **WHEN** el usuario abre el Editor con el tema por defecto
- **THEN** el botón Guardar se lee como acción principal por su color de acento y su negrita, y el botón Anular conserva su texto rojo de peligro

#### Scenario: Anular habilitado no parece deshabilitado
- **WHEN** el usuario mira el botón Anular habilitado junto a un botón deshabilitado con el tema por defecto
- **THEN** el Anular se ve con su rojo a plena intensidad y se distingue a simple vista del botón deshabilitado, que aparece atenuado

#### Scenario: El resto de temas no cambian
- **WHEN** el usuario abre el Histórico, Clientes o el Editor con un tema distinto del por defecto
- **THEN** los colores de esas zonas son los propios de cada tema, sin los cambios del tema por defecto
