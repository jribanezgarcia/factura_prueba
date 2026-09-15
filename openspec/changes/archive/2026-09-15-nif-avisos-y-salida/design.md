## Context

Estado a 15/09/2026 (commit `4d7be66`). Números de línea orientativos.

**Validadores** (`cabofactu.utilidades`):

- `ValidadorDocumentoFiscal.esValido(String)`: vacío → `true` (opcional); si no, DNI `\d{8}[A-Z]`, NIE `[XYZ]\d{7}[A-Z]` o CIF `[ABCDEFGHJKLMNPQRSUVW]\d{7}[0-9A-J]` con su letra o carácter de control. Solo devuelve `boolean`. Lo usan también los datos de empresa (`Configuracion.datosPendientes`), que comprueban el vacío por su cuenta.
- `ValidadorCodigoPostal.esValido(String)`: vacío → `false`; cinco dígitos entre 01 y 52.
- `ValidadorEmail.esValido(String)`: vacío → `true`.

**Ficha de cliente** (`ClientesController.construirFicha`, ~195-367): crea los campos como variables locales. Para NIF, CP y email usa `BooleanSupplier`, `boolean[] avisando…`, `Runnable avisar…Invalido` con `Dialogos.error` + `Platform.runLater(campo::requestFocus)`. NIF y email avisan **al perder el foco** (de ahí que el clic en Cancelar se pierda); CP solo al Guardar. Hay **tres** `addEventFilter` sobre Guardar, uno por campo, que pueden mostrar hasta tres avisos. Etiquetas: solo `Nombre*`. `nuevo()` y `editar()` llaman a `modelo.getClientes().insertar/actualizar` y capturan `Exception` («No se pudo guardar el cliente: …»).

**Editor** (`EditorController`): `validarNifCliente(boolean avisar)` (~579) pone el borde rojo y, al perder el foco, muestra el aviso y devuelve el foco con `runLater`, protegido por el campo `corrigiendoNif`. CP y email no se validan. `guardar()` (~989) llama a `validarNifCliente(true)` y comprueba solo el nombre («Indique el nombre del cliente.»). `clienteDeFormulario()` (~1071) devuelve `null` si todos los campos están vacíos, y la factura se guarda sin cliente. Etiquetas en `Editor.fxml` (~145-157) sin asterisco.

**Negocio**: `Clientes.insertar/actualizar` no validan. `Facturas.crearFacturaSinTransaccion` (~110) y `Facturas.guardarEditada` (~169) aceptan `cliente == null`. `FacturacionMensual.validar` (~96) solo exige un cliente existente. `Rectificativas.crearRectificativa` pasa por `Facturas.crearFactura`.

**Spec**: requisito «Clientes» (`openspec/specs/invoicing/spec.md:8-30`): «El NIF será opcional» y escenario «mantiene el foco en el campo».

**Datos**: `seed_demo.sql` tiene el cliente «Otro Cliente S.L.» con `B77777777`, que no es válido (también en las facturas A-3/9 y A-4/9). Tests con NIF no válido **por descuido**: `EditorIvaInactivoTest` (`12345678A`) y `CopiaSeguridadTest` (`B12345678`, `A11111111`, `Z00000000`). Tests con NIF no válido **a propósito** (se quedan): `ValidadorDocumentoFiscalTest`, `ClientesValidacionNifTest`, `EditorValidacionNifTest` y `ConfiguracionTest.nifNoValidoDevuelveNif`. Ocho facturas de prueba se crean con cliente `null` (`FacturasTest` ×6, `EstadosTest` ×2… ver D8).

**Decisiones del usuario (15/09/2026)**: textos exactos de los avisos; NIF, dirección, CP, localidad y provincia obligatorios; email opcional validado si se escribe; factura sin cliente no permitida; misma `ValidacionException` con mensaje por caso (no una excepción por error); los datos se van a reiniciar, no hay migración.

## Goals / Non-Goals

**Goals:** avisos distintos para el NIF; no quedarse atrapado en ningún campo; datos obligatorios del cliente comprobados en el negocio por todos los caminos; un solo aviso al guardar; datos de prueba válidos.

**Non-Goals:**
- Datos de empresa en Configuración (ya exigen NIF, CP, etc.; no cambian).
- Pasar la ficha de cliente a FXML (change posterior).
- Cambiar construcciones que prohíbe `AGENTS.md` fuera de las líneas que se tocan (ternarios existentes, `Optional` de `showAndWait`, streams…).
- Tratar datos antiguos incompletos: el programa está en desarrollo y los datos se reinician.
- Validar el NIF del cliente contra la AEAT o comprobar que exista.

