## 1. Corrección de styleClass en los FXML

- [x] 1.1 Cambiar `styleClass="card zona-contenido"` por `styleClass="card, zona-contenido"` en Backup.fxml (líneas 15, 26, 32) y verificar con grep que no queda ningún espacio sin coma
- [x] 1.2 Cambiar el mismo separador en Clientes.fxml (línea 15), Historico.fxml (línea 15) y Versiones.fxml (línea 15) y verificar con grep
- [x] 1.3 Cambiar el mismo separador en Configuracion.fxml (líneas 24, 48, 68, 74, 110, 141, 183, 203, 218) y verificar con grep
- [x] 1.4 Ejecutar `grep -r "styleClass=\"[^\"]* \"` sobre `src/main/resources/com/alcazaba/facturacion/ui/*.fxml` y verificar que no quedan styleClass con espacios sin coma (0 coincidencias)

## 2. Test antirregresión

- [x] 2.1 Crear el test `StyleClassSeparadorTest` que recorra los FXML de `src/main/resources/com/alcazaba/facturacion/ui/`, lea cada atributo `styleClass` y falle si contiene un espacio no precedido de coma, y verificar que detecta el caso `card zona-contenido` (falla contra un FXML con el bug y pasa con el corregido)

## 3. Verificación visual y de regresión

- [x] 3.1 Ejecutar la suite completa `mvn test` y verificar que todos los tests pasan (incluidos los de tamaño de ventana y los de pantallas)
- [x] 3.2 Arrancar la app y revisar Backup, Clientes, Configuración, Histórico y Versiones a 1024×768, verificando que los paneles muestran fondo de tarjeta, borde, esquinas redondeadas y margen respecto al borde, sin scroll nuevo ni recortes
- [ ] 3.3 ~~Si en la revisión la separación resulta excesiva por duplicación con los espaciados del parche fix-ui-spacing, ajustar el padding redundante del FXML correspondiente y verificar que la pantalla sigue correcta a 1024×768~~ (no necesario: el espaciado es correcto)