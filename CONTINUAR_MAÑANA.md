# Continuacion del proyecto de facturacion

Estado actualizado: 30/08/2026

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

- `openspec/specs/invoicing/spec.md` (actualizada con los requisitos «Historico» y «Exportacion a PDF» tras el cierre del 22/08)

Cambios OpenSpec archivados (ademas de los anteriores):

- `openspec/changes/archive/2026-08-21-redesign-pdf-factura` (archivado el 21/08/2026)
- `openspec/changes/archive/2026-08-22-exportar-pdf-desde-historico` (archivado el 22/08/2026)
- `openspec/changes/archive/2026-08-22-fix-pdf-totales-tarjeta-pago` (archivado el 22/08/2026)
- `openspec/changes/archive/2026-08-22-pdf-fidelidad-prototipo` (archivado el 22/08/2026)
- `openspec/changes/archive/2026-08-22-pdf-etiquetas-factura-cliente` (archivado el 22/08/2026)
- `openspec/changes/archive/2026-08-22-pdf-cp-datos-pago-opcional` (archivado el 22/08/2026)
- `openspec/changes/archive/2026-08-22-guardar-version-nueva-edicion` (archivado el 22/08/2026)
- `openspec/changes/archive/2026-08-23-ventana-800-responsive` (archivado el 23/08/2026)
- `openspec/changes/archive/2026-08-24-formato-numeracion-series` (archivado el 24/08/2026)
- `openspec/changes/archive/2026-08-30-multi-empresa-ejercicio-fiscal` (archivado el 30/08/2026)

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

### 6. CP como fila propia y «Datos de pago» ocultable (change `pdf-cp-datos-pago-opcional`, cerrado)

- «Facturar a»: filas independientes etiquetadas — Nombre (negrita) / NIF / Dirección / **Código postal** / **Población** (solo localidad) / **Provincia** / Email; columna de etiquetas 32/68; filas vacías omitidas.
- «Datos de pago»: si forma de pago, vencimiento y realizada por están vacíos, la tarjeta NO se pinta; «Facturar A» conserva su ancho (49%) con hueco colspan=2 a la derecha.
- `tarjetaPago` refactorizada: recibe la lista de filas desde el nuevo helper `filasDatosPago(vc)` (se elimina el caso «—»).
- Tests: ajustados los existentes (la muestra sin pago afirma ahora que «DATOS DE PAGO» no aparece; ojo: la extracción concatena celdas SIN espacio, «04009 ALMERIA» ya no existe como cadena) + 2 nuevos (`datosDePagoVaciosOcultanLaTarjeta`, `codigoPostalYProvinciaFilasPropias`). Suite 48/48.
- Commit `8ab9683`.

### 7. Guardar como nueva versión (change `guardar-version-nueva-edicion`, cerrado)

**Contexto del problema del usuario**: al modificar una factura emitida, el guardado SOBRESCRIBÍA la única versión en su lugar (comportamiento mandado por la spec anterior) — no quedaba copia independiente exportable de la modificación ni se conservaba la previa.

- Spec delta sobre «Versionado»: al editar la última versión, la aplicación ofrece dos caminos — sobrescribir (mismo vN, flujo actual) o crear nueva versión (vN+1); la versión anterior queda intacta y ambas aparecen en Histórico exportables por separado. Escenario nuevo «Guardar como nueva versión» + «Cancelar el guardado».
- `FacturaService.guardarEditada`: sobrecarga con `boolean comoNuevaVersion` (la firma antigua delega con false — compatibilidad total). Con true, siempre `versionadoService.crearVersion(...)` aunque versionAbiertaId == ultima.getId().
- `Dialogos`: enum `ModoGuardarVersion` {SOBRESCRIBIR, NUEVA_VERSION, CANCELAR}; nuevo método en `Impl` como DEFAULT que mapea al antiguo confirmar (true→SOBRESCRIBIR, false→CANCELAR) — las implementaciones de test existentes no se rompen; la implementación real muestra tres botones («Sobrescribir versión actual» / «Guardar como nueva versión» / «Cancelar»).
- `EditorController`: sustituido el confirmar sí/no por `Dialogos.modoGuardarVersion()`; cancelar aborta; NUEVA_VERSION pasa true al servicio.
- Test servicio `guardarComoNuevaVersionConservaLaAnterior`: v2 con cambios, v1 intacta (total/observaciones/lineas), ambas listadas. Suite **49/49**.
- Commit `78624d3`.

## Sesion del 23/08/2026 (cerrada y commiteada)

### Verificacion pendiente del 22/08

