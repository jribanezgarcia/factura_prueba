## Context

La aplicación JavaFX utiliza un único `Stage` gestionado por `Navegador`. `Main` configura un tamaño mínimo global de 800×600 y guarda/restaura posición y tamaño entre sesiones. Esto provoca que pantallas con más contenido puedan reducirse demasiado. El change debe definir tamaños por vista sin romper el guardado de preferencias.

## Goals / Non-Goals

**Goals:**
- Definir tamaño predefinido, mínimo y (solo para Arranque) máximo por vista.
- Aplicar esos tamaños automáticamente al cargar cada FXML.
- Centrar la ventana al cambiar de vista.
- Mantener Arranque fijo y no redimensionable.
- Verificar con un test que el Editor se ve completo a 1000×620.

**Non-Goals:**
- No se rediseña el layout de las pantallas.
- No se añade persistencia de tamaño por vista (solo se guarda el tamaño al cerrar, la posición es lo más útil).
- No se cambian temas ni colores.

## Decisions

1. **`VentanaConfig` como fuente de verdad:** enum/clase que asocia cada FXML con su configuración (ancho, alto, minAncho, minAlto, maxAncho, maxAlto, redimensionable).
2. **Aplicación en `Navegador.mostrar(...)`:** justo después de `stage.setScene(scene)`, se consulta la configuración para el FXML y se ajusta el `Stage`.
3. **Diálogo `GenerarFacturasMensuales`:** se aplica la misma configuración al `Stage` del diálogo tras crearlo.
4. **Preferencias:** se mantienen `VENTANA_X`, `VENTANA_Y`, `VENTANA_W`, `VENTANA_H`. Al arrancar se restauran, pero `Navegador` aplicará la configuración de Arranque inmediatamente, por lo que el tamaño guardado solo será efectivo si la última vista cerrada fue Arranque; la posición sí se conserva.
5. **Centrado:** se usa `stage.centerOnScreen()` cada vez que se aplica una configuración con tamaño predefinido.
6. **Test de UI:** se carga el Editor en un `Stage` de 1000×760, se fuerza layout y se comprueba que los nodos principales tienen `visible` y `layoutBounds` dentro de la escena.

## Risks / Trade-offs

- El cambio automático de tamaño al navegar puede molestar si el usuario había agrandado la ventana a propósito; sin embargo, el requisito elegido por el usuario es exactamente ese.
- Si la resolución de pantalla es menor que el mínimo de una vista, parte de la ventana quedará fuera de la pantalla; es el comportamiento esperado al imponer un mínimo.
- El test de UI en modo headless puede necesitar `Platform.startup` y `applyCss/layout` para obtener métricas reales.

## Migration Plan

Ninguna. Cambios puros de UI.

## Open Questions

Ninguna.
