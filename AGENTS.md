# CaboFactu: normas del proyecto

Aplicación de escritorio de facturación en Java 21, JavaFX 21, SQLite y OpenPDF, con Maven.

El autor es alumno de 1º de DAM: el código tiene que poder leerlo, entenderlo y defenderlo alguien de ese nivel. La referencia de estilo es su proyecto final, Biblioteca8. Si algo no está en estas normas, se escribe como en Biblioteca8.

**Antes de trabajar, lee `ESTADO.md`**: qué está hecho, qué hay en curso y qué toca ahora.

## Flujo de trabajo

Todo pasa por OpenSpec, con los comandos de opencode: `/opsx-propose` → `/opsx-apply` → `/opsx-archive`. No se toca el código ni `openspec/specs/` fuera de un change.

Obligatorio en cada change:

- **Al aplicar**: añadir o actualizar el change en la sección «En curso» de `ESTADO.md`, con una línea de qué cambia.
- **Al archivar**: pasarlo a «Hecho» en `ESTADO.md`, actualizar «Qué toca ahora» y apuntar en «Trampas conocidas» lo que haya salido mal por el camino.
- **Si el change cambia alguna norma de este fichero** (estilo, arquitectura o comentarios), actualizar `AGENTS.md` en el mismo change.

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
  openspec list --specs                                                      # qué specs hay
  ```
- **El código, con `grep` primero**: busca el método o el texto y abre solo el trozo. `EditorController` tiene 1.800 líneas y `ConfiguracionController` 1.000.
- **El historial, con git**: `git log -S "textoQueBuscas"` encuentra el commit donde apareció algo, sin abrir los changes archivados.
- **No copies contenido de un sitio a otro**: cada cosa vive en un único fichero; duplicarla la deja obsoleta.

## Estilo del código

Obligatorio en todo código nuevo o modificado, también en los tests.

- Una clase por fichero. Nada de clases, enums, records ni interfaces dentro de otra clase. Única excepción: las celdas de la tabla de líneas dentro de `EditorController`.
- Sin `record`: clases normales con campos `private`, constructor y getters. Si una clase se usa como clave de un `Map`, lleva `equals` y `hashCode`.
- Campos de las clases de datos siempre `private`, con getters.
- Sin clases anónimas (`new X() { ... }`): clase con nombre en su fichero.
- Sin streams ni referencias a método (`::`): bucles `for` y lambdas escritas, por ejemplo `() -> guardar()`.
- Una lambda solo llama a un método con nombre. Si el bloque pasa de 2 o 3 líneas, va a un método privado.
- Nada de lambdas guardadas en variables (`Runnable`, `BooleanSupplier`, `Consumer`…) ni arrays de una casilla (`boolean[] x = {false}`).
- En `addListener`, parámetros con nombre: `(propiedad, anterior, nuevo)`.
- Los eventos de controles que están en el FXML van con `onAction="#metodo"` y `@FXML`; la lambda solo para controles creados en Java.
- Sin `Optional`, salvo el que devuelve `showAndWait()` de un diálogo, leído con `isPresent()` y `get()`. En el resto, `null`.
- Sin `var`: el tipo siempre escrito.
- `instanceof` clásico con cast, sin variable de patrón.
- Sin operador ternario (`? :`): siempre `if / else`.
- Siempre `import`. Nunca nombres completos de clase en medio del código (`javafx.scene.control.DateCell`, `java.sql.Types.INTEGER`…).
- Sin `Task`, `Thread` ni hilos propios. El trabajo se hace en el método del botón con `try / catch / finally` y cursor de espera. Antes de una operación larga (por ejemplo, varios PDF) se avisa pidiendo confirmación.
- `Platform.runLater` solo en los tests y en el salto de celdas del Editor.
- Permitido: `switch` con flecha (`case X ->`) y bloques de texto (`"""`) para SQL largo. El SQL corto va en una línea.
- Métodos de más de unas 40 líneas se parten en métodos privados con nombre; el método principal se lee como un índice.
- Ventanas y formularios siempre en FXML con su controlador, nunca construidos en Java.
- Nombres en español, salvo el sufijo `Controller`. Nunca dos clases con el mismo nombre en paquetes distintos.

## Arquitectura (MVC como Biblioteca8)

- `AppCaboFactu.main` crea `Modelo`, `Vista.getInstancia()` y `Controlador(modelo, vista)`, y llama a `controlador.comenzar()`.
- `Vista` es un singleton: arranca `LanzadorVentanaPrincipal` y cambia de pantalla con `Vista.getInstancia().mostrar(fxml)`.
- `Controlador` arranca y cierra la aplicación y repite las operaciones del modelo.
- `Modelo` se crea una vez y repite las operaciones del negocio.
- Las pantallas (`*Controller`) cargan sus datos en `initialize()` y llaman con `Vista.getInstancia().getControlador().altaCliente(cliente)`.
- Paquetes: `cabofactu.modelo.dominio` (datos), `cabofactu.modelo.negocio` (reglas, en plural: `Facturas`, `Clientes`), `cabofactu.modelo.negocio.sqlite` (`Conexion` y el script de tablas), `cabofactu.controlador`, `cabofactu.vista`, `cabofactu.vista.controlador`, `cabofactu.vista.recursos`, `cabofactu.vista.utilidades`, `cabofactu.fichero`, `cabofactu.pdf`, `cabofactu.utilidades`.
- `static` solo en clases herramienta que no guardan datos propios (`Conexion`, `Dialogos`, `Formatos`, validadores…).

## Comentarios

- Javadoc corto en todas las clases y en los métodos públicos. En métodos privados, solo si el nombre no lo dice todo. Getters y setters sin comentario.
- `//` solo donde algo no se entiende a la primera.
- Si una clase usa algo que no se ve en 1º de DAM (celdas y `setCellFactory`, eventos de OpenPDF, `Property`, `runLater`, `StringConverter`, `equals` y `hashCode` como clave de un `Map`…), un párrafo «Cómo funciona» que lo explique.
- En primera persona del plural y con tildes: «guardamos», «desactivamos».
- `@author jribanezgarcia` solo en `AppCaboFactu`.
- En los tests, un Javadoc por clase y `//` donde haya algo raro; no en cada `@Test`.
- Nunca comentarios sobre la historia del código: nada de «antes era…», nombres de changes ni arreglos.

## Transición

El código todavía no cumple todas estas normas, y la sección de arquitectura describe adónde vamos, no lo que hay hoy. En cada change se hace **solo** lo que pide su `tasks.md`, sin arreglar de paso otras partes, pero **ningún código nuevo o modificado puede introducir algo que estas normas prohíben**.

Antes de dar un change por terminado, busca en los ficheros tocados: `record`, operador ternario, `var`, `::`, `.stream()`, clases anónimas y nombres completos de clase. No debe haberse añadido ninguno.
