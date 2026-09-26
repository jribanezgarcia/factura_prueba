# invoicing Specification

## Purpose
Sistema de facturación de escritorio para Windows, local y de un único usuario, que permite crear, editar, buscar y exportar a PDF facturas y rectificativas, sustituyendo el proceso manual en hoja de cálculo.

## Requirements

### Requirement: Clientes

La aplicación SHALL permitir gestionar una ficha de clientes con nombre/razón social, NIF, dirección, código postal, localidad, provincia y email. El nombre, el NIF, la dirección, el código postal, la localidad y la provincia SHALL ser obligatorios, y sus etiquetas SHALL marcarse con un asterisco tanto en la ficha de cliente como en el bloque Cliente del editor de factura. El email SHALL ser opcional, pero cuando se informe SHALL tener un formato válido. El NIF SHALL validarse como DNI, NIE o NIF/CIF español, y la aplicación SHALL distinguir el aviso según el error: si el campo está vacío, «El NIF/NIE es obligatorio.»; si no tiene la forma de ningún documento, «Formato NIF/NIE incorrecto. Debe ser como 12345678Z (DNI), X1234567L (NIE) o B12345674 (CIF).»; si tiene la forma pero la letra o el carácter de control no corresponde, «La letra no es correcta.». El código postal SHALL tener cinco dígitos y comenzar entre 01 y 52. El NIF SHALL guardarse siempre en mayúsculas, se escriba como se escriba.

Los datos del cliente SHALL comprobarse al intentar guardar, y no al abandonar un campo ni al pulsar Enter en él: la aplicación SHALL NOT mostrar avisos ni retener el foco mientras el usuario rellena la ficha o el bloque Cliente, de modo que pueda moverse entre campos, cancelar la ficha o salir del editor en cualquier momento. Al intentar guardar con algún dato incorrecto, la aplicación SHALL marcar como erróneos todos los campos incorrectos y SHALL mostrar un único aviso, el del primer dato incorrecto. Estas comprobaciones SHALL aplicarse también al guardar una factura desde el editor, al generar facturas mensuales y al crear rectificativas, de modo que no pueda guardarse ninguna factura nueva o editada sin cliente o con datos de cliente incompletos o incorrectos.

Un cliente sin facturas asociadas SHALL poder eliminarse físicamente. Un cliente con facturas asociadas SHALL NOT poder eliminarse físicamente y SHALL poder marcarse como inactivo. Un cliente inactivo SHALL NOT aparecer normalmente al crear nuevas facturas, SHALL seguir apareciendo en el histórico y sus facturas SHALL seguir siendo consultables.

El NIF SHALL ser único: dos clientes SHALL NOT tener el mismo NIF, estén activos o inactivos, y la base de datos SHALL impedirlo. Al dar de alta un cliente con el NIF de otro cliente activo, o al modificar un cliente poniéndole el NIF de otro, la aplicación SHALL NOT guardarlo, SHALL marcar el NIF como erróneo y SHALL avisar indicando a qué cliente pertenece. Al dar de alta un cliente con el NIF de un cliente inactivo, la aplicación SHALL ofrecer recuperarlo: si el usuario acepta, ese cliente SHALL volver a estar activo con los datos recién escritos y SHALL conservar sus facturas; si no acepta, SHALL NOT guardarse nada y la ficha SHALL seguir abierta. Al guardar una factura con un cliente escrito a mano cuyo NIF ya pertenece a un cliente de la lista, la factura SHALL asociarse a ese cliente, SHALL NOT crearse otro y la ficha del cliente SHALL NOT modificarse.

#### Scenario: NIF inválido al alta o edición de cliente
- **WHEN** el usuario escribe un NIF, un código postal o un email incorrectos en la ficha de cliente o en el editor y abandona el campo mediante Tab, Enter o haciendo clic en otro control
- **THEN** no se muestra ningún aviso ni se marca el campo
- **AND** el foco pasa al campo elegido por el usuario

#### Scenario: Salir con un dato incorrecto
- **WHEN** hay un dato de cliente incorrecto y el usuario pulsa Cancelar en la ficha o Volver en el editor
- **THEN** la ficha se cierra o el editor sale como con cualquier otro dato, avisando de los cambios sin guardar si los hay

#### Scenario: NIF con formato incorrecto
- **WHEN** el usuario intenta guardar un cliente con el NIF `123`
- **THEN** la aplicación no guarda el cliente
- **AND** muestra un único aviso «Formato NIF/NIE incorrecto. Debe ser como 12345678Z (DNI), X1234567L (NIE) o B12345674 (CIF).»

#### Scenario: NIF con la letra incorrecta
- **WHEN** el usuario intenta guardar un cliente con el NIF `12345678A`
- **THEN** la aplicación no guarda el cliente
- **AND** muestra un único aviso «La letra no es correcta.»

#### Scenario: NIF vacío
- **WHEN** el usuario intenta guardar un cliente sin NIF
- **THEN** la aplicación no guarda el cliente
- **AND** muestra un único aviso «El NIF/NIE es obligatorio.»

#### Scenario: Código postal obligatorio
- **WHEN** el usuario intenta guardar un cliente sin código postal
- **THEN** la aplicación no guarda el cliente y avisa de que el código postal es obligatorio

#### Scenario: Cliente sin email
- **WHEN** el usuario guarda un cliente con todos los datos obligatorios correctos y sin email
- **THEN** el cliente se guarda

#### Scenario: Email mal escrito
- **WHEN** el usuario intenta guardar un cliente con un email mal escrito
- **THEN** la aplicación no guarda el cliente y avisa de que revise el formato del correo electrónico

#### Scenario: Salvaguarda al guardar cliente
- **WHEN** el usuario intenta guardar un cliente con el NIF vacío y el código postal vacío
- **THEN** la aplicación no guarda el cliente
- **AND** los dos campos quedan marcados como erróneos
- **AND** se muestra un único aviso, el del NIF

#### Scenario: Factura sin cliente o con datos de cliente incompletos
- **WHEN** el usuario intenta guardar una factura desde el editor sin datos de cliente, o genera facturas mensuales o crea una rectificativa para un cliente con algún dato obligatorio vacío o incorrecto
- **THEN** la factura no se guarda ni se genera
- **AND** la aplicación avisa del primer dato de cliente que falta o es incorrecto

#### Scenario: NIF escrito en minúsculas
- **WHEN** el usuario guarda un cliente escribiendo el NIF en minúsculas
- **THEN** el cliente se guarda con el NIF en mayúsculas

#### Scenario: Borrado físico de cliente sin facturas
- **WHEN** el usuario elimina un cliente que no tiene facturas asociadas
- **THEN** el cliente se elimina físicamente de la base de datos

#### Scenario: Bloqueo de borrado de cliente con facturas
- **WHEN** el usuario intenta eliminar un cliente que tiene facturas asociadas
- **THEN** la aplicación no permite el borrado y ofrece marcar el cliente como inactivo

#### Scenario: Cliente inactivo en histórico
- **WHEN** el usuario busca en el histórico facturas de un cliente inactivo
- **THEN** las facturas aparecen y son consultables

#### Scenario: NIF de otro cliente
- **WHEN** el usuario intenta guardar un cliente con el NIF `B88888888`, que ya tiene el cliente activo «Cliente Ejemplo S.L.»
- **THEN** la aplicación no guarda el cliente
- **AND** el NIF queda marcado como erróneo y el aviso dice «Ya existe un cliente con el NIF B88888888: Cliente Ejemplo S.L.»

#### Scenario: NIF de un cliente dado de baja
- **WHEN** el usuario da de alta un cliente con el NIF de un cliente inactivo
- **THEN** la aplicación pregunta si quiere volver a darlo de alta con los datos que acaba de escribir
- **AND** si acepta, ese cliente vuelve a estar activo con esos datos y conserva sus facturas, sin que se cree un cliente nuevo

