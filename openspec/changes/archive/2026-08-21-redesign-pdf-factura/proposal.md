## Why

El PDF actual es funcional pero no se parece al documento Excel original que usa la empresa. El usuario ha aprobado un prototipo HTML (`prototipos/pdf-final-clasica-tarjetas.html`, base ALT 6 + ajustes) que replica la hoja de cálculo: logo grande, NIF de empresa destacado, dos tarjetas bicolor (cliente / datos de pago), tabla de líneas estilo hoja de cálculo con total por línea incluyendo IVA, fila TOTAL en color y RGPD en recuadro. Además faltan datos que la empresa necesita: email del cliente y datos de pago opcionales (forma de pago, vencimiento, realizada por). El color de acento será configurable desde Configuración con tono arena Alcazaba por defecto.

## What Changes

- **PDF rediseñado** siguiendo el prototipo aprobado:
  - Cabecera: logo al doble del tamaño configurado, datos de empresa con NIF destacado en línea propia; a la derecha FACTURA + Serie/Nº + fecha.
  - Dos tarjetas bicolor: «FACTURAR A» (nombre, NIF, dirección, población, email si existe) y «DATOS DE PAGO» (forma de pago, vencimiento, realizada por — filas solo cuando están rellenas).
  - Tabla de líneas con celdas bordeadas: Cant / Descripción / Precio / IVA % / Total (con IVA incluido calculable como base × (1 + IVA%); exentas sin cambio).
  - Descripción siempre en un único estilo (sin línea secundaria diferenciada).
  - Resumen de totales a la derecha con desglose por tipo de IVA, descuento global si aplica y fila TOTAL con fondo de color.
  - Observaciones en caja clara; pie legal (RGPD configurable) repetido en todas las páginas dentro de un recuadro con borde de color; `Página X de Y`.
- **Nuevos datos**: campo email en la ficha de cliente (y su instantánea en cada versión de factura); campos opcionales forma de pago, vencimiento y realizada por en los datos generales de la factura.
- **Color configurable**: preferencia nueva `color_pdf` editable con selector de color en Configuración; valor por defecto arena `#B08D57`; el marrón de cabeceras y el resto de tonos se derivan programáticamente de ese color.

## Impact

- **Affected specs**: `invoicing` (requisitos Exportación a PDF, Configuración, Búsqueda de clientes al crear factura, Facturas normales).
- **Affected code**: migración `002_*` (columnas nuevas en `cliente` y `factura_version`), modelos `Cliente`/`FacturaVersion`, repositorios de clientes/versiones, `VersionadoService`, `FacturaService`, `EstadoService` (paso de nuevos campos en anular/restaurar), `Editor.fxml`+controller, ficha de clientes, `PdfService` (reescritura del diseño), `Configuracion.fxml`+controller, tests nuevos de servicio y de PDF.
