## 1. Preparación

- [x] 1.1 Cambiar `Migrations.userVersion(Connection)` de `private` a `public` en `db/Migrations.java` y verificar que compila.
- [x] 1.2 Añadir el método `rutaLibre(Path carpeta, String base)` en `service/BackupService.java` y verificar que compila.

## 2. BackupService — leerResumen

- [x] 2.1 Crear el record `ResumenBackup` (nombreEmpresa, nif, logoPath, logoExiste, numFacturas, ultimaFecha, userVersion, tablasCoinciden) en `service/BackupService.java`.
- [x] 2.2 Implementar `leerResumen(Path origen)` con validación (archivo, no base activa, quick_check, tablas, user_version) y lectura de datos (empresa, facturas, última fecha, esquema). Verificar que compila.

## 3. BackupService — restaurarEnEmpresaActiva

- [x] 3.1 Implementar `restaurarEnEmpresaActiva(Path origen)` con copia de rescate automática, resetConnection, Files.copy, limpieza de diario, reconexión y rollback. Verificar que compila.

## 4. BackupService — restaurarComoEmpresaNueva

- [x] 4.1 Implementar `restaurarComoEmpresaNueva(Path origen, String nombre)` con crearEmpresa, Files.copy sobre dbPathDe(slug), limpieza de diario y migración con conexión local. Verificar que compila.

## 5. BackupService — rutaLibre en crearBackup

- [x] 5.1 Modificar `crearBackup` para usar `rutaLibre` en lugar del `resolve` directo. Verificar que compila.

## 6. UI — Backup.fxml

- [x] 6.1 Envolver el `<center>` actual en un contenedor con ScrollPane si es necesario y añadir la segunda tarjeta «Restaurar una copia» con Label título, Label muted, HBox con botón y lblOrigen, cajaResumen con lblResumen, RadioButton con ToggleGroup, txtNombreEmpresa, btnRestaurar y lblResultadoRestauracion. Verificar que carga sin errores en la aplicación.

## 7. UI — BackupController

- [x] 7.1 Implementar `seleccionarOrigen()` con FileChooser (*.db), llamada a leerResumen en hilo aparte y pintado del resumen.
- [x] 7.2 Implementar la regla del NIF: comparar NIF del backup con el de la empresa activa, deshabilitar «Reemplazar» si no coinciden, excepción si la activa está vacía.
- [x] 7.3 Implementar `restaurar()` con Dialogos.confirmar(), llamada a restaurarEnEmpresaActiva o restaurarComoEmpresaNueva en Task, manejo de éxito y error.
- [x] 7.4 Al terminar reemplazando la activa: Dialogos.info() con ruta del rescate y nav.mostrar(MenuPrincipal.fxml). Al terminar creando nueva: Dialogos.confirmar() preguntando si cambiar, y conectar + nav.mostrar o quedarse en Backup.

## 8. Pruebas

- [x] 8.1 Crear `service/BackupServiceTest.java` con montaje @TempDir, Database.setDataDir, resetConnection, Sesion.reiniciar, crearEmpresa y conectar. Implementar tests: restaurarDevuelveLosDatosDeLaCopia, restaurarDejaCopiaDeRescateConElEstadoPrevio, restaurarComoEmpresaNuevaNoTocaLaActiva, leerResumenDevuelveDatosCorrectos, leerResumenSinFacturas, rechazaArchivoQueNoEsBaseDeDatos, rechazaCopiaSinLasTablasDeLaAplicacion, rechazaLaPropiaBaseActivaComoOrigen, aceptaEsquemaPosteriorConLasMismasTablas, rechazaEsquemaPosteriorConTablasDistintas, limpiaElDiarioHuerfano.
- [x] 8.2 Crear `ui/BackupLayoutTest.java` siguiendo el patrón de ConfiguracionLayoutTest: JavaFxTestSupport, Platform.runLater, nav.mostrar, applyCss + resize(1024, 768) + layout, comprobando que ningún Region visible se sale por abajo ni por la derecha con las dos tarjetas.
- [x] 8.3 Ejecutar la suite completa (`mvn -o test`) y confirmar todos los tests en verde.

## 9. Cierre

- [x] 9.1 Sincronizar la spec delta con `openspec/specs/invoicing/spec.md`.
- [x] 9.2 Archivar el change y actualizar CONTINUAR_MAÑANA.md.
- [x] 9.3 Commit y push.
