## MODIFIED Requirements

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

## ADDED Requirements

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
