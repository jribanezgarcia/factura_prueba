## ADDED Requirements

### Requirement: Facturación mensual por cliente

La aplicación SHALL permitir generar múltiples facturas mensuales para un único cliente desde un diálogo específico. El usuario SHALL seleccionar el cliente, el año, el rango de meses, la serie de numeración y el día del mes que se usará como fecha de cada factura, pudiendo elegir entre un día fijo editable, el primer día del mes o el último día del mes. El usuario SHALL poder configurar las líneas de concepto que se replicarán en cada factura, con la opción de añadir automáticamente el nombre del mes a la descripción de cada línea. El usuario SHALL seleccionar el tipo de IVA y, opcionalmente, el tipo de retención IRPF que se aplicarán a todas las facturas generadas. El sistema SHALL crear una factura por cada mes del rango, asignando a cada una el siguiente número de la serie seleccionado y la fecha correspondiente. Si para un mes ya existe una factura para ese cliente y año, el sistema SHALL mostrar una advertencia con los meses afectados y SHALL permitir al usuario decidir si genera las facturas de todos modos o cancela la operación. Las facturas generadas SHALL aparecer en el histórico y SHALL poder exportarse a PDF.

#### Scenario: Acceso desde el menú principal
- **WHEN** el usuario pulsa la opción "Generar facturas mensuales" en el menú principal
- **THEN** se abre el diálogo de facturación mensual

#### Scenario: Acceso desde el histórico
- **WHEN** el usuario pulsa el botón "Generar mensual" en la pantalla de histórico
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

La aplicación SHALL permitir anular y borrar facturas directamente desde la pantalla de histórico. El usuario SHALL poder seleccionar una o varias facturas (independientemente de su estado o tipo). La acción **Anular** SHALL cambiar el estado de la factura a `ANULADA` y conservar el registro. La acción **Borrar** SHALL eliminar físicamente la factura, sus versiones y sus líneas de la base de datos; antes de borrar, el sistema SHALL advertir al usuario del número de versiones y líneas que se eliminarán y SHALL pedir confirmación. Al borrar una factura, su número SHALL quedar registrado como disponible para poder reutilizarse al crear la siguiente factura de la misma serie y año. El sistema SHALL mostrar un resumen con el resultado de la operación y SHALL refrescar la tabla del histórico.

#### Scenario: Anular una factura desde el histórico
- **WHEN** el usuario selecciona una factura emitida y pulsa "Anular"
- **THEN** el sistema pide confirmación
- **AND** tras confirmar, la factura pasa a estado Anulada y el resumen indica 1 anulada

#### Scenario: Borrar una factura desde el histórico
- **WHEN** el usuario selecciona una factura y pulsa "Borrar"
- **THEN** el sistema muestra un aviso con las versiones y líneas que se eliminarán
- **AND** tras confirmar, la factura desaparece de la base de datos y su número queda disponible

#### Scenario: Menú contextual del histórico
- **WHEN** el usuario hace clic derecho sobre las facturas seleccionadas del histórico
- **THEN** aparece un menú contextual con las opciones "Exportar a PDF", "Anular facturas seleccionadas" y "Borrar facturas seleccionadas"

#### Scenario: Resumen tras anular varias facturas
- **WHEN** el usuario anula una selección que incluye facturas emitidas y facturas ya anuladas
- **THEN** se muestra un resumen con las anuladas y las ya anuladas

### Requirement: Exportación múltiple a PDF desde el histórico

La aplicación SHALL permitir exportar a PDF varias facturas seleccionadas en el histórico. Cuando se seleccionen varias facturas, el sistema SHALL preguntar si se desea generar un PDF por factura o un único PDF agrupado. Para la opción "un PDF por factura", el sistema SHALL guardar un archivo por cada factura en la carpeta elegida. Para la opción "único PDF agrupado", el sistema SHALL guardar un solo archivo que contenga todas las facturas seleccionadas.

#### Scenario: Exportar varias facturas a PDF
- **WHEN** el usuario selecciona varias facturas y pulsa "Exportar a PDF"
- **THEN** el sistema pregunta si quiere un PDF por factura o un PDF agrupado
- **AND** genera el resultado elegido