## Decisions

### D1. `ValidadorDocumentoFiscal`: saber por qué falla

Se añaden dos métodos `public static` y `esValido` pasa a usarlos, sin cambiar su resultado:

| Método | Devuelve `true` si… |
|---|---|
| `formatoCorrecto(String valor)` | tras quitar espacios y pasar a mayúsculas, tiene forma de DNI (`\d{8}[A-Z]`), NIE (`[XYZ]\d{7}[A-Z]`) o CIF (`[ABCDEFGHJKLMNPQRSUVW]\d{7}[0-9A-J]`). Vacío o `null` → `false` |
| `letraCorrecta(String valor)` | tiene formato correcto **y** la letra (DNI, NIE) o el carácter de control (CIF) coinciden con el cálculo actual. Vacío, `null` o formato incorrecto → `false` |
| `esValido(String valor)` | *(igual que hoy)* vacío o `null` → `true`; si no, `formatoCorrecto(valor) && letraCorrecta(valor)` |

La lógica de la letra y del CIF no cambia; solo se reparte. `ConfiguracionTest` debe seguir pasando sin tocarlo.

Ejemplos (comprobados con el validador actual): `12345678Z`, `X1234567L`, `B12345674` → los dos `true`. `12345678A`, `B12345678` → formato `true`, letra `false`. `123`, `ABCD`, `1234567` → formato `false`.

### D2. `ValidacionCliente`: la regla, en un solo sitio

Clase nueva `cabofactu.modelo.negocio.ValidacionCliente`, herramienta con métodos `static` y constructor privado (no guarda datos, como `Dialogos` o `Conexion`). Javadoc corto en la clase y en cada método público, en primera persona del plural.

Un método por campo que **devuelve el mensaje de error o `null`** si está bien, y uno que comprueba el cliente entero:

| Método | Condición | Mensaje |
|---|---|---|
| `errorNombre(String)` | vacío | `Indique el nombre del cliente.` |
| `errorNif(String)` | vacío | `El NIF/NIE es obligatorio.` |
| | formato incorrecto | `Formato NIF/NIE incorrecto. Debe ser como 12345678Z (DNI), X1234567L (NIE) o B12345674 (CIF).` |
| | letra o carácter final incorrecto | `La letra no es correcta.` |
| `errorDireccion(String)` | vacío | `La dirección del cliente es obligatoria.` |
| `errorCodigoPostal(String)` | vacío | `El código postal es obligatorio.` |
| | no válido | `El código postal debe tener cinco dígitos y comenzar entre 01 y 52.` |
| `errorLocalidad(String)` | vacío | `La localidad del cliente es obligatoria.` |
| `errorProvincia(String)` | vacío | `La provincia del cliente es obligatoria.` |
| `errorEmail(String)` | no vacío y no válido | `Revise el formato del correo electrónico.` |
| `comprobar(Cliente cliente)` | `cliente == null` | `Indique los datos del cliente.` |
| | si no: el **primer** error en este orden: nombre, NIF, dirección, CP, localidad, provincia, email | lanza `ValidacionException` con ese mensaje |

«Vacío» es `null` o solo espacios. `comprobar` declara `throws ValidacionException`.

Descartado: una excepción por error (`NifVacioException`…). El usuario eligió la misma `ValidacionException` con mensaje, como `throw new Exception("…")` en Biblioteca8.

Descartado: meter la regla dentro de `Clientes` y darle `Clientes` a `Facturas` por constructor. Obliga a cambiar el constructor de `Facturas` y sus tests; una herramienta sin estado es más simple.

### D3. `Clientes`

`insertar(Cliente c)` y `actualizar(Cliente c)` llaman primero a `ValidacionCliente.comprobar(c)` y declaran `throws ValidacionException`.

### D4. `Facturas`

- `crearFacturaSinTransaccion(...)`: primera instrucción `ValidacionCliente.comprobar(cliente);` (antes de `validar(lineas, descuento)`). Cubre el Editor, la generación mensual y las rectificativas.
- `guardarEditada(...)` (la sobrecarga que hace el trabajo, ~169): primera instrucción `ValidacionCliente.comprobar(cliente);`.
- El resto del código que admite `cliente == null` no se toca (ya no llegará `null`).

### D5. `FacturacionMensual.validar`

