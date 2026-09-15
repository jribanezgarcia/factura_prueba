> Requiere `paquetes-en-espanol` archivado. Tablas exactas en `design.md`. Renombrar ficheros con `git mv` y, si se puede, con *Refactor > Rename* del IDE. No cambia ningún texto visible ni mensaje de excepción. Ningún comentario nuevo en el código; los Javadoc existentes que citen un nombre viejo se actualizan al nuevo.

## 1. Datos (`modelo.negocio.sqlite`)

- [ ] 1.1 Renombrar `Database` → `Conexion` y `Migrations` → `Migraciones` (clase, fichero y usos). Ver `design.md - D3`.
- [ ] 1.2 Renombrar los métodos públicos y los privados de `Conexion` y `Migraciones` según `design.md - D4`, en la declaración y en todos los usos de main y test.
- [ ] 1.3 Renombrar los 11 `*Repository` a `*DAO` según `design.md - D3` (clase, fichero y usos).
- [ ] 1.4 `mvn -q compile` sin errores.

## 2. Dominio (`modelo.dominio`)

- [ ] 2.1 Renombrar `FacturaVersion` → `VersionFactura` y `HistorialFila` → `FilaHistorial` (clase, fichero y usos). Ver `design.md - D2`.

## 3. Reglas (`modelo.negocio`)

- [ ] 3.1 Renombrar las 14 clases de reglas y `ValidationException` según `design.md - D1` (clase, fichero y usos).
- [ ] 3.2 Renombrar el tipo anidado `DiaMode` → `ModoDia` en `FacturacionMensual` y en sus usos (incluido `GenerarFacturasMensualesController`).
- [ ] 3.3 Renombrar campos, parámetros y variables según `design.md - D7`. Anotar aquí los choques resueltos con sufijo: ______.

## 4. Copia de seguridad (`fichero`)

- [ ] 4.1 Renombrar `BackupService` → `CopiaSeguridad`, `ResumenBackup` → `ResumenCopia` y `crearBackup` → `crearCopia`, con sus campos. Ver `design.md - D6`.

## 5. Modelo

- [ ] 5.1 Renombrar `Servicios` → `Modelo`. Sus campos pasan de `public final` a `private final` con los nombres nuevos y se añade un getter por campo, según la tabla de `design.md - D5`. Variables locales del constructor a `xDAO`.
- [ ] 5.2 Aplicar en `vista`, en `Main` y en los tests la tabla «Uso desde las pantallas» de `design.md - D5`: `Vista.setModelo`, `Navegador.modelo()`, constructor de `Navegador`, `GenerarFacturasMensualesController.setModelo`, parámetros de `ThemeManager`, variable `servicios` → `modelo` y acceso a campos → getters (`servicios.factura.x(...)` → `modelo.getFacturas().x(...)`).
- [ ] 5.3 No sacar tipos anidados ni cambiar construcciones existentes (streams, ternarios, `var`…): ver `design.md - D5b`.

## 6. Tests

- [ ] 6.1 Renombrar los ficheros de test según las tablas de `design.md - D1`, `D3` y `D6` con `git mv`, y la clase dentro.
- [ ] 6.2 Adaptar los tests a los nombres nuevos (clases, métodos de `Conexion`, campos de `Modelo`, variable `servicios` → `modelo`). Si algún test busca por reflexión el campo `"servicios"`, cambiar la cadena a `"modelo"`. **No cambiar lo que comprueba ningún test.**

## 7. Comprobaciones

- [ ] 7.1 `git grep -nE "\b(Database|Migrations|Servicios|ValidationException|EmpresaManager|BackupService|ResumenBackup|FacturaVersion|HistorialFila|DiaMode)\b" -- src` no devuelve nada.
- [ ] 7.2 `git grep -nE "\w+(Service|Repository)\b" -- src/main/java src/test/java` no devuelve nada (salvo, si aparece, `PdfService`, que se renombra en el tercer change).
- [ ] 7.3 `git grep -nE "getConnection\(\)|resetConnection|beginTransaction|endTransaction|setDataDir|dbPath|dataDir\(\)|lockPath|\.migrate\(|userVersion\(" -- src` no devuelve nada.
- [ ] 7.4 `git grep -nE "\bservicios(\.|\(|\)|,|;| =)" -- src` solo devuelve líneas de comentario (ninguna de código).
- [ ] 7.4b `git grep -nE "\bmodelo\.(reloj|clientes|series|tiposIva|tiposRetencion|configuracion|numeracion|versiones|facturas|estados|rectificativas|facturacionMensual|historial|copiaSeguridad)\b" -- src` no devuelve nada (todo va por getters) y `git grep -n "public final" -- src/main/java/cabofactu/modelo/Modelo.java` tampoco.
- [ ] 7.4c `git grep -nE "cabofactu\.[a-z]+(\.[a-z]+)*\.[A-Z]" -- src` solo devuelve líneas `import`, `package` o de FXML.
- [ ] 7.5 `git status` muestra los ficheros renombrados como `renamed`.
- [ ] 7.6 Repasar en `git diff -M HEAD -- src` que ninguna cadena de texto entre comillas ha cambiado, salvo búsquedas por reflexión (`"servicios"` → `"modelo"`).

## 8. Verificación automática

- [ ] 8.1 `mvn test` en verde, con el mismo número de tests que antes del change.

## 9. Verificación manual

- [ ] 9.1 `mvn javafx:run`: elegir empresa y entrar al Menú.
- [ ] 9.2 Crear una factura, guardarla y abrirla desde el Histórico; los totales cuadran como antes.
- [ ] 9.3 Anular y restaurar esa factura; en Versiones aparecen las versiones nuevas.
- [ ] 9.4 Copias: crear una copia, ver su resumen y restaurarla en la empresa activa.
- [ ] 9.5 Configuración: crear una empresa nueva y cambiar a ella.
- [ ] 9.6 Doble clic en `cargar_demo.bat`: termina sin errores.
