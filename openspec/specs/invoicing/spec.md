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

Al crear o editar una factura, el usuario SHALL poder buscar un cliente por nombre/razón social o NIF con búsqueda incremental mientras escribe. Al seleccionar un cliente, sus datos SHALL cargarse en la factura. Los datos del cliente SHALL poder modificarse desde la factura, y esos cambios SHALL actualizar también la ficha general del cliente.

#### Scenario: Búsqueda incremental
- **WHEN** el usuario escribe caracteres en el campo de búsqueda de cliente
- **THEN** la lista de clientes coincidentes por nombre o NIF se actualiza con cada carácter

#### Scenario: Desplegable de clientes
- **WHEN** el usuario pulsa la flecha del selector de cliente sin texto de búsqueda
- **THEN** la aplicación muestra los clientes activos disponibles

#### Scenario: Selección de cliente
- **WHEN** el usuario selecciona un cliente de la lista
- **THEN** nombre/razón social, NIF, dirección, código postal, localidad y provincia se cargan en la factura

#### Scenario: Modificación del cliente desde la factura
- **WHEN** el usuario modifica un dato de cliente dentro de la factura y guarda
- **THEN** la ficha general del cliente queda actualizada con ese dato

### Requirement: Facturas normales

La aplicación SHALL permitir crear y editar facturas normales con número, fecha, cliente, líneas, descuento general, IVA, observaciones y totales. La fecha de la factura SHALL ser editable mediante un selector/calendario. La introducción de líneas SHALL ser similar a trabajar con una hoja de cálculo.

#### Scenario: Crear factura con datos completos
- **WHEN** el usuario crea una factura con cliente, líneas, descuento, IVA y observaciones y la guarda
- **THEN** la factura se almacena con su número definitivo y aparece en el histórico

#### Scenario: Editar factura emitida
- **WHEN** el usuario modifica la versión actual de una factura en estado Emitida y guarda
- **THEN** tras la confirmación, la versión actual se sobrescribe con los cambios

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

La aplicación SHALL permitir configurar tipos de IVA: tipos porcentuales e IVA exento. Para IVA exento SHALL poder indicarse un motivo o texto de exención. Cada línea SHALL poder tener un tipo de IVA diferente. Los tipos de IVA SHALL poder crearse, modificarse mientras sea seguro, marcarse como inactivos si ya se han utilizado y SHALL NOT eliminarse físicamente si forman parte del histórico. El resumen de una factura SHALL desglosar cada tipo de IVA por separado (base y cuota). Los cálculos SHALL usar BigDecimal; no se permite usar double/float para importes monetarios.

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

La aplicación SHALL tener series de numeración. Inicialmente existirán C (Cocinas), P (Puertas) y R (Rectificativas). Las series C y P SHALL usar el formato `CODIGO-CORRELATIVO-MES` (p. ej. `C-59/7`, `P-35/8`), donde el mes deriva de la fecha de la factura y el correlativo es independiente del mes. La serie R SHALL usar el formato `R-CORRELATIVO` (p. ej. `R-1`) sin mes. Cada serie SHALL tener su propio correlativo. El separador del número SHALL ser la barra (p. ej. `C-59/7`). El correlativo SHALL ser la identidad de la factura; el componente de mes SHALL recalcularse según la fecha y guardarse en cada versión. Las series SHALL poder crearse y configurarse desde la aplicación; las nuevas series normales SHALL seguir el formato `CODIGO-CORRELATIVO-MES` y la serie R es especial y no usa mes. La aplicación SHALL recordar la última serie utilizada y proponerla al crear la siguiente factura. El número SHALL proponerse automáticamente y poder modificarse manualmente. El número SHALL NOT consumirse hasta que la factura se guarda correctamente. SHALL ser configurable por serie el comportamiento con números anulados: continuar hacia delante o reutilizar números anulados; el comportamiento predeterminado SHALL ser continuar hacia delante. En Configuración → Series el usuario SHALL poder ver las series, ver el siguiente número y modificarlo.

#### Scenario: Propuesta de número al crear factura
- **WHEN** el usuario crea una factura en la serie C con fecha 11/08/2026 y el siguiente correlativo es 58
- **THEN** la aplicación propone el número `C-58/8`

