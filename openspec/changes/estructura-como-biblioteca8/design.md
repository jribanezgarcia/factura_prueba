## Situación de partida

Estado a 20/09/2026 (commit `7d9fcdf`). Números de línea orientativos.

**Arranque hoy**: `Launcher.main` → `Application.launch(Main.class)` → `Main.start(stage)`: crea la carpeta de datos, coge el bloqueo de instancia única, `Platform.setImplicitExit(false)`, pone título e icono y el `setOnCloseRequest`, carga la demostración, crea un `Navegador` **sin modelo** para mostrar `Arranque.fxml` y le deja un `Consumer` (`setOnEntrar`). Al entrar: `new Modelo()`, `stage.hide()`, otro `Navegador` **con modelo**, `setOnVistaCambio` y `stage.show()`.

**`Navegador.mostrar(fxml)`** (genérico): `puedeCerrar()` → `FXMLLoader.load` → `new Scene` → `GestorTemas.aplicar` → `stage.setScene` → `ConfiguracionVentana.para(fxml).ifPresent(...)` → `Ventanas.aplicarIcono` → `setModelo`, `setNavegador`, `onVistaCambio`, `alIniciar()` → `applyCss`, `Botones.igualarGrupos`, `Microinteracciones.escalaSuave`.

**Las 9 pantallas** implementan la interfaz `vista.Vista` (`setModelo`, `setNavegador`, `alIniciar`, `puedeCerrar`, `alCerrar`) y usan los campos `modelo` y `nav`: 157 usos de `modelo.` y 49 de `nav.`. Los atajos de teclado y el foco inicial del menú viven dentro de `alIniciar` y necesitan la escena ya puesta.

**Barra de navegación**: `BarraNavegacion.crear(nav, "clientes")` construye en Java un `HBox` con 7 botones; los 6 FXML con barra tienen `<HBox fx:id="barraNavegacion" styleClass="nav-bar"/>` y meten dentro el resultado, así que hay **dos** `HBox` con clase `nav-bar` anidados. `base.css`: `.nav-bar { -fx-alignment: CENTER; -fx-spacing: 22px; -fx-padding: 4px 0 4px 0; }`.

**`Dialogos`**: métodos `error`, `info`, `confirmar` (Sí/No), `confirmarCambiosSinGuardar`, `modoGuardarVersion`, más `aplicarTema` y `ponerMensaje`. Delegan en la interfaz `MostradorDialogos`, cuya implementación real es `DialogosReales`, sustituible con `setImpl` desde los tests. En el código hay 114 `Dialogos.error`, 28 `Dialogos.info` y 18 `Dialogos.confirmar`.

**Tests**: 43 ficheros. De pantalla: `ClientesValidacionNifTest`, `EditorValidacionNifTest`, `EditorIvaInactivoTest` (usan `Dialogos.setImpl`), `ConfiguracionLayoutTest`, `CopiaSeguridadLayoutTest`, `EditorBarraAccionesTest`, `EditorFlujoTecladoTest`, `EditorTamanoMinimoTest`, `EditorTotalesDescuentoTest`, `MenuPrincipalLayoutTest`, `NavegacionCambiosSinGuardarTest` (con `VistaPrueba` y `VistaPrueba.fxml`), `VentanaTransicionTest`, `BotonesTest` y `DialogosTextoCompletoTest`. `CargaPantallasTest` abre todos los FXML. `ClaseSeparadorTest` solo lee los FXML como texto.

**Referencia**: `AGENTS.md`, secciones «Arquitectura», «Pantallas» y «Estilo del código»; los apuntes del alumno (`Índice Proyectos JavaFX`, fichas 01 y 03) y `Biblioteca8`.

## Objetivos y lo que queda fuera

**Objetivos:** el esqueleto de Biblioteca8 funcionando (`AppCaboFactu`, `Controlador`, `Modelo`, `Vista` singleton, `LanzadorVentanaPrincipal`); pantallas sin campos inyectados y con `initialize()`; arranque en ventana propia; barra y diálogos como en clase; y la aplicación haciendo exactamente lo mismo que antes.

