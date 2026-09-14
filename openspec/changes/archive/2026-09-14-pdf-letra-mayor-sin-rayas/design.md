## Context

Todo el PDF lo dibujan cuatro clases de `src/main/java/com/alcazaba/facturacion/pdf/`: `OpenPdfRenderer` (tarjetas, tablas, totales, observaciones, pie legal), `EstiloPdf` (constantes y eventos de dibujo), `CabeceraPiePdf` (cabecera, paginación y marca `ANULADA`) y `CabeceraLayout` (altos de cabecera compartidos con la vista previa `ui/PreviaCabecera`).

Hoy cada celda de la tabla de líneas (`celdaLinea`) tiene borde por los cuatro lados, así que cada artículo queda entre dos rayas. La cabecera de columnas (`celdaCabeceraColumna`) también tiene los cuatro bordes, y el marco de relleno (`tablaRelleno`) tiene `LEFT | RIGHT | BOTTOM`: su borde de abajo es la raya que cierra la tabla antes de suplidos o totales.

`PdfPCell` construida con una `Phrase` fija el interlineado con `setLeading(0, 1)`, es decir, 1 × el tamaño de letra (comprobado en el bytecode de OpenPDF 1.3.39). Hoy la descripción, a 9 pt, tiene 9 pt entre renglones.

## Goals / Non-Goals

**Goals:**

- Letra 1 pt mayor en todo el documento.
- Descripciones de varias líneas con 11 pt entre renglones.
- Tabla de líneas sin rayas horizontales entre artículos, pero siempre cerrada por abajo.

**Non-Goals:**

- No se cambian anchos de columna, colores, textos ni importes.
- No se toca la tabla de suplidos más allá de su letra: conserva sus bordes.
- No se quita el rayado alterno de fondo.
- No se toca la familia tipográfica.

## Decisions

### D1. Decisión: +1 pt en cada tamaño, con lista cerrada

Cada tamaño sube exactamente 1 pt. La lista es completa: si aparece un tamaño que no está aquí, no se toca y se anota.

**`OpenPdfRenderer`**

| Sitio | Antes | Después |
|---|---|---|
| `tarjetaCliente`: marca de tarjeta vacía (`card.emptyMarker()`) | 9.5f | 10.5f |
| `tarjetaCliente` y `tarjetaPago`: `bfEtiq.getWidthPoint(..., 8.5f)` | 8.5f | 9.5f |
| `tarjetaCliente` y `tarjetaPago`: fuente de la etiqueta | 8.5f | 9.5f |
| `tarjetaCliente`: valor «Nombre» en negrita | 10.5f | 11.5f |
| `tarjetaCliente` y `tarjetaPago`: resto de valores | 9.5f | 10.5f |
| `bloqueSuplidos`: nota | 7f | 8f |
| `celdaCabeceraColumnaCompacta` | 7f | 8f |
| `celdaLineaCompacta` | 8f | 9f |
| `celdaCabeceraColumna` | 8f | 9f |
| `celdaLinea` | 9f | 10f |
| `notaDescuento`: fuente y `setLeading` | 7f y 8.5f | 8f y 9.5f |
| `rejillaLiquidacion`: `etiquetaTotal` e `importeTotal` | 11f | 12f |
| `celdaCabeceraRejilla` | 7f | 8f |
| `celdaCuerpoRejilla`: las dos fuentes (cursiva de retención y normal) | 8.5f | 9.5f |
| `cajaObservaciones`: título «Observaciones» | 8.5f | 9.5f |
| `cajaObservaciones`: texto | 9f | 10f |

**`EstiloPdf`**

| Sitio | Antes | Después |
|---|---|---|
| `PIE_LEGAL_TAM` (el interlineado `PIE_LEGAL_TAM + 1.5f` sigue igual) | 6.5f | 7.5f |
| `RotuloTarjeta.cellLayout`: `setFontAndSize` y el `8.5f` de la fórmula de centrado `(h - 8.5f) / 2` | 8.5f | 9.5f |

**`CabeceraPiePdf`**

