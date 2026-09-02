## Why

Al configurar un logo grande, la cabecera del PDF se descuadra. No es una impresion: los numeros lo confirman.

- El ancho efectivo del logo llega hasta **480 pt** (`CabeceraLayout.anchoLogoEfectivo`: el doble de lo configurado, con tope de 480).
- El hueco real disponible antes del bloque FACTURA es de **345 pt**: `derecha - RESERVA_FACTURA - izquierda`, es decir (595 - 40) - 170 - 40, con `RESERVA_FACTURA = 170f` en `PdfService`.
- **El maximo que la aplicacion permite hoy ya se pasa del espacio disponible** y pisa el bloque FACTURA.
- Ademas, `offsetLogoX` y `offsetLogoY` **no tienen tope ninguno**, asi que empujan el logo sin limite.
- El descuadre visible sale de `PdfService`, donde el ancho de la columna de datos de empresa se calcula como `Math.max(derecha - RESERVA_FACTURA - xInfo, 80f)`: con un logo ancho o desplazado, **colapsa al suelo de 80 pt**, que es ilegible.

Configurar tamano y posicion del logo es, en la practica, una manera comoda de romper la factura. La decision es quitar esa posibilidad y fijar el tamano.

## What Changes

- El logo se dibuja siempre en una **caja fija de 240 x 120 pt**, respetando su proporcion con `scaleToFit`, que ya se usa hoy.
- **240 x 120 es exactamente lo que produce la configuracion por defecto actual** (120 x 60 duplicado). Quien nunca toco esos campos **no vera ningun cambio en sus PDFs**: es la migracion mas segura posible.
- `CabeceraLayout` pierde el factor x2, los topes y los offsets; pasa a exponer constantes fijas.
- La seccion "Cabecera y pie" de Configuracion pierde las cuatro filas de posicion X, posicion Y, ancho y alto, y la etiqueta `lblTamanoEfectivo`.
- **`PreviaCabecera` se mantiene**: sigue sirviendo para ver el modo texto frente al modo logo y el color de acento. Solo pierde el papel de ajustar posicion y tamano.
- Los campos `logoAncho`, `logoAlto`, `logoX` y `logoY` **se quedan en el modelo y en la base de datos**, para no obligar a una migracion. Simplemente dejan de leerse.
- `CabeceraLayoutTest` comprueba hoy el doble y los topes: **se reescribe**, no se borra, para fijar el tamano constante.

## Capabilities

### New Capabilities
- Ninguna.

### Modified Capabilities
- Exportacion a PDF: el logo pasa a tamano fijo y deja de ser configurable.
- Configuracion: desaparecen los ajustes de tamano y posicion del logo.

## Impact

- src/main/java/com/alcazaba/facturacion/pdf/CabeceraLayout.java
- src/main/java/com/alcazaba/facturacion/pdf/PdfService.java
- src/main/java/com/alcazaba/facturacion/ui/PreviaCabecera.java
- src/main/java/com/alcazaba/facturacion/ui/ConfiguracionController.java
- src/main/resources/com/alcazaba/facturacion/ui/Configuracion.fxml
- src/test/java/com/alcazaba/facturacion/pdf/CabeceraLayoutTest.java
