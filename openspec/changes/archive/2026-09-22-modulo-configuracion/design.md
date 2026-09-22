## Situación de partida

| Pieza | Hoy |
|---|---|
| `ConfiguracionController` | 942 líneas: seis secciones, la clase interna `ItemSeccion`, una celda anónima para la lista lateral y un `StringConverter` anónimo para el tema (hay otro en Series) |
| `Configuracion.fxml` | 210 líneas: `ListView` lateral y las seis secciones apiladas en un `StackPane` |
| `Empresa` | Sin validar; 16 campos, 4 de ellos del logo (`logoX`, `logoY`, `logoAncho`, `logoAlto`) que se guardan y **nadie lee** |
| `Configuracion` + `ConfiguracionDAO` | La fila de la empresa y las preferencias; `datosPendientes()` repite las reglas de la empresa |
| `TiposIva`/`TiposRetencion` + sus DAO | Negocios que solo reenvían; SQL con `SELECT *` y ternarios |
| IVA y retenciones en pantalla | Filas de «alta rápida» debajo de la tabla, con Nuevo, Guardar e Inactivar/Activar |

Del módulo anterior quedan la franja del menú y cinco comprobaciones de «Faltan datos» que, con el bloqueo, dejan de tener sentido.

## Objetivos y lo que queda fuera

**Objetivo**: la empresa incompleta bloquea; `Empresa`, `TipoIva` y `TipoRetencion` se validan como `Cliente`; `Configuracion`, `TiposIva` y `TiposRetencion` llevan su SQL; IVA y retenciones usan ficha modal; y Configuración, que se ve igual, queda sin clases internas ni anónimas en todo lo que se rehace.

**Fuera**:
- **Series**: su sección, su negocio, `SerieDAO`, su `StringConverter` anónimo y el `enum` dentro de `Serie` se quedan como están. Van con la numeración en el módulo de facturas.
- `PreviaCabecera`, `DisposicionCabecera` y la cabecera del PDF no cambian por dentro.
- El ejercicio y la fecha de trabajo del arranque.

---

## D1. `Empresa`: igual que `Cliente`

`modelo/dominio/Empresa.java`, rehecha. **Obligatorios** (van en el constructor): nombre, NIF, dirección, CP, localidad, provincia, email y teléfono. **Opcionales** (con su setter): actividad, modo de cabecera, ruta del logo y pie legal. **Salen** `logoX`, `logoY`, `logoAncho` y `logoAlto`.

```java
/**
 * Datos fiscales y de contacto de la empresa, y cómo se pinta la cabecera de
 * sus facturas. Los setters comprueban lo que reciben, así que una Empresa que
 * existe tiene siempre sus datos obligatorios completos.
 */
public class Empresa {

    public static final String CABECERA_TEXTO = "TEXTO";
    public static final String CABECERA_LOGO = "LOGO";

    public Empresa(String nombre, String nif, String direccion, String cp, String localidad,
                   String provincia, String email, String telefono) throws Exception {
        setNombre(nombre);
        setNif(nif);
        setDireccion(direccion);
        setCp(cp);
        setLocalidad(localidad);
        setProvincia(provincia);
        setEmail(email);
        setTelefono(telefono);
        setActividad("");
        setCabeceraModo(CABECERA_TEXTO);
        setLogoPath("");
        setPieLegal("");
    }
}
```

Con constructor copia, y `equals`/`hashCode` por el NIF. Los setters de los obligatorios usan su `errorX`, como en `Cliente`:

| Comprobador | Mensaje |
|---|---|
| `errorNombre` | «Indique el nombre o razón social de la empresa.» |
| `errorNif` | Los tres de `Cliente`: vacío, formato y letra (con `ValidadorDocumentoFiscal`) |
| `errorDireccion` | «La dirección de la empresa es obligatoria.» |
| `errorCp` | Los dos de `Cliente` (con `ValidadorCodigoPostal`) |
| `errorLocalidad` | «La localidad de la empresa es obligatoria.» |
| `errorProvincia` | «La provincia de la empresa es obligatoria.» |
| `errorEmail` | Vacío: «El email de la empresa es obligatorio.»; mal escrito: «Revise el formato del correo electrónico.» (con `ValidadorEmail`) |
| `errorTelefono` | «El teléfono de la empresa es obligatorio.» |

