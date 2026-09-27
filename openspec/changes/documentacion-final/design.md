## Situación de partida

Tres documentos (587 líneas en total) que describen la arquitectura anterior a la reescritura, con un emoji en cada título y cada fila. Las capturas de `docs/capturas/` son de las pantallas antiguas. `openspec/config.yaml` pide `git add -A` al archivar, y el proyecto añade siempre por rutas.

## Objetivos y lo que queda fuera

**Objetivo**: un alumno presenta el proyecto a su profesor en GitHub. En menos de diez minutos, el profesor tiene que poder entender tres cosas: qué hace la aplicación, cómo se ha desarrollado con OpenSpec y cómo recorre el código una operación real. Todo lo que se cuente tiene que ser verdad hoy y comprobable en el repositorio.

**Fuera**: el paquete `pdf`, y Jasper y VeriFactu, que se documentarán en sus ramas. Por decisión del alumno, este change incluye además los arreglos de D8 a D11 que salieron al revisar la documentación.

---

## D1. Decisiones tomadas con el alumno

| # | Tema | Decisión |
|---|---|---|
| 1 | Ficheros | Cuatro: `README.md`, `docs/metodologia.md`, `docs/tecnico.md` y `docs/flujos.md` |
| 2 | Herramientas de IA | Con detalle y claro: quién hace qué en cada paso, y que las decisiones y la revisión son del alumno |
| 3 | Commits | Como es de verdad: un commit por paso, con prefijo, y push al archivar |
| 4 | Ramas | Las pruebas y los cambios sensibles van en su rama y vuelven a `main` con un pull request en GitHub |
| 5 | Ejemplo real | `modulo-copias` |
| 6 | Historia | Sección corta por fases |
| 7 | Formato | Para leer en GitHub: diagramas en Mermaid |
| 8 | Detalle de los flujos | Clase, método y una o dos frases por paso; sin pegar código salvo 2 o 3 líneas clave |
| 9 | Capturas | Rehacer las cinco y añadir Copias y el PDF |
| 10 | Estilo | Sobrio: sin emojis en títulos ni tablas; insignias de tecnologías y diagramas en color |

**Reglas para todo el texto**:

- En español, en primera persona del singular cuando habla el alumno («he decidido») y en impersonal en lo técnico.
- Frases cortas, tablas donde haya que comparar, y nada que no se pueda comprobar en el repositorio.
- Los nombres de clases, métodos y ficheros, entre comillas invertidas y con enlace relativo al fichero la primera vez que salen.
- Los diagramas Mermaid tienen que ser válidos en GitHub: nada de HTML raro y textos entre comillas.
- Cada documento, con un índice arriba y enlaces entre los cuatro al principio y al final.
- **Longitud orientativa**: README unas 120 líneas; metodología unas 250; técnico unas 200; flujos unas 200.

## D2. `README.md`

1. **Cabecera**:
   - icono, nombre y una frase;
   - insignias: Java 21, JavaFX 21, SQLite, Maven, JUnit 5 y OpenSpec, y fuera las de «implementa opencode» y «revisa Claude Code», que pasan a la metodología;
   - enlaces a los tres documentos de `docs/` y a `openspec/specs/`.
2. **Sobre el proyecto**: el problema real (la hoja de Excel) y la tabla «antes y después», quitando la fila de versiones y poniendo al día las de copias y PDF.
3. **Qué hace**: la lista de funciones de hoy, sin versiones. Una factura emitida se puede editar, anular, restaurar y rectificar. También: facturación mensual, histórico con filtros y exportación, varias empresas, copias y 7 temas.
4. **Capturas**: tabla con las siete (arranque, menú, editor, histórico, clientes, copias y PDF).
5. **Cómo está montado**: un párrafo y un diagrama pequeño del MVC (D4) con un enlace a `docs/tecnico.md`.
6. **Cómo se ha hecho**: un párrafo sobre OpenSpec y el ciclo de un change, con un enlace a `docs/metodologia.md` y otro a `docs/flujos.md`.
7. **Cómo arrancarlo**: JDK 21, Maven, `mvn javafx:run`, `mvn test` y la carpeta de datos `%APPDATA%\Facturacion`, con la empresa demo. Comprobar si `lanzar.bat` existe antes de nombrarlo.
8. **Lo que he aprendido**: al día. MVC como Biblioteca8, transacciones, tests en tres capas (469 pruebas), escribir antes de programar con OpenSpec, trabajar con IA revisándolo todo, y cambios pequeños. Fuera el `Clock` y la excepción de datos.
9. **Próximos pasos**:
   - hecho: reescritura módulo a módulo y documentación;
   - pendiente: rama `pdf-jasper` (el PDF con JasperReports y los informes) y VeriFactu.

