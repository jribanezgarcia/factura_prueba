# Elegir entre sobrescribir o guardar como nueva version al editar

## Why

Hoy, al editar la última versión de una factura emitida, el guardado la sobrescribe en su lugar: la modificación no queda como copia independiente exportable y el estado previo se pierde. El usuario necesita poder conservar ambas: sobrescribir cuando quiere corregir, o crear una versión nueva cuando quiere conservar el histórico de cambios.

## What Changes

- Al guardar cambios sobre la última versión de una factura emitida, la aplicación SHALL ofrecer dos caminos: sobrescribir la versión actual (mismo número de versión, comportamiento actual) o crear una nueva versión (vN+1) con los cambios, dejando la versión anterior intacta.
- La versión nueva SHALL aparecer como fila propia en el Histórico y SHALL poder exportarse por separado.

## Capabilities

### New Capabilities

- (ninguna)

### Modified Capabilities

- `invoicing`: requisito modificado «Versionado» (dos opciones al guardar sobre la última versión).

## Impact

- `service/FacturaService.java`: parámetro `comoNuevaVersion` en `guardarEditada`.
- `ui/Dialogos.java`: nuevo diálogo de tres opciones.
- `ui/EditorController.java`: uso del nuevo diálogo.
- Tests: caso de servicio que verifica convivencia de v1 intacta y v2.
