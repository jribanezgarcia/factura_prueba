# Continuacion del proyecto de facturacion

Estado actualizado: 22/08/2026 (tarde)

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

- `openspec/specs/invoicing/spec.md` (actualizada con los requisitos «Historico» y «Exportacion a PDF» tras el cierre del 22/08)

Cambios OpenSpec archivados (ademas de los anteriores):

- `openspec/changes/archive/2026-08-21-redesign-pdf-factura` (archivado el 21/08/2026)
- `openspec/changes/archive/2026-08-22-exportar-pdf-desde-historico` (archivado el 22/08/2026)
- `openspec/changes/archive/2026-08-22-fix-pdf-totales-tarjeta-pago` (archivado el 22/08/2026)

No hay ningun cambio OpenSpec activo ahora mismo.

## Sesion del 22/08/2026 (lo hecho hoy, todo cerrado y commiteado)

### 1. Exportar PDF desde el Historico

- `util/Formatos.java`: nuevo metodo estatico `nombreArchivoPdf(String)` (barra por guion + `.pdf`), usado por editor e historico.
- Test nuevo `src/test/java/com/alcazaba/facturacion/util/FormatosTest.java`.
- `ui/Historico.fxml`: boton «Exportar PDF» junto a Buscar.
- `ui/HistoricoController.java`: seleccion multiple (`SelectionMode.MULTIPLE`, doble clic sigue abriendo); `exportarUna(...)` con FileChooser igual que el editor; `exportarVarias(...)` con DirectoryChooser unico + Task en segundo plano y resumen final (generadas/falladas por fila); reutiliza preferencias `ultima_carpeta_export` y `color_pdf`.
- Verificado manualmente por el usuario (individual y lote).

### 2. Fix visual del PDF segun `prototipos/pdf-fix-v2.html` (aprobado y verificado)

Change `fix-pdf-totales-tarjeta-pago`, aplicado sobre `pdf/PdfService.java` + modelo/calculo:

- **Totales**: sin filas «Base total»/«IVA total»; queda `Base` → `IVA n%` (cuota) → `Descuento n%` solo si existe (restando, en rojo) → `TOTAL` en color. Con un solo tipo de IVA la fila es «Base» a secas; con varios, «Base 21%», etc., y un solo descuento. Cuadre visible: Base − Descuento + IVA = TOTAL (con descuento se muestran las bases brutas).
- **Modelo**: `ResumenFactura` expone `baseBruta` e `importeDescuento`; `IvaGrupo` expone `baseBruta`. Asignados en `CalculoService.resumen(...)` desde datos que ya tenia (sin divisiones inversas).
- **Tarjeta «Datos de pago»**: cabecera blanca con borde fino inferior y texto marron oscuro (`cabeceraTarjetaClara`); «Facturar a» sigue bicolor.
- **Paginacion**: totales mas compactos (ancho 44%, paddings reducidos) y espaciados del cierre menores (`espacio(doc, alto)`); el contador usa plantilla con paginas reales.
- **Bug latente corregido**: el antiguo `filaResumen` construia la celda de la etiqueta pero devolvia solo la del valor, asi que las etiquetas del resumen («Base», «IVA n%», ...) NUNCA se pintaron en los PDF anteriores. Ahora firma `void filaResumen(PdfPTable t, String etiqueta, String valor)` y anade ambas celdas.
- Tests nuevos: 3 en `CalculoServiceTest` (bruta/descuento/cuadre) y 2 en `PdfServiceTest` (totales con descuento restando, paginacion real en documento largo).

### 3. Cierre OpenSpec y commit

- Sync de ambas deltas a `openspec/specs/invoicing/spec.md` y archive de ambos cambios.
- Commit `30e4c36` "Exportar PDF desde el Historico y arreglo de totales, tarjeta de pago y paginacion del PDF, archivados en OpenSpec" (24 archivos).

## Proximos pasos

- Sin cola pendiente: la siguiente tarea la decide el usuario (se anuncia al inicio de la sesion y entra por `/opsx-propose`).

## Git

- Rama `main`, ultimo commit `30e4c36`; va 8 commits por delante de `origin/main` SIN push (el usuario no lo ha pedido).
- Arbol limpio: nada pendiente de commitear.

## Notas tecnicas que evitan perder tiempo

- Comando Maven: `& "C:\Program Files\Apache NetBeans\java\maven\bin\mvn.cmd" test` (suite completa: 44 tests, todos verdes).
- FXML: `maxWidth="USE_PREF_SIZE"` es invalido; usar `maxWidth="-Infinity"`.
- Cierre programatico de ventana: `nav.stage().fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST))`.
- Constructor de `EstadoService` (7 parametros): `(FacturaRepository, SerieRepository, VersionRepository, LineaRepository, VersionadoService, NumeroService, FacturaService)`.
- En los PDF el `.xlsx` original es referencia de formato: la columna TOTAL lleva IVA incluido (base × 1,21).
- El texto extraible de un PDF no incluye lo dibujado via plantilla/XObject (p. ej. la cifra final de «Pagina X de Y»): para testear el pie solo se puede afirmar hasta «de »; la cifra completa se comprueba a la vista.
- No crear dentro del proyecto carpetas/archivos de metadatos de IA ni documentacion no pedida.
