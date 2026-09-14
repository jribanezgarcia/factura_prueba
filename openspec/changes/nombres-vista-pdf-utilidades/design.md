## Context

Parte del estado que deja `nombres-modelo-y-datos`: paquetes en español, `Modelo`, `Facturas`, `FacturaDAO`, `Conexion`, `CopiaSeguridad`…

**Referencia de Biblioteca8:** `biblioteca.AppBiblioteca` tiene el `main`; `biblioteca.vista.LanzadorVentanaPrincipal` arranca JavaFX; `vista.controlador.MenuPrincipalController` va con `MenuPrincipal.fxml`; `vista.utilidades.Dialogos`; `utilidades.UtilidadesXML`.

**Comprobaciones hechas (sobre el código del 14/09/2026, con nombres de entonces):**

- `Launcher` solo hace `Application.launch(Main.class, args)`. Nadie más usa `Main`.
- `Main` usa `InstanciaUnica` y `PreparacionDatos`, que son clases `public` con métodos `public static`: puede pasar a `vista` sin tocar visibilidades.
- Claves de la pantalla de copias: `VentanaConfig.BACKUP` (`VentanaConfig.java:20`), `BarraNavegacion.RUTA_BACKUP` (`:22`), `ICONO_BACKUP` y la clave `"backup"` (`BarraNavegacion.java:45`, `BackupController.java:74`), `MenuController.backup()` (`:141`) con `onAction="#backup"` en `MenuPrincipal.fxml:98`, y `UiSmokeTest.java:92` con `cargar("Backup.fxml")`.
- `InvoiceDocument` tiene 15 records anidados con componentes en inglés, que usan `InvoiceDocumentBuilder` (28 llamadas a sus métodos), `OpenPdfRenderer` y `CabeceraPiePdf`.
- `ThemeManager.DEFAULT = "biblioteca8"` es el nombre de un tema guardado en preferencias: **el valor no cambia**, solo el nombre de la constante.
- En Biblioteca8 el sufijo `Controller` se mantiene: no se traduce.

## Goals / Non-Goals

**Goals:** ningún nombre de clase, record, constante pública o método público en inglés en `vista`, `pdf` y `utilidades`; arranque igual que Biblioteca8; documentación coherente con el código.

**Non-Goals:**
- Cambiar textos visibles, incluido el «El logo del backup no se encontrará…» de `BackupController.java:172`.
- Cambiar valores guardados (`"biblioteca8"`, `"tema"`, `"color_pdf"`).
- Renombrar los demás FXML o los CSS.
- Traducir el sufijo `Controller`.
- Renombrar métodos de test o métodos privados que no estén en este documento.
- Tocar los changes archivados.

## Decisions

### D1. Arranque

| Hoy | Nuevo |
|---|---|
| `cabofactu.Launcher` | `cabofactu.AppCaboFactu` (`main` → `Application.launch(LanzadorVentanaPrincipal.class, args)`) |
| `cabofactu.Main` | `cabofactu.vista.LanzadorVentanaPrincipal` (se mueve a `vista`) |

`pom.xml`: `<mainClass>cabofactu.Main</mainClass>` → `<mainClass>cabofactu.vista.LanzadorVentanaPrincipal</mainClass>`.

`InstanciaUnica` y `PreparacionDatos` se quedan en `cabofactu`.

### D2. Vista

| Hoy | Nuevo |
|---|---|
| `vista.controlador.MenuController` | `vista.controlador.MenuPrincipalController` |
| `vista.controlador.BackupController` | `vista.controlador.CopiaSeguridadController` |
| `vista/recursos/Backup.fxml` | `vista/recursos/CopiaSeguridad.fxml` |
| `vista.utilidades.ThemeManager` | `vista.utilidades.GestorTemas` |
| `ThemeManager.DEFAULT` / `PREV_TEMA` | `GestorTemas.POR_DEFECTO` / `PREF_TEMA` (**mismos valores**) |
| `vista.VentanaConfig` | `vista.ConfiguracionVentana` |
| `VentanaConfig.BACKUP` | `ConfiguracionVentana.COPIA_SEGURIDAD` |
| `BarraNavegacion.RUTA_BACKUP` / `ICONO_BACKUP` | `RUTA_COPIA_SEGURIDAD` / `ICONO_COPIA_SEGURIDAD` |
| clave `"backup"` en `BarraNavegacion.crear(nav, "backup")` y en su comparación | `"copiaSeguridad"` |
| `MenuController.backup()` + `onAction="#backup"` | `MenuPrincipalController.copiaSeguridad()` + `onAction="#copiaSeguridad"` |
| `fx:controller` de `MenuPrincipal.fxml` y del nuevo `CopiaSeguridad.fxml` | `cabofactu.vista.controlador.MenuPrincipalController` / `…CopiaSeguridadController` |
| rutas `"/cabofactu/vista/recursos/Backup.fxml"` | `"/cabofactu/vista/recursos/CopiaSeguridad.fxml"` |

