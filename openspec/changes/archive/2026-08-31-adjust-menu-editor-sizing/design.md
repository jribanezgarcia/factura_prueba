## Context

El usuario reporta que el Menú principal debe ser de 800×600 y que las tarjetas no deben llegar justo al borde inferior. Además, el Editor necesita aprovechar todo el alto de pantalla, por lo que debe abrirse maximizado.

## Goals / Non-Goals

**Goals:**
- Menú principal: tamaño 800×600 con margen inferior visible.
- Editor: abrir maximizado por defecto.
- Actualizar `VentanaConfig` para soportar flag de maximizado.

**Non-Goals:**
- No se rediseña el contenido interno del Menú ni del Editor.
- No se cambian otros tamaños de ventana.

## Decisions

1. **Menú principal 800×600:** se ajustan `VentanaConfig.MENU` y `MenuPrincipal.fxml`.
2. **Margen inferior:** se aumenta el padding inferior del `BorderPane` raíz a 20 px y se alinea el contenido del centro arriba (`alignment="TOP_CENTER"`) para garantizar espacio visible debajo de las tarjetas.
3. **Editor maximizado:** se añade un campo `maximizado` en `VentanaConfig`; `EDITOR` lo activa. En `aplicar` se llama a `stage.setMaximized(maximizado)`.
4. **Test del Editor:** se desmaximiza el `Stage` antes de comprobar el tamaño mínimo, ya que el test valida el tamaño restaurado.

## Risks / Trade-offs

- Si el usuario restaura el Editor, seguirá viéndose completo gracias al mínimo 1000×760.
- Al navegar del Editor maximizado al Menú, la ventana se desmaximiza y se centra; puede haber un pequeño parpadeo.

## Migration Plan

Ninguna.

## Open Questions

Ninguna.
