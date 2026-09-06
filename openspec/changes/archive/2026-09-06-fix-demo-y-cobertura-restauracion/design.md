## Context

`BackupService.restaurarEnEmpresaActiva(...)` (`BackupService.java:176-190`) copia el fichero restaurado sobre `Database.dbPath()` y llama a `Database.getConnection()`, que es donde se ejecutan las migraciones. Ese es el enganche que el test antiguo cubría y el nuevo ya no.

`CargarDemo` (`CargarDemo.java`) borra a mano la carpeta de la empresa `demo`, llama a `EmpresaManager.crearEmpresa("Demo")` —que crea carpeta, base migrada y entrada de catálogo— y después ejecuta `db/seed_demo.sql` troceado por `;`.

`EmpresaManager.eliminarEmpresa(slug)` (`EmpresaManager.java:88-99`) quita la entrada del catálogo y borra la carpeta recursivamente. Rechaza la empresa activa comparando con `Sesion.empresaSlug()`, que en el proceso aparte de `CargarDemo` no está inicializada, así que no estorba.

`Database.getEmpresasDisponibles()` (`Database.java:68-81`) lista carpetas del disco que contengan `facturas.db`, no entradas de catálogo: por eso `crearEmpresa` falla si la carpeta sobrevivió al borrado. Ver proposal.md - Why.

## Goals / Non-Goals

**Goals:**

- Que la suite falle si alguien retira la migración del camino de restauración.
- Que la demostración no dependa del orden de las siembras del esquema.
- Que ejecutar la carga con la aplicación abierta lo diga.

**Non-Goals:**

- No se toca `001_baseline.sql` ni ningún dato de la demostración: los importes ya cuadran.
- No se reintroduce la premisa del esquema antiguo en los tests; esas bases ya no existen.
- No se convierte la carga de la demostración en una pantalla de la aplicación.

## Decisions

### D1. Decisión: asertar la versión tras restaurar, no resucitar el test antiguo

En `restaurarCopiaEsquemaActualNoDuplicaSiembras`, justo después de `servicio.restaurarEnEmpresaActiva(copia)` y **antes** de cualquier `Migrations.migrate(...)` manual, comprobar que `Migrations.userVersion(Database.getConnection())` es igual a `Migrations.ultimaVersion()`.

Con eso el test vuelve a cubrir el cableado sin necesitar una copia de esquema antiguo, que es lo que ya no se puede fabricar.

**Resultado real (06/09/2026): esta decisión no consigue lo que pretendía.** Al comprobarlo a mano (tarea 1.4), el test **no falla** si se retira la migración del camino de restauración: con un único esquema, la copia restaurada ya trae `user_version = 1` grabada en el fichero, así que la aserción se cumple venga o no de haber migrado. Queda como invariante documentado, no como detector.

El hueco solo se puede cerrar el día que exista una segunda migración y, por tanto, una copia atrasada que fabricar. Mientras la aplicación esté en desarrollo con un solo esquema, no hay versiones anteriores: por eso el requisito de copias pasa a decir que una copia con estructura incompleta se rechaza, en lugar de prometer que se actualiza.

Las dos aserciones de doble migración se retiran de este test: `MigrationsTest.migrarDosVecesNoDuplicaSiembras` ya cubre exactamente eso, y aquí solo añadían ruido. Lo que sí se conserva es la comprobación de que el `nif` restaurado es el de la copia y no el que se escribió después, que es el objeto del test.

### D2. Decisión: el seed resuelve los tipos de IVA por nombre

En `seed_demo.sql`, cada `tipo_iva_id` pasa a ser una subconsulta:

```sql
(SELECT id FROM tipo_iva WHERE nombre = 'IVA 21%')
(SELECT id FROM tipo_iva WHERE nombre = 'IVA 10%')
(SELECT id FROM tipo_iva WHERE nombre = 'Suplido')
```

El resto de identificadores literales del fichero —`serie_id`, `factura_id`, `factura_version_id`, `tipo_retencion_id`— se dejan como están: las filas a las que apuntan las crea el propio `seed_demo.sql` unas líneas más arriba, así que el orden lo controla el mismo fichero que lo asume.

### D3. Decisión: trocear respetando las comillas

El troceo recorre el texto carácter a carácter y solo corta en un `;` que esté fuera de una cadena entrecomillada, teniendo en cuenta que en SQL una comilla simple se escapa duplicándola (`''`).

Alternativa descartada: obligar a que cada sentencia ocupe una línea. Hace ilegible un `INSERT` de varias filas como los que ya tiene el seed.

### D4. Decisión: borrar con `EmpresaManager` y fallar a la vista

`CargarDemo` sustituye su bloque de `Files.walk` por `EmpresaManager.eliminarEmpresa(SLUG)` cuando la carpeta existe, sin capturar la excepción.

Antes de eso, comprobar que la carpeta se ha ido de verdad. Si sigue ahí, terminar con un mensaje explícito del tipo «No se ha podido eliminar la empresa de demostración: cierra la aplicación antes de cargarla», en vez de dejar que reviente más adelante hablando de carpetas existentes.

### D5. Decisión: la copia se juzga por su estructura, no por su número

`BackupService.leerResumen(...)` decide hoy con `if (uv > Migrations.ultimaVersion())` (`BackupService.java:128`). Ese número dejó de significar lo mismo al aplanar: una copia anterior al aplanado lleva un 6, un 7 o un 9, y la aplicación lleva un 1, así que toda copia vieja se juzga como «más nueva».

La comparación numérica pasa a ser solo una **señal para el mensaje**, no el criterio: lo que decide es `estructuraCompleta(c)`, que ya existe. Si la estructura está completa, se acepta avisando; si no, se rechaza. El texto del aviso y del rechazo dice si el número de la copia es anterior o posterior, sin afirmar que proceda de una versión más nueva de la aplicación.

Alternativa descartada: renumerar el baseline a 10 para que los números sigan creciendo. Disfraza el problema —la copia vieja seguiría sin poder migrarse— y además reintroduce en el esquema la aritmética de versiones que acabamos de simplificar.

### D6. Decisión: declarar el plugin en el `pom`

`exec-maven-plugin` en la versión que ya usa `cargar_demo.bat` se declara en `pom.xml`, y el `.bat` pasa a invocar el goal corto. Así la primera ejecución no depende de tener red.

## Verificación

- Quitar temporalmente la llamada que migra en el camino de restauración: la suite debe fallar. Reponerla.
- Reordenar a mano los cuatro `INSERT` de `tipo_iva` de `001_baseline.sql`, recargar la demostración y comprobar que cada línea conserva su tipo. Deshacer el reorden.
- Añadir al seed una descripción con un punto y coma dentro y comprobar que la carga sigue funcionando.
- Ejecutar la carga con la aplicación abierta en `demo` y leer el mensaje.
