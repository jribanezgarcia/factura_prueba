# invoicing Specification

## Purpose
Sistema de facturación de escritorio para Windows, local y de un único usuario, que permite crear, editar, versionar, buscar y exportar a PDF facturas y rectificativas, sustituyendo el proceso manual en hoja de cálculo.

## Requirements

### Requirement: Clientes

La aplicación SHALL permitir gestionar una ficha de clientes con nombre/razón social, NIF, dirección, código postal, localidad y provincia. El NIF será opcional; cuando se informe, la aplicación SHALL validar DNI, NIE y NIF/CIF español antes de permitir guardar. Un cliente sin facturas asociadas SHALL poder eliminarse físicamente. Un cliente con facturas asociadas SHALL NOT poder eliminarse físicamente y SHALL poder marcarse como inactivo. Un cliente inactivo SHALL NOT aparecer normalmente al crear nuevas facturas, SHALL seguir apareciendo en el histórico y sus facturas SHALL seguir siendo consultables.

#### Scenario: NIF inválido al alta o edición de cliente
- **WHEN** el usuario abandona el campo NIF mediante Enter o cambiando el foco y el documento no vacío es inválido
- **THEN** la aplicación informa de que el NIF no es válido y mantiene el foco en el campo

#### Scenario: Salvaguarda al guardar cliente
- **WHEN** el usuario intenta guardar un cliente con un NIF no vacío inválido
- **THEN** la aplicación no guarda el cliente e informa del error

#### Scenario: Borrado físico de cliente sin facturas
- **WHEN** el usuario elimina un cliente que no tiene facturas asociadas
- **THEN** el cliente se elimina físicamente de la base de datos

#### Scenario: Bloqueo de borrado de cliente con facturas
- **WHEN** el usuario intenta eliminar un cliente que tiene facturas asociadas
- **THEN** la aplicación no permite el borrado y ofrece marcar el cliente como inactivo

#### Scenario: Cliente inactivo en histórico
- **WHEN** el usuario busca en el histórico facturas de un cliente inactivo
- **THEN** las facturas aparecen y son consultables

### Requirement: Búsqueda de clientes al crear factura

Al crear o editar una factura, el usuario SHALL poder buscar un cliente por nombre/razón social o NIF con búsqueda incremental mientras escribe. Al seleccionar un cliente, sus datos —incluido el email— SHALL cargarse en la factura. Los datos del cliente (email incluido) SHALL poder modificarse desde la factura, y esos cambios SHALL actualizar también la ficha general del cliente. La ficha general de clientes SHALL incluir el campo email.

#### Scenario: Búsqueda incremental
- **WHEN** el usuario escribe caracteres en el campo de búsqueda de cliente
- **THEN** la lista de clientes coincidentes por nombre o NIF se actualiza con cada carácter

#### Scenario: Desplegable de clientes
- **WHEN** el usuario pulsa la flecha del selector de cliente sin texto de búsqueda
- **THEN** la aplicación muestra los clientes activos disponibles

#### Scenario: Selección de cliente
- **WHEN** el usuario selecciona un cliente de la lista
- **THEN** nombre/razón social, NIF, dirección, código postal, localidad, provincia y email se cargan en la factura

#### Scenario: Modificación del cliente desde la factura
- **WHEN** el usuario modifica un dato de cliente (email incluido) dentro de la factura y guarda
- **THEN** la ficha general del cliente queda actualizada con ese dato

### Requirement: Facturas normales

La aplicación SHALL permitir crear y editar facturas normales con número, fecha, cliente, líneas, descuento general, IVA, observaciones, totales y tres datos de pago opcionales: forma de pago, fecha de vencimiento y realizada por. Estos datos de pago SHALL quedar guardados en la versión de la factura y SHALL aparecer en el PDF solo cuando estén rellenos. La fecha de la factura SHALL ser editable mediante un selector/calendario. La introducción de líneas SHALL ser similar a trabajar con una hoja de cálculo.

#### Scenario: Crear factura con datos completos
- **WHEN** el usuario crea una factura con cliente, líneas, descuento, IVA y observaciones y la guarda
- **THEN** la factura se almacena con su número definitivo y aparece en el histórico

#### Scenario: Editar factura emitida
- **WHEN** el usuario modifica la versión actual de una factura en estado Emitida y guarda
- **THEN** tras la confirmación, la versión actual se sobrescribe con los cambios

#### Scenario: Datos de pago opcionales
- **WHEN** el usuario guarda una factura dejando vacíos forma de pago, vencimiento y realizada por
- **THEN** la factura se guarda igualmente y el PDF no incluye esas filas

### Requirement: Líneas de factura

Cada línea SHALL tener cantidad, descripción, precio unitario, total e IVA. La cantidad SHALL ser un número entero que empieza en 1. El usuario SHALL poder añadir y eliminar líneas mediante botón y mediante la tecla Supr. Se SHALL mantener el orden de introducción de las líneas. No SHALL existir límite artificial de líneas. La descripción SHALL poder ser larga y ocupar varias líneas. Una factura SHALL requerir al menos una línea para poder guardarse.

#### Scenario: Añadir línea por flujo de teclado
- **WHEN** el usuario introduce cantidad, Enter, descripción, Enter, precio, Enter y total, Enter
- **THEN** la línea queda completa y se crea una nueva línea vacía a continuación

#### Scenario: Enter en total con línea incompleta
- **WHEN** el usuario pulsa Enter en el total y falta un dato necesario de la línea
- **THEN** no se crea una nueva línea y la línea actual se mantiene editable

#### Scenario: Eliminar línea
- **WHEN** el usuario selecciona una línea y pulsa el botón de eliminar o la tecla Supr
- **THEN** la línea se elimina de la factura

#### Scenario: Guardar sin líneas
- **WHEN** el usuario intenta guardar una factura sin ninguna línea
- **THEN** la aplicación impide el guardado e informa del error

### Requirement: Precios y recálculo

El modo normal de introducción SHALL ser con importes sin IVA: el precio unitario y el total de línea representan la base imponible y el IVA se calcula aparte. El usuario SHALL poder modificar tanto el precio unitario como el total de línea; al modificar uno, el otro se recalcula según la cantidad y el IVA de la línea. El precio unitario SHALL poder tener más precisión internamente aunque se muestre con 2 decimales. La aplicación SHALL permitir además introducir el total final de línea con IVA incluido, calculando hacia atrás la base imponible y el IVA. El tipo de IVA predeterminado SHALL ser el 21%.

#### Scenario: Modificar precio unitario
- **WHEN** el usuario modifica el precio unitario de una línea con cantidad 2 y tipo de IVA 21%
- **THEN** el total de línea se recalcula como cantidad por precio y el importe de IVA se recalcula sobre esa base

#### Scenario: Modificar total de línea
- **WHEN** el usuario modifica el total de línea
- **THEN** el precio unitario se recalcula como total dividido por cantidad

#### Scenario: Introducir total con IVA incluido
- **WHEN** el usuario introduce el total final de línea con IVA incluido
- **THEN** la aplicación calcula hacia atrás la base imponible (total dividido por 1 más el tipo) y el IVA correspondiente, y deduce el precio unitario

#### Scenario: Redondeo de importes
- **WHEN** el sistema calcula cualquier importe con más de 2 decimales
- **THEN** lo redondea a 2 decimales con redondeo HALF_UP

### Requirement: IVA

La aplicación SHALL permitir configurar tipos de IVA: tipos porcentuales e IVA exento. Para IVA exento SHALL poder indicarse un motivo o texto de exención. Cada línea SHALL poder tener un tipo de IVA diferente. Los tipos de IVA SHALL poder crearse, modificarse mientras sea seguro, marcarse como inactivos si ya se han utilizado y SHALL NOT eliminarse físicamente si forman parte del histórico. En la exportación a PDF, el resumen de la factura SHALL desglosar cada tipo de IVA por separado (base y cuota), y en el editor la aplicación SHALL mostrar la base total, el IVA total y el total general como valores separados. Los cálculos SHALL usar BigDecimal; no se permite usar double/float para importes monetarios.

#### Scenario: Líneas con distintos tipos de IVA
- **WHEN** una factura tiene líneas con tipos de IVA diferentes, incluida una exenta
- **THEN** el resumen muestra cada tipo por separado: base 21% e IVA 21%, base 10% e IVA 10%, y base exenta con IVA 0% y su motivo de exención

#### Scenario: Inactivar tipo de IVA usado
- **WHEN** el usuario intenta inactivar un tipo de IVA que ya aparece en facturas del histórico
- **THEN** el tipo pasa a inactivo, no se ofrece para nuevas facturas y el histórico se conserva intacto

### Requirement: Descuento global

La factura SHALL tener un descuento general que se aplica sobre toda la factura, no sobre cada línea. El descuento SHALL ser siempre porcentual, un porcentaje entero que empieza en 0%, sin descuento predeterminado configurable. Se SHALL aplicar antes del cálculo del IVA. Cuando existan varios tipos de IVA, el descuento SHALL repartirse correctamente entre las bases de cada tipo de IVA para mantener el desglose fiscal: cada base se reduce en el porcentaje de descuento y la suma de las bases descontadas cuadra exactamente con el total base descontado.

#### Scenario: Descuento con un solo tipo de IVA
- **WHEN** el usuario aplica un descuento del 10% a una factura con una sola base de IVA
- **THEN** la base descontada es el 90% de la base original y el IVA se calcula sobre esa base descontada

#### Scenario: Descuento con varios tipos de IVA
- **WHEN** el usuario aplica un descuento del 10% a una factura con líneas al 21% y al 10%
- **THEN** cada base de IVA se reduce al 90%, el desglose muestra las bases descontadas de cada tipo y la suma de bases descontadas es exacta frente al total base descontado

#### Scenario: Descuento predeterminado
- **WHEN** el usuario crea una nueva factura
- **THEN** el descuento general empieza en 0%

### Requirement: Numeración por series

La aplicación SHALL tener series de numeración. En una instalación nueva la aplicación SHALL NO crear ninguna serie por defecto: el listado de series comienza vacío y el usuario las crea a mano. Cada serie SHALL tener su propio correlativo. El correlativo SHALL ser la identidad de la factura; el componente de fecha (mes o año) SHALL recalcularse según la fecha y guardarse en cada versión. El correlativo de cada serie SHALL ser independiente por ejercicio: cada año de trabajo reinicia su propia cuenta sobre el siguiente número de ese año, sin afectar al correlativo de otros años. Las series SHALL poder crearse y configurarse desde la aplicación. La aplicación SHALL recordar la última serie utilizada y proponerla al crear la siguiente factura. El número SHALL proponerse automáticamente y poder modificarse manualmente. El número SHALL NOT consumirse hasta que la factura se guarda correctamente. Los números liberados por el borrado físico de facturas SHALL reutilizarse siempre antes de proponer un correlativo nuevo. SHALL ser configurable por serie el comportamiento con números anulados: continuar hacia delante o reutilizar números anulados; el comportamiento predeterminado SHALL ser continuar hacia delante. En Configuración → Series el usuario SHALL poder ver las series, ver el siguiente número y modificarlo.

Cada serie SHALL tener un campo \sufijo_fecha\ con tres opciones posibles: \MES\ (formato CODIGO-CORRELATIVO/MES o CORRELATIVO/MES si no hay código), \ANIO\ (formato CODIGO-CORRELATIVO-ANIO o CORRELATIVO-ANIO si no hay código) y \NINGUNO\ (formato CODIGO-CORRELATIVO o solo CORRELATIVO si no hay código). El campo \codigo\ de una serie SHALL poder estar vacío, en cuyo caso el número NO tendrá prefijo de letra; solo SHALL admitirse una serie sin código a la vez, de modo que otra serie en blanco se rechaza y se identifica la serie por su descripción. El formato predeterminado para series nuevas SHALL ser \MES\. La reutilización de números anulados y de números borrados SHALL operar dentro del mismo año: solo se reutilizan correlativos libres de ese ejercicio y solo se consideran ocupados los correlativos activos de ese ejercicio.

