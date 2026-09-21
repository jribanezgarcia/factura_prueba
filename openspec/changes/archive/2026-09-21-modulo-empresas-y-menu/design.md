## Situación de partida

| Fichero | Qué hace hoy |
|---|---|
| `modelo/negocio/Empresas` | 169 líneas de métodos `static`: `listarEmpresas`, `crearEmpresa`, `conectar`, `eliminarEmpresa`, `registrarNombre`, `recordarTema`, `slugDe` y el catálogo `empresas.properties`. Lleva dentro `public record EmpresaInfo(String slug, String nombre)`. |
| `modelo/negocio/Sesion` | Dos campos `static` (empresa y fecha de trabajo) con `inicializar`, `empresaSlug`, `fechaTrabajo` y `reiniciar`. |
| `vista/controlador/ArranqueController` | Crear (con `TextInputDialog`) y elegir empresa. Dos `ListCell` anónimas solo para escribir el nombre en el desplegable. Streams con `::` para preseleccionar. No se puede eliminar. |
| `vista/controlador/ConfiguracionController` | Sección «Empresas» con tabla, «Nueva empresa», «Cambiar a esta» y «Eliminar». Si la empresa está incompleta, **bloquea** la barra (`bloquearSalvoSalir`) y el botón Volver. |
| `vista/Vista.mostrarInicio` | Menú si la empresa está completa; si no, Configuración. |

Quien usa `Empresas` y `Sesion`, además de los anteriores: `fichero/CopiaSeguridad`, `CargarDemo`, `PreparacionDatos`, `CopiaSeguridadController`, `Reloj` y cinco tests.

## Objetivos y lo que queda fuera

**Objetivo**: las empresas se crean, se eligen y se eliminan **solo en el arranque**; Configuración solo ofrece volver a él; entrar en una empresa **nunca bloquea**, y los datos obligatorios se exigen justo donde hacen falta. `Empresas` y `Sesion` quedan como el resto del negocio.

**Fuera**:
- El ejercicio y la fecha de trabajo del arranque, tal como están (F4). `restringirAlEjercicio` no se toca.
- `Empresa` (los datos fiscales) con validación en sus setters: va con Configuración, módulo 4.
- «¿Cambiar a ella?» tras restaurar una copia sigue en Copias; que vuelva al arranque es del módulo de copias (F12).
- `Conexion` guarda también su propia carpeta activa; se queda así (es una herramienta).

---

## D1. `EmpresaDisponible`: el `record` pasa a clase de datos

`modelo/dominio/EmpresaDisponible.java`, nueva. Es una empresa que se puede abrir desde el arranque: el nombre de su **carpeta** de datos y el **nombre** que ve el usuario. Sustituye a `Empresas.EmpresaInfo`.

```java
/**
 * Una empresa que se puede abrir desde el arranque: el nombre de su carpeta
 * de datos y el nombre que ve el usuario. Se ordena por nombre.
 */
public class EmpresaDisponible implements Comparable<EmpresaDisponible> {

    private String carpeta;
    private String nombre;

    public EmpresaDisponible(String carpeta, String nombre) throws Exception {
        setCarpeta(carpeta);
        setNombre(nombre);
    }

    public void setCarpeta(String carpeta) throws Exception {
        if (carpeta == null || carpeta.isBlank()) {
            throw new Exception("La empresa no tiene carpeta de datos.");
        }
        this.carpeta = carpeta.trim();
    }

    /** Si no tiene nombre guardado, usamos el de su carpeta. */
    public void setNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            this.nombre = carpeta;
        } else {
            this.nombre = nombre.trim();
        }
    }

    /** Ordenamos por nombre, sin distinguir mayúsculas. */
    @Override
    public int compareTo(EmpresaDisponible otra) {
        return nombre.compareToIgnoreCase(otra.getNombre());
    }

    /** Lo que se ve en el desplegable del arranque. */
    @Override
    public String toString() {
        return nombre;
    }
}
```

Con getters, `equals` y `hashCode` por la carpeta. `setNombre` va después de `setCarpeta` en el constructor porque la usa.

