## 1. Clasificador de imagen

- [x] 1.1 Crear util `LogoMarco` que clasifique la imagen muestreando solo el marco exterior (~6% por lado, con paso), descartando píxeles con alfa < 0,9 y agrupando los opacos en cubos de 5 bits por canal; devuelve el caso (plano/color exacto, difuminado, transparente). Verificar con tests `WritableImage` construidos por código (marco blanco → `#FFFFFF`, marco de color → ese color, marco transparente → sin cambio, ruido aleatorio → difuminado, imagen nula y 2x2 → sin cambio), arrancando el toolkit con `JavaFxTestSupport`.

## 2. Aplicación y limpieza del relleno

- [x] 2.1 En `LogoMarco`, método `aplicar(StackPane, Image)`: para caso plano aplica `setStyle` inline pisando solo `-fx-background-color` y `-fx-border-color`; para caso difuminado añade el respaldo como primer hijo (índice 0, `mouseTransparent`, `setManaged(false)`, layout manual con desborde ~60 px y `fitWidth`/`fitHeight` = caja + desborde) y un clip de esquinas redondeadas (radio 10 px ligado a width/height de la caja); para caso transparente no hace nada. El StackPane debe quedar con tamaño fijo (min = pref = max). Verificar que sobre un StackPane el caso plano queda sin hijos, el difuminado añade un hijo y un clip, el transparente no deja ni estilo ni hijos ni clip, y aplicar dos logos seguidos no acumula nada.
- [x] 2.2 Método de limpieza que deshaga las tres cosas (estilo inline, clip y respaldo), llamado al principio de `aplicar` y en las tres salidas tempranas de la carga de logo (ruta vacía, fichero inexistente, imagen con error). Verificar que cambiar de empresa entre foto y logo plano (en los dos sentidos) no deja rastro del anterior.

## 3. Menú principal

- [x] 3.1 En `MenuController.cargarLogo`, invocar al clasificador/aplicación sobre el StackPane del menú (280x100) tras cargar la imagen y llamar a la limpieza en las salidas tempranas. Verificar visualmente (o por snapshot) que un logo con fondo blanco queda blanco entero sin franjas ni borde visible con el tema negro-dorado.

## 4. Editor

- [x] 4.1 Envolver el `ImageView` del logo dentro de un `StackPane` `menu-logo-box` de tamaño fijo 110x40 en el `ToolBar` de `Editor.fxml`, y añadir `fitHeight` (además del `fitWidth=92`) en `EditorController.cargarLogo`.
- [x] 4.2 Aplicar el mismo clasificador/relleno al StackPane del editor y la limpieza en las salidas tempranas. Verificar que, con una fotografía como logo a 1024x768, el `ToolBar` mantiene su altura, la barra no se descoloca y no aparece desenfoque fuera de la caja de 110x40 (a ojo o con un test temporal de `snapshot()` que se borre después y no quede en el proyecto).

## 5. Verificación

- [x] 5.1 Comprobar los tres casos de logo y el cambio de empresa entre foto y logo plano en los dos sentidos (no queda rastro). Confirmar que el PNG transparente se queda con el color del tema.
- [x] 5.2 Ejecutar la suite completa (`mvn test`) y confirmar que los 126 tests actuales siguen en verde.

## 6. Cierre

- [x] 6.1 /opsx-sync-specs (este change SI lleva delta) y actualizar CONTINUAR_MAÑANA.md.
- [x] 6.2 /opsx-archive y commit/push.
