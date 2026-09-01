# Continuacion del proyecto de facturacion

Estado actualizado: 01/09/2026

NOTA: hasta aqui se ha hecho la app con modelos gratuitos de OPENCODE.

Este documento sirve como traspaso para continuar con cualquier IA. El proyecto se esta construyendo con OpenCode/OpenSpec. Antes de tocar codigo, leer:

- `facturacion_openspec_explore.md`
- `openspec/specs/invoicing/spec.md`
- este archivo

## REGLA OBLIGATORIA: usar OpenSpec siempre

Todo trabajo en este proyecto (nuevas funcionalidades, cambios, fixes, redisenos de interfaz, temas, etc.) se realiza SIEMPRE con el flujo OpenSpec a traves de las skills/commands de opencode (`/opsx-*`): primero `/opsx-propose`, despues `/opsx-apply-change`, luego `/opsx-sync-specs` y por ultimo `/opsx-archive-change`.

No se permite:

- tocar codigo ni spec fuera del flujo OpenSpec;
- modificar `openspec/specs/invoicing/spec.md` a mano sin pasar por `/opsx-sync-specs`;
- implementar cambios sin su cambio OpenSpec correspondiente (ni siquiera redisenos "rapidos");

El CLI `openspec` ya esta instalado (version 1.10.0). Cualquier IA que trabaje en este proyecto debe seguir este flujo en todas las sesiones.

## Estado general

Aplicacion JavaFX de facturacion local para Windows.

Stack actual:

- Java 21
- JavaFX/FXML
- Maven
- SQLite/JDBC
- OpenPDF
- JUnit 5
- Arquitectura por capas: `ui`, `service`, `repository`, `model`, `db`, `pdf`, `util`

La especificacion activa esta en:

- `openspec/specs/invoicing/spec.md`

Cambios OpenSpec archivados:

- `openspec/changes/archive/2026-08-21-redesign-pdf-factura`
- `openspec/changes/archive/2026-08-22-exportar-pdf-desde-historico`
- `openspec/changes/archive/2026-08-22-fix-pdf-totales-tarjeta-pago`
- `openspec/changes/archive/2026-08-22-pdf-fidelidad-prototipo`
- `openspec/changes/archive/2026-08-22-pdf-etiquetas-factura-cliente`
- `openspec/changes/archive/2026-08-22-pdf-cp-datos-pago-opcional`
- `openspec/changes/archive/2026-08-22-guardar-version-nueva-edicion`
- `openspec/changes/archive/2026-08-23-ventana-800-responsive`
- `openspec/changes/archive/2026-08-24-formato-numeracion-series`
- `openspec/changes/archive/2026-08-30-multi-empresa-ejercicio-fiscal`
- `openspec/changes/archive/2026-08-31-retencion-irpf`
- `openspec/changes/archive/2026-08-31-facturacion-mensual-cliente`
- `openspec/changes/archive/2026-08-31-redesign-ui-apple`
- `openspec/changes/archive/2026-08-31-fix-ui-spacing`
- `openspec/changes/archive/2026-08-31-window-sizing`
- `openspec/changes/archive/2026-08-31-fix-menu-min-height`
- `openspec/changes/archive/2026-08-31-adjust-menu-editor-sizing`
- `openspec/changes/archive/2026-09-01-fix-reutilizar-numeros-borrados`
- `openspec/changes/archive/2026-09-01-mejorar-menu-principal`
- `openspec/changes/archive/2026-09-01-editor-800x600-scroll`
- `openspec/changes/archive/2026-09-01-uniformizar-ventanas-800x600`
- `openspec/changes/archive/2026-09-01-uniformizar-1024-scroll-editor`
- `openspec/changes/archive/2026-09-01-fix-ventana-1024-transicion-arranque-menu`
- `openspec/changes/archive/2026-09-01-fix-ventana-1024-tras-pulse`

