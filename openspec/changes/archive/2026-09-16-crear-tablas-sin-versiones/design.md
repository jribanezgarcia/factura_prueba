## Context

Estado a 16/09/2026 (commit `9b0e59c`). Números de línea orientativos.

**`Migraciones`** (`modelo/negocio/sqlite/Migraciones.java`, 78 líneas, `static`):
- `SCRIPTS = List.of("db/migrations/001_baseline.sql")`.
- `migrar(Connection)`: lee `PRAGMA user_version`; ejecuta los scripts con número mayor y apunta la versión.
- `ultimaVersion()`, `versionActual(Connection)`, `leerScript(String)`, `ejecutarScript(Connection, String)` (parte el script por `;` y ejecuta cada sentencia).

**El script** `db/migrations/001_baseline.sql`: 11 `CREATE TABLE IF NOT EXISTS`, 3 `CREATE INDEX IF NOT EXISTS` y siembras: **4 `INSERT INTO tipo_iva`** (sin `OR IGNORE`) y `INSERT OR IGNORE INTO empresa (id) VALUES (1)`. Hoy no se duplican porque `migrar` no vuelve a ejecutar el script cuando la versión ya es 1. **Si se ejecutara siempre, los 4 tipos de IVA se duplicarían en cada arranque.**

**Quién llama a `Migraciones`:**
- `Conexion.establecerConexion()` (~113): abre la base de la empresa activa y `Migraciones.migrar(conexion)`.
- `Conexion.crearBase(Path)` (~134): crea la carpeta, abre y `Migraciones.migrar(c)`. La usa `Empresas.crearEmpresa`.
- `Conexion.migrarBase(Path)` (~146): abre y migra una base existente. La usa `CopiaSeguridadDAO.instalarComoBase` (restaurar como empresa nueva).
- `CopiaSeguridadDAO.leerResumen` (~70): lee `PRAGMA user_version` en `uv`, rechaza si `uv <= 0` («La copia no tiene una versión de esquema válida.»), añade `notaVersion(uv)` al mensaje de lo que falta y pasa `uv` a `ResumenCopia`.
- `CopiaSeguridadDAO.notaVersion(int)` (~166): «La versión de esquema de la copia (N) es anterior/posterior…».
- `CopiaSeguridad.versionEsquemaAplicacion()` (~127) y el componente `int versionActual` del record `ResumenCopia` (~36).
- `CopiaSeguridadController.mostrarResumen` (~162): línea «Versión de esquema: N» y « (anterior/posterior a la de la aplicación)».

**Javadoc que nombra las migraciones**: `CargarDemo` (clase, ~17), `Empresas.conectar` (~63, «abre y migra su base de datos»).

**Tests**:
- `MigracionesTest`: `baseNuevaConVersion1YTodasLasTablas` (11 tablas, 4 tipos de IVA, uno suplido llamado «Suplido») y `migrarDosVecesNoDuplicaSiembras`.
- `ConexionTest`: `crearBaseDejaVersionDeEsquema` y `migrarBaseNoFalla`.
- `EmpresasTest.laBaseNuevaTieneElEsquemaCompleto` (~146): comprueba `user_version`.
- `CopiaSeguridadTest`: `leerResumenDevuelveDatosCorrectos` (~185, `versionActual`), `rechazaCopiaSinLasTablasNucleo` (~236, `PRAGMA user_version = 1`), `restaurarDejaLaBaseMigrada` (~253), `aceptaEsquemaPosteriorConLasMismasTablas` (~270), `rechazaEsquemaPosteriorConTablasDistintas` (~285).

**Decisión del usuario (16/09/2026)**: quitar las versiones; es una beta, las tablas son siempre las mismas y los datos se reinician.

## Goals / Non-Goals

**Goals:** crear las tablas de una base nueva sin números de versión; que abrir una base que ya tiene tablas no duplique nada; que restaurar una copia dependa solo de sus tablas y columnas; menos código que explicar.

**Non-Goals:**
- Cambiar el contenido del script (tablas, columnas, siembras).
- Convertir el record `ResumenCopia` en clase (change `clases-independientes`) ni quitar las `Task` de `CopiaSeguridadController` (change `sin-clases-anonimas-ni-hilos`).
- Arreglar construcciones que prohíbe `AGENTS.md` fuera de las líneas tocadas (streams de `getEmpresasDisponibles`, ternarios de `leerResumen`, `var` de los tests…).

