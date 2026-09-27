## Situación de partida

| Pieza | Hoy |
|---|---|
| `fichero/CopiaSeguridad` | 124 líneas. `record ResumenCopia` dentro; `Clock`; recibe `CopiaSeguridadDAO` y `Facturas` en el constructor; `Modelo(Clock)` la crea |
| `CopiaSeguridadDAO` | 187 líneas. `TABLAS_NUCLEO` (7 tablas), `TABLAS_APLICACION` (las mismas y `tipo_retencion`) y `COLUMNAS_APLICACION` (mapa con ~80 columnas); `leerResumen` de 63 líneas con ternarios |
| `DatosException` | `RuntimeException` propia, en el DAO y en `Conexion` |
| `CopiaSeguridadController` | 306 líneas. Tres `Task` anónimas con `new Thread`; `Object[]` para devolver el tipo de restauración y su resultado; la regla del NIF y la de la empresa vacía en la pantalla; `restaurar()` de 71 líneas |
| `Conexion` | `confirmar`, `deshacer`, `iniciarTransaccion` y `terminarTransaccion` sin uso; `getEmpresasDisponibles` con stream y `::`; ternario en `carpetaRaizPorDefecto`; `IllegalStateException` en `leerScript`; Javadoc sin tildes |

## Objetivos y lo que queda fuera

**Objetivo**: la pantalla hace lo mismo que hoy, más lo decidido con el usuario: un botón que recuerda la carpeta, el nombre con la empresa, la comprobación contra el script, reemplazar solo con el mismo NIF y la vuelta al arranque. Todo según `AGENTS.md`, y sin ninguna pieza pendiente de la transición.

**Fuera**: restaurar desde la pantalla de arranque, limpiar `copias_previas`, y copiar los PDF o el logo.

---

## D1. `ResumenCopia`

`modelo/dominio/ResumenCopia.java`: lo que se lee de una copia antes de restaurarla. Es una clase de resultado, como `GrupoIva`: sin validación, sin constructor copia y sin `equals`.

- Campos: `String nombreEmpresa`, `String nif`, `int numeroFacturas`, `LocalDate ultimaFecha` (`null` si no hay facturas) y `String logo` (`""` si no hay).
- Constructor con todo: `ResumenCopia(String nombreEmpresa, String nif, int numeroFacturas, LocalDate ultimaFecha, String logo)`. Un `null` en los textos se guarda como `""`.
- Getters y setters normales.
- Métodos propios:
  - `isLogoPerdido()`: `true` si `logo` no está vacío y el fichero no existe en este ordenador (`Files.exists(Path.of(logo))`).
  - `getTexto()`: el resumen que enseña la pantalla, con `String.format` y `Formatos.fecha`:
    ```
    Empresa: Empresa Demo S.L.
    NIF: B12345674
    Facturas: 27
    Última factura: 15/09/2026
    ```
    - Sin facturas, la última línea dice `Última factura: ninguna`.
    - Sin NIF, `NIF: sin NIF`.
    - Si `isLogoPerdido()`, una línea más al final: `El logo de la copia no está en este ordenador: la empresa se quedará sin logo.` (sin el «⚠» de hoy).

## D2. `CopiaSeguridad`

`modelo/negocio/CopiaSeguridad.java`, singleton con `getCopiaSeguridad()` y constructor privado, como el resto del negocio. Sustituye a `fichero/CopiaSeguridad` y a `CopiaSeguridadDAO`.

**Constantes**: `CARPETA_RESCATE = "copias_previas"` y `FORMATO_NOMBRE = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")`.

**Métodos públicos**, todos `throws Exception` y con sus guardas al principio:

| Método | Qué hace |
|---|---|
| `Path crear(Path carpeta)` | Guarda: `carpeta == null` → `Elija la carpeta de la copia.` Crea la carpeta si falta, calcula el nombre (`Sesion.getSesion().getCarpetaEmpresa() + "_" + LocalDateTime.now().format(FORMATO_NOMBRE)`) con `rutaLibre` y ejecuta `VACUUM INTO`. Después guarda la carpeta en `PreferenciasGlobales.CARPETA_COPIAS`. Devuelve la ruta del archivo |
| `Path carpetaCopias()` | La carpeta guardada en `PreferenciasGlobales.CARPETA_COPIAS`, o `null` si no hay ninguna o ya no existe |
| `ResumenCopia leer(Path origen)` | Guardas: `origen == null` o no es un archivo legible → `El archivo elegido no se puede leer.`; es la base activa (`toAbsolutePath().normalize()`) → `No se puede restaurar la base de la empresa activa sobre sí misma.` Abre la copia, `comprobarIntegridad`, `comprobarEstructura` y `crearResumen` |
| `boolean puedeReemplazar(ResumenCopia resumen)` | `true` si la empresa activa (`Configuracion.getConfiguracion().buscarEmpresa()`) no es `null` y su NIF coincide con el de la copia, sin contar espacios ni mayúsculas (`trim()` y `equalsIgnoreCase`) |
| `Path restaurar(Path origen)` | `leer(origen)`; si `!puedeReemplazar(resumen)` → `La copia es de otra empresa (NIF %s). Restáurela como empresa nueva.` Crea la copia de rescate en `Conexion.carpetaEmpresa().resolve(CARPETA_RESCATE)`, `sustituirBase(origen)` y `Empresas.getEmpresas().recordarTema()`. Si `sustituirBase` falla, vuelve a poner el rescate y lanza `No se pudo restaurar la copia; se ha recuperado la base anterior: ` + mensaje. Devuelve la ruta del rescate |
| `EmpresaDisponible restaurarComoEmpresa(Path origen, String nombre)` | `leer(origen)`; `Empresas.getEmpresas().alta(nombre)` (ya comprueba el nombre); copia el archivo sobre su `facturas.db` y borra el diario. Si la copia falla, da de baja la empresa recién creada y lanza `No se pudo crear la empresa desde la copia: ` + mensaje. No cambia la empresa activa |

La guarda de `restaurar` es la red de seguridad: la pantalla ya no ofrece reemplazar si el NIF no coincide.

**Métodos privados** con nombre, todos de menos de 40 líneas:

- `rutaLibre(Path carpeta, String nombre)`: `nombre.db` y, si existe, `nombre_2.db`, `nombre_3.db`… con un `while`, sin `for (;;)`.
- `comprobarIntegridad(Connection copia)`: `PRAGMA quick_check`. Si no devuelve `ok` → `El archivo no es una base de datos válida.`
- `comprobarEstructura(Connection copia)`: abre una base vacía en memoria (`jdbc:sqlite::memory:`), le crea las tablas con `Conexion.crearTablasSiFaltan` y, por cada tabla de esa base, comprueba que la copia la tiene con todas sus columnas. Junta lo que falta («la tabla 'serie'», «la columna 'email' de 'cliente'») y, si hay algo, lanza `La copia no contiene ` + `String.join(", ", faltan)` + `.`. Lo que la copia tenga de más no importa. Lleva el párrafo «Cómo funciona»: la base en memoria no crea ningún archivo y desaparece al cerrarla.
- `tablas(Connection c)`: `SELECT name FROM sqlite_master WHERE type = 'table' AND name NOT LIKE 'sqlite_%'`.
- `columnas(Connection c, String tabla)`: `SELECT name FROM pragma_table_info(?)`, con `PreparedStatement`.
- `crearResumen(Connection copia)`: `SELECT nombre, nif, logo_path FROM empresa WHERE id = 1` y `SELECT COUNT(*), MAX(fecha) FROM factura`, sin ternarios.
- `sustituirBase(Path origen)`: cierra la conexión, copia el archivo sobre `Conexion.rutaBase()`, `borrarDiario` y vuelve a conectar.
- `borrarDiario(Path carpeta)`: borra `facturas.db-wal` y `facturas.db-shm` si están.

Los `SQLException` se relanzan con `throw new Exception("Error SQLite: " + e.getMessage())`, salvo al abrir la copia en `leer`, que dice `El archivo no es una base de datos válida.` Los `IOException` se relanzan con un mensaje que diga qué no se pudo hacer (`No se pudo crear la carpeta: …`, `No se pudo copiar el archivo: …`).

**`PreferenciasGlobales`**: constante nueva `CARPETA_COPIAS = "carpeta_copias"`.

**`Empresas`**: método nuevo `recordarUltima(String carpeta)`, que guarda la carpeta en `PreferenciasGlobales.ULTIMA_EMPRESA`. `abrir` pasa a usarlo, y la pantalla de arranque, que ya elige la última empresa, la encuentra marcada.

**`Controlador` y `Modelo`**, con una línea cada uno:

- `crearCopia(Path carpeta)` y `carpetaCopias()`;
- `leerCopia(Path origen)` y `puedeReemplazarEmpresa(ResumenCopia resumen)`;
- `restaurarCopia(Path origen)` y `restaurarCopiaComoEmpresa(Path origen, String nombre)`;
- `recordarUltimaEmpresa(String carpeta)`.

Fuera `Modelo(Clock)`, el campo `copiaSeguridad`, `Modelo.getCopiaSeguridad()` y `Controlador.getModelo()`, que ya no usa nadie. `Modelo` se queda sin constructores escritos.

