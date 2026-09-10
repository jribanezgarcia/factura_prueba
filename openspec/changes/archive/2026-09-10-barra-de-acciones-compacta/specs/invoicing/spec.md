## MODIFIED Requirements

### Requirement: Menú y navegación

La aplicación SHALL tener un menú principal con las opciones Nueva factura, Facturar mes, Histórico, Clientes, Configuración, Copia de seguridad y Salir. Cada opción SHALL llamarse igual que el botón que ejecuta esa misma acción en el resto de la aplicación. Dentro de una factura SHALL existir una barra superior con Guardar, Exportar, Versiones, Rectificar, Anular o Restaurar según el estado, Nueva y Volver. En todas las pantallas salvo el menú principal SHALL existir una barra de navegación superior que permita acceder a Menú principal, Nueva factura, Histórico, Clientes, Configuración, Copia de seguridad y Salir.

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

### Requirement: Rectificativas

La aplicación SHALL permitir crear facturas rectificativas usando la serie independiente R (`R-1`, `R-2`, ...), que no distinguen entre cocina y puerta. Las rectificativas SHALL crearse desde una factura existente; no SHALL existir una opción independiente de "Nueva rectificativa" en el menú principal. Al crear una rectificativa SHALL copiarse los datos de la factura original: cliente, líneas, cantidades, descripciones, precios, IVA, descuento y observaciones. La rectificativa SHALL indicar qué factura rectifica mediante una referencia que se genera automáticamente y puede modificarse manualmente. La fecha de una rectificativa SHALL inicializarse con la fecha de trabajo actual y poder cambiarse. Una rectificativa SHALL poder ser parcial o total. Una rectificativa SHALL poder rectificar a otra rectificativa.

#### Scenario: Crear rectificativa desde factura
- **WHEN** el usuario crea una rectificativa desde una factura C-59/8
- **THEN** se crea una factura en la serie R con los datos copiados y la referencia a C-59/8 generada automáticamente

#### Scenario: Modificar referencia
- **WHEN** el usuario modifica manualmente la referencia de la rectificativa
- **THEN** la aplicación guarda la referencia indicada

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
- **WHEN** el usuario mira el botón `Facturar mes` en el Histórico
- **THEN** muestra su etiqueta en dos líneas, partida por el espacio entre palabras, sin que ninguna palabra quede cortada

#### Scenario: La barra no reserva alto de más
- **WHEN** el usuario abre el Editor, donde todas las etiquetas caben en una línea
- **THEN** la barra de acciones ocupa el alto de un icono más una línea de texto, sin espacio sobrante por encima ni por debajo de los botones

#### Scenario: El título no se pega al identificador de empresa
- **WHEN** el usuario abre cualquier factura
- **THEN** entre el identificador de empresa y el título hay un hueco perceptible, y no se leen como un bloque continuo

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

### Requirement: Atajos de teclado

La aplicación SHALL proporcionar los atajos Ctrl+N para Nueva factura, Ctrl+S para Guardar, Ctrl+F para Buscar, Ctrl+P para Exportar y Esc para volver/cancelar cuando corresponda.

#### Scenario: Guardar con atajo
- **WHEN** el usuario pulsa Ctrl+S en una factura abierta
- **THEN** la factura se guarda, sobrescribiendo la versión actual o creando una nueva versión según corresponda

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
- **THEN** los botones de tabla Añadir línea y Eliminar línea ya no se muestran con fondo blanco y texto negro: se ven sobre el sombreado suave, sin recuadro y con el texto en negrita

#### Scenario: La barra de acciones pierde el fondo blanco de sus botones
- **WHEN** el usuario abre el Editor con el tema por defecto
- **THEN** los botones Guardar, Nueva, Exportar, Versiones, Rectificar, Anular, Restaurar y Volver se ven sin recuadro blanco, con el color de la barra de fondo

#### Scenario: Botones del Editor que conservan su estilo
- **WHEN** el usuario abre el Editor con el tema por defecto
- **THEN** el botón Guardar se lee como acción principal por su color de acento y su negrita, y el botón Anular conserva su texto rojo de peligro

#### Scenario: Anular habilitado no parece deshabilitado
- **WHEN** el usuario mira el botón Anular habilitado junto a un botón deshabilitado con el tema por defecto
- **THEN** el Anular se ve con su rojo a plena intensidad y se distingue a simple vista del botón deshabilitado, que aparece atenuado