#### Scenario: Factura con un cliente escrito a mano que ya existe
- **WHEN** el usuario guarda una factura escribiendo a mano los datos de un cliente cuyo NIF ya está en la lista
- **THEN** la factura queda asociada a ese cliente y no se crea otro
- **AND** la ficha del cliente no cambia, y la factura conserva los datos tal como se escribieron

### Requirement: Búsqueda de clientes al crear factura

Al crear o editar una factura, el usuario SHALL poder buscar un cliente por nombre/razón social o NIF con búsqueda incremental mientras escribe. Al seleccionar un cliente, sus datos —incluido el email— SHALL cargarse en la factura. Los datos del cliente (email incluido) SHALL poder modificarse desde la factura, y esos datos modificados SHALL quedar siempre en la factura que se guarda. La aplicación SHALL NOT modificar la ficha general del cliente sin preguntar: al guardar una factura cuyos datos de cliente difieran de los de su ficha, la aplicación SHALL pedir confirmación para guardar también esos cambios en la ficha, y SHALL actualizarla solo si el usuario acepta. El NIF SHALL identificar al cliente de la factura: si el usuario cambia en la factura el NIF del cliente elegido, la factura SHALL tratarse como la de otro cliente, y la ficha del cliente elegido SHALL NOT modificarse ni ofrecerse para actualizar. Al guardar una factura con datos de cliente correctos cuyo NIF no tiene ningún cliente de la lista, la aplicación SHALL avisar de que ese cliente se guardará en la lista junto con la factura, con Aceptar y Cancelar: si el usuario acepta, SHALL guardarse la factura y el cliente; si cancela, SHALL NOT guardarse nada y el editor SHALL seguir abierto con los datos escritos. La ficha general de clientes SHALL incluir el campo email.

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
- **WHEN** el usuario modifica un dato de cliente (email incluido) dentro de la factura, guarda y acepta la pregunta de actualizar su ficha
- **THEN** la factura se guarda con el dato modificado
- **AND** la ficha general del cliente queda actualizada con ese dato

#### Scenario: Modificación del cliente desde la factura sin actualizar su ficha
- **WHEN** el usuario modifica un dato de cliente dentro de la factura, guarda y cancela la pregunta de actualizar su ficha
- **THEN** la factura se guarda con el dato modificado
- **AND** la ficha general del cliente conserva sus datos anteriores

#### Scenario: Factura con los datos del cliente sin tocar
- **WHEN** el usuario guarda una factura sin haber cambiado ningún dato del cliente
- **THEN** la aplicación no pregunta nada sobre la ficha del cliente

#### Scenario: Cliente nuevo escrito a mano
- **WHEN** el usuario guarda una factura con los datos correctos de un cliente cuyo NIF no está en la lista
- **THEN** la aplicación avisa de que el cliente se guardará en la lista junto con la factura
- **AND** si el usuario acepta, la factura se guarda y el cliente aparece en la lista de clientes

#### Scenario: Cliente nuevo cancelado
- **WHEN** el usuario guarda una factura con un cliente cuyo NIF no está en la lista y cancela el aviso
- **THEN** no se guardan ni la factura ni el cliente
- **AND** el editor sigue abierto con los datos escritos

#### Scenario: Otro NIF sobre el cliente elegido
- **WHEN** el usuario elige un cliente de la lista, cambia su NIF en la factura por uno que no tiene ningún cliente y guarda
- **THEN** la aplicación no pregunta si actualizar la ficha del cliente elegido y avisa de que el cliente nuevo se guardará en la lista
- **AND** la ficha del cliente elegido no cambia

### Requirement: Facturas normales

La aplicación SHALL permitir crear y editar facturas normales con número, fecha, cliente, líneas, descuento general, IVA, observaciones, totales y tres datos de pago opcionales: forma de pago, fecha de vencimiento y realizada por. Estos datos de pago SHALL quedar guardados en la factura y SHALL aparecer en el PDF solo cuando estén rellenos. Una factura SHALL ser una sola: editarla la sobrescribe, sin guardar versiones anteriores. Al guardar una factura ya emitida, la aplicación SHALL pedir confirmación antes de sobrescribirla. La fecha de la factura SHALL ser editable mediante un selector/calendario. La introducción de líneas SHALL ser similar a trabajar con una hoja de cálculo.

#### Scenario: Crear factura con datos completos
- **WHEN** el usuario crea una factura con cliente, líneas, descuento, IVA y observaciones y la guarda
- **THEN** la factura se almacena con su número definitivo y aparece en el histórico

#### Scenario: Editar factura emitida
- **WHEN** el usuario modifica una factura en estado Emitida y pulsa Guardar
- **THEN** la aplicación pide confirmación y, tras aceptar, la factura se sobrescribe con los cambios, sin crear otra

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

#### Scenario: Descripción larga
- **WHEN** el usuario escribe en una línea una descripción más larga que el ancho de su columna y pulsa Enter
- **THEN** la fila crece y muestra la descripción entera, partida en varias líneas

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

La aplicación SHALL permitir configurar tipos de IVA: tipos porcentuales e IVA exento. Para IVA exento SHALL poder indicarse un motivo o texto de exención. Cada línea SHALL poder tener un tipo de IVA diferente. Los tipos de IVA SHALL poder crearse, modificarse mientras sea seguro, marcarse como inactivos si ya se han utilizado y SHALL NOT eliminarse físicamente si forman parte del histórico. El alta y la edición de un tipo de IVA, y también de un tipo de retención, SHALL hacerse en una ficha propia que se abre desde la tabla de Configuración. El nombre de un tipo de IVA SHALL ser único entre los tipos de IVA, y el de un tipo de retención entre los tipos de retención; la base de datos SHALL impedir los repetidos, y al intentar guardar un nombre que ya tiene otro tipo la aplicación SHALL NOT guardarlo, SHALL marcar el nombre como erróneo y SHALL avisar. El porcentaje de un tipo que ya aparece en facturas SHALL NOT poder modificarse, un tipo existente SHALL NOT poder pasar de porcentaje a exento ni al revés, y SHALL NOT poder convertirse en suplido ni dejar de serlo. En la exportación a PDF, el resumen de la factura SHALL desglosar cada tipo de IVA por separado (base y cuota), y en el editor la aplicación SHALL mostrar la base total, el IVA total y el total general como valores separados. Los cálculos SHALL usar BigDecimal; no se permite usar double/float para importes monetarios.

#### Scenario: Líneas con distintos tipos de IVA
- **WHEN** una factura tiene líneas con tipos de IVA diferentes, incluida una exenta
- **THEN** el resumen muestra cada tipo por separado: base 21% e IVA 21%, base 10% e IVA 10%, y base exenta con IVA 0% y su motivo de exención

#### Scenario: Inactivar tipo de IVA usado
- **WHEN** el usuario intenta inactivar un tipo de IVA que ya aparece en facturas del histórico
- **THEN** el tipo pasa a inactivo, no se ofrece para nuevas facturas y el histórico se conserva intacto

#### Scenario: Alta de un tipo de IVA en su ficha
- **WHEN** el usuario pulsa Nuevo en la sección IVA de Configuración, rellena el nombre y el porcentaje y guarda la ficha
- **THEN** el tipo aparece en la tabla y queda disponible para las facturas nuevas

#### Scenario: Porcentaje bloqueado en un tipo usado
- **WHEN** el usuario abre la ficha de un tipo de IVA o de retención que ya aparece en facturas
- **THEN** el porcentaje se muestra pero no se puede modificar, y el resto de los datos sí

#### Scenario: Eliminar un tipo sin uso
- **WHEN** el usuario elimina un tipo de IVA o de retención que no aparece en ninguna factura y confirma
- **THEN** el tipo desaparece de la tabla

