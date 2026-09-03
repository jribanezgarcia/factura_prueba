## Why

`EmpresaManager.crearEmpresa()` no solo crea la empresa: la **activa** en silencio. Al llamarla desde `ConfiguracionController.nuevaEmpresa()` con una empresa ya en uso, la aplicación cambia de base de datos por debajo y deja un estado incoherente: la pantalla sigue mostrando los datos de la empresa anterior, pero todo lo que se guarda (configuración, histórico, editor) escribe en la base de datos de la empresa nueva vacía. Además cambia la última empresa recordada, de modo que el siguiente arranque preselecciona la nueva.

## What Changes

- `crearEmpresa` deja de tocar el estado global (no activa la empresa nueva, no cierra la conexión en curso, no cambia la última empresa ni la sesión). Crea la base de datos de la empresa nueva sobre una conexión JDBC local y temporal, la migra ahí y la cierra, sin que la empresa activa se entere.
- Se añade un accesor en `Database` para construir la ruta de la base de una empresa sin activarla (`dbPathDe(slug)`).
- En Configuración, al crear una empresa desde la pestaña Empresas, se ofrece al usuario cambiar a la empresa nueva; si lo rechaza, se queda en su empresa actual con la conexión intacta.
- `conectar(slug, fecha)` queda como la única puerta para cambiar de empresa.
- Cambiar de empresa SHALL ser siempre una acción explícita del usuario; crear una empresa desde Configuración SHALL NOT cambiar la empresa activa, ni la conexión en curso, ni la última empresa recordada.
- **BREAKING**: Ninguno del esquema de base de datos ni de la firma pública de `crearEmpresa` ni de `conectar`.

## Capabilities

### New Capabilities
- (ninguna)

### Modified Capabilities
- `invoicing`: cambia el comportamiento del requisito «Gestión de empresas» (crear desde Configuración ya no activa la empresa nueva).

## Impact

- `db/Database.java`: nuevo accesor `dbPathDe(String slug)`.
- `service/EmpresaManager.java`: `crearEmpresa` deja de activar la empresa; crea la base con una conexión local.
- `ui/ConfiguracionController.java`: `nuevaEmpresa()` ofrece el cambio en lugar de imponerlo.
- `service/EmpresaManagerTest.java`: se reescriben los 4 tests que dependían del efecto secundario y se añaden 3 tests nuevos que fijan el arreglo.
- Se revisan `Main.java` y `ui/ArranqueController.java` pero no se modifican.
