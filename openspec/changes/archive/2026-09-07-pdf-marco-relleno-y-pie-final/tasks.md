> Geometría y decisiones: `design.md`. Maqueta aprobada: `prototipos/pdf-marco-y-pie-final.html`.
>
> **No se toca el anclaje del cierre ni la tarjeta repetida.** Esa parte costó dos intentos, está
> verificada —el cierre acaba en 118,9 pt en los siete casos de prueba— y aquí solo cambia qué se
> pinta en el hueco y qué entra en el cierre.
>
> **Regla de oro, la misma:** ningún elemento puede solaparse con otro y ninguna página puede
> quedar vacía. Cada tarea de verificación lleva su invariante o su test; si no puedes escribirlo,
> di que no está hecha en vez de marcarla.

## 1. El hueco pasa a ser marco de columnas

- [x] 1.1 Reescribir `tablaRelleno(...)` para que devuelva una tabla de **una sola fila** con las mismas cinco columnas `{0.7f, 4.3f, 1.4f, 1.0f, 1.9f}` y celdas vacías.
- [x] 1.2 Cada celda con `setFixedHeight(hueco)`, `setBorder(Rectangle.LEFT | Rectangle.RIGHT | Rectangle.BOTTOM)`, color `c.bordeTabla` y **sin fondo**. Nada de rayado alterno.
- [x] 1.3 En `exportar(...)`, sustituir el cálculo de `filas × altoFila` más espaciador por una sola llamada al marco con el hueco exacto. El anclaje debe seguir dando el mismo resultado.
- [x] 1.4 El marco **no** lleva `setHeaderRows`. Si cae partido entre páginas no debe repetir nada.
- [x] 1.5 Retirar `altoFilaLinea(...)` si deja de tener llamantes; y `filasDeRelleno(...)` con sus tests si tampoco lo tiene. Comprobarlo antes de borrar, no a ojo.

## 2. El pie legal entra en el cierre

- [x] 2.1 Añadir `cajaPieLegal(String pie, Colores c)` que construya una `PdfPTable` de una columna con el mismo aspecto que la caja actual de `dibujarPieLegal(...)` (`PdfService.java:1198`): fondo `c.clarisimo`, contorno redondeado `c.bordeTabla`, filete izquierdo del color de acento, texto a `PIE_LEGAL_TAM` con interlineado `PIE_LEGAL_INTERLINEADO`.
- [x] 2.2 Quitar la llamada `dibujarPieLegal(...)` de `onEndPage` (`PdfService.java:1056`) y borrar el método si no queda nadie que lo use.
- [x] 2.3 Medir la caja como las demás piezas y sumarla a `hCierre` con una constante `ESP_PIE` de separación. Solo cuando el pie legal no esté vacío.
- [x] 2.4 Añadirla al flujo **después** de las observaciones, como último elemento del cierre.
- [x] 2.5 `setSplitLate(true)` y sin división de filas, para que el recuadro no se parta entre dos páginas.
- [x] 2.6 En `margenes(...)` (`PdfService.java:808-821`), dejar de contar las líneas del pie. El margen inferior pasa a ser el mínimo para el `Página X de Y`, en una constante con nombre. **No inventes el valor**: mide lo que ocupa hoy ese rótulo y deja holgura, y escríbelo en la constante.
- [x] 2.7 El `Página X de Y` sigue dibujándose en el evento, en **todas** las páginas.

## 3. Cabecera de columnas repetida

- [x] 3.1 `setHeaderRows(1)` en la tabla de `tablaLineas(...)` (`PdfService.java:508`).
- [x] 3.2 Comprobar que la cabecera repetida no descuadra la medición del hueco: se lee el cursor después de añadir la tabla, así que debería ser transparente, pero hay que verlo en el caso de dos páginas.

## 4. Tests

- [x] 4.1 **Marco sin renglones**: exportar una factura de 2 líneas y comprobar que entre la última línea y el bloque de totales no hay ninguna fila de tabla adicional; el marco es un único elemento.
- [x] 4.2 **El anclaje no se mueve**: el cierre sigue acabando a la misma altura que antes del change en una factura de 2, 10 y 20 líneas. Compáralo contra un valor escrito a mano en el test.
- [x] 4.3 **El pie legal aparece una sola vez**: en una factura de dos páginas, el texto legal está en la última y en ninguna otra.
- [x] 4.4 **Se gana espacio**: una factura con pie legal largo cabe en la misma o menos páginas que antes del change. Escribe el número esperado a mano.
- [x] 4.5 **Cabecera repetida**: en una factura de dos páginas, `DESCRIPCIÓN` aparece en las dos.
- [x] 4.6 **Modo logo**, que hoy no cubre ningún test: exportar con `cabeceraModo` en logo y comprobar que el documento se genera, que el número de páginas es el esperado y que el cierre está en la última.
- [x] 4.7 **Anulada de varias páginas**: la marca `ANULADA` aparece en todas las páginas y el texto de las líneas y de la tarjeta sigue siendo extraíble en todas.
- [x] 4.8 **Ninguna página en blanco** y **el cierre solo en la última**: mantener los tests que ya existen, adaptados si hace falta.
- [x] 4.9 Los tests de importes siguen en verde sin tocarlos.

## 5. Verificación final

- [x] 5.1 `mvn test` en verde.
- [x] 5.2 `openspec validate pdf-marco-relleno-y-pie-final --strict`.
- [x] 5.3 Exportar a `C:\Users\juan\Desktop\revision-totales-pdf` los casos: 1 línea, 2, 10, 20, con suplidos, solo suplidos, dos páginas, **con logo** y **anulada de dos páginas**, todos con el pie legal real de `capturas_pantalla/pie_factura.txt`.
- [x] 5.4 Repasar uno a uno esos PDF: nada solapado, cierre al pie de la última, marco sin renglones, pie legal solo al final, cabecera de columnas en todas las páginas, ninguna página vacía ni de más.
- [x] 5.5 Comparar con `prototipos/pdf-marco-y-pie-final.html`. Si un PDF no se parece a su maqueta, la tarea no está hecha.
