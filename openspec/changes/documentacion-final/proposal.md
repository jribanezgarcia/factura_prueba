## Why

La reescritura módulo a módulo ha terminado, pero la documentación cuenta el proyecto de antes:

| Documento | Qué ya no es verdad |
|---|---|
| `README.md` | Versiones de factura, capas con DAO, `Clock`, `DatosException`, «más de 200 tests» (hoy 469), próximos pasos antiguos y capturas de pantallas que ya han cambiado |
| `docs/tecnico.md` | Capas con DAO y servicios, `Modelo` que reparte objetos por constructor, `Launcher`/`Main`, el paquete `fichero`, `record`, la tabla de versiones y el ejemplo «guardar una factura» con versiones |
| `docs/metodologia.md` | No explica qué hace cada comando de OpenSpec ni qué ficheros crea, no dice nada de commits ni ramas, el reparto de trabajo es el de antes y el ejemplo (`reloj-inyectable`) explica un `Clock` que ya no existe |

Y falta lo que el profesor tiene que poder ver: cómo se ha desarrollado el proyecto con OpenSpec, paso a paso, y cómo recorre el código una operación real.

## What Changes

- **Cuatro documentos**:
  - `README.md`: presentación corta, capturas nuevas y enlaces;
  - `docs/metodologia.md`: cómo se ha hecho;
  - `docs/tecnico.md`: cómo está construido;
  - `docs/flujos.md` (nuevo): dos recorridos por el código, clase a clase.
- **Metodología**:
  - qué hace cada comando de OpenSpec y qué ficheros crea;
  - un diagrama con el flujo completo de un change;
  - quién hace qué, con las herramientas de IA contadas con detalle y las decisiones y la revisión del alumno;
  - los commits de cada paso y el push al archivar;
  - las ramas con pull request;
  - `modulo-copias` como ejemplo real;
  - la historia del proyecto por fases.
- **Técnico**: la arquitectura MVC de hoy (singletons con su SQL, sin DAO), los paquetes, las tablas sin versiones y las decisiones técnicas al día.
- **Flujos**: el primer arranque, clase a clase, y el camino de una factura nueva hasta exportarla a PDF. Se para en el PDF, porque irá con JasperReports.
- **Capturas nuevas** con la aplicación real: las cinco de hoy, más Copias y el PDF de una factura.
- **Estilo sobrio**: sin emojis en títulos ni tablas; se quedan las insignias de tecnologías y los diagramas en Mermaid, que GitHub dibuja solo.
- **`AGENTS.md` y `openspec/config.yaml`**: el flujo con los dos juegos de comandos (opencode y Claude Code), `docs/flujos.md` en el mapa de la documentación, y la guía de archivado sin `git add -A`.

## Capacidades

### Capacidades nuevas

Ninguna.

### Capacidades modificadas

Ninguna: solo cambia la documentación (`skip_specs: true` en `.openspec.yaml`).

## A qué afecta

- **Se reescriben**: `README.md`, `docs/metodologia.md` y `docs/tecnico.md`.
- **Nuevo**: `docs/flujos.md`.
- **Capturas**: `docs/capturas/` (las cinco de hoy rehechas, más `copias.png` y `pdf.png`).
- **Cambian**: `AGENTS.md` (flujo de trabajo y mapa de la documentación) y `openspec/config.yaml` (guía de archivado).
- **Queda fuera**: el código, las especificaciones, y documentar Jasper o VeriFactu, que se documentarán en sus ramas.
