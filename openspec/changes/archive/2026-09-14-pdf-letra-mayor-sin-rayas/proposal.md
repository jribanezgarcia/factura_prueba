## Why

El PDF de la factura se lee pequeño y la tabla de líneas parece una hoja de cálculo cuadriculada: cada artículo queda encerrado entre dos rayas horizontales, lo que recarga la tabla sin aportar nada, porque el rayado alterno de fondo ya separa una línea de otra. Las descripciones de varias líneas, además, van muy apretadas.

## What Changes

- **Todo** el texto del PDF SHALL componerse 1 pt mayor: cabecera (nombre, datos de empresa, NIF, FACTURA, Serie/Nº, fecha, marcas), tarjetas, tabla de líneas, suplidos, rejillas de totales, banda `TOTAL`, observaciones, pie legal (6,5 → 7,5 pt), `Página X de Y` y marca de agua `ANULADA`.
- Las celdas de las líneas SHALL usar un interlineado de 1 pt más que su letra: la descripción pasa de 9 pt a 11 pt entre renglones.
- La tabla de líneas SHALL perder las rayas horizontales entre artículos. Se mantienen la raya superior de la cabecera de columnas, la raya bajo `CANT. / DESCRIPCIÓN / …`, las divisiones verticales, el rayado alterno y la raya que cierra la tabla antes de los suplidos o de los totales.
- Cuando la tabla se corte al final de una página, esa página SHALL mostrar una raya de cierre en el borde inferior del trozo.
- La geometría de la cabecera se ajusta para que la letra mayor no se apriete, y la vista previa de Configuración copia esos mismos valores.
- Si al medir no cabe algo que hoy cabe (banda `TOTAL` de siete cifras, 20 líneas en una página), se recortan rellenos internos, nunca la letra ni los anchos de columna.
- No cambia ningún importe, texto, color ni ancho de columna.

## Capabilities

### New Capabilities

Ninguna.

### Modified Capabilities

- `pdf-rendering`: el requisito «Exportación a PDF» describe la tabla sin rayas entre líneas, cerrada por abajo también al cortar de página, y el pie legal a 7,5 pt.

## Impact

- `src/main/java/com/alcazaba/facturacion/pdf/OpenPdfRenderer.java`: tamaños de letra, bordes e interlineado de las líneas, evento de cierre en la tabla de líneas y, si la medición lo pide, dos rellenos.
- `src/main/java/com/alcazaba/facturacion/pdf/EstiloPdf.java`: `PIE_LEGAL_TAM`, rótulo de tarjeta y un evento nuevo `RayaAlCortar`.
- `src/main/java/com/alcazaba/facturacion/pdf/CabeceraPiePdf.java` y `CabeceraLayout.java`: tamaños de letra y paso entre líneas de la cabecera.
- `src/main/java/com/alcazaba/facturacion/ui/PreviaCabecera.java`: los mismos números que la cabecera del PDF.
- `src/test/java/com/alcazaba/facturacion/pdf/CabeceraLayoutTest.java`: nuevos altos de cabecera en modo texto.
- Se aplica **antes** que `paquetes-en-espanol`, `nombres-modelo-y-datos` y `nombres-vista-pdf-utilidades`; esos changes solo renombran y siguen valiendo después.
