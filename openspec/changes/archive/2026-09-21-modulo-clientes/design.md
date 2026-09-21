## Situación de partida

Una sola tabla, `cliente`, repartida hoy en cuatro sitios:

| Fichero | Qué hace hoy |
|---|---|
| `modelo/dominio/Cliente` | 98 líneas: constructor vacío, 9 pares de getter/setter que no comprueban nada, `nombreNif()` y `toString()` |
| `modelo/negocio/ValidacionCliente` | Un método por campo que devuelve el mensaje de error o `null`, más `comprobar(Cliente)` |
| `modelo/negocio/Clientes` | 47 líneas que solo reenvían al DAO, con `ValidacionCliente.comprobar` delante de `insertar` y `actualizar` |
| `modelo/negocio/sqlite/ClienteDAO` | 161 líneas con el SQL, `SELECT *` y `DatosException` |

Y la pantalla, `ClientesController`, con 358 líneas de las que **más de 200 son la ficha construida en Java**.

Quien usa todo esto:

- `ClientesController` → la pantalla de clientes.
- `EditorController` → busca clientes para el desplegable (`buscar`, `listar`).
- `GenerarFacturasMensualesController` → lista los activos.
- `Facturas` → recibe el `ClienteDAO` por el constructor y lo usa para `insertar`, `actualizar` y `getById`.
- `Estados` y `Rectificativas` → rehacen un `Cliente` a partir de los datos guardados en la versión de la factura.

## Objetivos y lo que queda fuera

**Objetivo**: dejar la tabla `cliente` con **una clase de datos que se valida sola** y **una clase de negocio con su SQL**, la pantalla con el patrón de tabla + formulario reutilizable, y las operaciones repetidas en `Controlador` y `Modelo`. Este módulo es la plantilla de los siguientes.

**Fuera**:

- El bloque Cliente del Editor sigue igual (módulo 6). Por eso `ValidacionCliente` **no se borra**: la siguen usando el Editor, la generación mensual y las rectificativas.
- Las demás tablas siguen con su DAO.
- `DatosException` y `ValidacionException` siguen vivas mientras queden DAO.

---

## D1. `Cliente`: los datos se validan en los setters

`modelo/dominio/Cliente.java`, rehecha. Obligatorios: nombre, NIF, dirección, código postal, localidad y provincia. Opcionales: email, `id` y `activo`.

```java
/**
 * Datos de un cliente. Los setters comprueban lo que reciben y lanzan
 * Exception con el mensaje que verá el usuario, así que un Cliente que
 * existe es siempre un Cliente válido.
 */
public class Cliente {

    private Long id;
    private String nombre;
    private String nif;
    private String direccion;
    private String cp;
    private String localidad;
    private String provincia;
    private String email;
    private boolean activo;

    public Cliente(String nombre, String nif, String direccion, String cp,
                   String localidad, String provincia) throws Exception {
        setNombre(nombre);
        setNif(nif);
        setDireccion(direccion);
        setCp(cp);
        setLocalidad(localidad);
        setProvincia(provincia);
        setEmail("");
        setActivo(true);
    }

    /** Copiamos un cliente entero, para poder editarlo sin tocar el original. */
    public Cliente(Cliente otro) throws Exception {
        this(otro.getNombre(), otro.getNif(), otro.getDireccion(), otro.getCp(),
                otro.getLocalidad(), otro.getProvincia());
        setId(otro.getId());
        setEmail(otro.getEmail());
        setActivo(otro.isActivo());
    }
}
```

Los setters, en el orden `null` → `isBlank()` → `trim()` → comprobar → asignar. **Los mensajes son exactamente los de hoy**, porque la especificación los cita palabra por palabra:

```java
public void setNombre(String nombre) throws Exception {
    String error = errorNombre(nombre);
    if (error != null) {
        throw new Exception(error);
    }
    this.nombre = nombre.trim();
}

public void setNif(String nif) throws Exception {
    String error = errorNif(nif);
    if (error != null) {
        throw new Exception(error);
    }
    this.nif = nif.trim().toUpperCase();
}

public void setCp(String cp) throws Exception {
    String error = errorCp(cp);
    if (error != null) {
        throw new Exception(error);
    }
    this.cp = cp.trim();
}

/** El email es opcional: si no se escribe, lo guardamos vacío. */
public void setEmail(String email) throws Exception {
    String error = errorEmail(email);
    if (error != null) {
        throw new Exception(error);
    }
    if (email == null || email.isBlank()) {
        this.email = "";
    } else {
        this.email = email.trim();
    }
}
```

