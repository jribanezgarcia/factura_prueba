## Why

La aplicación solo maneja una empresa (tabla `empresa` con `id = 1` y una única BD SQLite). El usuario quiere llevar la contabilidad de varias empresas (p. ej. «Comercial Alcazaba» y «Asesoría María Luisa Ibáñez») con datos completamente aislados, y poder trabajar en un ejercicio fiscal distinto del año natural (p. ej. entrar en 2025 para numerar facturas de ese año). Hoy no hay forma de elegir empresa ni de fijar el ejercicio de trabajo.

## What Changes

- **Multi-empresa por BD**: cada empresa tendrá su propia base de datos SQLite en `%APPDATA%\Facturacion\<slug>\facturas.db`. La migración funciona igual por BD (`PRAGMA user_version`).
- **Pantalla de arranque**: al abrir la aplicación se muestra una pantalla con el listado de empresas y un selector de fecha de trabajo. La última empresa utilizada queda preseleccionada.
- **Fecha de trabajo**: la fecha elegida al arrancar define el mes y el año que se usan para la numeración (formatos MES y ANIO) y es la fecha por defecto de las nuevas facturas. Solo se elige al arrancar.
- **Correlativo por año**: el correlativo de cada serie es independiente por ejercicio (reinicia en 1 en cada año). **BREAKING** sobre el comportamiento actual en que cada serie tiene un único correlativo global.
- **Migración de datos**: la BD actual del usuario (facturas de Comercial Alcazaba) se mueve a la primera empresa `comercial_alcazaba`.
- **Preferencias globales**: ventana, tema y «última empresa utilizada» se guardan de forma global (fuera de la BD de empresa), porque la tabla `preferencias` vive dentro de cada BD.
- **Nombre visible + slug**: cada empresa se identifica con un nombre visible y un slug interno para su carpeta.
- **Gestión de empresas**: nueva pestaña «Empresas» en Configuración para crear, cambiar o eliminar empresas.
- **Histórico global**: el histórico muestra todas las facturas de todos los años; solo la numeración de facturas nuevas respeta el ejercicio elegido.

## Capabilities

### New Capabilities

- (ninguna; la gestión de empresas y el ejercicio fiscal se integran en la capability existente `invoicing`)

### Modified Capabilities

- `invoicing`: el requisito «Fecha de trabajo» pasa a definirse en la pantalla de arranque junto con la selección de empresa; el requisito «Numeración por series» incorpora el correlativo por año; el requisito «Persistencia local» contempla una base de datos por empresa; el requisito «Configuración» incorpora la gestión de empresas.

## Impact

- `db/Database.java`: directorio de datos por empresa (`%APPDATA%/Facturacion/<slug>/`) y listado de empresas disponibles.
- Nueva clase de gestión de empresas (servicio): listar/crear/cambiar/eliminar y preferencia global `ultima_empresa`.
- `db/Migrations.java`: crear BD nueva migra desde cero (sin cambios en el mecanismo).
- Arranque (`Main` y controlador de arranque): pantalla de selección de empresa + fecha de trabajo; acceso a la gestión desde Configuración.
- `repository/SerieRepository.java`, `service/NumeroService.java` y modelo `Serie`: correlativo por año (consultas filtradas por año y contador por ejercicio).
- `repository/ConfigRepository.java` y preferencias globales fuera de la BD de empresa.
- `ui/ConfiguracionController.java` y `Configuracion.fxml`: nueva pestaña «Empresas».
- `repository/ConfigRepository.java`: datos de empresa se mantienen por BD (cada empresa tiene su propia configuración de empresa/cabecera/logo).
- Tests: nuevos tests de aislamiento entre empresas, correlativo por año y selector/fecha de trabajo.