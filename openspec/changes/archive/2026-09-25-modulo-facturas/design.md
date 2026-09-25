## Situación de partida

| Pieza | Hoy |
|---|---|
| Tablas | `factura` (serie, correlativo, cliente) + `factura_version` (todo lo demás, una fila por versión) + `factura_linea` (cuelga de la versión y guarda `total_base` e `iva_importe`) |
| Clases de datos | `Factura` (41 líneas), `VersionFactura` (261), `LineaFactura`, `FilaHistorial`, `DatosPago` (`record`) |
| Negocio | `Facturas` (354), `Versiones` (167), `Estados` (175), `Rectificativas` (113), `Historial` (24), sobre `FacturaDAO`, `VersionFacturaDAO`, `LineaFacturaDAO` e `HistorialDAO` |
| Excepciones y `record` | `ValidacionException` en 11 ficheros; `ResumenBorrado` y `VersionCompleta` en `Facturas`; `ResultadoConIva` y `ClaveIva` en `Calculos` |
| Pantallas | Pantalla de versiones, botón «Versiones», pregunta de «sobrescribir o nueva versión», columna «Versión» en el histórico |

## Objetivos y lo que queda fuera

**Objetivo**: una fila por factura; `Factura` como la factura entera con sus objetos dentro; todo el SQL de las facturas en el singleton `Facturas`; sin versiones, sin `ValidacionException` y sin `record` en lo que toca a las facturas; y las pantallas funcionando igual que hoy salvo lo que decide este change.

**Fuera**: rehacer las pantallas (el editor, el histórico, el PDF, las mensuales y las copias se rehacen en sus módulos: aquí solo se adaptan); los `record` del paquete `pdf` y de `CopiaSeguridad`; la pregunta de F5 al guardar un cliente escrito a mano (va con el editor); `DatosException` de `Conexion`; y las dos pruebas de pantalla del editor que necesitan editar celdas (número ocupado a mano y aviso del hueco), que van con el editor nuevo.

---

## D1. El esquema

En `db/crear_tablas.sql`, fuera `factura_version` y su índice, y las dos tablas de facturas quedan así:

```sql
CREATE TABLE IF NOT EXISTS factura (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  serie_id INTEGER NOT NULL REFERENCES serie(id),
  anio INTEGER NOT NULL,
  correlativo INTEGER NOT NULL,
  numero TEXT NOT NULL,
  fecha TEXT NOT NULL,
  estado TEXT NOT NULL CHECK (estado IN ('EMITIDA', 'ANULADA')),
  cliente_id INTEGER REFERENCES cliente(id),
  cli_nombre TEXT,
  cli_nif TEXT,
  cli_direccion TEXT,
  cli_cp TEXT,
  cli_localidad TEXT,
  cli_provincia TEXT,
  cli_email TEXT,
  descuento INTEGER NOT NULL DEFAULT 0,
  observaciones TEXT,
  rectifica_id INTEGER REFERENCES factura(id),
  forma_pago TEXT,
  vencimiento TEXT,
  realizada_por TEXT,
  retencion_id INTEGER REFERENCES tipo_retencion(id),
  retencion_nombre TEXT,
  retencion_porcentaje INTEGER,
  base_total TEXT NOT NULL DEFAULT '0.00',
  iva_total TEXT NOT NULL DEFAULT '0.00',
  importe_retencion TEXT NOT NULL DEFAULT '0.00',
  total_suplidos TEXT NOT NULL DEFAULT '0.00',
  total TEXT NOT NULL DEFAULT '0.00',
  UNIQUE (serie_id, anio, correlativo)
);

CREATE TABLE IF NOT EXISTS factura_linea (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  factura_id INTEGER NOT NULL REFERENCES factura(id),
  orden INTEGER NOT NULL,
  cantidad INTEGER NOT NULL DEFAULT 1,
  descripcion TEXT,
  precio_unitario TEXT NOT NULL DEFAULT '0',
  tipo_iva_id INTEGER REFERENCES tipo_iva(id),
  iva_nombre TEXT,
  iva_porcentaje INTEGER,
  iva_motivo_exencion TEXT,
  es_suplido INTEGER NOT NULL DEFAULT 0 CHECK (es_suplido IN (0, 1))
);

CREATE INDEX IF NOT EXISTS idx_linea_factura ON factura_linea(factura_id, orden);
```

