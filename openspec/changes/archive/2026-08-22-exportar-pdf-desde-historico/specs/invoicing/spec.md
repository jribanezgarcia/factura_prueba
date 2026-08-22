## MODIFIED Requirements

### Requirement: Histórico

La aplicación SHALL tener un histórico de facturas que muestre cada versión como una fila independiente. El histórico SHALL permitir buscar por serie, cliente/razón social, NIF, fecha desde/hasta, importe desde/hasta y estado, combinando los filtros entre sí. La búsqueda SHALL ejecutarse mediante un botón "Buscar", no en tiempo real. Los resultados SHALL ordenarse por número de factura. Las columnas SHALL ser: fecha, número, versión, cliente, NIF, base, IVA, total y estado. Al seleccionar una fila SHALL poder abrirse esa factura/versión. La tabla SHALL permitir seleccionar varias filas a la vez. El histórico SHALL ofrecer exportar directamente a PDF las filas seleccionadas sin necesidad de abrir la factura: con una selección se generará un único PDF preguntando dónde guardarlo; con varias selecciones se elegirá una carpeta de destino y se generarán todos los PDF en esa carpeta con sus nombres propuestos, informando al finalizar del resultado de cada generación.

#### Scenario: Búsqueda combinando filtros
- **WHEN** el usuario establece una serie, un cliente y un rango de fechas y pulsa Buscar
- **THEN** se muestran todas las versiones de facturas que cumplen los tres filtros

#### Scenario: Búsqueda sin límites de importe
- **WHEN** el usuario deja vacíos los campos de importe desde y hasta y pulsa Buscar
- **THEN** la aplicación no aplica ningún límite de importe y muestra también las facturas con total mayor que cero

#### Scenario: Apertura desde el histórico
- **WHEN** el usuario selecciona una fila del histórico
- **THEN** se abre la factura en la versión correspondiente

#### Scenario: Selección múltiple en la tabla

- **WHEN** el usuario mantiene Ctrl o Shift mientras hace clic sobre filas del histórico
- **THEN** quedan seleccionadas simultáneamente todas las filas marcadas

#### Scenario: Exportar una versión seleccionada

- **WHEN** el usuario selecciona una única fila del histórico y pulsa Exportar PDF
- **THEN** la aplicación propone guardar un PDF con el nombre propuesto para esa factura y lo genera sin abrir el editor

#### Scenario: Exportar varias versiones en lote

- **WHEN** el usuario selecciona varias filas del histórico y pulsa Exportar PDF
- **THEN** la aplicación pide una carpeta de destino una sola vez y genera en ella un PDF por cada fila seleccionada con su nombre propuesto
- **AND** al terminar informa cuántos PDF se generaron correctamente y cuáles fallaron
