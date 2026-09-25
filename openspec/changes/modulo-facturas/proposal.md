## Why

Una factura está hoy partida en dos tablas: `factura` guarda solo la serie, el correlativo y el cliente, y cada edición crea una fila en `factura_version` con **todo lo demás**. Por eso las líneas cuelgan de la versión, todas las consultas llevan un `JOIN` a «la última versión», y el histórico enseña una fila por versión. Las versiones se decidieron quitar (F2): una factura es una sola y editarla la sobrescribe.

El código arrastra esa división y además no sigue las normas:

| Qué | Hoy |
|---|---|
| Clases de datos | `Factura` (41 líneas, casi vacía) y `VersionFactura` (261, la factura de verdad). `DatosPago` es un `record` |
| Negocio | Repartido en `Facturas`, `Versiones`, `Estados`, `Rectificativas` e `Historial`, sobre cuatro DAO. `crearFactura` tiene **cuatro** sobrecargas y `guardarEditada` tres, porque la factura viaja en diez parámetros sueltos |
| Excepciones | `ValidacionException`, prohibida por `AGENTS.md`, en 11 ficheros |
| `record` | `ResumenBorrado` y `VersionCompleta` dentro de `Facturas`; `ResultadoConIva` y `ClaveIva` dentro de `Calculos` |
| Líneas | Guardan `total_base` e `iva_importe`, que se pueden calcular. `iva_importe` ni se usa: `Calculos` lo suma en un mapa que nunca se lee |

## What Changes

- **Una fila por factura**: `factura` y `factura_version` se funden en `factura`. Las líneas cuelgan de la factura y ya no guardan sus totales. La factura guarda su año en `anio` y la base de datos exige `UNIQUE (serie_id, anio, correlativo)`. La rectificativa apunta a su original con `rectifica_id`.
- **`Factura` es la factura entera**, con objetos dentro: su `Serie`, la copia de su `Cliente`, su lista de `LineaFactura` y su `TipoRetencion`. Desaparecen `VersionFactura` y el `record` `DatosPago`.
- **`Facturas` es el único dueño de las tablas de facturas**, como singleton con su SQL: `alta`, `altaVarias`, `modificar`, `baja`, `buscar`, `listado`, `anular`, `restaurar` y `rectificar`. Desaparecen `Versiones`, `Estados`, `Rectificativas`, `Historial` y los cuatro DAO.
- **Editar una factura emitida la sobrescribe, previa confirmación.** Su fecha puede cambiar dentro del año, pero no de año.
- **La referencia de una rectificativa ya no se escribe a mano**: es siempre el número de su factura original. Una factura con rectificativa no se puede borrar.
- **Fuera las versiones de las pantallas**: la pantalla de versiones, su botón, la pregunta de «nueva versión» y la columna «Versión» del histórico. Anular cambia el estado de la misma factura.
- **Sin `ValidacionException` y sin `record`** en todo lo que toca a las facturas.
- Las pantallas (editor, histórico, mensuales, PDF y copias) **solo se adaptan** para seguir funcionando: cada una se rehace después en su módulo.

## Capacidades

### Capacidades nuevas

Ninguna.

### Capacidades modificadas

- `invoicing`: se quita «Versionado»; «Histórico» pasa a «Histórico de facturas» y «Numeración por series» a «Numeración de facturas por series» (cambian el título de algún escenario, y OpenSpec no deja renombrar escenarios); y se modifican «Facturas normales», «Estados de factura», «Rectificativas», «Menú y navegación», «Cambios sin guardar», «Atajos de teclado», «Retención de IRPF», «Retención en histórico», «Persistencia local», «Identidad de la aplicación en la interfaz», «Anulación y borrado de facturas desde el histórico» y «Suplidos en la facturación».
- `pdf-rendering`: «Exportación a PDF» pasa a «Exportación de facturas a PDF» (mismo contenido; un escenario nombraba las versiones), y se modifican «Separación de responsabilidades en la exportación» y «Composición del documento verificable sin PDF».

## A qué afecta

- **Base de datos**: `db/crear_tablas.sql` (tablas `factura` y `factura_linea`; fuera `factura_version`) y `db/seed_demo.sql`.
- **Nuevo o rehecho**: `modelo/dominio/Factura`, `modelo/dominio/LineaFactura`, `modelo/negocio/Facturas`, `modelo/negocio/Calculos`.
- **Se borran**: `VersionFactura`, `DatosPago`, `FilaHistorial`, `Versiones`, `Estados`, `Rectificativas`, `Historial`, `ValidacionException`, `FacturaDAO`, `VersionFacturaDAO`, `LineaFacturaDAO`, `HistorialDAO`, `VersionesController`, `Versiones.fxml` y `ModoGuardarVersion`.
- **Cambian**: `Series` (consultas de una sola tabla y la serie rectificativa), `Controlador`, `Modelo`, `FacturacionMensual`, `CopiaSeguridadDAO`.
- **Solo plumbing**: `EditorController`, `HistoricoController`, `GenerarFacturasMensualesController`, `ConstructorDocumentoFactura`, `ExportadorPdf` y `Dialogos`.
- **Queda fuera**: rehacer las pantallas (editor, histórico, PDF, mensuales y copias van en sus módulos); los `record` del PDF y de las copias; la pregunta de F5 al guardar un cliente escrito a mano, que va con el editor; y las dos pruebas de pantalla del editor que necesitan editar celdas.
