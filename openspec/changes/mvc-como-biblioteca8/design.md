## Context

Estado a 16/09/2026 tras `crear-tablas-sin-versiones`. Números de línea orientativos.

**Arranque hoy:**
1. `Launcher.main` → `Application.launch(Main.class)`.
2. `Main.start(stage)`: `PreparacionDatos.crearCarpeta()` (error → aviso y `Platform.exit()`), `InstanciaUnica.adquirir()` (ocupada o error → aviso y salir), `Platform.setImplicitExit(false)`, `configurarVentana()` (título, icono, `setOnCloseRequest` → `cerrarAplicacion`), `PreparacionDatos.cargarDemoSiNoHayEmpresas()` (error → aviso y **continúa**), `mostrarArranque()`, `stage.show()`, `Platform.runLater(() -> arranque.mostrarAvisoInicial(demo))`.
3. `mostrarArranque()`: `new Navegador(stage, modelo)` con `modelo == null`, `mostrar(Arranque.fxml)`, `arranque.setOnEntrar(e -> entrarEnMenu())`.
4. `ArranqueController.entrar()`: `Empresas.conectar(slug, fecha)` y `onEntrar.accept(elegida)`.
5. `Main.entrarEnMenu()`: `modelo = new Modelo()`, `stage.hide()`, `nav = new Navegador(stage, modelo)`, `nav.setOnVistaCambio(v -> this.actual = v)`, `nav.mostrarInicio()`, `stage.show()`.
6. `Main.cerrarAplicacion()`: `actual.puedeCerrar()`, `Dialogos.confirmar("Salir", "¿Seguro que deseas salir de la aplicación?")`, `actual.alCerrar()`, `InstanciaUnica.liberar()`, `Platform.exit()`.

**`Navegador.mostrar(fxml)`** (genérico `<T extends Vista> T`): `puedeCerrar()` de la vista actual → `FXMLLoader.load` → `new Scene` → `GestorTemas.aplicar(scene, modelo)` → `stage.setScene` → `ConfiguracionVentana.para(fxml).ifPresent(...)` (tamaño y título `Ventanas.PREFIJO + titulo`) → `Ventanas.aplicarIcono` → `setModelo`, `setNavegador`, `vistaActual`, `onVistaCambio`, `alIniciar()` → `root.applyCss()`, `Botones.igualarGrupos(root)`, `Microinteracciones.escalaSuave` en `.primary-button, .menu-item` → devuelve la vista. `mostrarInicio()`: Menú si `modelo.getConfiguracion().empresaCompleta()`, si no Configuración.

**Interfaz `vista.Vista`**: `default` `setModelo`, `setNavegador`, `alIniciar`, `puedeCerrar` (true), `alCerrar`. La implementan 8 controladores y `VistaPrueba` (test).

**Uso en las pantallas** (`vista/controlador`):

| Pantalla | `modelo.` | `nav.` | Atajos / foco en `alIniciar` | Barra |
|---|---|---|---|---|
| `ArranqueController` | 0 | 0 | — | — |
| `MenuPrincipalController` | 4 | 10 | Ctrl+N, Ctrl+F, `quitarFocoInicial()` | — |
| `EditorController` | 51 | 12 | `atajos()`: Ctrl+S, Ctrl+P, Ctrl+N, Esc | `"editor"` |
| `HistoricoController` | 21 | 6 | Ctrl+F (~166) | `"historico"` |
| `ClientesController` | 10 | 3 | Esc (~108) | `"clientes"` |
| `VersionesController` | 4 | 3 | Esc (~83) | `"versiones"` |
| `ConfiguracionController` | 35 | 6 | — | `"configuracion"`, `bloquearSalvoSalir` si faltan datos |
| `CopiaSeguridadController` | 12 | 6 | — | `"copiaSeguridad"` |
| `GenerarFacturasMensualesController` | 16 | 3 | — | — (ventana modal propia, `abrir(Navegador)`, `setModelo`, `setStage`, `initialize()` + `alIniciar()`) |

Pantallas que abren otra y le pasan un dato: `HistoricoController.abrirVersion` y `VersionesController.abrirVersion` → `EditorController.cargarVersion(id)`; `EditorController.verVersiones` → `VersionesController.cargarFactura(id)`.