Tests:

| Hoy | Nuevo |
|---|---|
| `vista.controlador.MenuLayoutTest` | `MenuPrincipalLayoutTest` |
| `vista.controlador.BackupLayoutTest` | `CopiaSeguridadLayoutTest` (también sus textos de `fail`/`assert` que citan el nombre del test o del FXML) |
| `vista.UiSmokeTest` | `vista.CargaPantallasTest` |
| `vista.StyleClassSeparadorTest` | `vista.ClaseSeparadorTest` |
| `vista.JavaFxTestSupport` | `vista.PruebasJavaFx` (sin sufijo `Test`, para que Surefire no lo trate como test) |

### D3. PDF

Clases:

| Hoy | Nuevo | Test hoy | Test nuevo |
|---|---|---|---|
| `PdfService` | `ExportadorPdf` | `PdfServiceTest` | `ExportadorPdfTest` |
| `InvoiceDocument` | `DocumentoFactura` | — | — |
| `InvoiceDocumentBuilder` | `ConstructorDocumentoFactura` | `InvoiceDocumentBuilderTest` | `ConstructorDocumentoFacturaTest` |
| `OpenPdfRenderer` | `GeneradorPdf` | `OpenPdfRendererTest` | `GeneradorPdfTest` |
| `CabeceraLayout` | `DisposicionCabecera` | `CabeceraLayoutTest` | `DisposicionCabeceraTest` |
| `CabeceraPiePdf`, `EstiloPdf` | *(igual)* | — | — |

`ExportadorPdf.PREF_COLOR` y `COLOR_DEFECTO` conservan nombre y valor.

Records de `DocumentoFactura` (tipo → nuevo tipo, componentes en orden):

| Hoy | Nuevo |
|---|---|
| `InvoiceDocument(header, clientCard, paymentCard, linesTable, suplidos, totals, observations, legalFooter)` | `DocumentoFactura(cabecera, tarjetaCliente, tarjetaPago, tablaLineas, suplidos, totales, observaciones, pieLegal)` |
| `Header(number, date, corrective, correctsReference, cancelled)` | `Cabecera(numero, fecha, rectificativa, referenciaRectificada, anulada)` |
| `FieldRow(label, value)` | `FilaCampo(etiqueta, valor)` |
| `ClientCard(title, rows, emptyMarker)` | `TarjetaCliente(titulo, filas, marcaVacio)` |
| `PaymentCard(title, rows)` | `TarjetaPago(titulo, filas)` |
| `LineRow(quantity, description, price, iva, total)` | `FilaLinea(cantidad, descripcion, precio, iva, total)` |
| `LinesTable(headers, rows)` | `TablaLineas(cabeceras, filas)` |
| `SuplidoRow(description, amount)` | `FilaSuplido(descripcion, importe)` |
| `SuplidosBlock(headers, rows, note)` | `BloqueSuplidos(cabeceras, filas, nota)` |
| `IvaRow(type, base, quota)` | `FilaIva(tipo, base, cuota)` |
| `RetentionRow(label, amount)` | `FilaRetencion(etiqueta, importe)` |
| `SuplidosTotalRow(label, amount)` | `FilaTotalSuplidos(etiqueta, importe)` |
| `TotalBand(label, amount)` | `BandaTotal(etiqueta, importe)` |
| `Liquidation(title, baseLabel, baseAmount, ivaLabel, ivaAmount, retention, suplidos, total)` | `Liquidacion(titulo, etiquetaBase, importeBase, etiquetaIva, importeIva, retencion, suplidos, total)` |
| `TotalsBlock(desgloseHeaders, ivaRows, totalsRow, discountNote, liquidation)` | `BloqueTotales(cabecerasDesglose, filasIva, filaTotales, notaDescuento, liquidacion)` |

Al renombrar un componente de record cambia también su método de acceso (`header()` → `cabecera()`), y hay que actualizarlo en `GeneradorPdf`, `CabeceraPiePdf`, `ConstructorDocumentoFactura` y los tests.

Métodos de `ConstructorDocumentoFactura`:

| Hoy | Nuevo |
|---|---|
| `build(vc, empresa, colorHex)` | `construir(vc, empresa, colorHex)` |
| `header(v)` | `cabecera(v)` |
| `clientCard(v)` | `tarjetaCliente(v)` |
| `paymentCardTitle()` | `tituloTarjetaPago()` |
| `paymentCard(v)` | `tarjetaPago(v)` |
| `paymentRows(v)` | `filasPago(v)` |
| `linesTable(lineas)` | `tablaLineas(lineas)` |
| `suplidosBlock(suplidos)` | `bloqueSuplidos(suplidos)` |
| `totalsBlock(r, descuento)` | `bloqueTotales(r, descuento)` |
| `discountNote(r, descuento)` | `notaDescuento(r, descuento)` |
| `observations(v)` | `observaciones(v)` |
| `legalFooter(empresa)` | `pieLegal(empresa)` |
| `importePdf`, `porcentajeRejilla` | *(igual)* |

Los textos que acaban en el PDF no cambian.

### D4. Utilidades

| Hoy | Nuevo | Test nuevo |
|---|---|---|
| `EmailValidator` | `ValidadorEmail` | `ValidadorEmailTest` |
| `CodigoPostalValidator` | `ValidadorCodigoPostal` | `ValidadorCodigoPostalTest` |
| `DocumentoFiscalValidator` | `ValidadorDocumentoFiscal` | `ValidadorDocumentoFiscalTest` |
| `Formatos`, `LogoMarco` | *(igual)* | *(igual)* |

`ClientesNifValidationTest` y `EditorNifValidationTest` pasan a `ClientesValidacionNifTest` y `EditorValidacionNifTest`.

### D5. Documentación

Se actualiza para que describa **el código tal como queda**. Los nombres de changes archivados (`sql-fuera-de-ui-y-service`, `capa-servicios-catalogos`…) **se dejan tal cual**, porque son identificadores del historial.

- `README.md`:
  - Diagrama de capas (líneas ~93-96): `ui/*Controller + *.fxml` → `vista/controlador/*Controller + vista/recursos/*.fxml`; `service/*Service` → `modelo/negocio` (`Facturas`, `Clientes`…); `repository/*Repository` → `modelo/negocio/sqlite/*DAO`; `Database` → `Conexion`.
  - Caja «💡 Concepto» (línea ~106): `Database` → `Conexion`.
  - Añadir una frase: la estructura sigue la del proyecto Biblioteca8.
- `docs/tecnico.md`:
  - Diagrama (líneas ~28-33): `Launcher → Main` → `AppCaboFactu → LanzadorVentanaPrincipal`; `servicios.factura` → `modelo.facturas`; `Servicios` → `Modelo`; `Database + Migrations` → `Conexion + Migraciones`.
  - Tabla de capas (~46-49) y cajas de concepto (~52-58): mismos cambios. La caja «repositorio = DAO» pasa a explicar que las clases **se llaman** `*DAO`.
  - Árbol de paquetes (~65-75): sustituir por el árbol real de `cabofactu/` (`AppCaboFactu`, `InstanciaUnica`, `PreparacionDatos`, `modelo/`, `modelo/dominio/`, `modelo/negocio/`, `modelo/negocio/sqlite/`, `fichero/`, `pdf/`, `vista/`, `vista/controlador/`, `vista/utilidades/`, `utilidades/`) y `src/main/resources/cabofactu/vista/recursos/` con `temas/` e `imagenes/`.
  - Diagrama de secuencia (~88-91) y pasos (~105): `EditorController`, `Facturas`, `Numeracion`, `Calculos`, `Conexion`.
  - Caja de excepción no comprobada (~113): «Los DAO convierten…».
  - Tabla de decisiones (~222): «Capas negocio + DAO». Añadir fila: «Nombres en español al estilo Biblioteca8 | Nombres en inglés (`Service`, `Repository`) | Coherencia con el resto del código y con lo visto en clase».
- `docs/metodologia.md` (~85-144): actualizar los nombres de clase (`NumeroService` → `Numeracion`, `Servicios(Clock)` → `Modelo(Clock)`), sin tocar los nombres de los changes. Añadir los tres changes de renombrado a la tabla de resultados de la auditoría.

## Risks / Trade-offs

- **Riesgo medio: claves de texto de la pantalla de copias.** `"backup"` se usa como identificador en dos sitios y `#backup` en el FXML. Si se cambia solo uno, el botón de la barra deja de marcarse como activo o el menú no abre la pantalla. `CargaPantallasTest` y la verificación manual lo cubren.
- **Riesgo bajo: records del PDF.** El compilador detecta cada acceso. Los tests del PDF comparan contenido y deben pasar sin cambios de lógica.
- **Riesgo bajo: `javafx:run`.** Si `mainClass` apunta mal, la aplicación no arranca desde Maven. Tarea 8.1.
- **Trade-off aceptado:** el sufijo `Controller` se queda en inglés, como en Biblioteca8 y como lo espera Scene Builder por convención.
