## MODIFIED Requirements

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
