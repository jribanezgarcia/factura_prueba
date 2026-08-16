## Context

Greenfield: no existe código en `Factura_prueba`. El proyecto previo `factualcazaba` queda descartado. La motivación y el alcance están en proposal.md; los requisitos de comportamiento en specs/invoicing/spec.md. Stack decidido: Java 21 LTS, JavaFX con FXML, Maven, SQLite + JDBC. Restricciones: escritorio Windows, un único usuario, un único ordenador, una sola instancia, idioma español, moneda euro, sin frameworks pesados (ni Spring Boot ni Hibernate/JPA).

La factura real de referencia (Excel) aporta el punto de partida del layout del PDF (cabecera con logo, bloque de cliente, tabla de líneas, bloque BASE/IVA/DESCUENTO/TOTAL, pie legal largo) y confirma que los importes de línea se tratan como base antes de IVA.

## Goals / Non-Goals

**Goals:**
- Arquitectura MVC con capas Service/Repository, sencilla y mantenible para una app pequeña.
- Modelo de datos que garantiza integridad histórica: snapshots de datos de cliente, IVA y número por versión; las versiones anteriores a la más reciente son inmutables, y la versión actual puede sobrescribirse al editar la factura.
- Cálculo monetario centralizado y auditable (BigDecimal, redondeo HALF_UP, descuento global con reparto proporcional entre bases de IVA).
- Numeración robusta: correlativo como identidad, mes derivado de fecha, consumo solo al guardar.
- Una base de datos SQLite en carpeta de datos separada de la instalación, con transacciones correctas.

**Non-Goals:**
- No hay migración de datos desde Excel en V1 (contadores de series se configuran manualmente en Configuración).
- No se diseña para multi-usuario ni concurrencia externa; el único guardián de concurrencia es la instancia única.
- No se empaqueta el instalador `.exe` ni la versión portable en esta fase.

## Decisions

### Stack y estructura de módulos

Paquete base `com.alcazaba.facturacion` con subpaquetes: `ui` (FXML + controllers), `service`, `repository`, `model`, `db`, `pdf`, `util`.

**Decisión**: JavaFX con FXML + Controladores; la lógica de negocio vive en Services, no en controllers. Repositories son los únicos que tocan JDBC. Sin framework de DI: las dependencias se inyectan a mano por constructor (suficiente y sin magia para este tamaño).

### Modelo de datos (SQLite)

```
cliente (id, nombre, nif, direccion, cp, localidad, provincia, activo)
serie (id, codigo, descripcion, es_rectificativa, siguiente_correlativo,
       reutilizar_anulados)
tipo_iva (id, nombre, porcentaje, motivo_exencion, activo)   -- porcentaje NULL = exento
factura (id, serie_id, correlativo, cliente_id)              -- identidad, sin estado
factura_version (id, factura_id, version_num, numero, fecha_factura, fecha_guardado,
                 estado, descuento_porcentaje, observaciones, referencia_rectifica,
                 cli_nombre, cli_nif, cli_direccion, cli_cp, cli_localidad,
                 cli_provincia, base_total, iva_total, total)
factura_linea (id, factura_version_id, orden, cantidad, descripcion, precio_unitario,
               total_base, tipo_iva_id, iva_nombre, iva_porcentaje,
               iva_motivo_exencion, iva_importe)
empresa (id=1, nombre, nif, direccion, cp, localidad, provincia, actividad, email,
         telefono, cabecera_modo, logo_path, logo_x, logo_y, logo_ancho,
         logo_alto, pie_legal)
preferencias (clave, valor)    -- ultima_serie, ventana_*, ultima_carpeta_export
```

