## ADDED Requirements

### Requirement: Menú principal adaptado a 800×600

La pantalla del Menú principal SHALL tener un tamaño de 800×600 y SHALL ajustar sus elementos para que la información de la empresa sea visible sin cortarse horizontalmente y para que la lista de opciones deje un margen inferior visible respecto al borde de la ventana. Las tarjetas de la pantalla SHALL mantener el estilo visual del sistema de diseño (fondo de tarjeta, esquinas redondeadas, sombra) y SHALL adaptarse al ancho disponible sin superponerse.

#### Scenario: Información de empresa visible
- **WHEN** el usuario abre el Menú principal
- **THEN** la tarjeta de empresa muestra el nombre, la información adicional y el logo sin cortarse horizontalmente

#### Scenario: Margen inferior en el Menú principal
- **WHEN** el usuario abre el Menú principal
- **THEN** debajo de la última opción del menú queda un espacio visible antes del borde inferior de la ventana

#### Scenario: Opciones del menño dentro de 800×600
- **WHEN** el usuario abre el Menú principal
- **THEN** todas las opciones del menú y la tarjeta de empresa caben dentro de la ventana sin scroll
