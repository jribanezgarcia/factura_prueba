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

- Al **perder el foco** NIF, CP o email: solo `marcarCampo(campo, ValidacionCliente.errorX(campo.getText()))`. **Sin aviso y sin devolver el foco.** Al perder el foco el resto de obligatorios: igual, con su `errorX`.
- **Enter** en NIF, CP o email: si hay error en ese campo, `marcarCampo` y un `Dialogos.error` con su título y mensaje. El foco se queda donde está (no se usa `runLater`).
- **Guardar**: un **único** `addEventFilter(ActionEvent.ACTION, e -> comprobarAntesDeGuardar(e))`. `comprobarAntesDeGuardar` llama a `marcarCamposFicha()` y, si `avisarPrimerErrorFicha()` devuelve `true`, hace `e.consume()`.
- **Cancelar** y cerrar la ficha funcionan siempre: nada retiene el foco.
- La regla del botón Guardar desactivado hasta que haya nombre se queda como está.
- Lambdas en `addListener`: `(propiedad, anterior, tieneFoco) -> alSalirDeCampo(...)`, que solo llaman a un método.

**`nuevo()` y `editar()`**: añadir `catch (ValidacionException e)` antes del `catch (Exception e)` actual, con `Dialogos.error("Datos del cliente", e.getMessage())`. Es la red de seguridad: normalmente la ficha ya ha avisado.

### D7. Editor (`EditorController` y `Editor.fxml`)

**Etiquetas** en `Editor.fxml`: `Nombre*`, `NIF*`, `Dirección*`, `CP*`, `Localidad*`, `Provincia*`; `Email` sin asterisco.

**Se quitan**: `validarNifCliente(boolean)`, el campo `corrigiendoNif`, su `Platform.runLater(cliNif::requestFocus)` y su aviso al perder el foco.

**Métodos privados nuevos**, con la misma idea que D6: `marcarCampo(TextField, String)`, `marcarCamposCliente()` y `avisarPrimerErrorCliente()`, sobre `cliNombre`, `cliNif`, `cliDireccion`, `cliCp`, `cliLocalidad`, `cliProvincia` y `cliEmail`, con los títulos de D6.

**Comportamiento**:

- Al **perder el foco** cualquiera de esos campos (y `!cargando`): solo `marcarCampo`. Sin aviso.
- **Enter** en `cliNif`: si hay error, `marcarCampo` y un aviso con título `NIF no válido`.
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
| `vista/controlador/ClientesValidacionNifTest` | Con un NIF no válido, **al salir del campo no hay aviso** (`grabador.errores == 0`) pero el campo queda en rojo; **al Guardar**, `grabador.errores == 1`, no se guarda y el mensaje es el de formato o el de letra según el caso. Nuevo caso: NIF vacío → al Guardar un aviso «El NIF/NIE es obligatorio.». El caso válido rellena todos los obligatorios |
| `vista/controlador/EditorValidacionNifTest` | `nifInvalidoMuestraRojoYNoGuarda`: al salir, rojo y `errores == 0`; `guardar` devuelve `false` con `errores == 1`. `nifVacioYValidoNoAvisanNiBloquean` pasa a `nifVacioAvisaAlGuardarYValidoNoAvisa`: vacío → al salir rojo sin aviso, al guardar un aviso «El NIF/NIE es obligatorio.»; válido → ni rojo ni aviso |

Los tests que usan un NIF no válido **a propósito** conservan ese NIF.

Para que `ClientesValidacionNifTest` y `EditorValidacionNifTest` puedan leer el mensaje, el ayudante falso de `Dialogos` guarda el último mensaje de error recibido.

### D9. Spec

`MODIFIED` del requisito completo «Clientes» de `openspec/specs/invoicing/spec.md`, con **todos** sus escenarios (ver la spec delta). Cambia: datos obligatorios, avisos distintos, sin retener el foco y factura sin cliente no permitida. Se conservan los escenarios de borrado físico, bloqueo de borrado e inactivo en histórico.

## Risks / Trade-offs

- **Riesgo medio: tests de pantalla con JavaFX.** Cambian lo que comprueban (ahora sin aviso al salir). Si dependen del orden de eventos de foco, pueden necesitar forzar la pérdida de foco como hoy.
- **Riesgo bajo: etiquetas con asterisco en el Editor.** Un carácter más en `NIF*`, `CP*` y `Provincia*` puede mover la maquetación del bloque Cliente. `EditorTamanoMinimoTest` y la prueba manual lo cubren.
- **Trade-off aceptado:** `ValidacionCliente` es una clase `static`. Es una herramienta sin estado, permitida por `AGENTS.md`.
- **Trade-off aceptado:** los `TextField` de la ficha pasan a campos de `ClientesController` hasta que la ficha tenga su FXML.
