## MODIFIED Requirements

### Requirement: Botones de acción con icono identificativo

Los botones de las barras de acciones del Editor, del Histórico y de Clientes SHALL mostrar un icono identificativo de la acción encima de su etiqueta de texto.

En reposo el botón SHALL NOT pintar fondo ni borde propios: SHALL adoptar el color del contenedor en el que está, de modo que lo único visible sea la silueta del icono y su etiqueta, igual que en los botones de la barra de navegación superior. El botón SHALL conservar su forma y su tamaño, de manera que todos los botones de una misma barra sigan midiendo lo mismo.

Al pasar el puntero por encima, el botón SHALL insinuar su superficie con un velo translúcido neutro, y SHALL oscurecerlo al mantenerlo pulsado. Al recibir el foco de teclado, el botón SHALL dibujar un borde en el color de acento del tema, de modo que la navegación con teclado siga siendo visible pese a la ausencia de borde en reposo.

Los iconos SHALL ser monocromo de un solo color, dibujados como trazado vectorial, de modo que el tema activo pueda recolorearlos. SHALL NOT usarse imágenes de mapa de bits ni iconos multicolor de color fijo.

El color del icono y el de la etiqueta SHALL provenir del tema activo y SHALL mantener contraste legible sobre el fondo de la barra en los siete temas, incluidos los oscuros. Tanto el icono como la etiqueta SHALL ir en el color de acento del tema, también en los botones de acciones destructivas: ningún botón de barra SHALL mostrarse en el color de peligro. El carácter destructivo de una acción lo comunica su icono, no su color.

Ningún botón SHALL destacarse como acción principal: todos los de una misma barra SHALL presentarse con el mismo peso, el mismo color y el mismo tratamiento, y el orden de los botones SHALL ser la única jerarquía. Ninguna etiqueta SHALL mostrarse en negrita.

Una misma acción SHALL llevar el mismo icono en todas las pantallas donde aparezca, y dos acciones distintas SHALL NOT compartir icono. Cuando una acción necesite un icono que no exista en la librería de origen, SHALL componerse a partir de trazados ya presentes en la aplicación, y la composición SHALL quedar documentada de forma que pueda reproducirse.

Los botones de una barra de acciones SHALL agruparse por afinidad, y los grupos SHALL separarse visualmente mediante un separador vertical. La agrupación SHALL NOT alterar el significado ni el comportamiento de ningún botón.

Este requisito alcanza únicamente a las barras de acciones del Editor, del Histórico y de Clientes. Los botones de formulario, los de las tablas, los de los diálogos modales y los de las demás pantallas SHALL conservar su aspecto actual mientras no se especifique lo contrario.

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
- **WHEN** el usuario mira el botón Guardar junto a Nueva y a Exportar
- **THEN** los tres muestran su icono y su etiqueta en el color de acento del tema, con el mismo peso, y ninguno se destaca sobre los demás

#### Scenario: El icono cambia de color con el tema
- **WHEN** el usuario cambia el tema desde Configuración
- **THEN** los iconos de los botones de acción adoptan el color del tema nuevo y siguen leyéndose con contraste suficiente, también en los temas oscuros

#### Scenario: Un icono por acción, coherente entre pantallas
- **WHEN** el usuario compara el botón de exportar del Editor con el del Histórico, y los botones de Eliminar y Volver del Histórico con los de Clientes
- **THEN** cada acción muestra el mismo icono en todas las pantallas donde aparece

#### Scenario: Grupos separados en la barra
- **WHEN** el usuario mira la barra de acciones del Editor
- **THEN** ve las acciones repartidas en grupos separados por una línea vertical, con las de escritura juntas y las destructivas en su propio grupo

#### Scenario: Clientes usa la misma barra que sus pantallas hermanas
- **WHEN** el usuario abre Clientes
- **THEN** los botones Nuevo, Editar, Eliminar y Volver muestran icono sobre la etiqueta, en la misma fila que el campo de búsqueda y alineados a la derecha, agrupados por afinidad y separados por líneas verticales

#### Scenario: Las pantallas fuera de alcance no cambian
- **WHEN** el usuario abre Configuración, Copia de seguridad o Versiones
- **THEN** sus botones siguen mostrando solo texto, sobre el sombreado suave de los botones de solo texto

### Requirement: Sombreado uniforme de los botones de solo texto

Los botones que muestran únicamente texto, sin icono, SHALL presentarse sobre un sombreado suave y uniforme, sin borde y sin fondo blanco. El sombreado SHALL ser el mismo en todos los botones de una misma pantalla.

El sombreado SHALL ser un gris neutro teñido levemente con el color de acento del tema activo, de modo que se lea como gris y acompañe al tema sin competir con él. Cada tema de apariencia SHALL declarar su propio valor de sombreado.

Estos botones SHALL mostrar su texto en peso normal, sin negrita.

Ningún botón SHALL destacarse como acción principal: todos los de una misma pantalla SHALL compartir el mismo sombreado y el mismo color de texto. Tampoco los botones de acciones destructivas SHALL mostrarse en color de peligro: van en el mismo color que los demás.

