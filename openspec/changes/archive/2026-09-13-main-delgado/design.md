## Context

`Main.java` tras `empresa-inicial-obligatoria` (commit `bfbd1e4`):

| Bloque | Miembros | Líneas aprox. |
|---|---|---|
| Entrada | `main` (fija `Locale es_ES` y lanza) | 40-43 |
| Orden del arranque | `start` | 45-72 |
| Carpeta de datos | `prepararDatos` (crea `Database.baseDataDir()`; si falla, `Dialogos.error` + `Platform.exit`) | ~75-85 |
| Demostración | `cmbEmpresaVacia` + bloque en `start` que llama a `CargarDemo.cargar()` y fija `ULTIMA_EMPRESA` | ~58-66, ~87 |
| Ventana | `configurarVentana` (título, icono, `onCloseRequest`) | ~90-98 |
| Flujo | `mostrarArranque`, `entrarEnMenu`, `cerrarAplicacion`; campos `servicios`, `nav`, `actual`, `stage` | ~100-140 |
| Instancia única | `adquirirLock`, `liberarLock`; campos `lockChannel`, `lock` | ~140-165 |
| Ventana (guardar) | `guardarPreferenciasVentana` | ~167-172 |

Hechos comprobados:

- `adquirirLock` abre `Database.lockPathGlobal()` con `FileChannel.open(CREATE, WRITE)` y `tryLock()`. Cualquier `IOException` devuelve `false`, y `start` muestra «La aplicación ya está en ejecución».
- `tryLock()` lanza `OverlappingFileLockException` (no comprobada) si el bloqueo ya lo tiene la misma JVM. Hoy no se captura.
- `PreferenciasGlobales.VENTANA_X/Y/W/H` solo se escriben en `Main.guardarPreferenciasVentana` y se usan en `PreferenciasGlobalesTest.lecturaEscrituraEsIdempotente`. Nadie los lee en `src/main`.
- La spec «Configuración» dice: «La aplicación SHALL recordar preferencias de trabajo: última serie utilizada, tamaño/posición de ventana, última carpeta de exportación y tema de apariencia. El tamaño/posición de ventana y el tema SHALL guardarse de forma global».
- `Database.setDataDir(Path)` permite a los tests usar una carpeta temporal; `lockPathGlobal()` y `PreferenciasGlobales` cuelgan de ella.

## Goals / Non-Goals

**Goals:** que `Main` muestre el orden del arranque y delegue; que la instancia única y la preparación de datos se puedan probar sin ventana; quitar el guardado de ventana que nadie usa; no avisar de «ya está en ejecución» cuando el fallo es otro.

**Non-Goals:** raíz de composición con objetos inyectados; mover `Main`/`Launcher` de paquete; sacar el flujo arranque → menú → cierre de `Main`; recuperar la posición de la ventana al abrir; tocar `PreferenciasGlobales.getDouble` (sigue probado en su test); cualquier cambio en controladores o FXML.

## Decisions

### D1. Utilidades estáticas junto a `Main`

Decisión del usuario: clases `final` con constructor privado y métodos `static`, en `com.alcazaba.facturacion`, como `Dialogos`, `Ventanas` o `EmpresaManager`. Descartado para este change: objetos creados en `Main` y pasados por constructor (raíz de composición), por ser más grande y no encajar con el estilo elegido.

### D2. `InstanciaUnica`

```java
/**
 * Garantiza que solo haya una ventana de la aplicación abierta a la vez en este
 * usuario de Windows. Usa un bloqueo del sistema operativo sobre el fichero
 * facturas.lock de la carpeta de datos: mientras la aplicación está abierta el
 * fichero queda bloqueado y una segunda ejecución no puede bloquearlo. El sistema
 * libera el bloqueo aunque la aplicación termine de forma inesperada.
 */
public final class InstanciaUnica {

    private static FileChannel canal;
    private static FileLock bloqueo;

    private InstanciaUnica() {
    }

    /**
     * Intenta quedarse con el bloqueo de la aplicación.
     *
     * @return true si esta ejecución es la única abierta (o ya tenía el bloqueo);
     *         false si otra ejecución lo tiene
     * @throws IOException si no se puede abrir el fichero de bloqueo
     */
    public static synchronized boolean adquirir() throws IOException

    /**
     * Suelta el bloqueo y cierra el fichero. No hace nada si no se tenía.
     */
    public static synchronized void liberar()
}
```

Comportamiento de `adquirir()`:

