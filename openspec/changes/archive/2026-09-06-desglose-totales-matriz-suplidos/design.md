## Context

`CalculoService.resumen(...)` (`CalculoService.java:92`) agrupa las líneas por `ClaveIva(nombre, porcentaje, motivo)`, aplica el factor de descuento a cada base, ajusta los céntimos sobre el grupo de mayor base y recalcula cada cuota. Todo eso ya está en `ResumenFactura.getGrupos()`, pero el editor lo ignora: `EditorController.actualizarResumen(...)` (`EditorController.java:874-903`) rellena seis `HBox` fijos declarados en `Editor.fxml:163-194`, y la fila de IVA es un agregado (`EditorController.java:889`).

`CalculoService` solo ve `LineaFactura`, que guarda un snapshot del IVA (`ivaNombre`, `ivaPorcentaje`, `ivaMotivoExencion`), no el `TipoIva` de origen. Ver proposal.md - Why.

## Goals / Non-Goals

**Goals:**

- Que el editor muestre el desglose por tipo de IVA que ya calcula el servicio, en forma de matriz.
- Que observaciones y totales dejen de competir por el ancho.
- Que un suplido se pueda facturar como línea sin contaminar base imponible ni retención.

**Non-Goals:**

- No se añaden recargo de equivalencia, portes, financiación ni pronto pago.
- No se recalculan versiones históricas.
- No se tocan los 7 ficheros de tema.

## Decisions

### D1. Decisión: los suplidos son un tipo de IVA con bandera propia

El usuario elige «Suplido» en el mismo desplegable de IVA de la línea, igual que hoy elige «Exento». Para que el cálculo pueda distinguirlos, se añade `es_suplido` a `tipo_iva` y se propaga al snapshot `factura_linea.es_suplido`.

Alternativa descartada: usar un valor centinela en `motivo_exencion`. Se rompe en cuanto alguien renombre el tipo, y `CalculoService` acabaría comparando cadenas.

Alternativa descartada: un campo global de importe junto a `Descuento` y `Retención`. Más barato, pero no detalla qué gastos son ni los lleva al PDF línea a línea.

### D2. Decisión: los suplidos no entran en base, IVA, retención ni descuento

En `CalculoService.resumen(...)` las líneas con `esSuplido` se separan antes del bucle de agrupación (`CalculoService.java:99-106`): no entran en `bases`, ni en `baseTotalSinDescuento`, ni en el ajuste de céntimos, ni en la base de retención, y el descuento global no les afecta. Se suman aparte en `ResumenFactura.totalSuplidos`.

El total pasa de `baseTotalDescontada + ivaTotal − importeRetencion` (`CalculoService.java:158`) a `baseTotalDescontada + ivaTotal − importeRetencion + totalSuplidos`.

Hay que actualizar el javadoc de reglas de la clase (`CalculoService.java:14-27`), que documenta las reglas vigentes.

### D3. Decisión: matriz construida en Java, escalera declarada en FXML

El número de filas de la matriz es dinámico, así que `EditorController` la repuebla en `actualizarResumen(...)` a partir de `r.getGrupos()`. La escalera son filas fijas condicionales, así que sigue en FXML con el patrón actual de `visible`/`managed` que ya usan `filaDescuento` y la retención (`EditorController.java:876-894`), incluida la llamada a `togglePrimera(...)` (`EditorController.java:905-913`).

### D4. Decisión: la matriz solo lleva base y cuota

Columnas `IVA | Base imponible | Cuota IVA`. Se descartan las columnas Neto y Descuento por tipo: el subtotal y el descuento ya aparecen en la escalera, y repetirlos obliga al lector a cuadrar dos veces la misma cifra. La matriz sirve para una sola comprobación: que cada cuota sale de la base que tiene al lado.

### D5. Valores exactos

Pie del editor, sustituyendo `Editor.fxml:157-196`:

- `bottom` pasa de `HBox` a `VBox spacing="4" styleClass="zona-editor-pie"`.
- Fila de observaciones: `HBox spacing="8"` con `Label text="Observaciones"` y `TextArea fx:id="txtObservaciones" wrapText="true" prefRowCount="1" HBox.hgrow="ALWAYS"`, que crece hasta 3 filas según el texto.
- Fila inferior: `HBox spacing="8" alignment="TOP_LEFT"` con la matriz (`HBox.hgrow="ALWAYS"`) y la escalera (`VBox fx:id="escalera" styleClass="totales, totales-compacta" minWidth="236" maxWidth="236"`).

Clases CSS: la escalera reutiliza `totales`, `totales-compacta`, `total-fila`, `total-fila-primera`, `total-grande` y `valor`; la matriz reutiliza `tabla-productos`. Todas están ya coloreadas en los 7 temas, así que ninguno se toca. En `base.css` (zona 191-256) solo se añade lo estructural.

Etiquetas de la escalera: `Subtotal`, `Descuento N %`, `Base imponible`, `IVA total`, nombre de la retención + `N %`, `Suplidos`, `TOTAL FACTURA`.

Filas por grupo en la matriz: nombre del tipo con su porcentaje (`General 21 %`), `Exento` con su motivo entre paréntesis si lo tiene, y fila final `Totales` con las sumas de base y cuota.

Migración `008_suplidos.sql`:

- `ALTER TABLE tipo_iva ADD COLUMN es_suplido INTEGER NOT NULL DEFAULT 0;`
- `ALTER TABLE factura_linea ADD COLUMN es_suplido INTEGER NOT NULL DEFAULT 0;`
- `ALTER TABLE factura_version ADD COLUMN total_suplidos TEXT;`
- `INSERT OR IGNORE INTO tipo_iva (id, nombre, porcentaje, motivo_exencion, activo, es_suplido) VALUES (4, 'Suplido', NULL, NULL, 1, 1);`

Registrar el script en `Migrations.SCRIPTS` (`Migrations.java:18-26`).

En `ConfiguracionController` (zona 451-548), al marcar «Es suplido» el porcentaje queda deshabilitado y se guarda nulo. `IvaRepository.enUso(...)` ya protege los tipos usados en el histórico.

## D6. Fuera de alcance

- Recargo de equivalencia (exigiría porcentajes decimales; `TipoIva.porcentaje` y `TipoRetencion.porcentaje` son `Integer`).
- Portes, financiación y descuento por pronto pago.
- Reparto de la retención por tipo de IVA: se calcula sobre la base imponible completa, y repartirla por filas sería un reparto inventado.

## Verificación

- Casos nuevos en los tests de `CalculoService`: suplido solo, suplido con descuento, suplido con retención, y una factura sin suplidos cuyos importes no cambian.
- `PdfServiceTest`: fila de suplidos presente y en su posición, entre retención y TOTAL.
- `EditorTamanoMinimoTest`, `EditorTotalesDescuentoTest` y `StyleClassSeparadorTest` ajustados a la estructura nueva.
- Comprobación manual a 1024×768 con factura de varios tipos, descuento, retención y suplido.
