## MODIFIED Requirements

### Requirement: Distribución estable al redimensionar en Editor e Histórico

Los campos del Editor SHALL ocupar siempre la misma anchura, tanto en el tamaño mínimo de ventana (1024×768) como maximizado: el espacio sobrante SHALL quedar vacío a la derecha y solo la tabla de líneas SHALL crecer. Las etiquetas de los bloques FACTURA y CLIENTE SHALL verse enteras a 1024×768, sin recortes ni puntos suspensivos.

En el bloque CLIENTE, los campos Nombre, Email y Localidad SHALL tener anchura suficiente para nombres de empresa y de persona habituales —al menos el doble que los campos NIF, CP y Provincia— y las etiquetas NIF, CP y Provincia SHALL quedar próximas a sus campos, sin huecos que las desconecten visualmente de ellos.

Los filtros del Histórico SHALL mantener siempre las mismas filas y posiciones, tanto a 1024×768 como maximizado: solo la tabla de facturas SHALL crecer con el ancho disponible.

#### Scenario: Etiquetas del Editor legibles a 1024
- **WHEN** el usuario abre el Editor en el tamaño mínimo de ventana
- **THEN** las etiquetas «Forma de pago» y «Vencimiento» se leen enteras, sin «…»

#### Scenario: Editor idéntico maximizado
- **WHEN** el usuario maximiza la ventana con el Editor abierto
- **THEN** los campos ocupan exactamente el mismo ancho que a 1024, el hueco queda a la derecha y solo la tabla de líneas se ensancha

#### Scenario: Campos de cliente anchos y etiquetas próximas
- **WHEN** el usuario mira el bloque CLIENTE del Editor a 1024×768
- **THEN** los campos Nombre, Email y Localidad muestran al menos el doble de ancho que los campos NIF, CP y Provincia
- **AND** las etiquetas NIF, CP y Provincia aparecen junto a sus campos

#### Scenario: Filtros del Histórico estables
- **WHEN** el usuario abre el Histórico a 1024 y luego maximiza
- **THEN** los 7 filtros mantienen las mismas filas y posiciones y solo la tabla crece