1. Si `bloqueo != null && bloqueo.isValid()`, devuelve `true` (llamarlo dos veces no falla).
2. Abre `Database.lockPathGlobal()` con `CREATE, WRITE`. Si lanza `IOException`, se propaga.
3. `tryLock()`: si devuelve `null` **o** lanza `OverlappingFileLockException`, cierra el canal, deja los campos a `null` y devuelve `false`.
4. Si lo obtiene, guarda canal y bloqueo y devuelve `true`.

`liberar()` hace lo que hoy hace `liberarLock` (soltar y cerrar ignorando `IOException`) y además deja los dos campos a `null`, para que un `adquirir()` posterior funcione.

Los Javadoc de este bloque son los que deben quedar en el código (breves y explicando el porqué).

### D3. `PreparacionDatos`

```java
/**
 * Deja lista la carpeta de datos antes de mostrar la pantalla de arranque:
 * crea la carpeta si no existe y, en una instalación nueva, carga la empresa
 * de demostración para poder probar el programa.
 */
public final class PreparacionDatos {

    private PreparacionDatos() {
    }

    /**
     * Crea la carpeta raíz de datos (%APPDATA%\Facturacion) si no existe.
     * Sin ella la aplicación no puede funcionar.
     *
     * @throws IOException si no se puede crear
     */
    public static void crearCarpeta() throws IOException

    /**
     * Si no existe ninguna empresa, carga la de demostración y la deja como
     * última empresa usada para que salga preseleccionada. Si ya hay alguna
     * empresa no hace nada, así nunca pisa datos ni duplica la demostración.
     *
     * @return true si ha cargado la demostración en esta llamada
     * @throws Exception si falla la carga
     */
    public static boolean cargarDemoSiNoHayEmpresas() throws Exception
}
```

- `crearCarpeta()` = `Files.createDirectories(Database.baseDataDir())`.
- `cargarDemoSiNoHayEmpresas()` = si `EmpresaManager.listarEmpresas().isEmpty()`: `CargarDemo.cargar()`, `PreferenciasGlobales.set(ULTIMA_EMPRESA, CargarDemo.SLUG)`, `return true`; si no, `return false`.

Dos métodos y no uno porque sus fallos son distintos: sin carpeta la aplicación se cierra; sin demostración sigue con la lista vacía (decisión del usuario).

### D4. `Main` tras el cambio

Campos que quedan: `stage`, `servicios`, `nav`, `actual`. Desaparecen `lockChannel` y `lock`.

```java
/**
 * Punto de entrada de la aplicación. Fija el idioma español, prepara la carpeta
 * de datos, asegura que solo haya una ventana abierta, muestra la pantalla de
 * arranque (empresa y fecha de trabajo) y, al entrar, construye los servicios y
 * abre la primera pantalla. Cada paso delega en su pieza: PreparacionDatos,
 * InstanciaUnica, ArranqueController y Navegador.
 */
public class Main extends Application {
```

`start`, en este orden y con estos mensajes:

```java
@Override
public void start(Stage stage) {
    this.stage = stage;
    try {
        PreparacionDatos.crearCarpeta();
    } catch (IOException e) {
        Dialogos.error("Facturación", "No se pudo preparar la carpeta de datos:\n" + e.getMessage());
        Platform.exit();
        return;
    }
    try {
        if (!InstanciaUnica.adquirir()) {
            Dialogos.error("Facturación", "La aplicación ya está en ejecución.\nSolo puede abrirse una instancia.");
            Platform.exit();
            return;
        }
    } catch (IOException e) {
        Dialogos.error("Facturación", "No se pudo comprobar si la aplicación ya está abierta:\n" + e.getMessage());
        Platform.exit();
        return;
    }
    Platform.setImplicitExit(false);
    configurarVentana();
    boolean demoCargada = false;
    try {
        demoCargada = PreparacionDatos.cargarDemoSiNoHayEmpresas();
    } catch (Exception e) {
        Dialogos.error("Facturación", "No se pudo cargar la empresa de demostración:\n" + e.getMessage());
    }
    ArranqueController arranque = mostrarArranque();
    stage.show();
    boolean demo = demoCargada;
    Platform.runLater(() -> arranque.mostrarAvisoInicial(demo));
}
```

Ajustar a cómo esté hoy `mostrarArranque` y la llamada a `mostrarAvisoInicial` en el código real; lo que importa es el orden y los tres mensajes. El orden carpeta → bloqueo → demostración se mantiene: la demostración no se carga si hay otra instancia abierta.

