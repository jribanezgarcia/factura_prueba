## Situación de partida

| Pieza | Hoy |
|---|---|
| `HistoricoController` | 493 líneas. `initialize` de 49 con las columnas en lambdas, un `setRowFactory` con bloque y el `ContextMenu` construido en Java. Exportar con dos `Task` anónimos y `new Thread`; `ChoiceDialog` para elegir cómo exportar varias |
| Filtros | `comboSerie` de `String` con «(Todas)» reconocido por `startsWith("(")`; `comboEstado` con un `null` como primer elemento. Importes leídos con `Formatos.parseMonedaOpcional`, que convierte un texto mal escrito en 0 |
| `FiltrosHistorial` | Constructor vacío, `serieCodigo` como texto, setters sin comprobar nada |
| Anular y eliminar | Trabajan con un `Set<Long>` de ids y el resumen nombra las facturas por el id |
| `Facturas.listado` | Filtra la serie por `s.codigo = ?`; con `null` crea un `new FiltrosHistorial()` |

## Objetivos y lo que queda fuera

**Objetivo**: el histórico hace lo mismo que hoy, más lo que decide este change: el año de trabajo al entrar, los filtros que se comprueban, «Eliminar» y el resumen con números. Todo escrito según `AGENTS.md`.

**Fuera**: el paquete `pdf` (va en `modulo-pdf`) y el botón «Mensual», que sigue llamando a `GenerarFacturasMensualesController.abrir()` como hoy.

---

## D1. La pantalla

**`Historico.fxml`**:

- Imports explícitos, uno por clase, como `Editor.fxml`. Fuera los `.*`.
- El botón «Eliminar» se queda con su texto, que ya es el bueno.
- La tabla lleva `onMouseClicked="#pulsarTabla"`, su texto de tabla vacía y su menú:
  ```xml
  <placeholder>
      <Label text="No hay facturas con estos filtros." />
  </placeholder>
  <contextMenu>
      <ContextMenu>
          <items>
              <MenuItem onAction="#exportarPdf" text="Exportar a PDF" />
              <MenuItem onAction="#anularSeleccionadas" text="Anular facturas seleccionadas" />
              <MenuItem onAction="#eliminarSeleccionadas" text="Eliminar facturas seleccionadas" />
          </items>
      </ContextMenu>
  </contextMenu>
  ```
- El botón «Eliminar» llama a `#eliminarSeleccionadas` (hoy es `#borrarSeleccionadas`).

**`HistoricoController`**, en el orden de `AGENTS.md`. `initialize` se lee como un índice:

```java
barraController.marcarActivo("historico");
cargarSeries();
cargarEstados();
prepararTabla();
ponerAnioDeTrabajo();
buscar();
```

- `prepararTabla()`:
  - Las nueve columnas con `PropertyValueFactory`, sobre los getters de texto que ya tiene `Factura`: `fechaTexto`, `numero`, `clienteNombre`, `clienteNif`, `baseTexto`, `ivaTexto`, `retencionTexto`, `totalTexto` y `estadoTexto`.
  - La selección múltiple.
- `ponerAnioDeTrabajo()`: con el año de `Vista.getInstancia().getControlador().fechaTrabajo()`, pone `fechaDesde` en el 1 de enero y `fechaHasta` en el 31 de diciembre.
- `@FXML void pulsarTabla(MouseEvent evento)`: con doble clic de botón izquierdo y una fila seleccionada, abre esa factura en el editor, como hace hoy `abrirFactura`. Fuera el `setRowFactory`.
- `alMostrar`: el atajo Ctrl+F, que llama a `buscar()`.

## D2. Los filtros

**Combos de texto con su lista al lado**, sin trucos:

- `comboSerie` es un `ComboBox<String>` con «Todas» y, después, el `toString()` de cada serie. El controlador guarda en un campo la `List<Serie>` que cargó. En `buscar()`:
  ```java
  int posicion = comboSerie.getSelectionModel().getSelectedIndex();
  Serie serie = null;
  if (posicion > 0) {
      serie = series.get(posicion - 1);
  }
  ```
- `comboEstado` es un `ComboBox<String>` con «Todos», «Emitida» y «Anulada». La posición 0 es `null`, la 1 es `EMITIDA` y la 2 es `ANULADA`. Los dos textos salen de `EstadoFactura.label()`.

**`FiltrosHistorial`** (`modelo/dominio`), como las demás clases de datos:

- Campos: `Serie serie` (sustituye a `serieCodigo`), `clienteTexto`, `fechaDesde`, `fechaHasta`, `importeDesde`, `importeHasta` y `estado`.
- Todos son optativos, así que el constructor los recibe todos y llama a los setters:
  ```java
  public FiltrosHistorial(Serie serie, String clienteTexto, LocalDate fechaDesde, LocalDate fechaHasta,
                          BigDecimal importeDesde, BigDecimal importeHasta, EstadoFactura estado) throws Exception
  ```