| Sitio | Antes | Después |
|---|---|---|
| `onEndPage`: `tamNombre` en modo logo | 13f | 14f |
| `onEndPage`: `tamNombre` en modo texto | 15f | 16f |
| `dibujarDatosEmpresa`: líneas de empresa (`ajustarTamano(..., 9f, ...)`) | 9f | 10f |
| `ajustarTamano`: mínimo legible (las dos apariciones de `9f`) | 9f | 10f |
| `dibujarChipNif`: tamaño del NIF | 9f | 10f |
| `dibujarBloqueFactura`: FACTURA / RECTIFICATIVA | 18 | 19 |
| `dibujarBloqueFactura`: número y fecha | 10 | 11 |
| `dibujarBloqueFactura`: «Rectifica a» | 9 | 10 |
| `dibujarBloqueFactura`: ANULADA | 11 | 12 |
| `dibujarRotulo`: SERIE / Nº y FECHA | 6.5f | 7.5f |
| `dibujarPaginacion`: `getWidthPoint("00", 8)` y `setFontAndSize(bfPie, 8)` | 8 | 9 |
| `onCloseDocument`: fuente del total de páginas | 8 | 9 |
| `dibujarMarcaAnulada`: las dos apariciones de 64 | 64 | 65 |

No se tocan los `Phrase(" ")` de 1 pt de `cabeceraTarjeta` y `espaciador`, ni el `Phrase(" ")` de `tablaRelleno` ni el de los huecos de `tarjetas`: son espaciadores invisibles con alto fijo o sin texto.

Alternativa descartada: una constante de «aumento» sumada a todos los tamaños. Es más elegante, pero añade un concepto nuevo y deja los números reales escondidos; cambiar el literal se lee mejor.

### D2. Decisión: la cabecera gana 1 pt de aire por línea

Con la letra de empresa a 10 pt, el paso de 13 pt entre líneas queda justo. Se sube a 14 y se ajustan los huecos que dependen de él:

- `CabeceraPiePdf.dibujarDatosEmpresa`: `y -= 13` → `y -= 14`.
- `CabeceraLayout.altoCabeceraTexto`: `42f + lineas * 13f + 18f` → `42f + lineas * 14f + 18f`. Actualizar su comentario («42 pt de arranque mas 14 pt por linea»). (Corregido tras el apply: el borrador decía `43f`, que no da los altos de la tarea 3.4.)
- `CabeceraLayout.altoCabeceraLogo`: `17f + lineas * 13f` → `18f + lineas * 14f`.
- `CabeceraPiePdf.dibujarChipNif`: el rectángulo pasa de `yBase - 3.5f` y alto `12.5f` a `yBase - 4f` y alto `13.5f` (en `fill` y en `stroke`).
- `CabeceraPiePdf.dibujarBloqueFactura`: los dos `y -= 9` (rótulo → valor) pasan a `y -= 10`; `y -= 14` (número → rótulo FECHA) pasa a `y -= 15`. `y -= 22`, `y -= 12` y `y -= 15` no cambian.

`ui/PreviaCabecera` debe usar la misma geometría (lo exige el requisito «Vista previa de la cabecera del PDF»):

- `dibujarBloqueTexto(..., 13 * s, s)` → `14 * s`; `dibujarBloqueTexto(izquierda, 15 * s, s)` → `16 * s`.
- En `dibujarBloqueTexto`: `9 * s` → `10 * s` y `y += 13 * s` → `y += 14 * s`.
- En `dibujarChipNif`: los dos `9 * s` → `10 * s`, `3.5 * s` → `4 * s` y `12.5 * s` → `13.5 * s`.

`CabeceraLayoutTest.elAltoDeCabeceraCreceConLasLineas` pasa a esperar 108, 108, 116 y 130 para 0, 3, 4 y 5 líneas. `elAltoDeCabeceraConLogoUsaLaCajaFija` sigue en 170.

### D3. Decisión: interlineado de 1 pt extra en toda la fila

En `celdaLinea` se añade `celula.setLeading(1f, 1f)`: interlineado = 1 pt fijo + 1 × tamaño = 11 pt con letra de 10.

Se aplica a las cinco celdas de la fila, no solo a Descripción. El primer renglón de una celda se coloca según su interlineado; si solo lo llevara Descripción, su primera línea quedaría 1 pt más baja que la cantidad, el precio y el total de la misma fila.

`celdaLineaCompacta` (suplidos) no se toca: el usuario lo ha pedido para la tabla de líneas.

### D4. Decisión: sin rayas entre artículos

