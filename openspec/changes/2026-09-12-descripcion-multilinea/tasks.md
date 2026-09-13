> Las razones están en `design.md`. El flujo de teclado del Editor no debe cambiar en nada.

## 1. Medición previa

- [x] 1.1 Escribir un test temporal en `src/test/java/com/alcazaba/facturacion/ui/`, siguiendo el patrón de `EditorTamanoMinimoTest`: arrancar el Editor, `raiz.resize(1024, 768)`, `applyCss()` y `layout()`, dejar una línea con una descripción de unos 200 caracteres y leer el alto real de su fila recorriendo `tablaLineas.lookupAll(".table-row-cell")`.
- [x] 1.2 Anotar el alto que devuelve hoy. Sirve de referencia para saber si el cambio surte efecto. (Hoy: 34.0 px tanto la corta como la larga.)
- [x] 1.3 Decidir con esa medición si `-fx-cell-size: 34px` recorta el alto o solo marca un mínimo. Ver `design.md - D1`. (Decisión: implementar y re-medir en 3.1; si la larga crece, era mínimo y no se toca el CSS.)
- [x] 1.4 No borrar el test todavía: se reutiliza en la tarea 3.

## 2. La celda de descripción

- [x] 2.1 En `src/main/java/com/alcazaba/facturacion/ui/EditorController.java`, dar a `CeldaDescripcion` un `Label` propio con `setWrapText(true)` y el `prefWidth` atado a `colDescripcion.widthProperty()` menos el padding horizontal de la celda. Ver `design.md - D2`.
- [x] 2.2 Sobrescribir `updateItem` en `CeldaDescripcion` para pintar con ese `Label` como `graphic` y dejar la celda sin texto propio. La celda vacía sigue sin texto y sin gráfico.
- [x] 2.3 No tocar `startEdit` ni el manejo de ENTER, ESCAPE y pérdida de foco de `CeldaEditable`: el editor sigue siendo el mismo `TextField`. Ver `design.md - D3`.
- [x] 2.4 No tocar `CeldaCantidad`, `CeldaPrecio`, `CeldaTotal` ni `CeldaIva`. Ver `design.md - D4`.
- [ ] 2.5 Solo si la tarea 1.3 lo pidió, anular el alto fijo en `base.css` acotándolo a `#tablaLineas .table-row-cell`, nunca sobre `.table-view`.

## 3. Verificación

- [x] 3.1 Ejecutar el test temporal y comprobar que el alto de la fila con descripción larga es ahora mayor que el de una fila corta, y que una fila corta sigue midiendo lo mismo que antes del cambio. (Larga: 49.0 px; corta: 34.0 px. El 34 px era solo mínimo: no hizo falta el CSS de 2.5.)
- [x] 3.2 Borrar el test temporal. No debe quedar en el proyecto.
- [x] 3.3 `mvn test` en verde, con atención a `EditorTamanoMinimoTest` y `EditorFlujoTecladoTest`.
- [ ] 3.4 En la aplicación: escribir una descripción larga, confirmar con ENTER y comprobar que la fila crece, que se lee entera y que el foco salta a Precio. Comprobar también que al escribir se encadenan cantidad, descripción, precio y total como hasta ahora.
- [ ] 3.5 Maximizar la ventana y comprobar que la columna de descripción se ensancha y que esa misma línea pasa a ocupar menos alto.
- [ ] 3.6 Con una factura de muchas líneas, comprobar que el desglose de totales del pie sigue visible y que solo se desplaza la tabla.
- [ ] 3.7 Generar el PDF de esa factura y comprobar que la descripción impresa coincide con la que se ve en pantalla.
- [ ] 3.8 Abrir Clientes, Histórico y la matriz de IVA del Editor y comprobar que sus filas conservan el alto de siempre.
- [ ] 3.9 Repetir 3.4 y 3.5 con un tema claro y con uno oscuro, comprobando que el texto de la descripción se lee en ambos.

## 4. Envolver también mientras se escribe

> Añadido el 12/09/2026. Con las tareas 1 a 3 la fila solo crece al confirmar; el usuario quiere ver los renglones mientras teclea. Ver `design.md - D5` y `D6`.