#### Scenario: Eliminar un tipo en uso
- **WHEN** el usuario intenta eliminar un tipo de IVA o de retención que ya aparece en facturas
- **THEN** la aplicación no lo permite y propone desactivarlo en su ficha

#### Scenario: Nombre de tipo repetido
- **WHEN** el usuario intenta guardar un tipo de IVA con el nombre «IVA 21%», que ya tiene otro tipo de IVA
- **THEN** la aplicación no lo guarda
- **AND** el nombre queda marcado como erróneo y el aviso dice «Ya existe un tipo de IVA con el nombre IVA 21%.»

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

### Requirement: Numeración de facturas por series

La aplicación SHALL tener series de numeración. En una instalación nueva la aplicación SHALL NO crear ninguna serie por defecto: el listado de series comienza vacío y el usuario las crea a mano. Cada serie SHALL tener su propio correlativo. El correlativo SHALL ser la identidad de la factura; el componente de fecha (mes o año) SHALL recalcularse según la fecha y guardarse con la factura. Al editar una factura ya emitida, su fecha SHALL poder cambiar dentro del mismo año, pero SHALL NOT pasar a otro año: la factura se queda siempre en la numeración de su año. La base de datos SHALL impedir que dos facturas de la misma serie y el mismo año tengan el mismo correlativo. El correlativo de cada serie SHALL ser independiente por ejercicio: cada año de trabajo reinicia su propia cuenta sobre el siguiente número de ese año, sin afectar al correlativo de otros años. Las series SHALL poder crearse y configurarse desde la aplicación. La aplicación SHALL recordar la última serie utilizada y proponerla al crear la siguiente factura. El número SHALL proponerse automáticamente y poder modificarse manualmente. El número SHALL NOT consumirse hasta que la factura se guarda correctamente. El siguiente correlativo de una serie SHALL calcularse a partir de sus propias facturas: el mayor correlativo usado ese año más uno, o 1 si la serie no tiene ninguna factura ese año. La aplicación SHALL NOT guardar contadores de numeración. Una factura anulada SHALL conservar su correlativo, que SHALL NOT volver a proponerse. Los correlativos que queden libres por el borrado físico de facturas SHALL ofrecerse al guardar una factura nueva con el número propuesto: la aplicación SHALL avisar del menor número libre con dos botones, uno para usarlo y otro para continuar con el número propuesto. Si el usuario ha escrito el número a mano, SHALL respetarse sin preguntar. En Configuración → Series el usuario SHALL poder ver las series y su siguiente número, que SHALL NOT poder modificarse a mano.

Cada serie SHALL tener un campo `sufijo_fecha` con tres opciones posibles: `MES` (formato CODIGO-CORRELATIVO/MES o CORRELATIVO/MES si no hay código), `ANIO` (formato CODIGO-CORRELATIVO-ANIO o CORRELATIVO-ANIO si no hay código) y `NINGUNO` (formato CODIGO-CORRELATIVO o solo CORRELATIVO si no hay código). El campo `codigo` de una serie SHALL poder estar vacío, en cuyo caso el número NO tendrá prefijo de letra; solo SHALL admitirse una serie sin código a la vez, de modo que otra serie en blanco se rechaza y se identifica la serie por su descripción. El formato predeterminado para series nuevas SHALL ser `MES`. Los correlativos libres SHALL buscarse dentro del mismo año: un correlativo SHALL considerarse usado cuando lo tiene una factura de esa serie y ese ejercicio, esté activa o anulada.

#### Scenario: Propuesta de número con formato MES (actual)
- **WHEN** el usuario crea una factura en la serie C con formato MES, fecha 11/08/2026 y el siguiente correlativo de 2026 es 58
- **THEN** la aplicación propone el número `C-58/8`

#### Scenario: Propuesta de número con formato ANIO sin código
- **WHEN** el usuario crea una factura en una serie sin código, formato ANIO, fecha 15/07/2026 y el siguiente correlativo de 2026 es 56
- **THEN** la aplicación propone el número `56-2026`

#### Scenario: Propuesta de número con formato ANIO con código
- **WHEN** el usuario crea una factura en la serie C con formato ANIO, fecha 15/07/2026 y el siguiente correlativo de 2026 es 56
- **THEN** la aplicación propone el número `C-56-2026`

#### Scenario: Propuesta de número con formato NINGUNO sin código
- **WHEN** el usuario crea una factura en una serie sin código, formato NINGUNO y el siguiente correlativo es 56
- **THEN** la aplicación propone el número `56`

#### Scenario: Propuesta de número con formato NINGUNO con código
- **WHEN** el usuario crea una factura en la serie R con formato NINGUNO y el siguiente correlativo es 1
- **THEN** la aplicación propone el número `R-1`

#### Scenario: El mes sigue a la fecha con formato MES
- **WHEN** el usuario cambia la fecha de la factura de julio a agosto y la guarda con formato MES
- **THEN** el número se guarda con el mes correspondiente a la nueva fecha (p. ej. `C-59/8`) y el correlativo no cambia

#### Scenario: El año sigue a la fecha con formato ANIO
- **WHEN** el usuario crea una factura con fecha de 2027 en una serie con formato ANIO
- **THEN** el número lleva el año de esa fecha (p. ej. `1-2027`)

#### Scenario: Número manual duplicado
- **WHEN** el usuario introduce manualmente un número que ya tiene otra factura de la misma serie y el mismo año, esté activa o anulada
- **THEN** la aplicación impide guardar e informa del conflicto

#### Scenario: El número no se consume al abandonar
- **WHEN** el usuario cancela una factura sin guardarla
- **THEN** el número propuesto no queda consumido y el siguiente correlativo permanece

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
- **WHEN** la serie C tiene facturas hasta el correlativo 57 en 2026 y el usuario arranca con fecha de trabajo en 2025 y crea una factura
- **THEN** la aplicación propone el correlativo 1 para 2025, porque la serie no tiene facturas de ese año, y la numeración de 2026 no cambia

#### Scenario: Reutilización de números borrados
- **WHEN** la serie C tiene en 2026 facturas con los correlativos 1, 3 y 5, porque las del 2 y el 4 se borraron
- **THEN** al guardar una factura nueva de 2026 con el número propuesto, la aplicación ofrece el correlativo 2, el menor hueco libre, o continuar con el 6

#### Scenario: Reutilización de anulados limitada al año
- **WHEN** la serie C tiene en 2025 una factura anulada con correlativo 5, el mayor de ese año, y el usuario crea otra factura en 2025
- **THEN** la aplicación propone el correlativo 6: la factura anulada conserva el 5 y no se vuelve a ofrecer

#### Scenario: El siguiente número sale de las facturas
- **WHEN** la serie A tiene en 2026 las facturas 1, 2 y 3, y el usuario crea otra factura de 2026
- **THEN** la aplicación propone el correlativo 4

#### Scenario: Borrar la última factura libera su número
- **WHEN** la serie A tiene en 2026 las facturas 1, 2 y 3 y el usuario borra la 3
- **THEN** al crear otra factura de 2026 la aplicación propone el correlativo 3

#### Scenario: Una factura emitida no cambia de año
- **WHEN** el usuario edita una factura emitida de 2026, le pone una fecha de 2027 y guarda
- **THEN** la factura no se guarda
- **AND** la aplicación avisa «Una factura emitida no puede cambiar de año. Si es de otro año, anúlala y crea una nueva.»

#### Scenario: Usar el número libre
- **WHEN** la serie tiene un número libre y el usuario, al guardar una factura nueva con el número propuesto, elige usarlo
- **THEN** la factura se guarda con el número libre

#### Scenario: Continuar tras un número libre
- **WHEN** la serie tiene un número libre y el usuario, al guardar una factura nueva con el número propuesto, elige continuar
- **THEN** la factura se guarda con el número propuesto

