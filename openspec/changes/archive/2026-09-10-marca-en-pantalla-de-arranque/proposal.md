## Why

La pantalla de selección de empresa es lo primero que ve el usuario al abrir la aplicación, y hoy se presenta con un rótulo que dice «Facturación» (`Arranque.fxml:14`). No es el nombre del programa: es una descripción genérica. La marca es «CaboFactu®», y está en todas partes menos ahí.

El requisito de marca que existe hoy (`spec.md:788`) cubre el icono de la aplicación en la barra de título y en la barra de tareas, y el prefijo «CaboFactu® » delante del título de cada ventana. No dice nada de la portada, que es justo donde la marca tendría más sentido y donde no aparece.

## What Changes

- La pantalla de arranque SHALL identificarse con la marca «CaboFactu®» en lugar del rótulo genérico «Facturación».
- El icono de la aplicación SHALL mostrarse junto a ese rótulo, a su izquierda, formando un conjunto centrado.
- No cambia ningún control de la pantalla ni el flujo de selección de empresa, ejercicio y fecha de trabajo.

## Capabilities

### New Capabilities

Ninguna.

### Modified Capabilities

- `invoicing`: se añade un requisito de presencia de la marca en la pantalla de arranque, que hasta ahora no estaba recogido.

## Impact

- `src/main/resources/com/alcazaba/facturacion/ui/Arranque.fxml`: el rótulo de la línea 14 pasa a ser un conjunto de icono más texto, y se añade el import de `javafx.scene.image`.
- Ningún CSS, ningún `.java` y ninguna otra pantalla.
- No se añade ningún recurso: se reutiliza `src/main/resources/com/alcazaba/facturacion/images/icono-aplicacion.png`, el mismo que ya usa `Ventanas.aplicarIcono(...)`.