A diferencia de `Cliente`, **el email es obligatorio**. El NIF se guarda en mayúsculas. Los opcionales guardan `""` cuando llegan vacíos; `setCabeceraModo` solo acepta `CABECERA_TEXTO` o `CABECERA_LOGO` (cualquier otra cosa lanza «Modo de cabecera no válido.»). Para saber si la cabecera es de logo, `isCabeceraLogo()`.

---

## D2. `Configuracion`: singleton con el SQL dentro

`modelo/negocio/Configuracion.java`, rehecha. Se trae el SQL de `ConfiguracionDAO`, que se borra.

| Método | Qué hace |
|---|---|
| `buscarEmpresa()` | Lee la fila `id = 1`. Si le falta algún dato obligatorio o no es válido, **devuelve `null`**; si está completa, la `Empresa` |
| `modificarEmpresa(Empresa empresa)` | `UPDATE empresa ... WHERE id = 1` con las columnas escritas |
| `preferencia(String clave)` | El valor guardado, o `null` |
| `guardarPreferencia(String clave, String valor)` | Inserta o actualiza, como hoy |

```java
/**
 * Leemos la empresa. Si le falta algún dato obligatorio devolvemos null: eso
 * es lo que hace que la aplicación se quede en Configuración hasta completarla.
 */
public Empresa buscarEmpresa() throws Exception {
    String consulta = """
            SELECT nombre, nif, direccion, cp, localidad, provincia, email, telefono,
                   actividad, cabecera_modo, logo_path, pie_legal
            FROM empresa WHERE id = 1
            """;
    try (PreparedStatement sentencia = Conexion.establecerConexion().prepareStatement(consulta);
         ResultSet fila = sentencia.executeQuery()) {
        if (!fila.next() || !datosCompletos(fila)) {
            return null;
        }
        return crearEmpresa(fila);
    } catch (SQLException e) {
        throw new Exception("Error SQLite: " + e.getMessage());
    }
}
```

`datosCompletos(ResultSet)` pregunta los ocho `errorX` de `Empresa` y devuelve `false` en cuanto uno se queja. `crearEmpresa(ResultSet)` usa el constructor y después los opcionales.

**Desaparecen** `datosPendientes()`, `empresaCompleta()` y `comprobarEmpresaCompleta()`. Lo que falta lo dicen los campos en rojo de la ficha (D7), con los mismos `errorX`.

---

## D3. `TipoIva` y `TipoRetencion`: se validan solos

**`TipoIva`**: constructor `TipoIva(String nombre, Integer porcentaje, boolean esSuplido)`. Opcionales: `motivoExencion`, `id` y `activo`.

| Comprobador | Regla |
|---|---|
| `errorNombre(String)` | Vacío: «Indique el nombre del tipo de IVA.» |
| `errorPorcentaje(String texto)` | Vacío vale (es **exento**). Si no es un entero o no está entre 0 y 100: «El porcentaje debe ser un número entero entre 0 y 100, o quedarse vacío si es exento.» |

`setPorcentaje(Integer)` comprueba el rango con `errorPorcentaje`. `setEsSuplido(true)` deja el porcentaje en `null`, como hoy. Getters de texto para la tabla: `getPorcentajeTexto()` (lo que hoy es `label()`: «Suplido», «Exento» o «21%»), `getSuplidoTexto()` y `getActivoTexto()` («Sí»/«No»). `toString()` devuelve el nombre. **Se borra `label()`**: solo lo usaba Configuración.

**`TipoRetencion`**: constructor `TipoRetencion(String nombre, int porcentaje)`. Opcionales: `id` y `activo`.

| Comprobador | Regla |
|---|---|
| `errorNombre(String)` | «Indique el nombre del tipo de retención.» |
| `errorPorcentaje(String texto)` | Vacío: «Indique el porcentaje de la retención.»; no entero o fuera de 0–100: «El porcentaje debe ser un número entero entre 0 y 100.» |

Getters de texto `getPorcentajeTexto()` («15%») y `getActivoTexto()`. `toString()` = «nombre (15%)». Se borra `label()`.

---

## D4. `TiposIva` y `TiposRetencion`: singletons con el SQL

