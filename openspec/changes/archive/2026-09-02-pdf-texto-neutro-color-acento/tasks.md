## 1. Paleta neutra en el PDF

- [x] 1.1 Sustituir en `PdfService` las constantes de tinta marrón-arena (`TINTA`, `GRIS`, `GRIS_CLARO`, `VALOR_SUAVE`) por colores neutros (negro para valores, gris neutro para etiquetas/info y pie) y verificar que la clase compila (`mvn compile`).
- [x] 1.2 Revisar todos los usos de esas constantes (datos de empresa en cabecera, tarjeta FACTURAR A, tabla de líneas, observaciones, totales y pie «Página X de Y») y verificar que tras el cambio no queda ningún texto con tinte de color distinto del acento o del rojo.

## 2. SERIE / Nº y FECHA en color de acento

- [x] 2.1 Hacer que `dibujarRotulo` (rótulos «SERIE / Nº» y «FECHA») use el color de acento oscurecido (`c.oscuro`) y verificar visualmente (o por PDF) que el rótulo sale tintado del acento con contraste legible.
- [x] 2.2 Hacer que los valores del número y la fecha bajo esos rótulos usen el color de acento oscurecido (`c.oscuro`) en lugar de la tinta marrón, y verificar que queda legible sobre blanco.

## 3. Verificación

- [x] 3.1 Ejecutar la suite completa (`mvn test`) y confirmar que los tests existentes siguen en verde (138).
- [x] 3.2 Verificar visualmente un PDF exportado: texto por defecto en negro/gris neutro y SERIE/Nº–FECHA en color de acento, sin añadir entrada manual del color (se mantiene solo el `ColorPicker`).

## 4. Cierre

- [x] 4.1 /opsx-sync-specs (este change SI lleva delta) y actualizar CONTINUAR_MAÑANA.md.
- [x] 4.2 /opsx-archive y commit/push.
