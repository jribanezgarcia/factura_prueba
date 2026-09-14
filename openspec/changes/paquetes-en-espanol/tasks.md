> Tablas exactas en `design.md`. **No se cambia el nombre de ninguna clase**, solo paquetes, imports y rutas. Mover siempre con `git mv`. Ningún comentario nuevo en el código.

## 1. Código de producción

- [ ] 1.1 Crear las carpetas `src/main/java/cabofactu/{modelo/dominio,modelo/negocio/sqlite,fichero,pdf,vista/controlador,vista/utilidades,utilidades}`.
- [ ] 1.2 Mover cada clase de `src/main/java/com/alcazaba/facturacion/` a su carpeta nueva según `design.md - D1`, con `git mv`.
- [ ] 1.3 Cambiar la línea `package` de cada clase movida al paquete nuevo de `design.md - D1`.
- [ ] 1.4 Corregir todos los `import com.alcazaba.facturacion.…` de producción al paquete nuevo de cada clase importada. Añadir los `import` que falten entre clases que antes compartían paquete y ahora no (por ejemplo `vista.controlador` → `vista.Navegador`, `vista.utilidades.Dialogos`; `modelo.negocio` → `modelo.negocio.sqlite.*`; `fichero.BackupService` → `modelo.negocio.EmpresaManager`, `ValidationException`, `Reloj`; `modelo.Servicios` → `modelo.negocio.*`, `fichero.BackupService`). Quitar los `import` que pasen a ser del mismo paquete.
- [ ] 1.5 En `vista/utilidades/Dialogos.java`, hacer `public` `setImpl` y `restoreDefault`. Ver `design.md - D4`.
- [ ] 1.6 `mvn -q compile` sin errores.

## 2. Recursos

- [ ] 2.1 Mover los recursos según la tabla de `design.md - D3` con `git mv` (FXML, temas, imágenes y `VistaPrueba.fxml` de test). No mover `src/main/resources/db/`.
- [ ] 2.2 Aplicar en Java y FXML (main y test) las sustituciones de texto de `design.md - D3`, incluidos `fx:controller`, el `<?import …PreviaCabecera?>` de `Configuracion.fxml` y `@imagenes/icono-aplicacion.png` en `Arranque.fxml`.
- [ ] 2.3 Borrar las carpetas vacías que queden bajo `src/main/java/com`, `src/test/java/com`, `src/main/resources/com` y `src/test/resources/com`.

## 3. Tests

- [ ] 3.1 Mover cada test a su carpeta según `design.md - D2`, con `git mv`, y cambiar su `package` e imports.
- [ ] 3.2 En `StyleClassSeparadorTest`, cambiar la ruta de disco de los FXML según la última fila de la tabla de sustituciones de `design.md - D3`.
- [ ] 3.3 Si algún test no compila por un miembro no público de otro paquete, hacer `public` ese miembro en la clase de producción (no mover el test) y anotarlo aquí: ______.

## 4. Ficheros fuera de `src`

- [ ] 4.1 `pom.xml`: `groupId` y `mainClass` según `design.md - D5`.
- [ ] 4.2 `cargar_demo.bat`: `exec.mainClass` según `design.md - D5`.
- [ ] 4.3 `README.md`: solo la ruta de la imagen de la línea 3, según `design.md - D5`.

## 5. Comprobaciones

- [ ] 5.1 `git grep -n "alcazaba" -- src pom.xml cargar_demo.bat README.md` no devuelve nada.
- [ ] 5.2 `git grep -n "facturacion\.\(ui\|service\|db\|repository\|model\|util\)" -- src` no devuelve nada.
- [ ] 5.3 `git status` muestra los ficheros movidos como `renamed` (no como borrado + nuevo).
- [ ] 5.4 Ninguna clase ha cambiado de nombre: `git diff --stat -M HEAD` solo muestra rutas nuevas con los mismos nombres de fichero.

## 6. Verificación automática

- [ ] 6.1 `mvn test` en verde, con el mismo número de tests que antes del change.

## 7. Verificación manual

- [ ] 7.1 `mvn javafx:run`: la pantalla de arranque muestra el icono de la aplicación y la lista de empresas.
- [ ] 7.2 Entrar en una empresa: el Menú carga con su tema y su logo.
- [ ] 7.3 Configuración > Apariencia: cambiar a otro tema (por ejemplo `omarchy`) y volver; se aplica en el momento.
- [ ] 7.4 Abrir Editor, Histórico, Clientes, Copias y Versiones de una factura: todas cargan.
- [ ] 7.5 Exportar una factura a PDF: se genera igual que antes.
- [ ] 7.6 Doble clic en `cargar_demo.bat`: termina sin errores.
