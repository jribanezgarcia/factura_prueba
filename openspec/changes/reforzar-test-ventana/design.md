## Componentes Afectados
- `VentanaTransicionTest`: aserciones nuevas y test adicional.

## Logica
- El fallo tenia dos causas de codigo: `ARRANQUE` fijaba `maxWidth=760`/`maxHeight=520`, que el Stage arrastraba a la vista siguiente, y `aplicarSinRedimensionar` llamaba `setMaximized(false)` en cada navegacion. Ambas son observables como propiedades del Stage, sin depender del comportamiento de la ventana nativa.
- Se comprueban justo despues de cargar Arranque (maximos liberados) y tras navegar con la ventana maximizada (sigue maximizada). Las aserciones de tamano que ya existian se conservan como documentacion del comportamiento esperado, aunque por si solas no discriminan.
- No se intenta reproducir el sintoma visual: requeriria arrancar la aplicacion real con `Application.launch` en un JVM propio, pulsar Entrar mediante automatizacion y medir la ventana nativa. Es fragil y desproporcionado; la verificacion de ese extremo se hace a la vista.

## Alternativas consideradas
- Simular la secuencia antigua (`setWidth(1024)` antes de navegar) dentro del test: se probo y pasaba tambien con el codigo antiguo, porque en un Stage de test la ventana nativa si crece. Descartado por no discriminar.

## Testing
- `mvn test -Dtest=VentanaTransicionTest` con `VentanaConfig` revertido a `9ddd14f`: 2 fallos (maximo de Arranque y maximizado).
- `mvn test` con el codigo actual: 108 tests en verde.
