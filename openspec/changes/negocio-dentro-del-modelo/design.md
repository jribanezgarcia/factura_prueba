## Context

Estado esperado a la entrada: `crear-tablas-sin-versiones` y `mvc-como-biblioteca8` archivados. Números de línea orientativos (de 16/09/2026).

**`Sesion`** (`modelo/negocio/Sesion.java`, `final`, constructor privado): `private static String empresaSlug`, `private static LocalDate fechaTrabajo`; `static inicializar(slug, fecha)`, `static empresaSlug()`, `static fechaTrabajo()`, `static reiniciar()` (solo la usan los tests).

**Quién usa `Sesion`**:
- `Empresas.conectar` (`Sesion.inicializar`) y `Empresas.eliminarEmpresa` (`Sesion.empresaSlug()`).
- `Reloj.fechaTrabajo()` (`Sesion.fechaTrabajo()`).
- `ConfiguracionController`: `Sesion.empresaSlug()` ×2 y `Sesion.fechaTrabajo()` ×1.
- `CopiaSeguridadController`: `Sesion.empresaSlug()` ×1 y `Sesion.fechaTrabajo()` ×1.
- Tests: `EmpresasTest` (`reiniciar`, `empresaSlug` ×3), `CopiaSeguridadTest` (`reiniciar`, `empresaSlug`), `CopiaSeguridadDAOTest` (`reiniciar`).

**`Empresas`** (`modelo/negocio/Empresas.java`, `final`, constructor privado, todo `static`): `listarEmpresas`, `crearEmpresa`, `conectar`, `recordarTema`, `eliminarEmpresa`, `registrarNombre`, `slugDe` y los privados `claveNombre`, `archivoCatalogo`, `cargarCatalogo`, `guardarCatalogo`, `borrarRecursivo`. Record anidado `EmpresaInfo`. Usa `Conexion` y `PreferenciasGlobales` (siguen `static`).

**Quién usa `Empresas`** (sin contar el tipo `Empresas.EmpresaInfo`, que no cambia):
- `ArranqueController`: `listarEmpresas`, `crearEmpresa`, `conectar`.
- `ConfiguracionController`: `listarEmpresas` ×2, `crearEmpresa`, `conectar`, `eliminarEmpresa`.
- `CopiaSeguridadController`: `listarEmpresas`, `conectar`.
- `fichero/CopiaSeguridad`: `crearEmpresa`, `eliminarEmpresa`, `recordarTema`.
- `modelo/negocio/sqlite/CargarDemo`: `crearEmpresa`, `registrarNombre` (en `cargar()`), `eliminarEmpresa` (en `main`).
- `PreparacionDatos.cargarDemoSiNoHayEmpresas`: `listarEmpresas` y `CargarDemo.cargar()`; la llama `Controlador.cargarDemostracion()`.
- Tests: `EmpresasTest` (~34 llamadas), `CopiaSeguridadTest` (5), `CopiaSeguridadDAOTest` (2), `PreparacionDatosTest` (6, más `cargarDemoSiNoHayEmpresas()` ×4).

**`Modelo`**: crea `Reloj(clock)` y `CopiaSeguridad(copiaSeguridadDAO, facturaDAO, clock)`.

**Decisión del usuario (16/09/2026)**: solo `Empresas` y `Sesion` pasan al `Modelo`; `Calculos` y `PreferenciasGlobales` siguen `static` como herramientas.

## Goals / Non-Goals

**Goals:** `Empresas` y `Sesion` como objetos que da el `Modelo`; ningún campo `static` con datos en el negocio; mismo comportamiento.

**Non-Goals:**
- `Calculos` y `PreferenciasGlobales` no cambian.
- `Empresas.EmpresaInfo` sigue anidado (change `clases-independientes`).
- No se arreglan el stream y el `Comparator` de `listarEmpresas`, los ternarios de `listarEmpresas`/`slugDe` ni el `var` de `borrarRecursivo` (change `java-clasico`).

## Decisions

### D1. `Sesion`

```java
/**
 * Sesión de trabajo: la empresa activa y la fecha de trabajo elegidas en la
 * ventana de arranque. La fecha de trabajo es la inicial de las facturas nuevas.
 */
public class Sesion {

    private String empresaSlug;
    private LocalDate fechaTrabajo;

    /** Guardamos la empresa y la fecha con las que se ha entrado. */
    public void inicializar(String slug, LocalDate fecha) {
        empresaSlug = slug;
        fechaTrabajo = fecha;
    }

    public String getEmpresaSlug() {
        return empresaSlug;
    }

    public LocalDate getFechaTrabajo() {
        return fechaTrabajo;
    }
}
```

`reiniciar()` desaparece: cada test crea su propia `Sesion`.

### D2. `Empresas`

- `public final class Empresas` → `public class Empresas`; quitar el constructor privado y añadir:

  ```java
  private Sesion sesion;

  public Empresas(Sesion sesion) {
      this.sesion = sesion;
  }
  ```

- Quitar `static` de **todos** los métodos, públicos y privados. Las constantes (`CATALOGO`) siguen `private static final`.
- `conectar`: `Sesion.inicializar(slug, fecha)` → `sesion.inicializar(slug, fecha)`.
- `eliminarEmpresa`: `Sesion.empresaSlug()` → `sesion.getEmpresaSlug()`.
- Javadoc de clase: añadir al final «La empresa activa se guarda en la {@link Sesion} que recibimos por constructor.».

