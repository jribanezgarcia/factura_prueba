## MODIFIED Requirements

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

### Requirement: Anulación y borrado de facturas desde el histórico

La aplicación SHALL permitir anular y borrar facturas directamente desde la pantalla de histórico. El usuario SHALL poder seleccionar una o varias facturas (independientemente de su estado o tipo). La acción **Anular** SHALL cambiar el estado de la factura a `ANULADA` y conservar el registro. La acción **Borrar** SHALL eliminar físicamente la factura y sus líneas de la base de datos; antes de borrar, el sistema SHALL advertir al usuario del número de líneas que se eliminarán y SHALL pedir confirmación. Una factura que tiene alguna rectificativa SHALL NOT poder borrarse, y la aplicación SHALL avisar indicando qué rectificativa la corrige. Anular SHALL cambiar el estado de la misma factura, sin crear otra fila. Al borrar una factura, su número SHALL quedar registrado como disponible para poder reutilizarse al crear la siguiente factura de la misma serie y año. El sistema SHALL mostrar un resumen con el resultado de la operación y SHALL refrescar la tabla del histórico.

#### Scenario: Anular una factura desde el histórico
- **WHEN** el usuario selecciona una factura emitida y pulsa "Anular"
- **THEN** el sistema pide confirmación
- **AND** tras confirmar, la factura pasa a estado Anulada y el resumen indica 1 anulada

#### Scenario: Borrar una factura desde el histórico
- **WHEN** el usuario selecciona una factura y pulsa "Borrar"
- **THEN** el sistema muestra un aviso con las líneas que se eliminarán
- **AND** tras confirmar, la factura desaparece de la base de datos y su número queda disponible

#### Scenario: Menú contextual del histórico
- **WHEN** el usuario hace clic derecho sobre las facturas seleccionadas del histórico
- **THEN** aparece un menú contextual con las opciones "Exportar a PDF", "Anular facturas seleccionadas" y "Borrar facturas seleccionadas"

#### Scenario: Resumen tras anular varias facturas
- **WHEN** el usuario anula una selección que incluye facturas emitidas y facturas ya anuladas
- **THEN** se muestra un resumen con las anuladas y las ya anuladas

#### Scenario: Borrar una factura rectificada
- **WHEN** el usuario intenta borrar la factura A-1/9, que tiene la rectificativa R-1
- **THEN** la factura no se borra
- **AND** la aplicación avisa de que la corrige la rectificativa R-1

### Requirement: Suplidos en la facturación

> Recrea «Suplidos» sin sus parrafos de PDF ni su escenario de PDF («El
> suplido tiene su propio bloque en el PDF»), que estan en «Suplidos en el PDF»
> de `pdf-rendering`. El título es nuevo porque lo que queda no es solo fiscal:
> incluye como se introduce un suplido y que su total se guarda con la factura.

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

## REMOVED Requirements

### Requirement: Versionado
**Reason**: Las facturas dejan de tener versiones (decisión F2): una factura es una sola fila y editarla la sobrescribe, previa confirmación.
**Migration**: Lo que queda de su comportamiento está en «Facturas normales» (sobrescribir con confirmación), «Estados de factura» (anular y restaurar sin versiones) e «Histórico de facturas» (una fila por factura).

### Requirement: Histórico
**Reason**: Se rehace sin versiones con otro nombre, porque dos de sus escenarios nombraban las versiones en el título.
**Migration**: Sustituido por «Histórico de facturas».

### Requirement: Numeración por series
**Reason**: Se rehace con otro nombre porque uno de sus escenarios (restaurar una anulada cuyo número ha ocupado otra) ya no puede darse: la base de datos impide repetir un correlativo en la misma serie y año, también el de una anulada.
**Migration**: Sustituido por «Numeración de facturas por series», con el mismo contenido salvo ese escenario, el año fijo al editar y el número manual duplicado que cuenta también las anuladas.

## ADDED Requirements

### Requirement: Histórico de facturas