**Decisiones clave:**
- **Snapshot por versión**: cada `factura_version` guarda copia del número, fecha, estado y datos de cliente (columnas `cli_*`); cada `factura_linea` guarda copia del tipo de IVA (`iva_*`). Así, editar la ficha del cliente o un tipo de IVA nunca corrompe el histórico. La referencia `cliente_id`/`tipo_iva_id` solo sirve para actualizaciones del maestro.
- **Correlativo como entero** en `factura` para ordenar/filtrar sin parsear cadenas. El `numero` completo (p. ej. `C-59/8`, `R-1`) se calcula y se persiste como snapshot en cada versión.
- **Estado vivo en la última versión**: el estado "actual" de una factura es el de su versión de mayor `version_num`. No hay columna de estado redundante en `factura`.
- **Edición de la versión actual**: al editar la factura con la última versión abierta, `FacturaService.guardarEditada` sobrescribe esa versión en su lugar (update del snapshot + borrado y re-inserción de líneas) tras confirmación del usuario; si se edita una versión anterior, se crea vN+1 sin tocar la histórica.
- **Fechas**: `fecha_factura` como texto ISO `yyyy-MM-dd`; `fecha_guardado` como texto ISO `yyyy-MM-dd HH:mm:ss`. El mes del número se extrae de `fecha_factura`.
- **Migraciones**: `PRAGMA user_version` + runner de scripts SQL ordenados; cada versión de esquema es un archivo idempotente bajo `db/migrations`.

**Alternativa considerada**: guardar solo referencias (cliente_id, tipo_iva_id) en versiones. Descartada porque rompe el histórico al editar maestro o tipo de IVA, contradiciendo el requisito de versiones inmutables.

### Numeración

- El **correlativo** se asigna en creación: si la serie usa `siguiente_correlativo`, se propone ese valor; si `reutilizar_anulados = true`, se propone el menor correlativo libre entre anuladas.
- El **mes** se deriva siempre de `fecha_factura`: al cambiar la fecha y guardar, se genera una versión con el mismo correlativo y el mes nuevo.
- El **número completo** se forma como `CODIGO/CORRELATIVO[/MES]` (las rectificativas no llevan mes). El campo número es editable en creación (el usuario puede sobrescribir el correlativo); en ediciones posteriores el correlativo queda fijo y solo el mes sigue a la fecha.
- **Validación de unicidad**: al guardar, ningún correlativo de la misma serie puede pertenecer a una factura en estado Emitida (activa). La comprobación se hace dentro de la misma transacción que inserta.
- **Consumo**: el correlativo se consume solo si el commit tiene éxito. Guardado = una transacción que inserta `factura` (si es nueva) + `factura_version` + sus líneas y, al final, actualiza `serie.siguiente_correlativo`. Un fallo revierte todo y no consume el número.

### Cálculos (servicio central `CalculoService`)

- `BigDecimal`, `RoundingMode.HALF_UP`. Precio unitario conserva la precisión introducida (hasta 4 decimales); el total de línea se muestra/redondea a 2 decimales.
- **Recálculo precio ↔ total**:
  - Modifica precio → `total = round2(cantidad × precio)`; IVA de línea = `round2(total × tasa)`.
  - Modifica total → `precio = total / cantidad` (precisión interna, sin redondeo a 2 hasta mostrarse).
  - Entrada "con IVA": `base = round2(totalConIVA / (1 + tasa))`; `iva = totalConIVA − base`; `precio = base / cantidad`.
- **Descuento global** (`d` entero, 0–100): se agrupan las bases por tipo de IVA (`base_g`). `baseDescontada_g = round2(base_g × (100−d)/100)`; ajuste de céntimos en la mayor base descontada para que `Σ baseDescontada_g == round2(Σ base_g × (100−d)/100)`. `iva_g = round2(baseDescontada_g × tasa_g)`. Totales: `total = Σ baseDescontada_g + Σ iva_g`.
- Exento: `tasa = 0` y `motivo_exencion` se muestra en el desglose.

### Transacciones y acceso a SQLite

- Una única conexión JDBC compartida (single user, single instance), `autoCommit = false`; los métodos de Service marcan commit/rollback explícitos. `ConnectionFactory` abre la BD en la carpeta de datos.
- **Carpeta de datos**: `%APPDATA%/Facturacion` (creada al arrancar), con `facturas.db`, subcarpeta `Facturas/` para PDFs y fichero de lock. Separada de la instalación para permitir actualizaciones futuras sin tocar datos.
- **Instancia única**: `FileChannel.tryLock` sobre `facturas.lock` en la carpeta de datos; si no se obtiene el lock, se muestra un aviso y la aplicación termina.

