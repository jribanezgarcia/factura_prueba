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

La aplicación SHALL tener series de numeración. En una instalación nueva la aplicación SHALL NO crear ninguna serie por defecto: el listado de series comienza vacío y el usuario las crea a mano. Cada serie SHALL tener su propio correlativo. El correlativo SHALL ser la identidad de la factura; el componente de fecha (mes o año) SHALL recalcularse según la fecha y guardarse en cada versión. El correlativo de cada serie SHALL ser independiente por ejercicio: cada año de trabajo reinicia su propia cuenta sobre el siguiente número de ese año, sin afectar al correlativo de otros años. Las series SHALL poder crearse y configurarse desde la aplicación. La aplicación SHALL recordar la última serie utilizada y proponerla al crear la siguiente factura. El número SHALL proponerse automáticamente y poder modificarse manualmente. El número SHALL NOT consumirse hasta que la factura se guarda correctamente. SHALL ser configurable por serie el comportamiento con números anulados: continuar hacia delante o reutilizar números anulados; el comportamiento predeterminado SHALL ser continuar hacia delante. En Configuración → Series el usuario SHALL poder ver las series, ver el siguiente número y modificarlo.

Cada serie SHALL tener un campo \sufijo_fecha\ con tres opciones posibles: \MES\ (formato CODIGO-CORRELATIVO/MES o CORRELATIVO/MES si no hay código), \ANIO\ (formato CODIGO-CORRELATIVO-ANIO o CORRELATIVO-ANIO si no hay código) y \NINGUNO\ (formato CODIGO-CORRELATIVO o solo CORRELATIVO si no hay código). El campo \codigo\ de una serie SHALL poder estar vacío, en cuyo caso el número NO tendrá prefijo de letra; solo SHALL admitirse una serie sin código a la vez, de modo que otra serie en blanco se rechaza y se identifica la serie por su descripción. El formato predeterminado para series nuevas SHALL ser \MES\. La reutilización de números anulados SHALL operar dentro del mismo año: solo se reutilizan correlativos anulados de ese ejercicio y solo se consideran ocupados los correlativos activos de ese ejercicio.

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

La aplicación SHALL tener un menú principal con las opciones Nueva factura, Histórico, Configuración, Copia de seguridad y Salir. Dentro de una factura SHALL existir una barra superior con Guardar, Exportar PDF, Versiones, Crear rectificativa, Nueva factura y Volver. En todas las pantallas salvo el menú principal SHALL existir una barra de navegación superior con iconos que permita acceder a Menú principal, Nueva factura, Histórico, Clientes, Configuración, Copia de seguridad y Salir. Solo SHALL tenerse una factura abierta a la vez. No SHALL existir la opción "Nueva rectificativa" en el menú principal. Al cerrar la ventana o al pulsar Salir en la barra de navegación, la aplicación SHALL pedir confirmación antes de salir y SHALL seguir el mismo proceso de cierre (comprobación de cambios sin guardar, preferencias de ventana y lock).

#### Scenario: Crear rectificativa desde la factura
- **WHEN** el usuario pulsa "Crear rectificativa" en la barra de una factura abierta
- **THEN** se crea una rectificativa a partir de esa factura

#### Scenario: Navegar desde la barra de navegación
- **WHEN** el usuario pulsa un icono de la barra de navegación superior en una pantalla distinta del menú principal
- **THEN** la aplicación abre la pantalla correspondiente

#### Scenario: Cerrar la ventana con confirmación
- **WHEN** el usuario cierra la ventana
- **THEN** la aplicación pide confirmación antes de salir

#### Scenario: Salir desde la barra de navegación con confirmación
- **WHEN** el usuario pulsa Salir en la barra de navegación
- **THEN** la aplicación pide confirmación «¿Seguro que deseas salir de la aplicación?» y solo cierra si se acepta, siguiendo el mismo proceso de cierre que al cerrar la ventana

### Requirement: Cambios sin guardar

Si hay cambios sin guardar y el usuario pulsa Volver o cierra la aplicación, la aplicación SHALL ofrecer tres opciones: Guardar y volver/salir, Descartar cambios y volver/salir, y Cancelar. Si no hay cambios, la aplicación SHALL permitir salir normalmente.

