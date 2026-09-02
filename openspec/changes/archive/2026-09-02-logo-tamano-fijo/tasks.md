## 0. Requisito previo
- [x] 0.1 `configuracion-secciones-laterales` commiteado y archivado en `c75c55f`: via libre

## 1. Geometria fija
- [x] 1.1 En `CabeceraLayout`, sustituir el x2 y los topes por una caja fija de 240 x 120 pt
- [x] 1.2 Quitar `offsetLogoX` y `offsetLogoY`
- [x] 1.3 Ajustar el alto de cabecera en modo logo, que ya no depende del desplazamiento

## 2. Dibujado
- [x] 2.1 `PdfService` coloca el logo en el margen izquierdo, sin offsets, manteniendo `scaleToFit`
- [x] 2.2 `PreviaCabecera` deja de usar offsets y pinta la caja fija

## 3. Configuracion
- [x] 3.1 Quitar de `Configuracion.fxml` las filas de posicion X, posicion Y, ancho y alto
- [x] 3.2 Quitar `lblTamanoEfectivo` y su metodo `actualizarTamanoEfectivo()`
- [x] 3.3 Limpiar del controlador los `@FXML` y listeners que queden huerfanos
- [x] 3.4 Mantener `PreviaCabecera` y su cableado con modo, logo y color

## 4. Tests
- [x] 4.1 Reescribir `CabeceraLayoutTest`: tamano constante con cualquier valor de entrada, incluidos nulos y absurdos
- [x] 4.2 Comprobar que el test nuevo FALLA con el `CabeceraLayout` anterior
- [x] 4.3 Suite completa en verde

## 5. Verificacion
- [x] 5.1 PDF con logo apaisado: no pisa el bloque FACTURA
- [x] 5.2 PDF con logo cuadrado: no se deforma ni comprime los datos de empresa
- [x] 5.3 PDF con la configuracion por defecto: identico al de antes del cambio
- [x] 5.4 La seccion Cabecera y pie sigue cabiendo a 1024x768

## 6. Cierre
- [x] 6.1 Actualizar CONTINUAR_MAÑANA.md
- [x] 6.2 /opsx-sync-specs (este change SI lleva delta) y /opsx-archive
- [x] 6.3 Commit y push
