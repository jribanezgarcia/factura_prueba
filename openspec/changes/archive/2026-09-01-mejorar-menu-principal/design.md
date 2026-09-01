## Context

El Menú principal a 800×600 tiene dos tarjetas lado a lado. La tarjeta de empresa es estrecha (260 px) y puede cortar texto largo. La tarjeta de menú con 7 botones grandes ocupa mucho alto y deja poco margen inferior.

## Goals / Non-Goals

**Goals:**
- Que la información de la empresa no se corte horizontalmente.
- Que el contenido del Menú principal quepa en 800×600 dejando margen inferior visible.

**Non-Goals:**
- No se cambia el comportamiento funcional del menú.
- No se modifica el tamaño de ventana (sigue 800×600).

## Decisions

1. **Ampliar tarjeta de empresa:** aumentar el ancho de la tarjeta de empresa y del logo a ~300 px.
2. **Reducir altura de botones del menú:** bajar el padding vertical de `.menu-item` de 12 px a 10 px en `base.css`.
3. **Reducir espaciado interno del menú:** pasar el `spacing` de la VBox del menú de 4 a 2.
4. **Reducir altura del logo:** bajar `prefHeight` del logo de 120 a 100.
5. **Aumentar padding inferior:** subir el padding inferior del `BorderPane` raíz de 20 a 24 px.
6. **Mantener alineación arriba:** el `StackPane` central sigue alineado arriba para que el margen aparezca debajo.

## Risks / Trade-offs

- Reducir el padding de `.menu-item` afecta ligeramente la apariencia, pero sigue siendo táctil y legible.
- Si el nombre de la empresa es muy largo, puede seguir cortándose; se añadirá `wrapText="true"` a las etiquetas de la empresa.

## Migration Plan

Ninguna.

## Open Questions

Ninguna.
