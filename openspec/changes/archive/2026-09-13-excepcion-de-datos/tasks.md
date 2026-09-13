> Las razones y los fragmentos exactos están en `design.md`. No cambia ningún comportamiento ni mensaje visible. No añadir comentarios en el código.

## 1. Excepción y base de datos

- [x] 1.1 Crear `src/main/java/com/alcazaba/facturacion/db/DatosException.java` tal como está en `design.md - D1`.
- [x] 1.2 En `db/Database.java`, en `commit`, `rollback`, `beginTransaction` y `endTransaction`, cambiar `new RuntimeException(...)` por `new DatosException(...)` con el mismo texto. No tocar `getConnection` ni el resto de la clase. Ver `design.md - D5`.

## 2. Repositorios

- [x] 2.1 En los diez `src/main/java/com/alcazaba/facturacion/repository/*Repository.java`, quitar `throws SQLException` de cada método **público** y capturar `SQLException` para lanzar `new DatosException(e)`. Ver `design.md - D2`.
- [x] 2.2 Mantener `throws SQLException` en los métodos privados.
- [x] 2.3 No cambiar ninguna consulta, parámetro ni orden de sentencias.

## 3. Servicios

- [x] 3.1 En `ClienteService`, `SerieService`, `IvaService`, `RetencionService`, `ConfigService`, `NumeroService`, `VersionadoService`, `FacturaService`, `EstadoService`, `RectificativaService`, `FacturacionMensualService` y `HistorialService`, quitar `SQLException` de todas las firmas (conservando `ValidationException`) y su import. Ver `design.md - D3`.
- [x] 3.2 En los `catch` de transacción de `FacturaService` (crear, editar, borrar), `EstadoService` y `FacturacionMensualService`, quitar `SQLException` del multi-catch manteniendo el `rollback` y el relanzamiento.
- [x] 3.3 En `VersionadoService`, cambiar `throw new java.sql.SQLException("La version " + versionId + " no existe")` por `throw new DatosException(...)` con el mismo texto. Ver `design.md - D4`.
- [x] 3.4 En `Servicios`, quitar `throws SQLException` de los dos constructores y envolver `Database.getConnection()` en `try/catch` que lance `DatosException`. Ver `design.md - D6`.
- [x] 3.5 No tocar `BackupService` ni `EmpresaManager`. Ver `design.md - D7`.

## 4. Interfaz

- [x] 4.1 En `ui/EditorController.java`, quitar `throws java.sql.SQLException` de `proponerDestinoPdf`.
- [x] 4.2 No tocar `BackupController`.

## 5. Comprobaciones

- [x] 5.1 Buscar `SQLException` en `src/main/java/com/alcazaba/facturacion/service` y `ui`: solo puede aparecer en `BackupService`, `EmpresaManager` y `BackupController`.
- [x] 5.2 Revisar los cinco bloques de transacción y confirmar que un `RuntimeException` (y por tanto `DatosException`) sigue provocando `Database.rollback()`.
- [x] 5.3 Buscar `throws SQLException` en métodos `public` de `src/main/java/com/alcazaba/facturacion/repository`: no debe quedar ninguno.

## 6. Tests

- [x] 6.1 Compilar los tests (`mvn -q test-compile`) y cambiar los `catch (SQLException e)` que ya no compilen (como `ui/ClientesNifValidationTest.java` 197 y 205) por `catch (DatosException e)` o `catch (RuntimeException e)`, sin cambiar lo que comprueban.
- [x] 6.2 No es obligatorio quitar los `throws SQLException` de los métodos de test que sigan compilando.
- [x] 6.3 No cambiar lo que comprueba ningún test.

## 7. Verificación

- [x] 7.1 `mvn test` en verde.
- [x] 7.2 Arrancar la aplicación y recorrer: crear y editar una factura, anular y restaurar, crear una rectificativa, generar facturas mensuales, crear y borrar un cliente, crear una serie y exportar un PDF.
- [x] 7.3 Si hay una forma sencilla de provocar desde la interfaz un error que venga de la base de datos, comprobar que el mensaje mostrado es el mismo que antes del cambio. Si no la hay, anotarlo y no forzarlo.
