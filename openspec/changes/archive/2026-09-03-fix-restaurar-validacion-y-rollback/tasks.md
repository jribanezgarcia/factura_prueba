## 1. BackupService — comprobaciones de estructura

- [x] 1.1 Añadir la constante `TABLAS_NUCLEO` (migración `001_init.sql`: cliente, serie, tipo_iva, factura, factura_version, factura_linea, empresa, preferencias).
- [x] 1.2 Renombrar `verificarEstructura(Connection)` a `comprobarTablasNucleo(Connection)` (exige siempre las tablas núcleo; falta alguna → `ValidationException`).
- [x] 1.3 Añadir `estructuraCompleta(Connection)` (todas las tablas y columnas conocidas; devuelve `boolean` si falta algo).
- [x] 1.4 Ajustar `leerResumen` para llamar siempre a `comprobarTablasNucleo` y, solo cuando `userVersion > Migrations.ultimaVersion()`, a `estructuraCompleta`. Verificar que compila.

## 2. BackupService — rollback de `restaurarEnEmpresaActiva`

- [x] 2.1 Mover `Database.resetConnection()` a la primera línea del `catch`, antes del `Files.copy` del rescate (cerrar conexión → copiar rescate → borrar diario → reabrir → propagar).

## 3. BackupService — `restaurarComoEmpresaNueva` y excepción

- [x] 3.1 Envolver los pasos posteriores a `crearEmpresa` en `try/catch` que llame a `EmpresaManager.eliminarEmpresa(nueva.slug())` antes de propagar.
- [x] 3.2 Cambiar la firma de `restaurarEnEmpresaActiva` para lanzar `IOException` en lugar de `Exception` genérica (mantener `ValidationException`). Verificar que compila.

## 4. BackupService — `borrarDiario`

- [x] 4.1 Derivar los nombres `-wal`/`-shm` de `Database.dbPath().getFileName()` en lugar de hardcodear `facturas.db`.

## 5. UI — BackupController

- [x] 5.1 Mostrar el nombre visible de la empresa activa (`EmpresaManager.listarEmpresas()` con el slug como respaldo) en el mensaje de confirmación.
- [x] 5.2 Aplanar `aplicarReglaNif` eliminando la rama muerta (el segundo disyuntivo ya cubierto por el `if`).
- [x] 5.3 Al crear empresa nueva y responder «NO» al cambio, quedarse en la pantalla de Copias (`Backup.fxml`) en lugar de ir al menú principal.

## 6. Pruebas de servicio

- [x] 6.1 Ajustar `rechazaCopiaSinLasTablasDeLaAplicacion` para que deje caer una tabla núcleo (p. ej. `factura`) en lugar de `numero_disponible` (que ahora es legítimo que falte).
- [x] 6.2 Añadir `restaurarCopiaDeEsquemaAnteriorSeMigra`: copia con `DROP TABLE numero_disponible` y `PRAGMA user_version = 6`; restaurar se acepta y tras restaurar la activa vuelve a tener `numero_disponible` y `user_version == Migrations.ultimaVersion()`.
- [x] 6.3 Añadir `rechazaCopiaSinLasTablasNucleo`: SQLite válido con `user_version = 1` y sin `factura` → `ValidationException`.
- [x] 6.4 Añadir `restaurarComoEmpresaNuevaNoDejaBasuraSiFalla` (o documentarlo en `design.md` si montarlo resulta forzado).
- [x] 6.5 Ejecutar la suite completa (`mvn -o test` desde el proyecto) y confirmar todos los tests en verde (153 + nuevos).

## 7. Cierre

- [x] 7.1 Sincronizar la spec delta con `openspec/specs/invoicing/spec.md`.
- [x] 7.2 Archivar el change y actualizar CONTINUAR_MAÑANA.md.
- [x] 7.3 Commit y push.