La aplicación SHALL tener un histórico de facturas que muestre cada factura en una sola fila, con sus datos actuales. El histórico SHALL permitir buscar por serie, cliente/razón social, NIF, fecha desde/hasta, importe desde/hasta y estado, combinando los filtros entre sí. La búsqueda SHALL ejecutarse mediante un botón "Buscar", no en tiempo real. Los resultados SHALL ordenarse por número de factura. Las columnas SHALL ser: fecha, número, cliente, NIF, base, IVA, total y estado. Al seleccionar una fila SHALL poder abrirse esa factura. La tabla SHALL permitir seleccionar varias filas a la vez. El histórico SHALL ofrecer exportar directamente a PDF las filas seleccionadas sin necesidad de abrir la factura: con una selección se generará un único PDF preguntando dónde guardarlo; con varias selecciones se elegirá una carpeta de destino y se generarán todos los PDF en esa carpeta con sus nombres propuestos, informando al finalizar del resultado de cada generación.

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

### Requirement: Numeración de facturas por series

La aplicación SHALL tener series de numeración. En una instalación nueva la aplicación SHALL NO crear ninguna serie por defecto: el listado de series comienza vacío y el usuario las crea a mano. Cada serie SHALL tener su propio correlativo. El correlativo SHALL ser la identidad de la factura; el componente de fecha (mes o año) SHALL recalcularse según la fecha y guardarse con la factura. Al editar una factura ya emitida, su fecha SHALL poder cambiar dentro del mismo año, pero SHALL NOT pasar a otro año: la factura se queda siempre en la numeración de su año. La base de datos SHALL impedir que dos facturas de la misma serie y el mismo año tengan el mismo correlativo. El correlativo de cada serie SHALL ser independiente por ejercicio: cada año de trabajo reinicia su propia cuenta sobre el siguiente número de ese año, sin afectar al correlativo de otros años. Las series SHALL poder crearse y configurarse desde la aplicación. La aplicación SHALL recordar la última serie utilizada y proponerla al crear la siguiente factura. El número SHALL proponerse automáticamente y poder modificarse manualmente. El número SHALL NOT consumirse hasta que la factura se guarda correctamente. El siguiente correlativo de una serie SHALL calcularse a partir de sus propias facturas: el mayor correlativo usado ese año más uno, o 1 si la serie no tiene ninguna factura ese año. La aplicación SHALL NOT guardar contadores de numeración. Una factura anulada SHALL conservar su correlativo, que SHALL NOT volver a proponerse. Los correlativos que queden libres por el borrado físico de facturas SHALL ofrecerse antes de proponer un correlativo nuevo. En Configuración → Series el usuario SHALL poder ver las series y su siguiente número, que SHALL NOT poder modificarse a mano.

Cada serie SHALL tener un campo \sufijo_fecha\ con tres opciones posibles: \MES\ (formato CODIGO-CORRELATIVO/MES o CORRELATIVO/MES si no hay código), \ANIO\ (formato CODIGO-CORRELATIVO-ANIO o CORRELATIVO-ANIO si no hay código) y \NINGUNO\ (formato CODIGO-CORRELATIVO o solo CORRELATIVO si no hay código). El campo \codigo\ de una serie SHALL poder estar vacío, en cuyo caso el número NO tendrá prefijo de letra; solo SHALL admitirse una serie sin código a la vez, de modo que otra serie en blanco se rechaza y se identifica la serie por su descripción. El formato predeterminado para series nuevas SHALL ser \MES\. Los correlativos libres SHALL buscarse dentro del mismo año: un correlativo SHALL considerarse usado cuando lo tiene una factura de esa serie y ese ejercicio, esté activa o anulada.

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
- **WHEN** el usuario crea una factura con fecha de 2027 en una serie con formato ANIO
- **THEN** el número lleva el año de esa fecha (p. ej. \1-2027\)

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
- **THEN** al crear una factura en 2026 la aplicación ofrece el correlativo 2, el menor hueco libre, o continuar con el 6

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
