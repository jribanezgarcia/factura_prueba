## Why

Abrir una factura antigua puede reescribirle el tipo de IVA en silencio y dejarla marcada como modificada sin que el usuario haya tocado nada.

La cadena es esta:

1. `EditorController.cargarTiposIva()` (`:378-384`) llena `tiposIva` con `servicios.ivas.listar(true)`, es decir **solo los tipos activos**.
2. `CeldaIva.updateItem(...)` (`:1490-1498`) hace `combo.setValue(tipoIvaDe(l))` en cada render y cada scroll de la tabla.
3. `ComboBox.setValue(...)` dispara `ActionEvent`, así que el handler de `CeldaIva` (`:1476-1484`) se ejecuta **dentro** del `updateItem`.
4. `tipoIvaDe(l)` (`:674-693`) busca el tipo de la línea por id, luego por porcentaje, y si no lo encuentra devuelve `tiposIva.get(0)`.
5. El handler compara ids, ve que difieren y llama a `aplicarIva(l, t)` y `marcarModificado()`.

**Reproducción:** crear una factura con IVA 10 %. Inactivar el tipo 10 % en Configuración → IVA (algo que el spec autoriza expresamente). Abrir esa factura desde el Histórico: la columna IVA muestra el primer tipo activo, la línea se muta en memoria, se recalcula `ivaImporte` y la factura queda marcada como modificada. Si el usuario guarda —o pulsa «Guardar y salir» en el diálogo de cambios sin guardar al navegar— se persiste un IVA distinto al del snapshot histórico, y con él una base y un total distintos.

Esto incumple un requisito que ya existe: «Inactivar tipo de IVA usado → el tipo pasa a inactivo, no se ofrece para nuevas facturas y **el histórico se conserva intacto**» (requisito «IVA»).

El mismo mecanismo se dispara si el tipo maestro se borró: las líneas guardan el snapshot (`iva_nombre`, `iva_porcentaje`, `iva_motivo_exencion`), pero el editor no lo usa para reconstruir el tipo.

Como efecto secundario, `refrescarLineas()` —que es `tablaLineas.refresh()`— se invoca desde dentro de `updateItem`, que es reentrancia de layout en JavaFX.

## What Changes

- El editor SHALL ofrecer, además de los tipos de IVA activos, **el tipo concreto que use cada línea de la factura abierta**, aunque esté inactivo o borrado del maestro. Si el maestro ya no existe, el tipo se reconstruye desde el snapshot de la línea.
- `tipoIvaDe(...)` SHALL devolver `null` cuando no encuentre el tipo de la línea, en lugar de caer al primer tipo de la lista. Un desajuste no puede volver a traducirse en una reescritura silenciosa.
- El combo de la celda de IVA SHALL NOT disparar su handler al refrescar la celda: solo cuando el usuario elige un tipo.
- Abrir una factura SHALL NOT dejarla marcada como modificada.

## Capabilities

### New Capabilities

Ninguna.

### Modified Capabilities

Ninguna. Es una corrección de implementación de un requisito ya especificado (requisito «IVA», escenario «Inactivar tipo de IVA usado»), por eso el change lleva `skip_specs`.

## Impact

- `ui/EditorController`: `cargarTiposIva()`, `cargarVersion(...)`, `tipoIvaDe(...)` y la clase interna `CeldaIva`.
- `repository/IvaRepository`: se reutiliza `getById(long)`, que ya existe; no hace falta añadir nada.
- Se reutiliza el patrón de reconstrucción desde snapshot que ya usa `RectificativaService.retencionDeVersion(...)` (`:79-94`) para el tipo de retención.
- No se toca el esquema, ni el cálculo, ni el PDF, ni la persistencia.