`Facturas.contar()` se queda: lo usan los tests de `Facturas`.

## D3. `Conexion`

Se reescribe según las normas, sin cambiar lo que hace ni sus firmas públicas, salvo lo que se dice aquí:

- **Fuera** `confirmar`, `deshacer`, `iniciarTransaccion` y `terminarTransaccion`: cada negocio escribe su transacción en su propio método.
- **`crearBase(Path)`** pasa a `throws Exception`: `No se pudo crear la carpeta de datos: …` o `Error SQLite: …`.
- **`getEmpresasDisponibles()`**: `carpetaRaiz.toFile().listFiles()`, un `for-each` que se queda con las carpetas que tienen `facturas.db`, y `Collections.sort`. Si `listFiles()` devuelve `null`, la lista vacía.
- **`carpetaRaizPorDefecto()`**: `if / else` en lugar del ternario.
- **`leerScript`** lanza `SQLException` (`No se encontró el script de tablas.` o `No se pudo leer el script de tablas.`), para que `establecerConexion` y `crearTablasSiFaltan` sigan con `throws SQLException` y no cambie ningún negocio.
- **`establecerConexion`** pierde `synchronized`: ya no hay hilos.
- Javadoc corto en todos los métodos públicos, en primera persona del plural y con tildes.

Con esto se borra `DatosException`.

## D4. La pantalla

**`CopiaSeguridad.fxml`**, con imports explícitos, uno por clase:

- **Bloque «Copia de seguridad»**:
  - el título;
  - `La copia guarda todos los datos de la empresa en un solo archivo.`;
  - el botón `btnCrear` con el texto `Crear copia…` y `onAction="#crearCopia"`, siempre activo;
  - la etiqueta `lblCarpeta`.
  - Fuera `lblDestino`, el botón de elegir carpeta y `lblResultado`.
- **Bloque «Restaurar una copia»**:
  - como hoy: el texto, `Elegir copia…` (`onAction="#elegirCopia"`), `lblOrigen`, `cajaResumen` con `lblResumen`, los dos `RadioButton` con su `ToggleGroup`, `filaNombreEmpresa` con `txtNombreEmpresa` y `btnRestaurar`;
  - los dos `RadioButton` llevan `onAction="#cambiarDestino"`, en lugar del *listener* del controlador;
  - fuera `lblResultadoRestauracion`: sin hilos no hay «Restaurando…».
- La barra, el `ScrollPane` y «Volver», como hoy.

**`lblCarpeta`**:

- con carpeta recordada: `String.format("Las copias se guardan en %s", carpeta)`;
- sin carpeta: `Todavía no se ha elegido la carpeta de las copias.`

**`CopiaSeguridadController`**, con los campos `origen` (`Path`) y `resumen` (`ResumenCopia`):

- **`initialize`**: marca la barra y llama a `mostrarCarpeta()` y a `limpiarRestauracion()`.
- **`crearCopia`**:
  1. Abre `DirectoryChooser` con el título `Carpeta para la copia de seguridad` y, si hay carpeta recordada, empezando en ella. Si el usuario cancela, no hace nada.
  2. Con el cursor de espera, `crearCopia(carpeta)`.
  3. Avisa `String.format("Copia creada en:%n%s", ruta)` y `mostrarCarpeta()`.
- **`elegirCopia`**:
  1. Abre `FileChooser` con el título `Elegir la copia que se va a restaurar`, el filtro `Copias de CaboFactu (*.db)` y, si hay carpeta recordada, empezando en ella.
  2. Con el cursor de espera, `leerCopia`.
  3. Si va bien: `lblOrigen` con el nombre del archivo, `lblResumen` con `resumen.getTexto()`, la caja visible y `prepararDestino()`.
  4. Si falla: el error con `e.getMessage()` y `limpiarRestauracion()`.
- **`prepararDestino()`**:
  - si `puedeReemplazarEmpresa(resumen)`, los dos `RadioButton` activos y marcado «Reemplazar»;
  - si no, «Reemplazar» desactivado y marcado «Crear una empresa nueva».
  - Después, `cambiarDestino(null)` y `btnRestaurar` activo.
- **`cambiarDestino`**: enseña `filaNombreEmpresa` solo con «Crear una empresa nueva» marcado.
- **`restaurar`**: llama a `reemplazarEmpresa()` o a `crearEmpresaDesdeCopia()` según el `RadioButton` marcado.
- **`reemplazarEmpresa()`**:
  1. Confirma con `String.format("¿Reemplazar los datos de %s con los de la copia?%n%nAntes guardaremos una copia de rescate del estado actual.", nombreEmpresa)`. El nombre sale de `buscarEmpresa()`.
  2. Con el cursor de espera, `restaurarCopia(origen)`.
  3. Avisa `String.format("Copia restaurada. La copia de rescate está en:%n%s", rescate)` y llama a `Vista.getInstancia().mostrarInicio()`.
