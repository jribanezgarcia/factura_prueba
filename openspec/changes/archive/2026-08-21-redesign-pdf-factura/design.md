## Context

El diseño visual está cerrado por el usuario en `prototipos/pdf-final-clasica-tarjetas.html` (ALT 6 + tarjetas bicolor del ARENA 5 + logo ×2 + NIF empresa destacado + descripción en un solo estilo). Este cambio es sobre todo de presentación, pero introduce datos nuevos persistentes.

## Goals / Non-Goals

- Goals: PDF idéntico al prototipo; email de cliente persistente y con instantánea por versión; datos de pago opcionales por versión; color de acento configurable con derivación de tonos.
- Non-Goals: cambiar el cálculo del modelo de totales (el Total c/IVA por línea se calcula al pintar: `base × (1 + iva%)`); redondeos nuevos; exportación a otros formatos.

## Decisions

### D1. Esquema — migración `002_datos_factura_pdf.sql`
Una sola migración con `ALTER TABLE`:
- `cliente ADD COLUMN email TEXT NOT NULL DEFAULT ''`
- `factura_version ADD COLUMN cli_email TEXT NOT NULL DEFAULT ''`
- `factura_version ADD COLUMN forma_pago TEXT NOT NULL DEFAULT ''`
- `factura_version ADD COLUMN vencimiento TEXT` (nulo = sin vencimiento)
- `factura_version ADD COLUMN realizada_por TEXT NOT NULL DEFAULT ''`

El color NO va en tabla: preferencia `color_pdf` (clave/valor ya existente). `Migrations.SCRIPTS` añade la entrada; `user_version` se deduce de la posición.

### D2. Instantánea del email
Como los demás datos de cliente, el email se copia a la versión (`cli_email`) para que versiones antiguas conserven el dato de su momento. Fluye por `VersionadoService.crearVersion/sobrescribirVersion`, cuyas firmas crecen con `formaPago`, `vencimiento`, `realizadaPor`; call sites a actualizar: `FacturaService` (crearFactura/guardarEditada) y `EstadoService` (anular/restaurar reenvían lo que había).

### D3. Color y tonos derivados
`PdfService.COLOR_DEFECTO = "#B08D57"`. A partir del hex se derivan en tiempo de generación: tono oscuro (cabeceras/fila TOTAL), tono claro (~85 % blanco, fondos) y tono borde (~40 %). Si la preferencia no existe o es inválida → defecto. El selector en Configuración es un `ColorPicker` de JavaFX; se guarda hex `#RRGGBB`.

### D4. Logo al doble
El PDF escala el logo configurado ×2 en ancho y alto (posiciones X/Y se mantienen). El margen superior del documento se calcula según la altura efectiva del logo para que no invada contenido. Si algún logo queda demasiado grande, el usuario puede ajustarlo desde Configuración.

### D5. Pie legal repetido en recuadro
Se mantiene el patrón actual (`PdfPageEventHelper`): en cada página se dibuja el pie legal dentro de un recuadro con borde de color y fuente pequeña justificada, y debajo `Página X de Y`. Margen inferior reservado acorde.

### D6. Filas vacías de las tarjetas
«Facturar a» omite la fila de email si está vacío; «Datos de pago» omite cada fila vacía y, si están las tres vacías, muestra una sola línea atenuada con guion. Coincide con "normalmente estarán en blanco".

## Risks / Trade-offs

- Cambio de firmas públicas de servicios → toca varios call sites; mitigado compilando y pasando la suite.
- Sin tests previos de PDF: se añaden pruebas de humo con extracción de texto (OpenPDF `PdfTextExtractor`) verificando número, NIF empresa, tarjeta «Facturar a» y total con IVA.
- Bases de datos existentes: `ALTER TABLE ... DEFAULT ''` es seguro en SQLite y no requiere backfill.

## Migration Plan

Migración aditiva automática al arrancar (runner existente). No hay rollback necesario; las columnas nuevas son opcionales/con valor por defecto.

## Open Questions

- Ninguno. Diseño aprobado por el usuario el 21/08/2026.
