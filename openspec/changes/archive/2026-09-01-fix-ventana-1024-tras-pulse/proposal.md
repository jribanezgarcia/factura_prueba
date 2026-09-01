## Why
El fix previo (clamp en `aplicarSinRedimensionar`) no bastaba: al pulsar Entrar el Stage ya está visible y no redimensionable (como Arranque, 760x520), `entrarEnMenu` pide 1024x768 antes de `setScene`, y en el momento del clamp `getWidth()` ya reporta 1024 aunque la ventana nativa siga en 760. Tras el layout de la nueva escena la ventana vuelve a 760x520 hasta que se redimensiona o maximiza manualmente.

## What Changes
- En `VentanaConfig.aplicar`, tras aplicar la vista con el Stage ya visible, se difiere la re-aplicacion del tamano minimo al siguiente pulse (`Platform.runLater`), de modo que se ejecuta una vez que la nueva escena ha hecho layout y el ancho real del Stage esta sincronizado con la ventana nativa.
- `VentanaTransicionTest` se corrige para reproducir el timing real: Stage mostrado y no redimensionable a 760x520, peticion de 1024x768, carga del Menu, layout, y comprobacion del tamano final tras el pulse diferido.

## Capabilities
### New Capabilities
- Ninguna.
### Modified Capabilities
- Ninguna (correccion de implementacion del requisito "Ventana" ya especificado).

## Impact
- src/main/java/com/alcazaba/facturacion/ui/VentanaConfig.java
- src/test/java/com/alcazaba/facturacion/ui/VentanaTransicionTest.java