#### Scenario: Propuesta de número con formato MES (actual)
- **WHEN** el usuario crea una factura en la serie C con formato MES, fecha 11/08/2026 y el siguiente correlativo de 2026 es 58
- **THEN** la aplicación propone el número \C-58/8\

#### Scenario: Propuesta de número con formato ANIO sin código
- **WHEN** el usuario crea una factura en una serie sin código, formato ANIO, fecha 15/07/2026 y el siguiente correlativo de 2026 es 56
- **THEN** la aplicación propone el número \56-2026\

#### Scenario: Propuesta de número con formato ANIO con código
- **WHEN** el usuario crea una factura en la serie C con formato ANIO, fecha 15/07/2026 y el siguiente correlativo de 2026 es 56
- **THEN** la aplicación propone el número \C-56-2026\

#### Scenario: Propuesta de número con formato NINGUNO sin código
- **WHEN** el usuario crea una factura en una serie sin código, formato NINGUNO y el siguiente correlativo es 56
- **THEN** la aplicación propone el número \56\

#### Scenario: Propuesta de número con formato NINGUNO con código
- **WHEN** el usuario crea una factura en la serie R con formato NINGUNO y el siguiente correlativo es 1
- **THEN** la aplicación propone el número \R-1\

#### Scenario: El mes sigue a la fecha con formato MES
- **WHEN** el usuario cambia la fecha de la factura de julio a agosto y la guarda con formato MES
- **THEN** el número se guarda con el mes correspondiente a la nueva fecha (p. ej. \C-59/8\) y el correlativo no cambia

#### Scenario: El año sigue a la fecha con formato ANIO
- **WHEN** el usuario cambia la fecha de la factura de 2026 a 2027 y la guarda con formato ANIO
- **THEN** el número se guarda con el año correspondiente a la nueva fecha (p. ej. \56-2027\) y el correlativo no cambia

#### Scenario: Número manual duplicado
- **WHEN** el usuario introduce manualmente un número que ya pertenece a una factura activa de la misma serie
- **THEN** la aplicación impide guardar e informa del conflicto

#### Scenario: El número no se consume al abandonar
- **WHEN** el usuario cancela una factura sin guardarla
- **THEN** el número propuesto no queda consumido y el siguiente correlativo permanece

#### Scenario: Anulada sin restaurar por número ocupado
- **WHEN** el usuario intenta restaurar una factura anulada cuyo número está ocupado por otra factura activa de la misma serie y año
- **THEN** la aplicación impide la restauración e informa del motivo

#### Scenario: Configurar formato de serie
- **WHEN** el usuario crea o edita una serie en Configuración y cambia el formato
- **THEN** el ejemplo debajo del desplegable se actualiza mostrando el resultado con correlativo 56 y fecha 15/07/2026

#### Scenario: Instalación nueva sin series
- **WHEN** el usuario crea una empresa nueva y abre Configuración → Series
- **THEN** el listado de series está vacío y no existe ninguna serie creada por la instalación

#### Scenario: Series sin código
- **WHEN** el usuario crea una serie dejando el código en blanco
- **THEN** la serie se guarda sin prefijo y sus números usan solo el correlativo y el sufijo de fecha
- **AND** si ya existe otra serie sin código, la aplicación rechaza el guardado y pide distinguir las series por código o descripción

#### Scenario: Borrar serie sin facturas
- **WHEN** el usuario elimina una serie que no tiene ninguna factura
- **THEN** la serie desaparece del listado de series

#### Scenario: Borrar serie con facturas
- **WHEN** el usuario intenta eliminar una serie que tiene alguna factura, activa o histórica
- **THEN** la aplicación impide el borrado e informa de que la serie tiene facturas y el histórico no se elimina

#### Scenario: Correlativo independiente por año
- **WHEN** la serie C tiene un siguiente correlativo 58 para 2026 y el usuario arranca con fecha de trabajo en 2025 y crea una factura
- **THEN** la aplicación propone la primera factura de la serie C de 2025 con su correlativo propio de ese año, sin alterar el siguiente correlativo de 2026

#### Scenario: Reutilización de números borrados
- **WHEN** la serie C tiene en 2026 facturas activas con correlativos 1 y 3, y los correlativos 2 y 4 están registrados como disponibles tras borrar facturas
- **THEN** al crear una factura en 2026 la aplicación propone reutilizar el correlativo 2, el menor hueco libre

#### Scenario: Reutilización de anulados limitada al año
- **WHEN** la serie C tiene en 2025 una factura anulada con correlativo 5 y en 2026 una factura activa con correlativo 5, y la serie reutiliza números anulados
- **THEN** al crear una factura en 2025 la aplicación propone reutilizar el correlativo 5, que está libre solo dentro del ejercicio 2025

### Requirement: Fecha de trabajo

La fecha de trabajo SHALL fijarse en la pantalla de arranque de la aplicación junto con la selección de la empresa y el año del ejercicio fiscal. El usuario SHALL elegir el año del ejercicio en la pantalla de arranque. La fecha de trabajo define el mes y el año utilizados para la numeración de las nuevas facturas (formatos MES y ANIO) y SHALL usarse como valor inicial de la fecha de las nuevas facturas. Si el ejercicio elegido es el año en curso, la fecha de trabajo SHALL fijarse automáticamente a la fecha del sistema (no editable); si el ejercicio es otro año, la fecha de trabajo SHALL pedirse a mano y solo SHALL admitirse dentro de ese ejercicio. La fecha de trabajo SHALL poder cambiarse solo al arrancar de la aplicación. Al cambiar la fecha de una factura dentro del editor, el mes del número propuesto SHALL actualizarse automáticamente.

#### Scenario: Fecha inicial al abrir
- **WHEN** el usuario abre la aplicación
- **THEN** la pantalla de arranque propone el año del sistema como ejercicio fiscal y la última empresa utilizada queda preseleccionada; si el ejercicio es el actual, la fecha de trabajo es la fecha del sistema

#### Scenario: Ejercicio distinto al actual
- **WHEN** el usuario elige en la pantalla de arranque un ejercicio distinto al año en curso
- **THEN** debe indicar manualmente una fecha de trabajo dentro de ese ejercicio; no se admiten fechas fuera de él

#### Scenario: Cambio de fecha de trabajo
- **WHEN** el usuario cambia la fecha de trabajo en la pantalla de arranque y crea después una nueva factura
- **THEN** la factura se inicializa con esa fecha de trabajo

#### Scenario: La fecha de trabajo define mes y año
- **WHEN** el usuario arranca con fecha de trabajo 15/07/2025 y crea facturas en series con formato MES y formato ANIO
- **THEN** los números propuestos usan el mes 7 en el formato MES y el año 2025 en el formato ANIO

### Requirement: Versionado

Las facturas emitidas SHALL poder editarse. Si se edita la versión más reciente de una factura, al guardar la aplicación SHALL ofrecer dos caminos tras pedir confirmación: sobrescribir esa versión en su lugar manteniendo el mismo número de versión, o crear una nueva versión (vN+1) a partir de los datos editados dejando la versión más reciente intacta. Si se edita una versión anterior, al guardar SHALL crearse una nueva versión (vN+1) a partir de esa versión sin modificar la versión histórica. Cada versión SHALL guardar todos los datos completos de la factura, la fecha de factura y la fecha/hora en que se creó la versión. No SHALL ser necesario guardar un resumen de diferencias. Cualquier versión SHALL poder abrirse. Las versiones anteriores a la más reciente SHALL NOT modificarse nunca. Anular y restaurar una factura SHALL también crear una nueva versión.

#### Scenario: Guardar sobrescribe la versión actual
- **WHEN** el usuario modifica la última versión de una factura emitida, guarda y confirma la sobrescritura
- **THEN** la versión actual se sobrescribe con los cambios y el número de versión permanece

#### Scenario: Guardar como nueva versión
- **WHEN** el usuario modifica la última versión de una factura emitida y elige «Guardar como nueva versión»
- **THEN** se crea una nueva versión (vN+1) con los cambios y la versión anterior permanece intacta
- **AND** ambas versiones aparecen en el Histórico como filas independientes y pueden exportarse por separado

#### Scenario: Cancelar el guardado
- **WHEN** el usuario modifica la última versión y cancela el diálogo de guardado
- **THEN** no se crea ni se modifica ninguna versión

#### Scenario: Editar una versión anterior
- **WHEN** el usuario abre la versión v1 de una factura, la modifica y guarda
- **THEN** se crea una nueva versión a partir de v1 y la versión v1 no se modifica

#### Scenario: Anular genera versión
- **WHEN** el usuario anula una factura
- **THEN** se crea una nueva versión que refleja el estado Anulada

### Requirement: Estados de factura

Una factura SHALL tener uno de dos estados: Emitida o Anulada. No SHALL existir estado Borrador en la V1. Una factura Emitida SHALL poder editarse. Una factura Anulada SHALL NOT poder editarse, SHALL poder consultarse, SHALL poder exportarse a PDF y SHALL poder restaurarse a Emitida. Anular y restaurar SHALL requerir confirmación. La restauración de una versión emitida anterior de una factura anulada SHALL crear una nueva versión Emitida.

#### Scenario: Consultar factura anulada
- **WHEN** el usuario abre una factura anulada
- **THEN** la factura se muestra en modo consulta, sin permitir edición, y aparece marcada como anulada

#### Scenario: Restaurar factura anulada
- **WHEN** el usuario confirma la restauración de una factura anulada cuyo número está libre
- **THEN** se crea una nueva versión en estado Emitida

### Requirement: Rectificativas

La aplicación SHALL permitir crear facturas rectificativas usando la serie independiente R (`R-1`, `R-2`, ...), que no distinguen entre cocina y puerta. Las rectificativas SHALL crearse desde una factura existente; no SHALL existir una opción independiente de "Nueva rectificativa" en el menú principal. Al crear una rectificativa SHALL copiarse los datos de la factura original: cliente, líneas, cantidades, descripciones, precios, IVA, descuento y observaciones. La rectificativa SHALL indicar qué factura rectifica mediante una referencia que se genera automáticamente y puede modificarse manualmente. La fecha de una rectificativa SHALL inicializarse con la fecha de trabajo actual y poder cambiarse. Una rectificativa SHALL poder ser parcial o total. Una rectificativa SHALL poder rectificar a otra rectificativa.

#### Scenario: Crear rectificativa desde factura
- **WHEN** el usuario crea una rectificativa desde una factura C-59/8
- **THEN** se crea una factura en la serie R con los datos copiados y la referencia a C-59/8 generada automáticamente

#### Scenario: Modificar referencia
- **WHEN** el usuario modifica manualmente la referencia de la rectificativa
- **THEN** la aplicación guarda la referencia indicada

### Requirement: Histórico

La aplicación SHALL tener un histórico de facturas que muestre cada versión como una fila independiente. El histórico SHALL permitir buscar por serie, cliente/razón social, NIF, fecha desde/hasta, importe desde/hasta y estado, combinando los filtros entre sí. La búsqueda SHALL ejecutarse mediante un botón "Buscar", no en tiempo real. Los resultados SHALL ordenarse por número de factura. Las columnas SHALL ser: fecha, número, versión, cliente, NIF, base, IVA, total y estado. Al seleccionar una fila SHALL poder abrirse esa factura/versión. La tabla SHALL permitir seleccionar varias filas a la vez. El histórico SHALL ofrecer exportar directamente a PDF las filas seleccionadas sin necesidad de abrir la factura: con una selección se generará un único PDF preguntando dónde guardarlo; con varias selecciones se elegirá una carpeta de destino y se generarán todos los PDF en esa carpeta con sus nombres propuestos, informando al finalizar del resultado de cada generación.

