# Continuacion del proyecto de facturacion

Estado actualizado: 22/08/2026

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

Cambios OpenSpec archivados (ademas de los anteriores):

- `openspec/changes/archive/2026-08-21-redesign-pdf-factura` (archivado el 21/08/2026)

Cambio OpenSpec activo (en implementacion, sin commitear):

- `openspec/changes/exportar-pdf-desde-historico/` — exportar PDF directamente desde el Historico, individual y en lote. Tareas 8/9 completadas; queda solo la 4.2 (verificacion manual por el usuario).

## Cambios realizados hoy (22/08/2026)

### 1. Cierre del cambio del rediseño del PDF (Fase 0)

- Sync de la delta a `openspec/specs/invoicing/spec.md` (4 requisitos modificados) y archive del change `2026-08-21-redesign-pdf-factura`.
- Commit `801ff5a` "Rediseño del PDF con tarjetas, datos de pago y color configurable, archivado en OpenSpec" (38 archivos).

### 2. Exportar PDF desde el Histórico (Fase 1, cambio activo `exportar-pdf-desde-historico`)

Implementado y con la suite en verde (39/39 tests):

- `util/Formatos.java`: nuevo metodo estatico `nombreArchivoPdf(String)` (barra por guion + `.pdf`), usado por editor e historico.
- Test nuevo `src/test/java/com/alcazaba/facturacion/util/FormatosTest.java`.
- `ui/Historico.fxml`: boton «Exportar PDF» junto a Buscar.
- `ui/HistoricoController.java`: seleccion multiple (`SelectionMode.MULTIPLE`, doble clic sigue abriendo); `exportarUna(...)` con FileChooser igual que el editor; `exportarVarias(...)` con DirectoryChooser unico + Task en segundo plano y resumen final (generadas/falladas por fila); reutiliza preferencias `ultima_carpeta_export` y `color_pdf`.

### 3. Ronda de revision del PDF exportado (PENDIENTE de aprobar prototipo)

El usuario reviso un PDF real y notifico 3 problemas. Diagnostico hecho y prototipo creado:

- **Totales duplicados**: el bloque mostraba `Base 21%/IVA 21%` y ademas `Base total/IVA total`. Nuevo diseno acordado en prototipo: `Base` → `IVA n%` (cuota) → `Descuento n%` solo si existe (restando, cuadre Base − Descuento + IVA = TOTAL) → `TOTAL` en color. Con varios tipos de IVA, cada par Base/IVA y un solo descuento.
- **Tarjeta «Datos de pago»**: quitar el fondo marron de su cabecera; dejarla blanca con borde fino inferior y texto marron («Facturar a» sigue bicolor como estaba).
- **Paginacion «Página X de Y»**: causa reproducida en pruebas reales: cuando el bloque de totales no cabe al final, salta entero a una segunda pagina casi vacia y el pie la cuenta. Solucion propuesta: bloque de totales mas compacto y mejor reparto del salto de pagina; el contador siempre refleja paginas reales.

Prototipo para aprobacion: `prototipos/pdf-fix-v2.html` (zonas cambiadas marcadas con recuadro naranja; tambien enlazado desde `prototipos/index.html`).

## Proximos pasos (por este orden)

1. Usuario aprueba o ajusta `prototipos/pdf-fix-v2.html`.
2. Con la aprobacion, crear change OpenSpec nuevo (`/opsx-propose`, nombre sugerido `fix-pdf-totales-tarjeta-pago`) para los 3 arreglos sobre `pdf/PdfService.java`: limpiar `bloqueTotales` (quitar filas repetidas, fila descuento restando), cabecera clara en `tarjetaPago`, compactar espaciados/paginacion. Aplicarlo y pasar `mvn test`.
3. Verificacion manual conjunta: exportar una factura y un lote de varias desde el Historico (cierra la tarea 4.2 del cambio activo).
4. Cerrar ambos cambios: `/opsx-sync-specs` + `/opsx-archive-change` de `exportar-pdf-desde-historico` y del nuevo fix, y commit (SIN push).

## Git

- Rama `main`, ultimo commit `801ff5a`; va 6 commits por delante de `origin/main` SIN push (el usuario no lo ha pedido).
- Sin commitear ahora mismo: todo lo del cambio activo `exportar-pdf-desde-historico` (Formatos, FormatosTest, HistoricoController, Historico.fxml, EditorController usando `nombreArchivoPdf`), el change en `openspec/changes/exportar-pdf-desde-historico/`, `prototipos/pdf-fix-v2.html` y `prototipos/index.html`.

## Notas tecnicas que evitan perder tiempo

- Comando Maven: `& "C:\Program Files\Apache NetBeans\java\maven\bin\mvn.cmd" test` (suite completa: 39 tests, todos verdes).
- FXML: `maxWidth="USE_PREF_SIZE"` es invalido; usar `maxWidth="-Infinity"`.
- Cierre programatico de ventana: `nav.stage().fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST))`.
- Constructor de `EstadoService` (7 parametros): `(FacturaRepository, SerieRepository, VersionRepository, LineaRepository, VersionadoService, NumeroService, FacturaService)`.
- En los PDF el `.xlsx` original es referencia de formato: la columna TOTAL lleva IVA incluido (base × 1,21).
- No crear dentro del proyecto carpetas/archivos de metadatos de IA ni documentacion no pedida.