## D3. `docs/metodologia.md`: cómo se ha hecho

1. **Qué es OpenSpec** (un párrafo): un marco de trabajo en el que cada cambio se escribe antes de programarlo, y la especificación de `openspec/specs/` describe siempre lo que hace la aplicación. Qué es una *spec*, un *requisito* y un *escenario* (WHEN / THEN), con un ejemplo corto sacado de `openspec/specs/invoicing/spec.md`.
2. **Las carpetas de OpenSpec**, en un árbol comentado:
   - `openspec/specs/` (la fuente de verdad);
   - `openspec/changes/<nombre>/` (los changes en curso) y `openspec/changes/archive/` (los terminados, con fecha);
   - `openspec/config.yaml` (las guías del proyecto para aplicar y archivar);
   - `AGENTS.md` y `ESTADO.md` (normas del código y estado del proyecto, que se leen antes de cada change).
3. **Los comandos y qué hacen**. Tabla con: comando en opencode (`/opsx-…`), comando en Claude Code (`/opsx:…`), qué le escribo, qué hace y qué ficheros crea o toca.
   - Comandos: `propose`, `apply`, `archive`, `explore`, `update` y `sync`.
   - El detalle de cada uno se saca de las skills y de los comandos de `.opencode/commands/`, no de memoria.
   - Debajo, los comandos de terminal que se usan para comprobar: `openspec list`, `openspec show`, `openspec validate --strict` y `openspec status`.
   - Un ejemplo de cada uno de los tres principales, con lo que escribe el alumno y lo que aparece en disco. Por ejemplo, `/opsx-propose modulo-copias` y la descripción → crea `openspec/changes/modulo-copias/` con `proposal.md` (por qué), `design.md` (cómo), `tasks.md` (pasos) y `specs/invoicing/spec.md` (delta con `MODIFIED Requirements`).
   - **Qué es un delta**: `ADDED`, `MODIFIED` y `REMOVED`, y que al archivar se funde en la spec.
4. **Cómo lo hacemos aquí**: el mismo ciclo, pero con parte automatizada. Sin ocultarlo:
   - antes de `propose`, una ronda de preguntas de una en una con opciones y una recomendada; decide el alumno;
   - el change lo escribe Claude Code (modelo Opus) con esas decisiones;
   - lo aplica un subagente de Claude Code con el modelo Sonnet (`implementador`), u opencode;
   - Opus revisa el diff, las normas y `mvn test`;
   - las pruebas manuales las hace el alumno o, desde el último módulo, Claude Code con Computer use, y el alumno revisa el resultado;
   - archiva el subagente u opencode, y Opus comprueba la especificación requisito por requisito.
   - Tabla **quién hace qué** con esas filas.
   - La configuración de Claude Code (`.claude/`) es local y no se sube al repositorio. Los comandos de opencode sí están (`.opencode/`).
   - Nota corta: por qué se reparte entre modelos (el que decide y revisa es el más capaz; el que teclea es más barato).
5. **Diagrama del flujo completo de un change** (Mermaid `flowchart`):
   - idea → preguntas → propose (commit `docs(openspec): propuesta …`) → apply (commit `refactor(…)`) → revisión (diff, normas y `mvn test`) → pruebas manuales;
   - si algo falla, vuelta a `update` / apply con commits `fix(…)`;
   - si todo va bien, archive (commit `refactor(openspec): archivar …`) → `git push`.
   - Una rama lateral para los cambios sensibles: rama → changes dentro de la rama → pull request → `main`.
   - Debajo, los pasos numerados en una línea cada uno.
