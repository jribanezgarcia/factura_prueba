## MODIFIED Requirements

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
