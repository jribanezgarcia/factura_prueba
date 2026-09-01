## Context

El Editor de facturas debe pasar a 800×600 y dejar de abrirse maximizado. El desafío es que la cabecera de la factura ocupa mucho espacio vertical, por lo que la tabla de líneas debe poder reducirse y tener scroll interno, mientras que al maximizar debe crecer para aprovechar el espacio.

## Goals / Non-Goals

**Goals:**
- Editor de 800×600 sin scroll general.
- Tabla de líneas scrollable y que crece al maximizar.
- Totales siempre visibles sin cortarse.
- No abrir maximizado.

**Non-Goals:**
- No se rediseña la cabecera ni los totales.
- No se cambian otros tamaños de ventana en este change.

## Decisions

1. **`VentanaConfig.EDITOR`:** 800×600, sin maximizado.
2. **`Editor.fxml`:**
   - `prefWidth/prefHeight` a 800×600.
   - Reducir `minHeight` de la tabla de líneas de 220 a 120 px.
   - Añadir `VBox.vgrow="ALWAYS"` a la tabla para que crezca con el espacio del centro.
   - Ajustar paddings/espaciados si es necesario para encajar en 600 px de alto.
3. **Test:** actualizar `EditorTamanoMinimoTest` a 800×600 y verificar visibilidad de tabla y totales.

## Risks / Trade-offs

- En 800×600 la tabla de líneas será pequeña (~120 px), suficiente para ver un par de líneas y añadir más con scroll.
- Al maximizar, la tabla ocupará todo el espacio sobrante, manteniendo cabecera y totales visibles.

## Migration Plan

Ninguna.

## Open Questions

Ninguna.
