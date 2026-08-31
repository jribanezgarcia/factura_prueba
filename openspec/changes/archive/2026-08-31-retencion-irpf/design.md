## Context

La aplicación calcula totales en `CalculoService` y almacena cada versión de factura en `factura_version`. Los tipos de IVA ya se gestionan con una tabla `tipo_iva`, un repositorio y una pestaña en Configuración. El resumen de totales (`ResumenFactura`) y la exportación a PDF (`PdfService`) ya desglosan base, descuento e IVA. Ver `proposal.md` para la motivación de añadir retención de IRPF.

## Goals / Non-Goals

**Goals:**
- Permitir configurar tipos de retención por empresa (nombre + porcentaje), gestionados como los tipos de IVA.
- Seleccionar un tipo de retención por factura (o ninguno) desde el editor.
- Calcular la retención sobre la base bruta y reflejarla en el total: `Total = Base − Descuento + IVA − Retención`.
- Mostrar la retención en el histórico (columna) y en el PDF (fila de totales).
- Copiar la retención al crear rectificativas, permitiendo modificarla antes de guardar.

**Non-Goals:**
- No se modifica el comportamiento de series para rectificativas (la serie R sigue siendo responsabilidad del usuario).
- No se añade soporte para múltiples retenciones en una misma factura.
- No se generan modelos 111 ni otros informes fiscales; solo se refleja la retención en el documento de factura.

## Decisions

1. **Tabla `tipo_retencion` análoga a `tipo_iva`**
   - Campos: `id`, `nombre`, `porcentaje`, `activo`.
   - Gestión en Configuración con alta/baja lógica (inactivar si se ha usado).
   - Rationale: reutiliza el patrón existente de `tipo_iva`, manteniendo consistencia en la UI y en el modelo de datos.

2. **Campos en `factura_version`**
   - `tipo_retencion_id INTEGER REFERENCES tipo_retencion(id)` (nullable).
   - `importe_retencion NUMERIC` (nullable o 0).
   - Rationale: la retención forma parte de la versión de la factura, igual que el descuento, el IVA y los totales. Guardar el importe evita recalcularlo al consultar versiones antiguas si el porcentaje cambia.

3. **Cálculo sobre base bruta**
   - `retencion = baseBruta * porcentaje / 100`.
   - `total = baseDescontada + iva - retencion`.
   - Rationale: decisión de negocio cerrada por el usuario. La base bruta se usa porque la retención es un anticipo fiscal sobre el importe íntegro.

4. **Extensión de `ResumenFactura` y `CalculoService`**
   - Añadir `importeRetencion` y `tipoRetencionId` al resumen.
   - El total se ajusta en `CalculoService.resumen(...)`.
   - Rationale: centraliza el cálculo y mantiene el cuadre de totales en un único punto.

5. **Selector de retención en el editor**
   - ComboBox junto al descuento/IVA con opción "Sin retención" (null) + tipos activos.
   - Al cambiar el tipo se recalcula el resumen en caliente.
   - Rationale: experiencia coherente con el selector de cliente/tipo de IVA y feedback inmediato de totales.

6. **PDF: fila de retención antes del total**
   - Formato: `Retención X% −Y,00 €` (en rojo suave, como el descuento).
   - Rationale: visualmente queda claro que reduce el total y mantiene la coherencia con el descuento.

7. **Rectificativas**
   - `FacturaService.crearRectificativa(...)` copia `tipoRetencionId` de la versión origen.
   - El usuario puede cambiarla en el editor antes de guardar.
   - Rationale: la rectificativa debe reflejar la misma naturaleza fiscal que la factura original, pero permitir correcciones.

## Risks / Trade-offs

- **[Risk]** Los tests existentes de totales fallarán si esperan el cálculo antiguo sin retención.  
  → Mitigation: actualizar tests y añadir casos nuevos con retención; mantener el comportamiento actual cuando `tipo_retencion_id` es null.

- **[Risk]** Cambiar el porcentaje de un tipo de retención después de usarlo no actualiza facturas antiguas.  
  → Mitigation: se guarda el `importe_retencion` en cada versión, por lo que el histórico/PDF conservan el importe original.

- **[Risk]** Redondeo acumulado en facturas con varios tipos de IVA y retención.  
  → Mitigation: usar `BigDecimal` con `HALF_UP`, como el resto de cálculos; el importe de retención se calcula sobre la base bruta total, no por grupos de IVA.

- **[Risk]** La columna de retención en el histórico reduce el ancho disponible para otras columnas a 800x600.  
  → Mitigation: hacer la columna estrecha y ajustar anchos; si es necesario, ocultar o truncar etiquetas largas.

## Migration Plan

1. Añadir migración SQL `005_retencion_irpf.sql`:
   - `CREATE TABLE tipo_retencion (...)`.
   - `ALTER TABLE factura_version ADD COLUMN tipo_retencion_id INTEGER REFERENCES tipo_retencion(id)`.
   - `ALTER TABLE factura_version ADD COLUMN importe_retencion NUMERIC`.
2. Registrar la migración en `Migrations.java`.
3. Al arrancar, `Database.getConnection()` ejecuta la migración como de costumbre.
4. No requiere migración de datos: las facturas existentes tendrán `tipo_retencion_id` null e `importe_retencion` null/0.

## Open Questions

Ninguna.
