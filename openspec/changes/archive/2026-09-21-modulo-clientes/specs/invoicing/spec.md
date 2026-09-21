## MODIFIED Requirements

### Requirement: Clientes

La aplicación SHALL permitir gestionar una ficha de clientes con nombre/razón social, NIF, dirección, código postal, localidad, provincia y email. El nombre, el NIF, la dirección, el código postal, la localidad y la provincia SHALL ser obligatorios, y sus etiquetas SHALL marcarse con un asterisco tanto en la ficha de cliente como en el bloque Cliente del editor de factura. El email SHALL ser opcional, pero cuando se informe SHALL tener un formato válido. El NIF SHALL validarse como DNI, NIE o NIF/CIF español, y la aplicación SHALL distinguir el aviso según el error: si el campo está vacío, «El NIF/NIE es obligatorio.»; si no tiene la forma de ningún documento, «Formato NIF/NIE incorrecto. Debe ser como 12345678Z (DNI), X1234567L (NIE) o B12345674 (CIF).»; si tiene la forma pero la letra o el carácter de control no corresponde, «La letra no es correcta.». El código postal SHALL tener cinco dígitos y comenzar entre 01 y 52. El NIF SHALL guardarse siempre en mayúsculas, se escriba como se escriba.

Los datos del cliente SHALL comprobarse al intentar guardar, y no al abandonar un campo ni al pulsar Enter en él: la aplicación SHALL NOT mostrar avisos ni retener el foco mientras el usuario rellena la ficha o el bloque Cliente, de modo que pueda moverse entre campos, cancelar la ficha o salir del editor en cualquier momento. Al intentar guardar con algún dato incorrecto, la aplicación SHALL marcar como erróneos todos los campos incorrectos y SHALL mostrar un único aviso, el del primer dato incorrecto. Estas comprobaciones SHALL aplicarse también al guardar una factura desde el editor, al generar facturas mensuales y al crear rectificativas, de modo que no pueda guardarse ninguna factura nueva o editada sin cliente o con datos de cliente incompletos o incorrectos.

Un cliente sin facturas asociadas SHALL poder eliminarse físicamente. Un cliente con facturas asociadas SHALL NOT poder eliminarse físicamente y SHALL poder marcarse como inactivo. Un cliente inactivo SHALL NOT aparecer normalmente al crear nuevas facturas, SHALL seguir apareciendo en el histórico y sus facturas SHALL seguir siendo consultables.

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

### Requirement: Búsqueda de clientes al crear factura

Al crear o editar una factura, el usuario SHALL poder buscar un cliente por nombre/razón social o NIF con búsqueda incremental mientras escribe. Al seleccionar un cliente, sus datos —incluido el email— SHALL cargarse en la factura. Los datos del cliente (email incluido) SHALL poder modificarse desde la factura, y esos datos modificados SHALL quedar siempre en la factura que se guarda. La aplicación SHALL NOT modificar la ficha general del cliente sin preguntar: al guardar una factura cuyos datos de cliente difieran de los de su ficha, la aplicación SHALL pedir confirmación para guardar también esos cambios en la ficha, y SHALL actualizarla solo si el usuario acepta. La ficha general de clientes SHALL incluir el campo email.

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