**Queda fuera** (módulos siguientes):
- El negocio conserva sus DAO, `ValidacionException` y `DatosException`, y las clases de datos siguen sin validar en sus setters.
- Siguen existiendo las versiones de factura y la pantalla «Versiones».
- Las pantallas siguen pidiendo el modelo (ver D3); cada módulo las pasará a `getControlador().altaCliente(...)`.
- `Botones.igualarGrupos` se queda: cada módulo fijará los anchos en su FXML y el último lo borrará.
- Ternarios, streams, `var` y demás que ya existen fuera de las líneas que se reescriben.

## Decisiones

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
 * y arrancamos la aplicación.
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
- Se borran `Launcher.java` y `Main.java`: `AppCaboFactu` no hereda de `Application`, así que ya no hace falta la clase lanzadera.

### D2. `Controlador`

`src/main/java/cabofactu/controlador/Controlador.java`:

```java
/**
 * Une el modelo y la vista: arranca la aplicación, prepara la carpeta de datos
 * y la cierra al terminar.
 */
public class Controlador {

    private Modelo modelo;
    private Vista vista;

    public Controlador(Modelo modelo, Vista vista) {
        if (modelo == null) {
            throw new IllegalArgumentException("El modelo no puede ser nulo.");
        }
        if (vista == null) {
            throw new IllegalArgumentException("La vista no puede ser nula.");
        }
        this.modelo = modelo;
        this.vista = vista;
        this.vista.setControlador(this);
    }

    /** Arrancamos la vista; cuando se cierra la última ventana, terminamos. */
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

Los mensajes son los que hoy muestra `Main`. La demostración va aparte porque su fallo **no** detiene la aplicación.

### D3. `getModelo()` es provisional

Este módulo todavía no crea las operaciones del `Controlador` (`altaCliente`, `anularFactura`…): eso lo hará cada módulo con las suyas. Mientras tanto, las pantallas piden el modelo:

```java
Vista.getInstancia().getControlador().getModelo().getClientes().listar(false);
```

`getModelo()` lleva este comentario, para que se sepa que es un puente temporal:

```java
/**
 * Damos el modelo mientras quedan pantallas por rehacer. Cuando todas llamen
 * a las operaciones del controlador, este método desaparece.
 */
```

### D4. `Pantalla`

La interfaz `vista/Vista.java` se renombra con `git mv` a `vista/Pantalla.java`:

```java
/**
 * Lo que la Vista puede pedirle a cada pantalla.
 *
 * Cómo funciona: los métodos llevan una implementación por defecto, así que
 * cada pantalla solo escribe los que necesita.
 */
public interface Pantalla {

