> Todos los cambios están en `src/main/java/com/alcazaba/facturacion/pdf/OpenPdfRenderer.java`. Las razones, en `design.md`.

## 1. Tabla de líneas

- [x] 1.1 En `tablaLineasVacia` (línea 319), cambiar `new float[]{0.7f, 4.3f, 1.4f, 1.0f, 1.9f}` por `new float[]{0.7f, 5.55f, 1.0f, 0.75f, 1.3f}`.
- [x] 1.2 En `tablaRelleno` (línea 596), el mismo cambio. Los dos arrays deben quedar idénticos. Ver `design.md - D1`.
- [x] 1.4 Corrección tras revisar el PDF: en `tablaLineasVacia` **y** en `tablaRelleno`, cambiar `new float[]{0.7f, 5.55f, 1.0f, 0.75f, 1.3f}` por `new float[]{0.7f, 5.3f, 1.25f, 0.75f, 1.3f}`. Solo cambian Descripción y Precio; Total se queda en `1.3f`. Ver `design.md - D1`.
- [x] 1.5 Segunda corrección, propuesta 6 elegida: en `tablaLineasVacia` **y** en `tablaRelleno`, cambiar `new float[]{0.7f, 5.3f, 1.25f, 0.75f, 1.3f}` por `new float[]{0.7f, 5.15f, 1.25f, 0.75f, 1.45f}`. Ver `design.md - D1`.
- [x] 1.3 En `tablaLineas`, cambiar a `Element.ALIGN_CENTER` la alineación de `l.price()` (línea 335) y de `l.total()` (línea 337). No tocar la de cantidad, descripción ni IVA. Ver `design.md - D3`.

## 2. Columnas de importe

- [x] 2.1 En `bloqueSuplidos` (línea 345), cambiar `new float[]{6.4f, 1.9f}` por `new float[]{8.0f, 1.3f}`.
- [x] 2.2 En `rejillaLiquidacion` (línea 463), cambiar `new float[]{2.2f, 1.4f}` por `new float[]{2.54f, 1.06f}`.
- [x] 2.4 En `bloqueSuplidos`, cambiar `new float[]{8.0f, 1.3f}` por `new float[]{7.85f, 1.45f}`.
- [x] 2.5 En `rejillaLiquidacion`, cambiar `new float[]{2.54f, 1.06f}` por `new float[]{2.0f, 1.45f}`. Ver `design.md - D4`.
- [x] 2.6 En `bloqueTotales`: contenedor `new float[]{3.3f, 3.0f}` → `new float[]{5.85f, 3.45f}`; en `celdaIzquierda`, `setPaddingRight(7f)` → `setPaddingRight(4f)` y añadir `setPaddingLeft(0f)`; en `celdaDerecha`, `setPaddingLeft(7f)` → `setPaddingLeft(0f)` y añadir `setPaddingRight(0f)`. No tocar los rellenos superior e inferior. Ver `design.md - D5`.
- [x] 2.7 En `rejillaDesgloseIva`, cambiar `new float[]{1.0f, 2.0f, 1.7f}` por `new float[]{38.78f, 152.08f, 129.26f}`. No tocar la banda `TOTAL` ni ninguna alineación.
- [x] 2.3 No tocar ninguna alineación de suplidos, liquidación ni desglose de IVA, ni ningún otro array de anchos. Ver `design.md - D4`.

## 3. Verificación final

- [x] 3.1 `mvn test` en verde, con `numeroPaginasPorCasos` incluido. Si este falla, no ajustar el test: los anchos son los que están mal.
- [x] 3.2 Exportar una factura normal y comprobar que la descripción es visiblemente más ancha y que Precio y Total salen centrados.
- [x] 3.3 Exportar una factura con importes de cuatro cifras (del tipo `3.128,10`) y comprobar que ningún importe se parte en dos renglones.
- [x] 3.4 Exportar una factura con suplidos y comprobar que la división vertical de la columna de importe del bloque de suplidos cae justo debajo de la de Total.
- [x] 3.5 Comprobar que la columna de importe de la liquidación queda alineada, a ojo, con la de Total.
- [x] 3.8 Exportar una factura con retención y suplidos cuyo total pase del millón y comprobar que la banda `TOTAL` muestra cifra y `€` en un único renglón.
- [x] 3.9 Comprobar en esa factura que el borde izquierdo de la liquidación cae sobre la línea de Precio, su división de importes sobre la de Total, la división Tipo | Base del desglose sobre la de Cant., y que las dos cajas llegan a los bordes de la tabla con solo un blanco mínimo entre ellas.
- [x] 3.7 Exportar una factura con un precio unitario de `100.000,00` y otra de `1.000.000,00` y comprobar que el precio sale en un renglón, centrado y con aire a los lados.
- [x] 3.6 Exportar una factura de varias páginas y comprobar que el marco de columnas hasta el cierre casa con la tabla en todas las páginas.
