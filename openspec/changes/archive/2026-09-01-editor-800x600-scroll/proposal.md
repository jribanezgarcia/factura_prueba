## Why

El usuario ha decidido que todas las pantallas de la aplicación (excepto la selección de empresa) deben tener un tamaño único de 800×600, sin saltos de tamaño al navegar. El Editor actualmente se abre maximizado y tiene un tamaño mínimo de 1000×760, lo que rompe esa uniformidad y dificulta su uso en pantallas de baja resolución.

## What Changes

- Cambiar el tamaño del Editor a 800×600 y eliminar la apertura maximizada.
- Hacer que la tabla de líneas tenga una altura mínima reducida y crezca cuando la ventana se maximice (scroll interno para muchas líneas).
- Ajustar paddings/espaciados para que la cabecera, la tabla y los totales encajen en 800×600 sin scroll general.
- Actualizar el test de tamaño mínimo del Editor.
- Actualizar la especificación de tamaños de ventana.

## Capabilities

### New Capabilities

- Ninguno.

### Modified Capabilities

- `invoicing`: Editor adaptado a 800×600 con tabla scrollable.

## Impact

- `VentanaConfig.java`
- `Editor.fxml`
- `EditorTamanoMinimoTest.java`
- `openspec/specs/invoicing/spec.md`
- No cambia lógica de negocio.