Tras la comprobación actual de cliente existente, añadir `ValidacionCliente.comprobar(cliente);`. Así avisa **antes** de empezar a generar, con el mensaje del primer dato que falte. `GenerarFacturasMensualesController` ya muestra `ValidacionException` con `Dialogos.error("Generar", e.getMessage())`.

### D6. Ficha de cliente (`ClientesController`)

**Etiquetas**: `Nombre*`, `NIF*`, `Dirección*`, `CP*`, `Localidad*`, `Provincia*`, `Email`.

**Campos**: los `TextField` de la ficha (`txtNombre`, `txtNif`, `txtDireccion`, `txtCp`, `txtLocalidad`, `txtProvincia`, `txtEmail`) pasan de variables locales de `construirFicha` a **campos privados** de `ClientesController`, creados en `construirFicha`. Así la validación va en métodos privados sin pasar siete parámetros. Se conservan los `setId` actuales (`txtNombreFicha`, `txtNifFicha`, `txtCpFicha`, `txtEmailFicha`, `btnGuardarFicha`), que usan los tests.

**Se quitan** de la ficha: los tres `BooleanSupplier`, los tres `boolean[] avisando…`, los tres `Runnable avisar…Invalido`, los tres `Platform.runLater(…requestFocus)` y los tres `addEventFilter` de Guardar.

**Métodos privados nuevos**:

| Método | Qué hace |
|---|---|
| `marcarCampo(TextField campo, String error)` | borde rojo (`-fx-border-color: #d32f2f; -fx-border-width: 2;`, el mismo de hoy) si `error != null`; sin estilo si `error == null` |
| `marcarCamposFicha()` | llama a `marcarCampo` con cada campo y su `ValidacionCliente.errorX(...)` |
| `avisarPrimerErrorFicha()` | recorre los campos en el orden de D2; en el primero con error muestra **un** `Dialogos.error(titulo, mensaje)` y devuelve `true`; si no hay ninguno devuelve `false` |

Títulos del aviso: NIF → `NIF no válido`; CP → `Código postal no válido`; email → `Correo electrónico no válido`; nombre, dirección, localidad y provincia → `Datos del cliente`.

**Comportamiento**:

**Decidido por el usuario el 15/09/2026, tras aplicar la primera versión: todo se comprueba solo al guardar, como en Biblioteca8.**

- Al **salir de un campo** o al pulsar **Enter** en él: **no pasa nada**. Ni aviso, ni rojo, ni se devuelve el foco. No hay escuchadores de foco (`focusedProperty().addListener`) ni `setOnAction` en los campos de la ficha.
- **Guardar**: un **único** `addEventFilter(ActionEvent.ACTION, e -> comprobarAntesDeGuardar(e))`. `comprobarAntesDeGuardar` llama a `marcarCamposFicha()` (rojo en todos los incorrectos, sin rojo en los correctos) y, si `avisarPrimerErrorFicha()` devuelve `true`, hace `e.consume()`.
- El rojo de un campo se actualiza en cada intento de guardar.
- **Cancelar** y cerrar la ficha funcionan siempre.
- La regla del botón Guardar desactivado hasta que haya nombre se queda como está.

Descartado: marcar en rojo al salir de cada campo y avisar con Enter. La primera versión aplicada marcaba todos los campos al salir de cualquiera (medio formulario en rojo antes de rellenarlo) y añadía escuchadores por campo. Avisar solo al guardar es más simple y es como lo hacía el usuario en Biblioteca8.

Descartado: comprobar en los `set` de `Cliente` (como `Autor.setNombre` en Biblioteca8). El DAO crea `Cliente` al leer de la base y el Editor lo rellena campo a campo: un `set` que lanza haría fallar pantallas enteras, solo daría el primer error y obligaría a añadir `throws` en muchos sitios.

**`nuevo()` y `editar()`**: añadir `catch (ValidacionException e)` antes del `catch (Exception e)` actual, con `Dialogos.error("Datos del cliente", e.getMessage())`. Es la red de seguridad: normalmente la ficha ya ha avisado.

### D7. Editor (`EditorController` y `Editor.fxml`)

**Etiquetas** en `Editor.fxml`: `Nombre*`, `NIF*`, `Dirección*`, `CP*`, `Localidad*`, `Provincia*`; `Email` sin asterisco.

**Se quitan**: `validarNifCliente(boolean)`, el campo `corrigiendoNif`, su `Platform.runLater(cliNif::requestFocus)` y su aviso al perder el foco.

