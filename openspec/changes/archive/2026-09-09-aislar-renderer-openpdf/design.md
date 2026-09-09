## Context

Tras el change 2, `PdfService` dibuja desde `InvoiceDocument` y ya no conoce el negocio: no queda ni una referencia a `BigDecimal`, `ResumenFactura`, `LineaFactura`, `CalculoService`, `Formatos` ni `TipoRetencion`. Lo que sigue dentro es dibujo puro, y son 1.187 líneas repartidas así:

| Zona | Líneas | Qué es |
|---|---|---|
| Constantes de estilo | 50-78 | colores, radios, márgenes |
| API pública | 80-102 | `exportar` ×2, `exportarAgrupado` ×2 |
| `exportar(...)` privado | 104-208 | orquestación, medición y anclaje del cierre |
| `concatenar` | 210-225 | unir PDF para la exportación agrupada |
| Fuentes | 227-260 | carga de Calibri y fallback a Helvetica |
| Tarjetas y sus eventos | 262-508 | incluye `RotuloTarjeta`, `ContornoRedondeado`, `ContornoTabla` |
| Tabla de líneas y celdas | 510-600 | |
| Totales, observaciones y pie legal | 602-786 | incluye `FondoPieLegal` |
| Márgenes y utilidades | 788-930 | |
| `Colores` | 930-955 | |
| `CabeceraPie` | 958-1187 | evento de página |

Referencia de arquitectura, para leer y no copiar: `RenderizadorPdfOpenPdf` del proyecto hermano en `C:\Users\juan\Desktop\DAM\programacion\proyectos\factualcazaba`.

## Goals / Non-Goals

**Goals:**

- Que todo el código de la librería PDF viva en la capa de dibujo.
- Que `PdfService` se lea de una sentada.
- Que estilo y estructura dejen de compartir fichero, porque se tocan por motivos distintos.

**Non-Goals:**

- No cambia la API pública, ni un importe, ni una coordenada, ni una página.
- No se toca `InvoiceDocument` ni `InvoiceDocumentBuilder`.
- No se añade interfaz sobre el renderer.

## Decisions

### D1. `PdfService` queda sin una sola referencia a OpenPDF

`concatenar(...)` se va al renderer con el resto, aunque no dibuje: usa `Document`, `PdfCopy` y `PdfReader`, y es la única pieza que impediría cumplir el criterio.

La ventaja de fijarlo así es que **se comprueba con un comando**, no con criterio:

```
grep -c "com.lowagie" src/main/java/com/alcazaba/facturacion/pdf/PdfService.java   →   0
```

Alternativa descartada: dejar `concatenar` en el servicio porque «unir ficheros no es dibujar». Se lee algo más natural, pero el resultado del refactor deja de poder verificarse de un vistazo.

### D2. El dibujo se parte en estructura y estilo

`OpenPdfRenderer` construye los bloques y orquesta el documento. `EstiloPdf` guarda la paleta `Colores`, las constantes de geometría, la carga de fuentes, las celdas base y los cuatro eventos de dibujo (`RotuloTarjeta`, `ContornoRedondeado`, `ContornoTabla`, `FondoPieLegal`).

El corte no es estético: el estilo se toca cuando cambia el aspecto —un color, un radio, un cuerpo de letra— y la estructura cuando cambia la composición. Hoy comparten fichero y cada retoque obliga a leer el otro.

Alternativa descartada: un único `OpenPdfRenderer` de unas 900 líneas. Menos ficheros, pero habríamos cambiado un fichero gigante por otro con otro nombre.

### D3. `filasDatosPago(...)` se elimina, no se mueve

`InvoiceDocumentBuilder.paymentRows(...)` ya compone esas filas. Lo que queda en `PdfService` es un adaptador que las convierte a `String[]`, y su único uso real es preguntar si hay datos de pago:

```java
List<String[]> pagoFilas = filasDatosPago(vc);
...
if (pagoFilas.isEmpty()) { ... }
```

Esa pregunta ya la responde el modelo con `document.paymentCard().isPresent()`. El adaptador desaparece y con él la última vez que la capa de dibujo toca una `VersionCompleta`.

### D4. El anclaje del cierre viaja entero y sin tocar

El bloque de `exportar(...)` que añade la tabla, lee `writer.getVerticalPosition(false)`, calcula el hueco y decide si el cierre cabe **se mueve tal cual**, con el mismo orden de `doc.add(...)`. Costó tres intentos dejarlo fino y sus testigos son tres medidas: `TOTAL.bottom = 123,4 pt`, pie legal en `94,8 pt` y holgura tarjeta→tabla de `23,2 pt` en todas las páginas.

Mover código alrededor de él es exactamente la ocasión de romperlo sin darse cuenta.

## Risks / Trade-offs

- **Es la primera vez en la serie que se tocan los tests.** En los changes 1 y 2 el criterio era `PdfServiceTest` intacto, y eso daba una garantía absoluta. Aquí cuatro tests cambian de clase, y con ello vuelve el riesgo que ya mordió dos veces: ajustar el test en lugar del código. `alturasTarjetasDistintas` pasó en verde con el defecto delante en dos rondas seguidas. Mitigación: la red pasa a ser la comparación píxel a píxel, que no se puede acomodar, y las aserciones que se mudan deben viajar palabra por palabra.
- **`CabeceraPie` es una clase interna no estática** y usa `nz(...)`, `fuente(...)`, `baseNegrita()` y `lineasEmpresa(...)` de la clase que la contiene. Sacarla a fichero propio obliga a pasarle `EstiloPdf` o a hacer estáticos esos helpers. Es la parte con más fricción mecánica del change.
- **Las tablas se miden antes de dibujarse** y la de tarjetas se reutiliza dentro del evento de página con `writeSelectedRows`. Un `PdfPTable` ya medido no es un objeto inocente: al repartir el código hay que conservar que se mida una sola vez y con el mismo ancho.

## 8.3. Defectos observados y no tocados

Ninguno: las 13 páginas salieron píxel-idénticas a las referencias y a los ficheros medidos, y en la inspección visual no se vio ningún defecto de maquetación. Nada que proponer aparte.
