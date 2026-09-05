## MODIFIED Requirements

### Requirement: Barra de acciones del editor sin desbordamiento

La barra de acciones del editor de facturas SHALL mostrar todos sus botones visibles a la vez en el tamaño mínimo de ventana (1024×768), sin recurrir a un menú de desbordamiento. En particular, la aparición del botón de anular al guardar una factura SHALL NOT ocultar ningún otro botón.

Ningún botón de la barra SHALL comprimirse por debajo de su anchura preferida ni salirse del ancho de la ventana. Los separadores entre grupos SHALL NOT contar como botones a efectos de este requisito.

Todos los botones de la barra SHALL tener la misma anchura, y esa anchura SHALL ser independiente de la longitud de la etiqueta: una etiqueta que no quepa en una línea SHALL envolverse a dos en lugar de ensanchar el botón.

El botón `Nueva` SHALL ir inmediatamente después de `Guardar`, de modo que las dos acciones de escritura queden juntas.

El título de la factura SHALL conservar su texto completo mientras haya espacio para él. SHALL tener una anchura máxima y recortarse con elipsis únicamente cuando el espacio disponible se reduzca, como ocurre al mostrarse el distintivo de factura anulada, de modo que un número de factura largo nunca desplace a los botones.

Los botones que requieren una factura ya guardada SHALL mostrarse deshabilitados mientras no la haya, en lugar de responder con un aviso al pulsarlos.

#### Scenario: Guardar una factura no esconde botones
- **WHEN** el usuario guarda una factura nueva y aparece el botón de anular
- **THEN** todos los botones de la barra siguen visibles y ninguno queda comprimido por debajo de su anchura preferida

#### Scenario: El título se lee entero en una factura emitida
- **WHEN** el usuario abre una factura emitida cuyo título es «Factura C-59/7 (v1)»
- **THEN** el título se muestra completo, sin elipsis

#### Scenario: Número de factura largo
- **WHEN** se abre una factura cuyo número hace el título especialmente largo, o se muestra el distintivo de anulada
- **THEN** el título se recorta con elipsis y los botones de la barra conservan su posición, su visibilidad y su anchura

#### Scenario: Botones que necesitan una factura guardada
- **WHEN** el usuario está en una factura nueva todavía sin guardar
- **THEN** los botones de Versiones y Rectificativa se muestran deshabilitados

#### Scenario: Nueva junto a Guardar
- **WHEN** el usuario mira la barra de acciones del Editor
- **THEN** el botón `Nueva` aparece inmediatamente después de `Guardar`

#### Scenario: Etiquetas largas no ensanchan el botón
- **WHEN** el usuario mira los botones `Guardar` y `Rectificativa` en la misma barra
- **THEN** ambos miden exactamente lo mismo de ancho, y el texto de `Rectificativa` se ha envuelto a dos líneas

## ADDED Requirements

### Requirement: Botones de acción con icono identificativo

Los botones de las barras de acciones del Editor y del Histórico SHALL mostrar un icono identificativo de la acción encima de su etiqueta de texto, en un botón de forma cuadrada delimitado por un borde visible.

Los iconos SHALL ser monocromo de un solo color, dibujados como trazado vectorial, de modo que el tema activo pueda recolorearlos. SHALL NOT usarse imágenes de mapa de bits ni iconos multicolor de color fijo.

El color del icono SHALL provenir del tema activo y SHALL mantener contraste legible sobre el fondo del botón en los siete temas, incluidos los oscuros. En un botón de acción principal el icono SHALL ir en el color del texto sobre acento; en un botón secundario, en el color de acento del tema; en un botón destructivo, en el color de peligro del tema.

Una misma acción SHALL llevar el mismo icono en todas las pantallas donde aparezca, y dos acciones distintas SHALL NOT compartir icono.

Los botones de una barra de acciones SHALL agruparse por afinidad, y los grupos SHALL separarse visualmente mediante un separador vertical. La agrupación SHALL NOT alterar el significado ni el comportamiento de ningún botón.

Este requisito alcanza únicamente a las barras de acciones del Editor y del Histórico. Los botones de formulario, los de los diálogos modales y los de las demás pantallas SHALL conservar su aspecto actual mientras no se especifique lo contrario.

#### Scenario: Icono sobre el texto en la barra del Editor
- **WHEN** el usuario abre el Editor
- **THEN** cada botón de la barra de acciones muestra un icono encima de su etiqueta, dentro de un recuadro cuadrado con borde

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
- **THEN** sus botones siguen siendo rectangulares y solo con texto, exactamente como antes