- `celdaLinea`: añadir `celula.setBorder(Rectangle.LEFT | Rectangle.RIGHT)`. El color de borde se queda.
- `celdaCabeceraColumna`: sin cambios. Conserva los cuatro bordes: son la franja de arriba y la raya bajo `CANT. / DESCRIPCIÓN / …`.
- `tablaRelleno`: sin cambios. Su `BOTTOM` es la última raya antes de suplidos o totales.
- El rayado alterno (`c.clarisimo` en filas impares) se queda: sin rayas, es lo que separa un artículo del siguiente.

Alternativa descartada: quitar también el rayado alterno. El usuario prefiere mantenerlo.

### D5. Decisión: raya de cierre cuando la tabla se corta de página

Sin rayas entre filas, si la tabla de líneas se parte entre dos páginas, la página cortada acaba sin raya abajo. Hoy no pasa porque cada fila la trae.

Se añade en `EstiloPdf` una clase pequeña, junto a `ContornoTabla` y con su mismo estilo:

```java
static final class RayaAlCortar implements PdfPTableEvent {
    private final Color borde;
    RayaAlCortar(Color borde) {
        this.borde = borde;
    }
    @Override
    public void tableLayout(PdfPTable table, float[][] widths, float[] heights, int headerRows, int rowStart, PdfContentByte[] canvases) {
        int filasDibujadas = heights.length - 1 - headerRows;
        if (rowStart + filasDibujadas >= table.size()) {
            return;
        }
        float x1 = widths[0][0];
        float x2 = widths[0][widths[0].length - 1];
        float y = heights[heights.length - 1];
        PdfContentByte cb = canvases[PdfPTable.LINECANVAS];
        cb.saveState();
        cb.setColorStroke(borde);
        cb.setLineWidth(0.5f);
        cb.moveTo(x1, y);
        cb.lineTo(x2, y);
        cb.stroke();
        cb.restoreState();
    }
}
```

En `OpenPdfRenderer.tablaLineas`, tras crear la tabla: `t.setTableEvent(new EstiloPdf.RayaAlCortar(c.bordeTabla))`. No se pone en `tablaLineasVacia`, que se usa sola en la página nueva del cierre y no se corta nunca.

En el último trozo no se dibuja nada, porque justo debajo va el marco de relleno y su raya inferior; una raya ahí sería la que el usuario quiere quitar.

**Hay que comprobar la condición del último trozo** con la factura de 60 líneas (tarea 5.3): la página 1 debe acabar con raya y la última página no debe tener raya entre la última línea y el marco. Si la cuenta de `rowStart` y `headerRows` de OpenPDF no cuadra con la fórmula, no improvisar otra solución: parar y anotarlo en `tasks.md`.

### D6. Decisión: medir y, si no cabe, recortar rellenos, nunca letra ni anchos

El requisito «Reparto de anchos de la tabla de líneas» sigue en vigor y la letra mayor puede romper tres de sus garantías. Se mide antes y después (tareas 1 y 6):

1. **Banda `TOTAL` a 12 pt.** Estimación: `9.999.999,99 €` en negrita ≈ 72 pt, más 10 pt de relleno, frente a unos 80 pt de columna. Probablemente se parte. Si se parte: en `rejillaLiquidacion`, `importeTotal` y `etiquetaTotal` cambian `setPadding(5f)` por `setPaddingTop(5f)`, `setPaddingBottom(5f)`, `setPaddingLeft(3f)` y `setPaddingRight(3f)`. Si aun así no cabe, parar y anotarlo.
2. **20 líneas en una página** (`PdfServiceTest.numeroPaginasPorCasos`). Cada fila crece unos 2 pt. Si la factura de 20 líneas pasa a dos páginas: en `celdaLinea`, `setPadding(4)` pasa a `setPaddingTop(3)`, `setPaddingBottom(3)`, `setPaddingLeft(4)` y `setPaddingRight(4)`. No se toca el test.
3. **Precio `1.000.000,00` a 10 pt.** Estimación: ≈ 53 pt de texto + 8 de relleno frente a ≈ 69 pt de columna. Se espera que quepa; solo se verifica.

Alternativa descartada: reducir la letra de la banda o ensanchar columnas. El requisito lo prohíbe y rompería la alineación de las rejillas con la tabla.

## Correcciones tras revisar el PDF aplicado

