## Context

`FXMLLoader` parte los atributos de lista de `styleClass` por comas, no por espacios. Los 15 nodos afectados usan `styleClass="card zona-contenido"`, lo que produce una sola clase literal `card zona-contenido`, sin coincidencia con ningún selector de `base.css`, de modo que pierden fondo de tarjeta, borde, radio, sombra y padding. `Editor.fxml` ya está corregido con `styleClass="card, zona-contenido"` y sirve de referencia.

Ver proposal.md para la motivación y la lista exacta de nodos; el delta spec define el comportamiento observable (todas las pantallas principales muestran paneles con estilo de tarjeta).

## Goals / Non-Goals

**Goals:**
- Unificar el separador de `styleClass` a la coma en los 15 nodos de las 5 pantallas.
- Garantizar que los paneles de esas pantallas reciban `.card` y `.zona-contenido`.
- Evitar regresiones con un test que detecte cualquier `styleClass` con espacio en los FXML.

**Non-Goals:**
- No rediseñar el CSS ni cambiar tamaños/spaciados de las clases (siguen como están en `base.css`).
- No tocar la lógica de los controladores.
- No revertir el archive de 2026-08-31-fix-ui-spacing (los espaciados manuales que no dupliquen la clase pueden permanecer; solo se revisa visualmente si sobran).

## Decisions

- **Separador coma en `styleClass`** (`card, zona-contenido`): es el mecanismo nativo de `FXMLLoader` y ya está usado en `Editor.fxml`. Alternativa descartada: cambiar `base.css` a selectores con escapado de espacios, frágil y no idiomático.
- **El padding no suma entre clases**: `.card` y `.zona-contenido` declaran ambas `-fx-padding: 16px`; al aplicar ambas clases gana una sola declaración, de modo que el nodo pasa de 0 px a 16 px (no 32 px). No hace falta tocar CSS.
- **Test antirregresión por lectura de FXML**: un test JUnit que recorra los FXML de `src/main/resources/com/alcazaba/facturacion/ui/` y falle si algún `styleClass` contiene un espacio sin coma. Detecta todos los futuros casos sin depender de JavaFX. Alternativa descartada: test en runtime con `lookup`, más lento y frágil.

## Risks / Trade-offs

- [El nuevo padding/borde consume espacio vertical y horizontal que hoy no consume] → Revisar Backup, Clientes, Configuración, Histórico y Versiones a 1024×768 tras el cambio (sin scroll nuevo ni recortes); los tests existentes de tamaño de ventana deben seguir pasando.
- [Cambios manuales previos en esos FXML (parche fix-ui-spacing) dupliquen espaciado] → En la verificación visual, si se detecta exceso de separación, documentar en tasks la limpieza puntual, manteniéndola como tarea separada y mínima.
- [El test falle al leer FXML por problemas de encoding/ruta] → Leer los FXML como UTF-8 y resolver la raíz desde recursos de test con rutas relativas a las fuentes.