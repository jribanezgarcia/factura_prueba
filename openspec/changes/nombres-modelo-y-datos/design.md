## Context

Parte del estado que deja `paquetes-en-espanol`: raíz `cabofactu`, clases con su nombre de siempre.

**Referencia de Biblioteca8:**

- `modelo.negocio.Libros`, `Usuarios`, `Prestamos`, `Autores`: plural, sin sufijo.
- `modelo.Modelo`: el objeto que tiene todo el negocio y usan las pantallas.
- `modelo.negocio.mysql.Conexion` con `establecerConexion()` y `cerrarConexion()`.
- `fichero.CopiaSeguridadXML`.

**Decisiones ya tomadas por el usuario (14/09/2026):** reglas en plural, datos con sufijo `DAO`, `Servicios` → `Modelo`, `BackupService` → `fichero.CopiaSeguridad`. No se vuelven a preguntar.

**Comprobaciones hechas:** ningún identificador del proyecto se llama ya `modelo`, `Modelo`, `Conexion`, `Facturas`, `Clientes`, `Series`, `Versiones`, `Estados`, `Empresas`, `Historial`, `Calculos`, `Numeracion`, `Configuracion`, `TiposIva` ni `CopiaSeguridad`: no hay choques. Solo aparecen dentro de textos y comentarios, que no se tocan.

Usos aproximados (main + test): `Database.getConnection` 91, `Database.resetConnection` 46, `Database.setDataDir` 31, variable `servicios` 242.

## Goals / Non-Goals

**Goals:** nombres de clase y API pública de modelo y datos en español, al estilo Biblioteca8; mismo comportamiento; historial de git conservado.

**Non-Goals:** cambiar textos visibles o mensajes de excepción; cambiar métodos de reglas o DAO que ya están en español; renombrar variables locales en inglés sueltas dentro de métodos; tocar `vista`, `pdf` o `utilidades` más allá de adaptar los usos; tocar `docs/`; cambiar esquema o SQL.

## Decisions

### D1. Reglas: `cabofactu.modelo.negocio`

| Hoy | Nuevo | Test hoy | Test nuevo |
|---|---|---|---|
| `FacturaService` | `Facturas` | `FacturaServiceTest` | `FacturasTest` |
| `ClienteService` | `Clientes` | — | — |
| `SerieService` | `Series` | — | — |
| `IvaService` | `TiposIva` | — | — |
| `RetencionService` | `TiposRetencion` | — | — |
| `ConfigService` | `Configuracion` | `ConfigServiceTest` | `ConfiguracionTest` |
| `EmpresaManager` | `Empresas` | `EmpresaManagerTest` | `EmpresasTest` |
| `VersionadoService` | `Versiones` | — | — |
| `EstadoService` | `Estados` | `EstadoServiceTest` | `EstadosTest` |
| `RectificativaService` | `Rectificativas` | — | — |
| `FacturacionMensualService` | `FacturacionMensual` | `FacturacionMensualServiceTest` | `FacturacionMensualTest` |
| `HistorialService` | `Historial` | `HistorialServiceTest` | `HistorialTest` |
| `NumeroService` | `Numeracion` | `NumeroServiceTest` | `NumeracionTest` |
| `CalculoService` | `Calculos` | `CalculoServiceTest` | `CalculosTest` |
| `ValidationException` | `ValidacionException` | — | — |
| `PreferenciasGlobales`, `Reloj`, `Sesion` | *(igual)* | `PreferenciasGlobalesTest` | *(igual)* |

Tipos anidados: `FacturacionMensualService.DiaMode` → `FacturacionMensual.ModoDia`. Los demás anidados ya están en español y se quedan (`Facturas.VersionCompleta`, `Facturas.ResumenBorrado`, `Estados.AnulacionResultado`, `Empresas.EmpresaInfo`, `Calculos.ResultadoConIva`, `FacturacionMensual.LineaPlantilla`, `FacturacionMensual.Resultado`).

### D2. Dominio: `cabofactu.modelo.dominio`

| Hoy | Nuevo |
|---|---|
| `FacturaVersion` | `VersionFactura` |
| `HistorialFila` | `FilaHistorial` |

El resto (`Cliente`, `Factura`, `LineaFactura`, `Serie`…) se queda igual.

### D3. Datos: `cabofactu.modelo.negocio.sqlite`

| Hoy | Nuevo | Test hoy | Test nuevo |
|---|---|---|---|
| `Database` | `Conexion` | `DatabaseTest` | `ConexionTest` |
| `Migrations` | `Migraciones` | `MigrationsTest` | `MigracionesTest` |
| `ClienteRepository` | `ClienteDAO` | — | — |
| `ConfigRepository` | `ConfiguracionDAO` | — | — |
| `CopiaRepository` | `CopiaSeguridadDAO` | `CopiaRepositoryTest` | `CopiaSeguridadDAOTest` |
| `FacturaRepository` | `FacturaDAO` | — | — |
| `HistorialRepository` | `HistorialDAO` | — | — |
| `IvaRepository` | `TipoIvaDAO` | — | — |
| `LineaRepository` | `LineaFacturaDAO` | — | — |
| `NumeroDisponibleRepository` | `NumeroDisponibleDAO` | — | — |
| `SerieRepository` | `SerieDAO` | `SerieRepositoryTest` | `SerieDAOTest` |
| `TipoRetencionRepository` | `TipoRetencionDAO` | `TipoRetencionRepositoryTest` | `TipoRetencionDAOTest` |
| `VersionRepository` | `VersionFacturaDAO` | — | — |
| `CargarDemo`, `DatosException` | *(igual)* | `CargarDemoTest` | *(igual)* |

