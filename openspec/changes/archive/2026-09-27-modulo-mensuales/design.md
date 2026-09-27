## Situación de partida

| Pieza | Hoy |
|---|---|
| `GenerarFacturasMensualesController` | 557 líneas. `abrir()` estático con su `FXMLLoader`, `Stage` y `ConfiguracionVentana`; 7 `StringConverter` anónimos; `LineaDialogo` dentro, con `Property`; `generar()` de 121 líneas |
| `FacturacionMensual` | 230 líneas, se crea en `Modelo` con `new`. Dentro: `ModoDia`, `LineaPlantilla` y `Resultado`. Dos `generar` (uno sin usar), `validar` con una comprobación repetida, y la rama de omitir meses que la ventana nunca pide |
| Año | `Spinner` de 5 años atrás a 10 adelante, empezando en `LocalDate.now()` |
| Números libres | `mostrarDialogoConfirmacion`: Aceptar los usa y Cancelar genera sin ellos |

## Objetivos y lo que queda fuera

**Objetivo**: el diálogo hace lo mismo que hoy, más lo decidido: año de trabajo fijo, casilla única «Añadir mes» y números libres con dos botones. Todo según `AGENTS.md`.

**Fuera**: avisar de cambios sin guardar al cerrar el diálogo, y el PDF, que irá con Jasper.

---

## D1. Clases de datos

**`modelo/dominio/ModoDia.java`**: el `enum` que hoy vive dentro de `FacturacionMensual`, en su fichero, con `FIJO`, `PRIMER_DIA` y `ULTIMO_DIA`.

**`modelo/dominio/PlantillaMensual.java`**: todo lo que se elige en el diálogo, validado en sus setters como las demás clases de datos.

- Campos:
  - `Cliente cliente`, `Serie serie`, `TipoIva tipoIva` y `TipoRetencion retencion` (esta última puede ser `null`);
  - `int mesInicio`, `int mesFin`, `ModoDia modoDia` e `int diaFijo`;
  - `List<LineaFactura> lineas` y `boolean anadirMes`.
- Constructor con lo obligatorio:
  ```java
  public PlantillaMensual(Cliente cliente, Serie serie, int mesInicio, int mesFin, ModoDia modoDia,
                          int diaFijo, TipoIva tipoIva, List<LineaFactura> lineas) throws Exception
  ```
  La retención y «Añadir mes» se ponen después con su setter.
- Mensajes de los setters:

| Setter | Cuándo lanza | Mensaje |
|---|---|---|
| `setCliente` | `null` o sin id | `Seleccione un cliente.` |
| `setSerie` | `null` | `Seleccione una serie.` |
| `setMesInicio` / `setMesFin` | fuera de 1 a 12 | `El mes debe estar entre enero y diciembre.` |
| `setMesInicio` / `setMesFin` | inicio posterior a fin, con los dos puestos | `El mes de inicio debe ser anterior o igual al mes de fin.` |
| `setModoDia` | `null` | `Elija el día del mes.` |
| `setDiaFijo` | con modo `FIJO` y fuera de 1 a 31 | `El día del mes debe estar entre 1 y 31.` |
| `setTipoIva` | `null` | `Seleccione un tipo de IVA.` |
| `setLineas` | `null` o vacía | `Añada al menos una línea con descripción.` |

- `setMesInicio` y `setMesFin` comprueban el otro si ya está puesto, como `FiltrosHistorial`: da igual el orden.
- `setLineas` guarda una copia de la lista.
- Un método propio: `getCantidadMeses()`, que devuelve `mesFin - mesInicio + 1`.

Las líneas de la plantilla son `LineaFactura`: la cantidad y el precio ya se validan en `LineaFactura`.

**`Formatos.nombreMes(int mes)`**: el nombre del mes en español y en minúscula («enero»). Hoy se calcula con `Month.getDisplayName` en dos sitios; pasa a `Formatos`, que es quien decide cómo se escribe un texto.

## D2. `FacturacionMensual`

Singleton con `getFacturacionMensual()`, como el resto del negocio. El año es siempre el de `Sesion.getSesion().getFechaTrabajo().getYear()`.

- `public List<String> mesesConFactura(PlantillaMensual plantilla) throws Exception`: los nombres de los meses del rango en que ese cliente ya tiene factura en el año de trabajo. Sustituye a `detectarDuplicados`.
- `public int generar(PlantillaMensual plantilla, boolean usarLibres) throws Exception`:
  - crea **todos** los meses del rango, sin omitir ninguno;
  - pide los números con `Series.getSeries().proponerNumeros(serie, anio, cantidad, usarLibres)`;
  - construye una `Factura` por mes y las guarda juntas con `Facturas.getFacturas().altaVarias(...)`, todas o ninguna;
  - devuelve cuántas ha creado.
- Cada factura:
  - lleva la fecha del mes según el modo de día (el día fijo se ajusta al último día del mes);
  - lleva una copia del cliente y la retención (o ninguna);
  - lleva una copia de cada línea en su orden, con el tipo de IVA puesto con `LineaFactura.setTipoIva`;
  - si `anadirMes` está marcada, la descripción de **todas** las líneas termina en `" - mes de " + Formatos.nombreMes(mes)`.
