> Todo el contenido y su orden está en `design.md`. Antes de escribir un nombre de clase, método, fichero, tamaño o color, se comprueba en el repositorio (`grep`, `ls`, `git log`). Estilo sobrio: sin emojis en títulos ni tablas. Commits **sin líneas de coautoría**.

## 1. Configuración y estado

- [ ] 1.1 `openspec/config.yaml`: la guía de `archive` con «rutas: », entre comillas dobles. `openspec status --change documentacion-diseno` ya no da el aviso «Guidance for operation 'archive'…». Ver `design.md - D5`.
- [ ] 1.2 Añadir este change a «En curso» en `ESTADO.md`.

## 2. Código

- [ ] 2.1 `GestorTemas` según `design.md - D4`: sin ternario, `guardar()` con `throws Exception` y sin el `catch` vacío, Javadoc de la clase y de los públicos.
- [ ] 2.2 Tildes del Javadoc de `ConfiguracionVentana`, y los ternarios de `Clientes.listado` y `PreferenciasGlobales.get`, según `design.md - D4`.
- [ ] 2.3 El `grep` de ternarios de `design.md - D4` solo devuelve el paquete `pdf`. El `grep` de estilo de siempre (`record`, `var`, `::`, `.stream()`, clases anónimas, nombres completos) sobre los ficheros tocados: nada nuevo.

## 3. Imagen de las paletas (sesión principal)

- [x] 3.1 `docs/capturas/paletas.png` según `design.md - D3`.
  - Hecha por la sesión principal: 980×636, 33 KB, colores leídos del `.root` de cada CSS.

## 4. Documento de diseño

- [ ] 4.1 Nuevo `docs/diseno.md` con los siete apartados de `design.md - D2`. Cada clase CSS, tamaño, color y método nombrado, comprobado con `grep`. La tabla de fases, con la frase sacada del `## Why` de cada change.

## 5. Enlaces y normas

- [ ] 5.1 `README.md`, `docs/tecnico.md`, `docs/metodologia.md`, `docs/flujos.md` y `AGENTS.md` según `design.md - D5`.

## 6. Repaso

- [ ] 6.1 `rm -rf target` y `mvn test` completo: todo en verde. Apuntar aquí pruebas y tiempo, y poner la cifra en el `README.md`.
- [ ] 6.2 Cada enlace relativo de `docs/diseno.md` y del `README.md` apunta a un fichero que existe.
- [ ] 6.3 `openspec validate documentacion-diseno --strict` en verde.

## 7. Revisión del alumno (al final)

- [ ] 7.1 El alumno lee `docs/diseno.md` y la sección «Diseño» del `README.md` en GitHub, con los diagramas y la imagen de las paletas.
- [ ] 7.2 En la aplicación, cambiar el tema en Configuración y guardar: se guarda sin avisos, y al volver a abrir la empresa sale el tema elegido.