#### Scenario: Cerrar con cambios sin guardar
- **WHEN** el usuario intenta volver o cerrar con cambios sin guardar
- **THEN** se muestra la confirmación con las opciones Guardar, Descartar o Cancelar

### Requirement: Atajos de teclado

La aplicación SHALL proporcionar los atajos Ctrl+N para Nueva factura, Ctrl+S para Guardar, Ctrl+F para Buscar, Ctrl+P para Exportar PDF y Esc para volver/cancelar cuando corresponda.

#### Scenario: Guardar con atajo
- **WHEN** el usuario pulsa Ctrl+S en una factura abierta
- **THEN** la factura se guarda, sobrescribiendo la versión actual o creando una nueva versión según corresponda

### Requirement: Configuración

La aplicación SHALL tener una pantalla de Configuración que permita configurar: los datos de la empresa (nombre, NIF, dirección, código postal, localidad, provincia y resto de datos necesarios para la cabecera); la cabecera del documento en dos modos, texto con datos de empresa o imagen/logo; el pie con texto legal libre configurable por el usuario; el tema de apariencia de la interfaz; los tipos de IVA; los tipos de retención de IRPF; las series (crear/configurar, ver y modificar el siguiente número, configurar la reutilización de números anulados y eliminar series sin facturas); las carpetas de PDF; el color de acento usado en los PDF exportados; y la gestión de empresas (ver el listado de empresas, crear una nueva, cambiar a otra y eliminar una empresa distinta de la actual). El color SHALL guardarse como preferencia `color_pdf`; si nunca se configura, los PDF SHALL usar arena Alcazaba (`#B08D57`), y del color elegido SHALL derivarse el resto de tonos del documento. La aplicación SHALL recordar preferencias de trabajo: última serie utilizada, tamaño/posición de ventana, última carpeta de exportación y tema de apariencia. El tamaño/posición de ventana y el tema SHALL guardarse de forma global, compartidos entre empresas.

#### Scenario: Configurar empresa
- **WHEN** el usuario guarda los datos de la empresa en Configuración
- **THEN** esos datos se usan en las nuevas exportaciones a PDF

#### Scenario: Elegir modo de cabecera
- **WHEN** el usuario selecciona un logo y lo ajusta en tamaño y posición
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

### Requirement: Retención de IRPF

La aplicación SHALL permitir aplicar una retención de IRPF a las facturas. La empresa SHALL poder configurar una lista de tipos de retención (nombre y porcentaje), gestionada de forma similar a los tipos de IVA. Cada factura SHALL poder seleccionar un tipo de retención configurado o ninguno. La retención SHALL calcularse sobre la base bruta de la factura (antes de aplicar el descuento global). El total de la factura SHALL ser `Base − Descuento + IVA − Retención`. La retención seleccionada y su importe SHALL guardarse en cada versión de la factura. Si no se selecciona ningún tipo de retención, el comportamiento SHALL ser el actual: `Total = Base − Descuento + IVA`.

#### Scenario: Factura con retención del 15%
- **WHEN** el usuario crea una factura con base 1.000,00 €, descuento 0 %, IVA 21 % y selecciona una retención del 15 % sobre la base bruta
- **THEN** el importe de retención es 150,00 € y el total es 1.060,00 €

#### Scenario: Factura con descuento y retención
- **WHEN** el usuario crea una factura con base 1.000,00 €, descuento global del 10 %, IVA 21 % y retención del 15 % sobre la base bruta
- **THEN** la base descontada es 900,00 €, el IVA es 189,00 €, la retención es 150,00 € y el total es 939,00 €

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

### Requirement: Retención en PDF

La exportación a PDF SHALL incluir una fila de retención en el resumen de totales cuando la factura tenga un tipo de retención seleccionado. La fila SHALL mostrar el nombre del tipo de retención y el importe retenido, y el total SHALL reflejar la resta de la retención.

#### Scenario: PDF con retención
- **WHEN** el usuario exporta a PDF una factura con base 1.000,00 €, IVA 21 % y retención del 15 %
- **THEN** el PDF muestra la retención como una fila propia y el total es 1.060,00 €

### Requirement: Retención en rectificativas

