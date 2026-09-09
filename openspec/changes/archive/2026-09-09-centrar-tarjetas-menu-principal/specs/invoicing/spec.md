## ADDED Requirements

### Requirement: Composición centrada del menú principal

El bloque de tarjetas del menú principal, formado por la tarjeta de información de la empresa activa y la tarjeta de opciones del programa, SHALL mostrarse centrado horizontal y verticalmente dentro del espacio disponible de la ventana.

Al redimensionar la ventana, el bloque SHALL permanecer centrado en ambos ejes, repartiendo el espacio sobrante por igual a los cuatro lados.

Las dos tarjetas SHALL conservar su anchura y su altura naturales, sin estirarse para ocupar el espacio disponible, y SHALL seguir alineadas entre sí por su borde superior.

La fila de la fecha de trabajo SHALL permanecer en la parte superior de la pantalla, sin verse afectada por el centrado del bloque.

#### Scenario: Bloque centrado en el tamaño mínimo
- **WHEN** el usuario abre el menú principal con la ventana en su tamaño mínimo de 1024x768
- **THEN** el bloque de las dos tarjetas queda centrado en horizontal y en vertical dentro del espacio disponible

#### Scenario: El bloque sigue centrado al agrandar la ventana
- **WHEN** el usuario agranda o maximiza la ventana desde el menú principal
- **THEN** el bloque se recoloca y queda centrado en ambos ejes, con el espacio sobrante repartido por igual arriba y abajo

#### Scenario: Las tarjetas no se estiran
- **WHEN** el usuario agranda la ventana desde el menú principal
- **THEN** las dos tarjetas conservan su anchura y su altura, sin crecer con la ventana, y siguen alineadas entre sí por su borde superior

#### Scenario: La fecha de trabajo no se mueve
- **WHEN** el usuario abre el menú principal a cualquier tamaño de ventana
- **THEN** la fila de la fecha de trabajo sigue mostrándose en la parte superior de la pantalla
