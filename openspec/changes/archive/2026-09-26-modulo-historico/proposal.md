## Why

El histórico es la siguiente pantalla del orden de módulos. `HistoricoController` tiene 493 líneas y no cumple las normas:

| Qué | Hoy |
|---|---|
| Hilos | Exportar lanza dos `Task` anónimos con `new Thread`, y uno devuelve sus dos contadores en un `int[]` |
| Avisos y menús | La pregunta «un PDF por factura o agrupado» es un `ChoiceDialog` construido en Java; el menú del clic derecho también se construye en Java |
| Tabla | Columnas con lambdas en lugar de `PropertyValueFactory`, y la fila que abre con doble clic es un bloque dentro de una lambda |
| Estilo | 3 ternarios, un nombre de clase completo, dos `catch (Exception ignored)` y mensajes con prefijo («Error al buscar: …») |
| Filtros | El de serie es un combo de textos en el que «(Todas)» se reconoce porque empieza por «(»; `FiltrosHistorial` tiene constructor vacío y no comprueba nada |

Y tres fallos:

- **Un importe mal escrito se convierte en 0 sin avisar** (`Formatos.parseMoneda`), así que el filtro no filtra. Una fecha desde posterior a la hasta tampoco avisa.
- **El resumen de anular o eliminar nombra las facturas por su id interno** («Factura 12: …»), no por su número.
- **La especificación dice «Borrar» y la pantalla «Eliminar»**, como el resto de la aplicación.

## What Changes

- **`HistoricoController` y `Historico.fxml` rehechos**: columnas con `PropertyValueFactory`, el menú del clic derecho y el texto de la tabla vacía en el FXML, doble clic con `onMouseClicked`, combos de serie y estado sin trucos, y sin nada de lo que prohíbe `AGENTS.md`.
- **Al entrar se ven ya las facturas del año de trabajo**: las fechas vienen puestas del 1 de enero al 31 de diciembre de ese año.
- **Filtros que se comprueban**: `FiltrosHistorial` con constructor y setters que validan (fechas y importes al revés). Un importe mal escrito o un rango al revés se marca en rojo y se avisa, sin buscar.
- **«Eliminar» en todas partes**: botón, menú, resumen y especificación.
- **El resumen nombra cada factura por su número.**
- **Exportar sin hilos**, con cursor de espera, y la pregunta de varias facturas como aviso de `Dialogos` con tres botones: `Un PDF por factura`, `Todas en un PDF` y `Cancelar`.

## Capacidades

### Capacidades nuevas

Ninguna.

### Capacidades modificadas

- `invoicing`: «Histórico de facturas» (lo que se ve al entrar y los filtros que no valen) y «Anulación y borrado de facturas desde el histórico» (Eliminar en lugar de Borrar, el número libre y el resumen con números). Los títulos de los escenarios «Borrar…» se conservan: OpenSpec no deja renombrarlos.

## A qué afecta

- **Se rehacen**: `vista/controlador/HistoricoController.java`, `vista/recursos/Historico.fxml` y `modelo/dominio/FiltrosHistorial.java`.
- **Nuevo**: `vista/utilidades/ExportacionVarias.java` (como `CambiosSinGuardar`) y `Dialogos.mostrarDialogoExportarVarias`.
- **Cambian**: `Facturas` (el filtro por serie y el mensaje de la factura con rectificativa).
- **Tests**: `PantallaHistoricoTest`, `FiltrosHistorialTest` (nuevo), `FacturasTest`, `SeriesTest` y `FacturacionMensualTest`.
- **Queda fuera**: el código del paquete `pdf` (`ExportadorPdf`, `GeneradorPdf`…), que va en `modulo-pdf`; el botón «Mensual», que sigue abriendo las mensuales como hoy.