El usuario confirmo que el dialogo «Sobrescribir version actual / Guardar como nueva version / Cancelar» funciona correctamente y que v1 y v2 aparecen como filas independientes en el Historico.

### Change `ventana-800-responsive` (archivado)

Motivacion: el arranque no tenia tamano propio (heredaba el prefWidth del FXML de cada pantalla), el minimo era 900x600 y varias filas se cortaban por debajo de ~1000px. Decisiones del usuario: primer arranque 800x600 redimensionable, SIN pantalla completa, responsive para que nada se corte.

- `Main.java`: minimo 800x600; sin preferencias `ventana_w/h` guardadas abre a 800x600 con `centerOnScreen()`; restaurar ultima sesion intacto (constantes `ANCHO_INICIAL`/`ALTO_INICIAL`).
- `Historico.fxml`: filtros en un FlowPane (cada grupo etiqueta+campo en un HBox propio para no separarse al envolver); Exportar PDF/Buscar/Volver en fila HBox propia abajo-derecha.
- `Configuracion.fxml`: altas rapidas de IVA y Series en FlowPane con los botones agrupados en un HBox final.
- `Editor.fxml`: cabecera GridPane con `columnConstraints` (hgrow=ALWAYS + fillWidth en columnas de valor 1/3/5); campos con `maxWidth="Infinity"` (CP acotado a 130).
- `UiSmokeTest`: tras cargar cada vista fuerza `applyCss() + resize(800,600) + layout()` para ejercitar el layout al minimo. Suite 49/49 en verde.
- Spec principal: requisito nuevo «Ventana» con 6 escenarios.
- Commit `5a4adea`.

## Sesion del 24/08/2026 (cerrada y commiteada)

### Change `formato-numeracion-series` (archivado)

- Nueva columna `sufijo_fecha` en tabla `serie` (migracion `003_formato_numeracion_series.sql`), registrada en `Migrations.java`.
- `Serie.java`: enum `SufijoFecha` (MES, ANIO, NINGUNO) + campo con getter/setter.
- `SerieRepository`: leer/escribir el nuevo campo.
- `NumeroService`: `formarNumero()` y `parseCorrelativo()` con switch por formato y soporte de codigo vacio.
- Configuracion: ComboBox de formato con ejemplo vivo (`comboSerieFormato` + `lblSerieEjemplo`) en la pestana Series.
- Tests: 10 nuevos en `NumeroServiceTest`. Suite 59/59 en verde.
- Spec principal: requisito «Numeracion por series» actualizado con los nuevos formatos y escenarios.

## Proximos pasos

- Sin cola pendiente: la siguiente tarea la decide el usuario (se anuncia al inicio de la sesion y entra por `/opsx-propose`).

## Funcionalidad pendiente: Multi-empresa (varias BD)

**Objetivo**: Permitir llevar la contabilidad de varias empresas (p.ej. "Comercial Alcazaba" y "Asesoría María Luisa Ibáñez") cada una con su propia base de datos SQLite independiente.

**Arquitectura elegida**: Una BD por empresa (archivo `facturas.db` en carpeta propia bajo `%APPDATA%/Facturacion/<slug>/`). Aislamiento total: configuración, series, clientes, facturas, IVA... todo por empresa.

### Cambios por archivo

| Archivo | Cambios |
|---------|---------|
| `Database.java` | Eliminar `dataDir` estático único. Añadir `setCurrentEmpresa(String slug)` que cambia a `%APPDATA%/Facturacion/<slug>/`. `getEmpresasDisponibles()` lista carpetas con `facturas.db`. `resetConnection()` cierra conexión antes de cambiar. |
| `EmpresaSelector.java` (nuevo) | Servicio: `listarEmpresas()`, `crearEmpresa(nombre, slug)`, `cambiarEmpresa(slug)` (cambia BD + migra + guarda preferencia), `getUltimaEmpresa()`. |
| `Main.java` | Al arrancar: lee `ultima_empresa` de config global; si existe y BD existe → `setCurrentEmpresa`; si no → **diálogo selector** antes de cargar UI. Tras selección → `Migrations.migrate()` → menú principal. |
| `ConfigRepository.java` | Preferencia global `ultima_empresa` en archivo `%APPDATA%/Facturacion/empresas.properties` (fuera de BD de empresa). Formato: `ultima_empresa=slug`, `slug.nombre=Nombre visible`. |
| `Servicios.java` | `recargarParaEmpresa(String slug)`: `Database.setCurrentEmpresa(slug)`, `Migrations.migrate()`, re-instanciar **todos** los repositorios y servicios que dependen de ellos. |
| `ConfiguracionController.java` | Nueva pestaña/sectión "Empresas": tabla (nombre, slug, última apertura), botones "Nueva empresa", "Cambiar a esta", "Eliminar" (solo si no es actual). "Nueva empresa" → pide nombre → genera slug → crea carpeta → migra → guarda como última. |
| `BarraNavegacion.java` | Item "Cambiar empresa..." que abre selector (diálogo o pantalla config). |
| `Migrations.java` | Sin cambios (ya usa `PRAGMA user_version` por BD). Al crear empresa nueva, `migrate()` crea tablas desde 001_init. |
| `BackupService.java` | Sin cambios (usa `Database.dbPath()` que ya apunta a la BD actual). |

