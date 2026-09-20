# CaboFactu: normas del proyecto

Aplicación de escritorio de facturación en Java 21, JavaFX 21, SQLite y OpenPDF, con Maven.

El autor es alumno de 1º de DAM: el código tiene que poder leerlo, entenderlo y defenderlo alguien de ese nivel. La referencia es su proyecto final, **Biblioteca8**, y sus apuntes de patrones. Si algo no está en estas normas, se escribe como en Biblioteca8.

**Antes de trabajar, lee `ESTADO.md`**: qué está hecho, qué hay en curso y qué toca ahora.

## Flujo de trabajo

Todo pasa por OpenSpec, con los comandos de opencode: `/opsx-propose` → `/opsx-apply` → `/opsx-archive`. No se toca el código ni `openspec/specs/` fuera de un change.

Obligatorio en cada change:

- **Al aplicar**: añadir o actualizar el change en la sección «En curso» de `ESTADO.md`, con una línea de qué cambia.
- **Al archivar**: pasarlo a «Hecho» en `ESTADO.md`, actualizar «Qué toca ahora» y apuntar en «Trampas conocidas» lo que haya salido mal por el camino.
- **Si el change cambia alguna norma de este fichero**, actualizar `AGENTS.md` en el mismo change.

### Idioma de los changes

Todo se escribe en **español**, salvo las palabras que OpenSpec necesita leer en inglés:

- En `proposal.md`, las cabeceras `## Why` y `## What Changes`. Sin ellas, `openspec show` falla con «Change must have a Why section».
- En las specs y en los deltas: `## ADDED Requirements`, `## MODIFIED Requirements`, `## REMOVED Requirements`, `### Requirement:`, `#### Scenario:`, `- **WHEN**`, `- **THEN**`, `- **AND**` y los `SHALL`. **Si se traducen, `openspec validate --strict` sigue diciendo «is valid» y el requisito se pierde en silencio al archivar.**

El resto de cabeceras van en español: «Capacidades», «A qué afecta», «Situación de partida», «Objetivos y lo que queda fuera», «Decisiones», «Riesgos y renuncias» y todo `tasks.md`.

`ESTADO.md` no guarda el historial de cada change: eso está en `openspec/changes/archive/` y en `git log`.

## Mapa de la documentación

Este fichero es lo único que se lee entero siempre. Lo demás se consulta **cuando hace falta**:

| Dónde | Qué hay | Cuándo leerlo |
|---|---|---|
| `ESTADO.md` | Estado del proyecto y trampas conocidas | Al empezar cualquier tarea |
| `openspec/specs/` | Qué hace la aplicación (fuente de verdad) | Al proponer o revisar comportamiento, **por requisito** |
| `openspec/changes/<nombre>/` | `proposal.md`, `design.md`, `tasks.md` de un change | Al aplicar o revisar ese change |
| `docs/tecnico.md` | Arquitectura, paquetes, modelo de datos, decisiones técnicas | Si necesitas el esquema de tablas o cómo fluye una operación |
| `docs/metodologia.md`, `README.md` | Explicación del proyecto para GitHub | Solo si hay que actualizarlos |
| `borrador_changes/` (fuera de git) | Decisiones tomadas con el usuario y análisis | Si necesitas **por qué** se decidió algo |
| `openspec/changes/archive/` | Los 100+ changes terminados | Solo para rastrear un cambio viejo; antes prueba `git log -S` |

## Cómo consultar sin gastar contexto

- **Las specs, por requisito, nunca enteras** (`invoicing` son ~29.000 tokens):
  ```bash
  openspec show invoicing --type spec --json --requirements --no-scenarios   # títulos de los 54 requisitos
  openspec show invoicing --type spec --json -r 8                            # el requisito 8 completo
  openspec list --specs
  ```
- **El código, con `grep` primero**: busca el método o el texto y abre solo el trozo.
- **El historial, con git**: `git log -S "textoQueBuscas"`.
- **No copies contenido de un sitio a otro**: cada cosa vive en un único fichero.

## Arquitectura (MVC como Biblioteca8)

```
AppCaboFactu            crea Modelo, Vista y Controlador, y arranca
controlador/Controlador arranca y cierra la aplicación; repite las operaciones del modelo
modelo/Modelo           repite las operaciones del negocio
modelo/negocio/         Clientes, Facturas, Series… singletons con el SQL dentro
modelo/dominio/         Cliente, Factura, LineaFactura… clases de datos que se validan solas
vista/Vista             singleton: guarda la ventana y cambia de pantalla
vista/controlador/      un *Controller por FXML
```

