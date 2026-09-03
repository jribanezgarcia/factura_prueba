## Why

La restauración de copias implementada en `2026-09-03-restaurar-copia-seguridad` tiene dos fallos
reproducidos que pueden volverse críticos:

1. **Una copia válida de un esquema anterior se rechaza.** `verificarEstructura` exige siempre la
   presencia de todas las tablas de la aplicación sin mirar la versión de esquema de la copia.
   `serie_siguiente` la crea la migración 004, `tipo_retencion` la 005 y `numero_disponible` la 007,
   así que una copia hecha con una versión anterior de la aplicación no las tiene y se rechaza, en
   lugar de aceptarse y migrarse al reconectar. Es una bomba de relojería: el día que una migración
   nueva cree una tabla, dejarán de poder restaurarse todas las copias existentes.

2. **El rollback puede fallar y ocultar el error original.** En `restaurarEnEmpresaActiva`, el
   `catch` copia el rescate antes de cerrar la conexión. `Database.getConnection()` asigna el campo
   `connection` y después migra; si la migración falla se entra al `catch` con la conexión abierta
   sobre `facturas.db` y en Windows el archivo no puede sobrescribirse mientras hay una conexión
   SQLite viva, con lo que el rollback revienta, se pierde el error original y la base activa se
   queda a medio migrar.

## What Changes

- `BackupService` separa las comprobaciones de estructura en dos conceptos:
  - **Tablas núcleo** (migración `001_init.sql`): `cliente`, `serie`, `tipo_iva`, `factura`,
    `factura_version`, `factura_linea`, `empresa`, `preferencias`. Se exigen siempre; si falta alguna
    → `ValidationException`. Son lo que distingue una base de esta aplicación de un SQLite cualquiera.
  - **Estructura completa** (todas las tablas y columnas conocidas). Solo se exige cuando la versión
    de esquema de la copia es posterior a `Migrations.ultimaVersion()`, el único caso que no puede
    resolverse migrando. Si falta algo ahí → `ValidationException` con las dos versiones.
  - Con `userVersion <= ultimaVersion()`, que falten tablas o columnas de migraciones posteriores es
    normal: no se rechaza nada, se restaura y `Database.getConnection()` la pone al día.
  - `verificarEstructura` se renombra a `comprobarTablasNucleo(Connection)` y `estructuraCompleta(Connection)`.
- `restaurarEnEmpresaActiva`: `Database.resetConnection()` pasa a ser la primera línea del `catch`,
  antes de cualquier `Files.copy`. Orden: cerrar conexión → copiar rescate → borrar diario → reabrir
  conexión → propagar el error.
- `restaurarComoEmpresaNueva`: si falla después de `crearEmpresa`, se llama a
  `EmpresaManager.eliminarEmpresa(slug)` para no dejar basura (carpeta + entrada en catálogo con base
  vacía o a medias).
- `restaurarEnEmpresaActiva` deja de lanzar `Exception` genérica y usa `IOException` (o excepción
  propia del servicio); se ajustan las firmas.
- `BackupController.restaurar()` muestra el **nombre visible** de la empresa activa (con
  `EmpresaManager.listarEmpresas()`, el slug como respaldo) en lugar del slug.
- `BackupController.aplicarReglaNif` se aplana y se elimina la rama muerta.
- Al crear empresa nueva y responder **NO** al cambio, la aplicación se queda en la pantalla de
  copias (su empresa activa sigue intacta) en lugar de ir al menú principal.
- `borrarDiario` deriva los nombres `-wal`/`-shm` de `Database.dbPath().getFileName()` en lugar de
  hardcodear `facturas.db`.

## Capabilities

### New Capabilities
- (ninguna)

### Modified Capabilities
- `invoicing`: se precisa el requisito «Copia de seguridad» para aclarar que una copia de una versión
  de esquema anterior se acepta y se migra al restaurarla, y que únicamente las tablas fundamentales
  se exigen siempre.

## Impact

- `service/BackupService.java`: separación de comprobaciones de estructura, rollback con
  `resetConnection` primero, limpieza en `restaurarComoEmpresaNueva`, excepción no genérica,
  `borrarDiario` derivado de `DB_FILE`.
- `ui/BackupController.java`: nombre visible de la empresa activa, `aplicarReglaNif` aplanado, no ir al
  menú al responder «NO» al cambio de empresa.
- `service/BackupServiceTest.java`: tests nuevos `restaurarCopiaDeEsquemaAnteriorSeMigra`,
  `rechazaCopiaSinLasTablasNucleo` y (si se puede montar) `restaurarComoEmpresaNuevaNoDejaBasuraSiFalla`.
- No cambia el esquema de base de datos ni ninguna migración.
- `Database.java` no se toca (salvo consulta de constantes ya existentes).
