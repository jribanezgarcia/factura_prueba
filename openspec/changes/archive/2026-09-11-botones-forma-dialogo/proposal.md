## Why

Los botones de solo texto de la aplicación tienen esquinas muy redondeadas (8 px) y un relleno generoso, y cada uno mide lo que pide su etiqueta. Junto a los diálogos de aviso y confirmación, que usan la forma estándar de la plataforma, parecen de otra aplicación: allí los botones tienen esquinas apenas redondeadas, relleno ajustado, un ancho mínimo y, dentro de un mismo grupo, todos miden lo mismo.

## What Changes

- Los botones de solo texto SHALL adoptar la forma de los botones de los diálogos: radio de esquina de 3 px, relleno de 4 px en vertical y 8 px en horizontal, y un ancho mínimo de 80 px. Colores, sombreado, texto y reacciones al puntero no cambian.
- Los botones de solo texto que comparten fila SHALL medir todos lo mismo: el ancho del más ancho del grupo. Un botón solo en su fila conserva su ancho natural, con el mínimo de 80 px.
- Los botones con icono, la barra de navegación, el menú principal, la pantalla de arranque y los diálogos quedan fuera.
- `ConfiguracionLayoutTest` deja de contar como visibles las regiones que tienen un padre oculto: hoy da por fallo el fondo de la barra de desplazamiento, oculta, de la lista interna de un desplegable.

## Capabilities

### New Capabilities

Ninguna.

### Modified Capabilities

- `invoicing`: el requisito «Sombreado uniforme de los botones de solo texto» pasa a fijar también la forma de esos botones y el ancho común dentro de cada grupo, y deja de afirmar que la maquetación no cambia.

## Impact

- `src/main/resources/com/alcazaba/facturacion/themes/base.css`: la regla base de botones cambia radio y relleno y gana un ancho mínimo.
- `src/main/java/com/alcazaba/facturacion/ui/Botones.java`: clase nueva que iguala los anchos por grupo.
- `src/main/java/com/alcazaba/facturacion/ui/Navegador.java`: una línea en `mostrar` para llamarla.
- `src/test/java/com/alcazaba/facturacion/ui/ConfiguracionLayoutTest.java`: el filtro de regiones visibles.
- `src/test/java/com/alcazaba/facturacion/ui/BotonesTest.java`: test nuevo.
- Ningún FXML ni ningún tema.