### PDF

- Librería recomendada: **OpenPDF** (`com.github.librepdf:openpdf`). Alternativa considerada: PDFBox (más bajo nivel, más código para tablas y repetición de cabecera/pie). OpenPDF ofrece tablas, celdas con ajuste de texto y eventos de página.
- `PdfService` construye el documento a partir de una `factura_version` concreta y de la configuración actual de empresa/logo/cabecera/pie.
- Cabecera y pie repetidos mediante `PdfPageEventHelper`; contador `Página X de Y` con el total de páginas (evento `onEndPage` + paso final).
- Descripciones largas: celdas con `Paragraph` ajustado automáticamente.
- Formato español: `DecimalFormat` con símbolos es-ES para `1.250,50 €` y `DateTimeFormatter` es-ES para `11/08/2026`.
- Estado Anulada: marca `ANULADA` en diagonal/destacada en todas las páginas.
- Estructura: `Facturas/AAAA/SERIE/` y archivo `CODIGO-CORRELATIVO-MES_vN.pdf`, sustituyendo `/` por `-` en el nombre de archivo (la barra no es legal en nombres de archivo Windows).

### UI (FXML)

Vistas: Menú principal, Editor de factura, Histórico, Clientes, Configuración (pestañas Empresa/Cabecera/Pie/IVA/Series/PDFs), Versiones, Backup.

- Editor de líneas con `TableView` editable que implementa el flujo de teclado Enter (cantidad → descripción → precio/total → siguiente línea) y Supr para borrar.
- Los Services se invocan desde controllers; los controllers no contienen lógica de negocio (solo orquestación y estados de la UI).
- Diálogo de cambios sin guardar (Guardar / Descartar / Cancelar) en Volver y en cierre de ventana.
- Preferencias (última serie, tamaño/posición de ventana, última carpeta de exportación) persistidas en la tabla `preferencias`.

### Backup

- `VACUUM INTO 'ruta'` a través del JDBC para obtener una copia consistente del SQLite en caliente, en un archivo con timestamp (`facturas_AAAAMMDD_HHMMSS.db`). Alternativa considerada: copia física del archivo (descartada por posible incoherencia con la conexión abierta).

## Risks / Trade-offs

- [Redondeos distribuidos (descuento + varios IVA) pueden descuadrar céntimos] → Todo el cálculo vive en `CalculoService` con ajuste explícito de céntimos y cobertura de tests unitarios.
- [Editar la fecha de una factura emitida cambia el mes del número] → Comportamiento acordado y especificado; el correlativo nunca cambia, por lo que no hay colisión real.
- [Snapshot por versión duplica datos y crece la BD] → Aceptable para un único usuario; índices sobre `factura_id` y `version_num`.
- [Pie legal muy largo repetido en cada página puede hacer PDFs pesados] → Aceptado (requisito); se paginan con OpenPDF.
- [Derivar base desde total con IVA puede producir céntimos de diferencia] → Aceptado para V1; la base se redondea y el IVA es el resto (suma exacta).
- [Operaciones JDBC largas en el hilo FX congelarían la UI] → La BD local es rápida; para V1 se ejecutan en el hilo FX salvo export/backup, que van en `Task` de JavaFX.

## Migration Plan

- Greenfield: no hay datos previos que migrar. La creación del esquema se hace al primer arranque mediante el runner de migraciones (`PRAGMA user_version`).
- Los contadores iniciales de series (C, P, R) se configuran manualmente en Configuración → Series, según los correlativos actuales de la hoja de cálculo.
- De cara al futuro: la instalación de nuevas versiones no debe tocar la carpeta de datos (`%APPDATA%/Facturacion`).

## Open Questions

- El ajuste exacto de campos extra de empresa (actividad, email, teléfono) y el formato del logo: se resuelven durante implementación con el modelo mínimo propuesto, sin impacto en specs.
- Librería de PDF definitiva (OpenPDF como propuesta): se confirma en la tarea de implementación del PDF; no afecta al comportamiento especificado.
