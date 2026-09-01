## Why
Al pulsar Entrar en Arranque, el menu y el resto de vistas se quedaban en 760x520 (el tamano de Arranque) en lugar de crecer a 1024x768, y solo se corregia redimensionando o maximizando a mano. Los tres intentos previos atacaron el timing del redimensionado (clamps y `Platform.runLater`) y no la causa real:

- `ARRANQUE` era la unica vista que fijaba maximos (`maxWidth=760`, `maxHeight=520`). Esos maximos quedaban puestos en el Stage y en la ventana nativa al salir de esa pantalla.
- `Main.entrarEnMenu` pedia `setWidth(1024)`/`setHeight(768)` antes de que nadie levantase esos maximos y con `resizable=false` todavia activo, asi que la ventana nativa recortaba la peticion.
- Los clamps de `VentanaConfig` eran condicionales a `stage.getWidth()`, que en ese instante devuelve la propiedad (1024) y no el tamano real de la ventana, por lo que nunca llegaban a ejecutarse.

## What Changes
- `VentanaConfig`: `ARRANQUE` deja de fijar maximos; su caracter fijo lo garantiza `redimensionable=false`.
- `VentanaConfig.aplicar` pasa a un unico camino determinista: libera los maximos de la vista anterior, fija `resizable`, minimos y maximos de la vista nueva y decide si redimensionar comparando la configuracion previa (guardada en `stage.getProperties()`) con la nueva, en lugar de leer `stage.getWidth()`. Solo maximiza cuando la vista lo pide, de modo que navegar ya no desmaximiza la ventana del usuario.
- `Main.entrarEnMenu` deja de dimensionar la ventana y oculta el Stage antes de cargar el menu, mostrandolo despues: la ventana nativa se recrea con el tamano de la vista destino, sin arrastrar restricciones de Arranque.
- `Main` recupera el tamano guardado de la sesion anterior cuando supera el minimo de la vista.
- `VentanaTransicionTest` reescrito: cubre la transicion Arranque -> Menu (tamano, maximos liberados y redimensionabilidad) y anade un caso de conservacion del tamano del usuario al navegar entre vistas del mismo tamano.

## Capabilities
### New Capabilities
- Ninguna.
### Modified Capabilities
- Ninguna (correccion de implementacion del requisito "Tamanos de ventana por vista" ya especificado).

## Impact
- src/main/java/com/alcazaba/facturacion/ui/VentanaConfig.java
- src/main/java/com/alcazaba/facturacion/Main.java
- src/test/java/com/alcazaba/facturacion/ui/VentanaTransicionTest.java
