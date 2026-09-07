> Segundo intento. El primero se descartó entero: la tarjeta se dibujaba encima de la tabla y
> tapaba líneas de la factura, el cierre no se anclaba cuando no había tabla de líneas, y salían
> páginas de más. Los fallos y sus causas están en `design.md - Postmortem`.
>
> Geometría y decisiones: `design.md`. Maquetas aprobadas:
> `prototipos/pdf-cierre-anclado-al-pie.html` y `prototipos/pdf-multipagina-cliente-repetido.html`
> (variante **B**).
>
> **Regla de oro de este change: ningún elemento puede solaparse con otro, y ninguna factura puede
> ganar páginas respecto a su versión anterior salvo por la tarjeta repetida.**

## 0. Punto de partida limpio

- [x] 0.1 `git checkout -- src/main/java/com/alcazaba/facturacion/pdf/PdfService.java src/test/java/com/alcazaba/facturacion/pdf/PdfServiceTest.java` para volver al commit `1c109e9`. No se parte del intento anterior: se rehace.
- [x] 0.2 Confirmar con `git status` que solo queda sin seguir la carpeta del change.

## 1. Pie legal a 6,5 pt

- [x] 1.1 Añadir `private static final float PIE_LEGAL_TAM = 6.5f;` y sustituir con ella los **dos** literales `7.5f`: el de `dibujarPieLegal(...)` y el de `margenes(...)`.
- [x] 1.2 El interlineado del pie (`9f`, también duplicado en los dos sitios) se extrae igual a `PIE_LEGAL_INTERLINEADO` y se ajusta a `PIE_LEGAL_TAM + 1.5f`.
- [x] 1.3 **Criterio de aceptación:** exportar con el aviso RGPD completo (más de 8 líneas) y comprobar en el PDF que ninguna línea del pie cae fuera del recuadro claro.

## 2. La tarjeta del cliente en la cabecera repetida

Aquí es donde falló el intento anterior. `writeSelectedRows(0, -1, x, y, cb)` **dibuja hacia abajo
desde `y`**: si se le pasa el borde superior del área de texto, la tarjeta invade el área de texto y
tapa las primeras filas de la tabla. Hay que dibujarla *por encima* de ese borde.

- [x] 2.1 Sacar `tarjetas(...)` del flujo: quitar el `doc.add(tarjetas(vc, colores))` de `exportar(...)`.
- [x] 2.2 Construir y medir la tabla una sola vez en `exportar(...)` (`setTotalWidth(anchoUtil)`, `setLockedWidth(true)`, `calculateHeights(true)`) y pasársela al evento de página junto con su alto.
- [x] 2.3 Añadir `HUECO_TARJETAS = 8f` y calcular el margen superior como `superior + altoTarjetas + HUECO_TARJETAS`.
- [x] 2.4 Extraer la coordenada a un método puro y testeable:
      `static float yTarjetas(float bordeSuperiorContenido, float altoTarjetas)`
      que devuelva `bordeSuperiorContenido + HUECO_TARJETAS + altoTarjetas`.
      Dibujar con `tabla.writeSelectedRows(0, -1, izquierda, yTarjetas(...), cb)`.
- [x] 2.5 Subir el separador por encima de la tarjeta: hoy se dibuja en `bordeSuperiorContenido + 4`; pasa a `yTarjetas(...) + 4`.
- [x] 2.6 En la página 1 se dibujan las dos tarjetas; a partir de la 2, solo «FACTURAR A», con **el mismo ancho de columna y el mismo alto total** que en la primera —la mitad derecha queda en blanco, no se ensancha—. La condición es el contador `paginasReales` que el evento ya lleva.
- [x] 2.7 **Invariante, con test unitario:** `yTarjetas(b, h) - h >= b`, o sea el borde inferior de la tarjeta nunca entra en el área de texto. Probarlo con varios altos, incluido `h = 0`.
- [x] 2.8 **Criterio de aceptación visual:** en cualquier factura exportada, entre el separador y la primera fila de la tabla de líneas no hay solape ni una franja vacía mayor que `HUECO_TARJETAS`.

## 3. Anclaje del cierre

El anclaje **no es** el relleno de filas. Anclar es empujar el cierre hasta abajo con un espaciador
del alto exacto del hueco; las filas vacías son solo la forma de pintar ese espaciador cuando hay
tabla de líneas. Separar las dos cosas evita el fallo del intento anterior, en el que las facturas
sin tabla de líneas no anclaban nada.

