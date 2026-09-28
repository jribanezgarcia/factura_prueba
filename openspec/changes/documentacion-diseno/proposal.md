## Why

La documentación cuenta qué hace la aplicación, cómo se ha desarrollado y cómo recorre el código una operación, pero no dice nada de su aspecto. Solo aparece una línea en el `README.md` («7 temas: …»). No explica cómo están hechos los temas, de dónde sale su estructura, qué colores tiene cada uno, cómo se añade uno nuevo ni cómo se comprueba que ninguno se rompe. El profesor ve las capturas, pero no el trabajo de diseño que hay detrás.

Al preparar el documento han salido además tres cosas que hay que arreglar antes de enseñarlo:

- **`GestorTemas` no cumple `AGENTS.md`**, y es la clase que el documento va a explicar:
  - tiene un operador ternario;
  - tiene un `catch (Exception ignored) {}` vacío que se traga el error al guardar el tema;
  - seis métodos públicos no llevan Javadoc;
  - el Javadoc de la clase dice que el tema se guarda en «la tabla de preferencias», cuando se guarda en cada empresa.
- **Quedan dos ternarios más fuera del paquete `pdf`**: `Clientes.listado` y `PreferenciasGlobales.get`. También hay una falta de tilde en el Javadoc de `ConfiguracionVentana`.
- **`openspec/config.yaml` pierde las guías de archivado**: una de ellas lleva «rutas: ESTADO.md…», YAML la lee como un par clave-valor y OpenSpec avisa «Guidance for operation 'archive' must be an array of strings, ignoring this operation's guidance».

## What Changes

- **`docs/diseno.md` (nuevo)**, con:
  - la idea y cómo nació, con los changes de diseño por fases;
  - cómo funcionan los temas, con un diagrama y los pasos para añadir uno;
  - las siete paletas;
  - letra y tamaños;
  - las piezas repetidas de las pantallas, los iconos y los tamaños de ventana;
  - el diseño del PDF;
  - cómo se vigila el aspecto (pruebas y errores ya corregidos).
- **Imagen de las paletas** en `docs/capturas/paletas.png`: los siete temas con cuatro muestras de color cada uno. **Capturas de pantalla, ninguna nueva**: las de `docs/capturas/` ya están hechas con biblioteca8, el tema principal.
- **`README.md`**: sección corta «Diseño», con la imagen de las paletas y el enlace, y «Diseño» en la barra de enlaces.
- **Enlaces**: `docs/diseno.md` en la cabecera y el pie de los otros documentos y en el mapa de `AGENTS.md`.
- **Código, sin cambiar lo que hace la aplicación**:
  - `GestorTemas` cumple las normas y `guardar()` deja pasar el error a la pantalla;
  - fuera los ternarios de `Clientes` y `PreferenciasGlobales`;
  - tilde en `ConfiguracionVentana`.
- **`openspec/config.yaml`**: la guía de archivado, entre comillas, para que OpenSpec vuelva a leerla.

## Capacidades

### Capacidades nuevas

Ninguna.

### Capacidades modificadas

Ninguna: no cambia el comportamiento (`skip_specs: true`).

## A qué afecta

- **Nuevo**: `docs/diseno.md` y `docs/capturas/paletas.png`.
- **Cambian**:
  - `README.md`, `docs/tecnico.md`, `docs/metodologia.md` y `docs/flujos.md` (los enlaces entre documentos; en `tecnico.md`, además, el apartado de temas remite a `diseno.md`);
  - `AGENTS.md` (mapa de la documentación);
  - `openspec/config.yaml` y `ESTADO.md`.
- **Código**: `vista/utilidades/GestorTemas.java`, `vista/ConfiguracionVentana.java`, `modelo/negocio/Clientes.java` y `modelo/negocio/PreferenciasGlobales.java`.
- **Queda fuera**:
  - cambiar colores, temas o pantallas;
  - capturas en otros temas;
  - el paquete `pdf`, que se rehará con Jasper.