Descartado: `FacturasSQLite`. Se confunde con `Facturas` (D1), y DAO es el nombre que se usa en clase.

### D4. Métodos de `Conexion` y `Migraciones`

`Conexion` (antes `Database`):

| Hoy | Nuevo |
|---|---|
| `getConnection()` | `establecerConexion()` |
| `resetConnection()` | `cerrarConexion()` |
| `beginTransaction()` | `iniciarTransaccion()` |
| `commit()` | `confirmar()` |
| `rollback()` | `deshacer()` |
| `endTransaction()` | `terminarTransaccion()` |
| `baseDataDir()` | `carpetaRaiz()` |
| `dataDir()` | `carpetaEmpresa()` |
| `setDataDir(Path)` | `setCarpetaRaiz(Path)` |
| `dbPath()` | `rutaBase()` |
| `dbPathDe(String)` | `rutaBaseDe(String)` |
| `lockPath()` | `rutaBloqueo()` |
| `lockPathGlobal()` | `rutaBloqueoGlobal()` |
| `setEmpresaActiva`, `getEmpresasDisponibles`, `crearBase`, `migrarBase` | *(igual)* |

Privados de `Conexion`: `DB_FILE` → `FICHERO_BASE`, `connection` → `conexion`, `baseDataDir` → `carpetaRaiz`, `dataDir` → `carpetaEmpresa`, `defaultDataDir()` → `carpetaRaizPorDefecto()`. `abrir(Path)` se queda.

`Migraciones` (antes `Migrations`):

| Hoy | Nuevo |
|---|---|
| `migrate(Connection)` | `migrar(Connection)` |
| `userVersion(Connection)` | `versionActual(Connection)` |
| `ultimaVersion()` | *(igual)* |
| privados `executeScript`, `readScript`, variable `current` | `ejecutarScript`, `leerScript`, `actual` |

**Los textos de excepción no cambian** (por ejemplo `"Error al confirmar la transaccion"`). Los nombres de fichero (`facturas.db`, `facturas.lock`) tampoco.

Si el IDE lo permite, usar *Refactor > Rename* para que cambien a la vez la declaración y todos los usos.

### D5. `Modelo` (antes `Servicios`), en `cabofactu.modelo`

Los campos dejan de ser `public final` y pasan a **`private final` con getter**, como pide `AGENTS.md` («campos de las clases de datos siempre `private`, con getters») y como será el `Modelo` definitivo:

| Campo hoy (`public final`) | Campo nuevo (`private final`) | Tipo nuevo | Getter |
|---|---|---|---|
| `reloj` | `reloj` | `Reloj` | `getReloj()` |
| `clientes` | `clientes` | `Clientes` | `getClientes()` |
| `series` | `series` | `Series` | `getSeries()` |
| `ivas` | `tiposIva` | `TiposIva` | `getTiposIva()` |
| `retenciones` | `tiposRetencion` | `TiposRetencion` | `getTiposRetencion()` |
| `config` | `configuracion` | `Configuracion` | `getConfiguracion()` |
| `numeros` | `numeracion` | `Numeracion` | `getNumeracion()` |
| `versionado` | `versiones` | `Versiones` | `getVersiones()` |
| `factura` | `facturas` | `Facturas` | `getFacturas()` |
| `estado` | `estados` | `Estados` | `getEstados()` |
| `rectificativas` | `rectificativas` | `Rectificativas` | `getRectificativas()` |
| `facturacionMensual` | `facturacionMensual` | `FacturacionMensual` | `getFacturacionMensual()` |
| `historialService` | `historial` | `Historial` | `getHistorial()` |
| `backup` | `copiaSeguridad` | `CopiaSeguridad` | `getCopiaSeguridad()` |

Getters sin Javadoc (se explican solos). Constructores `Modelo()` y `Modelo(Clock)`. Las variables locales del constructor pasan de `xRepository` a `xDAO` (`facturaDAO`, `lineaFacturaDAO`, `versionFacturaDAO`, `tipoIvaDAO`, `configuracionDAO`, `copiaSeguridadDAO`…).

**Uso desde las pantallas:**

