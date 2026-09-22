## Situación de partida

| Pieza | Hoy |
|---|---|
| `Serie` (79 líneas) | Sin validar, con el `enum SufijoFecha` dentro y un campo `siguienteCorrelativo` que a veces vale el contador global y a veces el del año |
| `Numeracion` (188) | Forma el número, propone el correlativo, calcula huecos, propone varios para las mensuales y lee un número escrito a mano |
| `SerieDAO` (260) | El SQL de `serie` y de `serie_siguiente`, y las consultas de correlativos activos y anulados (que miran la última versión de cada factura) |
| `NumeroDisponibleDAO` (58) | El SQL de `numero_disponible` |
| `Series` (54) | Reenvía al DAO |
| Sección Series | Filas de alta rápida, un `StringConverter` anónimo y varios ternarios |

## Objetivos y lo que queda fuera

**Objetivo**: que el siguiente número salga de las facturas, que la numeración viva entera en `Series`, que `Serie` se valide sola y que la sección Series quede como IVA y retenciones.

**Fuera**: las versiones de factura (siguiente change); el bloqueo del código y el formato en series con facturas (más adelante); el número escrito a mano y el borrado de facturas, que se quedan como están hasta VeriFactu.

---

## D1. `FormatoNumero`, en su propio fichero

`modelo/dominio/FormatoNumero.java`, nuevo. Sustituye a `Serie.SufijoFecha` y lleva su propio texto, así que el desplegable de la ficha no necesita traductor:

```java
/** Cómo se forma el número de una serie. El texto es el que se ve en el desplegable. */
public enum FormatoNumero {

    MES("Código-Número/Mes (ej: C-56/7)"),
    ANIO("Código-Número-Año (ej: C-56-2026)"),
    NINGUNO("Código-Número (ej: C-56)");

    private final String texto;

    FormatoNumero(String texto) {
        this.texto = texto;
    }

    @Override
    public String toString() {
        return texto;
    }
}
```

En la base de datos se sigue guardando su nombre (`MES`, `ANIO`, `NINGUNO`), como hoy.

---

## D2. `Serie`: se valida sola

`modelo/dominio/Serie.java`, rehecha. **Obligatorios**: el formato. **Opcionales**: código, descripción, `id` y si es rectificativa.

```java
public Serie(String codigo, String descripcion, FormatoNumero formato, boolean esRectificativa) throws Exception {
    setCodigo(codigo);
    setDescripcion(descripcion);
    setFormato(formato);
    setEsRectificativa(esRectificativa);
}
```

| Comprobador | Regla |
|---|---|
| `errorCodigo(String)` | Puede estar vacío (esa serie numera sin prefijo). Si se escribe, solo letras y números: «El código de la serie solo puede tener letras y números.» |
| `errorFormato(FormatoNumero)` | No puede faltar: «Indique el formato del número.» |

`setCodigo` guarda en mayúsculas y sin espacios; `setDescripcion` guarda `""` si viene vacía. **Salen** `siguienteCorrelativo` y `reutilizarAnulados`.

Getters de texto para la tabla: `getCodigoTexto()` («(sin código)» cuando está vacío), `getRectificativaTexto()` («Sí»/«No») y `getFormatoTexto()`. `toString()` se queda como está: el código y, entre paréntesis, la descripción. Constructor copia, y `equals`/`hashCode` por el `id`.

La regla de que **solo puede haber una serie sin código** no es de la clase, porque necesita ver las demás: va en `Series.alta` y `Series.modificar` (D3).

---

## D3. `Series`: el singleton con la numeración entera

`modelo/negocio/Series.java`, rehecha. Se trae el SQL de `SerieDAO` y las reglas de `Numeracion`; las dos clases y `NumeroDisponibleDAO` se borran.

**Las series**

| Método | Qué hace |
|---|---|
| `listado()` | Todas, ordenadas por código |
| `buscar(long id)` | Una, o `null` |
| `alta(Serie serie)` | Inserta, tras comprobar que el código no se repite y que no haya ya otra serie sin código |
| `modificar(Serie serie)` | Guarda los cambios, con las mismas comprobaciones |
| `baja(long id)` | Borra, si la serie no tiene ninguna factura |
| `tieneFacturas(long id)` | `true` si tiene alguna, activa o anulada |

