## Situación de partida

- **Qué hay en el código**:
  - 7 temas en `src/main/resources/cabofactu/vista/recursos/temas/`: `base.css` (la estructura común) y un `tema-*.css` por tema (solo colores);
  - `GestorTemas` junta las dos hojas;
  - los iconos son `SVGPath` en los FXML;
  - `TemasTest` y `TextosCompletosTest` vigilan el aspecto.
- **Qué hay en la documentación**: el `README.md` nombra los 7 temas y `docs/metodologia.md` cita dos trampas de CSS. Nada cuenta **cómo se diseñó** la aplicación.
- **Cómo se diseñó**, reconstruido con el historial de opencode, `openspec/changes/archive/` y la carpeta `prototipos/`:
  - 41 maquetas HTML entre el 20/08 y el 07/09;
  - el alumno eligió, corrigió y descartó en cada ronda, y solo lo aprobado se programó.
  - `prototipos/` está en `.gitignore` porque las maquetas llevan el nombre, el logo y facturas reales de la empresa.
- **Capturas de pantalla**: las de `docs/capturas/` ya están hechas con biblioteca8 (`documentacion-final`, tarea 6.1) y no se rehacen.

## Objetivos y lo que queda fuera

**Objetivo**: que el profesor vea que el aspecto de la aplicación está diseñado y no improvisado. Tiene que ver:
- el método (maquetas, decide el alumno, se programa lo aprobado);
- las rondas con lo que se eligió y lo que se descartó;
- por qué los iconos son SVG;
- en corto, cómo están hechos los temas y cómo se vigila que no se rompan.

**Fuera**:
- subir los prototipos al repositorio;
- cambiar colores, temas o pantallas;
- contar herramientas que no dejan rastro comprobable;
- el paquete `pdf`, que se rehará con Jasper.

---

## D1. Decisiones tomadas con el alumno

| # | Tema | Decisión |
|---|---|---|
| 1 | Dónde | `docs/diseno.md` nuevo, enlazado desde el `README.md` (barra de enlaces y sección corta «Diseño») |
| 2 | Estructura | **Proceso primero**: método, rondas de maquetas, iconos; al final, el resultado en corto (paletas, temas y comprobaciones) |
| 3 | Maquetas | Siete imágenes anonimizadas de los prototipos, en `docs/capturas/proceso-*.png`. Revisadas y aprobadas por el alumno |
| 4 | Capturas de la aplicación | Las que ya hay (tema biblioteca8) |
| 5 | Paletas | Imagen con muestras (`docs/capturas/paletas.png`) y tabla con los códigos |
| 6 | Herramientas | Como consta en el historial, con fechas:<br>• `javafx-design`, skill propia creada el 20/08 a imitación del flujo de Claude Design;<br>• `apple-design`, de Emil Kowalski, aplicada el 24/08;<br>• el editor definitivo se diseñó en Claude Design.<br>Nada que no se pueda comprobar |
| 7 | Limpieza | En este change: `GestorTemas`, la tilde de `ConfiguracionVentana`, los ternarios de `Clientes` y `PreferenciasGlobales`, y «Neon» → «Neón» |
| 8 | Logo de la aplicación | Apartado nuevo con la lámina `proceso-logo.png`. Contado como lo explica el alumno: la F es de Facturación, minimalista para que sea reconocible y moderna, y sus colores se inspiran en el mar, el sol y la playa del Cabo de Gata, de donde sale «CaboFactu». La torre de la demo no se explica |

**Reglas para el texto**: las de `documentacion-final`.
- Español; primera persona del singular cuando habla el alumno («elegí», «descarté») e impersonal en lo técnico.
- Sobrio: sin emojis en títulos ni tablas.
- Frases cortas y tablas.
- Clases y ficheros entre comillas invertidas, con enlace relativo la primera vez.
- Mermaid válido en GitHub.
- Índice arriba y enlaces a los otros documentos al principio y al final.
- **Longitud orientativa**: unas 250 líneas.
- **Todo lo que se afirme sale de este design o se comprueba en el repositorio**:
  - no se inventa ningún motivo de una elección que no esté aquí;
  - de los prototipos no se nombra la empresa real ni se copia su contenido;
  - los nombres de fichero de las maquetas sí se pueden citar.

## D2. `docs/diseno.md`

### 1. Cómo se trabajó el diseño

