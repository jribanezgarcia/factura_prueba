## 1. Esquema de datos y modelo

- [ ] 1.1 Crear migración `005_retencion_irpf.sql` con tabla `tipo_retencion` y columnas `tipo_retencion_id` e `importe_retencion` en `factura_version`; registrarla en `Migrations.java`. Verificar con `openspec validate --specs`.
- [ ] 1.2 Crear modelo `TipoRetencion` (id, nombre, porcentaje, activo) y repositorio `TipoRetencionRepository` (listar activos, insertar, actualizar, inactivar). Verificar con un test de repositorio.
- [ ] 1.3 Ampliar `FacturaVersion` con `tipoRetencionId` e `importeRetencion` y actualizar `VersionRepository` para leer/escribir los nuevos campos. Verificar con un test de persistencia.

## 2. Cálculo de totales

- [ ] 2.1 Ampliar `ResumenFactura` con `importeRetencion` y `tipoRetencionId`. Verificar compilación y tests existentes.
- [ ] 2.2 Actualizar `CalculoService.resumen(...)` para calcular la retención sobre la base bruta y ajustar el total a `Base − Descuento + IVA − Retención`. Verificar con tests para: sin retención, retención 15% sin descuento, retención 15% con descuento 10%.

## 3. Editor de factura

- [ ] 3.1 Añadir ComboBox de retención en `Editor.fxml` junto al descuento/IVA y enlazarlo en `EditorController`. Verificar en `UiSmokeTest` que la vista carga sin errores.
- [ ] 3.2 Al cambiar el tipo de retención, recalcular el resumen en caliente. Verificar manualmente que el total cambia al seleccionar/quitar retención.
- [ ] 3.3 Guardar `tipoRetencionId` e `importeRetencion` al crear/editar una factura. Verificar con un test de servicio que una factura guardada conserva la retención.

## 4. Rectificativas

- [ ] 4.1 Modificar `FacturaService.crearRectificativa(...)` para copiar `tipoRetencionId` de la factura origen. Verificar con un test de servicio.
- [ ] 4.2 Permitir modificar/quitar la retención en la rectificativa antes de guardar. Verificar manualmente creando una rectificativa desde una factura con retención.

## 5. Histórico

- [ ] 5.1 Añadir columna "Retención" en `Historico.fxml` y en el modelo de fila `HistorialFila`. Verificar en `UiSmokeTest` a 800x600.
- [ ] 5.2 Poblar la columna desde `HistorialService`/`HistorialRepository`. Verificar con un test de servicio que facturas con y sin retención muestran el importe correcto.

## 6. Exportación a PDF

- [ ] 6.1 Añadir fila de retención en el resumen del PDF (`PdfService`) antes del total, con formato `Retención X% −Y,00 €`. Verificar con un test que el texto aparece en el PDF generado.
- [ ] 6.2 Confirmar que el total del PDF respeta la fórmula con retención. Verificar con un test de extracción de texto del PDF.

## 7. Configuración

- [ ] 7.1 Añadir pestaña/sección de "Tipos de retención" en `Configuracion.fxml` con alta/baja lógica similar a IVA. Verificar en `UiSmokeTest` a 800x600.
- [ ] 7.2 Implementar handlers en `ConfiguracionController` usando `TipoRetencionRepository`. Verificar manualmente que se pueden crear, listar e inactivar tipos de retención.

## 8. Verificación final

- [ ] 8.1 Ejecutar la suite completa `& "C:\Program Files\Apache NetBeans\java\maven\bin\mvn.cmd" test` desde el directorio del proyecto y confirmar que todos los tests pasan.
- [ ] 8.2 Verificación manual con el usuario: crear facturas con y sin retención, generar PDF, comprobar histórico y rectificativas.
