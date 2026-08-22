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
- `openspec/changes/archive/2026-08-22-pdf-fidelidad-prototipo` (archivado el 22/08/2026)
- `openspec/changes/archive/2026-08-22-pdf-etiquetas-factura-cliente` (archivado el 22/08/2026)

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

### 4. Fidelidad del PDF al prototipo (change `pdf-fidelidad-prototipo`, cerrado)

El usuario comparo un PDF real con `prototipos/pdf-fix-v2.html` y reporto solapes y desviaciones. Implementado sobre `pdf/PdfService.java`:

- **Cabecera sin solapes**: columna derecha reservada (`RESERVA_FACTURA = 170pt`); nombre/actividad/contacto se miden con `getWidthPoint` y reducen tamano por pasos hasta caber (minimo 9pt). Serie/Nº y fecha siempre visibles.
- **Calibri embebida**: `calibri.ttf/calibrib/calibrii/calibriz` desde `%WINDIR%\Fonts`, cacheadas en estaticos; fallback Helvetica. Helpers `baseRegular()/baseNegrita()/baseCursiva()`.
- **Bordes redondeados**: eventos `PdfPCellEvent` en `TEXTCANVAS`: contorno curvo de tarjetas (7pt) y observaciones (6pt); los rotulos se pintan enteros en el evento (fondo redondeado arriba + titulo redibujado) para que el acento no sobresalga; chip NIF redondeado (2pt).
- **Pie corregido**: hueco fijo de 2 digitos para el total; ademas contador propio de paginas en `onEndPage` porque el writer contaba una pagina fantasma al cerrar («de 2» en documentos de 1 pagina).
- **Colores**: neutrales fijos segun prototipo (tinta #3A332B, gris #5F5548, etiquetas #A2937F, valores pago #C4BAAC); derivados del acento intactos.
- **Verificacion**: bucle visual automatizado (generar muestra -> rasterizar con Windows.Data.Pdf -> comparar con el prototipo), incluido caso de nombre larguisimo sin solape. Suite completa 46/46 en verde.
- Commit `c2fcd70` "Fidelidad del PDF al prototipo: ..." (8 archivos).

### 5. Etiquetas del bloque FACTURA y campos de «Facturar a» (change `pdf-etiquetas-factura-cliente`, cerrado)

- Bloque FACTURA: rótulos pequeños marrones (`SERIE / Nº`, `FECHA`, 6.5pt negrita) encima del número y la fecha; ambos valores ahora en negrita 10pt tinta.
- Tarjeta «Facturar a»: pares etiqueta→valor (Nombre en negrita 10.5 / NIF / Dirección / Población / Email) con etiquetas 8.5pt `GRIS_CLARO`; Población = CP + localidad (+ provincia entre paréntesis si existe); filas vacías omitidas. Misma estructura interna que «Datos de pago».
- Suite 46/46 en verde; bucle visual verificado con rasterización.
- Commit `5d995f3` "Etiquetas Serie/Nº y Fecha ... archivado en OpenSpec" (8 archivos).

## Proximos pasos

- Sin cola pendiente: la siguiente tarea la decide el usuario (se anuncia al inicio de la sesion y entra por `/opsx-propose`).

## Git

- Rama `main`, ultimo commit `5d995f3`; va 10 commits por delante de `origin/main` SIN push (el usuario no lo ha pedido).
- Arbol limpio: nada pendiente de commitear.

## Notas tecnicas que evitan perder tiempo

- Comando Maven: `& "C:\Program Files\Apache NetBeans\java\maven\bin\mvn.cmd" test` (suite completa: 46 tests, todos verdes). IMPORTANTE: lanzar maven siempre desde el directorio del proyecto; si se lanza desde otro workdir falla sin POM y los pasos siguientes usan clases viejas.
- Para inspeccionar PDFs visualmente: rasterizar pagina con `Windows.Data.Pdf` desde PowerShell 5.1 (`render.ps1` en %TEMP%\opencode\pdfcheck) y leer el PNG; el modelo no lee PDFs directamente.
- `PdfPCellEvent.cellLayout(PdfCell, Rectangle, PdfContentByte[])` dibuja DESPUES del contenido: usar `canvases[PdfPTable.TEXTCANVAS]` para contornos; para fondo+texto juntos, pintar ambos dentro del evento con celda de frase vacia. `PdfReader.getPageN(1).getAsDict(PdfName.RESOURCES)` + `PdfDictionary.getKeys()` para inspeccionar fuentes embebidas (no existe `getPageResources`).
- FXML: `maxWidth="USE_PREF_SIZE"` es invalido; usar `maxWidth="-Infinity"`.
- Cierre programatico de ventana: `nav.stage().fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST))`.
- Constructor de `EstadoService` (7 parametros): `(FacturaRepository, SerieRepository, VersionRepository, LineaRepository, VersionadoService, NumeroService, FacturaService)`.
- En los PDF el `.xlsx` original es referencia de formato: la columna TOTAL lleva IVA incluido (base × 1,21).
- El texto extraible de un PDF no incluye lo dibujado via plantilla/XObject (p. ej. la cifra final de «Pagina X de Y»): para testear el pie solo se puede afirmar hasta «de »; la cifra completa se comprueba a la vista.
- No crear dentro del proyecto carpetas/archivos de metadatos de IA ni documentacion no pedida.
