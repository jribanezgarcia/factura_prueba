## Context

`tiposIva` se llena una sola vez, en `alIniciar()` (`EditorController:206`), con los tipos **activos**. Las líneas se cargan después, en `cargarVersion(...)` (`:284`, `lineas.setAll(vc.lineas())`). Entre ambos momentos nadie comprueba que los tipos que usan las líneas estén en la lista. Ver proposal.md - Why.

## Goals / Non-Goals

**Goals:**

- Que abrir una factura nunca altere sus líneas ni la marque como modificada.
- Que una factura con un tipo de IVA inactivo o borrado se vea con su tipo real, no con otro.
- Que el combo siga permitiendo cambiar el tipo cuando el usuario lo elige a mano.

**Non-Goals:**

- No se ofrecen los tipos inactivos para **nuevas** líneas: solo se añade a la lista el que ya usa una línea de la factura abierta.
- No se cambia el cálculo, la persistencia ni el snapshot de las líneas.
- No se rehace la celda de IVA con otro control.

## Decisions

### D1. Ampliar la lista con los tipos que la factura ya usa

Después de `lineas.setAll(vc.lineas())` en `cargarVersion(...)`, recorrer las líneas y, por cada `tipoIvaId` que no esté en `tiposIva`, añadirlo:

- primero `servicios.ivas.getById(id)`, que ya existe (`IvaRepository:27`) y no filtra por activo;
- si devuelve `null` (el maestro se borró), reconstruir un `TipoIva` desde el snapshot de la línea (`ivaNombre`, `ivaPorcentaje`, `ivaMotivoExencion`), con `activo = false`.

Es el mismo patrón que `RectificativaService.retencionDeVersion(...)` (`:79-94`) usa para el tipo de retención cuando el maestro ya no está; conviene que se parezcan.

Se descarta la alternativa de cargar siempre `listar(false)` (todos los tipos): eso ofrecería los inactivos para líneas nuevas, que es justo lo que el requisito «IVA» prohíbe («no se ofrece para nuevas facturas»).

### D2. `tipoIvaDe(...)` deja de adivinar

Hoy, si no encuentra por id, prueba por porcentaje y si no cae a `tiposIva.get(0)`. Con D1 el id siempre estará, así que los dos respaldos sobran; el de `get(0)` es además el que convierte un desajuste en una reescritura. Pasa a devolver `null`.

El respaldo por porcentaje se puede conservar sin daño (dos tipos distintos con el mismo porcentaje son equivalentes a efectos de cuota), pero el `get(0)` final se sustituye por `null`. Con `null`, `combo.setValue(null)` deja la celda vacía en vez de mentir, y el handler no hace nada porque comprueba `t != null`.

### D3. El combo no dispara el handler al pintarse

Aunque D1 y D2 quitan la causa, la reentrancia sigue estando: `setValue` dentro de `updateItem` dispara `onAction`, y el handler llama a `refrescarLineas()` (`tablaLineas.refresh()`) desde dentro del propio `updateItem`.

Silenciar el handler alrededor del `setValue` de `updateItem`:

```java
combo.setOnAction(null);
combo.setValue(tipoIvaDe(l));
combo.setOnAction(handler);
```

con el handler guardado en un campo de la celda. Así solo se ejecuta cuando el usuario elige un valor. La alternativa —un flag `actualizando`— hace lo mismo con más estado.

### D4. Qué se prueba y cómo

El fallo es de UI y necesita un test de los que ya existen en `ui` (`EditorNifValidationTest` es el patrón más cercano: monta el `Navegador`, muestra el editor y opera sobre los controles en el hilo de JavaFX con `JavaFxTestSupport`).

El test debe: crear una factura con un tipo de IVA, inactivar ese tipo, abrir la factura en el editor, forzar el layout de la tabla (`applyCss` + `layout`, como hace `EditorTamanoMinimoTest`) para que `updateItem` se ejecute de verdad, y comprobar que la línea conserva su `tipoIvaId` y su `ivaImporte`, y que la vista **no** queda marcada como modificada.

Si forzar el render resulta frágil, el mínimo aceptable es un test sobre `tipoIvaDe(...)` y sobre la ampliación de la lista, más comprobación manual del render. Dejar constancia en `tasks.md` de cuál de los dos se hizo.

## Verificación

- El test nuevo debe **fallar** con el código actual.
- A mano: crear una factura con IVA 10 %, inactivar el 10 % en Configuración, abrir la factura desde el Histórico y comprobar que la columna IVA sigue diciendo 10 %, que los importes no cambian y que al pulsar Volver **no** aparece el diálogo de cambios sin guardar.
- A mano: comprobar que en una factura nueva el combo sigue ofreciendo solo los tipos activos.
