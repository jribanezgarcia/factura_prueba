## Why

El siguiente número de una serie está guardado **dos veces** y, además, se puede deducir de las propias facturas. El historial explica por qué:

| Cuándo | Qué se añadió |
|---|---|
| 16/08 | `serie.siguiente_correlativo`: un contador por serie |
| 31/08 | `serie_siguiente`: un contador **por año**, al llegar el ejercicio fiscal. El anterior no se borró |
| 31/08 | `numero_disponible`: una tabla con los huecos que deja el borrado de facturas |

Hoy, para proponer un número, `Numeracion` mezcla cuatro fuentes: los huecos apuntados, los anulados reutilizables, el contador del año y, por si el contador se ha quedado atrás, «el mayor ocupado + 1». La última ya da la respuesta sola; las otras tres existen porque un contador guardado se desincroniza al borrar una factura o al escribir un número a mano.

Y el resto del módulo tampoco sigue las normas: `Serie` no se valida y lleva un `enum` dentro; la numeración vive repartida entre `Numeracion`, `SerieDAO` y `NumeroDisponibleDAO`; y la sección Series de Configuración es la última con filas de alta rápida, una clase anónima y ternarios.

## What Changes

- **El siguiente número se calcula**: el mayor correlativo de esa serie y ese año más uno, o 1 si no hay ninguna. **Desaparecen** la columna `serie.siguiente_correlativo`, la tabla `serie_siguiente`, la tabla `numero_disponible` y `NumeroDisponibleDAO`. No queda ningún contador que se pueda desincronizar.
- **Fuera «Reutilizar anulados»**: una factura anulada conserva su correlativo para siempre, como exigirá VeriFactu. Desaparecen la columna, la casilla de la ficha y su columna en la tabla.
- **Los huecos se siguen ofreciendo**, pero calculados: son los correlativos que faltan por debajo del mayor, porque se borraron sus facturas.
- **`Serie` se valida sola**, con sus `errorX`, y su `enum` pasa a un fichero propio, `FormatoNumero`, que ya escribe su propio texto en el desplegable.
- **`Series` se queda con toda la numeración** (lo que hoy hacen `Numeracion`, `SerieDAO` y `NumeroDisponibleDAO`), como singleton con su SQL.
- **La sección Series de Configuración pasa a ficha modal**, como IVA y retenciones, con Nuevo, Editar y Eliminar debajo de la tabla. «Siguiente número» deja de ser editable: se ve calculado en la tabla.
- **El esquema y la demostración se rehacen** con las tablas y columnas que quedan.

## Capacidades

### Capacidades nuevas

Ninguna.

### Capacidades modificadas

- `invoicing`: «Numeración por series» (siguiente número calculado, sin contadores, sin reutilizar anulados), «Configuración» (el siguiente número ya no se modifica) y «Ventana» (Series ya no tiene fila de alta rápida).

## A qué afecta

- **Nuevo**: `modelo/dominio/FormatoNumero`, `vista/controlador/FichaSerieController`, `vista/recursos/FichaSerie.fxml`.
- **Se borran**: `modelo/negocio/Numeracion`, `modelo/negocio/sqlite/SerieDAO`, `modelo/negocio/sqlite/NumeroDisponibleDAO`.
- **Se rehacen**: `modelo/dominio/Serie`, `modelo/negocio/Series`, y la sección Series de `ConfiguracionController` y `Configuracion.fxml`.
- **Cambian**: `controlador/Controlador`, `modelo/Modelo`, `db/crear_tablas.sql`, `db/seed_demo.sql` y `CopiaSeguridadDAO`.
- **Solo plumbing**: `Facturas` (pierde la actualización de contadores y el apunte de huecos), `Estados`, `Rectificativas`, `FacturacionMensual`, `EditorController` (el diálogo del hueco y el número a mano), `GenerarFacturasMensualesController` y `HistoricoController`.
- **Tests**: `NumeracionTest` y `SerieDAOTest` se rehacen como `SeriesTest`; nuevo `SerieTest`; se adaptan `FacturasTest`, `EstadosTest`, `HistorialTest`, `FacturacionMensualTest`, `ClientesTest` y `EmpresasTest`.
- **Queda fuera**: las facturas siguen con versiones; las consultas de numeración siguen mirando la última versión de cada factura hasta el change siguiente. Que una serie con facturas no pueda cambiar de código o de formato durante el ejercicio se deja para más adelante.
