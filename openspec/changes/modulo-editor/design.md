## Situación de partida

| Pieza | Hoy |
|---|---|
| `EditorController` | 1.797 líneas: ~135 de campos, ~390 de carga y buscador de clientes, ~230 de tabla y salto entre celdas, ~290 de las seis clases de celda, ~155 de guardar, ~190 de estados y PDF y ~165 de ayudas sueltas |
| Estilo | ~45 ternarios; `StringConverter` anónimos en el buscador y en la retención; `Task` anónimo y `new Thread` en `exportarPdf`; `instanceof Cliente cli` y `instanceof TextInputControl tic`; `javafx.event.EventHandler` escrito entero y guardado en un campo |
| Restos | `DIAGNOSTICO_FOCO` y `trazarFoco` con `System.out`; `// Recálculo por tipo de cambio (6.4)`; separadores `// ------`; `catch (Exception ignored)` vacíos; mensajes con prefijo («Error al guardar: …») |
| Cliente | `clienteDeFormulario` copia siempre el cliente elegido (`new Cliente(clienteActual)`), aunque se le haya cambiado el NIF. Valida con `ValidacionCliente` y avisa con `avisarPrimerErrorCliente`, que repite lo que hace la ficha de cliente |
| Número | `txtNumero` se activa y desactiva desde `actualizarBotonesEstado` y desde `setEditable`. `pedirHueco` pregunta aunque el número se haya escrito a mano y lo pisa |
| Piezas de fuera | `ResumenFactura.IvaGrupo` (clase dentro de otra), `LineaFactura()` vacío y sin validar, `ValidacionCliente`, `Reloj` (`getModelo().getReloj()` desde cuatro pantallas) |

## Objetivos y lo que queda fuera

**Objetivo**: el editor hace lo mismo que hoy, más lo que decide este change (el cliente por NIF, el aviso del cliente nuevo, el número libre al guardar y la descripción de tres renglones), escrito para que lo pueda defender un alumno de 1º de DAM, y sin ninguna de las cuatro piezas de fuera que no cumplen las normas.

**Fuera**: el aspecto del editor (el FXML solo cambia si lo pide una tarea); el `Clock` de `CopiaSeguridad`, que se queda para `modulo-copias`; el histórico, el PDF, las mensuales y las copias, que solo se adaptan a `GrupoIva`, a `LineaFactura` y a la fecha de trabajo.

---

## D1. Un controlador que se lee como un índice

Un único `EditorController`. El orden dentro de la clase, el de `AGENTS.md`:

1. Constantes (`PREF_SERIE`, `PREF_CARPETA`, `PREF_EXPORTACION`), campos `@FXML` y campos propios.
2. `initialize`, que solo llama a métodos con nombre, uno por línea:
   ```java
   cargando = true;
   cargarLogo();
   cargarSeries();
   ponerFechaInicial();
   cargarTiposIva();
   cargarRetenciones();
   prepararBuscadorCliente();
   prepararTablaLineas();
   prepararMatriz();
   vigilarCambios();
   empezarFacturaNueva();
   cargando = false;
   actualizarTotales();
   ```
3. `alMostrar` (los atajos), `cargarFactura` (la llama el histórico) y `puedeCerrar`.
4. Los métodos de los botones, en el orden de la pantalla: `guardar`, `nuevaFactura`, `exportarPdf`, `crearRectificativa`, `anular`, `restaurar`, `volver`, `anadirLinea`, `eliminarLinea`.
5. Los privados de ayuda, agrupados por lo que tocan y en este orden: cliente, número, líneas, totales y estado.
6. Al final, las clases de celda (la única excepción de `AGENTS.md` a «una clase por fichero»).

Sin separadores `// ------`. Ningún método pasa de unas 40 líneas. Un `catch` vacío solo donde perder el dato no importa (el logo, la preferencia de la última serie); si no, se avisa con `Dialogos` y `e.getMessage()`, sin prefijos.

**Cambios sin guardar**: todos los campos avisan a un único método.

```java
private void cambiado() {
    if (!cargando) {
        modificado = true;
    }
}
```