**Métodos privados nuevos**, con la misma idea que D6: `marcarCampo(TextField, String)`, `marcarCamposCliente()` y `avisarPrimerErrorCliente()`, sobre `cliNombre`, `cliNif`, `cliDireccion`, `cliCp`, `cliLocalidad`, `cliProvincia` y `cliEmail`, con los títulos de D6.

**Comportamiento**:

- Al **salir de un campo** o pulsar **Enter** en él: **no pasa nada** (mismo criterio que D6). Sin escuchadores de foco ni `setOnAction` de validación en los campos del cliente.
- **`cargarDatosCliente(Cliente)`**: tras rellenar los campos, quitar el rojo de todos (`marcarCampo(campo, null)`), para que no quede el rojo de un intento de guardar anterior al elegir otro cliente.
- **`guardar()`**: sustituir `if (!validarNifCliente(true)) { return false; }` y la comprobación del nombre por:
  1. `marcarCamposCliente();`
  2. si todos los campos del cliente están vacíos: `Dialogos.error("Datos del cliente", "Indique los datos del cliente.")` y `return false`;
  3. si `avisarPrimerErrorCliente()` devuelve `true`: `return false`.
- `clienteDeFormulario()` no cambia; `Facturas` rechaza igualmente `null` (D4) y el `catch (ValidacionException e)` que ya existe en `guardar()` muestra el mensaje.
- **Volver**, **Nueva** y el resto de botones funcionan con un dato mal, porque nada retiene el foco. Si hay cambios sin guardar, sale el aviso de siempre.

### D8. Datos de demostración y tests

**`src/main/resources/db/seed_demo.sql`**: `B77777777` → `B77777779` en la ficha de «Otro Cliente S.L.» y en sus facturas A-3/9 y A-4/9 (tres apariciones).

**Cliente de prueba válido** para los tests que necesiten uno: nombre `Cliente Prueba`, NIF `12345678Z`, dirección `Calle Prueba 1`, CP `28001`, localidad `Madrid`, provincia `Madrid`.

| Test | Cambio |
|---|---|
| `utilidades/ValidadorDocumentoFiscalTest` | Añadir casos de `formatoCorrecto` y `letraCorrecta` con los ejemplos de D1. Los actuales no cambian |
| nuevo `modelo/negocio/ValidacionClienteTest` (sin ventana ni base de datos) | Cada fila de la tabla de D2 con su mensaje exacto; cliente completo no lanza; email vacío no da error; `comprobar(null)` lanza «Indique los datos del cliente.»; el orden (nombre y NIF vacíos → avisa del nombre) |
| `modelo/negocio/FacturasTest` | Las facturas con cliente `null` pasan a usar el cliente de prueba; los clientes de las líneas ~161 y ~202 se completan. Tests nuevos: `noCreaFacturaSinCliente` y `noCreaFacturaConLetraIncorrecta` (lanzan `ValidacionException` con su mensaje y no queda factura) |
| `modelo/negocio/FacturacionMensualTest` | El cliente de ~83 se completa. Test nuevo: `noGeneraConClienteSinCodigoPostal` (lanza con «El código postal es obligatorio.» y no genera ninguna factura) |
| `modelo/negocio/EstadosTest` y `HistorialTest` | Sus facturas con cliente `null` usan el cliente de prueba |
| `fichero/CopiaSeguridadTest` | `B12345678` → `B12345674`, `A11111111` → `A11111119`, `Z00000000` → `Z0000000M` (también en sus `assertEquals`) |
| `vista/controlador/EditorIvaInactivoTest` | El cliente pasa a ser el de prueba (`12345678Z` y datos completos) |
| `vista/controlador/ClientesValidacionNifTest` | Con un NIF no válido, **al salir del campo no hay aviso ni rojo** (`grabador.errores == 0`, sin estilo); **al Guardar**, `grabador.errores == 1`, el campo queda en rojo, no se guarda y el mensaje es el de formato o el de letra según el caso. Nuevo caso: NIF vacío → al Guardar un aviso «El NIF/NIE es obligatorio.». El caso válido rellena todos los obligatorios |
| `vista/controlador/EditorValidacionNifTest` | `nifInvalidoMuestraRojoYNoGuarda`: al salir, ni rojo ni aviso; `guardar` devuelve `false` con `errores == 1` y el campo en rojo. `nifVacioYValidoNoAvisanNiBloquean` pasa a `nifVacioAvisaAlGuardarYValidoNoAvisa`: vacío → al salir nada, al guardar un aviso «El NIF/NIE es obligatorio.»; válido → ni rojo ni aviso |

