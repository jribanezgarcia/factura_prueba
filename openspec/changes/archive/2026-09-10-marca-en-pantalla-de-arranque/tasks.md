> Las restricciones de la pantalla están en `design.md - D1`: 760x520 fijos y no redimensionable.

## 1. Marca en el arranque

- [x] 1.1 En `src/main/resources/com/alcazaba/facturacion/ui/Arranque.fxml`, añadir el import `<?import javafx.scene.image.*?>` junto a los que ya hay.
- [x] 1.2 Sustituir el `Label` de la línea 14 (`styleClass="empresa-nombre" text="Facturación"`) por un `HBox alignment="CENTER" spacing="12"` que contenga, en este orden: un `ImageView` con `fitHeight="40.0"` y `preserveRatio="true"` cargando `@../images/icono-aplicacion.png`, y un `Label styleClass="empresa-nombre" text="CaboFactu®"`.
- [x] 1.3 Comprobar que el `VBox` contenedor conserva `alignment="CENTER"`, `spacing="24"` y `maxWidth="-Infinity"`, y que no se ha tocado la tarjeta de selección ni la etiqueta de error.
- [x] 1.4 Comprobar que el símbolo ® se ha guardado correctamente y el archivo sigue en UTF-8.

## 2. Verificación final

- [x] 2.1 `mvn test` en verde. `UiSmokeTest` y `VentanaTransicionTest` cargan esta pantalla: un import olvidado o una ruta mal escrita harían fallar la carga del FXML.
- [x] 2.2 Arrancar la aplicación y comprobar que la portada muestra el icono y «CaboFactu®» centrados como conjunto.
- [x] 2.3 Comprobar que todo sigue cabiendo en los 760x520: la tarjeta de selección no se desplaza, y ni la etiqueta de error ni el botón Entrar quedan cortados.
- [x] 2.4 Comprobar que el icono se ve nítido a 40px de alto y conserva su proporción.
- [x] 2.5 Entrar al menú principal y comprobar que el nombre de la empresa sigue mostrándose igual que antes: comparte la style-class `empresa-nombre` con el rótulo que se ha cambiado.