- **El método**:
  1. antes de tocar una pantalla, se hacen maquetas en HTML (un fichero por propuesta, se abre con doble clic en el navegador);
  2. el alumno las compara, pide cambios y descarta;
  3. la maqueta aprobada se convierte en un change de OpenSpec que la cita;
  4. se programa en JavaFX (CSS y FXML);
  5. se compara con la maqueta.
  - **Diagrama Mermaid** del ciclo: maqueta → revisión del alumno → (cambios → maqueta) → aprobada → change → implementar → comparar con la maqueta.
- **Las cifras**:
  - 41 maquetas entre el 20/08 y el 07/09;
  - la del menú elegido pasó por 29 versiones en una tarde.
- **Por qué no están en el repositorio**: las maquetas usan el nombre, el logo y facturas reales de la empresa para la que se hizo la aplicación. `prototipos/` está en `.gitignore`, y las imágenes de este documento están hechas con los datos de la demo.
- **Changes que citan su maqueta**: una tabla con change y maqueta, sacada de `grep -rhoE "prototipos/[A-Za-z0-9_./-]+" openspec/changes/archive`, para que se vea la relación maqueta → change:
  - `alineacion-menu-e-iconos`;
  - `redesign-pdf-factura`;
  - `pdf-fidelidad-prototipo`;
  - `fix-pdf-totales-tarjeta-pago`;
  - `facturacion-mensual-cliente`;
  - `desglose-totales-matriz-suplidos`;
  - `pdf-desglose-rejilla-liquidacion`;
  - `pdf-cierre-anclado-al-pie`;
  - `pdf-marco-relleno-y-pie-final`;
  - `pdf-tabla-siempre-visible-y-tarjetas`.