#### Scenario: Búsqueda combinando filtros
- **WHEN** el usuario establece una serie, un cliente y un rango de fechas y pulsa Buscar
- **THEN** se muestran todas las versiones de facturas que cumplen los tres filtros

#### Scenario: Búsqueda sin límites de importe
- **WHEN** el usuario deja vacíos los campos de importe desde y hasta y pulsa Buscar
- **THEN** la aplicación no aplica ningún límite de importe y muestra también las facturas con total mayor que cero

#### Scenario: Apertura desde el histórico
- **WHEN** el usuario selecciona una fila del histórico
- **THEN** se abre la factura en la versión correspondiente

#### Scenario: Selección múltiple en la tabla

- **WHEN** el usuario mantiene Ctrl o Shift mientras hace clic sobre filas del histórico
- **THEN** quedan seleccionadas simultáneamente todas las filas marcadas

#### Scenario: Exportar una versión seleccionada

- **WHEN** el usuario selecciona una única fila del histórico y pulsa Exportar PDF
- **THEN** la aplicación propone guardar un PDF con el nombre propuesto para esa factura y lo genera sin abrir el editor

#### Scenario: Exportar varias versiones en lote

- **WHEN** el usuario selecciona varias filas del histórico y pulsa Exportar PDF
- **THEN** la aplicación pide una carpeta de destino una sola vez y genera en ella un PDF por cada fila seleccionada con su nombre propuesto
- **AND** al terminar informa cuántos PDF se generaron correctamente y cuáles fallaron

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

### Requirement: Cambios sin guardar

Si hay cambios sin guardar, la aplicación SHALL ofrecer tres opciones: Guardar y volver/salir, Descartar cambios y volver/salir, y Cancelar. Si no hay cambios, la aplicación SHALL permitir salir normalmente.

La confirmación SHALL pedirse ante **cualquier** navegación que abandone una vista con cambios sin guardar, no solo al pulsar Volver o al cerrar la aplicación: los botones de la barra de navegación, las entradas del menú principal y la apertura de una factura desde el Histórico o desde Versiones SHALL pasar por la misma confirmación. Si el usuario cancela, la aplicación SHALL permanecer en la vista actual sin cambiar de pantalla.

La confirmación SHALL mostrarse **una sola vez** por cada gesto del usuario.

#### Scenario: Cerrar con cambios sin guardar
- **WHEN** el usuario intenta volver o cerrar con cambios sin guardar
- **THEN** se muestra la confirmación con las opciones Guardar, Descartar o Cancelar

#### Scenario: Navegar desde la barra con cambios sin guardar
- **WHEN** el usuario tiene una factura a medias y pulsa un botón de la barra de navegación (Menú principal, Histórico, Clientes, Configuración o Copia de seguridad)
- **THEN** se muestra la confirmación de cambios sin guardar antes de cambiar de pantalla

#### Scenario: Cancelar la navegación
- **WHEN** el usuario elige Cancelar en la confirmación de cambios sin guardar
- **THEN** la aplicación permanece en la vista actual con sus datos intactos y no se carga la pantalla destino

#### Scenario: La confirmación no se repite
- **WHEN** el usuario pulsa Volver o Nueva factura en el editor con cambios sin guardar
- **THEN** la confirmación aparece una sola vez, y al elegir Guardar la factura se guarda una sola vez antes de navegar

### Requirement: Iconos en los diálogos de aviso

Cada diálogo de aviso de la aplicación SHALL mostrar el icono correspondiente a su tipo, de modo que el usuario identifique de un vistazo la índole del aviso. Los diálogos de información SHALL mostrar un icono de información; los diálogos de error SHALL mostrar un icono de alerta/error; y los diálogos de confirmación (incluidos los de cambios sin guardar y de guardar versión) SHALL mostrar un icono de pregunta/confirmación. El icono SHALL dibujarse con el color de acento del tema de apariencia activo para integrarse en cualquiera de los temas. La ventana de cada diálogo de aviso SHALL mostrar además el icono de aplicación de la marca en su barra de título y en la barra de tareas de Windows, igual que el resto de ventanas de la aplicación. Los diálogos SHALL conservar su título, mensaje y botones actuales; la adición de los iconos no SHALL alterar el flujo de confirmación/cancelación ni el contenido textual.

#### Scenario: Diálogo de información con icono de información
- **WHEN** la aplicación muestra un diálogo de información
- **THEN** el diálogo muestra el icono de información junto al mensaje

#### Scenario: Diálogo de error con icono de alerta
- **WHEN** la aplicación muestra un diálogo de error
- **THEN** el diálogo muestra el icono de alerta/error junto al mensaje

#### Scenario: Diálogo de confirmación con icono de pregunta
- **WHEN** la aplicación muestra un diálogo de confirmación (incluidos cambios sin guardar y guardar versión)
- **THEN** el diálogo muestra el icono de pregunta/confirmación junto al mensaje

#### Scenario: Icono con el color de acento del tema
- **WHEN** la aplicación muestra un diálogo de aviso con el tema de apariencia activo
- **THEN** el icono se dibuja con el color de acento de ese tema

#### Scenario: Icono de aplicación en la ventana del diálogo
- **WHEN** la aplicación muestra un diálogo de aviso
- **THEN** la barra de título y la barra de tareas de Windows de esa ventana muestran el icono de aplicación de la marca

### Requirement: Pantalla de generación mensual con estética alineada

La pantalla de generación de facturas mensuales SHALL presentar la misma estética visual que el resto de la aplicación. El título de la pantalla SHALL distinguirse con el mismo estilo de título del resto de ventanas, mostrándose con el tamaño de título y el color de texto del tema de apariencia activo. Las etiquetas del formulario (cliente, serie, año, mes de inicio, mes de fin, día del mes, tipo de IVA y retención de IRPF) SHALL mostrarse con una separación clara respecto a los campos y a los bordes del panel, sin quedar pegadas, y SHALL usar el mismo estilo de etiqueta de formulario que el resto de pantallas. El panel que contiene el formulario SHALL usar un fondo neutro acorde con la ventana (no un fondo blanco plano contrastado) y coherente con el resto de pantallas de la aplicación. La pantalla SHALL conservar los mismos campos, controles, botones y flujo de generación actuales; la modificación es exclusivamente de apariencia.

#### Scenario: Título destacado con el color del tema
- **WHEN** la aplicación muestra la pantalla de generación de facturas mensuales
- **THEN** el título «Generar facturas mensuales» se muestra con el mismo estilo destacado (tamaño de título y color de texto del tema) que los títulos del resto de pantallas

#### Scenario: Etiquetas separadas de los campos y del borde
- **WHEN** la aplicación muestra el formulario de la pantalla de generación mensual
- **THEN** cada etiqueta (cliente, serie, año, mes de inicio, mes de fin, día del mes, tipo de IVA, retención de IRPF) se muestra con separación clara respecto al borde del panel y a su campo, usando el estilo de etiqueta de formulario del resto de pantallas

#### Scenario: Panel con fondo neutro
- **WHEN** la aplicación muestra el panel del formulario de la pantalla de generación mensual
- **THEN** el panel se muestra con un fondo neutro del tema acorde con la ventana, y no como un bloque blanco plano contrastado

#### Scenario: Los campos y el flujo se mantienen
- **WHEN** el usuario abre la pantalla de generación mensual tras el cambio de apariencia
- **THEN** los mismos campos, controles, botones y el flujo de generación siguen disponibles y se comportan igual que antes

### Requirement: Atajos de teclado

La aplicación SHALL proporcionar los atajos Ctrl+N para Nueva factura, Ctrl+S para Guardar, Ctrl+F para Buscar, Ctrl+P para Exportar y Esc para volver/cancelar cuando corresponda.

#### Scenario: Guardar con atajo
- **WHEN** el usuario pulsa Ctrl+S en una factura abierta
- **THEN** la factura se guarda, sobrescribiendo la versión actual o creando una nueva versión según corresponda

### Requirement: Configuración

La aplicación SHALL tener una pantalla de Configuración que permita configurar: los datos de la empresa (nombre, NIF, dirección, código postal, localidad, provincia y resto de datos necesarios para la cabecera); la cabecera del documento en dos modos, texto con datos de empresa o imagen/logo; el pie con texto legal libre configurable por el usuario; el tema de apariencia de la interfaz; los tipos de IVA; los tipos de retención de IRPF; las series (crear/configurar, ver y modificar el siguiente número, configurar la reutilización de números anulados y eliminar series sin facturas); las carpetas de PDF; el color de acento usado en los PDF exportados; y la gestión de empresas (ver el listado de empresas, crear una nueva, cambiar a otra y eliminar una empresa distinta de la actual). El color SHALL guardarse como preferencia `color_pdf`; si nunca se configura, los PDF SHALL usar arena Alcazaba (`#B08D57`), y del color elegido SHALL derivarse el resto de tonos del documento. La aplicación SHALL recordar preferencias de trabajo: última serie utilizada, tamaño/posición de ventana, última carpeta de exportación y tema de apariencia. El tamaño/posición de ventana y el tema SHALL guardarse de forma global, compartidos entre empresas.

En el tamaño mínimo de ventana (1024×768), todos los campos de la sección Empresa SHALL verse completos, sin recortes por el borde derecho.

#### Scenario: Configurar empresa
- **WHEN** el usuario guarda los datos de la empresa en Configuración
- **THEN** esos datos se usan en las nuevas exportaciones a PDF

#### Scenario: Elegir modo de cabecera
- **WHEN** el usuario selecciona un logo
- **THEN** el PDF usa el logo como cabecera y los datos de empresa permanecen guardados

#### Scenario: Pie legal configurable
- **WHEN** el usuario modifica el texto legal del pie
- **THEN** el nuevo texto se repite en las páginas de los PDF generados

#### Scenario: Modificar siguiente número
- **WHEN** el usuario modifica el siguiente número de una serie en Configuración
- **THEN** la próxima factura de esa serie se propone a partir del nuevo valor

#### Scenario: Cambiar el tema de la interfaz
- **WHEN** el usuario selecciona un tema en Configuración y guarda
- **THEN** el tema se aplica de inmediato y queda guardado para las siguientes sesiones

#### Scenario: Cambiar el color del PDF
- **WHEN** el usuario elige un color en Configuración y guarda
- **THEN** los nuevos PDF usan ese color de acento y sus tonos derivados
- **AND** si se restablece el valor por defecto o la preferencia no existe, se usa `#B08D57`

#### Scenario: Gestionar empresas desde Configuración
- **WHEN** el usuario abre la pestaña Empresas de Configuración
- **THEN** ve el listado de empresas disponibles con su nombre y puede crear, cambiar o eliminar empresas

#### Scenario: Configurar tipos de retención
- **WHEN** el usuario añade un tipo de retención del 15% con nombre "IRPF 15%" en Configuración
- **THEN** ese tipo queda disponible para seleccionar en las facturas de esa empresa

#### Scenario: Campos de empresa legibles a 1024×768
- **WHEN** el usuario abre la sección Empresa de Configuración en el tamaño mínimo de ventana
- **THEN** los campos Nombre / razón social, Actividad y Localidad se ven enteros, sin recortes

### Requirement: Configuración organizada por secciones

La pantalla de Configuración SHALL organizarse en secciones navegables desde una lista lateral, en lugar de pestañas. Las secciones SHALL ser: Empresa, Cabecera y pie, PDF y apariencia, IVA, Retenciones, Series y Empresas. El botón de guardado general SHALL mostrarse únicamente en las secciones cuyos datos guarda (Empresa, Cabecera y pie, y PDF y apariencia) y SHALL ocultarse en las secciones que se administran fila a fila (IVA, Retenciones, Series y Empresas), donde cada una conserva sus propias acciones. El tema de la aplicación SHALL presentarse en la sección PDF y apariencia, por tratarse de una preferencia global compartida entre empresas.

#### Scenario: Navegación entre secciones
- **WHEN** el usuario selecciona una sección en la lista lateral
- **THEN** el contenido de esa sección ocupa la zona derecha y el resto de secciones queda oculto

