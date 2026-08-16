## 1. Setup del proyecto

- [x] 1.1 Crear estructura Maven (`pom.xml` con Java 21, JavaFX, driver SQLite JDBC, OpenPDF, JUnit) y estructura de paquetes base `com.alcazaba.facturacion`
- [x] 1.2 Configurar plugin JavaFX-Maven y una clase `Application` mínima que abra el menú principal
- [x] 1.3 Implementar arranque con carpeta de datos `%APPDATA%/Facturacion` (crearla si no existe), conexión SQLite y runner de migraciones con `PRAGMA user_version`
- [x] 1.4 Implementar instancia única con `FileChannel.tryLock` sobre `facturas.lock` y aviso al usuario si ya hay una instancia
- [x] 1.5 Configurar idioma español y moneda euro para formatos de importe y fecha

## 2. Capa de datos

- [x] 2.1 Escribir migración inicial: tablas `cliente`, `serie`, `tipo_iva`, `factura`, `factura_version`, `factura_linea`, `empresa`, `preferencias`
- [x] 2.2 Implementar `ConnectionFactory` (una conexión compartida, `autoCommit=false`) y helpers de commit/rollback
- [x] 2.3 Implementar repositorios: `ClienteRepository` (CRUD, búsqueda incremental, borrado lógico/físico)
- [x] 2.4 Implementar `SerieRepository` (CRUD, siguiente correlativo, reutilizar anulados) y `IvaRepository` (CRUD, activo/inactivo)
- [x] 2.5 Implementar `FacturaRepository` (insertar factura, listar por filtros, marcar números activos), `VersionRepository` (insertar y leer versiones) y `LineaRepository` (insertar y leer líneas por versión)
- [x] 2.6 Implementar `ConfigRepository` (empresa, preferencias) con carga/guardado de configuración
- [x] 2.7 Implementar repositorios de acceso para histórico con joins sobre snapshots (filtros combinados)

## 3. Servicios

- [x] 3.1 Implementar `CalculoService`: recálculo precio↔total (neto y con IVA), descuento global con reparto proporcional entre bases y ajuste de céntimos, totales; con `BigDecimal` y `HALF_UP`
- [x] 3.2 Implementar `NumeroService`: formación del número (`C-59/8`, `R-1`), propuesta del siguiente correlativo (con o sin reutilización de anulados), validación de unicidad contra activas, consumo solo tras commit
- [x] 3.3 Implementar `VersionadoService`: creación de versiones (v1, v2, ...) con snapshot completo, invariante de que las versiones anteriores no se modifican
- [x] 3.4 Implementar `FacturaService`: guardado transaccional (factura + versión + líneas + consumo de número), apertura de cualquier versión, edición a partir de una versión anterior
- [x] 3.5 Implementar `EstadoService`: anular y restaurar con confirmación, creación de versión en ambos casos, bloqueo de restauración si el número está ocupado
- [x] 3.6 Implementar `RectificativaService`: creación desde factura existente copiando datos, referencia automática editable, serie R
- [x] 3.7 Implementar `HistorialService`: búsqueda con filtros combinados (serie, cliente, NIF, fechas, importes, estado) ordenada
- [x] 3.8 Implementar `BackupService` con `VACUUM INTO` a archivo con timestamp
- [x] 3.9 Escribir tests unitarios de `CalculoService` (descuento simple, varios IVA, entrada con IVA, redondeo) y `NumeroService` (mes derivado, reutilización, unicidad)

## 4. UI base

- [x] 4.1 Implementar menú principal (Nueva factura, Histórico, Configuración, Copia de seguridad, Salir)
- [x] 4.2 Implementar navegación con una sola factura abierta y barra superior de factura (Guardar, Exportar PDF, Versiones, Crear rectificativa, Nueva factura, Volver)
- [x] 4.3 Implementar diálogo de cambios sin guardar (Guardar / Descartar / Cancelar) al volver o cerrar
- [x] 4.4 Implementar atajos Ctrl+N, Ctrl+S, Ctrl+F, Ctrl+P y Esc
- [x] 4.5 Implementar persistencia de preferencias (última serie, tamaño/posición de ventana, última carpeta de exportación)

## 5. Gestión de clientes

