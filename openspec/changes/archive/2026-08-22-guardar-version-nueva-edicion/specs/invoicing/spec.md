## MODIFIED Requirements

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