`cerrarAplicacion` pierde la llamada a `guardarPreferenciasVentana` y cambia `liberarLock()` por `InstanciaUnica.liberar()`. Se borran `prepararDatos`, `cmbEmpresaVacia`, `adquirirLock`, `liberarLock` y `guardarPreferenciasVentana`, y los imports sobrantes (`FileChannel`, `FileLock`, `StandardOpenOption`, `Files`, `CargarDemo`, `EmpresaManager`…).

Javadoc de una a tres líneas en `start`, `configurarVentana`, `mostrarArranque`, `entrarEnMenu` y `cerrarAplicacion` que diga qué hace cada uno y, en `cerrarAplicacion`, por qué devuelve `boolean` (si el usuario cancela, quien llama consume el evento de cierre).

### D5. Fuera el guardado de la ventana

- Se borra `Main.guardarPreferenciasVentana` y su llamada.
- Se borran `VENTANA_X`, `VENTANA_Y`, `VENTANA_W` y `VENTANA_H` de `PreferenciasGlobales`.
- En `PreferenciasGlobalesTest.lecturaEscrituraEsIdempotente`, las dos preferencias de ventana se sustituyen por claves literales de prueba (`"clave_decimal"` con `"120.5"` y `"clave_entera"` con `"900"`), manteniendo las mismas comprobaciones de `getDouble`.
- Los ficheros `preferencias.properties` que ya tengan `ventana_*` no molestan: nadie los lee. No se limpian.

Descartado: moverlo a `PreferenciasGlobales.guardarVentana(x, y, ancho, alto)`. Se preguntó, pero al comprobar que nadie lo lee el usuario eligió eliminarlo. Recuperar la posición al abrir sería funcionalidad nueva, para otro change.

### D6. Tests

`src/test/java/com/alcazaba/facturacion/InstanciaUnicaTest.java` (con `@TempDir` y `Database.setDataDir`, y `InstanciaUnica.liberar()` en `@AfterEach`):

- `adquirir()` devuelve `true` con la carpeta vacía y crea `facturas.lock`.
- Llamar dos veces a `adquirir()` devuelve `true` las dos.
- Con el fichero bloqueado desde el test (`FileChannel.open(...).tryLock()` sobre `Database.lockPathGlobal()`), `adquirir()` devuelve `false`. Soltar el bloqueo del test al final.
- Tras `liberar()`, `adquirir()` vuelve a devolver `true`.
- Si la carpeta de datos apunta a una ruta imposible (por ejemplo, dentro de un fichero normal creado en el `@TempDir`), `adquirir()` lanza `IOException`.

`src/test/java/com/alcazaba/facturacion/PreparacionDatosTest.java` (con `@TempDir` y `Database.setDataDir`; `Database.resetConnection()` en `@AfterEach`):

- `crearCarpeta()` crea la carpeta si no existe y no falla si ya existe.
- Sin empresas, `cargarDemoSiNoHayEmpresas()` devuelve `true`, existe la empresa `demo` y `ULTIMA_EMPRESA` vale `demo`.
- Llamarlo una segunda vez devuelve `false` y sigue habiendo una sola empresa.
- Con otra empresa creada antes (`EmpresaManager.crearEmpresa("Otra")`), devuelve `false` y no crea `demo`.

Si algún test deja estático `Database` apuntando a la carpeta temporal, restaurarlo como hacen los tests existentes (`DatabaseTest`, `EmpresaManagerTest`).

## Risks / Trade-offs

- **Riesgo bajo:** mismo orden de arranque y mismos mensajes, salvo el nuevo de «no se pudo comprobar».
- **Estado global:** `InstanciaUnica` guarda el bloqueo en campos estáticos. Es inherente a una instancia única por proceso y es el estilo elegido; se mitiga con `liberar()` que deja todo a `null`.
- **Descuido posible:** olvidar `InstanciaUnica.liberar()` en `cerrarAplicacion`. El sistema operativo libera el bloqueo al morir el proceso, pero la tarea 5.2 lo comprueba.
- **Descuido posible:** que `PreparacionDatosTest` deje la empresa `demo` en la carpeta real. Todos los tests deben usar `@TempDir` antes de llamar a nada.
- **Spec:** el cambio de «Configuración» es solo quitar el tamaño/posición de ventana de la lista; se copia el requisito completo con todos sus escenarios.