#### Scenario: El mes sigue a la fecha
- **WHEN** el usuario cambia la fecha de la factura de julio a agosto y la guarda
- **THEN** el número se guarda con el mes correspondiente a la nueva fecha (p. ej. `C-59/8`) y el correlativo no cambia

#### Scenario: Número manual duplicado
- **WHEN** el usuario introduce manualmente un número que ya pertenece a una factura activa de la misma serie
- **THEN** la aplicación impide guardar e informa del conflicto

#### Scenario: El número no se consume al abandonar
- **WHEN** el usuario cancela una factura sin guardarla
- **THEN** el número propuesto no queda consumido y el siguiente correlativo permanece

#### Scenario: Anulada sin restaurar por número ocupado
- **WHEN** el usuario intenta restaurar una factura anulada cuyo número está ocupado por otra factura activa
- **THEN** la aplicación impide la restauración e informa del motivo

### Requirement: Fecha de trabajo

Al abrir la aplicación, la fecha de trabajo SHALL ser la fecha del sistema. El usuario SHALL poder cambiarla. La fecha de trabajo SHALL usarse como valor inicial de la fecha de las nuevas facturas. Al cambiar la fecha de una factura, el mes del número propuesto SHALL actualizarse automáticamente.

#### Scenario: Fecha inicial al abrir
- **WHEN** el usuario abre la aplicación
- **THEN** la fecha de trabajo es la fecha del sistema

#### Scenario: Cambio de fecha de trabajo
- **WHEN** el usuario cambia la fecha de trabajo y crea una nueva factura
- **THEN** la factura se inicializa con esa fecha de trabajo

### Requirement: Versionado

Las facturas emitidas SHALL poder editarse. Si se edita la versión más reciente de una factura, al guardar SHALL sobrescribirse esa versión en su lugar tras pedir confirmación, manteniendo el mismo número de versión. Si se edita una versión anterior, al guardar SHALL crearse una nueva versión (vN+1) a partir de esa versión sin modificar la versión histórica. Cada versión SHALL guardar todos los datos completos de la factura, la fecha de factura y la fecha/hora en que se creó la versión. No SHALL ser necesario guardar un resumen de diferencias. Cualquier versión SHALL poder abrirse. Las versiones anteriores a la más reciente SHALL NOT modificarse nunca. Anular y restaurar una factura SHALL también crear una nueva versión.

#### Scenario: Guardar sobrescribe la versión actual
- **WHEN** el usuario modifica la última versión de una factura emitida, guarda y confirma la sobrescritura
- **THEN** la versión actual se sobrescribe con los cambios y el número de versión permanece

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

La aplicación SHALL tener un histórico de facturas que muestre cada versión como una fila independiente. El histórico SHALL permitir buscar por serie, cliente/razón social, NIF, fecha desde/hasta, importe desde/hasta y estado, combinando los filtros entre sí. La búsqueda SHALL ejecutarse mediante un botón "Buscar", no en tiempo real. Los resultados SHALL ordenarse por número de factura. Las columnas SHALL ser: fecha, número, versión, cliente, NIF, base, IVA, total y estado. Al seleccionar una fila SHALL poder abrirse esa factura/versión.

#### Scenario: Búsqueda combinando filtros
- **WHEN** el usuario establece una serie, un cliente y un rango de fechas y pulsa Buscar
- **THEN** se muestran todas las versiones de facturas que cumplen los tres filtros

#### Scenario: Búsqueda sin límites de importe
- **WHEN** el usuario deja vacíos los campos de importe desde y hasta y pulsa Buscar
- **THEN** la aplicación no aplica ningún límite de importe y muestra también las facturas con total mayor que cero

#### Scenario: Apertura desde el histórico
- **WHEN** el usuario selecciona una fila del histórico
- **THEN** se abre la factura en la versión correspondiente

### Requirement: Menú y navegación