**Barra de navegación** (`vista/utilidades/BarraNavegacion`): `crear(Navegador, String actual)` devuelve un `HBox(34)` con clase `nav-bar` y 7 botones `nav-button` (texto, tooltip, icono `SVGPath` con escala en un `StackPane` de 26x26, clase `activo` en el botón de la pantalla actual); «Salir» dispara `WINDOW_CLOSE_REQUEST`. `bloquearSalvoSalir(HBox)` desactiva todos menos «Salir». Las 6 pantallas tienen en su FXML `<HBox fx:id="barraNavegacion" styleClass="nav-bar"/>` y añaden dentro la barra creada: hay **dos** `HBox` con `nav-bar` anidados. `base.css`: `.nav-bar { -fx-alignment: CENTER; -fx-spacing: 22px; -fx-padding: 4px 0 4px 0; }` (el espaciado del CSS gana al 34 del constructor); cada tema le da color de fondo.

**Modelo**: su constructor abre la conexión (`Conexion.establecerConexion()`); los DAO piden la conexión en cada método.

**Tests que usan `Navegador`**: `CargaPantallasTest`, `VentanaTransicionTest`, `NavegacionCambiosSinGuardarTest` (con `VistaPrueba` y `VistaPrueba.fxml` en `src/test/resources`) y en `vista/controlador`: `ClientesValidacionNifTest`, `ConfiguracionLayoutTest`, `CopiaSeguridadLayoutTest`, `EditorBarraAccionesTest`, `EditorFlujoTecladoTest`, `EditorIvaInactivoTest`, `EditorTamanoMinimoTest`, `EditorTotalesDescuentoTest`, `EditorValidacionNifTest`, `MenuPrincipalLayoutTest`, más `BotonesTest` en `vista/utilidades`. Todos crean `new Stage()` + `new Navegador(stage, modelo)`.

**Decisiones del usuario (16/09/2026)**: estructura de Biblioteca8; pantalla actual pedida a `Vista`; `Navegador` absorbido por `Vista.mostrar`; atajos y foco en `alMostrar()`; arranque en ventana propia; barra en FXML con controlador inyectado; comprobaciones de arranque en `start`; abrir el Editor con cast, como `fxmlLoader.getController()` en Biblioteca8; `Empresas`/`Sesion`/`PreferenciasGlobales`/`Calculos` en un change posterior.

## Goals / Non-Goals

**Goals:** misma estructura que Biblioteca8 (`AppCaboFactu`, `Controlador`, `Vista` singleton, `LanzadorVentanaPrincipal`); pantallas sin campos `modelo`/`nav` ni métodos inyectados; sin `Consumer` de avisos; barra en FXML; arranque en ventana propia; comportamiento visible igual.

**Non-Goals:**
- `Empresas`, `Sesion`, `PreferenciasGlobales` y `Calculos` siguen `static` (change `negocio-dentro-del-modelo`).
- Quitar la pantalla «Versiones» (apuntado para el futuro).
- Quitar ternarios, streams, clases anónimas, `Task`, `runLater` o `::` que ya existen fuera del código nuevo o de las líneas que se reescriben por completo (changes `sin-clases-anonimas-ni-hilos` y `java-clasico`).
- Cambiar textos, tamaños, estilos o atajos.

## Decisions

### D1. `AppCaboFactu`

`src/main/java/cabofactu/AppCaboFactu.java`:

```java
package cabofactu;

import cabofactu.controlador.Controlador;
import cabofactu.modelo.Modelo;
import cabofactu.vista.Vista;

import java.util.Locale;

/**
 * Punto de entrada de CaboFactu. Creamos el modelo, la vista y el controlador,
 * igual que en el patrón MVC, y arrancamos la aplicación.
 *
 * @author jribanezgarcia
 */
public class AppCaboFactu {

    public static void main(String[] args) {
        Locale.setDefault(new Locale("es", "ES"));
        Modelo modelo = new Modelo();
        Vista vista = Vista.getInstancia();
        Controlador controlador = new Controlador(modelo, vista);
        controlador.comenzar();
    }
}
```

- `pom.xml`: `<mainClass>cabofactu.Main</mainClass>` → `<mainClass>cabofactu.AppCaboFactu</mainClass>`.
- Como `AppCaboFactu` no hereda de `Application`, ya no hace falta `Launcher`. Se borran `Launcher.java` y `Main.java`.

### D2. `Controlador`

