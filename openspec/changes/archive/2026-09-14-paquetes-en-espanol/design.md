## Context

Estado a 14/09/2026 (commit `eb94141`).

**Referencia de estilo (Biblioteca8):**

```
biblioteca/
├── AppBiblioteca
├── modelo/          Modelo
│   ├── dominio/     Libro, Usuario, Prestamo…
│   └── negocio/     Libros, Usuarios, Prestamos…
│       └── mysql/   Conexion
├── vista/           Vista, LanzadorVentanaPrincipal
│   ├── controlador/ LibrosController, LoginController…
│   ├── recursos/    Libros.fxml, Login.fxml…
│   └── utilidades/  Dialogos
├── fichero/         CopiaSeguridadXML
└── utilidades/      UtilidadesXML
```

**Diferencia que se mantiene:** Biblioteca8 tiene una sola capa (`Libros` hace reglas y SQL). Aquí se conservan las dos capas que acordó la auditoría: reglas en `modelo.negocio` y SQL en `modelo.negocio.sqlite`.

**Comprobaciones previas hechas:**

- Los datos viven en `%APPDATA%\Facturacion` (`Database.java:30-37`) y no se usa `java.util.prefs`: cambiar el paquete no pierde empresas, facturas ni preferencias.
- `Migrations` y `CargarDemo` cargan `db/migrations/*.sql` y `db/seed_demo.sql` con `getClassLoader().getResourceAsStream(...)`, **sin** el paquete: `src/main/resources/db/` no se mueve.
- Todas las rutas de FXML, CSS e imágenes en Java son absolutas (`"/com/alcazaba/facturacion/..."`). La única referencia relativa es `Arranque.fxml:18` → `@../images/icono-aplicacion.png`.
- `Configuracion.fxml:3` importa `com.alcazaba.facturacion.ui.PreviaCabecera`.
- Miembros sin `public` que se usan desde otro paquete **tras** mover: `Dialogos.setImpl(Impl)` (`Dialogos.java:139`) y `Dialogos.restoreDefault()` (`:146`), usados por `JavaFxTestSupport`, `ClientesNifValidationTest`, `EditorIvaInactivoTest` y `EditorNifValidationTest`. El resto de lo no público (`pdf/*`, `CargarDemo.trocear`, `BackupService.rutaLibre`, `ClientesController.construirFicha`, `ConfiguracionController.ItemSeccion`) sigue usándose solo desde su mismo paquete.
- Los tests que usan `getDeclaredField` + `setAccessible` funcionan igual desde otro paquete.

## Goals / Non-Goals

**Goals:** paquetes en español con la forma de Biblioteca8; raíz `cabofactu`; misma aplicación, mismos tests en verde; historial de git conservado (`git mv`).

**Non-Goals:** cambiar el nombre de ninguna clase, método, campo o FXML; tocar `docs/`; mover `src/main/resources/db/`; cambiar `artifactId`; tocar changes archivados.

## Decisions

### D1. Tabla de paquetes (código de producción)

Todas en `src/main/java/`. La columna «Nuevo» es el paquete Java; la carpeta es la misma con `/`.