#### Scenario: El botón de guardado solo donde aplica
- **WHEN** el usuario abre las secciones de IVA, Retenciones, Series o Empresas
- **THEN** el botón de guardado general no se muestra, y las acciones disponibles son las propias de la sección

#### Scenario: Cada sección cabe sin scroll
- **WHEN** el usuario recorre las secciones con la ventana en su tamaño mínimo de 1024×768
- **THEN** el contenido de cada sección es visible completo sin necesidad de desplazarse

### Requirement: Retención de IRPF

La aplicación SHALL permitir aplicar una retención de IRPF a las facturas. La empresa SHALL poder configurar una lista de tipos de retención (nombre y porcentaje), gestionada de forma similar a los tipos de IVA. Cada factura SHALL poder seleccionar un tipo de retención configurado o ninguno. La retención SHALL calcularse sobre la **base imponible** de la factura, es decir, sobre la misma base sobre la que se calcula el IVA, después de aplicar el descuento global. El total de la factura SHALL ser `Base − Descuento + IVA − Retención`. La retención seleccionada y su importe SHALL guardarse en cada versión de la factura. Si no se selecciona ningún tipo de retención, el comportamiento SHALL ser el actual: `Total = Base − Descuento + IVA`. El total de una factura SHALL NOT ser negativo por efecto de la retención.

#### Scenario: Factura con retención del 15%
- **WHEN** el usuario crea una factura con base 1.000,00 €, descuento 0 %, IVA 21 % y selecciona una retención del 15 %
- **THEN** el importe de retención es 150,00 € y el total es 1.060,00 €

#### Scenario: Factura con descuento y retención
- **WHEN** el usuario crea una factura con base 1.000,00 €, descuento global del 10 %, IVA 21 % y retención del 15 %
- **THEN** la base imponible es 900,00 €, el IVA es 189,00 €, la retención es 135,00 € (el 15 % de 900,00) y el total es 954,00 €

#### Scenario: La retención usa la misma base que el IVA
- **WHEN** una factura tiene descuento global y retención
- **THEN** el importe de retención se calcula sobre la base imponible descontada, la misma sobre la que se calcula la cuota de IVA, de modo que el porcentaje de retención que se deduce del resumen coincide con el tipo seleccionado

#### Scenario: Descuento del 100 % con retención
- **WHEN** el usuario crea una factura con base 1.000,00 €, descuento global del 100 % y una retención del 15 %
- **THEN** la base imponible es 0,00 €, el IVA es 0,00 €, la retención es 0,00 € y el total es 0,00 €, nunca un importe negativo

#### Scenario: Factura sin retención
- **WHEN** el usuario crea una factura y no selecciona ningún tipo de retención
- **THEN** el cálculo del total no incluye retención y el comportamiento es el actual

#### Scenario: Seleccionar tipo de retención en el editor
- **WHEN** el usuario abre el editor de una factura nueva
- **THEN** puede elegir entre los tipos de retención configurados para la empresa o dejar la factura sin retención

### Requirement: Retención en histórico

El histórico de facturas SHALL mostrar el importe de retención de cada versión en una columna propia. Cuando una factura no tiene retención, la columna SHALL mostrar un valor vacío o cero según el criterio de la interfaz.

#### Scenario: Histórico con retención
- **WHEN** el usuario busca en el histórico facturas con y sin retención
- **THEN** la columna de retención muestra el importe correspondiente a cada versión

### Requirement: Retención en rectificativas

Al crear una rectificativa desde una factura, la aplicación SHALL copiar el tipo de retención de la factura original. El usuario SHALL poder modificar o quitar la retención en la rectificativa antes de guardarla. El cálculo del total de la rectificativa SHALL aplicar la misma fórmula de retención sobre la base imponible.

#### Scenario: Rectificativa hereda retención
- **WHEN** el usuario crea una rectificativa desde una factura que tiene una retención del 15 %
- **THEN** la rectificativa se crea con el mismo tipo de retención del 15 %, editable antes de guardar

### Requirement: Persistencia local

La aplicación SHALL guardar los datos de cada empresa en una base de datos SQLite local dedicada ubicada en una carpeta de datos de la aplicación, separada de la instalación. Cada empresa SHALL tener su propia base de datos: los datos de una empresa SHALL NOT mezclarse con los de otra. La aplicación SHALL ser la vía normal para modificar los datos. La base de datos de la empresa activa SHALL contener su histórico completo y su configuración de empresa. Las operaciones importantes de persistencia SHALL ser transaccionales con confirmación y reversión correctas. No SHALL eliminarse físicamente datos históricos que hayan sido utilizados. La aplicación SHALL ejecutarse como una única instancia a la vez.

#### Scenario: Guardado transaccional
- **WHEN** el usuario guarda una factura con sus líneas y su versión
- **THEN** la operación se confirma de forma atómica o se revierte por completo si falla

#### Scenario: Segunda instancia
- **WHEN** el usuario intenta abrir una segunda instancia de la aplicación
- **THEN** la segunda instancia no se abre y se notifica al usuario

#### Scenario: Aislamiento entre empresas
- **WHEN** el usuario crea facturas en la empresa A, cambia a la empresa B y abre el histórico
- **THEN** en la empresa B no aparecen las facturas de la empresa A

### Requirement: Gestión de empresas

La aplicación SHALL permitir gestionar varias empresas con datos totalmente aislados. Cada empresa SHALL tener un nombre visible y un identificador interno (slug) que da nombre a su carpeta de datos. La aplicación SHALL listar las empresas disponibles, crear nuevas empresas, cambiar a una empresa existente y eliminar una empresa distinta de la actual. Al crear una empresa nueva la aplicación SHALL crear su base de datos desde cero con la estructura de tablas completa, sin interrumpir la empresa que esté en uso. La aplicación SHALL recordar de forma global la última empresa utilizada y SHALL preseleccionarla al abrir. Cada empresa SHALL tener su propia configuración de empresa, clientes, series, tipos de IVA y facturas. La empresa actualmente activa SHALL NOT poder eliminarse. Cambiar de empresa SHALL ser siempre una acción explícita del usuario; crear una empresa desde Configuración SHALL NOT cambiar la empresa activa, ni la conexión en curso, ni la última empresa recordada.

#### Scenario: Crear una nueva empresa
- **WHEN** el usuario crea la empresa «Asesoría María Luisa Ibáñez» desde Configuración
- **THEN** se crea su base de datos con la estructura completa y la empresa aparece en el listado
- **AND** la empresa activa no cambia: los datos que se siguen viendo y editando son los de la empresa anterior

#### Scenario: Nombre visible e identificador interno
- **WHEN** el usuario crea una empresa con nombre «Asesoría María Luisa Ibáñez»
- **THEN** el listado muestra el nombre visible y su carpeta de datos usa el identificador interno correspondiente

#### Scenario: Cambiar a otra empresa
- **WHEN** el usuario elige cambiar a una empresa distinta de la actual
- **THEN** la aplicación pasa a usar la base de datos de esa empresa y sus datos (configuración, clientes, series, facturas) sustituyen a los de la anterior

#### Scenario: Crear y aceptar el cambio
- **WHEN** el usuario crea una empresa desde Configuración y acepta el ofrecimiento de cambiar a ella
- **THEN** la aplicación pasa a la empresa nueva y recarga el menú principal

#### Scenario: Eliminar empresa no actual
- **WHEN** el usuario elimina una empresa distinta de la actual y confirma
- **THEN** la empresa desaparece del listado de disponibles

#### Scenario: La empresa activa no se elimina
- **WHEN** el usuario intenta eliminar la empresa actualmente en uso
- **THEN** la aplicación no permite eliminarla

#### Scenario: Última empresa preseleccionada
- **WHEN** el usuario abre la aplicación después de haber usado «Comercial Alcazaba»
- **THEN** «Comercial Alcazaba» aparece seleccionada por defecto en la pantalla de arranque

#### Scenario: Crear empresa desde el arranque
- **WHEN** el usuario usa la opción de crear empresa de la pantalla de arranque
- **THEN** se crea la empresa, aparece en el listado y queda seleccionada sin salir de la pantalla de arranque

### Requirement: Copia de seguridad

La aplicación SHALL tener un botón para crear una copia de seguridad manual. En la V1 la copia SHALL ser únicamente del archivo SQLite; no se incluyen PDFs ni configuración. La aplicación SHALL permitir restaurar una copia de seguridad desde la misma pantalla. Antes de restaurar, la aplicación SHALL mostrar un resumen del contenido del archivo (empresa, NIF, número de facturas, última fecha y versión de esquema). La aplicación SHALL validar la copia antes de sustituir nada: rechazará archivos que no sean bases de datos válidas de la aplicación, que no contengan las tablas fundamentales de la aplicación ni que sean la propia base activa.

La aplicación SHALL decidir si acepta una copia por **su estructura**, no por su número de versión de esquema. Una copia cuya versión no coincida con la de la aplicación SHALL aceptarse si contiene todas las tablas y columnas que la aplicación necesita, avisando antes de continuar; SHALL rechazarse en caso contrario. El aviso y el rechazo SHALL decir si la versión de la copia es anterior o posterior a la de la aplicación, y SHALL NOT describir como más nueva una copia cuyo número de versión sea mayor por proceder de un historial de migraciones distinto.

Cuando la copia sea de una versión anterior dentro del historial de migraciones que la aplicación conserva, SHALL aplicársele las migraciones pendientes al restaurarla. Tras restaurar, la base SHALL quedar en un estado utilizable sin que el usuario tenga que hacer nada más.

Antes de restaurar, la aplicación SHALL guardar automáticamente una copia de rescate del estado previo de la empresa activa. La aplicación SHALL permitir restaurar sobre la empresa activa o crear una nueva empresa a partir de la copia. La aplicación SHALL NOT permitir sobrescribir una empresa con los datos de otra con NIF distinto; en ese caso solo se ofrecerá crear una empresa nueva. Si el logo referenciado en la copia no existe en la máquina, la aplicación SHALL avisar y continuar sin bloquear.

#### Scenario: Crear copia de seguridad
- **WHEN** el usuario pulsa el botón de copia de seguridad y elige dónde guardarla
- **THEN** se genera una copia del archivo SQLite en la ubicación elegida

#### Scenario: Restaurar sobre la empresa activa
- **WHEN** el usuario selecciona un archivo de copia, elige «Reemplazar la empresa activa» y confirma
- **THEN** los datos de la copia sustituyen a los de la empresa activa y la aplicación vuelve al menú principal

#### Scenario: Copia de rescate automática
- **WHEN** el usuario restaura una copia sobre la empresa activa
- **THEN** antes de restaurar queda guardada una copia del estado previo en la subcarpeta `copias_previas`

#### Scenario: La base restaurada queda utilizable
- **WHEN** el usuario restaura una copia de seguridad
- **THEN** la base resultante queda a la última versión de esquema que la aplicación conoce, lista para usarse sin pasos adicionales

#### Scenario: Restaurar una copia de esquema anterior
- **WHEN** el usuario restaura una copia cuya versión de esquema es anterior a la de la aplicación dentro del historial de migraciones que la aplicación conserva
- **THEN** la aplicación la acepta, la restaura y le aplica las migraciones pendientes, quedando al esquema actual

#### Scenario: Archivo sin las tablas fundamentales
- **WHEN** el usuario selecciona un archivo que no contiene las tablas fundamentales de la aplicación
- **THEN** la aplicación rechaza el archivo e informa del error sin tocar los datos

#### Scenario: Crear empresa nueva desde una copia
- **WHEN** el usuario selecciona un archivo de copia, elige «Crear una empresa nueva con estos datos», introduce un nombre y confirma
- **THEN** se crea la empresa con los datos de la copia y la empresa activa no cambia hasta que el usuario acepta el cambio

