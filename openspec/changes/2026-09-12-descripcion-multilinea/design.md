## Context

La tabla `tablaLineas` (`Editor.fxml:188-199`) usa cinco celdas propias, clases internas de `EditorController`: `CeldaCantidad`, `CeldaDescripcion`, `CeldaPrecio`, `CeldaTotal` y `CeldaIva`. Las cuatro primeras heredan de `CeldaEditable` (`EditorController.java:1442-1527`), que en modo lectura pinta con `setText(...)` sobre el propio `TableCell` y en modo edición pone un `TextField` como `graphic`.

Un `TableCell` es un `Labeled` con `wrapText` en falso, así que el texto sale en una sola línea. Y el editor es un `TextField`, que por definición desplaza el contenido en lugar de partirlo.

Hay un detalle de JavaFX que condiciona la solución: `TableRowSkinBase.computePrefHeight` pregunta el alto a cada celda con `prefHeight(-1)`, **sin pasarle el ancho**. Por eso poner `setWrapText(true)` en la celda no basta: sin ancho no hay envoltura que calcular y la celda sigue devolviendo el alto de una línea. Hace falta que el nodo que pinta el texto tenga ya un ancho propio, atado al de la columna, cuando se le pregunta el alto.

La tabla no tiene `setFixedCellSize`, que sí tiene `matrizIva` (`EditorController.java:700`). Eso es lo que deja la puerta abierta: `fixedCellSize` impediría por completo el alto variable.

## Goals / Non-Goals

**Goals:** que la descripción se lea entera en el Editor, igual que sale en el PDF.

**Non-Goals:** saltos de línea escritos a mano; cambiar el flujo de teclado; tocar las otras cuatro columnas; tocar el alto de fila de Clientes, Histórico o la matriz de IVA; poner un tope al alto de fila.

## Decisions

### D1. Medir antes de tocar el CSS

`base.css:262` fija `.table-view .table-row-cell { -fx-cell-size: 34px; }` para todas las tablas. Queda por confirmar si ese valor recorta el alto o si solo marca un mínimo: `TableRowSkinBase` devuelve el `cellSize` tal cual únicamente cuando es menor que su constante interna de 24 px, y 34 no lo es, de modo que lo esperable es que las filas cortas se queden en 34 px y las largas crezcan por encima.

Se comprueba con una medición real (tarea 1) antes de escribir nada. Si el CSS no recorta, no se toca: la regla se queda como está y ninguna otra tabla se ve afectada. Si recortara, la anulación se acota con el identificador de la tabla, `#tablaLineas .table-row-cell`, y nunca sobre `.table-view`, que alcanzaría a Clientes y al Histórico.

### D2. Una etiqueta con envoltura atada al ancho de la columna

`CeldaDescripcion` deja de apoyarse en el `setText` de `CeldaEditable` y pinta con un `Label` propio como `graphic`:

- `setWrapText(true)`.
- `prefWidth` atado a `colDescripcion.widthProperty()` menos el padding horizontal de la celda, para que el texto no toque los bordes ni provoque desplazamiento horizontal.
- La celda se queda sin texto propio: `setText(null)`.

Se usa `Label` y no `Text` a propósito. Un `Text` se dibuja en negro salvo que se le dé color, y en omarchy, neón y negro-dorado quedaría ilegible sobre el fondo oscuro; habría que añadir una regla de tema. El `Label` toma el color por `-fx-text-fill`, que ya definen `.list-cell` y la tabla en cada tema, así que ningún `tema-*.css` se toca.

### D3. La edición no cambia

**Corregida por D5 el 12/09/2026.** Lo que sigue describe la primera versión, en la que la fila solo crecía al confirmar. Sigue valiendo todo lo que dice sobre ENTER y sobre las celdas que no son la descripción.

`startEdit` sigue poniendo el mismo `TextField` como `graphic`. Se mantiene así por tres motivos:

- ENTER conserva su significado: confirmar y avanzar a la columna siguiente, encadenando cantidad, descripción, precio y total.
- `editarCeldaSegura` (`EditorController.java:875`) localiza el editor con `tablaLineas.lookup(".text-field")`, y `EditorFlujoTecladoTest` hace lo mismo en las líneas 118 y 127. Un `TextArea` cambiaría la clase a `.text-area` y rompería ambos.
- El usuario ha descartado los saltos de línea a mano, que era lo único que justificaba un editor multilínea.

Mientras se escribe, la fila conserva el alto que tenía; crece al confirmar, cuando la etiqueta vuelve a pintar el texto ya guardado.

### D4. Solo la columna de descripción

