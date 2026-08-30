## Context

El formato de numeración actual está hardcodeado en \NumeroService.formarNumero()\: siempre \{codigo}-{correlativo}/{mes}\ para series normales y \{codigo}-{correlativo}\ para rectificativas. El correlativo se almacena como entero en la tabla \actura\, y el número completo se forma al vuelo. La tabla \serie\ tiene las columnas: id, codigo, descripcion, es_rectificativa, siguiente_correlativo, reutilizar_anulados.

## Goals / Non-Goals

**Goals:**
- Permitir que el campo \codigo\ de una serie esté vacío (sin prefijo de letra).
- Añadir un campo \sufijo_fecha\ a la tabla \serie\ con tres opciones: MES (default), ANIO, NINGUNO.
- Modificar \NumeroService\ para generar y parsear números según el formato configurado.
- Añadir un ComboBox en Configuración → Series para elegir el formato, con ejemplo vivo.
- Mantener compatibilidad total con el formato actual (MES).

**Non-Goals:**
- No se implementa un sistema de patrones libre con placeholders.
- No se permite configurar separadores (guion, barra, etc.) de forma independiente.
- No se cambia el formato de las series rectificativas (siguen sin sufijo de fecha).

## Decisions

### 1. Campo sufijo_fecha como enum en la BD

**Decisión**: Añadir columna \sufijo_fecha TEXT DEFAULT 'MES'\ a la tabla \serie\.

**Alternativas consideradas**:
- Patrón libre con placeholders (\{correlativo}-{anio}\): más flexible pero complejo de parsear y validar.
- Solo dos opciones (MES y ANIO): no cubre el caso de solo número (\56\).

**Razón**: El enum cubre los tres casos reales sin complejidad innecesaria. El parsing es trivial porque se conoce el formato exacto.

### 2. Parsing de correlativo según sufijo

**Decisión**: \parseCorrelativo()\ usa un switch sobre \sufijo_fecha\ para extraer el correlativo.

- MES: busca \/\ y toma lo que hay antes.
- ANIO: busca \-\ después del código (si existe) y toma lo que hay antes.
- NINGUNO: si hay código, busca \-\ y toma lo que hay antes; si no hay código, intenta parsear todo como entero.

**Alternativa**: Usar regex dinámico construido desde el formato. Más complejo y propenso a errores.

**Razón**: Con solo 3 formatos, un switch es más legible y fácil de testear.

### 3. Ejemplo vivo en la UI

**Decisión**: Debajo del ComboBox de formato, mostrar un Label con el ejemplo \56-2026\ (o el equivalente del formato seleccionado) que se actualiza al cambiar la selección.

**Razón**: El usuario ve exactamente cómo quedará su número sin tener que imaginarlo. Es una mudança pequeña en la UI con gran impacto en usabilidad.

### 4. Migración de series existentes

**Decisión**: Las series existentes mantienen su \codigo\ y se les asigna \sufijo_fecha = 'MES'\ por defecto (comportamiento actual).

**Razón**: No se rompe nada. El usuario puede cambiar el formato después si lo necesita.

## Risks / Trade-offs

- **Riesgo**: Un usuario configura una serie con formato ANIO y luego cambia de opinión a mitad de año. **Mitigación**: Se podría permitir cambiar el formato, pero habría que considerar si las facturas existentes de esa serie se ven afectadas. Por ahora, el cambio de formato solo afecta a facturas nuevas.

- **Riesgo**: El parseCorrelativo() con código vacío podría ser ambiguo si el usuario escribe un número que no se ajusta al formato. **Mitigación**: Mensaje de error claro indicando el formato esperado.

- **Trade-off**: Se pierde flexibilidad (no se puede configurar separadores libremente) a cambio de simplicidad. Esto es aceptable para la V1.