#### Scenario: Backup con NIF distinto
- **WHEN** el NIF del archivo de copia no coincide con el NIF de la empresa activa
- **THEN** la opción «Reemplazar la empresa activa» se deshabilita y solo se ofrece crear una empresa nueva

#### Scenario: Empresa activa vacía sin NIF
- **WHEN** la empresa activa no tiene NIF configurado y no tiene ninguna factura, y el usuario restaura una copia con NIF distinto
- **THEN** se permite reemplazar la empresa activa aunque el NIF no coincida

#### Scenario: Archivo que no es una copia válida
- **WHEN** el usuario selecciona un archivo que no es una base de datos SQLite válida de la aplicación
- **THEN** la aplicación rechaza el archivo e informa del error sin tocar los datos

#### Scenario: Copia de esquema posterior con las mismas tablas
- **WHEN** el archivo de copia tiene una versión de esquema superior pero contiene todas las tablas y columnas que la aplicación conoce
- **THEN** la aplicación acepta la copia y avisa de la diferencia de versión antes de continuar
- **AND** el aviso no afirma que la copia proceda de una versión más nueva de la aplicación cuando su número mayor solo refleja un historial de migraciones distinto

#### Scenario: Copia de esquema posterior con tablas distintas
- **WHEN** el archivo de copia tiene una versión de esquema superior y falta alguna tabla o columna que la aplicación necesita
- **THEN** la aplicación rechaza la copia e informa de qué falta, indicando si su número de versión es anterior o posterior

#### Scenario: Logo del backup inexistente
- **WHEN** la copia referencia un archivo de logo que no existe en la máquina actual
- **THEN** la aplicación avisa de que el logo no se encontrará pero permite continuar con la restauración

#### Scenario: Dos copias seguidas dentro del mismo segundo
- **WHEN** el usuario crea dos copias de seguridad en menos de un segundo
- **THEN** ambas se crean correctamente con nombres distintos sin error de colisión

### Requirement: Temas y apariencia

La aplicación SHALL disponer de un sistema de temas de apariencia de la interfaz compuesto por una hoja de estilos base común y temas de colores. La aplicación SHALL ofrecer los temas biblioteca8 (predeterminado), omarchy, esmeralda, terracota, negro-dorado, sakura y neon. El tema activo SHALL aplicarse a cada pantalla al cargarla, SHALL poder cambiarse desde Configuración y SHALL recordarse entre sesiones.

#### Scenario: Cambiar el tema al vuelo
- **WHEN** el usuario selecciona un tema distinto en Configuración
- **THEN** la interfaz cambia de tema de inmediato

#### Scenario: Tema recordado entre sesiones
- **WHEN** el usuario reinicia la aplicación después de guardar un tema en Configuración
- **THEN** el tema guardado se aplica en todas las pantallas

#### Scenario: Tema predeterminado
- **WHEN** el usuario abre la aplicación sin un tema guardado
- **THEN** se aplica el tema biblioteca8

### Requirement: Identidad de empresa en la interfaz

El menú principal SHALL mostrar el nombre, el NIF y el logo de la empresa configurados. El editor de factura SHALL mostrar el logo de la empresa en su cabecera. Los datos mostrados SHALL tomarse de la configuración de empresa. El recuadro que envuelve al logo (fondo y borde) SHALL rellenarse con los colores del propio logo según su tipo de imagen, de modo que imagen y recuadro se vean como una sola pieza sea cual sea el tema. El recuadro SHALL conservar sus esquinas redondeadas y el grosor de borde que define el tema. Si el logo es principalmente transparente (PNG con canal alfa), el recuadro SHALL mantener el fondo y borde del tema sin cambios. En el editor, el logo SHALL quedar contenido en una caja de tamaño fijo dentro de la cabecera.

#### Scenario: Mostrar datos de empresa en el menú principal
- **WHEN** el usuario abre el menú principal con datos de empresa configurados
- **THEN** se muestran el nombre, el NIF y el logo de la empresa

#### Scenario: Mostrar logo en el editor
- **WHEN** el usuario abre una factura con un logo configurado
- **THEN** el logo de la empresa aparece en la cabecera del editor contenido en una caja de tamaño fijo

#### Scenario: Rellenar el recuadro con un fondo plano y opaco
- **WHEN** el logo tiene un fondo plano y opaco, como un fondo blanco o de un color uniforme
- **THEN** el recuadro adopta ese color exacto tanto en el fondo como en el borde

#### Scenario: Rellenar el recuadro con un fondo difuminado
- **WHEN** el logo es opaco pero sin fondo plano, como una fotografía o un degradado
- **THEN** el recuadro se rellena con una copia ampliada y desenfocada de la propia imagen, sin que el desenfoque se salga del recuadro

#### Scenario: Dejar intacto el recuadro de un logo transparente
- **WHEN** el logo es principalmente transparente
- **THEN** el recuadro mantiene el fondo y el borde del tema, sin cambios

### Requirement: Ventana

La aplicación SHALL abrir su ventana con las siguientes medidas: en la primera ejecución (sin preferencias de ventana guardadas) SHALL medir 1024x768 y SHALL quedar centrada en la pantalla principal; en ejecuciones posteriores SHALL restaurar la posición y, como máximo, el tamaño guardados de la última sesión, sin bajar nunca de 1024x768 en las vistas principales. El tamaño mínimo de las vistas principales SHALL ser 1024x768 y el usuario SHALL poder redimensionar hasta ese mínimo. Ningún tamaño de ventana guardado inferior a 1024x768 SHALL hacer que una vista principal se muestre recortada: la aplicación SHALL corregirlo al entrar en el menú. Con la ventana en su tamaño mínimo, ninguna pantalla SHALL recortar ni ocultar controles: los filtros del Histórico y las filas de alta rápida de IVA y Series en Configuración SHALL reorganizarse en varias líneas cuando el ancho no baste, manteniendo cada grupo de botones de acción unido, y los campos de la cabecera del Editor SHALL repartirse el ancho disponible. El arranque (selección de empresa) es una pantalla fija pequeña de 760x520; al pasar de ella a una vista principal con tamaño mínimo 1024x768 con la ventana ya visible, la aplicación SHALL hacer crecer la ventana hasta ese mínimo.

#### Scenario: Primera ejecución abre a 1024x768 centrada
- **WHEN** el usuario inicia la aplicación sin preferencias de ventana guardadas
- **THEN** la ventana mide 1024x768 y aparece centrada en la pantalla principal

#### Scenario: Siguientes ejecuciones restauran la última sesión
- **WHEN** el usuario cierra la aplicación tras moverla o redimensionarla y vuelve a abrirla
- **THEN** la ventana recupera la posición y el tamaño (nunca inferior a 1024x768 en las vistas principales) de la sesión anterior

#### Scenario: Tamaño guardado inferior al mínimo
- **WHEN** la aplicación encuentra un tamaño de ventana guardado inferior a 1024x768
- **THEN** al entrar en el menú la ventana se corrige a 1024x768 y no se muestra recortada

#### Scenario: Mínimo de redimensionado
- **WHEN** el usuario arrastra el borde de la ventana para hacerla más pequeña
- **THEN** la ventana no puede bajar de 1024x768

#### Scenario: Filtros del Histórico con ventana mínima
- **WHEN** la ventana está al mínimo 1024x768 y se abre el Histórico
- **THEN** todos los filtros siguen visibles reorganizados en varias líneas y los botones Exportar PDF, Buscar y Volver permanecen accesibles

#### Scenario: Altas rápidas de IVA y Series con ventana mínima
- **WHEN** la ventana está al mínimo 1024x768 y se abren las pestañas IVA o Series de Configuración
- **THEN** los campos de alta se reorganizan sin cortarse y los botones Nuevo, Guardar e Inactivar/Activar (o Nuevo y Guardar en Series) permanecen visibles y agrupados

#### Scenario: Cabecera del Editor con ventana mínima
- **WHEN** la ventana está al mínimo 1024x768 y se abre una factura
- **THEN** los campos de la cabecera se reparten el ancho disponible sin salirse de la ventana

#### Scenario: Corrección al navegar desde una vista pequeña
- **WHEN** la aplicación pasa de la pantalla de arranque (760x520) al menú u otra vista principal con tamaño mínimo 1024x768 con la ventana ya visible
- **THEN** la ventana crece a 1024x768 (o hasta el mínimo de la vista de destino) al cargar la vista, sin necesidad de redimensionar o maximizar manualmente

### Requirement: Identidad de la aplicación en la interfaz

La aplicación SHALL mostrar un icono de aplicación propio en cada una de sus ventanas. El icono SHALL aplicarse a la ventana principal y a las ventanas secundarias (`Stage`) que la aplicación abre, de modo que se vea en la barra de tareas, en la esquina de la ventana y en la vista minimizada. La ventana principal SHALL mostrarse siempre con un título compuesto por la marca «CaboFactu®», un espacio y el nombre de la pantalla activa. Las ventanas secundarias SHALL mostrar el mismo prefijo de marca delante de su propio título («CaboFactu® » + título).

#### Scenario: Icono en la ventana principal
- **WHEN** la aplicación inicia su ventana principal
- **THEN** la ventana muestra el icono de aplicación en su barra de título, en la barra de tareas de Windows y en la vista minimizada

#### Scenario: Icono en ventanas secundarias
- **WHEN** la aplicación abre una ventana secundaria de tipo `Stage` (p. ej. el diálogo «Generar facturas mensuales»)
- **THEN** esa ventana muestra el mismo icono de aplicación en su barra de título y en la barra de tareas de Windows

#### Scenario: Título de la ventana principal por pantalla
- **WHEN** el usuario navega entre las pantallas de la aplicación (Menú Principal, Histórico, Configuración, Editor, Clientes, Versiones, Copias o Arranque)
- **THEN** la ventana principal se titula «CaboFactu® <nombre de la pantalla actual>»

#### Scenario: Título con prefijo de marca en ventanas secundarias
- **WHEN** se abre una ventana secundaria de tipo `Stage` con su propio título
- **THEN** el título mostrado es «CaboFactu® <título propio de la ventana>»

### Requirement: Estilo de zona de acciones en tema por defecto

En el tema por defecto (Biblioteca8), la zona de acciones de las pantallas SHALL distinguirse visualmente sin que resalte: las tarjetas superiores del Histórico, de Clientes y del Editor (Nueva factura), que contienen los campos de búsqueda o de factura y los botones de acción, SHALL tener un fondo gris claro `#F6F6F6`.

Los botones de la barra de acciones del Editor y del Histórico SHALL NOT mostrarse con fondo blanco: SHALL ser planos y adoptar el color del contenedor en el que están, según el requisito «Botones de acción con icono identificativo». Guardar SHALL mantener su condición de acción principal mediante el color de acento en su icono y su etiqueta, en lugar de mediante un fondo de acento. Anular SHALL mostrarse a plena intensidad en el color del tema, igual que los demás botones, de modo que un Anular habilitado SHALL NOT confundirse con un botón deshabilitado, que aparece atenuado.

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
- **THEN** los botones de tabla Añadir línea y Eliminar línea ya no se muestran con fondo blanco y texto negro: se ven sobre el sombreado suave, sin recuadro y con el texto en peso normal

#### Scenario: La barra de acciones pierde el fondo blanco de sus botones
- **WHEN** el usuario abre el Editor con el tema por defecto
- **THEN** los botones Guardar, Nueva, Exportar, Versiones, Rectificar, Anular, Restaurar y Volver se ven sin recuadro blanco, con el color de la barra de fondo

#### Scenario: Botones del Editor que conservan su estilo
- **WHEN** el usuario abre el Editor con el tema por defecto
- **THEN** el botón Guardar se ve igual que los demás botones de la barra, sin negrita ni distintivo propio, y el botón Anular se ve en el mismo color que los demás

#### Scenario: Anular habilitado no parece deshabilitado
- **WHEN** el usuario mira el botón Anular habilitado junto a un botón deshabilitado con el tema por defecto
- **THEN** el Anular se ve a plena intensidad en el color del tema y se distingue a simple vista del botón deshabilitado, que aparece atenuado

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

