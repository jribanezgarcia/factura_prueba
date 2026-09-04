> Los textos, el padding y el tope del título están fijados con sus valores
> exactos en `design.md`, sección «D5. Valores exactos». Usar esos números tal
> cual; no reinterpretarlos.

## 1. Test que reproduce el fallo

- [x] 1.1 Crear `EditorBarraAccionesTest` calcado de `EditorTamanoMinimoTest`: monta el Editor a 1024x768, fuerza `btnAnular` a `setVisible(true)` y `setManaged(true)`, pone en `lblTitulo` un texto largo tipo «Factura R-12/2026 (v3)» y hace `applyCss()` y `layout()`.
- [x] 1.2 Comprobar que ningún `Button` de la barra tiene bounds vacíos (antes se comprobaba `.tool-bar-overflow-button`, pero al sustituir `ToolBar` por `HBox` ya no existe ese nodo).
- [x] 1.3 Ejecutar y confirmar que **falla** con el FXML actual.
- [x] 1.4 Añadir `barraNoDesbordaConFacturaAnuladaYTituloLargo`: fuerza chip ANULADA visible + `btnRestaurar` visible + título largo → comprueba sin desbordamiento.

## 2. Etiquetas del editor

- [x] 2.1 En `Editor.fxml`, «Crear rectificativa» pasa a «Rectificativa».
- [x] 2.2 «Nueva factura» pasa a «Nueva».
- [x] 2.3 «Eliminar línea (Supr)» pasa a «Eliminar línea», con el atajo movido a un `Tooltip`.

## 3. Botones más compactos

- [x] 3.1 En `base.css:132`, cambiar `-fx-padding: 8px 14px` por `-fx-padding: 8px 10px` en la regla compartida de `.primary-button, .default-button, .danger-button, .action-button, .action-danger-button`.
- [x] 3.2 Revisar visualmente Histórico, Clientes, Configuración, Backup y Generar mensuales, que heredan el cambio.

## 4. Tope de anchura del título

- [x] 4.1 Dar a `lblTitulo` un `maxWidth="130.0"` y `minWidth="0.0"`, dejando que la elipsis por defecto de `Label` recorte el texto. (El valor es 130, no 200, para dejar margen al chip ANULADA.)
- [x] 4.2 Comprobar con un número de factura largo que el título se recorta y los botones no se mueven.

## 5. Campos `fx:id` huérfanos

- [x] 5.1 Añadir los campos `@FXML private Button btnVersiones` y `btnRectificativa` en `EditorController`.
- [x] 5.2 En `actualizarBotonesEstado()`, deshabilitarlos cuando no hay factura abierta (`facturaAbiertaId == null`), en lugar de dejar que respondan con un `Dialogos.info`.

## 6. Especificación

- [x] 6.1 MODIFIED «Menú y navegación»: la barra del editor se nombra con las etiquetas nuevas, y el escenario «Crear rectificativa desde la factura» se ajusta al nombre nuevo del botón.
- [x] 6.2 ADDED «Barra de acciones del editor sin desbordamiento».

## 7. Verificación final

- [x] 7.1 Suite completa en verde con `mvn test` (165 tests, 0 fallos).
- [x] 7.2 Verificación manual: factura nueva, factura guardada (aparece Anular), factura con número largo y factura anulada; en ningún caso debe salir el chevrón de desbordamiento.
- [x] 7.3 Repaso rápido de las demás pantallas con los botones más compactos.

## 8. Corrección adicional (factura anulada)

El test original solo cubría el caso con `btnAnular` visible. Al añadir el caso anulado (chip ANULADA + `btnRestaurar`), se descubrió que el `ToolBar` de JavaFX ignora `maxWidth` de sus items — el chip ANULADA empujaba los botones fuera sin que la restricción de ancho tuviera efecto. Se corrigió:

- [x] 8.1 Sustituir `ToolBar` por `HBox` en `Editor.fxml` (el `HBox` sí respeta `maxWidth` de sus hijos).
- [x] 8.2 Reducir `lblTitulo` `maxWidth` de 200 a 130 para compensar el espacio del chip.
- [x] 8.3 Añadir `lblEstado` `maxWidth="80.0"` para limitar el chip ANULADA.
- [x] 8.4 Actualizar test para usar `HBox` en lugar de `ToolBar`.
