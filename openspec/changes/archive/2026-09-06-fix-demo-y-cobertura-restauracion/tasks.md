> Los motivos y los valores exactos están en `design.md`.

## 1. Cobertura de la restauración

- [x] 1.1 En `src/test/java/com/alcazaba/facturacion/service/BackupServiceTest.java`, dentro de `restaurarCopiaEsquemaActualNoDuplicaSiembras`, asertar justo después de `restaurarEnEmpresaActiva(copia)` y antes de cualquier `migrate` manual que `Migrations.userVersion(Database.getConnection())` es igual a `Migrations.ultimaVersion()`.
- [x] 1.2 Retirar de ese test las dos aserciones de doble migración, que duplican `MigrationsTest.migrarDosVecesNoDuplicaSiembras`. Conservar la comprobación del `nif` restaurado.
- [x] 1.3 Renombrar el test a algo que describa lo que ahora comprueba, por ejemplo `restaurarDejaLaBaseMigrada`.
- [x] 1.4 Comprobar a mano que el test falla si se retira la migración del camino de restauración, y reponerla. *(Hecho 2026-09-06: NO falla — con el baseline la copia ya lleva user_version=1 en el fichero y `crearEmpresa` migra por vía directa, así que la aserción no puede detectar el sabotaje. La aserción fija el invariante pero no es un tripwire.)*

## 2. Seed sin identificadores del esquema

- [x] 2.1 En `src/main/resources/db/seed_demo.sql`, sustituir los `tipo_iva_id` literales por subconsultas por nombre, según `design.md - D2`.
- [x] 2.2 Dejar el resto de identificadores literales del fichero como están.

## 3. Troceo del script

- [x] 3.1 En `src/main/java/com/alcazaba/facturacion/db/CargarDemo.java`, sustituir `sql.split(";")` por un troceo que solo corte en los `;` que estén fuera de una cadena entrecomillada, contemplando la comilla escapada como `''`.

## 4. Borrado de la demostración

- [x] 4.1 Sustituir el bloque de `Files.walk` de `CargarDemo` por `EmpresaManager.eliminarEmpresa(SLUG)` cuando la carpeta exista, sin capturar la excepción.
- [x] 4.2 Comprobar después que la carpeta ya no está y, si sigue, terminar con un mensaje que diga que hay que cerrar la aplicación antes de cargar la demostración.

## 5. Validación de copias de seguridad

- [x] 5.1 En `src/main/java/com/alcazaba/facturacion/service/BackupService.java`, hacer que la aceptación de una copia dependa de `estructuraCompleta(...)` y no de la comparación numérica de versiones, según `design.md - D5`.
- [x] 5.2 Reescribir el mensaje de rechazo y el aviso para que digan si el número de versión de la copia es anterior o posterior, sin afirmar que la copia sea de una versión más nueva de la aplicación.
- [x] 5.3 Test: una copia con `user_version` mayor que `Migrations.ultimaVersion()` y estructura completa se acepta; otra con estructura incompleta se rechaza con un mensaje que menciona lo que falta.

## 6. Plugin declarado

- [x] 6.1 Declarar `exec-maven-plugin` en `pom.xml` con la versión que hoy invoca `cargar_demo.bat`.
- [x] 6.2 Cambiar `cargar_demo.bat` para que use el goal corto.

## 7. Especificación

- [x] 7.1 Comprobar que el delta de `specs/invoicing/spec.md` recoge el comportamiento ante una demostración en uso, la independencia respecto a los identificadores del esquema, y la validación de copias por estructura en lugar de por número de versión.

## 8. Verificación final

- [x] 8.1 Suite completa en verde con `mvn test`.
- [x] 8.2 Reordenar a mano los cuatro `INSERT` de `tipo_iva` de `001_baseline.sql`, recargar la demostración, comprobar que cada línea conserva su tipo y deshacer el reorden.
- [x] 8.3 Añadir temporalmente al seed una descripción con un punto y coma dentro y comprobar que la carga funciona.
- [x] 8.4 Ejecutar `cargar_demo.bat` dos veces seguidas: mismo resultado, sin duplicados.
- [x] 8.5 Ejecutar `cargar_demo.bat` con la aplicación abierta en `demo` y comprobar el mensaje.