### Requirement: Anulación y borrado de facturas desde el histórico

La aplicación SHALL permitir anular y borrar facturas directamente desde la pantalla de histórico. El usuario SHALL poder seleccionar una o varias facturas (independientemente de su estado o tipo). La acción **Anular** SHALL cambiar el estado de la factura a `ANULADA` y conservar el registro. La acción **Borrar** SHALL eliminar físicamente la factura, sus versiones y sus líneas de la base de datos; antes de borrar, el sistema SHALL advertir al usuario del número de versiones y líneas que se eliminarán y SHALL pedir confirmación. Al borrar una factura, su número SHALL quedar registrado como disponible para poder reutilizarse al crear la siguiente factura de la misma serie y año. El sistema SHALL mostrar un resumen con el resultado de la operación y SHALL refrescar la tabla del histórico.

#### Scenario: Anular una factura desde el histórico
- **WHEN** el usuario selecciona una factura emitida y pulsa "Anular"
- **THEN** el sistema pide confirmación
- **AND** tras confirmar, la factura pasa a estado Anulada y el resumen indica 1 anulada

#### Scenario: Borrar una factura desde el histórico
- **WHEN** el usuario selecciona una factura y pulsa "Borrar"
- **THEN** el sistema muestra un aviso con las versiones y líneas que se eliminarán
- **AND** tras confirmar, la factura desaparece de la base de datos y su número queda disponible

#### Scenario: Menú contextual del histórico
- **WHEN** el usuario hace clic derecho sobre las facturas seleccionadas del histórico
- **THEN** aparece un menú contextual con las opciones "Exportar a PDF", "Anular facturas seleccionadas" y "Borrar facturas seleccionadas"

#### Scenario: Resumen tras anular varias facturas
- **WHEN** el usuario anula una selección que incluye facturas emitidas y facturas ya anuladas
- **THEN** se muestra un resumen con las anuladas y las ya anuladas

### Requirement: Sistema de diseño visual Apple

La aplicación SHALL aplicar un sistema de diseño visual inspirado en Ajustes de Apple: jerarquía clara, espaciado generoso, agrupación de controles en secciones con fondo de tarjeta, esquinas redondeadas, tipografía con tamaños diferenciados y una paleta de acentos coherente. Todos los controles interactivos (botones, campos, tablas, listas) SHALL mostrar un estado visual de `:hover` y `:focused` inmediato y sutil. Los formularios SHALL alinear etiquetas y campos con una cuadrícula coherente. Las tablas y listas SHALL usar filas de altura uniforme, separación clara y estado seleccionado visible pero no agresivo. Además, los paneles de contenido SHALL mantener un margen claro respecto al borde de la ventana y respecto a la barra de menú superior, de modo que los campos y tarjetas no queden pegados al borde ni se perciban solapados con la navegación. En cada pantalla principal, los paneles de contenido SHALL aplicarse como tarjetas con fondo, borde, esquinas redondeadas y espaciado interior; ningún panel SHALL quedarse sin ese estilo por una clase de estilo mal declarada.

#### Scenario: Pantallas con tarjetas de sección
- **WHEN** el usuario abre Configuración, el Editor, el Histórico, Clientes, Versiones o Backup
- **THEN** los controles se agrupan en secciones con fondo de tarjeta, esquinas redondeadas y separación respecto al fondo de la ventana

#### Scenario: Estados de foco visibles
- **WHEN** el usuario navega por el formulario con Tab o hace clic en un campo
- **THEN** el control enfocado muestra un anillo o borde de acento sutil y el botón bajo el cursor cambia de tono sin esperar al clic

#### Scenario: Tipografía jerárquica
- **WHEN** el usuario abre cualquier pantalla principal
- **THEN** los títulos son más grandes, los subtítulos algo menores y los datos secundarios aparecen en un tono más tenue, sin que la jerarquía dependa del peso de la letra

#### Scenario: Tablas limpias
- **WHEN** el usuario abre el Histórico
- **THEN** la tabla tiene cabecera clara, filas de altura uniforme, separadores suaves y la fila seleccionada se destaca con el color de acento muy suave

#### Scenario: Margen respecto al borde de la ventana
- **WHEN** el usuario abre cualquier pantalla principal
- **THEN** los paneles de contenido mantienen un margen visible respecto al borde izquierdo, derecho, superior e inferior de la ventana

#### Scenario: Separación respecto a la barra de menú
- **WHEN** el usuario abre el Histórico o la Configuración
- **THEN** existe una separación clara entre la barra de menú superior y la primera fila de controles o pestañas

#### Scenario: Filtros sin tocar el borde izquierdo
- **WHEN** el usuario abre el Histórico
- **THEN** el campo "Serie" y los demás filtros no están pegados al borde izquierdo de su tarjeta

#### Scenario: Ningún panel pierde el estilo de tarjeta
- **WHEN** la aplicación carga cualquiera de las pantallas principales
- **THEN** cada panel de contenido recibe las clases de estilo que declara y ninguno queda con una única clase errónea que impida aplicar el fondo de tarjeta, el borde, las esquinas redondeadas ni el espaciado

### Requirement: Microinteracciones visuales

La interfaz SHALL incluir microinteracciones suaves que refuercen la sensación de pulido: transiciones cortas en cambios de color de botones, suavizado en el cambio de foco y, cuando sea técnicamente viable sin bloquear la interacción, una transición ligera al cambiar entre pantallas. Las animaciones SHALL ser cortas (menos de 200 ms) y no SHALL bloquear la entrada del usuario. Si el sistema operativo indica preferencia por movimiento reducido, las animaciones se reducirán o desactivarán.

#### Scenario: Hover en botón primario
- **WHEN** el usuario pasa el ratón por encima de un botón primario
- **THEN** el fondo del botón cambia a un tono ligeramente más oscuro de forma suave

#### Scenario: Transición de foco en campo
- **WHEN** el usuario hace clic en un campo de texto
- **THEN** el borde de acento aparece con una transición suave en lugar de un cambio brusco

#### Scenario: Movimiento reducido
- **WHEN** el sistema operativo tiene activada la opción de reducir animaciones
- **THEN** la aplicación omite o acorta las transiciones visuales

### Requirement: Tamaños de ventana por vista

La aplicación SHALL garantizar que cada pantalla principal tenga un tamaño mínimo suficiente para que todo su contenido sea legible y usable. El tamaño predefinido y mínimo de todas las pantallas principales SHALL ser 1024×768, salvo la pantalla de selección de empresa (`Arranque`), que SHALL ser de tamaño fijo y no redimensionable, y el diálogo de Generar facturas mensuales, que mantiene 800×600. La ventana principal SHALL tener un tamaño mínimo de 1024×768 y, una vez dentro de la aplicación, al cambiar entre vistas SHALL conservar su tamaño actual (nunca inferior a 1024×768) en lugar de redimensionarse o recentrarse. Ninguna vista principal SHALL fijar un mínimo global de ventana superior que impida a `Arranque` mostrarse a su tamaño fijo; cada vista SHALL controlar su tamaño mediante su propia configuración.

#### Scenario: Arranque fijo
- **WHEN** la aplicación muestra la pantalla de selección de empresa
- **THEN** la ventana tiene un tamaño fijo de 760×520, no es redimensionable y no se puede maximizar

#### Scenario: Editor con tamaño mínimo legible
- **WHEN** el usuario abre el Editor de facturas
- **THEN** la ventana tiene un tamaño predefinido y mínimo de 1024×768, no se abre maximizada y todo su contenido (barra de navegación, cabecera, tabla de líneas, botones de acción y resumen de totales) es accesible
- **AND** el contenido completo del Editor tiene scroll general vertical cuando no cabe en el alto de la ventana
- **AND** la tabla de líneas muestra scroll vertical interno cuando hay muchas líneas

#### Scenario: Configuración con tamaño mínimo
- **WHEN** el usuario abre Configuración
- **THEN** la ventana tiene un tamaño predefinido y mínimo de 1024×768 y todos los controles de las pestañas son accesibles

#### Scenario: Histórico con tamaño mínimo
- **WHEN** el usuario abre el Histórico
- **THEN** la ventana tiene un tamaño predefinido y mínimo de 1024×768 y la tabla de facturas se adapta al ancho mostrando scroll horizontal si es necesario

#### Scenario: Clientes con tamaño mínimo
- **WHEN** el usuario abre Clientes
- **THEN** la ventana tiene un tamaño predefinido y mínimo de 1024×768 y la tabla de clientes se adapta al ancho mostrando scroll horizontal si es necesario

#### Scenario: Menú principal con tamaño mínimo
- **WHEN** el usuario abre el Menú principal
- **THEN** la ventana tiene un tamaño predefinido y mínimo de 1024×768 y las tarjetas dejan un margen visible respecto al borde inferior de la ventana

#### Scenario: Versiones con tamaño mínimo
- **WHEN** el usuario abre Versiones
- **THEN** la ventana tiene un tamaño predefinido y mínimo de 1024×768 y la tabla de versiones se adapta al ancho

#### Scenario: Backup con tamaño mínimo
- **WHEN** el usuario abre Backup
- **THEN** la ventana tiene un tamaño predefinido y mínimo de 1024×768

#### Scenario: Diálogo de facturación mensual
- **WHEN** se abre el diálogo de Generar facturas mensuales
- **THEN** el diálogo tiene un tamaño predefinido de 800×600 y muestra scroll si el contenido no cabe

#### Scenario: Centrado al cambiar de vista
- **WHEN** el usuario navega entre el Menú principal, el Editor, Configuración, Histórico, Clientes, Versiones o Backup
- **THEN** la ventana principal mantiene su tamaño actual (nunca inferior a 1024×768) y no se produce ningún salto ni recentrado

### Requirement: Menú principal adaptado a 1024×768

La pantalla del Menú principal SHALL tener un tamaño de 1024×768 y SHALL ajustar sus elementos para que la información de la empresa sea visible sin cortarse horizontalmente y para que la lista de opciones deje un margen inferior visible respecto al borde de la ventana. Las tarjetas de la pantalla SHALL mantener el estilo visual del sistema de diseño (fondo de tarjeta, esquinas redondeadas, sombra) y SHALL adaptarse al ancho disponible sin superponerse.

#### Scenario: Información de empresa visible
- **WHEN** el usuario abre el Menú principal
- **THEN** la tarjeta de empresa muestra el nombre, la información adicional y el logo sin cortarse horizontalmente

#### Scenario: Margen inferior en el Menú principal
- **WHEN** el usuario abre el Menú principal
- **THEN** debajo de la última opción del menú queda un espacio visible antes del borde inferior de la ventana

#### Scenario: Opciones del menú dentro de 1024×768
- **WHEN** el usuario abre el Menú principal
- **THEN** todas las opciones del menú y la tarjeta de empresa caben dentro de la ventana sin scroll

### Requirement: Editor legible sin scroll en facturas cortas

El Editor de facturas SHALL mostrar completa una factura de pocas lineas en el tamaño mínimo de ventana de 1024x768, sin que el usuario tenga que desplazarse. El resumen de totales SHALL permanecer visible en todo momento, con independencia del número de líneas. La tabla de líneas SHALL ser el único elemento que crece con la ventana y SHALL desplazarse internamente cuando las líneas no quepan.

El campo de observaciones SHALL ocupar una línea a todo el ancho por encima del bloque de totales, y SHALL crecer en altura cuando el texto lo requiera, hasta un máximo que no comprometa la visibilidad de los totales.

#### Scenario: Factura corta sin scroll
- **WHEN** el usuario abre una factura nueva con la ventana en 1024x768
- **THEN** la cabecera, la tabla de lineas, las observaciones y el resumen de totales son visibles sin desplazarse