    /** Se llama cuando la pantalla ya está dentro de la ventana: atajos de teclado y foco. */
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

### D5. `Vista`

`src/main/java/cabofactu/vista/Vista.java`, clase nueva:

```java
/**
 * Vista de la aplicación. Es un singleton: solo existe un objeto, que se pide
 * con Vista.getInstancia() desde cualquier pantalla. Guarda el controlador, la
 * ventana en la que se muestran las pantallas y la pantalla actual.
 */
public class Vista {

    private static final String MENU = "MenuPrincipal.fxml";
    private static final String CONFIGURACION = "Configuracion.fxml";

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
            throw new IllegalArgumentException("El controlador no puede ser nulo.");
        }
        this.controlador = controlador;
    }

    public Controlador getControlador() { ... }
    public Stage getVentana() { ... }
    public Pantalla getPantallaActual() { ... }

    /** Cambiamos la ventana donde se muestran las pantallas; todavía no tiene ninguna. */
    public void setVentana(Stage ventana) {
        this.ventana = ventana;
        this.pantallaActual = null;
    }

    /** Arrancamos JavaFX; el método vuelve cuando se cierra la última ventana. */
    public void comenzar() {
        LanzadorVentanaPrincipal.comenzar();
    }

    /** Menú principal si la empresa tiene sus datos completos; si no, Configuración. */
    public void mostrarInicio() {
        if (controlador.getModelo().getConfiguracion().empresaCompleta()) {
            mostrar(MENU);
        } else {
            mostrar(CONFIGURACION);
        }
    }
}
```

### D6. `Vista.mostrar(fxml)`

Hace lo mismo que `Navegador.mostrar`, con tres diferencias: sin genérico (devuelve `Pantalla`), sin `setModelo`/`setNavegador`/`onVistaCambio`, y con `alMostrar()` **después** de poner la escena. Sin `Optional` y sin streams:

```java
/**
 * Cargamos la pantalla del FXML y la ponemos en la ventana. Si la pantalla
 * actual tiene cambios sin guardar y el usuario decide quedarse, no cambiamos
 * y devolvemos null. Si no, devolvemos su controlador, por si hay que pasarle
 * algún dato.
 *
 * Cómo funciona: al hacer loader.load(), JavaFX crea el controlador y llama a
 * su initialize(), donde la pantalla carga sus datos. En ese momento la
 * pantalla todavía no está en la ventana; por eso los atajos de teclado se
 * registran después, en alMostrar().
 */
public Pantalla mostrar(String fxml) {
    if (pantallaActual != null && !pantallaActual.puedeCerrar()) {
        return null;
    }
    try {
        FXMLLoader loader = new FXMLLoader(LocalizadorRecursos.class.getResource(fxml));
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
        return pantalla;
    } catch (IOException e) {
        throw new RuntimeException("No se pudo cargar la pantalla " + fxml, e);
    }
}
```

`ConfiguracionVentana` pasa a guardar el **nombre** del fichero (`"MenuPrincipal.fxml"`) en lugar de la ruta completa, y `para(String fxml)` devuelve `null` si no la encuentra:

```java
/** Devolvemos la configuración de esa pantalla, o null si no tiene. */
public static ConfiguracionVentana para(String fxml) {
    for (ConfiguracionVentana v : values()) {
        if (v.fxml.equals(fxml)) {
            return v;
        }
    }
    return null;
}
```

### D7. Las dos ventanas

**`LocalizadorRecursos`**, en `src/main/java/cabofactu/vista/recursos/LocalizadorRecursos.java`:

```java
/** Marcador para localizar la carpeta de recursos: los FXML se piden con su nombre. */
public interface LocalizadorRecursos {
}
```

**`LanzadorVentanaPrincipal`**, en `vista/`:

```java
/**
 * Arranca JavaFX y abre la ventana de arranque (empresa y fecha de trabajo).
 * Comprobamos aquí la carpeta de datos y la instancia única porque hasta que
 * JavaFX no arranca no se pueden mostrar avisos.
 */
public class LanzadorVentanaPrincipal extends Application {

    private static final String ARRANQUE = "Arranque.fxml";

    public static void comenzar() {
        launch(LanzadorVentanaPrincipal.class);
    }

    @Override
    public void start(Stage stage) {
        Controlador controlador = Vista.getInstancia().getControlador();
        try {
            controlador.prepararDatos();
        } catch (Exception e) {
            Dialogos.mostrarDialogoError("Facturación", e.getMessage());
            Platform.exit();
            return;
        }
        boolean demoCargada = false;
        try {
            demoCargada = controlador.cargarDemostracion();
        } catch (Exception e) {
            Dialogos.mostrarDialogoError("Facturación",
                    "No se pudo cargar la empresa de demostración:\n" + e.getMessage());
        }
        Vista.getInstancia().setVentana(stage);
        ArranqueController arranque = (ArranqueController) Vista.getInstancia().mostrar(ARRANQUE);
        boolean demo = demoCargada;
        stage.setOnShown(e -> arranque.mostrarAvisoInicial(demo));
        stage.show();
    }
}
```

**La ventana principal**, en `Vista`:

```java
/**
 * Abrimos la ventana principal (1024x768) con la primera pantalla. Al pulsar la
 * X o Salir preguntamos antes de cerrar.
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

/** Las mismas preguntas de antes de salir: cambios sin guardar y confirmación. */
private boolean puedeSalir() {
    if (pantallaActual != null && !pantallaActual.puedeCerrar()) {
        return false;
    }
    if (!Dialogos.mostrarDialogoConfirmacion("Salir", "¿Seguro que deseas salir de la aplicación?")) {
        return false;
    }
    if (pantallaActual != null) {
        pantallaActual.alCerrar();
    }
    return true;
}

/** Pedimos cerrar la ventana principal, igual que al pulsar la X. */
public void salir() {
    ventana.fireEvent(new WindowEvent(ventana, WindowEvent.WINDOW_CLOSE_REQUEST));
}
```

En `ArranqueController.entrar()`, donde hoy avisa por el `Consumer`:

```java
Empresas.conectar(elegida.slug(), fecha);
Stage ventanaArranque = (Stage) btnEntrar.getScene().getWindow();
Vista.getInstancia().abrirVentanaPrincipal();
ventanaArranque.close();
```

Se abre la principal **antes** de cerrar la de arranque, para que nunca queden cero ventanas. Desaparecen el campo `onEntrar`, `setOnEntrar`, el import de `Consumer`, `Platform.setImplicitExit(false)` y el `hide()`/`show()`.

### D8. `Dialogos`

Se parte de la clase de Biblioteca8 (`biblioteca/vista/utilidades/Dialogos.java`) y se le añade lo que este proyecto ya tenía:

```java
public class Dialogos {

    private Dialogos() {
    }

    public static void mostrarDialogoError(String titulo, String contenido) { ... }
    public static void mostrarDialogoInformacion(String titulo, String contenido) { ... }
    public static void mostrarDialogoAdvertencia(String titulo, String contenido) { ... }
    public static boolean mostrarDialogoConfirmacion(String titulo, String contenido) { ... }
    public static CambiosSinGuardar mostrarDialogoCambiosSinGuardar() { ... }
    public static ModoGuardarVersion mostrarDialogoModoGuardarVersion() { ... }
    public static void aplicarTema(DialogPane pane) { ... }
}
```

Reglas de cada método, tomadas de la clase de clase y de lo que ya hacía `DialogosReales`:
- `Alert` del tipo correspondiente, `setHeaderText(null)` y `showAndWait()`.
- **El mensaje va en una `Label` propia** (`setWrapText(true)`, `setMinHeight(Region.USE_PREF_SIZE)`, sin ancho fijo), no con `setContentText`: es el arreglo de los mensajes de más de una línea, que se cortaban con «...».
- `aplicarTema(pane)` (clase `dialog-card` y hojas del tema) y el icono de la aplicación en la ventana del aviso, como ahora.
- La confirmación usa los botones por defecto, **Aceptar / Cancelar**, y devuelve `true` si se pulsó Aceptar.
- `mostrarDialogoCambiosSinGuardar` mantiene sus tres botones y devuelve `CambiosSinGuardar`; `mostrarDialogoModoGuardarVersion` mantiene los suyos (desaparecerá con las versiones, en el módulo de facturas).
- **No se copian las sobrecargas con `propietario`** de la clase de clase: cierran la ventana dueña al aceptar (`propietario.close()`), y aquí eso cerraría la ventana principal.

Se borran `MostradorDialogos` y `DialogosReales`. En todo el código: `Dialogos.error(` → `Dialogos.mostrarDialogoError(`, `Dialogos.info(` → `Dialogos.mostrarDialogoInformacion(`, `Dialogos.confirmar(` → `Dialogos.mostrarDialogoConfirmacion(`, `Dialogos.confirmarCambiosSinGuardar()` → `Dialogos.mostrarDialogoCambiosSinGuardar()`, `Dialogos.modoGuardarVersion()` → `Dialogos.mostrarDialogoModoGuardarVersion()`.

### D9. Barra de navegación en FXML

`src/main/resources/cabofactu/vista/recursos/BarraNavegacion.fxml`, con raíz `<HBox styleClass="nav-bar" fx:controller="cabofactu.vista.controlador.BarraNavegacionController">` y los 7 botones, con los mismos textos, tooltips, rutas SVG y escalas que hoy tiene `BarraNavegacion.java`:

| fx:id | Texto | Tooltip | Escala | onAction | Destino |
|---|---|---|---|---|---|
| `btnInicio` | Inicio | Menú principal | 1.25 | `#irInicio` | `MenuPrincipal.fxml` |
| `btnNueva` | Nueva | Nueva factura | 1.25 | `#irNueva` | `Editor.fxml` |
| `btnHistorico` | Histórico | Histórico | 1.15 | `#irHistorico` | `Historico.fxml` |
| `btnClientes` | Clientes | Clientes | 1.4 | `#irClientes` | `Clientes.fxml` |
| `btnConfiguracion` | Configuración | Configuración | 1.25 | `#irConfiguracion` | `Configuracion.fxml` |
| `btnCopias` | Copias | Copia de seguridad | 1.25 | `#irCopias` | `CopiaSeguridad.fxml` |
| `btnSalir` | Salir | Salir | 1.25 | `#salir` | `Vista.getInstancia().salir()` |

Cada botón, con su icono dentro de un `StackPane` de 26x26:

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

`vista/controlador/BarraNavegacionController.java`: los 7 `@FXML Button`, un método por botón (`@FXML void irInicio(ActionEvent event)`) que llama a `Vista.getInstancia().mostrar(...)`, y además:

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
public void bloquearSalvoSalir() { ... }
```

En los 6 FXML con barra (`Clientes`, `Configuracion`, `CopiaSeguridad`, `Editor`, `Historico`, `Versiones`), el `<HBox fx:id="barraNavegacion" styleClass="nav-bar"/>` pasa a `<fx:include fx:id="barra" source="BarraNavegacion.fxml"/>`, y su controlador recibe el de la barra:

```java
// Cómo funciona: JavaFX inyecta aquí el controlador del <fx:include fx:id="barra">;
// el nombre del campo es el fx:id más "Controller".
@FXML
private BarraNavegacionController barraController;
```

En `initialize`, `barraController.marcarActivo("clientes")`. En `ConfiguracionController`, `BarraNavegacion.bloquearSalvoSalir(barraSuperior)` pasa a `barraController.bloquearSalvoSalir()`.

Como ya no hay dos `nav-bar` anidados, en `temas/base.css` el hueco se suma en uno solo: `-fx-padding: 8px 0 8px 0;` (antes 4 + 4). Se borra `vista/utilidades/BarraNavegacion.java`.

### D10. Las pantallas

En los 8 controladores que implementaban `Vista` (Arranque, MenuPrincipal, Editor, Historico, Clientes, Versiones, Configuracion, CopiaSeguridad):

1. `implements Vista` → `implements Pantalla, Initializable`; imports nuevos (`Pantalla`, `Initializable`, `URL`, `ResourceBundle`) y fuera los de `Navegador` y `Modelo`.
2. Borrar los campos `modelo` y `nav` y los métodos `setModelo` y `setNavegador`.
3. `public void alIniciar()` → `@Override public void initialize(URL url, ResourceBundle rb)`, **sin** las líneas de atajos, foco ni barra.
4. Sustituir en todo el fichero:
   - `modelo.` → `Vista.getInstancia().getControlador().getModelo().` (ver D3)
   - `nav.mostrar("/cabofactu/vista/recursos/X.fxml")` → `Vista.getInstancia().mostrar("X.fxml")`
   - `nav.mostrarInicio()` → `Vista.getInstancia().mostrarInicio()`
   - `nav.stage()` → `Vista.getInstancia().getVentana()`
   - Donde se recibía la pantalla abierta, cast: `EditorController editor = (EditorController) Vista.getInstancia().mostrar("Editor.fxml");` y lo mismo con `VersionesController`.
5. **Atajos y foco a `alMostrar()`** (`@Override public void alMostrar()`), con lambda en vez de `::`:
   - `MenuPrincipalController`: el contenido de `atajos()` y la llamada a `quitarFocoInicial()` (que conserva su `Platform.runLater`).
   - `EditorController`: la llamada a `atajos()`; dentro, `this::guardar` → `() -> guardar()` y lo mismo con `exportarPdf`, `nuevaFactura` y `volver`.
   - `HistoricoController`, `ClientesController` y `VersionesController`: su `getAccelerators().put(...)`, con `() -> buscar()` / `() -> volver()`.
6. `MenuPrincipalController.salir()` → `Vista.getInstancia().salir();`
7. La barra, como dice D9.
8. `GenerarFacturasMensualesController` (ventana modal propia, no es `Pantalla`): `abrir(Navegador nav)` → `abrir()`, con `Vista.getInstancia().getVentana()` como propietaria y `Vista.getInstancia().getControlador().getModelo()` para el tema; su `initialize()` pasa a `initialize(URL, ResourceBundle)` y absorbe lo que hacía `alIniciar()`; quien lo abría llama ahora a `GenerarFacturasMensualesController.abrir()`.

### D11. `Modelo` y `Microinteracciones`

- `Modelo`: quitar del constructor `Conexion.establecerConexion()` (y sus imports). Se crea al arrancar, antes de elegir empresa; los DAO piden la conexión en cada método.
- `Microinteracciones` se borra. En `temas/base.css`, el efecto pasa a CSS:
  ```css
  .menu-item:hover, .primary-button:hover {
      -fx-scale-x: 1.02;
      -fx-scale-y: 1.02;
  }
  ```
  El crecimiento deja de ser animado; es el único cambio visible de este change.

### D12. Tests

- **Se borran**: `ClientesValidacionNifTest`, `EditorValidacionNifTest`, `EditorIvaInactivoTest`, `ConfiguracionLayoutTest`, `CopiaSeguridadLayoutTest`, `EditorBarraAccionesTest`, `EditorFlujoTecladoTest`, `EditorTamanoMinimoTest`, `EditorTotalesDescuentoTest`, `MenuPrincipalLayoutTest`, `NavegacionCambiosSinGuardarTest`, `VentanaTransicionTest`, `BotonesTest`, `DialogosTextoCompletoTest`, `VistaPrueba.java` y `src/test/resources/cabofactu/vista/recursos/VistaPrueba.fxml`.
- **Se quedan**: todos los del negocio, los DAO, el PDF y las utilidades; `ClaseSeparadorTest` (solo lee los FXML como texto) y `CargaPantallasTest`.
- **`PruebasJavaFx`**: método nuevo, llamado dentro del hilo de JavaFX allí donde antes se creaba el `Navegador`:
  ```java
  /** Preparamos la Vista como al arrancar: con su controlador, el modelo y una ventana. */
  public static void prepararVista(Modelo modelo, Stage ventana) {
      new Controlador(modelo, Vista.getInstancia());
      Vista.getInstancia().setVentana(ventana);
  }
  ```
- **`CargaPantallasTest`**: usa `prepararVista` y carga cada pantalla con `Vista.getInstancia().mostrar("MenuPrincipal.fxml")`, con el nombre del fichero. Para la ventana modal de mensuales, `loader.load()` y `setStage(new Stage())`, sin `setModelo` ni `alIniciar`.

### D13. `AGENTS.md` y `ESTADO.md`

- `AGENTS.md`: en «Transición», quitar de la lista de lo que no cumple el esqueleto (ya cumple) y dejar el resto.
- `ESTADO.md`: este change en «En curso» al aplicar y en «Hecho» al archivar, como manda el flujo.

## Riesgos y renuncias

- **`fireEvent(WINDOW_CLOSE_REQUEST)` debe cerrar la ventana** si nadie consume el evento (comportamiento estándar de JavaFX). Si en la prueba manual «Salir» pregunta pero no cierra, se añade `ventana.close()` en `salir()` cuando `puedeSalir()` sea `true`.
- **Salir ya no llama a `Platform.exit()`**: la aplicación termina al cerrarse la última ventana. Por eso importa la prueba manual de que el proceso muere del todo.
- **`Vista` es un singleton compartido entre clases de test** → `setVentana` pone la pantalla actual a `null`, así que un test no hereda la pantalla del anterior.
- **Se pierden 14 tests de pantalla**, incluido el que medía que los avisos no se cortaran. El arreglo sigue en el código (la `Label` propia de D8) y su comprobación pasa a las pruebas manuales.
- **Las llamadas quedan largas** (`getControlador().getModelo().getFacturas()…`) hasta que cada módulo ponga su operación en el `Controlador`.