Gracias a `toString()`, el `ComboBox<EmpresaDisponible>` del arranque se escribe solo: **se borran las dos `ListCell` anónimas** de `configurarListaEmpresas`. Y gracias a `equals` por carpeta, `cmbEmpresa.setValue(empresa)` selecciona la que toca aunque sea otro objeto.

---

## D2. `Empresas`: singleton, con los verbos del negocio

`modelo/negocio/Empresas.java`, rehecha. Mismo catálogo `empresas.properties` y misma forma de formar la carpeta; cambian la forma y los nombres.

```java
/**
 * Las empresas de la aplicación: cada una es una carpeta con su base de datos,
 * y su nombre visible se guarda en el catálogo empresas.properties.
 */
public class Empresas {

    private static final String CATALOGO = "empresas.properties";
    private static Empresas empresas;

    private Empresas() {
    }

    public static Empresas getEmpresas() {
        if (empresas == null) {
            empresas = new Empresas();
        }
        return empresas;
    }
}
```

| Antes (`static`) | Ahora | Qué hace |
|---|---|---|
| `listarEmpresas()` | `listado()` | Las empresas ordenadas por nombre (`Collections.sort`, gracias a `Comparable`) |
| `crearEmpresa(nombre)` | `alta(String nombre)` | Crea carpeta, base y entrada del catálogo; devuelve la `EmpresaDisponible` |
| `eliminarEmpresa(slug)` | `baja(String carpeta)` | Quita del catálogo y borra la carpeta. No deja borrar la empresa en uso |
| `conectar(slug, fecha)` | `abrir(String carpeta, LocalDate fecha)` | La fija como activa, abre su base, inicia la sesión, la recuerda y copia su tema |
| — | `cerrar()` | Cierra la base de datos y termina la sesión |
| `registrarNombre` | `registrarNombre(String carpeta, String nombre)` | Igual |
| `recordarTema` | `recordarTema()` | Igual |
| `slugDe(nombre)` | `carpetaDe(String nombre)` | Igual, sin ternarios |

Todos los públicos `throws Exception`. `IllegalArgumentException` pasa a `Exception`, con estos mensajes:
- `alta`: «Indique el nombre de la empresa.» si llega vacío; «Ya existe una empresa con ese nombre de carpeta: X.» si la carpeta existe.
- `baja`: «La empresa en uso no se puede eliminar.»

`abrir` y `cerrar`:

```java
/** Abrimos la empresa: la dejamos activa, conectamos con su base y empezamos la sesión. */
public void abrir(String carpeta, LocalDate fecha) throws Exception {
    Conexion.setEmpresaActiva(carpeta);
    Conexion.cerrarConexion();
    Conexion.establecerConexion();
    Sesion.getSesion().iniciar(carpeta, fecha);
    PreferenciasGlobales.set(PreferenciasGlobales.ULTIMA_EMPRESA, carpeta);
    recordarTema();
}

/** Cerramos la empresa en uso, para poder volver al arranque. */
public void cerrar() {
    Conexion.cerrarConexion();
    Sesion.getSesion().terminar();
}
```

`borrarRecursivo`, que hoy usa `var`, streams, `Comparator.reverseOrder()` y una lambda con `try` dentro, se rehace como un método que se llama a sí mismo:

```java
/**
 * Borramos una carpeta con todo lo que tiene dentro. Cómo funciona: un
 * directorio no se puede borrar si no está vacío, así que primero borramos
 * cada cosa de dentro (y si es otra carpeta, la vaciamos llamando a este
 * mismo método) y al final la propia carpeta.
 */
private void borrarCarpeta(File carpeta) throws Exception {
    File[] contenido = carpeta.listFiles();
    if (contenido != null) {
        for (File hijo : contenido) {
            if (hijo.isDirectory()) {
                borrarCarpeta(hijo);
            } else if (!hijo.delete()) {
                throw new Exception("No se pudo borrar " + hijo.getName() + ". ¿Está abierta la empresa?");
            }
        }
    }
    if (!carpeta.delete()) {
        throw new Exception("No se pudo borrar la carpeta " + carpeta.getName() + ".");
    }
}
```

