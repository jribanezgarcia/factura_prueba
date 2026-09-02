## Why

Al cerrar la ventana, la aplicacion pide confirmacion. Si el usuario responde que **no**, la ventana desaparece igualmente y el proceso sigue vivo en segundo plano, sin ninguna ventana visible y sin forma de volver a ella: hay que matarlo desde el administrador de tareas. Ademas, como la aplicacion mantiene un lock de instancia unica, **no se puede volver a abrir** hasta matar el proceso huerfano.

Dos causas independientes:

1. `Main.configurarVentana` registra `stage.setOnCloseRequest(e -> cerrarAplicacion())` y **nunca consume el evento**. En JavaFX, si el manejador no llama a `e.consume()`, la ventana se cierra de todos modos: la confirmacion se muestra, se ignora la respuesta y la ventana se oculta. Con `Platform.setImplicitExit(false)` —necesario porque la aplicacion oculta y vuelve a mostrar el stage al pasar del arranque al menu— el JVM no termina, asi que queda corriendo sin interfaz.
2. `MenuController.salir` llama a `nav.stage().close()`. `Stage.close()` equivale a `hide()` y **no dispara `WINDOW_CLOSE_REQUEST`**, asi que el boton Salir del menu principal ni pregunta, ni comprueba cambios sin guardar, ni guarda las preferencias de ventana, ni libera el lock: solo esconde la ventana y deja el mismo proceso huerfano. La barra de navegacion ya lo hacia bien (`fireEvent(new WindowEvent(..., WINDOW_CLOSE_REQUEST))`); el menu principal se habia quedado fuera.

## What Changes

- `cerrarAplicacion` pasa a devolver `boolean`: `true` si la aplicacion se cierra, `false` si el usuario cancela o hay cambios sin guardar.
- El manejador de `setOnCloseRequest` **consume el evento** cuando ese metodo devuelve `false`, de modo que la ventana permanece visible.
- `MenuController.salir` dispara `WINDOW_CLOSE_REQUEST` igual que la barra de navegacion, con lo que el boton Salir del menu pasa por el mismo camino: confirmacion, cambios sin guardar, preferencias y lock.
- Se conserva el comportamiento actual en la pantalla de arranque (`actual == null`): ahi se cierra sin preguntar.

## Impact

- src/main/java/com/alcazaba/facturacion/Main.java
- src/main/java/com/alcazaba/facturacion/ui/MenuController.java