Los tests que usan un NIF no válido **a propósito** conservan ese NIF.

Para que `ClientesValidacionNifTest` y `EditorValidacionNifTest` puedan leer el mensaje, el ayudante falso de `Dialogos` guarda el último mensaje de error recibido.

### D9. Spec

`MODIFIED` del requisito completo «Clientes» de `openspec/specs/invoicing/spec.md`, con **todos** sus escenarios (ver la spec delta). Cambia: datos obligatorios, avisos distintos, sin retener el foco y factura sin cliente no permitida. Se conservan los escenarios de borrado físico, bloqueo de borrado e inactivo en histórico.

### D10. Avisos que no se cortan (`Dialogos`)

**Problema visto en la prueba manual (15/09/2026):** el aviso «El código postal debe tener cinco dígitos y comenzar entre 01 y 52.» se ve cortado («…comenzar entre 01 ...»).

**Causa, medida con un test temporal ya borrado:** `Dialogos` pone el mensaje con `Alert.setContentText`. JavaFX calcula el tamaño del aviso sin contar el margen de 18 px de `.dialog-card` (`base.css`); al aplicarse, la zona de texto queda en 360 px. Un mensaje que mide algo más de una línea (entre unos 360 y 390 px) recibe el alto de **una** línea y se corta con «...». Le pasa a cualquier mensaje de ese largo, no solo al del código postal (comprobado también con «Hay cambios sin guardar que se descartarán. ¿Desea continuar ya?»).

**Arreglos medidos que NO funcionan**: `getDialogPane().setMinHeight(Region.USE_PREF_SIZE)`; etiqueta propia con `setPrefWidth(360)`; `applyCss()` antes de mostrar; `sizeToScene()` al mostrarse.

**Arreglo medido que SÍ funciona**: en lugar de `setContentText`, poner como contenido del `DialogPane` una `Label` propia con `setWrapText(true)` y `setMinHeight(Region.USE_PREF_SIZE)`, **sin fijar su ancho**. Ningún texto se corta; a cambio, el ancho del aviso depende del mensaje (el del formato del NIF sale en una línea de unos 620 px).

**Cambio en `vista/utilidades/Dialogos.java`:**

- Método nuevo `static void ponerMensaje(Alert alerta, String mensaje)` (visible en el paquete para poder probarlo), con Javadoc corto que explique por qué no se usa `setContentText`:

```java
Label texto = new Label(mensaje);
texto.setWrapText(true);
texto.setMinHeight(Region.USE_PREF_SIZE);
alerta.getDialogPane().setContent(texto);
```

- En `error` e `info`: sustituir `a.setContentText(mensaje)` por `ponerMensaje(a, mensaje)`.
- En `confirmar`: crear el `Alert` sin mensaje (`new Alert(Alert.AlertType.CONFIRMATION, "", ButtonType.YES, ButtonType.NO)`) y llamar a `ponerMensaje(a, mensaje)`.
- `confirmarCambiosSinGuardar` y `modoGuardarVersion` tienen textos cortos y fijos: no se tocan.
- Fuera: los `TextInputDialog` y `ChoiceDialog` de Arranque, Configuración, Editor e Histórico (textos cortos junto a un campo).

**Test nuevo** `src/test/java/cabofactu/vista/utilidades/DialogosTextoCompletoTest.java` (con `PruebasJavaFx.arrancarFx()`): para «El código postal debe tener cinco dígitos y comenzar entre 01 y 52.» y «Hay cambios sin guardar que se descartarán. ¿Desea continuar ya?», crear un `Alert` de error, llamar a `ponerMensaje` y `aplicarTema`, mostrarlo con `show()`, hacer `applyCss()` y `layout()`, y comprobar que la unión del texto de los nodos `.text` de la etiqueta es **igual al mensaje completo** (sin «...»). Cerrar el aviso al terminar.

### D11. `Dialogos` ordenado como Biblioteca8 (adelantado)

**Decidido por el usuario el 15/09/2026, al revisar `Dialogos`:** adelantar a este change la parte de `Dialogos` que estaba prevista para `clases-independientes`, `sin-clases-anonimas-ni-hilos` y `java-clasico`, para dejar el fichero legible como en Biblioteca8. No cambia nada visible ni el comportamiento de ningún aviso.

**Ficheros en `cabofactu.vista.utilidades`:**

