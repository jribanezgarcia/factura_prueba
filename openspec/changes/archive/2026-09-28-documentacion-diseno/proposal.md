## Why

La documentación cuenta qué hace la aplicación, cómo se ha desarrollado y cómo recorre el código una operación, pero no dice nada de cómo se diseñó su aspecto. Ese trabajo se hizo con maquetas: 41 maquetas HTML entre el 20/08 y el 07/09. En cada ronda el alumno eligió, corrigió y descartó, y solo lo aprobado se programó. Para hacerlas se creó una skill propia (`javafx-design`) y se aplicó `apple-design`, de Emil Kowalski.

De todo eso no queda nada visible: `prototipos/` está fuera de git porque lleva datos reales de la empresa. Tampoco se explica por qué los iconos son SVG ni cómo están hechos los temas. El profesor ve las capturas, pero no el trabajo de diseño que hay detrás.

Al preparar el documento han salido además tres cosas que hay que arreglar antes de enseñarlo:

- **`GestorTemas` no cumple `AGENTS.md`**, y es la clase que el documento va a explicar:
  - tiene un operador ternario;
  - tiene un `catch (Exception ignored) {}` vacío que se traga el error al guardar el tema;
  - seis métodos públicos no llevan Javadoc;
  - el Javadoc de la clase dice que el tema se guarda en «la tabla de preferencias», cuando se guarda en cada empresa.
- **Quedan dos ternarios más fuera del paquete `pdf`**: `Clientes.listado` y `PreferenciasGlobales.get`. También hay una falta de tilde en el Javadoc de `ConfiguracionVentana`.
- **`openspec/config.yaml` pierde las guías de archivado**: una de ellas lleva «rutas: ESTADO.md…», YAML la lee como un par clave-valor y OpenSpec avisa «Guidance for operation 'archive' must be an array of strings, ignoring this operation's guidance».

## What Changes

- **`docs/diseno.md` (nuevo)**, con el proceso primero:
  - cómo se trabajó el diseño: maquetas, decisiones del alumno, las skills y la relación maqueta → change;
  - las siete rondas de maquetas, con lo elegido y lo descartado: temas, la propuesta elegida, menú e iconos, PDF, estructura Apple, editor y totales del PDF;
  - los iconos: por qué SVG, de dónde salen y cómo están en el código;
  - al final, en corto, las paletas, cómo funcionan los temas y cómo se vigila el aspecto.
- **Ocho imágenes nuevas** en `docs/capturas/`:
  - `paletas.png`;
  - siete `proceso-*.png`, con las maquetas anonimizadas con los datos de la demo.
  - Las capturas de la aplicación no cambian: ya están con biblioteca8.
- **`README.md`**: sección corta «Diseño», con la imagen de las paletas y el enlace, y «Diseño» en la barra de enlaces.
- **Enlaces**: `docs/diseno.md` en la cabecera y el pie de los otros documentos y en el mapa de `AGENTS.md`.
- **Código, sin cambiar lo que hace la aplicación**:
  - `GestorTemas` cumple las normas y `guardar()` deja pasar el error a la pantalla;
  - el tema «Neon» se enseña como «Neón» (la clave guardada no cambia);
  - fuera los ternarios de `Clientes` y `PreferenciasGlobales`;
  - tilde en `ConfiguracionVentana`.
- **`openspec/config.yaml`**: la guía de archivado, entre comillas, para que OpenSpec vuelva a leerla.

## Capacidades

### Capacidades nuevas

Ninguna.

### Capacidades modificadas

Ninguna: no cambia el comportamiento (`skip_specs: true`).

## A qué afecta

- **Nuevo**: `docs/diseno.md`, `docs/capturas/paletas.png` y `docs/capturas/proceso-*.png`.
- **Cambian**:
  - `README.md`, `docs/tecnico.md`, `docs/metodologia.md` y `docs/flujos.md` (los enlaces entre documentos; en `tecnico.md`, además, el apartado de temas remite a `diseno.md`);
  - `AGENTS.md` (mapa de la documentación);
  - `openspec/config.yaml` y `ESTADO.md`.
- **Código**: `vista/utilidades/GestorTemas.java`, `vista/ConfiguracionVentana.java`, `modelo/negocio/Clientes.java` y `modelo/negocio/PreferenciasGlobales.java`.
- **Queda fuera**:
  - subir los prototipos;
  - cambiar colores, temas o pantallas;
  - el paquete `pdf`, que se rehará con Jasper.
