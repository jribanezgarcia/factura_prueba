> Los casos exactos del troceo están en `design.md - D1`.

## 1. Tests de `trocear`

- [ ] 1.1 Crear `src/test/java/com/alcazaba/facturacion/db/CargarDemoTest.java` en el paquete `com.alcazaba.facturacion.db`, sin montar base de datos.
- [ ] 1.2 Cubrir los casos de la tabla de `design.md - D1`: sentencia simple, dos sentencias, punto y coma dentro de una cadena, y comilla simple escapada como `''`.
- [ ] 1.3 Comprobar que los tests fallan si se sustituye el cuerpo de `trocear` por `sql.split(";")`, y reponer el cuerpo bueno.

## 2. Quitar `tablasCoinciden`

- [ ] 2.1 Eliminar el campo `tablasCoinciden` del record `ResumenBackup` en `src/main/java/com/alcazaba/facturacion/service/BackupService.java`.
- [ ] 2.2 Quitar la variable local `boolean tablasCoinciden = true;` y su uso en la construcción del record.
- [ ] 2.3 Comprobar que `src/main/java/com/alcazaba/facturacion/ui/BackupController.java` no lo referencia.
- [ ] 2.4 Retirar `assertTrue(r.tablasCoinciden())` de `src/test/java/com/alcazaba/facturacion/service/BackupServiceTest.java`, sin tocar el resto de ese test.

## 3. Verificación final

- [ ] 3.1 Suite completa en verde con `mvn test`.
- [ ] 3.2 `cargar_demo.bat`: la demostración se carga con sus 6 facturas.