### Orden de implementación recomendado

1. `Database.java` - conexión dinámica + `getEmpresasDisponibles()`
2. `ConfigRepository` - preferencia global `ultima_empresa` en properties
3. `EmpresaSelector` - nueva clase servicio
4. `Main.java` - selector al arranque
5. `Servicios.java` - `recargarParaEmpresa()` re-instancia todo
6. `ConfiguracionController` - pestaña "Empresas"
7. `BarraNavegacion` - acceso rápido
8. Tests - verificar aislamiento entre empresas

### Nota importante

Al cambiar de empresa, **toda la capa de repositorios y servicios se re-instancia** (`Servicios.recargarParaEmpresa()`). Los controladores ya implementan `alIniciar()` que recarga datos, así que basta con `nav.mostrar("/MenuPrincipal.fxml")` tras el cambio.

### Pendiente de decidir (para `/opsx-propose`)

- ¿Selector al arrancar **diálogo modal** o **pantalla completa**?
- ¿Migración de datos existentes? (La BD actual en `%APPDATA%/Facturacion/facturas.db` → mover a `%APPDATA%/Facturacion/comercial_alcazaba/facturas.db`)
- ¿Compartir algo global? (Temas UI, preferencias de ventana, atajos)

---

## Git

- Rama `main`, ultimo commit `0beed22`; **SINCRONIZADA** con `origin/main` (push realizado el 24/08/2026).
- Arbol limpio: nada pendiente de commitear.

## Notas tecnicas que evitan perder tiempo

- Comando Maven: `& "C:\Program Files\Apache NetBeans\java\maven\bin\mvn.cmd" test` (suite completa: 49 tests, todos verdes). IMPORTANTE: lanzar maven siempre desde el directorio del proyecto; si se lanza desde otro workdir falla sin POM y los pasos siguientes usan clases viejas.
- Para inspeccionar PDFs visualmente: rasterizar pagina con `Windows.Data.Pdf` desde PowerShell 5.1 (`render.ps1` en %TEMP%\opencode\pdfcheck) y leer el PNG; el modelo no lee PDFs directamente.
- `PdfPCellEvent.cellLayout(PdfCell, Rectangle, PdfContentByte[])` dibuja DESPUES del contenido: usar `canvases[PdfPTable.TEXTCANVAS]` para contornos; para fondo+texto juntos, pintar ambos dentro del evento con celda de frase vacia. `PdfReader.getPageN(1).getAsDict(PdfName.RESOURCES)` + `PdfDictionary.getKeys()` para inspeccionar fuentes embebidas (no existe `getPageResources`).
- FXML: `maxWidth="USE_PREF_SIZE"` es invalido; usar `maxWidth="-Infinity"`. Para que un control CREZCA dentro de una celda de GridPane con `hgrow` hacen falta AMBAS cosas: `ColumnConstraints hgrow="ALWAYS" fillWidth="true"` y `maxWidth="Infinity"` en el control (los controles no crecen por defecto). Las filas que deben envolver usan `FlowPane` con cada grupo etiqueta+campo en su propio HBox (FlowPane no tiene hgrow).
- El smoke test de UI no muestra ventanas: para ejercitar layout real usa `root.applyCss(); root.resize(800,600); root.layout();`.
- Cierre programatico de ventana: `nav.stage().fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST))`.
- Constructor de `EstadoService` (7 parametros): `(FacturaRepository, SerieRepository, VersionRepository, LineaRepository, VersionadoService, NumeroService, FacturaService)`.
- En los PDF el `.xlsx` original es referencia de formato: la columna TOTAL lleva IVA incluido (base × 1,21).
- El texto extraible de un PDF no incluye lo dibujado via plantilla/XObject (p. ej. la cifra final de «Pagina X de Y»): para testear el pie solo se puede afirmar hasta «de »; la cifra completa se comprueba a la vista.
- No crear dentro del proyecto carpetas/archivos de metadatos de IA ni documentacion no pedida.
