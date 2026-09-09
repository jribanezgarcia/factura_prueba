> La causa y el razonamiento están en `design.md`.

## 1. Centrar el bloque

- [x] 1.1 En `src/main/resources/com/alcazaba/facturacion/ui/MenuPrincipal.fxml`, línea 23, cambiar el `alignment` del `StackPane` de `TOP_CENTER` a `CENTER`.
- [x] 1.2 Comprobar que no se ha tocado nada más del archivo: el `HBox` de la línea 24 conserva `alignment="TOP_LEFT"`, y las tarjetas conservan sus anchos de 300 y 430 sin `vgrow` ni `hgrow`.

## 2. Test de maquetación

- [x] 2.1 Crear `src/test/java/com/alcazaba/facturacion/ui/MenuLayoutTest.java` siguiendo el patrón de `src/test/java/com/alcazaba/facturacion/ui/BackupLayoutTest.java`: `JavaFxTestSupport.arrancarFx()`, un `Navegador` sobre un `Stage` nuevo, y aserciones sobre `Bounds` en coordenadas de escena.
- [x] 2.2 Comprobar a 1024x768 que el centro del bloque de tarjetas coincide con el centro de su contenedor en los dos ejes, con una tolerancia de un par de píxeles para el redondeo del layout.
- [x] 2.3 Repetir la comprobación con la escena a un tamaño mayor, por ejemplo 1280x900, para cubrir el redimensionado.
- [x] 2.4 Comprobar que las dos tarjetas siguen alineadas por su borde superior y que ninguna ha crecido más allá de su ancho declarado.
- [x] 2.5 Comprobar que el test falla si se repone `TOP_CENTER` en el FXML, y reponer el valor bueno.

## 3. Verificación final

- [x] 3.1 `mvn test` en verde, con el test nuevo incluido.
- [x] 3.2 Abrir el menú principal y redimensionar desde 1024x768 hasta maximizada: el bloque se mantiene centrado en todo momento, sin saltos.
- [x] 3.3 Comprobar que la fila de la fecha de trabajo, en el `top` del `BorderPane`, sigue donde estaba.
- [x] 3.4 Entrar y salir del menú desde otra pantalla para confirmar que ninguna otra vista se ha visto afectada.