Los dos mensajes de las comprobaciones: «Ya existe una serie con el código X.» y «Solo puede haber una serie sin código. Ponle un código para distinguirla.».

**La numeración**

| Método | Qué hace |
|---|---|
| `siguienteCorrelativo(Serie serie, LocalDate fecha)` | El mayor correlativo usado ese año + 1, o 1 si no hay ninguno |
| `huecos(Serie serie, LocalDate fecha)` | Los correlativos que faltan entre 1 y el mayor usado |
| `proponerNumeros(Serie serie, int anio, int cantidad, boolean usarHuecos)` | Para las mensuales: tantos correlativos libres como haga falta |
| `formarNumero(Serie serie, int correlativo, LocalDate fecha)` | El número según el formato, igual que hoy |
| `parseCorrelativo(Serie serie, String numero)` | El correlativo de un número escrito a mano, o `null` si no encaja |
| `correlativoOcupado(Serie serie, int correlativo, LocalDate fecha)` | `true` si lo tiene una factura **activa** de ese año |

El corazón son dos consultas privadas. Los correlativos **usados** son los de todas las facturas de esa serie y ese año, anuladas incluidas; los **ocupados** son solo los de las activas, que es lo que mira el restaurar de una factura anulada:

```java
/**
 * Correlativos que ya tiene alguna factura de esa serie en ese año, anuladas
 * incluidas: una factura anulada conserva su número para siempre.
 *
 * Cómo funciona: el año sale de la fecha de la última versión de cada factura.
 * Cuando las facturas dejen de tener versiones, esta consulta será de una sola tabla.
 */
private Set<Integer> correlativosUsados(long serieId, int anio) throws Exception {
    String consulta = """
            SELECT f.correlativo FROM factura f
            JOIN factura_version v ON v.id = (
                SELECT v2.id FROM factura_version v2 WHERE v2.factura_id = f.id
                ORDER BY v2.version_num DESC LIMIT 1)
            WHERE f.serie_id = ? AND strftime('%Y', v.fecha_factura) = ?
            """;
    ...
}
```

`correlativosActivos(long, int)` es la misma con `AND v.estado <> 'ANULADA'`. Las dos salen de `SerieDAO`, donde ya existen.

Con ellas, el siguiente correlativo y los huecos quedan así:

```java
/** El siguiente correlativo de la serie en el año de esa fecha: el mayor usado más uno. */
public int siguienteCorrelativo(Serie serie, LocalDate fecha) throws Exception {
    int mayor = 0;
    for (int usado : correlativosUsados(serie.getId(), anioDe(fecha))) {
        if (usado > mayor) {
            mayor = usado;
        }
    }
    return mayor + 1;
}

/**
 * Los correlativos libres por debajo del mayor: los que quedaron vacíos al
 * borrar facturas. Van de menor a mayor.
 */
public List<Integer> huecos(Serie serie, LocalDate fecha) throws Exception {
    Set<Integer> usados = correlativosUsados(serie.getId(), anioDe(fecha));
    List<Integer> libres = new ArrayList<>();
    int siguiente = siguienteCorrelativo(serie, fecha);
    for (int correlativo = 1; correlativo < siguiente; correlativo++) {
        if (!usados.contains(correlativo)) {
            libres.add(correlativo);
        }
    }
    return libres;
}
```

`formarNumero` y `parseCorrelativo` se traen tal cual de `Numeracion`, cambiando `Serie.SufijoFecha` por `FormatoNumero` y los ternarios por `if / else`. `proponerNumeros` se simplifica: los huecos (si se piden) y después los siguientes, sin contador.

---

## D4. `Controlador` y `Modelo`

Una línea cada una: `listadoSeries()`, `buscarSerie(long)`, `altaSerie(Serie)`, `modificarSerie(Serie)`, `bajaSerie(long)`, `serieTieneFacturas(long)`, `siguienteCorrelativo(Serie, LocalDate)`, `huecosDeSerie(Serie, LocalDate)`, `proponerNumeros(Serie, int, int, boolean)`, `formarNumero(Serie, int, LocalDate)`, `parseCorrelativo(Serie, String)` y `correlativoOcupado(Serie, int, LocalDate)`.

