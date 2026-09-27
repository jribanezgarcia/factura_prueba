## Why

La reescritura módulo a módulo ha terminado, pero la documentación cuenta el proyecto de antes:

| Documento | Qué ya no es verdad |
|---|---|
| `README.md` | Versiones de factura, capas con DAO, `Clock`, `DatosException`, «más de 200 tests» (hoy 469), próximos pasos antiguos y capturas de pantallas que ya han cambiado |
| `docs/tecnico.md` | Capas con DAO y servicios, `Modelo` que reparte objetos por constructor, `Launcher`/`Main`, el paquete `fichero`, `record`, la tabla de versiones y el ejemplo «guardar una factura» con versiones |
| `docs/metodologia.md` | No explica qué hace cada comando de OpenSpec ni qué ficheros crea, no dice nada de commits ni ramas, el reparto de trabajo es el de antes y el ejemplo (`reloj-inyectable`) explica un `Clock` que ya no existe |

Al revisar la documentación salieron además cuatro cosas que hay que arreglar antes de presentarla. Van en este mismo change, por decisión del alumno:

- **Faltas de ortografía**: cuatro títulos de ventana sin tilde («Seleccion de empresa», «Menu Principal», «Historico», «Configuracion»), que se ven en las capturas, y unas noventa líneas de comentarios sin tildes.
- **La demo no tiene logo**: no se puede ver cómo queda la aplicación con logo, ni en pantalla ni en el PDF.
- **La norma de excepciones es demasiado rígida** («solo `Exception`»), y aun así `CargarDemo` lanza `IllegalStateException`.
- **No hay un manejador global de errores**: un error que ninguna pantalla captura se queda en la consola y la pantalla puede quedar a medias sin avisar.

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
- **Ortografía**: los cuatro títulos con tilde, el escenario de la especificación que copiaba la falta, y los comentarios y Javadoc sin tildes.
- **La demo trae logo**: el logo de CaboFactu, reducido, dentro de la aplicación; al cargar la demo se copia a su carpeta de datos y la cabecera del PDF queda en modo logo.
- **Excepciones**: `Exception` por defecto; una propia solo si aporta algo; las de Java solo para errores de programación. `CargarDemo` pasa a `Exception`.
- **Errores inesperados**: un manejador global que avisa sin cerrar la aplicación y apunta cada error en `errores.log`.
- **Controlador y Modelo bien explicados**: la documentación dejaba entender que solo repiten métodos. Se cuenta para qué sirven, y las tres pantallas que se saltaban el `Controlador` pasan por él. `LanzadorVentanaPrincipal` queda más corto y parecido al de Biblioteca8.
- **`AGENTS.md` y `openspec/config.yaml`**: el flujo con los dos juegos de comandos (opencode y Claude Code), `docs/flujos.md` en el mapa de la documentación, y la guía de archivado sin `git add -A`.

## Capacidades

### Capacidades nuevas

Ninguna.

### Capacidades modificadas

- `invoicing`: requisito nuevo «Errores inesperados»; «Identidad de la aplicación en la interfaz» (título de arranque con tilde) y «Datos de demostración recreables» (la demo trae logo).

## A qué afecta

- **Se reescriben**: `README.md`, `docs/metodologia.md` y `docs/tecnico.md`.
- **Nuevo**: `docs/flujos.md`.
- **Capturas**: `docs/capturas/` (las cinco de hoy rehechas, más `copias.png` y `pdf.png`).
- **Cambian**: `AGENTS.md` (flujo de trabajo y mapa de la documentación) y `openspec/config.yaml` (guía de archivado).
- **Código**: `ConfiguracionVentana` (títulos), `CargarDemo` (logo y `Exception`), nuevo `vista/utilidades/ErroresInesperados.java`, `LanzadorVentanaPrincipal` y `AppCaboFactu` (registrar el manejador), el logo en `src/main/resources/db/logo_demo.png` y los comentarios sin tildes.
- **Tests**: `CargarDemoTest` y `ErroresInesperadosTest` (nuevo), y los que comprueben los títulos.
- **Queda fuera**: el paquete `pdf` (irá con Jasper), una excepción propia inventada sin necesidad, y documentar Jasper o VeriFactu, que se documentarán en sus ramas.
