> Las medidas de cada etiqueta y el presupuesto de la barra están en `design.md - D1` y `D2`.
> Las tareas marcadas ya están aplicadas en el árbol de trabajo. Las sin marcar son la ampliación posterior.

## 1. Etiquetas

- [x] 1.1 En `src/main/resources/com/alcazaba/facturacion/ui/Editor.fxml`, cambiar el `text` del botón de exportar de `Exportar PDF` a `Exportar`, sin tocar su `fx:id`, su `onAction` ni su `SVGPath`.
- [x] 1.2 En el mismo archivo, cambiar el `text` del botón de rectificar de `Rectificativa` a `Rectificar`, sin tocar nada más del botón.
- [x] 1.3 En `src/main/resources/com/alcazaba/facturacion/ui/Historico.fxml`, cambiar `Exportar PDF` por `Exportar` y `Generar mensuales` por `Facturar mes`.
- [x] 1.4 Comprobar que no queda ninguna aparición de `Exportar PDF`, `Rectificativa` ni `Generar mensuales` como texto de botón en ningún FXML.
- [x] 1.5 No tocar ningún `.java`: las coincidencias de «Rectificativa» en el código son del dominio (`esRectificativa`, `RectificativaService`) y no deben cambiarse.
- [x] 1.6 En `src/main/resources/com/alcazaba/facturacion/ui/MenuPrincipal.fxml`, línea 49, cambiar el `Label styleClass="nombre"` de `Generar mensuales` a `Facturar mes`. Es la misma acción que el botón del Histórico y el requisito de etiquetado exige que se llame igual. No tocar ni la descripción de debajo ni el icono.

## 2. Medidas de los botones de acción

- [x] 2.1 En `src/main/resources/com/alcazaba/facturacion/themes/base.css`, bloque `.btn-ribbon`: `-fx-min-width`, `-fx-pref-width` y `-fx-max-width` de `86` a `72`. Los tres.
- [x] 2.2 En el mismo bloque, **eliminar** la declaración `-fx-min-height: 78`. No sustituirla por otro valor: el alto debe salir del contenido.
- [x] 2.3 No tocar `-fx-font-size`, `-fx-graphic-text-gap` ni `-fx-padding`.

## 3. Altura de la barra de navegación

- [x] 3.1 En `base.css`, bloque `.nav-bar`: `-fx-padding` de `6px 0 8px 0` a `4px 0 4px 0`.
- [x] 3.2 En el bloque `.nav-button`: `-fx-padding` de `4px 8px` a `2px 8px`. Solo cambia el vertical; el horizontal de 8 se mantiene.
- [x] 3.3 No tocar `-fx-graphic-text-gap`, que sigue en `6px`. El icono va escalado a 1.25 dentro de una caja de 26 y ya invade ese hueco: reducirlo dejaría el icono pegado a la palabra.
- [x] 3.4 No tocar el borde inferior de `3` que marca la pantalla activa, ni los anchos de `100`.

## 4. Aire en la barra del Editor

- [x] 4.1 En el bloque `.action-bar`: `-fx-spacing` de `4px` a `8px`.
- [x] 4.2 En `Editor.fxml`, devolver `prefWidth`, `maxWidth` y `minWidth` del `StackPane fx:id="logoBox"` de `80.0` a `110.0`. No tocar las alturas.
- [x] 4.3 En `Editor.fxml`, poner `wrapText="true"` en `lblTitulo` y subir su `maxWidth` de `130.0` a `147.0`, manteniendo `minWidth="0.0"`.
- [x] 4.4 No tocar el `logoBox` de `MenuPrincipal.fxml`, que mide 280 y es otra cosa.

## 5. Verificación

- [x] 5.1 `mvn test` en verde. Atención a `EditorBarraAccionesTest` y `EditorTamanoMinimoTest`.
- [x] 5.2 Abrir el Editor a 1024x768: los ocho botones en una fila, todos del mismo ancho, ninguna etiqueta partida por dentro de una palabra.
- [x] 5.3 Comprobar en el Histórico que `Facturar mes` se parte en `Facturar` y `mes`, por el espacio. Si `Facturar` se partiera por dentro, subir el ancho de `72` a `76` y anotarlo al reportar.
- [x] 5.4 Abrir una factura con número largo y comprobar que el título se lee entero, envuelto en varias líneas, y que no se recorta con puntos suspensivos.
- [x] 5.5 Comprobar que hay un hueco visible entre el identificador de empresa y el título.
- [x] 5.6 Comprobar que la barra de acciones es más baja que antes y que la tabla de líneas ha ganado ese espacio.
- [x] 5.7 Comprobar que la barra de navegación es más baja y que el icono no llega a tocar su etiqueta en ninguna de las siete entradas.
- [x] 5.8 Comprobar en el menú principal que la entrada dice `Facturar mes` y que su descripción sigue explicando qué hace.
- [x] 5.9 Guardar una factura y anularla para que aparezca el distintivo, y comprobar que ningún botón se comprime ni desaparece.
