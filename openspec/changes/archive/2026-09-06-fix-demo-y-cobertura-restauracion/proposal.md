## Why

Al revisar `baseline-esquema-limpio` aparecieron cuatro defectos. El esquema y los datos de demostración son correctos —bases en `user_version = 1`, cuatro tipos de IVA con «Suplido» marcado, importes uniformes en `TEXT`, y los totales del seed cuadran con `CalculoService`—, pero lo que quedó alrededor tiene arreglo pendiente.

**La restauración perdió su cobertura.** El test que probaba que restaurar una copia deja la base migrada se sustituyó por `BackupServiceTest.restaurarCopiaEsquemaActualNoDuplicaSiembras`, que restaura una copia **ya al día**. El paso de migración queda en no-op: el test seguiría en verde aunque `BackupService.restaurarEnEmpresaActiva` dejara de migrar. Sus aserciones de doble migración, además, duplican lo que ya cubre `MigrationsTest.migrarDosVecesNoDuplicaSiembras`. Importará el día que exista una `002`.

**El seed de demostración reintroduce los identificadores fijos que el baseline acababa de quitar.** `seed_demo.sql` adjudica `tipo_iva_id = 4` a la línea de suplido y 1 y 2 a las demás, y esos valores dependen del orden de los `INSERT` de `001_baseline.sql`, que es otro fichero. Reordenarlos haría que la demostración asignara en silencio el tipo de IVA equivocado a cada línea.

**`CargarDemo` trocea el SQL con `split(";")`**, así que cualquier punto y coma dentro de una cadena parte una sentencia por la mitad. El seed va a crecer.

**`CargarDemo` se traga los fallos de borrado.** Con la aplicación abierta el `.db` está bloqueado en Windows, el borrado falla en silencio y el `crearEmpresa("Demo")` siguiente termina en «Ya existe una empresa con esa carpeta de datos: demo», que no dice lo que de verdad pasa. `EmpresaManager.eliminarEmpresa(slug)` ya hacía ese borrado bien, y además limpia el catálogo, pero no se reutilizó.

**El aplanado dejó el spec de copias de seguridad contradicho.** `ultimaVersion()` pasó de 9 a 1, pero `BackupService` sigue decidiendo por número: `if (uv > Migrations.ultimaVersion())` (`BackupService.java:128`) trata como «más nueva que la aplicación» **cualquier copia hecha antes del aplanado**, que llevará un 6, un 7 o un 9. Una copia anterior al soporte de suplidos se rechazaría con el mensaje «La copia es de una versión de esquema más nueva (6) que la aplicación (1)», que dice justo lo contrario de la verdad. Y el requisito «Copia de seguridad» sigue prometiendo que la aplicación acepta una copia de versión anterior y le aplica las migraciones pendientes, cosa que ya no puede cumplir porque ese historial no existe.

Hoy no hay ninguna copia guardada en `%APPDATA%\Facturacion`, así que nada está roto ahora mismo. Pero la primera copia que se restaure tras un cambio de esquema se topará con ello.

## What Changes

- El test de restauración SHALL comprobar que, tras restaurar una copia, la base queda a la última versión de esquema, de modo que retirar la migración del camino de restauración haga fallar la suite.
- `seed_demo.sql` SHALL resolver los tipos de IVA por nombre en lugar de por identificador literal.
- La carga de la demostración SHALL trocear el script respetando las cadenas entrecomilladas.
- La carga de la demostración SHALL reutilizar `EmpresaManager.eliminarEmpresa(...)` y SHALL fallar con un mensaje que diga que hay que cerrar la aplicación, en lugar de continuar tras un borrado fallido.
- `cargar_demo.bat` SHALL apoyarse en un plugin declarado en `pom.xml`, para no depender de la red en la primera ejecución.
- La validación de una copia de seguridad SHALL decidir por la estructura del archivo, no por su número de versión, y sus mensajes SHALL decir si la versión de la copia es anterior o posterior sin describir como más nueva una copia que procede de un historial de migraciones distinto.
- No cambia el esquema, ni los importes, ni ninguna pantalla.

## Capabilities

### New Capabilities

Ninguna.

### Modified Capabilities

- `invoicing`: se amplía «Datos de demostración recreables» con el comportamiento ante una demostración en uso y con la independencia respecto a los identificadores del esquema; se corrige «Copia de seguridad», que prometía migrar copias de un historial que ya no existe.

## Impact

- `src/test/java/com/alcazaba/facturacion/service/BackupServiceTest.java`: recuperar la aserción de versión tras restaurar y quitar la duplicación con `MigrationsTest`.
- `src/main/resources/db/seed_demo.sql`: subconsultas por nombre para `tipo_iva_id`.
- `src/main/java/com/alcazaba/facturacion/db/CargarDemo.java`: troceo del script y borrado mediante `EmpresaManager`.
- `pom.xml`: declarar `exec-maven-plugin`.
- `src/main/java/com/alcazaba/facturacion/service/BackupService.java`: validación por estructura y mensajes que no confundan anterior con posterior.
- Sin cambios en `001_baseline.sql`, modelos ni repositorios.
