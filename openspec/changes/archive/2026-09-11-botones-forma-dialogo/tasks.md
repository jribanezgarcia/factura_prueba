> Las razones y los detalles están en `design.md`.

## 1. Forma

- [x] 1.1 En `src/main/resources/com/alcazaba/facturacion/themes/base.css`, en la regla `.primary-button, .default-button, .danger-button, .action-button, .action-danger-button` (línea 125): `-fx-background-radius` y `-fx-border-radius` de `8px` a `3px`, `-fx-padding` de `8px 10px` a `4px 8px`, y añadir `-fx-min-width: 80px`. Ver `design.md - D1`.
- [x] 1.2 No tocar `.btn-ribbon`, `.btn-suave` ni ningún `tema-*.css`.

## 2. Ancho común por grupo

- [x] 2.1 Crear `src/main/java/com/alcazaba/facturacion/ui/Botones.java` con `static void igualarGrupos(Parent raiz)` según `design.md - D2`: grupos de dos o más `Button` hijos directos del mismo contenedor, sin `btn-ribbon`, `nav-button`, `menu-item` ni botones dentro de un `DialogPane`; `prefWidth` de todos al máximo del grupo; se vuelve a igualar si cambia el texto de un botón.
- [x] 2.2 En `src/main/java/com/alcazaba/facturacion/ui/Navegador.java`, en `mostrar`, justo antes de `root.lookupAll(".primary-button, .menu-item")` (línea 69): `root.applyCss();` y `Botones.igualarGrupos(root);`.
- [x] 2.3 Crear `src/test/java/com/alcazaba/facturacion/ui/BotonesTest.java`, con el mismo arranque que `ConfiguracionLayoutTest`: abrir Configuración y comprobar que Nuevo, Guardar e Inactivar/Activar de la sección de IVA miden lo mismo, y que ningún botón de solo texto mide menos de 80.

- [x] 2.4 Corrección tras la revisión: en `Botones.igualar`, antes de medir, devolver cada botón del grupo a su ancho calculado con `b.setPrefWidth(Region.USE_COMPUTED_SIZE)`. Sin eso, al volver a igualar por un cambio de texto, `prefWidth(-1)` devuelve el ancho fijado la primera vez y el botón no crece. Ver `design.md - D2`.

- [x] 2.5 Segunda corrección tras la revisión visual, en `src/main/java/com/alcazaba/facturacion/ui/GenerarFacturasMensualesController.java`: el botón `btnGenerar` se queda siempre con el texto «Generar» del FXML. Renombrar `actualizarInfoBoton()` a `actualizarInfo()` (y sus cinco llamadas, en las líneas 148, 309, 332, 342 y 355) y hacer que, en lugar de cambiar el texto del botón, escriba en `lblInfo` «Se generará 1 factura» o «Se generarán N facturas», y «No se generará ninguna factura» cuando el mes fin sea anterior al de inicio. Ver `design.md - D4`.
- [x] 2.6 En `alIniciar()`, añadir un listener sobre `valueProperty()` de `comboMesInicio` y de `comboMesFin` que llame a `actualizarInfo()`. Hoy no existen, y por eso el número se queda siempre en 12.

## 3. Test de Configuración

- [x] 3.1 En `src/test/java/com/alcazaba/facturacion/ui/ConfiguracionLayoutTest.java`, en el bucle de la línea 104, descartar también los nodos con algún antecesor no visible. Ver `design.md - D3`. No cambiar los límites de 1024 ni de 768, ni ninguna otra aserción.

## 4. Verificación final

- [x] 4.1 `mvn test` en verde, `ConfiguracionLayoutTest` y `BotonesTest` incluidos.
- [x] 4.2 Abrir Configuración, Copia de seguridad, Versiones y la generación de facturas mensuales con un tema claro y uno oscuro, y comprobar que las esquinas de 3 px se ven bien sobre el sombreado.
- [x] 4.3 En cada sección de Configuración, comprobar que los botones de una misma fila miden lo mismo y que ningún texto se corta.
- [x] 4.4 Comprobar que los botones con icono, la barra de navegación, el menú principal, la pantalla de arranque y un diálogo de confirmación siguen igual que antes.
- [x] 4.6 Abrir la generación de facturas mensuales y comprobar que Cancelar y Generar miden lo mismo y que el botón dice siempre «Generar». Cambiar el mes de inicio y el de fin y comprobar que la etiqueta de la izquierda muestra al momento el número correcto de facturas.
- [x] 4.5 Comprobar que en la sección Series de Configuración, a 1024x768, se ve todo sin barra de desplazamiento ni controles cortados.
