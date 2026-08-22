## Context

El requisito «Versionado» manda sobrescribir en su lugar cuando se edita la última versión; el usuario necesita además la opción de crear una versión nueva para conservar copias independientes exportables. El diálogo actual (`Dialogos.confirmar` sí/no) solo permite sobrescribir o cancelar.

## Goals / Non-Goals

**Goals:**

- Diálogo de guardado con tres salidas: Sobrescribir / Guardar como nueva versión / Cancelar.
- Ruta de servicio que cree vN+1 aunque la versión abierta sea la última.

**Non-Goals:**

- No cambia el comportamiento por defecto (sobrescribir sigue siendo un camino válido).
- No toca anulación/restauración/rectificativas.

## Decisions

- **`FacturaService.guardarEditada`**: nueva sobrecarga con `boolean comoNuevaVersion`; la firma anterior delega con `false`, así el resto de llamadas y tests existentes no cambian. Con `comoNuevaVersion=true` se omite la rama de sobrescritura y se llama siempre a `crearVersion`.
- **`Dialogos`**: nuevo enum `ModoGuardarVersion` + método en `Impl` como **método default** que mapea al antiguo `confirmar` (SOBRESCRIBIR/CANCELAR): las implementaciones de test existentes siguen compilando y conservan su comportamiento; la implementación real muestra tres botones.
- **`EditorController`**: sustituye el confirmar sí/no por `Dialogos.modoGuardarVersion()`; cancelar aborta, nueva versión pasa `true` a la sobrecarga. La etiqueta tras guardar ya muestra el nº de versión devuelto (v2, …).

## Risks / Trade-offs

- [Diálogo con más pasos puede cansar] → «Sobrescribir» es el primer botón (por defecto) y conserva el flujo habitual; cancelar no toca datos.
