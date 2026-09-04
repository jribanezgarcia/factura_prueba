## 1. Test que reproduce el fallo

- [x] 1.1 Añadir `EditorIvaInactivoTest` siguiendo el patrón de `EditorNifValidationTest`: crear una factura con un tipo de IVA, inactivar ese tipo, abrir la factura en el editor y forzar el layout de la tabla (`applyCss()` + `layout()`, como en `EditorTamanoMinimoTest`) para que `CeldaIva.updateItem` se ejecute.
- [x] 1.2 Comprobar que la línea conserva su `tipoIvaId` y su `ivaImporte`, y que la vista no queda marcada como modificada (`puedeCerrar()` devuelve `true` sin abrir diálogo).
- [x] 1.3 Ejecutar y confirmar que **falla** con el código actual. Se tomo el camino del test de render directo (applyCss + layout + comprobacion del combo).

## 2. Tipos que la factura ya usa

- [x] 2.1 En `cargarVersion(...)`, tras `lineas.setAll(vc.lineas())`, añadir a `tiposIva` los tipos que usen las líneas y no estén ya en la lista, resolviéndolos con `servicios.ivas.getById(id)`.
- [x] 2.2 Si `getById` devuelve `null`, reconstruir el `TipoIva` desde el snapshot de la línea (`ivaNombre`, `ivaPorcentaje`, `ivaMotivoExencion`) con `activo = false`, igual que hace `RectificativaService.retencionDeVersion(...)`.
- [x] 2.3 Comprobar que en una factura **nueva** el combo sigue ofreciendo solo los tipos activos.

## 3. `tipoIvaDe`

- [x] 3.1 Sustituir el respaldo final `tiposIva.get(0)` por `null`.
- [x] 3.2 Comprobar que `CeldaIva` tolera `null` sin pintar nada raro y sin disparar el handler (ya comprueba `t != null`).

## 4. Reentrancia del combo

- [x] 4.1 Guardar el handler de `CeldaIva` en un campo y silenciarlo alrededor del `setValue(...)` de `updateItem` (`setOnAction(null)` → `setValue` → `setOnAction(handler)`).
- [x] 4.2 Comprobar que cambiar el tipo a mano en el combo sigue recalculando la línea y marcando la factura como modificada.

## 5. Verificación final

- [x] 5.1 Suite completa en verde con `mvn clean test`.
- [x] 5.2 Verificación manual: factura con IVA 10 %, inactivar el 10 %, abrirla desde el Histórico; la columna sigue en 10 %, los importes no cambian y Volver no pregunta por cambios sin guardar.
- [x] 5.3 Verificación manual: en una factura nueva el desplegable de IVA no ofrece el tipo inactivo.
