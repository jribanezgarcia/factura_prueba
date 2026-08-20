# Continuacion del proyecto de facturacion

Estado actualizado: 20/08/2026

Este documento sirve como traspaso para continuar con cualquier IA. El proyecto se esta construyendo con OpenCode/OpenSpec. Antes de tocar codigo, leer:

- `facturacion_openspec_explore.md`
- `openspec/specs/invoicing/spec.md`
- este archivo

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

### OpenSpec

La spec activa sigue incluyendo versionado, lo cual coincide con la decision actual del usuario.

El cambio realizado solo afecta a la presentacion y al nombre del PDF exportado; no requiere eliminar ni modificar el modelo de versiones.

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

## Proximo paso recomendado

Probar manualmente la exportacion desde la interfaz y comprobar que:

1. el PDF no muestra ninguna referencia a la version;
2. el archivo se propone como `numero.pdf`;
3. el historial y la pantalla de versiones siguen mostrando el versionado normalmente.

Despues de esa comprobacion, se puede hacer un commit de los cambios si el usuario lo solicita.
