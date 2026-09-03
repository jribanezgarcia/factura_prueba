## Why

`BackupService.crearBackup()` crea copias de la base de datos pero no hay forma de recuperarlas desde la aplicación. La única vía es sustituir el archivo a mano en `%APPDATA%\Facturacion\<slug>\facturas.db`, que es frágil porque `Database.connection` es estática y se abre una sola vez —Windows mantiene el handle de SQLite—, el nombre `facturas.db` es una constante privada y un diario huérfano (`-wal`/`-shm`) puede corromper la base. Además, `crearBackup` tiene precisión de segundo y `VACUUM INTO` no sobrescribe, así que dos copias dentro del mismo segundo fallan.

## What Changes

- `BackupService` gana un método `leerResumen` que abre una conexión JDBC temporal a un archivo `.db` externo, extrae empresa/NIF/nº facturas/última fecha/versión de esquema y valida integridad (quick_check, tablas, user_version). No toca `Database.connection`.
- `BackupService` gana `restaurarEnEmpresaActiva` (copia de rescate automática, resetConnection, Files.copy, limpieza de diario, reconexión con rollback) y `restaurarComoEmpresaNueva` (crear empresa, copiar base, migrar con conexión local).
- `Migrations.userVersion(Connection)` cambia de `private` a `public` para leer la versión de esquema de una copia externa.
- `BackupService.crearBackup` usa un método `rutaLibre` que genera nombres sin colisión (`base_2.db`, `base_3.db`…), arreglando el fallo de dos copias en el mismo segundo.
- `Backup.fxml` añade una segunda tarjeta «Restaurar una copia» con selección de archivo, resumen, dos RadioButton (reemplazar activa / crear nueva) y campo de nombre para la nueva empresa.
- `BackupController` gestiona la selección de archivo (FileChooser), validación NIF, confirmación, restauración en hilo aparte y navegación post-restauración.

## Capabilities

### New Capabilities
- (ninguna)

### Modified Capabilities
- `invoicing`: se modifica el requisito «Copia de seguridad» para añadir la capacidad de restauración, resumen previo, copia de rescate automática, dos destinos (reemplazar activa / crear nueva) y validación de compatibilidad de esquema.

## Impact

- `db/Migrations.java`: `userVersion` pasa de `private` a `public`.
- `service/BackupService.java`: nuevos métodos `leerResumen`, `restaurarEnEmpresaActiva`, `restaurarComoEmpresaNueva`, `rutaLibre`; modificación de `crearBackup`.
- `ui/Backup.fxml`: segunda tarjeta con controles de restauración.
- `ui/BackupController.java`: handlers `seleccionarOrigen`, `restaurar`, lógica NIF, navegación.
- `service/BackupServiceTest.java` (nuevo): 11 tests de unit/integration.
- `ui/BackupLayoutTest.java` (nuevo): test de layout con las dos tarjetas.
- No cambia el esquema de base de datos ni ninguna migración.
- `Database.java`, `EmpresaManager.java` y `Servicios.java` no se tocan.
