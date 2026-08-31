## Context

El diagnostic del layout del Menú principal (`MenuPrincipalTamanoDiagnosticoTest`) muestra que el contenido necesita al menos 589 px de alto, pero la configuración fija el tamaño en 520 px, por lo que parte del contenido queda cortado.

## Goals / Non-Goals

**Goals:**
- Aumentar la altura del Menú principal a 600 px (mínimo y predefinido).
- Actualizar la spec.

**Non-Goals:**
- No se modifica el layout interno del menú.
- No se cambian otros tamaños de ventana.

## Decisions

1. **Altura 600 px:** redondea por encima del mínimo real medido (589 px) y deja un pequeño margen.
2. **Mantener ancho 760 px:** el ancho es suficiente según el diagnóstico.

## Risks / Trade-offs

Ninguno relevante.

## Migration Plan

Ninguna.

## Open Questions

Ninguna.