#### Scenario: Número a mano con números libres
- **WHEN** la serie tiene un número libre y el usuario escribe a mano otro número que no tiene ninguna factura y guarda
- **THEN** la aplicación no pregunta por el número libre
- **AND** la factura se guarda con el número escrito

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

### Requirement: Estados de factura

Una factura SHALL tener uno de dos estados: Emitida o Anulada. No SHALL existir estado Borrador en la V1. Una factura Emitida SHALL poder editarse. Una factura Anulada SHALL NOT poder editarse, SHALL poder consultarse, SHALL poder exportarse a PDF y SHALL poder restaurarse a Emitida. Anular y restaurar SHALL requerir confirmación. Restaurar una factura anulada SHALL devolverla al estado Emitida, conservando su número.

#### Scenario: Consultar factura anulada
- **WHEN** el usuario abre una factura anulada
- **THEN** la factura se muestra en modo consulta, sin permitir edición, y aparece marcada como anulada

#### Scenario: Restaurar factura anulada
- **WHEN** el usuario confirma la restauración de una factura anulada
- **THEN** la factura vuelve al estado Emitida con su mismo número

### Requirement: Rectificativas

La aplicación SHALL permitir crear facturas rectificativas usando la serie independiente R (`R-1`, `R-2`, ...), que no distinguen entre cocina y puerta. Las rectificativas SHALL crearse desde una factura existente; no SHALL existir una opción independiente de "Nueva rectificativa" en el menú principal. Al crear una rectificativa SHALL copiarse los datos de la factura original: cliente, líneas, cantidades, descripciones, precios, IVA, descuento y observaciones. La rectificativa SHALL indicar qué factura rectifica apuntando a ella: la referencia que se muestra y se imprime SHALL ser siempre el número de la factura original y SHALL NOT poder modificarse manualmente. La fecha de una rectificativa SHALL inicializarse con la fecha de trabajo actual y poder cambiarse. Una rectificativa SHALL poder ser parcial o total. Una rectificativa SHALL poder rectificar a otra rectificativa.

#### Scenario: Crear rectificativa desde factura
- **WHEN** el usuario crea una rectificativa desde una factura C-59/8
- **THEN** se crea una factura en la serie R con los datos copiados y la referencia a C-59/8 generada automáticamente

#### Scenario: Modificar referencia
- **WHEN** el usuario abre una rectificativa para editarla
- **THEN** la referencia muestra el número de la factura original y no se puede modificar

### Requirement: Histórico de facturas

La aplicación SHALL tener un histórico de facturas que muestre cada factura en una sola fila, con sus datos actuales. El histórico SHALL permitir buscar por serie, cliente/razón social, NIF, fecha desde/hasta, importe desde/hasta y estado, combinando los filtros entre sí. La búsqueda SHALL ejecutarse mediante un botón "Buscar", no en tiempo real. Al entrar en el histórico, los filtros de fecha SHALL venir puestos del 1 de enero al 31 de diciembre del año de la fecha de trabajo y la tabla SHALL mostrar ya las facturas de ese año, sin pulsar Buscar. Si al buscar algún filtro no es válido —un importe mal escrito, una fecha desde posterior a la fecha hasta o un importe desde mayor que el importe hasta—, la aplicación SHALL NOT buscar, SHALL marcar como erróneos los campos afectados y SHALL mostrar un único aviso que diga qué filtro corregir. Los resultados SHALL ordenarse por número de factura. Las columnas SHALL ser: fecha, número, cliente, NIF, base, IVA, total y estado. Al seleccionar una fila SHALL poder abrirse esa factura. La tabla SHALL permitir seleccionar varias filas a la vez. El histórico SHALL ofrecer exportar directamente a PDF las filas seleccionadas sin necesidad de abrir la factura: con una selección se generará un único PDF preguntando dónde guardarlo; con varias selecciones se elegirá una carpeta de destino y se generarán todos los PDF en esa carpeta con sus nombres propuestos, informando al finalizar del resultado de cada generación.

#### Scenario: Búsqueda combinando filtros
- **WHEN** el usuario establece una serie, un cliente y un rango de fechas y pulsa Buscar
- **THEN** se muestran todas las facturas que cumplen los tres filtros

#### Scenario: Búsqueda sin límites de importe
- **WHEN** el usuario deja vacíos los campos de importe desde y hasta y pulsa Buscar
- **THEN** la aplicación no aplica ningún límite de importe y muestra también las facturas con total mayor que cero

#### Scenario: Apertura desde el histórico
- **WHEN** el usuario selecciona una fila del histórico
- **THEN** se abre esa factura en el editor

#### Scenario: Selección múltiple en la tabla

- **WHEN** el usuario mantiene Ctrl o Shift mientras hace clic sobre filas del histórico
- **THEN** quedan seleccionadas simultáneamente todas las filas marcadas

#### Scenario: Exportar una factura seleccionada

- **WHEN** el usuario selecciona una única fila del histórico y pulsa Exportar PDF
- **THEN** la aplicación propone guardar un PDF con el nombre propuesto para esa factura y lo genera sin abrir el editor

#### Scenario: Exportar varias facturas en lote

- **WHEN** el usuario selecciona varias filas del histórico y pulsa Exportar PDF
- **THEN** la aplicación pide una carpeta de destino una sola vez y genera en ella un PDF por cada fila seleccionada con su nombre propuesto
- **AND** al terminar informa cuántos PDF se generaron correctamente y cuáles fallaron

#### Scenario: Una fila por factura
- **WHEN** una factura se ha editado varias veces o se ha anulado
- **THEN** aparece en el histórico una sola vez, con sus datos actuales y su estado

#### Scenario: Histórico al entrar
- **WHEN** el usuario entra en el histórico con fecha de trabajo en 2026
- **THEN** las fechas desde y hasta vienen puestas del 01/01/2026 al 31/12/2026
- **AND** la tabla muestra ya las facturas de 2026

#### Scenario: Importe mal escrito
- **WHEN** el usuario escribe «12,5x» en el importe desde y pulsa Buscar
- **THEN** la aplicación no busca, marca el importe desde como erróneo y avisa de que no es un importe válido

#### Scenario: Fechas al revés
- **WHEN** el usuario pone una fecha desde posterior a la fecha hasta y pulsa Buscar
- **THEN** la aplicación no busca, marca las dos fechas como erróneas y avisa de que la fecha desde es posterior a la fecha hasta

### Requirement: Menú y navegación

La aplicación SHALL tener un menú principal con las opciones Nueva factura, Facturar mes, Histórico, Clientes, Configuración, Copia de seguridad y Salir. Cada opción SHALL nombrar la misma acción que el botón que la ejecuta en el resto de la aplicación. Cuando el ancho de una barra de iconos no permita mostrar el nombre completo, el botón SHALL poder usar una forma breve de ese nombre y SHALL llevar el nombre completo en su tooltip; SHALL NOT usar un nombre distinto. Dentro de una factura SHALL existir una barra superior con Guardar, Exportar, Rectificar, Anular o Restaurar según el estado, Nueva y Volver. En todas las pantallas salvo el menú principal SHALL existir una barra de navegación superior que permita acceder a Menú principal, Nueva factura, Histórico, Clientes, Configuración, Copia de seguridad y Salir.

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

### Requirement: Cambios sin guardar

Si hay cambios sin guardar, la aplicación SHALL ofrecer tres opciones: Guardar y volver/salir, Descartar cambios y volver/salir, y Cancelar. Si no hay cambios, la aplicación SHALL permitir salir normalmente.

La confirmación SHALL pedirse ante **cualquier** navegación que abandone una vista con cambios sin guardar, no solo al pulsar Volver o al cerrar la aplicación: los botones de la barra de navegación, las entradas del menú principal y la apertura de una factura desde el Histórico SHALL pasar por la misma confirmación. Si el usuario cancela, la aplicación SHALL permanecer en la vista actual sin cambiar de pantalla.

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

