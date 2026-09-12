> Las razones están en `design.md`. Solo FXML: ningún `.java`, ningún tema.

## 1. Clientes

- [x] 1.1 En `src/main/resources/com/alcazaba/facturacion/ui/Clientes.fxml`, partir la `HBox` única de la tarjeta en dos: primero una `HBox styleClass="action-bar" alignment="CENTER_LEFT"` con los cuatro botones y los dos `Separator`, en el mismo orden; después una `HBox spacing="8" alignment="CENTER_LEFT"` con `txtBusqueda` y `lblConteo`. Ver `design.md - D1`, `D2` y `D3`.
- [x] 1.2 Quitar el `Region HBox.hgrow="ALWAYS"` y el `spacing="8"` de la fila de botones: el espaciado lo pone `action-bar`. Ver `design.md - D1` y `D2`.
- [x] 1.3 No cambiar ningún `fx:id`, ningún `onAction`, ningún `styleClass` de botón, ningún trazado ni ningún texto.

## 2. Histórico

- [x] 2.1 En `src/main/resources/com/alcazaba/facturacion/ui/Historico.fxml`, mover la `HBox` de botones para que vaya **antes** del `GridPane styleClass="grid-filtros"` dentro de la tarjeta.
- [x] 2.2 En esa `HBox`, cambiar `alignment="CENTER_RIGHT"` por `alignment="CENTER_LEFT"`, quitar `spacing="8"` y añadir `action-bar` a su `styleClass`. Ver `design.md - D1` y `D2`.
- [x] 2.3 Dejar el `GridPane` de filtros exactamente como está: mismas filas, mismas columnas, mismo `maxWidth="-Infinity"`. Ver `design.md - D4`.
- [x] 2.4 No cambiar ningún `fx:id`, ningún `onAction`, ningún `styleClass` de botón, ningún trazado ni ningún texto.

## 3. Ajuste de ancho

- [x] 3.1 Comprobar en la aplicación que la franja llega de borde a borde de la tarjeta en las dos pantallas. Si queda ceñida al contenido, añadir `maxWidth="Infinity"` a la `HBox` en el FXML, sin tocar el CSS. Ver `design.md - D5`.

## 4. Verificación

- [x] 4.1 `mvn test` en verde. Prestar atención a `ConfiguracionLayoutTest` y a los tests de la barra de acciones: la tarjeta de Clientes y la del Histórico cambian de alto.
- [x] 4.2 Abrir el Editor, Clientes y el Histórico con el tema por defecto a 1024×768 y comprobar que en las tres la fila de iconos se ve sobre la misma franja gris, con los campos debajo.
- [x] 4.3 Repetir con un tema oscuro y comprobar que la franja también se distingue de la tarjeta.
- [x] 4.4 Comprobar que en el Histórico los filtros siguen en sus dos filas y en las mismas posiciones, tanto a 1024×768 como maximizado.
