## Why

Cada empresa tiene su propia base de datos SQLite y el programa crea sus tablas al crear la empresa. Hoy lo hace con un sistema de **migraciones**: guarda un número de versión dentro de cada base (`PRAGMA user_version`) y ejecuta los scripts numerados que falten (`001_baseline.sql`, `002_…`). Solo existe `001` y el programa está en beta: las tablas son siempre las mismas y los datos se van a reiniciar. El número de versión solo añade código difícil de explicar (la clase `Migraciones`, avisos de «versión anterior/posterior» al restaurar una copia) sin aportar nada.

## What Changes

- **Crear tablas, sin versiones**: la clase `Migraciones` desaparece. `Conexion` crea las tablas cuando abre o crea una base que todavía no las tiene, con el mismo script, que pasa a llamarse `db/crear_tablas.sql`.
- **Sin número de versión**: ninguna base guarda ni lee `PRAGMA user_version`.
- **Restaurar copia**: se sigue comprobando que la copia tiene todas las tablas y columnas; desaparecen la lectura de la versión, el rechazo por «versión de esquema no válida» y la frase «versión anterior/posterior» en los avisos y en el resumen.
- **Tests**: `MigracionesTest` desaparece; sus dos comprobaciones útiles (la base nueva tiene todas las tablas y crearla dos veces no duplica filas) pasan a `ConexionTest`.

## Capabilities

### New Capabilities

Ninguna.

### Modified Capabilities

- `invoicing`: el requisito «Copia de seguridad» deja de hablar de versión de esquema y migraciones; la copia se acepta o rechaza solo por sus tablas y columnas.

## Impact

- Código: `modelo/negocio/sqlite/Conexion`, `modelo/negocio/sqlite/CopiaSeguridadDAO`, `modelo/negocio/sqlite/CargarDemo` (Javadoc), `modelo/negocio/Empresas` (Javadoc), `fichero/CopiaSeguridad`, `vista/controlador/CopiaSeguridadController`. Se borra `modelo/negocio/sqlite/Migraciones`.
- Recursos: `src/main/resources/db/migrations/001_baseline.sql` → `src/main/resources/db/crear_tablas.sql` (mismo contenido); la carpeta `migrations` desaparece.
- Documentación: `docs/tecnico.md` (diagrama de capas y tabla de decisiones).
- Tests: se borra `MigracionesTest`; cambian `ConexionTest`, `EmpresasTest` y `CopiaSeguridadTest`.
- No hay migración de datos: el programa está en desarrollo y los datos se van a reiniciar. Las bases ya creadas siguen funcionando porque ya tienen sus tablas.
- Fuera: pasar `ResumenCopia` a clase normal (change `clases-independientes`) y quitar la `Task` de restaurar (change `sin-clases-anonimas-ni-hilos`).