Cambio OpenSpec activo: `fix-ventana-max-heredado-arranque` (implementado y verificado, pendiente de `/opsx-sync-specs` y `/opsx-archive-change`). `uniformizar-1024-scroll-editor` archivado el 01/09/2026 (1024×768 + scroll Editor + spec actualizada), `fix-ventana-1024-transicion-arranque-menu` archivado el 01/09/2026 (clamp de subida en `aplicarSinRedimensionar`) y `fix-ventana-1024-tras-pulse` archivado el 01/09/2026 (clamp diferido al layout, que es el que realmente resuelve la transición Arranque→menú).

## Sesion del 31/08/2026 (cerrada y commiteada)

### Change `retencion-irpf` (archivado)

- Tipos de retencion configurables por empresa, con pestana en Configuracion similar a IVA.
- Selector de retencion en el editor de factura, con recalculo en caliente.
- Calculo sobre base bruta: `Total = Base − Descuento + IVA − Retencion`.
- Persistencia del tipo e importe de retencion en cada version de factura.
- Columna de retencion en el Historico.
- Fila de retencion en el resumen del PDF.
- Rectificativas heredan la retencion de la factura origen, editable antes de guardar.
- Snapshot de nombre y porcentaje de retencion en `factura_version` para conservar el historico aunque el tipo maestro cambie o se borre.
- Migraciones `005_retencion_irpf.sql` y `006_retencion_irpf_snapshot.sql`.
- Tests nuevos: `TipoRetencionRepositoryTest`, retencion en `CalculoServiceTest`, retencion en `FacturaServiceTest`. Suite **86/86** en verde.
- Commits `428958c` (implementacion) y `9d77eb5` (sync de spec y archivo en OpenSpec), push realizado.

### Change `facturacion-mensual-cliente` (archivado)

- Dialogo de facturacion mensual accesible desde el menu principal y desde el historico.
- Generacion de facturas mensuales para un unico cliente con seleccion de año, rango de meses, serie de numeracion y dia del mes (fijo, primer dia o ultimo dia).
- Lineas de concepto configurables con opcion de añadir el nombre del mes a la descripcion.
- Seleccion de tipo de IVA y tipo de retencion IRPF aplicados a todas las facturas generadas.
- Deteccion de facturas ya existentes para el mismo cliente, año y mes, con confirmacion antes de generar duplicados.
- Acciones de **Anular** y **Borrar** en el historico, con menu contextual y resumen de resultados.
- Borrado fisico de facturas que registra el numero como disponible en la tabla `numero_disponible` para reutilizacion posterior.
- Exportacion multiple a PDF desde el historico: un PDF por factura o un unico PDF agrupado.
- Migracion `007_numeros_disponibles.sql` y repositorio `NumeroDisponibleRepository`.
- Servicios nuevos/modificados: `FacturacionMensualService`, `NumeroService.proponerNumeros(...)`, `FacturaService.borrarFactura(...)`, `EstadoService.anularFacturas(...)`.
- Tests nuevos/actualizados: `FacturacionMensualServiceTest`, `FacturaServiceTest`, `EstadoServiceTest`, `NumeroServiceTest`, `HistorialServiceTest`. Suite **101/101** en verde.
- Commits `ebbc641` (implementacion) y `9156850` (sync de spec y archivo en OpenSpec), push realizado.

### Change `redesign-ui-apple` (archivado)

- Rediseño estructural de la interfaz inspirado en Ajustes de Apple: tarjetas de seccion, espaciado generoso, esquinas redondeadas y jerarquia tipografica.
- Se mantienen los 7 temas de color existentes (`biblioteca8`, `omarchy`, `esmeralda`, `terracota`, `negro-dorado`, `sakura`, `neon`); el nuevo diseño se adapta a cada paleta.
- Refactor de `base.css` con clases utilitarias `.card`, `.surface`, `.section-title`, `.form-label`, `.toolbar`, `.dialog-card` y estados de hover/focus.
- Ajustes en FXML de `MenuPrincipal`, `Editor`, `Historico`, `Configuracion`, `Clientes`, `GenerarFacturasMensuales`, `Versiones`, `Backup` y `Arranque` para usar las nuevas tarjetas.
- Microinteracciones CSS en botones, campos y navegacion; transiciones JavaFX de escala suave en botones primarios e items del menu (`Microinteracciones`).
- `Navegador` aplica las microinteracciones tras cargar cada vista.
- Se añade `minHeight` a la tabla de lineas del editor para garantizar que los tests de edicion siguen funcionando con el nuevo layout.
- Suite **101/101** en verde.
- Change archivado como `2026-08-31-redesign-ui-apple`.

