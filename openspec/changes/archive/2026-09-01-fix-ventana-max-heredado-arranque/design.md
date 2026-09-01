## Componentes Afectados
- `VentanaConfig`: configuracion de `ARRANQUE` y metodo `aplicar`.
- `Main`: `start`, `entrarEnMenu` y recuperacion del tamano guardado.
- `VentanaTransicionTest`: cobertura de la transicion y de la conservacion del tamano.

## Logica
- Un Stage arrastra las restricciones de la vista anterior. Como `ARRANQUE` fijaba `maxWidth=760`/`maxHeight=520`, la ventana nativa seguia limitada a ese tamano cuando `entrarEnMenu` pedia 1024x768, y la peticion se recortaba. Los clamps posteriores no corregian nada porque `stage.getWidth()` ya devolvia 1024 (propiedad de JavaFX) mientras la ventana nativa seguia en 760.
- `aplicar` libera ahora `maxWidth`/`maxHeight` antes de aplicar la vista nueva, fija `resizable` antes de tocar el tamano y decide si redimensionar comparando el tamano de la configuracion anterior (guardada por Stage en `getProperties()`) con el de la nueva. Asi no depende de leer el tamano actual, que puede no reflejar la ventana real.
- La comparacion por configuracion cumple ademas el requisito de conservar el tamano del usuario: Menu, Editor, Historico, Clientes, Configuracion, Versiones y Backup comparten 1024x768, asi que navegar entre ellas no toca el tamano; solo Arranque (760x520) fuerza el cambio.
- La transicion Arranque -> Menu se hace con el Stage oculto: al cargar la vista con la ventana no visible, `aplicar` fija tamano, minimos y centrado, y `show()` crea la ventana nativa ya con esas medidas. Es el unico camino que no depende del comportamiento del peer de Windows ante una ventana visible y no redimensionable. Requiere `Platform.setImplicitExit(false)` para que ocultar la unica ventana no cierre la aplicacion; el cierre real sigue siendo el `Platform.exit()` de `cerrarAplicacion`.
- El dialogo `GenerarFacturasMensuales` usa un Stage propio y mantiene su 800x600; la configuracion previa se guarda por Stage, de modo que el dialogo no interfiere con la ventana principal.

## Alternativas consideradas
- Mantener el clamp diferido a `Platform.runLater`: es la solucion de los dos intentos anteriores y no funciona, porque `setWidth` al valor que ya tiene la propiedad no genera peticion a la ventana nativa.
- Forzar el tamano en cada navegacion: cumpliria la transicion, pero incumple el requisito de conservar el tamano del usuario al cambiar de vista.

## Testing
- `VentanaTransicionTest.menuSubeHasta1024AlPasarDeArranque`: Arranque a 760x520, navegacion al Menu y comprobacion de tamano, minimos, maximos liberados y redimensionabilidad, repetida tras estabilizarse la ventana.
- `VentanaTransicionTest.navegarEntreVistasConservaElTamanoDelUsuario`: menu ampliado a 1300x900, navegacion al Historico y comprobacion de que el tamano se mantiene.
- `EditorTamanoMinimoTest` sin cambios.
- `mvn test` (107 tests).
