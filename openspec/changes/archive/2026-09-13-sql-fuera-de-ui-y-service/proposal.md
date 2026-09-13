## Why

Tras los changes de servicios y excepciones, JDBC y SQL solo deberían vivir en `db/` y `repository/`, pero quedan tres excepciones:

- `ui/BackupController` hace `SELECT COUNT(*) FROM factura` sobre la conexión e importa `Database`, `Migrations`, `Statement` y `ResultSet`.
- `service/BackupService` ejecuta `VACUUM INTO`, abre copias con `DriverManager`, lanza `PRAGMA quick_check`, `PRAGMA user_version`, `PRAGMA table_info`, consulta `sqlite_master`, hace `SELECT` sobre la copia, sustituye el fichero `.db` y borra los diarios `-wal`/`-shm`, y declara `throws SQLException`.
- `service/EmpresaManager.crearEmpresa` abre la base nueva con `DriverManager`, activa `PRAGMA foreign_keys` y migra, repitiendo lo que ya hace `Database.getConnection`.

Todo eso es propio de SQLite. Mientras esté en la interfaz y en los servicios, pasar a otro motor obliga a reescribir lógica de negocio, y las copias de seguridad no se pueden probar sin una base real. Es el change 4 del orden acordado tras la auditoría de arquitectura.

## What Changes

- Nuevo `repository/CopiaRepository` (objeto, como el resto de repositorios), que hace todo lo que toca SQLite en las copias: crear la copia, **leer y comprobar** una copia devolviendo el `ResumenBackup` que ya existe (o `ValidationException` con los mismos mensajes de hoy), sustituir la base activa por una copia e instalar una copia como base de otra empresa. No se crean `record` nuevos.
- `BackupService` recibe `CopiaRepository` y `FacturaRepository` por constructor y queda como organizador de los pasos (copia de rescate, recuperar si falla, crear empresa), como `CopiaSeguridadXML` en Biblioteca8. Deja de usar `java.sql`. Gana `facturasEmpresaActiva()` y `versionEsquemaAplicacion()`.
- `Database` gana `crearBase(Path)` y `migrarBase(Path)`, y `getConnection` reutiliza la misma apertura. `EmpresaManager.crearEmpresa` usa `crearBase`.
- `FacturaRepository` gana `contar()`.
- `BackupController` deja de importar `Database`, `Migrations` y `java.sql`: pide la cuenta y la versión a `servicios.backup`.
- `Servicios` construye `CopiaRepository` y se lo pasa a `BackupService`.
- Corrección menor en `InstanciaUnica.adquirir()`: si `tryLock()` lanza `IOException`, cerrar el canal antes de propagarla.
- Tests nuevos de `CopiaRepository` y `Database.crearBase`; los existentes siguen comprobando lo mismo.
- Ningún cambio visible: mismas pantallas, mismos mensajes, mismos ficheros de copia.

## Capabilities

### New Capabilities

Ninguna.

### Modified Capabilities

Ninguna: `skip_specs: true`. «Copia de seguridad» y «Gestión de empresas» no cambian.

## Impact

- Nuevo: `src/main/java/com/alcazaba/facturacion/repository/CopiaRepository.java`.
- `db/Database.java`, `repository/FacturaRepository.java`, `service/BackupService.java`, `service/EmpresaManager.java`, `service/Servicios.java`, `ui/BackupController.java`, `InstanciaUnica.java`.
- Tests: `service/BackupServiceTest` (constructor), nuevos `repository/CopiaRepositoryTest` y casos en `db/DatabaseTest`.
- Fuera: `db/CargarDemo` y `db/Migrations` (ya están en `db`), el uso de rutas de `Database` (`dbPath`, `dataDir`, `dbPathDe`) desde servicios, y cualquier cambio de esquema.