#### Scenario: El resto de temas no cambian
- **WHEN** el usuario abre el Histórico, Clientes o el Editor con un tema distinto del por defecto
- **THEN** los colores de esas zonas son los propios de cada tema, sin los cambios del tema por defecto

### Requirement: Facturación mensual por cliente

La aplicación SHALL permitir generar múltiples facturas mensuales para un único cliente desde un diálogo específico. El usuario SHALL seleccionar el cliente, el año, el rango de meses, la serie de numeración y el día del mes que se usará como fecha de cada factura, pudiendo elegir entre un día fijo editable, el primer día del mes o el último día del mes. El usuario SHALL poder configurar las líneas de concepto que se replicarán en cada factura, con la opción de añadir automáticamente el nombre del mes a la descripción de cada línea. El usuario SHALL seleccionar el tipo de IVA y, opcionalmente, el tipo de retención IRPF que se aplicarán a todas las facturas generadas. El sistema SHALL crear una factura por cada mes del rango, asignando a cada una el siguiente número de la serie seleccionado y la fecha correspondiente. Si para un mes ya existe una factura para ese cliente y año, el sistema SHALL mostrar una advertencia con los meses afectados y SHALL permitir al usuario decidir si genera las facturas de todos modos o cancela la operación. Las facturas generadas SHALL aparecer en el histórico y SHALL poder exportarse a PDF.

#### Scenario: Acceso desde el menú principal
- **WHEN** el usuario pulsa la opción "Facturar mes" en el menú principal
- **THEN** se abre el diálogo de facturación mensual

#### Scenario: Acceso desde el histórico
- **WHEN** el usuario pulsa el botón "Facturar mes" en la pantalla de histórico
- **THEN** se abre el diálogo de facturación mensual

#### Scenario: Configuración de la generación
- **WHEN** el usuario selecciona un cliente, un año, un mes de inicio, un mes de fin, una serie de numeración y un día del mes
- **THEN** el diálogo muestra los datos completos y habilita el botón de generar

#### Scenario: Líneas con descripción mensual
- **WHEN** el usuario añade una línea con descripción "contabilidad y laboral" y marca la opción "Añadir mes"
- **THEN** las facturas generadas contendrán una línea con descripción "contabilidad y laboral - mes de enero", "contabilidad y laboral - mes de febrero", etc.

#### Scenario: Aplicación de IVA y retención
- **WHEN** el usuario selecciona un tipo de IVA del 21% y un tipo de retención del 15%
- **THEN** todas las facturas generadas aplican esos porcentajes en el cálculo de totales

#### Scenario: Fechas con día ajustado
- **WHEN** el usuario elige día 31 y el mes de febrero del año seleccionado no tiene 31 días
- **THEN** la factura de febrero se fecha con el último día válido de ese mes

#### Scenario: Selección de primer día del mes
- **WHEN** el usuario marca la opción "Primer día del mes"
- **THEN** todas las facturas generadas usan el día 1 de cada mes

#### Scenario: Selección de último día del mes
- **WHEN** el usuario marca la opción "Último día del mes"
- **THEN** cada factura se fecha con el último día válido de su mes

#### Scenario: Numeración correlativa por serie
- **WHEN** el usuario selecciona una serie con formato MES y el siguiente correlativo de 2026 es 10
- **THEN** las facturas generadas reciben los números correspondientes a los meses, incrementando el correlativo según la serie y el ejercicio

#### Scenario: Advertencia ante meses con facturas existentes
- **WHEN** ya existen facturas para el cliente seleccionado en marzo y abril de 2026
- **THEN** el sistema muestra un diálogo de confirmación listando esos meses
- **AND** si el usuario acepta, se generan las facturas de todos los meses incluyendo los duplicados
- **AND** si el usuario cancela, no se genera ninguna factura

#### Scenario: Resumen tras generación
- **WHEN** el usuario genera facturas mensuales para todo el año
- **THEN** se cierra el diálogo y se muestra una alerta con el número de facturas generadas

#### Scenario: Cancelación sin generar nada
- **WHEN** el usuario abre el diálogo y pulsa "Cancelar"
- **THEN** no se crea ninguna factura y el diálogo se cierra

#### Scenario: Uso de huecos de numeración al generar mensualmente
- **WHEN** el usuario genera 12 facturas mensuales, las borra y vuelve a generar 12 facturas del mismo año
- **THEN** el sistema detecta los 12 huecos libres y pregunta si se deben rellenar
- **AND** si el usuario acepta, las nuevas facturas usan los números 1 a 12 en lugar de empezar por el 13