- `AppCaboFactu.main`: `Modelo modelo = new Modelo(); Vista vista = Vista.getInstancia(); Controlador controlador = new Controlador(modelo, vista); controlador.comenzar();`
- `Vista` es singleton (`Vista.getInstancia()`), guarda el `Controlador` y la ventana, y cambia de pantalla con `mostrar("Clientes.fxml")`. `LanzadorVentanaPrincipal extends Application` arranca JavaFX.
- **Las pantallas llaman siempre así**: `Vista.getInstancia().getControlador().altaCliente(cliente);`
- `Controlador` y `Modelo` **repiten cada operación** con un método de una línea, como en Biblioteca8. Nombres **verbo + entidad**: `altaCliente`, `bajaCliente`, `modificarCliente`, `buscarCliente`, `listadoClientes`, `anularFactura`…
- Las clases de negocio son **singletons** (`Clientes.getClientes()`, `Facturas.getFacturas()`) y **llevan dentro el SQL de sus propias tablas**. No hay clases DAO.
  - `Clientes` → `cliente`. `Facturas` → `factura`, `factura_linea`. `Series` → `serie`, `serie_siguiente`, `numero_disponible` (incluida toda la numeración). `TiposIva`, `TiposRetencion`, `Configuracion`, `Empresas`, `CopiaSeguridad`.
  - Dentro del negocio los métodos llevan solo el verbo: `alta`, `baja`, `modificar`, `buscar`, `listado`.
- `Calculos` (fórmulas de importes) y las herramientas (`Conexion`, `Dialogos`, `Formatos`, `ValidadorNif`…) son `static` y no guardan datos propios.
- Paquetes: `cabofactu` (App, `InstanciaUnica`, `PreparacionDatos`), `.controlador`, `.modelo`, `.modelo.dominio`, `.modelo.negocio`, `.modelo.negocio.sqlite` (`Conexion` y el script de tablas), `.vista`, `.vista.controlador`, `.vista.recursos` (`LocalizadorRecursos`), `.vista.utilidades`, `.fichero`, `.pdf`, `.utilidades`.

## Clases de datos (`modelo/dominio`)

- **Nunca constructor vacío.** El constructor recibe los datos **obligatorios** y llama a los setters; los opcionales se ponen después con su setter.
- **Los setters validan** y lanzan `Exception` con el mensaje para el usuario, en este orden: `null` → `isBlank()` → `trim()` → `matches(PATRON)` → asignar. Los patrones son constantes de la clase (`public static final String CP_PATTERN`).
- Las validaciones con cálculo que usan varias clases (la letra del NIF) van en una herramienta `static` de `utilidades`.
- **Constructor copia**, y `equals`, `hashCode` y `toString` en todas: `equals`/`hashCode` por el identificador (NIF, código, id) y `toString` con el texto que se ve en los desplegables (así no hacen falta traductores).
- Los textos con formato (`getTotalTexto()`, `getEstadoTexto()`) se apoyan siempre en `Formatos`, que es el único sitio donde se decide cómo se escribe un importe o una fecha.
- `Factura` lleva **objetos dentro**: su `Serie`, su `Cliente` (la **copia** de los datos tal como estaban al emitirla), su `List<LineaFactura>` y su `TipoRetencion`.
- Orden dentro de la clase: constantes, campos, constructores, getters y setters por pares, `equals`/`hashCode`/`toString` y métodos propios.

## Negocio y base de datos (`modelo/negocio`)

- Singleton con constructor privado y `getClientes()`, `getFacturas()`…
- Los métodos públicos declaran `throws Exception`. Empiezan con sus **guardas**: comprobar y lanzar, sin anidar.
- El texto SQL va en una **variable local** con nombre de acción (`insertar`, `consulta`, `borrar`, `actualizar`), con las **columnas escritas** (nunca `SELECT *`). El SQL largo, en bloque `"""`.
- `PreparedStatement` y `ResultSet` en try-with-resources. Tras `executeUpdate()`, comprobar `if (filas == 0)`.
- `SQLException` se captura y se relanza: `throw new Exception("Error SQLite: " + e.getMessage());`
- Las transacciones se escriben **en el propio método**: `setAutoCommit(false)`, `commit`, `rollback` en el `catch` y `setAutoCommit(true)` en el `finally`.
- Pasar una fila a objeto, en un **método privado** reutilizable (`crearCliente(ResultSet fila)`) con un comentario que lo explique.

## Pantallas (`vista/controlador`)