6. **Commits y ramas**:
   - un commit por paso, con los prefijos `docs`, `refactor`, `fix` y `chore`, y el alcance entre paréntesis;
   - el `git log` real de `modulo-copias` (los ocho commits) como ejemplo;
   - el push, al archivar, con el change entero terminado;
   - las ramas: `pdf-jasper` y VeriFactu, cada una con sus changes, que vuelven a `main` con un pull request en GitHub si salen bien; si no, se cierra el pull request y `main` no cambia.
7. **Ejemplo real: `modulo-copias`**. Qué se preguntó y qué se decidió: botón único, carpeta recordada, nombre con la empresa, comprobar contra el script, NIF y `Conexion` entera. Después:
   - el change escrito;
   - la implementación (465 pruebas);
   - la revisión, que encontró un fallo del propio diseño (la copia de rescate pisaba la carpeta recordada);
   - las pruebas manuales con Computer use, que encontraron dos fallos fuera del módulo;
   - el change ampliado con una sección 8 y las pruebas que tenían que fallar antes del arreglo;
   - el archivado (469 pruebas).
   - Cerrar con qué se aprende: el ciclo vuelve atrás y queda escrito.
8. **Historia del proyecto por fases**:
   1. primera versión con servicios y DAO;
   2. auditoría con IA: se queda la lección de que un modelo se inventó clases y hubo que comprobarlo todo en el código;
   3. reescritura módulo a módulo al estilo de Biblioteca8 (sin DAO, singletons con su SQL, clases de datos que se validan solas, sin `record`, streams ni ternarios, para que el código sea defendible a nivel de DAM);
   4. lo que viene: Jasper y VeriFactu.
   - Cifras reales: changes archivados (`ls openspec/changes/archive | wc -l`) y pruebas.
9. **Documentos de trabajo**: qué son `AGENTS.md` (normas), `ESTADO.md` (estado y trampas conocidas) y las «trampas» (lo que salió mal y no debe repetirse).

## D4. `docs/tecnico.md`: cómo está construido

1. **Arquitectura MVC como Biblioteca8**:
   - diagrama Mermaid: `AppCaboFactu` → `Controlador` → `Modelo` → negocio (singletons) → `Conexion` → SQLite, y `Vista` con las pantallas llamando a `Vista.getInstancia().getControlador()`;
   - tabla de capas con qué hace y qué no hace cada una;
   - las tres líneas de `AppCaboFactu.main`.
2. **Paquetes**: árbol de `src/main/java/cabofactu/` y de `src/main/resources/`, sacado del disco con las clases de hoy.
3. **Clases de datos, negocio y pantallas**: las reglas principales en tabla, con un enlace a `AGENTS.md`. Por ejemplo: el setter que valida, el singleton con su SQL, la transacción dentro del método y el formulario modal con `setRegistro` / `getRegistro`.
4. **Modelo de datos**:
   - las tablas de `src/main/resources/db/crear_tablas.sql` con sus campos principales, en un diagrama `erDiagram` o en tablas;
   - por qué la factura guarda una copia del cliente y cada línea una copia del IVA;
   - una base por empresa en `%APPDATA%\Facturacion\<empresa>\facturas.db`.
5. **Tests**: las tres capas y las dos de apariencia, con cifras. Qué se queda a mano y por qué.
6. **Decisiones técnicas**: la tabla de hoy, puesta al día.
   - Fuera: versiones, `Clock`, excepción de datos, capas negocio + DAO y `record`.
   - Dentro: singletons con SQL (como Biblioteca8), `Exception` con el mensaje para el usuario, sin hilos (cursor de espera), un único script de tablas, la copia comprobada contra el script, `VACUUM INTO` y la copia de rescate, instancia única, `BigDecimal` y una base por empresa.
7. **VeriFactu**: la tabla de situación al día, en pocas líneas, con un enlace a la rama futura.