- **`anio`** repite el año de `fecha`, pero lo pone siempre `Facturas` al guardar. Gracias a él, el `UNIQUE` impide repetir un número y las consultas de numeración pierden el `CAST(strftime(...))`.
- **Las copias** (`cli_*`, `retencion_nombre`/`retencion_porcentaje`, `iva_*` de las líneas) guardan lo que decía la factura al emitirse; los `_id` dicen *quién* es. Las dos cosas se quedan.
- **Los totales de la factura se quedan**: el histórico los lee sin recalcular. Los de las líneas se van: `precio_unitario` se guarda con 6 decimales, así que cantidad × precio redondeado a dos decimales devuelve siempre el mismo total.
- Cambian de nombre, para que se entiendan solos: `fecha_factura` → `fecha`, `descuento_porcentaje` → `descuento`, `tipo_retencion_*` → `retencion_*`.

`db/seed_demo.sql` se rehace con la misma demostración en una sola tabla: A-1 a A-5 (la A-5 anulada) y la R-1, que rectifica la A-1 (`rectifica_id` = id de la A-1). Las líneas, con `factura_id` y sin sus totales.

---

## D2. Las clases de datos

### `Factura`: la factura entera

```java
private Long id;
private Serie serie;
private int correlativo;          // 0 mientras no tiene número
private String numero;
private LocalDate fecha;
private EstadoFactura estado;     // EMITIDA al crearla
private Cliente cliente;          // la copia de los datos; su id es cliente_id
private int descuento;
private String observaciones;
private Long rectificaId;         // la factura que rectifica, o null
private String rectificaNumero;   // su número, solo para mostrarlo; lo rellena buscar
private String formaPago;
private LocalDate vencimiento;
private String realizadaPor;
private TipoRetencion retencion;  // la copia: id, nombre y porcentaje; null sin retención
private List<LineaFactura> lineas;
private BigDecimal baseTotal;
private BigDecimal ivaTotal;
private BigDecimal importeRetencion;
private BigDecimal totalSuplidos;
private BigDecimal total;
```

- Constructor con lo obligatorio: `Factura(Serie serie, LocalDate fecha, Cliente cliente)`. Los setters de esos tres rechazan el `null` con «Seleccione la serie.», «Indique la fecha de la factura.» e «Indique el cliente de la factura.». Lo demás, con su setter.
- `getAnio()` devuelve `fecha.getYear()`: el año **no es un campo**, sale de la fecha.
- Constructor copia, `equals`/`hashCode` por el `id` y `toString` con el número.
- Getters de texto para las tablas, apoyados en `Formatos`: `getFechaTexto()`, `getClienteNombre()`, `getClienteNif()`, `getBaseTexto()`, `getIvaTexto()`, `getRetencionTexto()`, `getTotalTexto()` y `getEstadoTexto()`.
- Los datos de pago son tres campos de la factura: **fuera el `record` `DatosPago`**.

### `LineaFactura`

Pierde los campos `totalBase` e `ivaImporte` con sus setters. `getTotalBase()` se queda, pero **calculado**: `Calculos.totalLinea(precioUnitario, cantidad)`. El método `copia()` pasa a constructor copia.

### Lo que desaparece

`VersionFactura`, `DatosPago`, `FilaHistorial` (el histórico enseña objetos `Factura`), `Facturas.VersionCompleta`, `Facturas.ResumenBorrado`, `ModoGuardarVersion` y `ValidacionException` (todo pasa a `Exception`, con el mensaje tal como lo verá el usuario). `FiltrosHistorial` se queda: es el parámetro de `listado`.

---

## D3. `Facturas`: el único dueño de las tablas de facturas

Singleton con el SQL de `factura` y `factura_linea`. Operaciones públicas:

| Operación | Qué hace |
|---|---|
| `long alta(Factura factura)` | Numera (si `correlativo` es 0, el siguiente; si no, comprueba que esté libre), forma el número, asegura el cliente, calcula los totales y guarda la factura con sus líneas. En una transacción |
| `void altaVarias(List<Factura> facturas)` | Lo mismo para varias, **todas o ninguna**. Es lo que usan las mensuales |
| `void modificar(Factura factura)` | Sobrescribe la factura y sustituye sus líneas. No deja editar una anulada ni cambiar de año |
| `void baja(long id)` | Borra la factura y sus líneas. No deja si tiene rectificativa |
| `Factura buscar(long id)` | La factura con su serie, sus líneas y el número de la factura que rectifica |
| `List<Factura> listado(FiltrosHistorial filtros)` | El histórico: una `Factura` por fila, sin líneas, ordenadas por serie, año y correlativo |
| `void anular(long id)` / `void restaurar(long id)` | Cambian el estado de la misma fila |
| `long rectificar(long id, LocalDate fecha)` | Crea la rectificativa en la serie de rectificativas, copiando cliente, líneas, descuento, observaciones y retención, con `rectificaId` = `id` |
| `int numeroDeLineas(long id)` | Para el aviso de borrado |
| `boolean clienteTieneFacturaEnMes(long clienteId, int anio, int mes)` | Para las mensuales |

Privados: `insertar(Factura)` (sin transacción, lo comparten `alta` y `altaVarias`), `guardarLineas(long facturaId, List<LineaFactura>)`, `calcularTotales(Factura)`, `asegurarCliente(Cliente)` (el que ya existe), `crearFactura(ResultSet)` y `crearLinea(ResultSet)`.

**Transacciones**, escritas en el propio método como pide `AGENTS.md`:

```java
/** Damos de alta la factura con sus líneas: o se guarda todo o no se guarda nada. */
public long alta(Factura factura) throws Exception {
    comprobarLineas(factura);
    Connection conexion = Conexion.establecerConexion();
    try {
        conexion.setAutoCommit(false);
        long id = insertar(factura);
        conexion.commit();
        return id;
    } catch (Exception e) {
        conexion.rollback();
        throw e;
    } finally {
        conexion.setAutoCommit(true);
    }
}
```

**Las comprobaciones de `modificar`**, antes de tocar nada:

```java
Factura actual = buscar(factura.getId());
if (actual == null) {
    throw new Exception("No se ha encontrado la factura.");
}
if (actual.getEstado() == EstadoFactura.ANULADA) {
    throw new Exception("Una factura anulada no se puede editar.");
}
if (factura.getAnio() != actual.getAnio()) {
    throw new Exception("Una factura emitida no puede cambiar de año. Si es de otro año, anúlala y crea una nueva.");
}
```

Después, el número se vuelve a formar con el correlativo de siempre y la fecha nueva (con formato MES, el mes del número sigue a la fecha).

**`baja`** mira antes si alguna factura la rectifica (`SELECT numero FROM factura WHERE rectifica_id = ?`) y, si la hay: «La factura A-1/9 tiene la rectificativa R-1 y no se puede borrar.». La clave ajena lo impediría de todas formas, pero con un error de SQLite en vez de un aviso.

**Número pedido a mano y ocupado** (en `insertar`): «El número %s ya lo tiene otra factura de la serie %s.» El `UNIQUE` es la última red.

**`clienteTieneFacturaEnMes`** busca por intervalo de fechas (`fecha >= ? AND fecha <= ?`, con el primer y el último día del mes en formato ISO), sin `strftime`.

**Sin reloj**: `Facturas` no guarda `Clock`. La fecha viene siempre en la `Factura`.

---

## D4. `Series`: numerar con una sola tabla

```java
private Set<Integer> correlativosUsados(long serieId, int anio) throws Exception {
    String consulta = "SELECT correlativo FROM factura WHERE serie_id = ? AND anio = ?";
    ...
}
```