**Se borran** de `Modelo` los campos y getters `getSeries()` y `getNumeracion()`, y el `SerieDAO`/`NumeroDisponibleDAO` de su constructor.

---

## D5. La ficha de serie

`FichaSerie.fxml` + `FichaSerieController`, nuevos, con el patrón de `FichaTipoIvaController`: `setRegistro`/`getRegistro` con `original` y `registro`, `marcarCamposMalos`/`revisar`, `fotoDeLosCampos`/`confirmarDescartar`, `implements Pantalla, Initializable` con `puedeCerrar()`.

| Campo | Notas |
|---|---|
| Código | Puede quedarse vacío. Se guarda en mayúsculas |
| Descripción | Opcional |
| Formato | `ComboBox<FormatoNumero>` con los tres valores; sin traductor, porque el `enum` ya escribe su texto |
| Rectificativa | Casilla |
| Ejemplo | Una etiqueta debajo que se actualiza al escribir el código o cambiar el formato: «Ejemplo: C-56/7» |

El ejemplo se calcula con `formarNumero` sobre una serie de prueba, con el correlativo 56 y la fecha 15/07/2026, para no repetir las reglas del formato en la pantalla:

```java
/** Enseñamos cómo quedará el número con lo que hay escrito ahora mismo. */
private void actualizarEjemplo() {
    try {
        Serie prueba = new Serie(txtCodigo.getText().trim(), "", comboFormato.getValue(), chkRectificativa.isSelected());
        lblEjemplo.setText("Ejemplo: " + Vista.getInstancia().getControlador()
                .formarNumero(prueba, 56, LocalDate.of(2026, 7, 15)));
    } catch (Exception e) {
        lblEjemplo.setText("");
    }
}
```

**No hay campo «Siguiente número»**: ya no se guarda.

---

## D6. La sección Series de Configuración

En `Configuracion.fxml`, la sección Series queda como las de IVA y retenciones: la tabla y, debajo, **Nuevo**, **Editar** y **Eliminar**. Fuera la fila de alta rápida entera y la columna «Reutilizar anulados».

| Columna | De dónde sale |
|---|---|
| Código | `getCodigoTexto()` |
| Descripción | `getDescripcion()` |
| Rectificativa | `getRectificativaTexto()` |
| Formato | `getFormatoTexto()` |
| Siguiente (año) | Calculado con `siguienteCorrelativo` para el año de trabajo |

En `ConfiguracionController`: `seleccionarSerie(MouseEvent)` (con doble clic para editar y solo si hay fila), `nuevaSerie`, `editarSerie`, `eliminarSerie`, `abrirFichaSerie(Serie, String)` y `refrescarSeries`. **Se borran** `cargarSeries` con su `StringConverter` anónimo, `actualizarEjemploFormato`, `guardarSerie`, `nuevoSerie`, `seleccionarSerie(Serie)`, `anioTrabajo()` solo si deja de usarse y `codigoOBlanco`.

Con esto, `ConfiguracionController` se queda **sin ninguna clase anónima ni interna**.

La columna «Siguiente (año)» se rellena al refrescar, preguntando por cada serie; el título sigue diciendo el año de trabajo, como hoy.

---

## D7. Base de datos y demostración

`db/crear_tablas.sql`:

```sql
CREATE TABLE IF NOT EXISTS serie (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  codigo TEXT NOT NULL UNIQUE,
  descripcion TEXT,
  es_rectificativa INTEGER NOT NULL DEFAULT 0,
  sufijo_fecha TEXT NOT NULL DEFAULT 'MES'
);
```

Fuera `siguiente_correlativo` y `reutilizar_anulados`, y fuera las tablas `serie_siguiente` y `numero_disponible` enteras.

`db/seed_demo.sql`: los `INSERT` de la serie pierden esas dos columnas y desaparecen los de `serie_siguiente`. En `CopiaSeguridadDAO`, fuera las entradas de `serie_siguiente` y `numero_disponible` y esas dos columnas de `serie`.