> Añadidas el 14/09/2026 después de revisar `A-3-9.pdf`, una factura de tres líneas con suplidos, retención, logo y pie legal largo.

### D7. Decisión: `RayaAlCortar` cuenta las filas que lleva dibujadas

**Defecto:** en la factura de una página sale una raya bajo la última línea («otro elemento 2»), justo lo que el usuario pidió quitar. Opencode lo midió en la tarea 5.3: OpenPDF pasa `rowStart = 0` en **todos** los trozos, así que la condición de D5 nunca reconoce el último y la raya se dibuja siempre.

**Solución:** el evento guarda en un campo cuántas filas de datos lleva dibujadas y lo va sumando en cada trozo. Solo dibuja la raya si, después de sumar, todavía quedan filas por dibujar en otra página:

```java
static final class RayaAlCortar implements PdfPTableEvent {
    private final Color borde;
    private int filasYaDibujadas = 0;
    RayaAlCortar(Color borde) {
        this.borde = borde;
    }
    @Override
    public void tableLayout(PdfPTable table, float[][] widths, float[] heights, int headerRows, int rowStart, PdfContentByte[] canvases) {
        filasYaDibujadas += heights.length - 1 - headerRows;
        if (filasYaDibujadas + headerRows >= table.size()) {
            return;
        }
        float x1 = widths[0][0];
        float x2 = widths[0][widths[0].length - 1];
        float y = heights[heights.length - 1];
        PdfContentByte cb = canvases[PdfPTable.LINECANVAS];
        cb.saveState();
        cb.setColorStroke(borde);
        cb.setLineWidth(0.5f);
        cb.moveTo(x1, y);
        cb.lineTo(x2, y);
        cb.stroke();
        cb.restoreState();
    }
}
```

Con las cifras medidas por opencode (tabla de 61 filas, 1 de cabecera, trozos de 32 y 28): tras la página 1 van 32, y 32 + 1 < 61, así que se dibuja. Tras la página 2 van 60, y 60 + 1 = 61, así que no. En la factura de 3 líneas: 3 + 1 = 4, así que no se dibuja.

Funciona porque `tablaLineas` crea un evento nuevo en cada exportación y OpenPDF dibuja los trozos en orden. Si una fila muy alta se partiera entre dos páginas, contaría dos veces; eso solo adelanta la cuenta y en el último trozo sigue sin dibujar.

Alternativa descartada: quitar la raya al cortar de página. La página cortada quedaría con la tabla abierta por abajo.

### D8. Decisión: el pie legal baja a 5,5 pt

El usuario no quería que el pie legal subiera con el resto: debe quedar 2 pt por debajo de lo aplicado. `EstiloPdf.PIE_LEGAL_TAM` pasa de `7.5f` a `5.5f`. El interlineado sigue siendo `PIE_LEGAL_TAM + 1.5f` (7 pt). Esto sustituye la fila de `PIE_LEGAL_TAM` de la tabla de D1.

### D9. Decisión: márgenes de la banda TOTAL y de los suplidos

- **Banda TOTAL.** El ajuste 1 de D6 solo le hacía falta al importe. `etiquetaTotal` vuelve a `setPaddingLeft(5f)` y `setPaddingRight(5f)`; `importeTotal` se queda con 3 pt a los lados.
- **Suplidos.** En `celdaLineaCompacta`, `setPadding(2.5f)` pasa a `setPaddingTop(2.5f)`, `setPaddingBottom(2.5f)`, `setPaddingLeft(4f)` y `setPaddingRight(4f)`, igual que la tabla de líneas a los lados. El alto de fila no cambia, así que no puede añadir páginas. Un suplido de siete cifras (`1.000.000,00`) sigue cabiendo: a 9 pt mide ≈ 48 pt, más 8 de relleno, en una columna de ≈ 80 pt.

### D10. Decisión: los datos de empresa arrancan donde acaba el logo dibujado

> **Sustituida por D11** tras ver el resultado: el usuario prefiere que el hueco del logo y la posición de los datos sean siempre los mismos. Se conserva el método `anchoLogoDibujado`, que pasa a usarse en D12.

**Defecto (anterior a este change):** en modo logo los datos de empresa empiezan siempre a `ANCHO_LOGO_FIJO + 14` pt del margen. La caja del logo mide 240 × 120, pero un logo cuadrado se dibuja a 120 × 120, y queda un hueco de unos 120 pt entre el logo y «Empresa Demo S.L.».

