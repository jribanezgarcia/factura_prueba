## Why

Hoy la aplicación recuerda dónde y cómo de grande estaba la ventana al cerrar, y la vuelve a abrir así. En la práctica eso produce aperturas descolocadas: medio fuera de la pantalla tras cambiar de monitor, o a un tamaño que ya no encaja con el diseño, que está pensado para 1024x768.

## What Changes

- La ventana SHALL abrirse siempre a 1024x768 y centrada en la pantalla principal, como en la primera ejecución.
- La posición y el tamaño de la ventana se SHALL seguir guardando al cerrar, pero la aplicación SHALL ignorarlos al abrir. Las preferencias ya guardadas no se borran.
- El usuario sigue pudiendo redimensionar y maximizar durante la sesión, sin bajar de 1024x768.
- La pantalla de arranque sigue midiendo 760x520 y sale también centrada.

## Capabilities

### New Capabilities

Ninguna.

### Modified Capabilities

- `invoicing`: el requisito «Ventana» deja de restaurar la posición y el tamaño de la sesión anterior.

## Impact

- `src/main/java/com/alcazaba/facturacion/Main.java`: se retiran `aplicarPreferenciasVentana`, `restaurarTamanoGuardado`, los campos `anchoGuardado` y `altoGuardado` y las constantes `ANCHO_INICIAL` y `ALTO_INICIAL`. `guardarPreferenciasVentana` se queda.
- Ningún test cambia: `PreferenciasGlobalesTest` solo comprueba que las preferencias se guardan y se leen.