- [x] 3.1 Medir el cierre antes de añadir nada: `hCierre = (suplidos ? ESP_SUPLIDOS + altoSuplidos + ESP_SUPLIDOS : 0) + altoTotales + (observaciones ? ESP_OBS + altoObs : 0)`, con `ESP_SUPLIDOS = 4f` y `ESP_OBS = 6f` como constantes con nombre. Nada de números sueltos inventados sobre la marcha, y **ningún «margen de seguridad»**: si hace falta holgura, va dentro de una constante con nombre y justificada.
- [x] 3.2 Añadir la tabla de líneas solo si hay operaciones, como hoy.
- [x] 3.3 Leer la posición del cursor **sin** provocar saltos: usar `writer.getVerticalPosition(false)`. Con `true` iText puede forzar un salto de página, y eso generó páginas fantasma en el intento anterior.
- [x] 3.4 `hueco = y − doc.bottom() − hCierre`.
- [x] 3.5 Si `hueco > 0`, **anclar siempre**, haya o no tabla de líneas:
      - con tabla de líneas: `filas = (int) Math.floor(hueco / altoFila)` filas vacías, y un espaciador con el resto `hueco − filas × altoFila` (aunque el resto sea menor que una fila; si no, el cierre no baja del todo);
      - sin tabla de líneas: un único espaciador de `hueco`.
- [x] 3.6 Si `hueco <= 0`: rellenar lo que quede de la página actual con filas vacías hasta el borde inferior, `doc.newPage()`, y añadir el cierre anclado al pie de la página nueva con un espaciador —**sin filas vacías**, porque la tabla de líneas ya ha terminado—. Una página con cuarenta renglones vacíos y nada más es justo lo que hay que evitar.
- [x] 3.7 Después: bloque de suplidos si lo hay, bloque de totales, y observaciones si las hay.

## 4. Alto de las filas de relleno

El intento anterior midió la fila con celdas vacías (`celdaLinea("", ...)`) y le salió la mitad de
alto que a las filas con texto, así que el relleno tenía un rayado más apretado que la tabla.

- [x] 4.1 Medir el alto de fila con una fila **de contenido representativo** —por ejemplo `"1"`, `"X"`, un importe, `"21 %"`, un importe— no con celdas vacías.
- [x] 4.2 Construir las filas de relleno con `celdaLinea(" ", fila, ...)` y fijarles ese alto con `setFixedHeight(altoFila)` en las cinco celdas, de modo que coincidan exactamente con las reales.
- [x] 4.3 Continuar la alternancia de color: la primera fila vacía lleva el índice `nº de líneas reales`, no `0`.
- [x] 4.4 **Invariante, con test:** el alto de una fila de relleno es igual al de una fila real con contenido, con tolerancia de 0,5 pt.

## 5. Tests que habrían detectado los fallos

Los tests del intento anterior pasaron los 177 con el PDF roto. Estos son los que faltaban:

- [x] 5.1 `yTarjetas(...)`: el invariante de 2.7.
- [x] 5.2 Alto de fila: el invariante de 4.4.
- [x] 5.3 `filasDeRelleno(...)`: hueco amplio, hueco justo, hueco cero, hueco negativo y resto no exacto.
- [x] 5.4 **Número de páginas**, un test por caso y con el número esperado escrito a mano:
      2 líneas → 1; 10 líneas → 1; 20 líneas → 1; 1 línea + 1 suplido → 1; solo suplidos → 1.
      Este es el test que habría cazado que una factura de una página pasara a dos.
- [x] 5.5 **El cierre está en la última página**: extraer el texto página a página y comprobar que `TOTAL` y `LIQUIDACIÓN` aparecen en la última y **en ninguna otra**.
- [x] 5.6 **La tarjeta aparece una vez por página**: en una factura de dos páginas, `FACTURAR A` sale en las dos y `DATOS DE PAGO` solo en la primera.
- [x] 5.7 **Ninguna página en blanco**: ninguna página del PDF puede quedar sin texto extraíble aparte de cabecera y pie.
- [x] 5.8 Los tests de importes siguen en verde sin tocarlos.

## 6. Verificación final

- [x] 6.1 `mvn test` en verde.
- [x] 6.2 `openspec validate pdf-cierre-anclado-al-pie --strict`.
- [x] 6.3 Exportar a `C:\Users\juan\Desktop\revision-totales-pdf` los siete casos: 2 líneas, 10, 20, con suplidos, solo suplidos, dos páginas y pie RGPD completo.
- [x] 6.4 **Repasar uno a uno los siete PDF** y comprobar, en cada uno: que nada se solapa, que el cierre está al pie de la última página, que no hay páginas de más ni páginas vacías, y que las filas de relleno tienen el mismo alto y el mismo rayado que las reales.
- [x] 6.5 Comparar con las dos maquetas antes de dar nada por terminado. Si un PDF no se parece a su maqueta, la tarea no está hecha.
