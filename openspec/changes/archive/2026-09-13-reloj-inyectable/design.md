## Context

Lecturas del reloj del sistema a 13/09/2026 (los números de línea son orientativos; manda buscar `now()`):

| Sitio | Uso |
|---|---|
| `service/NumeroService.java:51` | `siguienteCorrelativo(Serie)` usa `LocalDate.now()` como fecha |
| `service/NumeroService.java:62, 92, 136` | `fecha != null ? fecha.getYear() : LocalDate.now().getYear()` |
| `service/NumeroService.java:132` | `correlativoOcupadoPorActiva(Serie,int)` usa `LocalDate.now()` |
| `service/VersionadoService.java:60, 114` | `v.setFechaGuardado(LocalDateTime.now())` en `crearVersion` y `sobrescribirVersion` |
| `service/FacturaService.java:248` | año de respaldo en `borrarFactura` |
| `service/BackupService.java:79` | sello de hora del nombre de la copia |
| `repository/SerieRepository.java:75` | `inicializarSiguiente` siembra `serie_siguiente` del año en curso |
| `ui/EditorController.java:401, 1193` | `Sesion.fechaTrabajo() != null ? Sesion.fechaTrabajo() : LocalDate.now()` |
| `ui/MenuController.java:55` | la misma regla |
| `ui/ConfiguracionController.java:746-747` | la misma regla, quedándose con el año |
| `ui/GenerarFacturasMensualesController.java:267` | año actual para el spinner |
| `ui/ArranqueController.java:76, 94, 95, 171, 172` | ejercicio y fecha por defecto en la pantalla de arranque |

Constructores afectados y quién los llama: `Servicios` (en `Main.entrarEnMenu:106` y en los tests de `ui/`), y `NumeroService`, `VersionadoService`, `FacturaService`, `BackupService` (en `Servicios` y en `EstadoServiceTest`, `FacturacionMensualServiceTest`, `FacturaServiceTest`, `HistorialServiceTest`, `NumeroServiceTest`, `BackupServiceTest`). `SerieRepository.insertar(Serie)` se llama desde `SerieService` y desde seis tests.

## Goals / Non-Goals

**Goals:** que ninguna clase de `service/` ni `repository/` llame a `now()` sin reloj; que la regla de la fecha de trabajo esté en un solo sitio; que un test pueda fijar la fecha.

**Non-Goals:** convertir `Sesion` en un objeto inyectado; guardar la hora con zona u offset en la base de datos (irá con el registro de VeriFactu); tocar `ArranqueController`; cambiar `throws SQLException`; cambiar ninguna fecha o número que vea el usuario.

## Decisions

### D1. `java.time.Clock` en los servicios

Los servicios reciben el `Clock` estándar de Java como **último** parámetro del constructor y lo guardan en un campo `private final Clock clock`. Todas las lecturas pasan a `LocalDate.now(clock)` y `LocalDateTime.now(clock)`.

```java
public NumeroService(SerieRepository serieRepository, NumeroDisponibleRepository numeroDisponibleRepository, Clock clock)
public VersionadoService(VersionRepository versionRepository, LineaRepository lineaRepository, Clock clock)
public FacturaService(FacturaRepository ..., NumeroDisponibleRepository numeroDisponibleRepository, Clock clock)
public BackupService(Clock clock)
```

No se dejan los constructores antiguos como sobrecarga: un constructor que coge el reloj del sistema por su cuenta es justo la dependencia escondida que se quiere quitar. Los tests existentes pasan `Clock.systemDefaultZone()`, con lo que siguen comprobando lo mismo que hoy.

Descartado: una interfaz propia tipo `ProveedorFecha`. `Clock` ya existe, es inmutable, tiene `Clock.fixed(...)` para tests y lo entiende cualquier desarrollador Java.

### D2. El año del contador de una serie nueva lo decide el servicio

`SerieRepository.insertar(Serie s)` pasa a `insertar(Serie s, int anioContador)`, y `inicializarSiguiente` usa ese año en lugar de `LocalDate.now().getYear()`. `SerieService` recibe el `Clock` (tercer parámetro) y llama a `serieRepository.insertar(s, LocalDate.now(clock).getYear())`. Así el repositorio no conoce la fecha actual. En los tests que insertan series directamente en el repositorio se pasa `LocalDate.now().getYear()`, que es lo que hacía el repositorio.

