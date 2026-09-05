## 1. Rehacer las aserciones del test

- [x] 1.1 En `EditorBarraAccionesTest`, eliminar de **los dos** métodos la aserción sobre `.tool-bar-overflow-button`: ese nodo no existe desde que la barra es un `HBox`, así que la condición es siempre falsa y la aserción no puede fallar.
- [x] 1.2 Sustituir `assertFalse(bounds.isEmpty())` por las dos comprobaciones de D1: que `getBoundsInParent().getMaxX()` de cada botón no supera el ancho de la escena, y que `getWidth() >= prefWidth(-1) - 0.5`.
- [x] 1.3 Usar la variable `anchoEscena`, que hoy se calcula en la línea 97 y no se usa; replicarla en el segundo método, que ni siquiera la calcula.
- [x] 1.4 Comprobar que el mensaje de fallo nombra el botón concreto, para que una regresión futura se diagnostique sin depurar.

## 2. Demostrar que el test sirve

- [x] 2.1 Revertir temporalmente `base.css` a `-fx-padding: 8px 14px` en la regla compartida de botones.
- [x] 2.2 Ejecutar `EditorBarraAccionesTest` y confirmar que **los dos métodos fallan** por la aserción de compresión.
- [x] 2.3 Restaurar `-fx-padding: 8px 10px` y confirmar que vuelven a pasar. Anotar aquí el resultado de los dos pasos.

> **Resultado de la demostración:** con `8px 14px` el método de factura anulada falla por la aserción de compresión («El boton Guardar esta comprimido por debajo de su ancho preferido»), y con `8px 10px` vuelven a pasar los dos. El método de factura emitida no falla ni siquiera con 14 px: con el título dinámico a 200 y un título corto, la barra tiene hueco real y no comprime ningún botón — el test acierta al no señalar una compresión que no existe.

## 3. Tope del título según el estado

- [x] 3.1 En `EditorController.actualizarBotonesEstado()`, añadir `lblTitulo.setMaxWidth(anulada ? 130 : 200);` aprovechando la variable `anulada` que ya se calcula allí.
- [x] 3.2 Dejar el `maxWidth="130.0"` del FXML como valor inicial y **no** tocar `minWidth="0.0"`.
- [x] 3.3 Comprobar a mano que «Factura C-59/7 (v1)» se lee entero en una factura emitida y que en una anulada el chip ANULADA y todos los botones siguen cabiendo.

## 4. Ancho de los botones de navegación

- [x] 4.1 En `base.css`, subir `-fx-min-width` y `-fx-pref-width` de `.nav-button` de 80 a 90.
- [x] 4.2 **Eliminar** `-fx-max-width`, para que el botón pueda crecer si una etiqueta lo pide en vez de recortarla.
- [x] 4.3 Comprobar que «Configuración» se lee completo y que la barra sigue centrada y cabiendo a 1024 px.

> **Nota:** el 90 de 4.1 se aplicó, se revirtió a 80 al meter la caja de icono 26×26 y se ha vuelto a dejar en 90: con 80 la caja de contenido son 64 px contra ~61 de «Configuración» (3 px, la estrechez que motivaba el 90), y la caja del icono no cambia esa cuenta porque el ancho lo manda el rótulo. El código actual dice 90 sin `-fx-max-width`, acorde con 4.1 y 4.2.
> **Desvío óptico (fuera de 4.x, sin cambio de layout):** la caja 26×26 centra la envolvente pero la tinta de «Salir» se va a la izquierda (puerta ancha + flecha fina). `BarraNavegacion.icono(...)` acepta `offsetX` y aplica `caja.setTranslateX(offsetX)`: Salir +2,8, Histórico −1,3, resto 0. El translate no mueve el texto ni cambia el ancho del botón.

## 5. Especificación

- [x] 5.1 MODIFIED «Barra de acciones del editor sin desbordamiento»: corregir `SHALL NO` por `SHALL NOT` y precisar que el título conserva su texto completo mientras haya espacio, recortándose solo cuando el distintivo de anulada reduce el sitio disponible.

## 6. Verificación final

- [x] 6.1 Suite completa en verde con `mvn clean test`.
- [x] 6.2 `EditorTamanoMinimoTest` en verde, confirmando que nada de esto afecta al alto del editor.
- [x] 6.3 Repaso visual de la barra de navegación en los 7 temas a 1024×768.