Los datos que hay hoy se pueden borrar: el usuario lo ha confirmado.

---

## D8. Solo plumbing

| Dónde | Cambio |
|---|---|
| `Facturas` | Pierde `SerieDAO`, `NumeroDisponibleDAO` y `Numeracion` del constructor. Se borran las tres líneas que actualizaban contadores al crear una factura y el apunte del hueco al borrarla. El resto pasa por el controlador del negocio: `Series.getSeries()` |
| `Estados` | Igual: `Series.getSeries().correlativoOcupado(...)` y `buscar(...)` |
| `Rectificativas` | `Series.getSeries().listado()` |
| `FacturacionMensual` | `Series.getSeries().proponerNumeros(...)` |
| `EditorController` | `listadoSeries`, `buscarSerie`, `formarNumero`, `parseCorrelativo`, `siguienteCorrelativo` y, en `pedirHueco`, `huecosDeSerie` con un `for` en lugar del stream |
| `GenerarFacturasMensualesController` | `listadoSeries` y `proponerNumeros` |
| `HistoricoController` | `listadoSeries` |

Las clases de negocio que cambian de constructor arrastran a `Modelo` y a sus tests, como en los módulos anteriores.

---

## D9. Tests

- **`SerieTest`, nuevo**: código vacío que vale, código con espacios o símbolos que falla, código que se guarda en mayúsculas, formato nulo que falla, getters de texto, constructor copia y `errorX` que coinciden con sus setters.
- **`SeriesTest`, nuevo**, contra una base temporal, con lo que hoy prueban `NumeracionTest` y `SerieDAOTest`: alta, listado, modificar, baja, baja con facturas que falla, código repetido, dos series sin código, los tres formatos de `formarNumero`, `parseCorrelativo` de los tres formatos y de uno que no encaja, siguiente correlativo con y sin facturas, siguiente correlativo por año, que una anulada no libera su número, huecos tras borrar, `proponerNumeros` con y sin huecos, y `correlativoOcupado`.
- **Se borran** `NumeracionTest` y `sqlite/SerieDAOTest`.
- **Se adaptan** `FacturasTest`, `EstadosTest`, `HistorialTest`, `FacturacionMensualTest`, `ClientesTest` y `EmpresasTest`: crean sus series con el constructor y sin los campos que desaparecen.
- `CargaPantallasTest`: se añade `FichaSerie.fxml`.

---

## Decisiones

**1. El siguiente número se calcula, no se guarda.** Un contador guardado se desincroniza al borrar una factura o al escribir un número a mano, y por eso hacían falta la corrección con «el mayor ocupado + 1» y la tabla de huecos. Preguntando a las facturas hay una sola regla y nada que mantener. Para empezar una serie en el 56 al venir de otro programa, se escribe el número a mano en la primera factura.

**2. Fuera «Reutilizar anulados».** Era la única pieza que permitía dos facturas con el mismo número. VeriFactu lo prohíbe y quitarlo ahora simplifica el cálculo.

**3. Los huecos se siguen ofreciendo**, pero calculados: son los correlativos que faltan por debajo del mayor.

**4. Las series con facturas siguen siendo editables**, como hoy. El usuario quiere que en el futuro una serie con facturas se mantenga durante todo el ejercicio; se hará más adelante.

**5. El código de la serie sigue pudiendo estar vacío**, con la regla de que solo una puede estarlo, que vive en `Series` porque necesita ver las demás.

## Riesgos y renuncias

- **La numeración de los datos que ya existan se recalcula**: si una serie tenía el contador en 20 pero su última factura es la 12, la siguiente pasa a ser la 13. Los datos actuales son de prueba y se pueden borrar.
- **«Siguiente número» deja de poder editarse.** Para empezar en un número concreto se escribe a mano en la primera factura del año.
- **Las consultas de numeración siguen mirando la última versión de cada factura** hasta el change siguiente, que quita las versiones. Quedan en un método privado, con su comentario, para que ese cambio sea de una sola consulta.
- **Se toca `Facturas` sin rehacerla**: solo pierde los contadores y cambia a quién pide los números. Se rehace entera en el change siguiente.