**Los comprobadores.** Cada campo tiene además un método `static` que devuelve el mensaje de error, o `null` si el dato está bien. **El setter lo usa**, así que la comprobación vive en un solo sitio; y el formulario puede preguntar campo por campo para marcarlos todos sin tener que construir el objeto:

```java
/** Decimos qué le pasa al NIF, o null si está bien. */
public static String errorNif(String valor) {
    if (valor == null || valor.isBlank()) {
        return "El NIF/NIE es obligatorio.";
    }
    String nif = valor.trim().toUpperCase();
    if (!ValidadorDocumentoFiscal.formatoCorrecto(nif)) {
        return "Formato NIF/NIE incorrecto. Debe ser como 12345678Z (DNI), "
                + "X1234567L (NIE) o B12345674 (CIF).";
    }
    if (!ValidadorDocumentoFiscal.letraCorrecta(nif)) {
        return "La letra no es correcta.";
    }
    return null;
}

/** El email es opcional: vacío está bien. */
public static String errorEmail(String valor) {
    if (valor == null || valor.isBlank()) {
        return null;
    }
    if (!ValidadorEmail.esValido(valor.trim())) {
        return "Revise el formato del correo electrónico.";
    }
    return null;
}
```

Los siete: `errorNombre`, `errorNif`, `errorDireccion`, `errorCp`, `errorLocalidad`, `errorProvincia` y `errorEmail`. Los cuatro de texto suelto solo comprueban que no esté vacío y devuelven su mensaje. Van juntos al final de la clase, detrás de los getters de texto.

Este es **el patrón para todas las clases de datos**: `Serie`, `TipoIva`, `TipoRetencion` y `Empresa` llevarán sus `errorX` igual. Cuando el módulo 6 rehaga el Editor, `ValidacionCliente` se borrará y el bloque Cliente usará `Cliente.errorNombre(...)`, `Cliente.errorNif(...)`…

`setDireccion`, `setLocalidad` y `setProvincia` son como `setNombre`, con sus mensajes: «La dirección del cliente es obligatoria.», «La localidad del cliente es obligatoria.» y «La provincia del cliente es obligatoria.». `setId(Long)` y `setActivo(boolean)` asignan sin comprobar nada.

**Dónde están los patrones.** `Cliente` no lleva constantes `*_PATTERN` porque sus tres comprobaciones con cálculo ya viven en `utilidades` y las comparte con `Empresa`: `ValidadorDocumentoFiscal` (forma y letra), `ValidadorCodigoPostal` (cinco dígitos y provincia entre 01 y 52) y `ValidadorEmail`. Los demás campos solo piden no estar vacíos.

**Getters de texto**, para que las columnas de la tabla y los desplegables no necesiten traductores:

```java
/** «Activo» o «Inactivo», para la columna Estado de la tabla. */
public String getEstadoTexto() {
    if (activo) {
        return "Activo";
    }
    return "Inactivo";
}

/** «Nombre (NIF)», para el desplegable de clientes del Editor. */
public String getNombreNif() {
    return nombre + " (" + nif + ")";
}
```

`equals` y `hashCode` por el NIF; `toString()` devuelve `getNombreNif()`. Orden dentro de la clase: campos, constructores, getters y setters por pares, `equals`/`hashCode`/`toString` y los getters de texto al final.

**Un cambio pequeño y visible**: el NIF se guarda **en mayúsculas**. Hoy se guarda tal cual se escribe.

---

## D2. `Clientes`: el singleton con el SQL dentro

`modelo/negocio/Clientes.java`, rehecha. `ClienteDAO` se borra y su SQL se trae aquí, con las columnas escritas y sin `SELECT *`.

```java
/** Los clientes de la empresa activa: es el único sitio con el SQL de la tabla cliente. */
public class Clientes {

    private static Clientes clientes;

    private Clientes() {
    }

    public static Clientes getClientes() {
        if (clientes == null) {
            clientes = new Clientes();
        }
        return clientes;
    }
}
```

Métodos públicos, todos `throws Exception`:

| Método | Qué hace |
|---|---|
| `listado(boolean soloActivos)` | Todos, o solo los activos, ordenados por nombre |
| `listado(String texto, boolean soloActivos)` | Los que coinciden por nombre o NIF, hasta 100 |
| `buscar(long id)` | Uno, o `null` si no está |
| `alta(Cliente cliente)` | Inserta y devuelve el `id` nuevo |
| `modificar(Cliente cliente)` | Guarda los cambios |
| `baja(long id)` | Borra la fila |
| `desactivar(long id)` | Pone `activo = 0` |
| `tieneFacturas(long id)` | `true` si hay facturas de ese cliente |

Así queda `alta`, como plantilla del resto:

```java
/** Damos de alta el cliente y devolvemos el id que le ha puesto la base de datos. */
public long alta(Cliente cliente) throws Exception {
    if (cliente == null) {
        throw new Exception("Indique los datos del cliente.");
    }
    String insertar = """
            INSERT INTO cliente (nombre, nif, direccion, cp, localidad, provincia, email, activo)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;
    try (PreparedStatement sentencia = Conexion.establecerConexion()
            .prepareStatement(insertar, Statement.RETURN_GENERATED_KEYS)) {
        sentencia.setString(1, cliente.getNombre());
        sentencia.setString(2, cliente.getNif());
        sentencia.setString(3, cliente.getDireccion());
        sentencia.setString(4, cliente.getCp());
        sentencia.setString(5, cliente.getLocalidad());
        sentencia.setString(6, cliente.getProvincia());
        sentencia.setString(7, cliente.getEmail());
        int activo = 0;
        if (cliente.isActivo()) {
            activo = 1;
        }
        sentencia.setInt(8, activo);
        int filas = sentencia.executeUpdate();
        if (filas == 0) {
            throw new Exception("No se ha podido guardar el cliente.");
        }
        try (ResultSet clave = sentencia.getGeneratedKeys()) {
            clave.next();
            return clave.getLong(1);
        }
    } catch (SQLException e) {
        throw new Exception("Error SQLite: " + e.getMessage());
    }
}
```

Y el método que pasa una fila a objeto, reutilizado por los tres métodos de consulta:

```java
/**
 * Pasamos una fila de la consulta a un objeto Cliente. Como el constructor
 * comprueba los datos, una fila incompleta se convierte en un aviso.
 */
private Cliente crearCliente(ResultSet fila) throws Exception {
    Cliente cliente = new Cliente(
            fila.getString("nombre"),
            fila.getString("nif"),
            fila.getString("direccion"),
            fila.getString("cp"),
            fila.getString("localidad"),
            fila.getString("provincia"));
    cliente.setId(fila.getLong("id"));
    cliente.setEmail(fila.getString("email"));
    cliente.setActivo(fila.getInt("activo") == 1);
    return cliente;
}
```

`desactivar` y `baja` comprueban `if (filas == 0)` y avisan: «No se ha encontrado el cliente.».

---

## D3. Las operaciones en `Controlador` y `Modelo`

`Modelo` deja de construir `Clientes` con un DAO y pasa a pedir el singleton. Repite cada operación con un método de una línea:

```java
public List<Cliente> listadoClientes(boolean soloActivos) throws Exception {
    return Clientes.getClientes().listado(soloActivos);
}

public long altaCliente(Cliente cliente) throws Exception {
    return Clientes.getClientes().alta(cliente);
}
```

Y `Controlador` repite las del modelo:

```java
public long altaCliente(Cliente cliente) throws Exception {
    return modelo.altaCliente(cliente);
}
```

Los ocho: `listadoClientes(boolean)`, `listadoClientes(String, boolean)`, `buscarCliente(long)`, `altaCliente(Cliente)`, `modificarCliente(Cliente)`, `bajaCliente(long)`, `desactivarCliente(long)` y `clienteTieneFacturas(long)`.

`Modelo.getClientes()` **se borra**: ya no hay nada que lo use. Las pantallas llaman siempre así:

```java
Vista.getInstancia().getControlador().altaCliente(cliente);
```

`Controlador.getModelo()` sigue ahí, para las pantallas que todavía no se han rehecho.

---

## D4. `Vista.crearVentanaModal`: cómo se abre un formulario

Todos los módulos van a abrir formularios modales, así que el tema, el icono, el dueño y la modalidad se ponen en un solo sitio. En `Vista`:

```java
/**
 * Preparamos una ventana modal con el tema y el icono de la aplicación.
 * La devolvemos sin mostrar, para que quien la abre pueda darle sus datos
 * al controlador antes de llamar a showAndWait().
 */
public Stage crearVentanaModal(Parent raiz, String titulo) {
    Scene escena = new Scene(raiz);
    escena.getStylesheets().setAll(GestorTemas.hojas());
    Stage modal = new Stage();
    modal.initModality(Modality.APPLICATION_MODAL);
    modal.initOwner(ventana);
    modal.setTitle(Ventanas.PREFIJO + titulo);
    modal.setResizable(false);
    modal.setScene(escena);
    Ventanas.aplicarIcono(modal);
    return modal;
}
```

---

## D5. `FichaCliente.fxml` y su controlador

`src/main/resources/cabofactu/vista/recursos/FichaCliente.fxml`, nuevo. Un `VBox` con un `GridPane` de dos columnas (etiqueta y campo) y, abajo, **Guardar** y **Cancelar**. Sustituye al `Dialog` que hoy se monta en Java. Campos y `fx:id`:

