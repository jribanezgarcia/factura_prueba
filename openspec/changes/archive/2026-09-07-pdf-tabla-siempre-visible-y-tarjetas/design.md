## Context

`exportar(...)` calcula `hayOperaciones` (`PdfService.java:123`) y con esa bandera decide si construir la tabla de líneas (`PdfService.java:146`) y si añadirla (`PdfService.java:185`). Cuando es falsa —factura de solo suplidos— no hay tabla ni marco, y el anclaje se resuelve con un espaciador desnudo.

La rama de «el cierre no cabe» (`PdfService.java:192-198`) completa la página en curso con el marco, hace `newPage()` y ancla el cierre al pie de la página nueva con un espaciador, sin tabla.

Las dos tarjetas se montan en una sola fila de `tarjetas(...)` (`PdfService.java:287`), así que iText las iguala en alto por construcción. `tarjetasSinPago(...)` (`PdfService.java:899`) es la variante de las páginas siguientes, y en `PdfService.java:141` se le fija a mano el alto de la tarjeta doble para que el espacio reservado arriba sea idéntico en todas las páginas.

Maqueta aprobada del apartado de tarjetas: `prototipos/pdf-cabecera-y-tarjetas.html`.

## Goals / Non-Goals

**Goals:**

- Que ninguna página quede con media hoja en blanco entre las tarjetas y el cierre.
- Que las tarjetas ocupen lo que necesitan y se lean sin pasillo.
- Que las pruebas se hagan con una empresa realista.

**Non-Goals:**

- No se toca el anclaje del cierre ni el alto de la cabecera de empresa.
- No se cambia el contenido de ninguna tabla ni ningún importe.
- No se toca el editor.

## Decisions

### D1. La tabla de líneas se imprime siempre

`hayOperaciones` deja de decidir **si hay tabla** y pasa a decidir **cuántas filas de datos tiene**. La tabla, con su cabecera de columnas, se construye y se añade siempre; con cero líneas reales sale solo la cabecera, y el marco baja desde ella hasta lo que venga debajo.

Con eso, la factura de solo suplidos queda igual que cualquier otra: tarjetas, cabecera de columnas, marco, bloque de suplidos, cierre. Y desaparece el caso especial del espaciador desnudo, que era la única rama del anclaje sin marco.

### D2. La página del cierre solitario también lleva tabla

En la rama de `PdfService.java:192-198`, después del `newPage()` se añade la tabla de líneas vacía —solo cabecera— y el marco hasta el cierre, en lugar del espaciador.

Ojo: esa tabla no debe llevar filas de datos repetidas. Es la misma tabla vacía de D1.

### D3. Las tarjetas dejan de igualarse

Las dos tarjetas dejan de ir en la misma fila de una tabla contenedora. Cada una se construye por separado y se colocan una junto a otra dejando que cada cual mida lo suyo: en iText, dos celdas de una fila con `setVerticalAlignment(Element.ALIGN_TOP)` y sin alto fijo, con la tabla anidada dentro de cada celda.

La columna de etiquetas pasa de un ancho relativo a ajustarse al contenido: se mide la etiqueta más larga de esa tarjeta con la fuente y el cuerpo reales, y ese es el ancho de la primera columna, más un pequeño margen. Nada de porcentajes a ojo.

### D4. El invariante que rompió la primera vez

**El alto reservado a las tarjetas en el margen superior tiene que seguir siendo idéntico en todas las páginas.** De eso depende que la tarjeta repetida no invada la tabla de líneas, que fue el fallo grave de `pdf-cierre-anclado-al-pie`.

En concreto: aunque en la página 1 cada tarjeta mida lo suyo, `altoTarjetas` —lo que se suma al margen superior y lo que se le fija a `tarjetasSinPago(...)` en `PdfService.java:141`— SHALL seguir siendo el alto del conjunto de la página 1, y `yTarjetas(...)` no se toca. Su test de invariante tampoco.

Si al separar las tarjetas ese alto deja de poder medirse de una vez, **hay que parar y decirlo**, no improvisar.

### D5. Empresa de pruebas completa

`empresaTexto()` (`PdfServiceTest.java:46`) pasa a llevar dirección, código postal, población, provincia, email y teléfono de COMERCIAL ALCAZABA. `CabeceraLayout.lineasEmpresa(...)` los pinta si están.

Consecuencia inmediata: la cabecera pasa de tres a cinco líneas y `altoCabeceraTexto(lineas)` sube de 108 a 125 pt, así que el área útil se reduce y **los tests de número de páginas van a fallar**. Hay que recalcular los números esperados a mano, uno por uno, no relajar las aserciones ni convertirlas en «al menos N».

## Risks / Trade-offs

- **La tabla vacía puede parecer un error** en la factura de solo suplidos: una cabecera de columnas sin ninguna fila. Es lo que se ha pedido y lo que evita la hoja en blanco, pero conviene mirarlo en el PDF.
- **Separar las tarjetas toca el sitio exacto que rompió** el primer intento del anclaje. Por eso D4 está escrito como invariante y con test.
- **Los números de páginas cambian** por la empresa completa. Es ruido esperado, pero hay que distinguirlo de una regresión real: si alguna factura gana más de una página, parar y mirar.
