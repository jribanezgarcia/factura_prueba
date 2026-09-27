> Todo el contenido y su orden está en `design.md`. **Las secciones 1 a 7 son solo documentación; las 9 a 14 tocan código (ver `design.md` D8 a D12).** Antes de escribir un nombre de clase, método, fichero o cifra, se comprueba en el repositorio (`grep`, `ls`, `git log`). Estilo sobrio: sin emojis en títulos ni tablas. Diagramas en Mermaid válidos para GitHub. Commits **sin líneas de coautoría**.

## 1. Metodología

- [x] 1.1 Leer `.opencode/commands/opsx-*.md` y las skills `openspec-*` de `.claude/skills/` para describir cada comando tal como es.
- [x] 1.2 Reescribir `docs/metodologia.md` con las nueve secciones de `design.md - D3`, incluido el diagrama del flujo completo de un change y el `git log` real de `modulo-copias`.

## 2. Técnico

- [x] 2.1 Reescribir `docs/tecnico.md` con las siete secciones de `design.md - D4`: el árbol de paquetes sacado del disco, las tablas sacadas de `crear_tablas.sql` y las cifras de pruebas reales.

## 3. Flujos

- [x] 3.1 Nuevo `docs/flujos.md` con los dos recorridos de `design.md - D5`. Cada método nombrado, comprobado con `grep` en el código de hoy.

## 4. README

- [x] 4.1 Reescribir `README.md` según `design.md - D2`, con la tabla de siete capturas (`docs/capturas/copias.png` y `docs/capturas/pdf.png` pueden no existir todavía: las hace la sección 6).

## 5. Normas y configuración

- [x] 5.1 `AGENTS.md`: flujo con los comandos de opencode y de Claude Code, y `docs/flujos.md` en el mapa de la documentación. Ver `design.md - D7`.
- [x] 5.2 `openspec/config.yaml`: la guía de `archive` sin `git add -A`. Ver `design.md - D7`.
- [x] 5.3 Añadir este change a «En curso» en `ESTADO.md`.

## 6. Capturas (sesión principal, con Computer use)

- [x] 6.1 Rehacer `arranque.png`, `menu.png`, `editor.png`, `historico.png` y `clientes.png` y añadir `copias.png` y `pdf.png` en `docs/capturas/`, con la demo y el tema biblioteca8. Ver `design.md - D6`.
  - Hechas por la sesión principal con Computer use sobre la aplicación real (demo y tema biblioteca8), recortando solo la ventana. `pdf.png` es la primera página del PDF de la factura A-1/9, convertida a imagen con Ghostscript. Al terminar, el tema volvió a omarchy y la última carpeta de exportación de la demo, a la de antes.

## 7. Repaso

- [x] 7.1 Todos los enlaces relativos entre los cuatro documentos, a las capturas y a los ficheros del código existen (comprobado con un script de `bash`/`grep` que resuelve cada `](...)` y cada `src="..."` contra el disco). Las siete capturas de `docs/capturas/` ya existían al comprobarlo, así que ningún enlace queda roto.
- [x] 7.2 `grep` en los cuatro documentos: sin `Clock`, sin `DatosException` fuera de la tabla de decisiones descartadas, sin `Launcher`. `DAO` y «versión/versiones» aparecen solo para decir que ya no existen (arquitectura sin DAO, sin versiones de factura) o en la historia del proyecto y las decisiones descartadas de `docs/tecnico.md` y `docs/metodologia.md`.
- [x] 7.3 Ningún emoji en títulos ni tablas (comprobado con `grep` sobre los rangos Unicode de emoji; solo aparecen flechas `→` y caracteres de árbol `├└│`, que no son emojis).
- [x] 7.4 Líneas de cada documento: `README.md` 128, `docs/metodologia.md` 208, `docs/tecnico.md` 239, `docs/flujos.md` 113 (688 en total).

## 8. Revisión del alumno (al final, después de la sección 16)

- [ ] 8.1 Leer los cuatro documentos en GitHub (en una rama o tras el push) y comprobar que los diagramas se dibujan.
- [ ] 8.2 Comprobar que lo que se cuenta del flujo de trabajo es como se ha trabajado de verdad.

## 9. Ortografía

- [x] 9.1 `ConfiguracionVentana`: los cuatro títulos con tilde, y los tests que los comparen, al día. Ver `design.md - D8`.
  - Ningún test compara esos títulos literalmente, así que no había ninguno que actualizar.
- [x] 9.2 Tildes en los comentarios y el Javadoc de `src/main/java` y `src/test/java` (fuera del paquete `pdf`), sin tocar ningún identificador. Apuntar aquí cuántas líneas se han corregido. Ver `design.md - D8`.
  - 46 líneas corregidas. No se han tocado `factura_linea`, `tipo_retencion` (identificadores de tabla partidos por el propio texto), `anio` ni `CODIGO`/`CORRELATIVO`/`ANIO`/`MES`/`NINGUNO` (notación de formato en mayúsculas de `Series.java`), ni `guion` (válido sin tilde en la ortografía actual).

## 10. Logo de la demo

- [x] 10.1 `src/main/resources/db/logo_demo.png`: el logo reducido a 600 px de ancho. Apuntar aquí su peso.
  - 600×400 px, 149,7 KB (153.302 bytes).
