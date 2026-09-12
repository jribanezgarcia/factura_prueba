## MODIFIED Requirements

### Requirement: Criterio de etiquetado de botones

Las etiquetas de los botones de la aplicación SHALL seguir un criterio único en todas las pantallas, de modo que una misma acción se llame siempre igual.

El botón SHALL nombrar la **acción**, dejando que el objeto lo aporte la pantalla en la que está: en la pantalla de Clientes, «Eliminar» ya significa eliminar el cliente seleccionado.

Se SHALL usar un único verbo por concepto. En particular, la acción destructiva SHALL llamarse siempre «Eliminar» y SHALL NOT llamarse «Borrar» en ninguna pantalla.

El criterio SHALL alcanzar también al **texto de los diálogos** que abre cada botón: el título y el cuerpo del mensaje SHALL usar el mismo verbo que el botón desde el que se llega, de modo que el usuario no tenga que decidir si dos palabras distintas nombran la misma acción justo antes de confirmarla. El título de un diálogo SHALL NOT nombrar una acción que ese flujo no realiza.

«Volver» SHALL usarse para salir de una pantalla conservando lo realizado. «Cancelar» SHALL usarse únicamente en diálogos modales, donde el gesto descarta lo que se estaba componiendo.

Los atajos de teclado SHALL indicarse en el tooltip del botón y SHALL NOT formar parte del texto de la etiqueta.

Una misma función SHALL tener el mismo nombre desde cualquier punto de entrada. Un botón de una barra de iconos, cuyo ancho es fijo, SHALL poder mostrar una forma abreviada de ese nombre cuando el completo no quepa en una línea, y en ese caso SHALL llevar el nombre completo en su tooltip. La forma abreviada SHALL ser reconocible como la misma acción: SHALL NOT cambiar de verbo ni de concepto.

Las etiquetas SHALL ser lo bastante cortas como para que las barras de acciones no necesiten menú de desbordamiento en el tamaño mínimo de ventana.

#### Scenario: La acción destructiva se llama igual en todas partes
- **WHEN** el usuario compara el botón de eliminar del Histórico con el de la pantalla de Clientes
- **THEN** ambos dicen «Eliminar», y ninguna pantalla usa «Borrar»

#### Scenario: El diálogo dice lo mismo que el botón
- **WHEN** el usuario pulsa «Eliminar» en el Histórico y aparece la confirmación
- **THEN** el título y el cuerpo del diálogo hablan de eliminar, no de borrar

#### Scenario: El título del diálogo no nombra acciones que no ocurren
- **WHEN** el usuario anula facturas desde el Histórico
- **THEN** el diálogo se titula «Anular» y no menciona borrar, porque la anulación crea una versión nueva y no elimina nada

#### Scenario: Volver frente a Cancelar
- **WHEN** el usuario está en una pantalla principal
- **THEN** el botón de salida dice «Volver»
- **AND** «Cancelar» solo aparece en diálogos modales como el de generación mensual

#### Scenario: Los atajos no van en la etiqueta
- **WHEN** un botón tiene un atajo de teclado asociado
- **THEN** el atajo se indica en su tooltip y la etiqueta contiene solo el nombre de la acción

#### Scenario: Una función, un nombre
- **WHEN** el usuario abre la generación de facturas mensuales desde el Menú principal y desde el Histórico
- **THEN** el menú muestra «Facturar mes» y el botón del Histórico muestra «Mensual», que es la misma acción abreviada
- **AND** el tooltip del botón del Histórico dice «Generar facturas mensuales»

#### Scenario: La abreviatura no rebautiza la acción
- **WHEN** un botón de una barra de iconos no puede mostrar el nombre completo de su acción
- **THEN** muestra una forma abreviada de ese mismo nombre, con el nombre completo en su tooltip, y no un verbo ni un concepto distintos

### Requirement: Menú y navegación