### Requirement: Atajos de teclado

La aplicación SHALL proporcionar los atajos Ctrl+N para Nueva factura, Ctrl+S para Guardar, Ctrl+F para Buscar, Ctrl+P para Exportar y Esc para volver/cancelar cuando corresponda.

#### Scenario: Guardar con atajo
- **WHEN** el usuario pulsa Ctrl+S en una factura abierta
- **THEN** la factura se guarda igual que con el botón Guardar, pidiendo confirmación si ya estaba emitida

### Requirement: Configuración

La aplicación SHALL tener una pantalla de Configuración que permita configurar: los datos de la empresa (nombre, NIF, dirección, código postal, localidad, provincia y resto de datos necesarios para la cabecera); la cabecera del documento en dos modos, texto con datos de empresa o imagen/logo; el pie con texto legal libre configurable por el usuario; el tema de apariencia de la interfaz; los tipos de IVA; los tipos de retención de IRPF; las series (crear y configurar en su ficha, ver el siguiente número y eliminar series sin facturas); las carpetas de PDF; el color de acento usado en los PDF exportados; y la acción «Cambiar de empresa», que vuelve a la pantalla de arranque. El color SHALL guardarse como preferencia `color_pdf`; si nunca se configura, los PDF SHALL usar arena Alcazaba (`#B08D57`), y del color elegido SHALL derivarse el resto de tonos del documento. La aplicación SHALL recordar preferencias de trabajo: última serie utilizada, última carpeta de exportación y tema de apariencia. El tema SHALL guardarse en cada empresa, igual que el color del PDF, de modo que cada empresa conserve su propio tema. Al entrar en una empresa o cambiar a otra SHALL aplicarse el tema guardado en ella y, si nunca se ha guardado ninguno, el tema biblioteca8. La pantalla de arranque, que se muestra antes de elegir empresa, SHALL usar el tema de la última empresa abierta. El tamaño y la posición de la ventana SHALL NOT recordarse: cada pantalla se abre con el tamaño que le corresponde.

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
- **WHEN** el usuario abre Configuración → Series
- **THEN** ve el siguiente número de cada serie, calculado a partir de sus facturas del año de trabajo, y no puede cambiarlo a mano

#### Scenario: Cambiar el tema de la interfaz
- **WHEN** el usuario selecciona un tema en Configuración y guarda
- **THEN** el tema se aplica de inmediato y queda guardado en la empresa activa para las siguientes sesiones

#### Scenario: Cada empresa conserva su tema
- **WHEN** el usuario guarda el tema omarchy en una empresa, cambia a otra empresa que tiene guardado el tema esmeralda
- **THEN** la interfaz pasa a mostrarse con esmeralda
- **AND** al volver a la primera empresa se muestra de nuevo con omarchy

#### Scenario: Empresa sin tema guardado
- **WHEN** el usuario entra en una empresa en la que nunca se ha guardado un tema
- **THEN** la interfaz se muestra con el tema biblioteca8, aunque la empresa anterior tuviera otro

#### Scenario: El arranque usa el tema de la última empresa
- **WHEN** el usuario abre la aplicación y la última empresa abierta tiene guardado el tema sakura
- **THEN** la pantalla de arranque se muestra con sakura

#### Scenario: Cambiar el color del PDF
- **WHEN** el usuario elige un color en Configuración y guarda
- **THEN** los nuevos PDF usan ese color de acento y sus tonos derivados
- **AND** si se restablece el valor por defecto o la preferencia no existe, se usa `#B08D57`

#### Scenario: Gestionar empresas desde Configuración
- **WHEN** el usuario abre Configuración
- **THEN** encuentra la acción «Cambiar de empresa» y ninguna opción para crear o eliminar empresas, que están en la pantalla de arranque

#### Scenario: Configurar tipos de retención
- **WHEN** el usuario añade un tipo de retención del 15% con nombre "IRPF 15%" en Configuración
- **THEN** ese tipo queda disponible para seleccionar en las facturas de esa empresa

#### Scenario: Campos de empresa legibles a 1024×768
- **WHEN** el usuario abre la sección Empresa de Configuración en el tamaño mínimo de ventana
- **THEN** los campos Nombre / razón social, Actividad y Localidad se ven enteros, sin recortes

#### Scenario: La ventana no recuerda posición ni tamaño
- **WHEN** el usuario cierra la aplicación con la ventana movida y vuelve a abrirla
- **THEN** la pantalla se abre con su tamaño propio y no se guarda la posición ni el tamaño anteriores

### Requirement: Configuración organizada por secciones

La pantalla de Configuración SHALL organizarse en secciones navegables desde una lista lateral, en lugar de pestañas. Las secciones SHALL ser: Empresa, Cabecera y pie, PDF y apariencia, IVA, Retenciones y Series. El botón de guardado general SHALL mostrarse únicamente en las secciones cuyos datos guarda (Empresa, Cabecera y pie, y PDF y apariencia) y SHALL ocultarse en las secciones que se administran fila a fila (IVA, Retenciones y Series), donde cada una conserva sus propias acciones. El tema de la aplicación SHALL presentarse en la sección PDF y apariencia, junto al color del PDF, por tratarse de una preferencia de la empresa activa.

#### Scenario: Navegación entre secciones
- **WHEN** el usuario selecciona una sección en la lista lateral
- **THEN** el contenido de esa sección ocupa la zona derecha y el resto de secciones queda oculto

#### Scenario: El botón de guardado solo donde aplica
- **WHEN** el usuario abre las secciones de IVA, Retenciones o Series
- **THEN** el botón de guardado general no se muestra, y las acciones disponibles son las propias de la sección

#### Scenario: Cada sección cabe sin scroll
- **WHEN** el usuario recorre las secciones con la ventana en su tamaño mínimo de 1024×768
- **THEN** el contenido de cada sección es visible completo sin necesidad de desplazarse

### Requirement: Retención de IRPF

La aplicación SHALL permitir aplicar una retención de IRPF a las facturas. La empresa SHALL poder configurar una lista de tipos de retención (nombre y porcentaje), gestionada de forma similar a los tipos de IVA. Cada factura SHALL poder seleccionar un tipo de retención configurado o ninguno. La retención SHALL calcularse sobre la **base imponible** de la factura, es decir, sobre la misma base sobre la que se calcula el IVA, después de aplicar el descuento global. El total de la factura SHALL ser `Base − Descuento + IVA − Retención`. La retención seleccionada y su importe SHALL guardarse con la factura. Si no se selecciona ningún tipo de retención, el comportamiento SHALL ser el actual: `Total = Base − Descuento + IVA`. El total de una factura SHALL NOT ser negativo por efecto de la retención.

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

El histórico de facturas SHALL mostrar el importe de retención de cada factura en una columna propia. Cuando una factura no tiene retención, la columna SHALL mostrar un valor vacío o cero según el criterio de la interfaz.

#### Scenario: Histórico con retención
- **WHEN** el usuario busca en el histórico facturas con y sin retención
- **THEN** la columna de retención muestra el importe correspondiente a cada factura

### Requirement: Retención en rectificativas

Al crear una rectificativa desde una factura, la aplicación SHALL copiar el tipo de retención de la factura original. El usuario SHALL poder modificar o quitar la retención en la rectificativa antes de guardarla. El cálculo del total de la rectificativa SHALL aplicar la misma fórmula de retención sobre la base imponible.

#### Scenario: Rectificativa hereda retención
- **WHEN** el usuario crea una rectificativa desde una factura que tiene una retención del 15 %
- **THEN** la rectificativa se crea con el mismo tipo de retención del 15 %, editable antes de guardar