- [ ] 4.1 En `CeldaEditable`, cambiar el tipo del campo `editor` de `TextField` a `TextInputControl` y crearlo en un método que las subclases puedan sobrescribir. No tocar el manejador de ENTER y ESCAPE, ni el listener de foco, ni `commitYAvanzar`, ni `commitSolo`, ni `commitValor`. Ver `design.md - D5`.
- [ ] 4.2 Comprobar que `CeldaCantidad`, `CeldaPrecio` y `CeldaTotal` siguen sin cambios y con su `TextField`.
- [ ] 4.3 En `CeldaDescripcion`, crear un `TextArea` con `setWrapText(true)` y `setPrefRowCount(1)`, y atar su `prefHeight` al alto del nodo de texto interno (`lookup(".text")`) en cuanto el control tenga skin. Ver `design.md - D5`.
- [ ] 4.4 Hacer que `computePrefHeight` de `CeldaDescripcion` mida el editor cuando la celda está en edición y la etiqueta cuando no. **Medir** con un test temporal, como en la tarea 1, que la fila crece al meter texto largo en el editor abierto, y que el `TextArea` no saca barra de desplazamiento. Si la `VirtualFlow` no recalcula el alto, pedir el recálculo de esa fila; **nunca** con `tablaLineas.refresh()`, que tira el editor. Borrar el test al terminar.
- [ ] 4.5 En `editarCeldaSegura`, buscar el editor por `.text-field` y por `.text-area`, y tratarlo como `TextInputControl`. Adaptar igual las dos búsquedas de `EditorFlujoTecladoTest` (líneas 118 y 127), sin cambiar lo que el test comprueba. Ver `design.md - D6`.
- [ ] 4.6 `mvn test` en verde y, en la aplicación: escribir una descripción larga y ver que salta de renglón y que la fila crece **mientras** se teclea; confirmar con ENTER y comprobar que el foco sigue yendo a Precio; comprobar que ENTER no deja ningún salto de línea dentro de la descripción, que ESCAPE sigue cancelando y qué hace la tecla TAB dentro del campo.
- [ ] 4.7 Repetir las comprobaciones 3.4 a 3.9, que quedaron pendientes, ya con el editor nuevo.

## 5. El editor debe mostrar siempre todo el texto

> Medido el 12/09/2026: `prefHeightAtado=false`. La atadura nunca se crea, el editor se queda clavado en 36,96 px, saca barra de desplazamiento a partir de dos renglones y la fila no se mueve de 42,96 px mientras se escribe. Ver `design.md - D5b`, que trae la medición entera y los criterios de aceptación.

- [x] 5.1 En `CeldaDescripcion`, arreglar la atadura del alto para que se cree siempre: forzar que el skin esté maquetado antes de buscar el nodo interno, o medir el alto del texto por cuenta propia con el mismo tipo de letra y ancho de envoltura. Un `lookup` a una sola carta no vale: si falla se queda sin atadura y no se entera nadie. Ver `design.md - D5b`, punto 1.
- [x] 5.2 Hacer que el `TextArea` tome su alto mínimo del preferido, para que el mínimo heredado del `ScrollPane` no vuelva a clavarlo en los 37 px. Ver `design.md - D5b`, punto 2.
- [x] 5.3 Al calcular el alto, contar el relleno del nodo `.content` (3 px arriba y 3 abajo) y el borde del `ScrollPane`, no los insets del `TextArea`, que Modena deja a cero. Sin eso el editor recorta la última línea.
- [x] 5.4 Hacer que la fila siga al editor: `computePrefHeight` lee el alto preferido real del editor y algo pide el recálculo de la fila cada vez que ese alto cambia. Sin eso la `VirtualFlow` reutiliza el alto cacheado. **Prohibido** `tablaLineas.refresh()`. Ver `design.md - D5b`, punto 3.
- [x] 5.5 Medir con un test temporal, y borrarlo después, los tres criterios de `design.md - D5b`: fila de 34,0 px con una línea, con el editor abierto y cerrado; editor y fila creciendo a la vez al pasar a dos renglones; y ninguna barra de desplazamiento con ninguna longitud de texto. El test debe comprobar además que `prefHeightProperty().isBound()` es cierto, que es lo que falló esta vez.
- [ ] 5.6 Comprobar a mano lo mismo: pinchar en una descripción corta y ver que la fila no da ningún salto; escribir sin parar y ver que el campo enseña todo lo escrito en todo momento, sin barra y sin tener que pulsar ENTER.
- [x] 5.7 Borrar `src/test/java/com/alcazaba/facturacion/ui/TmpEditorCreceTest.java`, que quedó de la tarea 4.4 y no debe llegar a un commit. Su comprobación de barras daba por bueno el estado actual, así que rehacerla como pide la tarea 5.5.
