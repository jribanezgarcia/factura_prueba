## Why

Tres defectos visuales en la app, todos de layout puro (FXML+CSS; los controladores no manipulan layout):

1. En el Editor, el botón `Nueva` está casi al final de la barra de acciones (`Editor.fxml:30`), tras `Anular`/`Restaurar` y lejos de `Guardar`. Las dos acciones de "escritura" deben ir juntas.
2. En el Editor a 1024 px, las etiquetas largas («Forma de pago», «Vencimiento») se comprimen y se recortan con puntos suspensivos: los dos `GridPane styleClass="grid-editor"` (`Editor.fxml:36-57, 62-85`) dejan las columnas de etiqueta (0 y 2) con `<ColumnConstraints/>` vacío —sin `minWidth` ni `prefWidth`—, mientras las columnas de campo llevan `hgrow="ALWAYS"`.
3. Al maximizar, los campos del Editor se estiran desmesuradamente (`maxWidth="Infinity"` + `hgrow="ALWAYS"`) y los filtros del Histórico —un `FlowPane` (`Historico.fxml:16-45`)— se recolocan en una sola fila: la distribución cambia según el tamaño de ventana.

## What Changes

- En la barra de acciones del Editor, `Nueva` pasa a ir inmediatamente después de `Guardar`.
- Las columnas de etiqueta de los dos grids del Editor reciben anchura fija que impide recortarlas.
- Los campos del Editor y los filtros del Histórico ocupan siempre el mismo ancho: la distribución es idéntica a 1024 y maximizado, el sobrante queda vacío a la derecha y solo crecen las tablas (`tablaLineas` en Editor, `tabla` en Histórico).
- No cambia ningún comportamiento, ningún texto, ningún controlador ni ningún servicio.

## Capabilities

### New Capabilities

Ninguna.

### Modified Capabilities

- `invoicing`: se amplía «Barra de acciones del editor sin desbordamiento» con el orden Guardar → Nueva, y se añade el requisito «Distribución estable al redimensionar en Editor e Histórico».

## Impact

- `ui/Editor.fxml`: orden de un botón, `ColumnConstraints` de etiquetas y campos, tope de los dos bloques de cabecera más `Region` de absorción.
- `ui/Historico.fxml`: filtros de `FlowPane` a `GridPane` fijo de 2 filas.
- `themes/base.css`: una regla nueva `.grid-filtros`; ninguna regla existente se toca.
- `ui/EditorBarraAccionesTest`: revisar si afirma el orden de la barra y ajustarlo al orden nuevo.
- No se tocan controladores, lógica, servicios ni persistencia.
