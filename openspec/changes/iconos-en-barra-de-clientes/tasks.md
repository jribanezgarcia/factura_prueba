> Los trazados exactos y la receta del icono compuesto están en `design.md - D1`, `D2` y `D3`.

## 1. Iconos

- [ ] 1.1 Copiar de `src/main/resources/com/alcazaba/facturacion/ui/Historico.fxml` los trazados de `Eliminar` y `Volver` sin modificar ni un carácter. Deben quedar idénticos en las dos pantallas.
- [ ] 1.2 Contrastar el trazado de `Nuevo` (`person_add`) con la fuente original de Material Icons antes de darlo por bueno: está escrito de memoria y solo validado a ojo. Si difiere, usar el oficial.
- [ ] 1.3 Usar para `Editar` el trazado compuesto de `design.md - D3` tal cual. No reescribirlo a mano: las coordenadas salen de una transformación calculada.
- [ ] 1.4 Comprobar que ninguno de los cuatro coincide con un icono ya asignado a otra acción, como exige `spec.md:1155`.

## 2. Barra de Clientes

- [ ] 2.1 En `src/main/resources/com/alcazaba/facturacion/ui/Clientes.fxml`, separar el `HBox` de la línea 16 en dos: arriba el campo de búsqueda y la etiqueta de conteo; debajo un `HBox spacing="8" alignment="CENTER_RIGHT"` con los botones, siguiendo el patrón del Histórico.
- [ ] 2.2 Retirar el `Region HBox.hgrow="ALWAYS"` de la fila de búsqueda, que ya no hace falta.
- [ ] 2.3 Cambiar el `styleClass` de los cuatro botones de `btn-suave` a `btn-ribbon`, conservando el tipo que ya tienen: `primary-button` en `Nuevo`, `default-button` en `Editar` y `Volver`, `danger-button` en `Eliminar`.
- [ ] 2.4 Añadir a cada botón su `<graphic><SVGPath styleClass="icono-boton" content="..."/></graphic>`, como en `Historico.fxml`.
- [ ] 2.5 Añadir dos `<Separator orientation="VERTICAL" styleClass="ribbon-sep"/>`: uno entre `Editar` y `Eliminar`, otro entre `Eliminar` y `Volver`.
- [ ] 2.6 No tocar ningún `onAction` ni ningún `text`.
- [ ] 2.7 Comprobar que no queda ninguna aparición de `btn-suave` en este archivo.

## 3. Verificación final

- [ ] 3.1 `mvn test` en verde, con `StyleClassSeparadorTest` incluido.
- [ ] 3.2 Abrir Clientes y el Histórico y compararlos: los botones deben verse idénticos en tamaño, tratamiento y comportamiento.
- [ ] 3.3 Comprobar que `Eliminar` y `Volver` muestran exactamente el mismo icono en las dos pantallas.
- [ ] 3.4 Mirar el icono compuesto de `Editar` a tamaño real: la persona debe reconocerse y el lápiz distinguirse sin esfuerzo.
- [ ] 3.5 Comprobar los colores en un tema claro y uno oscuro: `Nuevo` en color de acento con la etiqueta en negrita, `Eliminar` en color de peligro, los otros dos en color de texto normal.
- [ ] 3.6 Con la ventana en 1024x768, comprobar que la tarjeta de búsqueda al pasar a dos filas no deja la tabla de clientes sin altura útil.
- [ ] 3.7 Comprobar que los cuatro botones siguen funcionando: alta, edición, borrado y vuelta al menú.
