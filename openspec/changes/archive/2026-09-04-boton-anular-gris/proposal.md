## Why

En el tema por defecto (Biblioteca8), el botón Anular del Editor sale pintado en gris (`tema-biblioteca8.css:30`: fondo `#B0B4B8`, texto `#F3F5F6`). Como el proyecto no define estilo propio para `:disabled`, un botón deshabilitado se ve con el gris atenuado de Modena: el Anular habilitado y un botón deshabilitado son casi indistinguibles, y el usuario tiende a pensar que Anular no está disponible.

## What Changes

- En `tema-biblioteca8.css`, la regla `.action-danger-button` pasa a fondo blanco con texto y borde rojos, reutilizando el rojo de peligro que el propio tema ya usa (`.danger-button` y `.chip-anulada`: `#C0392B`).
- No cambia qué botones llevan el estilo (solo `btnAnular` lo usa), ni su comportamiento, ni ningún otro tema.

## Capabilities

### New Capabilities

Ninguna.

### Modified Capabilities

- `invoicing`: se precisa «Estilo de zona de acciones en tema por defecto» — Anular mantiene estilo de peligro pero definido (fondo blanco, texto rojo) para que no se confunda con un botón deshabilitado.

## Impact

- `themes/tema-biblioteca8.css`: dos líneas (`.action-danger-button` y su `:hover`).
- No se toca FXML, ni controladores, ni lógica, ni el resto de temas.