Los listeners que además hacen algo llaman a su propio método con nombre: `cambiarSerie()`, `cambiarFecha()`, `cambiarDescuento()`, `cambiarRetencion()` y `ajustarObservaciones()`. Sus parámetros, con nombre: `(propiedad, anterior, nuevo)`.

**Un solo sitio para el estado**: `setEditable` y `actualizarBotonesEstado` se juntan en `aplicarEstado()`, que se llama al empezar una factura nueva y al final de `cargarFactura`. Decide todo lo que depende de si hay factura abierta y de si está emitida: botones visibles, campos activos, la tabla y `txtNumero`, que **solo** se toca aquí (desactivado si hay factura abierta o si no se puede editar).

## D2. El cliente de la factura

**Buscador**: el `ComboBox<Cliente>` editable necesita un conversor, que pasa a su propio fichero, `vista/utilidades/ConversorCliente extends StringConverter<Cliente>`:

- Recibe en el constructor el `ComboBox<Cliente>` del que lee la lista.
- `toString(cliente)`: `""` si es `null`; si no, `cliente.getNombreNif()`.
- `fromString(texto)`: el cliente de la lista cuyo nombre o NIF coincide con el texto, sin distinguir mayúsculas, o `null`.

El combo de la retención **no** lleva conversor: no es editable y usa el `toString()` de `TipoRetencion`.

**Comprobar los datos al guardar**, como la ficha de cliente (`FichaClienteController.marcarCamposMalos`): se quitan las marcas, se revisa cada campo con `Cliente.errorNombre`, `errorNif`, `errorDireccion`, `errorCp`, `errorLocalidad`, `errorProvincia` y `errorEmail`, se marcan en rojo todos los incorrectos con la clase CSS `campo-error` (fuera los `setStyle`), y se muestra **un solo aviso**, el del primero, con el título «Datos del cliente». Si los siete campos están vacíos, el aviso es «Indique los datos del cliente.».

**El NIF identifica al cliente.** `clienteDeFormulario()` crea siempre un `new Cliente(...)` con los campos (ya no copia el elegido) y después:

```java
if (clienteActual != null && cliente.getNif().equals(clienteActual.getNif())) {
    cliente.setId(clienteActual.getId());
    cliente.setActivo(clienteActual.isActivo());
}
```

`clienteActual` es el último cliente elegido en el buscador o el de la factura abierta. Con otro NIF, el cliente de la factura se queda sin id.

**`confirmarCliente(cliente)`**, antes de guardar:

1. Si tiene id (mismo NIF que el elegido), sigue.
2. Si no, `buscarClientePorNif(nif)`: si existe, `cliente.setId(existente.getId())` y sigue, sin preguntar ni tocar su ficha (como ya dice la especificación).
3. Si no existe, pregunta con `Dialogos.mostrarDialogoConfirmacion`:
   - Título: `Cliente nuevo`
   - Texto: `String.format("El cliente %s (%s) no está en tu lista.%n%nSe guardará en ella junto con la factura.", nombre, nif)`
   - Aceptar: sigue, y el cliente lo da de alta `Facturas` dentro de la misma transacción de la factura (`asegurarCliente`, que ya existe).
   - Cancelar: `guardar` termina sin guardar nada y **sin tocar los campos**.

**`pedirActualizarFicha(cliente)`** pregunta, con el texto de hoy, solo si el cliente tiene el mismo id que `clienteActual` y `!cliente.tieneLosMismosDatos(clienteActual)`. Si se acepta, después de guardar la factura se llama a `modificarCliente(cliente)`.

## D3. El número y el número libre

- `recalcularNumero()` guarda en el campo `numeroPropuesto` el texto que pone en `txtNumero`.
- Al guardar una factura **nueva**, se ofrece el número libre solo si `txtNumero.getText().trim().equals(numeroPropuesto)`. Si el usuario lo ha escrito a mano, se respeta sin preguntar.
- `numeroLibre(serie, fecha)` devuelve el menor de `huecosDeSerie(serie, fecha)`, o `null` si no hay ninguno.
- Si hay número libre, `Dialogos.mostrarDialogoNumeroLibre(libre, propuesto)`, con los dos números ya formados con `formarNumero`. Si devuelve `true`, `txtNumero` pasa a tener el número libre y se guarda con él.
- El número ocupado lo sigue rechazando `Facturas` con su mensaje de siempre, «El número %s ya lo tiene otra factura de la serie %s.».