- Fuera: el `generar` con día fijo, `Resultado`, `LineaPlantilla`, el `ModoDia` de dentro, `validar` (lo hace `PlantillaMensual`), la rama de omitir meses y la comprobación repetida.
- Métodos privados con nombre, de menos de 40 líneas: `fechaDelMes`, `facturaDelMes` y `lineasDelMes`.

**`Controlador` y `Modelo`**:

- `generarFacturasMensuales(PlantillaMensual plantilla, boolean usarLibres)`, que devuelve `int`;
- `mesesConFacturaMensual(PlantillaMensual plantilla)`;
- `proponerNumeros`, que se queda como está.

`Modelo` pierde el campo `facturacionMensual`.

## D3. El aviso de números libres

Método nuevo en `Dialogos`:

```java
public static boolean mostrarDialogoNumerosLibres(String serie, String libres)
```

- Título: `Números libres`.
- Texto: `String.format("La serie %s tiene números libres este año: %s.%n%n¿Quieres usarlos para estas facturas?", serie, libres)`.
- Botones: `Usar los números libres` (`OK_DONE`, el de por defecto) y `Continuar sin ellos` (`CANCEL_CLOSE`).
- Devuelve `true` si se usan; cerrar el aviso es continuar sin ellos.
- Con el mensaje, el icono, el tema y el icono de ventana, como `mostrarDialogoNumeroLibre`.

Se enseña solo si, para esa cantidad de meses, `proponerNumeros(..., true)` y `proponerNumeros(..., false)` dan listas distintas. `libres` son los correlativos de la primera lista que no están en la segunda, separados por comas («3, 5»). `serie` es su `toString()`.

## D4. La ventana

**Abrir**: `GenerarFacturasMensualesController.abrir()` se queda estático, porque lo llaman el menú y el histórico. Por dentro:

1. carga el FXML con `LocalizadorRecursos.class.getResource("GenerarFacturasMensuales.fxml")`;
2. crea la ventana con `Vista.getInstancia().crearVentanaModal(raiz, "Generar facturas mensuales", null)`;
3. se la pasa al controlador y hace `showAndWait()`.

Fuera `ConfiguracionVentana.para(...)` en este sitio; si la entrada `GENERAR_MENSUAL` de `ConfiguracionVentana` se queda sin uso, se borra. El controlador guarda la `Stage` para cerrarse.

**`GenerarFacturasMensuales.fxml`**:

- Imports explícitos, uno por clase.
- Donde estaba el `Spinner` del año, una etiqueta `lblAnio`, que el controlador rellena con el año de trabajo.
- Los tres `RadioButton` comparten un `ToggleGroup` declarado en el FXML (`<fx:define>` y `toggleGroup="$grupoDia"`), y cada uno lleva `onAction="#cambiarModoDia"`.
- La tabla de líneas tiene tres columnas (cantidad, descripción y precio), sin «Añadir mes». Cada columna lleva `onEditCommit="#cambiarCantidad"`, `"#cambiarDescripcion"` o `"#cambiarPrecio"`.
- Debajo de la tabla, `CheckBox fx:id="chkAnadirMes"` con el texto `Añadir el mes a la descripción de las líneas`.

**`GenerarFacturasMensualesController`**, con `initialize` como índice:

```java
lblAnio.setText(String.valueOf(anioDeTrabajo()));
cargarClientes();
cargarSeries();
cargarMeses();
cargarIvas();
cargarRetenciones();
prepararTabla();
empezar();
```

- **Desplegables sin conversor**:
  - `ComboBox<Cliente>`, `ComboBox<Serie>` (sin la rectificativa), `ComboBox<TipoIva>` y `ComboBox<TipoRetencion>` usan su `toString()`. La retención empieza con «Sin retención» (un `TipoRetencion` sin id), como en el editor, y ese elemento significa `null`.
  - Los meses son `ComboBox<String>` con los doce nombres de `Formatos.nombreMes`; el número del mes es la posición más uno.
- **Tabla de líneas**:
  - `TableView<LineaFactura>`, columnas con `PropertyValueFactory` (`cantidadTexto`, `descripcion` y `precioUnitarioTexto`) y `TextFieldTableCell.forTableColumn()`.
  - Los tres métodos `@FXML void cambiarX(TableColumn.CellEditEvent<LineaFactura, String> evento)` aplican el texto a `evento.getRowValue()`.
  - Si el texto no vale (el setter lanza, o `Formatos.parseEntrada` devuelve `null`), la línea se queda como estaba.
  - Después, `tablaLineas.refresh()` y `actualizarInfo()`.