- **Las herramientas**:
  - **`javafx-design`**, una skill propia creada el 20/08. Imita el flujo de Claude Design: maquetas HTML primero, código después. Vive fuera del proyecto (`~/.config/opencode/skills/`). Sus reglas:
    - empezar por las pantallas reales (los FXML), no por la inspiración;
    - los colores como variables, una paleta por propuesta sobre la misma estructura;
    - cada maqueta, un HTML autocontenido con datos realistas (importes `1.250,50 €`, fechas `11/08/2026`);
    - iterar con el usuario y programar solo lo aprobado;
    - las maquetas en `prototipos/`, sin dejar documentación ni rastro de IA en el proyecto;
    - las paletas de Omarchy y de Biblioteca8 ya apuntadas como referencia.
  - **`apple-design`**, de Emil Kowalski ([github.com/emilkowalski/skills](https://github.com/emilkowalski/skills)). El 24/08 le pedí aplicarla para rediseñar la aplicación, y de ahí salió el change `redesign-ui-apple` (31/08): tarjetas, bordes redondeados, espacio y jerarquía, con las mismas paletas.
  - **Claude Design**: el editor definitivo, después de descartar las cuatro variantes de la ronda 6.
- **Quién decide**: una frase. La IA propone y hace las maquetas; el alumno elige, corrige y descarta. Los ejemplos están en las rondas.

### 2. Las rondas

Cada ronda con fecha, qué se propuso, qué elegí y qué descarté, y su imagen. **Solo estos datos**:

1. **Temas (20/08)**. `proceso-temas.png`.
   - 7 propuestas, cada una con las 7 pantallas (49 pantallas). Cada una cambiaba a la vez paleta, sitio del menú, letra y forma, para poder combinar «los colores de una y la distribución de otra».
   - Tabla:

     | Propuesta | Paleta | Menú | Letra |
     |---|---|---|---|
     | 1 Omarchy | oscuro, azul noche y lavanda | barra lateral | monoespaciada |
     | 2 Biblioteca8 | azul y gris | iconos arriba | Segoe UI |
     | 3 Esmeralda | blanco y verde | tarjetas grandes | Inter |
     | 4 Terracota | crema y terracota | cabecera y menú centrado | Georgia |
     | 5 Negro y dorado | negro y dorado | pestañas arriba | Playfair Display |
     | 6 Sakura | rosa pastel | botones redondeados a la derecha | Segoe UI |
     | 7 Neón | oscuro, violeta y cian | barra abajo | Segoe UI |

   - Elegí la 2, Biblioteca8. Las otras seis se quedaron como temas de color que se pueden elegir en Configuración: de aquí salen los 7 temas de la aplicación.
2. **La elegida, afinada (20/08)**. `proceso-final.png`.
   - `tema-02-final.html`, 29 versiones.
   - Lo que pedí:
     - el logo cuatro veces más grande en el menú y el doble en el editor;
     - sin enlaces repetidos en el menú principal;
     - «Salir» en todas las pantallas, con confirmación;
     - todos los iconos de un mismo color (azul en el menú, blancos sobre la barra azul);
     - la cara feliz de «Clientes» sustituida por la silueta de persona;
     - un engranaje más relleno;
     - un texto de ayuda al pasar el ratón por cada icono;
     - los botones de la barra de acciones en gris;
     - el logo a la izquierda con la lista de opciones a la derecha.
3. **Menú e iconos (20/08)**. `proceso-menu.png` y `proceso-iconos.png`, de `ajustes-menu-iconos.html`.
   - Alineación del menú: tres variantes (A columnas alineadas arriba, B apilado vertical centrado, C cabecera de empresa). Elegí la A.
   - Icono del Histórico: la lupa de entonces y tres opciones. La recomendada era el reloj con flecha; **elegí la 3, la lista de documentos**.
   - Icono de la copia de seguridad: varias opciones (disquete, caja fuerte con dial, con candado, escudo). Se eligió el disquete y después pedí una flecha. El icono de hoy se comprueba en `MenuPrincipal.fxml` y en `git log -S` antes de describirlo.
   - Ese mismo día, «al ejecutar la aplicación no se ha puesto el logo donde hemos dicho»: de aquí sale la costumbre de comparar la aplicación con la maqueta.
4. **El PDF (21–22/08)**. `proceso-pdf.png`.
   - Cinco propuestas; me gustó la 3 (moderno a dos columnas).
   - Pedí un color arena claro, configurable desde la aplicación, y salieron cinco propuestas en arena. Después, dos alternativas.
   - La final, `pdf-final-clasica-tarjetas`, parte de la alternativa «hoja clásica», sin datos repetidos (fecha y número solo arriba a la derecha, con serie y número).
   - Con `pdf-fix-v2` como modelo, el change `pdf-fidelidad-prototipo` ajustó espaciados, bordes redondeados, letra y colores hasta que el PDF fue igual que la maqueta.
   - Enlace a `capturas/pdf.png` como resultado de hoy.
5. **La estructura, al estilo Apple (24–31/08)**. Sin imagen.
   - `apple-design` → `redesign-ui-apple` y `fix-ui-spacing`.
   - Maquetas `02-editor.html`, `04-configuracion.html` y `generar-facturas-mensuales.html` (31/08).
6. **El editor a 1024×768 (01/09)**. `proceso-editor.png`.
   - Cuatro variantes para que la factura cupiera sin barra de desplazamiento: A columna única densa, B tarjetas separadas, C conservador compactado y D franja de totales.
   - **Las descarté todas** y el editor definitivo lo hice en Claude Design.
   - Enlace a `capturas/editor.png`.
7. **Los totales del PDF (06–07/09)**. `proceso-totales.png`.
   - `totales-desglose.html`, después «Diez maneras de cerrar la factura» (`pdf-totales-r1-diez-propuestas.html`) y dos rondas más (`r2-variantes`, `r3-rejilla`), hasta `pdf-totales-rejilla-hermanas.html` → change `pdf-desglose-rejilla-liquidacion`.
   - El 07/09 hubo cuatro maquetas más para el final de la hoja: `pdf-cierre-anclado-al-pie`, `pdf-multipagina-cliente-repetido`, `pdf-marco-y-pie-final` y `pdf-cabecera-y-tarjetas`.
- **Al final del apartado**, una línea: hubo otras maquetas pequeñas, como `colores-historico-clientes.html` (03/09), que salió en `ajuste-colores-historico-clientes-editor`.

### 2 bis. El logo de la aplicación

Va entre «Las rondas» y «Los iconos», con la imagen `proceso-logo.png`. En el índice, como apartado propio.

- **Qué es**: una F de tres franjas, dos azules y una dorada. **La F es de Facturación**. Es un diseño minimalista, para que la aplicación sea reconocible y moderna.
- **Por qué esos colores y ese nombre**: se inspiran en el mar, el sol y la playa del **Cabo de Gata**, y de ahí sale el nombre **CaboFactu**.
- **Cómo se hizo**, con fechas:
  1. 03/09 por la mañana: pruebas con ChatGPT (varias versiones de la F y la F grande);
  2. `logo1.png`, la elegida, con degradados y un brillo en el borde para que se vea bien sobre fondos oscuros;
  3. esa tarde, en una sesión de Claude Code, pregunté qué ventajas tenía el SVG. Se recreó a mano como `logo1.svg`: tres trazados con degradados, unos 2,5 KB frente a los casi 600 KB del PNG, nítido a cualquier tamaño;
  4. **me quedé con el PNG para el icono**:
     - el SVG pierde el brillo del borde, y sobre un fondo oscuro se nota (imagen, casillas 5 y 6);
     - además, Windows y JavaFX solo aceptan una imagen como icono de ventana (`Stage.getIcons()`), no un SVG;
  5. el icono se redujo desde `logo1.png` a 256×256 con LANCZOS, el remuestreo que mejor conserva la calidad al reducir, y es `icono-aplicacion.png`;
  6. change `icono-app-y-titulos-ventana` (03/09): el icono en todas las ventanas y el título «CaboFactu® + pantalla».
- **Dónde está**:
  - `logo1.png` y `logo1.svg` son los originales, en la carpeta `logos/`, fuera de git como los prototipos;
  - la aplicación usa `src/main/resources/cabofactu/vista/recursos/imagenes/icono-aplicacion.png`, que es también el icono de la cabecera del `README.md`.
- **La torre del logo de la demo**: una frase. El logo de la empresa de demostración (`logo_demo.png`) es otro dibujo, hecho también con ChatGPT el 02/09, que sirve para ver cómo queda una empresa con logo en las pantallas y en el PDF. No se explica qué representa.

### 3. Los iconos

- **Cómo se llegó a SVG**, con `proceso-final.png`: primero emojis de colores; luego símbolos Unicode (✎ ⌕ ☺ ⚙) de un solo color; al final, dibujos SVG.
- **Por qué SVG**:
  - los emojis traen su propio color y no se pueden pintar con el del tema;
  - los símbolos Unicode dependen de la fuente de cada equipo y se ven distintos;
  - un SVG es un trazado: toma cualquier color, escala sin pixelarse y no necesita ficheros de imagen.
- **De dónde salen**: los trazados son los de los iconos de Material Design de Google, en una rejilla de 24×24 y con licencia libre (Apache 2.0). Por ejemplo, `history`, `settings`, `save_alt`, `info` o `warning`.
- **Cómo están en el código**:
  - un `SVGPath` dentro del FXML, con su trazado en `content`. Se copian dos o tres líneas reales de `MenuPrincipal.fxml`;
  - el color, desde el tema: `.opcion-menu .icono { -fx-fill: ... }` en cada `tema-*.css`. Por eso los iconos cambian de color con el tema;
  - en la barra de navegación, cada icono va en una caja fija de 26×26 (`BarraNavegacion.fxml`) para que los siete textos queden a la misma altura (`iconos-normalizados`, `escala-iconos-navegacion`);
  - los diálogos llevan también su icono SVG (`iconos-dialogos-aviso`).
- **La excepción**: el icono de la aplicación (`icono-aplicacion.png`) es una imagen, porque Windows lo pide así para la barra de tareas. Se remite al apartado del logo.

### 4. El resultado

- **Paletas**: la imagen `paletas.png` y una tabla con, para cada tema, nombre, claro u oscuro, fondo, base, acento y texto (códigos copiados de los CSS) y letra (Segoe UI salvo omarchy, Cascadia Mono, y terracota, Georgia). Cada empresa guarda su propio tema (`tema-por-empresa`).
- **Cómo funcionan los temas**, corto:
  - dos hojas por pantalla, `base.css` (estructura) y `tema-<nombre>.css` (colores), juntadas por `GestorTemas.hojas()`;
  - un párrafo «Cómo funciona»: el estilo de JavaFX calcula los colores de sus controles a partir de unas pocas variables del `.root` (`-fx-base`, `-fx-accent`, `-fx-background`…), así que al cambiarlas se tiñen solos; las piezas propias llevan su color en cada tema;
  - dónde se guarda: en la empresa y en las preferencias globales, para la pantalla de arranque;
  - **cómo se añade un tema** en cuatro pasos:
    1. copiar un CSS y cambiarle los colores;
    2. una línea en `GestorTemas`;
    3. `TemasTest`;
    4. mirarlo a mano.
- **Cómo se vigila**:
  - `TemasTest`: cada tema define su paleta;
  - `TextosCompletosTest`: a 1024×768 ningún texto se corta;
  - a mano, el aspecto de cada pantalla y del PDF;
  - una lista corta de errores de diseño ya corregidos, sacada de «Trampas conocidas» de `ESTADO.md`: colores fijos en `base.css`, la clase `menu-item`, el punto de `.root` y el texto de ayuda ilegible en los temas oscuros.

## D3. Imágenes

- **`docs/capturas/paletas.png`**, ya hecha: los siete temas con fondo, base, acento y texto.
- **`docs/capturas/proceso-*.png`**, las siete de las rondas, hechas por la sesión principal:
  - `temas`, `final`, `menu`, `iconos`, `pdf`, `editor` y `totales`;
  - maquetas recuperadas del historial de opencode, copiadas fuera del proyecto, con los datos reales cambiados por los de la demo (empresa, NIF, correos, dirección, cliente) y el logo de la demo;
  - capturadas con Edge en modo headless;
  - revisadas por el alumno;
  - cada una de menos de 250 KB.
- **`docs/capturas/proceso-logo.png`**, hecha por la sesión principal con los originales de `logos/`: pruebas de ChatGPT, `logo1.png`, `logo1.svg`, el SVG y el PNG sobre fondo oscuro, y el icono en 16, 32, 64 y 96 px.
- Nada del proceso (scripts, maquetas recuperadas) queda en el repositorio.

## D4. Código

Sin cambiar lo que hace la aplicación.

- **`GestorTemas`**:
  - `css()`: el ternario pasa a `if / else`;
  - `guardar()` declara `throws Exception` y pierde el `catch` vacío. `ConfiguracionController.guardar` ya lo llama dentro de su `try` y enseña el error. La preferencia global se guarda después de la empresa;
  - Javadoc corto en `temas()`, `etiqueta()`, `temaActivo()`, `aplicar()`, `seleccionar()` y `hojas()`;
  - Javadoc de la clase: cada tema es un CSS con sus colores que se aplica junto a `base.css`; cada empresa guarda su tema, y lo copiamos a las preferencias globales para que la pantalla de arranque salga con el último;
  - el nombre visible del tema `neon` pasa a «Neón». La clave guardada sigue siendo `neon`. Se busca `"Neon"` en los tests.
- **`ConfiguracionVentana`**: el Javadoc de la clase con tilde, y se revisan las tildes del resto de comentarios del fichero.
- **`Clientes.listado`**: `(texto == null ? "" : texto.trim())` pasa a una variable con `if / else`.
- **`PreferenciasGlobales.get(clave, porDefecto)`**: `if / else`.
- **Los otros once ternarios fuera de `pdf`**, a `if / else` sin cambiar lo que hacen (salieron al aplicar, porque el primer `grep` estaba mal hecho):
  - `utilidades/Formatos.java` (dos: fecha y fecha con hora);
  - `utilidades/LogoMarco.java` (dos: ancho y alto);
  - `vista/controlador/ConfiguracionController.java` (dos: al leer un campo y un texto que puede ser `null`);
  - `vista/utilidades/PreviaCabecera.java` (cinco: el color de acento por defecto, ancho, alto, la negrita y un texto que puede ser `null`).
  - Si una misma forma se repite (`s == null ? "" : s`), puede quedar en un método privado de ayuda con nombre, en su clase.
- **Comprobación**: `grep -rnE "[^?]\? [^?]*[^:]: " src/main/java --include=*.java | grep -vE "LIKE \?|= \?|\?,|\?\)"` solo devuelve ficheros del paquete `pdf`.

## D5. Enlaces y normas

- **`README.md`**:
  - «Diseño» en la barra de enlaces de arriba;
  - sección «Diseño» después de «Capturas»: dos o tres frases (maquetas antes de programar, 41 maquetas, 7 temas y la estructura inspirada en Ajustes de Apple), la imagen `proceso-temas.png` y el enlace;
  - en «Apariencia», «Neón»;
  - la cifra de pruebas de «Lo que he aprendido», la real después de `mvn test` (hoy dice 469).
- **`docs/metodologia.md`**: en la historia por fases, donde salga el rediseño, una frase que remita a `diseno.md`.
- **`docs/tecnico.md`**: donde habla de `temas/`, una frase que remita a `diseno.md`.
- **Los cinco documentos**: `diseno.md` en la línea de enlaces de cabecera y pie.
- **`AGENTS.md`**: fila en el mapa de la documentación:
  - `docs/diseno.md` | Cómo se diseñó (maquetas), los iconos SVG y los temas | Si tocas CSS, FXML, iconos o el aspecto de una pantalla.
- **`openspec/config.yaml`**: la guía de `archive` que contiene «rutas: », entre comillas dobles. `openspec status --change documentacion-diseno` no debe dar el aviso «Guidance for operation 'archive'…».
- **`ESTADO.md`**:
  - «En curso» al aplicar;
  - al archivar, la trampa: «Una guía de `openspec/config.yaml` con `: ` en medio se lee como un par clave-valor y OpenSpec ignora todas las de esa operación: van entre comillas».

## D6. El resto de `AGENTS.md`, en todo `src/main`

Al revisar la 2.3 salió un `var` que no buscaba nadie. Una auditoría completa de `src/main` (sin `pdf`), hecha por la sesión principal, encontró cuatro ficheros más que no cumplen. Se arreglan aquí, sin cambiar lo que hace la aplicación:

- **`utilidades/LogoMarco`**:
  - tres tipos que viven dentro de la clase pasan a su propio fichero en `utilidades`:
    - `enum Tipo` → `TipoFondoLogo` (`PLANO`, `DIFUMINADO`, `TRANSPARENTE`);
    - `class Resultado` → `FondoLogo`, con campos privados, constructor y `getTipo()` / `getColor()` en vez de campos públicos;
    - `class Muestras` → `MuestrasMarco`, clase del paquete (sin `public`);
  - `var muestras` → `MuestrasMarco muestras`;
  - `cubos.computeIfAbsent(cubo, k -> new long[4])` → `get`, `if (acc == null)` y `put`;
  - `removeIf(n -> ...)` en `limpiar` → un bucle que recoge los nodos marcados y los quita después;
  - `LogoMarcoTest` se adapta a los nombres nuevos (`FondoLogo`, `TipoFondoLogo`, `getTipo()`, `getColor()`) sin cambiar lo que comprueba.
- **`vista/controlador/ArranqueController.restringirAlEjercicio`**:
  - las dos clases anónimas (`Callback` y `DateCell`), escritas con nombres completos, pasan a una clase con nombre, `vista/utilidades/CeldaFechaEjercicio extends DateCell`;
  - el constructor recibe el ejercicio y `updateItem` desactiva los días de otro año;
  - en el controlador queda `fechaTrabajo.setDayCellFactory(calendario -> new CeldaFechaEjercicio(ejercicio));`;
  - `CeldaFechaEjercicio` lleva el párrafo «Cómo funciona» (qué es una celda y cuándo llama JavaFX a `updateItem`).
- **`vista/controlador/ConfiguracionController.cargarTema`**: el cuerpo del `addListener` (cinco líneas) pasa a un método privado `cambiarTema(String nombre)`, y la lambda solo lo llama.
- **`vista/Ventanas`**: `try (var in = ...)` → `try (InputStream in = ...)`.
- **`utilidades/Formatos.fechaHora`**: `java.time.LocalDateTime` con su `import`.
- **Excepción que se queda**: las celdas dentro de `EditorController`, como dice `AGENTS.md`.

**Para que no vuelva a pasar**, en «Transición» de `AGENTS.md`:
- la búsqueda de siempre se hace sobre los ficheros tocados y además, **antes de archivar, sobre todo `src/main` salvo `pdf`**;
- se añaden los tipos dentro de otra clase y las lambdas de más de una llamada;
- los comandos, en un bloque:
  ```bash
  F=$(find src/main/java/cabofactu -name "*.java" -not -path "*/pdf/*")
  grep -nE "var [a-zA-Z]|record [A-Z]|\.stream\(\)|[A-Za-z)]::[a-z]" $F
  grep -nE "[^?]\? [^?]*[^:]: " $F | grep -vE "LIKE \?|= \?|\?,|\?\)"
  grep -nE "^\s+(public |private |protected |static |final |abstract )*(class|enum|interface|record) [A-Z]" $F
  grep -nE "new [A-Z][A-Za-z<>]*\([^;]*\)\s*\{|removeIf|computeIfAbsent|->\s*\{" $F
  ```
- lo que salga, o se arregla o es una excepción escrita en `AGENTS.md`: las celdas del editor, `case X -> {`, `jdbc:sqlite::memory:` y un `new File(...)` dentro de una condición (`Conexion`). El comentario `// clave cubo -> {...}` de `LogoMarco` se reescribe sin `->` para que no salga.

## Riesgos y renuncias

- **Contar el proceso sin enseñar los prototipos**: las imágenes anonimizadas lo cubren. Los prototipos originales siguen fuera de git.
- **La memoria del proceso sale del historial de opencode**, que es local. Lo que no está ahí (por ejemplo, otras herramientas usadas en claude.ai) no se cuenta.
- **Un documento de diseño se queda viejo en cuanto cambia un CSS**: los colores solo están en la tabla y en la imagen, y la fila del mapa de `AGENTS.md` avisa de actualizarlo.