El botón SHALL reaccionar a la interacción: al situar el puntero encima su sombreado SHALL oscurecerse, al mantenerlo pulsado SHALL oscurecerse más, y al recibir el foco de teclado SHALL mostrar un borde con el color de acento del tema activo.

Este requisito SHALL aplicarse a los botones de solo texto de Configuración, Copia de seguridad, Versiones, el Editor y la generación de facturas mensuales, incluidos los botones de línea de esta última, que hasta ahora mostraban el gris por defecto de la plataforma. SHALL NOT aplicarse a los botones de la barra de acciones de Clientes, que muestran icono, ni a los de la pantalla de arranque ni a los de los diálogos de aviso y confirmación, que la plataforma construye por su cuenta. La maquetación, el comportamiento y las acciones de todos ellos SHALL permanecer sin cambios: la modificación es exclusivamente de apariencia.

#### Scenario: Los botones de una pantalla comparten sombreado
- **WHEN** el usuario abre Configuración, Copia de seguridad o Versiones con cualquier tema
- **THEN** todos los botones de esa pantalla se ven sobre el mismo sombreado suave, sin borde ni fondo blanco, y con el texto en peso normal

#### Scenario: El sombreado se lee como gris en cada tema
- **WHEN** el usuario cambia entre los temas de apariencia disponibles
- **THEN** el sombreado de los botones cambia con el tema pero sigue leyéndose como un gris neutro, no como un color saturado

#### Scenario: La acción principal se distingue por dos señales
- **WHEN** el usuario mira los botones Guardar, Crear copia, Restaurar o Generar junto a los demás botones de su pantalla
- **THEN** todos ellos se ven con el mismo sombreado y el mismo color de texto que los demás botones de su pantalla, sin distintivo propio

#### Scenario: Los botones de peligro conservan su rojo
- **WHEN** el usuario mira el botón Eliminar de Configuración
- **THEN** su texto va en el mismo color que los demás botones de la pantalla, sobre el mismo sombreado

#### Scenario: El botón reacciona al puntero y al foco
- **WHEN** el usuario sitúa el puntero sobre uno de esos botones, lo mantiene pulsado y después recorre la pantalla con el tabulador
- **THEN** el sombreado se oscurece al pasar por encima, se oscurece más mientras está pulsado, y el botón que recibe el foco muestra un borde con el color de acento

#### Scenario: Los botones de línea de la generación mensual dejan de desentonar
- **WHEN** el usuario abre la pantalla de generación de facturas mensuales
- **THEN** los botones Añadir línea y Eliminar línea se ven igual que Cancelar y Generar, sin el gris por defecto de la plataforma

#### Scenario: Arranque y los diálogos no cambian
- **WHEN** el usuario abre la pantalla de arranque o un diálogo de aviso o de confirmación
- **THEN** sus botones conservan el aspecto que tenían y el flujo de confirmación y cancelación no cambia

### Requirement: Estilo de zona de acciones en tema por defecto

En el tema por defecto (Biblioteca8), la zona de acciones de las pantallas SHALL distinguirse visualmente sin que resalte: las tarjetas superiores del Histórico, de Clientes y del Editor (Nueva factura), que contienen los campos de búsqueda o de factura y los botones de acción, SHALL tener un fondo gris claro `#F6F6F6`.

Los botones de la barra de acciones del Editor y del Histórico SHALL NOT mostrarse con fondo blanco: SHALL ser planos y adoptar el color del contenedor en el que están, según el requisito «Botones de acción con icono identificativo». Guardar SHALL mantener su condición de acción principal mediante el color de acento en su icono y su etiqueta, en lugar de mediante un fondo de acento. Anular SHALL mostrarse a plena intensidad en el color del tema, igual que los demás botones, de modo que un Anular habilitado SHALL NOT confundirse con un botón deshabilitado, que aparece atenuado.

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

## ADDED Requirements

### Requirement: Iconos de tamaño uniforme dentro de una barra

Dentro de una misma barra de acciones, todos los iconos SHALL ocupar una caja del mismo tamaño y SHALL quedar centrados en ella, con independencia de lo que mida el dibujo de cada uno. Así las etiquetas de todos los botones de la barra SHALL arrancar a la misma altura y quedar alineadas entre sí.

La caja SHALL ser lo bastante grande para contener el icono más grande de la barra sin recortarlo. Cada icono SHALL conservar sus proporciones: la caja iguala el espacio que ocupa, no deforma el dibujo.

Este requisito SHALL aplicarse a la barra de acciones de Clientes.

#### Scenario: Las etiquetas de la barra quedan alineadas
- **WHEN** el usuario abre Clientes
- **THEN** las etiquetas Nuevo, Editar, Eliminar y Volver arrancan a la misma altura, aunque sus iconos tengan dibujos de distinto tamaño

#### Scenario: Ningún icono se recorta ni se deforma
- **WHEN** el usuario mira los iconos de la barra de Clientes
- **THEN** cada uno se ve entero, con sus proporciones originales y centrado en su espacio
