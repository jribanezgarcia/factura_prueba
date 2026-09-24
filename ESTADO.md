# CaboFactu: estado del proyecto

Actualizado: **22/09/2026**

Por dónde va el proyecto y qué toca ahora. Las normas de cómo se escribe el código y cómo se trabaja están en [AGENTS.md](AGENTS.md).

## Dónde está cada cosa

| Qué | Dónde |
|---|---|
| Qué hace la aplicación (fuente de verdad) | `openspec/specs/` |
| Cambios en curso | `openspec/changes/` |
| Cambios terminados | `openspec/changes/archive/` |
| Normas de código y flujo de trabajo | `AGENTS.md` |
| Estado (este fichero) | `ESTADO.md` |
| Notas de trabajo y decisiones sin publicar | `borrador_changes/` (fuera de git; en `viejo/` la auditoría y las pruebas visuales de agosto) |
| Documentación técnica | `docs/tecnico.md`, `README.md` |

El historial de lo que hizo cada cambio no se escribe aquí: está en `openspec/changes/archive/` y en `git log`.

## Hecho

- Renombrado completo al estilo Biblioteca8: paquete `cabofactu`, clases y paquetes en español.
- NIF con un aviso distinto por caso y datos de cliente obligatorios, comprobados al guardar.
- Tema de apariencia por empresa.
- Tablas creadas con un único script (`db/crear_tablas.sql`), sin migraciones ni versiones de esquema.
- Especificación sin píxeles: 20 requisitos de apariencia retirados y resumidos en «Apariencia de la interfaz» (54 → 35 requisitos).
- Esqueleto como Biblioteca8: `AppCaboFactu`, `Controlador`, `Vista` singleton, `LanzadorVentanaPrincipal`, `Pantalla`, barra en FXML y `Dialogos` como en clase.
- Módulo de clientes: `Cliente` que se valida en sus setters (con los errorX), `Clientes` singleton con el SQL dentro (sin `ClienteDAO`), las ocho operaciones en `Controlador` y `Modelo`, la ficha en `FichaCliente.fxml` con aviso de descarte y la pantalla con el patrón de tabla + formulario. El bloque Cliente del editor pregunta antes de actualizar la ficha.
- Módulo de empresas y menú: empresas solo en el arranque (crear, elegir y eliminar con confirmación), «Cambiar de empresa» en Configuración, sin bloqueo por datos incompletos (franja en el menú y comprobación al guardar, rectificar, generar y exportar), `Empresas` y `Sesion` singletons y `EmpresaDisponible` en lugar del `record`.
- Módulo de configuración: vuelve el bloqueo por empresa incompleta; `Empresa`, `TipoIva` y `TipoRetencion` que se validan en sus setters; `Configuracion`, `TiposIva` y `TiposRetencion` singletons con el SQL dentro (sin DAO); fichas modales de IVA y retención (con eliminar); lista lateral en el FXML sin clases internas; fuera las columnas del logo. Series se queda para el módulo de facturas.
- Módulo de series: el siguiente número se calcula a partir de las facturas (fuera los dos contadores y la tabla de huecos), fuera «Reutilizar anulados», `Serie` que se valida en sus setters con `FormatoNumero` en su fichero, `Series` singleton con toda la numeración (fuera `Numeracion`, `SerieDAO` y `NumeroDisponibleDAO`) y la sección Series con ficha modal.

Último cambio archivado: `2026-09-22-modulo-series`.

## En curso

**Replanteo para simplificar todo el proyecto** (19-20/09/2026).

- El análisis y las decisiones están en `borrador_changes/analisis-desde-cero.md`.
- **Decisiones cerradas** (20/09): sin versiones de factura; VeriFactu más adelante en otra rama y hasta entonces todo lo que choca con él se queda igual; negocio con el SQL dentro (sin DAO); clases de datos que se validan en sus setters; solo `Exception`; `Dialogos` como en clase; pantallas de tabla con formulario modal reutilizable; `Factura` con `Serie`, `Cliente` y sus líneas dentro. Todas están en `AGENTS.md`. **(22/09) Revertida F8**: una empresa sin sus datos obligatorios vuelve a bloquear (directo a Configuración, barra solo con Salir, «Cambiar de empresa» disponible).

