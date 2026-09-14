## Why

Tercer y último change del renombrado al estilo Biblioteca8. Tras `paquetes-en-espanol` y `nombres-modelo-y-datos`, quedan en inglés o mezclados el arranque (`Launcher`, `Main`), algunas piezas de la vista (`ThemeManager`, `VentanaConfig`, `BackupController`, `MenuController` frente a `MenuPrincipal.fxml`), todo el PDF (`InvoiceDocument` con `Header`, `ClientCard`, `TotalsBlock`…, `InvoiceDocumentBuilder`, `OpenPdfRenderer`, `PdfService`, `CabeceraLayout`) y los validadores (`EmailValidator`…).

Además, `README.md` y `docs/` describen los paquetes y clases antiguos (`ui/`, `service/`, `repository/`, `Database`, `Servicios`…), y deben reflejar la estructura nueva.

## What Changes

- **Arranque**, como `AppBiblioteca` y `vista.LanzadorVentanaPrincipal`: `Launcher` → `cabofactu.AppCaboFactu`, `Main` → `cabofactu.vista.LanzadorVentanaPrincipal`. Se actualiza la `mainClass` de `pom.xml`.
- **Vista:**
  - `MenuController` → `MenuPrincipalController`, a juego con `MenuPrincipal.fxml`.
  - `BackupController` → `CopiaSeguridadController` y `Backup.fxml` → `CopiaSeguridad.fxml`, con sus claves internas (`BACKUP`, `RUTA_BACKUP`, `"backup"`, `#backup`).
  - `ThemeManager` → `GestorTemas` y `VentanaConfig` → `ConfiguracionVentana`.
  - Los controladores conservan el sufijo `Controller`, igual que en Biblioteca8.
- **PDF:**
  - `PdfService` → `ExportadorPdf`, `InvoiceDocument` → `DocumentoFactura`, `InvoiceDocumentBuilder` → `ConstructorDocumentoFactura`, `OpenPdfRenderer` → `GeneradorPdf` y `CabeceraLayout` → `DisposicionCabecera`.
  - Los records internos, sus componentes y los métodos del constructor pasan a español (`Header(number, date…)` → `Cabecera(numero, fecha…)`, `build` → `construir`…).
- **Utilidades:** `EmailValidator` → `ValidadorEmail`, `CodigoPostalValidator` → `ValidadorCodigoPostal`, `DocumentoFiscalValidator` → `ValidadorDocumentoFiscal`.
- **Tests:** siguen a su clase. También se renombran los que tienen nombre en inglés sin clase detrás (`UiSmokeTest`, `StyleClassSeparadorTest`, `JavaFxTestSupport`).
- **Documentación:** `README.md`, `docs/tecnico.md` y `docs/metodologia.md` pasan a la estructura y los nombres nuevos.
- Ningún cambio visible: mismas pantallas, mismos textos, mismo PDF.

## Capabilities

### New Capabilities

Ninguna.

### Modified Capabilities

Ninguna: `skip_specs: true`. Solo nombres internos y documentación.

## Impact

- `cabofactu` (arranque), `cabofactu.vista.*`, `cabofactu.pdf`, `cabofactu.utilidades` y sus tests.
- Recursos: `cabofactu/vista/recursos/Backup.fxml` (renombrado), `MenuPrincipal.fxml` y `CopiaSeguridad.fxml` (`fx:controller` y `onAction`).
- `pom.xml` (`mainClass`), `README.md`, `docs/tecnico.md` y `docs/metodologia.md`.
- Fuera: textos que ve el usuario, nombres de fichero de los temas CSS y de los demás FXML, nombres de métodos de test, métodos privados que no aparecen en `design.md`, y los changes archivados.
- **Depende de** `nombres-modelo-y-datos` archivado.
