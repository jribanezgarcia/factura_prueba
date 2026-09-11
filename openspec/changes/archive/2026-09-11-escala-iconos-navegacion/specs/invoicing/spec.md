## MODIFIED Requirements

### Requirement: Iconos de tamaño uniforme dentro de una barra

Dentro de una misma barra de acciones, todos los iconos SHALL ocupar una caja del mismo tamaño y SHALL quedar centrados en ella, con independencia de lo que mida el dibujo de cada uno. Así las etiquetas de todos los botones de la barra SHALL arrancar a la misma altura y quedar alineadas entre sí.

La caja SHALL ser lo bastante grande para contener el icono más grande de la barra sin recortarlo. Cada icono SHALL conservar sus proporciones: la caja iguala el espacio que ocupa, no deforma el dibujo.

Este requisito SHALL aplicarse a las barras de acciones de Clientes, del Editor y del Histórico, que SHALL compartir el mismo tamaño de caja. SHALL aplicarse también a la lista de opciones del menú principal, con una caja propia: allí el icono va a la izquierda del texto, y lo que se iguala es la posición horizontal en la que arrancan el nombre y la descripción de cada opción.

SHALL aplicarse igualmente a la barra de navegación, con su propia caja. En ella ningún icono SHALL desplazarse a mano fuera del centro de su caja. Para que todos se vean de un tamaño parecido, cada icono de la barra de navegación SHALL poder llevar su propia escala, siempre la misma en horizontal y en vertical para no deformarlo, y sin salirse de su caja.

#### Scenario: Las etiquetas de la barra quedan alineadas
- **WHEN** el usuario abre Clientes
- **THEN** las etiquetas Nuevo, Editar, Eliminar y Volver arrancan a la misma altura, aunque sus iconos tengan dibujos de distinto tamaño

#### Scenario: Ningún icono se recorta ni se deforma
- **WHEN** el usuario mira los iconos de la barra de Clientes, del Editor, del Histórico, del menú principal o de la barra de navegación
- **THEN** cada uno se ve entero, con sus proporciones originales y centrado en su espacio

#### Scenario: Las etiquetas del Editor quedan alineadas
- **WHEN** el usuario abre el Editor
- **THEN** las etiquetas de todos los botones de la barra de acciones arrancan a la misma altura

#### Scenario: Las etiquetas del Histórico quedan alineadas
- **WHEN** el usuario abre el Histórico
- **THEN** las etiquetas de todos los botones de la barra de acciones arrancan a la misma altura

#### Scenario: Los textos del menú principal quedan alineados
- **WHEN** el usuario abre el menú principal
- **THEN** el nombre y la descripción de todas las opciones arrancan en la misma posición horizontal, sin que ningún icono toque su texto

#### Scenario: Los iconos de navegación se ven de un tamaño parecido
- **WHEN** el usuario mira la barra de navegación en cualquier pantalla
- **THEN** los siete iconos se ven de un tamaño parecido, cada uno centrado sobre su etiqueta y sin desplazamientos a un lado
