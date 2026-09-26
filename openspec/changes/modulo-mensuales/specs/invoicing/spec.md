## MODIFIED Requirements

### Requirement: Facturación mensual por cliente

La aplicación SHALL permitir generar múltiples facturas mensuales para un único cliente desde un diálogo específico. El usuario SHALL seleccionar el cliente, el rango de meses, la serie de numeración y el día del mes que se usará como fecha de cada factura, pudiendo elegir entre un día fijo editable, el primer día del mes o el último día del mes. El año SHALL ser siempre el de la fecha de trabajo y SHALL NOT poder cambiarse en el diálogo. El usuario SHALL poder configurar las líneas de concepto que se replicarán en cada factura, con una única casilla «Añadir mes» que, marcada, añade el nombre del mes a la descripción de todas las líneas. El usuario SHALL seleccionar el tipo de IVA y, opcionalmente, el tipo de retención IRPF que se aplicarán a todas las facturas generadas. El sistema SHALL crear una factura por cada mes del rango, asignando a cada una el siguiente número de la serie seleccionado y la fecha correspondiente. Si para un mes ya existe una factura para ese cliente y año, el sistema SHALL mostrar una advertencia con los meses afectados y SHALL permitir al usuario decidir si genera las facturas de todos modos o cancela la operación. Si la serie tiene números libres en ese año, el sistema SHALL ofrecerlos con dos botones, uno para usarlos primero y otro para continuar sin ellos; cerrar el aviso SHALL equivaler a continuar sin ellos. Las facturas generadas SHALL aparecer en el histórico y SHALL poder exportarse a PDF.

#### Scenario: Acceso desde el menú principal
- **WHEN** el usuario pulsa la opción "Facturar mes" en el menú principal
- **THEN** se abre el diálogo de facturación mensual

#### Scenario: Acceso desde el histórico
- **WHEN** el usuario pulsa el botón "Facturar mes" en la pantalla de histórico
- **THEN** se abre el diálogo de facturación mensual

#### Scenario: Configuración de la generación
- **WHEN** el usuario selecciona un cliente, un mes de inicio, un mes de fin, una serie de numeración y un día del mes
- **THEN** el diálogo indica cuántas facturas se generarán en el año de trabajo

#### Scenario: Líneas con descripción mensual
- **WHEN** el usuario añade una línea con descripción "contabilidad y laboral" y marca la casilla "Añadir mes"
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
- **THEN** el sistema avisa de los números libres con los botones «Usar los números libres» y «Continuar sin ellos»
- **AND** si el usuario elige usarlos, las nuevas facturas usan los números 1 a 12 en lugar de empezar por el 13

#### Scenario: Continuar sin los números libres
- **WHEN** la serie tiene números libres en el año de trabajo y el usuario elige «Continuar sin ellos»
- **THEN** las facturas generadas usan los números siguientes al mayor de la serie y los libres siguen libres

#### Scenario: Siempre en el año de trabajo
- **WHEN** el usuario abre el diálogo con la fecha de trabajo en 2026
- **THEN** el diálogo no permite elegir el año y todas las facturas se generan con fecha de 2026

#### Scenario: Añadir mes a todas las líneas
- **WHEN** el usuario añade dos líneas, "cuota" y "gestoría", y marca la casilla "Añadir mes"
- **THEN** en la factura de marzo las dos líneas terminan en " - mes de marzo"