- **`crearEmpresaDesdeCopia()`**:
  1. Lee el nombre con `txtNombreEmpresa.getText().trim()`. Si está vacío → `Escriba el nombre de la nueva empresa.`
  2. Confirma con `String.format("¿Crear la empresa «%s» con los datos de la copia?", nombre)`.
  3. Con el cursor de espera, `restaurarCopiaComoEmpresa`.
  4. Pregunta `String.format("Se ha creado la empresa «%s».%n%n¿Quieres cambiar a ella? Se cerrará esta empresa y volverás a la pantalla de arranque con la nueva elegida.", nombre)`.
     - Si acepta: `recordarUltimaEmpresa(nueva.getCarpeta())` y `Vista.getInstancia().volverAlArranque()`.
     - Si no: `limpiarRestauracion()`.
- **`volver`**: al menú, como hoy.
- **Privados de ayuda**: `mostrarCarpeta()`, `limpiarRestauracion()` (vacía `origen` y `resumen`, `lblOrigen` en `(ninguna copia elegida)`, esconde la caja y la fila del nombre, vacía el nombre y desactiva `btnRestaurar`), `esperar()` y `terminarEspera()` para el cursor, como en `HistoricoController`.

Todo con `try / catch / finally`: un único `catch (Exception e)` con `Dialogos.mostrarDialogoError("Copia de seguridad", e.getMessage())` (o `"Restaurar copia"` en el bloque de restaurar), y el cursor normal en el `finally`. Sin `Task`, sin `Thread`, sin ternarios y sin `Object[]`.

## D5. Tests

**`modelo/negocio/CopiaSeguridadTest`**, que sustituye a `fichero/CopiaSeguridadTest` y a `sqlite/CopiaSeguridadDAOTest`:

- Los de hoy, adaptados al singleton y sin `Clock`:
  - restaurar devuelve los datos de la copia;
  - la copia de rescate guarda el estado previo;
  - como empresa nueva no toca la activa;
  - el resumen, con facturas y sin ellas;
  - rechaza un archivo que no es una base, una copia sin las tablas y la propia base activa;
  - acepta una copia con tablas o columnas de más;
  - rechaza una copia a la que le falta una columna, con el mensaje que la nombra;
  - la base restaurada queda utilizable y sin el diario huérfano;
  - recuerda el tema de la copia;
  - y los cuatro de `CopiaSeguridadDAOTest`.
- Casos nuevos:
  - el nombre empieza por la carpeta de la empresa y sigue con la fecha de hoy (`pruebas_backup_AAAAMMDD_`);
  - dos copias seguidas en la misma carpeta dan dos archivos distintos;
  - tras `crear`, `carpetaCopias()` devuelve esa carpeta, y `null` si se borra la carpeta;
  - `puedeReemplazar` con el mismo NIF (también en minúsculas) y con otro;
  - `restaurar` una copia con otro NIF lanza el mensaje de D2 y no cambia nada.

**`modelo/dominio/ResumenCopiaTest`** (nuevo): `getTexto()` con facturas, sin facturas, sin NIF y con el logo perdido; `isLogoPerdido()` con logo vacío, con un archivo que existe y con uno que no.

**`ConexionTest`**: los de hoy, y además `getEmpresasDisponibles` devuelve ordenadas solo las carpetas que tienen `facturas.db`.

**`PantallaCopiasTest`**:

- Estado inicial: `Crear copia…` activo, `lblCarpeta` sin carpeta, `(ninguna copia elegida)` y `btnRestaurar` desactivado.
- Con una copia hecha antes de abrir la pantalla (con el controlador, en una carpeta temporal), `lblCarpeta` enseña esa carpeta.
- «Volver» lleva al menú.

Los selectores de archivos de Windows no se pueden manejar desde el test: crear, elegir y restaurar se prueban en el negocio y a mano.

## Riesgos y renuncias

- **Sin hilos, la ventana no responde mientras se copia.** Con bases de este tamaño son décimas de segundo, y restaurar ya pide confirmación antes.
- **La comprobación depende del script**: una copia de un programa más viejo, al que le falte una columna nueva, se rechaza aunque se pudiera usar. Es lo que ya pide la especificación.
- **La carpeta recordada es de todas las empresas**: quien quiera una carpeta por empresa tiene que elegirla cada vez.
- **Sin la excepción de la empresa vacía**: en un ordenador nuevo se entra en la demo y se restaura como empresa nueva.
