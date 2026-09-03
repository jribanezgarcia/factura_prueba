## Why

La pantalla «Generar facturas mensuales» rompe la estética común del resto de la aplicación: el título se muestra con el estilo por defecto de JavaFX porque su clase CSS (`titulo-dialogo`) no existe en ningún tema, las etiquetas del formulario aparecen pegadas al panel sin separación, y el panel central sale con un fondo blanco que choca con el fondo neutro del resto de ventanas. El resultado es que esta pantalla no parece parte del mismo programa.

## What Changes

- El título «Generar facturas mensuales» pasará a usar la clase estándar `.titulo` (20px, negrita, color de texto del tema), igual que el resto de pantallas (Configuración, Backups, Versiones...), en lugar de la clase inexistente `titulo-dialogo`.
- Las etiquetas del formulario (Cliente, Serie, Año, Mes inicio, Mes fin, Día del mes, Tipo de IVA, Retención IRPF) pasarán a usar la clase `.form-label` (ya definida en `base.css` y actualmente sin uso) para conseguir una separación y tono coherentes con el resto de formularios.
- El panel central se alineará con el resto de la aplicación: se sustituirá el fondo blanco del `card` por un fondo neutro del tema (p. ej. `derive(-fx-base, -3%)`) y se aumentará el espaciado del `GridPane` (`hgap`/`vgap` y margen) para que las etiquetas no queden pegadas al borde.
- El subtítulo «Líneas de cada factura» pasará de la clase inexistente `subtitulo-dialogo` a una clase existente (`section-title` o similar) coherente con el resto.

## Capabilities

### New Capabilities

- (ninguna)

### Modified Capabilities

- `invoicing`: nuevo requisito que describe la apariencia alineada de la pantalla de generación mensual (título destacado con el color del tema, etiquetas separadas y panel con fondo neutro), como comportamiento observable de la interfaz.

## Impact

- `src/main/resources/com/alcazaba/facturacion/ui/GenerarFacturasMensuales.fxml`: cambio de clases de estilo del título y subtítulo, clases de las etiquetas, espaciado del `GridPane` y clase del `ScrollPane`.
- `src/main/resources/com/alcazaba/facturacion/themes/base.css`: posible adición de una clase para el panel con fondo neutro (p. ej. `.panel-neutro`) o uso de una existente; no se modifican los temas de color por separado.
- Ningún cambio de comportamiento funcional: se mantienen los mismos campos, flujo de generación y botones.