**Solución:** la misma regla en el PDF y en la vista previa, calculada en un único sitio.

- En `CabeceraLayout`, añadir:

  ```java
  public static float anchoLogoDibujado(float anchoImagen, float altoImagen) {
      if (anchoImagen <= 0 || altoImagen <= 0) {
          return ANCHO_LOGO_FIJO;
      }
      float escala = Math.min(ANCHO_LOGO_FIJO / anchoImagen, ALTO_LOGO_FIJO / altoImagen);
      return anchoImagen * escala;
  }
  ```

- En `CabeceraPiePdf.onEndPage`, `xInfo` pasa a `izquierda + CabeceraLayout.anchoLogoDibujado(logo.getWidth(), logo.getHeight()) + 14f`. `Image.getWidth()` y `getHeight()` dan el tamaño original de la imagen, no el escalado, así que el resultado no depende de que `dibujarLogo` se haya llamado antes.
- En `PreviaCabecera`, el primer argumento de `dibujarBloqueTexto` pasa a `izquierda + CabeceraLayout.anchoLogoDibujado((float) logo.getWidth(), (float) logo.getHeight()) * s + 14 * s`.
- No cambian ni la caja de 240 × 120, ni `anchoLogoEfectivo`, ni `altoCabeceraLogo`, ni `RESERVA_FACTURA`. El ancho disponible para los datos de empresa crece solo, porque ya se calcula a partir de `xInfo`.
- En `CabeceraLayoutTest`, añadir un test con tres casos: logo cuadrado 500 × 500 → 120; logo apaisado 1000 × 250 → 240; logo alto 100 × 400 → 30.

Alternativa descartada: centrar el logo dentro de su caja. Deja el hueco repartido a los dos lados y no lo quita.

## Segunda revisión: logo en caja fija

> Añadida el 14/09/2026. El usuario revisa D10 aplicada y decide que el espacio del logo debe ser siempre el mismo.

### D11. Decisión: caja fija, logo arriba a la izquierda y datos siempre en el mismo sitio

**Qué pasa hoy:**

- Tras D10, los datos de empresa se mueven según el ancho del logo. Con un logo cuadrado se acercan; con uno apaisado no. La cabecera cambia de aspecto de una empresa a otra.
- `CabeceraPiePdf.dibujarLogo` apoya el logo en el **borde inferior** de su caja. Un logo apaisado, que queda más bajo que la caja, empieza muy por debajo del nombre de la empresa (se ve en el logo de la asesoría: 472 × 194 px, dibujado a 240 × 99 pt, con 21 pt vacíos encima).
- La vista previa (`PreviaCabecera`) ya lo coloca **arriba** (`img.setY(HUECO_LOGO_SUPERIOR * s)`), así que hoy la vista previa y el PDF no coinciden.

**Solución:**

1. **Datos de empresa en posición fija.** Deshacer el uso de D10:
   - `CabeceraPiePdf.onEndPage`: `xInfo` vuelve a `izquierda + CabeceraLayout.ANCHO_LOGO_FIJO + 14f`.
   - `PreviaCabecera`: el primer argumento de `dibujarBloqueTexto` vuelve a `izquierda + CabeceraLayout.ANCHO_LOGO_FIJO * s + 14 * s`.
   - `CabeceraLayout.anchoLogoDibujado` y su test **se quedan**: los usa D12.
2. **Logo pegado arriba en su caja.** En `CabeceraPiePdf.dibujarLogo`, tras `scaleToFit`, la coordenada vertical pasa a ser el borde inferior de la caja **más** lo que sobra de alto:

   ```java
   float bordeInferiorCaja = bordeSuperiorContenido + CabeceraLayout.HUECO_LOGO_INFERIOR + altoTarjetas + EstiloPdf.HUECO_TARJETAS;
   float sobraAlto = CabeceraLayout.ALTO_LOGO_FIJO - logo.getScaledHeight();
   logo.setAbsolutePosition(izquierda, bordeInferiorCaja + sobraAlto);
   ```

   En PDF la `y` crece hacia arriba: sumar lo que sobra sube el logo hasta que su borde superior toca el de la caja. Un logo que ya ocupa los 120 pt de alto (cuadrado o vertical) no cambia de sitio. La coordenada horizontal sigue siendo `izquierda`: pegado a la izquierda.
