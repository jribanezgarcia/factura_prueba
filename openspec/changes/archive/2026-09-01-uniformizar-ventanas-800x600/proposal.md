## Why

El usuario ha decidido que todas las pantallas de la aplicación, excepto la de selección de empresa (`Arranque`), deben tener un tamaño único de 800×600 y que la ventana no debe cambiar de tamaño al navegar entre secciones. Actualmente Configuración, Histórico, Clientes, Versiones, Backup y el diálogo de Generar facturas mensuales tienen tamaños distintos, lo que provoca saltos visuales.

## What Changes

- Establecer 800×600 como tamaño predefinido y mínimo para Configuración, Histórico, Clientes, Versiones, Backup y Generar facturas mensuales.
- Modificar `VentanaConfig.aplicar` para que no fuerce el redimensionamiento de la ventana principal al navegar entre pantallas (solo aplica mínimos y propiedades), manteniendo 800×600.
- Ajustar cada FXML para que encaje en 800×600: reducir paddings, ajustar tablas y añadir scroll horizontal donde sea necesario.
- Actualizar `Main.java` para que, al salir de `Arranque`, la ventana pase a 800×600.
- Actualizar la especificación de tamaños de ventana y los tests afectados.

## Capabilities

### New Capabilities

- Ninguno.

### Modified Capabilities

- `invoicing`: tamaño uniforme de 800×600 para todas las pantallas principales y navegación sin saltos.

## Impact

- `VentanaConfig.java`
- `Main.java`
- FXML de Configuración, Histórico, Clientes, Versiones, Backup y GenerarFacturasMensuales.
- `openspec/specs/invoicing/spec.md`
- Tests de UI si dependen de tamaños anteriores.
