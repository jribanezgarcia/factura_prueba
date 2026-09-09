## Context

`PdfService` expone cuatro métodos públicos (`exportar` y `exportarAgrupado`, con y sin color) y a partir de ahí todo es privado: unos sesenta métodos que van desde `celdaCabeceraColumna(...)` hasta `medir(...)`, pasando por tres consultas que no son de dibujo.

Las tres consultas:

- `retencionDeVersion(FacturaVersion v)` (`PdfService.java:653`) reconstruye un `TipoRetencion` a partir del snapshot congelado en la versión (`tipo_retencion_nombre`, `tipo_retencion_porcentaje`). La versión ya lleva ese dato; el PDF lo vuelve a armar.
- `totalConIva(LineaFactura l)` (`PdfService.java:664`) calcula `base × (1 + tipo)` para la columna Total de la tabla. `CalculoService` ya sabe hacer eso.
- `suplidosDe(VersionCompleta vc)` (`PdfService.java:577`) filtra las líneas cuyo tipo de IVA es suplido, criterio que también vive en el dominio.

La red de seguridad es sólida y hay que aprovecharla: `PdfServiceTest` son **33 tests y 949 líneas** que generan el PDF y lo reabren con `PdfReader`, comprobando texto extraído y número de páginas. Es lenta e indirecta, pero detecta cualquier desviación del documento.

En la spec, `invoicing` acumula cuarenta y siete requisitos. Seis describen el papel: «Vista previa de la cabecera del PDF», «Retención en PDF», «Exportación a PDF», «Exportación múltiple a PDF desde el histórico», y los párrafos de PDF de «Orden del desglose de totales» y «Suplidos».

Referencia de arquitectura: el proyecto hermano en `C:\Users\juan\Desktop\DAM\programacion\proyectos\factualcazaba` resolvió esto mismo con tres capas —un `record` de documento ya formateado a texto, un constructor de Java puro y un renderizador que es la única clase que importa la librería—. Se usa como **referencia de diseño, no como fuente de código**: los nombres aquí van en inglés, como el resto de la capa de servicio de este proyecto.

## Goals / Non-Goals

**Goals:**

- Que la capa de exportación deje de tener su propia copia de tres reglas de negocio.
- Que la especificación separe lo que es facturación de lo que es documento impreso.
- Dejar el terreno preparado para extraer el modelo de documento (change 2) sin arrastrar negocio con él.

**Non-Goals:**

- No cambia nada de lo impreso. Ni un importe, ni una coordenada, ni una página.
- No se extrae el modelo de documento ni el renderizador todavía.
- No se toca `PdfServiceTest`.
- No se corrige ningún defecto de maquetación que aparezca por el camino.

## Decisions

### D1. Tres changes encadenados, no uno

Son 1.295 líneas. Hacerlo de una sentada significa que, cuando un test de número de páginas falle, no sabrás si la causa fue mover la retención, extraer el modelo o reescribir el renderizador.

El orden importa y no es arbitrario: primero se saca el negocio, porque si se extrajera el modelo de documento antes, el modelo se llevaría dentro las tres consultas y habría que volver a sacarlas después.

1. **Este change**: la capability, el traslado de requisitos y las tres consultas de negocio.
2. **Change 2**: extraer `InvoiceDocument` (modelo textual) y `InvoiceDocumentBuilder`, paginación incluida, con tests que no abren ningún PDF.
3. **Change 3**: extraer la interfaz `DocumentRenderer` y `OpenPdfRenderer`; `PdfService` queda como fachada delgada, y se añade el requisito de que una sola clase importe la librería.

### D2. La API pública de `PdfService` no cambia en ninguno de los tres

`exportar(vc, empresa, ruta)`, su variante con `colorHex` y las dos de `exportarAgrupado` conservan firma y semántica de principio a fin. Quien llama al PDF —`EditorController`, `HistoricoController`— no se entera de nada.

Esto es lo que permite que `PdfServiceTest` sirva de red sin tocarlo.

### D3. Dónde va cada consulta trasladada

- `totalConIva` → `CalculoService`, que ya tiene `totalLinea`, `ivaDeBase` y el resto de la aritmética. Es su sitio natural y probablemente ya exista un equivalente: **antes de añadirlo, comprobar si `CalculoService.totalLinea(...)` ya hace exactamente esto**. Si lo hace, no se añade nada: se borra el duplicado del PDF y se llama al que ya había.
- `suplidosDe` → el criterio de «esto es un suplido» pertenece al dominio. Si `TipoIva` ya sabe responder si es suplido, el filtro se resuelve en una línea desde el servicio.
- `retencionDeVersion` → `FacturaService`, junto a lo que ya arma la `VersionCompleta`. La alternativa sería que `VersionCompleta` expusiera la retención ya resuelta como campo.

**Decisión tomada al implementar (4.1/4.2): método estático en `FacturaService`, no campo.** El campo obligaría a tocar los 34 sitios que construyen `VersionCompleta` en `PdfServiceTest`, que no se puede modificar; solo hay un sitio productivo (`abrirVersion`). El método es `public static TipoRetencion retencionDeVersion(FacturaVersion v)` con la lógica del snapshot verbatim y sin acceso al catálogo, usable tal cual por el constructor puro del change 2.

Nota: `RectificativaService` tiene su propia `retencionDeVersion` con semántica distinta (catálogo vivo primero, snapshot como fallback) porque crea facturas *nuevas*; no se toca en este change (ver 5.2).