Al crear una rectificativa desde una factura, la aplicación SHALL copiar el tipo de retención de la factura original. El usuario SHALL poder modificar o quitar la retención en la rectificativa antes de guardarla. El cálculo del total de la rectificativa SHALL aplicar la misma fórmula de retención sobre la base bruta.

#### Scenario: Rectificativa hereda retención
- **WHEN** el usuario crea una rectificativa desde una factura que tiene una retención del 15 %
- **THEN** la rectificativa se crea con el mismo tipo de retención del 15 %, editable antes de guardar

### Requirement: Exportación a PDF

La aplicación SHALL exportar facturas a PDF en A4 vertical con el diseño aprobado inspirado en el documento Excel de la empresa:

- Cabecera en todas las páginas: logo al doble del tamaño configurado (modo logo) o datos de empresa (modo texto), con el NIF de la empresa destacado en línea propia con esquinas redondeadas; a la derecha la palabra FACTURA y debajo, como pares rótulo→valor, `SERIE / Nº` sobre el número completo y `FECHA` sobre la fecha. Los datos de empresa SHALL ocupar una columna propia que SHALL NOT solaparse nunca con el bloque FACTURA: si el nombre o alguna línea excede el ancho disponible, se reduce su tamaño hasta caber. El número completo y la fecha SHALL ser siempre legibles.
- Dos tarjetas bajo la cabecera con esquinas redondeadas: «FACTURAR A» con los datos del cliente presentados como pares etiqueta→valor, cada dato en su propia fila — Nombre (destacado en negrita), NIF, Dirección, Código postal, Población (la localidad), Provincia y Email; las filas con campo vacío no aparecen. «DATOS DE PAGO» con forma de pago, vencimiento y realizada por (solo las filas rellenas); cuando los tres campos estén vacíos, la tarjeta «Datos de pago» SHALL NOT aparecer y «Facturar A» SHALL conservar su anchura con el espacio restante en blanco. La cabecera de «FACTURAR A» SHALL ir con fondo del color de acento y texto blanco; la cabecera de «DATOS DE PAGO» SHALL ir en blanco con un borde fino inferior del color de acento y texto marrón oscuro derivado del acento. Ambos cuerpos SHALL ir en blanco.
- Tabla de líneas con celdas bordeadas estilo hoja de cálculo: Cant / Descripción / Precio / IVA % / Total. El Total por línea SHALL incluir el IVA (base × (1 + IVA%)); las líneas exentas SHALL mostrar su importe sin IVA. La descripción SHALL mostrarse siempre en un único estilo, aunque ocupe varias líneas.
- Resumen de totales alineado a la derecha y compacto, sin filas de totales globales repetidas («Base total»/«IVA total» SHALL NOT aparecer): por cada tipo de IVA una fila `Base` (importes antes del descuento global) seguida de su fila `IVA n%` con la cuota calculada sobre la base ya descontada; si el descuento global es mayor que cero, una única fila `Descuento n%` con el importe restando y en rojo suave; después la fila TOTAL destacada con fondo del color de acento, separada de las filas anteriores por un espacio visible. Las cifras mostradas SHALL cuadrar: Base − Descuento + IVA = TOTAL.
- Observaciones en caja clara con esquinas redondeadas; pie legal configurable dentro de un recuadro con borde de color, repetido en todas las páginas; `Página X de Y` en cada página reflejando el número real de páginas, con el dígito total dibujado sin solapar la palabra «de». El cierre del documento SHALL mantenerse compacto (totales estrechos y tablas capaces de repartir sus filas entre páginas) para evitar una página que contenga únicamente el bloque de totales cuando el contenido cabe repartiéndose.
- Tipografía Calibri embebida en el documento cuando esté disponible en el sistema; en caso contrario Helvetica. Los tonos SHALL derivarse del color de acento configurado siguiendo el prototipo.

El resto se mantiene como estaba: descripciones largas ajustadas automáticamente, importes formato español (`1.250,50 €`), fechas formato español (`11/08/2026`), marca `ANULADA` destacada en facturas anuladas, correspondencia exacta con la versión exportada, uso de la configuración actual de empresa/logo/cabecera/pie legal, documentos independientes, estructura `Facturas/AAAA/SERIE/` y nombre `CODIGO-CORRELATIVO-MES.pdf` sin indicar versión. El color de acento SHALL tomarse de la preferencia `color_pdf`, con valor por defecto arena Alcazaba (`#B08D57`) si no está configurada.

