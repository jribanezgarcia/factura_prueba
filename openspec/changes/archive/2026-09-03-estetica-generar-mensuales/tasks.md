## 1. Ajustes de estilo en el FXML

- [x] 1.1 Cambiar el `Label` del título a `styleClass="titulo"` (en lugar de `titulo-dialogo`) y el subtítulo «Líneas de cada factura» a `styleClass="section-title"` (en lugar de `subtitulo-dialogo`) en `GenerarFacturasMensuales.fxml`; verificar que el FXML sigue válido y que la suite sigue en verde (`mvn -o test`).
- [x] 1.2 Añadir `styleClass="form-label"` a cada `Label` del `GridPane` (Cliente, Serie, Año, Mes inicio, Mes fin, Día del mes, Tipo de IVA, Retención IRPF) en `GenerarFacturasMensuales.fxml`; verificar que el FXML sigue válido y que la suite sigue en verde (`mvn -o test`).
- [x] 1.3 Sustituir `styleClass="card"` por `styleClass="panel-neutro"` en el `ScrollPane`, y subir el espaciado del `GridPane` (`hgap`/`vgap`, p. ej. `hgap="12" vgap="10"`) añadiendo margen interior para que las etiquetas no queden pegadas al borde en `GenerarFacturasMensuales.fxml`; verificar que el FXML sigue válido y que la suite sigue en verde (`mvn -o test`).

## 2. Clase de panel en base.css

- [x] 2.1 Añadir en `base.css` la clase `.panel-neutro` con `-fx-background-color: derive(-fx-base, -3%)`, radio y borde suaves (sin sombra fuerte) y el padding interior necesario; verificar que la suite sigue en verde (`mvn -o test`).

## 3. Verificación

- [x] 3.1 Lanzar la suite completa `mvn -o test` y confirmar que los 156 tests pasan.
- [x] 3.2 Comprobación visual con la app: abrir «Generar facturas mensuales» en al menos dos temas (p. ej. Biblioteca8 y Neon) y confirmar que el título se distingue (negrita + color del tema), las etiquetas tienen separación clara y el panel ya no es un bloque blanco plano.
- [x] 3.3 `git diff` y confirmar que solo se tocan `GenerarFacturasMensuales.fxml` y `base.css` (más specs/archivo), sin alterar otros temas ni pantallas.
