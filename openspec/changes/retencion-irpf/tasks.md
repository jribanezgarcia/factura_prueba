## 1. Esquema de datos y modelo

- [x] 1.1 Crear migración `005_retencion_irpf.sql` con tabla `tipo_retencion` y columnas `tipo_retencion_id` e `importe_retencion` en `factura_version`; crear `006_retencion_irpf_snapshot.sql` con `tipo_retencion_nombre` y `tipo_retencion_porcentaje`; registrar ambas en `Migrations.java`.
- [x] 1.2 Crear modelo `TipoRetencion` (id, nombre, porcentaje, activo) y repositorio `TipoRetencionRepository` (listar activos/todos, insertar, actualizar, inactivar, enUso).
- [x] 1.3 Ampliar `FacturaVersion` con `tipoRetencionId`, `tipoRetencionNombre`, `tipoRetencionPorcentaje` e `importeRetencion`; actualizar `VersionRepository` para leer/escribir los nuevos campos.

## 2. Cálculo de totales

- [x] 2.1 Ampliar `ResumenFactura` con `importeRetencion`, `nombreRetencion` y `porcentajeRetencion`.
- [x] 2.2 Actualizar `CalculoService.resumen(...)` para calcular la retención sobre la base bruta y ajustar el total a `Base − Descuento + IVA − Retención`.

## 3. Editor de factura

- [x] 3.1 Añadir ComboBox de retención en `Editor.fxml` junto al descuento y enlazarlo en `EditorController`.
- [x] 3.2 Al cambiar el tipo de retención, recalcular el resumen en caliente.
- [x] 3.3 Guardar `tipoRetencionId`, `tipoRetencionNombre`, `tipoRetencionPorcentaje` e `importeRetencion` al crear/editar una factura.

## 4. Rectificativas

- [x] 4.1 Modificar `RectificativaService.crearRectificativa(...)` para copiar el tipo de retención de la factura origen usando el snapshot almacenado si el maestro fue eliminado.
- [x] 4.2 Permitir modificar/quitar la retención en la rectificativa antes de guardar desde el editor.

## 5. Histórico

- [x] 5.1 Añadir columna "Retención" en `Historico.fxml` y en el modelo de fila `HistorialFila`.
- [x] 5.2 Poblar la columna desde `HistorialRepository`.

## 6. Exportación a PDF

- [x] 6.1 Añadir fila de retención en el resumen del PDF (`PdfService`) antes del total, con formato `Retención X% −Y,00 €`.
- [x] 6.2 Confirmar que el total del PDF respeta la fórmula con retención.

## 7. Configuración

- [x] 7.1 Añadir pestaña "Retenciones" en `Configuracion.fxml` con alta/baja lógica similar a IVA.
- [x] 7.2 Implementar handlers en `ConfiguracionController` usando `TipoRetencionRepository`.

## 8. Verificación final

- [x] 8.1 Ejecutar la suite completa y confirmar que todos los tests pasan (86 tests).
- [x] 8.2 Verificación manual con el usuario: crear facturas con y sin retención, generar PDF, comprobar histórico y rectificativas.