Método nuevo en `vista/utilidades/Dialogos`:

```java
public static boolean mostrarDialogoNumeroLibre(String libre, String propuesto)
```

- Título: `Número de factura`
- Texto: `String.format("El número %s está libre.%n%n¿Quieres usarlo o continuar con el %s?", libre, propuesto)`
- Botones: `"Usar " + libre` (`ButtonData.OK_DONE`, el de por defecto) y `"Continuar con " + propuesto` (`ButtonData.CANCEL_CLOSE`).
- Devuelve `true` si se pulsa el primero; cerrar el aviso con la X o con Esc es continuar.
- Con el mensaje, el icono, el tema y el icono de ventana puestos igual que en `mostrarDialogoConfirmacion`.

## D4. La tabla de líneas y sus celdas

**Columnas con `PropertyValueFactory`**, sobre getters de texto nuevos de `LineaFactura` (ver D6):

| Columna | Getter |
|---|---|
| Cant. | `cantidadTexto` |
| Descripción | `descripcion` |
| Precio | `precioUnitarioTexto` |
| Total | `totalBaseTexto` |
| IVA | `ivaNombre` |

Las celdas enseñan el texto que les llega (`getItem()`) y solo buscan la `LineaFactura` de su fila (`getTableRow().getItem()`) para aplicar lo escrito. Después de aplicar, `tablaLineas.refresh()` vuelve a pedir los textos.

**Clases de celda**, dentro del controlador, con un párrafo «Cómo funciona» sobre la primera que explique `setCellFactory`, `startEdit`, `commitEdit`, `cancelEdit` y `updateItem`:

- `CeldaTexto` (abstracta, `TableCell<LineaFactura, String>`): editor `TextField`. Enter aplica y salta a la celda siguiente, Esc cancela, y perder el foco aplica sin saltar. Un `boolean` evita aplicar dos veces. Declara `abstract void aplicar(LineaFactura linea, String texto) throws Exception`, y si `aplicar` lanza, la línea se queda como estaba.
- `CeldaCantidad`, `CeldaPrecio` y `CeldaTotal` extienden `CeldaTexto` y solo implementan `aplicar`. El total, con «Total de línea con IVA incluido» marcado, calcula hacia atrás como hoy (`Calculos.baseDesdeTotalConIva` y `precioDesdeTotal`).
- `CeldaDescripcion` extiende `CeldaTexto` con otro editor y otra forma de enseñarse:
  - Al editar, un `TextArea` con `setWrapText(true)` y `setPrefRowCount(3)`, de alto fijo; si no cabe, sale su barra.
  - Sin editar, un `Text` con `wrappingWidthProperty().bind(columna.widthProperty().subtract(10))` como `graphic`. La fila crece sola, porque la celda pide el alto del texto partido.
  - Fuera `ajustarAltoEtiqueta`, `atarAltoEditor`, `vertical`, `computePrefHeight` y los `lookup(".text")`.
- `CeldaIva` (`TableCell<LineaFactura, String>`): un `ComboBox<TipoIva>` con los tipos cargados. Al cambiar de valor llama a su método `cambiarIva()`, que aplica `linea.setTipoIva(tipo)` y actualiza los totales. Un `boolean cargando` propio evita que se dispare mientras `updateItem` le pone el valor. **Fuera** el `EventHandler` guardado en un campo y el `setOnAction(null)`.

**Salto con Enter** (`avanzarDesde`): la misma regla que hoy.

- Cantidad → descripción → precio → total.
- Desde el total pasa a la cantidad de la fila siguiente. Si no hay fila siguiente y la actual `tieneContenido()`, se añade una línea nueva; si no tiene contenido, se queda en la misma.
- La celda de destino se abre con **un solo** `Platform.runLater(() -> editarCelda(fila, columna))`, y `editarCelda` hace `scrollTo` y `edit`. El foco lo pone la propia celda en `startEdit` (`editor.requestFocus()`).
- Fuera `editarCeldaSegura`, el `runLater` anidado, el `lookup(".text-field")` y todo `trazarFoco`.
- Un comentario `//` explica por qué hace falta ese `runLater`: la tabla termina de cerrar la celda anterior después de `commitEdit`.

