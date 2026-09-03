## Context

Una vez implementada la restauración (`2026-09-03-restaurar-copia-seguridad`), hay dos fallos
reproducidos. `verificarEstructura` exige siempre todas las tablas, de modo que una copia de una
versión anterior (sin `serie_siguiente`, `tipo_retencion` o `numero_disponible`) se rechaza aunque
podría migrarse al reconectar. Y el rollback de `restaurarEnEmpresaActiva` copia el rescate con la
conexión todavía abierta, que en Windows impide sobrescribir el archivo y oculta el error original.
Además hay varios detalles menores de limpieza y UX. Ver proposal.md - Why.

## Goals / Non-Goals

**Goals:**
- Aceptar y migrar copias de esquema anterior (solo exigir la estructura completa si la copia es
  posterior a la versión actual).
- Garantizar que el rollback cierra la conexión antes de sobrescribir el archivo, preservando el
  error original.
- No dejar empresas parcialmente creadas si `restaurarComoEmpresaNueva` falla.
- Eliminar la excepción genérica `Exception` de la API de restauración.
- Correcciones de UX: mostrar el nombre visible de la empresa activa, aplanar la regla del NIF y
  quedarse en Copias al responder «NO» al cambio de empresa.
- Derivar los nombres del diario (`-wal`/`-shm`) de la constante de la base en lugar de hardcodearlos.

**Non-Goals:**
- No cambiar el esquema de base de datos ni ninguna migración.
- No tocar `Database.java` (solo leer constantes ya existentes).
- No implementar conversión de datos entre versiones de esquema.

## Decisions

### D1. Dos comprobaciones, no una

Se separa `verificarEstructura` en dos métodos con responsabilidades distintas:

- `comprobarTablasNucleo(Connection)`: recorre `TABLAS_NUCLEO` (las de la migración `001_init.sql`:
  `cliente`, `serie`, `tipo_iva`, `factura`, `factura_version`, `factura_linea`, `empresa`,
  `preferencias`). Si falta alguna → `ValidationException`. Se ejecuta siempre en `leerResumen`.
- `estructuraCompleta(Connection)`: comprueba todas las tablas de `TABLAS_APLICACION` y las columnas
  de `COLUMNAS_APLICACION`. Devuelve `false` si falta alguna. Solo se consulta cuando
  `userVersion > Migrations.ultimaVersion()`.

Con `userVersion <= ultimaVersion()` no se comprueba la estructura completa: las tablas y columnas
de migraciones posteriores es normal que no existan, y `Database.getConnection()` o una migración
local las creará al restaurar. Esto elimina la incoherencia anterior (falta una columna → tolerable;
falta una tabla → siempre rechazo).

### D2. `resetConnection` primero en el rollback

En `restaurarEnEmpresaActiva`, el `catch` ejecuta como primera instrucción
`Database.resetConnection()` para cerrar el handle de SQLite sobre `facturas.db`, y solo después hace
el `Files.copy` del rescate, `borrarDiario`, `resetConnection`/`getConnection` (reabrir) y lanza el
error original como causa. Orden correcto: cerrar conexión → copiar rescate → borrar diario → reabrir
conexión → propagar.

### D3. Limpieza en `restaurarComoEmpresaNueva`

Todo lo posterior a `crearEmpresa(nombre)` (obtener `dbPathDe`, `Files.copy`, `borrarDiario`,
migración local) se envuelve en `try/catch`; ante cualquier `Throwable` se llama a
`EmpresaManager.eliminarEmpresa(nueva.slug())` y se rela nza. No es la empresa activa (la activa
sigue intacta), así que `eliminarEmpresa` no se queja. Si montar el test resulta forzado, se deja
documentado aquí (ver tasks 8.x) en vez de escribir un test artificial.

### D4. Excepción de la API

`restaurarEnEmpresaActiva` lanza `IOException` (además de `ValidationException` de `leerResumen`) en
lugar de `Exception` genérica; se ajusta la firma pública y el manejador del controller no cambia de
comportamiento porque ya capturaba `Exception`.

### D5. Nombre visible de la empresa activa

`BackupController.restaurar()` resuelve el nombre con `EmpresaManager.listarEmpresas()` (busca el
slug en la lista y usa su `nombre`), con el slug como respaldo, igual que hace el resto de la
aplicación.

### D6. Regla NIF aplanada

`aplicarReglaNif` se reduce a: empresa activa sin NIF y sin facturas → permitir; NIFs iguales →
permitir; cualquier otro caso → solo crear empresa nueva. La rama muerta
(`nifActiva.isEmpty() && facturasActivas == 0` dentro del `else if` ya cubierto por el `if`) se
elimina.

### D7. Quedarse en Copias al responder «NO»

En el flujo de crear empresa nueva, si el usuario responde «NO» a cambiar de empresa, se muestra de
nuevo la pantalla de Copias (`Backup.fxml`) en lugar del menú principal: la empresa activa sigue
intacta y el usuario sigue donde estaba.

### D8. `borrarDiario` derivado de la constante

`borrarDiario(Path carpeta)` construye `carpeta.resolve(Database.dbPath().getFileName() + "-wal")` y
`... + "-shm"`, evitando el `"facturas.db"` hardcodeado que duplicaba la constante `DB_FILE`.

## Tests

Al `service/BackupServiceTest.java`:

- `restaurarCopiaDeEsquemaAnteriorSeMigra`: copia buena a la que se le hace `DROP TABLE
  numero_disponible` y `PRAGMA user_version = 6`; restaura sobre la activa; se acepta y tras
  restaurar la base activa vuelve a tener `numero_disponible` y `user_version ==
  Migrations.ultimaVersion()`.
- `rechazaCopiaSinLasTablasNucleo`: SQLite válido con `user_version = 1` y sin `factura` →
  `ValidationException`. Cubre que la comprobación núcleo sigue viva.
- `restaurarComoEmpresaNuevaNoDejaBasuraSiFalla`: provoca un fallo tras `crearEmpresa` y comprueba
  que no queda la carpeta ni la entrada en `listarEmpresas()`. Si montarlo resulta forzado, se
  documenta aquí.

Los tests `rechazaCopiaSinLasTablasDeLaAplicacion`, `aceptaEsquemaPosteriorConLasMismasTablas` y
`rechazaEsquemaPosteriorConTablasDistintas` deben seguir pasando; `rechazaCopiaSinLasTablasDeLaAplicacion` se
ajusta para que deje caer una **tabla núcleo** (p. ej. `factura`) en lugar de `numero_disponible`, que
ahora es legítimo que falte en una copia antigua.

## Nota: `restaurarComoEmpresaNuevaNoDejaBasuraSiFalla`

Se decide documentar en vez de montar el test. La limpieza asociada (D3) entra en juego solo cuando
`crearEmpresa` ya ha creado la carpeta y la entrada del catálogo y un paso posterior (`Files.copy`,
`Migrations.migrate` del destino) falla con una copia válida. Provocar ese fallo de forma fiable
exigiría una copia contrivada que rompiera una migración durante el `migrate` del destino, lo que
depende de scripts de migración específicos y no de dinero del servicio. La cobertura del resto del
flujo y el `catch` de limpieza (revisado con código) se consideran suficientes; la tarea 6.4 queda
así documentada.