- `setClienteTexto`: `null` pasa a `""`, y se guarda con `trim()`.
- Dos comprobaciones `static`, como `Cliente.errorNif`, que devuelven el mensaje o `null`:
  - `errorFechas(desde, hasta)`: si las dos están puestas y `hasta.isBefore(desde)`, devuelve `"La fecha desde es posterior a la fecha hasta."`.
  - `errorImportes(desde, hasta)`: si los dos están puestos y `desde.compareTo(hasta) > 0`, devuelve `"El importe desde es mayor que el importe hasta."`.
- `setFechaDesde` y `setFechaHasta` llaman a `errorFechas` con el otro valor que ya tengan, y lanzan `Exception` si hay error. `setImporteDesde` y `setImporteHasta` hacen lo mismo con `errorImportes`. Así da igual el orden en que se llamen.

**`buscar()`**, con el patrón de la ficha de cliente (`FichaClienteController.marcarCamposMalos`):

1. Quita las marcas (`campo-error`) de los dos importes y las dos fechas.
2. Lee cada importe con `leerImporte(TextField campo)`:
   - vacío: `null`, que significa sin filtro;
   - si no, `Formatos.parseEntrada(texto)`, que devuelve `null` cuando el texto no es un número.

   Si un importe escrito no vale, se marca y el primer mensaje es `"El importe desde no es un importe válido."` o `"El importe hasta no es un importe válido."`.
3. `FiltrosHistorial.errorFechas(...)`: si hay error, marca las **dos** fechas.
4. `FiltrosHistorial.errorImportes(...)`: si hay error, marca los **dos** importes.
5. Si hay algún mensaje, sale **un solo** aviso con el primero (`Dialogos.mostrarDialogoError("Histórico", mensaje)`) y no se busca.
6. Si no, crea el `FiltrosHistorial` y rellena la tabla con `listadoFacturas(filtros)`. Un error se avisa con `e.getMessage()`, sin prefijo.

**`Facturas.listado`**:

- Con `null` no crea un filtro vacío: devuelve todas las facturas. Su Javadoc lo dice.
- El filtro de serie pasa a ser `f.serie_id = ?`, con `serie.getId()`.
- En los tests, `new FiltrosHistorial()` pasa a `listado(null)`.

## D3. Anular y eliminar

- Trabajan con la `List<Factura>` seleccionada, sin `Set<Long>`: la selección de una tabla no tiene repetidos.
- `borrarSeleccionadas` pasa a llamarse `eliminarSeleccionadas`.
- Textos:
  - Confirmar eliminar: `String.format("Se van a eliminar %d factura(s) con %d línea(s) en total.%n%n¿Continuar?", facturas, lineas)`.
  - Resumen de eliminar: `Eliminadas: N` y, si alguna falla, `No se han podido eliminar:` seguido de una línea por factura, `String.format("%s: %s", factura.getNumero(), e.getMessage())`.
  - Anular, igual, con `Anuladas: N`, `Ya anuladas: M` y `No se han podido anular:`.
- El resumen se escribe con `String.format` y `+`, sin `StringBuilder`.
- En `Facturas.baja`, el mensaje de la factura con rectificativa pasa a ser `String.format("Tiene la rectificativa %s y no se puede eliminar.", rectificativa)`. En el resumen se leerá «A-1/9: Tiene la rectificativa R-1 y no se puede eliminar.».

## D4. Exportar

**Aviso nuevo en `Dialogos`**, con su tipo en un fichero propio, `vista/utilidades/ExportacionVarias.java`, igual que `CambiosSinGuardar`:

```java
public enum ExportacionVarias { POR_FACTURA, AGRUPADA, CANCELAR }

public static ExportacionVarias mostrarDialogoExportarVarias(int cantidad)
```

- Título: `Exportar PDF`.
- Texto: `String.format("¿Cómo quieres exportar las %d facturas seleccionadas?", cantidad)`.
- Botones:
  - `Un PDF por factura` (`ButtonData.OK_DONE`, el de por defecto);
  - `Todas en un PDF` (`ButtonData.OTHER`);
  - `Cancelar` (`ButtonData.CANCEL_CLOSE`).
- Cerrar con la X o con Esc es `CANCELAR`.
- Con el mensaje, el icono, el tema y el icono de ventana puestos igual que en `mostrarDialogoCambiosSinGuardar`.

**`exportarPdf()`**:

- Sin selección: «Selecciona al menos una factura del histórico.».
- Una factura: `exportarUna`. Varias: el aviso y, según la respuesta, `exportarPorFactura` o `exportarAgrupadas`.
- Cada una carga las facturas completas con `buscarFactura(id)`, porque las filas del listado no llevan líneas.

**Sin `Task` ni `Thread`**: cada método elige el destino y después hace el trabajo con el cursor de espera, como el editor.

```java
Vista.getInstancia().getVentana().getScene().setCursor(Cursor.WAIT);
try {
    ...
} finally {
    Vista.getInstancia().getVentana().getScene().setCursor(Cursor.DEFAULT);
}
```