### Change `fix-ui-spacing` (archivado)

- Correccion de espaciado en las pantallas redisenadas: contenido demasiado pegado al borde de la ventana y a la barra de navegacion.
- Se añade padding de ventana de 16px a los `BorderPane` raiz de `Historico`, `Configuracion`, `Clientes`, `Versiones`, `Backup`, `Editor`, `MenuPrincipal` y `Arranque`.
- Se añade separacion de 12px entre la barra de navegacion y la primera tarjeta de contenido en `Historico`, `Clientes`, `Editor`, `Versiones` y `Backup`.
- Se amplian los paddings de `.card` (20px) y `.zona-contenido` (16px) y se aumenta el margen inferior de `.nav-bar` en `base.css`.
- Se añaden escenarios de margen y separacion respecto al menu en la especificacion del sistema de diseño visual.
- Suite **101/101** en verde.
- Change archivado como `2026-08-31-fix-ui-spacing`.

### Change `window-sizing` (archivado)

- Configuracion de tamaño por vista para evitar que las pantallas se reduzcan mas de lo que su contenido permite.
- Nueva clase `VentanaConfig` con ancho, alto, minimo, maximo y redimensionabilidad por FXML.
- `Navegador` aplica la configuracion de la vista al `Stage` cada vez que cambia de pantalla, redimensionando y centrando la ventana.
- `Arranque` es fijo y no redimensionable (760×520).
- Editor: minimo 1000×760 para que se vean todos los controles.
- Resto de pantallas: `MenuPrincipal` 760×520, `Configuracion` 1000×620, `Historico` 1000×600, `Clientes` 1000×600, `Versiones` 900×500, `Backup` 720×450.
- Diálogo `Generar facturas mensuales`: minimo 920×680.
- Se mantiene el guardado de posicion y tamaño al cerrar; el tamaño se restaura pero las vistas lo sobreescriben al navegar.
- Nuevo test `EditorTamanoMinimoTest` que verifica que el Editor cabe completo en su tamaño minimo.
- Suite **102/102** en verde.
- Change archivado como `2026-08-31-window-sizing`.

### Change `fix-menu-min-height` (archivado)

- Correccion del tamaño minimo del Menu principal, que aparecia cortado tras los nuevos márgenes.
- Medida real del layout con test diagnostico: el contenido necesita al menos 589 px de alto.
- Se establece el tamaño predefinido/minimo del Menu principal en **760×600**.
- Se actualiza `VentanaConfig.MENU` y `MenuPrincipal.fxml`.
- Se actualiza la especificacion del sistema de tamaños de ventana.
- Suite **102/102** en verde.
- Change archivado como `2026-08-31-fix-menu-min-height`.

### Change `adjust-menu-editor-sizing` (archivado)

- Menu principal ajustado a **800×600** con margen inferior de 20 px y tarjetas alineadas arriba.
- Editor de facturas configurado para abrirse **maximizado** por defecto, aprovechando todo el alto de pantalla; la tabla de lineas ya permite scroll vertical.
- `VentanaConfig` añade soporte para flag `maximizado` y aplica `stage.setMaximized(...)` al cargar cada vista.
- Fix posterior: se reordena `VentanaConfig.aplicar` para llamar a `setMaximized` despues de fijar tamaño y centrar, garantizando que el Editor realmente se maximice.
- Test `EditorTamanoMinimoTest` actualizado para desmaximizar el Editor antes de validar su tamaño minimo.
- Se actualiza la especificacion del sistema de tamaños de ventana.
- Suite **102/102** en verde.
- Change archivado como `2026-08-31-adjust-menu-editor-sizing`.