**Teclas de la tabla**, en el método `teclaEnTabla(KeyEvent evento)`: Supr elimina la línea seleccionada; Enter sobre una celda sin editar la abre.

`lineaConContenido` pasa a `LineaFactura.tieneContenido()`.

## D5. Totales, PDF, rectificar, anular y restaurar

- **`actualizarTotales()`** hace lo de `actualizarResumen` sin ternarios. La matriz usa `PropertyValueFactory` con `etiqueta`, `baseTexto` y `cuotaTexto` de `GrupoIva`, y su última fila es `resumen.getFilaTotales()`. Fuera `etiquetaMatriz`.
- **`exportarPdf`**, sin `Task` ni `Thread`:
  1. `FileChooser` como hoy.
  2. Cursor de espera en la escena.
  3. `try`: `new ExportadorPdf().exportar(...)`, guardar la preferencia de la carpeta y avisar «PDF generado en: …».
  4. `catch`: `Dialogos.mostrarDialogoError("Exportar PDF", e.getMessage())`.
  5. `finally`: cursor normal.
- **`crearRectificativa`**, **`anular`** y **`restaurar`** hacen lo mismo que hoy, sin prefijos en los mensajes de error. La fecha de la rectificativa sale de `Vista.getInstancia().getControlador().fechaTrabajo()`.
- **Atajos** (`alMostrar`): Ctrl+S, Ctrl+P, Ctrl+N y Esc, como hoy. Cada lambda solo llama a su método.

## D6. Las cuatro piezas de fuera

**`GrupoIva`** (`modelo/dominio/GrupoIva.java`) sustituye a `ResumenFactura.IvaGrupo`:

- Los mismos campos, getters y setters.
- Constructor `GrupoIva(String nombre, Integer porcentaje, String motivoExencion)`.
- Tres getters de texto:
  - `getEtiqueta()`: las reglas de `etiquetaMatriz`, empezando por la fila «Totales».
  - `getBaseTexto()` y `getCuotaTexto()`, con `Formatos.moneda`.
- `ResumenFactura` guarda `List<GrupoIva>` y añade `getFilaTotales()`, que devuelve `new GrupoIva("Totales", null, null)` con la base total y el IVA total.
- Se adaptan `Calculos`, `ConstructorDocumentoFactura` y `CalculosTest`.

**`LineaFactura`**:

- Fuera el constructor vacío. Nuevo `public LineaFactura(int cantidad, BigDecimal precioUnitario) throws Exception`, que llama a los setters.
- Los setters que validan:
  - `setCantidad`: si es menor que 1, `throw new Exception("La cantidad debe ser 1 o más.")`.
  - `setPrecioUnitario`: si es `null` o negativo, `throw new Exception("El precio no puede ser negativo.")`.
  - `setDescripcion`: `null` pasa a `""`.
- El constructor copia declara `throws Exception`.
- Nuevos:
  - `setTipoIva(TipoIva tipo)`: copia sus cinco datos (id, nombre, porcentaje, motivo de exención y suplido), lo que hoy hace `aplicarIva` en el editor.
  - `tieneContenido()`: descripción no vacía o precio mayor que 0.
  - `getCantidadTexto()`, `getPrecioUnitarioTexto()` y `getTotalBaseTexto()`, con `Formatos.moneda` en los dos importes.
- Se adaptan `Facturas.crearLinea`, `FacturacionMensual` y los tests que construyen líneas.

**`ValidacionCliente`** se borra. Un `Cliente` se valida en sus setters, así que un objeto `Cliente` siempre es correcto. En `Facturas` (dos sitios) y en `FacturacionMensual`, `ValidacionCliente.comprobar(cliente)` pasa a:

```java
if (cliente == null) {
    throw new Exception("Indique los datos del cliente.");
}
```

**`Reloj`** se borra:

- `Sesion.getFechaTrabajo()` devuelve `LocalDate.now()` si no hay fecha (lo que hacía `Reloj.fechaTrabajo()`).
- Nuevas operaciones `Modelo.fechaTrabajo()` y `Controlador.fechaTrabajo()`, de una línea.
- `Modelo` pierde `reloj` y `getReloj()`, y conserva el `Clock` solo para `CopiaSeguridad`.
- Cambian:
  - `EditorController`, `ConfiguracionController` y `MenuPrincipalController`: `getControlador().fechaTrabajo()`.
  - `GenerarFacturasMensualesController`: `LocalDate.now().getYear()`.

## D7. Tests

**Ayudas nuevas en `PruebaDePantalla`**:

- `cancelarAviso()`: como `cerrarAviso`, pero con Esc, que pulsa el botón de cancelar.
- `escribirEnCelda(int fila, String columna, String texto)`, donde `columna` es el `fx:id` de la columna. Pulsa en la celda, Enter para abrirla, escribe y Enter para aplicar y saltar. Si en modo headless el clic y Enter no abren la celda, vale doble clic; nunca `tabla.edit(...)` desde el test.

**`PantallaEditorTest`**, casos nuevos, sin depender del año:

| Caso | Qué comprueba |
|---|---|
| Flujo de teclado | Cantidad 2, descripción, precio 10 y Enter en el total: hay dos líneas y el total contiene «24,20» |
| Descripción larga | Tras una descripción de unas 300 letras, esa fila es más alta que la fila vacía de debajo |
| Cliente nuevo | Cliente escrito a mano con NIF nuevo: sale «no está en tu lista»; al aceptar, «Factura guardada.» y el cliente está en `listadoClientes(true)` |
| Cliente nuevo cancelado | Mismo caso con `cancelarAviso()`: no hay factura nueva en el histórico, el cliente no está en la lista y `#cliNombre` conserva lo escrito |
| Otro NIF | Se elige un cliente de la demo en el buscador y se le cambia el NIF por uno válido que no tenga nadie: sale el aviso de cliente nuevo, no la pregunta de la ficha, y la ficha del elegido no cambia |
| Número ocupado a mano | Se guarda una factura, se empieza otra y se escribe el mismo número: sale «ya lo tiene otra factura» |
| Número libre: usar | Preparación con el controlador: dos facturas de la fecha de trabajo en la serie A y se borra la primera. Nueva factura: sale el aviso con el número libre, `aceptarAviso()` y la factura se guarda con ese número |
| Número libre: continuar | Misma preparación y `cancelarAviso()`: se guarda con el número propuesto |
| Número escrito a mano | Misma preparación, número escrito a mano (con `formarNumero`, el siguiente más 10): no sale el aviso del número libre y se guarda con el escrito |

Los de hoy siguen pasando.

**Negocio y clases de datos**:

- `modelo/dominio/LineaFacturaTest`: cantidad 0, precio negativo, `setTipoIva` copia sus cinco datos, `tieneContenido` y los textos.
- `modelo/dominio/GrupoIvaTest`: la etiqueta de un tipo al 21 %, la de un exento con motivo y la de la fila «Totales».
- `modelo/negocio/SesionTest`: sin fecha de trabajo devuelve la de hoy.
- `FacturasTest`: guardar una factura sin cliente sigue avisando «Indique los datos del cliente.».

## Riesgos y renuncias

- **El salto con Enter se simplifica** (un solo `runLater`). El `runLater` doble existía por un problema de foco en la ventana real que las pruebas headless no ven, así que hay una prueba manual para comprobarlo. Si falla, se puede añadir un segundo paso, explicado en su comentario, pero nunca volver al `lookup` ni al diagnóstico.
- **La descripción ya no crece mientras se escribe**: con más de tres renglones sale la barra. Decidido con el usuario.
- **Cambiar el NIF en la factura ya no corrige la ficha del cliente**: el NIF de una ficha solo se cambia en Clientes. Decidido con el usuario.
- **Una cantidad o un precio no válidos se ignoran**: la línea conserva su valor anterior. Hasta ahora, una cantidad menor que 1 se convertía en 1.