Mismo patrón que `Clientes`: `getTiposIva()` / `getTiposRetencion()`, columnas escritas, `if (filas == 0)`, y `crearTipoIva(ResultSet)` / `crearTipoRetencion(ResultSet)` privados.

| Método | Qué hace |
|---|---|
| `listado(boolean soloActivos)` | Ordenados por id, como hoy |
| `buscar(long id)` | Uno, o `null` |
| `alta(TipoIva tipo)` | Inserta y devuelve el id |
| `modificar(TipoIva tipo)` | Guarda los cambios, **tras sus guardas** |
| `baja(long id)` | Borra el tipo, que no puede estar en ninguna factura |
| `enUso(long id)` | `true` si lo usa alguna factura |

Las guardas de `modificar` son las reglas que hoy están en `ConfiguracionController.guardarIva`, ahora en el negocio:

```java
/** Guardamos los cambios de un tipo de IVA, sin dejar cambiar lo que estropearía el histórico. */
public void modificar(TipoIva tipo) throws Exception {
    TipoIva actual = buscar(tipo.getId());
    if (actual == null) {
        throw new Exception("No se ha encontrado el tipo de IVA.");
    }
    if (actual.isEsSuplido() != tipo.isEsSuplido()) {
        throw new Exception("Un tipo existente no puede convertirse en suplido ni dejar de serlo.");
    }
    if (actual.isExento() != tipo.isExento()) {
        throw new Exception("Un tipo existente no puede pasar de porcentaje a exento ni al revés.");
    }
    if (enUso(tipo.getId()) && !mismoPorcentaje(actual, tipo)) {
        throw new Exception("El porcentaje de un tipo que ya aparece en facturas no se puede modificar.");
    }
    ...   // UPDATE
}
```

`TiposRetencion.modificar` solo tiene la tercera guarda. El activo/inactivo se guarda con `modificar`, desde la casilla de la ficha: **desaparecen** `setActivo(id, activo)` y el botón «Inactivar/Activar», como en Clientes.

`baja` borra con `DELETE ... WHERE id = ?` (con `if (filas == 0)`), tras su guarda:

```java
/** Borramos el tipo, que no puede aparecer en ninguna factura. */
public void baja(long id) throws Exception {
    if (enUso(id)) {
        throw new Exception("El tipo ya aparece en facturas y no se puede eliminar. Desactívalo en su ficha.");
    }
    ...
}
```

---

## D5. `Controlador` y `Modelo`

Una línea cada una:

| Operación | Negocio |
|---|---|
| `buscarEmpresa()`, `modificarEmpresa(Empresa)` | `Configuracion` |
| `preferencia(String)`, `guardarPreferencia(String, String)` | `Configuracion` |
| `listadoTiposIva(boolean)`, `buscarTipoIva(long)`, `altaTipoIva(TipoIva)`, `modificarTipoIva(TipoIva)`, `bajaTipoIva(long)`, `tipoIvaEnUso(long)` | `TiposIva` |
| `listadoTiposRetencion(boolean)`, `buscarTipoRetencion(long)`, `altaTipoRetencion(TipoRetencion)`, `modificarTipoRetencion(TipoRetencion)`, `bajaTipoRetencion(long)`, `tipoRetencionEnUso(long)` | `TiposRetencion` |

**Se borran** de `Modelo` los campos y getters `getConfiguracion()`, `getTiposIva()` y `getTiposRetencion()`, y de los dos el `datosPendientesEmpresa()` y el `comprobarDatosEmpresa()` del módulo anterior. Sus pocos usos fuera de Configuración pasan a estas operaciones (D11).

---

## D6. `Vista`, `GestorTemas` y la barra

**`Vista.mostrarInicio()`** vuelve a decidir:

```java
/** Menú principal si la empresa está completa; si no, Configuración, que se queda bloqueada. */
public void mostrarInicio() {
    try {
        if (controlador.buscarEmpresa() == null) {
            mostrar(CONFIGURACION);
        } else {
            mostrar(MENU);
        }
    } catch (Exception e) {
        Dialogos.mostrarDialogoError("Empresa", e.getMessage());
    }
}
```

Vuelve la constante `CONFIGURACION`. **Se borra** `comprobarDatosEmpresa()`.

**`BarraNavegacionController.bloquearSalvoSalir()`** vuelve, sin comparar textos:

```java
/** Desactivamos todos los botones menos Salir. */
public void bloquearSalvoSalir() {
    btnInicio.setDisable(true);
    btnNueva.setDisable(true);
    btnHistorico.setDisable(true);
    btnClientes.setDisable(true);
    btnConfiguracion.setDisable(true);
    btnCopias.setDisable(true);
}
```

**`GestorTemas`**: `aplicar(Scene escena)` sin el `Modelo` que no usaba; `guardar()` guarda con `Vista.getInstancia().getControlador().guardarPreferencia(...)`; y dos métodos nuevos para que el desplegable no necesite traductor: `nombres()` (la lista «Biblioteca8», «Omarchy»…) y `claveDe(String nombre)` (de «Negro y dorado» a `negro-dorado`).

---

## D7. `ConfiguracionController` y `Configuracion.fxml`

**Se ve igual. Sigue siendo un solo FXML con un solo controlador.**

### La lista lateral, en el FXML

El `ListView` pasa a un `VBox` con los títulos como `Label` y las secciones como `ToggleButton` de un mismo grupo:

```xml
<fx:define>
    <ToggleGroup fx:id="grupoSecciones"/>
</fx:define>
...
<VBox styleClass="lista-secciones" minWidth="200" prefWidth="200" maxWidth="200">
    <Label text="CONFIGURACIÓN GENERAL" styleClass="grupo-secciones" maxWidth="Infinity"/>
    <ToggleButton fx:id="btnEmpresa" text="Empresa" toggleGroup="$grupoSecciones" onAction="#verEmpresa"/>
    <ToggleButton fx:id="btnCabecera" text="Cabecera y pie" toggleGroup="$grupoSecciones" onAction="#verCabecera"/>
    <ToggleButton fx:id="btnPdf" text="PDF y apariencia" toggleGroup="$grupoSecciones" onAction="#verPdf"/>
    <Label text="CATÁLOGOS" styleClass="grupo-secciones" maxWidth="Infinity"/>
    <ToggleButton fx:id="btnIva" text="IVA" toggleGroup="$grupoSecciones" onAction="#verIva"/>
    <ToggleButton fx:id="btnRetenciones" text="Retenciones" toggleGroup="$grupoSecciones" onAction="#verRetenciones"/>
    <ToggleButton fx:id="btnSeries" text="Series" toggleGroup="$grupoSecciones" onAction="#verSeries"/>
</VBox>
```

Cada botón llama a un método de una línea, por ejemplo `verEmpresa` → `mostrarSeccion(seccionEmpresa, btnEmpresa, true)`:

```java
/**
 * Enseñamos una sección y ocultamos las demás. Volvemos a marcar su botón
 * porque en un grupo de ToggleButton se puede desmarcar el que ya estaba
 * pulsado, y entonces no quedaría ninguna sección a la vista.
 */
private void mostrarSeccion(VBox seccion, ToggleButton boton, boolean conGuardar) {
    for (Node panel : pilaSecciones.getChildren()) {
        panel.setVisible(false);
        panel.setManaged(false);
    }
    seccion.setVisible(true);
    seccion.setManaged(true);
    boton.setSelected(true);
    barraGuardar.setVisible(conGuardar);
    barraGuardar.setManaged(conGuardar);
}
```

Con Guardar: Empresa, Cabecera y pie, y PDF y apariencia. Sin Guardar: IVA, Retenciones y Series. `initialize` termina con `mostrarSeccion(seccionEmpresa, btnEmpresa, true)`.

En `temas/base.css` se sustituyen las reglas de `.lista-secciones .list-cell` por las equivalentes para botones, con los mismos valores para que se vea igual:

```css
.lista-secciones { -fx-spacing: 0; }

.lista-secciones .toggle-button {
    -fx-background-color: transparent;
    -fx-background-insets: 0;
    -fx-border-width: 0;
    -fx-padding: 8px 10px;
    -fx-font-size: 13px;
    -fx-text-fill: -fx-text-background-color;
    -fx-alignment: CENTER_LEFT;
    -fx-max-width: Infinity;
    -fx-cursor: hand;
}

.lista-secciones .toggle-button:hover {
    -fx-background-color: derive(-fx-accent, 90%);
    -fx-background-radius: 8px;
}

.lista-secciones .toggle-button:selected {
    -fx-background-color: derive(-fx-accent, 84%);
    -fx-background-radius: 8px;
    -fx-text-fill: derive(-fx-accent, -35%);
}

.lista-secciones .grupo-secciones {
    -fx-font-size: 12px;
    -fx-padding: 12px 4px 4px 4px;
    -fx-opacity: 0.70;
    -fx-border-color: transparent transparent -fx-text-background-color transparent;
    -fx-border-width: 0 0 1 0;
}
```

