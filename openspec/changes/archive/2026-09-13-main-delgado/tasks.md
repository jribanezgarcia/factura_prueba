> Las firmas, el orden del arranque, los mensajes y los Javadoc están en `design.md`. En este change **sí se escriben Javadoc**, breves y explicando el porqué, en las clases y métodos nuevos y en los métodos que quedan en `Main` (decisión del usuario). Ningún otro comentario.

## 1. Instancia única

- [x] 1.1 Crear `src/main/java/com/alcazaba/facturacion/InstanciaUnica.java` como utilidad estática con `adquirir()` y `liberar()`, con el comportamiento y los Javadoc de `design.md - D2`.
- [x] 1.2 Capturar `OverlappingFileLockException` en `adquirir()` y tratarla como «otra instancia lo tiene» (`false`).

## 2. Preparación de datos

- [x] 2.1 Crear `src/main/java/com/alcazaba/facturacion/PreparacionDatos.java` con `crearCarpeta()` y `cargarDemoSiNoHayEmpresas()`, según `design.md - D3`.

## 3. Main

- [x] 3.1 En `Main.start`, sustituir la preparación de carpeta, el bloqueo y la carga de la demostración por llamadas a `PreparacionDatos` e `InstanciaUnica`, en el orden y con los tres mensajes de `design.md - D4`.
- [x] 3.2 En `cerrarAplicacion`, quitar `guardarPreferenciasVentana(stage)` y cambiar `liberarLock()` por `InstanciaUnica.liberar()`.
- [x] 3.3 Borrar de `Main` `prepararDatos`, `cmbEmpresaVacia`, `adquirirLock`, `liberarLock`, `guardarPreferenciasVentana`, los campos `lockChannel` y `lock`, y los imports sobrantes.
- [x] 3.4 Reescribir el Javadoc de la clase `Main` y poner Javadoc breve en `start`, `configurarVentana`, `mostrarArranque`, `entrarEnMenu` y `cerrarAplicacion`. Ver `design.md - D4`.
- [x] 3.5 No cambiar `main`, `configurarVentana`, `mostrarArranque` ni `entrarEnMenu` más allá del Javadoc (y de lo imprescindible para pasar el `ArranqueController` al aviso inicial, si ya no lo está).

## 4. Preferencias de ventana

- [x] 4.1 En `service/PreferenciasGlobales.java`, borrar `VENTANA_X`, `VENTANA_Y`, `VENTANA_W` y `VENTANA_H`. Ver `design.md - D5`.
- [x] 4.2 En `src/test/java/com/alcazaba/facturacion/service/PreferenciasGlobalesTest.java`, sustituir esas constantes por las claves literales `"clave_decimal"` y `"clave_entera"` manteniendo las comprobaciones.
- [x] 4.3 Buscar `VENTANA_` y `ventana_` en `src/main`: no debe quedar ningún uso.

## 5. Tests y verificación

- [x] 5.1 Crear `src/test/java/com/alcazaba/facturacion/InstanciaUnicaTest.java` con los casos de `design.md - D6`.
- [x] 5.2 Crear `src/test/java/com/alcazaba/facturacion/PreparacionDatosTest.java` con los casos de `design.md - D6`, siempre sobre `@TempDir`.
- [x] 5.3 `mvn test` en verde, y comprobar que la carpeta real `%APPDATA%\Facturacion` no ha cambiado tras la suite (ni empresa `demo` nueva ni `facturas.lock` bloqueado).
- [x] 5.4 Arrancar la aplicación normalmente: sale el arranque, se entra en una empresa y se cierra con Salir sin errores.
- [x] 5.5 Con la aplicación abierta, intentar abrir una segunda: sale «La aplicación ya está en ejecución» y la segunda se cierra.
- [x] 5.6 Cerrar la primera y abrirla de nuevo: arranca con normalidad (el bloqueo se liberó).
- [x] 5.7 Con la carpeta de datos apartada (renombrar `%APPDATA%\Facturacion`), arrancar: se carga la demostración y sale el aviso de bienvenida como antes. Restaurar la carpeta al terminar.