## D5. `docs/flujos.md`: dos recorridos por el código

Formato de cada recorrido:

- un diagrama `sequenceDiagram` arriba;
- pasos numerados: **Clase** (enlazada) · `método()` y lo que hace en una o dos frases;
- al final, un recuadro con qué conceptos de clase aparecen: singleton, MVC, transacción, FXML con `initialize`, formulario modal…

Todos los métodos se sacan del código de hoy, no de memoria, y **cada nombre se comprueba con `grep`** antes de escribirlo.

**Recorrido 1: primer arranque** (sin ninguna empresa en `%APPDATA%\Facturacion`). Unos 12-15 pasos:

1. `AppCaboFactu.main`: idioma, crea `Modelo`, `Vista` y `Controlador`, y llama a `comenzar()`.
2. `Controlador.comenzar` → `Vista.comenzar` → `LanzadorVentanaPrincipal.comenzar`: arranca JavaFX, que llama a `start`.
3. `Controlador.prepararDatos`: `PreparacionDatos.crearCarpeta` y `InstanciaUnica.adquirir` (qué pasa si ya está abierta).
4. `PreparacionDatos.cargarDemoSiNoHayEmpresas` → `CargarDemo.cargar`: crea la empresa demo con `Empresas.alta` y `Conexion.crearBase` y ejecuta el script de la demo.
5. `Vista.prepararArranque` → `ArranqueController.initialize`: carga las empresas y elige la última recordada o la primera, y enseña el aviso de la demo.
6. El usuario pulsa Entrar: `ArranqueController.entrar` → `Controlador.abrirEmpresa` → `Empresas.abrir` (activa la carpeta, conecta, `Sesion.iniciar`, recuerda la empresa y el tema).
7. `Vista.mostrarInicio`: menú principal si la empresa está completa; si no, Configuración bloqueada.
8. `MenuPrincipalController.initialize`: nombre, NIF y logo de la empresa.
9. Al cerrar la ventana: `Controlador.terminar` (suelta el bloqueo y cierra la conexión).

Los pasos exactos y sus nombres se ajustan a lo que haya en el código.

**Recorrido 2: de la empresa abierta a la factura en PDF**. Unos 12-15 pasos:

1. Menú → «Nueva factura»: `Vista.mostrar("Editor.fxml")` y `EditorController.initialize` (series, IVA, retenciones, fecha de trabajo y número propuesto con `Controlador.siguienteCorrelativo` / `Series`).
2. Elegir el cliente: búsqueda con `listadoClientes` y copia de sus datos en la factura.
3. Escribir las líneas en la tabla editable: las celdas de `EditorController`, `LineaFactura` que se valida sola y los totales recalculados con `Calculos`.
4. Guardar: `EditorController.guardar` → `guardarNueva`, con la comprobación del cliente (ficha nueva o actualizada) → `Controlador.altaFactura` → `Modelo` → `Facturas.alta`.
5. Dentro de `Facturas.alta`: guardas, número con `Series`, transacción (`setAutoCommit(false)`, `INSERT` de la factura y de sus líneas, `commit` y `rollback`).
6. Exportar: `EditorController.exportarPdf` → elegir la ruta (o la carpeta automática) → `ExportadorPdf.exportar`.
7. **Hasta aquí**: una nota dice que lo que pasa dentro del paquete `pdf` no se detalla porque se va a sustituir por JasperReports en la rama `pdf-jasper`.

## D6. Capturas

Las hace la sesión principal con Computer use sobre la aplicación real, con la empresa demo y el tema **biblioteca8**:

- las siete capturas: `arranque.png`, `menu.png`, `editor.png` (una factura con varias líneas), `historico.png`, `clientes.png`, `copias.png` y `pdf.png` (la primera página del PDF de una factura de la demo, abierto en el visor);
- ventana en su tamaño normal, sin nada personal en pantalla;
- PNG de ancho parecido.

Al terminar se deja el tema como estaba y se borra el PDF de prueba si no está dentro del repositorio.

## D7. `AGENTS.md` y `openspec/config.yaml`

