## Context

Estado a 13/09/2026 (commit `8141831`). Números de línea orientativos.

**`service/BackupService`** (constructor `BackupService(Clock)`):

| Método | Qué hace con la base |
|---|---|
| `crearBackup(Path carpeta)` | `Database.getConnection()` + `VACUUM INTO '<ruta>'`; `throws SQLException, IOException` |
| `leerResumen(Path origen)` | comprueba fichero legible y que no sea `Database.dbPath()`; abre con `DriverManager`; `PRAGMA quick_check`; `comprobarTablasNucleo` (`sqlite_master`); `PRAGMA user_version`; `elementosFaltantes` (`sqlite_master` + `PRAGMA table_info`); `SELECT` de empresa, número de facturas y última fecha; `SQLException` → `ValidationException("No se pudo leer la copia: " + msg)` |
| `restaurarEnEmpresaActiva(Path origen)` | `leerResumen`; copia de rescate en `dataDir/copias_previas`; `Database.resetConnection()`; `Files.copy` sobre `dbPath`; `borrarDiario`; `Database.getConnection()`; si falla, repite con el rescate y lanza `IOException("No se pudo restaurar; se ha recuperado la base anterior: …")` |
| `restaurarComoEmpresaNueva(Path, String)` | `leerResumen`; `EmpresaManager.crearEmpresa`; `Files.copy` a `dbPathDe(slug)`; `borrarDiario`; `DriverManager` + `Migrations.migrate`; si falla, elimina la empresa y lanza `IOException("No se pudo crear la empresa desde la copia: …")` |

**`service/EmpresaManager.crearEmpresa`**: `Files.createDirectories`, `DriverManager.getConnection("jdbc:sqlite:" + destino)`, `PRAGMA foreign_keys = ON`, `Migrations.migrate(c)`.

**`db/Database.getConnection`**: crea `dataDir`, `DriverManager.getConnection("jdbc:sqlite:" + dbPath())`, `PRAGMA foreign_keys = ON`, `Migrations.migrate`.

**`ui/BackupController`**: `contarFacturasActivas()` con `SELECT COUNT(*) FROM factura` (0 si falla); `mostrarResumen` usa `Migrations.ultimaVersion()`; importa `Database`, `ResultSet`, `Statement`.

**`service/BackupServiceTest`**: 13 tests con `new BackupService(Clock.systemDefaultZone())` que comprueban también los mensajes de error.

**`InstanciaUnica.adquirir()`**: si `tryLock()` lanza `IOException`, el canal queda abierto.

**Referencia de estilo del usuario (Biblioteca8):** una clase por entidad que hace sus consultas y sus comprobaciones (`Libros`), y una clase que organiza la copia de seguridad (`CopiaSeguridadXML`). El usuario pide la solución más simple posible.

## Goals / Non-Goals

**Goals:** que no haya `java.sql`, `DriverManager`, SQL ni `PRAGMA` en `service/` ni en `ui/`; mismos mensajes y mismo comportamiento; la forma más simple.

**Non-Goals:** `record` nuevos o clases intermedias; separar datos y reglas de la copia en capas distintas; cambiar formato o nombre de las copias; mover `CargarDemo` o `Migrations`; quitar a los servicios el uso de rutas de `Database`; cambiar el esquema.

## Decisions

### D1. `CopiaRepository`: todo lo de SQLite de las copias, incluidas sus comprobaciones

Clase de instancia en `repository/`, sin estado, construida en `Servicios`.

```java
public class CopiaRepository {

    public void crearCopia(Path archivo)
    public BackupService.ResumenBackup leerResumen(Path origen) throws ValidationException
    public void reemplazarBaseActiva(Path origen) throws IOException
    public void instalarComoBase(Path origen, Path destinoDb) throws IOException
}
```

- `crearCopia(archivo)`: el `VACUUM INTO` de hoy sobre `Database.getConnection()`, con la ruta escapada igual. `SQLException` → `DatosException`.
- `leerResumen(origen)`: se **mueve tal cual** desde `BackupService.leerResumen` todo lo que va desde abrir la copia con `DriverManager` hasta construir el `ResumenBackup`, junto con `comprobarTablasNucleo`, `elementosFaltantes`, `notaVersion` y las constantes `TABLAS_NUCLEO`, `TABLAS_APLICACION` y `COLUMNAS_APLICACION`. Mismos mensajes, mismo orden, mismo `catch (SQLException e)` → `ValidationException("No se pudo leer la copia: " + e.getMessage())`. Las dos comprobaciones previas (fichero legible y no ser la base activa) se quedan en `BackupService` antes de llamar.
- `reemplazarBaseActiva(origen)`: `Database.resetConnection()`, `Files.copy(origen, Database.dbPath(), REPLACE_EXISTING)`, borrar diario en `Database.dataDir()`, `Database.getConnection()` (con `SQLException` → `DatosException`).
- `instalarComoBase(origen, destinoDb)`: `Files.copy(origen, destinoDb, REPLACE_EXISTING)`, borrar diario en `destinoDb.getParent()`, `Database.migrarBase(destinoDb)`.
- `borrarDiario` pasa aquí como método privado, igual que hoy.

