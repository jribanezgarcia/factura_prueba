> Los trazados exactos y la receta del icono compuesto están en `design.md - D1`, `D2` y `D3`.

## 1. Iconos

- [x] 1.1 Copiar de `src/main/resources/com/alcazaba/facturacion/ui/Historico.fxml` los trazados de `Eliminar` y `Volver` sin modificar ni un carácter. Deben quedar idénticos en las dos pantallas.
- [x] 1.2 Contrastar el trazado de `Nuevo` (`person_add`) con la fuente original de Material Icons antes de darlo por bueno: está escrito de memoria y solo validado a ojo. Si difiere, usar el oficial.
- [x] 1.3 Usar para `Editar` el trazado compuesto de `design.md - D3` tal cual. No reescribirlo a mano: las coordenadas salen de una transformación calculada.
- [x] 1.4 Comprobar que ninguno de los cuatro coincide con un icono ya asignado a otra acción, como exige el requisito «Botones de acción con icono identificativo».

## 2. Barra de Clientes

- [x] 2.1 En `Clientes.fxml`, volver a juntar en un solo `HBox` con `alignment="CENTER_LEFT"` el campo de búsqueda, la etiqueta de conteo, un `Region HBox.hgrow="ALWAYS"` y los botones con sus separadores, en ese orden. Hoy están en dos filas: hay que deshacer esa separación.
- [x] 2.2 Mantener el `Region HBox.hgrow="ALWAYS"` entre el conteo y los botones: es el que los empuja a la derecha dentro de la fila única.
- [x] 2.3 Los cuatro botones con `btn-ribbon`: `primary-button` en `Nuevo`, `default-button` en `Editar`, `Eliminar` y `Volver`. `Eliminar` deja de ser `danger-button` para ir en el color del tema, como el `Eliminar` del Histórico. Ver `design.md - D7`.
- [x] 2.4 Añadir a cada botón su icono envuelto en una caja fija: `<graphic><StackPane styleClass="caja-icono"><SVGPath styleClass="icono-boton" content="..."/></StackPane></graphic>`. A diferencia del Histórico, el `SVGPath` no va suelto: la caja es la que alinea las etiquetas. Ver `design.md - D6`.
- [x] 2.5 Añadir dos `<Separator orientation="VERTICAL" styleClass="ribbon-sep"/>`: uno entre `Editar` y `Eliminar`, otro entre `Eliminar` y `Volver`.
- [x] 2.6 No tocar ningún `onAction` ni ningún `text`.
- [x] 2.7 Comprobar que no queda ninguna aparición de `btn-suave` en este archivo.
- [x] 2.8 En `src/main/resources/com/alcazaba/facturacion/themes/base.css`, justo después del bloque `.btn-ribbon .icono-boton`, crear `.caja-icono` con `-fx-min-width`, `-fx-pref-width`, `-fx-max-width`, `-fx-min-height`, `-fx-pref-height` y `-fx-max-height` a `22`, y `-fx-alignment: center`. No tocar la escala de `.btn-ribbon .icono-boton`.
- [x] 2.9 Añadir a `Clientes.fxml` el import `<?import javafx.scene.shape.*?>`, junto a los que ya tiene, con el mismo estilo de comodín. `StackPane` ya está cubierto por el `javafx.scene.layout.*` existente; lo que falta es `SVGPath`.
- [x] 2.10 En `src/main/resources/com/alcazaba/facturacion/ui/Editor.fxml`, cambiar el `styleClass` de `btnAnular` de `action-danger-button, btn-ribbon` a `action-button, btn-ribbon`. No tocar su `fx:id`, su `onAction`, su `text` ni su icono.
- [x] 2.11 **No** retirar de `base.css` ni de los temas las reglas de `danger-button` y `action-danger-button`, aunque queden sin uso. Es una limpieza aparte, ver `design.md - D7`.

## 3. Verificación final

- [x] 3.1 `mvn test` en verde, con `StyleClassSeparadorTest` incluido.
- [x] 3.2 Abrir Clientes y el Histórico y compararlos: los botones deben verse idénticos en tamaño, tratamiento y comportamiento. La alineación de etiquetas solo se exige en Clientes: el Histórico la recibirá cuando la norma se extienda a su barra.
- [x] 3.3 Comprobar que `Eliminar` y `Volver` muestran exactamente el mismo icono en las dos pantallas.
- [x] 3.4 Mirar el icono compuesto de `Editar` a tamaño real: la persona debe reconocerse y el lápiz distinguirse sin esfuerzo.
- [x] 3.5 Comprobar los colores en un tema claro y uno oscuro: los cuatro botones con icono y etiqueta en color de acento y sin negrita, `Eliminar` incluido. Ninguno debe destacarse sobre los demás.
- [x] 3.6 Con la ventana en 1024x768, comprobar que el campo de búsqueda queda centrado en vertical en la fila, sin pegarse arriba, y que la tabla de clientes conserva altura útil.
- [x] 3.7 Comprobar que los cuatro botones siguen funcionando: alta, edición, borrado y vuelta al menú.
- [x] 3.8 Comprobar que las etiquetas de los cuatro botones de Clientes arrancan exactamente a la misma altura. Si la de Editar queda más baja que las demás, la caja no se está aplicando.
- [x] 3.9 Guardar una factura en el Editor y comprobar que `Anular` aparece en el mismo color que el resto de la barra, a plena intensidad, y que no se confunde con un botón deshabilitado.
- [x] 3.10 Comprobar que no queda ningún botón rojo en ninguna barra: Editor, Histórico y Clientes.