### D3. `Reloj`

```java
private final Clock clock;
private final Sesion sesion;

public Reloj(Clock clock, Sesion sesion) {
    this.clock = clock;
    this.sesion = sesion;
}
```

`fechaTrabajo()`: `Sesion.fechaTrabajo()` → `sesion.getFechaTrabajo()`. La línea `return f != null ? f : hoy();` se toca, así que pasa a `if / else`.

### D4. `Modelo`

- Campos nuevos `private final Sesion sesion;` y `private final Empresas empresas;`, creados **los primeros** en `Modelo(Clock)`:

  ```java
  sesion = new Sesion();
  empresas = new Empresas(sesion);
  reloj = new Reloj(clock, sesion);
  ```

- `copiaSeguridad = new CopiaSeguridad(copiaSeguridadDAO, facturaDAO, clock, empresas);`.
- Getters `getSesion()` y `getEmpresas()` junto a los demás.

### D5. `CopiaSeguridad`

- Campo `private final Empresas empresas;` y parámetro al final del constructor: `CopiaSeguridad(CopiaSeguridadDAO copiaSeguridadDAO, FacturaDAO facturaDAO, Clock clock, Empresas empresas)`.
- `Empresas.crearEmpresa`, `Empresas.eliminarEmpresa`, `Empresas.recordarTema` → `empresas.…`.

### D6. `CargarDemo` y `PreparacionDatos`

- `CargarDemo.cargar()` → `cargar(Empresas empresas)`, usando `empresas.crearEmpresa(...)` y `empresas.registrarNombre(...)`.
- `CargarDemo.main`: al principio `Empresas empresas = new Empresas(new Sesion());`, `Empresas.eliminarEmpresa(SLUG)` → `empresas.eliminarEmpresa(SLUG)` y `cargar()` → `cargar(empresas)`. `cargar_demo.bat` no cambia.
- `PreparacionDatos.cargarDemoSiNoHayEmpresas()` → `cargarDemoSiNoHayEmpresas(Empresas empresas)`, con `empresas.listarEmpresas()` y `CargarDemo.cargar(empresas)`. Javadoc: añadir `@param empresas las empresas del modelo`.
- `Controlador.cargarDemostracion()`: `return PreparacionDatos.cargarDemoSiNoHayEmpresas(modelo.getEmpresas());`.

### D7. Pantallas

Sustituir, **sin tocar** las apariciones del tipo `Empresas.EmpresaInfo`:

- `Empresas.metodo(` → `Vista.getInstancia().getControlador().getModelo().getEmpresas().metodo(`
- `Sesion.empresaSlug()` → `Vista.getInstancia().getControlador().getModelo().getSesion().getEmpresaSlug()`
- `Sesion.fechaTrabajo()` → `Vista.getInstancia().getControlador().getModelo().getSesion().getFechaTrabajo()`

En `ArranqueController`, `ConfiguracionController` y `CopiaSeguridadController`. Quitar el import de `Sesion` donde deje de usarse (el de `Empresas` se queda por `EmpresaInfo`).

### D8. `AGENTS.md`

En la línea de `static` de «Arquitectura», la lista de ejemplos pasa a: «(`Conexion`, `Dialogos`, `Formatos`, `Calculos`, `PreferenciasGlobales`, validadores…)».

### D9. Tests

- Donde había `Sesion.reiniciar();` en un `@BeforeEach`: campos del test

  ```java
  private Sesion sesion;
  private Empresas empresas;
  ```

  y en el `@BeforeEach`, `sesion = new Sesion(); empresas = new Empresas(sesion);` en lugar de `Sesion.reiniciar();`.
- `Empresas.metodo(` → `empresas.metodo(`; `Sesion.empresaSlug()` → `sesion.getEmpresaSlug()`.
- **`EmpresasTest`**: lo anterior; `slugDe` también con `empresas.slugDe(...)`.
- **`CopiaSeguridadTest`**: lo anterior y `new CopiaSeguridad(new CopiaSeguridadDAO(), new FacturaDAO(), Clock.systemDefaultZone(), empresas)`.
- **`CopiaSeguridadDAOTest`**: lo anterior.
- **`PreparacionDatosTest`**: campo `private Empresas empresas;`, `empresas = new Empresas(new Sesion());` en el `@BeforeEach`, `PreparacionDatos.cargarDemoSiNoHayEmpresas()` → `cargarDemoSiNoHayEmpresas(empresas)` y `Empresas.x(` → `empresas.x(`.
- Tipos escritos, sin `var` en las líneas nuevas. Los streams de estos tests (`listarEmpresas().stream()…`) no se tocan en este change.

## Risks / Trade-offs

- **Dos `Sesion` distintas**: si algo creara sus propias `Empresas` dentro de la aplicación, conectaría sin actualizar la sesión del `Modelo` → en la aplicación solo el `Modelo` crea `Empresas`; `CargarDemo.main` es un programa aparte y los tests crean las suyas.
- **Las pantallas escriben llamadas largas** → aceptado por el usuario.
