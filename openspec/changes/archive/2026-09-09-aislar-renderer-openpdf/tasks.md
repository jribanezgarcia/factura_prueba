> Decisiones y sus porqués: `design.md`. Referencia de arquitectura (leer, no copiar):
> `RenderizadorPdfOpenPdf` del proyecto hermano en
> `C:\Users\juan\Desktop\DAM\programacion\proyectos\factualcazaba`.
>
> **Es una mudanza, no una reescritura.** OpenPDF sigue en el `pom.xml` y las llamadas a la
> librería son las mismas, línea por línea, con los mismos valores. Solo cambian de fichero. Si te
> encuentras reescribiendo cómo se dibuja algo, para: eso no es este change.
>
> **La red de seguridad ya no son los tests, es el píxel.** Por primera vez en la serie hay que
> tocar `PdfServiceTest`, y en cuanto se toca un test vuelve el riesgo de ajustarlo en vez de
> arreglar el código: `alturasTarjetasDistintas` pasó en verde con el defecto delante en dos rondas
> seguidas. Las 13 páginas tienen que salir **idénticas**, no parecidas.

## 1. Fotografía de partida

- [x] 1.1 `mvn test` y anotar el número exacto de tests en verde (deben ser 210).
- [x] 1.2 Exportar los nueve casos de siempre —1 línea, 2, 10, 20, con suplidos, solo suplidos, dos páginas, con logo y anulada de dos páginas— con la empresa completa y el pie legal real de `capturas_pantalla/pie_factura.txt`.
- [x] 1.3 Renderizar cada página a PNG y guardarlos como referencia. Son 13 páginas en total.

## 2. `EstiloPdf`: paleta, fuentes y eventos de dibujo

- [x] 2.1 Crear `EstiloPdf` en el paquete `pdf` y llevarle la clase `Colores` tal cual.
- [x] 2.2 Llevarle las constantes de geometría y color de `PdfService.java:50-78`, la carga de fuentes de `:227-260` y los helpers `fuente(...)`, `baseRegular()`, `baseNegrita()`, `baseCursiva()`.
- [x] 2.3 Llevarle los cuatro eventos de dibujo: `RotuloTarjeta`, `ContornoRedondeado`, `ContornoTabla` y `FondoPieLegal`.
- [x] 2.4 Compilar y pasar la suite. Sigue en 210.

## 3. `CabeceraPiePdf`: el evento de página

- [x] 3.1 Sacar la clase interna `CabeceraPie` (`PdfService.java:958-1187`) a fichero propio.
- [x] 3.2 Es una clase interna **no estática** que usa `nz(...)`, `fuente(...)`, `baseNegrita()` y `lineasEmpresa(...)` de la clase que la contiene. Resolverlo pasándole `EstiloPdf` o haciendo estáticos esos helpers. Es la parte con más fricción del change (`design.md - Risks`).
- [x] 3.3 `yTarjetas(...)` y su invariante no se tocan: solo cambian de clase.
- [x] 3.4 Compilar y pasar la suite.

## 4. `OpenPdfRenderer`: el dibujo

- [x] 4.1 Crear `OpenPdfRenderer` y llevarle la orquestación de `exportar(...)` privado (`:104-208`), todos los constructores de bloque —tarjetas, tabla de líneas, marco, suplidos, rejillas, observaciones, pie legal— y las utilidades de medición y márgenes.
- [x] 4.2 Llevarle también `concatenar(...)` (`:210-225`), aunque no dibuje: usa `Document`, `PdfCopy` y `PdfReader` (`design.md - D1`).
- [x] 4.3 **El bloque del anclaje se mueve tal cual**: mismo orden de `doc.add(...)`, misma lectura de `writer.getVerticalPosition(false)`, mismo cálculo del hueco. No lo reordenes ni lo «mejores» (`design.md - D4`).
- [x] 4.4 Eliminar `filasDatosPago(...)` y sustituir su uso por `document.paymentCard().isPresent()` (`design.md - D3`).
- [x] 4.5 Borrar los dos métodos muertos que quedan a la vista, `espacio(Document, float)` y `partir(String, BaseFont, float, float)`. Comprobar antes que nadie los llama, no darlo por hecho.
- [x] 4.6 Unificar `VALOR_SUAVE` con `GRIS`: son el mismo `0x555555` con dos nombres.

## 5. `PdfService`: la fachada

- [x] 5.1 Dejar solo la API pública —`exportar` ×2 y `exportarAgrupado` ×2— delegando en el renderer.
- [x] 5.2 **Criterio verificable:** `grep -c "com.lowagie" src/main/java/com/alcazaba/facturacion/pdf/PdfService.java` debe dar `0`. Si da otra cosa, queda dibujo dentro.
- [x] 5.3 Las firmas públicas no cambian. `EditorController` y `HistoricoController` no se tocan ni se enteran.

## 6. La mudanza de los cuatro tests

- [x] 6.1 Crear `OpenPdfRendererTest` y mover a él los cuatro tests acoplados a métodos internos: `yTarjetasNoEntraEnAreaTexto` (`PdfServiceTest.java:593-600`), el del marco (`:717-719`), `alturasTarjetasDistintas` (`:894-904`) y el del invariante D4 (`:933-940`).
- [x] 6.2 Las aserciones viajan **palabra por palabra**. Solo cambian el receptor de la llamada y los imports. Ni un valor esperado, ni un nombre de test, ni un mensaje.
- [x] 6.3 `alturasTarjetasDistintas` usaba `svc.filasDatosPago(vc)`, que desaparece: constrúyele la tarjeta de pago desde el modelo. Es el único de los cuatro que necesita algo más que cambiar el receptor.
- [x] 6.4 **Criterio verificable:** el diff de `PdfServiceTest` no puede tener **ni una línea añadida**, solo borrados. Y el total de tests sigue siendo **210**. Si sale otro número, falta o sobra algo.

## 7. Verificación de que nada ha cambiado

- [x] 7.1 `mvn test`: 210 verdes, sin adaptar ninguna aserción.
- [x] 7.2 Regenerar los nueve PDF y compararlos con las referencias del punto 1.3 **píxel a píxel**, con `ImageChops.difference(...).getbbox()`. Las 13 páginas deben dar `None`. Parecido no vale.
- [x] 7.3 Las tres medidas testigo: `TOTAL.bottom = 123,4 pt`, pie legal acabando en `94,8 pt` y holgura tarjeta→tabla de `23,2 pt` en **todas** las páginas.
- [x] 7.4 Si alguna página difiere, **parar** y decir en cuál y en qué. No sigas con una diferencia sin explicar.
- [x] 7.5 `wc -l` de los cuatro ficheros: `PdfService` en torno a 90 líneas y ninguno por encima de unas 700.

## 8. La especificación y el cierre

- [x] 8.1 `openspec validate aislar-renderer-openpdf --strict`.
- [x] 8.2 Comprobar que el delta conserva los dos escenarios que el requisito ya tenía y añade los dos nuevos.
- [x] 8.3 Anotar los defectos de maquetación que hayan aparecido y no se hayan tocado, para proponerlos aparte.
- [ ] 8.4 Aplicar y archivar. Al archivar, `git add -A` y comprobar que `git status --short` queda vacío, según la guía de `openspec/config.yaml`.