**Se borran** `ItemSeccion`, `configurarSecciones`, la celda anónima y el `ListView`.

### El bloqueo

```java
@Override
public void initialize(URL url, ResourceBundle rb) {
    barraController.marcarActivo("configuracion");
    cargarTema();
    cargarEmpresa();
    cargarPreferenciasPdf();
    cargarIvas();
    cargarRetenciones();
    cargarSeries();
    cablearPrevia();
    mostrarSeccion(seccionEmpresa, btnEmpresa, true);
}
```

`cargarEmpresa()` pide `buscarEmpresa()`. Si es `null`, activa el bloqueo: `bloqueada = true`, muestra `lblDatosPendientes`, propone el nombre visible de la empresa en `txtNombre`, llama a `barraController.bloquearSalvoSalir()` y desactiva `btnVolver`. Si no, rellena los campos con la empresa.

Texto de `lblDatosPendientes`: «Para empezar a usar el programa completa los datos de tu empresa. Rellena los campos marcados con * y pulsa «Guardar configuración».»

«Cambiar de empresa» **no** se bloquea.

### El Guardar global

Como en la ficha de cliente: primero se marcan **todos** los campos malos con los `errorX`, y solo si no hay ninguno se construye la `Empresa`.

```java
@FXML
void guardar(ActionEvent event) {
    String error = marcarCamposMalos();
    if (error != null) {
        mostrarSeccion(seccionEmpresa, btnEmpresa, true);
        Dialogos.mostrarDialogoError("Datos de la empresa", error);
        return;
    }
    try {
        Vista.getInstancia().getControlador().modificarEmpresa(empresaDeLosCampos());
        guardarPreferenciasPdf();
        GestorTemas.guardar();
        if (bloqueada) {
            Dialogos.mostrarDialogoInformacion("Configuración", "Datos de la empresa completados.");
            Vista.getInstancia().mostrar("MenuPrincipal.fxml");
        } else {
            Dialogos.mostrarDialogoInformacion("Configuración", "Configuración guardada.");
        }
    } catch (Exception e) {
        Dialogos.mostrarDialogoError("Configuración", e.getMessage());
    }
}
```

Si hay errores, se enseña la sección Empresa, porque los campos marcados están ahí aunque se haya pulsado Guardar desde otra sección.

- `marcarCamposMalos()` y `revisar(...)`: iguales que en `FichaClienteController`, con los ocho campos obligatorios y `Empresa.errorX`.
- `empresaDeLosCampos()`: el constructor con los ocho campos y, después, los opcionales: la actividad; el modo de cabecera (`CABECERA_LOGO` si está marcado `rbLogo`, si no `CABECERA_TEXTO`); la ruta del logo; y el pie legal.
- `guardarPreferenciasPdf()`: la carpeta automática y el color, como hoy.

### La vista previa de la cabecera

Hoy `repintarPrevia()` fabrica una `Empresa` con `new Empresa()` y los campos tal como estén. Ahora la construye con `empresaDeLosCampos()` dentro de un `try`: si los datos están incompletos, llama a `previaCabecera.mostrar(null, color)` y `PreviaCabecera` pinta el texto «La vista previa aparece cuando los datos de la empresa estén completos.» en lugar de la cabecera. Se añade `txtNombre.textProperty()` a los disparadores, para que la vista previa aparezca en cuanto se completan los datos.

`PreviaCabecera` solo cambia en eso: con `null` pinta el aviso en vez de crear un `new Empresa()`.

### El tema, sin traductor

`comboTema` pasa a `ComboBox<String>` con `GestorTemas.nombres()`. Al elegir uno, `GestorTemas.seleccionar(escena, GestorTemas.claveDe(nombre))`. **Se borra el `StringConverter` anónimo** de `cargarTema`.

### IVA y retenciones

