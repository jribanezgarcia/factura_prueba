## 1. Sistema de temas

- [x] 1.1 Crear `ThemeManager` con el catálogo de temas (biblioteca8, omarchy, esmeralda, terracota, negro-dorado, sakura, neon), aplicar `base.css` + tema a la `Scene` y persistir el activo en la preferencia `tema`; verificar que la suite de tests sigue en verde y que el tema por defecto es biblioteca8.
- [x] 1.2 Crear `base.css` y los siete `tema-*.css` bajo `themes/` con las clases de estructura e iconos; verificar que las vistas cargan sin errores de CSS.
- [x] 1.3 Aplicar el tema al crear cada escena en `Navegador.mostrar(...)`; verificar que al navegar entre vistas el tema se mantiene.

## 2. Barra de navegación superior

- [x] 2.1 Crear `BarraNavegacion` con botones de icono SVG (Menú principal, Nueva factura, Histórico, Clientes, Configuración, Copia de seguridad, Salir) y marca de pantalla activa; verificar que navega a cada vista.
- [x] 2.2 Integrar la barra en los controladores y FXML de todas las vistas salvo el menú principal (Backup, Clientes, Configuración, Editor, Histórico, Versiones); verificar que aparece en cada pantalla y que no aparece en el menú principal.

## 3. Cabecera de empresa y resumen del editor

- [x] 3.1 Mostrar en el menú principal el nombre, el NIF y el logo configurados de la empresa; verificar que se ven al abrir la aplicación con empresa configurada.
- [x] 3.2 Mostrar el logo de la empresa en la cabecera del editor; verificar que aparece al abrir una factura con logo configurado.
- [x] 3.3 Sustituir el resumen en bloque del editor por etiquetas separadas de base total, IVA total y total (`lblBaseTotal`, `lblIvaTotal`, `lblTotal`); verificar que los valores cuadran con el cálculo de `CalculoService`.

## 4. Confirmación de salida y cierre

- [x] 4.1 Pedir confirmación de salida al cerrar la ventana en `Main`, respetando la gestión de cambios sin guardar de la vista actual; verificar que cerrar pide confirmación y que los cambios sin guardar siguen bloqueando la salida.

## 5. Verificación final

- [x] 5.1 Ejecutar la suite completa (`mvn.cmd test`) y verificar que compila y que todos los tests pasan (31 tests, 0 fallos).
