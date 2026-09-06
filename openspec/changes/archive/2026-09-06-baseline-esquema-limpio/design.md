## Context

`Migrations` (`Migrations.java:18-27`) ejecuta en orden los scripts de `SCRIPTS` según `PRAGMA user_version`, y `ultimaVersion()` es el tamaño de esa lista. Hoy son nueve.

Estado real comprobado el 06/09/2026 en `%APPDATA%\Facturacion`:

| Base | user_version | facturas | líneas |
|---|---|---|---|
| `asesoria_maria_luisa_ibanez` | 9 | 8 | 16 |
| `comercial_alcazaba` | 7 | 1 | 2 |
| `jose_maria_morales_lopez` | 7 | 3 | 4 |

No hay ningún archivo de copia de seguridad en esa carpeta. Ver proposal.md - Why.

## Goals / Non-Goals

**Goals:**

- Un esquema que se lea de una vez, sin reconstruirlo mentalmente a partir de nueve parches.
- Que las incoherencias que provocaron la 009 no puedan repetirse.
- Que recrear datos de prueba tras un cambio de esquema sea un comando.

**Non-Goals:**

- No cambia ningún cálculo, pantalla ni PDF.
- No se conserva ninguna base existente ni se ofrece camino de actualización desde ellas.
- No se toca `serie.siguiente_correlativo` (ver proposal.md - Fuera de alcance).

## Decisions

### D1. Decisión: aplanar, no acumular

`001_baseline.sql` reproduce el esquema resultante de las nueve migraciones, con los arreglos de D2 a D4. Los nueve scripts se borran y `SCRIPTS` queda con un solo elemento.

La consecuencia que hay que asumir con los ojos abiertos: **una base creada con el esquema anterior deja de poder actualizarse**. No es un efecto colateral, es el precio de aplanar, y solo es aceptable porque hoy no hay ningún dato que conservar ni ninguna copia de seguridad guardada. A partir del momento en que se emita una factura real, este change no se podría volver a hacer.

El baseline se compone leyendo los nueve scripts actuales, no de memoria: cada tabla queda con las columnas que tenía tras la última migración que la tocó.

### D2. Decisión: todos los importes son TEXT

`importe_retencion` pasa de `NUMERIC` a `TEXT NOT NULL DEFAULT '0.00'` y `total_suplidos` de `TEXT` sin default a `TEXT NOT NULL DEFAULT '0.00'`.

`TEXT` es la elección correcta aquí y no un apaño: los importes se manejan con `BigDecimal` y se serializan con `toPlainString()`, que conserva la escala exacta; `NUMERIC` los convertiría a coma flotante. Lo que había que arreglar no era el tipo, era que una sola columna usara otro.

El código ya lee ambas con `getString(...)` (`VersionRepository.java:219-223`, `HistorialRepository.java:100`) y las escribe con `toPlainString()`, así que el cambio no debería obligar a tocar los repositorios. Hay que confirmarlo, no darlo por hecho.

### D3. Decisión: siembras sin identificadores literales

Las siembras usan `INSERT` normal sin columna `id`, porque el baseline se ejecuta sobre una base recién creada y las tablas están vacías. Desaparece el `INSERT OR IGNORE` con id fijo, que es lo que dejó a una instalación sin el tipo «Suplido».

Tipos de IVA sembrados, en este orden: `IVA 21%` (21), `IVA 10%` (10), `Exento` (NULL), `Suplido` (NULL, `es_suplido = 1`).

Se mantiene `INSERT OR IGNORE INTO empresa (id) VALUES (1)`, porque ahí el id 1 es parte del `CHECK (id = 1)` de la tabla: es la fila única, no un identificador arbitrario.

### D4. Decisión: nulabilidad coherente en texto opcional

`cliente.email`, `factura_version.cli_email`, `factura_version.forma_pago` y `factura_version.realizada_por` pierden el `NOT NULL DEFAULT ''` y quedan como `TEXT`, igual que el resto de campos opcionales de esas tablas. Es la dirección segura: relajar una restricción no puede romper ninguna inserción existente, y el código ya normaliza al leer con sus helpers `nz(...)` y `nzTexto(...)`.

Es el punto menos importante de los cuatro; si complica algo, se deja como está y se dice.

### D5. Decisión: los datos de demostración son un script, no una pantalla

`src/main/resources/db/seed_demo.sql` crea una empresa ficticia con clientes, series, tipos de retención y facturas de ejemplo. Vive junto al esquema y se actualiza con él, que es lo que lo hace sobrevivir a futuros cambios.

Se ejecuta con `cargar_demo.bat` en la raíz, al estilo de `lanzar.bat`, que invoca una clase con `main` que crea la empresa `demo`, aplica las migraciones y ejecuta el script. **El script SHALL ser repetible**: si la empresa `demo` ya existe, se recrea desde cero en lugar de duplicar datos.

Los datos de demostración SHALL NOT ser datos reales de ningún cliente: nombres, NIF y direcciones inventados.

La cobertura SHALL alcanzar los casos que cuestan de montar a mano: varias series, una factura con varios tipos de IVA, una con descuento global, una con retención, una con suplido, una anulada y una rectificativa.

### D6. Decisión: qué pasa con los tests de migración

`BackupServiceTest` tiene un caso que baja una base a `user_version = 6` a golpe de `DROP COLUMN` para comprobar que restaurar una copia antigua la vuelve a migrar (líneas 239-247). Con un solo script esa premisa desaparece.

Se sustituye por lo que sí sigue siendo cierto y merece cobertura: que restaurar una copia creada con el esquema actual funciona, y que ejecutar las migraciones dos veces sobre la misma base no duplica las filas sembradas.

## Verificación

- Base nueva desde cero: `user_version = 1`, todas las tablas presentes y los cuatro tipos de IVA sembrados, uno de ellos con `es_suplido = 1`.
- `cargar_demo.bat` deja la empresa `demo` con sus facturas; ejecutarlo dos veces seguidas da el mismo resultado y no duplica nada.
- Suite completa en verde, incluidos los tests reescritos de `BackupServiceTest`.
- Recorrido manual sobre la empresa de demostración: histórico, editor, PDF y copia de seguridad.
