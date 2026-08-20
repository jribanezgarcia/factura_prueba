# Continuacion del proyecto de facturacion

Estado actualizado: 20/08/2026

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

- `openspec/changes/archive/2026-08-16-add-invoicing-app`
- `openspec/changes/archive/2026-08-16-add-spanish-tax-id-validation`
- `openspec/changes/archive/2026-08-20-pdf-export-sin-version`
- `openspec/changes/archive/2026-08-20-temas-y-navegacion`

El rediseño de temas se formalizo en OpenSpec el 20/08/2026 (cambio `2026-08-20-temas-y-navegacion`, archivado tras `propose` → `sync-specs`).

## Cambios realizados hoy

### 1. Commit explicito al editar facturas

Archivo:

- `src/main/java/com/alcazaba/facturacion/service/FacturaService.java`

Se corrigio `guardarEditada(...)` para que no devuelva antes de confirmar la transaccion.

Antes:

- sobrescribia/creaba version y hacia `return` dentro del `try`;
- el commit quedaba implicito al llamar `Database.endTransaction()` y cambiar `autoCommit` a `true`.

Ahora:

- guarda el resultado en una variable;
- ejecuta `Database.commit()`;
- devuelve la factura/version guardada.

Tests ejecutados:

- `FacturaServiceTest`: 2 tests, 0 fallos.
- suite completa: 30 tests, 0 fallos antes del siguiente cambio.

### 2. Historico ordenado por numero de factura

Decision del usuario:

> El historico debe estar ordenado por numero de factura, puesto que si estan ordenados por numero por fecha tambien deben de estar ordenados.

Archivos tocados:

- `src/main/java/com/alcazaba/facturacion/repository/HistorialRepository.java`
- `src/main/java/com/alcazaba/facturacion/service/HistorialService.java`
- `src/test/java/com/alcazaba/facturacion/service/HistorialServiceTest.java`

Cambio:

- el `ORDER BY` del historico pasa de ordenar por fecha primero a ordenar por:
  - serie;
  - correlativo;
  - version;
  - fecha como desempate final.

No se ordena alfabeticamente por el texto del numero para evitar errores tipo `C-10` antes que `C-2`.

Test nuevo:

- `HistorialServiceTest.buscaOrdenadoPorNumeroDeFactura`
- crea `C-2/9` y `C-1/10` con fechas inversas;
- comprueba que el historico devuelve primero `C-1/10`.

Verificacion:

- suite completa: 31 tests, 0 fallos, `BUILD SUCCESS`.

Comando Maven usado:

```bat
C:\Users\juan\.m2\wrapper\dists\apache-maven-3.8.5-bin\5i5jha092a3i37g0paqnfr15e0\apache-maven-3.8.5\bin\mvn.cmd test
```

Maven no esta en `PATH`.

### 3. Exportacion PDF sin informacion de version

Decision del usuario:

- mantener todo el versionado interno y visible de la aplicacion;
- no mostrar la version en el PDF exportado;
- no incluir `_vN` en el nombre del archivo PDF exportado.

Archivos tocados:

- `src/main/java/com/alcazaba/facturacion/pdf/PdfService.java`
- `src/main/java/com/alcazaba/facturacion/ui/EditorController.java`

Cambios:

- el bloque `Numero:` del PDF ya no muestra `(vN)`;
- el nombre sugerido pasa de `numero_vN.pdf` a `numero.pdf`.

El versionado sigue funcionando en la base de datos, el historico, la interfaz y los servicios internos.

### 4. Rediseno de interfaz: temas, barra de navegacion y cabecera de empresa

Commit `dee0eb1` (maquetas de temas + spec PDF sin version). Despues de ese commit se empezo la integracion real de los temas en la aplicacion. **Este trabajo NO esta commiteado** (el 20/08/2026 se formalizo en OpenSpec con el cambio `2026-08-20-temas-y-navegacion`, ya archivado; falta solo el commit de codigo).

Archivos nuevos (sin commitear):

- `src/main/java/com/alcazaba/facturacion/ui/ThemeManager.java`
- `src/main/java/com/alcazaba/facturacion/ui/BarraNavegacion.java`
- `src/main/resources/com/alcazaba/facturacion/themes/` (base.css + 7 temas)

Que hace cada cosa:

- **ThemeManager**: sistema de temas. Cada tema es un CSS con sus colores que se aplica junto a `base.css`. El tema activo se recuerda en la tabla de preferencias (clave `tema`, por defecto `biblioteca8`). Aplica el tema al cargar cada vista y permite guardarlo desde Configuracion.
- **BarraNavegacion**: barra superior con iconos SVG que aparece en todas las pantallas salvo el menu principal. Botones: Menu, Nueva factura, Historico, Clientes, Configuracion, Copia de seguridad y Salir.
- **Temas**: `biblioteca8`, `omarchy`, `esmeralda`, `terracota`, `negro-dorado`, `sakura`, `neon`.

Archivos modificados (sin commitear):