Hoy los fallos al borrar se tragan en silencio (`catch (IOException ignored)`); ahora se avisa.

---

## D3. `Sesion`: singleton

`modelo/negocio/Sesion.java`, rehecha. Lo mismo, como objeto:

```java
/**
 * La empresa abierta y la fecha de trabajo elegidas en el arranque. La fecha
 * de trabajo es la inicial de las facturas nuevas.
 */
public class Sesion {

    private static Sesion sesion;
    private String carpetaEmpresa;
    private LocalDate fechaTrabajo;

    private Sesion() {
    }

    public static Sesion getSesion() { ... }

    public void iniciar(String carpetaEmpresa, LocalDate fechaTrabajo) { ... }

    /** Olvidamos la empresa y la fecha: no hay ninguna empresa abierta. */
    public void terminar() { ... }

    public String getCarpetaEmpresa() { ... }
    public LocalDate getFechaTrabajo() { ... }
}
```

`Reloj.fechaTrabajo()` pasa a `Sesion.getSesion().getFechaTrabajo()`, con `if / else` en lugar del ternario.

---

## D4. Las operaciones en `Controlador` y `Modelo`

Una línea cada una, como las de clientes:

| Operación | Llama a |
|---|---|
| `listadoEmpresas()` | `Empresas.getEmpresas().listado()` |
| `altaEmpresa(String nombre)` | `alta` |
| `bajaEmpresa(String carpeta)` | `baja` |
| `abrirEmpresa(String carpeta, LocalDate fecha)` | `abrir` |
| `cerrarEmpresa()` | `cerrar` |
| `datosPendientesEmpresa()` | `configuracion.datosPendientes()` |
| `comprobarDatosEmpresa()` | `configuracion.comprobarEmpresaCompleta()` |

En `Configuracion` se añade un método; el resto de la clase no se toca:

```java
/** Lanzamos un aviso con los datos que faltan si la empresa no está completa. */
public void comprobarEmpresaCompleta() throws Exception {
    List<String> faltan = datosPendientes();
    if (!faltan.isEmpty()) {
        throw new Exception("Faltan datos de tu empresa: " + String.join(", ", faltan) + ".\n\n"
                + "Complétalos en Configuración para poder guardar facturas y exportar PDF.");
    }
}
```

---

## D5. Las dos ventanas: `Vista` vuelve al arranque

En `Vista` entra la constante `ARRANQUE` (sale de `LanzadorVentanaPrincipal`) y tres métodos:

```java
/** Ponemos la pantalla de arranque en esa ventana, sin mostrarla todavía. */
public ArranqueController prepararArranque(Stage ventanaArranque) {
    setVentana(ventanaArranque);
    return (ArranqueController) mostrar(ARRANQUE);
}

/**
 * Cerramos la empresa en uso y volvemos a la pantalla de arranque. Abrimos la
 * ventana de arranque antes de cerrar la principal para que nunca se queden
 * cero ventanas, porque entonces JavaFX cerraría la aplicación.
 */
public void volverAlArranque() {
    Stage principal = ventana;
    controlador.cerrarEmpresa();
    Stage arranque = new Stage();
    prepararArranque(arranque);
    arranque.show();
    principal.close();
}

/**
 * Antes de guardar una factura, rectificar, generar las mensuales o exportar
 * PDF, comprobamos que la empresa tiene sus datos completos. Si no, avisamos
 * de lo que falta y devolvemos false.
 */
public boolean comprobarDatosEmpresa() {
    try {
        controlador.comprobarDatosEmpresa();
        return true;
    } catch (Exception e) {
        Dialogos.mostrarDialogoAdvertencia("Datos de la empresa", e.getMessage());
        return false;
    }
}
```

`principal.close()` no lanza `onCloseRequest`, así que **no** sale la pregunta de «¿Seguro que deseas salir?».

`mostrarInicio()` pasa a mostrar siempre el menú, y se borra la constante `CONFIGURACION`.

`LanzadorVentanaPrincipal.start` usa `prepararArranque(stage)` en lugar de hacer él el `setVentana` y el `mostrar`.

