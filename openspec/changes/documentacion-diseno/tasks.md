> Todo el contenido y su orden está en `design.md`. Antes de escribir un nombre de clase, método, fichero, tamaño o color, se comprueba en el repositorio (`grep`, `ls`, `git log`). De las maquetas, solo lo que dice `design.md - D2`: no se inventan motivos. Estilo sobrio: sin emojis en títulos ni tablas. Commits **sin líneas de coautoría**.

## 1. Configuración y estado

- [x] 1.1 `openspec/config.yaml`: la guía de `archive` con «rutas: », entre comillas dobles. `openspec status --change documentacion-diseno` ya no da el aviso «Guidance for operation 'archive'…». Ver `design.md - D5`.
- [x] 1.2 Añadir este change a «En curso» en `ESTADO.md`.

## 2. Código

- [x] 2.1 `GestorTemas` según `design.md - D4`: sin ternario, `guardar()` con `throws Exception` y sin el `catch` vacío, Javadoc de la clase y de los públicos, y «Neón».
- [x] 2.2 Tildes del Javadoc de `ConfiguracionVentana`, y los ternarios de `Clientes.listado` y `PreferenciasGlobales.get`, según `design.md - D4`.
- [x] 2.3 Los once ternarios de `Formatos`, `LogoMarco`, `ConfiguracionController` y `PreviaCabecera`, según `design.md - D4`.
- [x] 2.4 El `grep` de ternarios de `design.md - D4` solo devuelve el paquete `pdf`. El `grep` de estilo de siempre (`record`, `var`, `::`, `.stream()`, clases anónimas, nombres completos) sobre los ficheros tocados: nada nuevo (el `var` de `LogoMarco.java` línea 65 ya estaba antes de este change, fuera de lo que pedía `design.md`).
- [x] 2.5 `LogoMarco` según `design.md - D6`: `TipoFondoLogo`, `FondoLogo` y `MuestrasMarco` en su fichero, sin `var` ni `computeIfAbsent` ni `removeIf`; `LogoMarcoTest` adaptado.
- [x] 2.6 `CeldaFechaEjercicio` y `ArranqueController.restringirAlEjercicio` según `design.md - D6`.
- [x] 2.7 `ConfiguracionController.cambiarTema`, `Ventanas` y `Formatos.fechaHora` según `design.md - D6`.
- [x] 2.8 `AGENTS.md`, «Transición», según `design.md - D6`. Los comandos de D6 sobre todo `src/main` salvo `pdf`:
  ```
  === grep 1: var/record/stream/:: ===
  src/main/java/cabofactu/modelo/negocio/CopiaSeguridad.java:194:  jdbc:sqlite::memory: (excepción escrita)
  src/main/java/cabofactu/vista/controlador/FichaClienteController.java:156:  falso positivo del patrón `var [a-zA-Z]` sobre el Javadoc («...llevar su id y vuelve...», "var s" cae dentro de "llevar su"); no hay ningún var real. No es un caso de los escritos en AGENTS.md; se deja así por ser una coincidencia de texto, no código.
  === grep 2: ternarios ===
  (nada)
  === grep 3: tipos anidados ===
  src/main/java/cabofactu/vista/controlador/EditorController.java: las 5 celdas de la tabla de líneas (CeldaCantidad, CeldaPrecio, CeldaTotal, CeldaDescripcion, CeldaIva) — excepción escrita.
  === grep 4: new X(){ / removeIf / computeIfAbsent / lambda de bloque ===
  src/main/java/cabofactu/modelo/negocio/sqlite/Conexion.java:82: new File(...) dentro de un if — excepción escrita.
  src/main/java/cabofactu/vista/controlador/BarraNavegacionController.java:72: case X -> { — excepción escrita.
  ```
  Todo lo que sale son las excepciones ya escritas en `AGENTS.md`. El falso positivo de `FichaClienteController` venía de que al comando le faltaba `` delante de `var` y `record`; en la revisión se añadió (y `abstract` en el patrón de tipos anidados), y la sesión principal repitió los cuatro comandos: solo salen las excepciones.

## 3. Imágenes (sesión principal)

- [x] 3.1 `docs/capturas/paletas.png` según `design.md - D3`.
  - Hecha por la sesión principal: 980×636, 33 KB, colores leídos del `.root` de cada CSS.
- [x] 3.2 Las siete `docs/capturas/proceso-*.png` según `design.md - D3`, aprobadas por el alumno.
  - Hechas por la sesión principal: 41 maquetas recuperadas del historial de opencode, anonimizadas fuera del proyecto y capturadas con Edge headless. Entre 35 y 220 KB cada una.

## 4. Documento de diseño

- [x] 4.1 Nuevo `docs/diseno.md` con los cuatro apartados de `design.md - D2`, con las ocho imágenes y los enlaces a `capturas/pdf.png` y `capturas/editor.png`. La tabla de changes que citan su maqueta, sacada del `grep` de D2.

## 5. Enlaces y normas

- [x] 5.1 `README.md`, `docs/tecnico.md`, `docs/metodologia.md`, `docs/flujos.md` y `AGENTS.md` según `design.md - D5`.
- [x] 5.2 Retoques de la revisión:
  - `README.md`, fila «Apariencia»: los nombres visibles de los temas («Biblioteca8, Omarchy, Esmeralda, Terracota, Negro y dorado, Sakura y Neón»);
  - `docs/diseno.md`, apartado «El resultado»: un solo párrafo para «Cómo funcionan los temas» (hoy hay un «Cómo funciona:» repetido justo debajo);
  - `docs/metodologia.md`: la cifra de pruebas («469») pasa a la real de la 6.1 (475).

## 6. Repaso

- [x] 6.1 `rm -rf target` y `mvn test` completo: todo en verde. Apuntar aquí pruebas y tiempo, y poner la cifra en el `README.md`.
  - Primera pasada: 475 pruebas, 0 fallos, 0 errores, en 7 min 41 s.
  - Segunda pasada (tras 2.3 y 5.2): 475 pruebas, 0 fallos, 0 errores, en 7 min 44 s. Sin `ClassCastException` en la salida. La cifra no cambió; ya estaba puesta en `README.md` y `docs/metodologia.md`.
  - Tercera pasada (tras 2.5 a 2.8): 475 pruebas, 0 fallos, 0 errores, en 24 min 35 s (máquina más cargada que en las pasadas anteriores; sin relación con el código). Sin `ClassCastException` en la salida. Cifra sin cambios.
- [x] 6.2 Cada enlace relativo de `docs/diseno.md` y del `README.md` apunta a un fichero que existe.
- [x] 6.3 `grep -rniE "alcazab|martag|mulian|aurora" docs/ README.md`: nada.
- [x] 6.4 `openspec validate documentacion-diseno --strict` en verde.

## 7. Revisión del alumno (al final)

- [ ] 7.1 El alumno lee `docs/diseno.md` y la sección «Diseño» del `README.md` en GitHub, con los diagramas y las imágenes.
- [ ] 7.2 En la aplicación, el desplegable de temas dice «Neón». Cambiar el tema en Configuración y guardar: se guarda sin avisos, y al volver a abrir la empresa sale el tema elegido.