### Change `fix-reutilizar-numeros-borrados` (archivado)

- Corregida la numeración de facturas para reutilizar los huecos libres dejados por facturas borradas.
- `NumeroService.siguienteCorrelativo(...)` consulta primero `numero_disponible` y propone el menor correlativo libre antes de continuar con el siguiente número.
- Se añaden tests para reutilización de números borrados, exclusión de huecos ocupados por activas y prioridad de huecos borrados sobre anulados.
- Se actualiza la especificacion de numeración por series.
- Suite **105/105** en verde.
- Change archivado como `2026-09-01-fix-reutilizar-numeros-borrados`.

### Change `mejorar-menu-principal` (archivado)

- Ajustada la pantalla del Menú principal a 800×600 para que la información de la empresa no se corte y se vea margen inferior.
- Tarjeta de empresa ampliada a 300 px de ancho; logo reducido a 100 px de alto; etiquetas de empresa con `wrapText`.
- Espaciado interno de la lista de opciones reducido y padding vertical de `.menu-item` bajado a 10 px.
- Padding inferior del `BorderPane` raíz aumentado a 24 px.
- Se añade requisito visual del Menú principal a la especificación.
- Suite **105/105** en verde.
- Change archivado como `2026-09-01-mejorar-menu-principal`.

### Change `editor-800x600-scroll` (archivado)

- Editor de facturas adaptado a **800×600** y ya no se abre maximizado.
- Tabla de líneas con `minHeight` reducido a 120 px y `VBox.vgrow="ALWAYS"` para que crezca al maximizar y tenga scroll interno en 800×600.
- Ajustes compactos en `base.css`: padding de `.card` a 16 px, `.action-bar` a 8 px, `.cabecera-linea` y `.grid-cabecera` más ajustados.
- Reducción de paddings y espaciados en `Editor.fxml` para que la cabecera, la tabla y los totales encajen en 600 px de alto.
- Test `EditorTamanoMinimoTest` actualizado a 800×600 y verifica que la tabla y los totales son visibles.
- Se actualiza la especificación de tamaños de ventana.
- Suite **105/105** en verde.
- Change archivado como `2026-09-01-editor-800x600-scroll`.

### Change `uniformizar-ventanas-800x600` (archivado)

- Todas las pantallas principales y el diálogo de Generar facturas mensuales ajustados a **800×600**.
- `VentanaConfig.aplicar` ahora no redimensiona la ventana principal cuando ya está visible; solo aplica mínimos, máximos y propiedades, evitando saltos entre vistas.
- `Main.entrarEnMenu` fuerza el tamaño a 800×600 al salir de `Arranque` y desmaximiza si fuera necesario.
- FXML ajustados: Configuración (tablas y formularios más compactos), Histórico y Clientes (tablas adaptadas a 800 de ancho), Versiones y Backup (paddings reducidos), Generar facturas mensuales (diálogo con `ScrollPane`).
- Especificación actualizada: se elimina el redimensionado/recentrado automático y se definen los tamaños uniformes.
- Suite **105/105** en verde.
- Change archivado como `2026-09-01-uniformizar-ventanas-800x600`.

### Change `fix-ventana-1024-transicion-arranque-menu` (archivado)

- Corregido que el menú y las vistas principales se quedaban a 760×520 (tamaño de Arranque) al navegar desde la pantalla de empresa hasta su carga en 1024×768 hasta manualmente redimensionar o maximizar.
- Causa raíz: `VentanaConfig.aplicar` enrutaba los Stages ya visibles a `aplicarSinRedimensionar`, que solo aplicaba min/max/resizable/maximized y NO volvía a fijar el tamaño tras `stage.setScene(...)`, por lo que la ventana conservaba el tamaño previo de Arranque.
- Fix: `aplicarSinRedimensionar` ahora eleva `width`/`height` hasta el mínimo de la vista si la ventana está por debajo (`if stage.getWidth()<minAncho → setWidth`, igual con alto). Se ejecuta después de `setScene`, así que cualquier vista con mayor mínimo crece al cargar (sin recentrar ni reducir).
- Nuevo test `VentanaTransicionTest`: Stage 760×520 → `nav.mostrar(MenuPrincipal)` verifica que sube a 1024×768.
- Suite **106/106** en verde.
- Change archivado como `2026-09-01-fix-ventana-1024-transicion-arranque-menu`.

