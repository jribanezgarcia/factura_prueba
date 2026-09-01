## Componentes Afectados
- `VentanaConfig.aplicar`: nuevo paso diferido tras el layout.
- `VentanaTransicionTest`: test corregido al timing real.

## Logica
- `aplicar` con Stage visible llamaba `aplicarSinRedimensionar` (min/max/resizable + clamp) y termina. El problema es que `entrarEnMenu` (Main) pide `setWidth(1024)` ANTES de `nav.mostrar`, y en el momento del clamp `stage.getWidth()` ya devuelve 1024 aunque la ventana nativa siga en 760 (peticion asincrona no aplicada; el Stage no era redimensionable). El layout posterior de la nueva escena deja la ventana nativa en 760x520.
- Fix: en `aplicar`, cuando el Stage ya esta visible y la vista es redimensionable, tras `aplicarSinRedimensionar` se programa `Platform.runLater` que vuelve a subir width/height hasta el minimo si siguen por debajo. Al ejecutarse en el siguiente pulse (despues del layout de la escena nueva), `getWidth()` refleja el tamaño real y el setWidth surte efecto en una ventana ya redimensionable.

## Alternativas consideradas
- Mover el setWidth/setHeight despues de nav.mostrar en Main: solo cubre Arranque->Menu y depende del order exacto; se descarta a favor del diferido generico en VentanaConfig, que cubre cualquier navegacion hacia una vista mayor.

## Testing
- `VentanaTransicionTest` corregido: Stage mostrado no redimensionable 760x520, `setWidth(1024)`, `nav.mostrar(Menu)`, layout forzado, y verificacion tras dos pulses de que width/height == 1024x768.
- `mvn clean test` (106 tests).