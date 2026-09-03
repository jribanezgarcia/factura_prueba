## 1. Icono por tipo en los diálogos

- [x] 1.1 Añadir en `Dialogos.java` un helper privado que devuelva un `SVGPath` con `styleClass "dialog-icon"` según el tipo: glifo de información para `INFORMATION`, glifo de alerta para `ERROR` y glifo de pregunta para `CONFIRMATION`; verificar que compila y que la suite sigue en verde (`mvn -o test`, 156/156).
- [x] 1.2 Asignar `a.setGraphic(icono(tipo))` en cada constructor de `Alert` de `Dialogos` (`error`, `info`, `confirmar`, `confirmarCambiosSinGuardar`, `modoGuardarVersion`) y verificar que la suite sigue en verde (`mvn -o test`, 156/156).
- [x] 1.3 En cada diálogo, aplicar `Ventanas.aplicarIcono(Stage)` a la ventana del `Alert` (obtenida tras mostrarlo, p. ej. vía `getDialogPane().getScene().getWindow()` con `instanceof Stage`, llamando a `Ventanas.aplicarIcono` después de `showAndWait()` o en el momento en que la escena ya existe); verificar que la barra de título/barra de tareas del diálogo muestra el icono de la app y que la suite sigue en verde (`mvn -o test`, 156/156).

## 2. Color del icono por tema

- [x] 2.1 Añadir en `base.css` la regla `.dialog-icon { -fx-fill: -fx-accent; }` de modo que el icono herede el acento del tema activo sin tocar ningún `tema-*.css`; verificar que la suite sigue en verde (`mvn -o test`, 156/156).

## 3. Verificación

- [x] 3.1 Lanzar la suite completa `mvn -o test` y confirmar que los 156 tests pasan (la verificación de los tests no cambia porque los diálogos de test usan `setImpl`).
- [x] 3.2 Comprobación visual con la app: abrir un diálogo de información, uno de error y uno de confirmación en al menos dos temas distintos (p. ej. Biblioteca8 y Neon) y confirmar que cada uno muestra el icono correspondiente coloreado con el acento del tema y que la barra de título/barra de tareas de la ventana del diálogo muestra el icono de la marca.
- [x] 3.3 `git diff` y confirmar que solo se tocan `Dialogos.java` y `base.css` (más specs/archivo), sin alterar ningún otro tema ni pantalla.
