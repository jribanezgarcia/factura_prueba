## Why
Al pulsar Entrar en la pantalla de empresa (Arranque, 760x520), el Menu principal y el resto de vistas principales se muestran a 760x520 en lugar de 1024x768 hasta que se redimensiona o maximiza. `VentanaConfig.aplicar` envia los Stages ya visibles a `aplicarSinRedimensionar`, que solo aplica min/max/resizable/maximized y nunca vuelve a fijar el tamano tras el cambio de escena, por lo que una ventana pequena conserva su tamano al pasar a una vista mayor.

## What Changes
- En `VentanaConfig.aplicarSinRedimensionar`, elevar el ancho y alto del Stage hasta el minimo de la vista cuando la ventana este por debajo de dicho minimo (sin recentrar ni reducir).
- Nuevo test `VentanaTransicionTest` que simula la transicion desde una ventana 760x520 hacia el Menu y verifica que sube a 1024x768.

## Capabilities
### New Capabilities
- Ninguna.
### Modified Capabilities
- invoicing: garantia de que al navegar a una vista con mayor tamano minimo la ventana crece hasta ese minimo aunque ya este visible.

## Impact
- src/main/java/com/alcazaba/facturacion/ui/VentanaConfig.java
- src/test/java/com/alcazaba/facturacion/ui/VentanaTransicionTest.java
- openspec/specs/invoicing/spec.md (escenario de correccion al navegar)
