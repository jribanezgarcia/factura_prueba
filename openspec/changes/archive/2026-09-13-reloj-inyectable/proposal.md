## Why

La fecha y la hora actuales se leen directamente del sistema en capas que no deberían hacerlo. `NumeroService` llama a `LocalDate.now()` en cinco sitios para decidir el año de la numeración; `VersionadoService` sella `fecha_guardado` con `LocalDateTime.now()`; `FacturaService.borrarFactura` usa el año actual como respaldo; `BackupService` pone la hora en el nombre de la copia, y `SerieRepository` siembra el contador del año en curso con `LocalDate.now()`. En la interfaz, la regla «fecha de trabajo o, si no hay, hoy» está copiada a mano en `EditorController` (dos veces), `MenuController` y `ConfiguracionController`, y `GenerarFacturasMensualesController` toma el año de `LocalDate.now()`.

Con el reloj escondido no se puede fijar la fecha en un test, así que la numeración por año y el sellado de versiones solo se prueban el día en que se ejecuta la suite. Para VeriFactu la fecha y la hora de generación de cada registro tendrán que ser reproducibles, y ese requisito empieza aquí. Es el change 2 del orden acordado tras la auditoría de arquitectura; las excepciones de dominio, que iban en el mismo punto, se separan en un change propio para que este sea pequeño y revisable.

## What Changes

- Los servicios que leen la hora (`NumeroService`, `VersionadoService`, `FacturaService`, `BackupService`) reciben un `java.time.Clock` por constructor y dejan de llamar a `now()` sin argumentos.
- `SerieRepository.insertar` recibe el año del contador como parámetro; lo calcula `SerieService` con el reloj.
- Nueva clase `service/Reloj` con `hoy()`, `ahora()` y `fechaTrabajo()` (fecha de trabajo de la sesión o, si no hay, hoy). `Servicios` la publica como `reloj` y la interfaz la usa en lugar de repetir la regla.
- `Servicios` tiene un constructor `Servicios(Clock)` que monta todo con el reloj indicado; el constructor sin argumentos usa el reloj del sistema.
- Un test nuevo de `NumeroService` con reloj fijo comprueba que la numeración sin fecha usa el año del reloj.
- Ningún cambio de comportamiento visible.

## Capabilities

### New Capabilities

Ninguna.

### Modified Capabilities

Ninguna: `skip_specs: true`. «Fecha de trabajo» y «Numeración por series» no cambian: mismas fechas propuestas y mismos números.

## Impact

- Nuevo: `src/main/java/com/alcazaba/facturacion/service/Reloj.java`.
- `service/`: `Servicios`, `NumeroService`, `VersionadoService`, `FacturaService`, `BackupService`, `SerieService`.
- `repository/SerieRepository.java`.
- `ui/`: `EditorController`, `MenuController`, `ConfiguracionController`, `GenerarFacturasMensualesController`.
- Tests que construyen estos servicios o llaman a `SerieRepository.insertar` (`service/*ServiceTest`, `repository/SerieRepositoryTest`) y un test nuevo en `NumeroServiceTest`.
- Fuera: `ArranqueController` (se ejecuta antes de que existan `Servicios` y sesión), `Sesion` (sigue siendo estática), `throws SQLException`, esquema de base de datos.