### Requirement: Persistencia local

La aplicación SHALL guardar los datos de cada empresa en una base de datos SQLite local dedicada ubicada en una carpeta de datos de la aplicación, separada de la instalación. Cada empresa SHALL tener su propia base de datos: los datos de una empresa SHALL NOT mezclarse con los de otra. La aplicación SHALL ser la vía normal para modificar los datos. La base de datos de la empresa activa SHALL contener su histórico completo y su configuración de empresa. Las operaciones importantes de persistencia SHALL ser transaccionales con confirmación y reversión correctas. No SHALL eliminarse físicamente datos históricos que hayan sido utilizados. La aplicación SHALL ejecutarse como una única instancia a la vez.

#### Scenario: Guardado transaccional
- **WHEN** el usuario guarda una factura con sus líneas
- **THEN** la operación se confirma de forma atómica o se revierte por completo si falla

#### Scenario: Segunda instancia
- **WHEN** el usuario intenta abrir una segunda instancia de la aplicación
- **THEN** la segunda instancia no se abre y se notifica al usuario

#### Scenario: Aislamiento entre empresas
- **WHEN** el usuario crea facturas en la empresa A, cambia a la empresa B y abre el histórico
- **THEN** en la empresa B no aparecen las facturas de la empresa A

### Requirement: Gestión de empresas

La aplicación SHALL permitir gestionar varias empresas con datos totalmente aislados. Cada empresa SHALL tener un nombre visible y una carpeta de datos cuyo nombre se forma a partir de él. Crear, elegir y eliminar empresas SHALL hacerse únicamente en la pantalla de arranque. Al crear una empresa nueva la aplicación SHALL crear su base de datos desde cero con la estructura de tablas completa. Eliminar una empresa SHALL pedir confirmación y SHALL borrar su carpeta de datos. La aplicación SHALL recordar de forma global la última empresa utilizada y SHALL preseleccionarla al abrir. Cada empresa SHALL tener su propia configuración de empresa, clientes, series, tipos de IVA y facturas. La empresa en uso SHALL NOT poder eliminarse. Para trabajar con otra empresa, Configuración SHALL ofrecer la acción «Cambiar de empresa», que cierra la empresa en uso y vuelve a la pantalla de arranque; cambiar de empresa SHALL ser siempre una acción explícita del usuario.

Cuando al abrir la aplicación no exista ninguna empresa, la aplicación SHALL cargar la empresa de demostración y SHALL dejarla preseleccionada. La aplicación SHALL NOT crear ninguna otra empresa por su cuenta ni SHALL volver a cargar la demostración si ya existe alguna empresa. Cuando no exista ninguna empresa, o la única sea la de demostración, la pantalla de arranque SHALL mostrar un texto que invite a crear la empresa propia y a completar después sus datos.

#### Scenario: Crear una nueva empresa
- **WHEN** el usuario crea la empresa «Asesoría María Luisa Ibáñez» desde la pantalla de arranque
- **THEN** se crea su base de datos con la estructura completa y la empresa aparece en el listado de la pantalla de arranque

#### Scenario: Nombre visible e identificador interno
- **WHEN** el usuario crea una empresa con nombre «Asesoría María Luisa Ibáñez»
- **THEN** el listado muestra el nombre visible y su carpeta de datos usa un nombre formado a partir de él

#### Scenario: Cambiar a otra empresa
- **WHEN** el usuario pulsa «Cambiar de empresa» en Configuración, confirma, elige otra empresa en la pantalla de arranque y entra
- **THEN** la aplicación pasa a usar la base de datos de esa empresa y sus datos (configuración, clientes, series, facturas) sustituyen a los de la anterior

#### Scenario: Crear y aceptar el cambio
- **WHEN** el usuario crea una empresa en la pantalla de arranque y entra en ella
- **THEN** la aplicación abre Configuración en la sección Empresa, porque a la empresa nueva le faltan sus datos obligatorios

#### Scenario: Eliminar empresa no actual
- **WHEN** el usuario elimina una empresa en la pantalla de arranque y confirma
- **THEN** la empresa desaparece del listado y se borra su carpeta de datos

#### Scenario: La empresa activa no se elimina
- **WHEN** el usuario intenta eliminar la empresa actualmente en uso
- **THEN** la aplicación no permite eliminarla

#### Scenario: Última empresa preseleccionada
- **WHEN** el usuario abre la aplicación después de haber usado «Talleres Ejemplo S.L.»
- **THEN** «Talleres Ejemplo S.L.» aparece seleccionada por defecto en la pantalla de arranque

#### Scenario: Crear empresa desde el arranque
- **WHEN** el usuario usa la opción de crear empresa de la pantalla de arranque
- **THEN** se crea la empresa, aparece en el listado y queda seleccionada sin salir de la pantalla de arranque

#### Scenario: Instalación nueva
- **WHEN** el usuario abre la aplicación por primera vez y no existe ninguna empresa
- **THEN** se carga la empresa de demostración, aparece preseleccionada en la pantalla de arranque y se ve el texto que invita a crear la empresa propia

#### Scenario: La demostración no se recarga
- **WHEN** el usuario vuelve a abrir la aplicación y ya existe alguna empresa
- **THEN** no se carga ni se duplica la empresa de demostración

#### Scenario: Con empresa propia no hay texto de ayuda
- **WHEN** el usuario abre la aplicación y existe alguna empresa distinta de la de demostración
- **THEN** la pantalla de arranque no muestra el texto de ayuda

#### Scenario: Cancelar la eliminación de una empresa
- **WHEN** el usuario pulsa Eliminar en la pantalla de arranque y cancela la confirmación
- **THEN** la empresa sigue en el listado y sus datos no se tocan

#### Scenario: Volver al arranque desde Configuración
- **WHEN** el usuario pulsa «Cambiar de empresa» en Configuración y confirma
- **THEN** se cierra la ventana principal y se abre la pantalla de arranque con la empresa que se estaba usando preseleccionada

#### Scenario: Configuración ya no gestiona empresas
- **WHEN** el usuario abre Configuración
- **THEN** no hay ninguna sección para crear o eliminar empresas

### Requirement: Datos obligatorios de la empresa

La aplicación SHALL exigir que la empresa activa tenga completos sus datos obligatorios antes de permitir trabajar con ella: nombre o razón social, NIF válido, dirección, código postal válido, localidad, provincia, email válido y teléfono.

Al entrar en una empresa a la que le falte alguno de esos datos, sea desde la pantalla de arranque o al restaurar una copia, la aplicación SHALL abrir Configuración en la sección Empresa en lugar del menú principal, SHALL mostrar un texto que explique que para empezar a usar el programa hay que completar los datos de la empresa, y SHALL desactivar el botón de volver al menú y toda la barra de navegación salvo la opción de salir. Mientras tanto SHALL seguir disponibles todas las secciones de Configuración, el guardado de la configuración y la acción «Cambiar de empresa». Si el nombre de la empresa está vacío, SHALL proponerse el nombre con el que se creó.

Los campos obligatorios de la sección Empresa SHALL marcarse con un asterisco. Guardar la configuración SHALL NOT ser posible mientras falte algún dato obligatorio o no sea válido: la aplicación SHALL marcar como erróneos todos los campos incorrectos y SHALL mostrar un único aviso, el del primer dato incorrecto. Cuando se completan unos datos que estaban pendientes, la aplicación SHALL pasar al menú principal.

#### Scenario: Entrar en una empresa recién creada
- **WHEN** el usuario entra en una empresa que acaba de crear
- **THEN** se abre Configuración en la sección Empresa con el texto explicativo y el nombre de la empresa ya propuesto
- **AND** la barra de navegación solo permite salir

