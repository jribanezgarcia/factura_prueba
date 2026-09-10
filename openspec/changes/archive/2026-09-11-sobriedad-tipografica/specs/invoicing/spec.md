## MODIFIED Requirements

### Requirement: Pantalla de generación mensual con estética alineada

La pantalla de generación de facturas mensuales SHALL presentar la misma estética visual que el resto de la aplicación. El título de la pantalla SHALL distinguirse con el mismo estilo de título del resto de ventanas, mostrándose con el tamaño de título y el color de texto del tema de apariencia activo. Las etiquetas del formulario (cliente, serie, año, mes de inicio, mes de fin, día del mes, tipo de IVA y retención de IRPF) SHALL mostrarse con una separación clara respecto a los campos y a los bordes del panel, sin quedar pegadas, y SHALL usar el mismo estilo de etiqueta de formulario que el resto de pantallas. El panel que contiene el formulario SHALL usar un fondo neutro acorde con la ventana (no un fondo blanco plano contrastado) y coherente con el resto de pantallas de la aplicación. La pantalla SHALL conservar los mismos campos, controles, botones y flujo de generación actuales; la modificación es exclusivamente de apariencia.

#### Scenario: Título destacado con el color del tema
- **WHEN** la aplicación muestra la pantalla de generación de facturas mensuales
- **THEN** el título «Generar facturas mensuales» se muestra con el mismo estilo destacado (tamaño de título y color de texto del tema) que los títulos del resto de pantallas

#### Scenario: Etiquetas separadas de los campos y del borde
- **WHEN** la aplicación muestra el formulario de la pantalla de generación mensual
- **THEN** cada etiqueta (cliente, serie, año, mes de inicio, mes de fin, día del mes, tipo de IVA, retención de IRPF) se muestra con separación clara respecto al borde del panel y a su campo, usando el estilo de etiqueta de formulario del resto de pantallas

#### Scenario: Panel con fondo neutro
- **WHEN** la aplicación muestra el panel del formulario de la pantalla de generación mensual
- **THEN** el panel se muestra con un fondo neutro del tema acorde con la ventana, y no como un bloque blanco plano contrastado

#### Scenario: Los campos y el flujo se mantienen
- **WHEN** el usuario abre la pantalla de generación mensual tras el cambio de apariencia
- **THEN** los mismos campos, controles, botones y el flujo de generación siguen disponibles y se comportan igual que antes

### Requirement: Sistema de diseño visual Apple

La aplicación SHALL aplicar un sistema de diseño visual inspirado en Ajustes de Apple: jerarquía clara, espaciado generoso, agrupación de controles en secciones con fondo de tarjeta, esquinas redondeadas, tipografía con tamaños diferenciados y una paleta de acentos coherente. Todos los controles interactivos (botones, campos, tablas, listas) SHALL mostrar un estado visual de `:hover` y `:focused` inmediato y sutil. Los formularios SHALL alinear etiquetas y campos con una cuadrícula coherente. Las tablas y listas SHALL usar filas de altura uniforme, separación clara y estado seleccionado visible pero no agresivo. Además, los paneles de contenido SHALL mantener un margen claro respecto al borde de la ventana y respecto a la barra de menú superior, de modo que los campos y tarjetas no queden pegados al borde ni se perciban solapados con la navegación. En cada pantalla principal, los paneles de contenido SHALL aplicarse como tarjetas con fondo, borde, esquinas redondeadas y espaciado interior; ningún panel SHALL quedarse sin ese estilo por una clase de estilo mal declarada.

#### Scenario: Pantallas con tarjetas de sección
- **WHEN** el usuario abre Configuración, el Editor, el Histórico, Clientes, Versiones o Backup
- **THEN** los controles se agrupan en secciones con fondo de tarjeta, esquinas redondeadas y separación respecto al fondo de la ventana

#### Scenario: Estados de foco visibles
- **WHEN** el usuario navega por el formulario con Tab o hace clic en un campo
- **THEN** el control enfocado muestra un anillo o borde de acento sutil y el botón bajo el cursor cambia de tono sin esperar al clic

#### Scenario: Tipografía jerárquica
- **WHEN** el usuario abre cualquier pantalla principal
- **THEN** los títulos son más grandes, los subtítulos algo menores y los datos secundarios aparecen en un tono más tenue, sin que la jerarquía dependa del peso de la letra

#### Scenario: Tablas limpias
- **WHEN** el usuario abre el Histórico
- **THEN** la tabla tiene cabecera clara, filas de altura uniforme, separadores suaves y la fila seleccionada se destaca con el color de acento muy suave

#### Scenario: Margen respecto al borde de la ventana
- **WHEN** el usuario abre cualquier pantalla principal
- **THEN** los paneles de contenido mantienen un margen visible respecto al borde izquierdo, derecho, superior e inferior de la ventana