| Etiqueta | `fx:id` | Control |
|---|---|---|
| Nombre* | `txtNombre` | `TextField` |
| NIF* | `txtNif` | `TextField` |
| Dirección* | `txtDireccion` | `TextField` |
| CP* | `txtCp` | `TextField` |
| Localidad* | `txtLocalidad` | `TextField` |
| Provincia* | `txtProvincia` | `TextField` |
| Email | `txtEmail` | `TextField` |
| — | `chkActivo` | `CheckBox` con texto «Cliente activo» |
| — | `lblTitulo` | `Label` del encabezado |
| — | `btnGuardar` | `Button`, `onAction="#guardar"` |
| — | `btnCancelar` | `Button`, `onAction="#cancelar"` |

El asterisco va escrito en el texto de la etiqueta, como hoy. Ancho preferido del `VBox`, 375, el mismo que tenía el diálogo.

`vista/controlador/FichaClienteController.java`, nuevo. No toca la base de datos: crea o modifica el objeto y lo deja en `registro`.

```java
/**
 * Ficha de un cliente. Sirve para añadir y para editar: si el registro que
 * recibe es null, el formulario está en modo añadir.
 */
public class FichaClienteController implements Initializable {

    private Cliente registro;

    public Cliente getRegistro() {
        return registro;
    }

    public void setRegistro(Cliente registro) {
        this.registro = registro;
        if (this.registro == null) {
            lblTitulo.setText("Alta de cliente");
            btnGuardar.setText("Añadir");
            chkActivo.setSelected(true);
        } else {
            lblTitulo.setText("Datos del cliente");
            btnGuardar.setText("Guardar");
            txtNombre.setText(this.registro.getNombre());
            txtNif.setText(this.registro.getNif());
            txtDireccion.setText(this.registro.getDireccion());
            txtCp.setText(this.registro.getCp());
            txtLocalidad.setText(this.registro.getLocalidad());
            txtProvincia.setText(this.registro.getProvincia());
            txtEmail.setText(this.registro.getEmail());
            chkActivo.setSelected(this.registro.isActivo());
        }
    }
}
```

`initialize` deja el foco en el nombre y no hace nada más: aquí no hay desplegables que cargar.

```java
@FXML
void guardar(ActionEvent event) {
    quitarMarcas();
    try {
        if (registro == null) {
            registro = clienteDeLosCampos();
        } else {
            registro = actualizarRegistro();
        }
        cerrarVentana(event);
    } catch (Exception e) {
        marcarCampoDelError(e.getMessage());
        Dialogos.mostrarDialogoError("Datos del cliente", e.getMessage());
    }
}

@FXML
void cancelar(ActionEvent event) {
    registro = null;
    cerrarVentana(event);
}
```

**Por qué `actualizarRegistro` trabaja sobre una copia.** Si editásemos el objeto de la tabla y un setter fallara a mitad, la fila se quedaría con unos campos nuevos y otros viejos aunque el usuario cancelara. Por eso se copia primero con el constructor copia, se cambian los campos de la copia y solo si todo ha ido bien se devuelve:

```java
/** Trabajamos sobre una copia para que un dato mal escrito no estropee la fila de la tabla. */
private Cliente actualizarRegistro() throws Exception {
    Cliente copia = new Cliente(registro);
    copia.setNombre(txtNombre.getText().trim());
    copia.setNif(txtNif.getText().trim());
    copia.setDireccion(txtDireccion.getText().trim());
    copia.setCp(txtCp.getText().trim());
    copia.setLocalidad(txtLocalidad.getText().trim());
    copia.setProvincia(txtProvincia.getText().trim());
    copia.setEmail(txtEmail.getText().trim());
    copia.setActivo(chkActivo.isSelected());
    return copia;
}
```

`clienteDeLosCampos()` hace lo mismo con el constructor de seis datos y después el email y el activo.

**Marcar todos los campos malos y avisar del primero.** Antes de construir nada, se pregunta campo por campo con los comprobadores de `Cliente`. Se marcan **todos** los que fallan y se avisa **del primero**, que es lo que pide la especificación:

```java
/**
 * Marcamos en rojo todos los campos incorrectos y devolvemos el mensaje del
 * primero, o null si está todo bien.
 */
private String marcarCamposMalos() {
    quitarMarcas();
    String primero = null;
    primero = revisar(txtNombre, Cliente.errorNombre(txtNombre.getText()), primero);
    primero = revisar(txtNif, Cliente.errorNif(txtNif.getText()), primero);
    primero = revisar(txtDireccion, Cliente.errorDireccion(txtDireccion.getText()), primero);
    primero = revisar(txtCp, Cliente.errorCp(txtCp.getText()), primero);
    primero = revisar(txtLocalidad, Cliente.errorLocalidad(txtLocalidad.getText()), primero);
    primero = revisar(txtProvincia, Cliente.errorProvincia(txtProvincia.getText()), primero);
    primero = revisar(txtEmail, Cliente.errorEmail(txtEmail.getText()), primero);
    return primero;
}

/** Marcamos el campo si tiene error y nos quedamos con el primer mensaje. */
private String revisar(TextField campo, String error, String primero) {
    if (error == null) {
        return primero;
    }
    campo.getStyleClass().add("campo-error");
    if (primero == null) {
        return error;
    }
    return primero;
}
```

Y `guardar` queda así:

```java
@FXML
void guardar(ActionEvent event) {
    String error = marcarCamposMalos();
    if (error != null) {
        Dialogos.mostrarDialogoError("Datos del cliente", error);
        return;
    }
    try {
        if (registro == null) {
            registro = clienteDeLosCampos();
        } else {
            registro = actualizarRegistro();
        }
        cerrarVentana(event);
    } catch (Exception e) {
        Dialogos.mostrarDialogoError("Datos del cliente", e.getMessage());
    }
}
```

El `try / catch` se queda como red de seguridad: si los comprobadores dicen que todo está bien, ningún setter debería quejarse, pero si alguna vez pasara, el usuario ve el mensaje en lugar de un fallo mudo.

`quitarMarcas()` quita la clase `campo-error` de los siete campos. En `temas/base.css`, `.campo-error { -fx-border-color: -fx-accent-error; -fx-border-width: 2; }` con el color de aviso que ya usa el tema, en vez del `setStyle` con el rojo escrito a mano que hay hoy.

---

**Cerrar con cambios sin guardar.** Si el usuario ha escrito o cambiado algo, tanto **Cancelar** como la **X** preguntan antes de tirarlo; si no ha tocado nada, la ficha se cierra sin molestar. Es lo mismo que ya hace el Editor de facturas al salir.

Para saber si se ha tocado algo, guardamos una foto de los campos al abrir y la comparamos al cerrar:

```java
/**
 * Cómo funciona: juntamos el contenido de los campos en un solo texto. Guardamos
 * ese texto al abrir la ficha y lo volvemos a formar al cerrarla; si no coinciden,
 * es que el usuario ha tocado algo.
 */
private String fotoDeLosCampos() {
    return txtNombre.getText() + "|" + txtNif.getText() + "|" + txtDireccion.getText() + "|"
            + txtCp.getText() + "|" + txtLocalidad.getText() + "|" + txtProvincia.getText() + "|"
            + txtEmail.getText() + "|" + chkActivo.isSelected();
}

/** Si hay algo escrito sin guardar, preguntamos antes de tirarlo. */
private boolean confirmarDescartar() {
    if (fotoInicial.equals(fotoDeLosCampos())) {
        return true;
    }
    return Dialogos.mostrarDialogoConfirmacion("Ficha de cliente",
            "Hay cambios sin guardar en la ficha del cliente.

¿Quieres descartarlos?");
}
```

`fotoInicial = fotoDeLosCampos()` se guarda al final de `setRegistro`, cuando los campos ya están rellenos. En modo añadir la foto sale con todo vacío, así que abrir y cerrar sin escribir nada no pregunta.

`cancelar` empieza por ahí:

```java
@FXML
void cancelar(ActionEvent event) {
    if (!confirmarDescartar()) {
        return;
    }
    registro = null;
    cerrarVentana(event);
}
```

**Y la X hace lo mismo, reutilizando `Pantalla`.** `FichaClienteController` pasa a `implements Pantalla, Initializable` y contesta a `puedeCerrar()`:

```java
@Override
public boolean puedeCerrar() {
    return confirmarDescartar();
}
```

`crearVentanaModal` gana un tercer parámetro con la pantalla, y la X le pregunta:

```java
public Stage crearVentanaModal(Parent raiz, String titulo, Pantalla pantalla) {
    ...
    modal.setOnCloseRequest(evento -> pedirCierreModal(evento, pantalla));
    return modal;
}

/** Si la pantalla del formulario no quiere cerrarse, anulamos el cierre. */
private void pedirCierreModal(WindowEvent evento, Pantalla pantalla) {
    if (pantalla != null && !pantalla.puedeCerrar()) {
        evento.consume();
    }
}
```