---

## D6. El arranque: crear, elegir y **eliminar**

`Arranque.fxml`: al lado de «Nueva...», un botón **«Eliminar»** (`fx:id="btnEliminarEmpresa"`, `onAction="#eliminarEmpresa"`).

`ArranqueController`:

- El desplegable pasa a `ComboBox<EmpresaDisponible>`. **Se borran** las dos `ListCell` anónimas de `configurarListaEmpresas`; se queda solo el `addListener` que activa los botones.
- `cargarEmpresas()` pide `listadoEmpresas()` al controlador y preselecciona la última con un `for` que compara carpetas, en lugar del stream con `::`. El texto de ayuda de la demo compara con `CargarDemo.CARPETA`.
- `actualizarBoton()` activa **Entrar** y **Eliminar** solo si hay una empresa elegida.
- `nuevaEmpresa()` sigue con el `TextInputDialog`, leído con `isPresent()` y `get()` (el único `Optional` que permite `AGENTS.md`), llama a `altaEmpresa` y deja elegida la nueva con `cmbEmpresa.setValue(nueva)`.
- `entrar()` llama a `abrirEmpresa(elegida.getCarpeta(), fecha)`.
- `eliminarEmpresa()`, nuevo:

```java
@FXML
private void eliminarEmpresa() {
    EmpresaDisponible elegida = cmbEmpresa.getValue();
    if (elegida == null) {
        lblError.setText("Selecciona la empresa que quieres eliminar.");
        return;
    }
    if (!Dialogos.mostrarDialogoConfirmacion("Eliminar empresa",
            "¿Seguro que quieres eliminar «" + elegida.getNombre() + "»?\n\n"
                    + "Se borrará su carpeta de datos con sus facturas, clientes y configuración. "
                    + "No se puede deshacer: si la necesitas, haz antes una copia de seguridad.")) {
        return;
    }
    try {
        Vista.getInstancia().getControlador().bajaEmpresa(elegida.getCarpeta());
        cargarEmpresas();
    } catch (Exception e) {
        lblError.setText("No se pudo eliminar la empresa: " + e.getMessage());
    }
}
```

En el arranque nunca hay una empresa abierta (al volver de Configuración se cierra antes, D5), así que se puede eliminar cualquiera de la lista.

Los dos textos de bienvenida de `mostrarAvisoInicial` dejan de decir que hay que completar los datos antes de empezar y que la demostración se elimina desde «Configuración > Empresas»:
- Con la demo recién cargada: «…Cuando quieras trabajar con tu empresa, créala con «Nueva…». El menú te avisará de los datos fiscales y de contacto que le falten. La empresa de demostración se puede eliminar desde esta misma pantalla con «Eliminar».»
- Sin ninguna empresa: «Para iniciar el programa crea tu empresa con «Nueva…». El menú te avisará de los datos que le falten.»

---

## D7. Configuración: sin sección Empresas, sin bloqueo y con «Cambiar de empresa»

`Configuracion.fxml` y `ConfiguracionController`:

- **Se borra la sección «Empresas»**: el `VBox seccionEmpresas` del FXML, su entrada en `configurarSecciones`, los campos `tablaEmpresas`, `colEmpresaNombre`, `colEmpresaSlug`, `lblEmpresasAviso` y la lista `empresas`, y los métodos `cargarEmpresas`, `refrescarEmpresas`, `nuevaEmpresa`, `cambiarEmpresa` y `eliminarEmpresa`.
- **Se quita el bloqueo** de `initialize`: fuera `barraController.bloquearSalvoSalir()` y `btnVolver.setDisable(true)`. Se quedan el texto `lblDatosPendientes` y el nombre propuesto cuando falta. `nombreVisibleEmpresaActiva()` busca la carpeta de `Sesion.getSesion().getCarpetaEmpresa()` en `listadoEmpresas()`, con un `for`.
- Nuevo texto de `lblDatosPendientes`: «Faltan datos de tu empresa. Hasta completarlos no podrás guardar facturas, generar las mensuales ni exportar PDF. Rellena los campos marcados con * y pulsa «Guardar configuración».»
- **Botón «Cambiar de empresa»** en la barra de abajo, a la izquierda de «Guardar configuración» (`onAction="#cambiarDeEmpresa"`):