`CeldaCantidad`, `CeldaPrecio`, `CeldaTotal` y `CeldaIva` no se tocan. Sus valores son cortos y deben seguir en una línea; envolverlos solo produciría cifras partidas por la mitad.

### D5. El editor de la descripción también envuelve (corrige D3)

Con D3 la fila solo crece al confirmar: mientras se teclea sigue habiendo un `TextField`, que por definición desplaza el contenido a la derecha. El usuario ha pedido el 12/09/2026 ver los dos renglones **mientras escribe**, no al final, así que la celda de descripción pasa a editarse con un `TextArea`.

Lo que **no** cambia: ENTER sigue confirmando y avanzando a la columna siguiente, y ESCAPE sigue cancelando. Los dos se interceptan y se consumen igual que ahora, de modo que el `TextArea` nunca llega a insertar un salto de línea. La decisión de no escribir saltos a mano sigue en pie: el `TextArea` está por la envoltura, no por los renglones manuales.

Tres piezas:

1. **`CeldaEditable` deja de fijar el tipo del editor.** Su campo `editor` pasa de `TextField` a `TextInputControl`, creado por un método que las subclases puedan sobrescribir. Todo lo que hace hoy la clase base —`getText`, `setText`, `selectAll`, `requestFocus`, el manejador de teclas y el listener de foco— existe igual en `TextInputControl`, así que `CeldaCantidad`, `CeldaPrecio` y `CeldaTotal` siguen con su `TextField` sin tocar una línea.

2. **`CeldaDescripcion` crea un `TextArea`** con `setWrapText(true)` y `setPrefRowCount(1)`. El valor por defecto de `prefRowCount` es 10: sin bajarlo, el editor saldría como un bloque enorme. El alto tiene que seguir al contenido, y para eso se ata el `prefHeight` al alto real del nodo de texto interno, el que devuelve `lookup(".text")` sobre el `TextArea`. Ese nodo no existe hasta que el control tiene skin, así que la atadura se hace en cuanto aparece, no en el constructor.

3. **El alto de la fila mientras se edita.** `computePrefHeight` de `CeldaDescripcion` hoy mide siempre la etiqueta. Pasa a medir el editor cuando la celda está en edición y la etiqueta cuando no. Queda por comprobar si con eso basta: la `VirtualFlow` de la tabla cachea el alto de las filas, y puede que no vuelva a medir la que se está editando aunque su contenido crezca. **Se mide antes de dar el cambio por bueno** (tarea 4.4). Si no bastara, la salida es pedir explícitamente el recálculo de la fila al crecer el editor; lo que **no** vale es `tablaLineas.refresh()`, que descarta la celda en edición y tira el editor con el texto a medio escribir.

### D5b. El editor tiene que mostrar siempre todo el texto

Medido el 12/09/2026 sobre la implementación de D5, a 1024 px, con el editor abierto y metiendo texto de más en más largo:

```
prefHeightAtado=false   nodoTexto=ok   minHeight=-1.0   prefRowCount=1
chars=5     altoTexto=15   altoEditor=36,96   altoFila=42,96   barra=no
chars=62    altoTexto=15   altoEditor=36,96   altoFila=42,96   barra=no
chars=169   altoTexto=45   altoEditor=36,96   altoFila=42,96   barra=SI
```

Dos defectos, y el segundo es consecuencia del primero.

**La atadura del `prefHeight` nunca se crea.** `prefHeightProperty().isBound()` es `false`. El listener de `skinProperty` corre, pero en ese instante `lookup(".text")` todavía no encuentra el nodo interno, porque el skin aún no ha creado ni maquetado sus hijos. El listener no se vuelve a intentar nunca, así que el editor se queda con su alto calculado a partir de `prefRowCount = 1`, unos 37 px, pase lo que pase con el texto. El nodo sí existe más tarde —la medición lo encuentra sin problema—, solo que ya no hay nadie mirando.

**Por eso sale la barra de desplazamiento.** El texto pasa de 15 a 45 px de alto y el editor sigue en 36,96: el `ScrollPane` interno hace lo único que puede hacer, desplazar. Y la fila tampoco crece mientras se escribe: se queda en 42,96 en los tres casos. El texto solo se ve entero al confirmar, cuando vuelve a pintar la etiqueta, que esa sí envuelve bien.

Lo que se pide es que **el editor muestre siempre todo lo escrito**, creciendo a la vez que se teclea, sin barra de desplazamiento en ningún momento.

Tres correcciones, todas en `CeldaDescripcion`:

