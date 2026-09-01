## Why

La pantalla del Menú principal a 800×600 no aprovecha bien el espacio: la información de la empresa se corta horizontalmente porque la tarjeta es estrecha, y la lista de botones del menú queda tan larga que no deja espacio inferior visible, haciendo que la tarjeta toque el borde inferior y se pierda el fondo gris de la ventana.

## What Changes

- Ajustar el tamaño de las tarjetas del Menú principal para que la información de la empresa tenga más ancho y no se corte.
- Reducir ligeramente la altura de los elementos del menú para que quepan mejor en 800×600 y dejen margen inferior visible.
- Aumentar el padding inferior del `BorderPane` raíz para dejar espacio entre la tarjeta y el borde de la ventana.
- Actualizar la especificación visual del Menú principal.

## Capabilities

### New Capabilities

- Ninguno.

### Modified Capabilities

- `invoicing`: ajustes visuales del Menú principal.

## Impact

- `MenuPrincipal.fxml`
- `base.css` (padding de `.menu-item`)
- `CONTINUAR_MAÑANA.md`
- No cambia lógica de negocio.
