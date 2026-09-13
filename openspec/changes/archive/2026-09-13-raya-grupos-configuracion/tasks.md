> Las razones están en `design.md`. Solo cambian los dos rótulos de grupo de la lista lateral.

## 1. El controlador

- [x] 1.1 En `src/main/java/com/alcazaba/facturacion/ui/ConfiguracionController.java`, dentro de la fábrica de celdas de `configurarSecciones()`, cambiar la rama `item.grupo`: pintar el texto con `setText(item.texto)`, dejar `setGraphic(null)` y añadir la clase `grupo-secciones` a la celda en lugar de a un `Label`. Ver `design.md - D1`.
- [x] 1.2 Dejar intacto el `getStyleClass().remove("grupo-secciones")` que ya hay al principio de `updateItem`: es lo que limpia la clase al reciclarse la celda.
- [x] 1.3 No tocar la rama de las secciones navegables, ni el `setDisable(true)` de los grupos, ni el listener de selección, ni el `select(1)` del final.

## 2. El estilo

- [x] 2.1 En `src/main/resources/com/alcazaba/facturacion/themes/base.css`, cambiar el selector `.lista-secciones .list-cell .grupo-secciones` por `.lista-secciones .list-cell.grupo-secciones`. Ver `design.md - D2`.
- [x] 2.2 Añadir a esa regla la raya inferior con borde asimétrico, tomando el color de `-fx-text-background-color`, y subir el padding inferior de 2 a 4 px. Conservar el tamaño de 12 px y la opacidad de 0,70.
- [x] 2.3 No tocar `.lista-secciones`, `.lista-secciones .list-cell` ni sus estados de hover y selección.
- [x] 2.4 No tocar ningún `tema-*.css`.
- [x] 2.5 No añadir ningún título a los paneles de `pilaSecciones`. Ver `design.md - D3`.

## 3. Verificación

- [x] 3.1 Abrir Configuración y comprobar que hay una raya bajo CONFIGURACIÓN GENERAL y bajo CATÁLOGOS, que llega a todo el ancho de la lista y no solo hasta donde acaba el texto.
- [x] 3.2 Recorrer la lista arriba y abajo y pasar por las siete secciones, comprobando que ninguna entrada navegable sale con raya y que ningún rótulo aparece duplicado.
- [x] 3.3 Comprobar que los dos rótulos siguen sin poder seleccionarse y que el hover y el fondo de la entrada seleccionada se ven igual que antes.
- [x] 3.4 Con la ventana en 1024×768, recorrer las siete secciones y comprobar que cada una sigue cabiendo entera sin desplazarse.
- [x] 3.5 Repetir 3.1 con un tema claro y con uno oscuro, comprobando que la raya se ve en ambos sin resultar dura.
- [x] 3.6 `mvn test` en verde.