Borrados por quedar obsoletos: `mvc-como-biblioteca8` y `negocio-dentro-del-modelo`.

Change escrito y pendiente de aplicar: **`pruebas-de-pantalla`** (24/09). Añade la tercera capa de pruebas: TestFX en modo headless, una clase de prueba por pantalla y dos pruebas de apariencia (los temas y los textos recortados). Comprobado antes de escribirlo que `testfx-junit5:4.0.18` con `openjfx-monocle:21.0.2` funciona sin ventanas y que convive con la batería de hoy si el toolkit se arranca solo desde `PruebasJavaFx` con `FxToolkit`. Va **antes** que el módulo de facturas, para que ese módulo tenga red.

## Qué toca ahora

1. Aplicar **`pruebas-de-pantalla`**.
2. Después, el siguiente módulo: **facturas sin versiones**.
3. Después, en el orden de los módulos: editor → histórico → PDF → mensuales → copias → documentación.
4. VeriFactu, al final, en otra rama.

## Trampas conocidas

- **Maven no está en el PATH.** Usar `C:\Users\juan\.m2\wrapper\dists\apache-maven-3.8.5-bin\5i5jha092a3i37g0paqnfr15e0\apache-maven-3.8.5\bin\mvn.cmd`.
- **No lanzar los tests con la aplicación abierta**: se mezclan las clases de `target` y salen errores falsos.
- **`mvn clean` puede fallar** al borrar `target`; borrar la carpeta a mano y ejecutar `mvn test`.
- **En OpenSpec, un requisito `MODIFIED` reemplaza el bloque entero**: hay que copiar todos sus escenarios, aunque solo cambie una frase.
- **No traduzcas las palabras clave de OpenSpec** (`ADDED`/`MODIFIED`/`REMOVED Requirements`, `Requirement:`, `Scenario:`, `WHEN`/`THEN`, y `## Why` y `## What Changes` del proposal): `validate` sigue diciendo «is valid» y el requisito desaparece al archivar.
- **Los datos se van a reiniciar** mientras el programa esté en desarrollo: no hace falta migrar nada.
- **Borrar un «BOM» en los CSS** se llevó el punto de `.root` en los siete temas. Sin el punto se cae la paleta de todos los temas y los tests no lo detectan; lo delata una `ClassCastException … cannot be cast to Paint` en el log de `mvn test`.
- **Tras `new Cliente(original)`** (el constructor copia) hay que volver a aplicar los campos editados. Si no, el cambio se pierde sin avisar.
- **OpenSpec 1.10 rechaza un `MODIFIED`** que pierde un escenario por su título. Para sustituir un escenario, conserva su título.
- **En JavaFX el CSS manda sobre los atributos del FXML**: `base.css` fija `-fx-min-width: 80px` a los botones, así que un `minWidth` en el FXML no sirve. Para que un botón no se corte al lado de un texto que se parte en líneas, da un `prefWidth` al texto.
- **No des por arreglada una medida sin comprobarla**: un test temporal que cargue la pantalla a 1024×768 y compare `getWidth()` con `prefWidth(-1)` lo confirma en segundos.
- **Apareció una copia suelta de un change ya archivado en `openspec/changes/`** y después desapareció su carpeta en `archive/`. Antes de commitear, mira `git status` y no hagas `git add -A` sin revisar qué entra.
- **En SQLite, comparar el texto de `strftime` con un entero no devuelve filas**: en las consultas de correlativos hay que poner `CAST(strftime('%Y', ...) AS INTEGER)`, como ya hacían las consultas originales del `SerieDAO`.