### D3. `Reloj` para la interfaz

La interfaz no necesita un `Clock` en crudo, sino la regla de negocio de la fecha de trabajo. Nueva clase en `service/`:

```java
public class Reloj {

    private final Clock clock;

    public Reloj(Clock clock) {
        this.clock = clock;
    }

    public LocalDate hoy() {
        return LocalDate.now(clock);
    }

    public LocalDateTime ahora() {
        return LocalDateTime.now(clock);
    }

    public LocalDate fechaTrabajo() {
        LocalDate f = Sesion.fechaTrabajo();
        return f != null ? f : hoy();
    }
}
```

Sustituciones en la interfaz:

| Antes | Después |
|---|---|
| `EditorController.cargarFechaInicial` y `crearRectificativa`: `Sesion.fechaTrabajo() != null ? Sesion.fechaTrabajo() : LocalDate.now()` | `servicios.reloj.fechaTrabajo()` |
| `MenuController.alIniciar`: la misma expresión | `servicios.reloj.fechaTrabajo()` |
| `ConfiguracionController.anioTrabajo`: cuerpo de dos líneas | `return servicios.reloj.fechaTrabajo().getYear();` |
| `GenerarFacturasMensualesController.configurarSpinners`: `LocalDate.now().getYear()` | `servicios.reloj.hoy().getYear()` |

Hay que comprobar que en `GenerarFacturasMensualesController` `servicios` ya está asignado cuando se llama a `configurarSpinners`; si no lo está, mover la llamada a después de `setServicios`, sin cambiar lo que se ve.

`Sesion` no se toca: sigue siendo quien guarda la fecha elegida en el arranque. `Reloj` solo centraliza la lectura.

### D4. `Servicios(Clock)` como raíz

```java
public final Reloj reloj;

public Servicios() throws SQLException {
    this(Clock.systemDefaultZone());
}

public Servicios(Clock clock) throws SQLException {
    Database.getConnection();
    ... repositorios como hoy ...
    reloj = new Reloj(clock);
    series = new SerieService(serieRepository, facturaRepository, clock);
    numeros = new NumeroService(serieRepository, numeroDisponibleRepository, clock);
    versionado = new VersionadoService(versionRepository, lineaRepository, clock);
    factura = new FacturaService(..., numeroDisponibleRepository, clock);
    backup = new BackupService(clock);
    ... resto igual ...
}
```

El constructor sin argumentos se mantiene porque `Main` y los tests de `ui/` lo usan. Es aceptable aquí: `Servicios` es la raíz de composición, el único sitio donde tiene sentido elegir el reloj real.

### D5. `ArranqueController` queda fuera

La pantalla de arranque se muestra con `new Navegador(stage, null)`: todavía no hay `Servicios`, ni empresa, ni sesión. Sus `LocalDate.now()` solo proponen el ejercicio y la fecha de trabajo por defecto; no numeran ni sellan nada. Inyectarle un reloj obligaría a cambiar cómo arranca `Main` y no aporta nada a la numeración ni a VeriFactu.

### D6. Test con reloj fijo

En `NumeroServiceTest`, un test nuevo construye `NumeroService` con `Clock.fixed(Instant.parse("2031-06-15T10:00:00Z"), ZoneId.of("Europe/Madrid"))`, siembra `serie_siguiente` para 2031 con un valor conocido y comprueba que `siguienteCorrelativo(serie)` (la sobrecarga sin fecha) devuelve ese valor y no el del año real. Otro test con `VersionadoService` y el mismo reloj comprueba que `crearVersion` sella `fechaGuardado` con `2031-06-15T12:00`.

## Risks / Trade-offs

- **Riesgo bajo:** mismas lecturas de fecha, solo cambia de dónde salen.
- **Descuido posible:** dejar algún `now()` sin reloj en `service/` o `repository/`. La tarea 5.1 lo comprueba con una búsqueda.
- **Descuido posible:** construir cada servicio con un `Clock` distinto. Se crea uno en la raíz y se pasa el mismo a todos.
- **Trade-off aceptado:** `Sesion` sigue siendo estado global estático. Convertirla en objeto inyectado toca el arranque y el cambio de empresa; queda para cuando se trabaje el multiempresa.
- **Ajuste de tests:** unas quince llamadas a constructores y seis a `SerieRepository.insertar` cambian en los tests. Es mecánico y no cambia lo que comprueban.
