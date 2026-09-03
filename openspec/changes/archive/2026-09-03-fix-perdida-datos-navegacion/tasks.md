## 1. Test que reproduce el fallo

- [x] 1.1 Añadir `NavegacionCambiosSinGuardarTest`: una `Vista` de prueba cuyo `puedeCerrar()` devuelva `false`, mostrada en el `Navegador`; al llamar a `mostrar(...)` de otra vista, la escena del `Stage` no debe cambiar y el retorno debe ser `null`.
- [x] 1.2 Ejecutar y confirmar que **falla** con el código actual (hoy navega igualmente).
- [x] 1.3 Añadir el caso complementario: con `puedeCerrar()` devolviendo `true`, la navegación ocurre y el retorno no es `null`.

## 2. Guarda en el navegador

- [x] 2.1 Añadir a `Navegador` un campo `vistaActual`, asignado al final de `mostrar(...)` junto a la llamada a `onVistaCambio`.
- [x] 2.2 Al principio de `mostrar(...)`, devolver `null` sin tocar la escena si `vistaActual != null && !vistaActual.puedeCerrar()`.
- [x] 2.3 Documentar en el Javadoc de `mostrar(...)` que devuelve `null` cuando la vista actual cancela la salida.

## 3. Quitar las guardas duplicadas del editor

- [x] 3.1 En `EditorController.volver()`, `nuevaFactura()` y `verVersiones()`, eliminar la comprobación propia de `puedeCerrar()` y llamar directamente a `nav.mostrar(...)`.
- [x] 3.2 Comprobar a mano que el diálogo de cambios sin guardar sale **una sola vez** por gesto, y que al elegir «Guardar y salir» se guarda una vez y se navega.

## 4. Llamantes que usan el retorno

- [x] 4.1 `EditorController.verVersiones()` (`:1173`): comprobar `null` antes de usar el `VersionesController`.
- [x] 4.2 `HistoricoController.abrirVersion()` (`:187`): comprobar `null` antes de usar el `EditorController`.
- [x] 4.3 `VersionesController.abrirVersion()` (`:98`): comprobar `null` antes de usar el `EditorController`.

## 5. Tests existentes

- [x] 5.1 Revisar `EditorFlujoTecladoTest` y `EditorTamanoMinimoTest`: si dejan el editor con cambios y luego navegan, sustituir la implementación de diálogos con `Dialogos.setImpl(...)` para que no se abra un modal bloqueante en la suite.
- [x] 5.2 Confirmar que `VentanaTransicionTest` y `UiSmokeTest` siguen en verde sin cambios (sus vistas usan el `puedeCerrar()` por defecto).

## 6. Especificación

- [x] 6.1 MODIFIED «Cambios sin guardar»: la confirmación SHALL pedirse ante **cualquier** navegación que abandone una vista con cambios sin guardar, no solo al pulsar Volver o al cerrar la aplicación. Escenario nuevo con los botones de la barra de navegación.

## 7. Verificación final

- [x] 7.1 Suite completa en verde con `mvn clean test`.
- [x] 7.2 Verificación manual: rellenar media factura y pulsar cada uno de los seis iconos de la barra superior y cada entrada del menú principal; debe preguntar siempre.