```java
/** Volvemos al arranque para elegir otra empresa, avisando de lo que no se haya guardado. */
@FXML
private void cambiarDeEmpresa() {
    if (Dialogos.mostrarDialogoConfirmacion("Cambiar de empresa",
            "Se cerrará esta empresa y volverás a la pantalla de arranque.\n\n"
                    + "Lo que no hayas guardado en Configuración se perderá. ¿Continuar?")) {
        Vista.getInstancia().volverAlArranque();
    }
}
```

La barra de abajo se oculta en IVA, Retenciones y Series, como hoy; así que «Cambiar de empresa» se ve en Empresa, Cabecera y pie, y PDF y apariencia.

`BarraNavegacionController.bloquearSalvoSalir()` se queda sin uso y **se borra**.

---

## D8. El menú: el aviso de datos pendientes

`MenuPrincipal.fxml`, en el `VBox` de arriba, debajo de la fecha de trabajo, una franja oculta de entrada:

```xml
<HBox fx:id="franjaDatosPendientes" styleClass="franja-aviso" alignment="CENTER_LEFT" spacing="12"
      visible="false" managed="false" maxWidth="750">
    <Label fx:id="lblDatosPendientes" wrapText="true" HBox.hgrow="ALWAYS"/>
    <Button onAction="#completarDatos" styleClass="primary-button, btn-suave" text="Completar datos"/>
</HBox>
```

`MenuPrincipalController`, en `initialize`:

```java
/** Si a la empresa le faltan datos, enseñamos la franja con lo que falta. */
private void mostrarDatosPendientes() {
    try {
        List<String> faltan = Vista.getInstancia().getControlador().datosPendientesEmpresa();
        boolean hayQueCompletar = !faltan.isEmpty();
        franjaDatosPendientes.setVisible(hayQueCompletar);
        franjaDatosPendientes.setManaged(hayQueCompletar);
        lblDatosPendientes.setText("Faltan datos de tu empresa: " + String.join(", ", faltan)
                + ". Hasta completarlos no podrás guardar facturas ni exportar PDF.");
    } catch (Exception e) {
        Dialogos.mostrarDialogoError("Menú", e.getMessage());
    }
}

@FXML
void completarDatos(ActionEvent event) {
    Vista.getInstancia().mostrar("Configuracion.fxml");
}
```

Configuración abre por la sección Empresa, que es la primera.

En `temas/base.css`, con el color de aviso que ya tiene cada tema:

```css
/* Franja del menú con los datos de empresa que faltan */
.franja-aviso {
    -fx-background-color: derive(-fx-accent-error, 88%);
    -fx-border-color: -fx-accent-error;
    -fx-border-width: 0 0 0 4;
    -fx-background-radius: 6;
    -fx-padding: 8 12 8 12;
}
```

---

## D9. Dónde se exigen los datos

Una línea al principio de cada una de estas cinco acciones:

| Pantalla | Método | Línea |
|---|---|---|
| `EditorController` | `guardar()` (devuelve `boolean`) | `if (!Vista.getInstancia().comprobarDatosEmpresa()) { return false; }` |
| `EditorController` | `exportarPdf()` | `if (!Vista.getInstancia().comprobarDatosEmpresa()) { return; }` |
| `EditorController` | `crearRectificativa()` | igual |
| `HistoricoController` | `exportarPdf()` | igual |
| `GenerarFacturasMensualesController` | `generar()` | igual |

Es una comprobación de pantalla porque las pantallas todavía llaman al negocio por `getModelo()`; cuando los módulos 5 a 9 rehagan esas pantallas y su negocio, la comprobación irá con ellos. Abrir el Editor, escribir y mirar el Histórico sigue siendo posible.

`guardar()` también se llama desde «Guardar y salir» al abandonar el Editor con cambios: si la empresa está incompleta, avisa y se queda en el Editor, que es lo correcto.

---