| Hoy (`com.alcazaba.facturacion.…`) | Clases | Nuevo |
|---|---|---|
| *(raíz)* | `InstanciaUnica`, `Launcher`, `Main`, `PreparacionDatos` | `cabofactu` |
| `service` | `Servicios` | `cabofactu.modelo` |
| `model` | `Cliente`, `DatosPago`, `Empresa`, `EstadoFactura`, `Factura`, `FacturaVersion`, `FiltrosHistorial`, `HistorialFila`, `LineaFactura`, `ResumenFactura`, `Serie`, `TipoIva`, `TipoRetencion` | `cabofactu.modelo.dominio` |
| `service` | `CalculoService`, `ClienteService`, `ConfigService`, `EmpresaManager`, `EstadoService`, `FacturacionMensualService`, `FacturaService`, `HistorialService`, `IvaService`, `NumeroService`, `PreferenciasGlobales`, `RectificativaService`, `Reloj`, `RetencionService`, `SerieService`, `Sesion`, `ValidationException`, `VersionadoService` | `cabofactu.modelo.negocio` |
| `db` | `CargarDemo`, `Database`, `DatosException`, `Migrations` | `cabofactu.modelo.negocio.sqlite` |
| `repository` | `ClienteRepository`, `ConfigRepository`, `CopiaRepository`, `FacturaRepository`, `HistorialRepository`, `IvaRepository`, `LineaRepository`, `NumeroDisponibleRepository`, `SerieRepository`, `TipoRetencionRepository`, `VersionRepository` | `cabofactu.modelo.negocio.sqlite` |
| `service` | `BackupService` | `cabofactu.fichero` |
| `pdf` | `CabeceraLayout`, `CabeceraPiePdf`, `EstiloPdf`, `InvoiceDocument`, `InvoiceDocumentBuilder`, `OpenPdfRenderer`, `PdfService` | `cabofactu.pdf` |
| `ui` | `Navegador`, `Ventanas`, `VentanaConfig`, `Vista` | `cabofactu.vista` |
| `ui` | `ArranqueController`, `BackupController`, `ClientesController`, `ConfiguracionController`, `EditorController`, `GenerarFacturasMensualesController`, `HistoricoController`, `MenuController`, `VersionesController` | `cabofactu.vista.controlador` |
| `ui` | `BarraNavegacion`, `Botones`, `Dialogos`, `Microinteracciones`, `PreviaCabecera`, `ThemeManager` | `cabofactu.vista.utilidades` |
| `util` | `CodigoPostalValidator`, `DocumentoFiscalValidator`, `EmailValidator`, `Formatos`, `LogoMarco` | `cabofactu.utilidades` |

Descartado: `db` y `repository` en paquetes separados (`sqlite` y `sqlite.dao`). Biblioteca8 junta conexión y acceso a datos bajo `negocio.mysql`, y juntarlos aquí no rompe nada, porque los repositorios solo usan `Database`/`DatosException`.

### D2. Tabla de paquetes (tests)

En `src/test/java/`. Regla: cada test va al paquete de la clase que prueba.

| Tests | Nuevo |
|---|---|
| `InstanciaUnicaTest`, `PreparacionDatosTest` | `cabofactu` |
| `CalculoServiceTest`, `ConfigServiceTest`, `EmpresaManagerTest`, `EstadoServiceTest`, `FacturacionMensualServiceTest`, `FacturaServiceTest`, `HistorialServiceTest`, `NumeroServiceTest`, `PreferenciasGlobalesTest` | `cabofactu.modelo.negocio` |
| `CargarDemoTest`, `DatabaseTest`, `MigrationsTest`, `CopiaRepositoryTest`, `SerieRepositoryTest`, `TipoRetencionRepositoryTest` | `cabofactu.modelo.negocio.sqlite` |
| `BackupServiceTest` | `cabofactu.fichero` |
| `CabeceraLayoutTest`, `InvoiceDocumentBuilderTest`, `OpenPdfRendererTest`, `PdfServiceTest` | `cabofactu.pdf` |
| `JavaFxTestSupport`, `VistaPrueba`, `NavegacionCambiosSinGuardarTest`, `StyleClassSeparadorTest`, `UiSmokeTest`, `VentanaTransicionTest` | `cabofactu.vista` |
| `BackupLayoutTest`, `ClientesNifValidationTest`, `ConfiguracionLayoutTest`, `EditorBarraAccionesTest`, `EditorFlujoTecladoTest`, `EditorIvaInactivoTest`, `EditorNifValidationTest`, `EditorTamanoMinimoTest`, `EditorTotalesDescuentoTest`, `MenuLayoutTest` | `cabofactu.vista.controlador` |
| `BotonesTest` | `cabofactu.vista.utilidades` |
| `CodigoPostalValidatorTest`, `DocumentoFiscalValidatorTest`, `EmailValidatorTest`, `FormatosTest`, `LogoMarcoTest` | `cabofactu.utilidades` |

Si al compilar un test no ve un miembro que antes veía por estar en el mismo paquete, **se hace `public` ese miembro en la clase de producción**. No se mueve el test a otro paquete. Hay que anotar en `tasks.md` cuáles han sido (se esperan solo los dos de D4).

### D3. Recursos

