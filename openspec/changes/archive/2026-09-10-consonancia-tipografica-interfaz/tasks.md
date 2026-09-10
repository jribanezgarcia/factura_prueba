> Los anchos medidos y la aritmética del presupuesto de la barra están en `design.md - D2` y `D3`.
> El árbol de trabajo ya tiene aplicada una versión anterior de este change, con el ribbon a 12px pero sin ensanchar. Hay que ajustarla.

## 1. Barra de navegación

- [x] 1.1 En `src/main/resources/com/alcazaba/facturacion/themes/base.css`, bloque `.nav-button`: `-fx-font-size` a `12px` y `-fx-font-weight: bold`.
- [x] 1.2 En el mismo bloque, `-fx-min-width` y `-fx-pref-width` de `90` a `100`.

## 2. Barras de acciones del Editor y del Histórico

- [x] 2.1 En el bloque `.btn-ribbon`: `-fx-font-size` a `12px` y `-fx-font-weight: bold`.
- [x] 2.2 En el mismo bloque, `-fx-min-width`, `-fx-pref-width` y `-fx-max-width` de `64` a `86`. Los tres, para que el ancho siga clavado.
- [x] 2.3 En el mismo bloque, `-fx-min-height` de `68` a `78`.
- [x] 2.4 Dejar `.primary-button.btn-ribbon` como está. Su `-fx-font-weight: bold` pasa a ser redundante, pero el bloque también fija `-fx-text-fill: -fx-accent`.

## 3. Recuperar espacio en la barra del Editor

- [x] 3.1 En el bloque `.action-bar` de `base.css`: `-fx-spacing` de `8px` a `4px`. Comprobar antes que `action-bar` solo la usa `Editor.fxml`, para no afectar a otras pantallas.
- [x] 3.2 En `src/main/resources/com/alcazaba/facturacion/ui/Editor.fxml`, línea 19: bajar `prefWidth`, `maxWidth` y `minWidth` del `StackPane fx:id="logoBox"` de `110.0` a `80.0`. No tocar las alturas.
- [x] 3.3 No tocar el `logoBox` de `MenuPrincipal.fxml`, que mide 280 y es otra cosa.
- [x] 3.4 No tocar el `spacing="8"` del `HBox` de botones del Histórico: esa fila no compite por el ancho con nada.

## 4. Verificación de anchos y envoltura

- [x] 4.1 Abrir el Editor con la ventana en 1024x768: los 8 botones visibles a la vez, todos del mismo ancho, ninguno recortado.
- [x] 4.2 Comprobar que `Rectificativa` se muestra en **una sola línea**. Si se parte, subir el ancho de `86` a `90` y volver a comprobar, anotándolo al reportar.
- [x] 4.3 Comprobar que `Exportar PDF` se envuelve en dos líneas por el espacio, sin partir ninguna palabra.
- [x] 4.4 Abrir el Histórico y comprobar que `Generar mensuales` se muestra en **dos líneas**, no en tres.
- [x] 4.5 Comprobar que el título de la factura se sigue leyendo con el distintivo de anulada oculto, y que solo se recorta con elipsis cuando aparece.
- [x] 4.6 Comprobar que el logo de empresa a 80 de ancho sigue reconociéndose y no queda deformado.
- [x] 4.7 Comprobar en la barra de navegación que ninguna etiqueta se recorta, en especial `Configuración` e `Histórico`. Si se recortan, subir de `100` a `110` y anotarlo.

## 5. Verificación final

- [x] 5.1 `mvn test` en verde. Prestar atención a `EditorBarraAccionesTest` y `EditorTamanoMinimoTest`, que comprueban que la barra no desborda.
- [x] 5.2 En Configuración, comprobar que las entradas de la lista de secciones se leen en negrita y la seleccionada se distingue por su fondo y su color de texto.
- [x] 5.3 Recorrer las pantallas con un tema claro y uno oscuro.
