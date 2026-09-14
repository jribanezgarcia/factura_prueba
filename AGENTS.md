# CaboFactu: normas del proyecto

Aplicación de escritorio de facturación en Java 21, JavaFX 21, SQLite y OpenPDF, con Maven.

El autor es alumno de 1º de DAM: el código tiene que poder leerlo, entenderlo y defenderlo alguien de ese nivel. La referencia de estilo es su proyecto final, Biblioteca8. Si algo no está en estas normas, se escribe como en Biblioteca8.

## Dónde está cada cosa

- **Cómo funciona la aplicación** (reglas de negocio y lo que ve el usuario): `openspec/specs/`. Es la fuente de verdad. Si un cambio modifica el comportamiento, su change lleva la spec delta correspondiente.
- **Cómo se construye un cambio**: `design.md` y `tasks.md` de cada change en `openspec/changes/`.
- **Cómo se escribe el código**: este fichero.

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
- Sin `Task`, `Thread` ni hilos propios. El trabajo se hace en el método del botón con `try / catch / finally` y cursor de espera. Antes de una operación larga (por ejemplo, varios PDF) se avisa con `Dialogos.confirmar`.
- `Platform.runLater` solo en los tests y en el salto de celdas del Editor.
- Permitido: `switch` con flecha (`case X ->`) y bloques de texto (`"""`) para SQL largo. El SQL corto va en una línea.
- Métodos de más de unas 40 líneas se parten en métodos privados con nombre; el método principal se lee como un índice.
- Ventanas y formularios siempre en FXML con su controlador, nunca construidos en Java.
- Nombres en español, salvo el sufijo `Controller`. Nunca dos clases con el mismo nombre en paquetes distintos.

## Arquitectura (MVC como Biblioteca8)

- `AppCaboFactu.main` crea `Modelo`, `Vista.getInstancia()` y `Controlador(modelo, vista)`, y llama a `controlador.comenzar()`.
- `Vista` es un singleton: arranca `LanzadorVentanaPrincipal` y cambia de pantalla con `Vista.getInstancia().mostrar(fxml)`.
- `Controlador` arranca y cierra la aplicación y da el modelo con `getModelo()`. No repite los métodos del negocio.
- `Modelo` se crea una vez, guarda todo el negocio en campos `private` y lo da con getters.
- Las pantallas (`*Controller`) cargan sus datos en `initialize()` y escriben la llamada completa, sin guardarla en un campo:
  `Vista.getInstancia().getControlador().getModelo().getFacturas().crearFactura(...)`.
- Paquetes: `cabofactu.modelo.dominio` (datos), `cabofactu.modelo.negocio` (reglas, en plural: `Facturas`, `Clientes`), `cabofactu.modelo.negocio.sqlite` (acceso a datos: `FacturaDAO`, `Conexion`), `cabofactu.vista`, `cabofactu.vista.controlador`, `cabofactu.vista.recursos`, `cabofactu.vista.utilidades`, `cabofactu.fichero`, `cabofactu.pdf`, `cabofactu.utilidades`.
- `static` solo en clases herramienta que no guardan datos propios (`Conexion`, `Dialogos`, `Formatos`, validadores…).
- Las reglas se comprueban en el negocio, lanzando `ValidacionException`, además de en la pantalla.

## Comentarios

- Javadoc corto en todas las clases y en los métodos públicos. En métodos privados, solo si el nombre no lo dice todo. Getters y setters sin comentario.
- `//` solo donde algo no se entiende a la primera.
- Si una clase usa algo que no se ve en 1º de DAM (celdas y `setCellFactory`, eventos de OpenPDF, `Property`, `runLater`, `StringConverter`, `equals` y `hashCode` como clave de un `Map`…), un párrafo «Cómo funciona» que lo explique.
- En primera persona del plural y con tildes: «guardamos», «desactivamos».
- `@author jribanezgarcia` solo en `AppCaboFactu`.
- En los tests, un Javadoc por clase y `//` donde haya algo raro; no en cada `@Test`.
- Nunca comentarios sobre la historia del código: nada de «antes era…», nombres de changes ni arreglos.

## Transición

El código todavía no cumple todas estas normas. En cada change se hace **solo** lo que pide su `tasks.md`, sin arreglar de paso otras partes, pero **ningún código nuevo o modificado puede introducir algo que estas normas prohíben**.

Antes de dar un change por terminado, busca en los ficheros tocados: `record`, operador ternario, `var`, `::`, `.stream()`, clases anónimas y nombres completos de clase. No debe haberse añadido ninguno.