3. `PreviaCabecera` no cambia en la colocación del logo: ya lo pone arriba a la izquierda.

El borde superior de la caja queda a 26 pt del borde de la página (`HUECO_LOGO_SUPERIOR`), y la línea base del nombre de la empresa a 34 pt, así que la parte de arriba del logo y la del nombre quedan a la misma altura.

**Alternativas descartadas:**

- **Recortar el logo para llenar la caja.** Un logo cuadrado en una caja el doble de ancha perdería la mitad de su dibujo, y dejar recortar al usuario exige una herramienta de recorte en la aplicación.
- **Estirarlo para llenar la caja.** Lo deforma.
- **Límite máximo de tamaño al cargar.** No hace falta: `scaleToFit` ya reduce las imágenes grandes y amplía las pequeñas sin deformarlas.

### D12. Decisión: aviso, no bloqueo, cuando el logo tiene poca resolución

Ampliar una imagen pequeña para llenar la caja la vuelve borrosa al imprimir. En lugar de impedir cargarla, se avisa.

**Regla:** la imagen tiene poca resolución si su ancho en píxeles es menor que 1,5 veces el ancho con el que se dibuja en el PDF (en puntos). Basta con mirar el ancho: el alto se escala en la misma proporción. Con los logos de `logos/`:

| Imagen | Se dibuja a | Píxeles por punto | ¿Aviso? |
|---|---|---|---|
| 128 × 128 px | 120 pt de ancho | 1,07 | Sí |
| 256 × 256 px | 120 pt | 2,13 | No |
| 472 × 194 px (asesoría) | 240 pt | 1,97 | No |
| 1254 × 1254 px (torre) | 120 pt | 10,45 | No |

**Dónde:**

- En `CabeceraLayout`, junto a `anchoLogoDibujado`:

  ```java
  public static boolean logoConPocaResolucion(double anchoPx, double altoPx) {
      if (anchoPx <= 0 || altoPx <= 0) {
          return false;
      }
      return anchoPx < anchoLogoDibujado((float) anchoPx, (float) altoPx) * 1.5;
  }
  ```

  Si la imagen no se pudo leer (ancho 0), no se avisa: ya fallará al exportar como hoy, sin logo.
- En `ConfiguracionController.seleccionarLogo`, después de `txtLogoPath.setText(f.getAbsolutePath())`: cargar la imagen con `new javafx.scene.image.Image(f.toURI().toString())` y, si `CabeceraLayout.logoConPocaResolucion(imagen.getWidth(), imagen.getHeight())`, llamar a `Dialogos.info("Logo", "La imagen es pequeña (" + ancho + " × " + alto + " píxeles) y puede verse borrosa al imprimir la factura. Para un buen resultado, usa una imagen de al menos " + minimo + " píxeles de ancho.")`, con los píxeles como enteros y `minimo` = `Math.round(anchoLogoDibujado(...) * 1.5)`.
- El aviso **no** vacía el campo ni cancela nada.
- En `CabeceraLayoutTest`, un test con los cuatro casos de la tabla.

Alternativa descartada: rechazar la imagen. Hay empresas que solo tienen su logo en pequeño; es mejor que lo sepan y decidan.

### D13. Aclaración: el fondo negro del logo de la torre no es un defecto

Se sospechó que el PDF pintaba de negro las esquinas transparentes. Medido: `logos/ChatGPT Image 2 sept 2026, 19_24_42.png` es RGB **sin canal de transparencia** y sus esquinas son negras `(0, 0, 0)` en la propia imagen. Los logos con transparencia (`logo1.png`, `image-*.png`) son RGBA. No se toca nada.

## Risks / Trade-offs

- **Facturas al límite de una página** pueden pasar a dos. `numeroPaginasPorCasos` vigila el caso de 20 líneas; facturas con muchas líneas de descripción larga pueden ganar página, y se acepta.
- **Nombres de empresa largos** se reducen hasta 10 pt en lugar de 9, así que un nombre muy largo puede tocar antes el límite. `nombreEmpresaLargoNoSolapaFactura` vigila que no invada el bloque FACTURA.
- **Renombrados pendientes.** Este change usa los nombres actuales. Los tres changes de renombrado solo cambian nombres de clases y métodos, así que se aplican igual después.