- **Fuera `correlativosActivos`**. `correlativoOcupado` pasa a usar `correlativosUsados`: un número está ocupado si lo tiene **cualquier** factura de la serie y el año, activa o anulada. Es lo que ya decía la especificación desde `modulo-series` («la anulada conserva su número») y lo que ahora exige el `UNIQUE`. Por eso restaurar una anulada ya no necesita comprobar nada.
- **Nuevo `Serie rectificativa()`**: la primera serie de rectificativas por código, o `null`. `rectificar` avisa si no hay: «No hay ninguna serie de rectificativas. Créala en Configuración.».

---

## D5. `Calculos`

- Fuera el mapa `cuotasSinDescuento`, que se calcula y nunca se lee.
- Fuera el `record` `ClaveIva`: los grupos de IVA se agrupan con una clave de texto (`nombre|porcentaje|motivo`).
- Fuera el `record` `ResultadoConIva`: `calcularDesdeTotalConIva` pasa a `baseDesdeTotalConIva(BigDecimal total, Integer porcentaje)`, que devuelve solo la base; el editor saca el precio de ella con `precioDesdeTotal`, como ya hace.
- `Facturas.calcularTotales` rellena los cinco totales de la factura con `Calculos.resumen`.

---

## D6. Controlador y Modelo

Operaciones nuevas, de una línea cada una, en los dos:

`altaFactura`, `modificarFactura`, `bajaFactura`, `buscarFactura`, `listadoFacturas`, `anularFactura`, `restaurarFactura`, `rectificarFactura`, `numeroDeLineasFactura`, `generarFacturasMensuales` y `mensualesDuplicadas`.

`Modelo` pierde `getFacturas()`, `getVersiones()`, `getEstados()`, `getRectificativas()`, `getHistorial()` y `getFacturacionMensual()`, y su constructor pierde los DAO y el `Clock` que ya no hagan falta. Ninguna pantalla vuelve a llamar a `getModelo()` para facturas.

---

## D7. Las pantallas: solo plumbing

**Editor**:

- Abrir: `buscarFactura(id)` y rellenar. Con una factura abierta, **`txtNumero` se desactiva** como ya se hace con la serie (el número no se puede cambiar al editar y hoy se tiraba sin avisar).
- La referencia de una rectificativa: `txtReferencia` con `rectificaNumero` y **no editable**.
- Guardar nueva: se construye la `Factura` con lo de pantalla (el hueco y el número a mano, como hoy) y `altaFactura`.
- Guardar una emitida: pregunta con `Dialogos.mostrarDialogoConfirmacion("Guardar factura", String.format("¿Guardar los cambios de la factura %s?%n%nLa factura ya emitida se sobrescribirá.", numero))` y, si acepta, `modificarFactura`.
- Fuera: el botón «Versiones», su método, la pregunta de «nueva versión» y el «(vN)» del título.
- En `aplicarCantidad`, `aplicarPrecio` y `aplicarTotal`: fuera los `setTotalBase` y `setIvaImporte`; se queda solo el precio (el total ya sale de él).
- Rectificar, anular, restaurar y exportar, por las operaciones del controlador.

**Histórico**: la tabla enseña objetos `Factura` con sus getters de texto; fuera la columna «Versión» (también en `Historico.fxml`). Anular varias es un bucle de `anularFactura` que junta los avisos. Borrar avisa con `numeroDeLineasFactura` y enseña el aviso de la rectificativa si lo hay.

**Mensuales**: `FacturacionMensual` construye objetos `Factura` (con el correlativo que da `Series.proponerNumeros`) y llama a `Facturas.altaVarias`. `detectarDuplicados` usa `clienteTieneFacturaEnMes`. Pierde sus DAO.

**PDF**: `ConstructorDocumentoFactura.build(Factura, Empresa, String)` y `ExportadorPdf.exportar(Factura …)` / `exportarAgrupado(List<Factura> …)`. Los métodos de ayuda reciben `Factura` en vez de `VersionFactura`. Los `record` de `DocumentoFactura` se quedan para su módulo.

