## Situación de partida

- **Qué hay en el código**:
  - 7 temas en `src/main/resources/cabofactu/vista/recursos/temas/`: `base.css` (522 líneas, la estructura común) y un `tema-*.css` por tema (unas 60 líneas, solo colores);
  - `GestorTemas` junta las dos hojas;
  - `ConfiguracionVentana` fija el tamaño de cada pantalla;
  - los iconos son `SVGPath` en los FXML;
  - `TemasTest` y `TextosCompletosTest` vigilan el aspecto.
- **Qué hay en la documentación**:
  - el `README.md` nombra los 7 temas;
  - `docs/tecnico.md` dice dónde están los CSS y en la tabla de pruebas menciona `TemasTest`;
  - `docs/metodologia.md` cita dos trampas de CSS;
  - no hay nada más.
- **Capturas**: las de `docs/capturas/` ya están hechas con la demo y el tema biblioteca8 (`documentacion-final`, tarea 6.1). No se rehacen.

## Objetivos y lo que queda fuera

**Objetivo**: que el profesor vea que el aspecto de la aplicación también está pensado y organizado. Tiene que ver por qué se ve así, cómo están hechos los temas y cómo se comprueba que ninguno se rompe. Todo lo que se cuente tiene que poder comprobarse en el repositorio.

**Fuera**:
- cambiar colores, temas o pantallas;
- capturas en otros temas;
- inventar el origen de los temas que no consta en ningún change;
- el paquete `pdf`, que se rehará con Jasper.

---

## D1. Decisiones tomadas con el alumno

| # | Tema | Decisión |
|---|---|---|
| 1 | Dónde | `docs/diseno.md` nuevo, enlazado desde el `README.md` (barra de enlaces y sección corta «Diseño») |
| 2 | Capturas | Solo del tema principal, biblioteca8: las que ya hay |
| 3 | Paletas | Una imagen con muestras de color (`docs/capturas/paletas.png`) y debajo una tabla con los códigos |
| 4 | Apartados | Idea y cómo nació; cómo funcionan los temas; paletas, letra y componentes; PDF y comprobaciones |
| 5 | Origen de los temas | Solo lo que consta:<br>• la estructura se inspira en Ajustes de Apple (`redesign-ui-apple`);<br>• biblioteca8 usa el azul `#296796` del proyecto Biblioteca8;<br>• de los demás temas, solo el carácter (claro u oscuro, cálido o frío) |
| 6 | Limpieza | En este change: `GestorTemas`, la tilde de `ConfiguracionVentana` y los ternarios de `Clientes` y `PreferenciasGlobales` |

**Reglas para el texto**: las mismas de `documentacion-final`.
- Español; primera persona del singular cuando habla el alumno e impersonal en lo técnico.
- Estilo sobrio: sin emojis en títulos ni tablas.
- Frases cortas, y tablas donde haya que comparar.
- Clases, métodos y ficheros entre comillas invertidas, con enlace relativo la primera vez que salen.
- Diagramas Mermaid válidos en GitHub.
- Índice arriba y enlaces a los otros documentos al principio y al final.
- **Antes de escribir un nombre, un tamaño o un color, se comprueba en el código** (`grep`).
- **Longitud orientativa**: unas 220 líneas.

## D2. `docs/diseno.md`

### 1. La idea

- **Por qué es sobria**: es una aplicación de facturación para trabajar muchas horas; lo que importa es que se lea bien y se encuentre cada cosa.
- **La estructura se inspira en Ajustes de Apple**: tarjetas, bordes redondeados, espacio entre bloques y jerarquía clara con tamaños de letra.
- **biblioteca8 es el tema por defecto**, con el azul de Biblioteca8.
- **Cada empresa guarda su propio tema** (`tema-por-empresa`), igual que el color de su PDF.
- **Cómo nació, por fases**: una tabla corta, con fecha, change (con enlace a su carpeta de `openspec/changes/archive/`) y una frase. Para escribir la frase se lee el `## Why` de cada uno:
  - `2026-08-20-temas-y-navegacion`
  - `2026-08-20-alineacion-menu-e-iconos`
  - `2026-08-31-redesign-ui-apple` y `2026-08-31-fix-ui-spacing`
  - `2026-09-02-logo-relleno-tema`
  - `2026-09-03-icono-app-y-titulos-ventana` y `2026-09-03-iconos-dialogos-aviso`
  - `2026-09-10-marca-en-pantalla-de-arranque`
  - `2026-09-11-iconos-normalizados` (junto con `escala-iconos-navegacion` e `iconos-en-barra-de-clientes`)
  - `2026-09-13-prompt-legible-temas-oscuros`
  - `2026-09-16-tema-por-empresa`
  - `2026-09-25-pruebas-de-pantalla` (con ella llegan `TemasTest` y `TextosCompletosTest`; comprobarlo con `git log --diff-filter=A` sobre los dos ficheros)
