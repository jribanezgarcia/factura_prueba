> Los valores exactos están en `design.md`, sección «D5. Valores exactos».
> Usar esos números tal cual; no reinterpretarlos.

## 1. Editor.fxml: barra y cabecera

- [x] 1.1 Mover el `Button text="Nueva"` para que quede inmediatamente después de `btnGuardar` (orden D5-A1) y verificar que la barra carga con el orden nuevo.
- [x] 1.2 Re-ejecutar `EditorBarraAccionesTest` y ajustarlo si afirmase el orden antiguo de la barra; el test no afirma orden hoy, solo contenencia y no-compresión.
- [x] 1.3 Fijar las 4 columnas de etiqueta a `hgrow="NEVER" minWidth="-Infinity" prefWidth="105.0"` (D5-A2) y verificar que «Forma de pago» y «Vencimiento» se leen enteros a 1024.
- [x] 1.4 Quitar `maxWidth="Infinity"` de los controles y `hgrow="ALWAYS"` de las columnas de campo, conservando sus `prefWidth` (D5-A3); `txtReferencia` mantiene su `maxWidth`.
- [x] 1.5 Topar los dos `VBox` con `prefWidth`/`maxWidth` 440/485 y columnas CLIENTE 200/210 (NIF/CP/Provincia ~129 px, +47 %) y añadir el `Region HBox.hgrow="ALWAYS"` final (D5-A4); verificar que al maximizar solo crece `tablaLineas`.

## 2. Historico.fxml: filtros en rejilla fija

- [x] 2.1 Sustituir el `FlowPane` por el `GridPane styleClass="grid-filtros"` de 8 columnas × 2 filas con el reparto D5-B1, mismos `fx:id` y `prefWidth`; no tocar las etiquetas «Hasta» duplicadas.
- [x] 2.2 Añadir la regla `.grid-filtros` en `base.css` junto a `.grid-editor`, sin tocar ninguna regla existente.
- [x] 2.3 Verificar que a 1024 y maximizado los 7 filtros mantienen filas y posiciones y solo crece `tabla`.

## 3. Especificación

- [x] 3.1 MODIFIED «Barra de acciones del editor sin desbordamiento»: `Nueva` SHALL ir inmediatamente después de `Guardar`, con su escenario.
- [x] 3.2 ADDED «Distribución estable al redimensionar en Editor e Histórico», con sus tres escenarios.

## 4. Verificación final

- [x] 4.1 Suite completa en verde con `mvn test`, con atención a `EditorBarraAccionesTest`, `EditorTamanoMinimoTest` y `UiSmokeTest`.
- [x] 4.2 A mano a 1024×768 y maximizado: etiquetas enteras, campos con el mismo ancho en ambos tamaños, filtros fijos, `Nueva` junto a `Guardar`, y con factura anulada la barra sigue sin desbordarse.