| Hoy | Nuevo |
|---|---|
| `src/main/resources/com/alcazaba/facturacion/ui/*.fxml` (9) | `src/main/resources/cabofactu/vista/recursos/` |
| `src/main/resources/com/alcazaba/facturacion/themes/*.css` | `src/main/resources/cabofactu/vista/recursos/temas/` |
| `src/main/resources/com/alcazaba/facturacion/images/*` | `src/main/resources/cabofactu/vista/recursos/imagenes/` |
| `src/test/resources/com/alcazaba/facturacion/ui/VistaPrueba.fxml` | `src/test/resources/cabofactu/vista/recursos/VistaPrueba.fxml` |
| `src/main/resources/db/` | **no se mueve** |

Sustituciones de texto en Java y FXML (main y test):

| Texto viejo | Texto nuevo |
|---|---|
| `"/com/alcazaba/facturacion/ui/` | `"/cabofactu/vista/recursos/` |
| `"/com/alcazaba/facturacion/themes/` | `"/cabofactu/vista/recursos/temas/` |
| `"/com/alcazaba/facturacion/images/` | `"/cabofactu/vista/recursos/imagenes/` |
| `Arranque.fxml`: `@../images/icono-aplicacion.png` | `@imagenes/icono-aplicacion.png` |
| `fx:controller="com.alcazaba.facturacion.ui.XController"` | `fx:controller="cabofactu.vista.controlador.XController"` |
| `VistaPrueba.fxml`: `fx:controller="com.alcazaba.facturacion.ui.VistaPrueba"` | `fx:controller="cabofactu.vista.VistaPrueba"` |
| `Configuracion.fxml`: `<?import com.alcazaba.facturacion.ui.PreviaCabecera?>` | `<?import cabofactu.vista.utilidades.PreviaCabecera?>` |
| `StyleClassSeparadorTest.java:26`: `"src", "main", "resources", "com", "alcazaba", "facturacion", "ui"` (ruta de disco, no de classpath) | `"src", "main", "resources", "cabofactu", "vista", "recursos"` |

Lugares conocidos con rutas: `Main`, `Navegador`, `BarraNavegacion`, `VentanaConfig`, `Ventanas`, `ThemeManager`, `BackupController`, `ClientesController`, `ConfiguracionController`, `EditorController`, `GenerarFacturasMensualesController`, `HistoricoController`, `MenuController`, `VersionesController` y 12 tests de interfaz.

### D4. Visibilidad en `Dialogos`

`static void setImpl(Impl i)` → `public static void setImpl(Impl i)`, y `static void restoreDefault()` → `public static void restoreDefault()`. `Impl` ya es `public`.

### D5. Ficheros fuera de `src`

- `pom.xml`: `<groupId>com.alcazaba</groupId>` → `<groupId>cabofactu</groupId>`; `<mainClass>com.alcazaba.facturacion.Main</mainClass>` → `<mainClass>cabofactu.Main</mainClass>`. `artifactId` no cambia.
- `cargar_demo.bat`: `-Dexec.mainClass=com.alcazaba.facturacion.db.CargarDemo` → `-Dexec.mainClass=cabofactu.modelo.negocio.sqlite.CargarDemo`.
- `README.md:3`: `src/main/resources/com/alcazaba/facturacion/images/icono-aplicacion.png` → `src/main/resources/cabofactu/vista/recursos/imagenes/icono-aplicacion.png`. El resto del README se toca en el tercer change.

### D6. Cómo mover

Con `git mv` (o mover y después `git add -A`), para que `git status` muestre los ficheros como **renamed** y no se pierda el historial. Borrar las carpetas vacías de `com/alcazaba/facturacion` al terminar. No usar sustituciones ciegas de `com.alcazaba.facturacion` → `cabofactu` **sin** aplicar después la tabla de D1, porque `service`, `ui`, `db` y `repository` se reparten en varios paquetes.

## Risks / Trade-offs

- **Riesgo medio: un `import` que se pierde en el reparto de `ui` y `service`.** El compilador lo detecta; `mvn test` es la red de seguridad.
- **Riesgo bajo: una ruta de recurso mal escrita.** Solo falla en ejecución. `UiSmokeTest` carga todos los FXML y `VentanaTransicionTest` navega, pero la carga de temas e icono hay que mirarla en la aplicación (tareas 7.x).
- **Trade-off aceptado:** durante los changes 1 y 2 conviven paquetes en español con clases en inglés (`cabofactu.modelo.negocio.FacturaService`). Se resuelve en los changes siguientes.
