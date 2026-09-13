## Why

`Main` (170 líneas) mezcla seis responsabilidades: preparar la carpeta de datos, cargar la demostración, garantizar la instancia única con un bloqueo de fichero, configurar la ventana, llevar el flujo arranque → menú → cierre y guardar la posición y el tamaño de la ventana. La clase de entrada de una aplicación JavaFX debería limitarse a arrancar y delegar; así se lee de un vistazo el orden del arranque y cada pieza se puede probar sin abrir ventanas.

Además, al cerrar se guardan `ventana_x`, `ventana_y`, `ventana_w` y `ventana_h`, pero ningún código los lee desde que cada vista fija su tamaño (`ventana-siempre-1024`): es código muerto, y la spec sigue diciendo que la aplicación recuerda el tamaño y la posición de la ventana.

Por último, si el fichero de bloqueo no se puede abrir (por ejemplo, por permisos), hoy se avisa de que «la aplicación ya está en ejecución», que es falso.

## What Changes

- Nueva utilidad estática `InstanciaUnica` (junto a `Main`) con `adquirir()` y `liberar()`. `adquirir()` devuelve `false` si otra instancia tiene el bloqueo y lanza `IOException` si no puede abrir el fichero.
- Nueva utilidad estática `PreparacionDatos` (junto a `Main`) con `crearCarpeta()` y `cargarDemoSiNoHayEmpresas()`, que devuelve si ha cargado la demostración y la deja como última empresa.
- `Main` queda en: `main`, `start` (el orden del arranque llamando a esas piezas y mostrando los mensajes de error), `configurarVentana`, `mostrarArranque`, `entrarEnMenu` y `cerrarAplicacion`. Las piezas no muestran diálogos: `Main` decide qué mensaje sale y si la aplicación se cierra.
- Si el fichero de bloqueo no se puede abrir, el mensaje pasa a ser «No se pudo comprobar si la aplicación ya está abierta» con el motivo.
- Se elimina el guardado de posición y tamaño de la ventana: el método de `Main`, las constantes `VENTANA_X/Y/W/H` de `PreferenciasGlobales` y su uso en `PreferenciasGlobalesTest`. La spec deja de decir que se recuerdan.
- Javadoc breve en las clases y métodos nuevos y en los métodos que quedan en `Main`.
- Tests nuevos de `InstanciaUnica` y `PreparacionDatos`.

## Capabilities

### New Capabilities

Ninguna.

### Modified Capabilities

- `invoicing`: «Configuración» deja de incluir el tamaño y la posición de la ventana entre las preferencias recordadas.

## Impact

- `src/main/java/com/alcazaba/facturacion/Main.java`.
- Nuevos: `src/main/java/com/alcazaba/facturacion/InstanciaUnica.java`, `PreparacionDatos.java`.
- `service/PreferenciasGlobales.java` (se quitan cuatro constantes).
- Tests: nuevos `src/test/java/com/alcazaba/facturacion/InstanciaUnicaTest.java` y `PreparacionDatosTest.java`; ajuste de `service/PreferenciasGlobalesTest.java`.
- Sin cambios en `Launcher`, `pom.xml`, controladores, FXML ni base de datos.
