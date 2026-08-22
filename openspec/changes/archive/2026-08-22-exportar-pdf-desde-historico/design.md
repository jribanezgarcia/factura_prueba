## Context

El editor ya resuelve todo el ciclo de exportación individual: `FacturaService.abrirVersion(versionId)` devuelve el `VersionCompleta` que `PdfService.exportar(vc, empresa, ruta, color)` necesita; el nombre de archivo se propone desde la serie y el número (`proponerDestinoPdf` en `EditorController`), y se recuerdan la última carpeta de exportación y el color de acento como preferencias. El histórico (`HistoricoController`) tiene cada fila con `getVersionId()` disponible.

## Constraints

- Estilo MVC clásico del proyecto: lógica de pantalla en el controlador, servicios vía `Servicios`, diálogos con `Dialogos`.
- No duplicar la generación: reutilizar `PdfService` y las preferencias existentes tal cual.
- La generación no SHALL bloquear la interfaz (patrón `Task` + hilo, igual que el editor).

## Goals

- Un solo punto nuevo de lógica: `exportarSeleccionadas()` en `HistoricoController`, con dos rutas (individual / lote).
- Nombrado de archivo idéntico al del editor: `CODIGO-CORRELATIVO-MES.pdf` (barra por guion).

## Decisions

- **D1 — Selección múltiple siempre activa**: `SelectionMode.MULTIPLE` en la tabla. El doble clic sigue abriendo la factura; la selección múltiple no interfiere.
- **D2 — Ruta individual = mismo UX que el editor**: con exactamente una fila seleccionada se usa FileChooser con nombre propuesto y carpeta inicial recordada; al guardar se actualiza la preferencia de última carpeta.
- **D3 — Lote con DirectoryChooser único**: con dos o más filas se pide una carpeta una sola vez (inicializada con la última carpeta de exportación si existe) y se genera un PDF por fila dentro de esa carpeta.
- **D4 — Nombre propuesto compartido**: extraer el cálculo del nombre (`serie + "-" + numero + "-" + mes`, barra→guion) a un método estático pequeño reutilizable y testeable, usado por el editor y por el histórico. El editor conserva su propuesta de ruta completa; el histórico añade el nombre a la carpeta elegida.
- **D5 — Errores por fila en lote**: un fallo al generar una factura (p. ej. versión inexistente) no aborta el lote; se cuenta y se lista en el resumen final junto a los generados.
- **D6 — Sin cambios de servicio**: `abrirVersion` ya carga la versión completa; no se tocan servicios ni repositorios.

## Risks / Trade-offs

- Exportar muchas facturas a la vez puede tardar: se ejecuta en `Task` con mensaje de progreso y botón deshabilitado mientras genera.
- Dos versiones distintas pueden proponer el mismo nombre solo si coinciden serie/número/mes (caso raro: v1 y v2 misma factura); en lote, si el archivo ya existe se sobrescribe, coherente con el comportamiento actual del editor.
