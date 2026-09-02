## Componentes Afectados

- `Main.configurarVentana` y `Main.cerrarAplicacion`.
- `MenuController.salir`.

## Logica

**Consumir el evento es la unica forma de cancelar un cierre.** `WINDOW_CLOSE_REQUEST` es una notificacion: el manejador puede hacer lo que quiera, pero si no lo consume, la ventana se oculta. Devolver pronto del manejador no cancela nada. De ahi que la version anterior mostrara la confirmacion y luego ignorara la respuesta.

Para consumir hace falta saber si el usuario acepto, asi que `cerrarAplicacion` deja de ser `void`. Los tres caminos:

- Cambios sin guardar y el usuario decide quedarse (`puedeCerrar()` falso) → `false`.
- Confirmacion rechazada → `false`.
- Confirmacion aceptada, o pantalla de arranque sin vista (`actual == null`) → se guardan preferencias, se libera el lock, `Platform.exit()` y `true`.

**Por que la pantalla de arranque no pregunta.** `actual` solo se rellena cuando el `Navegador` del menu carga una vista. En el arranque no hay nada que perder y preguntar seria ruido; ese comportamiento ya era el de antes y se mantiene tal cual.

**Por que el menu debe disparar el evento y no llamar a `close()`.** Todo el proceso de cierre —confirmacion, `alCerrar()`, preferencias de ventana, lock— cuelga de `setOnCloseRequest`. Cualquier camino que llame directamente a `close()` o `hide()` se lo salta entero. Disparar `WINDOW_CLOSE_REQUEST` deja un unico punto de salida, que es lo que ya hacia `BarraNavegacion`.

**Efecto colateral del lock.** El proceso huerfano mantenia abierto el `FileLock` de instancia unica, asi que el siguiente arranque mostraba "La aplicacion ya esta en ejecucion". Al arreglar el cierre desaparece tambien ese sintoma, que es como se manifestaba el bug para el usuario.

## Alternativas consideradas

- **Quitar `Platform.setImplicitExit(false)`**: la ventana oculta cerraria el JVM y no habria proceso huerfano, pero se rompe el paso de arranque a menu, que oculta y vuelve a mostrar el mismo stage — el JVM moriria en ese hueco.
- **Volver a mostrar el stage cuando el usuario cancela** (`stage.show()` despues del dialogo): funciona de casualidad, parpadea y deja el codigo mintiendo sobre lo que hace. Consumir el evento es lo correcto.
- **Dejar `close()` en el menu y duplicar alli la confirmacion**: dos copias del proceso de cierre que se separan a la primera.

## Testing

Verificacion manual, porque `cerrarAplicacion` llama a `Platform.exit()` y no es aislable sin refactorizar `Main`:

- Cerrar con la X y responder que no: la ventana sigue visible y usable.
- Pulsar Salir en el menu principal y responder que no: igual.
- Repetir con una factura con cambios sin guardar: primero avisa de los cambios y, si se decide no salir, la ventana sigue visible.
- Responder que si: la aplicacion se cierra y **vuelve a abrirse a la primera**, sin el aviso de instancia ya en ejecucion (prueba de que el lock quedo liberado).