- [x] 10.2 `CargarDemo.cargar` copia el logo y pone `logo_path` y `cabecera_modo = 'LOGO'`. Ver `design.md - D9`.
- [x] 10.3 `CargarDemoTest` con los casos de `design.md - D9`.

## 11. Excepciones

- [x] 11.1 `AGENTS.md`: la norma nueva de excepciones. `CargarDemo`: `IllegalStateException` → `Exception`. Ver `design.md - D10`.

## 12. Errores inesperados

- [x] 12.1 Nuevo `vista/utilidades/ErroresInesperados.java`; registrarlo en `LanzadorVentanaPrincipal.start` y poner el `try / catch` de `AppCaboFactu.main`. Ver `design.md - D11`.
- [x] 12.2 `ErroresInesperadosTest` con los casos de `design.md - D11`, y la prueba de pantalla si es fiable (si no, apuntarlo aquí).
  - Sin prueba de pantalla: `PruebaDePantalla` es de paquete (`cabofactu.vista`) y monta la empresa de demostración entera; forzar una excepción no capturada dentro de un botón real en TestFX headless, con los problemas ya conocidos de esta batería con los diálogos, no era fiable. Queda como prueba manual (tarea 15).
- [x] 12.3 En los ficheros de código tocados, `grep -nE "\bvar \b|\.stream\(\)|::|record |\? .*: |new [A-Za-z<>]+\([^)]*\) *\{|instanceof [A-Za-z<>?]+ [a-z]|javafx\.[a-z]+\.[A-Z]|java\.[a-z]+\.[a-z]+\.[A-Z]|new Thread|Task<|static class|enum [A-Z]"` sin contar las líneas `import`: nada nuevo. Borrar `target` y `mvn test` completo: todo en verde. Apuntar aquí pruebas y tiempo.
  - Los únicos avisos del grep son de código ya existente en los ficheros solo tocados por comentarios (ternarios, `var`, el propio `enum ConfiguracionVentana`...); ninguno en las líneas que he escrito o cambiado. `mvn test` completo tras borrar `target`: **475 pruebas, 0 fallos, 0 errores, 7:16 min**, sin ningún `ClassCastException` en el log.

## 13. Documentos al día

- [x] 13.1 `docs/tecnico.md`, `docs/flujos.md` y `README.md` según `design.md - D12`.
- [x] 13.2 `openspec validate documentacion-final --strict` sin errores. Actualizar la línea del change en «En curso» de `ESTADO.md`.

## 14. Capturas otra vez (sesión principal, con Computer use)

- [x] 14.1 Recargar la demo para que traiga el logo y rehacer las siete capturas con los títulos corregidos. Ver `design.md - D12`.
  - Hechas con Computer use: la demo se recreó desde cero (la anterior quedó apartada fuera de la carpeta de datos) y trae el logo en el menú, el editor y el PDF; los títulos salen con tilde. Al terminar, la demo nueva quedó con el tema omarchy y la última carpeta de exportación de antes.

## 15. Pruebas manuales

- [x] 15.1 La ventana de arranque se titula «CaboFactu® Selección de empresa», y el menú, el histórico y la configuración llevan sus tildes.
- [x] 15.2 Con la demo recién cargada, el menú y el editor enseñan el logo, y el PDF de una factura lo lleva a la izquierda, con los datos de la empresa a la derecha.
  - Comprobadas con Computer use al hacer las capturas de la 14.1 (títulos en la barra de cada ventana; logo en menú, editor y PDF).

## 16. Controlador, Modelo y arranque

- [x] 16.1 `LanzadorVentanaPrincipal.start` como en `design.md - D13`, con `Vista.mostrarArranque` y `Vista.cargarDemostracion` nuevos, sin `setOnShown` ni la variable copiada.
- [x] 16.2 Las tres fugas de `design.md - D14`: `ultimaEmpresa`, `esEmpresaDemo`, `carpetaEmpresa` y `carpetaDatosEmpresa` en `Controlador` y `Modelo`, y `Empresas.ultima()`. Comprobar con el `grep` de D14 que las pantallas solo importan `Calculos` de `modelo/negocio`.
- [x] 16.3 Javadoc de `Controlador` y `Modelo`, y `AGENTS.md`, según `design.md - D15`.
- [x] 16.4 `docs/tecnico.md`, `README.md` y `docs/flujos.md` según `design.md - D15`, incluido el apartado nuevo «Para qué sirven el `Controlador` y el `Modelo`». `grep -rn -i "repite\|repetir\|reenv" README.md docs/ AGENTS.md`: no queda nada que describa el `Controlador` o el `Modelo` así.
- [x] 16.5 El `grep` de estilo de la 12.3 sobre los ficheros tocados: nada nuevo. `rm -rf target` y `mvn test` completo: todo en verde. 475 pruebas, 0 fallos, 7:45 min.
- [x] 16.6 Prueba manual: arrancar la aplicación borrando antes la carpeta de la demo. Sale el aviso de bienvenida encima de la ventana de arranque y la demo queda elegida. Sin borrar nada, arranca sin aviso.
  - Comprobada con Computer use: con la demo apartada, sale «Bienvenido» encima de la ventana de arranque y la demo queda elegida; con la demo en su sitio, arranca sin aviso. La demo del usuario quedó como estaba.
