# Continuacion del proyecto de facturacion

Estado actualizado: 31/08/2026

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

Cambio OpenSpec activo: **ninguno**.

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

- Correccion de espaciado en las pantallas rediseñadas: contenido demasiado pegado al borde de la ventana y a la barra de navegacion.
- Se añade padding de ventana de 16px a los `BorderPane` raiz de `Historico`, `Configuracion`, `Clientes`, `Versiones`, `Backup`, `Editor`, `MenuPrincipal` y `Arranque`.
- Se añade separacion de 12px entre la barra de navegacion y la primera tarjeta de contenido en `Historico`, `Clientes`, `Editor`, `Versiones` y `Backup`.
- Se amplian los paddings de `.card` (20px) y `.zona-contenido` (16px) y se aumenta el margen inferior de `.nav-bar` en `base.css`.
- Se añaden escenarios de margen y separacion respecto al menu en la especificacion del sistema de diseño visual.
- Suite **101/101** en verde.
- Change archivado como `2026-08-31-fix-ui-spacing`.

## Proximos pasos

- No hay changes activos. Esperar instrucciones del usuario para el siguiente change.
- Opciones conocidas pendientes en el spec principal:
  - Flujo de **clientes inactivos** (clientes con facturas no se borran, se marcan inactivos y no se ofrecen al crear facturas nuevas).
  - **Copia de seguridad** manual (V1: solo copia del SQLite).

## Git

- Rama `main`, ultimo commit `f3fd483`; **SINCRONIZADA** con `origin/main` (push realizado el 31/08/2026).
- Arbol limpio: nada pendiente de commitear.

## Notas tecnicas que evitan perder tiempo

- Comando Maven: `& "C:\Program Files\Apache NetBeans\java\maven\bin\mvn.cmd" test` (suite completa: 101 tests, todos verdes). IMPORTANTE: lanzar maven siempre desde el directorio del proyecto; si se lanza desde otro workdir falla sin POM y los pasos siguientes usan clases viejas.
- Para inspeccionar PDFs visualmente: rasterizar pagina con `Windows.Data.Pdf` desde PowerShell 5.1 (`render.ps1` en %TEMP%\opencode\pdfcheck) y leer el PNG; el modelo no lee PDFs directamente.
- `PdfPCellEvent.cellLayout(PdfCell, Rectangle, PdfContentByte[])` dibuja DESPUES del contenido: usar `canvases[PdfPTable.TEXTCANVAS]` para contornos; para fondo+texto juntos, pintar ambos dentro del evento con celda de frase vacia. `PdfReader.getPageN(1).getAsDict(PdfName.RESOURCES)` + `PdfDictionary.getKeys()` para inspeccionar fuentes embebidas (no existe `getPageResources`).
- FXML: `maxWidth="USE_PREF_SIZE"` es invalido; usar `maxWidth="-Infinity"`. Para que un control CREZCA dentro de una celda de GridPane con `hgrow` hacen falta AMBAS cosas: `ColumnConstraints hgrow="ALWAYS" fillWidth="true"` y `maxWidth="Infinity"` en el control (los controles no crecen por defecto). Las filas que deben envolver usan `FlowPane` con cada grupo etiqueta+campo en su propio HBox (FlowPane no tiene hgrow).
- El smoke test de UI no muestra ventanas: para ejercitar layout real usa `root.applyCss(); root.resize(800,600); root.layout();`.
- Cierre programatico de ventana: `nav.stage().fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST))`.
- Constructor de `EstadoService` (7 parametros): `(FacturaRepository, SerieRepository, VersionRepository, LineaRepository, VersionadoService, NumeroService, FacturaService)`.
- En los PDF el `.xlsx` original es referencia de formato: la columna TOTAL lleva IVA incluido (base × 1,21).
- El texto extraible de un PDF no incluye lo dibujado via plantilla/XObject (p. ej. la cifra final de «Pagina X de Y»): para testear el pie solo se puede afirmar hasta «de »; la cifra completa se comprueba a la vista.
- No crear dentro del proyecto carpetas/archivos de metadatos de IA ni documentacion no pedida.