- Un FXML por pantalla, cargado con `LocalizadorRecursos.class.getResource("Clientes.fxml")`. Ventanas y formularios **siempre en FXML**, nunca construidos en Java.
- El controlador `implements Initializable` y carga sus datos en `initialize`. Lo que necesita la ventana (atajos de teclado, foco) va en `alMostrar()`.
- Orden dentro del controlador: constantes, campos `@FXML`, campos propios, `initialize` y `alMostrar`, métodos de los botones en el orden de la pantalla y, al final, los privados de ayuda.
- Los botones: `@FXML void anadirCliente(ActionEvent event)`, en minúscula y con verbo. Los eventos se enganchan en el FXML con `onAction="#anadirCliente"`.
- **Tabla + alta y edición**: la pantalla guarda la fila elegida en un campo `registro` (`onMouseClicked="#seleccionar"`), abre un **formulario modal reutilizable** con `setRegistro(null)` para añadir o `setRegistro(registro)` para editar, recoge el objeto con `getRegistro()` y **es la pantalla principal quien llama al controlador para guardar**. Después, `refrescarTabla()`.
- Columnas siempre con `PropertyValueFactory<>("nombreDelGetter")`, apoyadas en los getters de texto de las clases de datos.
- Los avisos, siempre con `Dialogos`: `mostrarDialogoError`, `mostrarDialogoInformacion`, `mostrarDialogoAdvertencia`, `mostrarDialogoConfirmacion` (Aceptar / Cancelar) y `mostrarDialogoCambiosSinGuardar`.
- Un único `catch (Exception e)` que muestra `e.getMessage()` con `Dialogos.mostrarDialogoError`.

## Estilo del código

Obligatorio en todo código nuevo o modificado, también en los tests.

- Una clase por fichero. Nada de clases, enums, records ni interfaces dentro de otra clase. Única excepción: las celdas de la tabla de líneas dentro de `EditorController`.
- Sin `record`, sin `var`, sin streams, sin referencias a método (`::`), sin clases anónimas y sin operador ternario: siempre `if / else`.
- Sin `Optional`, salvo el que devuelve `showAndWait()` de un diálogo, leído con `isPresent()` y `get()`. En el resto, `null`.
- Solo `Exception`: nada de excepciones propias. Los mensajes van sin prefijos, tal como los verá el usuario.
- Bucles `for-each`; con índice solo cuando se necesita la posición.
- Textos con datos dentro, con `String.format`.
- Comprobar texto vacío escrito tal cual: `if (texto == null || texto.isBlank())`.
- Leer un campo de pantalla: `this.txtNombre.getText().trim()`.
- `this.` solo cuando hace falta (constructores y setters).
- Una lambda solo llama a un método con nombre; si el bloque pasa de dos o tres líneas, va a un método privado. Nada de lambdas guardadas en variables ni arrays de una casilla.
- En `addListener`, parámetros con nombre: `(propiedad, anterior, nuevo)`.
- `instanceof` clásico con cast, sin variable de patrón.
- Siempre `import`; nunca nombres completos de clase en medio del código.
- Sin `Task`, `Thread` ni hilos propios: el trabajo va en el método del botón con `try / catch / finally` y cursor de espera. Antes de una operación larga, se pide confirmación.
- `Platform.runLater` solo en los tests y en el salto de celdas del Editor.
- Permitido: `switch` con flecha (`case X ->`) y bloques de texto (`"""`) para SQL largo.
- Métodos de más de unas 40 líneas se parten en métodos privados con nombre; el principal se lee como un índice.
- Nombres en español, salvo el sufijo `Controller`. Nunca dos clases con el mismo nombre en paquetes distintos.

## Tests

- Tests del negocio y de las clases de datos, contra una base temporal.
- Un único test de pantallas, que carga cada FXML y comprueba que no hay errores de cableado.
- Sin reloj inyectado: los tests usan la fecha real y calculan lo que esperan a partir de ella.

## Comentarios

- Javadoc corto en todas las clases y en los métodos públicos. En métodos privados, solo si el nombre no lo dice todo. Getters y setters sin comentario.
- `//` solo donde algo no se entiende a la primera.
- Si una clase usa algo que no se ve en 1º de DAM (celdas y `setCellFactory`, eventos de OpenPDF, `Property`, `runLater`, transacciones…), un párrafo «Cómo funciona» que lo explique.
- En primera persona del plural y con tildes: «guardamos», «desactivamos».
- `@author jribanezgarcia` solo en `AppCaboFactu`.
- En los tests, un Javadoc por clase y `//` donde haya algo raro; no en cada `@Test`.
- Nunca comentarios sobre la historia del código: nada de «antes era…», nombres de changes ni arreglos.

## Transición

El código de hoy **no** cumple estas normas: tiene DAO, excepciones propias, versiones de factura y clases de datos sin validar. El esqueleto (AppCaboFactu, Controlador, Vista y pantallas) ya las cumple. Estas normas describen **adónde vamos**, y el proyecto se rehace módulo a módulo (ver `ESTADO.md`).

Mientras tanto: en cada change se hace **solo** lo que pide su `tasks.md`, y **ningún código nuevo o modificado puede introducir algo que estas normas prohíben**. Antes de dar un change por terminado, busca en los ficheros tocados `record`, `? :`, `var`, `::`, `.stream()`, clases anónimas y nombres completos de clase: no debe haberse añadido ninguno.
