## 1. Persistencia

- [x] 1.1 Crear `src/main/resources/db/migrations/002_datos_factura_pdf.sql` con las 5 columnas nuevas y registrarla en `Migrations.SCRIPTS`
- [x] 1.2 `Cliente`: campo `email`; `FacturaVersion`: `cliEmail`, `formaPago`, `vencimiento` (LocalDate), `realizadaPor`
- [x] 1.3 `ClienteRepository` (insert/actualizar) y `VersionRepository` (insertarVersion/actualizarVersion/map) con las columnas nuevas
- [x] 1.4 Firmas de `VersionadoService.crearVersion/sobrescribirVersion`, `FacturaService.crearFactura/guardarEditada` y paso de campos en `EstadoService`

## 2. UI de datos nuevos

- [x] 2.1 Ficha de clientes: campo email (dialog programático en `ClientosController.construirFicha`)
- [x] 2.2 `Editor.fxml`: filas Forma de pago / Vencimiento / Realizada por + campo email del cliente; `EditorController`: carga, listeners, guardado y setEditable
- [x] 2.3 Configuración: `ColorPicker` para el color del PDF, carga/guardado preferencia `color_pdf`

## 3. PDF

- [x] 3.1 Reescribir `PdfService.exportar` con color por parámetro: cabecera (logo ×2, NIF destacado), tarjetas bicolor, tabla líneas con Total c/IVA, totales con fila TOTAL en color, observaciones en caja, pie legal en recuadro repetido, Página X de Y
- [x] 3.2 Derivación de tonos desde hex con fallback `#B08D57`; llamada actualizada desde quien exporta

## 4. Verificación

- [x] 4.1 Test de servicio: guardar factura con datos de pago + email persisten en la versión
- [x] 4.2 Test de humo de PDF: texto extraído contiene número, NIF empresa, «FACTURAR A» y total c/IVA
- [x] 4.3 Suite completa verde (`mvn test`) y verificación manual de un PDF exportado