| Hoy | Nuevo |
|---|---|
| `Vista.setServicios(Servicios s)` | `Vista.setModelo(Modelo m)` |
| `Navegador(Stage, Servicios)` y campo `servicios` | `Navegador(Stage, Modelo)` y campo `modelo` |
| `Navegador.servicios()` | `Navegador.modelo()` |
| `GenerarFacturasMensualesController.setServicios(...)` | `setModelo(...)` |
| `ThemeManager.aplicar(Scene, Servicios)` / `guardar(Servicios)` | `aplicar(Scene, Modelo)` / `guardar(Modelo)` |
| campo o variable `servicios` en controladores, `Main` y tests | `modelo` |
| `servicios.factura.crearFactura(...)` | `modelo.getFacturas().crearFactura(...)` (lo mismo con cada campo de la tabla de arriba) |

Descartado: dejar `Servicios`. El usuario eligió `Modelo`, como en Biblioteca8.

Descartado: mantener los campos públicos (`modelo.facturas`). Choca con `AGENTS.md` y habría que volver a cambiar las ~110 llamadas en el change de la arquitectura MVC.

**Temporal:** `Vista.setModelo`, `Navegador` y la variable `modelo` en las pantallas son un paso intermedio. Más adelante, el change de la arquitectura MVC sustituye esto por `Vista.getInstancia().getControlador().getModelo()`. Aquí **no** se hace esa parte.

### D5b. Relación con `AGENTS.md` durante este change

Este change solo renombra. Según el apartado «Transición» de `AGENTS.md`, lo que ya existe y todavía incumple las normas **se queda como está** y lo arreglan changes posteriores:

- tipos anidados (`Facturas.VersionCompleta`, `Facturas.ResumenBorrado`, `Estados.AnulacionResultado`, `Empresas.EmpresaInfo`, `Calculos.ResultadoConIva`, `Calculos.ClaveIva`, `FacturacionMensual.ModoDia`, `FacturacionMensual.LineaPlantilla`, `FacturacionMensual.Resultado`, `CopiaSeguridad.ResumenCopia`, `Serie.SufijoFecha`, `ResumenFactura.IvaGrupo`);
- `record`, streams, `::`, ternarios, `var` y lambdas que ya estén en las líneas que se tocan: solo se cambia el nombre, no la construcción;
- clases con todo `static` (`Empresas`, `Calculos`, `Sesion`, `PreferenciasGlobales`, `Conexion`).

Lo que **sí** se exige: ningún nombre completo de clase en las líneas tocadas (usar `import`) y los campos de `Modelo` como indica D5.

### D6. `CopiaSeguridad` (antes `BackupService`), en `cabofactu.fichero`

- Clase `BackupService` → `CopiaSeguridad`; test `BackupServiceTest` → `CopiaSeguridadTest`.
- Record anidado `ResumenBackup` → `ResumenCopia`.
- Campos `copiaRepository` → `copiaSeguridadDAO`, `facturaRepository` → `facturaDAO`.
- Métodos: `crearBackup` → `crearCopia`; el resto ya está en español (`leerResumen`, `restaurarEnEmpresaActiva`, `restaurarComoEmpresaNueva`, `facturasEmpresaActiva`, `versionEsquemaAplicacion`, `rutaLibre`).
- El formato del nombre de fichero (`facturas_AAAAMMDD_HHMMSS.db`) y la carpeta `copias_previas` no cambian.

### D7. Campos, parámetros y variables que repiten el nombre viejo

En producción y tests, cuando el nombre de un campo, parámetro o variable es el nombre viejo de la clase en minúscula, se renombra al nuevo:

| Hoy | Nuevo |
|---|---|
| `facturaRepository`, `clienteRepository`, `serieRepository`, `versionRepository`, `lineaRepository`, `ivaRepository`, `tipoRetencionRepository`, `configRepository`, `copiaRepository`, `historialRepository`, `numeroDisponibleRepository` | `facturaDAO`, `clienteDAO`, `serieDAO`, `versionFacturaDAO`, `lineaFacturaDAO`, `tipoIvaDAO`, `tipoRetencionDAO`, `configuracionDAO`, `copiaSeguridadDAO`, `historialDAO`, `numeroDisponibleDAO` |
| `facturaService` | `facturas` |
| `numeroService` | `numeracion` |
| `versionadoService` | `versiones` |
| `historialRepository` en `Historial` | `historialDAO` |

Si un nombre nuevo choca con otro identificador del mismo ámbito, se añade el sufijo del tipo (por ejemplo `facturasNegocio`) y se anota en `tasks.md`.

## Risks / Trade-offs

- **Riesgo medio: volumen.** Unos 400 cambios de identificador. El compilador detecta todo lo que falte; `mvn test` es la red.
- **Riesgo bajo: reflexión en tests.** Los tests de interfaz usan `getDeclaredField("…")` con nombres de campos `@FXML`; esos nombres **no** cambian en este change. Si algún test busca por reflexión un campo llamado `servicios`, hay que actualizar la cadena a `"modelo"`.
- **Trade-off aceptado:** `Configuracion` (negocio) y `ConfiguracionController` / `Configuracion.fxml` (vista) comparten raíz. Están en paquetes distintos y no chocan.
- **Trade-off aceptado:** entre este change y el siguiente, las pantallas se siguen llamando `BackupController`, `MenuController`… y los validadores `EmailValidator`.
