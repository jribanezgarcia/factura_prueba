## Context

`grep -rn "danger" src --include=*.fxml --include=*.java` no devuelve nada en producción: las dos clases solo aparecen en el CSS y, como filtro, en `BotonesTest`.

En `base.css` nunca tienen una regla propia: siempre son una línea dentro de un selector agrupado con `primary-button`, `default-button` y `action-button`. En los temas sí tienen reglas propias, cuatro por tema: `.danger-button`, `.action-danger-button`, `.action-danger-button:hover` y una línea compartida para el relleno de sus iconos.

## Goals / Non-Goals

**Goals:** que el CSS deje de describir botones que no existen.

**Non-Goals:** cambiar el aspecto de nada; tocar `btn-ribbon`, `btn-suave` o cualquier otra clase; reintroducir el color de peligro.

## Decisions

### D1. Quitar solo las líneas de peligro de cada grupo

En `base.css` hay nueve selectores agrupados afectados: la regla base de botones y los bloques de `btn-ribbon` y `btn-suave` con sus estados `:hover`, `:pressed` y `:focused`. De cada uno se borra únicamente la línea o líneas que nombran las clases de peligro, dejando intactas las demás y la coma del último selector que queda.

### D2. Reglas enteras en los temas

En cada `tema-*.css` se borran las cuatro líneas completas. La línea del relleno de iconos nombra las dos clases y también desaparece entera.

### D3. El test deja de filtrarlas

En `BotonesTest.botonesSoloTexto` sobran las dos condiciones que descartan botones con esas clases. Se quitan para que el test no sugiera que existen. El resto del método no cambia.

## Risks / Trade-offs

- **Riesgo bajo.** Si algún día se quiere un botón rojo, habrá que volver a escribir las reglas. Es lo decidido el 11/09/2026: ningún botón va en rojo.
- **Descuido posible:** borrar una línea de más en un selector agrupado dejaría sin estilo a los botones normales. Lo cubre la revisión visual de la tarea 3.2.