- `Main.java`: al cerrar la ventana pide confirmacion "¿Seguro que deseas salir?".
- `Navegador.java`: aplica `ThemeManager.aplicar(scene, servicios)` al crear cada escena.
- `MenuController.java`: muestra logo, nombre y NIF de la empresa en el menu principal.
- `EditorController.java`: anade barra de navegacion, logo de empresa, y el resumen deja de ser un texto en bloque y pasa a labels separados `lblBaseTotal`, `lblIvaTotal`, `lblTotal`.
- `ConfiguracionController.java`: selector de tema (`ComboBox`) que aplica el tema al vuelo y lo guarda al pulsar Guardar.
- Todos los FXML de las vistas: incluyen la barra de navegacion y los nuevos campos.

Verificacion:

- suite completa: 31 tests, 0 fallos, `BUILD SUCCESS` (ejecutada despues de los cambios de temas).

## Funcionalidades afectadas si se quitan versiones

- `Editor.fxml`
- `EditorController`
- `Versiones.fxml`
- `VersionesController`
- `Vista`
- `Navegador`
- `HistorialRepository`
- `HistorialService`
- `HistorialFila`
- `Historico.fxml`
- `HistoricoController`
- `FacturaService`
- `VersionadoService`
- `VersionRepository`
- `EstadoService`
- `RectificativaService`
- `PdfService`
- tests de servicios y UI
- `openspec/specs/invoicing/spec.md`

## Riesgos/deudas conocidos

### Git sucio

Hay artefactos compilados en `target/classes` apareciendo modificados y tambien `.idea/`.

Antes de continuar mucho mas, conviene:

- revisar/crear `.gitignore`;
- ignorar `target/`;
- decidir si `.idea/` debe quedar fuera;
- no borrar ni revertir cambios sin confirmar con el usuario.

Ademas, los cambios del rediseno de temas (seccion 4) estan pendientes de commit.

### OpenSpec

La spec activa sigue incluyendo versionado, lo cual coincide con la decision actual del usuario.

El cambio realizado solo afecta a la presentacion y al nombre del PDF exportado; no requiere eliminar ni modificar el modelo de versiones.

El rediseno de temas se formalizo el 20/08/2026 con el cambio `2026-08-20-temas-y-navegacion` (propose → sync-specs → archive). La spec principal ya lo refleja (requisitos "Temas y apariencia", "Identidad de empresa en la interfaz", y las modificaciones en "Menú y navegación", "Configuración" e "IVA").

### Series iniciales

El documento inicial decia que las series no tenian que crearse automaticamente, pero la migracion actual si crea:

- C
- P
- R

La continuidad previa ya lo trataba como decision aceptada. No tocar salvo que el usuario lo reabra.

## Estado de pruebas

Ultima ejecucion completa:

- fecha: 20/08/2026
- resultado: 31 tests, 0 fallos, 0 errores, `BUILD SUCCESS`

Avisos observados durante tests:

- warnings de Java sobre APIs nativas/restringidas;
- warning de JavaFX por configuracion en classpath/modulos;
- SLF4J sin binder, cae a NOP logger.

No bloquearon la suite.

## Como seguir usando OpenSpec desde opencode

El proyecto usa OpenSpec a traves de las skills de opencode (`/opsx-propose`, `/opsx-apply-change`, `/opsx-sync-specs`, `/opsx-archive-change`) que estan en `.opencode/skills/openspec-*`. Esas skills llaman al CLI `openspec` por debajo.

**IMPORTANTE: el CLI `openspec` ya esta instalado** (version 1.10.0, instalado con `npm install -g @fission-ai/openspec@latest`). Si en otra maquina hiciera falta: `npm install -g @fission-ai/openspec@latest`. Verificar con `openspec --version`. Alternativas: `pnpm add -g`, `bun add -g` o `yarn global add` (mismo paquete `@fission-ai/openspec`).

El flujo OpenSpec dentro de opencode es:

1. `/opsx-propose` → el usuario describe lo que quiere; se generan proposal, spec (delta), design y tasks.
2. `/opsx-apply-change` → la IA implementa los tasks en el codigo.
3. `/opsx-sync-specs` → se pliega la delta a la spec principal (`openspec/specs/invoicing/spec.md`).
4. `/opsx-archive-change` → se archiva el cambio en `openspec/changes/archive/`.

Cada cambio vive en `openspec/changes/<fecha>-<nombre>/` con un `.openspec.yaml` y se mueve a `archive/` al archivarse. La spec principal es el unico fuente de verdad persistente.

## Proximo paso recomendado (para la siguiente sesion)

Los cambios de temas (seccion 4) estan implementados, compilan, los tests pasan y el cambio OpenSpec `2026-08-20-temas-y-navegacion` ya esta formalizado y archivado. Plan para la siguiente sesion, en orden:

1. **Probar manualmente la interfaz** con los temas:
   - el selector de tema en Configuracion cambia el tema al vuelo y se recuerda al reiniciar;
   - la barra de navegacion aparece en todas las pantallas salvo el menu principal;
   - logo y datos de empresa salen en el menu principal y en el editor;
   - el resumen del editor muestra base/IVA/total por separado;
   - al cerrar la ventana pide confirmacion.
2. **Commit de los cambios de temas** si el usuario lo solicita.
