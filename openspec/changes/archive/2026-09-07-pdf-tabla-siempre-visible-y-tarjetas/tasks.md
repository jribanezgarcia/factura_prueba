> Decisiones y sus porqués: `design.md`. Maqueta aprobada de las tarjetas:
> `prototipos/pdf-cabecera-y-tarjetas.html`, apartado 2.
>
> **No se toca el anclaje del cierre ni el alto de la cabecera de empresa.** El cierre debe seguir
> acabando donde acaba hoy: `TOTAL.bottom = 123,4 pt` y el pie legal en `94,8 pt`, medidos sobre
> los nueve PDF de la revisión anterior.
>
> **El invariante de `design.md - D4` no se negocia:** el alto reservado a las tarjetas en el
> margen superior tiene que ser idéntico en todas las páginas. Es lo que rompió el primer intento
> del anclaje. Si al separar las tarjetas ese alto deja de poder medirse, **para y dilo**.

## 1. La empresa de pruebas, completa

- [x] 1.1 En `empresaTexto()` (`PdfServiceTest.java:46`) añadir dirección, código postal, población, provincia, email y teléfono de COMERCIAL ALCAZABA, tomando los datos reales del pie legal de `capturas_pantalla/pie_factura.txt`: Avda. Alhambra nº 18, 04007, Almería, Almería, cial.alcazaba@hotmail.com.
- [x] 1.2 Pasar la suite y **anotar qué tests de número de páginas fallan**. Es esperado: la cabecera pasa de tres a cinco líneas y `altoCabeceraTexto` sube de 108 a 125 pt.
- [x] 1.3 Recalcular a mano los números esperados de esos tests. **No** los relajes a `assertTrue(n >= …)` ni los borres.
- [x] 1.4 Si alguna factura gana **más de una** página, para y dilo antes de seguir: eso ya no sería el efecto de la cabecera.

## 2. La tabla de líneas se imprime siempre

- [x] 2.1 En `exportar(...)`, dejar de condicionar la construcción de la tabla a `hayOperaciones` (`PdfService.java:146`): se construye siempre.
- [x] 2.2 Quitar el `if (hayOperaciones)` que envuelve el `doc.add(...)` de la tabla (`PdfService.java:185`). Con cero líneas reales la tabla sale con su cabecera de columnas y sin filas de datos.
- [x] 2.3 Comprobar que el marco arranca justo debajo de esa cabecera y baja hasta lo que venga después, sea el bloque de suplidos o el cierre.
- [x] 2.4 `hayOperaciones` deja de decidir si hay tabla. Si tras esto no lo usa nadie más, retirarlo; comprobarlo antes de borrar.

## 3. La página del cierre solitario

- [x] 3.1 En la rama de «el cierre no cabe» (`PdfService.java:192-198`), después del `doc.newPage()` añadir la tabla de líneas vacía —solo cabecera— y el marco hasta el cierre, en lugar del espaciador desnudo.
- [x] 3.2 Esa tabla **no** repite filas de datos: es la misma tabla vacía del punto 2.
- [x] 3.3 Comprobar que el cierre sigue acabando exactamente en `123,4 pt` en esa página.

## 4. Las tarjetas

- [x] 4.1 Separar las dos tarjetas para que cada una mida lo suyo: dejan de ir en la misma fila de `tarjetas(...)` (`PdfService.java:287`); se construyen por separado y se colocan en dos celdas con `setVerticalAlignment(Element.ALIGN_TOP)` y sin alto fijo, con la tabla anidada dentro de cada celda.
- [x] 4.2 La columna de etiquetas de cada tarjeta pasa a ajustarse al contenido: **mide** la etiqueta más larga de esa tarjeta con la fuente y el cuerpo reales y usa ese ancho más un pequeño margen con nombre. Nada de porcentajes a ojo.
- [x] 4.3 `altoTarjetas` —lo que se suma al margen superior y lo que se fija a `tarjetasSinPago(...)` en `PdfService.java:141`— SHALL seguir siendo el alto del conjunto de la página 1. `yTarjetas(...)` y su test de invariante **no se tocan**.
- [x] 4.4 En las páginas 2 y siguientes la tarjeta de cliente sigue reservando ese mismo alto, aunque ella sola mida menos.

## 5. Tests

- [x] 5.1 Invertir y renombrar `soloSuplidosOmiteLaTablaDeLineas` (`PdfServiceTest.java:425`): ahora la tabla **sí** aparece. Comprueba que el PDF contiene la cabecera `DESCRIPCIÓN` y ninguna fila de datos.
- [x] 5.2 Test de la página del cierre solitario: en una factura larga, la última página contiene `DESCRIPCIÓN` y el cierre.
- [x] 5.3 Test de alturas de tarjeta: construir las dos tarjetas de una factura con siete campos de cliente y tres de pago y comprobar que sus altos **son distintos**. Es un test sobre las tablas, sin generar PDF.
- [x] 5.4 Test del invariante de D4: el alto reservado en el margen superior es el mismo valor que se le fija a la tarjeta de las páginas siguientes.
- [x] 5.5 Mantener verdes `yTarjetasNoEntraEnAreaTexto`, los de páginas, los del cierre en la última página y los de ninguna página en blanco.
- [x] 5.6 Los tests de importes siguen en verde sin tocarlos.

## 6. Verificación final

- [x] 6.1 `mvn test` en verde.
- [x] 6.2 `openspec validate pdf-tabla-siempre-visible-y-tarjetas --strict`.
- [x] 6.3 Exportar a `C:\Users\juan\Desktop\revision-totales-pdf` los mismos nueve casos de la vez anterior: 1 línea, 2, 10, 20, con suplidos, solo suplidos, dos páginas, con logo y anulada de dos páginas, todos con el pie legal real y con la empresa completa.
- [x] 6.4 Repasar los nueve: ninguna hoja con medio folio en blanco, ninguna tarjeta invadiendo la tabla, cierre al pie de la última, tabla presente en todas las páginas, y `Datos de pago` sin el hueco de abajo.
- [x] 6.5 Comparar el apartado de tarjetas con `prototipos/pdf-cabecera-y-tarjetas.html`. Si no se parece, la tarea no está hecha.
