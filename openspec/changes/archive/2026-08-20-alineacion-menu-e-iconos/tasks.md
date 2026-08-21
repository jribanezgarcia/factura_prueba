## 1. Alineación del menú principal

- [x] 1.1 Alinear las columnas del menú principal por el borde superior (`HBox` `TOP_LEFT`) y ajustar la caja del logo a la proporción exacta del logo real (260×120, igual a la imagen renderizada con `fitWidth=260` y `preserveRatio`) para que el borde superior del logo coincida con el borde superior del botón «Nueva factura»; verificar ejecutando la aplicación que ambos quedan alineados sin hueco superior. Corrección posterior: el bloque completo debe quedar centrado en la ventana (envoltura en `StackPane` con el `HBox` limitado a su tamaño preferido), no pegado al margen superior izquierdo.
- [x] 1.2 Verificar que el menú principal carga con el tema actual sin errores de CSS; `base.css` no requiere cambios porque la caja no impone tamaños fijos.

## 2. Iconos de Histórico, Nueva factura y Copia de seguridad

- [x] 2.1 Sustituir el icono de Histórico por el de lista/expediente de documentos en `MenuPrincipal.fxml` y `BarraNavegacion.java`; verificar que aparece el nuevo glifo en el menú principal y en la barra de navegación.
- [x] 2.2 Sustituir el icono de Nueva factura de la barra de navegación (era un «+») por el mismo lápiz del menú principal; verificar que el glifo coincide con el del menú.
- [x] 2.3 Sustituir el icono de Copia de seguridad por el de disquete con flecha hacia arriba (material `save_alt`) en `MenuPrincipal.fxml` y `BarraNavegacion.java`; verificar que aparece el nuevo glifo en el menú principal y en la barra de navegación.

## 3. Verificación final

- [x] 3.1 Ejecutar la suite completa (`mvn.cmd test`) y verificar que compila y que todos los tests pasan (31 tests, 0 fallos).