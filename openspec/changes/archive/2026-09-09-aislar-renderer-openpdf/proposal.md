## Why

Es el tercero y último de los changes del refactor de `PdfService`. El primero sacó las consultas de negocio a la capa de servicio; el segundo extrajo `InvoiceDocument` e `InvoiceDocumentBuilder`, de modo que el contenido del documento ya existe como dato y se prueba sin abrir un PDF.

Falta lo que da nombre a la serie. **`PdfService` sigue en 1.187 líneas** porque todo OpenPDF vive dentro. La clase es a la vez fachada pública, orquestador del documento, constructor de cada bloque, dueña de la paleta y las fuentes, y contenedora del evento de página (`CabeceraPie`, unas 230 líneas de clase interna). Quien va a cambiar un margen tiene que atravesar la carga de fuentes; quien toca la paleta se topa con el anclaje del cierre.

Es **una mudanza, no una reescritura**: OpenPDF sigue en el `pom.xml` y las llamadas a la librería son las mismas, línea por línea, con los mismos valores. Solo cambian de fichero.

## What Changes

- Nuevo `OpenPdfRenderer`: recibe el `InvoiceDocument` ya compuesto y lo dibuja. Se lleva la orquestación del documento, el anclaje del cierre, todos los bloques y `concatenar(...)`.
- Nuevo `EstiloPdf`: la paleta `Colores`, las constantes de geometría, la carga de fuentes, las celdas base y los cuatro eventos de dibujo.
- Nuevo `CabeceraPiePdf`: el evento de página, hoy clase interna.
- `PdfService` queda como fachada: API pública y delegación, **sin una sola referencia a OpenPDF**.
- Se elimina `filasDatosPago(...)`, que es un adaptador que ya no hace falta.
- Refactor puro: ni un importe, ni una coordenada, ni una página distinta.

## Capabilities

### New Capabilities

Ninguna.

### Modified Capabilities

- `pdf-rendering`: se amplía «Separación de responsabilidades en la exportación» para recoger el estado final —componer y dibujar son piezas distintas, y el código de la librería vive solo en la de dibujo—. Ningún requisito cambia de comportamiento.

## Impact

- `src/main/java/com/alcazaba/facturacion/pdf/PdfService.java`: adelgaza a fachada, de 1.187 líneas a unas 90.
- `OpenPdfRenderer.java`, `EstiloPdf.java`, `CabeceraPiePdf.java` (nuevos).
- `src/test/java/com/alcazaba/facturacion/pdf/PdfServiceTest.java`: pierde cuatro tests, que se mudan.
- `OpenPdfRendererTest.java` (nuevo): los recibe con sus aserciones intactas.
- No se toca el editor, ni el histórico, ni la configuración, ni `InvoiceDocument`, ni `InvoiceDocumentBuilder`.

## Non-Goals

- No se añade una interfaz sobre el renderer: con una sola implementación es ceremonia. Se añadirá el día que haya una segunda.
- No se corrige ningún defecto de maquetación. Si aparece uno, se anota y se propone aparte.
