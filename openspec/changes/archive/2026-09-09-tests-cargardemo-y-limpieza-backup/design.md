## Context

`CargarDemo.trocear(String)` (`CargarDemo.java:33-56`) recorre el script carácter a carácter y solo corta en un `;` que esté fuera de una cadena, tratando `''` como comilla escapada. No tiene tests.

`BackupService.leerResumen(...)` construye `ResumenBackup` con `tablasCoinciden` (`BackupService.java:134` y `:169`). Desde que la validación rechaza toda copia con estructura incompleta, ese valor solo puede ser `true`. Ver proposal.md - Why.

## Goals / Non-Goals

**Goals:**

- Cubrir el troceo del script, incluida su rama de escape.
- Quitar un campo que ya no puede tomar más de un valor, y el test que lo comprueba.

**Non-Goals:**

- No se toca la validación de copias ni sus mensajes.
- No se vuelve sobre la restauración de copias de versiones anteriores: con un único esquema no existen, y el requisito ya lo refleja.
- No se cambia `CargarDemo` más allá de lo que exija el test.

## Decisions

### D1. Decisión: `trocear` se prueba directamente, sin base de datos

El método es `static` y de paquete, así que el test vive en `com.alcazaba.facturacion.db` y lo llama sin montar ninguna base ni ejecutar el seed. Casos:

| Entrada | Resultado esperado |
|---|---|
| `SELECT 1;` | una sentencia |
| `SELECT 1; SELECT 2;` | dos sentencias |
| `INSERT INTO t VALUES ('Montaje; incluye transporte');` | **una** sentencia; el `;` de dentro de la cadena no corta |
| `INSERT INTO t VALUES ('L''Hospitalet; centro');` | una sentencia, con la comilla escapada intacta en el texto |
| script con línea en blanco final | las partes vacías se descartan al ejecutarlas, como ya hace `main` |

La comprobación de la comilla escapada es la que importa: sin ella, `enCadena` se invertiría en mitad de la cadena y el `;` posterior partiría la sentencia.

### D2. Decisión: quitar `tablasCoinciden`, no dejarlo por si acaso

Se elimina del record `ResumenBackup`, de la llamada que lo construye y de la aserción del test. Un campo que solo puede valer `true` no informa de nada y sugiere una distinción que ya no existe.

Alternativa descartada: devolverle sentido haciendo que la validación acepte copias incompletas y marque la diferencia. Sería volver a permitir restaurar copias que no encajan, justo lo que se acaba de cerrar.

## Verificación

- Los tests nuevos fallan si se sustituye el cuerpo de `trocear` por un `sql.split(";")`.
- Suite completa en verde.
- `cargar_demo.bat` sigue funcionando: la demostración se carga con sus 6 facturas.