- `exportarUna`: `FileChooser` con la última carpeta y el nombre propuesto. Al terminar, `"PDF generado en:\n" + ruta`.
- `exportarPorFactura`: `DirectoryChooser`, y un bucle con un contador de generados y una `List<String>` de fallos (`numero: mensaje`). Al terminar, `"%d PDF generados en:%n%s"` y, si hay fallos, `No se han podido generar:` con sus líneas. Fuera el `int[]`.
- `exportarAgrupadas`: `FileChooser` con `facturas.pdf`. Al terminar, `"PDF agrupado generado en:\n" + ruta`.
- Los tres guardan la carpeta en la preferencia `ultima_carpeta_export`, como hoy.
- Los `catch (Exception ignored)` de leer y guardar esa preferencia se quedan con un `//` que diga que, si falla, solo se pierde la carpeta propuesta.

## D5. Tests

**`FiltrosHistorialTest`** (nuevo, `modelo/dominio`):

- sin ningún filtro vale;
- fecha desde posterior a la hasta lanza con su mensaje, las pongas en el orden que las pongas;
- lo mismo con los importes;
- `errorFechas` y `errorImportes` devuelven `null` cuando está bien o falta uno de los dos;
- el texto del cliente se guarda sin espacios alrededor.

**`FacturasTest`**:

- el filtro por un objeto `Serie`;
- `listado(null)` devuelve todas;
- el mensaje nuevo de la factura con rectificativa.

`SeriesTest` y `FacturacionMensualTest` solo cambian `new FiltrosHistorial()` por `null`.

**`PantallaHistoricoTest`**:

- La demostración es de 2026 y la fecha de trabajo de los tests es la de hoy. Por eso los tests que cuentan facturas de la demostración **vacían antes las dos fechas**, con una ayuda privada `quitarFechas()`.
- Casos nuevos:

| Caso | Qué comprueba |
|---|---|
| Al entrar | Las fechas son el 1 de enero y el 31 de diciembre del año de hoy, y la tabla tiene tantas filas como `listadoFacturas` con esos filtros, sin pulsar Buscar |
| Importe mal escrito | «12,5x» en el importe desde y Buscar: el aviso dice «no es un importe válido» y `#txtImporteDesde` tiene la clase `campo-error` |
| Fechas al revés | El aviso dice «posterior a la fecha hasta» y las dos fechas tienen `campo-error` |
| Eliminar rectificada | El resumen contiene «A-1/9» y «R-1» |
| Menú del clic derecho | Clic derecho sobre la tabla: salen «Exportar a PDF», «Anular facturas seleccionadas» y «Eliminar facturas seleccionadas» |
| Exportar varias | Con dos filas seleccionadas, «Exportar» abre el aviso con «Un PDF por factura», «Todas en un PDF» y «Cancelar»; `cancelarAviso()` lo cierra sin abrir ninguna ventana de archivos |

- Los de hoy se adaptan: los textos de «Eliminar», el mensaje nuevo de la rectificada y `quitarFechas()` donde cuenten la demostración.

## D6. El menú del clic derecho, legible en todos los temas

Encontrado en las pruebas manuales. Los botones del menú principal usan la clase de estilo `menu-item`, que es también la que JavaFX pone a cada opción de un `ContextMenu`. Cada tema pinta `.menu-item:hover` con un fondo pensado para esos botones, y JavaFX pone el texto de la opción marcada en blanco o negro pensando en el color principal del tema. Resultado, medido en los siete temas al pasar el ratón por una opción: en cinco el contraste es de 1,2 a 1,4 (ilegible).

**Arreglo**: nuestra clase pasa a llamarse `opcion-menu`, un nombre que JavaFX no usa.

- `vista/recursos/MenuPrincipal.fxml`: los siete `styleClass="menu-item"` pasan a `styleClass="opcion-menu"`.
- `vista/recursos/temas/base.css` y los siete `tema-*.css`: todas las reglas `.menu-item` (con `:hover`, `.icono`, `.nombre` y `.descripcion`) pasan a `.opcion-menu`. Nada más cambia en esos ficheros, y el `.root` de los temas no se toca.
- `vista/utilidades/Botones.java`: el `contains("menu-item")` pasa a `contains("opcion-menu")`.

Con el arreglo, la opción marcada del menú del clic derecho usa el color principal de cada tema, con un contraste de entre 4,6 y 8,3 (lo mínimo legible es 4,5). El menú principal se ve igual que antes.

## Riesgos y renuncias

- **Exportar muchas facturas congela la ventana** mientras se generan los PDF, con el cursor de espera. `AGENTS.md` no permite hilos, y el aviso de cómo exportar hace de confirmación antes de la operación larga.
- **Los títulos de dos escenarios siguen diciendo «Borrar»**: OpenSpec no deja renombrar un escenario en un `MODIFIED`. El texto de dentro dice «Eliminar».
- **El doble clic en el hueco vacío de la tabla** abre la fila que estuviera seleccionada. Es el precio de usar `onMouseClicked` en la tabla, como en Biblioteca8, en lugar de un `setRowFactory`.