#### Scenario: Pie con matriz y escalera dentro de 1024×768
- **WHEN** el usuario edita a 1024×768 una factura con tres tipos de IVA distintos, descuento, retención y un suplido
- **THEN** la matriz completa, la escalera completa y la línea de observaciones caben en el pie sin scroll

#### Scenario: Totales siempre visibles
- **WHEN** una factura tiene más lineas de las que caben en la tabla
- **THEN** solo se desplaza la tabla, y el desglose de base imponible, IVA, retencion y total sigue visible al pie

#### Scenario: Cabecera repartida en dos bloques
- **WHEN** el usuario abre el Editor
- **THEN** los datos de la factura y los del cliente se muestran en dos bloques contiguos, y los campos de cliente, nombre y direccion disponen del ancho suficiente para su contenido habitual

#### Scenario: La tabla aprovecha el alto disponible
- **WHEN** el usuario amplia o maximiza la ventana del Editor
- **THEN** la tabla de lineas absorbe todo el alto adicional y el resto de zonas conserva su tamaño

### Requirement: Desglose de totales por tipo de IVA

> Recrea «Orden del desglose de totales» sin su parrafo de PDF ni su escenario
> de PDF («Factura con descuento en el PDF»), que estan en «Orden del desglose
> en el PDF» de `pdf-rendering`. El título es nuevo porque el primero es una
> regla general que sostiene tambien al requisito del PDF, no solo al editor.

Cuando la factura tenga **varios tipos de IVA**, el desglose SHALL mostrar la base imponible de **cada tipo** junto a su cuota, de modo que cada cuota impresa sea comprobable a partir de una base impresa. SHALL NOT mostrarse una única base imponible agregada en lugar de las bases por tipo. El desglose SHALL incluir además una suma de las bases y una suma de las cuotas.

En el **editor**, ese desglose por tipo SHALL presentarse como una matriz con una fila por tipo de IVA y columnas de base imponible y cuota, más una fila de totales. Junto a la matriz SHALL mostrarse la escalera hasta el total, con las filas de subtotal, descuento, base imponible, IVA total, retención, suplidos y TOTAL FACTURA, cada una sujeta a su condición de aparición. Las filas de subtotal y descuento SHALL aparecer solo cuando el descuento global sea mayor que 0.

El editor y el PDF SHALL partir del mismo cálculo y SHALL mostrar los mismos importes para la misma factura. No están obligados a compartir disposición ni rótulos.

#### Scenario: Desglose por tipo en el editor
- **WHEN** el usuario edita una factura con una línea de 1.000,00 € al 21 %, otra de 500,00 € al 10 % y un descuento global del 10 %
- **THEN** la matriz muestra una fila de 900,00 € con cuota 189,00 €, otra de 450,00 € con cuota 45,00 €, y una fila de totales con 1.350,00 € y 234,00 €
- **AND** cada cuota coincide con aplicar su tipo a la base imponible de su misma fila

#### Scenario: Factura con descuento en el editor
- **WHEN** el usuario edita una factura con base 200,00 €, descuento global del 10 %, IVA 21 % y retención del 15 %
- **THEN** la escalera muestra, en este orden: subtotal 200,00 €, descuento −20,00 €, base imponible 180,00 €, IVA total 37,80 €, retención −27,00 € y TOTAL FACTURA 190,80 €

#### Scenario: Factura sin descuento
- **WHEN** la factura no tiene descuento global
- **THEN** la escalera del editor no muestra las filas de subtotal ni de descuento, y su primera fila visible es la base imponible
- **AND** bajo la rejilla de IVA del PDF no aparece ninguna nota de descuento

#### Scenario: Los importes no cambian
- **WHEN** se presenta el desglose de cualquier factura
- **THEN** todos los importes son idénticos a los calculados antes del cambio

### Requirement: Criterio de etiquetado de botones

Las etiquetas de los botones de la aplicación SHALL seguir un criterio único en todas las pantallas, de modo que una misma acción se llame siempre igual.

El botón SHALL nombrar la **acción**, dejando que el objeto lo aporte la pantalla en la que está: en la pantalla de Clientes, «Eliminar» ya significa eliminar el cliente seleccionado.

Se SHALL usar un único verbo por concepto. En particular, la acción destructiva SHALL llamarse siempre «Eliminar» y SHALL NOT llamarse «Borrar» en ninguna pantalla.

El criterio SHALL alcanzar también al **texto de los diálogos** que abre cada botón: el título y el cuerpo del mensaje SHALL usar el mismo verbo que el botón desde el que se llega, de modo que el usuario no tenga que decidir si dos palabras distintas nombran la misma acción justo antes de confirmarla. El título de un diálogo SHALL NOT nombrar una acción que ese flujo no realiza.

«Volver» SHALL usarse para salir de una pantalla conservando lo realizado. «Cancelar» SHALL usarse únicamente en diálogos modales, donde el gesto descarta lo que se estaba componiendo.

Los atajos de teclado SHALL indicarse en el tooltip del botón y SHALL NOT formar parte del texto de la etiqueta.

Una misma función SHALL tener el mismo nombre desde cualquier punto de entrada.

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
- **THEN** el botón se llama igual en los dos sitios

### Requirement: Botones de acción con icono identificativo

Los botones de las barras de acciones del Editor, del Histórico y de Clientes SHALL mostrar un icono identificativo de la acción encima de su etiqueta de texto.

En reposo el botón SHALL NOT pintar fondo ni borde propios: SHALL adoptar el color del contenedor en el que está, de modo que lo único visible sea la silueta del icono y su etiqueta, igual que en los botones de la barra de navegación superior. El botón SHALL conservar su forma y su tamaño, de manera que todos los botones de una misma barra sigan midiendo lo mismo.

Al pasar el puntero por encima, el botón SHALL insinuar su superficie con un velo translúcido neutro, y SHALL oscurecerlo al mantenerlo pulsado. Al recibir el foco de teclado, el botón SHALL dibujar un borde en el color de acento del tema, de modo que la navegación con teclado siga siendo visible pese a la ausencia de borde en reposo.

Los iconos SHALL ser monocromo de un solo color, dibujados como trazado vectorial, de modo que el tema activo pueda recolorearlos. SHALL NOT usarse imágenes de mapa de bits ni iconos multicolor de color fijo.

El color del icono y el de la etiqueta SHALL provenir del tema activo y SHALL mantener contraste legible sobre el fondo de la barra en los siete temas, incluidos los oscuros. Tanto el icono como la etiqueta SHALL ir en el color de acento del tema, también en los botones de acciones destructivas: ningún botón de barra SHALL mostrarse en el color de peligro. El carácter destructivo de una acción lo comunica su icono, no su color.

Ningún botón SHALL destacarse como acción principal: todos los de una misma barra SHALL presentarse con el mismo peso, el mismo color y el mismo tratamiento, y el orden de los botones SHALL ser la única jerarquía. Ninguna etiqueta SHALL mostrarse en negrita.

Una misma acción SHALL llevar el mismo icono en todas las pantallas donde aparezca, y dos acciones distintas SHALL NOT compartir icono. Cuando una acción necesite un icono que no exista en la librería de origen, SHALL componerse a partir de trazados ya presentes en la aplicación, y la composición SHALL quedar documentada de forma que pueda reproducirse.

Los botones de una barra de acciones SHALL agruparse por afinidad, y los grupos SHALL separarse visualmente mediante un separador vertical. La agrupación SHALL NOT alterar el significado ni el comportamiento de ningún botón.

Este requisito alcanza únicamente a las barras de acciones del Editor, del Histórico y de Clientes. Los botones de formulario, los de las tablas, los de los diálogos modales y los de las demás pantallas SHALL conservar su aspecto actual mientras no se especifique lo contrario.

#### Scenario: Icono sobre el texto en la barra del Editor
- **WHEN** el usuario abre el Editor
- **THEN** cada botón de la barra de acciones muestra un icono encima de su etiqueta, sin recuadro ni borde alrededor, fundido con el fondo de la barra

#### Scenario: El botón resalta al pasar el ratón
- **WHEN** el usuario pasa el puntero sobre un botón de la barra de acciones
- **THEN** aparece un velo translúcido que delimita el botón, y desaparece al retirar el puntero

#### Scenario: El foco de teclado es visible
- **WHEN** el usuario recorre la barra de acciones con el tabulador
- **THEN** el botón enfocado muestra un borde en el color de acento del tema

#### Scenario: La acción principal se distingue sin fondo de color
- **WHEN** el usuario mira el botón Guardar junto a Nueva y a Exportar
- **THEN** los tres muestran su icono y su etiqueta en el color de acento del tema, con el mismo peso, y ninguno se destaca sobre los demás

#### Scenario: El icono cambia de color con el tema
- **WHEN** el usuario cambia el tema desde Configuración
- **THEN** los iconos de los botones de acción adoptan el color del tema nuevo y siguen leyéndose con contraste suficiente, también en los temas oscuros

#### Scenario: Un icono por acción, coherente entre pantallas
- **WHEN** el usuario compara el botón de exportar del Editor con el del Histórico, y los botones de Eliminar y Volver del Histórico con los de Clientes
- **THEN** cada acción muestra el mismo icono en todas las pantallas donde aparece

#### Scenario: Grupos separados en la barra
- **WHEN** el usuario mira la barra de acciones del Editor
- **THEN** ve las acciones repartidas en grupos separados por una línea vertical, con las de escritura juntas y las destructivas en su propio grupo

#### Scenario: Clientes usa la misma barra que sus pantallas hermanas
- **WHEN** el usuario abre Clientes
- **THEN** los botones Nuevo, Editar, Eliminar y Volver muestran icono sobre la etiqueta, en la misma fila que el campo de búsqueda y alineados a la derecha, agrupados por afinidad y separados por líneas verticales

#### Scenario: Las pantallas fuera de alcance no cambian
- **WHEN** el usuario abre Configuración, Copia de seguridad o Versiones
- **THEN** sus botones siguen mostrando solo texto, sobre el sombreado suave de los botones de solo texto

### Requirement: Suplidos en la facturación

> Recrea «Suplidos» sin sus parrafos de PDF ni su escenario de PDF («El
> suplido tiene su propio bloque en el PDF»), que estan en «Suplidos en el PDF»
> de `pdf-rendering`. El título es nuevo porque lo que queda no es solo fiscal:
> incluye como se introduce un suplido y que su total se guarda en la version.

La aplicación SHALL permitir facturar suplidos: gastos pagados por cuenta del cliente que no forman parte de la contraprestación.

Un suplido SHALL introducirse como una línea más de la factura, eligiendo el tipo de IVA «Suplido» en el mismo desplegable que el resto de tipos.

Una línea de suplido SHALL NOT formar parte de la base imponible, SHALL NOT generar cuota de IVA, SHALL NOT entrar en la base sobre la que se calcula la retención y SHALL NOT verse afectada por el descuento global. Su importe SHALL sumarse al total de la factura.

Las líneas de suplido SHALL NOT aparecer en el desglose por tipo de IVA, que solo describe operaciones sujetas.

El total de suplidos SHALL guardarse en la versión de la factura, de modo que reabrir una factura antigua muestre los mismos importes.

#### Scenario: Factura con suplido
- **WHEN** el usuario factura una línea de 1.000,00 € al 21 % y una línea de suplido de 250,00 €, con retención del 15 %
- **THEN** la base imponible es 1.000,00 €, la cuota de IVA 210,00 €, la retención 150,00 € y el total 1.310,00 €
- **AND** el suplido no aparece en el desglose por tipo de IVA

#### Scenario: El descuento global no afecta a los suplidos
- **WHEN** la factura anterior lleva además un descuento global del 10 %
- **THEN** la base imponible pasa a 900,00 € y la cuota a 189,00 €, mientras el suplido sigue siendo 250,00 €

#### Scenario: Factura sin suplidos
- **WHEN** ninguna línea de la factura es un suplido
- **THEN** el desglose no muestra la fila de suplidos, ni en el editor ni en el PDF
- **AND** el PDF no incluye el bloque de suplidos ni su nota
- **AND** todos los importes son idénticos a los calculados antes de existir los suplidos

