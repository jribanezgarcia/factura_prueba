## Context

La aplicación ya tiene un sistema de temas basado en `ThemeManager`, que carga `base.css` y un tema de colores (`tema-<nombre>.css`) en cada `Scene`. La especificación de UI actual cubre ventana, menú, configuración e histórico, pero no define un sistema de diseño visual coherente ni modo claro/oscuro. Ver `proposal.md` para la motivación.

## Goals / Non-Goals

**Goals:**
- Renovar `base.css` como sistema de diseño visual tipo Ajustes de Apple a nivel de estructura (espaciado, tarjetas, tipografía, estados), manteniendo las paletas de color actuales.
- Ajustar FXML principales para usar el nuevo sistema de espaciado y tarjetas.
- Añadir microinteracciones suaves (hover/foco) sin bloquear la interacción.
- Mantener los 7 temas de color actuales intactos.

**Non-Goals:**
- No se rediseñan flujos de negocio ni estructuras de datos.
- No se implementan animaciones físicas tipo springs ni `backdrop-filter` (no soportado nativamente por JavaFX CSS).
- No se cambian los PDFs generados.
- No se eliminan temas existentes.

## Decisions

### 1. Variables estructurales en `base.css`

`base.css` definirá variables de estructura y apariencia comunes que no dependen de la paleta: `-card-radius`, `-card-padding`, `-shadow`, `-hover-overlay`, `-focus-ring`, etc. Los colores seguirán definidos en cada `tema-<nombre>.css`, de modo que los temas existentes no cambian de paleta.

**Alternativa considerada:** definir variables CSS estándar (`var(--x)`). JavaFX CSS no las soporta, así que se descarta.

### 2. Temas de colores intactos

No se modifica la lista de temas ni sus paletas. Cada `tema-<nombre>.css` seguirá definiendo los colores concretos para fondos, textos, acentos y estados. `base.css` solo aplicará layout, radios, sombras y estados genéricos usando transparencias (`rgba`) sobre los colores ya definidos por el tema.

**Alternativa considerada:** centralizar las paletas en `base.css`. Se descarta para respetar los colores actuales.

### 3. Microinteracciones

- **Hover/foco de color:** se implementan con pseudo-clases `:hover` y `:focused` en CSS (JavaFX las soporta).
- **Transiciones suaves:** JavaFX CSS no soporta `transition`. Se usarán pequeñas animaciones JavaFX (`FadeTransition`, `ScaleTransition`) ligadas a eventos de ratón/foco solo en controles principales (botones primarios, items del menú), aplicadas con duraciones cortas (< 150 ms) y respetando preferencia de movimiento reducido.

**Alternativa considerada:** no usar animaciones. Se descarta porque la especificación pide microinteracciones; se limita a las técnicamente simples.

### 5. Prototipado visual con `javafx-design`

Antes de tocar FXML/CSS se generarán prototipos HTML autocontenidos de las pantallas clave (menú, editor, histórico, configuración) para validar la dirección visual con el usuario.

## Risks / Trade-offs

- **Riesgo:** Los tests de UI que dependen de clases CSS antiguas o de la estructura exacta de nodos pueden fallar.
  - **Mitigación:** Revisar y actualizar tests tras el cambio; ejecutar `mvn test` frecuentemente.
- **Riesgo:** Algunos ajustes de estructura (sombras, bordes redondeados, transparencias) pueden verse distintos en temas oscuros o muy saturados.
  - **Mitigación:** Probar cada tema individualmente y ajustar `base.css` para que las transparencias funcionen sobre fondos claros y oscuros.
- **Trade-off:** El diseño Apple prioriza aire y tarjetas, lo que puede reducir la densidad de información en pantallas pequeñas.
  - **Mitigación:** Mantener el mínimo de 800x600 y probar que todos los controles siguen visibles.

## Migration Plan

No requiere migración de datos. Los cambios son puros estilos y FXML. La preferencia de tema existente seguirá funcionando exactamente igual.

## Open Questions

Ninguno. Las decisiones principales están cerradas por las respuestas del usuario en `/opsx-propose`.