Es el mismo reparto que `Libros` en Biblioteca8: la clase que habla con la base de datos también comprueba lo que lee.

`ResumenBackup` sigue declarado dentro de `BackupService` (no se mueve para no tocar `BackupController`).

Descartado: devolver datos crudos en `record` nuevos y validar en el servicio. Es más «puro», pero añade dos clases y reescribe la validación; el usuario prefiere lo simple.

### D2. `BackupService`: organiza los pasos

```java
public BackupService(CopiaRepository copiaRepository, FacturaRepository facturaRepository, Clock clock)

public Path crearBackup(Path carpetaDestino) throws IOException
public ResumenBackup leerResumen(Path origen) throws ValidationException
public Path restaurarEnEmpresaActiva(Path origen) throws IOException, ValidationException
public EmpresaManager.EmpresaInfo restaurarComoEmpresaNueva(Path origen, String nombre) throws IOException, ValidationException
public int facturasEmpresaActiva()
public int versionEsquemaAplicacion()
```

- `crearBackup`: crea la carpeta, elige nombre con `rutaLibre` (se queda aquí) y llama a `copiaRepository.crearCopia(archivo)`.
- `leerResumen`: las dos comprobaciones de fichero de hoy y `return copiaRepository.leerResumen(origen);`.
- `restaurarEnEmpresaActiva`: `leerResumen`; `rescate = crearBackup(Database.dataDir().resolve("copias_previas"))`; `try { copiaRepository.reemplazarBaseActiva(origen); } catch (Exception e) { copiaRepository.reemplazarBaseActiva(rescate); throw new IOException("No se pudo restaurar; se ha recuperado la base anterior: " + e.getMessage(), e); }`; devuelve `rescate`.
- `restaurarComoEmpresaNueva`: igual que hoy, cambiando copia + diario + migración por `copiaRepository.instalarComoBase(origen, Database.dbPathDe(nueva.slug()))`.
- `facturasEmpresaActiva()` = `facturaRepository.contar()`.
- `versionEsquemaAplicacion()` = `Migrations.ultimaVersion()`.
- Sin imports de `java.sql`.

### D3. `Database.crearBase` y `migrarBase`

```java
public static void crearBase(Path destinoDb)
public static void migrarBase(Path destinoDb)
private static Connection abrir(Path db) throws SQLException
```

- `abrir(db)`: `DriverManager.getConnection("jdbc:sqlite:" + db)` + `PRAGMA foreign_keys = ON`. Lo usa también `getConnection()`, que no cambia de firma ni de comportamiento.
- `crearBase(destinoDb)`: crea la carpeta padre, `abrir`, `Migrations.migrate`, cierra. `IOException`/`SQLException` → `DatosException`.
- `migrarBase(destinoDb)`: `abrir`, `Migrations.migrate`, cierra. `SQLException` → `DatosException`.

`EmpresaManager.crearEmpresa` usa `Database.crearBase(destino)` y pierde los imports de `java.sql`.

### D4. `FacturaRepository.contar()`

`public int contar()` con `SELECT COUNT(*) FROM factura`, `SQLException` → `DatosException`.

### D5. `BackupController`

- `contarFacturasActivas()` → `try { return servicios.backup.facturasEmpresaActiva(); } catch (Exception e) { return 0; }`.
- `mostrarResumen` usa `servicios.backup.versionEsquemaAplicacion()`; la comparación y sus textos se quedan en el controlador.
- Quitar imports de `Database`, `ResultSet`, `Statement` y la referencia a `Migrations`.

### D6. `Servicios`

```java
CopiaRepository copiaRepository = new CopiaRepository();
backup = new BackupService(copiaRepository, facturaRepository, clock);
```

### D7. `InstanciaUnica`

En `adquirir()`, si `tryLock()` lanza `IOException`: cerrar `abierto`, dejar `canal` y `bloqueo` a `null` y relanzar.

### D8. Tests

- `service/BackupServiceTest`: solo cambia la construcción a `new BackupService(new CopiaRepository(), new FacturaRepository(), Clock.systemDefaultZone())`. Los 13 tests deben pasar sin tocar lo que comprueban.
- Nuevo `repository/CopiaRepositoryTest` (con `@TempDir` y `Database.setDataDir`): `crearCopia` genera un fichero; `leerResumen` de esa copia devuelve el número de facturas; `leerResumen` de un fichero de texto lanza `ValidationException`; `reemplazarBaseActiva` borra un `facturas.db-wal` huérfano.
- `db/DatabaseTest`: `crearBase` sobre una ruta nueva deja `PRAGMA user_version` igual a `Migrations.ultimaVersion()`; `migrarBase` sobre esa base no falla.

## Risks / Trade-offs

- **Riesgo bajo:** la validación se mueve sin reescribirse; los 13 tests de `BackupServiceTest` comprueban los mensajes.
- **Trade-off aceptado:** `CopiaRepository` contiene reglas de validación además de consultas, y lanza `ValidationException`. Es menos «puro», pero más simple y sigue el estilo de Biblioteca8.
- **Descuido posible:** dejar las constantes de tablas duplicadas en los dos sitios. Deben quedar solo en `CopiaRepository`.