- **Si falta alguno**: si al buscar con `ls openspec/changes/archive | grep -i -E "tema|icono|ui|diseno|visual|marca|logo"` sale otro que trate del aspecto, se añade.

### 2. Cómo funcionan los temas

- **Dos hojas por pantalla**:
  - `base.css`, común, con tamaños, márgenes, bordes, sombras y las piezas;
  - `tema-<nombre>.css`, con los colores.
  - `GestorTemas.hojas()` devuelve las dos, en ese orden.
- **Las variables del `.root`**. Una tabla con cada variable y para qué sirve:
  - `-fx-background`, `-fx-base`, `-fx-accent`, `-fx-focus-color`, `-fx-faint-focus-color`;
  - `-fx-control-inner-background`, `-fx-text-background-color`, `-fx-accent-error`;
  - `-fx-boton-lavado`, que es nuestra.
  - Se comprueba en los CSS cuáles define cada tema.
- **Párrafo «Cómo funciona»**: el estilo de JavaFX (Modena) calcula los colores de todos sus controles a partir de unas pocas variables del `.root`. Si el tema cambia `-fx-base` o `-fx-accent`, los campos, los desplegables y las casillas se tiñen solos. Las piezas propias (barra de navegación, botones, tablas, totales, menú) llevan en cada tema su color concreto.
- **Cuándo se aplica**:
  - `Vista` pone las hojas al cargar cada pantalla y en las ventanas modales;
  - `Dialogos` las pone en los avisos;
  - en Configuración, al elegir un tema se ve en el momento (`GestorTemas.seleccionar`) y se guarda al pulsar Guardar (`GestorTemas.guardar`).
  - Se comprueba cada punto con `grep -rn "GestorTemas" src/main/java`.
- **Dónde se guarda**:
  - en la base de datos de cada empresa;
  - y además en las preferencias globales, porque la pantalla de arranque se enseña antes de abrir ninguna empresa (`Empresas.recordarTema`).
- **Diagrama Mermaid**: Configuración → `GestorTemas` → empresa y preferencias globales; y al abrir una pantalla: `Vista` → `GestorTemas.hojas()` → `base.css` + `tema-x.css`.
- **Cómo se añade un tema**, en pasos:
  1. copiar un `tema-*.css` y cambiarle los colores;
  2. añadir una línea en el bloque `static` de `GestorTemas` (clave y nombre visible);
  3. pasar `TemasTest`, que encuentra los ficheros solo por su nombre;
  4. mirarlo a mano en las pantallas y en el PDF.

### 3. Paletas

- La imagen `docs/capturas/paletas.png` (ver D3).
- Debajo, una tabla con, para cada tema: nombre, claro u oscuro, fondo (`-fx-background`), base (`-fx-base`), acento (`-fx-accent`), texto (`-fx-text-background-color`) y letra. Los códigos, en comillas invertidas y copiados de los CSS.
- Una frase por tema con su carácter, sacado solo de sus colores (por ejemplo, «oscuro, azul noche con acento lavanda»). Nada de su origen, salvo biblioteca8.

### 4. Letra y tamaños

- **Letra**:
  - Segoe UI, la de Windows, fijada en `base.css`;
  - omarchy usa Cascadia Mono (monoespaciada) y terracota usa Georgia (con serifa).
- **Tamaños**: una tabla sacada de `base.css` con cada tamaño usado y qué lo usa (título de pantalla, nombre de empresa del menú, textos de ayuda…), de mayor a menor.

### 5. Piezas de las pantallas

- Apoyado en las capturas que ya hay (`menu.png` y `editor.png`, enlazadas; sin capturas nuevas).
- **Tabla**: pieza, clase CSS y dónde sale. Se comprueban las clases en `base.css` y en los FXML.
  - barra de navegación (`nav-bar`, `nav-button`, `activo`);
  - barra de acciones del editor (`action-bar`, botones con el icono encima del texto);
  - los tres botones (`primary-button`, `default-button`, `action-button`) y cuándo se usa cada uno;
  - tarjetas y paneles (`card`, `surface`, `panel-neutro`);
  - tablas (filas alternas, al pasar el ratón, seleccionada);
  - totales (`totales`, `total-grande`);
  - opciones del menú principal (`opcion-menu`);
  - campos con un dato mal (borde de aviso);
  - diálogos.
- **Iconos**:
  - dibujos vectoriales (`SVGPath`) dentro del FXML, sin ficheros de imagen;
  - cogen el color del tema con `-fx-fill`;
  - van en una caja de tamaño fijo para que los textos queden alineados (`iconos-normalizados`).
  - El icono de la aplicación (`icono-aplicacion.png`) y los títulos «CaboFactu® + pantalla».
- **Ventanas**:
  - tabla sacada de `ConfiguracionVentana`, con pantalla, tamaño inicial, mínimo y si se puede agrandar;
  - una frase: 1024×768 es el mínimo con el que se prueba que ningún texto se corta.

### 6. El PDF

