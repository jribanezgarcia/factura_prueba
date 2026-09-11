> Todos los cambios están en `src/main/java/com/alcazaba/facturacion/pdf/OpenPdfRenderer.java`. Las razones, en `design.md`.

## 1. Tabla de líneas

- [ ] 1.1 En `tablaLineasVacia` (línea 319), cambiar `new float[]{0.7f, 4.3f, 1.4f, 1.0f, 1.9f}` por `new float[]{0.7f, 5.55f, 1.0f, 0.75f, 1.3f}`.
- [ ] 1.2 En `tablaRelleno` (línea 596), el mismo cambio. Los dos arrays deben quedar idénticos. Ver `design.md - D1`.
- [ ] 1.3 En `tablaLineas`, cambiar a `Element.ALIGN_CENTER` la alineación de `l.price()` (línea 335) y de `l.total()` (línea 337). No tocar la de cantidad, descripción ni IVA. Ver `design.md - D3`.

## 2. Columnas de importe

- [ ] 2.1 En `bloqueSuplidos` (línea 345), cambiar `new float[]{6.4f, 1.9f}` por `new float[]{8.0f, 1.3f}`.
- [ ] 2.2 En `rejillaLiquidacion` (línea 463), cambiar `new float[]{2.2f, 1.4f}` por `new float[]{2.54f, 1.06f}`.
- [ ] 2.3 No tocar ninguna alineación de suplidos, liquidación ni desglose de IVA, ni ningún otro array de anchos. Ver `design.md - D4`.

## 3. Verificación final

- [ ] 3.1 `mvn test` en verde, con `numeroPaginasPorCasos` incluido. Si este falla, no ajustar el test: los anchos son los que están mal.
- [ ] 3.2 Exportar una factura normal y comprobar que la descripción es visiblemente más ancha y que Precio y Total salen centrados.
- [ ] 3.3 Exportar una factura con importes de cuatro cifras (del tipo `3.128,10`) y comprobar que ningún importe se parte en dos renglones.
- [ ] 3.4 Exportar una factura con suplidos y comprobar que la división vertical de la columna de importe del bloque de suplidos cae justo debajo de la de Total.
- [ ] 3.5 Comprobar que la columna de importe de la liquidación queda alineada, a ojo, con la de Total.
- [ ] 3.6 Exportar una factura de varias páginas y comprobar que el marco de columnas hasta el cierre casa con la tabla en todas las páginas.