#### Scenario: Separación respecto a la barra de menú
- **WHEN** el usuario abre el Histórico o la Configuración
- **THEN** existe una separación clara entre la barra de menú superior y la primera fila de controles o pestañas

#### Scenario: Filtros sin tocar el borde izquierdo
- **WHEN** el usuario abre el Histórico
- **THEN** el campo "Serie" y los demás filtros no están pegados al borde izquierdo de su tarjeta

#### Scenario: Ningún panel pierde el estilo de tarjeta
- **WHEN** la aplicación carga cualquiera de las pantallas principales
- **THEN** cada panel de contenido recibe las clases de estilo que declara y ninguno queda con una única clase errónea que impida aplicar el fondo de tarjeta, el borde, las esquinas redondeadas ni el espaciado

### Requirement: Botones de acción con icono identificativo

Los botones de las barras de acciones del Editor y del Histórico SHALL mostrar un icono identificativo de la acción encima de su etiqueta de texto.

En reposo el botón SHALL NOT pintar fondo ni borde propios: SHALL adoptar el color del contenedor en el que está, de modo que lo único visible sea la silueta del icono y su etiqueta, igual que en los botones de la barra de navegación superior. El botón SHALL conservar su forma y su tamaño, de manera que todos los botones de una misma barra sigan midiendo lo mismo.

Al pasar el puntero por encima, el botón SHALL insinuar su superficie con un velo translúcido neutro, y SHALL oscurecerlo al mantenerlo pulsado. Al recibir el foco de teclado, el botón SHALL dibujar un borde en el color de acento del tema, de modo que la navegación con teclado siga siendo visible pese a la ausencia de borde en reposo.

Los iconos SHALL ser monocromo de un solo color, dibujados como trazado vectorial, de modo que el tema activo pueda recolorearlos. SHALL NOT usarse imágenes de mapa de bits ni iconos multicolor de color fijo.

El color del icono y el de la etiqueta SHALL provenir del tema activo y SHALL mantener contraste legible sobre el fondo de la barra en los siete temas, incluidos los oscuros. Tanto el icono como la etiqueta SHALL ir en el color de acento del tema; en un botón destructivo, ambos SHALL ir en el color de peligro.

Ningún botón SHALL destacarse como acción principal: todos los de una misma barra SHALL presentarse con el mismo peso, el mismo color y el mismo tratamiento, y el orden de los botones SHALL ser la única jerarquía. Ninguna etiqueta SHALL mostrarse en negrita.

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
- **THEN** los tres muestran su icono y su etiqueta en el color de acento del tema, con el mismo peso, y ninguno se destaca sobre los demás

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

### Requirement: Sombreado uniforme de los botones de solo texto

Los botones que muestran únicamente texto, sin icono, SHALL presentarse sobre un sombreado suave y uniforme, sin borde y sin fondo blanco. El sombreado SHALL ser el mismo en todos los botones de una misma pantalla, salvo el de la acción principal.

El sombreado SHALL ser un gris neutro teñido levemente con el color de acento del tema activo, de modo que se lea como gris y acompañe al tema sin competir con él. Cada tema de apariencia SHALL declarar su propio valor, tanto el sombreado normal como el de la acción principal.

Estos botones SHALL mostrar su texto en peso normal, sin negrita.

Ningún botón SHALL destacarse como acción principal: todos los de una misma pantalla SHALL compartir el mismo sombreado y el mismo color de texto. Los botones de peligro SHALL conservar su color de texto rojo sobre el sombreado normal.

El botón SHALL reaccionar a la interacción: al situar el puntero encima su sombreado SHALL oscurecerse, al mantenerlo pulsado SHALL oscurecerse más, y al recibir el foco de teclado SHALL mostrar un borde con el color de acento del tema activo.

Este requisito SHALL aplicarse a los botones de solo texto de Clientes, Configuración, Copia de seguridad, Versiones, el Editor y la generación de facturas mensuales, incluidos los botones de línea de esta última, que hasta ahora mostraban el gris por defecto de la plataforma. SHALL NOT aplicarse a los botones de la pantalla de arranque ni a los de los diálogos de aviso y confirmación, que la plataforma construye por su cuenta. La maquetación, el comportamiento y las acciones de todos ellos SHALL permanecer sin cambios: la modificación es exclusivamente de apariencia.

#### Scenario: Los botones de una pantalla comparten sombreado
- **WHEN** el usuario abre Clientes, Configuración, Copia de seguridad o Versiones con cualquier tema
- **THEN** todos los botones de esa pantalla se ven sobre el mismo sombreado suave, sin borde ni fondo blanco, y con el texto en peso normal

#### Scenario: El sombreado se lee como gris en cada tema
- **WHEN** el usuario cambia entre los temas de apariencia disponibles
- **THEN** el sombreado de los botones cambia con el tema pero sigue leyéndose como un gris neutro, no como un color saturado