## Decisions

### D1. Solo creamos las tablas si la base todavía no las tiene

Una base «nueva» es la que **no tiene la tabla `empresa`**. Solo entonces se ejecuta el script. Así el script se queda igual (con sus `INSERT` de tipos de IVA) y abrir una base ya creada no duplica nada.

Alternativa descartada: ejecutar siempre el script cambiando los `INSERT` por `INSERT … WHERE NOT EXISTS (…)`. Obliga a explicar SQL menos habitual y a tocar el script.

### D2. Los métodos pasan de `Migraciones` a `Conexion`

En `Conexion`, junto al resto de la base de datos:

```java
private static final String SCRIPT_TABLAS = "db/crear_tablas.sql";

/**
 * Creamos las tablas de la aplicación si la base todavía no las tiene.
 * Consideramos que una base es nueva cuando no existe la tabla empresa;
 * así, abrir una base ya creada no vuelve a insertar los tipos de IVA.
 */
public static void crearTablasSiFaltan(Connection c) throws SQLException {
    if (!existeTabla(c, "empresa")) {
        ejecutarScript(c, leerScript(SCRIPT_TABLAS));
    }
}

private static boolean existeTabla(Connection c, String tabla) throws SQLException {
    try (PreparedStatement ps = c.prepareStatement(
            "SELECT name FROM sqlite_master WHERE type = 'table' AND name = ?")) {
        ps.setString(1, tabla);
        try (ResultSet rs = ps.executeQuery()) {
            return rs.next();
        }
    }
}
```

- `leerScript(String)` y `ejecutarScript(Connection, String)` se copian de `Migraciones` **tal cual** a `Conexion` como `private static`, con su Javadoc actual (el de `ejecutarScript` explica por qué se parte por `;`). En `leerScript`, el mensaje «Migracion no encontrada: » pasa a «Script no encontrado: » y «Error al leer la migracion » a «Error al leer el script ».
- `establecerConexion()`: `Migraciones.migrar(conexion)` → `crearTablasSiFaltan(conexion)`.
- `crearBase(Path)`: `Migraciones.migrar(c)` → `crearTablasSiFaltan(c)`. Su Javadoc: «Crea una base nueva en la ruta indicada, con su carpeta y sus tablas.».
- `migrarBase(Path)` **se borra**.
- Imports nuevos en `Conexion`: `java.io.InputStream`, `java.nio.charset.StandardCharsets`, `java.sql.PreparedStatement`, `java.sql.ResultSet`.
- `crearTablasSiFaltan` es `public` para poder probarla desde `ConexionTest`.

### D3. El script cambia de sitio, no de contenido

`git mv src/main/resources/db/migrations/001_baseline.sql src/main/resources/db/crear_tablas.sql`. La carpeta `migrations` queda vacía y desaparece. `Migraciones.java` y `MigracionesTest.java` se borran.

### D4. Restaurar como empresa nueva

`CopiaSeguridadDAO.instalarComoBase(Path origen, Path destinoDb)`: se quita la línea `Conexion.migrarBase(destinoDb);`. La copia ya se ha comprobado con `leerResumen` antes de instalarla (`CopiaSeguridad.restaurarComoEmpresaNueva` llama primero a `leerResumen`), así que tiene todas las tablas.

### D5. La copia se comprueba solo por su estructura

En `CopiaSeguridadDAO.leerResumen`:
- Borrar el bloque que lee `PRAGMA user_version` en `uv` y el `if (uv <= 0)`.
- El mensaje de lo que falta queda: `"La copia no contiene " + String.join(", ", faltantes) + "."`.
- `return new CopiaSeguridad.ResumenCopia(nombre, nif, logoPath, logoExiste, numFacturas, ultimaFecha);` (sin `uv`).
- Borrar `notaVersion(int)`.

En `CopiaSeguridad`:
- Record `ResumenCopia`: quitar el componente `int versionActual`.
- Borrar `versionEsquemaAplicacion()` y el import de `Migraciones`.

