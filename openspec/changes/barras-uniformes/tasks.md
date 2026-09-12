> Las razones están en `design.md`. Ningún `.java`, ningún tema, ningún `Editor.fxml`.

## 1. Botones a la derecha

- [x] 1.1 En `src/main/resources/com/alcazaba/facturacion/ui/Clientes.fxml`, añadir `<Region HBox.hgrow="ALWAYS"/>` como primer hijo de la `HBox styleClass="action-bar"`, delante del botón Nuevo. Dejar `alignment="CENTER_LEFT"` como está. Ver `design.md - D1`.
- [x] 1.2 En `src/main/resources/com/alcazaba/facturacion/ui/Historico.fxml`, hacer lo mismo delante del botón Buscar.
- [x] 1.3 No cambiar el orden de los botones ni de los `Separator`.

## 2. Encaje de la franja en la tarjeta

- [x] 2.1 En los dos FXML, cambiar el `VBox spacing="12"` del `top` a `spacing="8"`, como en el Editor. Ver `design.md - D2`.
- [x] 2.2 En los dos FXML, cambiar la style-class `zona-contenido` de la tarjeta por `card-editor`, dejando `card` y `panel-busqueda` intactas.
- [x] 2.3 Comprobar que `zona-contenido` sigue usándose en otras pantallas y no queda huérfana; si lo quedara, no borrarla: está fuera del alcance de este change.

## 3. Alto común de la franja

- [x] 3.1 En `src/main/resources/com/alcazaba/facturacion/themes/base.css`, añadir a `.action-bar` `-fx-min-height: 69px` y `-fx-pref-height: 69px`. No añadir `-fx-max-height`. Ver `design.md - D3`.
- [x] 3.2 Comprobar en la aplicación que con 69px ningún botón queda recortado por arriba o por abajo y que el logo y el título del Editor siguen cabiendo. Si hiciera falta ajustar el valor, ajustarlo en los dos sitios a la vez y anotarlo aquí.

## 4. Verificación

- [x] 4.1 Comprobar que los iconos de las tres barras siguen dentro de `caja-icono` y con la escala 1.05 de `.btn-ribbon .icono-boton`, sin excepciones por pantalla. Ver `design.md - D4`.
- [x] 4.2 `mvn test` en verde. Prestar atención a los tests de la barra de acciones del Editor y a los de layout: la tarjeta de Clientes y la del Histórico cambian de alto y de relleno.
- [ ] 4.3 A 1024×768 con el tema por defecto, pasar del Editor a Clientes y al Histórico y comprobar que el último botón de la barra no se desplaza entre pantallas y que la franja arranca y termina a la misma altura.
- [ ] 4.4 Repetir maximizado y con un tema oscuro.
- [ ] 4.5 Comprobar que el Editor sigue cabiendo sin scroll a 1024×768 en una factura corta.
