> Todo el contenido y su orden está en `design.md`. **No se toca el código ni `openspec/specs/`.** Antes de escribir un nombre de clase, método, fichero o cifra, se comprueba en el repositorio (`grep`, `ls`, `git log`). Estilo sobrio: sin emojis en títulos ni tablas. Diagramas en Mermaid válidos para GitHub. Commits **sin líneas de coautoría**.

## 1. Metodología

- [ ] 1.1 Leer `.opencode/commands/opsx-*.md` y las skills `openspec-*` de `.claude/skills/` para describir cada comando tal como es.
- [ ] 1.2 Reescribir `docs/metodologia.md` con las nueve secciones de `design.md - D3`, incluido el diagrama del flujo completo de un change y el `git log` real de `modulo-copias`.

## 2. Técnico

- [ ] 2.1 Reescribir `docs/tecnico.md` con las siete secciones de `design.md - D4`: el árbol de paquetes sacado del disco, las tablas sacadas de `crear_tablas.sql` y las cifras de pruebas reales.

## 3. Flujos

- [ ] 3.1 Nuevo `docs/flujos.md` con los dos recorridos de `design.md - D5`. Cada método nombrado, comprobado con `grep` en el código de hoy.

## 4. README

- [ ] 4.1 Reescribir `README.md` según `design.md - D2`, con la tabla de siete capturas (`docs/capturas/copias.png` y `docs/capturas/pdf.png` pueden no existir todavía: las hace la sección 6).

## 5. Normas y configuración

- [ ] 5.1 `AGENTS.md`: flujo con los comandos de opencode y de Claude Code, y `docs/flujos.md` en el mapa de la documentación. Ver `design.md - D7`.
- [ ] 5.2 `openspec/config.yaml`: la guía de `archive` sin `git add -A`. Ver `design.md - D7`.
- [ ] 5.3 Añadir este change a «En curso» en `ESTADO.md`.

## 6. Capturas (sesión principal, con Computer use)

- [ ] 6.1 Rehacer `arranque.png`, `menu.png`, `editor.png`, `historico.png` y `clientes.png` y añadir `copias.png` y `pdf.png` en `docs/capturas/`, con la demo y el tema biblioteca8. Ver `design.md - D6`.

## 7. Repaso

- [ ] 7.1 Todos los enlaces relativos entre los cuatro documentos, a las capturas y a los ficheros del código existen (comprobarlo con un script).
- [ ] 7.2 `grep` en los cuatro documentos: ni `DAO`, ni `Versión`/`versiones` de factura, ni `Clock`, ni `DatosException`, ni `record`, ni `Launcher`, salvo en la historia del proyecto y en las decisiones descartadas.
- [ ] 7.3 Ningún emoji en títulos ni tablas.
- [ ] 7.4 Apuntar aquí las líneas de cada documento.

## 8. Revisión del alumno

- [ ] 8.1 Leer los cuatro documentos en GitHub (en una rama o tras el push) y comprobar que los diagramas se dibujan.
- [ ] 8.2 Comprobar que lo que se cuenta del flujo de trabajo es como se ha trabajado de verdad.
