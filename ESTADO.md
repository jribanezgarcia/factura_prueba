# CaboFactu: estado del proyecto

Actualizado: **25/09/2026**

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
- Módulo de pruebas: la tercera capa, TestFX en modo headless, una clase de prueba por pantalla (heredan de `PruebaDePantalla`, que prepara la empresa de demostración en una carpeta temporal) y dos pruebas de apariencia (`TemasTest` y `TextosCompletosTest`). El toolkit se arranca solo desde `PruebasJavaFx`. En total 381 pruebas; la batería completa, ~4:21 con `mvn test`. La columna del histórico queda como «Base» para que quepa en la ventana mínima.
- Revisión de la base de datos: NIF y nombres únicos en las tablas maestras, recuperación de clientes inactivos y reutilización del cliente al facturar.

Último cambio archivado: `2026-09-25-revision-base-de-datos`.

## En curso

**Replanteo para simplificar todo el proyecto** (19-20/09/2026).

- El análisis y las decisiones están en `borrador_changes/analisis-desde-cero.md`.
- **Decisiones cerradas** (20/09): sin versiones de factura; VeriFactu más adelante en otra rama y hasta entonces todo lo que choca con él se queda igual; negocio con el SQL dentro (sin DAO); clases de datos que se validan en sus setters; solo `Exception`; `Dialogos` como en clase; pantallas de tabla con formulario modal reutilizable; `Factura` con `Serie`, `Cliente` y sus líneas dentro. Todas están en `AGENTS.md`. **(22/09) Revertida F8**: una empresa sin sus datos obligatorios vuelve a bloquear (directo a Configuración, barra solo con Salir, «Cambiar de empresa» disponible).

Borrados por quedar obsoletos: `mvc-como-biblioteca8` y `negocio-dentro-del-modelo`.

**`modulo-facturas`** (en curso): una fila por factura (`factura` y `factura_linea`, sin `factura_version`), `Factura` entera con su serie, su cliente, sus líneas y su retención dentro, `Facturas` singleton con todo el SQL y pantallas adaptadas sin versiones.

## Qué toca ahora

1. **`modulo-facturas`** (facturas sin versiones).
2. Después, en el orden de los módulos: editor → histórico → PDF → mensuales → copias → documentación.
3. VeriFactu, al final, en otra rama.

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
- **Los clics del robot no llegan a los botones de los avisos en headless**: cerrarlos con `ENTER` (`push`) y esperar al cierre. Los eventos lanzados durante la transición se pierden, así que hay que esperar.
- **Las ventanas cerradas siguen saliendo en los `lookup`**: filtrar por `isShowing` leído en el hilo FX, con `lookupAll` de JavaFX (el `lookup` de TestFX fuera del hilo FX es inestable con modales). El texto de un botón se busca recorriendo `Labeled`, porque `lookupAll` solo entiende CSS.
- **Los fx:id se repiten entre secciones ocultas**: `lookupAll` encuentra nodos de secciones con `visible=false` y el robot escribe en el vacío. Hay que subir por los padres y descartar lo que tenga un `visible=false` por encima.
- **Tras Aceptar puede venir otro aviso encadenado sin hueco entre medias** (confirmar un borrado que falla): la espera de `contestarAviso` acepta que se cierre **o** que cambie el texto.
- Tras cambiar crear_tablas.sql hay que borrar %APPDATA%\Facturacion: las tablas se crean con CREATE TABLE IF NOT EXISTS y las bases que ya existen no reciben los cambios.
- **La moneda lleva espacio inseparable** (`Formatos.moneda`): en los asserts, `contains("1.760,00")` en vez del texto entero.
- **Anular crea versión**: el histórico enseña una fila por versión; tras anular hay dos filas del mismo número.
- **`pulsar` no toca la celda-botón de un `ComboBox`**: solo las celdas de la lista abierta (viven en un `ListView`). Si no, al elegir valor se pulsa el propio desplegable.
- **Los desplegables enseñan unas diez filas**: las celdas que hay que bajar a ver no existen para el robot; en mensuales se eligen meses de los visibles.
- **Rectificar guarda al momento y `Guardar` abre versiones; el número solo vale al crear** (al editar se ignora) y el hueco solo sale en factura nueva. Sin editar celdas no hay líneas con contenido, así que el ocupado y el hueco no se pueden probar hasta modulo-facturas.
