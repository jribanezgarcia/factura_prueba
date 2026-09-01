## Componentes Afectados
- `VentanaConfig.aplicarSinRedimensionar`: elevacion al minimo de la vista.
- `VentanaTransicionTest`: test de la transicion Arranque(760x520) -> Menu(1024x768).

## Logica
- `aplicar` ya envia los Stages visibles a `aplicarSinRedimensionar`; este metodo solo aplicaba min/max/resizable/maximized sin tocar width/height. Al navegar desde Arranque (760x520) al Menu (min 1024x768) la ventana conservaba el tamano previamente fijado por Arranque.
- Fix: tras aplicar min/max, si `stage.getWidth() < minAncho` se llama `stage.setWidth(minAncho)` y si `stage.getHeight() < minAlto` se llama `stage.setHeight(minAlto)`. Esto se ejecuta DESPUES de `stage.setScene(...)` en `Navegador.mostrar`, de modo que la ventana crece hasta el minimo de la nueva vista (nunca reduce ni recentra).
- `Main.entrarEnMenu` mantiene su `setWidth(1024)/setHeight(768)`; queda redundante pero inofensivo.

## Alternativas consideradas
- Mover el setWidth/setHeight a despues del setScene solo en entrarEnMenu: cubre Arranque->Menu pero no otras transiciones; se descarta a favor del clamp en aplicarSinRedimensionar, que cubre cualquier navegacion hacia un tamano mayor.

## Testing
- Nuevo `VentanaTransicionTest`: Stage a 760x520, `nav.mostrar(MenuPrincipal)`, se verifica minWidth/minHeight/width/height == 1024x768.
- `mvn test` (106 tests).
