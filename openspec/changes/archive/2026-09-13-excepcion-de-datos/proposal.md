## Why

`java.sql.SQLException` forma parte del contrato de toda la capa de servicios. `FacturaService`, `NumeroService`, `VersionadoService`, `EstadoService`, `RectificativaService`, `FacturacionMensualService`, `HistorialService` y los cinco servicios de catálogos declaran `throws SQLException` en casi todos sus métodos públicos; también `Servicios` en sus constructores y `EditorController.proponerDestinoPdf`. `VersionadoService` llega a construir una `SQLException` a mano para decir que una versión no existe.

Mientras el driver JDBC aparezca en las firmas de negocio, la interfaz y los servicios están atados a que haya una base de datos relacional detrás, y cambiar de SQLite a PostgreSQL o meter un envío a la AEAT con reintentos obliga a revisar cada firma. Es el tercer change del orden acordado tras la auditoría de arquitectura (la parte de excepciones que se separó del reloj inyectable).

## What Changes

- Nueva excepción no comprobada `db/DatosException`, que envuelve la `SQLException` original conservando su mensaje y su causa.
- Cada método público de los repositorios captura `SQLException` y lanza `DatosException`. Sus firmas dejan de declarar `throws SQLException`.
- Los servicios, `Servicios` y `EditorController` dejan de declarar `throws SQLException` y dejan de importarla.
- Las transacciones de `FacturaService`, `EstadoService` y `FacturacionMensualService` siguen haciendo `rollback` ante cualquier fallo: su `catch` pasa de `SQLException | ValidationException | RuntimeException` a `ValidationException | RuntimeException`.
- `Database.commit`, `rollback`, `beginTransaction` y `endTransaction` lanzan `DatosException` en lugar de `RuntimeException` genérica, con los mismos mensajes.
- `ValidationException` no cambia: sigue siendo comprobada y sigue siendo la de las reglas de negocio.
- Ningún cambio visible: los mensajes de error que ve el usuario son los mismos.

## Capabilities

### New Capabilities

Ninguna.

### Modified Capabilities

Ninguna: `skip_specs: true`. Ningún requisito describe tipos de excepción; los mensajes de error no cambian.

## Impact

- Nuevo: `src/main/java/com/alcazaba/facturacion/db/DatosException.java`.
- `db/Database.java` (solo los cuatro métodos de transacción).
- Los diez `repository/*Repository.java`.
- `service/`: `Servicios`, `ClienteService`, `SerieService`, `IvaService`, `RetencionService`, `ConfigService`, `NumeroService`, `VersionadoService`, `FacturaService`, `EstadoService`, `RectificativaService`, `FacturacionMensualService`, `HistorialService`.
- `ui/EditorController.java` (firma de `proponerDestinoPdf`).
- Tests: `ui/ClientesNifValidationTest` y los que ya no compilen por capturar `SQLException`.
- Fuera: `BackupService`, `EmpresaManager`, `BackupController` y `Migrations`, que usan JDBC directamente y se reorganizan en el change de sacar SQL de la interfaz y los servicios; `Database.getConnection()`, que sigue lanzando `SQLException` porque solo lo usan la capa de datos y esas clases.
