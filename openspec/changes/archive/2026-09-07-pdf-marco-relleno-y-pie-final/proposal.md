## Why

El change anterior ancló el cierre al pie y rellenó el hueco con filas vacías de la tabla de líneas. Funciona, pero al verlo impreso el resultado no convence: en una factura de una o dos líneas quedan más de veinte renglones vacíos seguidos, y la reja pesa tanto que se lee como si faltaran datos.

Además hay dos cosas que se ven mal en cuanto la factura pasa de una página:

El **pie legal se dibuja en todas las páginas** desde el evento de página (`PdfService.java:1056`). Con el aviso de protección de datos real de la empresa —1.527 caracteres, nueve líneas a 6,5 pt— eso son unos cuatro centímetros por página. En una factura de dos páginas se imprime dos veces el mismo texto y se pierden unas diez filas de factura.

Y la **tabla de líneas no repite su cabecera**: `tablaLineas(...)` (`PdfService.java:508`) nunca ha llamado a `setHeaderRows(1)`, así que las páginas segunda y siguientes arrancan con datos sin que se sepa qué es cada columna. Ahora que la tabla llega hasta el pie en todas las páginas, se nota mucho más.

## What Changes

- El hueco entre la última línea y el cierre SHALL dibujarse como **marco de columnas**: bordes laterales y divisiones verticales prolongados hasta el cierre y cerrados por abajo, **sin renglones ni rayado alterno**.
- El **pie legal** SHALL salir del evento de página y pasar a formar parte del cierre, detrás de las observaciones. Aparece una sola vez, en la última página. Las páginas anteriores dejan de reservarle espacio.
- La tabla de líneas SHALL repetir su fila de cabecera en cada página que la continúe.
- No cambia ningún importe, ni el contenido de ninguna tabla, ni el anclaje del cierre, que sigue acabando al pie de la última página.

## Capabilities

### New Capabilities

Ninguna.

### Modified Capabilities

- `invoicing`: se modifica «Exportación a PDF». Su apartado de cierre describe hoy el relleno con filas vacías, y el de observaciones exige que el pie legal se repita en todas las páginas.

## Impact

- `src/main/java/com/alcazaba/facturacion/pdf/PdfService.java`:
  - `tablaRelleno(...)` se sustituye por un marco de una sola fila; `altoFilaLinea(...)` y `filasDeRelleno(...)` dejan de hacer falta para el relleno, aunque `filasDeRelleno` sigue teniendo sus tests.
  - `dibujarPieLegal(...)` deja de llamarse desde `onEndPage` (`PdfService.java:1056`) y se convierte en una tabla que se añade al flujo; su alto entra en `hCierre`.
  - `margenes(...)` (`PdfService.java:808-821`) deja de sumar el pie al margen inferior, que baja a lo justo para `Página X de Y`.
  - `tablaLineas(...)` (`PdfService.java:508`): `setHeaderRows(1)`.
- `src/test/java/com/alcazaba/facturacion/pdf/PdfServiceTest.java`: tests nuevos del marco, del pie único, de la cabecera repetida, del **modo logo** y de la **anulada de varias páginas**, que hoy no están cubiertos por ningún test.
- `prototipos/pdf-marco-y-pie-final.html` (nuevo): maqueta aprobada.
- No se tocan `CalculoService`, `ResumenFactura`, el editor ni ningún fichero de tema. **La cabecera y las tarjetas no se tocan en este change.**