La tabla se queda y las filas de alta rápida se van. Debajo de cada tabla, **Nuevo**, **Editar** y **Eliminar**, como la fila de botones de Series; doble clic para editar. Columnas con `PropertyValueFactory` sobre los getters de texto (D3).

Todo igual que `ClientesController`: campo `ivaElegido` con `seleccionarIva(MouseEvent)`, `nuevoIva`, `editarIva`, `eliminarIva` y `abrirFichaIva(TipoIva registro, String titulo)`, que carga el FXML, le da el registro y usa `crearVentanaModal(raiz, titulo, ficha)`. Al volver, `altaTipoIva` o `modificarTipoIva`, y se refresca la tabla. `eliminarIva` pide confirmación y llama a `bajaTipoIva`; si el tipo está en uso, el aviso del negocio dice que se desactive en su ficha. Lo mismo para retenciones.

**Se borran** `nuevoIva`/`guardarIva`/`inactivarIva` tal como están hoy, sus equivalentes de retenciones, `enUsoIva`, `enUsoRetencion` y los campos de las filas de alta rápida (del FXML y del controlador).

### Series

**No se toca.** `cargarSeries`, `guardarSerie`, `eliminarSerie`, su `StringConverter` y su fila de alta rápida se quedan igual.

---

## D8. Las fichas de IVA y de retención

`FichaTipoIva.fxml` + `FichaTipoIvaController` y `FichaTipoRetencion.fxml` + `FichaTipoRetencionController`, nuevos, **copiando el patrón de `FichaClienteController`**: `setRegistro`/`getRegistro` con `original` y `registro`, `marcarCamposMalos`/`revisar`, `fotoDeLosCampos`/`confirmarDescartar`, `implements Pantalla, Initializable` con `puedeCerrar()`, y `cerrarVentana`.

| Ficha | Campos |
|---|---|
| IVA | Nombre*, Porcentaje (vacío = exento), «Es suplido», Motivo de exención, «Activo» |
| Retención | Nombre*, Porcentaje*, «Activo» |

En **modo editar**, `setRegistro` pregunta `tipoIvaEnUso(id)` al controlador y:
- desactiva «Es suplido» (un tipo existente no cambia de naturaleza);
- desactiva el porcentaje si el tipo es exento, suplido o ya está en uso, y enseña el aviso «El porcentaje de un tipo que ya aparece en facturas no se puede modificar.» cuando es por estar en uso.

En **modo añadir**, marcar «Es suplido» desactiva y vacía el porcentaje. El `addListener` usa los parámetros `(propiedad, anterior, nuevo)` y llama a un método con nombre.

Si aun así se intenta algo que las guardas de D4 no permiten (por ejemplo, vaciar el porcentaje de un tipo que no está en uso), el aviso llega al guardar, desde la pantalla principal.

---

## D9. El menú, sin franja

Se borran de `MenuPrincipal.fxml` la franja `franjaDatosPendientes` y, de `MenuPrincipalController`, `mostrarDatosPendientes()`, `completarDatos()` y sus campos. En `base.css` se borra `.franja-aviso`. `cargarEmpresa()` del menú pide `buscarEmpresa()`, que ahí nunca es `null`.

---

## D10. Base de datos

- `db/crear_tablas.sql`: fuera `logo_x`, `logo_y`, `logo_ancho` y `logo_alto` de `empresa`.
- `CopiaSeguridadDAO`: fuera esas cuatro columnas de la lista de `empresa`.

Las bases que ya existan conservan esas columnas y no pasa nada: todas las consultas escriben sus columnas.

---

## D11. Solo plumbing

