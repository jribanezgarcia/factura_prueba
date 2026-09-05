## MODIFIED Requirements

### Requirement: Barra de acciones del editor sin desbordamiento

La barra de acciones del editor de facturas SHALL mostrar todos sus botones visibles a la vez en el tamaño mínimo de ventana (1024×768), sin recurrir a un menú de desbordamiento. En particular, la aparición del botón de anular al guardar una factura SHALL NOT ocultar ningún otro botón.

Ningún botón de la barra SHALL comprimirse por debajo de su anchura preferida ni salirse del ancho de la ventana.

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
