## Context

El PDF ya implementa el diseño aprobado (tarjetas, totales limpios, pie con plantilla) pero la comparación visual contra `prototipos/pdf-fix-v2.html` muestra desviaciones: solape del nombre de empresa con el bloque FACTURA (tapa Serie/Nº y fecha), tipografía Helvetica en lugar de Calibri, esquinas rectas donde el prototipo redondea, banda TOTAL pegada a la fila anterior, y pie `Página X de Y` con el dígito total dibujado encima de «de».

Causa raíz del solape: la cabecera se pinta por evento de página con posiciones fijas (`dibujarDatosEmpresa` desde la izquierda, `dibujarBloqueFactura` anclado a `derecha`) sin medir ni reservar anchos.

## Goals / Non-Goals

**Goals:**

- Cabecera a dos columnas reales: datos de empresa confinados a su zona, bloque FACTURA siempre visible.
- Tipografía Calibri embebida con fallback.
- Esquinas redondeadas en tarjetas, observaciones y chip NIF.
- Espaciados y tonos fieles al prototipo; pie sin solape.

**Non-Goals:**

- No cambia cálculo fiscal, modelo, ni nada fuera del PDF.
- No se sustituye el mecanismo configurable de color de acento.

## Decisions

- **Reserva de columna en cabecera**: ancho disponible para datos de empresa = `derecha − izquierda − 170f` (bloque FACTURA ≈ ancho de "RECTIFICATIVA" + margen). Nombre/actividad/contacto se miden con `BaseFont.getWidthPoint`; si exceden, se reduce el tamaño por pasos (15→13→11→9pt) hasta caber. Alternativa descartada: partir el texto en dos líneas (cambia el alto de la franja y complica los márgenes dinámicos).
- **Calibri**: `BaseFont.createFont("C:\Windows\Fonts\calibri.ttf", IDENTITY_H, EMBEDDED)` (+ `calibrib.ttf`, `calibrii.ttf`, `calibriz.ttf`) cacheados en estáticos de `PdfService`; helper `fuente()` devuelve Font de Calibri cuando existe y Helvetica si no. La fuente queda embebida en cada documento (requisito de fidelidad tipográfica).
- **Esquinas redondeadas**: eventos `PdfPCellEvent` dibujando en `TEXTCANVAS`: `ContornoRedondeado` traza el contorno curvo de tarjetas (radio 7pt) y observaciones (6pt); los rótulos de cabecera (`RotuloTarjeta`) se pintan íntegramente en el evento (fondo con esquinas superiores redondeadas + título redibujado encima) para que el fondo de acento no sobresalga del contorno curvo. El chip NIF pasa a `roundRectangle` (2pt).
- **Pie sin solape**: hueco fijo para el total = anchura de `"00"` a 8pt; plantilla anclada en `xTotal = derecha − wHueco`; prefijo alineado a la derecha terminando en `xTotal − 2`. Además, un contador propio de páginas incrementado en `onEndPage` sustituye a `writer.getPageNumber()` en `onCloseDocument`: el writer registraba una página fantasma extra al cerrar y el pie mostraba «de 2» en documentos de una sola página.
- **Colores**: constantes neutras fijas según prototipo (`TINTA #3A332B`, `GRIS #5F5548`, etiquetas `GRIS_CLARO #A2937F`, valores suaves de pago `#C4BAAC`, descuento `#8A2B2B`); los tonos derivados del acento (claro/clarísimo/borde/oscuro) se mantienen para respetar la configurabilidad.

## Risks / Trade-offs

- [Calibri no presente (Windows incompleto)] → fallback automático a Helvetica; comportamiento idéntico al actual.
- [Reducción de fuente agresiva con nombres larguísimos] → mínimo 9pt legible; verificación manual con nombre real.
- [roundRectangle en celdas puede pintar sobre contenido] → el evento traza solo el contorno después del layout, grosor 0.9pt igual que bordes actuales.