#### Scenario: Exportar factura de varias páginas
- **WHEN** el usuario exporta una factura con descripciones largas que ocupa varias páginas
- **THEN** el PDF repite cabecera y pie en cada página e indica `Página X de Y`

#### Scenario: Exportar factura anulada
- **WHEN** el usuario exporta una factura anulada
- **THEN** el PDF muestra la marca `ANULADA` de forma destacada

#### Scenario: Exportar versión concreta
- **WHEN** el usuario exporta una versión concreta del histórico
- **THEN** el PDF refleja exactamente los datos de esa versión

#### Scenario: Nombres de archivo
- **WHEN** el usuario exporta la factura C-59/8
- **THEN** se genera el archivo `Facturas/2026/C/C-59-8.pdf`

#### Scenario: Rótulos Serie/Nº y Fecha bajo FACTURA
- **WHEN** el usuario exporta cualquier factura
- **THEN** bajo la palabra FACTURA aparece el rótulo `SERIE / Nº` encima del número completo y el rótulo `FECHA` encima de la fecha

#### Scenario: Cabecera sin solapes con nombre largo
- **WHEN** el usuario exporta una factura de una empresa cuyo nombre o líneas son más anchos que la columna disponible
- **THEN** los datos de empresa no invaden el bloque FACTURA
- **AND** el número completo (Serie/Nº) y la fecha son legibles en su posición

#### Scenario: Tarjeta Facturar a con campos etiquetados
- **WHEN** el usuario exporta una factura cuyo cliente tiene nombre, NIF, dirección, código postal, localidad, provincia y email
- **THEN** la tarjeta «Facturar a» muestra cada dato precedido de su etiqueta: Nombre, NIF, Dirección, Código postal, Población, Provincia y Email
- **AND** el código postal aparece como fila propia, separada de Población

#### Scenario: Campos vacíos sin fila
- **WHEN** el cliente carece de alguno de esos campos
- **THEN** la fila correspondiente no aparece en la tarjeta

#### Scenario: Tarjeta Facturar a bicolor y tarjeta Datos de pago clara
- **WHEN** el usuario exporta una factura con datos de pago rellenados
- **THEN** la cabecera de «Facturar a» lleva fondo del color de acento con texto blanco
- **AND** la cabecera de «Datos de pago» va en blanco con borde fino inferior y texto marrón oscuro

#### Scenario: Datos de pago vacíos ocultan la tarjeta
- **WHEN** el usuario exporta una factura sin forma de pago, sin vencimiento y sin realizada por
- **THEN** la tarjeta «Datos de pago» no aparece en el PDF
- **AND** «Facturar A» conserva su anchura con el espacio restante en blanco

#### Scenario: Tarjetas bicolor con email y datos de pago opcionales

- **WHEN** el usuario exporta una factura cuyo cliente tiene email y con forma de pago, vencimiento y realizada por rellenados
- **THEN** la tarjeta «Facturar a» muestra el email del cliente y la tarjeta «Datos de pago» muestra los tres valores
- **AND** si el cliente no tiene email esa fila no aparece en el PDF

#### Scenario: Total por línea con IVA incluido

- **WHEN** el PDF contiene una línea con base 100,00 € e IVA 21 %
- **THEN** la columna Total de esa línea muestra 121,00 €
- **AND** las líneas exentas muestran su importe sin IVA añadido

#### Scenario: Totales sin filas duplicadas
- **WHEN** el usuario exporta una factura sin descuento con base 100,00 € al 21 %
- **THEN** el resumen muestra exactamente `Base 100,00 €`, `IVA 21 % 21,00 €` y `TOTAL 121,00 €`
- **AND** no aparecen las filas `Base total` ni `IVA total`

#### Scenario: Totales con descuento cuadrando
- **WHEN** el usuario exporta una factura con descuento global del 10 % sobre base 1.000,00 € al 21 %
- **THEN** el resumen muestra `Base 1.000,00 €`, `IVA 21 % 189,00 €` (cuota sobre la base descontada), `Descuento 10 % −100,00 €` restando y `TOTAL 1.089,00 €`
- **AND** se cumple Base − Descuento + IVA = TOTAL