### Requirement: Base de datos desde un esquema único

La base de datos de cada empresa SHALL crearse a partir de un único script de esquema, sin necesidad de encadenar migraciones sucesivas para llegar al estado actual.

Todos los importes monetarios SHALL guardarse con el mismo tipo de columna, de modo que ninguna cantidad se almacene con un criterio distinto del resto.

Las filas sembradas por el esquema SHALL NOT depender de que un identificador concreto esté libre. Toda base recién creada SHALL disponer de los tipos de IVA habituales y del tipo «Suplido».

Ejecutar el proceso de creación sobre una base que ya está al día SHALL NOT duplicar ninguna fila sembrada.

#### Scenario: Base nueva lista para facturar
- **WHEN** el usuario crea una empresa nueva
- **THEN** la base resultante tiene todas las tablas de la aplicación y ofrece los tipos de IVA habituales más «Suplido»
- **AND** exactamente un tipo de IVA está marcado como suplido

#### Scenario: Los importes comparten criterio
- **WHEN** se guarda una factura con retención y suplidos
- **THEN** la base imponible, la cuota de IVA, el importe de retención, el total de suplidos y el total se almacenan con el mismo tipo de columna
- **AND** los decimales se conservan exactamente como se calcularon

#### Scenario: Crear la base dos veces no duplica nada
- **WHEN** el proceso de creación se ejecuta de nuevo sobre una base que ya está al día
- **THEN** el número de tipos de IVA, de tipos de retención y de filas de empresa no cambia

### Requirement: Datos de demostración recreables

La aplicación SHALL disponer de un juego de datos de demostración que se pueda cargar bajo demanda, para probar sin teclear facturas a mano.

Los datos de demostración SHALL versionarse junto al esquema, de modo que un cambio de esquema pueda actualizarlos en el mismo sitio.

Los datos de demostración SHALL NOT depender de los identificadores concretos que el esquema asigne a sus filas sembradas: SHALL referirse a ellas por su nombre, de modo que cambiar el orden de las siembras no altere lo que la demostración representa.

La carga SHALL ser repetible: ejecutarla dos veces SHALL dejar el mismo resultado, sin datos duplicados.

Cuando la demostración no se pueda recrear porque su base está en uso, la carga SHALL detenerse con un mensaje que diga que hay que cerrar la aplicación, y SHALL NOT continuar sobre una demostración a medio borrar.

Los datos de demostración SHALL ser ficticios y SHALL NOT contener datos reales de ningún cliente.

La demostración SHALL cubrir los casos que cuestan de montar a mano: varias series, una factura con varios tipos de IVA, una con descuento global, una con retención, una con suplido, una anulada y una rectificativa.

#### Scenario: Cargar la demostración
- **WHEN** el usuario carga los datos de demostración
- **THEN** existe una empresa de demostración con clientes, series y facturas de ejemplo
- **AND** entre ellas hay una con varios tipos de IVA, una con descuento, una con retención, una con suplido, una anulada y una rectificativa

#### Scenario: Cargar la demostración dos veces
- **WHEN** el usuario carga los datos de demostración sobre una empresa de demostración que ya existía
- **THEN** la empresa queda con los mismos datos que la primera vez, sin duplicados

#### Scenario: La demostración no depende del orden de las siembras
- **WHEN** cambia el orden en que el esquema siembra los tipos de IVA
- **THEN** cada línea de la demostración sigue llevando el tipo de IVA que le corresponde por su nombre

#### Scenario: Cargar la demostración con la aplicación abierta
- **WHEN** el usuario carga los datos de demostración mientras la aplicación tiene abierta esa misma empresa
- **THEN** la carga se detiene indicando que hay que cerrar la aplicación
- **AND** la demostración anterior queda intacta, no a medio borrar

#### Scenario: Los datos de demostración son ficticios
- **WHEN** alguien revisa la empresa de demostración
- **THEN** ningún cliente, NIF ni dirección corresponde a una persona o empresa real

### Requirement: Sombreado uniforme de los botones de solo texto

Los botones que muestran únicamente texto, sin icono, SHALL presentarse sobre un sombreado suave y uniforme, sin borde y sin fondo blanco. El sombreado SHALL ser el mismo en todos los botones de una misma pantalla.

El sombreado SHALL ser un gris neutro teñido levemente con el color de acento del tema activo, de modo que se lea como gris y acompañe al tema sin competir con él. Cada tema de apariencia SHALL declarar su propio valor de sombreado.

Estos botones SHALL mostrar su texto en peso normal, sin negrita.

Ningún botón SHALL destacarse como acción principal: todos los de una misma pantalla SHALL compartir el mismo sombreado y el mismo color de texto. Tampoco los botones de acciones destructivas SHALL mostrarse en color de peligro: van en el mismo color que los demás.

El botón SHALL reaccionar a la interacción: al situar el puntero encima su sombreado SHALL oscurecerse, al mantenerlo pulsado SHALL oscurecerse más, y al recibir el foco de teclado SHALL mostrar un borde con el color de acento del tema activo.

Este requisito SHALL aplicarse a los botones de solo texto de Configuración, Copia de seguridad, Versiones, el Editor y la generación de facturas mensuales, incluidos los botones de línea de esta última, que hasta ahora mostraban el gris por defecto de la plataforma. SHALL NOT aplicarse a los botones de la barra de acciones de Clientes, que muestran icono, ni a los de la pantalla de arranque ni a los de los diálogos de aviso y confirmación, que la plataforma construye por su cuenta. La maquetación, el comportamiento y las acciones de todos ellos SHALL permanecer sin cambios: la modificación es exclusivamente de apariencia.

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

### Requirement: Marca en la pantalla de arranque

La pantalla de selección de empresa SHALL identificarse con la marca de la aplicación, «CaboFactu®», en lugar de con una descripción genérica de su función.

Junto a ese rótulo SHALL mostrarse el icono de la aplicación, el mismo que aparece en la barra de título y en la barra de tareas, situado a su izquierda y en la misma línea. El icono y el rótulo SHALL presentarse centrados como un único conjunto en la parte superior de la pantalla.

El conjunto de marca SHALL caber dentro del tamaño fijo de la pantalla de arranque sin desplazar ni recortar la tarjeta de selección de empresa, ejercicio y fecha de trabajo, ni el mensaje de error.

Los controles de la pantalla, su disposición y el flujo de selección SHALL permanecer sin cambios: la modificación es exclusivamente de identidad visual.

#### Scenario: La portada muestra la marca
- **WHEN** el usuario abre la aplicación y aparece la pantalla de selección de empresa
- **THEN** la pantalla muestra «CaboFactu®» como rótulo, no una descripción genérica de la función del programa

#### Scenario: El icono acompaña al rótulo
- **WHEN** el usuario mira la parte superior de la pantalla de arranque
- **THEN** el icono de la aplicación aparece a la izquierda del rótulo, en la misma línea, y ambos quedan centrados como conjunto

#### Scenario: El contenido sigue cabiendo
- **WHEN** el usuario abre la pantalla de arranque, que es de tamaño fijo y no redimensionable
- **THEN** la tarjeta de selección de empresa, ejercicio y fecha de trabajo se muestra completa, sin desplazarse ni recortarse, igual que antes del cambio

#### Scenario: El flujo no cambia
- **WHEN** el usuario selecciona empresa, ejercicio y fecha de trabajo y pulsa Entrar
- **THEN** la aplicación se comporta igual que antes del cambio

### Requirement: Consonancia tipográfica de la interfaz

Los elementos de navegación y de acción de la aplicación SHALL presentar un peso tipográfico acorde entre sí, de modo que los controles más usados no se lean con menos presencia que los secundarios.

El texto de los botones que muestran icono y etiqueta —los de la barra de navegación y los de las barras de acciones del Editor y del Histórico— SHALL mostrarse en peso normal y a un tamaño que no quede por debajo del de las etiquetas de los botones de solo texto en más de un punto.

Las entradas seleccionables de la lista de secciones de la pantalla de Configuración SHALL mostrarse en peso normal. La entrada seleccionada SHALL seguir distinguiéndose de las demás por su fondo y por su color de texto.

Ningún control, etiqueta, acción ni disposición SHALL cambiar por este motivo: la modificación es exclusivamente tipográfica.

#### Scenario: Los botones con icono pesan como los de solo texto
- **WHEN** el usuario mira la barra de navegación o la barra de acciones del Editor junto a un botón de solo texto
- **THEN** las etiquetas de los botones con icono se leen con el mismo peso que las de los botones de solo texto, sin quedar unas visiblemente más pesadas que otras

#### Scenario: Las etiquetas de navegación no se recortan
- **WHEN** el usuario abre cualquier pantalla con la ventana en su tamaño mínimo de 1024x768
- **THEN** las siete etiquetas de la barra de navegación se muestran completas, sin recortarse, y la barra cabe en el ancho de la ventana

#### Scenario: La lista de secciones se lee en negrita
- **WHEN** el usuario abre la pantalla de Configuración
- **THEN** las entradas de la lista de secciones se muestran en peso normal, y la que está seleccionada se distingue por su fondo y su color de texto

### Requirement: La negrita queda reservada a los importes

La aplicación SHALL NOT usar la negrita como recurso de jerarquía en su interfaz. Los títulos de pantalla, los rótulos de sección, las etiquetas de los botones, las cabeceras de tabla, las entradas de lista y los nombres del menú principal SHALL mostrarse en peso normal, y SHALL distinguirse entre sí por su tamaño y por su color.

La negrita SHALL reservarse a los importes: el valor de las filas de totales y el total destacado de cada tema de apariencia. Fuera de esos dos casos, ningún texto de la interfaz SHALL mostrarse en negrita.

Esta regla SHALL alcanzar a los siete temas de apariencia por igual.

#### Scenario: Ningún control se muestra en negrita
- **WHEN** el usuario recorre el menú principal, el Editor, el Histórico, Clientes y Configuración
- **THEN** ninguna etiqueta de botón, cabecera de tabla, entrada de lista ni título de pantalla se muestra en negrita

#### Scenario: Los importes conservan la negrita
- **WHEN** el usuario mira el bloque de totales de una factura
- **THEN** el importe del total se sigue mostrando en negrita, de modo que se localiza de un vistazo

#### Scenario: La jerarquía se mantiene sin peso
- **WHEN** el usuario compara el título de una pantalla con el texto normal que la rodea
- **THEN** el título se distingue por ser mayor y por su color, no por su peso

### Requirement: Iconos de tamaño uniforme dentro de una barra

Dentro de una misma barra de acciones, todos los iconos SHALL ocupar una caja del mismo tamaño y SHALL quedar centrados en ella, con independencia de lo que mida el dibujo de cada uno. Así las etiquetas de todos los botones de la barra SHALL arrancar a la misma altura y quedar alineadas entre sí.

La caja SHALL ser lo bastante grande para contener el icono más grande de la barra sin recortarlo. Cada icono SHALL conservar sus proporciones: la caja iguala el espacio que ocupa, no deforma el dibujo.

Este requisito SHALL aplicarse a las barras de acciones de Clientes, del Editor y del Histórico, que SHALL compartir el mismo tamaño de caja. SHALL aplicarse también a la lista de opciones del menú principal, con una caja propia: allí el icono va a la izquierda del texto, y lo que se iguala es la posición horizontal en la que arrancan el nombre y la descripción de cada opción. La barra de navegación queda fuera de este requisito.

#### Scenario: Las etiquetas de la barra quedan alineadas
- **WHEN** el usuario abre Clientes
- **THEN** las etiquetas Nuevo, Editar, Eliminar y Volver arrancan a la misma altura, aunque sus iconos tengan dibujos de distinto tamaño

#### Scenario: Ningún icono se recorta ni se deforma
- **WHEN** el usuario mira los iconos de la barra de Clientes, del Editor, del Histórico o del menú principal
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
