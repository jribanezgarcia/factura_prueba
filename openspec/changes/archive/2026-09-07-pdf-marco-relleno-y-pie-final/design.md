## Context

Tras `pdf-cierre-anclado-al-pie`, `exportar(...)` mide el cierre antes de añadirlo, añade la tabla de líneas, lee `writer.getVerticalPosition(false)`, calcula `hueco = y − doc.bottom() − hCierre` y lo consume con `tablaRelleno(...)` más un espaciador. El cierre acaba siempre en 118,9 pt, medido sobre los siete PDF de prueba.

Lo que cambia aquí es **cómo se pinta ese hueco** y **qué entra en el cierre**. La mecánica del anclaje no se toca: es la parte que costó dos intentos y está verificada.

El pie legal lo dibuja hoy `dibujarPieLegal(...)` (`PdfService.java:1198`) desde `onEndPage`, con `PIE_LEGAL_TAM = 6.5f` y `PIE_LEGAL_INTERLINEADO = 8f` (`PdfService.java:80-81`). `margenes(...)` usa las mismas constantes para calcular el margen inferior: `max(112, 30 + líneas × 8 + 28)` (`PdfService.java:821`).

La maqueta aprobada es `prototipos/pdf-marco-y-pie-final.html`.

## Goals / Non-Goals

**Goals:**

- Que el hueco no parezca una tabla a medio rellenar.
- Que el texto legal se imprima una vez y no robe espacio a las páginas anteriores.
- Que las páginas siguientes sepan qué es cada columna.

**Non-Goals:**

- No se toca el anclaje del cierre ni la tarjeta repetida.
- No se toca la cabecera de empresa ni el alto de las tarjetas: se decidió dejarlas como están.
- No se cambia el tamaño ni el texto del pie legal, solo dónde se imprime.

## Decisions

### D1. El marco es una tabla de una sola fila

`tablaRelleno(...)` deja de generar N filas y pasa a generar **una**, con las mismas cinco columnas `{0.7f, 4.3f, 1.4f, 1.0f, 1.9f}`, celdas vacías y `setFixedHeight(hueco)`.

Bordes por celda: `Rectangle.LEFT | Rectangle.RIGHT` en las cinco, más `Rectangle.BOTTOM` en todas para cerrar el marco por abajo. Color `c.bordeTabla`, sin fondo. Así salen los cuatro separadores verticales y los dos bordes laterales prolongados hasta el cierre, sin ningún renglón.

Con esto desaparece la aritmética de `filas × altoFila` y el resto sobrante: el hueco se consume exacto con `setFixedHeight`. `filasDeRelleno(...)` y `altoFilaLinea(...)` ya no participan en el montaje; `filasDeRelleno` se conserva solo si algún test lo sigue usando, y si no, se retira con sus tests.

Alternativa descartada: dibujar las líneas a mano en el evento de página con `PdfContentByte`. Habría que saber dónde acaba la tabla desde el evento, que es información que solo tiene el flujo.

### D2. El pie legal entra en el cierre

`dibujarPieLegal(...)` deja de llamarse desde `onEndPage` (`PdfService.java:1056`). En su lugar se construye una `PdfPTable` de una columna con el mismo aspecto que la caja actual —fondo `c.clarisimo`, contorno redondeado `c.bordeTabla`, filete izquierdo del color de acento, texto a `PIE_LEGAL_TAM` con interlineado `PIE_LEGAL_INTERLINEADO`— y se añade al flujo detrás de las observaciones.

Su alto se mide como el de las demás piezas y se suma a `hCierre`, con una constante `ESP_PIE` de separación. Como el cierre ya se ancla al pie de la última página, el legal sale ahí y solo ahí **por construcción**: no hay que preguntar «¿es esta la última página?», que es lo que obligaría a rehacer los márgenes página a página.

`margenes(...)` deja de contar las líneas del pie: el margen inferior pasa a ser el mínimo necesario para el `Página X de Y`, que se sigue dibujando en el evento en todas las páginas. Ese mínimo se fija en una constante con nombre.

Ese es el punto que devuelve espacio: las páginas anteriores recuperan los cuatro centímetros del texto legal.

### D3. Cabecera de columnas repetida

`setHeaderRows(1)` en la tabla de `tablaLineas(...)`. Cuesta una fila de alto por página a partir de la segunda.

Ojo con el marco: es una tabla aparte y **no** debe llevar cabecera. Si el marco cayera partido entre dos páginas no debe repetir nada.

### D4. Qué pasa si el cierre no cabe

Con el pie dentro, el cierre es bastante más alto y el caso de «no cabe» deja de ser teórico. La rama que ya existe sirve: se completa la página en curso con el marco hasta el borde inferior, `doc.newPage()`, y el cierre entero —totales, observaciones y pie legal— se ancla al pie de la página nueva con un espaciador, sin marco.

El recuadro del pie legal **no debe partirse** entre dos páginas: la tabla del pie va con `setSplitLate(true)` y sin permitir división de filas.

## Risks / Trade-offs

- **El cierre crece mucho.** Con el aviso de protección de datos real son unos 4 cm más de bloque. En una factura de muchas líneas eso puede empujar el cierre a una página nueva que antes no existía. Es aceptable, pero hay que verlo en el caso de prueba de dos páginas.
- **El modo logo no está cubierto por ningún test.** El margen superior en modo logo es 170 pt frente a 108 en texto, más el alto de la tarjeta repetida. Nadie lo ha comprobado nunca de forma automática; este change añade ese caso.
- **La marca `ANULADA` y la tarjeta se dibujan las dos en el evento de página.** No hay ningún test que compruebe que no se pisan en una factura de varias páginas.
- **El `Página X de Y` se queda solo en el pie.** Al retirar el recuadro legal de las páginas intermedias, el pie de esas páginas queda muy vacío. Es lo buscado, pero conviene mirarlo en la maqueta antes de darlo por bueno.
