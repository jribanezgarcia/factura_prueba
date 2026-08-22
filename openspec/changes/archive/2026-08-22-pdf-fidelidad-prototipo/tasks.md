## 1. Tipografía Calibri

- [x] 1.1 Añadir carga cacheada de Calibri (regular/negrita/cursiva desde `C:\Windows\Fonts\`) con fallback a Helvetica en el helper `fuente(...)`; verificar compilación
- [x] 1.2 Test: exportar muestra y comprobar con `BaseFont`/`PdfReader` que los recursos del documento incluyen la fuente embebida cuando existe

## 2. Cabecera sin solapes

- [x] 2.1 Reservar columna derecha para FACTURA (≈170pt): medir nombre/actividad/contacto con `getWidthPoint` y reducir tamaño por pasos hasta caber en el ancho disponible; aplicar igualmente en modo texto y modo logo; verificar compilación
- [x] 2.2 Nombre de empresa en tono marrón (`oscuro`) como el prototipo; test nuevo: empresa con nombre muy largo → el PDF contiene el número completo y la fecha extraibles (no tapados) y no se genera solape

## 3. Esquinas redondeadas

- [x] 3.1 Evento de celda que traza contorno redondeado para las tarjetas (radio 7pt), caja de observaciones (6pt) y chip NIF (2pt); verificar compilación

## 4. Espaciados y pie

- [x] 4.1 Separación visible entre última fila del resumen y banda TOTAL (hueco/padding según prototipo); paddings de rotulos/cuerpos revisados; verificar compilación
- [x] 4.2 Pie: plantilla del total anclada en hueco fijo (ancho «00») y prefijo alineado a terminar antes sin solape; test: el texto extraible contiene `Página 1 de ` y la posición del dígito no coincide con el final de «de»

## 5. Verificación completa

- [x] 5.1 Pasar la suite completa: `& "C:\Program Files\Apache NetBeans\java\maven\bin\mvn.cmd" test` (44 tests + nuevos, todos verdes)
- [x] 5.2 Generar PDF de muestra con datos reales, rasterizarlo y comparar visualmente con `prototipos/pdf-fix-v2.html` (cabecera, redondeos, espaciados, colores, pie)
- [x] 5.3 Verificación manual del usuario con su factura real (nombre largo incluido)