- **`AGENTS.md`, «Flujo de trabajo»**: los comandos, en opencode (`/opsx-propose` → `/opsx-apply` → `/opsx-archive`) o en Claude Code (`/opsx:propose` → `/opsx:apply` → `/opsx:archive`). En el mapa de la documentación, una fila para `docs/flujos.md`.
- **`openspec/config.yaml`, guía de `archive`**: la línea de `git add -A` pasa a «Añade al commit por rutas: `ESTADO.md`, `openspec/specs/`, la carpeta del change y su carpeta en `archive/`; git detecta el movimiento». La de `git status` vacío se queda.

## D8. Ortografía

- **`vista/ConfiguracionVentana.java`**: `Selección de empresa`, `Menú principal`, `Configuración` e `Histórico`. Comprobar con `grep` los tests que comparen esos títulos y ponerlos al día.
- **Comentarios y Javadoc** de `src/main/java` y `src/test/java`: poner las tildes a las palabras en español («conexión», «configuración», «aplicación», «número», «también», «después»…). **Nunca** a los nombres de clases, métodos, variables o claves (`Configuracion`, `conexion`, `ultima_carpeta_export`, la carpeta `Facturacion`), aunque salgan en un comentario.
  - Sobre todo en `Conexion`, `Configuracion`, `Series`, `Dialogos`, `MenuPrincipalController`, `Calculos`, `Botones`, `ArranqueController`, `Ventanas`, `LogoMarco`, `Formatos` y `PreferenciasGlobales`.
  - No se toca el paquete `pdf`, que irá con Jasper.
- Los textos que ve el usuario ya se revisaron uno a uno (1.652 textos): solo fallaban los cuatro títulos.

## D9. La demo trae logo

- **Imagen**: `logos/ChatGPT Image 2 sept 2026, 20_35_35.png` (fuera de git) reducida a **600 px de ancho**, manteniendo la proporción, y guardada como `src/main/resources/db/logo_demo.png`, junto a `seed_demo.sql`. Se reduce con un script de Python fuera del proyecto (PIL, `Image.LANCZOS`); en el repositorio solo entra la imagen.
- **`CargarDemo.cargar`**, después de ejecutar `seed_demo.sql`:
  1. copia el recurso a `logo.png`, dentro de la carpeta de datos de la demo (`Conexion.rutaBaseDe(CARPETA).getParent()`), reemplazándolo si existe;
  2. guarda en la empresa `logo_path` (la ruta absoluta de esa copia) y `cabecera_modo = 'LOGO'`, con un `UPDATE empresa ... WHERE id = 1` y `PreparedStatement`.
  - Si el recurso no está: `Exception` con `No se encontró el logo de la demostración dentro de la aplicación.`
- **`CargarDemoTest`**: tras cargar, la empresa tiene `cabecera_modo` `LOGO` y su `logo_path` apunta a un fichero que existe dentro de la carpeta de la demo; cargar dos veces deja un solo `logo.png`.

## D10. Excepciones

- **`AGENTS.md`, «Estilo del código»**: la línea «Solo `Exception`: nada de excepciones propias…» pasa a:
  > Por defecto, `Exception` con el mensaje tal como lo verá el usuario, sin prefijos. Una excepción propia solo cuando aporte algo (un `catch` que la trate aparte), en su propio fichero. Las de Java (`IllegalArgumentException`, `IllegalStateException`) solo para errores de programación que el usuario no puede provocar, como un `null` donde no debe. Todo lo que se escape lo recoge `ErroresInesperados`.
- **`CargarDemo`**: la `IllegalStateException` de `seed_demo.sql` pasa a `Exception("No se encontró seed_demo.sql dentro de la aplicación.")`.
- Se quedan como están:
  - las `IllegalArgumentException` de los constructores de `Controlador` y `Vista`, porque un `null` ahí es un error de programación;
  - la `RuntimeException` de `Vista.mostrar`, que salta con un FXML roto;
  - las del paquete `pdf`.

## D11. Errores inesperados

