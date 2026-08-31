## Why

Actualmente la aplicación usa una única ventana cuyo tamaño mínimo global es 800×600. Al navegar a pantallas con mucho contenido (como el Editor de facturas) el usuario puede reducir la ventana hasta que parte de la interfaz desaparece o se corta. Además, la pantalla de selección de empresa (`Arranque`) no tiene un tamaño fijo, por lo que el usuario la puede redimensionar accidentalmente. Se necesita definir tamaños mínimos/predefinidos por pantalla y mantener la experiencia coherente.

## What Changes

- Crear una configuración centralizada de tamaños por vista (`VentanaConfig`).
- Aplicar automáticamente ancho, alto, mínimos, máximos y redimensionabilidad al `Stage` cada vez que se carga una vista FXML.
- Hacer que la ventana se redimensione y centre al cambiar de pantalla.
- Mantener el guardado de posición/tamaño al cerrar, pero permitir que la configuración por vista lo sobreescriba al navegar.
- Añadir un test de UI que verifique que el Editor es visible completo a su tamaño mínimo.

## Capabilities

### New Capabilities

- `invoicing`: ventanas con tamaños mínimos y predefinidos por vista.

### Modified Capabilities

- Ninguno.

## Impact

- `Main.java` (preferencias de ventana).
- `Navegador.java` (aplicar configuración al cambiar de vista).
- `GenerarFacturasMensualesController.java` (diálogo).
- Nuevo `VentanaConfig.java`.
- Nuevo test `EditorTamañoMinimoTest.java`.
- No cambia lógica de negocio.