La aplicación SHALL tener un menú principal con las opciones Nueva factura, Facturar mes, Histórico, Clientes, Configuración, Copia de seguridad y Salir. Cada opción SHALL nombrar la misma acción que el botón que la ejecuta en el resto de la aplicación. Cuando el ancho de una barra de iconos no permita mostrar el nombre completo, el botón SHALL poder usar una forma breve de ese nombre y SHALL llevar el nombre completo en su tooltip; SHALL NOT usar un nombre distinto. Dentro de una factura SHALL existir una barra superior con Guardar, Exportar, Versiones, Rectificar, Anular o Restaurar según el estado, Nueva y Volver. En todas las pantallas salvo el menú principal SHALL existir una barra de navegación superior que permita acceder a Menú principal, Nueva factura, Histórico, Clientes, Configuración, Copia de seguridad y Salir.

Cada botón de la barra de navegación SHALL mostrar un icono y, **debajo de él, una etiqueta de texto** con el nombre de su destino, de modo que la función de cada botón se reconozca sin necesidad de posar el puntero. La etiqueta visible SHALL ser breve y el tooltip SHALL conservar el nombre completo del destino. El texto SHALL usar, en cada tema de apariencia, un color legible sobre el fondo propio de la barra de navegación.

La barra de navegación SHALL NOT ocupar más alto del que pide su contenido: el icono, la etiqueta y el indicador de la pantalla activa, con márgenes ajustados. El hueco entre el icono y la etiqueta SHALL bastar para que el dibujo del icono no llegue a tocar el texto.

Solo SHALL tenerse una factura abierta a la vez. No SHALL existir la opción "Nueva rectificativa" en el menú principal. Al cerrar la ventana o al pulsar Salir en la barra de navegación, la aplicación SHALL pedir confirmación antes de salir y SHALL seguir el mismo proceso de cierre (comprobación de cambios sin guardar, preferencias de ventana y lock).

#### Scenario: La barra de navegación no reserva alto de más
- **WHEN** el usuario abre cualquier pantalla distinta del menú principal
- **THEN** la barra de navegación ocupa el alto de un icono más una línea de texto con sus márgenes, sin franjas vacías por encima ni por debajo, y el icono no toca la etiqueta

#### Scenario: Crear rectificativa desde la factura
- **WHEN** el usuario pulsa "Rectificar" en la barra de una factura abierta
- **THEN** se crea una rectificativa a partir de esa factura

#### Scenario: Navegar desde la barra de navegación
- **WHEN** el usuario pulsa un icono de la barra de navegación superior en una pantalla distinta del menú principal
- **THEN** la aplicación abre la pantalla correspondiente

#### Scenario: Cada botón de navegación se identifica sin tooltip
- **WHEN** el usuario mira la barra de navegación en cualquier pantalla
- **THEN** cada botón muestra su icono con el nombre de su destino escrito debajo

#### Scenario: Texto legible en todos los temas
- **WHEN** el usuario cambia entre los temas de apariencia disponibles
- **THEN** el texto de la barra de navegación se lee con claridad sobre el fondo propio de la barra en cada uno de ellos

#### Scenario: Cerrar la ventana con confirmación
- **WHEN** el usuario cierra la ventana
- **THEN** la aplicación pide confirmación antes de salir

#### Scenario: Salir desde la barra de navegación con confirmación
- **WHEN** el usuario pulsa Salir en la barra de navegación
- **THEN** la aplicación pide confirmación «¿Seguro que deseas salir de la aplicación?» y solo cierra si se acepta, siguiendo el mismo proceso de cierre que al cerrar la ventana

### Requirement: Barra de acciones del editor sin desbordamiento

La barra de acciones del editor de facturas SHALL mostrar todos sus botones visibles a la vez en el tamaño mínimo de ventana (1024×768), sin recurrir a un menú de desbordamiento. En particular, la aparición del botón de anular al guardar una factura SHALL NOT ocultar ningún otro botón.

Ningún botón de la barra SHALL comprimirse por debajo de su anchura preferida ni salirse del ancho de la ventana. Los separadores entre grupos SHALL NOT contar como botones a efectos de este requisito.