| Fichero | Contenido |
|---|---|
| `CambiosSinGuardar.java` | `public enum CambiosSinGuardar { GUARDAR, DESCARTAR, CANCELAR }` (sale de `Dialogos`) |
| `ModoGuardarVersion.java` | `public enum ModoGuardarVersion { SOBRESCRIBIR, NUEVA_VERSION, CANCELAR }` (sale de `Dialogos`) |
| `MostradorDialogos.java` | la interfaz que hoy es `Dialogos.Impl`, con los mismos cinco métodos. El método `default modoGuardarVersion()` se conserva, pero con `if / else` en lugar del ternario |
| `DialogosReales.java` | `public class DialogosReales implements MostradorDialogos`: el contenido de la clase anónima `IMPLEMENTACION_POR_DEFECTO`, método a método, sin cambiar qué hace |
| `Dialogos.java` | solo la parte `static` que usan las pantallas y los tests (ver abajo) |

**`Dialogos.java` queda así** (sin tipos dentro, sin clase anónima):

- `private static MostradorDialogos mostrador = new DialogosReales();` (sustituye a `impl` e `IMPLEMENTACION_POR_DEFECTO`; se quita `volatile`).
- `setImpl(MostradorDialogos m)` y `restoreDefault()` (este hace `mostrador = new DialogosReales();`). Se conservan los nombres para no tocar más tests.
- `error`, `info`, `confirmar`, `confirmarCambiosSinGuardar` y `modoGuardarVersion` llaman a `mostrador`.
- `ponerMensaje(Alert, String)` y `aplicarTema(DialogPane)` se quedan aquí, públicos o de paquete como hoy, porque los usan `DialogosReales` y otras pantallas.
- `icono(Alert.AlertType)` e `iconoVentana(Alert)` pasan a `DialogosReales` como métodos privados, porque solo los usa esa clase.

**Construcciones que se cambian al mover el código** (regla de `AGENTS.md`):

| Hoy | Queda |
|---|---|
| `default` con `confirmar(...) ? SOBRESCRIBIR : CANCELAR` | `if (confirmar(...)) { return ModoGuardarVersion.SOBRESCRIBIR; } return ModoGuardarVersion.CANCELAR;` |
| `a.showAndWait().map(b -> b == ButtonType.YES).orElse(false)` | `Optional<ButtonType> respuesta = a.showAndWait(); return respuesta.isPresent() && respuesta.get() == ButtonType.YES;` |
| `iconoVentana`: `a.setOnShown(e -> { ...5 líneas... })` | `a.setOnShown(e -> ponerIconoVentana(a));` con método privado `ponerIconoVentana(Alert a)` |
| `if (w instanceof Stage s) { Ventanas.aplicarIcono(s); }` | `if (w instanceof Stage) { Ventanas.aplicarIcono((Stage) w); }` |

**Usos que cambian:**

- `EditorController`: `Dialogos.CambiosSinGuardar` → `CambiosSinGuardar` y `Dialogos.ModoGuardarVersion` → `ModoGuardarVersion`, con sus `import`.
- `ClientesValidacionNifTest`, `EditorIvaInactivoTest` y `EditorValidacionNifTest`: `implements Dialogos.Impl` → `implements MostradorDialogos`, y los tipos de los enum con su nombre corto e `import`. Sus clases `Grabador` siguen dentro del test (eso se ordena en `clases-independientes`).
- `DialogosTextoCompletoTest`: el bloque dentro de `Platform.runLater(() -> { ... })` pasa a un método privado, para que la lambda solo llame a ese método.

**Javadoc** corto en `MostradorDialogos`, `DialogosReales` y los dos enum, en primera persona del plural. En `MostradorDialogos`, un párrafo «Cómo funciona»: los avisos reales abren ventanas que esperan a que alguien pulse un botón; en los tests se cambia el mostrador por uno falso que solo cuenta los avisos, para que no se queden bloqueados.

## Risks / Trade-offs

- **Riesgo medio: tests de pantalla con JavaFX.** Cambian lo que comprueban (ahora sin aviso al salir). Si dependen del orden de eventos de foco, pueden necesitar forzar la pérdida de foco como hoy.
- **Riesgo bajo: etiquetas con asterisco en el Editor.** Un carácter más en `NIF*`, `CP*` y `Provincia*` puede mover la maquetación del bloque Cliente. `EditorTamanoMinimoTest` y la prueba manual lo cubren.
- **Trade-off aceptado:** `ValidacionCliente` es una clase `static`. Es una herramienta sin estado, permitida por `AGENTS.md`.
- **Trade-off aceptado:** los `TextField` de la ficha pasan a campos de `ClientesController` hasta que la ficha tenga su FXML.