En `CopiaSeguridadController.mostrarResumen`:
- Borrar la línea «Versión de esquema: » y las líneas `int app = …` y el `if / else if` de «anterior/posterior».
- La línea «Última fecha: » deja de terminar en `"\n"`, porque ya no hay nada debajo salvo el aviso del logo, que empieza por `"\n⚠ …"`. Como esa línea tiene un ternario y hay que tocarla, se escribe con `if / else`:

```java
sb.append("Última fecha: ");
if (r.ultimaFecha() == null) {
    sb.append("(ninguna)");
} else {
    sb.append(r.ultimaFecha());
}
```

### D6. Javadoc

- `CargarDemo` (clase): «… se elimina antes, así que ejecutar dos veces no duplica nada), crea sus tablas y ejecuta {@code db/seed_demo.sql}.».
- `Empresas.conectar`: «… la fija como activa, abre su base de datos (creando las tablas si es nueva) e inicializa la sesion con la fecha de trabajo.».

### D7. Tests

- **`ConexionTest`**:
  - `crearBaseDejaVersionDeEsquema` → `crearBaseCreaTodasLasTablas`: crea la base con `Conexion.crearBase(destino)` y comprueba las 11 tablas, 4 tipos de IVA y exactamente uno con `es_suplido = 1` llamado «Suplido» (lo que comprobaba `MigracionesTest.baseNuevaConVersion1YTodasLasTablas`, sin la versión).
  - `migrarBaseNoFalla` → `crearTablasDosVecesNoDuplica`: crea la base, abre una conexión, llama dos veces a `Conexion.crearTablasSiFaltan(c)` y comprueba 4 tipos de IVA, 1 suplido y 1 fila de empresa.
  - Tipos siempre escritos, sin `var`, sin streams, `for` clásico para las tablas esperadas.
- **`EmpresasTest.laBaseNuevaTieneElEsquemaCompleto`**: en lugar de `PRAGMA user_version`, comprobar que existe la tabla `empresa` (`SELECT name FROM sqlite_master WHERE type='table' AND name='empresa'` devuelve fila).
- **`CopiaSeguridadTest`**:
  - `leerResumenDevuelveDatosCorrectos`: borrar el `assertEquals` de `versionActual`.
  - `rechazaCopiaSinLasTablasNucleo`: borrar `st.executeUpdate("PRAGMA user_version = 1");`.
  - `restaurarDejaLaBaseMigrada` → `restaurarDejaLaBaseUtilizable`: borrar el `assertEquals` de versiones; añadir que `SELECT COUNT(*) FROM tipo_iva` devuelve 4 tras restaurar.
  - `aceptaEsquemaPosteriorConLasMismasTablas` → `aceptaCopiaConLasMismasTablas`: sin el `PRAGMA user_version = 99` (y sin la conexión que solo servía para eso); comprobar que `leerResumen(copia)` no lanza y devuelve el nombre de la empresa.
  - `rechazaEsquemaPosteriorConTablasDistintas` → `rechazaCopiaConTablasDistintas`: borrar `st.executeUpdate("PRAGMA user_version = 99");`.
  - Quitar el import de `Migraciones`.
- Borrar `MigracionesTest.java`.

### D8. Documentación

En `docs/tecnico.md`:
- Diagrama de capas: `F["🔌 Conexion + Migraciones<br/><small>conexión SQLite y tablas</small>"]` → `F["🔌 Conexion<br/><small>conexión SQLite y creación de tablas</small>"]`.
- Tabla de decisiones: la fila «**Migraciones versionadas** (`PRAGMA user_version`) | Crear las tablas a mano | Una base antigua se actualiza sola al abrirla» → «**Un único script de tablas** (`db/crear_tablas.sql`) | Migraciones versionadas (`PRAGMA user_version`) | En una versión en desarrollo las tablas son siempre las mismas; crear una base nueva es ejecutar un solo script».

## Risks / Trade-offs

- **Una base con la tabla `empresa` pero sin otras tablas** no se completaría → solo puede pasar a mano; las copias se comprueban por estructura antes de restaurar. Aceptado.
- **Si en el futuro cambian las tablas**, las bases existentes no se actualizan solas → aceptado por el usuario: los datos se reinician mientras el programa está en desarrollo.
- **Copias antiguas con `user_version`**: ese número se ignora; se aceptan si tienen las tablas.
