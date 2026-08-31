## Why

Tras aplicar el change `window-sizing`, el Menú principal se muestra cortado porque su altura predefinida/mínima (520) es inferior a la que realmente necesita el layout tras los nuevos márgenes y espaciados del sistema de diseño. Hay que corregir el tamaño mínimo/predefinido del Menú principal.

## What Changes

- Aumentar la altura mínima y predefinida del Menú principal de 520 a 600.
- Actualizar el FXML `MenuPrincipal.fxml` para reflejar el nuevo tamaño.
- Actualizar la especificación correspondiente.

## Capabilities

### New Capabilities

- Ninguno.

### Modified Capabilities

- `invoicing`: ajuste del escenario de tamaño del Menú principal.

## Impact

- `VentanaConfig.java`
- `MenuPrincipal.fxml`
- `openspec/specs/invoicing/spec.md`
- No cambia lógica de negocio.
