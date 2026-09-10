> Los valores exactos del lavado por tema están en la tabla de `design.md - D2`.
> El árbol de trabajo ya tiene aplicada una versión anterior de esta idea, con los botones transparentes. Hay que ajustarla, no añadir encima. Ver `design.md - Riesgos`.

## 1. Variables de lavado en los temas

- [x] 1.1 En cada uno de los 7 `src/main/resources/com/alcazaba/facturacion/themes/tema-*.css`, añadir al bloque `.root` las variables `-fx-boton-lavado` y `-fx-boton-lavado-fuerte` con los valores de la tabla de `design.md - D2`, junto a `-fx-faint-focus-color`.
- [x] 1.2 Comprobar que los 7 quedan con las dos variables y que ningún valor se ha copiado del tema vecino.

## 2. Variante `btn-suave` en `base.css`

- [x] 2.1 Retirar de `src/main/resources/com/alcazaba/facturacion/themes/base.css` el bloque `btn-plano` que aplicó la versión anterior.
- [x] 2.2 Añadir tras el bloque de `btn-ribbon` los cinco selectores `.primary-button.btn-suave`, `.default-button.btn-suave`, `.danger-button.btn-suave`, `.action-button.btn-suave` y `.action-danger-button.btn-suave` con `-fx-background-color: -fx-boton-lavado`, `-fx-background-insets: 0`, `-fx-border-color: transparent` y `-fx-font-weight: bold`.
- [x] 2.3 Añadir `.primary-button.btn-suave` con `-fx-background-color: -fx-boton-lavado-fuerte` y `-fx-text-fill: -fx-accent`, después del bloque anterior para que lo pise.
- [x] 2.4 `:hover` de las cinco: dos capas, `-fx-background-color: -fx-boton-lavado, rgba(128, 128, 128, 0.12)`. Para `.primary-button.btn-suave:hover`, la capa de abajo es `-fx-boton-lavado-fuerte`.
- [x] 2.5 `:pressed` igual que `:hover` subiendo la capa gris a `rgba(128, 128, 128, 0.20)`.
- [x] 2.6 `:focused` de las cinco: `-fx-border-color: -fx-accent`.
- [x] 2.7 No declarar en `btn-suave` ni `-fx-content-display`, ni anchos, ni alto mínimo, ni `-fx-font-size`, ni `-fx-wrap-text`: eso es maquetación de `btn-ribbon` y aquí rompería el botón.

## 3. Aplicar `btn-suave` en los FXML

En `src/main/resources/com/alcazaba/facturacion/ui/`. Donde la versión anterior dejó `btn-plano`, se sustituye por `btn-suave`, separado por coma y espacio: `styleClass="primary-button, btn-suave"`. No se cambia ningún `fx:id`, `text`, `onAction` ni la maquetación.

- [x] 3.1 `Clientes.fxml`: los 4 botones.
- [x] 3.2 `Configuracion.fxml`: los 16 botones.
- [x] 3.3 `Backup.fxml`: los 5 botones.
- [x] 3.4 `Editor.fxml`: solo `btnAnadirLinea` y `btnEliminarLinea`. Los que ya llevan `btn-ribbon` no se tocan.
- [x] 3.5 `Versiones.fxml`: el botón Volver.
- [x] 3.6 `GenerarFacturasMensuales.fxml`: Cancelar y Generar, que ya tienen clase; y además `btnAnadirLinea` y `btnEliminarLinea` (líneas 72 y 73), que hoy no tienen ninguna: darles `action-button, btn-suave`.
- [x] 3.7 Comprobar que `Arranque.fxml` sigue sin tocar y que no queda ninguna aparición de `btn-plano` en el proyecto.

## 4. Verificación final

- [x] 4.1 `mvn test` en verde, con `StyleClassSeparadorTest` incluido: si algún `styleClass` se ha escrito con espacio en vez de coma, ese test falla.
- [x] 4.2 Arrancar con biblioteca8 y recorrer Clientes, Configuración, Copia de seguridad, Versiones, el Editor y Generar mensuales: todos los botones sobre el mismo sombreado, sin borde, en negrita.
- [x] 4.3 Comprobar en cada pantalla que el principal (Nuevo, Guardar, Crear copia, Restaurar, Generar) se distingue por el sombreado fuerte y el texto de acento.
- [x] 4.4 Comprobar los estados: el sombreado se oscurece al pasar el ratón, más al pulsar, y sale borde de acento al tabular.
- [x] 4.5 Repetir el recorrido en negro dorado y en sakura, y confirmar que el lavado sigue leyéndose como gris y no como un color del tema.
- [x] 4.6 En Generar mensuales, comprobar que los cuatro botones se ven iguales entre sí.
- [x] 4.7 Comprobar que en Configuración los botones no quedan apelmazados. Si lo están, subir el `-fx-spacing` del HBox contenedor, nunca el padding del botón.
