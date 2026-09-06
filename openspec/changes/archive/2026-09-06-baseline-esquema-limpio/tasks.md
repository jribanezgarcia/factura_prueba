> Las decisiones y sus motivos están en `design.md`. El baseline se compone leyendo los nueve scripts actuales, no de memoria.

## 1. Baseline

- [x] 1.1 Crear `src/main/resources/db/migrations/001_baseline.sql` con el esquema consolidado de los nueve scripts actuales: tablas `cliente`, `serie`, `serie_siguiente`, `tipo_iva`, `tipo_retencion`, `factura`, `factura_version`, `factura_linea`, `numero_disponible`, `empresa` y `preferencias`, con sus índices.
- [x] 1.2 Aplicar el tipo uniforme de importes de `design.md - D2`: `importe_retencion` y `total_suplidos` como `TEXT NOT NULL DEFAULT '0.00'`.
- [x] 1.3 Aplicar las siembras sin identificadores literales de `design.md - D3`, con los cuatro tipos de IVA incluido «Suplido».
- [x] 1.4 Aplicar la nulabilidad de `design.md - D4`.
- [x] 1.5 Borrar los nueve scripts anteriores y dejar `Migrations.SCRIPTS` con un solo elemento.

## 2. Repositorios

- [x] 2.1 Comprobar que `VersionRepository` y `HistorialRepository` siguen funcionando con `importe_retencion` como `TEXT`, y ajustar lo que haga falta.
- [x] 2.2 Comprobar que ninguna consulta dependía de que `cliente.email`, `cli_email`, `forma_pago` o `realizada_por` fueran `NOT NULL`.

## 3. Datos de demostración

- [x] 3.1 Crear `src/main/resources/db/seed_demo.sql` con la empresa ficticia y la cobertura de casos de `design.md - D5`.
- [x] 3.2 Añadir la clase con `main` que crea la empresa `demo`, aplica migraciones y ejecuta el script, recreándola desde cero si ya existía.
- [x] 3.3 Añadir `cargar_demo.bat` en la raíz, al estilo de `lanzar.bat`.
- [x] 3.4 Comprobar que ejecutarlo dos veces seguidas no duplica datos.

## 4. Tests

- [x] 4.1 Rehacer el caso de `BackupServiceTest` que bajaba una base a `user_version = 6` (líneas 239-247), según `design.md - D6`.
- [x] 4.2 Test: base nueva desde cero con `user_version = 1`, todas las tablas presentes y exactamente un `tipo_iva` con `es_suplido = 1`.
- [x] 4.3 Test: ejecutar las migraciones dos veces sobre la misma base no duplica las filas sembradas.
- [x] 4.4 En `PdfServiceTest`, sustituir la aserción `assertFalse(texto.contains("Exento"))` de `suplidoTieneBloquePropioYNoSeRotulaExento` por una factura con una línea al 21 %, una exenta y un suplido: la tabla de líneas contiene la exenta rotulada «Exento» y no contiene el suplido; el bloque `SUPLIDOS` sí lo contiene.

## 5. Limpieza

- [x] 5.1 Borrar las tres bases de prueba de `%APPDATA%\Facturacion`: `asesoria_maria_luisa_ibanez`, `comercial_alcazaba` y `jose_maria_morales_lopez`. Confirmar con el usuario antes de borrar. Hacerlo **después** de que el baseline esté en su sitio; si no, la aplicación las recrearía con las migraciones antiguas.
- [x] 5.2 Limpiar el estado que vive fuera de SQLite en esa misma carpeta: quitar las tres empresas de `empresas.properties` y la clave `ultima_empresa` de `preferencias.properties`, que apunta a una de ellas.
- [x] 5.3 Comprobar qué hace la aplicación al arrancar con una empresa listada en `empresas.properties` cuya carpeta no existe, y con `ultima_empresa` apuntando a una empresa borrada. Si no lo resuelve con un mensaje claro, dejarlo anotado para un change propio en lugar de arreglarlo aquí.

## 6. Especificación

- [x] 6.1 Comprobar que el delta de `specs/invoicing/spec.md` recoge el esquema único y la garantía del tipo «Suplido» por siembra.

## 7. Verificación final

- [x] 7.1 Suite completa en verde con `mvn test`.
- [x] 7.2 Arrancar la aplicación, crear una empresa nueva y comprobar que el desplegable de IVA ofrece los cuatro tipos, incluido «Suplido».
- [x] 7.3 Cargar la demostración y recorrer histórico, editor, PDF y copia de seguridad.