La lambda solo llama a un método con nombre, como pide `AGENTS.md` y como ya hace `abrirVentanaPrincipal` con `pedirCierre`.

Así **todos** los formularios modales de los módulos siguientes se cierran igual, con la interfaz que ya existe. `cerrarVentana` (el `hide()` de Guardar y Cancelar) no dispara `setOnCloseRequest`, así que no se pregunta dos veces.

## D6. `ClientesController`: la tabla y el formulario

`vista/controlador/ClientesController.java`, rehecho. Pasa de 358 a unas 150 líneas, porque la ficha ya no está aquí.

Campos propios: `registro` (la fila elegida), `filtro` (lo escrito en el buscador) y `listaClientes` (la lista observable de la tabla).

```java
@Override
public void initialize(URL url, ResourceBundle rb) {
    barraController.marcarActivo("clientes");
    colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
    colNif.setCellValueFactory(new PropertyValueFactory<>("nif"));
    colLocalidad.setCellValueFactory(new PropertyValueFactory<>("localidad"));
    colEstado.setCellValueFactory(new PropertyValueFactory<>("estadoTexto"));
    tabla.setPlaceholder(new Label("No hay clientes."));
    listaClientes = FXCollections.observableArrayList();
    tabla.setItems(listaClientes);
    filtro = "";
    refrescarTabla();
}
```

`refrescarTabla()` deja `registro` en `null`, limpia la selección, pide el listado al controlador, filtra por nombre o NIF y pone el conteo. Tiene su `try / catch` dentro, para que `initialize` no lo arrastre.

Los botones, con el nombre y la forma que manda `AGENTS.md`:

```java
@FXML
void seleccionar(MouseEvent event) {
    registro = tabla.getSelectionModel().getSelectedItem();
    if (event.getClickCount() == 2 && registro != null) {
        editar();
    }
}

@FXML
void anadirCliente(ActionEvent event) {
    try {
        FichaClienteController ficha = abrirFicha(null, "Nuevo cliente");
        Cliente nuevo = ficha.getRegistro();
        if (nuevo != null) {
            Vista.getInstancia().getControlador().altaCliente(nuevo);
            refrescarTabla();
        }
    } catch (Exception e) {
        Dialogos.mostrarDialogoError("Clientes", e.getMessage());
    }
}
```

`editarCliente(ActionEvent)` avisa con `mostrarDialogoAdvertencia` si no hay fila elegida y llama a `editar()`, que abre la ficha con `setRegistro(registro)` y guarda con `modificarCliente`. El doble clic entra por el mismo sitio.

`borrarCliente(ActionEvent)` mantiene las dos reglas de hoy:

```java
if (Vista.getInstancia().getControlador().clienteTieneFacturas(registro.getId())) {
    if (Dialogos.mostrarDialogoConfirmacion("Cliente con facturas",
            "El cliente \"" + registro.getNombre() + "\" tiene facturas asociadas y no puede "
                    + "eliminarse.\n\n¿Desea marcarlo como inactivo?")) {
        Vista.getInstancia().getControlador().desactivarCliente(registro.getId());
        refrescarTabla();
    }
    return;
}
```

Y si no tiene facturas, la confirmación de siempre y `bajaCliente`.

El trozo que abre la ficha, en un método privado porque lo usan el alta y la edición:

```java
/** Abrimos la ficha en modo añadir (registro null) o en modo editar, y esperamos a que se cierre. */
private FichaClienteController abrirFicha(Cliente registro, String titulo) throws Exception {
    FXMLLoader cargador = new FXMLLoader(LocalizadorRecursos.class.getResource("FichaCliente.fxml"));
    Parent raiz = cargador.load();
    FichaClienteController ficha = cargador.getController();
    ficha.setRegistro(registro);
    Stage modal = Vista.getInstancia().crearVentanaModal(raiz, titulo);
    modal.showAndWait();
    return ficha;
}
```

`buscar(KeyEvent)` guarda el texto en `filtro` y llama a `refrescarTabla()`. `volver(ActionEvent)` vuelve al menú. `alMostrar()` sigue registrando el atajo Esc.

En `Clientes.fxml` cambian cuatro cosas: la tabla lleva `onMouseClicked="#seleccionar"`, el buscador `onKeyReleased="#buscar"`, los `onAction` pasan a los nombres nuevos (`#anadirCliente`, `#editarCliente`, `#borrarCliente`, `#volver`) y se añade el menú contextual con Editar y Borrar. Todo lo demás (tarjetas, iconos, anchos) se queda igual.

---

## D7. Los que usaban el DAO

Ninguno cambia lo que hace; solo cambia a quién se lo pide.

**`Facturas`** pierde el parámetro `ClienteDAO` de su constructor y usa el singleton:

| Antes | Ahora |
|---|---|
| `clienteDAO.insertar(cliente)` | `Clientes.getClientes().alta(cliente)` |
| `clienteDAO.actualizar(cliente)` | `Clientes.getClientes().modificar(cliente)` |
| `clienteDAO.getById(id)` | `Clientes.getClientes().buscar(id)` |

Los métodos que lo llaman (`crearFactura`, `guardarEditada`, `cliente`, `abrirVersion`) pasan de `throws ValidacionException` a `throws Exception`. Como `ValidacionException extends Exception`, quien los llama sigue compilando si ya capturaba `Exception`; donde se capture solo `ValidacionException`, se cambia a `Exception`.

**`Estados.snapshotCliente`** y **`Rectificativas.clienteDeVersion`** rehacen un cliente a partir de lo que se guardó en la versión de la factura. Ahora ese cliente se construye con el constructor, así que si la versión guardada no tiene los datos completos salta un aviso en vez de crear un cliente a medias:

```java
/**
 * Rehacemos el cliente tal como se guardó en la versión. Si a esa versión le
 * faltan datos del cliente, avisamos: no se puede anular ni rectificar una
 * factura cuyo cliente está incompleto.
 */
private Cliente clienteDeVersion(Facturas.VersionCompleta origen) throws Exception {
    VersionFactura v = origen.version();
    Cliente cliente = new Cliente(v.getCliNombre(), v.getCliNif(), v.getCliDireccion(),
            v.getCliCp(), v.getCliLocalidad(), v.getCliProvincia());
    if (origen.factura() != null) {
        cliente.setId(origen.factura().getClienteId());
    }
    cliente.setEmail(v.getCliEmail());
    return cliente;
}
```

**`EditorController`** cambia en dos sitios:

1. Las tres llamadas del desplegable pasan a `listadoClientes(texto, true)` y `listadoClientes(true)`.
2. Cuando abre una factura cuyo cliente ya no existe, hoy fabrica un `Cliente` vacío solo para llevar el `id`. Ahora no se puede, así que deja el desplegable en `null` y `cargarDatosCliente(null)` limpia los campos; el `id` se guarda en el campo `clienteAbiertoId` que ya tiene la clase.

**`GenerarFacturasMensualesController`** cambia su `listar(true)` por `listadoClientes(true)`.

---

## D9. Preguntar antes de tocar la ficha del cliente

Hoy la aplicación hace **dos cosas distintas** con los datos de cliente que se escriben dentro de una factura, y ninguna es la correcta:

| Al guardar | Qué pasa con la ficha del cliente |
|---|---|
| Una factura **nueva** (`crearFactura`) | No se toca. El cambio solo queda dentro de la factura. |
| Una factura **ya guardada** (`guardarEditada`) | Se sobrescribe **sin preguntar**, con un `modificar(cliente)` suelto. |

A partir de ahora, en los dos casos: **la factura se guarda siempre con lo que el usuario escribió**, y la ficha del cliente solo se toca **si el usuario dice que sí**. Además de ser lo que se espera, es lo que hará falta para VeriFactu: la factura conserva para siempre los datos tal como se emitió; la ficha del cliente es otra cosa.

**1. `Cliente` sabe comparar sus datos.** `equals` va por el NIF, así que no sirve para esto:

```java
/** Decimos si otro cliente tiene los mismos datos que este, sin mirar el id ni si está activo. */
public boolean tieneLosMismosDatos(Cliente otro) {
    if (otro == null) {
        return false;
    }
    return nombre.equals(otro.getNombre())
            && nif.equals(otro.getNif())
            && direccion.equals(otro.getDireccion())
            && cp.equals(otro.getCp())
            && localidad.equals(otro.getLocalidad())
            && provincia.equals(otro.getProvincia())
            && email.equals(otro.getEmail());
}
```

**2. El negocio deja de decidirlo.** En `Facturas.guardarEditada` se **quita** el bloque que pisaba la ficha:

```java
if (cliente != null && cliente.getId() != null) {
    Clientes.getClientes().modificar(cliente);   // se borra
}
```

Se queda el `alta` de cuando el cliente es nuevo (`getId() == null`), porque un cliente escrito a mano en la factura sí hay que crearlo, y se queda `facturaDAO.actualizarCliente`. Esto además cumple la norma de `AGENTS.md`: es la pantalla quien llama al controlador para guardar.

**3. El Editor pregunta.** En `EditorController.guardar()`, después de tener el cliente del formulario y **antes** de guardar la factura, se decide si hay que preguntar:

```java
/**
 * Si el cliente ya existe y sus datos no son los de su ficha, preguntamos si
 * queremos guardarlos también allí. La factura se guarda con ellos en cualquier caso.
 */
private boolean pedirActualizarFicha(Cliente cli) {
    if (cli.getId() == null || clienteActual == null) {
        return false;
    }
    if (cli.tieneLosMismosDatos(clienteActual)) {
        return false;
    }
    return Dialogos.mostrarDialogoConfirmacion("Datos del cliente",
            "Has cambiado los datos de «" + clienteActual.getNombre() + "» en esta factura.

"
                    + "¿Quieres guardar también esos cambios en su ficha de cliente?");
}
```

Se pregunta antes de guardar la factura para que, si la factura no llega a guardarse, la ficha no se haya tocado. Y solo **después** de que la factura se guarde bien:

```java
if (actualizarFicha) {
    Vista.getInstancia().getControlador().modificarCliente(cli);
    clienteActual = cli;
}
```

`clienteActual = cli` evita que vuelva a preguntar lo mismo si se guarda otra vez sin cambiar nada más.

## D8. Tests

- **Se borra** `ValidacionClienteTest` (355 líneas) y **se escribe `ClienteTest`** con lo mismo, pero contra los setters: nombre vacío, NIF vacío, NIF con formato malo, NIF con letra mala, NIF en minúsculas que se guarda en mayúsculas, CP vacío, CP de cuatro dígitos, CP que empieza por 53, localidad vacía, provincia vacía, email vacío que vale, email mal escrito, constructor copia y `equals` por NIF.
- **Se añade `ClientesTest`**, contra una base temporal: alta y listado, alta y búsqueda por nombre y por NIF, modificar, desactivar y que deje de salir en el listado de activos, baja, `tieneFacturas` y `buscar` de un id que no existe devolviendo `null`.
- **Se adaptan** `FacturasTest`, `EstadosTest`, `HistorialTest` y `FacturacionMensualTest` al constructor de `Facturas` sin `ClienteDAO`; donde creaban clientes con `new Cliente()` y setters sueltos, pasan al constructor.
- `CargaPantallasTest` añade `FichaCliente.fxml` a la lista de pantallas que carga.

---

## Decisiones

**1. Los setters validan, pero los comprobadores son públicos.** Si la ficha solo tuviera los setters, el primero que falla cortaría y únicamente se podría marcar un campo. Por eso cada comprobación vive en un `errorX` `static` **que el setter usa**: la regla sigue estando en un solo sitio, nada puede construir un `Cliente` inválido, y la ficha puede preguntar por los siete campos y marcarlos todos. Se conserva el comportamiento de hoy: **todos los campos malos en rojo y el aviso del primero.**

**2. `ValidacionCliente` no se borra.** La usan el Editor, la generación mensual y las rectificativas, que se rehacen en los módulos 5 y 6. Borrarla aquí obligaría a rehacer el Editor en este change. Queda duplicada la comprobación (los `errorX` de `Cliente` y `ValidacionCliente`) hasta el módulo 6, con los mismos mensajes en las dos.

**3. `Clientes` es singleton y `Modelo` ya no lo construye.** `Modelo.getClientes()` desaparece porque ninguna pantalla lo necesita ya. `Facturas`, `Estados` y `Rectificativas` piden `Clientes.getClientes()` directamente, como `Estados` pide hoy `facturas`.

**4. La ficha trabaja sobre una copia.** Ver D5.

**5. El borde rojo pasa a CSS.** Hoy es un `setStyle("-fx-border-color: #d32f2f; ...")` con el rojo escrito a mano, que no cambia con el tema.

## Riesgos y renuncias

- **Una fila de `cliente` incompleta deja de poder leerse.** Las columnas `nif`, `direccion`, `cp`, `localidad` y `provincia` admiten `NULL` en la tabla, y hasta hoy nada impedía guardar una fila a medias desde el Editor. A partir de ahora, `crearCliente` lanza un aviso al leerla y la pantalla de Clientes no se abre. Los dos clientes de la demostración están completos y sus NIF son válidos, así que con datos nuevos no pasa; si aparece con datos viejos, el aviso dirá qué falta y se arregla borrando la carpeta de datos, que es lo previsto.
- **Anular o rectificar una factura vieja sin datos de cliente ahora avisa** en vez de seguir con un cliente a medias (D7). Es más correcto, pero es un cambio de comportamiento que no se ve hasta que se prueba.
- **La comprobación del cliente queda duplicada** hasta el módulo 6 (decisión 2): los mismos siete mensajes están en `Cliente` y en `ValidacionCliente`. Si se toca uno hay que tocar el otro. Es la razón de que el módulo 6 la borre.
