## Why

La aplicación no permite reflejar retenciones de IRPF en las facturas. En ciertos sectores profesionales es obligatorio descontar una retención del total, calculada sobre la base imponible. Sin este soporte los totales y los PDF no reflejan la cantidad realmente adeudada, y las facturas no cumplen con los requisitos fiscales del usuario.

## What Changes

- Se añade una lista configurable de **tipos de retención** (porcentaje + nombre) en Configuración, gestionada como los tipos de IVA.
- El editor de factura permite seleccionar un tipo de retención por factura (o ninguno), usando los tipos configurados por la empresa.
- El cálculo de la retención se realiza sobre la **base bruta** (antes del descuento global) y se resta del total: `Total = Base − Descuento + IVA − Retención`.
- El resumen de la factura muestra la retención como una fila propia, junto a base, descuento, IVA y total.
- La retención se guarda en cada versión de factura y se muestra en el histórico (nueva columna) y en el PDF.
- Las **rectificativas** también permiten retención; al crear una rectificativa se copia el tipo de retención de la factura original, pudiendo modificarse.
- No se modifica el comportamiento de la serie R para rectificativas: el usuario sigue pudiendo crear la serie R manualmente si la necesita.

## Capabilities

### New Capabilities

Ninguna.

### Modified Capabilities

- `invoicing`: se modifican los requisitos de facturas, totales, configuración, histórico, exportación a PDF y rectificativas para incluir la retención de IRPF.

## Impact

- Esquema de base de datos: nueva tabla `tipo_retencion` y nuevos campos en `factura_version` para almacenar el tipo e importe de retención.
- `service/CalculoService`, `service/ResumenFactura` y modelos relacionados: cálculo del total con retención sobre base bruta.
- `ui/Editor.fxml` y `ui/EditorController`: selector de tipo de retención y visualización en el resumen.
- `ui/Historico.fxml` y `ui/HistoricoController`: columna de retención en la tabla.
- `pdf/PdfService`: fila de retención en los totales del PDF.
- `ui/Configuracion.fxml` y `ui/ConfiguracionController`: pestaña/selector para gestionar tipos de retención.
- Tests unitarios y de servicio para el cálculo con retención y su persistencia.