`src/main/java/cabofactu/controlador/Controlador.java` (paquete nuevo, como en Biblioteca8):

```java
/**
 * Une el modelo y la vista. Arranca la aplicación, prepara la carpeta de datos
 * y la cierra al terminar.
 *
 * Cómo funciona: las pantallas no repiten aquí los métodos del negocio, sino que
 * piden el modelo con getModelo() y llaman directamente a lo que necesitan, por
 * ejemplo Vista.getInstancia().getControlador().getModelo().getFacturas().
 */
public class Controlador {

    private Modelo modelo;
    private Vista vista;

    public Controlador(Modelo modelo, Vista vista) {
        if (modelo == null) {
            throw new IllegalArgumentException("ERROR: El modelo no puede ser nulo.");
        }
        if (vista == null) {
            throw new IllegalArgumentException("ERROR: La vista no puede ser nula.");
        }
        this.modelo = modelo;
        this.vista = vista;
        this.vista.setControlador(this);
    }

    /** Arrancamos la vista y, cuando se cierra la última ventana, terminamos. */
    public void comenzar() {
        vista.comenzar();
        terminar();
    }

    /** Soltamos el bloqueo de instancia única y cerramos la base de datos. */
    public void terminar() {
        InstanciaUnica.liberar();
        Conexion.cerrarConexion();
    }

    /**
     * Creamos la carpeta de datos y comprobamos que no haya otra aplicación
     * abierta. Si algo falla, lanzamos la excepción con el mensaje para el usuario.
     */
    public void prepararDatos() throws Exception {
        try {
            PreparacionDatos.crearCarpeta();
        } catch (IOException e) {
            throw new Exception("No se pudo preparar la carpeta de datos:\n" + e.getMessage());
        }
        boolean adquirido;
        try {
            adquirido = InstanciaUnica.adquirir();
        } catch (IOException e) {
            throw new Exception("No se pudo comprobar si la aplicación ya está abierta:\n" + e.getMessage());
        }
        if (!adquirido) {
            throw new Exception("La aplicación ya está en ejecución.\nSolo puede abrirse una instancia.");
        }
    }

    /** Cargamos la empresa de demostración si no hay ninguna; devolvemos true si se ha cargado. */
    public boolean cargarDemostracion() throws Exception {
        return PreparacionDatos.cargarDemoSiNoHayEmpresas();
    }

    public Modelo getModelo() {
        return modelo;
    }
}
```

Los mensajes son los de `Main` hoy. La demostración va en un método aparte porque su fallo **no** detiene la aplicación (igual que hoy).

### D3. `Pantalla`

La interfaz `vista/Vista.java` se renombra a `vista/Pantalla.java` (con `git mv`) y queda:

```java
/**
 * Lo que la Vista puede pedir a cada pantalla.
 *
 * Cómo funciona: los métodos default ya traen un comportamiento por defecto,
 * así que cada pantalla solo escribe los que necesita.
 */
public interface Pantalla {

    /** Se llama cuando la pantalla ya está dentro de la ventana (atajos de teclado, foco). */
    default void alMostrar() {
    }

    /** Devolvemos false si hay cambios sin guardar y el usuario decide quedarse. */
    default boolean puedeCerrar() {
        return true;
    }

    /** Se llama al cerrar la aplicación. */
    default void alCerrar() {
    }
}
```

### D4. `Vista`

`src/main/java/cabofactu/vista/Vista.java` (clase nueva, mismo nombre que en Biblioteca8):

