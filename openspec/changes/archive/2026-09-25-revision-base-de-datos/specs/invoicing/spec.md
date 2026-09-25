## MODIFIED Requirements

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
