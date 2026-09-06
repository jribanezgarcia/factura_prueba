## MODIFIED Requirements

### Requirement: Botones de acción con icono identificativo

Los botones de las barras de acciones del Editor y del Histórico SHALL mostrar un icono identificativo de la acción encima de su etiqueta de texto.

En reposo el botón SHALL NOT pintar fondo ni borde propios: SHALL adoptar el color del contenedor en el que está, de modo que lo único visible sea la silueta del icono y su etiqueta, igual que en los botones de la barra de navegación superior. El botón SHALL conservar su forma y su tamaño, de manera que todos los botones de una misma barra sigan midiendo lo mismo.

Al pasar el puntero por encima, el botón SHALL insinuar su superficie con un velo translúcido neutro, y SHALL oscurecerlo al mantenerlo pulsado. Al recibir el foco de teclado, el botón SHALL dibujar un borde en el color de acento del tema, de modo que la navegación con teclado siga siendo visible pese a la ausencia de borde en reposo.

Los iconos SHALL ser monocromo de un solo color, dibujados como trazado vectorial, de modo que el tema activo pueda recolorearlos. SHALL NOT usarse imágenes de mapa de bits ni iconos multicolor de color fijo.

El color del icono SHALL provenir del tema activo y SHALL mantener contraste legible sobre el fondo de la barra en los siete temas, incluidos los oscuros. En un botón de acción principal el icono y la etiqueta SHALL ir en el color de acento del tema, y la etiqueta SHALL ir en negrita, de modo que la acción principal se distinga de las secundarias sin recurrir a un fondo de color. En un botón secundario el icono SHALL ir en el color de acento del tema; en un botón destructivo, en el color de peligro del tema.

Una misma acción SHALL llevar el mismo icono en todas las pantallas donde aparezca, y dos acciones distintas SHALL NOT compartir icono.

Los botones de una barra de acciones SHALL agruparse por afinidad, y los grupos SHALL separarse visualmente mediante un separador vertical. La agrupación SHALL NOT alterar el significado ni el comportamiento de ningún botón.

Este requisito alcanza únicamente a las barras de acciones del Editor y del Histórico. Los botones de formulario, los de las tablas, los de los diálogos modales y los de las demás pantallas SHALL conservar su aspecto actual mientras no se especifique lo contrario.

#### Scenario: Icono sobre el texto en la barra del Editor
- **WHEN** el usuario abre el Editor
- **THEN** cada botón de la barra de acciones muestra un icono encima de su etiqueta, sin recuadro ni borde alrededor, fundido con el fondo de la barra

#### Scenario: El botón resalta al pasar el ratón
- **WHEN** el usuario pasa el puntero sobre un botón de la barra de acciones
- **THEN** aparece un velo translúcido que delimita el botón, y desaparece al retirar el puntero

#### Scenario: El foco de teclado es visible
- **WHEN** el usuario recorre la barra de acciones con el tabulador
- **THEN** el botón enfocado muestra un borde en el color de acento del tema

#### Scenario: La acción principal se distingue sin fondo de color
- **WHEN** el usuario mira el botón Guardar junto a Nueva y a Exportar PDF
- **THEN** Guardar muestra su icono y su etiqueta en el color de acento del tema y con la etiqueta en negrita, mientras que los otros dos usan el color de texto normal

#### Scenario: El icono cambia de color con el tema
- **WHEN** el usuario cambia el tema desde Configuración
- **THEN** los iconos de los botones de acción adoptan el color del tema nuevo y siguen leyéndose con contraste suficiente, también en los temas oscuros

#### Scenario: Un icono por acción, coherente entre pantallas
- **WHEN** el usuario compara el botón de exportar a PDF del Editor con el del Histórico
- **THEN** ambos muestran el mismo icono

#### Scenario: Grupos separados en la barra
- **WHEN** el usuario mira la barra de acciones del Editor
- **THEN** ve las acciones repartidas en grupos separados por una línea vertical, con las de escritura juntas y las destructivas en su propio grupo

#### Scenario: Las pantallas fuera de alcance no cambian
- **WHEN** el usuario abre Clientes, Configuración o Copia de seguridad
- **THEN** sus botones siguen siendo rectangulares, con fondo y borde propios y solo con texto, exactamente como antes

### Requirement: Estilo de zona de acciones en tema por defecto

En el tema por defecto (Biblioteca8), la zona de acciones de las pantallas SHALL distinguirse visualmente sin que resalte: las tarjetas superiores del Histórico, de Clientes y del Editor (Nueva factura), que contienen los campos de búsqueda o de factura y los botones de acción, SHALL tener un fondo gris claro `#F6F6F6`.

Los botones de la barra de acciones del Editor y del Histórico SHALL NOT mostrarse con fondo blanco: SHALL ser planos y adoptar el color del contenedor en el que están, según el requisito «Botones de acción con icono identificativo». Guardar SHALL mantener su condición de acción principal mediante el color de acento en su icono y su etiqueta, en lugar de mediante un fondo de acento. Anular SHALL mantener su color de peligro, de modo que un Anular habilitado SHALL NOT confundirse con un botón deshabilitado.

Los botones de tabla del Editor (Añadir línea y Eliminar línea) SHALL conservar su fondo blanco y su texto negro, porque no forman parte de la barra de acciones.

Este estilo SHALL aplicarse solo en el tema por defecto (Biblioteca8); el resto de temas no cambian.

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
- **THEN** los botones de tabla Añadir línea y Eliminar línea siguen mostrándose con fondo blanco y texto negro

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