#### Scenario: Varios tipos de IVA con un solo descuento
- **WHEN** el usuario exporta una factura con líneas al 21 % y al 10 % y descuento global mayor que cero
- **THEN** el resumen muestra cada par Base/IVA por separado y una única fila `Descuento n%`
- **AND** no aparecen filas de totales globales repetidas

#### Scenario: Los totales no quedan solos en una página
- **WHEN** el usuario exporta una factura larga cuyo bloque de totales cabe en el espacio restante de la última página una vez compactado
- **THEN** no se genera una página adicional que contenga únicamente los totales
- **AND** si el bloque realmente no cabe, pasa íntegro a la página siguiente y `Página X de Y` la cuenta como página real

#### Scenario: Tipografía y pie sin solape
- **WHEN** el usuario exporta una factura con Calibri disponible en el sistema
- **THEN** el documento embebe la fuente Calibri
- **AND** el pie muestra `Página X de Y` con el número total separado correctamente de la palabra «de»

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

La aplicación SHALL permitir gestionar varias empresas con datos totalmente aislados. Cada empresa SHALL tener un nombre visible y un identificador interno (slug) que da nombre a su carpeta de datos. La aplicación SHALL listar las empresas disponibles, crear nuevas empresas, cambiar a una empresa existente y eliminar una empresa distinta de la actual. Al crear una empresa nueva la aplicación SHALL crear su base de datos desde cero con la estructura de tablas completa. La aplicación SHALL recordar de forma global la última empresa utilizada y SHALL preseleccionarla al abrir. Cada empresa SHALL tener su propia configuración de empresa, clientes, series, tipos de IVA y facturas. La empresa actualmente activa SHALL NOT poder eliminarse.

#### Scenario: Crear una nueva empresa
- **WHEN** el usuario crea la empresa «Asesoría María Luisa Ibáñez» desde Configuración
- **THEN** se crea su base de datos con la estructura completa y la empresa aparece en el listado y queda seleccionada

#### Scenario: Nombre visible e identificador interno
- **WHEN** el usuario crea una empresa con nombre «Asesoría María Luisa Ibáñez»
- **THEN** el listado muestra el nombre visible y su carpeta de datos usa el identificador interno correspondiente

#### Scenario: Cambiar a otra empresa
- **WHEN** el usuario elige cambiar a una empresa distinta de la actual
- **THEN** la aplicación pasa a usar la base de datos de esa empresa y sus datos (configuración, clientes, series, facturas) sustituyen a los de la anterior

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

La aplicación SHALL tener un botón para crear una copia de seguridad manual. En la V1 la copia SHALL ser únicamente del archivo SQLite; no se incluyen PDFs ni configuración.

#### Scenario: Crear copia de seguridad
- **WHEN** el usuario pulsa el botón de copia de seguridad y elige dónde guardarla
- **THEN** se genera una copia del archivo SQLite en la ubicación elegida

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

El menú principal SHALL mostrar el nombre, el NIF y el logo de la empresa configurados. El editor de factura SHALL mostrar el logo de la empresa en su cabecera. Los datos mostrados SHALL tomarse de la configuración de empresa.

#### Scenario: Mostrar datos de empresa en el menú principal
- **WHEN** el usuario abre el menú principal con datos de empresa configurados
- **THEN** se muestran el nombre, el NIF y el logo de la empresa

#### Scenario: Mostrar logo en el editor
- **WHEN** el usuario abre una factura con un logo configurado
- **THEN** el logo de la empresa aparece en la cabecera del editor

### Requirement: Ventana

La aplicación SHALL abrir su ventana con las siguientes medidas: en la primera ejecución (sin preferencias de ventana guardadas) SHALL medir 800x600 y SHALL quedar centrada en la pantalla principal; en ejecuciones posteriores SHALL restaurar el tamaño y la posición guardados de la última sesión. El tamaño mínimo de ventana SHALL ser 800x600 y el usuario SHALL poder redimensionar hasta ese mínimo. Con la ventana en su tamaño mínimo, ninguna pantalla SHALL recortar ni ocultar controles: los filtros del Histórico y las filas de alta rápida de IVA y Series en Configuración SHALL reorganizarse en varias líneas cuando el ancho no baste, manteniendo cada grupo de botones de acción unido, y los campos de la cabecera del Editor SHALL repartirse el ancho disponible.