### Change `fix-ventana-1024-tras-pulse` (archivado)

- El clamp del change anterior no bastaba: `entrarEnMenu` pide `setWidth(1024)` ANTES de `nav.mostrar`, y en el momento del clamp sincrono `stage.getWidth()` ya reporta 1024 aunque la ventana nativa siga en 760 (peticion asincrona de un Stage visible y no redimensionable). El layout de la escena nueva dejaba la ventana en 760×520.
- Fix definitivo en `VentanaConfig.aplicar`: cuando el Stage ya esta visible y la vista es redimensionable, tras `aplicarSinRedimensionar` se programa un `Platform.runLater` que re-sube width/height al minimo de la vista; se ejecuta en el siguiente pulse, despues del layout de la escena nueva, cuando `getWidth()` ya refleja el tamaño real.
- `VentanaTransicionTest` corregido para reproducir el timing real (Stage mostrado no redimensionable a 760×520 → `setWidth(1024)` → `nav.mostrar(Menu)` → layout → comprobacion tras dos pulses).
- Suite **106/106** en verde (`mvn clean test`).
- Change archivado como `2026-09-01-fix-ventana-1024-tras-pulse` (skip_specs).

### Change `fix-ventana-max-heredado-arranque` (implementado, pendiente de archivar)

- Corregido definitivamente que el menu y las vistas principales se quedaran a 760x520 (tamano de Arranque) al pulsar Entrar.
- Causa raiz real: `ARRANQUE` era la unica vista que fijaba maximos (`maxWidth=760`, `maxHeight=520`). Esos maximos quedaban puestos en el Stage y en la ventana nativa; `entrarEnMenu` pedia 1024x768 antes de que nadie los levantase y con `resizable=false` activo, asi que Windows recortaba la peticion. Los clamps de los dos intentos anteriores eran condicionales a `stage.getWidth()`, que devuelve la propiedad de JavaFX (ya 1024) y no el tamano real de la ventana, por lo que nunca llegaban a ejecutarse.
- `VentanaConfig`: `ARRANQUE` deja de fijar maximos (su caracter fijo lo da `redimensionable=false`) y `aplicar` pasa a un unico camino determinista: libera los maximos de la vista anterior, fija `resizable` antes de tocar el tamano, aplica minimos y maximos, y decide si redimensionar comparando la configuracion previa (guardada por Stage en `getProperties()`) con la nueva, sin leer `getWidth()`. Solo maximiza cuando la vista lo pide, asi que navegar ya no desmaximiza la ventana.
- `Main.entrarEnMenu` deja de dimensionar y hace la transicion con la ventana oculta (`hide()` -> cargar menu -> `show()`), de modo que la ventana nativa se recrea con las medidas de la vista destino. Requiere `Platform.setImplicitExit(false)`; el cierre real sigue siendo el `Platform.exit()` de `cerrarAplicacion`.
- `Main` recupera ademas el tamano guardado de la sesion anterior cuando supera el minimo de la vista.
- `VentanaTransicionTest` reescrito (transicion Arranque -> Menu, maximos liberados, redimensionabilidad) mas un caso nuevo de conservacion del tamano del usuario al navegar entre vistas del mismo tamano. Suite **107/107** en verde.
- IMPORTANTE: el test NO reproduce el bug (comprobado revirtiendo el codigo: el test pasaba igual). En un Stage creado en un test la ventana nativa si crece; el fallo solo se daba en el primary stage de la aplicacion real. La verificacion valida fue visual, con la app en marcha.

## Proximos pasos

- No hay changes activos. Esperar instrucciones del usuario para el siguiente change.
- Opciones conocidas pendientes en el spec principal:
  - Flujo de **clientes inactivos** (clientes con facturas no se borran, se marcan inactivos y no se ofrecen al crear facturas nuevas).
  - **Copia de seguridad** manual (V1: solo copia del SQLite).