| Dónde | Cambio |
|---|---|
| `EditorController` | `getConfiguracion().getEmpresa()` → `buscarEmpresa()`; `getPreferencia`/`setPreferencia` → `preferencia`/`guardarPreferencia`; `getTiposIva().listar/getById` → `listadoTiposIva`/`buscarTipoIva`; `getTiposRetencion().listar` → `listadoTiposRetencion`. Se quitan sus tres `comprobarDatosEmpresa()`. «Sin retención» se crea con `new TipoRetencion("Sin retención", 0)`. Los tipos rehechos a partir de una factura usan el constructor; si el nombre guardado está vacío, «Tipo de IVA» o «Retención» |
| `HistoricoController` | Igual con empresa y preferencias; se quita su `comprobarDatosEmpresa()` |
| `GenerarFacturasMensualesController` | `listadoTiposIva`, `listadoTiposRetencion`, «Sin retención» con el constructor, `GestorTemas.aplicar(escena)`; se quita su `comprobarDatosEmpresa()` |
| `CopiaSeguridadController` | `getConfiguracion().getEmpresa()` → `buscarEmpresa()` |
| `Empresas.recordarTema()` | `Configuracion.getConfiguracion().preferencia(...)` en lugar de `new ConfiguracionDAO()` |
| `Facturas.retencionDeVersion` y `Rectificativas` | El constructor de `TipoRetencion`, con el mismo nombre de reserva. `Rectificativas` usa `TiposRetencion.getTiposRetencion().buscar(id)` y pierde el `TipoRetencionDAO` de su constructor |
| `Vista.mostrar` | `GestorTemas.aplicar(escena)` |

Todos los métodos que empiezan a lanzar `Exception` por esto se declaran `throws Exception` y quien los llama se ajusta, como en el módulo de clientes.

---

## D12. Tests

- **Nuevos**: `EmpresaTest` (cada obligatorio vacío falla con su mensaje, NIF/CP/email mal escritos, NIF en mayúsculas, opcionales vacíos, modo de cabecera no válido, constructor copia, cada `errorX` igual que su setter); `TipoIvaTest` y `TipoRetencionTest` (nombre vacío, porcentaje fuera de rango o no numérico, exento, suplido deja el porcentaje en `null`, getters de texto); `TiposIvaTest` y `TiposRetencionTest` contra una base temporal (alta y listado, solo activos, buscar un id que no existe, modificar, cada guarda de `modificar`, `baja` y `baja` en uso).
- **Se rehace** `ConfiguracionTest`: `buscarEmpresa()` devuelve `null` con la fila vacía y con un dato inválido, y la empresa tras `modificarEmpresa`; y las preferencias.
- **Se borra** `TipoRetencionDAOTest` (sus casos pasan a `TiposRetencionTest`).
- **Se adaptan** los que construyen `Empresa`, `TipoIva` o `TipoRetencion` con `new X()` y setters (`CalculosTest`, `FacturasTest`, `FacturacionMensualTest`, `ConstructorDocumentoFacturaTest`, `DisposicionCabeceraTest`, `ExportadorPdfTest`) y los que usaban `ConfiguracionDAO` (`CopiaSeguridadTest`, `EmpresasTest`).
- `CargaPantallasTest`: se añaden `FichaTipoIva.fxml` y `FichaTipoRetencion.fxml`.

---

## Decisiones

**1. La empresa incompleta vuelve a bloquear** (decisión del usuario del 22/09, que revierte F8). Directo a Configuración; se pueden usar todas sus secciones, Guardar y «Cambiar de empresa»; la barra solo deja Salir.

**2. Una empresa incompleta es `null`**, igual que no existe un `Cliente` a medias. Solo dos sitios lo miran: `Vista.mostrarInicio` y `ConfiguracionController.cargarEmpresa`. El resto nunca la ve, porque no se puede llegar a ellos.

**3. Un solo FXML y un solo controlador para Configuración**, con el Guardar global, que así lee sus campos directamente. La lista lateral se ve igual, escrita en el FXML.

**4. Series se queda como está** hasta el módulo de facturas, porque su negocio es la numeración.

**5. Las reglas de qué no se puede cambiar de un tipo** pasan al negocio (`modificar`), y la ficha desactiva los controles para que normalmente ni se intente.

**6. Activo/inactivo desde la casilla de la ficha**, como en Clientes. Desaparece el botón «Inactivar/Activar».

## Riesgos y renuncias

- **Se deshace parte del módulo anterior**: la franja del menú y las cinco comprobaciones.
- **Una empresa incompleta abre Configuración con los campos vacíos**, aunque en la base tuviera algunos datos rellenos (solo puede pasar con copias o datos antiguos, porque Configuración nunca guarda una empresa a medias). Hay que volver a escribirlos.
- **La vista previa de la cabecera no se ve hasta que los datos están completos.**
- **`ConfiguracionController` sigue siendo grande** (unas 550 líneas), porque es una pantalla con seis secciones y la de Series no se toca todavía.
- **Guardar desde otra sección con la empresa mal** salta a la sección Empresa para enseñar los campos marcados.
