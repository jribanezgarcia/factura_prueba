> Requiere `nombres-modelo-y-datos` archivado. Tablas exactas en `design.md`. Renombrar ficheros con `git mv`. No cambia ningún texto visible ni valor guardado. Ningún comentario nuevo en el código; los Javadoc existentes que citen un nombre viejo se actualizan al nuevo.

## 1. Arranque

- [ ] 1.1 Renombrar `cabofactu/Launcher.java` → `cabofactu/AppCaboFactu.java` y mover `cabofactu/Main.java` → `cabofactu/vista/LanzadorVentanaPrincipal.java`, con sus `package`, imports y la llamada a `Application.launch`. Ver `design.md - D1`.
- [ ] 1.2 `pom.xml`: `mainClass` según `design.md - D1`.

## 2. Vista

- [ ] 2.1 Renombrar `MenuController` → `MenuPrincipalController` y su método `backup()` → `copiaSeguridad()`; en `MenuPrincipal.fxml`, `fx:controller` y `onAction="#copiaSeguridad"`.
- [ ] 2.2 Renombrar `BackupController` → `CopiaSeguridadController` y `Backup.fxml` → `CopiaSeguridad.fxml` (con su `fx:controller`); cambiar todas las rutas a `CopiaSeguridad.fxml`.
- [ ] 2.3 Aplicar las claves de la tabla de `design.md - D2`: `ConfiguracionVentana.COPIA_SEGURIDAD`, `RUTA_COPIA_SEGURIDAD`, `ICONO_COPIA_SEGURIDAD` y la clave `"copiaSeguridad"` en los **dos** sitios donde hoy está `"backup"`.
- [ ] 2.4 Renombrar `ThemeManager` → `GestorTemas` (constantes `POR_DEFECTO` y `PREF_TEMA` **con los mismos valores**) y `VentanaConfig` → `ConfiguracionVentana`.
- [ ] 2.5 `mvn -q compile` sin errores.

## 3. PDF

- [ ] 3.1 Renombrar `PdfService` → `ExportadorPdf`, `InvoiceDocumentBuilder` → `ConstructorDocumentoFactura`, `OpenPdfRenderer` → `GeneradorPdf` y `CabeceraLayout` → `DisposicionCabecera` (clase, fichero y usos, incluido `PreviaCabecera`).
- [ ] 3.2 Renombrar solo la clase `InvoiceDocument` → `DocumentoFactura` (fichero y usos). **No** renombrar sus records internos, sus componentes ni los métodos de `ConstructorDocumentoFactura`. Ver `design.md - D3` y Non-Goals.
- [ ] 3.3 `mvn -q compile` sin errores.

## 4. Utilidades

- [ ] 4.1 Renombrar los tres validadores según `design.md - D4` (clase, fichero y usos en `ClientesController`, `EditorController` y demás).

## 5. Tests

- [ ] 5.1 Renombrar con `git mv` los tests de `design.md - D2`, `D3` y `D4`, incluidos `UiSmokeTest` → `CargaPantallasTest`, `StyleClassSeparadorTest` → `ClaseSeparadorTest`, `JavaFxTestSupport` → `PruebasJavaFx`, `ClientesNifValidationTest` → `ClientesValidacionNifTest` y `EditorNifValidationTest` → `EditorValidacionNifTest`.
- [ ] 5.2 Adaptar sus usos: `cargar("CopiaSeguridad.fxml")` en `CargaPantallasTest`, rutas a `MenuPrincipal.fxml` y `CopiaSeguridad.fxml`, y las cadenas de `fail`/`assert` que citen el nombre viejo del test o del FXML. **No cambiar lo que comprueba ningún test.**

## 6. Documentación

- [ ] 6.1 `README.md` según `design.md - D5`.
- [ ] 6.2 `docs/tecnico.md` según `design.md - D5`, con el árbol de paquetes real (comprobarlo con `Get-ChildItem src/main/java/cabofactu -Recurse -Directory`).
- [ ] 6.3 `docs/metodologia.md` según `design.md - D5`, sin cambiar los nombres de los changes archivados.

## 7. Comprobaciones

- [ ] 7.1 `git grep -nE "\b(Launcher|Main|MenuController|BackupController|ThemeManager|VentanaConfig|PdfService|InvoiceDocument\w*|OpenPdfRenderer|CabeceraLayout|EmailValidator|CodigoPostalValidator|DocumentoFiscalValidator|UiSmokeTest|StyleClassSeparadorTest|JavaFxTestSupport)\b" -- src pom.xml` no devuelve nada.
- [ ] 7.2 `git grep -nE "\b(Header|FieldRow|ClientCard|PaymentCard|LineRow|LinesTable|SuplidoRow|SuplidosBlock|IvaRow|RetentionRow|SuplidosTotalRow|TotalBand|Liquidation|TotalsBlock)\b" -- src` sigue devolviendo los mismos usos que antes del change (no se han traducido).
- [ ] 7.3 `git grep -n "Backup.fxml\|\"backup\"\|#backup\|\bBACKUP\b" -- src` no devuelve nada.
- [ ] 7.4 `git grep -nE "\b(Service|Repository|Database|Migrations|Servicios)\b|\w+(Service|Repository)\b|com/alcazaba|\bui/|\bservice/|\brepository/" -- README.md docs` solo devuelve nombres de changes archivados (por ejemplo `sql-fuera-de-ui-y-service`).
- [ ] 7.5 `git grep -n "\"biblioteca8\"\|\"tema\"\|\"color_pdf\"" -- src/main` sigue devolviendo los mismos valores que antes.
- [ ] 7.6 `git status` muestra los ficheros renombrados como `renamed`.

## 8. Verificación automática

- [ ] 8.1 `mvn test` en verde, con el mismo número de tests que antes del change.

## 9. Verificación manual

- [ ] 9.1 `mvn javafx:run` arranca la aplicación.
- [ ] 9.2 El tema elegido antes del change sigue aplicado al abrir (se lee `"tema"` con el mismo valor).
- [ ] 9.3 Desde el Menú, el botón «Copia de seguridad» abre la pantalla de copias; en la barra de navegación, el botón «Copias» aparece marcado como activo.
- [ ] 9.4 Exportar una factura a PDF, una rectificativa y un PDF agrupado desde el Histórico: salen iguales que antes (cabecera, tarjetas, líneas, suplidos, totales y pie).
- [ ] 9.5 Configuración > PDF: la previsualización de cabecera se ve igual.
- [ ] 9.6 Clientes y Editor: un NIF inválido sigue marcándose en rojo.
- [ ] 9.7 Revisar `README.md` y `docs/` en GitHub: los diagramas se dibujan y los nombres coinciden con el código.