## Git

- **Corregido el bug de 1024×768 en transición (01/09/2026)**: al pasar de Arranque (760×520) al menú, la ventana se quedaba en 760×520 hasta redimensionar o maximizar. Causa raíz: `VentanaConfig.aplicar` enviaba los Stages ya visibles a `aplicarSinRedimensionar`, que solo aplicaba min/max sin fijar el tamaño tras `setScene`. Fix: ahora `aplicarSinRedimensionar` eleva width/height hasta el mínimo de la vista (`VentanaTransicionTest` lo verifica, suite **106/106**).
- Commit y push de: fix `VentanaConfig` + `VentanaTransicionTest` + spec y archive OpenSpec.
- Despues, fix definitivo con clamp diferido al pulse (`fix-ventana-1024-tras-pulse`) commiteado y pusheado.
- **Fix real del tamano de ventana (01/09/2026)**: los dos fixes anteriores no bastaban. La causa era el `maxWidth`/`maxHeight` de Arranque heredado por el Stage. Corregido en `VentanaConfig` (sin maximos en Arranque, `aplicar` determinista) y `Main` (transicion con la ventana oculta). Verificado a la vista con la app en marcha; suite **107/107**.

## Notas tecnicas que evitan perder tiempo

- Comando Maven: `& "C:\Program Files\Apache NetBeans\java\maven\bin\mvn.cmd" test` (suite completa: 105 tests, todos verdes). IMPORTANTE: lanzar maven siempre desde el directorio del proyecto; si se lanza desde otro workdir falla sin POM y los pasos siguientes usan clases viejas. IMPORTANTE: si la app sigue mostrando tamaños antiguos tras cambiar código, borrar `target\` y `mvn clean` (el compilador incremental puede dejar `.class` mezclados).
- Para inspeccionar PDFs visualmente: rasterizar pagina con `Windows.Data.Pdf` desde PowerShell 5.1 (`render.ps1` en %TEMP%\opencode\pdfcheck) y leer el PNG; el modelo no lee PDFs directamente.
- `PdfPCellEvent.cellLayout(PdfCell, Rectangle, PdfContentByte[])` dibuja DESPUES del contenido: usar `canvases[PdfPTable.TEXTCANVAS]` para contornos; para fondo+texto juntos, pintar ambos dentro del evento con celda de frase vacia. `PdfReader.getPageN(1).getAsDict(PdfName.RESOURCES)` + `PdfDictionary.getKeys()` para inspeccionar fuentes embebidas (no existe `getPageResources`).
- FXML: `maxWidth="USE_PREF_SIZE"` es invalido; usar `maxWidth="-Infinity"`. Para que un control CREZCA dentro de una celda de GridPane con `hgrow` hacen falta AMBAS cosas: `ColumnConstraints hgrow="ALWAYS" fillWidth="true"` y `maxWidth="Infinity"` en el control (los controles no crecen por defecto). Las filas que deben envolver usan `FlowPane` con cada grupo etiqueta+campo en su propio HBox (FlowPane no tiene hgrow).
- El smoke test de UI no muestra ventanas: para ejercitar layout real usa `root.applyCss(); root.resize(1024,768); root.layout();`.
- Cierre programatico de ventana: `nav.stage().fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST))`.
- Constructor de `EstadoService` (7 parametros): `(FacturaRepository, SerieRepository, VersionRepository, LineaRepository, VersionadoService, NumeroService, FacturaService)`.
- En los PDF el `.xlsx` original es referencia de formato: la columna TOTAL lleva IVA incluido (base × 1,21).
- El texto extraible de un PDF no incluye lo dibujado via plantilla/XObject (p. ej. la cifra final de «Pagina X de Y»): para testear el pie solo se puede afirmar hasta «de »; la cifra completa se comprueba a la vista.
- No crear dentro del proyecto carpetas/archivos de metadatos de IA ni documentacion no pedida.
