## Context

La pantalla «Generar facturas mensuales» se construye de forma declarativa en `GenerarFacturasMensuales.fxml` con un `BorderPane` (título en `top`, formulario en `center` dentro de un `ScrollPane styleClass="card"`, botones en `bottom`). La aplicación usa un sistema de temas compartido: `ThemeManager.hojas()` carga siempre `base.css` más el tema de color activo. El problema estético tiene tres causas concretas:

1. El título usa `styleClass="titulo-dialogo"` y el subtítulo `subtitulo-dialogo`: **ambas clases no existen en ningún CSS**, por lo que caen al estilo por defecto de JavaFX. El resto de pantallas usa `titulo` (definido en `base.css` y con `-fx-text-fill` por tema).
2. Las etiquetas del `GridPane` no llevan clase de estilo: la clase `.form-label` ya existe en `base.css` (12px, opacidad 0.75) pero **nunca se aplica**.
3. El `ScrollPane` con `styleClass="card"` aplica `-fx-background-color: -fx-base` (blanco en el tema por defecto), que contrasta con el fondo de la ventana.

Ver proposal.md para la motivación.

## Goals / Non-Goals

**Goals:**
- Alinear la apariencia de la pantalla de generación mensual con el resto de pantallas (Configuración, Backups, Versiones).
- Título destacado con `.titulo` (negrita + color del tema).
- Etiquetas con separación y estilo coherente (`.form-label`).
- Panel con fondo neutro en lugar de blanco plano.
- Cambio exclusivamente de presentación: cero impacto funcional.

**Non-Goals:**
- Rediseñar el flujo de generación ni los campos del formulario.
- Crear clases nuevas en los temas de color por separado.
- Tocar otras pantallas.

## Decisions

**D1 — Título con la clase estándar `.titulo`.**
Cambiar `styleClass="titulo-dialogo"` → `styleClass="titulo"` en el `Label` del `top`. Rationale: `titulo` ya está definido en `base.css` (20px, negrita) y en cada tema de color (`-fx-text-fill`), por lo que se integra automáticamente en los 7 temas sin tocar ningún `tema-*.css`. Alternativa descartada: crear una clase nueva `titulo-dialogo`; innecesaria porque el objetivo es reutilizar el patrón existente.

**D2 — Subtítulo con la clase existente `.section-title`.**
Cambiar `styleClass="subtitulo-dialogo"` → `styleClass="section-title"` en el `Label` «Líneas de cada factura». Rationale: `section-title` ya existe en `base.css` (13px, negrita, opacidad 0.75) y es la clase usada para títulos de sección dentro de formularios en otras pantallas.

**D3 — Etiquetas con `.form-label`.**
Añadir `styleClass="form-label"` a cada `Label` del `GridPane` (Cliente, Serie, Año, Mes inicio, Mes fin, Día del mes, Tipo de IVA, Retención IRPF). Rationale: `.form-label` ya está definida y es la clase prevista para etiquetas de formulario. Alternativa descartada: escribir estilos inline; rompería la coherencia.

**D4 — Panel con fondo neutro en vez de `card` blanco.**
Sustituir `styleClass="card"` del `ScrollPane` por una clase nueva `.panel-neutro` definida en `base.css` con `-fx-background-color: derive(-fx-base, -3%)` y radio/borde suaves (sin sombra fuerte). Rationale: se elimina el bloque blanco plano manteniendo un panel sutilmente diferenciado del fondo de la ventana. No se toca `.card` (lo usan otras pantallas a propósito). Alternativa descartada: no usar ningún panel (fondo totalmente transparente); el usuario prefirió alinear con el resto, y el resto distingue el contenido con un panel suave. El color deriva del `-fx-base` del tema, así que funciona en los 7 temas sin tocarlos.

**D5 — Separación del formulario.**
Subir el espaciado del `GridPane` y dar margen respecto al borde del panel: aumentar `hgap`/`vgap` (p. ej. `hgap="12" vgap="10"`) y añadir `padding` interior al `ScrollPane` o al `VBox` para que las etiquetas no queden pegadas. Rationale: directamente en el FXML, sin CSS extra; es un ajuste de composición breve y legible.

## Risks / Trade-offs

- [El nuevo fondo `.panel-neutro` derive de `-fx-base` y resulte demasiado tenue o fuerte en algún tema] → Usar `derive(-fx-base, -3%)`, un cambio imperceptible que siempre es válido en todos los temas; se verifica visualmente en un par de temas.
- [Cambiar el espaciado del `GridPane` altere ligeramente la altura de la pantalla] → La pantalla está en un `ScrollPane` (desplazamiento si hiciera falta) y el cambio de espaciado es pequeño; no afecta al tamaño mínimo (800×600).
- [`.section-title` tenga `-fx-padding` que cambie la posición del subtítulo] → Es un glifo de sección esperado; se ajusta visualmente durante la verificación.

## Migration Plan

Tras aplicar los cambios en el FXML y `base.css`, relanzar la suite completa (`mvn -o test`). No hay migración de datos ni de esquema. Rollback: revertir `GenerarFacturasMensuales.fxml` y la clase añadida en `base.css`.
