## Why

Hoy exportar un PDF exige abrir la factura en el editor y pulsar Exportar PDF, una a una. Para enviar varias facturas (por ejemplo, todas las de un mes) el proceso es repetitivo y lento. El histórico ya muestra cada versión con su identificador, así que es el lugar natural para exportar directamente sin abrir la factura.

## What Changes

- Botón «Exportar PDF» en la pantalla de Histórico, junto a Buscar/Volver.
- La tabla del histórico permite selección múltiple (Ctrl+clic / Shift+clic).
- Con una sola factura seleccionada: diálogo Guardar como con el nombre propuesto por la aplicación (mismo comportamiento que el editor).
- Con varias seleccionadas: diálogo de elección de carpeta una sola vez y generación en segundo plano de todos los PDF con su nombre propuesto; al terminar se informa cuántos se generaron y cuántos fallaron.
- Se reutilizan las preferencias existentes: última carpeta de exportación y color de acento configurado.

## Capabilities

### New Capabilities

(ninguna)

### Modified Capabilities

- `invoicing`: el requisito Histórico incorpora la exportación directa a PDF de las versiones seleccionadas, individual o en lote.

## Impact

- `ui/Historico.fxml` e `ui/HistoricoController.java`: nuevo botón, selección múltiple y lógica de exportación individual y en lote.
- Sin cambios en servicios, repositorios, base de datos ni en el flujo de exportación existente del editor.