- [x] 5.1 Implementar vista de lista de clientes con alta/edición de ficha (nombre, NIF, dirección, CP, localidad, provincia)
- [x] 5.2 Implementar búsqueda incremental por nombre y NIF al crear factura y carga de datos al seleccionar
- [x] 5.3 Implementar borrado físico (solo sin facturas) e inactivación, y comportamiento de inactivos en nuevas facturas e histórico

## 6. Editor de factura

- [x] 6.1 Implementar cabecera de factura: fecha con selector/calendario, cliente (búsqueda + carga), número propuesto y editable, fecha de trabajo
- [x] 6.2 Implementar tabla de líneas editable (cantidad entera con default 1, descripción multi-línea, precio unitario, total, IVA por línea) con orden de introducción
- [x] 6.3 Implementar flujo de teclado Enter (cantidad → descripción → precio → total → siguiente línea) y eliminación con botón y Supr
- [x] 6.4 Implementar recálculo precio↔total según cantidad e IVA, y modo de entrada de total con IVA incluido (base e IVA hacia atrás)
- [x] 6.5 Implementar descuento general (entero, 0% default) y desglose de totales por tipo de IVA (base/IVA por tipo, total)
- [x] 6.6 Implementar campo de observaciones y validaciones de guardado (mínimo 1 línea, importes no negativos, número no duplicado)

## 7. Versionado y estados

- [x] 7.1 Implementar vista de Versiones de una factura (lista de versiones, apertura de cualquiera, edición desde versión anterior sin modificar la histórica)
- [x] 7.2 Implementar estados Emitida/Anulada: edición solo en Emitida, consulta y exportación en Anulada
- [x] 7.3 Implementar anular y restaurar con confirmación, creación de versión en ambos casos y bloqueo de restauración por número ocupado

## 8. Rectificativas

- [x] 8.1 Implementar creación de rectificativa desde una factura existente (serie R, datos copiados, referencia automática editable, fecha inicial = fecha de trabajo)

## 9. Histórico

- [x] 9.1 Implementar vista de Histórico con filtros combinables (serie, cliente, NIF, fecha desde/hasta, importe desde/hasta, estado) y botón Buscar
- [x] 9.2 Implementar tabla de resultados (fecha, número, versión, cliente, NIF, base, IVA, total, estado) con una fila por versión, ordenada, y apertura de la versión seleccionada

## 10. Configuración

- [x] 10.1 Implementar pestaña Empresa (datos de cabecera) y pestaña Cabecera (modo texto/logo, selección de logo, ajuste de tamaño y posición)
- [x] 10.2 Implementar pestaña Pie con texto legal libre configurable (sin contenido obligatorio)
- [x] 10.3 Implementar pestaña IVA (crear/modificar/inactivar tipos, exento con motivo)
- [x] 10.4 Implementar pestaña Series (crear/configurar series, ver y modificar siguiente número, reutilización de anulados por serie)
- [x] 10.5 Implementar pestaña PDFs (carpeta automática de almacenamiento y última carpeta utilizada)

## 11. Exportación a PDF

- [x] 11.1 Implementar `PdfService` con OpenPDF: A4 vertical, cabecera (texto o logo) y pie legal configurable repetidos en todas las páginas, `Página X de Y`
- [x] 11.2 Implementar tabla de líneas con descripciones ajustadas, bloque BASE/IVA/DESCUENTO/TOTAL y desglose por tipo de IVA
- [x] 11.3 Implementar formato español de importes (`1.250,50 €`) y fechas (`11/08/2026`)
- [x] 11.4 Implementar marca `ANULADA` destacada en exportaciones de facturas anuladas
- [x] 11.5 Implementar estructura de almacenamiento `Facturas/AAAA/SERIE/` y nombre `CODIGO-CORRELATIVO-MES_vN.pdf` (barra sustituida por guion en el archivo)
- [x] 11.6 Asegurar que el PDF de una versión concreta refleja exactamente los datos de esa versión

## 12. Copia de seguridad y cierre

- [x] 12.1 Conectar el botón de Copia de seguridad al `BackupService` con diálogo de destino
- [x] 12.2 Revisión final: verificaciones de histórico, anuladas, rectificativas, atajos y cierre con cambios sin guardar
- [ ] 12.3 Validar el cambio completo con `openspec validate` y dejar la implementación lista para pruebas
- [x] 12.4 Corregir el filtro de Histórico para que los límites de importe vacíos no se interpreten como 0 €
- [x] 12.5 Mostrar los clientes activos al abrir el desplegable de cliente en el editor