- Cabecera de texto o con logo: en el modo logo, el logo va a la izquierda y los datos de la empresa a la derecha.
- Color de acento elegido por cada empresa (`color_pdf`) y pie legal.
- Letra: Calibri, la de Windows, con Helvetica si no está (`EstiloPdf`).
- Enlace a `capturas/pdf.png`.
- Una frase: el PDF se rehará con JasperReports en la rama `pdf-jasper`.

### 7. Cómo se vigila el aspecto

- **`TemasTest`**: cada `tema-*.css` define su paleta y cada variable se resuelve.
- **`TextosCompletosTest`**: en cada pantalla a 1024×768, incluidas las seis secciones de Configuración, ningún texto se corta ni sale con «…».
- **A mano**: si una pantalla queda bien y el aspecto del PDF (ya lo dice `AGENTS.md`).
- **Errores de diseño ya corregidos**: una lista corta sacada de «Trampas conocidas» de `ESTADO.md`, contada como aprendizaje:
  - colores fijos en `base.css` que dejaban ilegibles los temas oscuros;
  - una clase propia con nombre de JavaFX (`menu-item`);
  - el punto de `.root` borrado con el BOM;
  - el CSS manda sobre el FXML;
  - el texto de ayuda ilegible en temas oscuros.

## D3. `docs/capturas/paletas.png`

- **La hace la sesión principal**, con un script de Python fuera del proyecto que lee el `.root` de los siete CSS. Nada del script queda en el repositorio.
- **Forma**:
  - fondo blanco;
  - una fila por tema, en el orden de `GestorTemas`;
  - el nombre visible del tema a la izquierda;
  - cuatro muestras (fondo, base, acento y texto) con su código debajo;
  - encima de las columnas, sus nombres.
- **Tamaño**: unos 1000 px de ancho, letra Segoe UI y menos de 100 KB.

## D4. Código

Sin cambiar lo que hace la aplicación.

- **`GestorTemas`**:
  - `css()`: el ternario pasa a `if / else`;
  - `guardar()` declara `throws Exception` y quita el `catch` vacío. `ConfiguracionController.guardar` ya lo llama dentro de su `try` y enseña el error. La preferencia global se guarda después de la empresa;
  - Javadoc corto en `temas()`, `etiqueta()`, `temaActivo()`, `aplicar()`, `seleccionar()` y `hojas()`;
  - Javadoc de la clase: cada tema es un CSS con sus colores que se aplica junto a `base.css`; cada empresa guarda su tema, y lo copiamos a las preferencias globales para que la pantalla de arranque salga con el último.
- **`ConfiguracionVentana`**: el Javadoc de la clase con tilde. Se revisan las tildes del resto de comentarios del fichero.
- **`Clientes.listado`**: `(texto == null ? "" : texto.trim())` pasa a una variable con `if / else`.
- **`PreferenciasGlobales.get(clave, porDefecto)`**: `if / else`.
- **Comprobación**: `grep -rnE "\? [^?]*: " src/main/java --include=*.java`, quitando los `?` de SQL, solo devuelve ficheros del paquete `pdf`.

## D5. Enlaces y normas

- **`README.md`**:
  - «Diseño» en la barra de enlaces de arriba;
  - sección «Diseño» después de «Capturas»: dos o tres frases (estructura inspirada en Ajustes de Apple, 7 temas y un tema por empresa), la imagen `paletas.png` y el enlace;
  - la cifra de pruebas de «Lo que he aprendido», la real después de `mvn test` (hoy dice 469).
- **`docs/tecnico.md`**: donde habla de `temas/`, una frase que remite a `diseno.md`.
- **Los cinco documentos**: `diseno.md` en la línea de enlaces de cabecera y pie.
- **`AGENTS.md`**: fila nueva en el mapa de la documentación: `docs/diseno.md` | Temas, paletas, piezas de las pantallas y cómo se añade un tema | Si tocas CSS, FXML o el aspecto de una pantalla.
- **`openspec/config.yaml`**: la guía de `archive` que contiene «rutas: », entre comillas dobles. Se comprueba con `openspec status --change documentacion-diseno`, que no debe dar el aviso «Guidance for operation 'archive'…».
- **`ESTADO.md`**:
  - «En curso» al aplicar;
  - al archivar, la trampa: «Una guía de `openspec/config.yaml` con `: ` en medio se lee como un par clave-valor y OpenSpec ignora todas las de esa operación: van entre comillas».

## Riesgos y renuncias

- **Un documento de diseño se queda viejo en cuanto cambia un CSS.** Para que dure:
  - los colores van solo en la tabla y en la imagen;
  - los tamaños y las clases, en una tabla que se puede comprobar con `grep`;
  - la regla del mapa de `AGENTS.md` avisa de actualizarlo.
- **`guardar()` con `throws`**: si falla guardar el tema, el usuario verá el aviso de Configuración en vez de nada. Es lo que pide la norma.