La aplicación SHALL tener un menú principal con las opciones Nueva factura, Histórico, Configuración, Copia de seguridad y Salir. Dentro de una factura SHALL existir una barra superior con Guardar, Exportar PDF, Versiones, Crear rectificativa, Nueva factura y Volver. Solo SHALL tenerse una factura abierta a la vez. No SHALL existir la opción "Nueva rectificativa" en el menú principal.

#### Scenario: Crear rectificativa desde la factura
- **WHEN** el usuario pulsa "Crear rectificativa" en la barra de una factura abierta
- **THEN** se crea una rectificativa a partir de esa factura

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

La aplicación SHALL tener una pantalla de Configuración que permita configurar: los datos de la empresa (nombre, NIF, dirección, código postal, localidad, provincia y resto de datos necesarios para la cabecera); la cabecera del documento en dos modos, texto con datos de empresa o imagen/logo (el logo se selecciona desde un archivo y permite ajustar tamaño y posición, y los datos de empresa se guardan siempre aunque la cabecera visible use solo el logo); el pie con texto legal libre configurable por el usuario, sin contenido obligatorio (el texto del Excel existente solo sirve como referencia inicial opcional); los tipos de IVA; las series (crear/configurar, ver y modificar el siguiente número, y configurar la reutilización de números anulados); y las carpetas de PDF (carpeta automática de almacenamiento y última carpeta utilizada). La aplicación SHALL recordar preferencias de trabajo: última serie utilizada, tamaño/posición de ventana y última carpeta de exportación.

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

### Requirement: Exportación a PDF

La aplicación SHALL exportar facturas a PDF en A4 vertical, con diseño moderno y profesional inspirado en la información del documento actual sin necesidad de copiarlo. La cabecera SHALL usar el texto de empresa o el logo según configuración. El pie legal SHALL ser configurable y repetirse en todas las páginas. Si hay varias páginas, SHALL repetirse la cabecera y el pie y SHALL aparecer `Página X de Y`. Las descripciones largas SHALL ajustarse automáticamente. Los importes SHALL usar formato español (`1.250,50 €`) y las fechas formato español (`11/08/2026`). Una factura anulada SHALL poder exportarse y SHALL aparecer claramente marcada como `ANULADA`. Si se exporta una versión concreta, el contenido SHALL corresponder exactamente a esa versión. El PDF SHALL usar la configuración actual de empresa, logo, cabecera y pie legal. Los PDF generados SHALL permanecer como documentos independientes. La estructura de almacenamiento SHALL ser `Facturas/AAAA/SERIE/` (p. ej. `Facturas/2026/C/`) y el nombre SHALL ser `CODIGO-CORRELATIVO-MES_vN.pdf` (p. ej. `C-59-7_v1.pdf`), sustituyendo la barra por un guion en el nombre de archivo.

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
- **WHEN** el usuario exporta la versión v1 de la factura C-59/8
- **THEN** se genera el archivo `Facturas/2026/C/C-59-8_v1.pdf`

### Requirement: Persistencia local

La aplicación SHALL guardar los datos en una base de datos SQLite local ubicada en una carpeta de datos de la aplicación, separada de la instalación. La aplicación SHALL ser la vía normal para modificar los datos. La base de datos SHALL contener el histórico completo. Las operaciones importantes de persistencia SHALL ser transaccionales con confirmación y reversión correctas. No SHALL eliminarse físicamente datos históricos que hayan sido utilizados. La aplicación SHALL ejecutarse como una única instancia a la vez.

#### Scenario: Guardado transaccional
- **WHEN** el usuario guarda una factura con sus líneas y su versión
- **THEN** la operación se confirma de forma atómica o se revierte por completo si falla

#### Scenario: Segunda instancia
- **WHEN** el usuario intenta abrir una segunda instancia de la aplicación
- **THEN** la segunda instancia no se abre y se notifica al usuario

### Requirement: Copia de seguridad

La aplicación SHALL tener un botón para crear una copia de seguridad manual. En la V1 la copia SHALL ser únicamente del archivo SQLite; no se incluyen PDFs ni configuración.

#### Scenario: Crear copia de seguridad
- **WHEN** el usuario pulsa el botón de copia de seguridad y elige dónde guardarla
- **THEN** se genera una copia del archivo SQLite en la ubicación elegida

