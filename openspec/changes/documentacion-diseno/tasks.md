> Todo el contenido y su orden está en `design.md`. Antes de escribir un nombre de clase, método, fichero, tamaño o color, se comprueba en el repositorio (`grep`, `ls`, `git log`). De las maquetas, solo lo que dice `design.md - D2`: no se inventan motivos. Estilo sobrio: sin emojis en títulos ni tablas. Commits **sin líneas de coautoría**.

## 1. Configuración y estado

- [x] 1.1 `openspec/config.yaml`: la guía de `archive` con «rutas: », entre comillas dobles. `openspec status --change documentacion-diseno` ya no da el aviso «Guidance for operation 'archive'…». Ver `design.md - D5`.
- [x] 1.2 Añadir este change a «En curso» en `ESTADO.md`.

## 2. Código

- [x] 2.1 `GestorTemas` según `design.md - D4`: sin ternario, `guardar()` con `throws Exception` y sin el `catch` vacío, Javadoc de la clase y de los públicos, y «Neón».
- [x] 2.2 Tildes del Javadoc de `ConfiguracionVentana`, y los ternarios de `Clientes.listado` y `PreferenciasGlobales.get`, según `design.md - D4`.
- [ ] 2.3 El `grep` de ternarios de `design.md - D4` **no** solo devuelve el paquete `pdf`: además de `pdf/`, hay ternarios previos (no tocados por este change) en `Formatos.java`, `LogoMarco.java`, `ConfiguracionController.java` y `PreviaCabecera.java`. Sin marcar; ver informe. El `grep` de estilo de siempre (`record`, `var`, `::`, `.stream()`, clases anónimas, nombres completos) sobre los ficheros tocados: nada nuevo.

## 3. Imágenes (sesión principal)

- [x] 3.1 `docs/capturas/paletas.png` según `design.md - D3`.
  - Hecha por la sesión principal: 980×636, 33 KB, colores leídos del `.root` de cada CSS.
- [x] 3.2 Las siete `docs/capturas/proceso-*.png` según `design.md - D3`, aprobadas por el alumno.
  - Hechas por la sesión principal: 41 maquetas recuperadas del historial de opencode, anonimizadas fuera del proyecto y capturadas con Edge headless. Entre 35 y 220 KB cada una.

## 4. Documento de diseño

- [ ] 4.1 Nuevo `docs/diseno.md` con los cuatro apartados de `design.md - D2`, con las ocho imágenes y los enlaces a `capturas/pdf.png` y `capturas/editor.png`. La tabla de changes que citan su maqueta, sacada del `grep` de D2.

## 5. Enlaces y normas

- [ ] 5.1 `README.md`, `docs/tecnico.md`, `docs/metodologia.md`, `docs/flujos.md` y `AGENTS.md` según `design.md - D5`.

## 6. Repaso

- [ ] 6.1 `rm -rf target` y `mvn test` completo: todo en verde. Apuntar aquí pruebas y tiempo, y poner la cifra en el `README.md`.
- [ ] 6.2 Cada enlace relativo de `docs/diseno.md` y del `README.md` apunta a un fichero que existe.
- [ ] 6.3 `grep -rniE "alcazab|martag|mulian|aurora" docs/ README.md`: nada.
- [ ] 6.4 `openspec validate documentacion-diseno --strict` en verde.

## 7. Revisión del alumno (al final)

- [ ] 7.1 El alumno lee `docs/diseno.md` y la sección «Diseño» del `README.md` en GitHub, con los diagramas y las imágenes.
- [ ] 7.2 En la aplicación, el desplegable de temas dice «Neón». Cambiar el tema en Configuración y guardar: se guarda sin avisos, y al volver a abrir la empresa sale el tema elegido.
