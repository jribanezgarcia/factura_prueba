## Why

Varios clientes recurrentes cobran el mismo importe todos los meses por el mismo concepto (p. ej. "contabilidad y laboral"). Actualmente hay que crear cada factura mes a mes de forma manual, lo que es repetitivo y propenso a errores de numeración, fechas o importes. Se necesita una forma rápida de generar las facturas de varios meses de una sola vez.

## What Changes

- Nuevo diálogo de **facturación mensual por cliente** accesible desde el menú principal y desde el histórico.
- El usuario selecciona un cliente, un año, un rango de meses, una serie de numeración y un día fijo del mes para las fechas.
- Se permiten añadir varias líneas de concepto que se replicarán en cada factura. Cada línea podrá llevar un checkbox para añadir automáticamente el mes a la descripción.
- Se elige el tipo de IVA y, opcionalmente, el tipo de retención IRPF que se aplicarán a todas las facturas generadas.
- El sistema genera una factura por mes, asignando el siguiente correlativo de la serie elegida y la fecha correspondiente.
- Si ya existe una factura para ese cliente, mes y año, se advierte y se permite generar de todos modos o cancelar.
- Las facturas generadas aparecen en el histórico y son exportables a PDF como cualquier otra factura.
- Desde el histórico se distinguen dos acciones: **Anular** (cambia el estado a ANULADA) y **Borrar** (elimina la factura físicamente, liberando su número para reutilizarlo más adelante).
- La exportación a PDF desde el histórico permite generar un PDF por factura o un único PDF agrupado.

## Capabilities

### New Capabilities

*None*

### Modified Capabilities

- `invoicing`: se añade el requisito **Facturación mensual por cliente** al sistema de facturación existente.

## Impact

- Capa de UI: nuevo diálogo (`GenerarFacturasMensuales.fxml` + controller) y nuevos puntos de acceso en `MenuPrincipal.fxml` e `Historico.fxml`.
- Capa de servicio: nuevo `FacturacionMensualService` que orquesta la creación de facturas a partir de los parámetros del diálogo.
- Reutiliza `ClienteRepository`, `FacturaService`, `VersionadoService`, `NumeroService`, `CalculoService` y `Servicios`.
- Añade la tabla `numero_disponible` para recordar los números liberados por borrado y poder ofrecerlos al crear la siguiente factura.
