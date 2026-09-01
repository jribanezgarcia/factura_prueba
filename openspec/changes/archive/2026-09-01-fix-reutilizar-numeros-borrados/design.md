## Context

`NumeroService.siguienteCorrelativo(...)` actualmente ignora la tabla `numero_disponible`, que registra los correlativos liberados al borrar facturas físicamente. El método `huecosDisponibles(...)` ya existe y `proponerNumeros(...)` lo usa, pero la creación de facturas individuales no lo consulta.

## Goals / Non-Goals

**Goals:**
- Hacer que `siguienteCorrelativo` reutilice los huecos de `numero_disponible` antes de proponer un número nuevo.
- Añadir tests para la reutilización de números borrados.

**Non-Goals:**
- No se cambia la base de datos.
- No se modifica el comportamiento de reutilización de anulados.

## Decisions

1. **Huecos borrados tienen prioridad:** `siguienteCorrelativo` consultará `huecosDisponibles` y devolverá el menor correlativo libre.
2. **Fallback:** si no hay huecos borrados, se mantiene la lógica actual (reutilizar anulados si está activado, o continuar con el siguiente correlativo).
3. **Guardado limpia el hueco:** `FacturaService.crearFacturaSinTransaccion` ya llama a `numeroDisponibleRepository.eliminar(...)`, por lo que al guardar con un hueco éste desaparece de la tabla.

## Risks / Trade-offs

- Si una serie tiene huecos borrados y anulados, se rellenarán primero los borrados (ordenados de menor a mayor) antes que los anulados. Esto es consistente con aprovechar todos los números libres.

## Migration Plan

Ninguna.

## Open Questions

Ninguna.
