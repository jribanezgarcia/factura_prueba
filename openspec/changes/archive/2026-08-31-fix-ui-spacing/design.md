## Context

El change `redesign-ui-apple` introdujo tarjetas de sección y un nuevo `base.css`. Tras probarlo visualmente se detecta que los paneles quedan demasiado pegados al borde de la ventana y a la barra de menú. La solución es puramente de márgenes y paddings, sin cambiar colores ni lógica.

## Goals / Non-Goals

**Goals:**
- Añadir márgenes externos a los paneles de contenido para separarlos del borde de la ventana.
- Aumentar la separación entre la barra de menú y los controles.
- Corregir el espaciado interno de las filas de filtros en el Histórico.
- Mantener los tests de UI existentes en verde.

**Non-Goals:**
- No se cambian colores, temas ni tipografía.
- No se modifica la lógica de negocio.
- No se reestructuran pantallas.

## Decisions

1. **Márgenes globales con `.zona-contenido`**: ampliar el padding de `.zona-contenido` en `base.css` de 12px a 16px y, en las pantallas afectadas, envolver el contenido de `top`/`center`/`bottom` con un contenedor que use esta clase.
2. **Separación menú-contenido**: añadir `padding` adicional en el `VBox` que envuelve `nav-bar` + contenido, o bien añadir un espaciador/margen debajo de `nav-bar`.
3. **Filtros del Histórico**: añadir padding interno a la `FlowPane` de filtros para que el primer campo no toque el borde.
4. **No tocar `.card`**: el padding interno de 18px de las tarjetas se mantiene; el problema es el margen externo, no el interno.

## Risks / Trade-offs

- Aumentar los márgenes reduce ligeramente el espacio útil disponible a 800x600. Se verificará que ningún control se corte.
- Algunas pantallas (Editor) tienen una tabla con `minHeight`; hay que comprobar que sigue visible.

## Migration Plan

Ninguna. Cambios puros de CSS/FXML.

## Open Questions

Ninguna.
