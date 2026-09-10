## ADDED Requirements

### Requirement: Consonancia tipográfica de la interfaz

Los elementos de navegación y de acción de la aplicación SHALL presentar un peso tipográfico acorde entre sí, de modo que los controles más usados no se lean con menos presencia que los secundarios.

El texto de los botones que muestran icono y etiqueta —los de la barra de navegación y los de las barras de acciones del Editor y del Histórico— SHALL mostrarse en negrita y a un tamaño que no quede por debajo del de las etiquetas de los botones de solo texto en más de un punto.

Las entradas seleccionables de la lista de secciones de la pantalla de Configuración SHALL mostrarse en negrita. La entrada seleccionada SHALL seguir distinguiéndose de las demás por su fondo y por su color de texto.

Ningún control, etiqueta, acción ni disposición SHALL cambiar por este motivo: la modificación es exclusivamente tipográfica.

#### Scenario: Los botones con icono pesan como los de solo texto
- **WHEN** el usuario mira la barra de navegación o la barra de acciones del Editor junto a un botón de solo texto
- **THEN** las etiquetas de los botones con icono se leen en negrita, sin quedar visiblemente más ligeras que las de los botones de solo texto

#### Scenario: Las etiquetas de navegación no se recortan
- **WHEN** el usuario abre cualquier pantalla con la ventana en su tamaño mínimo de 1024x768
- **THEN** las siete etiquetas de la barra de navegación se muestran completas, sin recortarse, y la barra cabe en el ancho de la ventana

#### Scenario: La lista de secciones se lee en negrita
- **WHEN** el usuario abre la pantalla de Configuración
- **THEN** las entradas de la lista de secciones se muestran en negrita, y la que está seleccionada se distingue por su fondo y su color de texto

## MODIFIED Requirements

### Requirement: Barra de acciones del editor sin desbordamiento

La barra de acciones del editor de facturas SHALL mostrar todos sus botones visibles a la vez en el tamaño mínimo de ventana (1024×768), sin recurrir a un menú de desbordamiento. En particular, la aparición del botón de anular al guardar una factura SHALL NOT ocultar ningún otro botón.

Ningún botón de la barra SHALL comprimirse por debajo de su anchura preferida ni salirse del ancho de la ventana. Los separadores entre grupos SHALL NOT contar como botones a efectos de este requisito.

Todos los botones de la barra SHALL tener la misma anchura, y esa anchura SHALL ser independiente de la longitud de la etiqueta. La anchura SHALL bastar para que una etiqueta de una sola palabra se muestre siempre en una única línea, sin partirse. Una etiqueta de varias palabras SHALL envolverse por sus espacios, y el botón SHALL reservar el alto necesario para mostrarla completa, en lugar de ensancharse o recortar el texto.

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
- **THEN** ambos miden exactamente lo mismo de ancho, y `Rectificativa` se lee en una sola línea, sin partirse

#### Scenario: Una etiqueta de dos palabras se envuelve por su espacio
- **WHEN** el usuario mira el botón `Generar mensuales` en el Histórico y el botón `Exportar PDF` en el Editor
- **THEN** cada uno muestra su etiqueta en dos líneas, partida por el espacio entre palabras, sin que ninguna palabra quede cortada
