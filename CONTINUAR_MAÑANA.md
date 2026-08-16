# Continuación — estado al 12/08/2026

## Estado OpenSpec

- `add-invoicing-app`: implementación y revisión manual completadas (12.1, 12.2, 12.4 y 12.5).
  Falta únicamente la tarea **12.3**, validación formal y archivo del cambio; no archivar todavía.
- `add-spanish-tax-id-validation`: validador e integración implementados. Quedan pendientes las
  pruebas UI automatizadas (2.3); las pruebas unitarias y la comprobación manual están confirmadas.

## Corregido y confirmado hoy

1. **Histórico vacío con facturas existentes**: los límites de importe vacíos se convertían a `0 €`,
   ocultando cualquier factura positiva. Ahora un importe vacío no aplica límite y `C-5/8` aparece
   correctamente al buscar.
2. **Cliente no borrable `75238360a`**: se comprobó directamente en SQLite que corresponde a
   `cristina flores checa` y tiene la factura emitida `C-5/8`, versión 1. El bloqueo de borrado era
   correcto; el problema era el filtro del Histórico.
3. **Validación documental**: añadida `DocumentoFiscalValidator` para DNI, NIE y NIF/CIF español.
   El campo NIF admite estar vacío; si no lo está, se valida al pulsar Enter, al abandonar el campo y
   antes de guardar una factura o ficha de cliente. Los valores inválidos se marcan en rojo, muestran
   aviso y no se guardan. Ejemplo: `75238360A` es inválido; la letra correcta es `R`.
4. **Selector de cliente**: al pulsar la flecha muestra los clientes activos; al escribir continúa
   filtrando de forma incremental por nombre o NIF.
5. **Foco en líneas**: Descripción → Enter → Precio de la misma línea permanece corregido y confirmado.
   Las trazas de diagnóstico se han desactivado (`DIAGNOSTICO_FOCO = false`).

## Verificación actual

- Suite Maven: **26/26 tests en verde**.
- El usuario confirmó las pruebas manuales de la aplicación, incluida la corrección del Histórico,
  validación de NIF y selector de clientes.
- Maven cacheado (no está en PATH):
  `C:\Users\usuario\.m2\wrapper\dists\apache-maven-3.9.9-bin\33b4b2b4\apache-maven-3.9.9\bin\mvn.cmd`

## Próximos pasos

1. Añadir pruebas UI automatizadas para el aviso y bloqueo de NIF inválido (tarea 2.3 de
   `add-spanish-tax-id-validation`).
2. Ejecutar la validación formal de OpenSpec de ambos cambios y, solo con confirmación explícita,
   archivarlos.
3. Continuar con la mejora de diseño de la UI y del PDF, prioridad indicada por el usuario.

## Decisiones de producto ya acordadas (no re-preguntar)

- Número mostrado con barra y guion entre código-correlativo (`C-59/7`, `R-1`); nombre de archivo
  PDF con guion (`C-59-7_v1.pdf`).
- El correlativo es la identidad; el mes deriva de la fecha. Al cambiar la fecha en una factura
  guardada, el número se recalcula con el mes nuevo y se guarda como snapshot.
- Modo normal de precios = importes netos (sin IVA); se permite entrada de total con IVA
  (base = total/(1+tipo)).
- Descuento global entero 0–100 (default 0%), aplicado antes del IVA, repartido proporcionalmente
  entre bases con ajuste de céntimos en la mayor base.
- Versionado (CAMBIADO por el usuario): al editar la **última** versión, Guardar pide confirmación y
  **sobrescribe esa versión** (mismo número de versión); al editar una **versión anterior** se crea vN+1
  y las versiones anteriores a la más reciente nunca se modifican. Anular/restaurar también crean versión.
- Numeración: continuar hacia delante por defecto (configurable por serie); restauración bloqueada
  si el correlativo está ocupado por una activa.
- Pie legal libre y configurable, sin contenido obligatorio.
- Número editable en creación (parsear correlativo escrito manualmente); en ediciones posteriores
  el correlativo queda fijo.
- Series iniciales C (Cocinas), P (Puertas), R (Rectificativas); IVA 21%, 10%, Exento; empresa id=1.