**`vista/utilidades/ErroresInesperados.java`**: herramienta `static` sin datos propios. Lleva un párrafo «Cómo funciona» que explica `Thread.setDefaultUncaughtExceptionHandler`: Java llama a ese método con cualquier excepción que se escape sin `catch`, también las de los botones de JavaFX.

- `public static void registrar()`: `Thread.setDefaultUncaughtExceptionHandler((hilo, error) -> tratar(error));`
- `public static void tratar(Throwable error)`:
  1. `guardar(error)`;
  2. si `Platform.isFxApplicationThread()`, `Dialogos.mostrarDialogoError("Error inesperado", texto)` con `String.format("Ha ocurrido un error inesperado:%n%s%n%nLa aplicación sigue abierta. Si se repite, revisa errores.log en la carpeta de datos.", mensaje)`. `mensaje` es `error.getMessage()` o, si es `null` o vacío, `error.getClass().getSimpleName()`;
  3. todo dentro de un `try / catch (Exception e)` que solo escribe en `System.err`, para que un fallo al avisar no provoque otro error.
- `public static void guardar(Throwable error)`: añade una entrada a `Conexion.carpetaRaiz().resolve("errores.log")`, creando el fichero si no existe (`StandardOpenOption.CREATE` y `APPEND`):
  ```
  2026-09-27 18:45:12  NullPointerException: <mensaje>
      <detalle completo, con StringWriter y printStackTrace(PrintWriter)>
  ```
  Si no se puede escribir, lo cuenta en `System.err` y sigue.
- **Dónde se registra**: en la primera línea de `LanzadorVentanaPrincipal.start`, antes de nada, para que los avisos ya se puedan mostrar.
- **`AppCaboFactu.main`**: sus cuatro líneas van dentro de un `try / catch (Exception e)` que llama a `ErroresInesperados.guardar(e)`, para los fallos antes de que exista la ventana.
- No se usa `Platform.runLater`: la aplicación no tiene hilos propios, así que los errores de pantalla llegan ya en el hilo de JavaFX.

**`ErroresInesperadosTest`** (nuevo):

- `guardar` crea `errores.log` en la carpeta de datos temporal, con la fecha, el tipo, el mensaje y una línea `at `;
- dos errores seguidos dejan dos entradas;
- `tratar`, fuera del hilo de JavaFX, guarda sin mostrar aviso;
- un error sin mensaje se apunta con su tipo.
- Si se puede hacer de forma fiable, una prueba de pantalla que registra el manejador, provoca un error dentro de `Platform.runLater` y comprueba el aviso «Error inesperado» con `textoAviso()`; al terminar, deja el manejador como estaba. Si en *headless* no es fiable, se apunta en la tarea y se queda como prueba manual.

## D12. Documentos y capturas al día

- **`docs/tecnico.md`**:
  - la fila «Solo `Exception`» de las decisiones pasa a la norma de D10;
  - fila nueva: «Manejador global de errores con aviso y `errores.log`», frente a «dejar que el error salga por consola»;
  - en paquetes: `ErroresInesperados` en `vista/utilidades` y `logo_demo.png` en `db/`.
- **`docs/flujos.md`**, primer arranque: el paso `ErroresInesperados.registrar()` al principio de `start`, y `CargarDemo` copiando el logo. En los conceptos, el manejador global.
- **`README.md`**: «Demostración» con logo, y una línea en «Lo que he aprendido» sobre el manejador global de errores.
- **Capturas**: se rehacen **las siete** siguiendo D6, con la demo recién cargada (con logo) y los títulos corregidos. Antes hay que borrar la carpeta de la demo de `%APPDATA%\Facturacion`, o recargarla, para que traiga el logo; el resto de empresas no se toca.

## Riesgos y renuncias

- **La documentación se queda vieja con la rama Jasper**: los flujos se paran antes del PDF por eso, y la rama pondrá al día `tecnico.md` y `flujos.md`.
- **Nombrar las herramientas de IA**: decidido por el alumno. Se cuenta lo que hace cada una y que las decisiones y la revisión son suyas.
- **Mermaid solo se ve bien en GitHub**: decidido así, porque se presenta desde GitHub.
