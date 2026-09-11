> Las decisiones y medidas están en `design.md`.

## 1. Barras del Editor y del Histórico

- [x] 1.1 En `src/main/resources/com/alcazaba/facturacion/ui/Editor.fxml`, envolver cada uno de los ocho `SVGPath` de la barra de acciones en `<StackPane styleClass="caja-icono">`, dentro del `<graphic>`. Ver `design.md - D1`.
- [x] 1.2 En `src/main/resources/com/alcazaba/facturacion/ui/Historico.fxml`, lo mismo con los seis `SVGPath` de la barra.
- [x] 1.3 No tocar ningún `content`, `styleClass` de botón, `fx:id`, `onAction` ni `text`. El `content` de cada trazado debe quedar idéntico carácter a carácter.
- [x] 1.4 No crear ninguna caja nueva para estas barras: reutilizar `.caja-icono` tal como está en `base.css`.

## 2. Menú principal

- [x] 2.1 En `src/main/resources/com/alcazaba/facturacion/themes/base.css`, justo después de `.menu-item .icono`, crear `.caja-icono-menu` con `-fx-min-width`, `-fx-pref-width`, `-fx-max-width`, `-fx-min-height`, `-fx-pref-height` y `-fx-max-height` a `28`, y `-fx-alignment: center`. No tocar la escala de `.menu-item .icono`. Ver `design.md - D3`.
- [x] 2.2 En `src/main/resources/com/alcazaba/facturacion/ui/MenuPrincipal.fxml`, envolver cada uno de los siete `SVGPath` en `<StackPane styleClass="caja-icono-menu">`, como primer hijo del `HBox` de cada opción, en el lugar que ocupaba el icono.
- [x] 2.3 No tocar el `spacing` de los `HBox`, los textos ni los anchos de las tarjetas.

## 3. Verificación final

- [x] 3.1 `mvn test` en verde.
- [x] 3.2 Abrir el Editor y comprobar que las ocho etiquetas de la barra arrancan exactamente a la misma altura.
- [x] 3.3 Abrir el Histórico y comprobar lo mismo con sus seis etiquetas.
- [x] 3.4 Comprobar que las barras del Editor y del Histórico no han crecido de alto respecto a antes, y que las del Editor, Histórico y Clientes se ven iguales entre sí.
- [x] 3.5 Abrir el menú principal y comprobar que los siete nombres y las siete descripciones arrancan en la misma posición horizontal, que ningún icono toca su texto y que ninguna descripción aparece cortada.
- [x] 3.6 Comprobar que ningún icono aparece recortado ni deformado en ninguna de las tres pantallas.
- [x] 3.7 Comprobar que la barra de navegación no ha cambiado.
