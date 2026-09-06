## Why

El esquema se ha ido formando por acumulación a lo largo de nueve migraciones, y dos de ellas existen solo para tapar la anterior: la 006 parchea a la 005 y la 009 a la 008. El resultado arrastra incoherencias que ya han costado un fallo real:

- **Tipos de dinero dispares.** Todos los importes se guardan como `TEXT` —`base_total`, `iva_total`, `total`, `precio_unitario`, `total_base`, `iva_importe`, `total_suplidos`— salvo `importe_retencion`, que la 005 declaró `NUMERIC`. El código lo escribe con `toPlainString()` y lo lee con `getString(...)`, y solo funciona porque SQLite tiene tipado dinámico.
- **Siembras con identificadores literales.** `001` e `008` insertan tipos de IVA con `id` fijo y `INSERT OR IGNORE`. Cuando el id ya estaba ocupado, la inserción se descartó en silencio: en la base de `asesoria_maria_luisa_ibanez` el id 4 lo tenía un tipo creado por el usuario, así que el tipo «Suplido» nunca llegó a existir y hubo que añadir la 009 para repararlo.
- **Nulabilidad dispar.** La 002 añadió `cliente.email` como `NOT NULL DEFAULT ''` mientras `nif`, `direccion`, `cp`, `localidad` y `provincia` siguen aceptando nulos desde la 001.

Todas las bases de datos existentes son empresas de prueba y no contienen nada que haya que conservar: 12 facturas repartidas en tres empresas, y ningún archivo de copia de seguridad en `%APPDATA%\Facturacion`. Aplanar el historial en un baseline limpio solo es posible mientras eso sea cierto: en cuanto exista una factura real, la ventana se cierra para siempre.

Falta además una forma barata de recrear datos de prueba. Hoy, cambiar el esquema obliga a volver a teclear empresas, clientes y facturas a mano, lo que desincentiva justamente los cambios de esquema que conviene hacer ahora.

## What Changes

- El historial de migraciones SHALL reducirse a un único `001_baseline.sql` que reproduzca el esquema consolidado de las nueve migraciones actuales.
- Todos los importes SHALL guardarse con el mismo tipo: `TEXT` con `DEFAULT '0.00'`. `importe_retencion` y `total_suplidos` SHALL dejar de ser la excepción.
- Las siembras SHALL NOT fijar identificadores literales. Los tipos de IVA sembrados SHALL ser «IVA 21%», «IVA 10%», «Exento» y «Suplido», este último con `es_suplido = 1`.
- Las columnas de texto opcionales SHALL admitir nulos de forma coherente, y el código SHALL seguir normalizándolas al leerlas como hace hoy.
- SHALL añadirse un script de datos de demostración, versionado junto al esquema, que cree una empresa ficticia con clientes, series y facturas de ejemplo, ejecutable bajo demanda y repetible.
- Las tres bases de datos de prueba actuales SHALL borrarse.
- El comportamiento de la aplicación SHALL NOT cambiar: mismos cálculos, mismas pantallas, mismos PDF.

## Capabilities

### New Capabilities

Ninguna.

### Modified Capabilities

- `invoicing`: se añade el requisito «Base de datos desde un esquema único» y se ajusta «Suplidos», cuya garantía sobre el tipo pasa a cumplirla la siembra del baseline.

## Impact

- `src/main/resources/db/migrations/`: se sustituyen los nueve scripts por `001_baseline.sql`.
- `db/Migrations.java`: `SCRIPTS` pasa a un solo elemento; `ultimaVersion()` pasa a 1.
- `src/main/resources/db/seed_demo.sql` (nuevo) y su lanzador.
- `repository/VersionRepository.java` y `repository/HistorialRepository.java`: revisar la lectura y escritura de `importe_retencion`, que pasa de `NUMERIC` a `TEXT`.
- `src/test/java/com/alcazaba/facturacion/service/BackupServiceTest.java`: el caso que baja una base a `user_version = 6` con `DROP COLUMN` se queda sin premisa y hay que rehacerlo.
- `src/test/java/com/alcazaba/facturacion/pdf/PdfServiceTest.java`: arreglar la aserción sobre «Exento», que hoy no comprueba lo que dice.
- Bases de datos de `%APPDATA%\Facturacion`: se borran las tres de prueba.

## Fuera de alcance

- Retirar `serie.siguiente_correlativo`, duplicada por la tabla `serie_siguiente` desde la 004. El campo del modelo se reutiliza como portador del valor por año que lee `ConfiguracionController` (línea 737) y `FacturaService` (línea 136) lo usa para avanzar el contador, así que tocarlo cambia cómo se numeran las facturas. Va en un change propio.
- Rediseñar el modelo de datos. `factura` + `factura_version` + `factura_linea` con snapshots es correcto y es lo que sostiene el versionado y las rectificativas.