#### Scenario: Guardar con datos incompletos
- **WHEN** el usuario pulsa Guardar configuración sin haber rellenado el teléfono y con un NIF no válido
- **THEN** la configuración no se guarda
- **AND** los campos NIF y Teléfono quedan marcados como erróneos y se muestra un único aviso, el del NIF

#### Scenario: Completar los datos
- **WHEN** el usuario rellena todos los datos obligatorios con valores válidos y guarda
- **THEN** los datos se guardan, la barra de navegación se habilita y la aplicación muestra el menú principal

#### Scenario: Empresa existente incompleta
- **WHEN** el usuario entra en una empresa que ya tenía facturas pero no tiene email
- **THEN** se abre Configuración en la sección Empresa en lugar del menú principal

#### Scenario: Empresa completa
- **WHEN** el usuario entra en una empresa con todos los datos obligatorios válidos
- **THEN** se abre el menú principal directamente

#### Scenario: No se pueden vaciar los datos de una empresa completa
- **WHEN** el usuario borra el NIF de una empresa completa en Configuración y guarda
- **THEN** la configuración no se guarda y la aplicación indica que falta el NIF

#### Scenario: La demostración entra al menú
- **WHEN** el usuario entra en la empresa de demostración recién cargada
- **THEN** se abre el menú principal directamente

#### Scenario: Guardar una factura con la empresa incompleta
- **WHEN** a la empresa le falta el teléfono
- **THEN** el usuario no puede llegar al editor de facturas: la aplicación está en Configuración con la barra de navegación bloqueada

#### Scenario: Exportar o generar con la empresa incompleta
- **WHEN** a la empresa le falta algún dato obligatorio
- **THEN** no se puede exportar a PDF, crear una rectificativa ni generar las facturas mensuales, porque ninguna de esas pantallas está disponible hasta completar los datos

#### Scenario: Cambiar de empresa durante el bloqueo
- **WHEN** el usuario ha entrado en una empresa incompleta por error y pulsa «Cambiar de empresa» en Configuración
- **THEN** vuelve a la pantalla de arranque sin tener que completar los datos ni cerrar la aplicación

### Requirement: Copia de seguridad

La aplicación SHALL tener un botón para crear una copia de seguridad manual. En la V1 la copia SHALL ser únicamente del archivo SQLite; no se incluyen PDFs ni configuración. La aplicación SHALL permitir restaurar una copia de seguridad desde la misma pantalla. Antes de restaurar, la aplicación SHALL mostrar un resumen del contenido del archivo (empresa, NIF, número de facturas y última fecha). La aplicación SHALL validar la copia antes de sustituir nada: rechazará archivos que no sean bases de datos válidas de la aplicación, que no contengan las tablas fundamentales de la aplicación ni que sean la propia base activa.

La aplicación SHALL decidir si acepta una copia por **su estructura**: SHALL aceptarla si contiene todas las tablas y columnas que la aplicación necesita, y SHALL rechazarla en caso contrario, indicando qué tabla o columna falta. Las bases de datos SHALL NOT llevar número de versión de esquema. Tras restaurar, la base SHALL quedar en un estado utilizable sin que el usuario tenga que hacer nada más.

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
- **THEN** la base resultante tiene todas las tablas y columnas de la aplicación y queda lista para usarse sin pasos adicionales

#### Scenario: Restaurar una copia de esquema anterior
- **WHEN** el usuario restaura una copia hecha con un programa anterior que contiene todas las tablas y columnas que la aplicación necesita
- **THEN** la aplicación la acepta y la restaura tal cual, sin modificar sus tablas

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
- **WHEN** el archivo de copia procede de un programa más nuevo pero contiene todas las tablas y columnas que la aplicación conoce
- **THEN** la aplicación acepta la copia sin mostrar ningún aviso de versión

#### Scenario: Copia de esquema posterior con tablas distintas
- **WHEN** al archivo de copia le falta alguna tabla o columna que la aplicación necesita
- **THEN** la aplicación rechaza la copia e informa de qué falta

#### Scenario: Logo del backup inexistente
- **WHEN** la copia referencia un archivo de logo que no existe en la máquina actual
- **THEN** la aplicación avisa de que el logo no se encontrará pero permite continuar con la restauración

#### Scenario: Dos copias seguidas dentro del mismo segundo
- **WHEN** el usuario crea dos copias de seguridad en menos de un segundo
- **THEN** ambas se crean correctamente con nombres distintos sin error de colisión

### Requirement: Apariencia de la interfaz

La aplicación SHALL presentar la misma apariencia en todas sus pantallas. Los detalles de acabado —colores, sombreados, márgenes, tamaños de letra y de icono, esquinas y animaciones— SHALL definirse en las hojas de estilo (`base.css` y un fichero por tema) y SHALL NOT describirse en esta especificación.

Las reglas de apariencia que sí SHALL cumplirse son estas:

La aplicación SHALL ofrecer los temas de apariencia biblioteca8 (predeterminado), omarchy, esmeralda, terracota, negro-dorado, sakura y neon. El tema SHALL elegirse en Configuración, SHALL guardarse en cada empresa y SHALL aplicarse a todas las pantallas y a los diálogos de aviso. La pantalla de arranque SHALL usar el tema de la última empresa abierta.

Con cualquier tema, todo el texto de la interfaz SHALL leerse con claridad sobre su fondo, incluido el texto de ayuda de los campos vacíos.

En el tamaño mínimo de ventana, ninguna pantalla SHALL recortar ni ocultar controles: las filas de campos y de botones SHALL reorganizarse en varias líneas cuando el ancho no baste, manteniendo unido cada grupo de botones.

Una misma acción SHALL llamarse igual en todas las pantallas, y su botón SHALL nombrar la acción, no el objeto. Dentro de una misma barra, todos los botones SHALL presentarse con el mismo criterio de icono, etiqueta y tamaño.

Cada diálogo de aviso SHALL mostrar el icono correspondiente a su tipo —información, error o pregunta— y el icono de la aplicación en su ventana.

La negrita SHALL reservarse a los importes; ningún otro texto de la interfaz SHALL mostrarse en negrita.

#### Scenario: Cambiar el tema
- **WHEN** el usuario elige otro tema en Configuración y guarda
- **THEN** la interfaz cambia de tema y ese tema queda guardado en la empresa activa

#### Scenario: Texto legible en cualquier tema
- **WHEN** el usuario abre Clientes o el Histórico con un tema oscuro
- **THEN** los textos y el texto de ayuda de los campos se leen con claridad

#### Scenario: Ninguna pantalla recorta contenido
- **WHEN** el usuario recorre las pantallas con la ventana en su tamaño mínimo
- **THEN** todos los campos y botones siguen visibles, reorganizados en varias líneas si hace falta

#### Scenario: La misma acción se llama igual
- **WHEN** el usuario compara el botón de borrar de Clientes con el del Histórico
- **THEN** los dos usan la misma palabra para esa acción

#### Scenario: Iconos en los diálogos
- **WHEN** la aplicación muestra un aviso de información, de error o de confirmación
- **THEN** el diálogo muestra el icono de su tipo y la ventana muestra el icono de la aplicación

#### Scenario: La negrita solo en los importes
- **WHEN** el usuario mira cualquier pantalla de la aplicación
- **THEN** solo los importes de las filas de totales se muestran en negrita

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

La aplicación SHALL abrir su ventana siempre a 1024x768 y centrada en la pantalla principal, en la primera ejecución y en todas las siguientes. La posición y el tamaño de la ventana SHALL seguir guardándose al cerrar la aplicación, pero SHALL NOT usarse al abrirla: la aplicación SHALL ignorar cualquier posición o tamaño guardados. El tamaño mínimo de las vistas principales SHALL ser 1024x768 y el usuario SHALL poder redimensionar hasta ese mínimo y maximizar la ventana durante la sesión. Con la ventana en su tamaño mínimo, ninguna pantalla SHALL recortar ni ocultar controles: los filtros del Histórico SHALL reorganizarse en varias líneas cuando el ancho no baste, manteniendo cada grupo de botones de acción unido, y los campos de la cabecera del Editor SHALL repartirse el ancho disponible. El arranque (selección de empresa) SHALL mostrarse en una ventana propia, fija y pequeña de 760x520, también centrada. Al entrar en una empresa, la aplicación SHALL abrir la ventana principal a 1024x768 y centrada con la primera pantalla, y SHALL cerrar la ventana de arranque.

