## Why

Los controladores de la interfaz leen y escriben en la base de datos a través de los repositorios, sin pasar por ningún servicio. `Servicios` publica los diez repositorios como campos públicos, y `EditorController`, `ConfiguracionController`, `ClientesController`, `HistoricoController`, `GenerarFacturasMensualesController`, `MenuController` y `BackupController` llaman directamente a `servicios.clientes`, `servicios.series`, `servicios.ivas`, `servicios.retenciones`, `servicios.config` y `servicios.facturas`.

Así la interfaz queda atada a la capa de datos. Cualquier cambio posterior (cambiar de motor de base de datos, introducir el estado borrador o un punto único de emisión para VeriFactu) obligaría a tocar los controladores uno por uno. Es el primer paso del orden acordado tras la auditoría de arquitectura.

## What Changes

- Cinco servicios nuevos en `service/`: `ClienteService`, `SerieService`, `IvaService`, `RetencionService` y `ConfigService`. Cada uno recibe sus repositorios por constructor y ofrece las operaciones que hoy usa la interfaz.
- `Servicios` deja de publicar repositorios: los crea como variables locales del constructor, se los pasa a los servicios y publica solo servicios.
- Todos los controladores y los tests que usaban `servicios.<repositorio>` pasan a usar el servicio equivalente.
- Ningún cambio de comportamiento: mismas consultas, mismos mensajes, mismas pantallas. Los métodos siguen declarando `throws SQLException`; quitarlo es un change posterior.

## Capabilities

### New Capabilities

Ninguna.

### Modified Capabilities

Ninguna: `skip_specs: true`. Es una reorganización interna; ningún requisito de `openspec/specs/invoicing/spec.md` describe cómo se conectan las capas.

## Impact

- Nuevos: `src/main/java/com/alcazaba/facturacion/service/{ClienteService,SerieService,IvaService,RetencionService,ConfigService}.java`.
- `src/main/java/com/alcazaba/facturacion/service/Servicios.java`.
- `src/main/java/com/alcazaba/facturacion/ui/`: `EditorController`, `ConfiguracionController`, `ClientesController`, `HistoricoController`, `GenerarFacturasMensualesController`, `MenuController`, `BackupController`.
- `src/test/java/com/alcazaba/facturacion/ui/`: `EditorIvaInactivoTest`, `ClientesNifValidationTest`.
- Sin cambios en repositorios, modelo, base de datos, FXML ni CSS.
