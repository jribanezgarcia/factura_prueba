## MODIFIED Requirements

### Requirement: Clientes

La aplicación SHALL permitir gestionar una ficha de clientes con nombre/razón social, NIF, dirección, código postal, localidad y provincia. El NIF será opcional; cuando se informe, la aplicación SHALL validar DNI, NIE y NIF/CIF español antes de permitir guardar. Un cliente sin facturas asociadas SHALL poder eliminarse físicamente. Un cliente con facturas asociadas SHALL NOT poder eliminarse físicamente y SHALL poder marcarse como inactivo. Un cliente inactivo SHALL NOT aparecer normalmente al crear nuevas facturas, SHALL seguir apareciendo en el histórico y sus facturas SHALL seguir siendo consultables.

#### Scenario: NIF inválido al alta o edición de cliente
- **WHEN** el usuario abandona el campo NIF mediante Enter o cambiando el foco y el documento no vacío es inválido
- **THEN** la aplicación informa de que el NIF no es válido y mantiene el foco en el campo

#### Scenario: Salvaguarda al guardar cliente
- **WHEN** el usuario intenta guardar un cliente con un NIF no vacío inválido
- **THEN** la aplicación no guarda el cliente e informa del error

### Requirement: Datos de cliente en el editor de factura

Al crear o editar una factura, la aplicación SHALL validar el NIF no vacío del cliente como DNI, NIE o NIF/CIF español al abandonar el campo mediante Enter o cambio de foco, y SHALL impedir guardar la factura si el documento es inválido.

#### Scenario: NIF inválido en una factura
- **WHEN** el usuario abandona el campo NIF de una factura con un documento no vacío inválido
- **THEN** la aplicación informa del error y devuelve el foco al campo

#### Scenario: Intento de guardar factura con NIF inválido
- **WHEN** el usuario intenta guardar una factura cuyo cliente tiene un NIF no vacío inválido
- **THEN** la factura no se guarda y se informa del error