## D10. Solo plumbing

Cambian las llamadas; no lo que hacen.

| Fichero | Cambio |
|---|---|
| `fichero/CopiaSeguridad` | `restaurarComoEmpresaNueva` devuelve `EmpresaDisponible` y usa `Empresas.getEmpresas().alta/baja`; `recordarTema()` por el singleton; `throws Exception` donde haga falta. |
| `CargarDemo` | `SLUG` pasa a `CARPETA`; `cargar()` devuelve `EmpresaDisponible`; `alta`, `registrarNombre` y `baja` por el singleton. |
| `PreparacionDatos` | `listado()` por el singleton y `CargarDemo.CARPETA`. |
| `CopiaSeguridadController` | `nombreEmpresaActiva()` con `Sesion.getSesion().getCarpetaEmpresa()` y `listadoEmpresas()`; tras restaurar como empresa nueva y aceptar cambiar, `abrirEmpresa(nueva.getCarpeta(), Sesion.getSesion().getFechaTrabajo())`. |

---

## D11. Tests

- **`EmpresasTest`, rehecho** con la API nueva y los mismos casos de hoy: la carpeta se forma bien a partir del nombre; `alta` crea la base sin abrirla; `listado` usa el nombre del catálogo y sale ordenado; dos empresas no comparten datos; `baja` borra la carpeta; la empresa en uso no se puede dar de baja; `alta` no cambia la empresa abierta ni rompe su conexión; la base nueva tiene el esquema completo; `abrir` recuerda el tema de cada empresa. Y dos nuevos: `alta` con nombre vacío falla, y `cerrar` deja la sesión sin empresa.
- **`EmpresaDisponibleTest`, nuevo**: carpeta vacía falla; sin nombre usa la carpeta; `toString` es el nombre; `equals` por carpeta; se ordena por nombre sin distinguir mayúsculas.
- **`ConfiguracionTest`**: `comprobarEmpresaCompleta` lanza un aviso que nombra lo que falta, y no lanza con la empresa completa.
- Se adaptan `CopiaSeguridadTest`, `CopiaSeguridadDAOTest` y `PreparacionDatosTest` a `Empresas.getEmpresas()`, `Sesion.getSesion()`, `getCarpeta()` y `CargarDemo.CARPETA`.

---

## Decisiones

**1. La comprobación de datos entra ya, aunque el plan la dejaba para el módulo de facturas.** Quitar el bloqueo sin ella dejaría guardar facturas y exportar PDF sin NIF ni dirección de la empresa durante varios módulos. Son cinco líneas de pantalla que se irán con cada pantalla cuando se rehaga.

**2. `EmpresaDisponible` y no reutilizar `Empresa`.** `Empresa` son los datos fiscales que se guardan dentro de la base de cada empresa; `EmpresaDisponible` es una entrada de la lista del arranque, que existe antes de abrir ninguna base. Son dos cosas distintas y mezclarlas obligaría a abrir cada base solo para enseñar el desplegable.

**3. «Cambiar de empresa» cierra la ventana principal y abre la de arranque**, en lugar de cambiar de empresa dentro de Configuración. Así elegir empresa se hace en un solo sitio y con las mismas reglas (ejercicio y fecha de trabajo incluidos).

**4. «Carpeta» en vez de «slug».** Es exactamente lo que es: el nombre de la carpeta de datos.

## Riesgos y renuncias

- **Eliminar una empresa borra sus datos de verdad.** Hoy también, desde Configuración; lo que cambia es el sitio. El aviso recuerda hacer antes una copia de seguridad.
- **En Windows, una carpeta con la base abierta no se puede borrar.** Por eso `volverAlArranque` cierra la empresa antes de abrir el arranque, y `borrarCarpeta` ahora avisa si no puede borrar en lugar de callarse.
- **La comprobación de datos vive en las pantallas** hasta los módulos 5 a 9 (decisión 1).
- **Se pierde el atajo «crear empresa y cambiar a ella» de Configuración.** Ahora se hace en dos pasos: «Cambiar de empresa» y, en el arranque, «Nueva…» y «Entrar».