Todos los botones de la barra SHALL tener la misma anchura, y esa anchura SHALL ser independiente de la longitud de la etiqueta. La anchura SHALL bastar para que una etiqueta de una sola palabra se muestre siempre en una única línea, sin partirse. Una etiqueta de varias palabras SHALL envolverse por sus espacios, y el botón SHALL reservar el alto necesario para mostrarla completa, en lugar de ensancharse o recortar el texto.

El botón `Nueva` SHALL ir inmediatamente después de `Guardar`, de modo que las dos acciones de escritura queden juntas.

El título de la factura SHALL mostrarse siempre completo. Cuando no quepa en el ancho disponible SHALL envolverse en varias líneas por sus espacios, sin recortarse con puntos suspensivos y sin desplazar a los botones. El alto de la barra lo marcan los botones, de modo que un título de dos o tres líneas SHALL caber sin hacerla crecer.

El título SHALL mostrarse separado del identificador de empresa por un hueco perceptible, de modo que ambos no se lean como un solo bloque.

Los botones SHALL NOT reservar un alto mínimo fijo por encima de lo que pide su contenido: su altura SHALL ser la del icono más la etiqueta con sus márgenes, de modo que la barra no ocupe más espacio vertical del necesario.

Los botones que requieren una factura ya guardada SHALL mostrarse deshabilitados mientras no la haya, en lugar de responder con un aviso al pulsarlos.

#### Scenario: Guardar una factura no esconde botones
- **WHEN** el usuario guarda una factura nueva y aparece el botón de anular
- **THEN** todos los botones de la barra siguen visibles y ninguno queda comprimido por debajo de su anchura preferida

#### Scenario: El título se lee entero en una factura emitida
- **WHEN** el usuario abre una factura emitida cuyo título es «Factura C-59/7 (v1)»
- **THEN** el título se muestra completo, sin elipsis

#### Scenario: Número de factura largo
- **WHEN** se abre una factura cuyo número hace el título especialmente largo, o se muestra el distintivo de anulada
- **THEN** el título se envuelve en varias líneas dentro del alto de la barra, sin recortarse, y los botones conservan su posición, su visibilidad y su anchura

#### Scenario: Botones que necesitan una factura guardada
- **WHEN** el usuario está en una factura nueva todavía sin guardar
- **THEN** los botones de Versiones y Rectificar se muestran deshabilitados

#### Scenario: Nueva junto a Guardar
- **WHEN** el usuario mira la barra de acciones del Editor
- **THEN** el botón `Nueva` aparece inmediatamente después de `Guardar`

#### Scenario: Etiquetas largas no ensanchan el botón
- **WHEN** el usuario mira los botones `Guardar` y `Rectificar` en la misma barra
- **THEN** ambos miden exactamente lo mismo de ancho, y `Rectificar` se lee en una sola línea, sin partirse

#### Scenario: Una etiqueta de dos palabras se envuelve por su espacio
- **WHEN** un botón de la barra lleva una etiqueta de dos palabras que no cabe en una sola línea
- **THEN** se envuelve partida por el espacio entre palabras, sin que ninguna palabra quede cortada y sin ensanchar el botón
- **AND** si la barra no da alto para esas dos líneas, la etiqueta SHALL abreviarse en vez de recortarse, según el requisito «Criterio de etiquetado de botones»

#### Scenario: La etiqueta del botón mensual se lee entera
- **WHEN** el usuario mira el botón de generación mensual en el Histórico
- **THEN** su etiqueta se lee completa en una sola línea, sin recortarse con puntos suspensivos
- **AND** al posar el puntero sobre él aparece el nombre completo de la acción

#### Scenario: La barra no reserva alto de más
- **WHEN** el usuario abre el Editor, donde todas las etiquetas caben en una línea
- **THEN** la barra de acciones ocupa el alto de un icono más una línea de texto, sin espacio sobrante por encima ni por debajo de los botones

#### Scenario: El título no se pega al identificador de empresa
- **WHEN** el usuario abre cualquier factura
- **THEN** entre el identificador de empresa y el título hay un hueco perceptible, y no se leen como un bloque continuo