#### Scenario: Primera ejecución abre a 800x600 centrada
- **WHEN** el usuario inicia la aplicación sin preferencias de ventana guardadas
- **THEN** la ventana mide 800x600 y aparece centrada en la pantalla principal

#### Scenario: Siguientes ejecuciones restauran la última sesión
- **WHEN** el usuario cierra la aplicación tras moverla o redimensionarla y vuelve a abrirla
- **THEN** la ventana recupera el tamaño y la posición de la sesión anterior

#### Scenario: Mínimo de redimensionado
- **WHEN** el usuario arrastra el borde de la ventana para hacerla más pequeña
- **THEN** la ventana no puede bajar de 800x600

#### Scenario: Filtros del Histórico con ventana mínima
- **WHEN** la ventana está al mínimo 800x600 y se abre el Histórico
- **THEN** todos los filtros siguen visibles reorganizados en varias líneas y los botones Exportar PDF, Buscar y Volver permanecen accesibles

#### Scenario: Altas rápidas de IVA y Series con ventana mínima
- **WHEN** la ventana está al mínimo 800x600 y se abren las pestañas IVA o Series de Configuración
- **THEN** los campos de alta se reorganizan sin cortarse y los botones Nuevo, Guardar e Inactivar/Activar (o Nuevo y Guardar en Series) permanecen visibles y agrupados

#### Scenario: Cabecera del Editor con ventana mínima
- **WHEN** la ventana está al mínimo 800x600 y se abre una factura
- **THEN** los campos de la cabecera se reparten el ancho disponible sin salirse de la ventana

### Requirement: Facturación mensual por cliente

La aplicación SHALL permitir generar múltiples facturas mensuales para un único cliente desde un diálogo específico. El usuario SHALL seleccionar el cliente, el año, el rango de meses, la serie de numeración y el día del mes que se usará como fecha de cada factura, pudiendo elegir entre un día fijo editable, el primer día del mes o el último día del mes. El usuario SHALL poder configurar las líneas de concepto que se replicarán en cada factura, con la opción de añadir automáticamente el nombre del mes a la descripción de cada línea. El usuario SHALL seleccionar el tipo de IVA y, opcionalmente, el tipo de retención IRPF que se aplicarán a todas las facturas generadas. El sistema SHALL crear una factura por cada mes del rango, asignando a cada una el siguiente número de la serie seleccionado y la fecha correspondiente. Si para un mes ya existe una factura para ese cliente y año, el sistema SHALL mostrar una advertencia con los meses afectados y SHALL permitir al usuario decidir si genera las facturas de todos modos o cancela la operación. Las facturas generadas SHALL aparecer en el histórico y SHALL poder exportarse a PDF.

#### Scenario: Acceso desde el menú principal
- **WHEN** el usuario pulsa la opción "Generar facturas mensuales" en el menú principal
- **THEN** se abre el diálogo de facturación mensual

#### Scenario: Acceso desde el histórico
- **WHEN** el usuario pulsa el botón "Generar mensual" en la pantalla de histórico
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

### Requirement: Exportación múltiple a PDF desde el histórico

La aplicación SHALL permitir exportar a PDF varias facturas seleccionadas en el histórico. Cuando se seleccionen varias facturas, el sistema SHALL preguntar si se desea generar un PDF por factura o un único PDF agrupado. Para la opción "un PDF por factura", el sistema SHALL guardar un archivo por cada factura en la carpeta elegida. Para la opción "único PDF agrupado", el sistema SHALL guardar un solo archivo que contenga todas las facturas seleccionadas.

#### Scenario: Exportar varias facturas a PDF
- **WHEN** el usuario selecciona varias facturas y pulsa "Exportar a PDF"
- **THEN** el sistema pregunta si quiere un PDF por factura o un PDF agrupado
- **AND** genera el resultado elegido

### Requirement: Sistema de diseño visual Apple