#### Scenario: Primera ejecución abre a 1024x768 centrada
- **WHEN** el usuario inicia la aplicación sin preferencias de ventana guardadas
- **THEN** la ventana mide 1024x768 y aparece centrada en la pantalla principal

#### Scenario: Siguientes ejecuciones restauran la última sesión
- **WHEN** el usuario cierra la aplicación tras moverla o redimensionarla y vuelve a abrirla
- **THEN** la ventana vuelve a abrirse a 1024x768 y centrada en la pantalla principal, sin recuperar la posición ni el tamaño de la sesión anterior, aunque estos sigan guardados

#### Scenario: Tamaño guardado inferior al mínimo
- **WHEN** la aplicación encuentra un tamaño de ventana guardado inferior a 1024x768
- **THEN** lo ignora y la ventana se abre a 1024x768, sin mostrarse recortada

#### Scenario: Mínimo de redimensionado
- **WHEN** el usuario arrastra el borde de la ventana para hacerla más pequeña
- **THEN** la ventana no puede bajar de 1024x768

#### Scenario: Filtros del Histórico con ventana mínima
- **WHEN** la ventana está al mínimo 1024x768 y se abre el Histórico
- **THEN** todos los filtros siguen visibles reorganizados en varias líneas y los botones Exportar PDF, Buscar y Volver permanecen accesibles

#### Scenario: Altas rápidas de IVA y Series con ventana mínima
- **WHEN** la ventana está al mínimo 1024x768 y se abren las secciones IVA, Retenciones o Series de Configuración
- **THEN** en Series los campos de alta se reorganizan sin cortarse y sus botones permanecen visibles y agrupados, y en IVA y Retenciones los botones de la tabla se ven completos

#### Scenario: Cabecera del Editor con ventana mínima
- **WHEN** la ventana está al mínimo 1024x768 y se abre una factura
- **THEN** los campos de la cabecera se reparten el ancho disponible sin salirse de la ventana

#### Scenario: Corrección al navegar desde una vista pequeña
- **WHEN** el usuario pulsa Entrar en la ventana de arranque (760x520)
- **THEN** se abre la ventana principal a 1024x768 y centrada con el menú u otra vista principal, sin necesidad de redimensionar o maximizar manualmente
- **AND** la ventana de arranque se cierra

### Requirement: Identidad de la aplicación en la interfaz

La aplicación SHALL mostrar un icono de aplicación propio en cada una de sus ventanas. El icono SHALL aplicarse a la ventana principal y a las ventanas secundarias (`Stage`) que la aplicación abre, de modo que se vea en la barra de tareas, en la esquina de la ventana y en la vista minimizada. La ventana principal SHALL mostrarse siempre con un título compuesto por la marca «CaboFactu®», un espacio y el nombre de la pantalla activa. Las ventanas secundarias SHALL mostrar el mismo prefijo de marca delante de su propio título («CaboFactu® » + título).

#### Scenario: Icono en la ventana principal
- **WHEN** la aplicación inicia su ventana principal
- **THEN** la ventana muestra el icono de aplicación en su barra de título, en la barra de tareas de Windows y en la vista minimizada

#### Scenario: Icono en ventanas secundarias
- **WHEN** la aplicación abre una ventana secundaria de tipo `Stage` (p. ej. el diálogo «Generar facturas mensuales»)
- **THEN** esa ventana muestra el mismo icono de aplicación en su barra de título y en la barra de tareas de Windows

#### Scenario: Título de la ventana principal por pantalla
- **WHEN** el usuario navega entre las pantallas de la aplicación (Menú Principal, Histórico, Configuración, Editor, Clientes o Copias)
- **THEN** la ventana principal se titula «CaboFactu® <nombre de la pantalla actual>»
- **AND** la ventana de arranque, que es una ventana propia, se titula «CaboFactu® Seleccion de empresa»

#### Scenario: Título con prefijo de marca en ventanas secundarias
- **WHEN** se abre una ventana secundaria de tipo `Stage` con su propio título
- **THEN** el título mostrado es «CaboFactu® <título propio de la ventana>»

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

La aplicación SHALL permitir anular y eliminar facturas directamente desde la pantalla de histórico. El usuario SHALL poder seleccionar una o varias facturas (independientemente de su estado o tipo). La acción **Anular** SHALL cambiar el estado de la factura a `ANULADA` y conservar el registro. La acción **Eliminar** SHALL borrar físicamente la factura y sus líneas de la base de datos; antes de eliminar, el sistema SHALL advertir al usuario del número de líneas que se eliminarán y SHALL pedir confirmación. Una factura que tiene alguna rectificativa SHALL NOT poder eliminarse, y la aplicación SHALL avisar indicando qué rectificativa la corrige. Anular SHALL cambiar el estado de la misma factura, sin crear otra fila. Al eliminar una factura, su número SHALL quedar libre para ofrecerse al crear la siguiente factura de la misma serie y año. El sistema SHALL mostrar un resumen con el resultado de la operación, que SHALL nombrar por su número cada factura que no se haya podido anular o eliminar, y SHALL refrescar la tabla del histórico.

#### Scenario: Anular una factura desde el histórico
- **WHEN** el usuario selecciona una factura emitida y pulsa "Anular"
- **THEN** el sistema pide confirmación
- **AND** tras confirmar, la factura pasa a estado Anulada y el resumen indica 1 anulada

#### Scenario: Borrar una factura desde el histórico
- **WHEN** el usuario selecciona una factura y pulsa "Eliminar"
- **THEN** el sistema muestra un aviso con las líneas que se eliminarán
- **AND** tras confirmar, la factura desaparece de la base de datos y su número queda libre

#### Scenario: Menú contextual del histórico
- **WHEN** el usuario hace clic derecho sobre las facturas seleccionadas del histórico
- **THEN** aparece un menú contextual con las opciones "Exportar a PDF", "Anular facturas seleccionadas" y "Eliminar facturas seleccionadas"

#### Scenario: Resumen tras anular varias facturas
- **WHEN** el usuario anula una selección que incluye facturas emitidas y facturas ya anuladas
- **THEN** se muestra un resumen con las anuladas y las ya anuladas

#### Scenario: Borrar una factura rectificada
- **WHEN** el usuario intenta eliminar la factura A-1/9, que tiene la rectificativa R-1
- **THEN** la factura no se elimina
- **AND** la aplicación avisa de que la corrige la rectificativa R-1

### Requirement: Desglose de totales por tipo de IVA

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

### Requirement: Suplidos en la facturación

La aplicación SHALL permitir facturar suplidos: gastos pagados por cuenta del cliente que no forman parte de la contraprestación.

Un suplido SHALL introducirse como una línea más de la factura, eligiendo el tipo de IVA «Suplido» en el mismo desplegable que el resto de tipos.

Una línea de suplido SHALL NOT formar parte de la base imponible, SHALL NOT generar cuota de IVA, SHALL NOT entrar en la base sobre la que se calcula la retención y SHALL NOT verse afectada por el descuento global. Su importe SHALL sumarse al total de la factura.

Las líneas de suplido SHALL NOT aparecer en el desglose por tipo de IVA, que solo describe operaciones sujetas.

El total de suplidos SHALL guardarse con la factura, de modo que reabrir una factura antigua muestre los mismos importes.

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