```java
/**
 * Vista de la aplicación. Es un singleton: solo existe un objeto, que se pide
 * con Vista.getInstancia() desde cualquier pantalla.
 *
 * Guarda el controlador, la ventana en la que se muestran las pantallas y la
 * pantalla actual, y cambia de pantalla con mostrar(fxml).
 */
public class Vista {

    private static final String RUTA_MENU = "/cabofactu/vista/recursos/MenuPrincipal.fxml";
    private static final String RUTA_CONFIGURACION = "/cabofactu/vista/recursos/Configuracion.fxml";

    private static Vista instancia;
    private Controlador controlador;
    private Stage ventana;
    private Pantalla pantallaActual;

    private Vista() {
    }

    public static Vista getInstancia() {
        if (instancia == null) {
            instancia = new Vista();
        }
        return instancia;
    }

    public void setControlador(Controlador controlador) {
        if (controlador == null) {
            throw new IllegalArgumentException("ERROR: El controlador no puede ser nulo.");
        }
        this.controlador = controlador;
    }

    public Controlador getControlador() {
        return controlador;
    }

    public Stage getVentana() {
        return ventana;
    }

    /** Cambiamos la ventana donde se muestran las pantallas; todavía no tiene ninguna. */
    public void setVentana(Stage ventana) {
        this.ventana = ventana;
        this.pantallaActual = null;
    }

    public Pantalla getPantallaActual() {
        return pantallaActual;
    }

    /** Arrancamos JavaFX; el método termina cuando se cierra la última ventana. */
    public void comenzar() {
        LanzadorVentanaPrincipal.comenzar();
    }

    /** Menú principal si la empresa tiene sus datos completos; si no, Configuración. */
    public void mostrarInicio() {
        if (controlador.getModelo().getConfiguracion().empresaCompleta()) {
            mostrar(RUTA_MENU);
        } else {
            mostrar(RUTA_CONFIGURACION);
        }
    }

    /** Ver D5. */
    public Pantalla mostrar(String fxml) { ... }

    /** Ver D6. */
    public void abrirVentanaPrincipal() { ... }

    /** Pedimos cerrar la ventana principal, igual que al pulsar la X. */
    public void salir() {
        ventana.fireEvent(new WindowEvent(ventana, WindowEvent.WINDOW_CLOSE_REQUEST));
    }
}
```

### D5. `Vista.mostrar(fxml)`

Hace lo mismo que `Navegador.mostrar`, en el mismo orden, con estas diferencias: sin genérico, sin `setModelo`/`setNavegador`/`onVistaCambio`, `alMostrar()` en lugar de `alIniciar()` **después** de poner la escena, sin `Optional` y sin streams.

```java
/**
 * Cargamos la pantalla del FXML y la ponemos en la ventana. Si la pantalla
 * actual tiene cambios sin guardar y el usuario decide quedarse, no cambiamos
 * y devolvemos null. Si no, devolvemos el controlador de la pantalla nueva,
 * por si hay que pasarle algún dato.
 *
 * Cómo funciona: al hacer loader.load(), JavaFX crea el controlador y llama a su
 * initialize(), donde la pantalla carga sus datos. En ese momento la pantalla
 * todavía no está en la ventana, por eso los atajos de teclado se registran
 * después, en alMostrar().
 */
public Pantalla mostrar(String fxml) {
    if (pantallaActual != null && !pantallaActual.puedeCerrar()) {
        return null;
    }
    try {
        FXMLLoader loader = new FXMLLoader(Vista.class.getResource(fxml));
        Parent raiz = loader.load();
        Scene escena = new Scene(raiz);
        GestorTemas.aplicar(escena, controlador.getModelo());
        ventana.setScene(escena);
        ConfiguracionVentana configuracion = ConfiguracionVentana.para(fxml);
        if (configuracion != null) {
            configuracion.aplicar(ventana);
            ventana.setTitle(Ventanas.PREFIJO + configuracion.titulo());
        }
        Ventanas.aplicarIcono(ventana);
        Pantalla pantalla = loader.getController();
        pantallaActual = pantalla;
        if (pantalla != null) {
            pantalla.alMostrar();
        }
        raiz.applyCss();
        Botones.igualarGrupos(raiz);
        for (Node nodo : raiz.lookupAll(".primary-button, .menu-item")) {
            Microinteracciones.escalaSuave(nodo);
        }
        return pantalla;
    } catch (IOException e) {
        throw new RuntimeException("No se pudo cargar la pantalla " + fxml, e);
    }
}
```

### D6. Ventana de arranque y ventana principal

**`LanzadorVentanaPrincipal`** (`vista/LanzadorVentanaPrincipal.java`), como en Biblioteca8:

```java
/**
 * Arranca JavaFX y abre la ventana de arranque (empresa y fecha de trabajo).
 * Aquí comprobamos la carpeta de datos y la instancia única, porque hasta que
 * JavaFX no ha arrancado no se pueden mostrar avisos.
 */
public class LanzadorVentanaPrincipal extends Application {

    public static void comenzar() {
        launch(LanzadorVentanaPrincipal.class);
    }

    @Override
    public void start(Stage stage) {
        Controlador controlador = Vista.getInstancia().getControlador();
        try {
            controlador.prepararDatos();
        } catch (Exception e) {
            Dialogos.error("Facturación", e.getMessage());
            Platform.exit();
            return;
        }
        boolean demoCargada = false;
        try {
            demoCargada = controlador.cargarDemostracion();
        } catch (Exception e) {
            Dialogos.error("Facturación", "No se pudo cargar la empresa de demostración:\n" + e.getMessage());
        }
        Vista.getInstancia().setVentana(stage);
        ArranqueController arranque = (ArranqueController) Vista.getInstancia().mostrar(RUTA_ARRANQUE);
        boolean demo = demoCargada;
        stage.setOnShown(e -> arranque.mostrarAvisoInicial(demo));
        stage.show();
    }
}
```

- `RUTA_ARRANQUE = "/cabofactu/vista/recursos/Arranque.fxml"` como constante privada.
- La ventana de arranque **no** lleva `setOnCloseRequest`: cerrarla sin entrar cierra la aplicación (era lo que pasaba, porque `cerrarAplicacion` no preguntaba nada sin pantalla actual).
- Sin `Platform.setImplicitExit(false)`: JavaFX termina solo cuando no queda ninguna ventana abierta, `launch` vuelve y `Controlador.comenzar()` llama a `terminar()`.

**`Vista.abrirVentanaPrincipal()`**:

```java
/**
 * Abrimos la ventana principal (1024x768) con la primera pantalla. Al pulsar la X
 * o Salir preguntamos antes de cerrar.
 */
public void abrirVentanaPrincipal() {
    Stage principal = new Stage();
    principal.setOnCloseRequest(e -> pedirCierre(e));
    setVentana(principal);
    mostrarInicio();
    principal.show();
}

/** Si el usuario no quiere salir, anulamos el cierre de la ventana. */
private void pedirCierre(WindowEvent evento) {
    if (!puedeSalir()) {
        evento.consume();
    }
}

/** Mismas preguntas que antes de salir: cambios sin guardar y confirmación. */
private boolean puedeSalir() {
    if (pantallaActual != null && !pantallaActual.puedeCerrar()) {
        return false;
    }
    if (!Dialogos.confirmar("Salir", "¿Seguro que deseas salir de la aplicación?")) {
        return false;
    }
    if (pantallaActual != null) {
        pantallaActual.alCerrar();
    }
    return true;
}
```

Si el evento no se consume, JavaFX cierra la ventana; al no quedar ninguna, termina la aplicación.

**`ArranqueController.entrar()`**: sustituir

```java
Empresas.conectar(elegida.slug(), fecha);
if (onEntrar != null) {
    onEntrar.accept(elegida);
}
```

por

```java
Empresas.conectar(elegida.slug(), fecha);
Stage ventanaArranque = (Stage) btnEntrar.getScene().getWindow();
Vista.getInstancia().abrirVentanaPrincipal();
ventanaArranque.close();
```

Y quitar el campo `onEntrar`, `setOnEntrar` y el import de `Consumer`. Abrimos la principal **antes** de cerrar la de arranque para que nunca quede la aplicación sin ventanas.

### D7. `Modelo`

- Quitar del constructor `Modelo(Clock)` el bloque `try { Conexion.establecerConexion(); } catch (SQLException e) { throw new DatosException(e); }` y sus imports (`Conexion`, `DatosException`, `SQLException`).
- Javadoc de clase: «Guarda todo el negocio de la aplicación. Se crea una sola vez al arrancar, antes de elegir empresa: los DAO piden la conexión en cada método y usan la empresa activa.».

### D8. `ConfiguracionVentana.para`

```java
/** Devolvemos la configuración de la pantalla, o null si no tiene. */
public static ConfiguracionVentana para(String fxml) {
    for (ConfiguracionVentana v : values()) {
        if (v.fxml.equals(fxml)) {
            return v;
        }
    }
    return null;
}
```

Quitar los imports de `Arrays` y `Optional`.

### D9. Pantallas

En los 8 controladores que implementaban `Vista` (Arranque, MenuPrincipal, Editor, Historico, Clientes, Versiones, Configuracion, CopiaSeguridad):

