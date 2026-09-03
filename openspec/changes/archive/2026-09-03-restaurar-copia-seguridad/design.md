## Context

`BackupService.crearBackup()` ejecuta `VACUUM INTO` a una carpeta elegida y genera `facturas_AAAAMMDD_HHMMSS.db`. La pantalla de Backup solo crea copias; no hay forma de recuperarlas desde la aplicación. La conexión de `Database` es estática y se abre una vez; Windows mantiene el handle de SQLite, así que sustituir el archivo a mano falla o corrompe. El nombre `facturas.db` es una constante privada. Un diario `-wal`/`-shm` huérfano queda en la carpeta. `crearBackup` usa timestamp de segundo y `VACUUM INTO` no sobrescribe, así que dos copias en el mismo segundo fallan. Ver proposal.md - Why.

## Goals / Non-Goals

**Goals:**
- Restaurar una copia sobre la empresa activa con copia de rescate automática y rollback si falla.
- Restaurar una copia creando una empresa nueva sin tocar la activa.
- Validar integridad, tablas y compatibilidad de esquema antes de restaurar.
- Mostrar un resumen del contenido de la copia antes de restaurar.
- Arreglar la colisión de nombres de `crearBackup` con `rutaLibre`.
- Hacer `Migrations.userVersion(Connection)` pública para leer versiones de copias externas.

**Non-Goals:**
- No cambiar el esquema de base de datos ni ninguna migración.
- No tocar `Database.java`, `EmpresaManager.java` ni `Servicios.java`.
- No implementar conversión entre versiones de esquema.
- No incluir PDFs ni configuración en la copia (solo SQLite, como hasta ahora).

## Decisions

### D1. Conexión JDBC temporal para `leerResumen`

En lugar de reutilizar `Database.connection` (que apunta a la empresa activa y no se puede cerrar sin afectar al usuario), `leerResumen` abre su propia conexión JDBC con `DriverManager.getConnection("jdbc:sqlite:" + origen)` en un try-with-resources. Esto garantiza aislamiento total: la empresa activa no se entera de la lectura. La conexión se cierra automáticamente al terminar.

### D2. Comparación con la base activa

`leerResumen` rechaza como origen la propia base activa comparando `origen.toAbsolutePath().normalize()` con `Database.dbPath().toAbsolutePath().normalize()`. Esto previene un `Files.copy` de un archivo sobre sí mismo.

### D3. `rutaLibre` para nombres sin colisión

Extraer un método privado `rutaLibre(Path carpeta, String base)` que devuelva `carpeta/base.db` si está libre y, si no, pruebe `base_2.db`, `base_3.db`… Se usa desde `crearBackup` en lugar del `resolve` directo. Con esto, dos copias en el mismo segundo generan archivos distintos.

### D4. Copia de rescate en subcarpeta `copias_previas`

Antes de restaurar sobre la empresa activa, `restaurarEnEmpresaActiva` llama a `crearBackup(Database.dataDir().resolve("copias_previas"))`. La subcarpeta evita dejar `facturas_*.db` sueltos junto a la base activa y no interfiere con `Database.getEmpresasDisponibles()`, que solo mira subcarpetas de la raíz de datos.

### D5. Rollback en `restaurarEnEmpresaActiva`

Si algo falla entre el `Files.copy` del origen y la reconexión, se copia la copia de rescate de vuelta a `Database.dbPath()`, se borra el diario, se reconecta y se propaga el error con un mensaje claro. Sin esto, un fallo a mitad deja la base activa medio machacada.

### D6. `restaurarComoEmpresaNueva` con conexión local

`EmpresaManager.crearEmpresa(nombre)` crea la carpeta y una base migrada **sin activar nada**. Luego `Files.copy(origen, Database.dbPathDe(slug))` escribe la copia sobre esa base. La migración se ejecuta con una conexión JDBC temporal a `dbPathDe(slug)` y `Migrations.migrate(c)`. No se usa `Database.getConnection()` en ningún momento porque trabajarían sobre la empresa activa.

### D7. Regla de compatibilidad de esquema

- `userVersion <= Migrations.ultimaVersion()` → compatible sin más.
- `userVersion > Migrations.ultimaVersion()` → se permite solo si el backup contiene todas las tablas de la aplicación y, para cada una, todas las columnas que la aplicación conoce (comprobación con `PRAGMA table_info`). Columnas de más se toleran. Si falta algo, se bloquea con un mensaje que dice las dos versiones.

### D8. NIF como guardia de sobrescritura

El controller compara el NIF del backup con el de la empresa activa (normalizando: quitar espacios y guiones, pasar a mayúsculas). Si no coinciden, deshabilita «Reemplazar la empresa activa» y selecciona «Crear una empresa nueva». Excepción: si la empresa activa no tiene NIF y no tiene facturas, se considera vacía y sí se permite reemplazar.

### D9. Navegación post-restauración

Al terminar reemplazando la activa: `Dialogos.info()` con la ruta del rescate y `nav.mostrar(MenuPrincipal.fxml)`. Al terminar creando empresa nueva: `Dialogos.confirmar()` preguntando si quiere cambiar a ella; si sí → `conectar` + `nav.mostrar(MenuPrincipal.fxml)`. No se navega a `Arranque.fxml` porque su `onEntrar` solo lo asigna `Main.mostrarArranque()` y el botón "Entrar" queda sin navegación.

## Risks / Trade-offs

- [Base activa como origen] → Rechazada explícitamente en `leerResumen` con comparación normalizada. Mitigación: el usuario siempre debe elegir un archivo distinto.
- [Ventana entre resetConnection y reconexión] → Si el usuario intenta usar la app durante la restauración podría encontrar errores. Mitigación: la restauración es rápida (copia de fichero) y se ejecuta en un Task en hilo aparte.
- [Tests que encadenan copias en el mismo segundo] → `restaurarDejaCopiaDeRescateConElEstadoPrevio` crea dos copias seguidas; falla si `rutaLibre` no está hecho. Esto valida el fix de colisión.

## Migration Plan

Sin cambios de datos ni de esquema. Es un cambio de comportamiento de `BackupService` y de la UI de Backup. Rollback trivial revertiendo los cambios en `BackupService`, `BackupController`, `Backup.fxml` y `Migrations`.

## Open Questions

Ninguna.
