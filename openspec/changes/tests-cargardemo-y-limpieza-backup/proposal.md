## Why

Al revisar `fix-demo-y-cobertura-restauracion` quedaron dos cabos sueltos, ninguno de comportamiento: son cobertura y código muerto.

`CargarDemo.trocear(...)` es lógica pura, `static` y de paquete, con una rama de escape (la comilla simple duplicada dentro de una cadena) que nadie ejercita. Es el test más barato del proyecto y no se pidió al escribir la tarea que introdujo el método.

`BackupService.ResumenBackup.tablasCoinciden` se quedó sin sentido. Desde que `elementosFaltantes(...)` rechaza cualquier copia con estructura incompleta, el campo se asigna a `true` fijo (`BackupService.java:134`), viaja en el record hasta la interfaz, y `BackupController` ya no lo consulta. `BackupServiceTest` lo comprueba con un `assertTrue` que no puede fallar: un test que no distingue nada.

Este change no vuelve sobre la restauración de copias de versiones anteriores. La aplicación está en desarrollo, no hay ni versión beta en uso, y con un único esquema no existen copias atrasadas: el requisito ya dice que una copia con estructura incompleta se rechaza.

## What Changes

- `CargarDemo.trocear(...)` SHALL quedar cubierto por tests que ejerciten al menos: una sentencia simple, varias sentencias, un punto y coma dentro de una cadena, y una comilla simple escapada como `''`.
- El campo `tablasCoinciden` SHALL desaparecer de `ResumenBackup` y de todo lo que lo consume, por no poder tomar ya más de un valor.
- SHALL retirarse la aserción `assertTrue(r.tablasCoinciden())` de `BackupServiceTest`, que no puede fallar.
- No cambia ningún comportamiento visible de la aplicación.

## Capabilities

### New Capabilities

Ninguna.

### Modified Capabilities

Ninguna. Es cobertura y limpieza de código muerto; el comportamiento especificado no cambia.

## Impact

- `src/test/java/com/alcazaba/facturacion/db/CargarDemoTest.java` (nuevo).
- `src/main/java/com/alcazaba/facturacion/service/BackupService.java`: quitar `tablasCoinciden` del record y de su construcción.
- `src/main/java/com/alcazaba/facturacion/ui/BackupController.java`: comprobar que no queda ninguna referencia.
- `src/test/java/com/alcazaba/facturacion/service/BackupServiceTest.java`: retirar la aserción tautológica.