1. `implements Vista` → `implements Pantalla, Initializable` (imports `cabofactu.vista.Pantalla`, `javafx.fxml.Initializable`, `java.net.URL`, `java.util.ResourceBundle`; quitar `cabofactu.vista.Navegador` y, donde ya no se use, `cabofactu.modelo.Modelo`).
2. Borrar los campos `private Modelo modelo;` y `private Navegador nav;` y los métodos `setModelo` y `setNavegador`.
3. `public void alIniciar()` → `public void initialize(URL url, ResourceBundle rb)` con `@Override`, **sin** las líneas de atajos, foco ni barra (ver puntos 5-7).
4. Sustituir en todo el fichero:
   - `modelo.` → `Vista.getInstancia().getControlador().getModelo().`
   - `nav.mostrar(` → `Vista.getInstancia().mostrar(`
   - `nav.mostrarInicio()` → `Vista.getInstancia().mostrarInicio()`
   - `nav.stage()` → `Vista.getInstancia().getVentana()`
   - Donde se recibe la pantalla abierta: `EditorController editor = (EditorController) Vista.getInstancia().mostrar(...)` y `VersionesController vc = (VersionesController) Vista.getInstancia().mostrar(...)`.
   - Cuidado con variables locales o parámetros que se llamen `modelo` y no sean el campo.
5. **Atajos y foco a `alMostrar()`** (`@Override public void alMostrar()`), escritos con lambda en lugar de `::`:
   - `MenuPrincipalController`: el contenido de `atajos()` y la llamada a `quitarFocoInicial()` (este mantiene su `Platform.runLater`; se decide en `java-clasico`).
   - `EditorController`: la llamada a `atajos()`; dentro de `atajos()`, `this::guardar` → `() -> guardar()` y así con `exportarPdf`, `nuevaFactura` y `volver`.
   - `HistoricoController`, `ClientesController`, `VersionesController`: su `getAccelerators().put(...)`, con `() -> buscar()` / `() -> volver()`.
6. **`salir()`** de `MenuPrincipalController`: `Vista.getInstancia().salir();`.
7. **Barra** (Editor, Historico, Clientes, Versiones, Configuracion, CopiaSeguridad): quitar `@FXML private HBox barraNavegacion;` y la línea `BarraNavegacion.crear(...)`; añadir

   ```java
   // JavaFX mete aquí el controlador del <fx:include fx:id="barra">: el nombre es el fx:id + "Controller"
   @FXML
   private BarraNavegacionController barraController;
   ```

   y, al principio de `initialize`, `barraController.marcarActivo("editor");` (con el mismo texto que hoy se pasa a `crear`). En `ConfiguracionController`, quitar el campo `barraSuperior` y cambiar `BarraNavegacion.bloquearSalvoSalir(barraSuperior)` por `barraController.bloquearSalvoSalir()`.
8. `EditorController`, bloque `DIAGNOSTICO_FOCO` (~735) y `editarCeldaSegura` (~933): solo la sustitución del punto 4.
9. `ConfiguracionController.cambiarEmpresa` y `CopiaSeguridadController` (restaurar como empresa nueva): solo el punto 4.

**`GenerarFacturasMensualesController`** (no es `Pantalla`, es una ventana modal propia):
- Borrar el campo `modelo` y `setModelo`; `modelo.` → llamada completa.
- `public static void abrir(Navegador nav)` → `public static void abrir()`: `dialog.initOwner(Vista.getInstancia().getVentana())`, `GestorTemas.aplicar(scene, Vista.getInstancia().getControlador().getModelo())`, y `ConfiguracionVentana configuracion = ConfiguracionVentana.para(RUTA); if (configuracion != null) { configuracion.aplicar(dialog); }`. Quitar `c.alIniciar()`.
- `initialize()` pasa a `implements Initializable` con `initialize(URL, ResourceBundle)` y, tras `configurarTabla()`, todo lo que hacía `alIniciar()`. Se borra `alIniciar()`.
- Llamadas: `GenerarFacturasMensualesController.abrir(nav)` → `GenerarFacturasMensualesController.abrir()` en Menú e Histórico.

### D10. Barra de navegación en FXML

**`src/main/resources/cabofactu/vista/recursos/BarraNavegacion.fxml`**: raíz `<HBox styleClass="nav-bar" fx:controller="cabofactu.vista.controlador.BarraNavegacionController">` con los 7 botones, en el mismo orden, con los mismos textos, tooltips, rutas SVG y escalas que las constantes de `BarraNavegacion.java`:

```xml
<Button fx:id="btnInicio" text="Inicio" styleClass="nav-button" onAction="#irInicio">
    <tooltip><Tooltip text="Menú principal"/></tooltip>
    <graphic>
        <StackPane minWidth="26" minHeight="26" prefWidth="26" prefHeight="26" maxWidth="26" maxHeight="26">
            <SVGPath styleClass="nav-icon" scaleX="1.25" scaleY="1.25" content="M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z"/>
        </StackPane>
    </graphic>
</Button>
```

| fx:id | Texto | Tooltip | Escala | onAction | Destino |
|---|---|---|---|---|---|
| `btnInicio` | Inicio | Menú principal | 1.25 | `#irInicio` | `MenuPrincipal.fxml` |
| `btnNueva` | Nueva | Nueva factura | 1.25 | `#irNueva` | `Editor.fxml` |
| `btnHistorico` | Histórico | Histórico | 1.15 | `#irHistorico` | `Historico.fxml` |
| `btnClientes` | Clientes | Clientes | 1.4 | `#irClientes` | `Clientes.fxml` |
| `btnConfiguracion` | Configuración | Configuración | 1.25 | `#irConfiguracion` | `Configuracion.fxml` |
| `btnCopias` | Copias | Copia de seguridad | 1.25 | `#irCopias` | `CopiaSeguridad.fxml` |
| `btnSalir` | Salir | Salir | 1.25 | `#salir` | `Vista.getInstancia().salir()` |

**`vista/controlador/BarraNavegacionController.java`**: los 7 `@FXML Button`, un método `@FXML private void irX()` por botón con `Vista.getInstancia().mostrar(ruta)`, y:

```java
/** Marcamos con la raya de color el botón de la pantalla en la que estamos. */
public void marcarActivo(String pantalla) {
    switch (pantalla) {
        case "editor" -> btnNueva.getStyleClass().add("activo");
        case "historico" -> btnHistorico.getStyleClass().add("activo");
        case "clientes" -> btnClientes.getStyleClass().add("activo");
        case "configuracion" -> btnConfiguracion.getStyleClass().add("activo");
        case "copiaSeguridad" -> btnCopias.getStyleClass().add("activo");
        default -> {
        }
    }
}

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

(Hoy «menu» y «versiones» no marcan ningún botón; se mantiene.)

**Los 6 FXML con barra**: `<HBox fx:id="barraNavegacion" styleClass="nav-bar"/>` → `<fx:include fx:id="barra" source="BarraNavegacion.fxml"/>`.

**`temas/base.css`**: como ya no hay dos `nav-bar` anidados, el hueco vertical se suma en uno: `.nav-bar { ... -fx-padding: 8px 0 8px 0; }` (antes 4 + 4). Así la barra mide lo mismo.

Se borra `vista/utilidades/BarraNavegacion.java`.

### D11. `AGENTS.md`

En «Arquitectura (MVC como Biblioteca8)»:
- Tras la línea de `Controlador`, añadir: «`LanzadorVentanaPrincipal` abre la ventana de arranque; al entrar en una empresa, `Vista.abrirVentanaPrincipal()` abre la ventana principal.».
- La línea de pantallas pasa a: «Las pantallas (`*Controller`) implementan `Pantalla` e `Initializable`, cargan sus datos en `initialize()` y dejan para `alMostrar()` lo que necesita la ventana (atajos de teclado, foco). Escriben la llamada completa, sin guardarla en un campo:».
- En «Paquetes», añadir `cabofactu.controlador` (el `Controlador`) tras `cabofactu.vista.recursos`.

### D12. Tests

- **`PruebasJavaFx`**: nuevo método

  ```java
  /** Preparamos la Vista como al arrancar: con su controlador, el modelo y una ventana. */
  public static void prepararVista(Modelo modelo, Stage ventana) {
      new Controlador(modelo, Vista.getInstancia());
      Vista.getInstancia().setVentana(ventana);
  }
  ```

  Se llama dentro del hilo de JavaFX, donde hoy se hace `new Navegador(stage, modelo)`.
- **Todos los tests que usan `Navegador`**: `new Navegador(stage, modelo)` → `PruebasJavaFx.prepararVista(modelo, stage)`; `nav.mostrar(` → `Vista.getInstancia().mostrar(`; `nav.stage()` → `Vista.getInstancia().getVentana()`; `Vista v = nav.mostrar(...)` → `Pantalla p = Vista.getInstancia().mostrar(...)`; donde se asigna a un controlador concreto, cast (`(EditorController)`, `(ClientesController)`…). Quitar los campos `static Navegador nav`. Actualizar el comentario de `EditorValidacionNifTest` que nombra `Navegador.mostrar()`.
- **`VistaPrueba`**: `implements Vista` → `implements Pantalla`. Javadoc de `NavegacionCambiosSinGuardarTest`: `Vista.mostrar()`.
- **`CargaPantallasTest.cargarGenerarFacturasMensuales`**: tras `prepararVista`, solo `loader.load()` (el `initialize` ya carga todo); quitar `setModelo` y `alIniciar`, mantener `setStage(new Stage())`. Javadoc de clase: «a través de Vista (parseo FXML, inyección @FXML e initialize)». `cargarArranque` sigue con `Vista.getInstancia().mostrar(...)`.
- **`VentanaTransicionTest`**:
  - `menuSubeHasta1024AlPasarDeArranque` → `arranqueYVentanaPrincipalConSuTamano`: preparar la vista con una ventana, `mostrar(Arranque.fxml)` y `show()`: 760x520 y no redimensionable. Después `Vista.getInstancia().abrirVentanaPrincipal()`: la nueva `Vista.getInstancia().getVentana()` es **otro** `Stage` (`assertNotSame`), mide 1024x768, mínimo 1024x768 y es redimensionable. Ocultar las dos ventanas al final.
  - Los otros dos tests: solo la sustitución de `Navegador`.
  - Javadoc de clase: «Comprueba que el arranque tiene su propia ventana de 760x520 y que la ventana principal se abre a 1024x768 y conserva el tamaño del usuario al navegar.».
- Sin `var`, ternarios, streams ni `::` en las líneas nuevas o reescritas.

### D13. Documentación

En `docs/tecnico.md`:
- Diagrama de capas: `A["🚀 Launcher → Main<br/><small>PreparacionDatos · InstanciaUnica</small>"] --> B` → `A["🚀 AppCaboFactu → Controlador → Vista<br/><small>PreparacionDatos · InstanciaUnica</small>"] --> B`, y la flecha de B a C con el texto `"Vista.getInstancia().getControlador().getModelo().getFacturas()…"`.
- Paquetes: `├── 🚀 Launcher, Main         → punto de entrada JavaFX` → `├── 🚀 AppCaboFactu           → punto de entrada: crea Modelo, Vista y Controlador`; añadir debajo `├── 🎛️ controlador/           → Controlador: arranca y cierra la aplicación`; `vista/` → `Vista (singleton), Pantalla, LanzadorVentanaPrincipal, ConfiguracionVentana y Ventanas`; `vista/controlador/` → `un Controller por FXML (MenuPrincipalController, BarraNavegacionController…)`; `vista/utilidades/` → `Dialogos, GestorTemas y PreviaCabecera`.

En `README.md`, diagrama «Cómo está montado»: `A["🚀 Main<br/><small>arranque</small>"] --> B` → `A["🚀 AppCaboFactu · Controlador · Vista<br/><small>arranque</small>"] --> B`.

## Risks / Trade-offs

- **`fireEvent(WINDOW_CLOSE_REQUEST)` debe cerrar la ventana si nadie consume el evento** (comportamiento estándar de JavaFX) → si en la prueba manual «Salir» pregunta pero no cierra, añadir `ventana.close()` en `salir()` cuando `puedeSalir()` sea true, en lugar de disparar el evento.
- **Salir de la aplicación ya no llama a `Platform.exit()`**: termina al cerrarse la última ventana. Si el diálogo modal de mensuales o un aviso quedara abierto no pasaría, porque son modales sobre la principal.
- **`Vista` es un singleton compartido entre clases de test** → `setVentana` pone `pantallaActual` a `null`, así que un test no hereda la pantalla (ni sus cambios sin guardar) del anterior.
- **La barra mide lo mismo** solo si el CSS suma el hueco (D10) → lo comprueban `EditorTamanoMinimoTest` y la prueba manual.
- **Las líneas con la llamada completa quedan largas** → aceptado por el usuario: prefiere verla completa a guardarla en un campo.