1. **Que la atadura se cree de verdad.** No basta con esperar al skin. Hay que forzar que el skin exista y esté maquetado antes de buscar el nodo interno, o no depender de ese nodo: medir el alto que necesita el texto con el mismo tipo de letra y el mismo ancho de envoltura, como se hace ya en el proyecto para saber si un texto cabe. Lo que no vale es un `lookup` a una sola carta: si falla, se queda sin atadura y el fallo es silencioso.
2. **Que el mínimo no estorbe.** `minHeight` está hoy en `USE_COMPUTED_SIZE`, y el mínimo que calcula un `TextArea` arrastra el del `ScrollPane`. Aunque el preferido quede bien atado, el mínimo puede volver a clavar el editor en los 37. El `TextArea` pasa a tomar su alto mínimo del preferido.
3. **Que la fila siga al editor.** Hoy la fila mide 42,96 con el editor abierto y no se mueve por mucho que crezca el texto. `computePrefHeight` tiene que leer el alto preferido real del editor, y **algo tiene que pedir el recálculo de la fila** cada vez que ese alto cambia; si no, la `VirtualFlow` reutiliza el alto que ya tenía cacheado. Sigue prohibido `tablaLineas.refresh()`, que tira el editor con el texto a medio escribir.

Criterios de aceptación, medibles los tres:

- Con una descripción de una línea, la fila mide **34,0 px** con el editor abierto y con él cerrado. Entrar a escribir no da ningún salto.
- Al pasar a dos renglones, el editor y la fila crecen a la vez que se teclea, y el texto se ve entero.
- El `TextArea` **no muestra barra de desplazamiento** con ninguna longitud de texto.

Cuidado al ajustar el alto: el relleno real no está en los insets del `TextArea`, que Modena deja a cero (`modena.css:1313`), sino en el nodo `.content`, 3 px arriba y 3 abajo (`modena.css:1332`), más el borde del `ScrollPane`. Si no se cuenta, el editor queda más corto que su texto y recorta la última línea.

### D6. Los dos sitios que buscan el editor por `.text-field`

`editarCeldaSegura` (`EditorController.java:876`) localiza el editor recién abierto con `tablaLineas.lookup(".text-field")` y lo enfoca. Con un `TextArea` en la descripción, ese `lookup` no lo encuentra y la cadena de ENTER se rompe justo al llegar a esa columna: el foco se iría a la tabla.

Pasa a buscar el editor por las dos clases y a tratarlo como `TextInputControl`, que es lo único que necesita para pedir el foco.

`EditorFlujoTecladoTest` (líneas 118 y 127) hace lo mismo y castea a `TextField`. Se adapta igual: buscar por las dos clases y declarar `TextInputControl`. El test **debe seguir comprobando exactamente lo mismo** —escribir en la descripción, pulsar ENTER y verificar el avance—; lo único que cambia es cómo alcanza el editor.

## Risks / Trade-offs

- **El `ComboBox` de la columna IVA.** `CeldaIva` monta un `ComboBox` permanente con alto fijo de 28 px (`base.css:384`). No impide que la fila crezca, pero en una fila alta quedará arriba, con hueco debajo. Se acepta: es lo mismo que ocurre con la cantidad y el precio.
- **Menos líneas visibles a la vez.** Una descripción de cuatro líneas ocupa el sitio de cuatro filas cortas. La tabla se desplaza internamente y los totales del pie siguen visibles, que es lo que exige el requisito «Editor legible sin scroll en facturas cortas».
- **`EditorTamanoMinimoTest`** exige que la tabla conserve al menos 150 px de alto. Las filas altas reducen cuántas caben dentro, no el alto de la tabla, así que debe seguir en verde. Lo cubre la tarea 3.1.
- **Descuido posible:** atar el `prefWidth` al ancho de la columna sin descontar el padding deja la etiqueta más ancha que el hueco disponible y reaparece el desplazamiento horizontal, esta vez de toda la fila. Lo cubre la comprobación a mano de la tarea 3.2.
- **El `TextArea` y la tecla TAB (D5).** En un `TextField` la tecla TAB saca el foco del campo. En un `TextArea` no está claro que haga lo mismo, y podría acabar metiendo un tabulador dentro de la descripción. El flujo del Editor encadena celdas con ENTER, no con TAB, así que el riesgo es menor, pero se comprueba a mano en la tarea 4.6.
- **El `TextArea` y la barra de desplazamiento (D5).** Si el alto no sigue bien al contenido, el `TextArea` no crece y saca su propia barra vertical dentro de la celda: el mismo problema de antes con otra forma. Lo cubre la tarea 4.4.