- `empezar()` pone de enero a diciembre, el día fijo 15, una línea vacía (`new LineaFactura(1, BigDecimal.ZERO)`) y la casilla «Añadir mes» marcada, que es lo que hoy trae marcado cada línea.
- `actualizarInfo()`:
  - una factura: `String.format("Se generará 1 factura en %d.", anio)`;
  - varias: `String.format("Se generarán %d facturas en %d.", meses, anio)`;
  - meses al revés: `No se generará ninguna factura.`.

**`generar()`**, partido en métodos con nombre:

1. `leerPlantilla()` junta las líneas con descripción (sin espacios alrededor) y crea la `PlantillaMensual`. Si lanza, avisa con `e.getMessage()` y no sigue.
2. `confirmarMesesConFactura(plantilla)`:
   - si `mesesConFacturaMensual` devuelve meses, pregunta con `mostrarDialogoConfirmacion` y el texto de hoy («Ya existen facturas para este cliente en: … ¿Deseas generar las facturas de todos modos?»);
   - Cancelar no genera nada.
3. `preguntarNumerosLibres(plantilla)`: el aviso de D3 si hay números libres.
4. `generarFacturasMensuales(plantilla, usarLibres)`. Después, `String.format("Se han generado %d facturas.", n)` y se cierra la ventana.

Los errores, con `e.getMessage()` y sin prefijos.

**Histórico**: en `Historico.fxml` el botón «Mensual» pasa a decir `Facturar mes`, como el menú y el escenario «Acceso desde el histórico». Su *tooltip* sobra y se quita.

## D5. Tests

**`PlantillaMensualTest`** (nuevo, `modelo/dominio`):

- cada mensaje de la tabla de D1;
- los meses al revés lanzan pongas primero el que pongas;
- el día fijo solo se comprueba con el modo `FIJO`;
- `getCantidadMeses`.

**`FacturacionMensualTest`**, rehecho sobre `PlantillaMensual`, con el año de `Sesion.getSesion().getFechaTrabajo()`:

- doce facturas para todo el año;
- genera también los meses que ya tenían factura;
- `mesesConFactura`;
- día ajustado a febrero;
- primer y último día;
- IVA y retención en los totales;
- «Añadir mes» en todas las líneas, y sin marcar las descripciones no cambian;
- no guarda nada si falla;
- usa los números libres, y sin usarlos sigue por el siguiente.

Fuera `omiteMesesYaFacturados`.

**`PantallaMensualesTest`**:

- Los cuatro de hoy, con los textos nuevos.
- Casos nuevos:
  - la etiqueta del año enseña el año de hoy y no hay `Spinner` de año;
  - la casilla «Añadir mes» existe y viene marcada;
  - con dos facturas del cliente en la serie y la primera borrada (preparado con el controlador), al generar sale el aviso de números libres con sus dos botones, y `cancelarAviso()` sigue sin usarlos;
  - con un mes que ya tiene factura, el aviso de meses con factura, y con `cancelarAviso()` no se genera nada.
- Para escribir en la tabla, doble clic en la celda, texto y Enter.

## D6. Lo que salió en las pruebas manuales

**El botón del histórico no cabe.** Los botones de la barra de herramientas miden 72 px fijos (`.btn-ribbon` en `base.css`), y «Facturar mes» se corta en todos los temas. En `Historico.fxml` el botón pasa a `text="Fact. mes"` con `<tooltip><Tooltip text="Facturar mes"/></tooltip>`. El requisito «Menú y navegación» ya permite una forma breve si el nombre completo va en el tooltip, así que la especificación no cambia.

**La ventana de facturar mes es ilegible en los temas oscuros.** En `base.css`, `.panel-neutro` y `.panel-neutro > .viewport` fijan `#F6F6F6`. En omarchy, neon y negro-dorado, los textos del tema, que son claros, quedan sobre ese gris claro. Los dos `-fx-background-color: #F6F6F6;` pasan a `-fx-background-color: -fx-control-inner-background;`, el color de las tarjetas de cada tema. En los temas claros apenas cambia (de `#F6F6F6` a blanco). No se toca nada más de `base.css` ni de los temas.

**`TextosCompletosTest` no vio el botón cortado.** Compara `getWidth()` con `prefWidth(-1)`, pero con un ancho fijo en el CSS el ancho preferido es ese ancho fijo, así que la comparación sale bien aunque el texto lleve «…». `comprobarTextos` pasa a mirar también el `Text` que dibuja cada `Labeled`: el texto se da por cortado si `lookup(".text")` es un `Text` con un texto distinto de `getText()`. La comparación de anchos se queda. Antes de arreglar el botón, la prueba nueva tiene que fallar con «Facturar mes» (se comprueba y se apunta en la tarea).

## Riesgos y renuncias

- **Solo el año de trabajo**: para facturar otro ejercicio hay que volver al arranque y elegirlo. Decidido con el usuario.
- **La tabla de líneas usa las celdas de JavaFX**: se edita con doble clic y Enter guarda, sin saltar a la celda siguiente como el editor. Para una plantilla de dos o tres líneas basta, y no hace falta ninguna clase de celda propia.
- **Sin aviso de cambios sin guardar al cerrar**: el diálogo no edita un registro, solo genera facturas.