La aplicación SHALL aplicar un sistema de diseño visual inspirado en Ajustes de Apple: jerarquía clara, espaciado generoso, agrupación de controles en secciones con fondo de tarjeta, esquinas redondeadas, tipografía con pesos diferenciados y una paleta de acentos coherente. Todos los controles interactivos (botones, campos, tablas, listas) SHALL mostrar un estado visual de `:hover` y `:focused` inmediato y sutil. Los formularios SHALL alinear etiquetas y campos con una cuadrícula coherente. Las tablas y listas SHALL usar filas de altura uniforme, separación clara y estado seleccionado visible pero no agresivo. Además, los paneles de contenido SHALL mantener un margen claro respecto al borde de la ventana y respecto a la barra de menú superior, de modo que los campos y tarjetas no queden pegados al borde ni se perciban solapados con la navegación.

#### Scenario: Pantallas con tarjetas de sección
- **WHEN** el usuario abre Configuración, el Editor o el Histórico
- **THEN** los controles se agrupan en secciones con fondo de tarjeta, esquinas redondeadas y separación respecto al fondo de la ventana

#### Scenario: Estados de foco visibles
- **WHEN** el usuario navega por el formulario con Tab o hace clic en un campo
- **THEN** el control enfocado muestra un anillo o borde de acento sutil y el botón bajo el cursor cambia de tono sin esperar al clic

#### Scenario: Tipografía jerárquica
- **WHEN** el usuario abre cualquier pantalla principal
- **THEN** los títulos son más grandes y en negrita, los subtítulos usan un peso intermedio y los datos secundarios aparecen en un tono más tenue

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

La aplicación SHALL garantizar que cada pantalla principal tenga un tamaño mínimo suficiente para que todo su contenido sea legible y usable. El tamaño mínimo y el tamaño predefinido de cada vista pueden ser iguales o diferentes, pero en ningún caso el usuario podrá reducir la ventana por debajo del mínimo definido para la vista activa. La pantalla de selección de empresa (`Arranque`) SHALL ser de tamaño fijo y no redimensionable. Al cambiar entre vistas, la ventana principal SHALL ajustarse automáticamente al tamaño predefinido de la nueva vista y SHALL centrarse en la pantalla. Los diálogos modales SHALL respetar su propia configuración de tamaño mínimo y predefinido. Algunas vistas (como el Editor) pueden abrirse maximizadas por defecto para aprovechar todo el espacio de pantalla.

#### Scenario: Arranque fijo
- **WHEN** la aplicación muestra la pantalla de selección de empresa
- **THEN** la ventana tiene un tamaño fijo de 760×520, no es redimensionable y no se puede maximizar

#### Scenario: Editor con tamaño mínimo legible
- **WHEN** el usuario abre el Editor de facturas
- **THEN** la ventana se abre maximizada; al restaurarla, no puede ser menor de 1000×760 y todos los controles principales (barra de navegación, cabecera, tabla de líneas, botones de acción y resumen de totales) son visibles sin cortarse

#### Scenario: Configuración con tamaño mínimo
- **WHEN** el usuario abre Configuración
- **THEN** la ventana no puede ser menor de 1000×620

#### Scenario: Histórico con tamaño mínimo
- **WHEN** el usuario abre el Histórico
- **THEN** la ventana no puede ser menor de 1000×600

#### Scenario: Clientes con tamaño mínimo
- **WHEN** el usuario abre Clientes
- **THEN** la ventana no puede ser menor de 1000×600

#### Scenario: Menú principal con tamaño mínimo
- **WHEN** el usuario abre el Menú principal
- **THEN** la ventana tiene un tamaño predefinido de 800×600 y las tarjetas dejan un margen visible respecto al borde inferior de la ventana

#### Scenario: Versiones con tamaño mínimo
- **WHEN** el usuario abre Versiones
- **THEN** la ventana no puede ser menor de 900×500

#### Scenario: Backup con tamaño mínimo
- **WHEN** el usuario abre Backup
- **THEN** la ventana no puede ser menor de 720×450

#### Scenario: Diálogo de facturación mensual
- **WHEN** se abre el diálogo de Generar facturas mensuales
- **THEN** el diálogo no puede ser menor de 920×680

#### Scenario: Centrado al cambiar de vista
- **WHEN** el usuario navega de una pantalla a otra
- **THEN** la ventana se redimensiona al tamaño predefinido de la nueva vista y se centra en la pantalla
