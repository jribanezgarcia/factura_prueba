## Why

Tras aplicar el rediseño visual del change `redesign-ui-apple`, se observan defectos de espaciado en varias pantallas: los paneles de contenido quedan demasiado pegados al borde izquierdo de la ventana y las filas de filtros del histórico casi se solapan con la barra de menú. Es necesario corregir los márgenes para que el diseño respire y no se perciban elementos amontonados.

## What Changes

- Aumentar el margen horizontal y vertical entre el borde de la ventana y los paneles de contenido en `Historico`, `Configuracion`, `Clientes`, `Versiones`, `Backup`, `GenerarFacturasMensuales`, `Editor` y `MenuPrincipal`.
- Aumentar la separación entre la barra de menú superior y la primera fila de controles.
- Corregir el espaciado interno de las filas de filtros para que los campos no toquen el borde izquierdo de su tarjeta.
- Ajustar `base.css` para que `.zona-contenido` y `.card` aporten márgenes coherentes sin romper los temas existentes.

## Capabilities

### New Capabilities

- Ninguno.

### Modified Capabilities

- `invoicing`: se añaden requisitos de márgenes y separación visual.

## Impact

- FXML de las pantallas principales.
- `base.css`.
- No cambia lógica de negocio ni temas de color.
