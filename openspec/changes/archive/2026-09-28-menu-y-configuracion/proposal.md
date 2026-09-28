## Why

**En el menú principal, las opciones se ven como botones con borde y degradado gris.** Antes no pasaba.
- El 26/09 (`modulo-historico`, commit `708ce66`), la clase de estilo `menu-item` pasó a llamarse `opcion-menu`, porque `menu-item` es un nombre que JavaFX ya usa y dejaba ilegible el menú del clic derecho.
- Pero ese estilo de JavaFX era también el que quitaba el fondo y el borde de nuestras opciones. Con el nombre nuevo, cada opción volvió al aspecto normal de un botón.
- Ninguna prueba lo detectó, porque ninguna mira el fondo de un botón.

**La pantalla de Configuración mezcla cosas en la misma sección y no explica ninguna:**
- el logo está en «Cabecera y pie», que es del PDF;
- el color del PDF y el tema de la aplicación comparten «PDF y apariencia»;
- las secciones no tienen título ni ayuda, y la barra lateral no lleva iconos.

El alumno ha preparado en Figma una propuesta de cómo debería verse (`prototipos/figma/`, fuera de git): iconos pequeños en la barra lateral, título en cada sección y la sección elegida bien marcada.

## What Changes

- **Menú principal**: las opciones sin fondo ni borde. Al pasar el ratón (o con el foco del teclado), el icono crece un poco y el nombre se resalta en el color de acento, subrayado. Una prueba nueva comprueba que las opciones no tienen fondo ni borde.
- **Configuración, reorganizada en 4 grupos y 8 secciones**, cada una con una sola cosa:
  - DATOS DE LA EMPRESA: Datos fiscales · Logotipo (con vista previa y un botón nuevo, «Quitar logotipo»);
  - FISCALIDAD: Tipos de IVA · Retenciones IRPF · Series de numeración;
  - FACTURA EN PDF: Diseño del PDF (cabecera, pie legal, color de acento y vista previa);
  - PREFERENCIAS: Apariencia (tema) · Carpetas.
- **Iconos de 16 px** (SVG de Material) en la barra lateral y junto al título de cada sección. La sección elegida se marca con un fondo suave, una raya de acento a la izquierda y el color de acento, **sin negrita** (la norma de la especificación la reserva a los importes).
- **Cada sección empieza por su icono, su título y una frase de ayuda**, elegidas con el alumno.
- **Textos más profesionales**, elegidos uno a uno con el alumno: etiquetas de los campos, columnas, el botón «Guardar cambios» y los puntos suspensivos como un solo carácter (…) en todos los botones y textos de ayuda que los llevan.
- **Especificación**:
  - `invoicing`: se modifican «Configuración», «Configuración organizada por secciones» y «Datos obligatorios de la empresa»;
  - `pdf-rendering`: «Vista previa de la cabecera del PDF» (sin los campos de tamaño, que ya no existen) y «Aviso de logo de baja resolución».
- **Documentación**:
  - `docs/diseno.md`, ronda 8 con la propuesta de Figma;
  - capturas `menu.png` rehecha y `configuracion.png` nueva.

## Capacidades

### Capacidades nuevas

Ninguna.

### Capacidades modificadas

- `invoicing`: «Configuración», «Configuración organizada por secciones», «Datos obligatorios de la empresa».
- `pdf-rendering`: «Vista previa de la cabecera del PDF», «Aviso de logo de baja resolución».

## A qué afecta

- **CSS**: `base.css` y los siete `tema-*.css`.
- **FXML**: `Configuracion.fxml` (reorganizado) y los botones con «...» de `Arranque.fxml`, `Clientes.fxml` y `CopiaSeguridad.fxml`.
- **Código**: `ConfiguracionController` (secciones nuevas, vista previa del logotipo, «Quitar logotipo», textos).
- **Tests**:
  - `PantallaConfiguracionTest`, `PantallaMenuPrincipalTest`, `PantallaSeriesTest`, `PantallaArranqueTest` y `TextosCompletosTest` (ocho secciones);
  - los que busquen los textos cambiados.
- **Documentación**: `docs/diseno.md`, `README.md` (captura), `docs/capturas/`, `ESTADO.md`.
- **Queda fuera**:
  - lo que la propuesta de Figma tiene y la aplicación no (Usuarios, Preferencias de facturación);
  - cambiar el comportamiento de guardar;
  - el paquete `pdf`.
