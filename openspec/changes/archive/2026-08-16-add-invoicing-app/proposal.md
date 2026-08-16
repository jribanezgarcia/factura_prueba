## Why

Actualmente el usuario crea sus facturas una a una en una hoja de cálculo de Excel. Ese proceso es manual, propenso a errores y no deja histórico, versionado ni búsqueda. Se quiere sustituir por una aplicación de escritorio para Windows, local y de un único usuario, que permita crear, editar, buscar y exportar facturas de forma rápida, manteniendo un histórico íntegro y versionado.

## What Changes

- **Nueva aplicación de escritorio** (Java 21 + JavaFX + Maven + SQLite/JDBC) que reemplaza la hoja de cálculo.
- **Gestión de clientes**: ficha con nombre/razón social, NIF, dirección, CP, localidad y provincia; búsqueda incremental al crear factura; borrado físico solo si no tiene facturas; inactividad para clientes con histórico.
- **Facturas normales** (series C/P): número `CODIGO-CORRELATIVO-MES` (p. ej. `C-59/7`), fecha editable, cliente, líneas con cantidad/descripción/precio/total, IVA por línea, descuento global, observaciones y totales.
- **Rectificativas** (serie R): creadas desde una factura existente, con datos copiados, referencia a la factura rectificada (auto y editable), parciales o totales.
- **Precios**: importes netos (sin IVA) como modo normal; el editor permite introducir el total final con IVA y la aplicación calcula hacia atrás base e IVA. IVA predeterminado 21%.
- **Descuento global** porcentual (entero, 0% por defecto) aplicado antes del IVA, con reparto proporcional entre las bases de cada tipo de IVA para el desglose fiscal.
- **Versionado**: cada guardado de cambios crea una versión (v1, v2, ...); anular y restaurar también crean versión; las versiones anteriores nunca se modifican.
- **Estados**: Emitida (editable) y Anulada (consulta, PDF, restauración con confirmación; bloqueada si el número está ocupado por una activa).
- **Histórico**: fila por versión, búsqueda por serie, cliente, NIF, fechas, importes y estado; filtros combinables.
- **Configuración**: empresa (datos, cabecera texto/logo, pie legal libre), tipos de IVA, series (siguiente número editable, reutilización de anulados configurable) y carpetas de PDF.
- **Exportación a PDF**: A4, cabecera y pie repetidos en todas las páginas, `Página X de Y`, formato español, marca `ANULADA` en anuladas, carpeta `Facturas/AAAA/SERIE/` y archivo `C-59-7_v1.pdf`.
- **Copia de seguridad manual** del archivo SQLite.

## Capabilities

### New Capabilities

- `invoicing`: Capacidad raíz de la aplicación de facturación. Cubre clientes, facturas normales y rectificativas, líneas y cálculos (precios, IVA, descuento), numeración por series, versionado y estados, histórico, configuración, exportación a PDF y copia de seguridad.

### Modified Capabilities

- Sin capacidades existentes: es un proyecto greenfield sin `openspec/specs/` previo.

## Impact

- **Código nuevo**: aplicación completa desde cero en este repositorio (no hay código existente en `Factura_prueba`). El proyecto previo `factualcazaba` queda descartado.
- **Dependencias**: Java 21 LTS, JavaFX (Maven), driver SQLite JDBC, librería PDF (se decide en design).
- **Datos**: base de datos SQLite local en carpeta de datos de la aplicación, separada de la instalación.
- **Fuera de alcance (V1)**: importación de facturas históricas desde Excel, catálogo de productos, exportación a Excel/CSV, instalador `.exe`/versión portable, estado Borrador, tracking de saldo rectificado, multi-usuario.
