## MODIFIED Requirements

### Requirement: Sombreado uniforme de los botones de solo texto

Los botones que muestran únicamente texto, sin icono, SHALL presentarse sobre un sombreado suave y uniforme, sin borde y sin fondo blanco. El sombreado SHALL ser el mismo en todos los botones de una misma pantalla.

El sombreado SHALL ser un gris neutro teñido levemente con el color de acento del tema activo, de modo que se lea como gris y acompañe al tema sin competir con él. Cada tema de apariencia SHALL declarar su propio valor de sombreado.

Estos botones SHALL mostrar su texto en peso normal, sin negrita.

Ningún botón SHALL destacarse como acción principal: todos los de una misma pantalla SHALL compartir el mismo sombreado y el mismo color de texto. Tampoco los botones de acciones destructivas SHALL mostrarse en color de peligro: van en el mismo color que los demás.

El botón SHALL reaccionar a la interacción: al situar el puntero encima su sombreado SHALL oscurecerse, al mantenerlo pulsado SHALL oscurecerse más, y al recibir el foco de teclado SHALL mostrar un borde con el color de acento del tema activo.

Estos botones SHALL tener la misma forma que los botones de los diálogos de aviso y confirmación: esquinas apenas redondeadas, relleno ajustado al texto y un ancho mínimo común. Los botones de solo texto que compartan fila SHALL medir todos lo mismo, el ancho del más ancho de ellos, sin que ninguna etiqueta se corte. Un botón solo en su fila SHALL conservar su ancho natural, sin bajar del mínimo.

Este requisito SHALL aplicarse a los botones de solo texto de Configuración, Copia de seguridad, Versiones, el Editor y la generación de facturas mensuales, incluidos los botones de línea de esta última, que hasta ahora mostraban el gris por defecto de la plataforma. SHALL NOT aplicarse a los botones que muestran icono, ni a los de la barra de navegación, el menú principal, la pantalla de arranque o los diálogos de aviso y confirmación, que la plataforma construye por su cuenta. El comportamiento y las acciones de todos ellos SHALL permanecer sin cambios, y la maquetación de cada pantalla SHALL seguir cabiendo en el tamaño mínimo de ventana.

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

#### Scenario: Los botones tienen forma de diálogo
- **WHEN** el usuario compara un botón de solo texto de Configuración con los botones de un diálogo de confirmación
- **THEN** los dos tienen las mismas esquinas apenas redondeadas y un relleno igual de ajustado

#### Scenario: Los botones de una fila miden lo mismo
- **WHEN** el usuario mira los botones Nuevo, Guardar e Inactivar/Activar de la sección de tipos de IVA de Configuración
- **THEN** los tres miden lo mismo, el ancho de Inactivar/Activar, y ninguna etiqueta aparece cortada

#### Scenario: El número de facturas a generar se indica fuera del botón
- **WHEN** el usuario cambia el mes de inicio o el de fin en la pantalla de generación de facturas mensuales
- **THEN** el botón sigue diciendo «Generar» y mide lo mismo que Cancelar, y una etiqueta junto a ellos indica al momento cuántas facturas se van a generar
