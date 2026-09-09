## Context

Tras el change 1, `PdfService` ya no calcula nada: resuelve retención, totales con IVA y suplidos desde `CalculoService` y `FacturaService`. Lo que queda dentro es composición (qué textos, qué filas, en qué orden) mezclada con dibujo OpenPDF (`Document`, `PdfWriter`, `writeSelectedRows`, eventos de página).

La red de seguridad sigue siendo `PdfServiceTest`: 33 tests que generan y reabren PDFs. Este change añade una red nueva: tests sobre el builder sin abrir ningún PDF.

Referencia de arquitectura (leer, no copiar): `DocumentoFactura` y `ConstructorDocumentoFactura` del proyecto hermano en `C:\Users\juan\Desktop\DAM\programacion\proyectos\factualcazaba`.

## Goals / Non-Goals

**Goals:**

- Que el contenido del documento —textos, rótulos y filas— exista como dato, independiente de OpenPDF.
- Que ese contenido sea verificable sin generar ni reabrir ningún PDF.
- Que `PdfService` dibuje desde el modelo sin derivar nada.

**Non-Goals:**

- No se aísla el código OpenPDF: sigue en `PdfService` hasta el change 3 (`OpenPdfRenderer`).
- No cambia la API pública, ni un importe, ni una coordenada, ni una página.
- No se toca `PdfServiceTest`.

## Decisions

### D1. El builder compone contenido; la paginación se queda en el dibujo

El corte es este: **el builder decide qué textos y qué filas hay; el dibujo decide en qué página caen.** El modelo no conoce páginas, ni alturas, ni coordenadas.

La razón no es de gusto, es que la paginación aquí **no se puede calcular antes de dibujar**. El anclaje del cierre funciona así (`PdfService.exportar`):

1. `doc.add(tablaLineasTabla)` — iText reparte las filas entre páginas.
2. `writer.getVerticalPosition(false)` — se lee el cursor **después**.
3. Con ese `y` se calcula el hueco y se decide si el cierre cabe.

Ese `y` depende de cómo iText haya partido la tabla, y eso depende de las alturas reales de descripciones que **pueden ocupar varias líneas**, cosa que el spec exige expresamente. No existe antes de dibujar. Pasárselo al builder obligaría a una de dos: reimplementar el cálculo de alturas de texto de iText, o construir el documento en dos fases —componer a medias, dibujar las líneas, medir, completar—, y entonces el modelo deja de poder construirse de una vez, que es la premisa de D2 y de los tests sin PDF.

Alternativa descartada: contar filas por página como hace el proyecto hermano. Allí funciona porque `truncar(...)` recorta la descripción a 80 caracteres y toda fila mide igual. Aquí eso sería un cambio visible y contra el requisito de descripciones multilínea.

Lo que sí es puro y ya lo es hoy: el cálculo del hueco dado `y`, `bottom` y el alto del cierre. Se queda donde está, en `PdfService`, y en el change 3 pasa al renderer, que es quien mide.

### D2. El record solo lleva texto compuesto y decisiones

`InvoiceDocument` no conoce `BigDecimal`, ni `LocalDate`, ni ningún tipo de OpenPDF: importes ya formateados, fechas ya formateadas, rótulos fijos como datos (`String`) y cada fila condicional como `Optional`. Si el dibujo necesita un `if` sobre negocio, el modelo está mal: el builder ya lo resolvió.

Alternativa descartada: modelo con tipos ricos (montos, fechas). Obligaría al dibujo a formatear y reabriría la puerta que cierra el change 1.

### D3. El formato viaja con el builder

`importePdf` y `porcentajeRejilla` son formato, no negocio, y el modelo solo admite texto: entran al builder con salida idéntica, verificada por los tests nuevos y por `PdfServiceTest` sin tocar.

### D4. `PdfService` adelgaza por dentro, sin cambiar por fuera

`exportar` / `exportarAgrupado` conservan firma y semántica; por dentro componen con el builder y dibujan el modelo. Quien llama (`EditorController`, `HistoricoController`) no se entera.

## Risks / Trade-offs

- **El anclaje del cierre es lo que no se puede tocar.** Costó tres intentos dejarlo fino y su testigo son tres medidas: `TOTAL.bottom = 123,4 pt`, pie legal en `94,8 pt` y holgura tarjeta→tabla de `23,2 pt` en todas las páginas. Por D1 este change no lo toca, pero pasa por al lado: si al reordenar `exportar(...)` se altera el orden de los `doc.add(...)`, se rompe. Mitigación: no mover ese bloque de sitio y comprobar las tres medidas al final.
- **La cobertura del modelo.** El riesgo real pasa a ser que el modelo se deje una variante de contenido —una fila condicional, un rótulo que hoy es literal en el dibujo— y el `if` sobrevida en `PdfService`. Mitigación: revisar que no quede ni un `if` sobre datos de negocio en el código de dibujo.
- **Granularidad del record.** Demasiado fino y el dibujo es un volcado bobo difícil de revisar; demasiado grueso y vuelve a esconder decisiones. Mitigación: el modelo refleja 1:1 los bloques dibujados (tarjetas, tabla, marco, suplidos, totales, observaciones, pie).
- **Reparto poco natural durante la transición.** Hasta el change 3, `PdfService` dibuja y además mide y pagina. Es feo pero está acotado y es temporal: el change 3 se lleva las tres cosas al renderer de una vez.

## Migration Plan

No hay migración: refactor interno sin cambios de UI, de API ni de formato de fichero. Reversión: revertir el commit.