**Copias**: `CopiaSeguridadDAO` con las tablas y columnas nuevas (fuera `factura_version`); el resumen de una copia (número de facturas y última fecha) lee de `factura.fecha`. Lo que `CopiaSeguridad` pedía a `FacturaDAO` lo pide a `Facturas`.

**Se borran**: `VersionesController`, `Versiones.fxml` y `Dialogos.mostrarDialogoModoGuardarVersion`.

---

## D8. Tests

- **`FacturasTest`** reúne lo de `EstadosTest`, `HistorialTest` y las pruebas de versiones, contra una base temporal: el alta numera; un número pedido a mano que ya tiene otra factura (también **anulada**) se rechaza; modificar deja **una** fila y sustituye las líneas; cambiar el mes rehace el número; **cambiar de año** se rechaza con su mensaje; una anulada no se modifica; anular y restaurar cambian el estado de la misma fila; rectificar crea la rectificativa en la serie R con `rectificaId` y el número de la original; **borrar una factura rectificada** se rechaza con su mensaje; borrar se lleva las líneas; `listado` filtra y da una fila por factura; `altaVarias` es todo o nada; `clienteTieneFacturaEnMes`.
- **`SeriesTest`**: `correlativoOcupado` cuenta las anuladas; `rectificativa()`.
- **`CalculosTest`**: `baseDesdeTotalConIva`, y que el precio calculado hacia atrás desde un total vuelve a dar ese total (100,00 entre 3, 0,01 entre 7, 1.234,56 entre 9).
- **Pantalla**: `PantallaHistoricoTest` (la demo son **6 filas**, anular no añade ninguna, borrar la A-1 avisa de la R-1); `PantallaEditorTest` (guardar una emitida pregunta; el número y la referencia no se editan); `CargaPantallasTest` y `TextosCompletosTest` sin `Versiones.fxml`.
- Se adaptan los del PDF, las copias, las mensuales y la demostración.

## Decisiones

| Decisión | Por qué |
|---|---|
| Un solo change | Decisión del usuario (25/09): las consultas se escriben una vez, ya en su sitio |
| Todo el SQL de facturas en `Facturas` | Decisión del usuario; es lo que dice `AGENTS.md` («`Facturas` → `factura`, `factura_linea`») |
| La fecha solo cambia dentro del año al editar | Decisión del usuario: la factura se queda en la numeración de su año, sin huecos ni facturas fuera de orden |
| Pregunta antes de sobrescribir una emitida | Decisión del usuario: no tocar por descuido un documento ya entregado |
| La rectificativa apunta a su original y la referencia no se edita | Decisión del usuario: relación real y sin borrar una factura rectificada |
| `anio` y `UNIQUE`; líneas sin totales; `rectifica_id` | Decididos al revisar la base de datos (25/09) |
| «Histórico», «Numeración por series» y «Exportación a PDF» se rehacen con otro nombre | OpenSpec no deja quitar ni renombrar escenarios en un `MODIFIED`, y los tres tenían escenarios que ya no son verdad o que nombraban las versiones. Comprobado en una copia antes de escribirlo |
| `DatosPago` pasa a tres campos de `Factura` | Es un `record`, y son tres columnas de la tabla |

## Riesgos y renuncias

- **Es el change más grande del proyecto**: toca el negocio entero de facturas y cinco pantallas. Las pruebas de pantalla hechas en `pruebas-de-pantalla` son la red.
- **El delta de la especificación es el mayor hasta ahora**: 16 requisitos en `invoicing` y 3 en `pdf-rendering`. Se generó por script desde la especificación viva y se comprobó escenario a escenario.
- **Las bases y las copias de hoy no valen**: cambia el esquema. Hay que borrar `%APPDATA%\Facturacion`; los datos son de prueba.
- **La columna del editor con el total con IVA de cada línea puede cambiar un céntimo** en casos de redondeo, porque ya no se guarda `iva_importe` y se calcula. Los totales de la factura no cambian: ya se calculaban por tipo de IVA.
- **El editor sigue siendo enorme** tras este change: solo se adapta. Se rehace en `modulo-editor`.