#### Scenario: La acción principal se distingue por dos señales
- **WHEN** el usuario mira los botones Nuevo, Guardar, Crear copia, Restaurar o Generar junto a los demás botones de su pantalla
- **THEN** todos ellos se ven con el mismo sombreado y el mismo color de texto que los demás botones de su pantalla, sin distintivo propio

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

### Requirement: Consonancia tipográfica de la interfaz

Los elementos de navegación y de acción de la aplicación SHALL presentar un peso tipográfico acorde entre sí, de modo que los controles más usados no se lean con menos presencia que los secundarios.

El texto de los botones que muestran icono y etiqueta —los de la barra de navegación y los de las barras de acciones del Editor y del Histórico— SHALL mostrarse en peso normal y a un tamaño que no quede por debajo del de las etiquetas de los botones de solo texto en más de un punto.

Las entradas seleccionables de la lista de secciones de la pantalla de Configuración SHALL mostrarse en peso normal. La entrada seleccionada SHALL seguir distinguiéndose de las demás por su fondo y por su color de texto.

Ningún control, etiqueta, acción ni disposición SHALL cambiar por este motivo: la modificación es exclusivamente tipográfica.

#### Scenario: Los botones con icono pesan como los de solo texto
- **WHEN** el usuario mira la barra de navegación o la barra de acciones del Editor junto a un botón de solo texto
- **THEN** las etiquetas de los botones con icono se leen con el mismo peso que las de los botones de solo texto, sin quedar unas visiblemente más pesadas que otras

#### Scenario: Las etiquetas de navegación no se recortan
- **WHEN** el usuario abre cualquier pantalla con la ventana en su tamaño mínimo de 1024x768
- **THEN** las siete etiquetas de la barra de navegación se muestran completas, sin recortarse, y la barra cabe en el ancho de la ventana

#### Scenario: La lista de secciones se lee en negrita
- **WHEN** el usuario abre la pantalla de Configuración
- **THEN** las entradas de la lista de secciones se muestran en peso normal, y la que está seleccionada se distingue por su fondo y su color de texto

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
- **THEN** los botones de tabla Añadir línea y Eliminar línea ya no se muestran con fondo blanco y texto negro: se ven sobre el sombreado suave, sin recuadro y con el texto en peso normal

#### Scenario: La barra de acciones pierde el fondo blanco de sus botones
- **WHEN** el usuario abre el Editor con el tema por defecto
- **THEN** los botones Guardar, Nueva, Exportar, Versiones, Rectificar, Anular, Restaurar y Volver se ven sin recuadro blanco, con el color de la barra de fondo

#### Scenario: Botones del Editor que conservan su estilo
- **WHEN** el usuario abre el Editor con el tema por defecto
- **THEN** el botón Guardar se ve igual que los demás botones de la barra, sin negrita ni distintivo propio, y el botón Anular conserva su texto rojo de peligro

#### Scenario: Anular habilitado no parece deshabilitado
- **WHEN** el usuario mira el botón Anular habilitado junto a un botón deshabilitado con el tema por defecto
- **THEN** el Anular se ve con su rojo a plena intensidad y se distingue a simple vista del botón deshabilitado, que aparece atenuado

#### Scenario: El resto de temas no cambian
- **WHEN** el usuario abre el Histórico, Clientes o el Editor con un tema distinto del por defecto
- **THEN** los colores de esas zonas son los propios de cada tema, sin los cambios del tema por defecto

## ADDED Requirements

### Requirement: La negrita queda reservada a los importes

La aplicación SHALL NOT usar la negrita como recurso de jerarquía en su interfaz. Los títulos de pantalla, los rótulos de sección, las etiquetas de los botones, las cabeceras de tabla, las entradas de lista y los nombres del menú principal SHALL mostrarse en peso normal, y SHALL distinguirse entre sí por su tamaño y por su color.

La negrita SHALL reservarse a los importes: el valor de las filas de totales y el total destacado de cada tema de apariencia. Fuera de esos dos casos, ningún texto de la interfaz SHALL mostrarse en negrita.

Esta regla SHALL alcanzar a los siete temas de apariencia por igual.

#### Scenario: Ningún control se muestra en negrita
- **WHEN** el usuario recorre el menú principal, el Editor, el Histórico, Clientes y Configuración
- **THEN** ninguna etiqueta de botón, cabecera de tabla, entrada de lista ni título de pantalla se muestra en negrita

#### Scenario: Los importes conservan la negrita
- **WHEN** el usuario mira el bloque de totales de una factura
- **THEN** el importe del total se sigue mostrando en negrita, de modo que se localiza de un vistazo

#### Scenario: La jerarquía se mantiene sin peso
- **WHEN** el usuario compara el título de una pantalla con el texto normal que la rodea
- **THEN** el título se distingue por ser mayor y por su color, no por su peso
