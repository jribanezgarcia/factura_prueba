## Why

El Menú principal necesita un tamaño de 800×600 y un poco de margen inferior para que las tarjetas no lleguen justo al borde de la ventana. Por otro lado, el Editor de facturas necesita todo el alto disponible de la pantalla, por lo que debe abrirse maximizado por defecto; la tabla de líneas ya permite scroll vertical cuando hay muchos conceptos.

## What Changes

- Cambiar el tamaño predefinido/mínimo del Menú principal a 800×600.
- Ajustar el FXML del Menú principal para dejar margen inferior y alinear las tarjetas arriba.
- Hacer que el Editor se abra maximizado por defecto.
- Actualizar `VentanaConfig` para soportar un flag de maximizado.
- Ajustar el test `EditorTamanoMinimoTest` para desmaximizar antes de comprobar el tamaño mínimo.
- Actualizar la especificación de tamaños de ventana.

## Capabilities

### New Capabilities

- Ninguno.

### Modified Capabilities

- `invoicing`: ajustes en los escenarios de Menú principal y Editor.

## Impact

- `VentanaConfig.java`
- `MenuPrincipal.fxml`
- `EditorTamanoMinimoTest.java`
- `openspec/specs/invoicing/spec.md`
