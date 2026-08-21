## Context

La barra de navegación (`BarraNavegacion.java`) es un `HBox` construido en Java que aparece en todas las pantallas salvo el menú principal. Su botón Salir ejecuta `nav.stage().close()`. En JavaFX, `close()` programático NO dispara el evento `WINDOW_CLOSE_REQUEST`, por lo que el handler `stage.setOnCloseRequest(...)` de `Main.java` (confirmación de salida, control de cambios sin guardar, guardado de preferencias de ventana y liberación del lock) solo se ejecuta al cerrar por medios externos (X de la ventana). `Dialogos.confirmar(titulo, mensaje)` ya existe y es sustituible en tests mediante `Dialogos.setImpl`.

## Goals / Non-Goals

**Goals:**

- Que pulsar Salir en la barra de navegación pida la misma confirmación que cerrar con la X.
- Que la salida desde la barra pase íntegramente por el mismo proceso de cierre definido en `Main.java`.

**Non-Goals:**

- No cambiar los diálogos existentes, sus textos ni el resto de botones de la barra.
- No tocar el menú principal ni otras formas de cierre.

## Decisions

**Lanzar `WINDOW_CLOSE_REQUEST` en lugar de llamar a `close()`.**
La acción del botón Salir pasa a ser `nav.stage().fireEvent(new WindowEvent(nav.stage(), WindowEvent.WINDOW_CLOSE_REQUEST))`. Así el cierre reutiliza el único punto de salida ya especificado en `Main.java`: comprueba `puedeCerrar()` (diálogo Guardar/Descartar/Cancelar), pide confirmación con `Dialogos.confirmar("Salir", "¿Seguro que deseas salir de la aplicación?")`, y solo entonces ejecuta `alCerrar()`, guarda preferencias de ventana y libera el lock. Una sola línea cambia en `BarraNavegacion.java`.
- Alternativa descartada: duplicar en `BarraNavegacion` la llamada a `Dialogos.confirmar` seguida de `stage.close()`. Repetiría la lógica en dos sitios, seguiría sin pasar por `puedeCerrar()` ni guardar preferencias/lock, y divergiría del cierre por la X.

**Sin cambios en tests existentes.**
El comportamiento nuevo (confirmación) vive en el handler de `Main.java`, no testeable con la suite actual sin arrancar la aplicación completa; la suite debe seguir pasando sin regresiones. La verificación final es manual.

## Risks / Trade-offs

- Si en el futuro se añade otra vía de cierre, debe lanzarse el mismo evento para no saltarse el proceso central; queda documentado en este design.