Barrido 5.1/5.2: en `PdfService` no queda ningún otro cálculo ni clasificación de negocio. El `continue` de `tablaLineas(...)` sobre `isEsSuplido()` es lectura de presentación del flag congelado (igual que `isExenta()` para la columna IVA %), no una consulta: moverlo sería churn. `porcentajeRejilla` e `importePdf` son formato y se quedan. Tras el change, `PdfService` solo usa del servicio `CalculoService.resumen/totalConIva/suplidosDe` y `FacturaService.retencionDeVersion`: el pipeline imprescindible que el change 2 recableará al constructor.

### D4. El criterio de aceptación es binario

Los 33 tests de `PdfServiceTest` pasan sin modificarse. No hay «pasan casi todos», no hay relajar una aserción a `assertTrue(n >= ...)`, no hay actualizar un número esperado.

Si un test falla, el refactor está mal: en este change ningún cambio puede alterar el documento. Es la única forma de que la red sirva de algo.

### D5. Los requisitos se mueven con su texto intacto

Al trasladar a `pdf-rendering` no se reescribe ni una frase. Cualquier mejora de redacción se vería como un cambio de comportamiento en la revisión y confundiría el diff.

Las dos excepciones son los requisitos que se parten, y ahí lo que se hace es **quitar**, nunca reescribir: «Orden del desglose de totales» pierde su párrafo de PDF y el escenario «Factura con descuento en el PDF»; «Suplidos» pierde su párrafo de PDF, el párrafo de la tabla vacía cuando todo son suplidos, y el escenario «El suplido tiene su propio bloque en el PDF». Esos fragmentos aparecen literalmente en la capability nueva.

### D6. Por qué partir esos dos y no moverlos enteros

«Suplidos» dice a la vez que un suplido no entra en la base imponible —regla fiscal, que seguiría siendo cierta aunque no existiera el PDF— y cómo se dibuja su bloque en el papel. Moverlo entero sacaría una regla de negocio de la capability de negocio.

El mismo razonamiento vale para «Orden del desglose de totales», que además describe la matriz del editor.

### D7. La partición por escenarios choca con el CLI (openspec 1.10.0)

Un `MODIFIED` reemplaza el bloque entero, y tanto `validate --strict` como el
propio archive **rechazan** soltar escenarios (`validator.js` /
`specs-apply.js`, `findMissingCurrentScenarios`). Tampoco vale `REMOVED` +
`ADDED` con el mismo nombre en el mismo fichero: el validador lo rechaza
(`Requirement present in both ADDED and REMOVED`). Es decir: con esta versión
del CLI, quitar «Factura con descuento en el PDF» y «El suplido tiene su
propio bloque en el PDF» de `invoicing` dejando el resto es inexpresable, y el
change, tal como está, no valida en verde ni puede archivarse.

Opciones que sí validan, a elegir por el propietario:

- **A. Duplicar los dos escenarios**: se quedan en `invoicing` (bloques
  `MODIFIED` completos) y además van a `pdf-rendering`. Cero cambios de
  títulos, pero el contenido queda en dos capabilities.
- **B. Renombrar al partir**: `REMOVED` de los dos requisitos en `invoicing` y
  `ADDED` con títulos nuevos, más los fragmentos en `pdf-rendering`. Cero
  duplicados, pero cambian dos títulos y hay que reescribir `tasks.md 7.2`.

Elegida **B**, con «Desglose de totales por tipo de IVA» y «Suplidos en la
facturación»: los títulos descartados falseaban el contenido (el primer
párrafo del desglose es regla general, no de editor; lo que queda de suplidos
no es solo fiscal). Duplicar los escenarios (opción A) reproduciría en la spec
el mismo defecto que este change corrige en el código: dos copias de la misma
regla en sitios distintos, con el mismo riesgo de que solo una se actualice.

Limpieza para un change posterior: el título «Factura de solo suplidos» ya
estaba duplicado en la spec viva antes de este change —en «Exportación a PDF»
y en «Suplidos», con textos distintos y complementarios— y tras el traslado
`pdf-rendering` lo hereda en «Exportación a PDF» y en «Suplidos en el PDF».
No se fusiona ni se borra ninguno aquí porque este change no pierde contenido;
quien lo toque después deberá decidir si los dos escenarios siguen teniendo
sentido por separado.

## Risks / Trade-offs

- **Riesgo bajo, coste de ceremonia alto.** Tres changes son tres propuestas, tres revisiones y tres archivados para un refactor que no cambia nada visible. Se acepta a cambio de poder localizar la causa cuando algo falle.
- **La spec queda repartida entre dos capabilities**, así que una regla como la de suplidos hay que leerla en dos sitios. Las notas de procedencia en ambos lados existen precisamente para eso.
- **`PdfServiceTest` es lento** (genera y reabre PDFs). Como no se toca y hay que pasarlo varias veces, este change tarda más en verificarse de lo que tarda en escribirse. Es el precio de la red.
- **`VersionCompleta` podría crecer.** Si se opta por el campo en lugar del método (D3), hay que vigilar que no se convierta en el cajón donde acaba todo lo que el PDF necesita.
- **8.1: sin defectos de maquetación que anotar.** En los PDF renderizados (suplidos, varias páginas p1-p2) no se observó ningún defecto: nada que proponer aparte.
